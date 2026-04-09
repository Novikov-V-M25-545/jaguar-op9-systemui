package com.android.keyguard;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Trace;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.slice.Slice;
import androidx.slice.SliceItem;
import androidx.slice.SliceViewManager;
import androidx.slice.core.SliceQuery;
import androidx.slice.widget.ListContent;
import androidx.slice.widget.RowContent;
import androidx.slice.widget.SliceContent;
import androidx.slice.widget.SliceLiveData;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.graphics.ColorUtils;
import com.android.settingslib.Utils;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.keyguard.KeyguardSliceProvider;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.ClockPlugin;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.wakelock.KeepAwakeAnimationListener;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

/* loaded from: classes.dex */
public class KeyguardSliceView extends LinearLayout implements View.OnClickListener, Observer<Slice>, TunerService.Tunable, ConfigurationController.ConfigurationListener {
    private final ActivityStarter mActivityStarter;
    private final HashMap<View, PendingIntent> mClickActions;
    private ClockPlugin mClockPlugin;
    private final ConfigurationController mConfigurationController;
    private Runnable mContentChangeListener;
    private float mDarkAmount;
    private int mDisplayId;
    private boolean mHasHeader;
    private int mIconSize;
    private int mIconSizeWithHeader;
    private Uri mKeyguardSliceUri;
    private final LayoutTransition mLayoutTransition;
    private LiveData<Slice> mLiveData;
    private Row mRow;
    private final int mRowPadding;
    private float mRowTextSize;
    private final int mRowWithHeaderPadding;
    private float mRowWithHeaderTextSize;
    private Slice mSlice;
    private int mTextColor;

    @VisibleForTesting
    TextView mTitle;
    private final TunerService mTunerService;
    private int mWeatherIconSize;

