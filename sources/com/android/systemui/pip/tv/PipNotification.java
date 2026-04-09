package com.android.systemui.pip.tv;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.text.TextUtils;
import android.util.Log;
import com.android.systemui.R;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.pip.tv.PipManager;
import com.android.systemui.util.NotificationChannels;

/* loaded from: classes.dex */
public class PipNotification {
    private static final boolean DEBUG = PipManager.DEBUG;
    private static final String NOTIFICATION_TAG = "PipNotification";
    private Bitmap mArt;
    private int mDefaultIconResId;
    private String mDefaultTitle;
    private final BroadcastReceiver mEventReceiver;
    private MediaController mMediaController;
    private String mMediaTitle;
    private final Notification.Builder mNotificationBuilder;
    private final NotificationManager mNotificationManager;
    private boolean mNotified;
    private final PackageManager mPackageManager;
    private String mPackageName;
    private final PipManager mPipManager;
    private final PipManager.MediaListener mPipMediaListener;
    private PipManager.Listener mPipListener = new PipManager.Listener() { // from class: com.android.systemui.pip.tv.PipNotification.1
        @Override // com.android.systemui.pip.tv.PipManager.Listener
        public void onPipMenuActionsChanged(ParceledListSlice parceledListSlice) {
        }

        @Override // com.android.systemui.pip.tv.PipManager.Listener
        public void onPipResizeAboutToStart() {
        }

        @Override // com.android.systemui.pip.tv.PipManager.Listener
        public void onShowPipMenu() {
        }

        @Override // com.android.systemui.pip.tv.PipManager.Listener
        public void onPipEntered(String str) {
            PipNotification.this.mPackageName = str;
            PipNotification.this.updateMediaControllerMetadata();
            PipNotification.this.notifyPipNotification();
        }

        @Override // com.android.systemui.pip.tv.PipManager.Listener
        public void onPipActivityClosed() {
            PipNotification.this.dismissPipNotification();
            PipNotification.this.mPackageName = null;
        }

        @Override // com.android.systemui.pip.tv.PipManager.Listener
        public void onMoveToFullscreen() {
            PipNotification.this.dismissPipNotification();
            PipNotification.this.mPackageName = null;
        }
    };
    private MediaController.Callback mMediaControllerCallback = new MediaController.Callback() { // from class: com.android.systemui.pip.tv.PipNotification.2
        @Override // android.media.session.MediaController.Callback
        public void onPlaybackStateChanged(PlaybackState playbackState) {
            if (PipNotification.this.updateMediaControllerMetadata() && PipNotification.this.mNotified) {
                PipNotification.this.notifyPipNotification();
            }
        }
    };

