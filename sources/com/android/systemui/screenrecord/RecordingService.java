package com.android.systemui.screenrecord;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.R;
import com.android.systemui.screenrecord.ScreenMediaRecorder;
import com.android.systemui.settings.CurrentUserContextTracker;
import com.android.systemui.statusbar.phone.KeyguardDismissUtil;
import java.io.IOException;
import java.util.concurrent.Executor;

/* loaded from: classes.dex */
public class RecordingService extends Service implements MediaRecorder.OnInfoListener {
    private ScreenRecordingAudioSource mAudioSource;
    private final RecordingController mController;
    private final KeyguardDismissUtil mKeyguardDismissUtil;
    private final Executor mLongExecutor;
    private final NotificationManager mNotificationManager;
    private boolean mOriginalShowTaps;
    private ScreenMediaRecorder mRecorder;
    private boolean mShowTaps;
    private final UiEventLogger mUiEventLogger;
    private final CurrentUserContextTracker mUserContextTracker;

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    public RecordingService(RecordingController recordingController, Executor executor, UiEventLogger uiEventLogger, NotificationManager notificationManager, CurrentUserContextTracker currentUserContextTracker, KeyguardDismissUtil keyguardDismissUtil) {
        this.mController = recordingController;
        this.mLongExecutor = executor;
        this.mUiEventLogger = uiEventLogger;
        this.mNotificationManager = notificationManager;
        this.mUserContextTracker = currentUserContextTracker;
        this.mKeyguardDismissUtil = keyguardDismissUtil;
    }

