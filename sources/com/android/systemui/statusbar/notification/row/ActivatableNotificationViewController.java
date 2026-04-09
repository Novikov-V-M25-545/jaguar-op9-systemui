package com.android.systemui.statusbar.notification.row;

import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import com.android.systemui.Gefingerpoken;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationView;
import com.android.systemui.statusbar.phone.DoubleTapHelper;
import java.util.Objects;

/* loaded from: classes.dex */
public class ActivatableNotificationViewController {
    private final AccessibilityManager mAccessibilityManager;
    private DoubleTapHelper mDoubleTapHelper;
    private final ExpandableOutlineViewController mExpandableOutlineViewController;
    private final FalsingManager mFalsingManager;
    private boolean mNeedsDimming;
    private TouchHandler mTouchHandler = new TouchHandler();
    private final ActivatableNotificationView mView;

    public ActivatableNotificationViewController(ActivatableNotificationView activatableNotificationView, ExpandableOutlineViewController expandableOutlineViewController, AccessibilityManager accessibilityManager, FalsingManager falsingManager) {
        this.mView = activatableNotificationView;
        this.mExpandableOutlineViewController = expandableOutlineViewController;
        this.mAccessibilityManager = accessibilityManager;
        this.mFalsingManager = falsingManager;
        activatableNotificationView.setOnActivatedListener(new ActivatableNotificationView.OnActivatedListener() { // from class: com.android.systemui.statusbar.notification.row.ActivatableNotificationViewController.1
            @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView.OnActivatedListener
            public void onActivationReset(ActivatableNotificationView activatableNotificationView2) {
            }

            @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView.OnActivatedListener
            public void onActivated(ActivatableNotificationView activatableNotificationView2) {
                ActivatableNotificationViewController.this.mFalsingManager.onNotificationActive();
            }
        });
    }

    public void init() {
        this.mExpandableOutlineViewController.init();
        final ActivatableNotificationView activatableNotificationView = this.mView;
        DoubleTapHelper.ActivationListener activationListener = new DoubleTapHelper.ActivationListener() { // from class: com.android.systemui.statusbar.notification.row.ActivatableNotificationViewController$$ExternalSyntheticLambda1
            @Override // com.android.systemui.statusbar.phone.DoubleTapHelper.ActivationListener
            public final void onActiveChanged(boolean z) {
                this.f$0.lambda$init$0(z);
            }
        };
        Objects.requireNonNull(activatableNotificationView);
        DoubleTapHelper.DoubleTapListener doubleTapListener = new DoubleTapHelper.DoubleTapListener() { // from class: com.android.systemui.statusbar.notification.row.ActivatableNotificationViewController$$ExternalSyntheticLambda2
            @Override // com.android.systemui.statusbar.phone.DoubleTapHelper.DoubleTapListener
            public final boolean onDoubleTap() {
                return activatableNotificationView.performClick();
            }
        };
        final ActivatableNotificationView activatableNotificationView2 = this.mView;
        Objects.requireNonNull(activatableNotificationView2);
        DoubleTapHelper.SlideBackListener slideBackListener = new DoubleTapHelper.SlideBackListener() { // from class: com.android.systemui.statusbar.notification.row.ActivatableNotificationViewController$$ExternalSyntheticLambda4
            @Override // com.android.systemui.statusbar.phone.DoubleTapHelper.SlideBackListener
            public final boolean onSlideBack() {
                return activatableNotificationView2.handleSlideBack();
            }
        };
        final FalsingManager falsingManager = this.mFalsingManager;
        Objects.requireNonNull(falsingManager);
        this.mDoubleTapHelper = new DoubleTapHelper(activatableNotificationView, activationListener, doubleTapListener, slideBackListener, new DoubleTapHelper.DoubleTapLogListener() { // from class: com.android.systemui.statusbar.notification.row.ActivatableNotificationViewController$$ExternalSyntheticLambda3
            @Override // com.android.systemui.statusbar.phone.DoubleTapHelper.DoubleTapLogListener
            public final void onDoubleTapLog(boolean z, float f, float f2) {
                falsingManager.onNotificationDoubleTap(z, f, f2);
            }
        });
        this.mView.setOnTouchListener(this.mTouchHandler);
        this.mView.setTouchHandler(this.mTouchHandler);
        this.mView.setOnDimmedListener(new ActivatableNotificationView.OnDimmedListener() { // from class: com.android.systemui.statusbar.notification.row.ActivatableNotificationViewController$$ExternalSyntheticLambda0
            @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView.OnDimmedListener
            public final void onSetDimmed(boolean z) {
                this.f$0.lambda$init$1(z);
            }
        });
        this.mView.setAccessibilityManager(this.mAccessibilityManager);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$init$0(boolean z) {
        if (z) {
            this.mView.makeActive();
            this.mFalsingManager.onNotificationActive();
        } else {
            this.mView.makeInactive(true);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$init$1(boolean z) {
        this.mNeedsDimming = z;
    }

    class TouchHandler implements Gefingerpoken, View.OnTouchListener {
        private boolean mBlockNextTouch;

        @Override // com.android.systemui.Gefingerpoken
        public boolean onTouchEvent(MotionEvent motionEvent) {
            return false;
        }

        TouchHandler() {
        }

        @Override // android.view.View.OnTouchListener
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (!this.mBlockNextTouch) {
                if (!ActivatableNotificationViewController.this.mNeedsDimming || ActivatableNotificationViewController.this.mAccessibilityManager.isTouchExplorationEnabled() || !ActivatableNotificationViewController.this.mView.isInteractive()) {
                    return false;
                }
                if (!ActivatableNotificationViewController.this.mNeedsDimming || ActivatableNotificationViewController.this.mView.isDimmed()) {
                    return ActivatableNotificationViewController.this.mDoubleTapHelper.onTouchEvent(motionEvent, ActivatableNotificationViewController.this.mView.getActualHeight());
                }
                return false;
            }
            this.mBlockNextTouch = false;
            return true;
        }

        @Override // com.android.systemui.Gefingerpoken
        public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
            if (!ActivatableNotificationViewController.this.mNeedsDimming || motionEvent.getActionMasked() != 0 || !ActivatableNotificationViewController.this.mView.disallowSingleClick(motionEvent) || ActivatableNotificationViewController.this.mAccessibilityManager.isTouchExplorationEnabled()) {
                return false;
            }
            if (!ActivatableNotificationViewController.this.mView.isActive()) {
                return true;
            }
            if (ActivatableNotificationViewController.this.mDoubleTapHelper.isWithinDoubleTapSlop(motionEvent)) {
                return false;
            }
            this.mBlockNextTouch = true;
            ActivatableNotificationViewController.this.mView.makeInactive(true);
            return true;
        }
    }
}
