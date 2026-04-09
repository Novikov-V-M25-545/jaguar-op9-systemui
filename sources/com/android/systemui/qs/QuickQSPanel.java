package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.media.MediaHost;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.customize.QSCustomizer;
import com.android.systemui.qs.logging.QSLogger;
import com.android.systemui.tuner.TunerService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/* loaded from: classes.dex */
public class QuickQSPanel extends QSPanel {
    private static int sDefaultMaxTiles = 6;
    private boolean mDisabledByPolicy;
    protected QSPanel mFullPanel;
    private int mMaxTiles;
    private final TunerService.Tunable mNumTiles;

    @Override // com.android.systemui.qs.QSPanel
    protected void addSecurityFooter() {
    }

    @Override // com.android.systemui.qs.QSPanel
    protected void addViewsAboveTiles() {
    }

    @Override // com.android.systemui.qs.QSPanel
    protected boolean displayMediaMarginsOnMedia() {
        return false;
    }

    @Override // com.android.systemui.qs.QSPanel
    protected String getDumpableTag() {
        return "QuickQSPanel";
    }

    @Override // com.android.systemui.qs.QSPanel
    protected boolean needsDynamicRowsAndColumns() {
        return false;
    }

    @Override // com.android.systemui.qs.QSPanel
    protected void updatePadding() {
    }

    public QuickQSPanel(Context context, AttributeSet attributeSet, DumpManager dumpManager, BroadcastDispatcher broadcastDispatcher, QSLogger qSLogger, MediaHost mediaHost, UiEventLogger uiEventLogger) throws Resources.NotFoundException {
        super(context, attributeSet, dumpManager, broadcastDispatcher, qSLogger, mediaHost, uiEventLogger);
        this.mNumTiles = new TunerService.Tunable() { // from class: com.android.systemui.qs.QuickQSPanel.1
            @Override // com.android.systemui.tuner.TunerService.Tunable
            public void onTuningChanged(String str, String str2) {
                QuickQSPanel.this.setMaxTiles(QuickQSPanel.parseNumTiles(str2));
            }
        };
        sDefaultMaxTiles = getResources().getInteger(R.integer.quick_qs_panel_max_columns);
        applyBottomMargin((View) this.mRegularTileLayout);
    }

    private void applyBottomMargin(View view) throws Resources.NotFoundException {
        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.qs_header_tile_margin_bottom);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        marginLayoutParams.bottomMargin = dimensionPixelSize;
        view.setLayoutParams(marginLayoutParams);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSPanel
    public TileLayout createRegularTileLayout() {
        return new HeaderTileLayout(this.mContext, this.mUiEventLogger);
    }

    @Override // com.android.systemui.qs.QSPanel
    protected QSPanel.QSTileLayout createHorizontalTileLayout() {
        return new DoubleLineTileLayout(this.mContext, this.mUiEventLogger);
    }

    @Override // com.android.systemui.qs.QSPanel
    protected void initMediaHostState() {
        this.mMediaHost.setExpansion(0.0f);
        this.mMediaHost.setShowsOnlyActiveMedia(true);
        this.mMediaHost.init(1);
    }

