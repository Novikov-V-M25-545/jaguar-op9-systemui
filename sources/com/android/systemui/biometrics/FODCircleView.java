package com.android.systemui.biometrics;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.biometrics.BiometricSourceType;
import android.os.Handler;
import android.os.IHwBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.pocket.IPocketCallback;
import android.pocket.PocketManager;
import android.provider.Settings;
import android.view.WindowManager;
import android.widget.ImageView;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.tuner.TunerService;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;
import lineageos.hardware.LineageHardwareManager;
import vendor.lineage.biometrics.fingerprint.inscreen.V1_0.IFingerprintInscreen;
import vendor.lineage.biometrics.fingerprint.inscreen.V1_0.IFingerprintInscreenCallback;

/* loaded from: classes.dex */
public class FODCircleView extends ImageView implements TunerService.Tunable, ConfigurationController.ConfigurationListener {
    private final int[] ICON_STYLES;
    private final int[] PRESSED_COLOR;
    private Timer mBurnInProtectionTimer;
    private boolean mCanUnlockWithFp;
    private int mColorBackground;
    private boolean mCutoutMasked;
    private int mDefaultPressedColor;
    private boolean mDozeEnabled;
    private boolean mDozeEnabledByDefault;
    private final int mDreamingMaxOffset;
    private int mDreamingOffsetX;
    private int mDreamingOffsetY;
    private FODAnimation mFODAnimation;
    private boolean mFading;
    private IFingerprintInscreenCallback mFingerprintInscreenCallback;
    private IFingerprintInscreen mFingerprintInscreenDaemon;
    private int mFodAnim;
    private boolean mFodGestureEnable;
    private Handler mHandler;
    private boolean mIsBiometricRunning;
    private boolean mIsBouncer;
    private boolean mIsCircleShowing;
    private boolean mIsDeviceInPocket;
    private boolean mIsDreaming;
    private boolean mIsKeyguard;
    private boolean mIsRecognizingAnimEnabled;
    private LockPatternUtils mLockPatternUtils;
    private KeyguardUpdateMonitorCallback mMonitorCallback;
    private final int mNavigationBarSize;
    private final Paint mPaintFingerprintBackground;
    private final WindowManager.LayoutParams mParams;
    private final IPocketCallback mPocketCallback;
    private boolean mPocketCallbackAdded;
    private PocketManager mPocketManager;
    private final int mPositionX;
    private final int mPositionY;
    private PowerManager mPowerManager;
    private boolean mPressPending;
    private int mPressedColor;
    private final WindowManager.LayoutParams mPressedParams;
    private final ImageView mPressedView;
    private boolean mScreenTurnedOn;
    private int mSelectedIcon;
    private final boolean mShouldBoostBrightness;
    private final int mSize;
    private int mStatusbarHeight;
    private boolean mTouchedOutside;
    private KeyguardUpdateMonitor mUpdateMonitor;
    private PowerManager.WakeLock mWakeLock;
    private final WindowManager mWindowManager;

    static /* synthetic */ int access$2620(FODCircleView fODCircleView, int i) {
        int i2 = fODCircleView.mDreamingOffsetX - i;
        fODCircleView.mDreamingOffsetX = i2;
        return i2;
    }

    static /* synthetic */ int access$2820(FODCircleView fODCircleView, int i) {
        int i2 = fODCircleView.mDreamingOffsetY - i;
        fODCircleView.mDreamingOffsetY = i2;
        return i2;
    }

    /* renamed from: com.android.systemui.biometrics.FODCircleView$1, reason: invalid class name */
    class AnonymousClass1 extends IFingerprintInscreenCallback.Stub {
        AnonymousClass1() {
        }

