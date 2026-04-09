package com.android.systemui.qs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import androidx.lifecycle.Lifecycle;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.media.MediaHost;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.qs.QSContainerImplController;
import com.android.systemui.qs.customize.QSCustomizer;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.NotificationsQuickSettingsContainer;
import com.android.systemui.statusbar.policy.RemoteInputQuickSettingsDisabler;
import com.android.systemui.util.InjectionInflationController;
import com.android.systemui.util.LifecycleFragment;
import com.android.systemui.util.Utils;
import com.android.systemui.util.animation.UniqueObjectHostView;

/* loaded from: classes.dex */
public class QSFragment extends LifecycleFragment implements QS, CommandQueue.Callbacks, StatusBarStateController.StateListener {
    private QSContainerImpl mContainer;
    private long mDelay;
    private QSFooter mFooter;
    protected QuickStatusBarHeader mHeader;
    private boolean mHeaderAnimating;
    private final QSTileHost mHost;
    private final InjectionInflationController mInjectionInflater;
    private float mLastHeaderTranslation;
    private boolean mLastKeyguardAndExpanded;
    private int mLastViewHeight;
    private int mLayoutDirection;
    private boolean mListening;
    private QS.HeightListener mPanelView;
    private QSAnimator mQSAnimator;
    private QSContainerImplController mQSContainerImplController;
    private final QSContainerImplController.Builder mQSContainerImplControllerBuilder;
    private QSCustomizer mQSCustomizer;
    private QSDetail mQSDetail;
    protected QSPanel mQSPanel;
    protected NonInterceptingScrollView mQSPanelScrollView;
    private boolean mQsDisabled;
    private boolean mQsExpanded;
    private final RemoteInputQuickSettingsDisabler mRemoteInputQuickSettingsDisabler;
    private boolean mSecureExpandDisabled;
    private boolean mShowCollapsedOnKeyguard;
    private boolean mStackScrollerOverscrolling;
    private int mState;
    private final StatusBarStateController mStatusBarStateController;
    private final Rect mQsBounds = new Rect();
    private float mLastQSExpansion = -1.0f;
    private int[] mTmpLocation = new int[2];
    private final ViewTreeObserver.OnPreDrawListener mStartHeaderSlidingIn = new ViewTreeObserver.OnPreDrawListener() { // from class: com.android.systemui.qs.QSFragment.2
        @Override // android.view.ViewTreeObserver.OnPreDrawListener
        public boolean onPreDraw() {
            QSFragment.this.getView().getViewTreeObserver().removeOnPreDrawListener(this);
            QSFragment.this.getView().animate().translationY(0.0f).setStartDelay(QSFragment.this.mDelay).setDuration(448L).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setListener(QSFragment.this.mAnimateHeaderSlidingInListener).start();
            return true;
        }
    };
    private final Animator.AnimatorListener mAnimateHeaderSlidingInListener = new AnimatorListenerAdapter() { // from class: com.android.systemui.qs.QSFragment.3
        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            QSFragment.this.mHeaderAnimating = false;
            QSFragment.this.updateQsState();
        }
    };

    @Override // com.android.systemui.plugins.qs.QS
    public void setHasNotifications(boolean z) {
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setHeaderClickable(boolean z) {
    }

    public QSFragment(RemoteInputQuickSettingsDisabler remoteInputQuickSettingsDisabler, InjectionInflationController injectionInflationController, QSTileHost qSTileHost, StatusBarStateController statusBarStateController, CommandQueue commandQueue, QSContainerImplController.Builder builder) {
        this.mRemoteInputQuickSettingsDisabler = remoteInputQuickSettingsDisabler;
        this.mInjectionInflater = injectionInflationController;
        this.mQSContainerImplControllerBuilder = builder;
        commandQueue.observe(getLifecycle(), (Lifecycle) this);
        this.mHost = qSTileHost;
        this.mStatusBarStateController = statusBarStateController;
    }

    @Override // android.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return this.mInjectionInflater.injectable(layoutInflater.cloneInContext(new ContextThemeWrapper(getContext(), R.style.qs_theme))).inflate(R.layout.qs_panel, viewGroup, false);
    }

    @Override // android.app.Fragment
    public void onViewCreated(View view, Bundle bundle) throws Resources.NotFoundException {
        super.onViewCreated(view, bundle);
        this.mQSPanel = (QSPanel) view.findViewById(R.id.quick_settings_panel);
        NonInterceptingScrollView nonInterceptingScrollView = (NonInterceptingScrollView) view.findViewById(R.id.expanded_qs_scroll_view);
        this.mQSPanelScrollView = nonInterceptingScrollView;
        nonInterceptingScrollView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.android.systemui.qs.QSFragment$$ExternalSyntheticLambda0
            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view2, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                this.f$0.lambda$onViewCreated$0(view2, i, i2, i3, i4, i5, i6, i7, i8);
            }
        });
        this.mQSPanelScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() { // from class: com.android.systemui.qs.QSFragment$$ExternalSyntheticLambda2
            @Override // android.view.View.OnScrollChangeListener
            public final void onScrollChange(View view2, int i, int i2, int i3, int i4) {
                this.f$0.lambda$onViewCreated$1(view2, i, i2, i3, i4);
            }
        });
        this.mQSDetail = (QSDetail) view.findViewById(R.id.qs_detail);
        this.mHeader = (QuickStatusBarHeader) view.findViewById(R.id.header);
        this.mQSPanel.setHeaderContainer((ViewGroup) view.findViewById(R.id.header_text_container));
        this.mFooter = (QSFooter) view.findViewById(R.id.qs_footer);
        this.mContainer = (QSContainerImpl) view.findViewById(R.id.quick_settings_container);
        this.mQSContainerImplController = this.mQSContainerImplControllerBuilder.setQSContainerImpl((QSContainerImpl) view).build();
        this.mQSDetail.setQsPanel(this.mQSPanel, this.mHeader, (View) this.mFooter);
        this.mQSAnimator = new QSAnimator(this, (QuickQSPanel) this.mHeader.findViewById(R.id.quick_qs_panel), this.mQSPanel);
        QSCustomizer qSCustomizer = (QSCustomizer) view.findViewById(R.id.qs_customize);
        this.mQSCustomizer = qSCustomizer;
        qSCustomizer.setQs(this);
        if (bundle != null) {
            setExpanded(bundle.getBoolean("expanded"));
            setListening(bundle.getBoolean("listening"));
            setEditLocation(view);
            this.mQSCustomizer.restoreInstanceState(bundle);
            if (this.mQsExpanded) {
                this.mQSPanel.getTileLayout().restoreInstanceState(bundle);
            }
        }
        setHost(this.mHost);
        this.mStatusBarStateController.addCallback(this);
        onStateChanged(this.mStatusBarStateController.getState());
        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.android.systemui.qs.QSFragment$$ExternalSyntheticLambda1
            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view2, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) throws Resources.NotFoundException {
                this.f$0.lambda$onViewCreated$2(view2, i, i2, i3, i4, i5, i6, i7, i8);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onViewCreated$0(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        updateQsBounds();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onViewCreated$1(View view, int i, int i2, int i3, int i4) {
        this.mQSAnimator.onQsScrollingChanged();
        this.mHeader.setExpandedScrollAmount(i2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onViewCreated$2(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) throws Resources.NotFoundException {
        if (i6 - i8 != i2 - i4) {
            float f = this.mLastQSExpansion;
            setQsExpansion(f, f);
        }
    }

    @Override // com.android.systemui.util.LifecycleFragment, android.app.Fragment
    public void onDestroy() throws Resources.NotFoundException {
        super.onDestroy();
        this.mStatusBarStateController.removeCallback(this);
        if (this.mListening) {
            setListening(false);
        }
        this.mQSCustomizer.setQs(null);
    }

    @Override // android.app.Fragment
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean("expanded", this.mQsExpanded);
        bundle.putBoolean("listening", this.mListening);
        this.mQSCustomizer.saveInstanceState(bundle);
        if (this.mQsExpanded) {
            this.mQSPanel.getTileLayout().saveInstanceState(bundle);
        }
    }

    boolean isListening() {
        return this.mListening;
    }

    boolean isExpanded() {
        return this.mQsExpanded;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public View getHeader() {
        return this.mHeader;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setPanelView(QS.HeightListener heightListener) {
        this.mPanelView = heightListener;
    }

    @Override // android.app.Fragment, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        setEditLocation(getView());
        if (configuration.getLayoutDirection() != this.mLayoutDirection) {
            this.mLayoutDirection = configuration.getLayoutDirection();
            QSAnimator qSAnimator = this.mQSAnimator;
            if (qSAnimator != null) {
                qSAnimator.onRtlChanged();
            }
        }
    }

    private void setEditLocation(View view) {
        View viewFindViewById = view.findViewById(android.R.id.edit);
        int[] locationOnScreen = viewFindViewById.getLocationOnScreen();
        this.mQSCustomizer.setEditLocation(locationOnScreen[0] + (viewFindViewById.getWidth() / 2), locationOnScreen[1] + (viewFindViewById.getHeight() / 2));
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setContainer(ViewGroup viewGroup) {
        if (viewGroup instanceof NotificationsQuickSettingsContainer) {
            this.mQSCustomizer.setContainer((NotificationsQuickSettingsContainer) viewGroup);
        }
    }

    @Override // com.android.systemui.plugins.qs.QS
    public boolean isCustomizing() {
        return this.mQSCustomizer.isCustomizing();
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mQSPanel.setHost(qSTileHost, this.mQSCustomizer);
        this.mHeader.setQSPanel(this.mQSPanel);
        this.mFooter.setQSPanel(this.mQSPanel);
        this.mQSDetail.setHost(qSTileHost);
        QSAnimator qSAnimator = this.mQSAnimator;
        if (qSAnimator != null) {
            qSAnimator.setHost(qSTileHost);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void disable(int i, int i2, int i3, boolean z) throws Resources.NotFoundException {
        if (i != getContext().getDisplayId()) {
            return;
        }
        int iAdjustDisableFlags = this.mRemoteInputQuickSettingsDisabler.adjustDisableFlags(i3);
        boolean z2 = (iAdjustDisableFlags & 1) != 0;
        if (z2 == this.mQsDisabled) {
            return;
        }
        this.mQsDisabled = z2;
        this.mContainer.disable(i2, iAdjustDisableFlags, z);
        this.mHeader.disable(i2, iAdjustDisableFlags, z);
        this.mFooter.disable(i2, iAdjustDisableFlags, z);
        updateQsState();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateQsState() {
        boolean z = this.mQsExpanded;
        boolean z2 = true;
        boolean z3 = z || this.mStackScrollerOverscrolling || this.mHeaderAnimating;
        this.mQSPanel.setExpanded(z);
        this.mQSDetail.setExpanded(this.mQsExpanded);
        boolean zIsKeyguardShowing = isKeyguardShowing();
        this.mHeader.setVisibility((this.mQsExpanded || !zIsKeyguardShowing || this.mHeaderAnimating || this.mShowCollapsedOnKeyguard) ? 0 : 4);
        this.mHeader.setExpanded(!(!zIsKeyguardShowing || this.mHeaderAnimating || this.mShowCollapsedOnKeyguard) || (this.mQsExpanded && !this.mStackScrollerOverscrolling));
        this.mFooter.setVisibility((this.mQsDisabled || !(this.mQsExpanded || !zIsKeyguardShowing || this.mHeaderAnimating || this.mShowCollapsedOnKeyguard)) ? 4 : 0);
        QSFooter qSFooter = this.mFooter;
        if ((!zIsKeyguardShowing || this.mHeaderAnimating || this.mShowCollapsedOnKeyguard) && (!this.mQsExpanded || this.mStackScrollerOverscrolling)) {
            z2 = false;
        }
        qSFooter.setExpanded(z2);
        this.mQSPanel.setVisibility((this.mQsDisabled || !z3) ? 4 : 0);
    }

    private boolean isKeyguardShowing() {
        return this.mStatusBarStateController.getState() == 1;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setShowCollapsedOnKeyguard(boolean z) throws Resources.NotFoundException {
        if (z != this.mShowCollapsedOnKeyguard) {
            this.mShowCollapsedOnKeyguard = z;
            updateQsState();
            QSAnimator qSAnimator = this.mQSAnimator;
            if (qSAnimator != null) {
                qSAnimator.setShowCollapsedOnKeyguard(z);
            }
            if (z || !isKeyguardShowing()) {
                return;
            }
            setQsExpansion(this.mLastQSExpansion, 0.0f);
        }
    }

    public QSPanel getQsPanel() {
        return this.mQSPanel;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public boolean isShowingDetail() {
        return this.mQSPanel.isShowingCustomize() || this.mQSDetail.isShowingDetail();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setExpanded(boolean z) {
        this.mQsExpanded = z;
        this.mQSPanel.setListening(this.mListening, z);
        updateQsState();
    }

    private void setKeyguardShowing(boolean z) {
        this.mLastQSExpansion = -1.0f;
        QSAnimator qSAnimator = this.mQSAnimator;
        if (qSAnimator != null) {
            qSAnimator.setOnKeyguard(z);
        }
        this.mFooter.setKeyguardShowing(z);
        updateQsState();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setOverscrolling(boolean z) {
        this.mStackScrollerOverscrolling = z;
        updateQsState();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setListening(boolean z) throws Resources.NotFoundException {
        this.mListening = z;
        this.mQSContainerImplController.setListening(z);
        this.mHeader.setListening(z);
        this.mFooter.setListening(z);
        this.mQSPanel.setListening(this.mListening, this.mQsExpanded);
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setHeaderListening(boolean z) throws Resources.NotFoundException {
        this.mHeader.setListening(z);
        this.mFooter.setListening(z);
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setQsExpansion(float f, float f2) throws Resources.NotFoundException {
        this.mContainer.setExpansion(f);
        float f3 = f - 1.0f;
        boolean z = isKeyguardShowing() && !this.mShowCollapsedOnKeyguard;
        if (!this.mHeaderAnimating && !headerWillBeAnimating()) {
            getView().setTranslationY((z || this.mSecureExpandDisabled) ? this.mHeader.getHeight() * f3 : f2);
        }
        int height = getView().getHeight();
        this.mLastHeaderTranslation = f2;
        if (f == this.mLastQSExpansion && this.mLastKeyguardAndExpanded == z && this.mLastViewHeight == height) {
            return;
        }
        this.mLastQSExpansion = f;
        this.mLastKeyguardAndExpanded = z;
        this.mLastViewHeight = height;
        boolean z2 = f == 1.0f;
        boolean z3 = f == 0.0f;
        float bottom = f3 * ((this.mQSPanelScrollView.getBottom() - this.mHeader.getBottom()) + this.mHeader.getPaddingBottom());
        this.mHeader.setExpansion(z, f, bottom);
        this.mFooter.setExpansion(z ? 1.0f : f);
        this.mQSPanel.getQsTileRevealController().setExpansion(f);
        this.mQSPanel.getTileLayout().setExpansion(f);
        this.mQSPanelScrollView.setTranslationY(bottom);
        if (z3) {
            this.mQSPanelScrollView.setScrollY(0);
        }
        this.mQSDetail.setFullyExpanded(z2);
        if (!z2) {
            this.mQsBounds.top = (int) (-this.mQSPanelScrollView.getTranslationY());
            this.mQsBounds.right = this.mQSPanelScrollView.getWidth();
            this.mQsBounds.bottom = this.mQSPanelScrollView.getHeight();
        }
        updateQsBounds();
        QSAnimator qSAnimator = this.mQSAnimator;
        if (qSAnimator != null) {
            qSAnimator.setPosition(f);
        }
        updateMediaPositions();
    }

    private void updateQsBounds() {
        if (this.mLastQSExpansion == 1.0f) {
            this.mQsBounds.set(0, 0, this.mQSPanelScrollView.getWidth(), this.mQSPanelScrollView.getHeight());
        }
        this.mQSPanelScrollView.setClipBounds(this.mQsBounds);
    }

    private void updateMediaPositions() {
        if (Utils.useQsMediaPlayer(getContext())) {
            this.mContainer.getLocationOnScreen(this.mTmpLocation);
            float height = this.mTmpLocation[1] + this.mContainer.getHeight();
            pinToBottom((height - this.mQSPanelScrollView.getScrollY()) + this.mQSPanelScrollView.getScrollRange(), this.mQSPanel.getMediaHost(), true);
            pinToBottom(height, this.mHeader.getHeaderQsPanel().getMediaHost(), false);
        }
    }

    private void pinToBottom(float f, MediaHost mediaHost, boolean z) {
        float fMax;
        UniqueObjectHostView hostView = mediaHost.getHostView();
        if (this.mLastQSExpansion > 0.0f) {
            float totalBottomMargin = ((f - getTotalBottomMargin(hostView)) - hostView.getHeight()) - (mediaHost.getCurrentBounds().top - hostView.getTranslationY());
            if (z) {
                fMax = Math.min(totalBottomMargin, 0.0f);
            } else {
                fMax = Math.max(totalBottomMargin, 0.0f);
            }
            hostView.setTranslationY(fMax);
            return;
        }
        hostView.setTranslationY(0.0f);
    }

    private float getTotalBottomMargin(View view) {
        View view2 = (View) view.getParent();
        int height = 0;
        while (true) {
            View view3 = view;
            view = view2;
            if ((view instanceof QSContainerImpl) || view == null) {
                break;
            }
            height += view.getHeight() - view3.getBottom();
            view2 = (View) view.getParent();
        }
        return height;
    }

    private boolean headerWillBeAnimating() {
        return this.mState == 1 && this.mShowCollapsedOnKeyguard && !isKeyguardShowing();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void animateHeaderSlidingIn(long j) {
        if (this.mSecureExpandDisabled || this.mQsExpanded || getView().getTranslationY() == 0.0f) {
            return;
        }
        this.mHeaderAnimating = true;
        this.mDelay = j;
        getView().getViewTreeObserver().addOnPreDrawListener(this.mStartHeaderSlidingIn);
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void animateHeaderSlidingOut() {
        if (getView().getY() == (-this.mHeader.getHeight())) {
            return;
        }
        this.mHeaderAnimating = true;
        getView().animate().y(-this.mHeader.getHeight()).setStartDelay(0L).setDuration(360L).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.qs.QSFragment.1
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (QSFragment.this.getView() != null) {
                    QSFragment.this.getView().animate().setListener(null);
                }
                QSFragment.this.mHeaderAnimating = false;
                QSFragment.this.updateQsState();
            }
        }).start();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setExpandClickListener(View.OnClickListener onClickListener) {
        this.mFooter.setExpandClickListener(onClickListener);
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void closeDetail() {
        this.mQSPanel.closeDetail();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void notifyCustomizeChanged() {
        this.mContainer.updateExpansion();
        this.mQSPanelScrollView.setVisibility(!this.mQSCustomizer.isCustomizing() ? 0 : 4);
        this.mFooter.setVisibility(this.mQSCustomizer.isCustomizing() ? 4 : 0);
        this.mPanelView.onQsHeightChanged();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public int getDesiredHeight() {
        if (this.mQSCustomizer.isCustomizing()) {
            return getView().getHeight();
        }
        if (this.mQSDetail.isClosingDetail()) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mQSPanelScrollView.getLayoutParams();
            return layoutParams.topMargin + layoutParams.bottomMargin + this.mQSPanelScrollView.getMeasuredHeight() + getView().getPaddingBottom();
        }
        return getView().getMeasuredHeight();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setHeightOverride(int i) {
        this.mContainer.setHeightOverride(i);
    }

    @Override // com.android.systemui.plugins.qs.QS
    public int getQsMinExpansionHeight() {
        if (this.mSecureExpandDisabled) {
            return 0;
        }
        return this.mHeader.getHeight();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setSecureExpandDisabled(boolean z) {
        this.mSecureExpandDisabled = z;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void hideImmediately() {
        getView().animate().cancel();
        getView().setY(-this.mHeader.getHeight());
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
        this.mState = i;
        setKeyguardShowing(i == 1);
    }
}
