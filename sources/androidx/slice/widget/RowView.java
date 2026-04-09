package androidx.slice.widget;

import android.R;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.core.view.ViewCompat;
import androidx.slice.SliceItem;
import androidx.slice.core.SliceAction;
import androidx.slice.core.SliceActionImpl;
import androidx.slice.core.SliceQuery;
import androidx.slice.view.R$dimen;
import androidx.slice.view.R$id;
import androidx.slice.view.R$layout;
import androidx.slice.view.R$plurals;
import androidx.slice.widget.SliceActionView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* loaded from: classes.dex */
public class RowView extends SliceChildView implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private static final boolean sCanSpecifyLargerRangeBarHeight;
    private View mActionDivider;
    private ProgressBar mActionSpinner;
    private ArrayMap<SliceActionImpl, SliceActionView> mActions;
    private boolean mAllowTwoLines;
    private View mBottomDivider;
    private LinearLayout mContent;
    private LinearLayout mEndContainer;
    Handler mHandler;
    private List<SliceAction> mHeaderActions;
    private int mIconSize;
    private int mImageSize;
    private boolean mIsHeader;
    boolean mIsRangeSliding;
    long mLastSentRangeUpdate;
    private TextView mLastUpdatedText;
    protected Set<SliceItem> mLoadingActions;
    private int mMeasuredRangeHeight;
    private TextView mPrimaryText;
    private ProgressBar mRangeBar;
    boolean mRangeHasPendingUpdate;
    private SliceItem mRangeItem;
    int mRangeMaxValue;
    int mRangeMinValue;
    Runnable mRangeUpdater;
    boolean mRangeUpdaterRunning;
    int mRangeValue;
    private LinearLayout mRootView;
    private SliceActionImpl mRowAction;
    RowContent mRowContent;
    int mRowIndex;
    private TextView mSecondaryText;
    private View mSeeMoreView;
    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener;
    private SliceItem mSelectionItem;
    private ArrayList<String> mSelectionOptionKeys;
    private ArrayList<CharSequence> mSelectionOptionValues;
    private Spinner mSelectionSpinner;
    boolean mShowActionSpinner;
    private LinearLayout mStartContainer;
    private SliceItem mStartItem;
    private LinearLayout mSubContent;
    private ArrayMap<SliceActionImpl, SliceActionView> mToggles;

    @Override // android.widget.AdapterView.OnItemSelectedListener
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    static {
        sCanSpecifyLargerRangeBarHeight = Build.VERSION.SDK_INT >= 23;
    }

    public RowView(Context context) {
        super(context);
        this.mToggles = new ArrayMap<>();
        this.mActions = new ArrayMap<>();
        this.mLoadingActions = new HashSet();
        this.mRangeUpdater = new Runnable() { // from class: androidx.slice.widget.RowView.2
            @Override // java.lang.Runnable
            public void run() {
                RowView.this.sendSliderValue();
                RowView.this.mRangeUpdaterRunning = false;
            }
        };
        this.mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() { // from class: androidx.slice.widget.RowView.3
            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                RowView rowView = RowView.this;
                rowView.mRangeValue = i + rowView.mRangeMinValue;
                long jCurrentTimeMillis = System.currentTimeMillis();
                RowView rowView2 = RowView.this;
                long j = rowView2.mLastSentRangeUpdate;
                if (j != 0 && jCurrentTimeMillis - j > 200) {
                    rowView2.mRangeUpdaterRunning = false;
                    rowView2.mHandler.removeCallbacks(rowView2.mRangeUpdater);
                    RowView.this.sendSliderValue();
                } else {
                    if (rowView2.mRangeUpdaterRunning) {
                        return;
                    }
                    rowView2.mRangeUpdaterRunning = true;
                    rowView2.mHandler.postDelayed(rowView2.mRangeUpdater, 200L);
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar seekBar) {
                RowView.this.mIsRangeSliding = true;
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar seekBar) {
                RowView rowView = RowView.this;
                rowView.mIsRangeSliding = false;
                if (rowView.mRangeUpdaterRunning || rowView.mRangeHasPendingUpdate) {
                    rowView.mRangeUpdaterRunning = false;
                    rowView.mRangeHasPendingUpdate = false;
                    rowView.mHandler.removeCallbacks(rowView.mRangeUpdater);
                    RowView rowView2 = RowView.this;
                    int progress = seekBar.getProgress();
                    RowView rowView3 = RowView.this;
                    rowView2.mRangeValue = progress + rowView3.mRangeMinValue;
                    rowView3.sendSliderValue();
                }
            }
        };
        this.mIconSize = getContext().getResources().getDimensionPixelSize(R$dimen.abc_slice_icon_size);
        this.mImageSize = getContext().getResources().getDimensionPixelSize(R$dimen.abc_slice_small_image_size);
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(context).inflate(R$layout.abc_slice_small_template, (ViewGroup) this, false);
        this.mRootView = linearLayout;
        addView(linearLayout);
        this.mStartContainer = (LinearLayout) findViewById(R$id.icon_frame);
        this.mContent = (LinearLayout) findViewById(R.id.content);
        this.mSubContent = (LinearLayout) findViewById(R$id.subcontent);
        this.mPrimaryText = (TextView) findViewById(R.id.title);
        this.mSecondaryText = (TextView) findViewById(R.id.summary);
        this.mLastUpdatedText = (TextView) findViewById(R$id.last_updated);
        this.mBottomDivider = findViewById(R$id.bottom_divider);
        this.mActionDivider = findViewById(R$id.action_divider);
        this.mActionSpinner = (ProgressBar) findViewById(R$id.action_sent_indicator);
        SliceViewUtil.tintIndeterminateProgressBar(getContext(), this.mActionSpinner);
        this.mEndContainer = (LinearLayout) findViewById(R.id.widget_frame);
        ViewCompat.setImportantForAccessibility(this, 2);
        ViewCompat.setImportantForAccessibility(this.mContent, 2);
    }

    @Override // androidx.slice.widget.SliceChildView
    public void setStyle(SliceStyle sliceStyle) {
        super.setStyle(sliceStyle);
        applyRowStyle();
    }

    private void applyRowStyle() {
        SliceStyle sliceStyle = this.mSliceStyle;
        if (sliceStyle == null || sliceStyle.getRowStyle() == null) {
            return;
        }
        RowStyle rowStyle = this.mSliceStyle.getRowStyle();
        setViewSidePaddings(this.mStartContainer, rowStyle.getTitleItemStartPadding(), rowStyle.getTitleItemEndPadding());
        setViewSidePaddings(this.mContent, rowStyle.getContentStartPadding(), rowStyle.getContentEndPadding());
        setViewSidePaddings(this.mPrimaryText, rowStyle.getTitleStartPadding(), rowStyle.getTitleEndPadding());
        setViewSidePaddings(this.mSubContent, rowStyle.getSubContentStartPadding(), rowStyle.getSubContentEndPadding());
        setViewSidePaddings(this.mEndContainer, rowStyle.getEndItemStartPadding(), rowStyle.getEndItemEndPadding());
        setViewSideMargins(this.mBottomDivider, rowStyle.getBottomDividerStartPadding(), rowStyle.getBottomDividerEndPadding());
        setViewHeight(this.mActionDivider, rowStyle.getActionDividerHeight());
    }

    private void setViewSidePaddings(View view, int i, int i2) {
        boolean z = i < 0 && i2 < 0;
        if (view == null || z) {
            return;
        }
        if (i < 0) {
            i = view.getPaddingStart();
        }
        int paddingTop = view.getPaddingTop();
        if (i2 < 0) {
            i2 = view.getPaddingEnd();
        }
        view.setPaddingRelative(i, paddingTop, i2, view.getPaddingBottom());
    }

    private void setViewSideMargins(View view, int i, int i2) {
        boolean z = i < 0 && i2 < 0;
        if (view == null || z) {
            return;
        }
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        if (i >= 0) {
            marginLayoutParams.setMarginStart(i);
        }
        if (i2 >= 0) {
            marginLayoutParams.setMarginEnd(i2);
        }
        view.setLayoutParams(marginLayoutParams);
    }

    private void setViewHeight(View view, int i) {
        if (view == null || i < 0) {
            return;
        }
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = i;
        view.setLayoutParams(layoutParams);
    }

    private void setViewWidth(View view, int i) {
        if (view == null || i < 0) {
            return;
        }
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = i;
        view.setLayoutParams(layoutParams);
    }

    @Override // androidx.slice.widget.SliceChildView
    public void setInsets(int i, int i2, int i3, int i4) {
        super.setInsets(i, i2, i3, i4);
        setPadding(i, i2, i3, i4);
    }

    private int getRowContentHeight() {
        int height = this.mRowContent.getHeight(this.mSliceStyle, this.mViewPolicy);
        if (this.mRangeBar != null && this.mStartItem == null) {
            height -= this.mSliceStyle.getRowRangeHeight();
        }
        return this.mSelectionSpinner != null ? height - this.mSliceStyle.getRowSelectionHeight() : height;
    }

    @Override // androidx.slice.widget.SliceChildView
    public void setTint(int i) {
        super.setTint(i);
        if (this.mRowContent != null) {
            populateViews(true);
        }
    }

    @Override // androidx.slice.widget.SliceChildView
    public void setSliceActions(List<SliceAction> list) {
        this.mHeaderActions = list;
        if (this.mRowContent != null) {
            updateEndItems();
        }
    }

    @Override // androidx.slice.widget.SliceChildView
    public void setShowLastUpdated(boolean z) {
        super.setShowLastUpdated(z);
        if (this.mRowContent != null) {
            populateViews(true);
        }
    }

    @Override // androidx.slice.widget.SliceChildView
    public void setAllowTwoLines(boolean z) {
        this.mAllowTwoLines = z;
        if (this.mRowContent != null) {
            populateViews(true);
        }
    }

    private void measureChildWithExactHeight(View view, int i, int i2) {
        measureChild(view, i, View.MeasureSpec.makeMeasureSpec(i2 + this.mInsetTop + this.mInsetBottom, 1073741824));
    }

    @Override // android.widget.FrameLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        int iMax;
        int rowContentHeight = getRowContentHeight();
        if (rowContentHeight != 0) {
            this.mRootView.setVisibility(0);
            measureChildWithExactHeight(this.mRootView, i, rowContentHeight);
            iMax = this.mRootView.getMeasuredWidth();
        } else {
            this.mRootView.setVisibility(8);
            iMax = 0;
        }
        ProgressBar progressBar = this.mRangeBar;
        if (progressBar != null && this.mStartItem == null) {
            if (sCanSpecifyLargerRangeBarHeight) {
                measureChildWithExactHeight(progressBar, i, this.mSliceStyle.getRowRangeHeight());
            } else {
                measureChild(progressBar, i, View.MeasureSpec.makeMeasureSpec(0, 0));
            }
            this.mMeasuredRangeHeight = this.mRangeBar.getMeasuredHeight();
            iMax = Math.max(iMax, this.mRangeBar.getMeasuredWidth());
        } else {
            Spinner spinner = this.mSelectionSpinner;
            if (spinner != null) {
                measureChildWithExactHeight(spinner, i, this.mSliceStyle.getRowSelectionHeight());
                iMax = Math.max(iMax, this.mSelectionSpinner.getMeasuredWidth());
            }
        }
        int iMax2 = Math.max(iMax + this.mInsetStart + this.mInsetEnd, getSuggestedMinimumWidth());
        RowContent rowContent = this.mRowContent;
        setMeasuredDimension(FrameLayout.resolveSizeAndState(iMax2, i, 0), (rowContent != null ? rowContent.getHeight(this.mSliceStyle, this.mViewPolicy) : 0) + this.mInsetTop + this.mInsetBottom);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int paddingLeft = getPaddingLeft();
        LinearLayout linearLayout = this.mRootView;
        linearLayout.layout(paddingLeft, this.mInsetTop, linearLayout.getMeasuredWidth() + paddingLeft, getRowContentHeight() + this.mInsetTop);
        if (this.mRangeBar != null && this.mStartItem == null) {
            int rowContentHeight = getRowContentHeight() + ((this.mSliceStyle.getRowRangeHeight() - this.mMeasuredRangeHeight) / 2) + this.mInsetTop;
            int i5 = this.mMeasuredRangeHeight + rowContentHeight;
            ProgressBar progressBar = this.mRangeBar;
            progressBar.layout(paddingLeft, rowContentHeight, progressBar.getMeasuredWidth() + paddingLeft, i5);
            return;
        }
        if (this.mSelectionSpinner != null) {
            int rowContentHeight2 = getRowContentHeight() + this.mInsetTop;
            int measuredHeight = this.mSelectionSpinner.getMeasuredHeight() + rowContentHeight2;
            Spinner spinner = this.mSelectionSpinner;
            spinner.layout(paddingLeft, rowContentHeight2, spinner.getMeasuredWidth() + paddingLeft, measuredHeight);
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:28:0x0056  */
    @Override // androidx.slice.widget.SliceChildView
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void setSliceItem(androidx.slice.widget.SliceContent r5, boolean r6, int r7, int r8, androidx.slice.widget.SliceView.OnSliceActionListener r9) {
        /*
            r4 = this;
            r4.setSliceActionListener(r9)
            r8 = 0
            if (r5 == 0) goto L56
            androidx.slice.widget.RowContent r9 = r4.mRowContent
            if (r9 == 0) goto L56
            boolean r9 = r9.isValid()
            if (r9 == 0) goto L56
            androidx.slice.widget.RowContent r9 = r4.mRowContent
            if (r9 == 0) goto L1e
            androidx.slice.SliceStructure r0 = new androidx.slice.SliceStructure
            androidx.slice.SliceItem r9 = r9.getSliceItem()
            r0.<init>(r9)
            goto L1f
        L1e:
            r0 = 0
        L1f:
            androidx.slice.SliceStructure r9 = new androidx.slice.SliceStructure
            androidx.slice.SliceItem r1 = r5.getSliceItem()
            androidx.slice.Slice r1 = r1.getSlice()
            r9.<init>(r1)
            r1 = 1
            if (r0 == 0) goto L37
            boolean r2 = r0.equals(r9)
            if (r2 == 0) goto L37
            r2 = r1
            goto L38
        L37:
            r2 = r8
        L38:
            if (r0 == 0) goto L50
            android.net.Uri r3 = r0.getUri()
            if (r3 == 0) goto L50
            android.net.Uri r0 = r0.getUri()
            android.net.Uri r9 = r9.getUri()
            boolean r9 = r0.equals(r9)
            if (r9 == 0) goto L50
            r9 = r1
            goto L51
        L50:
            r9 = r8
        L51:
            if (r9 == 0) goto L56
            if (r2 == 0) goto L56
            goto L57
        L56:
            r1 = r8
        L57:
            r4.mShowActionSpinner = r8
            r4.mIsHeader = r6
            androidx.slice.widget.RowContent r5 = (androidx.slice.widget.RowContent) r5
            r4.mRowContent = r5
            r4.mRowIndex = r7
            r4.populateViews(r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.slice.widget.RowView.setSliceItem(androidx.slice.widget.SliceContent, boolean, int, int, androidx.slice.widget.SliceView$OnSliceActionListener):void");
    }

    private void populateViews(boolean z) {
        int titleSize;
        boolean z2 = z && this.mIsRangeSliding;
        if (!z2) {
            resetViewState();
        }
        if (this.mRowContent.getLayoutDir() != -1) {
            setLayoutDirection(this.mRowContent.getLayoutDir());
        }
        if (this.mRowContent.isDefaultSeeMore()) {
            showSeeMore();
            return;
        }
        CharSequence contentDescription = this.mRowContent.getContentDescription();
        if (contentDescription != null) {
            this.mContent.setContentDescription(contentDescription);
        }
        SliceItem startItem = this.mRowContent.getStartItem();
        this.mStartItem = startItem;
        boolean zAddItem = startItem != null && (this.mRowIndex > 0 || this.mRowContent.hasTitleItems());
        if (zAddItem) {
            zAddItem = addItem(this.mStartItem, this.mTintColor, true);
        }
        this.mStartContainer.setVisibility(zAddItem ? 0 : 8);
        SliceItem titleItem = this.mRowContent.getTitleItem();
        if (titleItem != null) {
            this.mPrimaryText.setText(titleItem.getSanitizedText());
        }
        SliceStyle sliceStyle = this.mSliceStyle;
        if (sliceStyle != null) {
            TextView textView = this.mPrimaryText;
            if (this.mIsHeader) {
                titleSize = sliceStyle.getHeaderTitleSize();
            } else {
                titleSize = sliceStyle.getTitleSize();
            }
            textView.setTextSize(0, titleSize);
            this.mPrimaryText.setTextColor(this.mSliceStyle.getTitleColor());
        }
        this.mPrimaryText.setVisibility(titleItem != null ? 0 : 8);
        addSubtitle(titleItem != null);
        this.mBottomDivider.setVisibility(this.mRowContent.hasBottomDivider() ? 0 : 8);
        SliceItem primaryAction = this.mRowContent.getPrimaryAction();
        if (primaryAction != null && primaryAction != this.mStartItem) {
            SliceActionImpl sliceActionImpl = new SliceActionImpl(primaryAction);
            this.mRowAction = sliceActionImpl;
            if (sliceActionImpl.isToggle()) {
                addAction(this.mRowAction, this.mTintColor, this.mEndContainer, false);
                setViewClickable(this.mRootView, true);
                return;
            }
        }
        SliceItem range = this.mRowContent.getRange();
        if (range != null) {
            if (this.mRowAction != null) {
                setViewClickable(this.mRootView, true);
            }
            this.mRangeItem = range;
            if (!z2) {
                setRangeBounds();
                addRange();
            }
            if (this.mStartItem == null) {
                return;
            }
        }
        SliceItem selection = this.mRowContent.getSelection();
        if (selection != null) {
            this.mSelectionItem = selection;
            addSelection(selection);
        } else {
            updateEndItems();
            updateActionSpinner();
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:96:0x0146  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private void updateEndItems() {
        /*
            Method dump skipped, instructions count: 331
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.slice.widget.RowView.updateEndItems():void");
    }

    @Override // androidx.slice.widget.SliceChildView
    public void setLastUpdated(long j) {
        super.setLastUpdated(j);
        RowContent rowContent = this.mRowContent;
        if (rowContent != null) {
            addSubtitle(rowContent.getTitleItem() != null && TextUtils.isEmpty(this.mRowContent.getTitleItem().getSanitizedText()));
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:20:0x0047  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private void addSubtitle(boolean r10) {
        /*
            Method dump skipped, instructions count: 318
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.slice.widget.RowView.addSubtitle(boolean):void");
    }

    private CharSequence getRelativeTimeString(long j) {
        long jCurrentTimeMillis = System.currentTimeMillis() - j;
        if (jCurrentTimeMillis > 31449600000L) {
            int i = (int) (jCurrentTimeMillis / 31449600000L);
            return getResources().getQuantityString(R$plurals.abc_slice_duration_years, i, Integer.valueOf(i));
        }
        if (jCurrentTimeMillis > 86400000) {
            int i2 = (int) (jCurrentTimeMillis / 86400000);
            return getResources().getQuantityString(R$plurals.abc_slice_duration_days, i2, Integer.valueOf(i2));
        }
        if (jCurrentTimeMillis <= 60000) {
            return null;
        }
        int i3 = (int) (jCurrentTimeMillis / 60000);
        return getResources().getQuantityString(R$plurals.abc_slice_duration_min, i3, Integer.valueOf(i3));
    }

    private void setRangeBounds() {
        SliceItem sliceItemFindSubtype = SliceQuery.findSubtype(this.mRangeItem, "int", "min");
        int i = sliceItemFindSubtype != null ? sliceItemFindSubtype.getInt() : 0;
        this.mRangeMinValue = i;
        SliceItem sliceItemFindSubtype2 = SliceQuery.findSubtype(this.mRangeItem, "int", "max");
        this.mRangeMaxValue = sliceItemFindSubtype2 != null ? sliceItemFindSubtype2.getInt() : 100;
        SliceItem sliceItemFindSubtype3 = SliceQuery.findSubtype(this.mRangeItem, "int", "value");
        this.mRangeValue = sliceItemFindSubtype3 != null ? sliceItemFindSubtype3.getInt() - i : 0;
    }

    private void addRange() {
        ProgressBar progressBar;
        Drawable drawableWrap;
        Drawable drawableLoadDrawable;
        if (this.mHandler == null) {
            this.mHandler = new Handler();
        }
        SliceItem sliceItemFindSubtype = SliceQuery.findSubtype(this.mRangeItem, "int", "range_mode");
        boolean z = sliceItemFindSubtype != null && sliceItemFindSubtype.getInt() == 1;
        boolean zEquals = "action".equals(this.mRangeItem.getFormat());
        boolean z2 = this.mStartItem == null;
        if (!zEquals) {
            if (z2) {
                progressBar = new ProgressBar(getContext(), null, R.attr.progressBarStyleHorizontal);
            } else {
                progressBar = (ProgressBar) LayoutInflater.from(getContext()).inflate(R$layout.abc_slice_progress_inline_view, (ViewGroup) this, false);
                SliceStyle sliceStyle = this.mSliceStyle;
                if (sliceStyle != null && sliceStyle.getRowStyle() != null) {
                    setViewWidth(progressBar, this.mSliceStyle.getRowStyle().getProgressBarInlineWidth());
                    setViewSidePaddings(progressBar, this.mSliceStyle.getRowStyle().getProgressBarStartPadding(), this.mSliceStyle.getRowStyle().getProgressBarEndPadding());
                }
            }
            if (z) {
                progressBar.setIndeterminate(true);
            }
        } else if (z2) {
            progressBar = new SeekBar(getContext());
        } else {
            progressBar = (SeekBar) LayoutInflater.from(getContext()).inflate(R$layout.abc_slice_seekbar_view, (ViewGroup) this, false);
            SliceStyle sliceStyle2 = this.mSliceStyle;
            if (sliceStyle2 != null && sliceStyle2.getRowStyle() != null) {
                setViewWidth(progressBar, this.mSliceStyle.getRowStyle().getSeekBarInlineWidth());
            }
        }
        if (z) {
            drawableWrap = DrawableCompat.wrap(progressBar.getIndeterminateDrawable());
        } else {
            drawableWrap = DrawableCompat.wrap(progressBar.getProgressDrawable());
        }
        int i = this.mTintColor;
        if (i != -1 && drawableWrap != null) {
            DrawableCompat.setTint(drawableWrap, i);
            if (z) {
                progressBar.setIndeterminateDrawable(drawableWrap);
            } else {
                progressBar.setProgressDrawable(drawableWrap);
            }
        }
        progressBar.setMax(this.mRangeMaxValue - this.mRangeMinValue);
        progressBar.setProgress(this.mRangeValue);
        progressBar.setVisibility(0);
        if (this.mStartItem == null) {
            addView(progressBar, new FrameLayout.LayoutParams(-1, -2));
        } else {
            this.mSubContent.setVisibility(8);
            this.mContent.addView(progressBar, 1);
        }
        this.mRangeBar = progressBar;
        if (zEquals) {
            SliceItem inputRangeThumb = this.mRowContent.getInputRangeThumb();
            SeekBar seekBar = (SeekBar) this.mRangeBar;
            if (inputRangeThumb != null && inputRangeThumb.getIcon() != null && (drawableLoadDrawable = inputRangeThumb.getIcon().loadDrawable(getContext())) != null) {
                seekBar.setThumb(drawableLoadDrawable);
            }
            Drawable drawableWrap2 = DrawableCompat.wrap(seekBar.getThumb());
            int i2 = this.mTintColor;
            if (i2 != -1 && drawableWrap2 != null) {
                DrawableCompat.setTint(drawableWrap2, i2);
                seekBar.setThumb(drawableWrap2);
            }
            seekBar.setOnSeekBarChangeListener(this.mSeekBarChangeListener);
        }
    }

    void sendSliderValue() {
        if (this.mRangeItem == null) {
            return;
        }
        try {
            this.mLastSentRangeUpdate = System.currentTimeMillis();
            this.mRangeItem.fireAction(getContext(), new Intent().addFlags(268435456).putExtra("android.app.slice.extra.RANGE_VALUE", this.mRangeValue));
            if (this.mObserver != null) {
                EventInfo eventInfo = new EventInfo(getMode(), 2, 4, this.mRowIndex);
                eventInfo.state = this.mRangeValue;
                this.mObserver.onSliceAction(eventInfo, this.mRangeItem);
            }
        } catch (PendingIntent.CanceledException e) {
            Log.e("RowView", "PendingIntent for slice cannot be sent", e);
        }
    }

    private void addSelection(SliceItem sliceItem) {
        if (this.mHandler == null) {
            this.mHandler = new Handler();
        }
        this.mSelectionOptionKeys = new ArrayList<>();
        this.mSelectionOptionValues = new ArrayList<>();
        List<SliceItem> items = sliceItem.getSlice().getItems();
        for (int i = 0; i < items.size(); i++) {
            SliceItem sliceItem2 = items.get(i);
            if (sliceItem2.hasHint("selection_option")) {
                SliceItem sliceItemFindSubtype = SliceQuery.findSubtype(sliceItem2, "text", "selection_option_key");
                SliceItem sliceItemFindSubtype2 = SliceQuery.findSubtype(sliceItem2, "text", "selection_option_value");
                if (sliceItemFindSubtype != null && sliceItemFindSubtype2 != null) {
                    this.mSelectionOptionKeys.add(sliceItemFindSubtype.getText().toString());
                    this.mSelectionOptionValues.add(sliceItemFindSubtype2.getSanitizedText());
                }
            }
        }
        this.mSelectionSpinner = (Spinner) LayoutInflater.from(getContext()).inflate(R$layout.abc_slice_row_selection, (ViewGroup) this, false);
        ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), R$layout.abc_slice_row_selection_text, this.mSelectionOptionValues);
        arrayAdapter.setDropDownViewResource(R$layout.abc_slice_row_selection_dropdown_text);
        this.mSelectionSpinner.setAdapter((SpinnerAdapter) arrayAdapter);
        addView(this.mSelectionSpinner);
        this.mSelectionSpinner.setOnItemSelectedListener(this);
    }

    private void addAction(SliceActionImpl sliceActionImpl, int i, ViewGroup viewGroup, boolean z) {
        SliceActionView sliceActionView = new SliceActionView(getContext());
        viewGroup.addView(sliceActionView);
        if (viewGroup.getVisibility() == 8) {
            viewGroup.setVisibility(0);
        }
        boolean zIsToggle = sliceActionImpl.isToggle();
        EventInfo eventInfo = new EventInfo(getMode(), !zIsToggle ? 1 : 0, zIsToggle ? 3 : 0, this.mRowIndex);
        if (z) {
            eventInfo.setPosition(0, 0, 1);
        }
        sliceActionView.setAction(sliceActionImpl, eventInfo, this.mObserver, i, this.mLoadingListener);
        if (this.mLoadingActions.contains(sliceActionImpl.getSliceItem())) {
            sliceActionView.setLoading(true);
        }
        if (zIsToggle) {
            this.mToggles.put(sliceActionImpl, sliceActionView);
        } else {
            this.mActions.put(sliceActionImpl, sliceActionView);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private boolean addItem(SliceItem sliceItem, int i, boolean z) {
        IconCompat icon;
        SliceItem sliceItem2;
        int iRound;
        ViewGroup viewGroup = z ? this.mStartContainer : this.mEndContainer;
        if ("slice".equals(sliceItem.getFormat()) || "action".equals(sliceItem.getFormat())) {
            if (sliceItem.hasHint("shortcut")) {
                addAction(new SliceActionImpl(sliceItem), i, viewGroup, z);
                return true;
            }
            if (sliceItem.getSlice().getItems().size() == 0) {
                return false;
            }
            sliceItem = sliceItem.getSlice().getItems().get(0);
        }
        TextView textView = null;
        if ("image".equals(sliceItem.getFormat())) {
            icon = sliceItem.getIcon();
            sliceItem2 = null;
        } else if ("long".equals(sliceItem.getFormat())) {
            sliceItem2 = sliceItem;
            icon = null;
        } else {
            icon = null;
            sliceItem2 = null;
        }
        if (icon != null) {
            boolean z2 = !sliceItem.hasHint("no_tint");
            boolean zHasHint = sliceItem.hasHint("raw");
            float f = getResources().getDisplayMetrics().density;
            ImageView imageView = new ImageView(getContext());
            imageView.setImageDrawable(icon.loadDrawable(getContext()));
            if (z2 && i != -1) {
                imageView.setColorFilter(i);
            }
            if (this.mIsRangeSliding) {
                viewGroup.removeAllViews();
                viewGroup.addView(imageView);
            } else {
                viewGroup.addView(imageView);
            }
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) imageView.getLayoutParams();
            layoutParams.width = zHasHint ? Math.round(r10.getIntrinsicWidth() / f) : this.mImageSize;
            if (zHasHint) {
                iRound = Math.round(r10.getIntrinsicHeight() / f);
            } else {
                iRound = this.mImageSize;
            }
            layoutParams.height = iRound;
            imageView.setLayoutParams(layoutParams);
            SliceStyle sliceStyle = this.mSliceStyle;
            if (sliceStyle != null && sliceStyle.getRowStyle() != null) {
                int iconSize = this.mSliceStyle.getRowStyle().getIconSize();
                if (iconSize <= 0) {
                    iconSize = this.mIconSize;
                }
                this.mIconSize = iconSize;
            }
            int i2 = z2 ? this.mIconSize / 2 : 0;
            imageView.setPadding(i2, i2, i2, i2);
            textView = imageView;
        } else if (sliceItem2 != null) {
            textView = new TextView(getContext());
            textView.setText(SliceViewUtil.getTimestampString(getContext(), sliceItem.getLong()));
            if (this.mSliceStyle != null) {
                textView.setTextSize(0, r8.getSubtitleSize());
                textView.setTextColor(this.mSliceStyle.getSubtitleColor());
            }
            viewGroup.addView(textView);
        }
        return textView != null;
    }

    private void showSeeMore() {
        final Button button = (Button) LayoutInflater.from(getContext()).inflate(R$layout.abc_slice_row_show_more, (ViewGroup) this, false);
        button.setOnClickListener(new View.OnClickListener() { // from class: androidx.slice.widget.RowView.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                try {
                    RowView rowView = RowView.this;
                    if (rowView.mObserver != null) {
                        EventInfo eventInfo = new EventInfo(rowView.getMode(), 4, 0, RowView.this.mRowIndex);
                        RowView rowView2 = RowView.this;
                        rowView2.mObserver.onSliceAction(eventInfo, rowView2.mRowContent.getSliceItem());
                    }
                    RowView rowView3 = RowView.this;
                    rowView3.mShowActionSpinner = rowView3.mRowContent.getSliceItem().fireActionInternal(RowView.this.getContext(), null);
                    RowView rowView4 = RowView.this;
                    if (rowView4.mShowActionSpinner) {
                        SliceActionView.SliceActionLoadingListener sliceActionLoadingListener = rowView4.mLoadingListener;
                        if (sliceActionLoadingListener != null) {
                            sliceActionLoadingListener.onSliceActionLoading(rowView4.mRowContent.getSliceItem(), RowView.this.mRowIndex);
                        }
                        RowView rowView5 = RowView.this;
                        rowView5.mLoadingActions.add(rowView5.mRowContent.getSliceItem());
                        button.setVisibility(8);
                    }
                    RowView.this.updateActionSpinner();
                } catch (PendingIntent.CanceledException e) {
                    Log.e("RowView", "PendingIntent for slice cannot be sent", e);
                }
            }
        });
        int i = this.mTintColor;
        if (i != -1) {
            button.setTextColor(i);
        }
        this.mSeeMoreView = button;
        this.mRootView.addView(button);
        if (this.mLoadingActions.contains(this.mRowContent.getSliceItem())) {
            this.mShowActionSpinner = true;
            button.setVisibility(8);
            updateActionSpinner();
        }
    }

    void updateActionSpinner() {
        this.mActionSpinner.setVisibility(this.mShowActionSpinner ? 0 : 8);
    }

    @Override // androidx.slice.widget.SliceChildView
    public void setLoadingActions(Set<SliceItem> set) {
        if (set == null) {
            this.mLoadingActions.clear();
            this.mShowActionSpinner = false;
        } else {
            this.mLoadingActions = set;
        }
        updateEndItems();
        updateActionSpinner();
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        SliceActionView sliceActionView;
        SliceActionView.SliceActionLoadingListener sliceActionLoadingListener;
        SliceActionImpl sliceActionImpl = this.mRowAction;
        if (sliceActionImpl == null || sliceActionImpl.getActionItem() == null) {
            return;
        }
        if (this.mRowAction.isToggle()) {
            sliceActionView = this.mToggles.get(this.mRowAction);
        } else {
            sliceActionView = this.mActions.get(this.mRowAction);
        }
        if (sliceActionView != null && !(view instanceof SliceActionView)) {
            sliceActionView.sendAction();
            return;
        }
        if (this.mRowIndex == 0) {
            performClick();
            return;
        }
        try {
            this.mShowActionSpinner = this.mRowAction.getActionItem().fireActionInternal(getContext(), null);
            if (this.mObserver != null) {
                this.mObserver.onSliceAction(new EventInfo(getMode(), 3, 0, this.mRowIndex), this.mRowAction.getSliceItem());
            }
            if (this.mShowActionSpinner && (sliceActionLoadingListener = this.mLoadingListener) != null) {
                sliceActionLoadingListener.onSliceActionLoading(this.mRowAction.getSliceItem(), this.mRowIndex);
                this.mLoadingActions.add(this.mRowAction.getSliceItem());
            }
            updateActionSpinner();
        } catch (PendingIntent.CanceledException e) {
            Log.e("RowView", "PendingIntent for slice cannot be sent", e);
        }
    }

    @Override // android.widget.AdapterView.OnItemSelectedListener
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long j) {
        if (this.mSelectionItem == null || adapterView != this.mSelectionSpinner || i < 0 || i >= this.mSelectionOptionKeys.size()) {
            return;
        }
        if (this.mObserver != null) {
            this.mObserver.onSliceAction(new EventInfo(getMode(), 5, 6, this.mRowIndex), this.mSelectionItem);
        }
        try {
            if (this.mSelectionItem.fireActionInternal(getContext(), new Intent().addFlags(268435456).putExtra("android.app.slice.extra.SELECTION", this.mSelectionOptionKeys.get(i)))) {
                this.mShowActionSpinner = true;
                SliceActionView.SliceActionLoadingListener sliceActionLoadingListener = this.mLoadingListener;
                if (sliceActionLoadingListener != null) {
                    sliceActionLoadingListener.onSliceActionLoading(this.mRowAction.getSliceItem(), this.mRowIndex);
                    this.mLoadingActions.add(this.mRowAction.getSliceItem());
                }
                updateActionSpinner();
            }
        } catch (PendingIntent.CanceledException e) {
            Log.e("RowView", "PendingIntent for slice cannot be sent", e);
        }
    }

    private void setViewClickable(View view, boolean z) {
        view.setOnClickListener(z ? this : null);
        view.setBackground(z ? SliceViewUtil.getDrawable(getContext(), R.attr.selectableItemBackground) : null);
        view.setClickable(z);
    }

    @Override // androidx.slice.widget.SliceChildView
    public void resetView() {
        this.mRowContent = null;
        this.mLoadingActions.clear();
        resetViewState();
    }

    private void resetViewState() {
        this.mRootView.setVisibility(0);
        setLayoutDirection(2);
        setViewClickable(this.mRootView, false);
        setViewClickable(this.mContent, false);
        this.mStartContainer.removeAllViews();
        this.mEndContainer.removeAllViews();
        this.mEndContainer.setVisibility(8);
        this.mPrimaryText.setText((CharSequence) null);
        this.mSecondaryText.setText((CharSequence) null);
        this.mLastUpdatedText.setText((CharSequence) null);
        this.mLastUpdatedText.setVisibility(8);
        this.mToggles.clear();
        this.mActions.clear();
        this.mRowAction = null;
        this.mBottomDivider.setVisibility(8);
        this.mActionDivider.setVisibility(8);
        View view = this.mSeeMoreView;
        if (view != null) {
            this.mRootView.removeView(view);
            this.mSeeMoreView = null;
        }
        this.mIsRangeSliding = false;
        this.mRangeHasPendingUpdate = false;
        this.mRangeItem = null;
        this.mRangeMinValue = 0;
        this.mRangeMaxValue = 0;
        this.mRangeValue = 0;
        this.mLastSentRangeUpdate = 0L;
        this.mHandler = null;
        ProgressBar progressBar = this.mRangeBar;
        if (progressBar != null) {
            if (this.mStartItem == null) {
                removeView(progressBar);
            } else {
                this.mContent.removeView(progressBar);
            }
            this.mRangeBar = null;
        }
        this.mSubContent.setVisibility(0);
        this.mStartItem = null;
        this.mActionSpinner.setVisibility(8);
        Spinner spinner = this.mSelectionSpinner;
        if (spinner != null) {
            removeView(spinner);
            this.mSelectionSpinner = null;
        }
        this.mSelectionItem = null;
    }
}
