package com.android.systemui.statusbar.phone;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import com.android.systemui.R;
import com.android.systemui.fragments.FragmentHostManager;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.statusbar.notification.AboveShelfObserver;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.ToIntFunction;

/* loaded from: classes.dex */
public class NotificationsQuickSettingsContainer extends FrameLayout implements ViewStub.OnInflateListener, FragmentHostManager.FragmentListener, AboveShelfObserver.HasViewAboveShelfChangedListener {
    private int mBottomPadding;
    private boolean mCustomizerAnimating;
    private ArrayList<View> mDrawingOrderedChildren;
    private boolean mHasViewsAboveShelf;
    private final Comparator<View> mIndexComparator;
    private boolean mInflated;
    private View mKeyguardStatusBar;
    private ArrayList<View> mLayoutDrawingOrder;
    private boolean mQsExpanded;
    private FrameLayout mQsFrame;
    private NotificationStackScrollLayout mStackScroller;
    private int mStackScrollerMargin;
    private View mUserSwitcher;

    public NotificationsQuickSettingsContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mDrawingOrderedChildren = new ArrayList<>();
        this.mLayoutDrawingOrder = new ArrayList<>();
        this.mIndexComparator = Comparator.comparingInt(new ToIntFunction() { // from class: com.android.systemui.statusbar.phone.NotificationsQuickSettingsContainer$$ExternalSyntheticLambda0
            @Override // java.util.function.ToIntFunction
            public final int applyAsInt(Object obj) {
                return this.f$0.indexOfChild((View) obj);
            }
        });
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mQsFrame = (FrameLayout) findViewById(R.id.qs_frame);
        NotificationStackScrollLayout notificationStackScrollLayout = (NotificationStackScrollLayout) findViewById(R.id.notification_stack_scroller);
        this.mStackScroller = notificationStackScrollLayout;
        this.mStackScrollerMargin = ((FrameLayout.LayoutParams) notificationStackScrollLayout.getLayoutParams()).bottomMargin;
        this.mKeyguardStatusBar = findViewById(R.id.keyguard_header);
        ViewStub viewStub = (ViewStub) findViewById(R.id.keyguard_user_switcher);
        viewStub.setOnInflateListener(this);
        this.mUserSwitcher = viewStub;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        FragmentHostManager.get(this).addTagListener(QS.TAG, this);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        FragmentHostManager.get(this).removeTagListener(QS.TAG, this);
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        reloadWidth(this.mQsFrame, R.dimen.qs_panel_width);
        reloadWidth(this.mStackScroller, R.dimen.notification_panel_width);
    }

    private void reloadWidth(View view, int i) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = getResources().getDimensionPixelSize(i);
        view.setLayoutParams(layoutParams);
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        int stableInsetBottom = windowInsets.getStableInsetBottom();
        this.mBottomPadding = stableInsetBottom;
        setPadding(0, 0, 0, stableInsetBottom);
        return windowInsets;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        this.mDrawingOrderedChildren.clear();
        this.mLayoutDrawingOrder.clear();
        if (this.mInflated && this.mUserSwitcher.getVisibility() == 0) {
            this.mDrawingOrderedChildren.add(this.mUserSwitcher);
            this.mLayoutDrawingOrder.add(this.mUserSwitcher);
        }
        if (this.mKeyguardStatusBar.getVisibility() == 0) {
            this.mDrawingOrderedChildren.add(this.mKeyguardStatusBar);
            this.mLayoutDrawingOrder.add(this.mKeyguardStatusBar);
        }
        if (this.mStackScroller.getVisibility() == 0) {
            this.mDrawingOrderedChildren.add(this.mStackScroller);
            this.mLayoutDrawingOrder.add(this.mStackScroller);
        }
        if (this.mQsFrame.getVisibility() == 0) {
            this.mDrawingOrderedChildren.add(this.mQsFrame);
            this.mLayoutDrawingOrder.add(this.mQsFrame);
        }
        if (this.mHasViewsAboveShelf) {
            this.mDrawingOrderedChildren.remove(this.mStackScroller);
            this.mDrawingOrderedChildren.add(this.mStackScroller);
        }
        this.mLayoutDrawingOrder.sort(this.mIndexComparator);
        super.dispatchDraw(canvas);
    }

    @Override // android.view.ViewGroup
    protected boolean drawChild(Canvas canvas, View view, long j) {
        int iIndexOf = this.mLayoutDrawingOrder.indexOf(view);
        if (iIndexOf >= 0) {
            return super.drawChild(canvas, this.mDrawingOrderedChildren.get(iIndexOf), j);
        }
        return super.drawChild(canvas, view, j);
    }

    @Override // android.view.ViewStub.OnInflateListener
    public void onInflate(ViewStub viewStub, View view) {
        if (viewStub == this.mUserSwitcher) {
            this.mUserSwitcher = view;
            this.mInflated = true;
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.android.systemui.fragments.FragmentHostManager.FragmentListener
    public void onFragmentViewCreated(String str, Fragment fragment) {
        ((QS) fragment).setContainer(this);
    }

    public void setQsExpanded(boolean z) {
        if (this.mQsExpanded != z) {
            this.mQsExpanded = z;
            invalidate();
        }
    }

    public void setCustomizerAnimating(boolean z) {
        if (this.mCustomizerAnimating != z) {
            this.mCustomizerAnimating = z;
            invalidate();
        }
    }

    public void setCustomizerShowing(boolean z) {
        if (z) {
            setPadding(0, 0, 0, 0);
            setBottomMargin(this.mStackScroller, 0);
        } else {
            setPadding(0, 0, 0, this.mBottomPadding);
            setBottomMargin(this.mStackScroller, this.mStackScrollerMargin);
        }
        this.mStackScroller.setQsCustomizerShowing(z);
    }

    private void setBottomMargin(View view, int i) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutParams.bottomMargin = i;
        view.setLayoutParams(layoutParams);
    }

    @Override // com.android.systemui.statusbar.notification.AboveShelfObserver.HasViewAboveShelfChangedListener
    public void onHasViewsAboveShelfChanged(boolean z) {
        this.mHasViewsAboveShelf = z;
        invalidate();
    }
}
