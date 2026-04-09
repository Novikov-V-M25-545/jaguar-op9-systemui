package com.android.systemui.statusbar.phone;

import android.app.ActivityOptions;
import android.app.ActivityTaskManager;
import android.app.IActivityTaskManager;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.hardware.biometrics.BiometricSourceType;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.ActivityIntentHelper;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.doze.util.BurnInHelperKt;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.IntentButtonProvider;
import com.android.systemui.statusbar.KeyguardAffordanceView;
import com.android.systemui.statusbar.policy.AccessibilityController;
import com.android.systemui.statusbar.policy.ExtensionController;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.PreviewInflater;
import com.android.systemui.tuner.LockscreenFragment;
import com.android.systemui.tuner.TunerService;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

/* loaded from: classes.dex */
public class KeyguardBottomAreaView extends FrameLayout implements View.OnClickListener, KeyguardStateController.Callback, AccessibilityController.AccessibilityStateChangedCallback {
    private AccessibilityController mAccessibilityController;
    private View.AccessibilityDelegate mAccessibilityDelegate;
    private ActivityIntentHelper mActivityIntentHelper;
    private ActivityStarter mActivityStarter;
    private KeyguardAffordanceHelper mAffordanceHelper;
    private int mBurnInXOffset;
    private int mBurnInYOffset;
    private View mCameraPreview;
    private float mDarkAmount;
    private final BroadcastReceiver mDevicePolicyReceiver;
    private boolean mDozing;
    private TextView mEnterpriseDisclosure;
    private FlashlightController mFlashlightController;
    private ViewGroup mIndicationArea;
    private int mIndicationBottomMargin;
    private int mIndicationBottomMarginFod;
    private TextView mIndicationText;
    private boolean mIsFingerprintRunning;
    private KeyguardStateController mKeyguardStateController;
    private KeyguardAffordanceView mLeftAffordanceView;
    private Drawable mLeftAssistIcon;
    private IntentButtonProvider.IntentButton mLeftButton;
    private String mLeftButtonStr;
    private ExtensionController.Extension<IntentButtonProvider.IntentButton> mLeftExtension;
    private boolean mLeftIsVoiceAssist;
    private View mLeftPreview;
    private ViewGroup mOverlayContainer;
    private ViewGroup mPreviewContainer;
    private PreviewInflater mPreviewInflater;
    private boolean mPrewarmBound;
    private final ServiceConnection mPrewarmConnection;
    private Messenger mPrewarmMessenger;
    private KeyguardAffordanceView mRightAffordanceView;
    private IntentButtonProvider.IntentButton mRightButton;
    private String mRightButtonStr;
    private ExtensionController.Extension<IntentButtonProvider.IntentButton> mRightExtension;
    private boolean mShowCameraAffordance;
    private boolean mShowLeftAffordance;
    private StatusBar mStatusBar;
    private final KeyguardUpdateMonitorCallback mUpdateMonitorCallback;
    private boolean mUserSetupComplete;
    private static final Intent SECURE_CAMERA_INTENT = new Intent("android.media.action.STILL_IMAGE_CAMERA_SECURE").addFlags(8388608);
    public static final Intent INSECURE_CAMERA_INTENT = new Intent("android.media.action.STILL_IMAGE_CAMERA");
    private static final Intent PHONE_INTENT = new Intent("android.intent.action.DIAL");

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean isSuccessfulLaunch(int i) {
        return i == 0 || i == 3 || i == 2;
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public KeyguardBottomAreaView(Context context) {
        this(context, null);
        updateCameraIconColor();
        updatePhoneIconColor();
        updateIndicationTextColor();
    }

    public KeyguardBottomAreaView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
        updateCameraIconColor();
        updatePhoneIconColor();
        updateIndicationTextColor();
    }