    public static Intent getStartIntent(Context context, int i, int i2, boolean z) {
        return new Intent(context, (Class<?>) RecordingService.class).setAction("com.android.systemui.screenrecord.START").putExtra("extra_resultCode", i).putExtra("extra_useAudio", i2).putExtra("extra_showTaps", z);
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue */
    /* JADX WARN: Removed duplicated region for block: B:7:0x003c  */
    @Override // android.app.Service
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public int onStartCommand(android.content.Intent r10, int r11, int r12) throws java.lang.IllegalStateException, java.lang.InterruptedException {
        /*
            Method dump skipped, instructions count: 428
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.screenrecord.RecordingService.onStartCommand(android.content.Intent, int, int):int");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$onStartCommand$0(Intent intent, UserHandle userHandle) {
        startActivity(Intent.createChooser(intent, getResources().getString(R.string.screenrecord_share_label)).setFlags(268435456));
        this.mNotificationManager.cancelAsUser(null, 4273, userHandle);
        return false;
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
    }

    @VisibleForTesting
    protected ScreenMediaRecorder getRecorder() {
        return this.mRecorder;
    }

    private void startRecording() {
        try {
            getRecorder().start();
            this.mController.updateState(true);
            createRecordingNotification();
            this.mUiEventLogger.log(Events$ScreenRecordEvent.SCREEN_RECORD_START);
        } catch (RemoteException | IOException | IllegalStateException e) {
            Toast.makeText(this, R.string.screenrecord_start_error, 1).show();
            e.printStackTrace();
            this.mController.updateState(false);
        }
    }

    @VisibleForTesting
    protected void createRecordingNotification() throws Resources.NotFoundException {
        String string;
        Resources resources = getResources();
        int i = R.string.screenrecord_name;
        NotificationChannel notificationChannel = new NotificationChannel("screen_record", getString(i), 3);
        notificationChannel.setDescription(getString(R.string.screenrecord_channel_description));
        notificationChannel.enableVibration(true);
        this.mNotificationManager.createNotificationChannel(notificationChannel);
        Bundle bundle = new Bundle();
        bundle.putString("android.substName", resources.getString(i));
        if (this.mAudioSource == ScreenRecordingAudioSource.NONE) {
            string = resources.getString(R.string.screenrecord_ongoing_screen_only);
        } else {
            string = resources.getString(R.string.screenrecord_ongoing_screen_and_audio);
        }
        startForeground(4274, new Notification.Builder(this, "screen_record").setSmallIcon(R.drawable.ic_screenrecord).setContentTitle(string).setContentText(getResources().getString(R.string.screenrecord_stop_text)).setUsesChronometer(true).setColorized(true).setColor(getResources().getColor(R.color.GM2_red_700)).setOngoing(true).setContentIntent(PendingIntent.getService(this, 2, getNotificationIntent(this), 201326592)).addExtras(bundle).build());
    }

    @VisibleForTesting
    protected Notification createProcessingNotification() throws Resources.NotFoundException {
        String string;
        Resources resources = getApplicationContext().getResources();
        if (this.mAudioSource == ScreenRecordingAudioSource.NONE) {
            string = resources.getString(R.string.screenrecord_ongoing_screen_only);
        } else {
            string = resources.getString(R.string.screenrecord_ongoing_screen_and_audio);
        }
        Bundle bundle = new Bundle();
        bundle.putString("android.substName", resources.getString(R.string.screenrecord_name));
        return new Notification.Builder(getApplicationContext(), "screen_record").setContentTitle(string).setContentText(getResources().getString(R.string.screenrecord_background_processing_label)).setSmallIcon(R.drawable.ic_screenrecord).addExtras(bundle).build();
    }

    @VisibleForTesting
    protected Notification createSaveNotification(ScreenMediaRecorder.SavedRecording savedRecording) {
        Uri uri = savedRecording.getUri();
        Intent dataAndType = new Intent("android.intent.action.VIEW").setFlags(268435457).setDataAndType(uri, "video/mp4");
        int i = R.drawable.ic_screenrecord;
        Notification.Action actionBuild = new Notification.Action.Builder(Icon.createWithResource(this, i), getResources().getString(R.string.screenrecord_share_label), PendingIntent.getService(this, 2, getShareIntent(this, uri.toString()), 201326592)).build();
        Notification.Action actionBuild2 = new Notification.Action.Builder(Icon.createWithResource(this, i), getResources().getString(R.string.screenrecord_delete_label), PendingIntent.getService(this, 2, getDeleteIntent(this, uri.toString()), 201326592)).build();
        Bundle bundle = new Bundle();
        bundle.putString("android.substName", getResources().getString(R.string.screenrecord_name));
        Notification.Builder builderAddExtras = new Notification.Builder(this, "screen_record").setSmallIcon(i).setContentTitle(getResources().getString(R.string.screenrecord_save_message)).setContentIntent(PendingIntent.getActivity(this, 2, dataAndType, 67108864)).addAction(actionBuild).addAction(actionBuild2).setAutoCancel(true).addExtras(bundle);
        Bitmap thumbnail = savedRecording.getThumbnail();
        if (thumbnail != null) {
            builderAddExtras.setLargeIcon(thumbnail).setStyle(new Notification.BigPictureStyle().bigPicture(thumbnail).bigLargeIcon((Bitmap) null));
        }
        return builderAddExtras.build();
    }

    private void stopRecording(int i) throws IllegalStateException, InterruptedException {
        setTapsVisible(this.mOriginalShowTaps);
        if (getRecorder() != null) {
            getRecorder().end();
            saveRecording(i);
        } else {
            Log.e("RecordingService", "stopRecording called, but recorder was null");
        }
        this.mController.updateState(false);
    }

    private void saveRecording(int i) {
        final UserHandle userHandle = new UserHandle(i);
        this.mNotificationManager.notifyAsUser(null, 4275, createProcessingNotification(), userHandle);
        this.mLongExecutor.execute(new Runnable() { // from class: com.android.systemui.screenrecord.RecordingService$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$saveRecording$1(userHandle);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r6v3, types: [android.app.NotificationManager] */
    public /* synthetic */ void lambda$saveRecording$1(UserHandle userHandle) {
        try {
            try {
                Log.d("RecordingService", "saving recording");
                Notification notificationCreateSaveNotification = createSaveNotification(getRecorder().save());
                if (!this.mController.isRecording()) {
                    this.mNotificationManager.notifyAsUser(null, 4273, notificationCreateSaveNotification, userHandle);
                }
            } catch (IOException e) {
                Log.e("RecordingService", "Error saving screen recording: " + e.getMessage());
                Toast.makeText(this, R.string.screenrecord_delete_error, 1).show();
            }
        } finally {
            this.mNotificationManager.cancelAsUser(null, 4275, userHandle);
        }
    }

    private void setTapsVisible(boolean z) {
        Settings.System.putInt(getContentResolver(), "show_touches", z ? 1 : 0);
    }

    public static Intent getStopIntent(Context context) {
        return new Intent(context, (Class<?>) RecordingService.class).setAction("com.android.systemui.screenrecord.STOP").putExtra("android.intent.extra.user_handle", context.getUserId());
    }

    protected static Intent getNotificationIntent(Context context) {
        return new Intent(context, (Class<?>) RecordingService.class).setAction("com.android.systemui.screenrecord.STOP_FROM_NOTIF");
    }

    private static Intent getShareIntent(Context context, String str) {
        return new Intent(context, (Class<?>) RecordingService.class).setAction("com.android.systemui.screenrecord.SHARE").putExtra("extra_path", str);
    }

    private static Intent getDeleteIntent(Context context, String str) {
        return new Intent(context, (Class<?>) RecordingService.class).setAction("com.android.systemui.screenrecord.DELETE").putExtra("extra_path", str);
    }

    @Override // android.media.MediaRecorder.OnInfoListener
    public void onInfo(MediaRecorder mediaRecorder, int i, int i2) throws IllegalStateException, InterruptedException {
        Log.d("RecordingService", "Media recorder info: " + i);
        onStartCommand(getStopIntent(this), 0, 0);
    }
}
