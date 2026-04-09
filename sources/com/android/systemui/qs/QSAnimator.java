package com.android.systemui.qs;

import android.util.Log;
import android.view.View;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.qs.PagedTileLayout;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.TouchAnimator;
import com.android.systemui.tuner.TunerService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/* loaded from: classes.dex */
public class QSAnimator implements QSHost.Callback, PagedTileLayout.PageListener, TouchAnimator.Listener, View.OnLayoutChangeListener, View.OnAttachStateChangeListener, TunerService.Tunable {
    private TouchAnimator mAllPagesDelayedAnimator;
    private boolean mAllowFancy;
    private TouchAnimator mBrightnessAnimator;
    private TouchAnimator mFirstPageAnimator;
    private TouchAnimator mFirstPageDelayedAnimator;
    private boolean mFullRows;
    private QSTileHost mHost;
    private boolean mIsQuickQsBrightnessEnabled;
    private float mLastPosition;
    private TouchAnimator mNonfirstPageAnimator;
    private TouchAnimator mNonfirstPageDelayedAnimator;
    private int mNumQuickTiles;
    private boolean mOnKeyguard;
    private PagedTileLayout mPagedLayout;
    private final QS mQs;
    private final QSPanel mQsPanel;
    private final QuickQSPanel mQuickQsPanel;
    private boolean mShowCollapsedOnKeyguard;
    private TouchAnimator mTranslationXAnimator;
    private TouchAnimator mTranslationYAnimator;
    private final ArrayList<View> mAllViews = new ArrayList<>();
    private final ArrayList<View> mQuickQsViews = new ArrayList<>();
    private boolean mOnFirstPage = true;
    private boolean mNeedsAnimatorUpdate = false;
    private final TouchAnimator.Listener mNonFirstPageListener = new TouchAnimator.ListenerAdapter() { // from class: com.android.systemui.qs.QSAnimator.1
        @Override // com.android.systemui.qs.TouchAnimator.Listener
        public void onAnimationAtEnd() {
            QSAnimator.this.mQuickQsPanel.setVisibility(4);
        }

        @Override // com.android.systemui.qs.TouchAnimator.Listener
        public void onAnimationStarted() {
            QSAnimator.this.mQuickQsPanel.setVisibility(0);
        }
    };
    private Runnable mUpdateAnimators = new Runnable() { // from class: com.android.systemui.qs.QSAnimator.2
        @Override // java.lang.Runnable
        public void run() {
            QSAnimator.this.updateAnimators();
            QSAnimator.this.setCurrentPosition();
        }
    };

    public QSAnimator(QS qs, QuickQSPanel quickQSPanel, QSPanel qSPanel) {
        this.mQs = qs;
        this.mQuickQsPanel = quickQSPanel;
        this.mQsPanel = qSPanel;
        qSPanel.addOnAttachStateChangeListener(this);
        qs.getView().addOnLayoutChangeListener(this);
        if (qSPanel.isAttachedToWindow()) {
            onViewAttachedToWindow(null);
        }
        QSPanel.QSTileLayout tileLayout = qSPanel.getTileLayout();
        if (tileLayout instanceof PagedTileLayout) {
            this.mPagedLayout = (PagedTileLayout) tileLayout;
        } else {
            Log.w("QSAnimator", "QS Not using page layout");
        }
        qSPanel.setPageListener(this);
    }

    public void onRtlChanged() {
        updateAnimators();
    }

    public void onQsScrollingChanged() {
        this.mNeedsAnimatorUpdate = true;
    }

    public void setOnKeyguard(boolean z) {
        this.mOnKeyguard = z;
        updateQQSVisibility();
        if (this.mOnKeyguard) {
            clearAnimationState();
        }
    }