    public KeyguardBottomAreaView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public KeyguardBottomAreaView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mPrewarmConnection = new ServiceConnection() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.1
            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                KeyguardBottomAreaView.this.mPrewarmMessenger = new Messenger(iBinder);
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName componentName) {
                KeyguardBottomAreaView.this.mPrewarmMessenger = null;
            }
        };
        this.mRightButton = new DefaultRightButton();
        this.mLeftButton = new DefaultLeftButton();
        this.mAccessibilityDelegate = new View.AccessibilityDelegate() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.2
            @Override // android.view.View.AccessibilityDelegate
            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) throws Resources.NotFoundException {
                String string;
                super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                if (view != KeyguardBottomAreaView.this.mRightAffordanceView) {
                    if (view != KeyguardBottomAreaView.this.mLeftAffordanceView) {
                        string = null;
                    } else if (KeyguardBottomAreaView.this.mLeftIsVoiceAssist) {
                        string = KeyguardBottomAreaView.this.getResources().getString(R.string.voice_assist_label);
                    } else {
                        string = KeyguardBottomAreaView.this.getResources().getString(R.string.phone_label);
                    }
                } else {
                    string = KeyguardBottomAreaView.this.getResources().getString(R.string.camera_label);
                }
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, string));
            }

            @Override // android.view.View.AccessibilityDelegate
            public boolean performAccessibilityAction(View view, int i3, Bundle bundle) {
                if (i3 == 16) {
                    if (view != KeyguardBottomAreaView.this.mRightAffordanceView) {
                        if (view == KeyguardBottomAreaView.this.mLeftAffordanceView) {
                            KeyguardBottomAreaView.this.launchLeftAffordance();
                            return true;
                        }
                    } else {
                        KeyguardBottomAreaView.this.launchCamera("lockscreen_affordance");
                        return true;
                    }
                }
                return super.performAccessibilityAction(view, i3, bundle);
            }
        };
        this.mDevicePolicyReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.7
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                KeyguardBottomAreaView.this.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.7.1
                    @Override // java.lang.Runnable
                    public void run() {
                        KeyguardBottomAreaView.this.updateCameraVisibility();
                        KeyguardBottomAreaView.this.updateCameraIconColor();
                        KeyguardBottomAreaView.this.updatePhoneIconColor();
                        KeyguardBottomAreaView.this.updateIndicationTextColor();
                    }
                });
            }
        };
        this.mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.8
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onUserSwitchComplete(int i3) {
                KeyguardBottomAreaView.this.updateCameraVisibility();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onUserUnlocked() {
                KeyguardBottomAreaView.this.inflateCameraPreview();
                KeyguardBottomAreaView.this.updateCameraVisibility();
                KeyguardBottomAreaView.this.updateLeftAffordance();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onBiometricRunningStateChanged(boolean z, BiometricSourceType biometricSourceType) {
                if (biometricSourceType == BiometricSourceType.FINGERPRINT) {
                    KeyguardBottomAreaView.this.mIsFingerprintRunning = z;
                    KeyguardBottomAreaView.this.updateIndicationAreaPadding();
                }
            }
        };
        ContentResolver contentResolver = getContext().getContentResolver();
        this.mShowLeftAffordance = Settings.Secure.getIntForUser(contentResolver, "enable_camera_lockscreen_shortcut", 1, -2) == 1;
        this.mShowCameraAffordance = Settings.Secure.getIntForUser(contentResolver, "enable_left_lockscreen_shortcut", 1, -2) == 1;
        updateCameraIconColor();
        updatePhoneIconColor();
        updateIndicationTextColor();
    }

    public void initFrom(KeyguardBottomAreaView keyguardBottomAreaView) {
        setStatusBar(keyguardBottomAreaView.mStatusBar);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mPreviewInflater = new PreviewInflater(((FrameLayout) this).mContext, new LockPatternUtils(((FrameLayout) this).mContext), new ActivityIntentHelper(((FrameLayout) this).mContext));
        this.mPreviewContainer = (ViewGroup) findViewById(R.id.preview_container);
        this.mOverlayContainer = (ViewGroup) findViewById(R.id.overlay_container);
        this.mRightAffordanceView = (KeyguardAffordanceView) findViewById(R.id.camera_button);
        this.mLeftAffordanceView = (KeyguardAffordanceView) findViewById(R.id.left_button);
        this.mIndicationArea = (ViewGroup) findViewById(R.id.keyguard_indication_area);
        this.mEnterpriseDisclosure = (TextView) findViewById(R.id.keyguard_indication_enterprise_disclosure);
        this.mIndicationText = (TextView) findViewById(R.id.keyguard_indication_text);
        this.mIndicationBottomMargin = getResources().getDimensionPixelSize(R.dimen.keyguard_indication_margin_bottom);
        this.mIndicationBottomMarginFod = getResources().getDimensionPixelSize(R.dimen.keyguard_indication_margin_bottom_fingerprint_in_display);
        this.mBurnInYOffset = getResources().getDimensionPixelSize(R.dimen.default_burn_in_prevention_offset);
        updateCameraVisibility();
        KeyguardStateController keyguardStateController = (KeyguardStateController) Dependency.get(KeyguardStateController.class);
        this.mKeyguardStateController = keyguardStateController;
        keyguardStateController.addCallback(this);
        setClipChildren(false);
        setClipToPadding(false);
        inflateCameraPreview();
        this.mRightAffordanceView.setOnClickListener(this);
        this.mLeftAffordanceView.setOnClickListener(this);
        initAccessibility();
        this.mActivityStarter = (ActivityStarter) Dependency.get(ActivityStarter.class);
        this.mFlashlightController = (FlashlightController) Dependency.get(FlashlightController.class);
        this.mAccessibilityController = (AccessibilityController) Dependency.get(AccessibilityController.class);
        this.mActivityIntentHelper = new ActivityIntentHelper(getContext());
        updateLeftAffordance();
        updateIndicationAreaPadding();
        updateCameraIconColor();
        updatePhoneIconColor();
        updateIndicationTextColor();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mAccessibilityController.addStateChangedCallback(this);
        this.mRightExtension = ((ExtensionController) Dependency.get(ExtensionController.class)).newExtension(IntentButtonProvider.IntentButton.class).withPlugin(IntentButtonProvider.class, "com.android.systemui.action.PLUGIN_LOCKSCREEN_RIGHT_BUTTON", new ExtensionController.PluginConverter() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView$$ExternalSyntheticLambda1
            @Override // com.android.systemui.statusbar.policy.ExtensionController.PluginConverter
            public final Object getInterfaceFromPlugin(Object obj) {
                return ((IntentButtonProvider) obj).getIntentButton();
            }
        }).withTunerFactory(new LockscreenFragment.LockButtonFactory(((FrameLayout) this).mContext, "sysui_keyguard_right")).withDefault(new Supplier() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView$$ExternalSyntheticLambda4
            @Override // java.util.function.Supplier
            public final Object get() {
                return this.f$0.lambda$onAttachedToWindow$1();
            }
        }).withCallback(new Consumer() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView$$ExternalSyntheticLambda3
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.lambda$onAttachedToWindow$2((IntentButtonProvider.IntentButton) obj);
            }
        }).build();
        this.mLeftExtension = ((ExtensionController) Dependency.get(ExtensionController.class)).newExtension(IntentButtonProvider.IntentButton.class).withPlugin(IntentButtonProvider.class, "com.android.systemui.action.PLUGIN_LOCKSCREEN_LEFT_BUTTON", new ExtensionController.PluginConverter() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView$$ExternalSyntheticLambda0
            @Override // com.android.systemui.statusbar.policy.ExtensionController.PluginConverter
            public final Object getInterfaceFromPlugin(Object obj) {
                return ((IntentButtonProvider) obj).getIntentButton();
            }
        }).withTunerFactory(new LockscreenFragment.LockButtonFactory(((FrameLayout) this).mContext, "sysui_keyguard_left")).withDefault(new Supplier() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView$$ExternalSyntheticLambda5
            @Override // java.util.function.Supplier
            public final Object get() {
                return this.f$0.lambda$onAttachedToWindow$4();
            }
        }).withCallback(new Consumer() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView$$ExternalSyntheticLambda2
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.lambda$onAttachedToWindow$5((IntentButtonProvider.IntentButton) obj);
            }
        }).build();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        getContext().registerReceiverAsUser(this.mDevicePolicyReceiver, UserHandle.ALL, intentFilter, null, null);
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mUpdateMonitorCallback);
        this.mKeyguardStateController.addCallback(this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ IntentButtonProvider.IntentButton lambda$onAttachedToWindow$1() {
        return new DefaultRightButton();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ IntentButtonProvider.IntentButton lambda$onAttachedToWindow$4() {
        return new DefaultLeftButton();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mKeyguardStateController.removeCallback(this);
        this.mAccessibilityController.removeStateChangedCallback(this);
        this.mRightExtension.destroy();
        this.mLeftExtension.destroy();
        getContext().unregisterReceiver(this.mDevicePolicyReceiver);
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).removeCallback(this.mUpdateMonitorCallback);
    }

    private void initAccessibility() {
        this.mLeftAffordanceView.setAccessibilityDelegate(this.mAccessibilityDelegate);
        this.mRightAffordanceView.setAccessibilityDelegate(this.mAccessibilityDelegate);
        updateCameraIconColor();
        updatePhoneIconColor();
        updateIndicationTextColor();
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateIndicationAreaPadding();
        this.mEnterpriseDisclosure.setTextSize(0, getResources().getDimensionPixelSize(android.R.dimen.notification_icon_circle_start));
        this.mIndicationText.setTextSize(0, getResources().getDimensionPixelSize(android.R.dimen.notification_icon_circle_start));
        ViewGroup.LayoutParams layoutParams = this.mRightAffordanceView.getLayoutParams();
        Resources resources = getResources();
        int i = R.dimen.keyguard_affordance_width;
        layoutParams.width = resources.getDimensionPixelSize(i);
        Resources resources2 = getResources();
        int i2 = R.dimen.keyguard_affordance_height;
        layoutParams.height = resources2.getDimensionPixelSize(i2);
        this.mRightAffordanceView.setLayoutParams(layoutParams);
        updateRightAffordanceIcon();
        ViewGroup.LayoutParams layoutParams2 = this.mLeftAffordanceView.getLayoutParams();
        layoutParams2.width = getResources().getDimensionPixelSize(i);
        layoutParams2.height = getResources().getDimensionPixelSize(i2);
        this.mLeftAffordanceView.setLayoutParams(layoutParams2);
        updateLeftAffordanceIcon();
        updateCameraIconColor();
        updatePhoneIconColor();
        updateIndicationTextColor();
    }

    private void updateRightAffordanceIcon() {
        IntentButtonProvider.IntentButton.IconState icon = this.mRightButton.getIcon();
        this.mRightAffordanceView.setVisibility((this.mDozing || !icon.isVisible) ? 8 : 0);
        if (icon.drawable != this.mRightAffordanceView.getDrawable() || icon.tint != this.mRightAffordanceView.shouldTint()) {
            this.mRightAffordanceView.setImageDrawable(icon.drawable, icon.tint);
        }
        this.mRightAffordanceView.setContentDescription(icon.contentDescription);
    }

    public void setStatusBar(StatusBar statusBar) {
        this.mStatusBar = statusBar;
        updateCameraVisibility();
    }

    public void setAffordanceHelper(KeyguardAffordanceHelper keyguardAffordanceHelper) {
        this.mAffordanceHelper = keyguardAffordanceHelper;
    }

    public void setUserSetupComplete(boolean z) {
        this.mUserSetupComplete = z;
        updateCameraVisibility();
        updateLeftAffordanceIcon();
    }

    private Intent getCameraIntent() {
        return this.mRightButton.getIntent();
    }

    public ResolveInfo resolveCameraIntent() {
        return ((FrameLayout) this).mContext.getPackageManager().resolveActivityAsUser(getCameraIntent(), 65536, KeyguardUpdateMonitor.getCurrentUser());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateCameraVisibility() {
        if (this.mRightAffordanceView == null) {
            return;
        }
        updateCameraIconColor();
        updatePhoneIconColor();
        updateIndicationTextColor();
        this.mRightAffordanceView.setVisibility((!this.mDozing && this.mShowCameraAffordance && this.mRightButton.getIcon().isVisible) ? 0 : 8);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateIndicationAreaPadding() {
        this.mIndicationBottomMargin = getResources().getDimensionPixelSize(R.dimen.keyguard_indication_margin_bottom);
        this.mIndicationBottomMarginFod = getResources().getDimensionPixelSize(R.dimen.keyguard_indication_margin_bottom_fingerprint_in_display);
        this.mBurnInYOffset = getResources().getDimensionPixelSize(R.dimen.default_burn_in_prevention_offset);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mIndicationArea.getLayoutParams();
        int i = hasInDisplayFingerprint() ? this.mIndicationBottomMarginFod : this.mIndicationBottomMargin;
        if (marginLayoutParams.bottomMargin != i) {
            marginLayoutParams.bottomMargin = i;
            this.mIndicationArea.setLayoutParams(marginLayoutParams);
        }
    }

    private void updateLeftAffordanceIcon() {
        if (!this.mShowLeftAffordance || this.mDozing) {
            this.mLeftAffordanceView.setVisibility(8);
            return;
        }
        IntentButtonProvider.IntentButton.IconState icon = this.mLeftButton.getIcon();
        this.mLeftAffordanceView.setVisibility(icon.isVisible ? 0 : 8);
        if (icon.drawable != this.mLeftAffordanceView.getDrawable() || icon.tint != this.mLeftAffordanceView.shouldTint()) {
            this.mLeftAffordanceView.setImageDrawable(icon.drawable, icon.tint);
        }
        this.mLeftAffordanceView.setContentDescription(icon.contentDescription);
    }

    private boolean hasInDisplayFingerprint() {
        return ((FrameLayout) this).mContext.getPackageManager().hasSystemFeature("vendor.lineage.biometrics.fingerprint.inscreen") && this.mIsFingerprintRunning;
    }

    public boolean isLeftVoiceAssist() {
        return this.mLeftIsVoiceAssist;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isPhoneVisible() {
        PackageManager packageManager = ((FrameLayout) this).mContext.getPackageManager();
        updateCameraIconColor();
        updatePhoneIconColor();
        updateIndicationTextColor();
        return packageManager.hasSystemFeature("android.hardware.telephony") && packageManager.resolveActivity(PHONE_INTENT, 0) != null;
    }

    @Override // com.android.systemui.statusbar.policy.AccessibilityController.AccessibilityStateChangedCallback
    public void onStateChanged(boolean z, boolean z2) {
        this.mRightAffordanceView.setClickable(z2);
        this.mLeftAffordanceView.setClickable(z2);
        this.mRightAffordanceView.setFocusable(z);
        this.mLeftAffordanceView.setFocusable(z);
        updateCameraIconColor();
        updatePhoneIconColor();
        updateIndicationTextColor();
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view == this.mRightAffordanceView) {
            launchCamera("lockscreen_affordance");
        } else if (view == this.mLeftAffordanceView) {
            launchLeftAffordance();
        }
    }

    public void bindCameraPrewarmService() {
        Bundle bundle;
        String string;
        ActivityInfo targetActivityInfo = this.mActivityIntentHelper.getTargetActivityInfo(getCameraIntent(), KeyguardUpdateMonitor.getCurrentUser(), true);
        if (targetActivityInfo == null || (bundle = targetActivityInfo.metaData) == null || (string = bundle.getString("android.media.still_image_camera_preview_service")) == null) {
            return;
        }
        Intent intent = new Intent();
        intent.setClassName(targetActivityInfo.packageName, string);
        intent.setAction("android.service.media.CameraPrewarmService.ACTION_PREWARM");
        try {
            if (getContext().bindServiceAsUser(intent, this.mPrewarmConnection, 67108865, new UserHandle(-2))) {
                this.mPrewarmBound = true;
            }
        } catch (SecurityException e) {
            Log.w("StatusBar/KeyguardBottomAreaView", "Unable to bind to prewarm service package=" + targetActivityInfo.packageName + " class=" + string, e);
        }
    }

    public void unbindCameraPrewarmService(boolean z) {
        if (this.mPrewarmBound) {
            Messenger messenger = this.mPrewarmMessenger;
            if (messenger != null && z) {
                try {
                    messenger.send(Message.obtain((Handler) null, 1));
                } catch (RemoteException e) {
                    Log.w("StatusBar/KeyguardBottomAreaView", "Error sending camera fired message", e);
                }
            }
            ((FrameLayout) this).mContext.unbindService(this.mPrewarmConnection);
            this.mPrewarmBound = false;
        }
    }

    public void launchCamera(String str) {
        final Intent cameraIntent = getCameraIntent();
        cameraIntent.putExtra("com.android.systemui.camera_launch_source", str);
        boolean zWouldLaunchResolverActivity = this.mActivityIntentHelper.wouldLaunchResolverActivity(cameraIntent, KeyguardUpdateMonitor.getCurrentUser());
        if (cameraIntent == SECURE_CAMERA_INTENT && !zWouldLaunchResolverActivity) {
            AsyncTask.execute(new Runnable() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.3
                @Override // java.lang.Runnable
                public void run() {
                    int iStartActivityAsUser;
                    ActivityOptions activityOptionsMakeBasic = ActivityOptions.makeBasic();
                    activityOptionsMakeBasic.setDisallowEnterPictureInPictureWhileLaunching(true);
                    activityOptionsMakeBasic.setRotationAnimationHint(3);
                    try {
                        IActivityTaskManager service = ActivityTaskManager.getService();
                        String basePackageName = KeyguardBottomAreaView.this.getContext().getBasePackageName();
                        String attributionTag = KeyguardBottomAreaView.this.getContext().getAttributionTag();
                        Intent intent = cameraIntent;
                        iStartActivityAsUser = service.startActivityAsUser((IApplicationThread) null, basePackageName, attributionTag, intent, intent.resolveTypeIfNeeded(KeyguardBottomAreaView.this.getContext().getContentResolver()), (IBinder) null, (String) null, 0, 268435456, (ProfilerInfo) null, activityOptionsMakeBasic.toBundle(), UserHandle.CURRENT.getIdentifier());
                    } catch (RemoteException e) {
                        Log.w("StatusBar/KeyguardBottomAreaView", "Unable to start camera activity", e);
                        iStartActivityAsUser = -96;
                    }
                    final boolean zIsSuccessfulLaunch = KeyguardBottomAreaView.isSuccessfulLaunch(iStartActivityAsUser);
                    KeyguardBottomAreaView.this.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.3.1
                        @Override // java.lang.Runnable
                        public void run() {
                            KeyguardBottomAreaView.this.unbindCameraPrewarmService(zIsSuccessfulLaunch);
                        }
                    });
                }
            });
            return;
        }
        this.mActivityStarter.startActivity(cameraIntent, false, new ActivityStarter.Callback() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.4
            @Override // com.android.systemui.plugins.ActivityStarter.Callback
            public void onActivityStarted(int i) {
                KeyguardBottomAreaView.this.unbindCameraPrewarmService(KeyguardBottomAreaView.isSuccessfulLaunch(i));
            }
        });
        updateCameraIconColor();
        updatePhoneIconColor();
        updateIndicationTextColor();
    }

    public void setDarkAmount(float f) {
        if (f == this.mDarkAmount) {
            return;
        }
        this.mDarkAmount = f;
        dozeTimeTick();
    }

    public void launchLeftAffordance() {
        if (this.mLeftIsVoiceAssist) {
            launchVoiceAssist();
        } else {
            launchPhone();
        }
    }

    @VisibleForTesting
    void launchVoiceAssist() {
        Runnable runnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.5
            @Override // java.lang.Runnable
            public void run() {
                ((AssistManager) Dependency.get(AssistManager.class)).launchVoiceAssistFromKeyguard();
            }
        };
        if (!this.mKeyguardStateController.canDismissLockScreen()) {
            ((Executor) Dependency.get(Dependency.BACKGROUND_EXECUTOR)).execute(runnable);
        } else {
            this.mStatusBar.executeRunnableDismissingKeyguard(runnable, null, (TextUtils.isEmpty(this.mRightButtonStr) || ((TunerService) Dependency.get(TunerService.class)).getValue("sysui_keyguard_right_unlock", 1) == 0) ? false : true, false, true);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean canLaunchVoiceAssist() {
        return ((AssistManager) Dependency.get(AssistManager.class)).canVoiceAssistBeLaunchedFromKeyguard();
    }

    private void launchPhone() {
        final TelecomManager telecomManagerFrom = TelecomManager.from(((FrameLayout) this).mContext);
        if (telecomManagerFrom.isInCall()) {
            AsyncTask.execute(new Runnable() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.6
                @Override // java.lang.Runnable
                public void run() {
                    telecomManagerFrom.showInCallScreen(false);
                }
            });
            return;
        }
        this.mActivityStarter.startActivity(this.mLeftButton.getIntent(), (TextUtils.isEmpty(this.mLeftButtonStr) || ((TunerService) Dependency.get(TunerService.class)).getValue("sysui_keyguard_left_unlock", 1) == 0) ? false : true);
        updateCameraIconColor();
        updatePhoneIconColor();
        updateIndicationTextColor();
    }

    @Override // android.view.View
    protected void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        if (view == this && i == 0) {
            updateCameraVisibility();
            updateCameraIconColor();
            updatePhoneIconColor();
            updateIndicationTextColor();
        }
    }

    public KeyguardAffordanceView getLeftView() {
        return this.mLeftAffordanceView;
    }

    public KeyguardAffordanceView getRightView() {
        return this.mRightAffordanceView;
    }

    public View getLeftPreview() {
        return this.mLeftPreview;
    }

    public View getRightPreview() {
        return this.mCameraPreview;
    }

    public View getIndicationArea() {
        return this.mIndicationArea;
    }

    @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
    public void onUnlockedChanged() {
        updateCameraVisibility();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:7:0x0012  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void inflateCameraPreview() {
        /*
            r4 = this;
            android.view.View r0 = r4.mCameraPreview
            r1 = 0
            if (r0 == 0) goto L12
            android.view.ViewGroup r2 = r4.mPreviewContainer
            r2.removeView(r0)
            int r0 = r0.getVisibility()
            if (r0 != 0) goto L12
            r0 = 1
            goto L13
        L12:
            r0 = r1
        L13:
            com.android.systemui.statusbar.policy.PreviewInflater r2 = r4.mPreviewInflater
            android.content.Intent r3 = r4.getCameraIntent()
            android.view.View r2 = r2.inflatePreview(r3)
            r4.mCameraPreview = r2
            if (r2 == 0) goto L38
            android.view.ViewGroup r3 = r4.mPreviewContainer
            r3.addView(r2)
            android.view.View r2 = r4.mCameraPreview
            if (r0 == 0) goto L2b
            goto L2c
        L2b:
            r1 = 4
        L2c:
            r2.setVisibility(r1)
            r4.updateCameraIconColor()
            r4.updatePhoneIconColor()
            r4.updateIndicationTextColor()
        L38:
            com.android.systemui.statusbar.phone.KeyguardAffordanceHelper r4 = r4.mAffordanceHelper
            if (r4 == 0) goto L3f
            r4.updatePreviews()
        L3f:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.inflateCameraPreview():void");
    }

    private void updateLeftPreview() {
        View view = this.mLeftPreview;
        if (view != null) {
            this.mPreviewContainer.removeView(view);
        }
        if (this.mLeftIsVoiceAssist) {
            if (((AssistManager) Dependency.get(AssistManager.class)).getVoiceInteractorComponentName() != null) {
                this.mLeftPreview = this.mPreviewInflater.inflatePreviewFromService(((AssistManager) Dependency.get(AssistManager.class)).getVoiceInteractorComponentName());
            }
        } else {
            this.mLeftPreview = this.mPreviewInflater.inflatePreview(this.mLeftButton.getIntent());
        }
        View view2 = this.mLeftPreview;
        if (view2 != null) {
            this.mPreviewContainer.addView(view2);
            this.mLeftPreview.setVisibility(4);
        }
        KeyguardAffordanceHelper keyguardAffordanceHelper = this.mAffordanceHelper;
        if (keyguardAffordanceHelper != null) {
            keyguardAffordanceHelper.updatePreviews();
        }
    }

    public void startFinishDozeAnimation() {
        long j = 0;
        if (this.mLeftAffordanceView.getVisibility() == 0) {
            startFinishDozeAnimationElement(this.mLeftAffordanceView, 0L);
            j = 48;
        }
        if (this.mRightAffordanceView.getVisibility() == 0) {
            startFinishDozeAnimationElement(this.mRightAffordanceView, j);
        }
        updateCameraIconColor();
        updatePhoneIconColor();
        updateIndicationTextColor();
    }

    private void startFinishDozeAnimationElement(View view, long j) {
        view.setAlpha(0.0f);
        view.setTranslationY(view.getHeight() / 2);
        view.animate().alpha(1.0f).translationY(0.0f).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).setStartDelay(j).setDuration(250L);
        updateCameraIconColor();
        updatePhoneIconColor();
        updateIndicationTextColor();
    }

    public void updateLeftAffordance() {
        updateLeftAffordanceIcon();
        updateLeftPreview();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: setRightButton, reason: merged with bridge method [inline-methods] */
    public void lambda$onAttachedToWindow$2(IntentButtonProvider.IntentButton intentButton) {
        this.mRightButton = intentButton;
        updateRightAffordanceIcon();
        updateCameraVisibility();
        inflateCameraPreview();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: setLeftButton, reason: merged with bridge method [inline-methods] */
    public void lambda$onAttachedToWindow$5(IntentButtonProvider.IntentButton intentButton) {
        this.mLeftButton = intentButton;
        if (!(intentButton instanceof DefaultLeftButton)) {
            this.mLeftIsVoiceAssist = false;
        }
        updateLeftAffordance();
    }

    public void setDozing(boolean z, boolean z2) {
        this.mDozing = z;
        updateCameraVisibility();
        updateLeftAffordanceIcon();
        if (z) {
            this.mOverlayContainer.setVisibility(4);
            return;
        }
        this.mOverlayContainer.setVisibility(0);
        if (z2) {
            startFinishDozeAnimation();
        }
    }

    public void dozeTimeTick() {
        this.mIndicationArea.setTranslationY((BurnInHelperKt.getBurnInOffset(this.mBurnInYOffset * 2, false) - this.mBurnInYOffset) * this.mDarkAmount);
    }

    public void setAntiBurnInOffsetX(int i) {
        if (this.mBurnInXOffset == i) {
            return;
        }
        this.mBurnInXOffset = i;
        this.mIndicationArea.setTranslationX(i);
    }

    public void setAffordanceAlpha(float f) {
        this.mLeftAffordanceView.setAlpha(f);
        this.mRightAffordanceView.setAlpha(f);
        this.mIndicationArea.setAlpha(f);
    }

    private class DefaultLeftButton implements IntentButtonProvider.IntentButton {
        private IntentButtonProvider.IntentButton.IconState mIconState;

        private DefaultLeftButton() {
            this.mIconState = new IntentButtonProvider.IntentButton.IconState();
        }

        @Override // com.android.systemui.plugins.IntentButtonProvider.IntentButton
        public IntentButtonProvider.IntentButton.IconState getIcon() {
            KeyguardBottomAreaView keyguardBottomAreaView = KeyguardBottomAreaView.this;
            keyguardBottomAreaView.mLeftIsVoiceAssist = keyguardBottomAreaView.canLaunchVoiceAssist();
            if (KeyguardBottomAreaView.this.mLeftIsVoiceAssist) {
                this.mIconState.isVisible = KeyguardBottomAreaView.this.mUserSetupComplete && KeyguardBottomAreaView.this.mShowLeftAffordance;
                if (KeyguardBottomAreaView.this.mLeftAssistIcon == null) {
                    this.mIconState.drawable = ((FrameLayout) KeyguardBottomAreaView.this).mContext.getDrawable(R.drawable.ic_mic_ssos);
                } else {
                    this.mIconState.drawable = KeyguardBottomAreaView.this.mLeftAssistIcon;
                }
                this.mIconState.contentDescription = ((FrameLayout) KeyguardBottomAreaView.this).mContext.getString(R.string.accessibility_voice_assist_button);
            } else {
                this.mIconState.isVisible = KeyguardBottomAreaView.this.mUserSetupComplete && KeyguardBottomAreaView.this.mShowLeftAffordance && KeyguardBottomAreaView.this.isPhoneVisible();
                this.mIconState.drawable = ((FrameLayout) KeyguardBottomAreaView.this).mContext.getDrawable(R.drawable.ic_phone_ssos);
                this.mIconState.contentDescription = ((FrameLayout) KeyguardBottomAreaView.this).mContext.getString(R.string.accessibility_phone_button);
            }
            return this.mIconState;
        }

        @Override // com.android.systemui.plugins.IntentButtonProvider.IntentButton
        public Intent getIntent() {
            return KeyguardBottomAreaView.PHONE_INTENT;
        }
    }

    private class DefaultRightButton implements IntentButtonProvider.IntentButton {
        private IntentButtonProvider.IntentButton.IconState mIconState;

        private DefaultRightButton() {
            this.mIconState = new IntentButtonProvider.IntentButton.IconState();
        }

        @Override // com.android.systemui.plugins.IntentButtonProvider.IntentButton
        public IntentButtonProvider.IntentButton.IconState getIcon() {
            boolean z = (KeyguardBottomAreaView.this.mStatusBar == null || KeyguardBottomAreaView.this.mStatusBar.isCameraAllowedByAdmin()) ? false : true;
            this.mIconState.isVisible = !z && KeyguardBottomAreaView.this.mShowCameraAffordance && KeyguardBottomAreaView.this.mUserSetupComplete && KeyguardBottomAreaView.this.resolveCameraIntent() != null;
            this.mIconState.drawable = ((FrameLayout) KeyguardBottomAreaView.this).mContext.getDrawable(R.drawable.ic_camera_ssos);
            this.mIconState.contentDescription = ((FrameLayout) KeyguardBottomAreaView.this).mContext.getString(R.string.accessibility_camera_button);
            return this.mIconState;
        }

        @Override // com.android.systemui.plugins.IntentButtonProvider.IntentButton
        public Intent getIntent() {
            boolean zCanDismissLockScreen = KeyguardBottomAreaView.this.mKeyguardStateController.canDismissLockScreen();
            boolean zIsMethodSecure = KeyguardBottomAreaView.this.mKeyguardStateController.isMethodSecure();
            KeyguardBottomAreaView.this.updateCameraIconColor();
            KeyguardBottomAreaView.this.updatePhoneIconColor();
            KeyguardBottomAreaView.this.updateIndicationTextColor();
            return (!zIsMethodSecure || zCanDismissLockScreen) ? KeyguardBottomAreaView.INSECURE_CAMERA_INTENT : KeyguardBottomAreaView.SECURE_CAMERA_INTENT;
        }
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        int safeInsetBottom = windowInsets.getDisplayCutout() != null ? windowInsets.getDisplayCutout().getSafeInsetBottom() : 0;
        if (isPaddingRelative()) {
            setPaddingRelative(getPaddingStart(), getPaddingTop(), getPaddingEnd(), safeInsetBottom);
        } else {
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), safeInsetBottom);
        }
        return windowInsets;
    }

    public void updateCameraIconColor() {
        int i = Settings.System.getInt(getContext().getContentResolver(), "lockscreen_camera_icon_color", -1711276033);
        KeyguardAffordanceView keyguardAffordanceView = this.mRightAffordanceView;
        if (keyguardAffordanceView != null) {
            keyguardAffordanceView.setColorFilter(i);
        }
    }

    public void updatePhoneIconColor() {
        int i = Settings.System.getInt(getContext().getContentResolver(), "lockscreen_phone_icon_color", -1711276033);
        KeyguardAffordanceView keyguardAffordanceView = this.mLeftAffordanceView;
        if (keyguardAffordanceView != null) {
            keyguardAffordanceView.setColorFilter(i);
        }
    }

    public void updateIndicationTextColor() {
        int i = Settings.System.getInt(getContext().getContentResolver(), "lockscreen_indication_text_color", -1711276033);
        TextView textView = this.mIndicationText;
        if (textView != null) {
            textView.setTextColor(i);
        }
    }
}