    public PipNotification(Context context, BroadcastDispatcher broadcastDispatcher, PipManager pipManager) {
        PipManager.MediaListener mediaListener = new PipManager.MediaListener() { // from class: com.android.systemui.pip.tv.PipNotification.3
            @Override // com.android.systemui.pip.tv.PipManager.MediaListener
            public void onMediaControllerChanged() {
                MediaController mediaController = PipNotification.this.mPipManager.getMediaController();
                if (PipNotification.this.mMediaController == mediaController) {
                    return;
                }
                if (PipNotification.this.mMediaController != null) {
                    PipNotification.this.mMediaController.unregisterCallback(PipNotification.this.mMediaControllerCallback);
                }
                PipNotification.this.mMediaController = mediaController;
                if (PipNotification.this.mMediaController != null) {
                    PipNotification.this.mMediaController.registerCallback(PipNotification.this.mMediaControllerCallback);
                }
                if (PipNotification.this.updateMediaControllerMetadata() && PipNotification.this.mNotified) {
                    PipNotification.this.notifyPipNotification();
                }
            }
        };
        this.mPipMediaListener = mediaListener;
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.pip.tv.PipNotification.4
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                if (PipNotification.DEBUG) {
                    Log.d("PipNotification", "Received " + intent.getAction() + " from the notification UI");
                }
                String action = intent.getAction();
                action.hashCode();
                if (action.equals("PipNotification.close")) {
                    PipNotification.this.mPipManager.closePip();
                } else if (action.equals("PipNotification.menu")) {
                    PipNotification.this.mPipManager.showPictureInPictureMenu();
                }
            }
        };
        this.mEventReceiver = broadcastReceiver;
        this.mPackageManager = context.getPackageManager();
        this.mNotificationManager = (NotificationManager) context.getSystemService("notification");
        this.mNotificationBuilder = new Notification.Builder(context, NotificationChannels.TVPIP).setLocalOnly(true).setOngoing(false).setCategory("sys").extend(new Notification.TvExtender().setContentIntent(createPendingIntent(context, "PipNotification.menu")).setDeleteIntent(createPendingIntent(context, "PipNotification.close")));
        this.mPipManager = pipManager;
        pipManager.addListener(this.mPipListener);
        pipManager.addMediaListener(mediaListener);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("PipNotification.menu");
        intentFilter.addAction("PipNotification.close");
        broadcastDispatcher.registerReceiver(broadcastReceiver, intentFilter);
        onConfigurationChanged(context);
    }

    void onConfigurationChanged(Context context) {
        this.mDefaultTitle = context.getResources().getString(R.string.pip_notification_unknown_title);
        this.mDefaultIconResId = R.drawable.pip_icon;
        if (this.mNotified) {
            notifyPipNotification();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyPipNotification() {
        this.mNotified = true;
        this.mNotificationBuilder.setShowWhen(true).setWhen(System.currentTimeMillis()).setSmallIcon(this.mDefaultIconResId).setContentTitle(getNotificationTitle());
        if (this.mArt != null) {
            this.mNotificationBuilder.setStyle(new Notification.BigPictureStyle().bigPicture(this.mArt));
        } else {
            this.mNotificationBuilder.setStyle(null);
        }
        this.mNotificationManager.notify(NOTIFICATION_TAG, 1100, this.mNotificationBuilder.build());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dismissPipNotification() {
        this.mNotified = false;
        this.mNotificationManager.cancel(NOTIFICATION_TAG, 1100);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean updateMediaControllerMetadata() {
        Bitmap bitmap;
        MediaMetadata metadata;
        String string = null;
        if (this.mPipManager.getMediaController() == null || (metadata = this.mPipManager.getMediaController().getMetadata()) == null) {
            bitmap = null;
        } else {
            string = metadata.getString("android.media.metadata.DISPLAY_TITLE");
            if (TextUtils.isEmpty(string)) {
                string = metadata.getString("android.media.metadata.TITLE");
            }
            Bitmap bitmap2 = metadata.getBitmap("android.media.metadata.ALBUM_ART");
            bitmap = bitmap2 == null ? metadata.getBitmap("android.media.metadata.ART") : bitmap2;
        }
        if (TextUtils.equals(string, this.mMediaTitle) && bitmap == this.mArt) {
            return false;
        }
        this.mMediaTitle = string;
        this.mArt = bitmap;
        return true;
    }

    private String getNotificationTitle() throws PackageManager.NameNotFoundException {
        if (!TextUtils.isEmpty(this.mMediaTitle)) {
            return this.mMediaTitle;
        }
        String applicationLabel = getApplicationLabel(this.mPackageName);
        return !TextUtils.isEmpty(applicationLabel) ? applicationLabel : this.mDefaultTitle;
    }

    private String getApplicationLabel(String str) throws PackageManager.NameNotFoundException {
        try {
            return this.mPackageManager.getApplicationLabel(this.mPackageManager.getApplicationInfo(str, 0)).toString();
        } catch (PackageManager.NameNotFoundException unused) {
            return null;
        }
    }

    private static PendingIntent createPendingIntent(Context context, String str) {
        return PendingIntent.getBroadcast(context, 0, new Intent(str), 268435456);
    }
}
