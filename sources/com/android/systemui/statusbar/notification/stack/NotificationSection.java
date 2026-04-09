package com.android.systemui.statusbar.notification.stack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Rect;
import android.view.View;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.notification.row.ExpandableView;

/* loaded from: classes.dex */
public class NotificationSection {
    private int mBucket;
    private ExpandableView mFirstVisibleChild;
    private ExpandableView mLastVisibleChild;
    private View mOwningView;
    private Rect mBounds = new Rect();
    private Rect mCurrentBounds = new Rect(-1, -1, -1, -1);
    private Rect mStartAnimationRect = new Rect();
    private Rect mEndAnimationRect = new Rect();
    private ObjectAnimator mTopAnimator = null;
    private ObjectAnimator mBottomAnimator = null;

    NotificationSection(View view, int i) {
        this.mOwningView = view;
        this.mBucket = i;
    }

    public void cancelAnimators() {
        ObjectAnimator objectAnimator = this.mBottomAnimator;
        if (objectAnimator != null) {
            objectAnimator.cancel();
        }
        ObjectAnimator objectAnimator2 = this.mTopAnimator;
        if (objectAnimator2 != null) {
            objectAnimator2.cancel();
        }
    }

    public Rect getCurrentBounds() {
        return this.mCurrentBounds;
    }

    public Rect getBounds() {
        return this.mBounds;
    }

    public boolean didBoundsChange() {
        return !this.mCurrentBounds.equals(this.mBounds);
    }

    public boolean areBoundsAnimating() {
        return (this.mBottomAnimator == null && this.mTopAnimator == null) ? false : true;
    }

    public int getBucket() {
        return this.mBucket;
    }

    public void startBackgroundAnimation(boolean z, boolean z2) {
        Rect rect = this.mCurrentBounds;
        Rect rect2 = this.mBounds;
        rect.left = rect2.left;
        rect.right = rect2.right;
        startBottomAnimation(z2);
        startTopAnimation(z);
    }

