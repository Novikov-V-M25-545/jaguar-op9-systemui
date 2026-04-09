package com.android.systemui.crdroid.onthego;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.android.internal.util.crdroid.OnTheGoUtils;
import com.android.systemui.R;
import java.io.IOException;

/* loaded from: classes.dex */
public class OnTheGoService extends Service {
    private Camera mCamera;
    private NotificationChannel mNotificationChannel;
    private NotificationManager mNotificationManager;
    private FrameLayout mOverlay;
    private final Handler mHandler = new Handler();
    private final Object mRestartObject = new Object();
    private final BroadcastReceiver mAlphaReceiver = new BroadcastReceiver() { // from class: com.android.systemui.crdroid.onthego.OnTheGoService.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            OnTheGoService.this.toggleOnTheGoAlpha(intent.getFloatExtra("extra_alpha", 0.5f));
        }
    };
    private final BroadcastReceiver mCameraReceiver = new BroadcastReceiver() { // from class: com.android.systemui.crdroid.onthego.OnTheGoService.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            synchronized (OnTheGoService.this.mRestartObject) {
                if (Settings.System.getInt(OnTheGoService.this.getContentResolver(), "on_the_go_service_restart", 0) == 1) {
                    OnTheGoService.this.restartOnTheGo();
                } else {
                    OnTheGoService.this.stopOnTheGo(true);
                }
            }
        }
    };
    private final Runnable mRestartRunnable = new Runnable() { // from class: com.android.systemui.crdroid.onthego.OnTheGoService.3
        @Override // java.lang.Runnable
        public void run() {
            synchronized (OnTheGoService.this.mRestartObject) {
                OnTheGoService.this.setupViews(true);
            }
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    public void logDebug(String str) {
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
        resetViews();
    }

    private void registerReceivers() {
        registerReceiver(this.mAlphaReceiver, new IntentFilter("toggle_alpha"));
        registerReceiver(this.mCameraReceiver, new IntentFilter("toggle_camera"));
    }

    private void unregisterReceivers() {
        try {
            unregisterReceiver(this.mAlphaReceiver);
        } catch (Exception unused) {
        }
        try {
            unregisterReceiver(this.mCameraReceiver);
        } catch (Exception unused2) {
        }
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        logDebug("onStartCommand called");
        if (intent == null || !OnTheGoUtils.hasCamera(this)) {
            stopSelf();
            return 2;
        }
        String action = intent.getAction();
        if (action != null && !action.isEmpty()) {
            logDebug("Action: " + action);
            if (action.equals("start")) {
                startOnTheGo();
            } else if (action.equals("stop")) {
                stopOnTheGo(false);
            } else if (action.equals("toggle_options")) {
                new OnTheGoDialog(this).show();
            }
        } else {
            logDebug("Action is NULL or EMPTY!");
            stopSelf();
        }
        return 2;
    }

    private void startOnTheGo() {
        if (this.mNotificationManager != null) {
            logDebug("Starting while active, stopping.");
            stopOnTheGo(false);
        } else {
            resetViews();
            registerReceivers();
            setupViews(false);
            createNotification(0);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopOnTheGo(boolean z) {
        unregisterReceivers();
        resetViews();
        NotificationManager notificationManager = this.mNotificationManager;
        if (notificationManager != null) {
            notificationManager.cancel(81333378);
            this.mNotificationManager.deleteNotificationChannel("onthego_notif");
            this.mNotificationManager = null;
        }
        if (z) {
            createNotification(1);
        }
        stopSelf();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void restartOnTheGo() {
        resetViews();
        this.mHandler.removeCallbacks(this.mRestartRunnable);
        this.mHandler.postDelayed(this.mRestartRunnable, 750L);
    }

    private void toggleOnTheGoAlpha() {
        toggleOnTheGoAlpha(Settings.System.getFloat(getContentResolver(), "on_the_go_alpha", 0.5f));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void toggleOnTheGoAlpha(float f) {
        Settings.System.putFloat(getContentResolver(), "on_the_go_alpha", f);
        FrameLayout frameLayout = this.mOverlay;
        if (frameLayout != null) {
            frameLayout.setAlpha(f);
        }
    }

    private void getCameraInstance(int i) throws IOException, RuntimeException {
        releaseCamera();
        if (!OnTheGoUtils.hasFrontCamera(this)) {
            this.mCamera = Camera.open();
            return;
        }
        if (i != 1) {
            this.mCamera = Camera.open(0);
            return;
        }
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i2 = 0; i2 < numberOfCameras; i2++) {
            Camera.getCameraInfo(i2, cameraInfo);
            if (cameraInfo.facing == 1) {
                this.mCamera = Camera.open(i2);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setupViews(boolean z) {
        StringBuilder sb = new StringBuilder();
        sb.append("Setup Views, restarting: ");
        sb.append(z ? "true" : "false");
        logDebug(sb.toString());
        try {
            getCameraInstance(Settings.System.getInt(getContentResolver(), "on_the_go_camera", 0));
        } catch (Exception e) {
            logDebug("Exception: " + e.getMessage());
            createNotification(2);
            stopOnTheGo(true);
        }
        TextureView textureView = new TextureView(this);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() { // from class: com.android.systemui.crdroid.onthego.OnTheGoService.4
            @Override // android.view.TextureView.SurfaceTextureListener
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
            }

            @Override // android.view.TextureView.SurfaceTextureListener
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            }

            @Override // android.view.TextureView.SurfaceTextureListener
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) throws IOException {
                try {
                    if (OnTheGoService.this.mCamera != null) {
                        OnTheGoService.this.mCamera.setDisplayOrientation(90);
                        OnTheGoService.this.mCamera.setPreviewTexture(surfaceTexture);
                        OnTheGoService.this.mCamera.startPreview();
                    }
                } catch (IOException e2) {
                    OnTheGoService.this.logDebug("IOException: " + e2.getMessage());
                }
            }

            @Override // android.view.TextureView.SurfaceTextureListener
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                OnTheGoService.this.releaseCamera();
                return true;
            }
        });
        FrameLayout frameLayout = new FrameLayout(this);
        this.mOverlay = frameLayout;
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        this.mOverlay.addView(textureView);
        ((WindowManager) getSystemService("window")).addView(this.mOverlay, new WindowManager.LayoutParams(2006, 218104872, -3));
        toggleOnTheGoAlpha();
    }

    private void resetViews() {
        releaseCamera();
        WindowManager windowManager = (WindowManager) getSystemService("window");
        FrameLayout frameLayout = this.mOverlay;
        if (frameLayout != null) {
            frameLayout.removeAllViews();
            windowManager.removeView(this.mOverlay);
            this.mOverlay = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void releaseCamera() {
        Camera camera = this.mCamera;
        if (camera != null) {
            camera.stopPreview();
            this.mCamera.release();
            this.mCamera = null;
        }
    }

    private void createNotification(int i) {
        int i2;
        int i3;
        Resources resources = getResources();
        Notification.Builder builder = new Notification.Builder(this, "onthego_notif");
        if (i == 1) {
            i2 = R.string.onthego_notif_camera_changed;
        } else if (i == 2) {
            i2 = R.string.onthego_notif_error;
        } else {
            i2 = R.string.onthego_notif_ticker;
        }
        Notification.Builder ticker = builder.setTicker(resources.getString(i2));
        if (i == 1) {
            i3 = R.string.onthego_notif_camera_changed;
        } else if (i == 2) {
            i3 = R.string.onthego_notif_error;
        } else {
            i3 = R.string.onthego_notif_title;
        }
        Notification.Builder ongoing = ticker.setContentTitle(resources.getString(i3)).setSmallIcon(R.drawable.ic_lock_onthego).setWhen(System.currentTimeMillis()).setOngoing((i == 1 || i == 2) ? false : true);
        if (i == 1 || i == 2) {
            ComponentName componentName = new ComponentName("com.android.systemui", "com.android.systemui.spark.onthego.OnTheGoService");
            Intent intent = new Intent();
            intent.setComponent(componentName);
            intent.setAction("start");
            ongoing.addAction(android.R.drawable.ic_media_play, resources.getString(R.string.onthego_notif_restart), PendingIntent.getService(this, 0, intent, 134217728));
        } else {
            ongoing.addAction(android.R.drawable.ic_media_route_connected_light_16_mtrl, resources.getString(R.string.onthego_notif_stop), PendingIntent.getService(this, 0, new Intent(this, (Class<?>) OnTheGoService.class).setAction("stop"), 134217728)).addAction(android.R.drawable.ic_minus, resources.getString(R.string.onthego_notif_options), PendingIntent.getService(this, 0, new Intent(this, (Class<?>) OnTheGoService.class).setAction("toggle_options"), 134217728));
        }
        Notification notificationBuild = ongoing.build();
        this.mNotificationManager = (NotificationManager) getSystemService("notification");
        NotificationChannel notificationChannel = new NotificationChannel("onthego_notif", resources.getString(R.string.onthego_channel_name), 2);
        this.mNotificationChannel = notificationChannel;
        this.mNotificationManager.createNotificationChannel(notificationChannel);
        this.mNotificationManager.notify(81333378, notificationBuild);
    }
}