    public KeyguardSliceView(Context context, AttributeSet attributeSet, ActivityStarter activityStarter, ConfigurationController configurationController, TunerService tunerService, Resources resources) {
        super(context, attributeSet);
        this.mDarkAmount = 0.0f;
        this.mDisplayId = -1;
        this.mTunerService = tunerService;
        this.mClickActions = new HashMap<>();
        this.mRowPadding = resources.getDimensionPixelSize(R.dimen.subtitle_clock_padding);
        this.mRowWithHeaderPadding = resources.getDimensionPixelSize(R.dimen.header_subtitle_padding);
        this.mActivityStarter = activityStarter;
        this.mConfigurationController = configurationController;
        LayoutTransition layoutTransition = new LayoutTransition();
        this.mLayoutTransition = layoutTransition;
        layoutTransition.setStagger(0, 275L);
        layoutTransition.setDuration(2, 550L);
        layoutTransition.setDuration(3, 275L);
        layoutTransition.disableTransitionType(0);
        layoutTransition.disableTransitionType(1);
        layoutTransition.setInterpolator(2, Interpolators.FAST_OUT_SLOW_IN);
        layoutTransition.setInterpolator(3, Interpolators.ALPHA_OUT);
        layoutTransition.setAnimateParentHierarchy(false);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mTitle = (TextView) findViewById(R.id.title);
        this.mRow = (Row) findViewById(R.id.row);
        this.mTextColor = Utils.getColorAttrDefaultColor(((LinearLayout) this).mContext, R.attr.wallpaperTextColor);
        this.mIconSize = (int) ((LinearLayout) this).mContext.getResources().getDimension(R.dimen.widget_icon_size);
        this.mIconSizeWithHeader = (int) ((LinearLayout) this).mContext.getResources().getDimension(R.dimen.header_icon_size);
        this.mRowTextSize = ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(R.dimen.widget_label_font_size);
        this.mRowWithHeaderTextSize = ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(R.dimen.header_row_font_size);
        this.mWeatherIconSize = ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(R.dimen.weather_icon_size);
        this.mTitle.setOnClickListener(this);
        this.mTitle.setBreakStrategy(2);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Display display = getDisplay();
        if (display != null) {
            this.mDisplayId = display.getDisplayId();
        }
        this.mTunerService.addTunable(this, "keyguard_slice_uri");
        if (this.mDisplayId == 0) {
            this.mLiveData.observeForever(this);
        }
        this.mConfigurationController.addCallback(this);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mDisplayId == 0) {
            this.mLiveData.removeObserver(this);
        }
        this.mTunerService.removeTunable(this);
        this.mConfigurationController.removeCallback(this);
    }

    @Override // android.view.View
    public void onVisibilityAggregated(boolean z) {
        super.onVisibilityAggregated(z);
        setLayoutTransition(z ? this.mLayoutTransition : null);
    }

    public boolean hasHeader() {
        return this.mHasHeader;
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void showSlice() {
        Drawable drawableLoadDrawable;
        Trace.beginSection("KeyguardSliceView#showSlice");
        int i = 0;
        if (this.mSlice == null) {
            this.mTitle.setVisibility(8);
            this.mRow.setVisibility(8);
            this.mHasHeader = false;
            Runnable runnable = this.mContentChangeListener;
            if (runnable != null) {
                runnable.run();
            }
            Trace.endSection();
            return;
        }
        this.mClickActions.clear();
        ListContent listContent = new ListContent(getContext(), this.mSlice);
        RowContent header = listContent.getHeader();
        this.mHasHeader = (header == null || header.getSliceItem().hasHint("list_item")) ? false : true;
        ArrayList arrayList = new ArrayList();
        for (int i2 = 0; i2 < listContent.getRowItems().size(); i2++) {
            SliceContent sliceContent = listContent.getRowItems().get(i2);
            if (!"content://com.android.systemui.keyguard/action".equals(sliceContent.getSliceItem().getSlice().getUri().toString())) {
                arrayList.add(sliceContent);
            }
        }
        if (!this.mHasHeader) {
            this.mTitle.setVisibility(8);
        } else {
            this.mTitle.setVisibility(0);
            RowContent header2 = listContent.getHeader();
            SliceItem titleItem = header2.getTitleItem();
            this.mTitle.setText(titleItem != null ? titleItem.getText() : null);
            if (header2.getPrimaryAction() != null && header2.getPrimaryAction().getAction() != null) {
                this.mClickActions.put(this.mTitle, header2.getPrimaryAction().getAction());
            }
        }
        int size = arrayList.size();
        int textColor = getTextColor();
        boolean z = this.mHasHeader;
        this.mRow.setVisibility(size > 0 ? 0 : 8);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mRow.getLayoutParams();
        layoutParams.topMargin = this.mHasHeader ? this.mRowWithHeaderPadding : this.mRowPadding;
        this.mRow.setLayoutParams(layoutParams);
        for (int i3 = z; i3 < size; i3++) {
            RowContent rowContent = (RowContent) arrayList.get(i3);
            SliceItem sliceItem = rowContent.getSliceItem();
            Uri uri = sliceItem.getSlice().getUri();
            boolean zEquals = uri.toString().equals("content://com.android.systemui.keyguard/weather");
            KeyguardSliceTextView keyguardSliceTextView = (KeyguardSliceTextView) this.mRow.findViewWithTag(uri);
            if (keyguardSliceTextView == null) {
                keyguardSliceTextView = new KeyguardSliceTextView(((LinearLayout) this).mContext);
                keyguardSliceTextView.setShouldTintDrawable(!zEquals);
                keyguardSliceTextView.setTextColor(textColor);
                keyguardSliceTextView.setTag(uri);
                this.mRow.addView(keyguardSliceTextView, i3 - (this.mHasHeader ? 1 : 0));
            } else {
                keyguardSliceTextView.setShouldTintDrawable(!zEquals);
            }
            PendingIntent action = rowContent.getPrimaryAction() != null ? rowContent.getPrimaryAction().getAction() : null;
            this.mClickActions.put(keyguardSliceTextView, action);
            SliceItem titleItem2 = rowContent.getTitleItem();
            keyguardSliceTextView.setText(titleItem2 == null ? null : titleItem2.getText());
            keyguardSliceTextView.setContentDescription(rowContent.getContentDescription());
            keyguardSliceTextView.setTextSize(0, this.mHasHeader ? this.mRowWithHeaderTextSize : this.mRowTextSize);
            SliceItem sliceItemFind = SliceQuery.find(sliceItem.getSlice(), "image");
            if (sliceItemFind != null) {
                int i4 = this.mHasHeader ? this.mIconSizeWithHeader : this.mIconSize;
                drawableLoadDrawable = sliceItemFind.getIcon().loadDrawable(((LinearLayout) this).mContext);
                if (drawableLoadDrawable != null) {
                    int iMax = Math.max((int) ((drawableLoadDrawable.getIntrinsicWidth() / drawableLoadDrawable.getIntrinsicHeight()) * (zEquals ? this.mWeatherIconSize : i4)), 1);
                    if (zEquals) {
                        i4 = this.mWeatherIconSize;
                    }
                    drawableLoadDrawable.setBounds(0, 0, iMax, i4);
                }
            } else {
                drawableLoadDrawable = null;
            }
            keyguardSliceTextView.setCompoundDrawables(drawableLoadDrawable, null, null, null);
            keyguardSliceTextView.setOnClickListener(this);
            keyguardSliceTextView.setClickable(action != null);
        }
        while (i < this.mRow.getChildCount()) {
            View childAt = this.mRow.getChildAt(i);
            if (!this.mClickActions.containsKey(childAt)) {
                this.mRow.removeView(childAt);
                i--;
            }
            i++;
        }
        Runnable runnable2 = this.mContentChangeListener;
        if (runnable2 != null) {
            runnable2.run();
        }
        Trace.endSection();
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

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        PendingIntent pendingIntent = this.mClickActions.get(view);
        if (pendingIntent != null) {
            this.mActivityStarter.lambda$postStartActivityDismissingKeyguard$24(pendingIntent);
        }
    }

    public void setContentChangeListener(Runnable runnable) {
        this.mContentChangeListener = runnable;
    }

    @Override // androidx.lifecycle.Observer
    public void onChanged(Slice slice) {
        this.mSlice = slice;
        showSlice();
        ClockPlugin clockPlugin = this.mClockPlugin;
        if (clockPlugin != null) {
            clockPlugin.setSlice(slice);
        }
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        setupUri(str2);
    }

    public void setupUri(String str) {
        if (str == null) {
            str = "content://com.android.systemui.keyguard/main";
        }
        boolean z = false;
        LiveData<Slice> liveData = this.mLiveData;
        if (liveData != null && liveData.hasActiveObservers()) {
            z = true;
            this.mLiveData.removeObserver(this);
        }
        Uri uri = Uri.parse(str);
        this.mKeyguardSliceUri = uri;
        LiveData<Slice> liveDataFromUri = SliceLiveData.fromUri(((LinearLayout) this).mContext, uri);
        this.mLiveData = liveDataFromUri;
        if (z) {
            liveDataFromUri.observeForever(this);
        }
    }

    @VisibleForTesting
    int getTextColor() {
        return ColorUtils.blendARGB(this.mTextColor, -1, this.mDarkAmount);
    }

    @VisibleForTesting
    void setTextColor(int i) {
        this.mTextColor = i;
        updateTextColors();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        this.mIconSize = ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(R.dimen.widget_icon_size);
        this.mIconSizeWithHeader = (int) ((LinearLayout) this).mContext.getResources().getDimension(R.dimen.header_icon_size);
        this.mRowTextSize = ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(R.dimen.widget_label_font_size);
        this.mRowWithHeaderTextSize = ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(R.dimen.header_row_font_size);
        this.mWeatherIconSize = ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(R.dimen.weather_icon_size);
    }

    public void refresh() {
        Slice sliceBindSlice;
        Trace.beginSection("KeyguardSliceView#refresh");
        if ("content://com.android.systemui.keyguard/main".equals(this.mKeyguardSliceUri.toString())) {
            KeyguardSliceProvider attachedInstance = KeyguardSliceProvider.getAttachedInstance();
            if (attachedInstance != null) {
                sliceBindSlice = attachedInstance.onBindSlice(this.mKeyguardSliceUri);
            } else {
                Log.w("KeyguardSliceView", "Keyguard slice not bound yet?");
                sliceBindSlice = null;
            }
        } else {
            sliceBindSlice = SliceViewManager.getInstance(getContext()).bindSlice(this.mKeyguardSliceUri);
        }
        onChanged(sliceBindSlice);
        Trace.endSection();
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        Object objValueOf;
        printWriter.println("KeyguardSliceView:");
        printWriter.println("  mClickActions: " + this.mClickActions);
        StringBuilder sb = new StringBuilder();
        sb.append("  mTitle: ");
        TextView textView = this.mTitle;
        Object objValueOf2 = "null";
        if (textView == null) {
            objValueOf = "null";
        } else {
            objValueOf = Boolean.valueOf(textView.getVisibility() == 0);
        }
        sb.append(objValueOf);
        printWriter.println(sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append("  mRow: ");
        Row row = this.mRow;
        if (row != null) {
            objValueOf2 = Boolean.valueOf(row.getVisibility() == 0);
        }
        sb2.append(objValueOf2);
        printWriter.println(sb2.toString());
        printWriter.println("  mTextColor: " + Integer.toHexString(this.mTextColor));
        printWriter.println("  mDarkAmount: " + this.mDarkAmount);
        printWriter.println("  mSlice: " + this.mSlice);
        printWriter.println("  mHasHeader: " + this.mHasHeader);
    }

    public static class Row extends LinearLayout {
        private float mDarkAmount;
        private final Animation.AnimationListener mKeepAwakeListener;
        private LayoutTransition mLayoutTransition;

        @Override // android.view.View
        public boolean hasOverlappingRendering() {
            return false;
        }

        public Row(Context context) {
            this(context, null);
        }

        public Row(Context context, AttributeSet attributeSet) {
            this(context, attributeSet, 0);
        }

        public Row(Context context, AttributeSet attributeSet, int i) {
            this(context, attributeSet, i, 0);
        }

        public Row(Context context, AttributeSet attributeSet, int i, int i2) {
            super(context, attributeSet, i, i2);
            this.mKeepAwakeListener = new KeepAwakeAnimationListener(((LinearLayout) this).mContext);
        }

        @Override // android.view.View
        protected void onFinishInflate() {
            LayoutTransition layoutTransition = new LayoutTransition();
            this.mLayoutTransition = layoutTransition;
            layoutTransition.setDuration(550L);
            ObjectAnimator objectAnimatorOfPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(null, PropertyValuesHolder.ofInt("left", 0, 1), PropertyValuesHolder.ofInt("right", 0, 1));
            this.mLayoutTransition.setAnimator(0, objectAnimatorOfPropertyValuesHolder);
            this.mLayoutTransition.setAnimator(1, objectAnimatorOfPropertyValuesHolder);
            LayoutTransition layoutTransition2 = this.mLayoutTransition;
            Interpolator interpolator = Interpolators.ACCELERATE_DECELERATE;
            layoutTransition2.setInterpolator(0, interpolator);
            this.mLayoutTransition.setInterpolator(1, interpolator);
            this.mLayoutTransition.setStartDelay(0, 550L);
            this.mLayoutTransition.setStartDelay(1, 550L);
            this.mLayoutTransition.setAnimator(2, ObjectAnimator.ofFloat((Object) null, "alpha", 0.0f, 1.0f));
            this.mLayoutTransition.setInterpolator(2, Interpolators.ALPHA_IN);
            ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat((Object) null, "alpha", 1.0f, 0.0f);
            this.mLayoutTransition.setInterpolator(3, Interpolators.ALPHA_OUT);
            this.mLayoutTransition.setDuration(3, 137L);
            this.mLayoutTransition.setAnimator(3, objectAnimatorOfFloat);
            this.mLayoutTransition.setAnimateParentHierarchy(false);
        }

        @Override // android.view.View
        public void onVisibilityAggregated(boolean z) {
            super.onVisibilityAggregated(z);
            setLayoutTransition(z ? this.mLayoutTransition : null);
        }

        @Override // android.widget.LinearLayout, android.view.View
        protected void onMeasure(int i, int i2) {
            int size = View.MeasureSpec.getSize(i);
            int childCount = getChildCount();
            for (int i3 = 0; i3 < childCount; i3++) {
                View childAt = getChildAt(i3);
                if (childAt instanceof KeyguardSliceTextView) {
                    ((KeyguardSliceTextView) childAt).setMaxWidth(size / 3);
                }
            }
            super.onMeasure(i, i2);
        }

        public void setDarkAmount(float f) {
            boolean z = f != 0.0f;
            if (z == (this.mDarkAmount != 0.0f)) {
                return;
            }
            this.mDarkAmount = f;
            setLayoutAnimationListener(z ? null : this.mKeepAwakeListener);
        }
    }

    @VisibleForTesting
    public static class KeyguardSliceTextView extends TextView implements ConfigurationController.ConfigurationListener {
        private static int sStyleId = R.style.TextAppearance_Keyguard_Secondary;
        private boolean shouldTintDrawable;

        public KeyguardSliceTextView(Context context) {
            super(context, null, 0, sStyleId);
            this.shouldTintDrawable = true;
            onDensityOrFontScaleChanged();
            setEllipsize(TextUtils.TruncateAt.END);
        }

        public void setShouldTintDrawable(boolean z) {
            this.shouldTintDrawable = z;
        }

        @Override // android.widget.TextView, android.view.View
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
        }

        @Override // android.view.View
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
        }

        @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
        public void onDensityOrFontScaleChanged() {
            updatePadding();
        }

        @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
        public void onOverlayChanged() {
            setTextAppearance(sStyleId);
        }

        @Override // android.widget.TextView
        public void setText(CharSequence charSequence, TextView.BufferType bufferType) {
            super.setText(charSequence, bufferType);
            updatePadding();
        }

        private void updatePadding() {
            boolean z = !TextUtils.isEmpty(getText());
            boolean zEquals = Uri.parse("content://com.android.systemui.keyguard/date").equals(getTag());
            int dimension = ((int) getContext().getResources().getDimension(R.dimen.widget_horizontal_padding)) / 2;
            int dimension2 = (int) ((TextView) this).mContext.getResources().getDimension(R.dimen.widget_icon_padding);
            setPadding(!zEquals ? dimension2 : dimension, 0, dimension * (z ? 1 : -1), 0);
            setCompoundDrawablePadding(dimension2);
        }

        @Override // android.widget.TextView
        public void setTextColor(int i) {
            super.setTextColor(i);
            updateDrawableColors();
        }

        @Override // android.widget.TextView
        public void setCompoundDrawables(Drawable drawable, Drawable drawable2, Drawable drawable3, Drawable drawable4) {
            super.setCompoundDrawables(drawable, drawable2, drawable3, drawable4);
            updateDrawableColors();
            updatePadding();
        }

        private void updateDrawableColors() {
            if (this.shouldTintDrawable) {
                int currentTextColor = getCurrentTextColor();
                for (Drawable drawable : getCompoundDrawables()) {
                    if (drawable != null) {
                        drawable.setTint(currentTextColor);
                    }
                }
            }
        }
    }
}
