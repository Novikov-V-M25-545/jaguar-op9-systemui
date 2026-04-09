package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.crdroid.header.StatusBarHeaderMachine;
import com.android.systemui.qs.customize.QSCustomizer;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.animation.PhysicsAnimator;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public class QSContainerImpl extends FrameLayout implements StatusBarHeaderMachine.IStatusBarHeaderMachineObserver, TunerService.Tunable {
    private static final FloatPropertyCompat<QSContainerImpl> BACKGROUND_BOTTOM = new FloatPropertyCompat<QSContainerImpl>("backgroundBottom") { // from class: com.android.systemui.qs.QSContainerImpl.1
        @Override // androidx.dynamicanimation.animation.FloatPropertyCompat
        public float getValue(QSContainerImpl qSContainerImpl) {
            return qSContainerImpl.getBackgroundBottom();
        }

        @Override // androidx.dynamicanimation.animation.FloatPropertyCompat
        public void setValue(QSContainerImpl qSContainerImpl, float f) {
            qSContainerImpl.setBackgroundBottom((int) f);
        }
    };
    private static final PhysicsAnimator.SpringConfig BACKGROUND_SPRING = new PhysicsAnimator.SpringConfig(1500.0f, 0.75f);
    private boolean mAnimateBottomOnNextLayout;
    private View mBackground;
    private int mBackgroundBottom;
    private View mBackgroundGradient;
    private ImageView mBackgroundImage;
    private int mContentPaddingEnd;
    private int mContentPaddingStart;
    private Drawable mCurrentBackground;
    private QuickStatusBarHeader mHeader;
    private boolean mHeaderImageEnabled;
    private int mHeaderShadow;
    private int mHeightOverride;
    private boolean mLandscape;
    private QSCustomizer mQSCustomizer;
    private View mQSDetail;
    private QSPanel mQSPanel;
    private View mQSPanelContainer;
    private int mQsBackgroundAlpha;
    private boolean mQsDisabled;
    private float mQsExpansion;
    private int mQsSBBackgroundAlpha;
    private boolean mQsSBBackgroundGradient;
    private int mSideMargins;
    private final Point mSizePoint;
    private View mStatusBarBackground;
    private StatusBarHeaderMachine mStatusBarHeaderMachine;

    @Override // android.view.View
    public boolean performClick() {
        return true;
    }

    public QSContainerImpl(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mSizePoint = new Point();
        this.mBackgroundBottom = -1;
        this.mHeightOverride = -1;
        this.mContentPaddingStart = -1;
        this.mContentPaddingEnd = -1;
        this.mHeaderShadow = 0;
        this.mQsBackgroundAlpha = 255;
        this.mQsSBBackgroundGradient = true;
        this.mQsSBBackgroundAlpha = 255;
        this.mStatusBarHeaderMachine = new StatusBarHeaderMachine(context);
    }

    @Override // android.view.View
    protected void onFinishInflate() throws Resources.NotFoundException {
        super.onFinishInflate();
        this.mQSPanel = (QSPanel) findViewById(R.id.quick_settings_panel);
        this.mQSPanelContainer = findViewById(R.id.expanded_qs_scroll_view);
        this.mQSDetail = findViewById(R.id.qs_detail);
        this.mHeader = (QuickStatusBarHeader) findViewById(R.id.header);
        this.mQSCustomizer = (QSCustomizer) findViewById(R.id.qs_customize);
        this.mBackground = findViewById(R.id.quick_settings_background);
        this.mStatusBarBackground = findViewById(R.id.quick_settings_status_bar_background);
        this.mBackgroundGradient = findViewById(R.id.quick_settings_gradient_view);
        ImageView imageView = (ImageView) findViewById(R.id.qs_header_image_view);
        this.mBackgroundImage = imageView;
        imageView.setClipToOutline(true);
        updateResources();
        this.mHeader.getHeaderQsPanel().setMediaVisibilityChangedListener(new Consumer() { // from class: com.android.systemui.qs.QSContainerImpl$$ExternalSyntheticLambda1
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.lambda$onFinishInflate$0((Boolean) obj);
            }
        });
        this.mQSPanel.setMediaVisibilityChangedListener(new Consumer() { // from class: com.android.systemui.qs.QSContainerImpl$$ExternalSyntheticLambda0
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.lambda$onFinishInflate$1((Boolean) obj);
            }
        });
        setImportantForAccessibility(2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onFinishInflate$0(Boolean bool) {
        if (this.mHeader.getHeaderQsPanel().isShown()) {
            this.mAnimateBottomOnNextLayout = true;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onFinishInflate$1(Boolean bool) {
        if (this.mQSPanel.isShown()) {
            this.mAnimateBottomOnNextLayout = true;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setBackgroundBottom(int i) {
        this.mBackgroundBottom = i;
        this.mBackground.setBottom(i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public float getBackgroundBottom() {
        int i = this.mBackgroundBottom;
        return i == -1 ? this.mBackground.getBottom() : i;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        TunerService tunerService = (TunerService) Dependency.get(TunerService.class);
        tunerService.addTunable(this, "system:status_bar_custom_header_shadow");
        tunerService.addTunable(this, "system:qs_panel_bg_alpha");
        tunerService.addTunable(this, "system:qs_sb_bg_gradient");
        tunerService.addTunable(this, "system:qs_sb_bg_alpha");
        this.mStatusBarHeaderMachine.addObserver(this);
        this.mStatusBarHeaderMachine.updateEnablement();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mStatusBarHeaderMachine.removeObserver(this);
        ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) throws Resources.NotFoundException {
        super.onConfigurationChanged(configuration);
        this.mLandscape = configuration.orientation == 2;
        updateResources();
        this.mSizePoint.set(0, 0);
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        str.hashCode();
        switch (str) {
            case "system:qs_sb_bg_alpha":
                this.mQsSBBackgroundAlpha = TunerService.parseInteger(str2, 255);
                updateAlpha();
                break;
            case "system:qs_sb_bg_gradient":
                this.mQsSBBackgroundGradient = TunerService.parseIntegerSwitch(str2, true);
                updateStatusbarVisibility();
                break;
            case "system:qs_panel_bg_alpha":
                this.mQsBackgroundAlpha = TunerService.parseInteger(str2, 255);
                updateAlpha();
                break;
            case "system:status_bar_custom_header_shadow":
                this.mHeaderShadow = TunerService.parseInteger(str2, 0);
                applyHeaderBackgroundShadow();
                break;
        }
    }

    private void updateAlpha() {
        this.mBackground.getBackground().setAlpha(this.mQsBackgroundAlpha);
        this.mStatusBarBackground.getBackground().setAlpha(this.mQsSBBackgroundAlpha);
        this.mBackgroundGradient.getBackground().setAlpha(this.mQsSBBackgroundAlpha);
    }

    @Override // android.widget.FrameLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        Configuration configuration = getResources().getConfiguration();
        boolean z = configuration.smallestScreenWidthDp >= 600 || configuration.orientation != 2;
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mQSPanelContainer.getLayoutParams();
        int displayHeight = ((getDisplayHeight() - marginLayoutParams.topMargin) - marginLayoutParams.bottomMargin) - getPaddingBottom();
        if (z) {
            displayHeight -= getResources().getDimensionPixelSize(R.dimen.navigation_bar_height);
        }
        int i3 = ((FrameLayout) this).mPaddingLeft + ((FrameLayout) this).mPaddingRight + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin;
        this.mQSPanelContainer.measure(FrameLayout.getChildMeasureSpec(i, i3, marginLayoutParams.width), View.MeasureSpec.makeMeasureSpec(displayHeight, Integer.MIN_VALUE));
        super.onMeasure(View.MeasureSpec.makeMeasureSpec(this.mQSPanelContainer.getMeasuredWidth() + i3, 1073741824), View.MeasureSpec.makeMeasureSpec(marginLayoutParams.topMargin + marginLayoutParams.bottomMargin + this.mQSPanelContainer.getMeasuredHeight() + getPaddingBottom(), 1073741824));
        this.mQSCustomizer.measure(i, View.MeasureSpec.makeMeasureSpec(getDisplayHeight(), 1073741824));
    }

    @Override // android.view.ViewGroup
    protected void measureChildWithMargins(View view, int i, int i2, int i3, int i4) {
        if (view != this.mQSPanelContainer) {
            super.measureChildWithMargins(view, i, i2, i3, i4);
        }
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        updateExpansion(this.mAnimateBottomOnNextLayout);
        this.mAnimateBottomOnNextLayout = false;
    }

    public void disable(int i, int i2, boolean z) {
        boolean z2 = (i2 & 1) != 0;
        if (z2 == this.mQsDisabled) {
            return;
        }
        this.mQsDisabled = z2;
        this.mBackground.setVisibility(z2 ? 8 : 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateResources() throws Resources.NotFoundException {
        int dimensionPixelSize = ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(android.R.dimen.message_progress_dialog_end_padding) + (this.mHeaderImageEnabled ? ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(R.dimen.qs_header_image_offset) : 0);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mQSPanelContainer.getLayoutParams();
        layoutParams.topMargin = dimensionPixelSize;
        this.mQSPanelContainer.setLayoutParams(layoutParams);
        this.mSideMargins = getResources().getDimensionPixelSize(R.dimen.notification_side_paddings);
        this.mContentPaddingStart = getResources().getDimensionPixelSize(android.R.dimen.indeterminate_progress_alpha_60);
        int dimensionPixelSize2 = getResources().getDimensionPixelSize(android.R.dimen.indeterminate_progress_alpha_59);
        boolean z = dimensionPixelSize2 != this.mContentPaddingEnd;
        this.mContentPaddingEnd = dimensionPixelSize2;
        if (z) {
            updatePaddingsAndMargins();
        }
        int dimensionPixelSize3 = this.mHeaderImageEnabled ? ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(R.dimen.qs_header_image_side_margin) : 0;
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mStatusBarBackground.getLayoutParams();
        marginLayoutParams.height = dimensionPixelSize;
        marginLayoutParams.setMargins(dimensionPixelSize3, 0, dimensionPixelSize3, 0);
        this.mStatusBarBackground.setLayoutParams(marginLayoutParams);
        updateStatusbarVisibility();
    }

    public void setHeightOverride(int i) {
        this.mHeightOverride = i;
        updateExpansion();
    }

    public void updateExpansion() {
        updateExpansion(false);
    }

    public void updateExpansion(boolean z) {
        int iCalculateContainerHeight = calculateContainerHeight();
        setBottom(getTop() + iCalculateContainerHeight);
        this.mQSDetail.setBottom(getTop() + iCalculateContainerHeight);
        this.mBackground.setTop(this.mQSPanelContainer.getTop());
        updateBackgroundBottom(iCalculateContainerHeight, z);
    }

    private void updateBackgroundBottom(int i, boolean z) {
        PhysicsAnimator physicsAnimator = PhysicsAnimator.getInstance(this);
        FloatPropertyCompat<QSContainerImpl> floatPropertyCompat = BACKGROUND_BOTTOM;
        if (physicsAnimator.isPropertyAnimating(floatPropertyCompat) || z) {
            floatPropertyCompat.setValue(this, floatPropertyCompat.getValue(this));
            physicsAnimator.spring(floatPropertyCompat, i, BACKGROUND_SPRING).start();
        } else {
            floatPropertyCompat.setValue(this, i);
        }
    }

    protected int calculateContainerHeight() {
        int measuredHeight = this.mHeightOverride;
        if (measuredHeight == -1) {
            measuredHeight = getMeasuredHeight();
        }
        if (this.mQSCustomizer.isCustomizing()) {
            return this.mQSCustomizer.getHeight();
        }
        return this.mHeader.getHeight() + Math.round(this.mQsExpansion * (measuredHeight - this.mHeader.getHeight()));
    }

    public void setExpansion(float f) {
        this.mQsExpansion = f;
        updateExpansion();
    }

    private void updatePaddingsAndMargins() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt != this.mStatusBarBackground && childAt != this.mBackgroundGradient && childAt != this.mQSCustomizer) {
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) childAt.getLayoutParams();
                int i2 = this.mSideMargins;
                layoutParams.rightMargin = i2;
                layoutParams.leftMargin = i2;
                if (childAt == this.mQSPanelContainer) {
                    this.mQSPanel.setContentMargins(this.mContentPaddingStart, this.mContentPaddingEnd);
                } else {
                    QuickStatusBarHeader quickStatusBarHeader = this.mHeader;
                    if (childAt == quickStatusBarHeader) {
                        quickStatusBarHeader.setContentMargins(this.mContentPaddingStart, this.mContentPaddingEnd);
                    } else {
                        childAt.setPaddingRelative(this.mContentPaddingStart, childAt.getPaddingTop(), this.mContentPaddingEnd, childAt.getPaddingBottom());
                    }
                }
            }
        }
    }

    private int getDisplayHeight() {
        if (this.mSizePoint.y == 0) {
            getDisplay().getRealSize(this.mSizePoint);
        }
        return this.mSizePoint.y;
    }

    @Override // com.android.systemui.crdroid.header.StatusBarHeaderMachine.IStatusBarHeaderMachineObserver
    public void updateHeader(final Drawable drawable, final boolean z) {
        post(new Runnable() { // from class: com.android.systemui.qs.QSContainerImpl.2
            @Override // java.lang.Runnable
            public void run() throws Resources.NotFoundException {
                QSContainerImpl.this.doUpdateStatusBarCustomHeader(drawable, z);
            }
        });
    }

    @Override // com.android.systemui.crdroid.header.StatusBarHeaderMachine.IStatusBarHeaderMachineObserver
    public void disableHeader() {
        post(new Runnable() { // from class: com.android.systemui.qs.QSContainerImpl.3
            @Override // java.lang.Runnable
            public void run() throws Resources.NotFoundException {
                QSContainerImpl.this.mCurrentBackground = null;
                QSContainerImpl.this.mBackgroundImage.setVisibility(8);
                QSContainerImpl.this.mHeaderImageEnabled = false;
                QSContainerImpl.this.updateResources();
            }
        });
    }

    @Override // com.android.systemui.crdroid.header.StatusBarHeaderMachine.IStatusBarHeaderMachineObserver
    public void refreshHeader() {
        post(new Runnable() { // from class: com.android.systemui.qs.QSContainerImpl.4
            @Override // java.lang.Runnable
            public void run() throws Resources.NotFoundException {
                QSContainerImpl qSContainerImpl = QSContainerImpl.this;
                qSContainerImpl.doUpdateStatusBarCustomHeader(qSContainerImpl.mCurrentBackground, true);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void doUpdateStatusBarCustomHeader(Drawable drawable, boolean z) throws Resources.NotFoundException {
        if (drawable != null) {
            this.mBackgroundImage.setVisibility(0);
            this.mCurrentBackground = drawable;
            setNotificationPanelHeaderBackground(drawable, z);
            this.mHeaderImageEnabled = true;
        } else {
            this.mCurrentBackground = null;
            this.mBackgroundImage.setVisibility(8);
            this.mHeaderImageEnabled = false;
        }
        updateResources();
    }

    private void setNotificationPanelHeaderBackground(Drawable drawable, boolean z) {
        if (this.mBackgroundImage.getDrawable() != null && !z) {
            TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[]{this.mBackgroundImage.getDrawable(), drawable});
            transitionDrawable.setCrossFadeEnabled(true);
            this.mBackgroundImage.setImageDrawable(transitionDrawable);
            transitionDrawable.startTransition(1000);
        } else {
            this.mBackgroundImage.setImageDrawable(drawable);
        }
        applyHeaderBackgroundShadow();
    }

    private void applyHeaderBackgroundShadow() {
        if (this.mCurrentBackground == null || this.mBackgroundImage.getDrawable() == null) {
            return;
        }
        this.mBackgroundImage.setImageAlpha(255 - this.mHeaderShadow);
    }

    private void updateStatusbarVisibility() {
        boolean z = this.mLandscape;
        boolean z2 = z || this.mHeaderImageEnabled;
        boolean z3 = z && !this.mHeaderImageEnabled;
        this.mBackgroundGradient.setVisibility((z2 || !this.mQsSBBackgroundGradient) ? 4 : 0);
        this.mStatusBarBackground.setBackgroundColor(z2 ? 0 : -16777216);
        this.mStatusBarBackground.setVisibility(z3 ? 4 : 0);
        updateAlpha();
        applyHeaderBackgroundShadow();
    }
}
