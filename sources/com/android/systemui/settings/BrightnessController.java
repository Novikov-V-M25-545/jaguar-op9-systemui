package com.android.systemui.settings;

import android.R;
import android.animation.ValueAnimator;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.util.Log;
import android.util.MathUtils;
import android.view.View;
import android.widget.ImageView;
import com.android.internal.BrightnessSynchronizer;
import com.android.internal.logging.MetricsLogger;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtilsInternal;
import com.android.settingslib.display.BrightnessUtils;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.plugins.SensorManagerPlugin;
import com.android.systemui.settings.ToggleSlider;
import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes.dex */
public class BrightnessController implements ToggleSlider.Listener {
    private volatile boolean mAutomatic;
    private final boolean mAutomaticAvailable;
    private final Handler mBackgroundHandler;
    private final BrightnessObserver mBrightnessObserver;
    private final Context mContext;
    private final ToggleSlider mControl;
    private boolean mControlValueInitialized;
    private final float mDefaultBacklight;
    private final float mDefaultBacklightForVr;
    private final DisplayManager mDisplayManager;
    private boolean mExternalChange;
    private final Handler mHandler;
    private final ImageView mIcon;
    private volatile boolean mIsVrModeEnabled;
    private boolean mListening;
    private final float mMaximumBacklight;
    private final float mMaximumBacklightForVr;
    private final float mMinimumBacklight;
    private final float mMinimumBacklightForVr;
    private ValueAnimator mSliderAnimator;
    private final CurrentUserTracker mUserTracker;
    private final IVrManager mVrManager;
    private static final Uri BRIGHTNESS_MODE_URI = Settings.System.getUriFor("screen_brightness_mode");
    private static final Uri BRIGHTNESS_URI = Settings.System.getUriFor("screen_brightness");
    private static final Uri BRIGHTNESS_FLOAT_URI = Settings.System.getUriFor("screen_brightness_float");
    private static final Uri BRIGHTNESS_FOR_VR_FLOAT_URI = Settings.System.getUriFor("screen_brightness_for_vr_float");
    private ArrayList<BrightnessStateChangeCallback> mChangeCallbacks = new ArrayList<>();
    private final Runnable mStartListeningRunnable = new Runnable() { // from class: com.android.systemui.settings.BrightnessController.1
        @Override // java.lang.Runnable
        public void run() {
            if (BrightnessController.this.mListening) {
                return;
            }
            BrightnessController.this.mListening = true;
            if (BrightnessController.this.mVrManager != null) {
                try {
                    BrightnessController.this.mVrManager.registerListener(BrightnessController.this.mVrStateCallbacks);
                    BrightnessController brightnessController = BrightnessController.this;
                    brightnessController.mIsVrModeEnabled = brightnessController.mVrManager.getVrModeState();
                } catch (RemoteException e) {
                    Log.e("StatusBar.BrightnessController", "Failed to register VR mode state listener: ", e);
                }
            }
            BrightnessController.this.mBrightnessObserver.startObserving();
            BrightnessController.this.mUserTracker.startTracking();
            BrightnessController.this.mUpdateModeRunnable.run();
            BrightnessController.this.mUpdateSliderRunnable.run();
            BrightnessController.this.mHandler.sendEmptyMessage(3);
        }
    };
    private final Runnable mStopListeningRunnable = new Runnable() { // from class: com.android.systemui.settings.BrightnessController.2
        @Override // java.lang.Runnable
        public void run() {
            if (BrightnessController.this.mListening) {
                BrightnessController.this.mListening = false;
                if (BrightnessController.this.mVrManager != null) {
                    try {
                        BrightnessController.this.mVrManager.unregisterListener(BrightnessController.this.mVrStateCallbacks);
                    } catch (RemoteException e) {
                        Log.e("StatusBar.BrightnessController", "Failed to unregister VR mode state listener: ", e);
                    }
                }
                BrightnessController.this.mBrightnessObserver.stopObserving();
                BrightnessController.this.mUserTracker.stopTracking();
                BrightnessController.this.mHandler.sendEmptyMessage(4);
            }
        }
    };
    private final Runnable mUpdateModeRunnable = new Runnable() { // from class: com.android.systemui.settings.BrightnessController.3
        @Override // java.lang.Runnable
        public void run() {
            if (BrightnessController.this.mAutomaticAvailable) {
                int intForUser = Settings.System.getIntForUser(BrightnessController.this.mContext.getContentResolver(), "screen_brightness_mode", 0, -2);
                BrightnessController.this.mAutomatic = intForUser != 0;
                BrightnessController.this.mHandler.obtainMessage(0, Integer.valueOf(BrightnessController.this.mAutomatic ? 1 : 0)).sendToTarget();
                return;
            }
            BrightnessController.this.mHandler.obtainMessage(2, 0).sendToTarget();
            BrightnessController.this.mHandler.obtainMessage(0, 0).sendToTarget();
        }
    };
    private final Runnable mUpdateSliderRunnable = new Runnable() { // from class: com.android.systemui.settings.BrightnessController.4
        @Override // java.lang.Runnable
        public void run() {
            float floatForUser;
            boolean z = BrightnessController.this.mIsVrModeEnabled;
            if (z) {
                floatForUser = Settings.System.getFloatForUser(BrightnessController.this.mContext.getContentResolver(), "screen_brightness_for_vr_float", BrightnessController.this.mDefaultBacklightForVr, -2);
            } else {
                floatForUser = Settings.System.getFloatForUser(BrightnessController.this.mContext.getContentResolver(), "screen_brightness_float", BrightnessController.this.mDefaultBacklight, -2);
            }
            BrightnessController.this.mHandler.obtainMessage(1, Float.floatToIntBits(floatForUser), z ? 1 : 0).sendToTarget();
        }
    };
    private final IVrStateCallbacks mVrStateCallbacks = new IVrStateCallbacks.Stub() { // from class: com.android.systemui.settings.BrightnessController.5
        public void onVrStateChanged(boolean z) {
            BrightnessController.this.mHandler.obtainMessage(5, z ? 1 : 0, 0).sendToTarget();
        }
    };

