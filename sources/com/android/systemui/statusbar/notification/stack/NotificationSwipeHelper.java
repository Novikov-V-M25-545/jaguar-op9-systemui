package com.android.systemui.statusbar.notification.stack;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.service.notification.StatusBarNotification;
import android.view.MotionEvent;
import android.view.View;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.SwipeHelper;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin;
import com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import java.lang.ref.WeakReference;

/* loaded from: classes.dex */
class NotificationSwipeHelper extends SwipeHelper implements NotificationSwipeActionHelper {

    @VisibleForTesting
    protected static final long COVER_MENU_DELAY = 4000;
    private final NotificationCallback mCallback;
    private WeakReference<NotificationMenuRowPlugin> mCurrMenuRowRef;
    private final Runnable mFalsingCheck;
    private boolean mIsExpanded;
    private View mMenuExposedView;
    private final NotificationMenuRowPlugin.OnMenuEventListener mMenuListener;
    private boolean mPulsing;
    private View mTranslatingParentView;

    public interface NotificationCallback extends SwipeHelper.Callback {
        void handleChildViewDismissed(View view);

        void onDismiss();

        void onSnooze(StatusBarNotification statusBarNotification, int i);

        void onSnooze(StatusBarNotification statusBarNotification, NotificationSwipeActionHelper.SnoozeOption snoozeOption);

        boolean shouldDismissQuickly();
    }

