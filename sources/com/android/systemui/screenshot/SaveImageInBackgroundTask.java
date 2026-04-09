package com.android.systemui.screenshot;

import android.app.ActivityTaskManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.DeviceConfig;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.R;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.screenshot.GlobalScreenshot;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/* loaded from: classes.dex */
class SaveImageInBackgroundTask extends AsyncTask<Void, Void, Void> {
    private final Context mContext;
    private final String mImageFileName;
    private final long mImageTime;
    private final GlobalScreenshot.SaveImageInBackgroundData mParams;
    private final String mScreenshotId;
    private final ScreenshotSmartActions mScreenshotSmartActions;
    private final boolean mSmartActionsEnabled;
    private final ScreenshotNotificationSmartActionsProvider mSmartActionsProvider;
    private final Random mRandom = new Random();
    private final GlobalScreenshot.SavedImageData mImageData = new GlobalScreenshot.SavedImageData();

    SaveImageInBackgroundTask(Context context, ScreenshotSmartActions screenshotSmartActions, GlobalScreenshot.SaveImageInBackgroundData saveImageInBackgroundData) {
        String str;
        this.mContext = context;
        this.mScreenshotSmartActions = screenshotSmartActions;
        this.mParams = saveImageInBackgroundData;
        long jCurrentTimeMillis = System.currentTimeMillis();
        this.mImageTime = jCurrentTimeMillis;
        String str2 = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date(jCurrentTimeMillis));
        if (!((KeyguardManager) context.getSystemService(KeyguardManager.class)).isKeyguardLocked() && (str = saveImageInBackgroundData.appLabel) != null) {
            this.mImageFileName = String.format("Screenshot_%s_%s.png", str2, str.toString().replaceAll("[\\\\/:*?\"<>|\\s]+", "_"));
        } else {
            this.mImageFileName = String.format("Screenshot_%s.png", str2);
        }
        this.mScreenshotId = String.format("Screenshot_%s", UUID.randomUUID());
        boolean z = DeviceConfig.getBoolean("systemui", "enable_screenshot_notification_smart_actions", true);
        this.mSmartActionsEnabled = z;
        if (z) {
            this.mSmartActionsProvider = SystemUIFactory.getInstance().createScreenshotNotificationSmartActionsProvider(context, AsyncTask.THREAD_POOL_EXECUTOR, new Handler());
        } else {
            this.mSmartActionsProvider = new ScreenshotNotificationSmartActionsProvider();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public Void doInBackground(Void... voidArr) throws Exception {
        ContentValues contentValues;
        Uri uriInsert;
        CompletableFuture<List<Notification.Action>> smartActionsFuture;
        if (isCancelled()) {
            return null;
        }
        Thread.currentThread().setPriority(10);
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Bitmap bitmap = this.mParams.image;
        this.mContext.getResources();
        try {
            contentValues = new ContentValues();
            contentValues.put("relative_path", Environment.DIRECTORY_PICTURES + File.separator + Environment.DIRECTORY_SCREENSHOTS);
            contentValues.put("_display_name", this.mImageFileName);
            contentValues.put("mime_type", "image/png");
            contentValues.put("date_added", Long.valueOf(this.mImageTime / 1000));
            contentValues.put("date_modified", Long.valueOf(this.mImageTime / 1000));
            contentValues.put("date_expires", Long.valueOf((this.mImageTime + 86400000) / 1000));
            contentValues.put("is_pending", (Integer) 1);
            uriInsert = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            smartActionsFuture = this.mScreenshotSmartActions.getSmartActionsFuture(this.mScreenshotId, uriInsert, bitmap, this.mSmartActionsProvider, this.mSmartActionsEnabled, getUserHandle(this.mContext));
        } catch (Exception e) {
            Slog.e("SaveImageInBackgroundTask", "unable to save screenshot", e);
            this.mParams.clearImage();
            this.mParams.errorMsgResId = R.string.screenshot_failed_to_save_text;
            this.mImageData.reset();
            this.mParams.mActionsReadyListener.onActionsReady(this.mImageData);
            this.mParams.finisher.accept(null);
        }
        try {
            OutputStream outputStreamOpenOutputStream = contentResolver.openOutputStream(uriInsert);
            try {
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStreamOpenOutputStream)) {
                    throw new IOException("Failed to compress");
                }
                if (outputStreamOpenOutputStream != null) {
                    outputStreamOpenOutputStream.close();
                }
                ParcelFileDescriptor parcelFileDescriptorOpenFile = contentResolver.openFile(uriInsert, "rw", null);
                try {
                    ExifInterface exifInterface = new ExifInterface(parcelFileDescriptorOpenFile.getFileDescriptor());
                    exifInterface.setAttribute("ImageWidth", Integer.toString(bitmap.getWidth()));
                    exifInterface.setAttribute("ImageLength", Integer.toString(bitmap.getHeight()));
                    ZonedDateTime zonedDateTimeOfInstant = ZonedDateTime.ofInstant(Instant.ofEpochMilli(this.mImageTime), ZoneId.systemDefault());
                    exifInterface.setAttribute("DateTimeOriginal", DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss").format(zonedDateTimeOfInstant));
                    exifInterface.setAttribute("SubSecTimeOriginal", DateTimeFormatter.ofPattern("SSS").format(zonedDateTimeOfInstant));
                    if (Objects.equals(zonedDateTimeOfInstant.getOffset(), ZoneOffset.UTC)) {
                        exifInterface.setAttribute("OffsetTimeOriginal", "+00:00");
                    } else {
                        exifInterface.setAttribute("OffsetTimeOriginal", DateTimeFormatter.ofPattern("XXX").format(zonedDateTimeOfInstant));
                    }
                    exifInterface.saveAttributes();
                    parcelFileDescriptorOpenFile.close();
                    contentValues.clear();
                    contentValues.put("is_pending", (Integer) 0);
                    contentValues.putNull("date_expires");
                    contentResolver.update(uriInsert, contentValues, null, null);
                    ArrayList arrayList = new ArrayList();
                    if (this.mSmartActionsEnabled) {
                        arrayList.addAll(buildSmartActions(this.mScreenshotSmartActions.getSmartActions(this.mScreenshotId, smartActionsFuture, DeviceConfig.getInt("systemui", "screenshot_notification_smart_actions_timeout_ms", 1000), this.mSmartActionsProvider), this.mContext));
                    }
                    GlobalScreenshot.SavedImageData savedImageData = this.mImageData;
                    savedImageData.uri = uriInsert;
                    savedImageData.smartActions = arrayList;
                    Context context = this.mContext;
                    savedImageData.shareAction = createShareAction(context, context.getResources(), uriInsert);
                    GlobalScreenshot.SavedImageData savedImageData2 = this.mImageData;
                    Context context2 = this.mContext;
                    savedImageData2.editAction = createEditAction(context2, context2.getResources(), uriInsert);
                    GlobalScreenshot.SavedImageData savedImageData3 = this.mImageData;
                    Context context3 = this.mContext;
                    savedImageData3.deleteAction = createDeleteAction(context3, context3.getResources(), uriInsert);
                    this.mParams.mActionsReadyListener.onActionsReady(this.mImageData);
                    this.mParams.finisher.accept(this.mImageData.uri);
                    GlobalScreenshot.SaveImageInBackgroundData saveImageInBackgroundData = this.mParams;
                    saveImageInBackgroundData.image = null;
                    saveImageInBackgroundData.errorMsgResId = 0;
                    return null;
                } finally {
                }
            } finally {
            }
        } catch (Exception e2) {
            contentResolver.delete(uriInsert, null);
            throw e2;
        }
    }

