package com.android.systemui.statusbar.phone;

import android.graphics.RectF;
import android.hardware.display.AmbientDisplayConfiguration;
import android.media.session.MediaSessionLegacyHelper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.R;
import com.android.systemui.dock.DockManager;
import com.android.systemui.doze.DozeLog;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.DragDownHelper;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationShadeDepthController;
import com.android.systemui.statusbar.PulseExpansionHandler;
import com.android.systemui.statusbar.SuperStatusBarViewFactory;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.notification.DynamicPrivacyController;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.phone.NotificationShadeWindowView;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.InjectionInflationController;
import java.io.FileDescriptor;
import java.io.PrintWriter;

/* loaded from: classes.dex */
public class NotificationShadeWindowViewController {
    private PhoneStatusBarTransitions mBarTransitions;
    private View mBrightnessMirror;
    private final KeyguardBypassController mBypassController;
    private final CommandQueue mCommandQueue;
    private final NotificationWakeUpCoordinator mCoordinator;
    private final NotificationShadeDepthController mDepthController;
    private final DockManager mDockManager;
    private boolean mDoubleTapEnabledNative;
    private boolean mDoubleTapToSleepEnabled;
    private final DozeLog mDozeLog;
    private final DozeParameters mDozeParameters;
    private DragDownHelper mDragDownHelper;
    private final DynamicPrivacyController mDynamicPrivacyController;
    private boolean mExpandAnimationPending;
    private boolean mExpandAnimationRunning;
    private boolean mExpandingBelowNotch;
    private final FalsingManager mFalsingManager;
    private GestureDetector mGestureDetector;
    private final InjectionInflationController mInjectionInflationController;
    private final KeyguardStateController mKeyguardStateController;
    private final NotificationEntryManager mNotificationEntryManager;
    private final NotificationLockscreenUserManager mNotificationLockscreenUserManager;
    private final NotificationPanelViewController mNotificationPanelViewController;
    private NotificationShadeWindowController mNotificationShadeWindowController;
    private final PluginManager mPluginManager;
    private final PowerManager mPowerManager;
    private final PulseExpansionHandler mPulseExpansionHandler;
    private int mQuickQsOffsetHeight;
    private StatusBar mService;
    private final ShadeController mShadeController;
    private boolean mSingleTapEnabled;
    private NotificationStackScrollLayout mStackScrollLayout;
    private final SysuiStatusBarStateController mStatusBarStateController;
    private PhoneStatusBarView mStatusBarView;
    private final SuperStatusBarViewFactory mStatusBarViewFactory;
    private boolean mTouchActive;
    private boolean mTouchCancelled;
    private final TunerService mTunerService;
    private final NotificationShadeWindowView mView;
    private boolean mDoubleTapEnabled = true;
    private int[] mTempLocation = new int[2];
    private RectF mTempRect = new RectF();
    private boolean mIsTrackingBarGesture = false;

    public NotificationShadeWindowViewController(InjectionInflationController injectionInflationController, NotificationWakeUpCoordinator notificationWakeUpCoordinator, PulseExpansionHandler pulseExpansionHandler, DynamicPrivacyController dynamicPrivacyController, KeyguardBypassController keyguardBypassController, FalsingManager falsingManager, PluginManager pluginManager, TunerService tunerService, NotificationLockscreenUserManager notificationLockscreenUserManager, NotificationEntryManager notificationEntryManager, KeyguardStateController keyguardStateController, SysuiStatusBarStateController sysuiStatusBarStateController, DozeLog dozeLog, DozeParameters dozeParameters, CommandQueue commandQueue, ShadeController shadeController, DockManager dockManager, NotificationShadeDepthController notificationShadeDepthController, NotificationShadeWindowView notificationShadeWindowView, NotificationPanelViewController notificationPanelViewController, SuperStatusBarViewFactory superStatusBarViewFactory) {
        this.mInjectionInflationController = injectionInflationController;
        this.mCoordinator = notificationWakeUpCoordinator;
        this.mPulseExpansionHandler = pulseExpansionHandler;
        this.mDynamicPrivacyController = dynamicPrivacyController;
        this.mBypassController = keyguardBypassController;
        this.mFalsingManager = falsingManager;
        this.mPluginManager = pluginManager;
        this.mTunerService = tunerService;
        this.mNotificationLockscreenUserManager = notificationLockscreenUserManager;
        this.mNotificationEntryManager = notificationEntryManager;
        this.mKeyguardStateController = keyguardStateController;
        this.mStatusBarStateController = sysuiStatusBarStateController;
        this.mDozeLog = dozeLog;
        this.mDozeParameters = dozeParameters;
        this.mCommandQueue = commandQueue;
        this.mView = notificationShadeWindowView;
        this.mShadeController = shadeController;
        this.mDockManager = dockManager;
        this.mNotificationPanelViewController = notificationPanelViewController;
        this.mDepthController = notificationShadeDepthController;
        this.mStatusBarViewFactory = superStatusBarViewFactory;
        this.mPowerManager = (PowerManager) notificationShadeWindowView.getContext().getSystemService(PowerManager.class);
        this.mBrightnessMirror = notificationShadeWindowView.findViewById(R.id.brightness_mirror);
    }

