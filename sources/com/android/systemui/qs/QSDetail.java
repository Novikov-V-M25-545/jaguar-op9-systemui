package com.android.systemui.qs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Animatable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.Dependency;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.R;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.tuner.TunerService;
import java.util.Objects;

/* loaded from: classes.dex */
public class QSDetail extends LinearLayout implements TunerService.Tunable {
    private boolean mAnimatingOpen;
    private QSDetailClipper mClipper;
    private boolean mClosingDetail;
    private DetailAdapter mDetailAdapter;
    private ViewGroup mDetailContent;
    protected TextView mDetailDoneButton;
    protected TextView mDetailSettingsButton;
    private final SparseArray<View> mDetailViews;
    private View mFooter;
    private boolean mFullyExpanded;
    private QuickStatusBarHeader mHeader;
    private boolean mHeaderImageEnabled;
    private final AnimatorListenerAdapter mHideGridContentWhenDone;
    protected QSTileHost mHost;
    private int mOpenX;
    private int mOpenY;
    protected View mQsDetailHeader;
    protected ImageView mQsDetailHeaderProgress;
    private Switch mQsDetailHeaderSwitch;
    private ViewStub mQsDetailHeaderSwitchStub;
    protected TextView mQsDetailHeaderTitle;
    protected View mQsDetailTopSpace;
    private QSPanel mQsPanel;
    protected Callback mQsPanelCallback;
    private boolean mScanState;
    private boolean mSwitchState;
    private final AnimatorListenerAdapter mTeardownDetailWhenDone;
    private boolean mTriggeredExpand;
    private final UiEventLogger mUiEventLogger;

    public interface Callback {
        void onScanStateChanged(boolean z);

        void onShowingDetail(DetailAdapter detailAdapter, int i, int i2);

        void onToggleStateChanged(boolean z);
    }