    void setActionsReadyListener(GlobalScreenshot.ActionsReadyListener actionsReadyListener) {
        this.mParams.mActionsReadyListener = actionsReadyListener;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public void onCancelled(Void r2) {
        this.mImageData.reset();
        this.mParams.mActionsReadyListener.onActionsReady(this.mImageData);
        this.mParams.finisher.accept(null);
        this.mParams.clearImage();
    }

    @VisibleForTesting
    Notification.Action createShareAction(Context context, Resources resources, Uri uri) {
        String str = String.format("Screenshot (%s)", DateFormat.getDateTimeInstance().format(new Date(this.mImageTime)));
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("image/png");
        intent.putExtra("android.intent.extra.STREAM", uri);
        intent.setClipData(new ClipData(new ClipDescription("content", new String[]{"text/plain"}), new ClipData.Item(uri)));
        intent.putExtra("android.intent.extra.SUBJECT", str);
        intent.addFlags(1);
        return new Notification.Action.Builder(Icon.createWithResource(resources, R.drawable.ic_screenshot_share), resources.getString(android.R.string.permlab_accessHiddenProfile), PendingIntent.getBroadcastAsUser(context, context.getUserId(), new Intent(context, (Class<?>) ActionProxyReceiver.class).putExtra("android:screenshot_action_intent", PendingIntent.getActivityAsUser(context, 0, Intent.createChooser(intent, null).addFlags(268468224).addFlags(1), 335544320, null, UserHandle.CURRENT)).putExtra("android:screenshot_disallow_enter_pip", true).putExtra("android:screenshot_id", this.mScreenshotId).putExtra("android:smart_actions_enabled", this.mSmartActionsEnabled).setAction("android.intent.action.SEND").addFlags(268435456), 335544320, UserHandle.SYSTEM)).build();
    }

    @VisibleForTesting
    Notification.Action createEditAction(Context context, Resources resources, Uri uri) {
        String string = context.getString(R.string.config_screenshotEditor);
        Intent intent = new Intent("android.intent.action.EDIT");
        if (!TextUtils.isEmpty(string)) {
            intent.setComponent(ComponentName.unflattenFromString(string));
        }
        intent.setType("image/png");
        intent.setData(uri);
        intent.addFlags(1);
        intent.addFlags(2);
        intent.addFlags(268468224);
        return new Notification.Action.Builder(Icon.createWithResource(resources, R.drawable.ic_screenshot_edit), resources.getString(android.R.string.permdesc_writeVerificationStateE2eeContactKeys), PendingIntent.getBroadcastAsUser(context, this.mContext.getUserId(), new Intent(context, (Class<?>) ActionProxyReceiver.class).putExtra("android:screenshot_action_intent", PendingIntent.getActivityAsUser(context, 0, intent, 67108864, null, UserHandle.CURRENT)).putExtra("android:screenshot_id", this.mScreenshotId).putExtra("android:smart_actions_enabled", this.mSmartActionsEnabled).setAction("android.intent.action.EDIT").addFlags(268435456), 335544320, UserHandle.SYSTEM)).build();
    }

    @VisibleForTesting
    Notification.Action createDeleteAction(Context context, Resources resources, Uri uri) {
        return new Notification.Action.Builder(Icon.createWithResource(resources, R.drawable.ic_screenshot_delete), resources.getString(android.R.string.config_systemImageEditor), PendingIntent.getBroadcast(context, this.mContext.getUserId(), new Intent(context, (Class<?>) DeleteScreenshotReceiver.class).putExtra("android:screenshot_uri_id", uri.toString()).putExtra("android:screenshot_id", this.mScreenshotId).putExtra("android:smart_actions_enabled", this.mSmartActionsEnabled).addFlags(268435456), 1409286144)).build();
    }

    private int getUserHandleOfForegroundApplication(Context context) {
        try {
            return ActivityTaskManager.getService().getLastResumedActivityUserId();
        } catch (RemoteException e) {
            Slog.w("SaveImageInBackgroundTask", "getUserHandleOfForegroundApplication: ", e);
            return context.getUserId();
        }
    }

    private UserHandle getUserHandle(Context context) {
        return UserManager.get(context).getUserInfo(getUserHandleOfForegroundApplication(context)).getUserHandle();
    }

    private List<Notification.Action> buildSmartActions(List<Notification.Action> list, Context context) {
        ArrayList arrayList = new ArrayList();
        for (Notification.Action action : list) {
            Bundle extras = action.getExtras();
            String string = extras.getString("action_type", "Smart Action");
            Intent intentAddFlags = new Intent(context, (Class<?>) SmartActionsReceiver.class).putExtra("android:screenshot_action_intent", action.actionIntent).addFlags(268435456);
            addIntentExtras(this.mScreenshotId, intentAddFlags, string, this.mSmartActionsEnabled);
            arrayList.add(new Notification.Action.Builder(action.getIcon(), action.title, PendingIntent.getBroadcast(context, this.mRandom.nextInt(), intentAddFlags, 335544320)).setContextual(true).addExtras(extras).build());
        }
        return arrayList;
    }

    private static void addIntentExtras(String str, Intent intent, String str2, boolean z) {
        intent.putExtra("android:screenshot_action_type", str2).putExtra("android:screenshot_id", str).putExtra("android:smart_actions_enabled", z);
    }
}
