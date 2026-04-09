package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import com.android.systemui.ExpandHelper;
import com.android.systemui.Gefingerpoken;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.statusbar.notification.row.ExpandableView;

/* loaded from: classes.dex */
public class DragDownHelper implements Gefingerpoken {
    private ExpandHelper.Callback mCallback;
    private DragDownCallback mDragDownCallback;
    private boolean mDraggedFarEnough;
    private boolean mDraggingDown;
    private FalsingManager mFalsingManager;
    private View mHost;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private float mLastHeight;
    private int mMinDragDistance;
    private final float mSlopMultiplier;
    private ExpandableView mStartingChild;
    private final int[] mTemp2 = new int[2];
    private final float mTouchSlop;

    public interface DragDownCallback {
        boolean isDragDownAnywhereEnabled();

        boolean isDragDownEnabledForView(ExpandableView expandableView);

        boolean isFalsingCheckNeeded();

        void onCrossedThreshold(boolean z);

        void onDragDownReset();

        boolean onDraggedDown(View view, int i);

        void onTouchSlopExceeded();

        void setEmptyDragAmount(float f);
    }

    public DragDownHelper(Context context, View view, ExpandHelper.Callback callback, DragDownCallback dragDownCallback, FalsingManager falsingManager) {
        this.mMinDragDistance = context.getResources().getDimensionPixelSize(R.dimen.keyguard_drag_down_min_distance);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        this.mTouchSlop = viewConfiguration.getScaledTouchSlop();
        this.mSlopMultiplier = viewConfiguration.getScaledAmbiguousGestureMultiplier();
        this.mCallback = callback;
        this.mDragDownCallback = dragDownCallback;
        this.mHost = view;
        this.mFalsingManager = falsingManager;
    }

