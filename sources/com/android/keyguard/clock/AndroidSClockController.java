package com.android.keyguard.clock;

import android.app.PendingIntent;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.slice.Slice;
import androidx.slice.SliceItem;
import androidx.slice.core.SliceQuery;
import androidx.slice.widget.ListContent;
import androidx.slice.widget.RowContent;
import androidx.slice.widget.SliceContent;
import com.android.internal.graphics.ColorUtils;
import com.android.internal.util.Converter;
import com.android.keyguard.KeyguardSliceView;
import com.android.settingslib.Utils;
import com.android.systemui.R;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.plugins.ClockPlugin;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

/* loaded from: classes.dex */
public class AndroidSClockController implements ClockPlugin {
    private TextClock mClock;
    private final SysuiColorExtractor mColorExtractor;
    private ConstraintLayout mContainer;
    private ConstraintLayout mContainerBig;
    private Context mContext;
    private boolean mHasHeader;
    private int mIconSize;
    private int mIconSizeWithHeader;
    private final LayoutInflater mLayoutInflater;
    private final Resources mResources;
    private KeyguardSliceView.Row mRow;
    private final int mRowPadding;
    private float mRowTextSize;
    private final int mRowWithHeaderPadding;
    private float mRowWithHeaderTextSize;
    private Slice mSlice;
    private Typeface mSliceTypeface;
    private int mTextColor;
    private TextView mTitle;
    private ClockLayout mView;
    private final float mTextSizeNormal = 38.0f;
    private final float mTextSizeBig = 68.0f;
    private final float mSliceTextSize = 24.0f;
    private final float mTitleTextSize = 28.0f;
    private boolean mHasVisibleNotification = false;
    private boolean mClockState = false;
    private float clockDividY = 6.0f;
    private final ViewPreviewer mRenderer = new ViewPreviewer();
    private ConstraintSet mContainerSet = new ConstraintSet();
    private ConstraintSet mContainerSetBig = new ConstraintSet();
    private float mDarkAmount = 0.0f;
    private int mRowHeight = 0;
    private final Calendar mTime = Calendar.getInstance(TimeZone.getDefault());
    private final HashMap<View, PendingIntent> mClickActions = new HashMap<>();

