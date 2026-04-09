package com.android.systemui.pulse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.media.IAudioService;
import android.media.MediaMetadata;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.widget.FrameLayout;
import com.android.systemui.Dependency;
import com.android.systemui.pulse.VisualizerStreamHandler;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.NavigationBarFrame;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.PulseController;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/* loaded from: classes.dex */
public class PulseControllerImpl implements PulseController, CommandQueue.Callbacks, ConfigurationController.ConfigurationListener {
    private static final String TAG = "PulseControllerImpl";
    private boolean mAmbPulseEnabled;
    private boolean mAttached;
    private final AudioManager mAudioManager;
    private final BroadcastReceiver mBroadcastReceiver;
    private final ColorController mColorController;
    private final Context mContext;
    private boolean mDozing;
    private final Handler mHandler;
    private boolean mIsMediaPlaying;
    private boolean mKeyguardGoingAway;
    private boolean mKeyguardShowing;
    private boolean mLeftInLandscape;
    private boolean mLinked;
    private boolean mLsPulseEnabled;
    private boolean mMusicStreamMuted;
    private boolean mNavPulseEnabled;
    private boolean mPowerSaveModeEnabled;
    private int mPulseStyle;
    private final PulseView mPulseView;
    private Renderer mRenderer;
    private boolean mScreenPinningEnabled;
    private final StatusBar mStatusbar;
    private final VisualizerStreamHandler mStreamHandler;
    private final VisualizerStreamHandler.Listener mStreamListener;
    private final List<PulseController.PulseStateListener> mStateListeners = new ArrayList();
    private boolean mScreenOn = true;

    private void log(String str) {
    }

