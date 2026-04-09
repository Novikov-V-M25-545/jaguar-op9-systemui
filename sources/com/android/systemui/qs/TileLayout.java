package com.android.systemui.qs;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.qs.QSPanel;
import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes.dex */
public class TileLayout extends ViewGroup implements QSPanel.QSTileLayout {
    protected int mCellHeight;
    protected int mCellMarginHorizontal;
    private int mCellMarginTop;
    protected int mCellMarginVertical;
    protected int mCellWidth;
    protected int mColumns;
    private final boolean mLessRows;
    protected boolean mListening;
    protected int mMaxAllowedRows;
    private int mMaxColumns;
    private int mMinRows;
    protected final ArrayList<QSPanel.TileRecord> mRecords;
    private int mResourceColumns;
    private int mResourceRows;
    protected int mRows;

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public TileLayout(Context context) {
        this(context, null);
    }

    public TileLayout(Context context, AttributeSet attributeSet) throws Resources.NotFoundException {
        super(context, attributeSet);
        this.mRows = 1;
        this.mRecords = new ArrayList<>();
        this.mMaxAllowedRows = 3;
        this.mMinRows = 1;
        this.mMaxColumns = 100;
        setFocusableInTouchMode(true);
        this.mLessRows = Settings.System.getInt(context.getContentResolver(), "qs_less_rows", 0) != 0;
        updateResources();
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public int getOffsetTop(QSPanel.TileRecord tileRecord) {
        return getTop();
    }

    public void setListening(boolean z) {
        if (this.mListening == z) {
            return;
        }
        this.mListening = z;
        Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            it.next().tile.setListening(this, this.mListening);
        }
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public boolean setMinRows(int i) {
        if (this.mMinRows == i) {
            return false;
        }
        this.mMinRows = i;
        updateResources();
        return true;
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public boolean setMaxColumns(int i) {
        this.mMaxColumns = i;
        return updateColumns();
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public void addTile(QSPanel.TileRecord tileRecord) {
        this.mRecords.add(tileRecord);
        tileRecord.tile.setListening(this, this.mListening);
        addTileView(tileRecord);
    }

    protected void addTileView(QSPanel.TileRecord tileRecord) {
        addView(tileRecord.tileView);
        tileRecord.tileView.textVisibility();
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public void removeTile(QSPanel.TileRecord tileRecord) {
        this.mRecords.remove(tileRecord);
        tileRecord.tile.setListening(this, false);
        removeView(tileRecord.tileView);
    }

    @Override // android.view.ViewGroup
    public void removeAllViews() {
        Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            it.next().tile.setListening(this, false);
        }
        this.mRecords.clear();
        super.removeAllViews();
    }

    public boolean updateResources() throws Resources.NotFoundException {
        Resources resources = ((ViewGroup) this).mContext.getResources();
        ContentResolver contentResolver = ((ViewGroup) this).mContext.getContentResolver();
        int integer = resources.getInteger(R.integer.config_qs_columns_portrait);
        int integer2 = resources.getInteger(R.integer.config_qs_rows_portrait);
        int integer3 = resources.getInteger(R.integer.config_qs_columns_landscape);
        int integer4 = resources.getInteger(R.integer.config_qs_rows_landscape);
        if (resources.getConfiguration().orientation == 1) {
            this.mResourceColumns = Settings.System.getIntForUser(contentResolver, "qs_columns_portrait", integer, -2);
            this.mResourceRows = Settings.System.getIntForUser(contentResolver, "qs_rows_portrait", integer2, -2);
        } else {
            this.mResourceColumns = Settings.System.getIntForUser(contentResolver, "qs_columns_landscape", integer3, -2);
            this.mResourceRows = Settings.System.getIntForUser(contentResolver, "qs_rows_landscape", integer4, -2);
        }
        if (this.mResourceColumns < 1) {
            this.mResourceColumns = 1;
        }
        if (this.mResourceRows < 1) {
            this.mResourceRows = 1;
        }
        if (Settings.System.getIntForUser(contentResolver, "qs_tile_title_visibility", 1, -2) == 1) {
            this.mCellHeight = ((ViewGroup) this).mContext.getResources().getDimensionPixelSize(R.dimen.qs_tile_height);
        } else {
            this.mCellHeight = ((ViewGroup) this).mContext.getResources().getDimensionPixelSize(R.dimen.qs_tile_height_wo_label);
        }
        this.mCellMarginHorizontal = resources.getDimensionPixelSize(R.dimen.qs_tile_margin_horizontal);
        this.mCellMarginVertical = resources.getDimensionPixelSize(R.dimen.qs_tile_margin_vertical);
        this.mCellMarginTop = resources.getDimensionPixelSize(R.dimen.qs_tile_margin_top);
        Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            it.next().tileView.textVisibility();
        }
        int iMax = Math.max(1, getResources().getInteger(R.integer.quick_settings_max_rows));
        this.mMaxAllowedRows = iMax;
        if (this.mLessRows) {
            this.mMaxAllowedRows = Math.max(this.mMinRows, iMax - 1);
        }
        if (updateColumns()) {
            requestLayout();
            return true;
        }
        requestLayout();
        return false;
    }

    private boolean updateColumns() {
        int i = this.mColumns;
        int i2 = this.mRows;
        this.mColumns = Math.min(this.mResourceColumns, this.mMaxColumns);
        int iMin = Math.min(this.mResourceRows, this.mMaxAllowedRows);
        this.mRows = iMin;
        return (i == this.mColumns && i2 == iMin) ? false : true;
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int size = this.mRecords.size();
        int size2 = View.MeasureSpec.getSize(i);
        int paddingStart = (size2 - getPaddingStart()) - getPaddingEnd();
        if (View.MeasureSpec.getMode(i2) == 0) {
            this.mRows = ((size + r7) - 1) / this.mColumns;
        }
        int i3 = this.mCellMarginHorizontal;
        int i4 = this.mColumns;
        this.mCellWidth = (paddingStart - (i3 * i4)) / i4;
        Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
        View viewUpdateAccessibilityOrder = this;
        while (it.hasNext()) {
            QSPanel.TileRecord next = it.next();
            if (next.tileView.getVisibility() != 8) {
                next.tileView.measure(exactly(this.mCellWidth), exactly(this.mCellHeight));
                viewUpdateAccessibilityOrder = next.tileView.updateAccessibilityOrder(viewUpdateAccessibilityOrder);
            }
        }
        int i5 = this.mCellHeight;
        int i6 = this.mCellMarginVertical;
        int i7 = this.mRows;
        int i8 = ((i5 + i6) * i7) + (i7 != 0 ? this.mCellMarginTop - i6 : 0);
        setMeasuredDimension(size2, i8 >= 0 ? i8 : 0);
    }

    public boolean updateMaxRows(int i, int i2) {
        int i3 = this.mRows;
        int i4 = this.mColumns;
        if (i3 > ((i2 + i4) - 1) / i4) {
            this.mRows = ((i2 + i4) - 1) / i4;
        }
        return i3 != this.mRows;
    }

    protected static int exactly(int i) {
        return View.MeasureSpec.makeMeasureSpec(i, 1073741824);
    }

    protected void layoutTileRecords(int i) {
        boolean z = getLayoutDirection() == 1;
        int iMin = Math.min(i, this.mRows * this.mColumns);
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        while (i2 < iMin) {
            if (i3 == this.mColumns) {
                i4++;
                i3 = 0;
            }
            QSPanel.TileRecord tileRecord = this.mRecords.get(i2);
            int rowTop = getRowTop(i4);
            int columnStart = getColumnStart(z ? (this.mColumns - i3) - 1 : i3);
            int i5 = this.mCellWidth + columnStart;
            QSTileView qSTileView = tileRecord.tileView;
            qSTileView.layout(columnStart, rowTop, i5, qSTileView.getMeasuredHeight() + rowTop);
            i2++;
            i3++;
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        layoutTileRecords(this.mRecords.size());
    }

    private int getRowTop(int i) {
        return (i * (this.mCellHeight + this.mCellMarginVertical)) + this.mCellMarginTop;
    }

    protected int getColumnStart(int i) {
        int paddingStart = getPaddingStart();
        int i2 = this.mCellMarginHorizontal;
        return paddingStart + (i2 / 2) + (i * (this.mCellWidth + i2));
    }

    public int getNumVisibleTiles() {
        return this.mRecords.size();
    }
}
