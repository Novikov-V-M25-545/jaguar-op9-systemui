package com.android.settingslib.utils;

import android.content.Context;
import android.icu.text.DateFormat;
import android.icu.text.MeasureFormat;
import android.icu.util.Measure;
import android.icu.util.MeasureUnit;
import android.text.TextUtils;
import com.android.settingslib.R$string;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/* loaded from: classes.dex */
public class PowerUtil {
    private static final long FIFTEEN_MINUTES_MILLIS;
    private static final long ONE_DAY_MILLIS;
    private static final long ONE_HOUR_MILLIS;
    private static final long SEVEN_MINUTES_MILLIS;
    private static final long TWO_DAYS_MILLIS;

    public static long convertMsToUs(long j) {
        return j * 1000;
    }

    static {
        TimeUnit timeUnit = TimeUnit.MINUTES;
        SEVEN_MINUTES_MILLIS = timeUnit.toMillis(7L);
        FIFTEEN_MINUTES_MILLIS = timeUnit.toMillis(15L);
        TimeUnit timeUnit2 = TimeUnit.DAYS;
        ONE_DAY_MILLIS = timeUnit2.toMillis(1L);
        TWO_DAYS_MILLIS = timeUnit2.toMillis(2L);
        ONE_HOUR_MILLIS = TimeUnit.HOURS.toMillis(1L);
    }

    public static String getBatteryRemainingStringFormatted(Context context, long j, String str, boolean z) {
        if (j <= 0) {
            return null;
        }
        if (j <= SEVEN_MINUTES_MILLIS) {
            return getShutdownImminentString(context, str);
        }
        long j2 = FIFTEEN_MINUTES_MILLIS;
        if (j <= j2) {
            return getUnderFifteenString(context, StringUtil.formatElapsedTime(context, j2, false), str);
        }
        if (j >= TWO_DAYS_MILLIS) {
            return getMoreThanTwoDaysString(context, str);
        }
        if (j >= ONE_DAY_MILLIS) {
            return getMoreThanOneDayString(context, j, str, z);
        }
        return getRegularTimeRemainingString(context, j, str, z);
    }

    public static String getBatteryRemainingShortStringFormatted(Context context, long j) {
        if (j <= 0) {
            return null;
        }
        if (j <= ONE_DAY_MILLIS) {
            return getRegularTimeRemainingShortString(context, j);
        }
        return getMoreThanOneDayShortString(context, j, R$string.power_remaining_duration_only_short);
    }

    private static String getShutdownImminentString(Context context, String str) {
        return TextUtils.isEmpty(str) ? context.getString(R$string.power_remaining_duration_only_shutdown_imminent) : context.getString(R$string.power_remaining_duration_shutdown_imminent, str);
    }

    private static String getUnderFifteenString(Context context, CharSequence charSequence, String str) {
        return TextUtils.isEmpty(str) ? context.getString(R$string.power_remaining_less_than_duration_only, charSequence) : context.getString(R$string.power_remaining_less_than_duration, charSequence, str);
    }

    private static String getMoreThanOneDayString(Context context, long j, String str, boolean z) {
        int i;
        int i2;
        CharSequence elapsedTime = StringUtil.formatElapsedTime(context, roundTimeToNearestThreshold(j, ONE_HOUR_MILLIS), false);
        if (TextUtils.isEmpty(str)) {
            if (z) {
                i2 = R$string.power_remaining_duration_only_enhanced;
            } else {
                i2 = R$string.power_remaining_duration_only;
            }
            return context.getString(i2, elapsedTime);
        }
        if (z) {
            i = R$string.power_discharging_duration_enhanced;
        } else {
            i = R$string.power_discharging_duration;
        }
        return context.getString(i, elapsedTime, str);
    }

    private static String getMoreThanOneDayShortString(Context context, long j, int i) {
        return context.getString(i, StringUtil.formatElapsedTime(context, roundTimeToNearestThreshold(j, ONE_HOUR_MILLIS), false));
    }

    private static String getMoreThanTwoDaysString(Context context, String str) {
        MeasureFormat measureFormat = MeasureFormat.getInstance(context.getResources().getConfiguration().getLocales().get(0), MeasureFormat.FormatWidth.SHORT);
        Measure measure = new Measure(2, MeasureUnit.DAY);
        if (TextUtils.isEmpty(str)) {
            return context.getString(R$string.power_remaining_only_more_than_subtext, measureFormat.formatMeasures(measure));
        }
        return context.getString(R$string.power_remaining_more_than_subtext, measureFormat.formatMeasures(measure), str);
    }

    private static String getRegularTimeRemainingString(Context context, long j, String str, boolean z) {
        int i;
        int i2;
        CharSequence dateTimeStringFromMs = getDateTimeStringFromMs(context, j);
        if (TextUtils.isEmpty(str)) {
            if (z) {
                i2 = R$string.power_discharge_by_only_enhanced;
            } else {
                i2 = R$string.power_discharge_by_only;
            }
            return context.getString(i2, dateTimeStringFromMs);
        }
        if (z) {
            i = R$string.power_discharge_by_enhanced;
        } else {
            i = R$string.power_discharge_by;
        }
        return context.getString(i, dateTimeStringFromMs, str);
    }

    private static CharSequence getDateTimeStringFromMs(Context context, long j) {
        return DateFormat.getInstanceForSkeleton(android.text.format.DateFormat.getTimeFormatString(context)).format(Date.from(Instant.ofEpochMilli(roundTimeToNearestThreshold(System.currentTimeMillis() + j, FIFTEEN_MINUTES_MILLIS))));
    }

    private static String getRegularTimeRemainingShortString(Context context, long j) {
        return context.getString(R$string.power_discharge_by_only_short, DateFormat.getInstanceForSkeleton(android.text.format.DateFormat.getTimeFormatString(context)).format(Date.from(Instant.ofEpochMilli(roundTimeToNearestThreshold(System.currentTimeMillis() + j, FIFTEEN_MINUTES_MILLIS)))));
    }

    public static long convertUsToMs(long j) {
        return j / 1000;
    }

    public static long roundTimeToNearestThreshold(long j, long j2) {
        long jAbs = Math.abs(j);
        long jAbs2 = Math.abs(j2);
        long j3 = jAbs % jAbs2;
        return j3 < jAbs2 / 2 ? jAbs - j3 : (jAbs - j3) + jAbs2;
    }
}