    @Override // com.android.systemui.qs.QSPanel, android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this.mNumTiles, "sysui_qqs_count");
    }

    @Override // com.android.systemui.qs.QSPanel, android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((TunerService) Dependency.get(TunerService.class)).removeTunable(this.mNumTiles);
    }

    public void setQSPanelAndHeader(QSPanel qSPanel, View view) {
        this.mFullPanel = qSPanel;
    }

    @Override // com.android.systemui.qs.QSPanel
    protected boolean shouldShowDetail() {
        return !this.mExpanded;
    }

    @Override // com.android.systemui.qs.QSPanel
    protected void drawTile(QSPanel.TileRecord tileRecord, QSTile.State state) {
        if (state instanceof QSTile.SignalState) {
            QSTile.SignalState signalState = new QSTile.SignalState();
            state.copyTo(signalState);
            signalState.activityIn = false;
            signalState.activityOut = false;
            state = signalState;
        }
        super.drawTile(tileRecord, state);
    }

    @Override // com.android.systemui.qs.QSPanel
    public void setHost(QSTileHost qSTileHost, QSCustomizer qSCustomizer) {
        super.setHost(qSTileHost, qSCustomizer);
        setTiles(this.mHost.getTiles());
    }

    public void setMaxTiles(int i) {
        this.mMaxTiles = i;
        QSTileHost qSTileHost = this.mHost;
        if (qSTileHost != null) {
            setTiles(qSTileHost.getTiles());
        }
    }

    @Override // com.android.systemui.qs.QSPanel, com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("lineagesecure:qs_show_brightness_slider".equals(str)) {
            super.onTuningChanged(str, "0");
        }
    }

    @Override // com.android.systemui.qs.QSPanel
    public void setTiles(Collection<QSTile> collection) {
        ArrayList arrayList = new ArrayList();
        Iterator<QSTile> it = collection.iterator();
        while (it.hasNext()) {
            arrayList.add(it.next());
            if (arrayList.size() == this.mMaxTiles) {
                break;
            }
        }
        super.setTiles(arrayList, true);
    }

    public int getNumQuickTiles() {
        return this.mMaxTiles;
    }

    public static int parseNumTiles(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException unused) {
            return sDefaultMaxTiles;
        }
    }

    public static int getDefaultMaxTiles() {
        return sDefaultMaxTiles;
    }

    void setDisabledByPolicy(boolean z) {
        if (z != this.mDisabledByPolicy) {
            this.mDisabledByPolicy = z;
            setVisibility(z ? 8 : 0);
        }
    }

    @Override // android.view.View
    public void setVisibility(int i) {
        if (this.mDisabledByPolicy) {
            if (getVisibility() == 8) {
                return;
            } else {
                i = 8;
            }
        }
        super.setVisibility(i);
    }

    @Override // com.android.systemui.qs.QSPanel
    protected QSEvent openPanelEvent() {
        return QSEvent.QQS_PANEL_EXPANDED;
    }

    @Override // com.android.systemui.qs.QSPanel
    protected QSEvent closePanelEvent() {
        return QSEvent.QQS_PANEL_COLLAPSED;
    }

    private static class HeaderTileLayout extends TileLayout {
        private Rect mClippingBounds;
        private final UiEventLogger mUiEventLogger;

        public HeaderTileLayout(Context context, UiEventLogger uiEventLogger) {
            super(context);
            this.mClippingBounds = new Rect();
            this.mUiEventLogger = uiEventLogger;
            setClipChildren(false);
            setClipToPadding(false);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
            layoutParams.gravity = 1;
            setLayoutParams(layoutParams);
        }

        @Override // android.view.View
        protected void onConfigurationChanged(Configuration configuration) throws Resources.NotFoundException {
            super.onConfigurationChanged(configuration);
            updateResources();
        }

        @Override // android.view.View
        public void onFinishInflate() throws Resources.NotFoundException {
            super.onFinishInflate();
            updateResources();
        }

        private ViewGroup.LayoutParams generateTileLayoutParams() {
            return new ViewGroup.LayoutParams(this.mCellWidth, this.mCellHeight);
        }

        @Override // com.android.systemui.qs.TileLayout
        protected void addTileView(QSPanel.TileRecord tileRecord) {
            addView(tileRecord.tileView, getChildCount(), generateTileLayoutParams());
        }

        @Override // com.android.systemui.qs.TileLayout, android.view.ViewGroup, android.view.View
        protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
            this.mClippingBounds.set(0, 0, i3 - i, 10000);
            setClipBounds(this.mClippingBounds);
            calculateColumns();
            int i5 = 0;
            while (i5 < this.mRecords.size()) {
                this.mRecords.get(i5).tileView.setVisibility(i5 < this.mColumns ? 0 : 8);
                i5++;
            }
            setAccessibilityOrder();
            layoutTileRecords(this.mColumns);
        }

        @Override // com.android.systemui.qs.TileLayout, com.android.systemui.qs.QSPanel.QSTileLayout
        public boolean updateResources() throws Resources.NotFoundException {
            int dimensionPixelSize = ((ViewGroup) this).mContext.getResources().getDimensionPixelSize(R.dimen.qs_quick_tile_size);
            this.mCellWidth = dimensionPixelSize;
            this.mCellHeight = dimensionPixelSize;
            return false;
        }

        private boolean calculateColumns() {
            int i = this.mColumns;
            int size = this.mRecords.size();
            if (size == 0) {
                this.mColumns = 0;
                return true;
            }
            int measuredWidth = (getMeasuredWidth() - getPaddingStart()) - getPaddingEnd();
            int iMax = (measuredWidth - (this.mCellWidth * size)) / Math.max(1, size - 1);
            if (iMax > 0) {
                this.mCellMarginHorizontal = iMax;
                this.mColumns = size;
            } else {
                int i2 = this.mCellWidth;
                int iMin = i2 == 0 ? 1 : Math.min(size, measuredWidth / i2);
                this.mColumns = iMin;
                if (iMin == 1) {
                    this.mCellMarginHorizontal = (measuredWidth - this.mCellWidth) / 2;
                } else {
                    this.mCellMarginHorizontal = (measuredWidth - (this.mCellWidth * iMin)) / (iMin - 1);
                }
            }
            return this.mColumns != i;
        }

        private void setAccessibilityOrder() {
            ArrayList<QSPanel.TileRecord> arrayList = this.mRecords;
            if (arrayList == null || arrayList.size() <= 0) {
                return;
            }
            Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
            View viewUpdateAccessibilityOrder = this;
            while (it.hasNext()) {
                QSPanel.TileRecord next = it.next();
                if (next.tileView.getVisibility() != 8) {
                    viewUpdateAccessibilityOrder = next.tileView.updateAccessibilityOrder(viewUpdateAccessibilityOrder);
                }
            }
            this.mRecords.get(r5.size() - 1).tileView.setAccessibilityTraversalBefore(R.id.expand_indicator);
        }

        @Override // com.android.systemui.qs.TileLayout, android.view.View
        protected void onMeasure(int i, int i2) {
            Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
            while (it.hasNext()) {
                QSPanel.TileRecord next = it.next();
                if (next.tileView.getVisibility() != 8) {
                    next.tileView.measure(TileLayout.exactly(this.mCellWidth), TileLayout.exactly(this.mCellHeight));
                }
            }
            int i3 = this.mCellHeight;
            if (i3 < 0) {
                i3 = 0;
            }
            setMeasuredDimension(View.MeasureSpec.getSize(i), i3);
        }

        @Override // com.android.systemui.qs.TileLayout, com.android.systemui.qs.QSPanel.QSTileLayout
        public int getNumVisibleTiles() {
            return this.mColumns;
        }

        @Override // com.android.systemui.qs.TileLayout
        protected int getColumnStart(int i) {
            if (this.mColumns == 1) {
                return getPaddingStart() + this.mCellMarginHorizontal;
            }
            return getPaddingStart() + (i * (this.mCellWidth + this.mCellMarginHorizontal));
        }

        @Override // com.android.systemui.qs.TileLayout, com.android.systemui.qs.QSPanel.QSTileLayout
        public void setListening(boolean z) {
            boolean z2 = !this.mListening && z;
            super.setListening(z);
            if (z2) {
                for (int i = 0; i < getNumVisibleTiles(); i++) {
                    QSTile qSTile = this.mRecords.get(i).tile;
                    this.mUiEventLogger.logWithInstanceId(QSEvent.QQS_TILE_VISIBLE, 0, qSTile.getMetricsSpec(), qSTile.getInstanceId());
                }
            }
        }
    }
}