    public QSDetail(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mDetailViews = new SparseArray<>();
        this.mUiEventLogger = QSEvents.INSTANCE.getQsUiEventsLogger();
        this.mQsPanelCallback = new Callback() { // from class: com.android.systemui.qs.QSDetail.3
            @Override // com.android.systemui.qs.QSDetail.Callback
            public void onToggleStateChanged(final boolean z) {
                QSDetail.this.post(new Runnable() { // from class: com.android.systemui.qs.QSDetail.3.1
                    @Override // java.lang.Runnable
                    public void run() {
                        QSDetail qSDetail = QSDetail.this;
                        qSDetail.handleToggleStateChanged(z, qSDetail.mDetailAdapter != null && QSDetail.this.mDetailAdapter.getToggleEnabled());
                    }
                });
            }

            @Override // com.android.systemui.qs.QSDetail.Callback
            public void onShowingDetail(final DetailAdapter detailAdapter, final int i, final int i2) {
                QSDetail.this.post(new Runnable() { // from class: com.android.systemui.qs.QSDetail.3.2
                    @Override // java.lang.Runnable
                    public void run() {
                        if (QSDetail.this.isAttachedToWindow()) {
                            QSDetail.this.handleShowingDetail(detailAdapter, i, i2, false);
                        }
                    }
                });
            }

            @Override // com.android.systemui.qs.QSDetail.Callback
            public void onScanStateChanged(final boolean z) {
                QSDetail.this.post(new Runnable() { // from class: com.android.systemui.qs.QSDetail.3.3
                    @Override // java.lang.Runnable
                    public void run() {
                        QSDetail.this.handleScanStateChanged(z);
                    }
                });
            }
        };
        this.mHideGridContentWhenDone = new AnimatorListenerAdapter() { // from class: com.android.systemui.qs.QSDetail.4
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                animator.removeListener(this);
                QSDetail.this.mAnimatingOpen = false;
                QSDetail.this.checkPendingAnimations();
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (QSDetail.this.mDetailAdapter != null) {
                    QSDetail.this.mQsPanel.setGridContentVisibility(false);
                    QSDetail.this.mFooter.setVisibility(4);
                }
                QSDetail.this.mAnimatingOpen = false;
                QSDetail.this.checkPendingAnimations();
            }
        };
        this.mTeardownDetailWhenDone = new AnimatorListenerAdapter() { // from class: com.android.systemui.qs.QSDetail.5
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                QSDetail.this.mDetailContent.removeAllViews();
                QSDetail.this.setVisibility(4);
                QSDetail.this.mClosingDetail = false;
            }
        };
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        TextView textView = this.mDetailDoneButton;
        int i = R.dimen.qs_detail_button_text_size;
        FontSizeUtils.updateFontSize(textView, i);
        FontSizeUtils.updateFontSize(this.mDetailSettingsButton, i);
        for (int i2 = 0; i2 < this.mDetailViews.size(); i2++) {
            this.mDetailViews.valueAt(i2).dispatchConfigurationChanged(configuration);
        }
        updateResources();
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mDetailContent = (ViewGroup) findViewById(android.R.id.content);
        this.mDetailSettingsButton = (TextView) findViewById(android.R.id.button2);
        this.mDetailDoneButton = (TextView) findViewById(android.R.id.button1);
        View viewFindViewById = findViewById(R.id.qs_detail_header);
        this.mQsDetailHeader = viewFindViewById;
        this.mQsDetailHeaderTitle = (TextView) viewFindViewById.findViewById(android.R.id.title);
        this.mQsDetailHeaderSwitchStub = (ViewStub) this.mQsDetailHeader.findViewById(R.id.toggle_stub);
        this.mQsDetailHeaderProgress = (ImageView) findViewById(R.id.qs_detail_header_progress);
        this.mQsDetailTopSpace = findViewById(R.id.qs_detail_top_space);
        updateDetailText();
        this.mClipper = new QSDetailClipper(findViewById(R.id.detail_container));
        this.mDetailDoneButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.qs.QSDetail.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                QSDetail qSDetail = QSDetail.this;
                qSDetail.announceForAccessibility(((LinearLayout) qSDetail).mContext.getString(R.string.accessibility_desc_quick_settings));
                QSDetail.this.mQsPanel.closeDetail();
            }
        });
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "system:status_bar_custom_header");
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        str.hashCode();
        if (str.equals("system:status_bar_custom_header")) {
            this.mHeaderImageEnabled = TunerService.parseIntegerSwitch(str2, false);
            updateResources();
        }
    }

    public void setQsPanel(QSPanel qSPanel, QuickStatusBarHeader quickStatusBarHeader, View view) {
        this.mQsPanel = qSPanel;
        this.mHeader = quickStatusBarHeader;
        this.mFooter = view;
        quickStatusBarHeader.setCallback(this.mQsPanelCallback);
        this.mQsPanel.setCallback(this.mQsPanelCallback);
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
    }

    public boolean isShowingDetail() {
        return this.mDetailAdapter != null;
    }

    public void setFullyExpanded(boolean z) {
        this.mFullyExpanded = z;
    }

    public void setExpanded(boolean z) {
        if (z) {
            return;
        }
        this.mTriggeredExpand = false;
    }

    private void updateDetailText() {
        this.mDetailDoneButton.setText(R.string.quick_settings_done);
        this.mDetailSettingsButton.setText(R.string.quick_settings_more_settings);
    }

    public void updateResources() {
        this.mQsDetailTopSpace.getLayoutParams().height = ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(android.R.dimen.message_progress_dialog_end_padding) + (this.mHeaderImageEnabled ? ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(R.dimen.qs_header_image_offset) : 0);
        View view = this.mQsDetailTopSpace;
        view.setLayoutParams(view.getLayoutParams());
        updateDetailText();
    }

    public boolean isClosingDetail() {
        return this.mClosingDetail;
    }

    public void handleShowingDetail(DetailAdapter detailAdapter, int i, int i2, boolean z) {
        AnimatorListenerAdapter animatorListenerAdapter;
        boolean z2 = detailAdapter != null;
        setClickable(z2);
        if (z2) {
            setupDetailHeader(detailAdapter);
            if (z && !this.mFullyExpanded) {
                this.mTriggeredExpand = true;
                ((CommandQueue) Dependency.get(CommandQueue.class)).animateExpandSettingsPanel(null);
            } else {
                this.mTriggeredExpand = false;
            }
            this.mOpenX = i;
            this.mOpenY = i2;
        } else {
            i = this.mOpenX;
            i2 = this.mOpenY;
            if (z && this.mTriggeredExpand) {
                ((CommandQueue) Dependency.get(CommandQueue.class)).animateCollapsePanels();
                this.mTriggeredExpand = false;
            }
        }
        DetailAdapter detailAdapter2 = this.mDetailAdapter;
        boolean z3 = (detailAdapter2 != null) != (detailAdapter != null);
        if (z3 || detailAdapter2 != detailAdapter) {
            if (detailAdapter != null) {
                int metricsCategory = detailAdapter.getMetricsCategory();
                View viewCreateDetailView = detailAdapter.createDetailView(((LinearLayout) this).mContext, this.mDetailViews.get(metricsCategory), this.mDetailContent);
                if (viewCreateDetailView == null) {
                    throw new IllegalStateException("Must return detail view");
                }
                setupDetailFooter(detailAdapter);
                this.mDetailContent.removeAllViews();
                this.mDetailContent.addView(viewCreateDetailView);
                this.mDetailViews.put(metricsCategory, viewCreateDetailView);
                ((MetricsLogger) Dependency.get(MetricsLogger.class)).visible(detailAdapter.getMetricsCategory());
                this.mUiEventLogger.log(detailAdapter.openDetailEvent());
                announceForAccessibility(((LinearLayout) this).mContext.getString(R.string.accessibility_quick_settings_detail, detailAdapter.getTitle()));
                this.mDetailAdapter = detailAdapter;
                animatorListenerAdapter = this.mHideGridContentWhenDone;
                setVisibility(0);
            } else {
                if (detailAdapter2 != null) {
                    ((MetricsLogger) Dependency.get(MetricsLogger.class)).hidden(this.mDetailAdapter.getMetricsCategory());
                    this.mUiEventLogger.log(this.mDetailAdapter.closeDetailEvent());
                }
                this.mClosingDetail = true;
                this.mDetailAdapter = null;
                animatorListenerAdapter = this.mTeardownDetailWhenDone;
                this.mHeader.setVisibility(0);
                this.mFooter.setVisibility(0);
                this.mQsPanel.setGridContentVisibility(true);
                this.mQsPanelCallback.onScanStateChanged(false);
            }
            sendAccessibilityEvent(32);
            animateDetailVisibleDiff(i, i2, z3, animatorListenerAdapter);
        }
    }

    protected void animateDetailVisibleDiff(int i, int i2, boolean z, Animator.AnimatorListener animatorListener) {
        if (z) {
            DetailAdapter detailAdapter = this.mDetailAdapter;
            this.mAnimatingOpen = detailAdapter != null;
            if (this.mFullyExpanded || detailAdapter != null) {
                setAlpha(1.0f);
                this.mClipper.animateCircularClip(i, i2, this.mDetailAdapter != null, animatorListener);
            } else {
                animate().alpha(0.0f).setDuration(300L).setListener(animatorListener).start();
            }
        }
    }

    protected void setupDetailFooter(final DetailAdapter detailAdapter) {
        final Intent settingsIntent = detailAdapter.getSettingsIntent();
        this.mDetailSettingsButton.setVisibility(settingsIntent != null ? 0 : 8);
        this.mDetailSettingsButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.qs.QSDetail$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$setupDetailFooter$0(detailAdapter, settingsIntent, view);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setupDetailFooter$0(DetailAdapter detailAdapter, Intent intent, View view) {
        ((MetricsLogger) Dependency.get(MetricsLogger.class)).action(929, detailAdapter.getMetricsCategory());
        this.mUiEventLogger.log(detailAdapter.moreSettingsEvent());
        ((ActivityStarter) Dependency.get(ActivityStarter.class)).postStartActivityDismissingKeyguard(intent, 0);
    }

    protected void setupDetailHeader(final DetailAdapter detailAdapter) {
        this.mQsDetailHeaderTitle.setText(detailAdapter.getTitle());
        Boolean toggleState = detailAdapter.getToggleState();
        if (toggleState == null) {
            Switch r4 = this.mQsDetailHeaderSwitch;
            if (r4 != null) {
                r4.setVisibility(4);
            }
            this.mQsDetailHeader.setClickable(false);
            return;
        }
        if (this.mQsDetailHeaderSwitch == null) {
            this.mQsDetailHeaderSwitch = (Switch) this.mQsDetailHeaderSwitchStub.inflate();
        }
        this.mQsDetailHeaderSwitch.setVisibility(0);
        handleToggleStateChanged(toggleState.booleanValue(), detailAdapter.getToggleEnabled());
        this.mQsDetailHeader.setClickable(true);
        this.mQsDetailHeader.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.qs.QSDetail.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                boolean z = !QSDetail.this.mQsDetailHeaderSwitch.isChecked();
                QSDetail.this.mQsDetailHeaderSwitch.setChecked(z);
                detailAdapter.setToggleState(z);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleToggleStateChanged(boolean z, boolean z2) {
        this.mSwitchState = z;
        if (this.mAnimatingOpen) {
            return;
        }
        Switch r0 = this.mQsDetailHeaderSwitch;
        if (r0 != null) {
            r0.setChecked(z);
        }
        this.mQsDetailHeader.setEnabled(z2);
        Switch r1 = this.mQsDetailHeaderSwitch;
        if (r1 != null) {
            r1.setEnabled(z2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleScanStateChanged(boolean z) {
        if (this.mScanState == z) {
            return;
        }
        this.mScanState = z;
        final Animatable animatable = (Animatable) this.mQsDetailHeaderProgress.getDrawable();
        if (z) {
            this.mQsDetailHeaderProgress.animate().cancel();
            ViewPropertyAnimator viewPropertyAnimatorAlpha = this.mQsDetailHeaderProgress.animate().alpha(1.0f);
            Objects.requireNonNull(animatable);
            viewPropertyAnimatorAlpha.withEndAction(new Runnable() { // from class: com.android.systemui.qs.QSDetail$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    animatable.start();
                }
            }).start();
            return;
        }
        this.mQsDetailHeaderProgress.animate().cancel();
        ViewPropertyAnimator viewPropertyAnimatorAlpha2 = this.mQsDetailHeaderProgress.animate().alpha(0.0f);
        Objects.requireNonNull(animatable);
        viewPropertyAnimatorAlpha2.withEndAction(new Runnable() { // from class: com.android.systemui.qs.QSDetail$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                animatable.stop();
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void checkPendingAnimations() {
        boolean z = this.mSwitchState;
        DetailAdapter detailAdapter = this.mDetailAdapter;
        handleToggleStateChanged(z, detailAdapter != null && detailAdapter.getToggleEnabled());
    }
}