    NotificationSwipeHelper(int i, NotificationCallback notificationCallback, Context context, NotificationMenuRowPlugin.OnMenuEventListener onMenuEventListener, FalsingManager falsingManager) {
        super(i, notificationCallback, context, falsingManager);
        this.mMenuListener = onMenuEventListener;
        this.mCallback = notificationCallback;
        this.mFalsingCheck = new Runnable() { // from class: com.android.systemui.statusbar.notification.stack.NotificationSwipeHelper$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$new$0();
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0() {
        resetExposedMenuView(true, true);
    }

    public View getTranslatingParentView() {
        return this.mTranslatingParentView;
    }

    public void clearTranslatingParentView() {
        setTranslatingParentView(null);
    }

    @VisibleForTesting
    protected void setTranslatingParentView(View view) {
        this.mTranslatingParentView = view;
    }

    public void setExposedMenuView(View view) {
        this.mMenuExposedView = view;
    }

    public void clearExposedMenuView() {
        setExposedMenuView(null);
    }

    public void clearCurrentMenuRow() {
        setCurrentMenuRow(null);
    }

    public View getExposedMenuView() {
        return this.mMenuExposedView;
    }

    @VisibleForTesting
    void setCurrentMenuRow(NotificationMenuRowPlugin notificationMenuRowPlugin) {
        this.mCurrMenuRowRef = notificationMenuRowPlugin != null ? new WeakReference<>(notificationMenuRowPlugin) : null;
    }

    public NotificationMenuRowPlugin getCurrentMenuRow() {
        WeakReference<NotificationMenuRowPlugin> weakReference = this.mCurrMenuRowRef;
        if (weakReference == null) {
            return null;
        }
        return weakReference.get();
    }

    @VisibleForTesting
    protected Handler getHandler() {
        return this.mHandler;
    }

    @VisibleForTesting
    protected Runnable getFalsingCheck() {
        return this.mFalsingCheck;
    }

    public void setIsExpanded(boolean z) {
        this.mIsExpanded = z;
    }

    @Override // com.android.systemui.SwipeHelper
    protected void onChildSnappedBack(View view, float f) {
        NotificationMenuRowPlugin currentMenuRow = getCurrentMenuRow();
        if (currentMenuRow == null || f != 0.0f) {
            return;
        }
        currentMenuRow.resetMenu();
        clearCurrentMenuRow();
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.android.systemui.SwipeHelper
    public void onDownUpdate(View view, MotionEvent motionEvent) {
        this.mTranslatingParentView = view;
        NotificationMenuRowPlugin currentMenuRow = getCurrentMenuRow();
        if (currentMenuRow != null) {
            currentMenuRow.onTouchStart();
        }
        clearCurrentMenuRow();
        getHandler().removeCallbacks(getFalsingCheck());
        resetExposedMenuView(true, false);
        if (view instanceof SwipeableView) {
            initializeRow((SwipeableView) view);
        }
    }

    @VisibleForTesting
    protected void initializeRow(SwipeableView swipeableView) {
        if (swipeableView.hasFinishedInitialization()) {
            NotificationMenuRowPlugin notificationMenuRowPluginCreateMenu = swipeableView.createMenu();
            setCurrentMenuRow(notificationMenuRowPluginCreateMenu);
            if (notificationMenuRowPluginCreateMenu != null) {
                notificationMenuRowPluginCreateMenu.setMenuClickListener(this.mMenuListener);
                notificationMenuRowPluginCreateMenu.onTouchStart();
            }
        }
    }

    private boolean swipedEnoughToShowMenu(NotificationMenuRowPlugin notificationMenuRowPlugin) {
        return !swipedFarEnough() && notificationMenuRowPlugin.isSwipedEnoughToShowMenu();
    }

    @Override // com.android.systemui.SwipeHelper
    public void onMoveUpdate(View view, MotionEvent motionEvent, float f, float f2) {
        getHandler().removeCallbacks(getFalsingCheck());
        NotificationMenuRowPlugin currentMenuRow = getCurrentMenuRow();
        if (currentMenuRow != null) {
            currentMenuRow.onTouchMove(f2);
        }
    }

    @Override // com.android.systemui.SwipeHelper
    public boolean handleUpEvent(MotionEvent motionEvent, View view, float f, float f2) {
        NotificationMenuRowPlugin currentMenuRow = getCurrentMenuRow();
        if (currentMenuRow == null) {
            return false;
        }
        currentMenuRow.onTouchEnd();
        handleMenuRowSwipe(motionEvent, view, f, currentMenuRow);
        return true;
    }

    @VisibleForTesting
    protected void handleMenuRowSwipe(MotionEvent motionEvent, View view, float f, NotificationMenuRowPlugin notificationMenuRowPlugin) {
        if (!notificationMenuRowPlugin.shouldShowMenu()) {
            if (isDismissGesture(motionEvent)) {
                dismiss(view, f);
                return;
            } else {
                snapClosed(view, f);
                notificationMenuRowPlugin.onSnapClosed();
                return;
            }
        }
        if (notificationMenuRowPlugin.isSnappedAndOnSameSide()) {
            handleSwipeFromOpenState(motionEvent, view, f, notificationMenuRowPlugin);
        } else {
            handleSwipeFromClosedState(motionEvent, view, f, notificationMenuRowPlugin);
        }
    }

    private void handleSwipeFromClosedState(MotionEvent motionEvent, View view, float f, NotificationMenuRowPlugin notificationMenuRowPlugin) {
        boolean zIsDismissGesture = isDismissGesture(motionEvent);
        boolean zIsTowardsMenu = notificationMenuRowPlugin.isTowardsMenu(f);
        boolean z = getEscapeVelocity() <= Math.abs(f);
        boolean z2 = !notificationMenuRowPlugin.canBeDismissed() && ((double) (motionEvent.getEventTime() - motionEvent.getDownTime())) >= 200.0d;
        boolean z3 = zIsTowardsMenu && !zIsDismissGesture;
        boolean z4 = (swipedEnoughToShowMenu(notificationMenuRowPlugin) && (!z || z2)) || ((z && !zIsTowardsMenu && !zIsDismissGesture) && (notificationMenuRowPlugin.shouldShowGutsOnSnapOpen() || (this.mIsExpanded && !this.mPulsing)));
        int menuSnapTarget = notificationMenuRowPlugin.getMenuSnapTarget();
        boolean z5 = !isFalseGesture() && z4;
        if ((z3 || z5) && menuSnapTarget != 0) {
            snapOpen(view, menuSnapTarget, f);
            notificationMenuRowPlugin.onSnapOpen();
        } else if (isDismissGesture(motionEvent) && !zIsTowardsMenu) {
            dismiss(view, f);
            notificationMenuRowPlugin.onDismiss();
        } else {
            snapClosed(view, f);
            notificationMenuRowPlugin.onSnapClosed();
        }
    }

    private void handleSwipeFromOpenState(MotionEvent motionEvent, View view, float f, NotificationMenuRowPlugin notificationMenuRowPlugin) {
        boolean zIsDismissGesture = isDismissGesture(motionEvent);
        if (notificationMenuRowPlugin.isWithinSnapMenuThreshold() && !zIsDismissGesture) {
            notificationMenuRowPlugin.onSnapOpen();
            snapOpen(view, notificationMenuRowPlugin.getMenuSnapTarget(), f);
        } else if (zIsDismissGesture && !notificationMenuRowPlugin.shouldSnapBack()) {
            dismiss(view, f);
            notificationMenuRowPlugin.onDismiss();
        } else {
            snapClosed(view, f);
            notificationMenuRowPlugin.onSnapClosed();
        }
    }

    @Override // com.android.systemui.SwipeHelper
    public void dismissChild(View view, float f, boolean z) {
        superDismissChild(view, f, z);
        if (this.mCallback.shouldDismissQuickly()) {
            this.mCallback.handleChildViewDismissed(view);
        }
        this.mCallback.onDismiss();
        handleMenuCoveredOrDismissed();
    }

    @VisibleForTesting
    protected void superDismissChild(View view, float f, boolean z) {
        super.dismissChild(view, f, z);
    }

    @VisibleForTesting
    protected void superSnapChild(View view, float f, float f2) {
        super.snapChild(view, f, f2);
    }

    @Override // com.android.systemui.SwipeHelper
    public void snapChild(View view, float f, float f2) {
        superSnapChild(view, f, f2);
        this.mCallback.onDragCancelled(view);
        if (f == 0.0f) {
            handleMenuCoveredOrDismissed();
        }
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper
    public void snooze(StatusBarNotification statusBarNotification, NotificationSwipeActionHelper.SnoozeOption snoozeOption) {
        this.mCallback.onSnooze(statusBarNotification, snoozeOption);
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper
    public void snooze(StatusBarNotification statusBarNotification, int i) {
        this.mCallback.onSnooze(statusBarNotification, i);
    }

    @VisibleForTesting
    protected void handleMenuCoveredOrDismissed() {
        View exposedMenuView = getExposedMenuView();
        if (exposedMenuView == null || exposedMenuView != this.mTranslatingParentView) {
            return;
        }
        clearExposedMenuView();
    }

    @VisibleForTesting
    protected Animator superGetViewTranslationAnimator(View view, float f, ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
        return super.getViewTranslationAnimator(view, f, animatorUpdateListener);
    }

    @Override // com.android.systemui.SwipeHelper
    public Animator getViewTranslationAnimator(View view, float f, ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
        if (view instanceof ExpandableNotificationRow) {
            return ((ExpandableNotificationRow) view).getTranslateViewAnimator(f, animatorUpdateListener);
        }
        return superGetViewTranslationAnimator(view, f, animatorUpdateListener);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.android.systemui.SwipeHelper
    public void setTranslation(View view, float f) {
        if (view instanceof SwipeableView) {
            ((SwipeableView) view).setTranslation(f);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.android.systemui.SwipeHelper
    public float getTranslation(View view) {
        if (view instanceof SwipeableView) {
            return ((SwipeableView) view).getTranslation();
        }
        return 0.0f;
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper
    public boolean swipedFastEnough(float f, float f2) {
        return swipedFastEnough();
    }

    @Override // com.android.systemui.SwipeHelper
    @VisibleForTesting
    protected boolean swipedFastEnough() {
        return super.swipedFastEnough();
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper
    public boolean swipedFarEnough(float f, float f2) {
        return swipedFarEnough();
    }

    @Override // com.android.systemui.SwipeHelper
    @VisibleForTesting
    protected boolean swipedFarEnough() {
        return super.swipedFarEnough();
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper
    public void dismiss(View view, float f) {
        dismissChild(view, f, !swipedFastEnough());
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper
    public void snapOpen(View view, int i, float f) {
        snapChild(view, i, f);
    }

    @VisibleForTesting
    protected void snapClosed(View view, float f) {
        snapChild(view, 0.0f, f);
    }

    @Override // com.android.systemui.SwipeHelper
    @VisibleForTesting
    protected float getEscapeVelocity() {
        return super.getEscapeVelocity();
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper
    public float getMinDismissVelocity() {
        return getEscapeVelocity();
    }

    public void onMenuShown(View view) {
        setExposedMenuView(getTranslatingParentView());
        this.mCallback.onDragCancelled(view);
        Handler handler = getHandler();
        if (this.mCallback.isAntiFalsingNeeded()) {
            handler.removeCallbacks(getFalsingCheck());
            handler.postDelayed(getFalsingCheck(), COVER_MENU_DELAY);
        }
    }

    @VisibleForTesting
    protected boolean shouldResetMenu(boolean z) {
        View view = this.mMenuExposedView;
        if (view != null) {
            return z || view != this.mTranslatingParentView;
        }
        return false;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void resetExposedMenuView(boolean z, boolean z2) {
        if (shouldResetMenu(z2)) {
            View exposedMenuView = getExposedMenuView();
            if (z) {
                Animator viewTranslationAnimator = getViewTranslationAnimator(exposedMenuView, 0.0f, null);
                if (viewTranslationAnimator != null) {
                    viewTranslationAnimator.start();
                }
            } else if (exposedMenuView instanceof SwipeableView) {
                SwipeableView swipeableView = (SwipeableView) exposedMenuView;
                if (!swipeableView.isRemoved()) {
                    swipeableView.resetTranslation();
                }
            }
            clearExposedMenuView();
        }
    }

    public static boolean isTouchInView(MotionEvent motionEvent, View view) {
        int height;
        if (view == null) {
            return false;
        }
        if (view instanceof ExpandableView) {
            height = ((ExpandableView) view).getActualHeight();
        } else {
            height = view.getHeight();
        }
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        int[] iArr = new int[2];
        view.getLocationOnScreen(iArr);
        int i = iArr[0];
        int i2 = iArr[1];
        return new Rect(i, i2, view.getWidth() + i, height + i2).contains(x, y);
    }

    public void setPulsing(boolean z) {
        this.mPulsing = z;
    }
}
