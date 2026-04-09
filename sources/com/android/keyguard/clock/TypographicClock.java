package com.android.keyguard.clock;

import android.content.Context;
import android.content.res.Resources;
import android.text.Annotation;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.systemui.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/* loaded from: classes.dex */
public class TypographicClock extends TextView {
    private int mAccentColor;
    private String mDescFormat;
    private final String[] mHours;
    private final String[] mMinutes;
    private final Resources mResources;
    private final Calendar mTime;
    private TimeZone mTimeZone;

    @Override // android.widget.TextView, android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public TypographicClock(Context context) {
        this(context, null);
    }

    public TypographicClock(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public TypographicClock(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mTime = Calendar.getInstance(TimeZone.getDefault());
        this.mDescFormat = ((SimpleDateFormat) DateFormat.getTimeFormat(context)).toLocalizedPattern();
        Resources resources = context.getResources();
        this.mResources = resources;
        this.mHours = resources.getStringArray(R.array.type_clock_hours);
        this.mMinutes = resources.getStringArray(R.array.type_clock_minutes);
        this.mAccentColor = resources.getColor(R.color.typeClockAccentColor, null);
    }

    public void onTimeChanged() {
        this.mTime.setTimeInMillis(System.currentTimeMillis());
        setContentDescription(DateFormat.format(this.mDescFormat, this.mTime));
        int i = this.mTime.get(10) % 12;
        int i2 = this.mTime.get(12) % 60;
        SpannedString spannedString = (SpannedString) this.mResources.getQuantityText(R.plurals.type_clock_header, i);
        Annotation[] annotationArr = (Annotation[]) spannedString.getSpans(0, spannedString.length(), Annotation.class);
        SpannableString spannableString = new SpannableString(spannedString);
        for (Annotation annotation : annotationArr) {
            if ("color".equals(annotation.getValue())) {
                spannableString.setSpan(new ForegroundColorSpan(this.mAccentColor), spannableString.getSpanStart(annotation), spannableString.getSpanEnd(annotation), 33);
            }
        }
        setText(TextUtils.expandTemplate(spannableString, this.mHours[i], this.mMinutes[i2]));
    }

    public void onTimeZoneChanged(TimeZone timeZone) {
        this.mTimeZone = timeZone;
        this.mTime.setTimeZone(timeZone);
    }

    public void setClockColor(int i) {
        this.mAccentColor = i;
        onTimeChanged();
    }

    @Override // android.widget.TextView, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Calendar calendar = this.mTime;
        TimeZone timeZone = this.mTimeZone;
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }
        calendar.setTimeZone(timeZone);
        onTimeChanged();
    }
}