    private class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        void register() {
            PulseControllerImpl.this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("navbar_pulse_enabled"), false, this, -1);
            PulseControllerImpl.this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("lockscreen_pulse_enabled"), false, this, -1);
            PulseControllerImpl.this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("ambient_pulse_enabled"), false, this, -1);
            PulseControllerImpl.this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("pulse_render_style"), false, this, -1);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            if (uri.equals(Settings.Secure.getUriFor("navbar_pulse_enabled")) || uri.equals(Settings.Secure.getUriFor("lockscreen_pulse_enabled")) || uri.equals(Settings.Secure.getUriFor("ambient_pulse_enabled"))) {
                updateEnabled();
                PulseControllerImpl.this.updatePulseVisibility();
            } else if (uri.equals(Settings.Secure.getUriFor("pulse_render_style"))) {
                updateRenderMode();
                PulseControllerImpl.this.loadRenderer();
            }
        }

        void updateSettings() {
            updateEnabled();
            updateRenderMode();
        }

        void updateEnabled() {
            PulseControllerImpl pulseControllerImpl = PulseControllerImpl.this;
            pulseControllerImpl.mNavPulseEnabled = Settings.Secure.getIntForUser(pulseControllerImpl.mContext.getContentResolver(), "navbar_pulse_enabled", 0, -2) == 1;
            PulseControllerImpl pulseControllerImpl2 = PulseControllerImpl.this;
            pulseControllerImpl2.mLsPulseEnabled = Settings.Secure.getIntForUser(pulseControllerImpl2.mContext.getContentResolver(), "lockscreen_pulse_enabled", 1, -2) == 1;
            PulseControllerImpl pulseControllerImpl3 = PulseControllerImpl.this;
            pulseControllerImpl3.mAmbPulseEnabled = Settings.Secure.getIntForUser(pulseControllerImpl3.mContext.getContentResolver(), "ambient_pulse_enabled", 0, -2) == 1;
        }

        void updateRenderMode() {
            PulseControllerImpl pulseControllerImpl = PulseControllerImpl.this;
            pulseControllerImpl.mPulseStyle = Settings.Secure.getIntForUser(pulseControllerImpl.mContext.getContentResolver(), "pulse_render_style", 1, -2);
        }
    }

    @Override // com.android.systemui.statusbar.policy.PulseController
    public void notifyKeyguardGoingAway() {
        if (this.mLsPulseEnabled) {
            this.mKeyguardGoingAway = true;
            updatePulseVisibility();
            this.mKeyguardGoingAway = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updatePulseVisibility() {
        StatusBar statusBar = this.mStatusbar;
        if (statusBar == null) {
            return;
        }
        NavigationBarFrame navbarFrame = statusBar.getNavigationBarView() != null ? this.mStatusbar.getNavigationBarView().getNavbarFrame() : null;
        VisualizerView lsVisualizer = this.mStatusbar.getLsVisualizer();
        boolean z = true;
        boolean z2 = lsVisualizer != null && lsVisualizer.isAttached() && this.mAmbPulseEnabled && this.mKeyguardShowing && this.mDozing;
        boolean z3 = lsVisualizer != null && lsVisualizer.isAttached() && this.mLsPulseEnabled && this.mKeyguardShowing && !this.mDozing;
        boolean z4 = navbarFrame != null && navbarFrame.isAttached() && this.mNavPulseEnabled && !this.mKeyguardShowing;
        if (this.mKeyguardGoingAway) {
            detachPulseFrom(lsVisualizer, z4);
            return;
        }
        if (!z4) {
            if (!z3 && !z2) {
                z = false;
            }
            detachPulseFrom(navbarFrame, z);
        }
        if (!z3 && !z2) {
            detachPulseFrom(lsVisualizer, z4);
        }
        if (z3 || z2) {
            attachPulseTo(lsVisualizer);
        } else if (z4) {
            attachPulseTo(navbarFrame);
        }
    }

    @Override // com.android.systemui.statusbar.policy.PulseController
    public void setDozing(boolean z) {
        if (this.mDozing != z) {
            this.mDozing = z;
            updatePulseVisibility();
        }
    }

    @Override // com.android.systemui.statusbar.policy.PulseController
    public void setKeyguardShowing(boolean z) {
        if (z != this.mKeyguardShowing) {
            this.mKeyguardShowing = z;
            Renderer renderer = this.mRenderer;
            if (renderer != null) {
                renderer.setKeyguardShowing(z);
            }
            updatePulseVisibility();
        }
    }

    public PulseControllerImpl(Context context, Handler handler, Executor executor) {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.pulse.PulseControllerImpl.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                int intExtra;
                boolean zIsMusicMuted;
                String action = intent.getAction();
                if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    PulseControllerImpl.this.mScreenOn = false;
                    PulseControllerImpl.this.doLinkage();
                    return;
                }
                if ("android.intent.action.SCREEN_ON".equals(action)) {
                    PulseControllerImpl.this.mScreenOn = true;
                    PulseControllerImpl.this.doLinkage();
                    return;
                }
                if ("android.os.action.POWER_SAVE_MODE_CHANGING".equals(intent.getAction())) {
                    PulseControllerImpl.this.mPowerSaveModeEnabled = intent.getBooleanExtra("mode", false);
                    PulseControllerImpl.this.doLinkage();
                } else if (("android.media.STREAM_MUTE_CHANGED_ACTION".equals(intent.getAction()) || "android.media.VOLUME_CHANGED_ACTION".equals(intent.getAction())) && (intExtra = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1)) == 3 && PulseControllerImpl.this.mMusicStreamMuted != (zIsMusicMuted = PulseControllerImpl.this.isMusicMuted(intExtra))) {
                    PulseControllerImpl.this.mMusicStreamMuted = zIsMusicMuted;
                    PulseControllerImpl.this.doLinkage();
                }
            }
        };
        this.mBroadcastReceiver = broadcastReceiver;
        VisualizerStreamHandler.Listener listener = new VisualizerStreamHandler.Listener() { // from class: com.android.systemui.pulse.PulseControllerImpl.2
            @Override // com.android.systemui.pulse.VisualizerStreamHandler.Listener
            public void onStreamAnalyzed(boolean z) {
                if (PulseControllerImpl.this.mRenderer != null) {
                    PulseControllerImpl.this.mRenderer.onStreamAnalyzed(z);
                }
                if (z) {
                    PulseControllerImpl.this.notifyStateListeners(true);
                    PulseControllerImpl.this.turnOnPulse();
                } else {
                    PulseControllerImpl.this.doSilentUnlinkVisualizer();
                }
            }

            @Override // com.android.systemui.pulse.VisualizerStreamHandler.Listener
            public void onFFTUpdate(byte[] bArr) {
                if (PulseControllerImpl.this.mRenderer == null || bArr == null) {
                    return;
                }
                PulseControllerImpl.this.mRenderer.onFFTUpdate(bArr);
            }
        };
        this.mStreamListener = listener;
        this.mContext = context;
        this.mStatusbar = (StatusBar) Dependency.get(StatusBar.class);
        this.mHandler = handler;
        SettingsObserver settingsObserver = new SettingsObserver(handler);
        settingsObserver.updateSettings();
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        this.mMusicStreamMuted = isMusicMuted(3);
        this.mPowerSaveModeEnabled = ((PowerManager) context.getSystemService("power")).isPowerSaveMode();
        settingsObserver.register();
        this.mStreamHandler = new VisualizerStreamHandler(context, this, listener, executor);
        this.mPulseView = new PulseView(context, this);
        this.mColorController = new ColorController(context, handler);
        loadRenderer();
        ((CommandQueue) Dependency.get(CommandQueue.class)).addCallback((CommandQueue.Callbacks) this);
        IntentFilter intentFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.os.action.POWER_SAVE_MODE_CHANGING");
        intentFilter.addAction("android.media.STREAM_MUTE_CHANGED_ACTION");
        intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
        context.registerReceiverAsUser(broadcastReceiver, UserHandle.ALL, intentFilter, null, null);
    }

    public void attachPulseTo(FrameLayout frameLayout) {
        if (frameLayout != null && frameLayout.findViewWithTag("PulseView") == null) {
            frameLayout.addView(this.mPulseView);
            this.mAttached = true;
            log("attachPulseTo() ");
            doLinkage();
        }
    }

    public void detachPulseFrom(FrameLayout frameLayout, boolean z) {
        if (frameLayout == null || frameLayout.findViewWithTag("PulseView") == null) {
            return;
        }
        frameLayout.removeView(this.mPulseView);
        this.mAttached = z;
        log("detachPulseFrom() ");
        doLinkage();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyStateListeners(boolean z) {
        for (PulseController.PulseStateListener pulseStateListener : this.mStateListeners) {
            if (pulseStateListener != null) {
                pulseStateListener.onPulseStateChanged(z);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void loadRenderer() {
        boolean zShouldDrawPulse = shouldDrawPulse();
        if (zShouldDrawPulse) {
            this.mStreamHandler.pause();
        }
        Renderer renderer = this.mRenderer;
        if (renderer != null) {
            renderer.destroy();
            this.mRenderer = null;
        }
        Renderer renderer2 = getRenderer();
        this.mRenderer = renderer2;
        this.mColorController.setRenderer(renderer2);
        this.mRenderer.setLeftInLandscape(this.mLeftInLandscape);
        if (zShouldDrawPulse) {
            this.mRenderer.onStreamAnalyzed(true);
            this.mStreamHandler.resume();
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void screenPinningStateChanged(boolean z) {
        this.mScreenPinningEnabled = z;
        doLinkage();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void leftInLandscapeChanged(boolean z) {
        if (this.mLeftInLandscape != z) {
            this.mLeftInLandscape = z;
            Renderer renderer = this.mRenderer;
            if (renderer != null) {
                renderer.setLeftInLandscape(z);
            }
        }
    }

    public boolean shouldDrawPulse() {
        return this.mLinked && this.mStreamHandler.isValidStream() && this.mRenderer != null;
    }

    public void onDraw(Canvas canvas) {
        if (shouldDrawPulse()) {
            this.mRenderer.draw(canvas);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void turnOnPulse() {
        if (shouldDrawPulse()) {
            this.mStreamHandler.resume();
        }
    }

    void onSizeChanged(int i, int i2, int i3, int i4) {
        Renderer renderer = this.mRenderer;
        if (renderer != null) {
            renderer.onSizeChanged(i, i2, i3, i4);
        }
    }

    private Renderer getRenderer() {
        if (this.mPulseStyle == 1) {
            return new SolidLineRenderer(this.mContext, this.mHandler, this.mPulseView, this.mColorController);
        }
        return new FadingBlockRenderer(this.mContext, this.mHandler, this.mPulseView, this.mColorController);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isMusicMuted(int i) {
        return i == 3 && (this.mAudioManager.isStreamMute(i) || this.mAudioManager.getStreamVolume(i) == 0);
    }

    private static void setVisualizerLocked(boolean z) {
        try {
            IAudioService.Stub.asInterface(ServiceManager.getService("audio")).setVisualizerLocked(z);
        } catch (RemoteException unused) {
            Log.e(TAG, "Error setting visualizer lock");
        }
    }

    private boolean isUnlinkRequired() {
        return !(this.mScreenOn || this.mAmbPulseEnabled) || this.mPowerSaveModeEnabled || this.mMusicStreamMuted || this.mScreenPinningEnabled || !this.mAttached;
    }

    private boolean isAbleToLink() {
        return (this.mScreenOn || this.mAmbPulseEnabled) && this.mIsMediaPlaying && !this.mPowerSaveModeEnabled && !this.mMusicStreamMuted && !this.mScreenPinningEnabled && this.mAttached;
    }

    private void doUnlinkVisualizer() {
        VisualizerStreamHandler visualizerStreamHandler = this.mStreamHandler;
        if (visualizerStreamHandler == null || !this.mLinked) {
            return;
        }
        visualizerStreamHandler.unlink();
        setVisualizerLocked(false);
        this.mLinked = false;
        Renderer renderer = this.mRenderer;
        if (renderer != null) {
            renderer.onVisualizerLinkChanged(false);
        }
        this.mPulseView.postInvalidate();
        notifyStateListeners(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void doLinkage() {
        if (isUnlinkRequired()) {
            if (this.mLinked) {
                doUnlinkVisualizer();
            }
        } else if (isAbleToLink()) {
            doLinkVisualizer();
        } else if (this.mLinked) {
            doUnlinkVisualizer();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void doSilentUnlinkVisualizer() {
        VisualizerStreamHandler visualizerStreamHandler = this.mStreamHandler;
        if (visualizerStreamHandler == null || !this.mLinked) {
            return;
        }
        visualizerStreamHandler.unlink();
        setVisualizerLocked(false);
        this.mLinked = false;
    }

    private void doLinkVisualizer() {
        if (this.mStreamHandler == null || this.mLinked) {
            return;
        }
        setVisualizerLocked(true);
        this.mStreamHandler.link();
        this.mLinked = true;
        Renderer renderer = this.mRenderer;
        if (renderer != null) {
            renderer.onVisualizerLinkChanged(true);
        }
    }

    @Override // com.android.systemui.statusbar.NotificationMediaManager.MediaListener
    public void onPrimaryMetadataOrStateChanged(MediaMetadata mediaMetadata, int i) {
        boolean z = i == 3;
        if (this.mIsMediaPlaying != z) {
            this.mIsMediaPlaying = z;
            doLinkage();
        }
    }

    @Override // com.android.systemui.statusbar.NotificationMediaManager.MediaListener
    public void setMediaNotificationColor(boolean z, int i) {
        this.mColorController.setMediaNotificationColor(z, i);
    }

    public String toString() {
        return TAG + " " + getState();
    }

    private String getState() {
        return "isAbleToLink() = " + isAbleToLink() + " shouldDrawPulse() = " + shouldDrawPulse() + " mScreenOn = " + this.mScreenOn + " mIsMediaPlaying = " + this.mIsMediaPlaying + " mLinked = " + this.mLinked + " mPowerSaveModeEnabled = " + this.mPowerSaveModeEnabled + " mMusicStreamMuted = " + this.mMusicStreamMuted + " mScreenPinningEnabled = " + this.mScreenPinningEnabled + " mAttached = " + this.mAttached + " mStreamHandler.isValidStream() = " + this.mStreamHandler.isValidStream() + " ";
    }
}