    void setShowCollapsedOnKeyguard(boolean z) {
        this.mShowCollapsedOnKeyguard = z;
        updateQQSVisibility();
        setCurrentPosition();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setCurrentPosition() {
        setPosition(this.mLastPosition);
    }

    private void updateQQSVisibility() {
        this.mQuickQsPanel.setVisibility((!this.mOnKeyguard || this.mShowCollapsedOnKeyguard) ? 0 : 4);
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
        qSTileHost.addCallback(this);
        updateAnimators();
    }

    @Override // android.view.View.OnAttachStateChangeListener
    public void onViewAttachedToWindow(View view) {
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "sysui_qs_fancy_anim", "sysui_qs_move_whole_rows", "sysui_qqs_count");
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "lineagesecure:qs_show_brightness_slider");
    }

    @Override // android.view.View.OnAttachStateChangeListener
    public void onViewDetachedFromWindow(View view) {
        QSTileHost qSTileHost = this.mHost;
        if (qSTileHost != null) {
            qSTileHost.removeCallback(this);
        }
        ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("sysui_qs_fancy_anim".equals(str)) {
            boolean integerSwitch = TunerService.parseIntegerSwitch(str2, true);
            this.mAllowFancy = integerSwitch;
            if (!integerSwitch) {
                clearAnimationState();
            }
        } else if ("sysui_qs_move_whole_rows".equals(str)) {
            this.mFullRows = TunerService.parseIntegerSwitch(str2, true);
        } else if ("sysui_qqs_count".equals(str)) {
            this.mNumQuickTiles = QuickQSPanel.parseNumTiles(str2);
            clearAnimationState();
        } else if ("lineagesecure:qs_show_brightness_slider".equals(str)) {
            this.mIsQuickQsBrightnessEnabled = TunerService.parseInteger(str2, 0) > 1;
        }
        updateAnimators();
    }

    @Override // com.android.systemui.qs.PagedTileLayout.PageListener
    public void onPageChanged(boolean z) {
        if (this.mOnFirstPage == z) {
            return;
        }
        if (!z) {
            clearAnimationState();
        }
        this.mOnFirstPage = z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public void updateAnimators() {
        QSPanel.QSTileLayout qSTileLayout;
        int i;
        float f;
        Collection<QSTile> collection;
        int i2;
        float f2;
        int i3;
        QSPanel.QSTileLayout qSTileLayout2;
        int[] iArr;
        this.mNeedsAnimatorUpdate = false;
        TouchAnimator.Builder builder = new TouchAnimator.Builder();
        TouchAnimator.Builder builder2 = new TouchAnimator.Builder();
        TouchAnimator.Builder builder3 = new TouchAnimator.Builder();
        if (this.mQsPanel.getHost() == null) {
            return;
        }
        Collection<QSTile> tiles = this.mQsPanel.getHost().getTiles();
        int[] iArr2 = new int[2];
        int[] iArr3 = new int[2];
        clearAnimationState();
        this.mAllViews.clear();
        this.mQuickQsViews.clear();
        QSPanel.QSTileLayout tileLayout = this.mQsPanel.getTileLayout();
        this.mAllViews.add((View) tileLayout);
        int measuredHeight = this.mQs.getView() != null ? this.mQs.getView().getMeasuredHeight() : 0;
        int measuredWidth = this.mQs.getView() != null ? this.mQs.getView().getMeasuredWidth() : 0;
        int bottom = (measuredHeight - this.mQs.getHeader().getBottom()) + this.mQs.getHeader().getPaddingBottom();
        float f3 = bottom;
        builder.addFloat(tileLayout, "translationY", f3, 0.0f);
        Iterator<QSTile> it = tiles.iterator();
        int i4 = 0;
        int i5 = 0;
        QSPanel.QSTileLayout qSTileLayout3 = tileLayout;
        while (it.hasNext()) {
            QSTile next = it.next();
            Iterator<QSTile> it2 = it;
            QSTileView tileView = this.mQsPanel.getTileView(next);
            if (tileView == null) {
                Log.e("QSAnimator", "tileView is null " + next.getTileSpec());
                collection = tiles;
                i2 = bottom;
                i3 = measuredWidth;
                f2 = f3;
            } else {
                collection = tiles;
                View iconView = tileView.getIcon().getIconView();
                i2 = bottom;
                View view = this.mQs.getView();
                f2 = f3;
                i3 = measuredWidth;
                if (i4 < this.mQuickQsPanel.getTileLayout().getNumVisibleTiles() && this.mAllowFancy) {
                    QSTileView tileView2 = this.mQuickQsPanel.getTileView(next);
                    if (tileView2 != null) {
                        int i6 = iArr2[0];
                        getRelativePosition(iArr2, tileView2.getIcon().getIconView(), view);
                        getRelativePosition(iArr3, iconView, view);
                        int i7 = iArr3[0] - iArr2[0];
                        int i8 = iArr3[1] - iArr2[1];
                        i5 = iArr2[0] - i6;
                        if (i4 < qSTileLayout3.getNumVisibleTiles()) {
                            builder2.addFloat(tileView2, "translationX", 0.0f, i7);
                            builder3.addFloat(tileView2, "translationY", 0.0f, i8);
                            builder2.addFloat(tileView, "translationX", -i7, 0.0f);
                            builder3.addFloat(tileView, "translationY", -i8, 0.0f);
                            qSTileLayout2 = qSTileLayout3;
                        } else {
                            qSTileLayout2 = qSTileLayout3;
                            builder.addFloat(tileView2, "alpha", 1.0f, 0.0f);
                            builder3.addFloat(tileView2, "translationY", 0.0f, i8);
                            builder2.addFloat(tileView2, "translationX", 0.0f, this.mQsPanel.isLayoutRtl() ? i7 - i3 : i7 + i3);
                        }
                        this.mQuickQsViews.add(tileView.getIconWithBackground());
                        this.mAllViews.add(tileView.getIcon());
                        this.mAllViews.add(tileView2);
                        iArr = iArr2;
                    }
                } else {
                    qSTileLayout2 = qSTileLayout3;
                    if (this.mFullRows && isIconInAnimatedRow(i4)) {
                        iArr2[0] = iArr2[0] + i5;
                        getRelativePosition(iArr3, iconView, view);
                        int i9 = iArr3[0] - iArr2[0];
                        int i10 = iArr3[1] - iArr2[1];
                        iArr = iArr2;
                        builder.addFloat(tileView, "translationY", f2, 0.0f);
                        builder2.addFloat(tileView, "translationX", -i9, 0.0f);
                        float f4 = -i10;
                        builder3.addFloat(tileView, "translationY", f4, 0.0f);
                        builder3.addFloat(iconView, "translationY", f4, 0.0f);
                        this.mAllViews.add(iconView);
                    } else {
                        iArr = iArr2;
                        builder.addFloat(tileView, "alpha", 0.0f, 1.0f);
                        bottom = i2;
                        builder.addFloat(tileView, "translationY", -bottom, 0.0f);
                        this.mAllViews.add(tileView);
                        i4++;
                        it = it2;
                        tiles = collection;
                        f3 = f2;
                        measuredWidth = i3;
                        iArr2 = iArr;
                        qSTileLayout3 = qSTileLayout2;
                        qSTileLayout3 = qSTileLayout3;
                    }
                }
                bottom = i2;
                this.mAllViews.add(tileView);
                i4++;
                it = it2;
                tiles = collection;
                f3 = f2;
                measuredWidth = i3;
                iArr2 = iArr;
                qSTileLayout3 = qSTileLayout2;
                qSTileLayout3 = qSTileLayout3;
            }
            it = it2;
            bottom = i2;
            tiles = collection;
            f3 = f2;
            measuredWidth = i3;
            qSTileLayout3 = qSTileLayout3;
        }
        Collection<QSTile> collection2 = tiles;
        QSPanel.QSTileLayout qSTileLayout4 = qSTileLayout3;
        float f5 = f3;
        View brightnessView = this.mQsPanel.getBrightnessView();
        if (this.mAllowFancy) {
            if (brightnessView != null && !this.mQsPanel.isBrightnessViewBottom()) {
                builder.addFloat(brightnessView, "translationY", f5, 0.0f);
                TouchAnimator.Builder builder4 = new TouchAnimator.Builder();
                float[] fArr = new float[2];
                fArr[0] = this.mIsQuickQsBrightnessEnabled ? 1.0f : 0.0f;
                fArr[1] = 1.0f;
                this.mBrightnessAnimator = builder4.addFloat(brightnessView, "alpha", fArr).setStartDelay(0.5f).build();
                this.mAllViews.add(brightnessView);
            } else {
                this.mBrightnessAnimator = null;
            }
            this.mFirstPageAnimator = builder.setListener(this).build();
            qSTileLayout = qSTileLayout4;
            TouchAnimator.Builder builderAddFloat = new TouchAnimator.Builder().setStartDelay(0.86f).addFloat(qSTileLayout, "alpha", 0.0f, 1.0f);
            if (brightnessView != null && this.mQsPanel.isBrightnessViewBottom()) {
                builderAddFloat.addFloat(brightnessView, "alpha", 0.0f, 1.0f);
            }
            this.mFirstPageDelayedAnimator = builderAddFloat.build();
            TouchAnimator.Builder startDelay = new TouchAnimator.Builder().setStartDelay(0.86f);
            if (this.mQsPanel.getSecurityFooter() != null) {
                i = 2;
                startDelay.addFloat(this.mQsPanel.getSecurityFooter().getView(), "alpha", 0.0f, 1.0f);
            } else {
                i = 2;
            }
            if (this.mQsPanel.getDivider() != null) {
                float[] fArr2 = new float[i];
                // fill-array-data instruction
                fArr2[0] = 0.0f;
                fArr2[1] = 1.0f;
                startDelay.addFloat(this.mQsPanel.getDivider(), "alpha", fArr2);
            }
            this.mAllPagesDelayedAnimator = startDelay.build();
            if (this.mQsPanel.getSecurityFooter() != null) {
                this.mAllViews.add(this.mQsPanel.getSecurityFooter().getView());
            }
            if (this.mQsPanel.getDivider() != null) {
                this.mAllViews.add(this.mQsPanel.getDivider());
            }
            if (collection2.size() <= 3) {
                f = 1.0f;
            } else {
                f = collection2.size() <= 6 ? 0.4f : 0.0f;
            }
            PathInterpolatorBuilder pathInterpolatorBuilder = new PathInterpolatorBuilder(0.0f, 0.0f, f, 1.0f);
            builder2.setInterpolator(pathInterpolatorBuilder.getXInterpolator());
            builder3.setInterpolator(pathInterpolatorBuilder.getYInterpolator());
            this.mTranslationXAnimator = builder2.build();
            this.mTranslationYAnimator = builder3.build();
        } else {
            qSTileLayout = qSTileLayout4;
        }
        TouchAnimator.Builder endDelay = new TouchAnimator.Builder().addFloat(this.mQuickQsPanel, "alpha", 1.0f, 0.0f).setListener(this.mNonFirstPageListener).setEndDelay(0.5f);
        if (brightnessView != null && this.mQsPanel.isBrightnessViewBottom()) {
            endDelay.addFloat(brightnessView, "alpha", 0.0f, 1.0f);
        }
        this.mNonfirstPageAnimator = endDelay.build();
        this.mNonfirstPageDelayedAnimator = new TouchAnimator.Builder().setStartDelay(0.14f).addFloat(qSTileLayout, "alpha", 0.0f, 1.0f).build();
    }

    private boolean isIconInAnimatedRow(int i) {
        PagedTileLayout pagedTileLayout = this.mPagedLayout;
        if (pagedTileLayout == null) {
            return false;
        }
        int columnCount = pagedTileLayout.getColumnCount();
        return i < (((this.mNumQuickTiles + columnCount) - 1) / columnCount) * columnCount;
    }

    private void getRelativePosition(int[] iArr, View view, View view2) {
        iArr[0] = (view.getWidth() / 2) + 0;
        iArr[1] = 0;
        getRelativePositionInt(iArr, view, view2);
    }

    private void getRelativePositionInt(int[] iArr, View view, View view2) {
        if (view == view2 || view == null) {
            return;
        }
        if (!(view instanceof PagedTileLayout.TilePage)) {
            iArr[0] = iArr[0] + view.getLeft();
            iArr[1] = iArr[1] + view.getTop();
        }
        if (!(view instanceof PagedTileLayout)) {
            iArr[0] = iArr[0] - view.getScrollX();
            iArr[1] = iArr[1] - view.getScrollY();
        }
        getRelativePositionInt(iArr, (View) view.getParent(), view2);
    }

    public void setPosition(float f) {
        if (this.mNeedsAnimatorUpdate) {
            updateAnimators();
        }
        if (this.mFirstPageAnimator == null) {
            return;
        }
        if (this.mOnKeyguard) {
            f = this.mShowCollapsedOnKeyguard ? 0.0f : 1.0f;
        }
        this.mLastPosition = f;
        if (this.mOnFirstPage && this.mAllowFancy) {
            this.mQuickQsPanel.setAlpha(1.0f);
            this.mFirstPageAnimator.setPosition(f);
            this.mFirstPageDelayedAnimator.setPosition(f);
            this.mTranslationXAnimator.setPosition(f);
            this.mTranslationYAnimator.setPosition(f);
            TouchAnimator touchAnimator = this.mBrightnessAnimator;
            if (touchAnimator != null) {
                touchAnimator.setPosition(f);
            }
        } else {
            this.mNonfirstPageAnimator.setPosition(f);
            this.mNonfirstPageDelayedAnimator.setPosition(f);
        }
        if (this.mAllowFancy) {
            this.mAllPagesDelayedAnimator.setPosition(f);
        }
    }

    @Override // com.android.systemui.qs.TouchAnimator.Listener
    public void onAnimationAtStart() {
        this.mQuickQsPanel.setVisibility(0);
    }

    @Override // com.android.systemui.qs.TouchAnimator.Listener
    public void onAnimationAtEnd() {
        this.mQuickQsPanel.setVisibility(4);
        int size = this.mQuickQsViews.size();
        for (int i = 0; i < size; i++) {
            this.mQuickQsViews.get(i).setVisibility(0);
        }
    }

    @Override // com.android.systemui.qs.TouchAnimator.Listener
    public void onAnimationStarted() {
        updateQQSVisibility();
        if (this.mOnFirstPage) {
            int size = this.mQuickQsViews.size();
            for (int i = 0; i < size; i++) {
                this.mQuickQsViews.get(i).setVisibility(4);
            }
        }
    }

    private void clearAnimationState() {
        int size = this.mAllViews.size();
        this.mQuickQsPanel.setAlpha(0.0f);
        for (int i = 0; i < size; i++) {
            View view = this.mAllViews.get(i);
            view.setAlpha(1.0f);
            view.setTranslationX(0.0f);
            view.setTranslationY(0.0f);
        }
        int size2 = this.mQuickQsViews.size();
        for (int i2 = 0; i2 < size2; i2++) {
            this.mQuickQsViews.get(i2).setVisibility(0);
        }
    }

    @Override // android.view.View.OnLayoutChangeListener
    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        this.mQsPanel.post(this.mUpdateAnimators);
    }

    @Override // com.android.systemui.qs.QSHost.Callback
    public void onTilesChanged() {
        this.mQsPanel.post(this.mUpdateAnimators);
    }
}