    @Override // com.android.systemui.Gefingerpoken
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        float f;
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.mDraggedFarEnough = false;
            this.mDraggingDown = false;
            this.mStartingChild = null;
            this.mInitialTouchY = y;
            this.mInitialTouchX = x;
        } else if (actionMasked == 2) {
            float f2 = y - this.mInitialTouchY;
            if (motionEvent.getClassification() == 1) {
                f = this.mTouchSlop * this.mSlopMultiplier;
            } else {
                f = this.mTouchSlop;
            }
            if (f2 > f && f2 > Math.abs(x - this.mInitialTouchX)) {
                this.mFalsingManager.onNotificatonStartDraggingDown();
                this.mDraggingDown = true;
                captureStartingChild(this.mInitialTouchX, this.mInitialTouchY);
                this.mInitialTouchY = y;
                this.mInitialTouchX = x;
                this.mDragDownCallback.onTouchSlopExceeded();
                return this.mStartingChild != null || this.mDragDownCallback.isDragDownAnywhereEnabled();
            }
        }
        return false;
    }

    @Override // com.android.systemui.Gefingerpoken
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!this.mDraggingDown) {
            return false;
        }
        motionEvent.getX();
        float y = motionEvent.getY();
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 1) {
            if (actionMasked == 2) {
                float f = this.mInitialTouchY;
                this.mLastHeight = y - f;
                captureStartingChild(this.mInitialTouchX, f);
                ExpandableView expandableView = this.mStartingChild;
                if (expandableView != null) {
                    handleExpansion(this.mLastHeight, expandableView);
                } else {
                    this.mDragDownCallback.setEmptyDragAmount(this.mLastHeight);
                }
                if (this.mLastHeight > this.mMinDragDistance) {
                    if (!this.mDraggedFarEnough) {
                        this.mDraggedFarEnough = true;
                        this.mDragDownCallback.onCrossedThreshold(true);
                    }
                } else if (this.mDraggedFarEnough) {
                    this.mDraggedFarEnough = false;
                    this.mDragDownCallback.onCrossedThreshold(false);
                }
                return true;
            }
            if (actionMasked == 3) {
                stopDragging();
                return false;
            }
        } else if (!this.mFalsingManager.isUnlockingDisabled() && !isFalseTouch() && this.mDragDownCallback.onDraggedDown(this.mStartingChild, (int) (y - this.mInitialTouchY))) {
            ExpandableView expandableView2 = this.mStartingChild;
            if (expandableView2 == null) {
                cancelExpansion();
            } else {
                this.mCallback.setUserLockedChild(expandableView2, false);
                this.mStartingChild = null;
            }
            this.mDraggingDown = false;
        } else {
            stopDragging();
            return false;
        }
        return false;
    }

    private boolean isFalseTouch() {
        if (this.mDragDownCallback.isFalsingCheckNeeded()) {
            return this.mFalsingManager.isFalseTouch() || !this.mDraggedFarEnough;
        }
        return false;
    }

    private void captureStartingChild(float f, float f2) {
        if (this.mStartingChild == null) {
            ExpandableView expandableViewFindView = findView(f, f2);
            this.mStartingChild = expandableViewFindView;
            if (expandableViewFindView != null) {
                if (this.mDragDownCallback.isDragDownEnabledForView(expandableViewFindView)) {
                    this.mCallback.setUserLockedChild(this.mStartingChild, true);
                } else {
                    this.mStartingChild = null;
                }
            }
        }
    }

    private void handleExpansion(float f, ExpandableView expandableView) {
        if (f < 0.0f) {
            f = 0.0f;
        }
        boolean zIsContentExpandable = expandableView.isContentExpandable();
        float collapsedHeight = f * (zIsContentExpandable ? 0.5f : 0.15f);
        if (zIsContentExpandable && expandableView.getCollapsedHeight() + collapsedHeight > expandableView.getMaxContentHeight()) {
            collapsedHeight -= ((expandableView.getCollapsedHeight() + collapsedHeight) - expandableView.getMaxContentHeight()) * 0.85f;
        }
        expandableView.setActualHeight((int) (expandableView.getCollapsedHeight() + collapsedHeight));
    }

    private void cancelExpansion(final ExpandableView expandableView) {
        if (expandableView.getActualHeight() == expandableView.getCollapsedHeight()) {
            this.mCallback.setUserLockedChild(expandableView, false);
            return;
        }
        ObjectAnimator objectAnimatorOfInt = ObjectAnimator.ofInt(expandableView, "actualHeight", expandableView.getActualHeight(), expandableView.getCollapsedHeight());
        objectAnimatorOfInt.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        objectAnimatorOfInt.setDuration(375L);
        objectAnimatorOfInt.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.DragDownHelper.1
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                DragDownHelper.this.mCallback.setUserLockedChild(expandableView, false);
            }
        });
        objectAnimatorOfInt.start();
    }

    private void cancelExpansion() {
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(this.mLastHeight, 0.0f);
        valueAnimatorOfFloat.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        valueAnimatorOfFloat.setDuration(375L);
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.DragDownHelper$$ExternalSyntheticLambda0
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.f$0.lambda$cancelExpansion$0(valueAnimator);
            }
        });
        valueAnimatorOfFloat.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$cancelExpansion$0(ValueAnimator valueAnimator) {
        this.mDragDownCallback.setEmptyDragAmount(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    private void stopDragging() {
        this.mFalsingManager.onNotificatonStopDraggingDown();
        ExpandableView expandableView = this.mStartingChild;
        if (expandableView != null) {
            cancelExpansion(expandableView);
            this.mStartingChild = null;
        } else {
            cancelExpansion();
        }
        this.mDraggingDown = false;
        this.mDragDownCallback.onDragDownReset();
    }

    private ExpandableView findView(float f, float f2) {
        this.mHost.getLocationOnScreen(this.mTemp2);
        int[] iArr = this.mTemp2;
        return this.mCallback.getChildAtRawPosition(f + iArr[0], f2 + iArr[1]);
    }

    public boolean isDraggingDown() {
        return this.mDraggingDown;
    }

    public boolean isDragDownEnabled() {
        return this.mDragDownCallback.isDragDownEnabledForView(null);
    }
}