    public void setupExpandedStatusBar() {
        this.mStackScrollLayout = (NotificationStackScrollLayout) this.mView.findViewById(R.id.notification_stack_scroller);
        this.mTunerService.addTunable(new TunerService.Tunable() { // from class: com.android.systemui.statusbar.phone.NotificationShadeWindowViewController$$ExternalSyntheticLambda0
            @Override // com.android.systemui.tuner.TunerService.Tunable
            public final void onTuningChanged(String str, String str2) {
                this.f$0.lambda$setupExpandedStatusBar$0(str, str2);
            }
        }, "double_tap_to_wake", "doze_pulse_on_double_tap", "doze_tap_gesture", "lineagesystem:double_tap_sleep_gesture");
        this.mQuickQsOffsetHeight = this.mView.getResources().getDimensionPixelSize(android.R.dimen.message_progress_dialog_end_padding);
        this.mGestureDetector = new GestureDetector(this.mView.getContext(), new GestureDetector.SimpleOnGestureListener() { // from class: com.android.systemui.statusbar.phone.NotificationShadeWindowViewController.1
            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnDoubleTapListener
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                if (!NotificationShadeWindowViewController.this.mSingleTapEnabled || NotificationShadeWindowViewController.this.mDockManager.isDocked()) {
                    return false;
                }
                NotificationShadeWindowViewController.this.mService.wakeUpIfDozing(SystemClock.uptimeMillis(), NotificationShadeWindowViewController.this.mView, "SINGLE_TAP");
                return true;
            }

            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnDoubleTapListener
            public boolean onDoubleTap(MotionEvent motionEvent) {
                if (NotificationShadeWindowViewController.this.mStatusBarStateController.isDozing() || !NotificationShadeWindowViewController.this.mDoubleTapToSleepEnabled || motionEvent.getY() >= NotificationShadeWindowViewController.this.mQuickQsOffsetHeight) {
                    if (!NotificationShadeWindowViewController.this.mDoubleTapEnabled && !NotificationShadeWindowViewController.this.mSingleTapEnabled && !NotificationShadeWindowViewController.this.mDoubleTapEnabledNative) {
                        return false;
                    }
                    NotificationShadeWindowViewController.this.mService.wakeUpIfDozing(SystemClock.uptimeMillis(), NotificationShadeWindowViewController.this.mView, "DOUBLE_TAP");
                    return true;
                }
                NotificationShadeWindowViewController.this.mPowerManager.goToSleep(motionEvent.getEventTime());
                return true;
            }
        });
        this.mView.setInteractionEventHandler(new NotificationShadeWindowView.InteractionEventHandler() { // from class: com.android.systemui.statusbar.phone.NotificationShadeWindowViewController.2
            @Override // com.android.systemui.statusbar.phone.NotificationShadeWindowView.InteractionEventHandler
            public Boolean handleDispatchTouchEvent(MotionEvent motionEvent) {
                boolean z = motionEvent.getActionMasked() == 0;
                boolean z2 = motionEvent.getActionMasked() == 1;
                boolean z3 = motionEvent.getActionMasked() == 3;
                boolean z4 = NotificationShadeWindowViewController.this.mExpandingBelowNotch;
                if (z2 || z3) {
                    NotificationShadeWindowViewController.this.mExpandingBelowNotch = false;
                }
                if (z3 || !NotificationShadeWindowViewController.this.mService.shouldIgnoreTouch()) {
                    if (z && NotificationShadeWindowViewController.this.mNotificationPanelViewController.isFullyCollapsed()) {
                        NotificationShadeWindowViewController.this.mNotificationPanelViewController.startExpandLatencyTracking();
                    }
                    if (z) {
                        NotificationShadeWindowViewController.this.setTouchActive(true);
                        NotificationShadeWindowViewController.this.mTouchCancelled = false;
                    } else if (motionEvent.getActionMasked() == 1 || motionEvent.getActionMasked() == 3) {
                        NotificationShadeWindowViewController.this.setTouchActive(false);
                    }
                    if (!NotificationShadeWindowViewController.this.mTouchCancelled && !NotificationShadeWindowViewController.this.mExpandAnimationRunning && !NotificationShadeWindowViewController.this.mExpandAnimationPending) {
                        NotificationShadeWindowViewController.this.mFalsingManager.onTouchEvent(motionEvent, NotificationShadeWindowViewController.this.mView.getWidth(), NotificationShadeWindowViewController.this.mView.getHeight());
                        NotificationShadeWindowViewController.this.mGestureDetector.onTouchEvent(motionEvent);
                        if (NotificationShadeWindowViewController.this.mBrightnessMirror == null || NotificationShadeWindowViewController.this.mBrightnessMirror.getVisibility() != 0 || motionEvent.getActionMasked() != 5) {
                            if (z) {
                                NotificationShadeWindowViewController.this.mStackScrollLayout.closeControlsIfOutsideTouch(motionEvent);
                            }
                            if (NotificationShadeWindowViewController.this.mStatusBarStateController.isDozing()) {
                                NotificationShadeWindowViewController.this.mService.mDozeScrimController.extendPulse();
                            }
                            if (z && motionEvent.getY() >= NotificationShadeWindowViewController.this.mView.getBottom()) {
                                NotificationShadeWindowViewController.this.mExpandingBelowNotch = true;
                                z4 = true;
                            }
                            if (z4) {
                                return Boolean.valueOf(NotificationShadeWindowViewController.this.mStatusBarView.dispatchTouchEvent(motionEvent));
                            }
                            if (NotificationShadeWindowViewController.this.mIsTrackingBarGesture || !z || !NotificationShadeWindowViewController.this.mNotificationPanelViewController.isFullyCollapsed()) {
                                if (!NotificationShadeWindowViewController.this.mIsTrackingBarGesture) {
                                    return null;
                                }
                                boolean zDispatchTouchEvent = NotificationShadeWindowViewController.this.mStatusBarView.dispatchTouchEvent(motionEvent);
                                if (z2 || z3) {
                                    NotificationShadeWindowViewController.this.mIsTrackingBarGesture = false;
                                }
                                return Boolean.valueOf(zDispatchTouchEvent);
                            }
                            float rawX = motionEvent.getRawX();
                            float rawY = motionEvent.getRawY();
                            NotificationShadeWindowViewController notificationShadeWindowViewController = NotificationShadeWindowViewController.this;
                            if (!notificationShadeWindowViewController.isIntersecting(notificationShadeWindowViewController.mStatusBarView, rawX, rawY)) {
                                return null;
                            }
                            if (NotificationShadeWindowViewController.this.mService.isSameStatusBarState(0)) {
                                NotificationShadeWindowViewController.this.mIsTrackingBarGesture = true;
                                return Boolean.valueOf(NotificationShadeWindowViewController.this.mStatusBarView.dispatchTouchEvent(motionEvent));
                            }
                            return Boolean.TRUE;
                        }
                        return Boolean.FALSE;
                    }
                    return Boolean.FALSE;
                }
                return Boolean.FALSE;
            }

            @Override // com.android.systemui.statusbar.phone.NotificationShadeWindowView.InteractionEventHandler
            public boolean shouldInterceptTouchEvent(MotionEvent motionEvent) {
                if (NotificationShadeWindowViewController.this.mStatusBarStateController.isDozing() && !NotificationShadeWindowViewController.this.mService.isPulsing() && !NotificationShadeWindowViewController.this.mDockManager.isDocked()) {
                    return true;
                }
                if (!NotificationShadeWindowViewController.this.mNotificationPanelViewController.isFullyExpanded() || !NotificationShadeWindowViewController.this.mDragDownHelper.isDragDownEnabled() || NotificationShadeWindowViewController.this.mService.isBouncerShowing() || NotificationShadeWindowViewController.this.mStatusBarStateController.isDozing()) {
                    return false;
                }
                return NotificationShadeWindowViewController.this.mDragDownHelper.onInterceptTouchEvent(motionEvent);
            }

            @Override // com.android.systemui.statusbar.phone.NotificationShadeWindowView.InteractionEventHandler
            public void didIntercept(MotionEvent motionEvent) {
                MotionEvent motionEventObtain = MotionEvent.obtain(motionEvent);
                motionEventObtain.setAction(3);
                NotificationShadeWindowViewController.this.mStackScrollLayout.onInterceptTouchEvent(motionEventObtain);
                NotificationShadeWindowViewController.this.mNotificationPanelViewController.getView().onInterceptTouchEvent(motionEventObtain);
                motionEventObtain.recycle();
            }

            @Override // com.android.systemui.statusbar.phone.NotificationShadeWindowView.InteractionEventHandler
            public boolean handleTouchEvent(MotionEvent motionEvent) {
                boolean z = NotificationShadeWindowViewController.this.mStatusBarStateController.isDozing() ? !NotificationShadeWindowViewController.this.mService.isPulsing() : false;
                return ((!NotificationShadeWindowViewController.this.mDragDownHelper.isDragDownEnabled() || z) && !NotificationShadeWindowViewController.this.mDragDownHelper.isDraggingDown()) ? z : NotificationShadeWindowViewController.this.mDragDownHelper.onTouchEvent(motionEvent);
            }

            @Override // com.android.systemui.statusbar.phone.NotificationShadeWindowView.InteractionEventHandler
            public void didNotHandleTouchEvent(MotionEvent motionEvent) {
                int actionMasked = motionEvent.getActionMasked();
                if (actionMasked == 1 || actionMasked == 3) {
                    NotificationShadeWindowViewController.this.mService.setInteracting(1, false);
                }
            }

            @Override // com.android.systemui.statusbar.phone.NotificationShadeWindowView.InteractionEventHandler
            public boolean interceptMediaKey(KeyEvent keyEvent) {
                return NotificationShadeWindowViewController.this.mService.interceptMediaKey(keyEvent);
            }

            @Override // com.android.systemui.statusbar.phone.NotificationShadeWindowView.InteractionEventHandler
            public boolean dispatchKeyEvent(KeyEvent keyEvent) {
                boolean z = keyEvent.getAction() == 0;
                int keyCode = keyEvent.getKeyCode();
                if (keyCode == 4) {
                    if (!z) {
                        NotificationShadeWindowViewController.this.mService.onBackPressed();
                    }
                    return true;
                }
                if (keyCode != 62) {
                    if (keyCode != 82) {
                        if ((keyCode == 24 || keyCode == 25) && NotificationShadeWindowViewController.this.mStatusBarStateController.isDozing()) {
                            MediaSessionLegacyHelper.getHelper(NotificationShadeWindowViewController.this.mView.getContext()).sendVolumeKeyEvent(keyEvent, Integer.MIN_VALUE, true);
                            return true;
                        }
                    } else if (!z) {
                        return NotificationShadeWindowViewController.this.mService.onMenuPressed();
                    }
                } else if (!z) {
                    return NotificationShadeWindowViewController.this.mService.onSpacePressed();
                }
                return false;
            }
        });
        this.mView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() { // from class: com.android.systemui.statusbar.phone.NotificationShadeWindowViewController.3
            @Override // android.view.ViewGroup.OnHierarchyChangeListener
            public void onChildViewRemoved(View view, View view2) {
            }

            @Override // android.view.ViewGroup.OnHierarchyChangeListener
            public void onChildViewAdded(View view, View view2) {
                if (view2.getId() == R.id.brightness_mirror) {
                    NotificationShadeWindowViewController.this.mBrightnessMirror = view2;
                }
            }
        });
        setDragDownHelper(new DragDownHelper(this.mView.getContext(), this.mView, this.mStackScrollLayout.getExpandHelperCallback(), this.mStackScrollLayout.getDragDownCallback(), this.mFalsingManager));
        this.mDepthController.setRoot(this.mView);
        this.mNotificationPanelViewController.addExpansionListener(this.mDepthController);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setupExpandedStatusBar$0(String str, String str2) {
        AmbientDisplayConfiguration ambientDisplayConfiguration;
        ambientDisplayConfiguration = new AmbientDisplayConfiguration(this.mView.getContext());
        str.hashCode();
        switch (str) {
            case "double_tap_to_wake":
                this.mDoubleTapEnabledNative = TunerService.parseIntegerSwitch(str2, false);
                break;
            case "lineagesystem:double_tap_sleep_gesture":
                this.mDoubleTapToSleepEnabled = TunerService.parseIntegerSwitch(str2, true);
                break;
            case "doze_tap_gesture":
                this.mSingleTapEnabled = ambientDisplayConfiguration.tapGestureEnabled(-2);
                break;
            case "doze_pulse_on_double_tap":
                this.mDoubleTapEnabled = true;
                break;
        }
    }

    public NotificationShadeWindowView getView() {
        return this.mView;
    }

    public void setTouchActive(boolean z) {
        this.mTouchActive = z;
    }

    public void cancelCurrentTouch() {
        if (this.mTouchActive) {
            long jUptimeMillis = SystemClock.uptimeMillis();
            MotionEvent motionEventObtain = MotionEvent.obtain(jUptimeMillis, jUptimeMillis, 3, 0.0f, 0.0f, 0);
            motionEventObtain.setSource(4098);
            this.mView.dispatchTouchEvent(motionEventObtain);
            motionEventObtain.recycle();
            this.mTouchCancelled = true;
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print("  mExpandAnimationPending=");
        printWriter.println(this.mExpandAnimationPending);
        printWriter.print("  mExpandAnimationRunning=");
        printWriter.println(this.mExpandAnimationRunning);
        printWriter.print("  mTouchCancelled=");
        printWriter.println(this.mTouchCancelled);
        printWriter.print("  mTouchActive=");
        printWriter.println(this.mTouchActive);
    }

    public void setExpandAnimationPending(boolean z) {
        if (this.mExpandAnimationPending != z) {
            this.mExpandAnimationPending = z;
            this.mNotificationShadeWindowController.setLaunchingActivity(this.mExpandAnimationRunning | z);
        }
    }

    public void setExpandAnimationRunning(boolean z) {
        if (this.mExpandAnimationRunning != z) {
            this.mExpandAnimationRunning = z;
            this.mNotificationShadeWindowController.setLaunchingActivity(this.mExpandAnimationPending | z);
        }
    }

    public void cancelExpandHelper() {
        NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScrollLayout;
        if (notificationStackScrollLayout != null) {
            notificationStackScrollLayout.cancelExpandHelper();
        }
    }

    public PhoneStatusBarTransitions getBarTransitions() {
        return this.mBarTransitions;
    }

    public void setStatusBarView(PhoneStatusBarView phoneStatusBarView) {
        SuperStatusBarViewFactory superStatusBarViewFactory;
        this.mStatusBarView = phoneStatusBarView;
        if (phoneStatusBarView == null || (superStatusBarViewFactory = this.mStatusBarViewFactory) == null) {
            return;
        }
        this.mBarTransitions = new PhoneStatusBarTransitions(phoneStatusBarView, superStatusBarViewFactory.getStatusBarWindowView().findViewById(R.id.status_bar_container));
    }

    public void setService(StatusBar statusBar, NotificationShadeWindowController notificationShadeWindowController) {
        this.mService = statusBar;
        this.mNotificationShadeWindowController = notificationShadeWindowController;
    }

    @VisibleForTesting
    void setDragDownHelper(DragDownHelper dragDownHelper) {
        this.mDragDownHelper = dragDownHelper;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isIntersecting(View view, float f, float f2) {
        this.mTempLocation = view.getLocationOnScreen();
        this.mTempRect.set(r0[0], r0[1], r0[0] + view.getWidth(), this.mTempLocation[1] + view.getHeight());
        return this.mTempRect.contains(f, f2);
    }
}