    public interface BrightnessStateChangeCallback {
        void onBrightnessLevelChanged();
    }

    @Override // com.android.systemui.settings.ToggleSlider.Listener
    public void onInit(ToggleSlider toggleSlider) {
    }

    private class BrightnessObserver extends ContentObserver {
        public BrightnessObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            onChange(z, null);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            if (z) {
                return;
            }
            if (BrightnessController.BRIGHTNESS_MODE_URI.equals(uri)) {
                BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateModeRunnable);
                BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateSliderRunnable);
            } else if (!BrightnessController.BRIGHTNESS_FLOAT_URI.equals(uri) && !BrightnessController.BRIGHTNESS_FOR_VR_FLOAT_URI.equals(uri)) {
                BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateModeRunnable);
                BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateSliderRunnable);
            } else {
                BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateSliderRunnable);
            }
            Iterator it = BrightnessController.this.mChangeCallbacks.iterator();
            while (it.hasNext()) {
                ((BrightnessStateChangeCallback) it.next()).onBrightnessLevelChanged();
            }
        }

        public void startObserving() {
            ContentResolver contentResolver = BrightnessController.this.mContext.getContentResolver();
            contentResolver.unregisterContentObserver(this);
            contentResolver.registerContentObserver(BrightnessController.BRIGHTNESS_MODE_URI, false, this, -1);
            contentResolver.registerContentObserver(BrightnessController.BRIGHTNESS_URI, false, this, -1);
            contentResolver.registerContentObserver(BrightnessController.BRIGHTNESS_FLOAT_URI, false, this, -1);
            contentResolver.registerContentObserver(BrightnessController.BRIGHTNESS_FOR_VR_FLOAT_URI, false, this, -1);
        }

        public void stopObserving() {
            BrightnessController.this.mContext.getContentResolver().unregisterContentObserver(this);
        }
    }

    public BrightnessController(Context context, ImageView imageView, ToggleSlider toggleSlider, BroadcastDispatcher broadcastDispatcher) {
        Handler handler = new Handler() { // from class: com.android.systemui.settings.BrightnessController.6
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                boolean z = true;
                BrightnessController.this.mExternalChange = true;
                try {
                    switch (message.what) {
                        case 0:
                            BrightnessController brightnessController = BrightnessController.this;
                            if (message.arg1 == 0) {
                                z = false;
                            }
                            brightnessController.updateIcon(z);
                            break;
                        case 1:
                            BrightnessController brightnessController2 = BrightnessController.this;
                            float fIntBitsToFloat = Float.intBitsToFloat(message.arg1);
                            if (message.arg2 == 0) {
                                z = false;
                            }
                            brightnessController2.updateSlider(fIntBitsToFloat, z);
                            break;
                        case 2:
                            ToggleSlider toggleSlider2 = BrightnessController.this.mControl;
                            if (message.arg1 == 0) {
                                z = false;
                            }
                            toggleSlider2.setChecked(z);
                            break;
                        case SensorManagerPlugin.Sensor.TYPE_SWIPE /* 3 */:
                            BrightnessController.this.mControl.setOnChangedListener(BrightnessController.this);
                            break;
                        case 4:
                            BrightnessController.this.mControl.setOnChangedListener(null);
                            break;
                        case 5:
                            BrightnessController brightnessController3 = BrightnessController.this;
                            if (message.arg1 == 0) {
                                z = false;
                            }
                            brightnessController3.updateVrMode(z);
                            break;
                        case 6:
                            ((ToggleSliderView) BrightnessController.this.mControl).setEnforcedAdmin((RestrictedLockUtils.EnforcedAdmin) message.obj);
                            break;
                        default:
                            super.handleMessage(message);
                            break;
                    }
                } finally {
                    BrightnessController.this.mExternalChange = false;
                }
            }
        };
        this.mHandler = handler;
        this.mContext = context;
        this.mIcon = imageView;
        this.mControl = toggleSlider;
        toggleSlider.setMax(65535);
        this.mBackgroundHandler = new Handler((Looper) Dependency.get(Dependency.BG_LOOPER));
        this.mUserTracker = new CurrentUserTracker(broadcastDispatcher) { // from class: com.android.systemui.settings.BrightnessController.7
            @Override // com.android.systemui.settings.CurrentUserTracker
            public void onUserSwitched(int i) {
                BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateModeRunnable);
                BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateSliderRunnable);
            }
        };
        this.mBrightnessObserver = new BrightnessObserver(handler);
        PowerManager powerManager = (PowerManager) context.getSystemService(PowerManager.class);
        this.mMinimumBacklight = powerManager.getBrightnessConstraint(0);
        this.mMaximumBacklight = powerManager.getBrightnessConstraint(1);
        this.mDefaultBacklight = powerManager.getBrightnessConstraint(2);
        this.mMinimumBacklightForVr = powerManager.getBrightnessConstraint(5);
        this.mMaximumBacklightForVr = powerManager.getBrightnessConstraint(6);
        this.mDefaultBacklightForVr = powerManager.getBrightnessConstraint(7);
        this.mAutomaticAvailable = context.getResources().getBoolean(R.bool.config_allow_pin_storage_for_unattended_reboot);
        this.mDisplayManager = (DisplayManager) context.getSystemService(DisplayManager.class);
        this.mVrManager = IVrManager.Stub.asInterface(ServiceManager.getService("vrmanager"));
        imageView.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.settings.BrightnessController.8
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Settings.System.putIntForUser(BrightnessController.this.mContext.getContentResolver(), "screen_brightness_mode", !BrightnessController.this.mAutomatic ? 1 : 0, -2);
            }
        });
    }

    public void registerCallbacks() {
        this.mBackgroundHandler.post(this.mStartListeningRunnable);
    }

    public void unregisterCallbacks() {
        this.mBackgroundHandler.post(this.mStopListeningRunnable);
        this.mControlValueInitialized = false;
    }

    @Override // com.android.systemui.settings.ToggleSlider.Listener
    public void onChanged(ToggleSlider toggleSlider, boolean z, boolean z2, int i, boolean z3) {
        int i2;
        float f;
        float f2;
        final String str;
        updateIcon(this.mAutomatic);
        if (this.mExternalChange) {
            return;
        }
        ValueAnimator valueAnimator = this.mSliderAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        if (this.mIsVrModeEnabled) {
            i2 = 498;
            f = this.mMinimumBacklightForVr;
            f2 = this.mMaximumBacklightForVr;
            str = "screen_brightness_for_vr_float";
        } else {
            i2 = this.mAutomatic ? 219 : 218;
            f = this.mMinimumBacklight;
            f2 = this.mMaximumBacklight;
            str = "screen_brightness_float";
        }
        final float fMin = MathUtils.min(BrightnessUtils.convertGammaToLinearFloat(i, f, f2), 1.0f);
        if (z3) {
            Context context = this.mContext;
            MetricsLogger.action(context, i2, BrightnessSynchronizer.brightnessFloatToInt(context, fMin));
        }
        setBrightness(fMin);
        if (!z) {
            AsyncTask.execute(new Runnable() { // from class: com.android.systemui.settings.BrightnessController.9
                @Override // java.lang.Runnable
                public void run() {
                    Settings.System.putFloatForUser(BrightnessController.this.mContext.getContentResolver(), str, fMin, -2);
                }
            });
        }
        Iterator<BrightnessStateChangeCallback> it = this.mChangeCallbacks.iterator();
        while (it.hasNext()) {
            it.next().onBrightnessLevelChanged();
        }
    }

    public void checkRestrictionAndSetEnabled() {
        this.mHandler.obtainMessage(6, RestrictedLockUtilsInternal.checkIfRestrictionEnforced(this.mContext, "no_config_brightness", this.mUserTracker.getCurrentUserId())).sendToTarget();
    }

    private void setBrightness(float f) {
        this.mDisplayManager.setTemporaryBrightness(f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateIcon(boolean z) {
        int i;
        ImageView imageView = this.mIcon;
        if (imageView != null) {
            if (this.mAutomatic) {
                i = com.android.systemui.R.drawable.ic_qs_brightness_auto_on;
            } else {
                i = com.android.systemui.R.drawable.ic_qs_brightness_auto_off;
            }
            imageView.setImageResource(i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateVrMode(boolean z) {
        if (this.mIsVrModeEnabled != z) {
            this.mIsVrModeEnabled = z;
            this.mBackgroundHandler.post(this.mUpdateSliderRunnable);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateSlider(float f, boolean z) {
        float f2;
        float f3;
        if (z) {
            f2 = this.mMinimumBacklightForVr;
            f3 = this.mMaximumBacklightForVr;
        } else {
            f2 = this.mMinimumBacklight;
            f3 = this.mMaximumBacklight;
        }
        if (BrightnessSynchronizer.floatEquals(f, BrightnessUtils.convertGammaToLinearFloat(this.mControl.getValue(), f2, f3))) {
            return;
        }
        animateSliderTo(BrightnessUtils.convertLinearToGammaFloat(f, f2, f3));
    }

    private void animateSliderTo(int i) {
        if (!this.mControlValueInitialized) {
            this.mControl.setValue(i);
            this.mControlValueInitialized = true;
        }
        ValueAnimator valueAnimator = this.mSliderAnimator;
        if (valueAnimator != null && valueAnimator.isStarted()) {
            this.mSliderAnimator.cancel();
        }
        ValueAnimator valueAnimatorOfInt = ValueAnimator.ofInt(this.mControl.getValue(), i);
        this.mSliderAnimator = valueAnimatorOfInt;
        valueAnimatorOfInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.settings.BrightnessController$$ExternalSyntheticLambda0
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator2) {
                this.f$0.lambda$animateSliderTo$0(valueAnimator2);
            }
        });
        this.mSliderAnimator.setDuration((Math.abs(this.mControl.getValue() - i) * 3000) / 65535);
        this.mSliderAnimator.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$animateSliderTo$0(ValueAnimator valueAnimator) {
        this.mExternalChange = true;
        this.mControl.setValue(((Integer) valueAnimator.getAnimatedValue()).intValue());
        this.mExternalChange = false;
    }
}
