package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.qs.QSPanel;
import java.util.ArrayList;
import java.util.Iterator;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: DoubleLineTileLayout.kt */
/* loaded from: classes.dex */
public final class DoubleLineTileLayout extends ViewGroup implements QSPanel.QSTileLayout {
    public static final Companion Companion = new Companion(null);
    private boolean _listening;
    private int cellMarginHorizontal;
    private int cellMarginVertical;

    @NotNull
    private final ArrayList<QSPanel.TileRecord> mRecords;
    private int smallTileSize;
    private int tilesToShow;
    private final UiEventLogger uiEventLogger;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public DoubleLineTileLayout(@NotNull Context context, @NotNull UiEventLogger uiEventLogger) {
        super(context);
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(uiEventLogger, "uiEventLogger");
        this.uiEventLogger = uiEventLogger;
        this.mRecords = new ArrayList<>();
        setFocusableInTouchMode(true);
        setClipChildren(false);
        setClipToPadding(false);
        updateResources();
    }

    /* compiled from: DoubleLineTileLayout.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    private final int getTwoLineHeight() {
        return (this.smallTileSize * 2) + (this.cellMarginVertical * 1);
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public void addTile(@NotNull QSPanel.TileRecord tile) {
        Intrinsics.checkParameterIsNotNull(tile, "tile");
        this.mRecords.add(tile);
        tile.tile.setListening(this, this._listening);
        addTileView(tile);
    }

    protected final void addTileView(@NotNull QSPanel.TileRecord tile) {
        Intrinsics.checkParameterIsNotNull(tile, "tile");
        addView(tile.tileView);
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public void removeTile(@NotNull QSPanel.TileRecord tile) {
        Intrinsics.checkParameterIsNotNull(tile, "tile");
        this.mRecords.remove(tile);
        tile.tile.setListening(this, false);
        removeView(tile.tileView);
    }

    @Override // android.view.ViewGroup
    public void removeAllViews() {
        Iterator<T> it = this.mRecords.iterator();
        while (it.hasNext()) {
            ((QSPanel.TileRecord) it.next()).tile.setListening(this, false);
        }
        this.mRecords.clear();
        super.removeAllViews();
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public int getOffsetTop(@Nullable QSPanel.TileRecord tileRecord) {
        return getTop();
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public boolean updateResources() {
        Context mContext = ((ViewGroup) this).mContext;
        Intrinsics.checkExpressionValueIsNotNull(mContext, "mContext");
        Resources resources = mContext.getResources();
        this.smallTileSize = resources.getDimensionPixelSize(R.dimen.qs_quick_tile_size);
        this.cellMarginHorizontal = resources.getDimensionPixelSize(R.dimen.qs_tile_margin_horizontal_two_line);
        this.cellMarginVertical = resources.getDimensionPixelSize(R.dimen.new_qs_vertical_margin);
        requestLayout();
        return false;
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public void setListening(boolean z) {
        if (this._listening == z) {
            return;
        }
        this._listening = z;
        Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            it.next().tile.setListening(this, z);
        }
        if (z) {
            int numVisibleTiles = getNumVisibleTiles();
            for (int i = 0; i < numVisibleTiles; i++) {
                QSTile tile = this.mRecords.get(i).tile;
                UiEventLogger uiEventLogger = this.uiEventLogger;
                QSEvent qSEvent = QSEvent.QQS_TILE_VISIBLE;
                Intrinsics.checkExpressionValueIsNotNull(tile, "tile");
                uiEventLogger.logWithInstanceId(qSEvent, 0, tile.getMetricsSpec(), tile.getInstanceId());
            }
        }
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public int getNumVisibleTiles() {
        return this.tilesToShow;
    }

    @Override // android.view.View
    protected void onConfigurationChanged(@NotNull Configuration newConfig) {
        Intrinsics.checkParameterIsNotNull(newConfig, "newConfig");
        super.onConfigurationChanged(newConfig);
        updateResources();
        postInvalidate();
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        updateResources();
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        Iterator<T> it = this.mRecords.iterator();
        while (it.hasNext()) {
            ((QSPanel.TileRecord) it.next()).tileView.measure(TileLayout.exactly(this.smallTileSize), TileLayout.exactly(this.smallTileSize));
        }
        setMeasuredDimension(View.MeasureSpec.getSize(i), getTwoLineHeight() + getPaddingBottom() + getPaddingTop());
    }

    private final int calculateMaxColumns(int i) {
        int i2 = this.smallTileSize;
        int i3 = this.cellMarginHorizontal;
        if (i2 + i3 == 0) {
            return 0;
        }
        return ((i - i2) / (i2 + i3)) + 1;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int paddingLeft = ((i3 - i) - getPaddingLeft()) - getPaddingRight();
        int iMin = Math.min(calculateMaxColumns(paddingLeft), this.mRecords.size() / 2);
        if (iMin == 0) {
            return;
        }
        this.tilesToShow = iMin * 2;
        int i5 = paddingLeft / iMin;
        int size = this.mRecords.size();
        int i6 = 0;
        while (i6 < size) {
            QSTileView tileView = this.mRecords.get(i6).tileView;
            if (i6 >= this.tilesToShow) {
                Intrinsics.checkExpressionValueIsNotNull(tileView, "tileView");
                tileView.setVisibility(8);
            } else {
                Intrinsics.checkExpressionValueIsNotNull(tileView, "tileView");
                tileView.setVisibility(0);
                if (i6 > 0) {
                    tileView.updateAccessibilityOrder(this.mRecords.get(i6 - 1).tileView);
                }
                int leftForColumn = getLeftForColumn(i6 % iMin, i5);
                int topBottomRow = i6 < iMin ? 0 : getTopBottomRow();
                int i7 = this.smallTileSize;
                tileView.layout(leftForColumn, topBottomRow, leftForColumn + i7, i7 + topBottomRow);
            }
            i6++;
        }
    }

    private final int getLeftForColumn(int i, int i2) {
        return (int) (((i * i2) + (i2 / 2.0f)) - (this.smallTileSize / 2.0f));
    }

    private final int getTopBottomRow() {
        return this.smallTileSize + this.cellMarginVertical;
    }
}