    @Override // com.android.systemui.plugins.ClockPlugin
    public View getBigClockView() {
        return null;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public String getName() {
        return "android_s";
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onTimeZoneChanged(TimeZone timeZone) {
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setColorPalette(boolean z, int[] iArr) {
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setHasVisibleNotifications(boolean z) {
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setStyle(Paint.Style style) {
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public boolean shouldShowStatusArea() {
        return false;
    }

    public AndroidSClockController(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        this.mResources = resources;
        this.mLayoutInflater = layoutInflater;
        this.mContext = layoutInflater.getContext();
        this.mColorExtractor = sysuiColorExtractor;
        this.mRowPadding = resources.getDimensionPixelSize(R.dimen.subtitle_clock_padding);
        this.mRowWithHeaderPadding = resources.getDimensionPixelSize(R.dimen.header_subtitle_padding);
    }

    private void createViews() {
        this.mView = (ClockLayout) this.mLayoutInflater.inflate(R.layout.android_s_clock, (ViewGroup) null);
        ClockLayout clockLayout = (ClockLayout) this.mLayoutInflater.inflate(R.layout.android_s_big_clock, (ViewGroup) null);
        this.mClock = (TextClock) this.mView.findViewById(R.id.clock);
        ClockLayout clockLayout2 = this.mView;
        int i = R.id.clock_view;
        this.mContainer = (ConstraintLayout) clockLayout2.findViewById(i);
        this.mContainerBig = (ConstraintLayout) clockLayout.findViewById(i);
        this.mContainerSet.clone(this.mContainer);
        this.mContainerSetBig.clone(this.mContainerBig);
        this.mClock.setFormat12Hour("hh\nmm");
        this.mClock.setFormat24Hour("kk\nmm");
        TextClock textClock = this.mClock;
        Resources resources = this.mContext.getResources();
        int i2 = R.dimen.widget_label_font_size;
        textClock.setTextSize(resources.getDimensionPixelSize(i2) * 2.5f);
        this.mTitle = (TextView) this.mView.findViewById(R.id.title);
        this.mRow = (KeyguardSliceView.Row) this.mView.findViewById(R.id.row);
        this.mIconSize = (int) this.mContext.getResources().getDimension(R.dimen.widget_icon_size);
        this.mIconSizeWithHeader = (int) this.mContext.getResources().getDimension(R.dimen.header_icon_size);
        this.mRowTextSize = this.mContext.getResources().getDimensionPixelSize(i2);
        this.mRowWithHeaderTextSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.header_row_font_size);
        this.mTextColor = Utils.getColorAttrDefaultColor(this.mContext, R.attr.wallpaperTextColor);
        this.mSliceTypeface = this.mClock.getTypeface();
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onDestroyView() {
        this.mView = null;
        this.mClock = null;
        this.mContainer = null;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public String getTitle() {
        return this.mResources.getString(R.string.clock_title_android_s);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public Bitmap getThumbnail() {
        return BitmapFactory.decodeResource(this.mResources, R.drawable.android_s_clock);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public Bitmap getPreview(int i, int i2) {
        View viewInflate = this.mLayoutInflater.inflate(R.layout.android_s_clock_preview, (ViewGroup) null);
        TextClock textClock = (TextClock) viewInflate.findViewById(R.id.clock);
        textClock.setFormat12Hour("hh\nmm");
        textClock.setFormat24Hour("kk\nmm");
        return this.mRenderer.createPreview(viewInflate, i, i2);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public View getView() {
        if (this.mView == null) {
            createViews();
        }
        return this.mView;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public int getPreferredY(int i) {
        return (int) (i / this.clockDividY);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setTextColor(int i) {
        this.mClock.setTextColor(i);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r4v4, types: [java.util.ArrayList, java.util.List] */
    /* JADX WARN: Type inference failed for: r8v0, types: [boolean] */
    /* JADX WARN: Type inference failed for: r8v1, types: [int] */
    /* JADX WARN: Type inference failed for: r8v2, types: [int] */
    @Override // com.android.systemui.plugins.ClockPlugin
    public void setSlice(Slice slice) {
        Drawable drawableLoadDrawable;
        this.mSlice = slice;
        if (slice == null) {
            this.mRow.setVisibility(8);
            this.mHasHeader = false;
            return;
        }
        this.mClickActions.clear();
        ListContent listContent = new ListContent(this.mContext, this.mSlice);
        RowContent header = listContent.getHeader();
        this.mHasHeader = (header == null || header.getSliceItem().hasHint("list_item")) ? false : true;
        ?? arrayList = new ArrayList();
        for (int i = 0; i < listContent.getRowItems().size(); i++) {
            SliceContent sliceContent = listContent.getRowItems().get(i);
            if (!"content://com.android.systemui.keyguard/action".equals(sliceContent.getSliceItem().getSlice().getUri().toString())) {
                arrayList.add(sliceContent);
            }
        }
        int size = arrayList.size();
        int textColor = getTextColor();
        this.mRow.setVisibility(size > 0 ? 0 : 8);
        if (!this.mHasHeader) {
            this.mTitle.setVisibility(8);
        } else {
            this.mTitle.setVisibility(0);
            RowContent header2 = listContent.getHeader();
            SliceItem titleItem = header2.getTitleItem();
            this.mTitle.setText(titleItem != null ? titleItem.getText() : null);
            this.mTitle.setTextSize(28.0f);
            Typeface typeface = this.mSliceTypeface;
            if (typeface != null) {
                this.mTitle.setTypeface(typeface);
            }
            if (header2.getPrimaryAction() != null && header2.getPrimaryAction().getAction() != null) {
                this.mClickActions.put(this.mTitle, header2.getPrimaryAction().getAction());
            }
        }
        for (?? r8 = this.mHasHeader; r8 < size; r8++) {
            RowContent rowContent = (RowContent) arrayList.get(r8);
            SliceItem sliceItem = rowContent.getSliceItem();
            Uri uri = sliceItem.getSlice().getUri();
            boolean zEquals = uri.toString().equals("content://com.android.systemui.keyguard/date");
            KeyguardSliceView.KeyguardSliceTextView keyguardSliceTextView = (KeyguardSliceView.KeyguardSliceTextView) this.mRow.findViewWithTag(uri);
            if (keyguardSliceTextView == null) {
                keyguardSliceTextView = new KeyguardSliceView.KeyguardSliceTextView(this.mContext);
                keyguardSliceTextView.setTextSize(zEquals ? 28.0f : 24.0f);
                keyguardSliceTextView.setTextColor(textColor);
                keyguardSliceTextView.setGravity(8388611);
                keyguardSliceTextView.setTag(uri);
                this.mRow.addView(keyguardSliceTextView, r8 - (this.mHasHeader ? 1 : 0));
            } else {
                keyguardSliceTextView.setTextSize(zEquals ? 28.0f : 24.0f);
                keyguardSliceTextView.setGravity(8388611);
            }
            Typeface typeface2 = this.mSliceTypeface;
            if (typeface2 != null) {
                keyguardSliceTextView.setTypeface(typeface2);
            }
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) keyguardSliceTextView.getLayoutParams();
            layoutParams.height = -2;
            layoutParams.width = -1;
            layoutParams.gravity = 8388611;
            layoutParams.topMargin = 8;
            layoutParams.bottomMargin = 8;
            keyguardSliceTextView.setLayoutParams(layoutParams);
            this.mClickActions.put(keyguardSliceTextView, rowContent.getPrimaryAction() != null ? rowContent.getPrimaryAction().getAction() : null);
            SliceItem titleItem2 = rowContent.getTitleItem();
            keyguardSliceTextView.setText(titleItem2 == null ? null : titleItem2.getText());
            keyguardSliceTextView.setContentDescription(rowContent.getContentDescription());
            SliceItem sliceItemFind = SliceQuery.find(sliceItem.getSlice(), "image");
            if (sliceItemFind != null) {
                int iDpToPx = Converter.dpToPx(this.mContext, 20);
                drawableLoadDrawable = sliceItemFind.getIcon().loadDrawable(this.mContext);
                if (drawableLoadDrawable != null) {
                    drawableLoadDrawable.setBounds(0, 0, Math.max((int) ((drawableLoadDrawable.getIntrinsicWidth() / drawableLoadDrawable.getIntrinsicHeight()) * iDpToPx), 1), iDpToPx);
                }
            } else {
                drawableLoadDrawable = null;
            }
            keyguardSliceTextView.setCompoundDrawables(drawableLoadDrawable, null, null, null);
        }
        int i2 = 0;
        while (i2 < this.mRow.getChildCount()) {
            View childAt = this.mRow.getChildAt(i2);
            if (!this.mClickActions.containsKey(childAt)) {
                this.mRow.removeView(childAt);
                i2--;
            }
            i2++;
        }
        this.mTitle.requestLayout();
        this.mRow.requestLayout();
        if (this.mClock != null) {
            this.mRowHeight = this.mRow.getHeight() + (this.mHasHeader ? this.mTitle.getHeight() : 0);
            if (this.mRow.getChildCount() != 0) {
                this.mContainerSetBig.setMargin(this.mClock.getId(), 3, this.mRowHeight);
            }
        }
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onTimeTick() {
        this.mView.onTimeChanged();
        this.mClock.refreshTime();
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setDarkAmount(float f) {
        this.mView.setDarkAmount(f);
        int i = 0;
        while (true) {
            float f2 = 28.0f;
            if (i < this.mRow.getChildCount()) {
                KeyguardSliceView.KeyguardSliceTextView keyguardSliceTextView = (KeyguardSliceView.KeyguardSliceTextView) this.mRow.getChildAt(i);
                if (!keyguardSliceTextView.getTag().toString().equals("content://com.android.systemui.keyguard/date")) {
                    f2 = 24.0f;
                }
                keyguardSliceTextView.setTextSize(f2 + (8.0f * f));
                i++;
            } else {
                this.mTitle.setTextSize((8.0f * f) + 28.0f);
                this.mRow.setDarkAmount(f);
                this.mTitle.requestLayout();
                this.mRow.requestLayout();
                this.mDarkAmount = f;
                updateTextColors();
                return;
            }
        }
    }

    private void updateTextColors() {
        int textColor = getTextColor();
        this.mTitle.setTextColor(textColor);
        int childCount = this.mRow.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = this.mRow.getChildAt(i);
            if (childAt instanceof TextView) {
                ((TextView) childAt).setTextColor(textColor);
            }
        }
    }

    int getTextColor() {
        return ColorUtils.blendARGB(this.mTextColor, -1, this.mDarkAmount);
    }
}