        @Override // vendor.lineage.biometrics.fingerprint.inscreen.V1_0.IFingerprintInscreenCallback
        public void onFingerDown() {
            if (!FODCircleView.this.mFodGestureEnable || FODCircleView.this.mScreenTurnedOn) {
                FODCircleView.this.mHandler.post(new Runnable() { // from class: com.android.systemui.biometrics.FODCircleView$1$$ExternalSyntheticLambda1
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onFingerDown$2();
                    }
                });
                return;
            }
            if (FODCircleView.this.mDozeEnabled) {
                FODCircleView.this.mHandler.post(new Runnable() { // from class: com.android.systemui.biometrics.FODCircleView$1$$ExternalSyntheticLambda3
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onFingerDown$0();
                    }
                });
            } else {
                FODCircleView.this.mWakeLock.acquire(3000L);
                FODCircleView.this.mHandler.post(new Runnable() { // from class: com.android.systemui.biometrics.FODCircleView$1$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onFingerDown$1();
                    }
                });
            }
            FODCircleView.this.mPressPending = true;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onFingerDown$0() {
            ((ImageView) FODCircleView.this).mContext.sendBroadcast(new Intent("com.android.systemui.doze.pulse"));
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onFingerDown$1() {
            FODCircleView.this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), 4, FODCircleView.class.getSimpleName());
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onFingerDown$2() {
            FODCircleView.this.showCircle();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onFingerUp$3() {
            FODCircleView.this.hideCircle();
        }

        @Override // vendor.lineage.biometrics.fingerprint.inscreen.V1_0.IFingerprintInscreenCallback
        public void onFingerUp() {
            FODCircleView.this.mHandler.post(new Runnable() { // from class: com.android.systemui.biometrics.FODCircleView$1$$ExternalSyntheticLambda2
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onFingerUp$3();
                }
            });
        }
    }

    /* renamed from: com.android.systemui.biometrics.FODCircleView$2, reason: invalid class name */
    class AnonymousClass2 extends KeyguardUpdateMonitorCallback {
        AnonymousClass2() {
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricAuthenticated(int i, BiometricSourceType biometricSourceType, boolean z) {
            if (biometricSourceType != BiometricSourceType.FINGERPRINT) {
                FODCircleView.this.hide();
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricRunningStateChanged(boolean z, BiometricSourceType biometricSourceType) {
            if (biometricSourceType == BiometricSourceType.FINGERPRINT) {
                FODCircleView.this.mIsBiometricRunning = z;
                if (FODCircleView.this.mIsBiometricRunning) {
                    return;
                }
                FODCircleView.this.hide();
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onDreamingStateChanged(boolean z) {
            FODCircleView.this.mIsDreaming = z;
            FODCircleView.this.updateAlpha();
            if (FODCircleView.this.mIsKeyguard && FODCircleView.this.mIsBiometricRunning) {
                FODCircleView.this.show();
                FODCircleView.this.updateAlpha();
            }
            if (!z) {
                if (FODCircleView.this.mBurnInProtectionTimer != null) {
                    FODCircleView.this.mBurnInProtectionTimer.cancel();
                }
            } else {
                FODCircleView.this.mBurnInProtectionTimer = new Timer();
                FODCircleView.this.mBurnInProtectionTimer.schedule(new BurnInProtectionTask(FODCircleView.this, null), 0L, 60000L);
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardVisibilityChanged(boolean z) {
            FODCircleView.this.mIsKeyguard = z;
            if (z) {
                FODCircleView.this.updateAlpha();
            } else {
                FODCircleView.this.hide();
            }
            if (FODCircleView.this.mFODAnimation != null && FODCircleView.this.mIsRecognizingAnimEnabled) {
                FODCircleView.this.mFODAnimation.setAnimationKeyguard(FODCircleView.this.mIsKeyguard);
            }
            FODCircleView.this.handlePocketManagerCallback(z);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardBouncerChanged(boolean z) {
            FODCircleView.this.mIsBouncer = z;
            if (FODCircleView.this.mIsBiometricRunning) {
                FODCircleView fODCircleView = FODCircleView.this;
                KeyguardUpdateMonitor unused = fODCircleView.mUpdateMonitor;
                if (fODCircleView.isPinOrPattern(KeyguardUpdateMonitor.getCurrentUser()) || !z) {
                    FODCircleView.this.show();
                } else {
                    FODCircleView.this.hide();
                }
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onScreenTurnedOff() {
            FODCircleView.this.mScreenTurnedOn = false;
            if (!FODCircleView.this.mFodGestureEnable) {
                FODCircleView.this.hide();
            } else {
                FODCircleView.this.hideCircle();
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onStartedWakingUp() {
            if (FODCircleView.this.mIsBiometricRunning) {
                FODCircleView.this.show();
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onScreenTurnedOn() {
            if (FODCircleView.this.mIsBiometricRunning) {
                if (FODCircleView.this.mFodGestureEnable) {
                    if (FODCircleView.this.mPressPending) {
                        FODCircleView.this.mHandler.post(new Runnable() { // from class: com.android.systemui.biometrics.FODCircleView$2$$ExternalSyntheticLambda1
                            @Override // java.lang.Runnable
                            public final void run() {
                                this.f$0.lambda$onScreenTurnedOn$0();
                            }
                        });
                        FODCircleView.this.mPressPending = false;
                    }
                } else {
                    FODCircleView.this.show();
                }
            }
            FODCircleView.this.mScreenTurnedOn = true;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onScreenTurnedOn$0() {
            FODCircleView.this.showCircle();
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onStrongAuthStateChanged(int i) {
            FODCircleView fODCircleView = FODCircleView.this;
            fODCircleView.mCanUnlockWithFp = fODCircleView.canUnlockWithFp();
            if (FODCircleView.this.mCanUnlockWithFp) {
                return;
            }
            FODCircleView.this.hide();
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricHelp(int i, String str, BiometricSourceType biometricSourceType) {
            if (i == -1 && FODCircleView.this.mFODAnimation != null && FODCircleView.this.mIsRecognizingAnimEnabled) {
                FODCircleView.this.mHandler.post(new Runnable() { // from class: com.android.systemui.biometrics.FODCircleView$2$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onBiometricHelp$1();
                    }
                });
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onBiometricHelp$1() {
            FODCircleView.this.mFODAnimation.hideFODanimation();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean canUnlockWithFp() {
        int currentUser = ActivityManager.getCurrentUser();
        boolean zIsUnlockingWithBiometricsPossible = this.mUpdateMonitor.isUnlockingWithBiometricsPossible(currentUser);
        KeyguardUpdateMonitor.StrongAuthTracker strongAuthTracker = this.mUpdateMonitor.getStrongAuthTracker();
        int strongAuthForUser = strongAuthTracker.getStrongAuthForUser(currentUser);
        if (zIsUnlockingWithBiometricsPossible && !strongAuthTracker.hasUserAuthenticatedSinceBoot()) {
            return false;
        }
        if (zIsUnlockingWithBiometricsPossible && (strongAuthForUser & 16) != 0) {
            return false;
        }
        if (zIsUnlockingWithBiometricsPossible && (strongAuthForUser & 2) != 0) {
            return false;
        }
        if (!zIsUnlockingWithBiometricsPossible || (strongAuthForUser & 8) == 0) {
            return !zIsUnlockingWithBiometricsPossible || (strongAuthForUser & 32) == 0;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePocketManagerCallback(boolean z) {
        if (!z) {
            if (this.mPocketCallbackAdded) {
                this.mPocketCallbackAdded = false;
                this.mPocketManager.removeCallback(this.mPocketCallback);
                return;
            }
            return;
        }
        if (this.mPocketCallbackAdded) {
            return;
        }
        this.mPocketCallbackAdded = true;
        this.mPocketManager.addCallback(this.mPocketCallback);
    }

    public FODCircleView(Context context) throws Resources.NotFoundException {
        super(context);
        Paint paint = new Paint();
        this.mPaintFingerprintBackground = paint;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        this.mParams = layoutParams;
        WindowManager.LayoutParams layoutParams2 = new WindowManager.LayoutParams();
        this.mPressedParams = layoutParams2;
        this.mFodAnim = 0;
        this.ICON_STYLES = new int[]{R.drawable.fod_icon_default, R.drawable.fod_icon_default_0, R.drawable.fod_icon_default_1, R.drawable.fod_icon_default_2, R.drawable.fod_icon_default_3, R.drawable.fod_icon_dragon_black_flat, R.drawable.fod_icon_neon_arc_gray, R.drawable.fod_icon_neon_triangle};
        this.PRESSED_COLOR = new int[]{R.drawable.fod_icon_pressed_white, R.drawable.fod_icon_pressed_cyan};
        this.mFingerprintInscreenCallback = new AnonymousClass1();
        this.mMonitorCallback = new AnonymousClass2();
        this.mPocketCallbackAdded = false;
        this.mPocketCallback = new IPocketCallback.Stub() { // from class: com.android.systemui.biometrics.FODCircleView.3
            public void onStateChanged(boolean z, int i) {
                boolean unused = FODCircleView.this.mIsDeviceInPocket;
                if (i == 0) {
                    FODCircleView.this.mIsDeviceInPocket = z;
                } else {
                    FODCircleView.this.mIsDeviceInPocket = false;
                }
            }
        };
        IFingerprintInscreen fingerprintInScreenDaemon = getFingerprintInScreenDaemon();
        if (fingerprintInScreenDaemon == null) {
            throw new RuntimeException("Unable to get IFingerprintInscreen");
        }
        try {
            this.mShouldBoostBrightness = fingerprintInScreenDaemon.shouldBoostBrightness();
            int positionX = fingerprintInScreenDaemon.getPositionX();
            this.mPositionX = positionX;
            int positionY = fingerprintInScreenDaemon.getPositionY();
            this.mPositionY = positionY;
            int size = fingerprintInScreenDaemon.getSize();
            this.mSize = size;
            Resources resources = context.getResources();
            this.mColorBackground = resources.getColor(R.color.config_fodColorBackground);
            this.mDefaultPressedColor = resources.getInteger(android.R.integer.config_defaultRefreshRateInHbmHdr);
            paint.setColor(this.mColorBackground);
            paint.setAntiAlias(true);
            PowerManager powerManager = (PowerManager) context.getSystemService(PowerManager.class);
            this.mPowerManager = powerManager;
            this.mWakeLock = powerManager.newWakeLock(1, FODCircleView.class.getSimpleName());
            WindowManager windowManager = (WindowManager) context.getSystemService(WindowManager.class);
            this.mWindowManager = windowManager;
            this.mNavigationBarSize = resources.getDimensionPixelSize(R.dimen.navigation_bar_size);
            this.mDreamingMaxOffset = (int) (size * 0.1f);
            this.mHandler = new Handler(Looper.getMainLooper());
            layoutParams.height = size;
            layoutParams.width = size;
            layoutParams.format = -3;
            layoutParams.packageName = "android";
            layoutParams.type = 2042;
            layoutParams.flags = 262408;
            layoutParams.gravity = 51;
            layoutParams2.copyFrom(layoutParams);
            layoutParams2.type = 2043;
            layoutParams2.flags |= LineageHardwareManager.FEATURE_PICTURE_ADJUSTMENT;
            layoutParams.setTitle("Fingerprint on display");
            layoutParams2.setTitle("Fingerprint on display.touched");
            this.mPressedView = new ImageView(context) { // from class: com.android.systemui.biometrics.FODCircleView.4
                @Override // android.widget.ImageView, android.view.View
                protected void onDraw(Canvas canvas) {
                    if (FODCircleView.this.mIsCircleShowing) {
                        setImageResource(FODCircleView.this.PRESSED_COLOR[FODCircleView.this.mPressedColor]);
                    }
                    super.onDraw(canvas);
                }
            };
            windowManager.addView(this, layoutParams);
            hide();
            this.mLockPatternUtils = new LockPatternUtils(((ImageView) this).mContext);
            KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
            this.mUpdateMonitor = keyguardUpdateMonitor;
            keyguardUpdateMonitor.registerCallback(this.mMonitorCallback);
            this.mCanUnlockWithFp = canUnlockWithFp();
            this.mPocketManager = (PocketManager) context.getSystemService("pocket");
            if (com.android.internal.util.crdroid.Utils.isPackageInstalled(context, context.getResources().getString(android.R.string.config_defaultContentProtectionService))) {
                this.mFODAnimation = new FODAnimation(context, positionX, positionY);
            }
            this.mDozeEnabledByDefault = ((ImageView) this).mContext.getResources().getBoolean(android.R.bool.config_canRemoveFirstAccount);
            updateCutoutFlags();
            ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
            ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "system:fod_gesture", "doze_enabled", "system:fod_anim", "system:fod_recognizing_animation", "system:fod_icon", "system:fod_color");
        } catch (RemoteException unused) {
            throw new RuntimeException("Failed to retrieve FOD circle position or size");
        }
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) throws PackageManager.NameNotFoundException {
        str.hashCode();
        switch (str) {
            case "system:fod_color":
                this.mPressedColor = TunerService.parseInteger(str2, this.mDefaultPressedColor);
                if (useAccentDefaultColor()) {
                    setColorFilter(com.android.internal.util.crdroid.Utils.getColorAccentDefaultColor(getContext()));
                    break;
                }
                break;
            case "system:fod_gesture":
                this.mFodGestureEnable = TunerService.parseIntegerSwitch(str2, false);
                break;
            case "doze_enabled":
                this.mDozeEnabled = TunerService.parseIntegerSwitch(str2, this.mDozeEnabledByDefault);
                break;
            case "system:fod_anim":
                int integer = TunerService.parseInteger(str2, 0);
                this.mFodAnim = integer;
                FODAnimation fODAnimation = this.mFODAnimation;
                if (fODAnimation != null) {
                    fODAnimation.update(this.mIsRecognizingAnimEnabled, integer);
                    break;
                }
                break;
            case "system:fod_icon":
                this.mSelectedIcon = TunerService.parseInteger(str2, 0);
                break;
            case "system:fod_recognizing_animation":
                boolean integerSwitch = TunerService.parseIntegerSwitch(str2, false);
                this.mIsRecognizingAnimEnabled = integerSwitch;
                FODAnimation fODAnimation2 = this.mFODAnimation;
                if (fODAnimation2 != null) {
                    fODAnimation2.update(integerSwitch, this.mFodAnim);
                    break;
                }
                break;
        }
    }

    @Override // android.widget.ImageView, android.view.View
    protected void onDraw(Canvas canvas) {
        if (!this.mIsCircleShowing) {
            canvas.drawCircle(r0 / 2, r0 / 2, this.mSize / 2.0f, this.mPaintFingerprintBackground);
        }
        super.onDraw(canvas);
    }

    /* JADX WARN: Removed duplicated region for block: B:11:0x0021  */
    @Override // android.view.View
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean onTouchEvent(android.view.MotionEvent r8) {
        /*
            r7 = this;
            r0 = 0
            float r1 = r8.getAxisValue(r0)
            r2 = 1
            float r3 = r8.getAxisValue(r2)
            r4 = 0
            int r5 = (r1 > r4 ? 1 : (r1 == r4 ? 0 : -1))
            if (r5 <= 0) goto L21
            int r5 = r7.mSize
            float r6 = (float) r5
            int r1 = (r1 > r6 ? 1 : (r1 == r6 ? 0 : -1))
            if (r1 >= 0) goto L21
            int r1 = (r3 > r4 ? 1 : (r3 == r4 ? 0 : -1))
            if (r1 <= 0) goto L21
            float r1 = (float) r5
            int r1 = (r3 > r1 ? 1 : (r3 == r1 ? 0 : -1))
            if (r1 >= 0) goto L21
            r1 = r2
            goto L22
        L21:
            r1 = r0
        L22:
            r7.mTouchedOutside = r0
            int r3 = r8.getAction()
            r4 = 4
            if (r3 != r4) goto L2e
            r7.mTouchedOutside = r2
            return r2
        L2e:
            int r3 = r8.getAction()
            if (r3 != 0) goto L3a
            if (r1 == 0) goto L3a
            r7.showCircle()
            return r2
        L3a:
            int r1 = r8.getAction()
            if (r1 != r2) goto L44
            r7.hideCircle()
            return r2
        L44:
            int r8 = r8.getAction()
            r1 = 2
            if (r8 != r1) goto L4c
            return r2
        L4c:
            com.android.systemui.biometrics.FODAnimation r8 = r7.mFODAnimation
            if (r8 == 0) goto L5e
            boolean r8 = r7.mIsRecognizingAnimEnabled
            if (r8 == 0) goto L5e
            android.os.Handler r8 = r7.mHandler
            com.android.systemui.biometrics.FODCircleView$$ExternalSyntheticLambda1 r1 = new com.android.systemui.biometrics.FODCircleView$$ExternalSyntheticLambda1
            r1.<init>()
            r8.post(r1)
        L5e:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.biometrics.FODCircleView.onTouchEvent(android.view.MotionEvent):boolean");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onTouchEvent$0() {
        this.mFODAnimation.hideFODanimation();
    }

    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        updatePosition();
    }

    public IFingerprintInscreen getFingerprintInScreenDaemon() {
        if (this.mFingerprintInscreenDaemon == null) {
            try {
                IFingerprintInscreen service = IFingerprintInscreen.getService();
                this.mFingerprintInscreenDaemon = service;
                if (service != null) {
                    service.setCallback(this.mFingerprintInscreenCallback);
                    this.mFingerprintInscreenDaemon.asBinder().linkToDeath(new IHwBinder.DeathRecipient() { // from class: com.android.systemui.biometrics.FODCircleView$$ExternalSyntheticLambda0
                        public final void serviceDied(long j) {
                            this.f$0.lambda$getFingerprintInScreenDaemon$1(j);
                        }
                    }, 0L);
                }
            } catch (RemoteException | NoSuchElementException unused) {
            }
        }
        return this.mFingerprintInscreenDaemon;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$getFingerprintInScreenDaemon$1(long j) {
        this.mFingerprintInscreenDaemon = null;
    }

    public void dispatchPress() {
        if (this.mFading) {
            return;
        }
        try {
            getFingerprintInScreenDaemon().onPress();
        } catch (RemoteException unused) {
        }
    }

    public void dispatchRelease() {
        try {
            getFingerprintInScreenDaemon().onRelease();
        } catch (RemoteException unused) {
        }
    }

    public void dispatchShow() {
        try {
            getFingerprintInScreenDaemon().onShowFODView();
        } catch (RemoteException unused) {
        }
    }

    public void dispatchHide() {
        try {
            getFingerprintInScreenDaemon().onHideFODView();
        } catch (RemoteException unused) {
        }
    }

    public void showCircle() {
        if (this.mFading || this.mIsCircleShowing || this.mTouchedOutside) {
            return;
        }
        if (this.mIsKeyguard && this.mIsDeviceInPocket) {
            return;
        }
        this.mIsCircleShowing = true;
        setKeepScreenOn(true);
        setDim(true);
        dispatchPress();
        if (this.mFODAnimation != null && this.mIsRecognizingAnimEnabled) {
            this.mHandler.post(new Runnable() { // from class: com.android.systemui.biometrics.FODCircleView$$ExternalSyntheticLambda5
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$showCircle$2();
                }
            });
        }
        setImageDrawable(null);
        invalidate();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showCircle$2() {
        this.mFODAnimation.showFODanimation();
    }

    public void hideCircle() {
        this.mIsCircleShowing = false;
        setImageResource(this.ICON_STYLES[this.mSelectedIcon]);
        invalidate();
        dispatchRelease();
        setDim(false);
        if (this.mFODAnimation != null && this.mIsRecognizingAnimEnabled) {
            this.mHandler.post(new Runnable() { // from class: com.android.systemui.biometrics.FODCircleView$$ExternalSyntheticLambda3
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$hideCircle$3();
                }
            });
        }
        setKeepScreenOn(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$hideCircle$3() {
        this.mFODAnimation.hideFODanimation();
    }

    public void show() {
        if (this.mUpdateMonitor.userNeedsStrongAuth()) {
            return;
        }
        if (this.mUpdateMonitor.isScreenOn() || this.mFodGestureEnable) {
            if (!this.mIsBouncer || isPinOrPattern(KeyguardUpdateMonitor.getCurrentUser())) {
                if (this.mIsKeyguard && this.mUpdateMonitor.getUserCanSkipBouncer(KeyguardUpdateMonitor.getCurrentUser())) {
                    return;
                }
                if (!this.mIsKeyguard || this.mIsBiometricRunning || this.mFodGestureEnable) {
                    updatePosition();
                    setVisibility(0);
                    animate().withStartAction(new Runnable() { // from class: com.android.systemui.biometrics.FODCircleView$$ExternalSyntheticLambda2
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f$0.lambda$show$4();
                        }
                    }).alpha(this.mIsDreaming ? 0.5f : 1.0f).setDuration(125L).withEndAction(new Runnable() { // from class: com.android.systemui.biometrics.FODCircleView$$ExternalSyntheticLambda4
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f$0.lambda$show$5();
                        }
                    }).start();
                    dispatchShow();
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$show$4() {
        this.mFading = true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$show$5() {
        this.mFading = false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$hide$6() {
        this.mFading = true;
    }

    public void hide() {
        animate().withStartAction(new Runnable() { // from class: com.android.systemui.biometrics.FODCircleView$$ExternalSyntheticLambda7
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$hide$6();
            }
        }).alpha(0.0f).setDuration(125L).withEndAction(new Runnable() { // from class: com.android.systemui.biometrics.FODCircleView$$ExternalSyntheticLambda6
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$hide$7();
            }
        }).start();
        hideCircle();
        dispatchHide();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$hide$7() {
        setVisibility(8);
        this.mFading = false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateAlpha() {
        setAlpha(this.mIsDreaming ? 0.5f : 1.0f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:22:0x0070  */
    /* JADX WARN: Removed duplicated region for block: B:30:0x009a  */
    /* JADX WARN: Removed duplicated region for block: B:32:? A[RETURN, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void updatePosition() {
        /*
            r4 = this;
            android.view.WindowManager r0 = r4.mWindowManager
            android.view.Display r0 = r0.getDefaultDisplay()
            android.graphics.Point r1 = new android.graphics.Point
            r1.<init>()
            r0.getRealSize(r1)
            int r0 = r0.getRotation()
            boolean r2 = r4.mCutoutMasked
            if (r2 == 0) goto L19
            int r2 = r4.mStatusbarHeight
            goto L1a
        L19:
            r2 = 0
        L1a:
            if (r0 == 0) goto L5b
            r3 = 1
            if (r0 == r3) goto L56
            r3 = 2
            if (r0 == r3) goto L4b
            r3 = 3
            if (r0 != r3) goto L34
            int r0 = r1.x
            int r1 = r4.mPositionY
            int r0 = r0 - r1
            int r1 = r4.mSize
            int r0 = r0 - r1
            int r1 = r4.mNavigationBarSize
            int r0 = r0 - r1
            int r0 = r0 - r2
            int r1 = r4.mPositionX
            goto L60
        L34:
            java.lang.IllegalArgumentException r4 = new java.lang.IllegalArgumentException
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "Unknown rotation: "
            r1.append(r2)
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            r4.<init>(r0)
            throw r4
        L4b:
            int r0 = r4.mPositionX
            int r1 = r1.y
            int r3 = r4.mPositionY
            int r1 = r1 - r3
            int r3 = r4.mSize
            int r1 = r1 - r3
            goto L5f
        L56:
            int r0 = r4.mPositionY
            int r1 = r4.mPositionX
            goto L5f
        L5b:
            int r0 = r4.mPositionX
            int r1 = r4.mPositionY
        L5f:
            int r1 = r1 - r2
        L60:
            android.view.WindowManager$LayoutParams r2 = r4.mPressedParams
            android.view.WindowManager$LayoutParams r3 = r4.mParams
            r3.x = r0
            r2.x = r0
            r3.y = r1
            r2.y = r1
            boolean r0 = r4.mIsDreaming
            if (r0 == 0) goto L7e
            int r0 = r3.x
            int r1 = r4.mDreamingOffsetX
            int r0 = r0 + r1
            r3.x = r0
            int r0 = r3.y
            int r1 = r4.mDreamingOffsetY
            int r0 = r0 + r1
            r3.y = r0
        L7e:
            com.android.systemui.biometrics.FODAnimation r0 = r4.mFODAnimation
            if (r0 == 0) goto L8b
            boolean r1 = r4.mIsRecognizingAnimEnabled
            if (r1 == 0) goto L8b
            int r1 = r3.y
            r0.updateParams(r1)
        L8b:
            android.view.WindowManager r0 = r4.mWindowManager
            android.view.WindowManager$LayoutParams r1 = r4.mParams
            r0.updateViewLayout(r4, r1)
            android.widget.ImageView r0 = r4.mPressedView
            android.view.ViewParent r0 = r0.getParent()
            if (r0 == 0) goto La3
            android.view.WindowManager r0 = r4.mWindowManager
            android.widget.ImageView r1 = r4.mPressedView
            android.view.WindowManager$LayoutParams r4 = r4.mPressedParams
            r0.updateViewLayout(r1, r4)
        La3:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.biometrics.FODCircleView.updatePosition():void");
    }

    private void setDim(boolean z) {
        if (z) {
            int dimAmount = 0;
            try {
                dimAmount = getFingerprintInScreenDaemon().getDimAmount(Settings.System.getInt(getContext().getContentResolver(), "screen_brightness", 100));
            } catch (RemoteException unused) {
            }
            if (this.mShouldBoostBrightness) {
                this.mPressedParams.screenBrightness = 1.0f;
            }
            this.mPressedParams.dimAmount = dimAmount / 255.0f;
            if (this.mPressedView.getParent() == null) {
                this.mWindowManager.addView(this.mPressedView, this.mPressedParams);
                return;
            } else {
                this.mWindowManager.updateViewLayout(this.mPressedView, this.mPressedParams);
                return;
            }
        }
        if (this.mShouldBoostBrightness) {
            this.mPressedParams.screenBrightness = 0.0f;
        }
        this.mPressedParams.dimAmount = 0.0f;
        if (this.mPressedView.getParent() != null) {
            this.mWindowManager.removeView(this.mPressedView);
        }
    }

    private boolean useAccentDefaultColor() {
        return Settings.System.getInt(((ImageView) this).mContext.getContentResolver(), "fod_icon_accent_default_color", 0) != 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isPinOrPattern(int i) {
        int activePasswordQuality = this.mLockPatternUtils.getActivePasswordQuality(i);
        return activePasswordQuality == 65536 || activePasswordQuality == 131072 || activePasswordQuality == 196608;
    }

    /* JADX INFO: Access modifiers changed from: private */
    class BurnInProtectionTask extends TimerTask {
        private BurnInProtectionTask() {
        }

        /* synthetic */ BurnInProtectionTask(FODCircleView fODCircleView, AnonymousClass1 anonymousClass1) {
            this();
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            long jCurrentTimeMillis = (System.currentTimeMillis() / 1000) / 60;
            FODCircleView.this.mDreamingOffsetX = (int) (jCurrentTimeMillis % (r2.mDreamingMaxOffset * 4));
            if (FODCircleView.this.mDreamingOffsetX > FODCircleView.this.mDreamingMaxOffset * 2) {
                FODCircleView fODCircleView = FODCircleView.this;
                fODCircleView.mDreamingOffsetX = (fODCircleView.mDreamingMaxOffset * 4) - FODCircleView.this.mDreamingOffsetX;
            }
            FODCircleView.this.mDreamingOffsetY = (int) ((jCurrentTimeMillis + (r2.mDreamingMaxOffset / 3)) % (FODCircleView.this.mDreamingMaxOffset * 2));
            if (FODCircleView.this.mDreamingOffsetY > FODCircleView.this.mDreamingMaxOffset * 2) {
                FODCircleView fODCircleView2 = FODCircleView.this;
                fODCircleView2.mDreamingOffsetY = (fODCircleView2.mDreamingMaxOffset * 4) - FODCircleView.this.mDreamingOffsetY;
            }
            FODCircleView fODCircleView3 = FODCircleView.this;
            FODCircleView.access$2620(fODCircleView3, fODCircleView3.mDreamingMaxOffset);
            FODCircleView fODCircleView4 = FODCircleView.this;
            FODCircleView.access$2820(fODCircleView4, fODCircleView4.mDreamingMaxOffset);
            FODCircleView.this.mHandler.post(new Runnable() { // from class: com.android.systemui.biometrics.FODCircleView$BurnInProtectionTask$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$run$0();
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$run$0() {
            FODCircleView.this.updatePosition();
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onOverlayChanged() throws Resources.NotFoundException {
        updateCutoutFlags();
    }

    private void updateCutoutFlags() throws Resources.NotFoundException {
        this.mStatusbarHeight = getContext().getResources().getDimensionPixelSize(android.R.dimen.notification_custom_view_max_image_width);
        boolean z = getContext().getResources().getBoolean(android.R.bool.config_cecQuerySadDtshdDisabled_allowed);
        if (this.mCutoutMasked != z) {
            this.mCutoutMasked = z;
            updatePosition();
        }
    }
}
