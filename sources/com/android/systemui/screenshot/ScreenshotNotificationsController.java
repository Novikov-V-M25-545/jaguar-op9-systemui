package com.android.systemui.screenshot;

import android.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Picture;
import android.os.UserHandle;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.android.systemui.SystemUI;
import com.android.systemui.screenshot.GlobalScreenshot;
import com.android.systemui.util.NotificationChannels;
import java.util.Iterator;

/* loaded from: classes.dex */
public class ScreenshotNotificationsController {
    private final Context mContext;
    private int mIconSize;
    private Notification.Builder mNotificationBuilder;
    private final NotificationManager mNotificationManager;
    private final Notification.BigPictureStyle mNotificationStyle;
    private int mPreviewHeight;
    private int mPreviewWidth;
    private Notification.Builder mPublicNotificationBuilder;
    private final Resources mResources;

    ScreenshotNotificationsController(Context context, WindowManager windowManager) throws Resources.NotFoundException {
        int dimensionPixelSize;
        this.mContext = context;
        Resources resources = context.getResources();
        this.mResources = resources;
        this.mNotificationManager = (NotificationManager) context.getSystemService("notification");
        this.mIconSize = resources.getDimensionPixelSize(R.dimen.notification_large_icon_height);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        try {
            dimensionPixelSize = resources.getDimensionPixelSize(com.android.systemui.R.dimen.notification_panel_width);
        } catch (Resources.NotFoundException unused) {
            dimensionPixelSize = 0;
        }
        this.mPreviewWidth = dimensionPixelSize <= 0 ? displayMetrics.widthPixels : dimensionPixelSize;
        this.mPreviewHeight = this.mResources.getDimensionPixelSize(com.android.systemui.R.dimen.notification_max_height);
        this.mNotificationStyle = new Notification.BigPictureStyle();
    }

    public void reset() {
        this.mPublicNotificationBuilder = new Notification.Builder(this.mContext, NotificationChannels.SCREENSHOTS_HEADSUP);
        this.mNotificationBuilder = new Notification.Builder(this.mContext, NotificationChannels.SCREENSHOTS_HEADSUP);
    }

    public void setImage(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0.25f);
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        Matrix matrix = new Matrix();
        matrix.setTranslate((this.mPreviewWidth - width) / 2.0f, (this.mPreviewHeight - height) / 2.0f);
        this.mNotificationStyle.bigPicture(generateAdjustedHwBitmap(bitmap, this.mPreviewWidth, this.mPreviewHeight, matrix, paint, 1090519039).createAshmemBitmap());
        float fMin = this.mIconSize / Math.min(width, height);
        matrix.setScale(fMin, fMin);
        int i = this.mIconSize;
        matrix.postTranslate((i - (width * fMin)) / 2.0f, (i - (fMin * height)) / 2.0f);
        int i2 = this.mIconSize;
        this.mNotificationBuilder.setLargeIcon(generateAdjustedHwBitmap(bitmap, i2, i2, matrix, paint, 1090519039).createAshmemBitmap());
        this.mNotificationStyle.bigLargeIcon((Bitmap) null);
    }

    public void showSilentScreenshotNotification(GlobalScreenshot.SavedImageData savedImageData) {
        this.mNotificationBuilder.addAction(savedImageData.shareAction);
        this.mNotificationBuilder.addAction(savedImageData.editAction);
        Iterator<Notification.Action> it = savedImageData.smartActions.iterator();
        while (it.hasNext()) {
            this.mNotificationBuilder.addAction(it.next());
        }
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(savedImageData.uri, "image/png");
        intent.setFlags(268435457);
        long jCurrentTimeMillis = System.currentTimeMillis();
        Notification.Builder builder = this.mPublicNotificationBuilder;
        Resources resources = this.mResources;
        int i = com.android.systemui.R.string.screenshot_saved_title;
        Notification.Builder contentTitle = builder.setContentTitle(resources.getString(i));
        Resources resources2 = this.mResources;
        int i2 = com.android.systemui.R.string.screenshot_saved_text;
        Notification.Builder contentIntent = contentTitle.setContentText(resources2.getString(i2)).setContentIntent(PendingIntent.getActivity(this.mContext, 0, intent, 0));
        int i3 = com.android.systemui.R.drawable.stat_notify_image;
        contentIntent.setSmallIcon(i3).setCategory("progress").setWhen(jCurrentTimeMillis).setShowWhen(true).setAutoCancel(true).setColor(this.mContext.getColor(R.color.system_notification_accent_color)).setGroup("silent").setGroupAlertBehavior(1);
        this.mNotificationBuilder.setContentTitle(this.mResources.getString(i)).setContentText(this.mResources.getString(i2)).setContentIntent(PendingIntent.getActivity(this.mContext, 0, intent, 0)).setSmallIcon(i3).setCategory("progress").setWhen(jCurrentTimeMillis).setShowWhen(true).setAutoCancel(true).setColor(this.mContext.getColor(R.color.system_notification_accent_color)).setPublicVersion(this.mPublicNotificationBuilder.build()).setStyle(this.mNotificationStyle).setFlag(32, false).setGroup("silent").setGroupAlertBehavior(1);
        SystemUI.overrideNotificationAppName(this.mContext, this.mPublicNotificationBuilder, true);
        SystemUI.overrideNotificationAppName(this.mContext, this.mNotificationBuilder, true);
        this.mNotificationManager.notify(1, this.mNotificationBuilder.build());
    }

    public void notifyScreenshotError(int i) throws Resources.NotFoundException {
        Resources resources = this.mContext.getResources();
        String string = resources.getString(i);
        Notification.Builder builder = new Notification.Builder(this.mContext, NotificationChannels.ALERTS);
        int i2 = com.android.systemui.R.string.screenshot_failed_title;
        Notification.Builder color = builder.setTicker(resources.getString(i2)).setContentTitle(resources.getString(i2)).setContentText(string).setSmallIcon(com.android.systemui.R.drawable.stat_notify_image_error).setWhen(System.currentTimeMillis()).setVisibility(1).setCategory("err").setAutoCancel(true).setColor(this.mContext.getColor(R.color.system_notification_accent_color));
        Intent intentCreateAdminSupportIntent = ((DevicePolicyManager) this.mContext.getSystemService("device_policy")).createAdminSupportIntent("policy_disable_screen_capture");
        if (intentCreateAdminSupportIntent != null) {
            color.setContentIntent(PendingIntent.getActivityAsUser(this.mContext, 0, intentCreateAdminSupportIntent, 67108864, null, UserHandle.CURRENT));
        }
        SystemUI.overrideNotificationAppName(this.mContext, color, true);
        this.mNotificationManager.notify(1, new Notification.BigTextStyle(color).bigText(string).build());
    }

    private Bitmap generateAdjustedHwBitmap(Bitmap bitmap, int i, int i2, Matrix matrix, Paint paint, int i3) {
        Picture picture = new Picture();
        Canvas canvasBeginRecording = picture.beginRecording(i, i2);
        canvasBeginRecording.drawColor(i3);
        canvasBeginRecording.drawBitmap(bitmap, matrix, paint);
        picture.endRecording();
        return Bitmap.createBitmap(picture);
    }

    static void cancelScreenshotNotification(Context context) {
        ((NotificationManager) context.getSystemService("notification")).cancel(1);
    }
}