    private void startTopAnimation(boolean z) {
        int i = this.mEndAnimationRect.top;
        int i2 = this.mBounds.top;
        ObjectAnimator objectAnimator = this.mTopAnimator;
        if (objectAnimator == null || i != i2) {
            if (!z) {
                if (objectAnimator != null) {
                    int i3 = this.mStartAnimationRect.top;
                    objectAnimator.getValues()[0].setIntValues(i3, i2);
                    this.mStartAnimationRect.top = i3;
                    this.mEndAnimationRect.top = i2;
                    objectAnimator.setCurrentPlayTime(objectAnimator.getCurrentPlayTime());
                    return;
                }
                setBackgroundTop(i2);
                return;
            }
            if (objectAnimator != null) {
                objectAnimator.cancel();
            }
            ObjectAnimator objectAnimatorOfInt = ObjectAnimator.ofInt(this, "backgroundTop", this.mCurrentBounds.top, i2);
            objectAnimatorOfInt.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            objectAnimatorOfInt.setDuration(360L);
            objectAnimatorOfInt.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.notification.stack.NotificationSection.1
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    NotificationSection.this.mStartAnimationRect.top = -1;
                    NotificationSection.this.mEndAnimationRect.top = -1;
                    NotificationSection.this.mTopAnimator = null;
                }
            });
            objectAnimatorOfInt.start();
            this.mStartAnimationRect.top = this.mCurrentBounds.top;
            this.mEndAnimationRect.top = i2;
            this.mTopAnimator = objectAnimatorOfInt;
        }
    }

    private void startBottomAnimation(boolean z) {
        int i = this.mStartAnimationRect.bottom;
        int i2 = this.mEndAnimationRect.bottom;
        int i3 = this.mBounds.bottom;
        ObjectAnimator objectAnimator = this.mBottomAnimator;
        if (objectAnimator == null || i2 != i3) {
            if (!z) {
                if (objectAnimator != null) {
                    objectAnimator.getValues()[0].setIntValues(i, i3);
                    this.mStartAnimationRect.bottom = i;
                    this.mEndAnimationRect.bottom = i3;
                    objectAnimator.setCurrentPlayTime(objectAnimator.getCurrentPlayTime());
                    return;
                }
                setBackgroundBottom(i3);
                return;
            }
            if (objectAnimator != null) {
                objectAnimator.cancel();
            }
            ObjectAnimator objectAnimatorOfInt = ObjectAnimator.ofInt(this, "backgroundBottom", this.mCurrentBounds.bottom, i3);
            objectAnimatorOfInt.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            objectAnimatorOfInt.setDuration(360L);
            objectAnimatorOfInt.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.notification.stack.NotificationSection.2
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    NotificationSection.this.mStartAnimationRect.bottom = -1;
                    NotificationSection.this.mEndAnimationRect.bottom = -1;
                    NotificationSection.this.mBottomAnimator = null;
                }
            });
            objectAnimatorOfInt.start();
            this.mStartAnimationRect.bottom = this.mCurrentBounds.bottom;
            this.mEndAnimationRect.bottom = i3;
            this.mBottomAnimator = objectAnimatorOfInt;
        }
    }

    private void setBackgroundTop(int i) {
        this.mCurrentBounds.top = i;
        this.mOwningView.invalidate();
    }

    private void setBackgroundBottom(int i) {
        this.mCurrentBounds.bottom = i;
        this.mOwningView.invalidate();
    }

    public ExpandableView getFirstVisibleChild() {
        return this.mFirstVisibleChild;
    }

    public ExpandableView getLastVisibleChild() {
        return this.mLastVisibleChild;
    }

    public boolean setFirstVisibleChild(ExpandableView expandableView) {
        boolean z = this.mFirstVisibleChild != expandableView;
        this.mFirstVisibleChild = expandableView;
        return z;
    }

    public boolean setLastVisibleChild(ExpandableView expandableView) {
        boolean z = this.mLastVisibleChild != expandableView;
        this.mLastVisibleChild = expandableView;
        return z;
    }

    public void resetCurrentBounds() {
        this.mCurrentBounds.set(this.mBounds);
    }

    public boolean isTargetTop(int i) {
        ObjectAnimator objectAnimator = this.mTopAnimator;
        return (objectAnimator == null && this.mCurrentBounds.top == i) || (objectAnimator != null && this.mEndAnimationRect.top == i);
    }

    public boolean isTargetBottom(int i) {
        ObjectAnimator objectAnimator = this.mBottomAnimator;
        return (objectAnimator == null && this.mCurrentBounds.bottom == i) || (objectAnimator != null && this.mEndAnimationRect.bottom == i);
    }

    public int updateBounds(int i, int i2, boolean z) {
        int iMax;
        int iMax2;
        ExpandableView firstVisibleChild = getFirstVisibleChild();
        if (firstVisibleChild != null) {
            int iCeil = (int) Math.ceil(ViewState.getFinalTranslationY(firstVisibleChild));
            iMax2 = Math.max(isTargetTop(iCeil) ? iCeil : (int) Math.ceil(firstVisibleChild.getTranslationY()), i);
            if (firstVisibleChild.showingPulsing()) {
                iMax = Math.max(i, iCeil + ExpandableViewState.getFinalActualHeight(firstVisibleChild));
                if (z) {
                    this.mBounds.left = (int) (r9.left + Math.max(firstVisibleChild.getTranslation(), 0.0f));
                    this.mBounds.right = (int) (r9.right + Math.min(firstVisibleChild.getTranslation(), 0.0f));
                }
            } else {
                iMax = i;
            }
        } else {
            iMax = i;
            iMax2 = iMax;
        }
        int iMax3 = Math.max(i, iMax2);
        ExpandableView lastVisibleChild = getLastVisibleChild();
        if (lastVisibleChild != null) {
            int iFloor = (int) Math.floor((ViewState.getFinalTranslationY(lastVisibleChild) + ExpandableViewState.getFinalActualHeight(lastVisibleChild)) - lastVisibleChild.getClipBottomAmount());
            if (!isTargetBottom(iFloor)) {
                iFloor = (int) ((lastVisibleChild.getTranslationY() + lastVisibleChild.getActualHeight()) - lastVisibleChild.getClipBottomAmount());
                i2 = (int) Math.min(lastVisibleChild.getTranslationY() + lastVisibleChild.getActualHeight(), i2);
            }
            iMax = Math.max(iMax, Math.max(iFloor, i2));
        }
        int iMax4 = Math.max(iMax3, iMax);
        Rect rect = this.mBounds;
        rect.top = iMax3;
        rect.bottom = iMax4;
        return iMax4;
    }

    public boolean needsBackground() {
        return (this.mFirstVisibleChild == null || this.mBucket == 1) ? false : true;
    }
}
