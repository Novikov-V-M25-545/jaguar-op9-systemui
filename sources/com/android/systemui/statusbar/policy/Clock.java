package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.settingslib.Utils;
import com.android.systemui.DemoMode;
import com.android.systemui.Dependency;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.R;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.tuner.TunerService;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import libcore.icu.LocaleData;

/* loaded from: classes.dex */
public class Clock extends TextView implements DemoMode, TunerService.Tunable, CommandQueue.Callbacks, DarkIconDispatcher.DarkReceiver, ConfigurationController.ConfigurationListener {
    private Handler autoHideHandler;
    private int mAmPmStyle;
    private boolean mAttached;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private Calendar mCalendar;
    private boolean mClockAutoHide;
    private int mClockDateDisplay;
    private String mClockDateFormat;
    private int mClockDatePosition;
    private int mClockDateStyle;
    private SimpleDateFormat mClockFormat;
    private String mClockFormatString;
    private int mClockSize;
    private int mClockSizeQsHeader;
    private boolean mClockVisibleByPolicy;
    private boolean mClockVisibleByUser;
    private final CommandQueue mCommandQueue;
    private SimpleDateFormat mContentDescriptionFormat;
    private int mCurrentUserId;
    private final CurrentUserTracker mCurrentUserTracker;
    private boolean mDemoMode;
    private int mHideDuration;
    private final BroadcastReceiver mIntentReceiver;
    private Locale mLocale;
    private int mNonAdaptedColor;
    private boolean mQsHeader;
    private boolean mScreenOn;
    private final BroadcastReceiver mScreenReceiver;
    private boolean mScreenReceiverRegistered;
    private final Runnable mSecondTick;
    private Handler mSecondsHandler;
    private final boolean mShowDark;
    private int mShowDuration;
    private boolean mShowSeconds;
    private TaskStackListenerImpl mTaskStackListener;
    private boolean mUseWallpaperTextColor;

    public Clock(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public Clock(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mClockVisibleByPolicy = true;
        this.mClockVisibleByUser = getVisibility() == 0;
        this.mTaskStackListener = null;
        this.mScreenOn = true;
        this.autoHideHandler = new Handler();
        this.mAmPmStyle = 2;
        this.mClockDateDisplay = 0;
        this.mClockDateStyle = 0;
        this.mClockDateFormat = null;
        this.mHideDuration = 60;
        this.mShowDuration = 5;
        this.mIntentReceiver = new AnonymousClass2();
        this.mScreenReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.policy.Clock.3
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    if (Clock.this.mSecondsHandler != null) {
                        Clock.this.mSecondsHandler.removeCallbacks(Clock.this.mSecondTick);
                    }
                } else {
                    if (!"android.intent.action.SCREEN_ON".equals(action) || Clock.this.mSecondsHandler == null) {
                        return;
                    }
                    Clock.this.mSecondsHandler.postAtTime(Clock.this.mSecondTick, ((SystemClock.uptimeMillis() / 1000) * 1000) + 1000);
                }
            }
        };
        this.mSecondTick = new Runnable() { // from class: com.android.systemui.statusbar.policy.Clock.4
            @Override // java.lang.Runnable
            public void run() {
                if (Clock.this.mCalendar != null) {
                    Clock.this.updateClock();
                }
                Clock.this.mSecondsHandler.postAtTime(this, ((SystemClock.uptimeMillis() / 1000) * 1000) + 1000);
            }
        };
        this.mCommandQueue = (CommandQueue) Dependency.get(CommandQueue.class);
        TypedArray typedArrayObtainStyledAttributes = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.Clock, 0, 0);
        try {
            this.mAmPmStyle = typedArrayObtainStyledAttributes.getInt(R.styleable.Clock_amPmStyle, this.mAmPmStyle);
            this.mShowDark = typedArrayObtainStyledAttributes.getBoolean(R.styleable.Clock_showDark, true);
            this.mNonAdaptedColor = getCurrentTextColor();
            typedArrayObtainStyledAttributes.recycle();
            BroadcastDispatcher broadcastDispatcher = (BroadcastDispatcher) Dependency.get(BroadcastDispatcher.class);
            this.mBroadcastDispatcher = broadcastDispatcher;
            this.mCurrentUserTracker = new CurrentUserTracker(broadcastDispatcher) { // from class: com.android.systemui.statusbar.policy.Clock.1
                @Override // com.android.systemui.settings.CurrentUserTracker
                public void onUserSwitched(int i2) {
                    Clock.this.mCurrentUserId = i2;
                }
            };
        } catch (Throwable th) {
            typedArrayObtainStyledAttributes.recycle();
            throw th;
        }
    }

    @Override // android.widget.TextView, android.view.View
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("clock_super_parcelable", super.onSaveInstanceState());
        bundle.putInt("current_user_id", this.mCurrentUserId);
        bundle.putBoolean("visible_by_policy", this.mClockVisibleByPolicy);
        bundle.putBoolean("visible_by_user", this.mClockVisibleByUser);
        bundle.putBoolean("show_seconds", this.mShowSeconds);
        bundle.putInt("visibility", getVisibility());
        bundle.putBoolean("qsheader", this.mQsHeader);
        return bundle;
    }

    @Override // android.widget.TextView, android.view.View
    public void onRestoreInstanceState(Parcelable parcelable) {
        if (parcelable == null || !(parcelable instanceof Bundle)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        Bundle bundle = (Bundle) parcelable;
        super.onRestoreInstanceState(bundle.getParcelable("clock_super_parcelable"));
        if (bundle.containsKey("current_user_id")) {
            this.mCurrentUserId = bundle.getInt("current_user_id");
        }
        this.mClockVisibleByPolicy = bundle.getBoolean("visible_by_policy", true);
        this.mClockVisibleByUser = bundle.getBoolean("visible_by_user", true);
        this.mShowSeconds = bundle.getBoolean("show_seconds", false);
        if (bundle.containsKey("visibility")) {
            super.setVisibility(bundle.getInt("visibility"));
        }
        this.mQsHeader = bundle.getBoolean("qsheader", false);
    }

    @Override // android.widget.TextView, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!this.mAttached) {
            this.mAttached = true;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.TIME_TICK");
            intentFilter.addAction("android.intent.action.TIME_SET");
            intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
            intentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
            intentFilter.addAction("android.intent.action.USER_SWITCHED");
            intentFilter.addAction("android.intent.action.SCREEN_ON");
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            this.mBroadcastDispatcher.registerReceiverWithHandler(this.mIntentReceiver, intentFilter, (Handler) Dependency.get(Dependency.TIME_TICK_HANDLER), UserHandle.ALL);
            ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "system:status_bar_clock_seconds", "lineagesystem:status_bar_am_pm", "system:status_bar_clock_date_display", "system:status_bar_clock_date_style", "system:status_bar_clock_date_position", "system:status_bar_clock_date_format", "system:status_bar_clock_auto_hide", "system:status_bar_clock_auto_hide_hduration", "system:status_bar_clock_auto_hide_sduration", "system:status_bar_clock_size", "system:qs_header_clock_size");
            this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
            if (this.mShowDark) {
                ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).addDarkReceiver(this);
            }
            this.mCurrentUserTracker.startTracking();
            this.mCurrentUserId = this.mCurrentUserTracker.getCurrentUserId();
        }
        this.mCalendar = Calendar.getInstance(TimeZone.getDefault());
        this.mClockFormatString = "";
        this.mClockFormatString = "";
        updateClock();
        lambda$autoHideClock$1();
        updateShowSeconds();
        updateClockSize();
        lambda$autoHideClock$1();
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mScreenReceiverRegistered) {
            this.mScreenReceiverRegistered = false;
            this.mBroadcastDispatcher.unregisterReceiver(this.mScreenReceiver);
            Handler handler = this.mSecondsHandler;
            if (handler != null) {
                handler.removeCallbacks(this.mSecondTick);
                this.mSecondsHandler = null;
            }
        }
        if (this.mAttached) {
            this.mBroadcastDispatcher.unregisterReceiver(this.mIntentReceiver);
            this.mAttached = false;
            ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
            this.mCommandQueue.removeCallback((CommandQueue.Callbacks) this);
            if (this.mShowDark) {
                ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).removeDarkReceiver(this);
            }
            this.mCurrentUserTracker.stopTracking();
            handleTaskStackListener(false);
        }
    }

    private void handleTaskStackListener(boolean z) {
        if (z && this.mTaskStackListener == null) {
            this.mTaskStackListener = new TaskStackListenerImpl();
            ActivityManagerWrapper.getInstance().registerTaskStackListener(this.mTaskStackListener);
        } else {
            if (z || this.mTaskStackListener == null) {
                return;
            }
            ActivityManagerWrapper.getInstance().unregisterTaskStackListener(this.mTaskStackListener);
            this.mTaskStackListener = null;
        }
    }

    /* renamed from: com.android.systemui.statusbar.policy.Clock$2, reason: invalid class name */
    class AnonymousClass2 extends BroadcastReceiver {
        AnonymousClass2() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Handler handler = Clock.this.getHandler();
            if (handler == null) {
                return;
            }
            String action = intent.getAction();
            if (action.equals("android.intent.action.TIMEZONE_CHANGED")) {
                final String stringExtra = intent.getStringExtra("time-zone");
                handler.post(new Runnable() { // from class: com.android.systemui.statusbar.policy.Clock$2$$ExternalSyntheticLambda2
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onReceive$0(stringExtra);
                    }
                });
            } else if (action.equals("android.intent.action.CONFIGURATION_CHANGED")) {
                final Locale locale = Clock.this.getResources().getConfiguration().locale;
                handler.post(new Runnable() { // from class: com.android.systemui.statusbar.policy.Clock$2$$ExternalSyntheticLambda3
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onReceive$1(locale);
                    }
                });
            } else if (action.equals("android.intent.action.SCREEN_ON")) {
                Clock.this.mScreenOn = true;
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                Clock.this.mScreenOn = false;
            }
            if (Clock.this.mScreenOn) {
                handler.post(new Runnable() { // from class: com.android.systemui.statusbar.policy.Clock$2$$ExternalSyntheticLambda1
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onReceive$2();
                    }
                });
                if (Clock.this.mClockAutoHide) {
                    Clock.this.autoHideHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.policy.Clock$2$$ExternalSyntheticLambda0
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f$0.lambda$onReceive$3();
                        }
                    });
                }
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onReceive$0(String str) {
            Clock.this.mCalendar = Calendar.getInstance(TimeZone.getTimeZone(str));
            if (Clock.this.mClockFormat != null) {
                Clock.this.mClockFormat.setTimeZone(Clock.this.mCalendar.getTimeZone());
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onReceive$1(Locale locale) {
            if (!locale.equals(Clock.this.mLocale)) {
                Clock.this.mLocale = locale;
            }
            Clock.this.mClockFormatString = "";
            Clock.this.lambda$autoHideClock$1();
            Clock.this.updateShowSeconds();
            Clock.this.updateClock();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onReceive$2() {
            Clock.this.updateClock();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onReceive$3() {
            Clock.this.lambda$autoHideClock$1();
        }
    }

    @Override // android.view.View
    public void setVisibility(int i) {
        if (i != 0 || shouldBeVisible()) {
            super.setVisibility(i);
        }
    }

    public void setQsHeader() {
        this.mQsHeader = true;
    }

    public void setClockVisibleByUser(boolean z) {
        this.mClockVisibleByUser = z;
        lambda$autoHideClock$1();
    }

    public void setClockVisibilityByPolicy(boolean z) {
        this.mClockVisibleByPolicy = z;
        lambda$autoHideClock$1();
    }

    public boolean shouldBeVisible() {
        return !this.mClockAutoHide && this.mClockVisibleByPolicy && this.mClockVisibleByUser;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: updateClockVisibility, reason: merged with bridge method [inline-methods] */
    public void lambda$autoHideClock$1() {
        boolean z = this.mClockVisibleByPolicy && this.mClockVisibleByUser;
        int i = z ? 0 : 8;
        try {
            this.autoHideHandler.removeCallbacksAndMessages(null);
        } catch (NullPointerException unused) {
        }
        setVisibility(i);
        if (!this.mQsHeader && this.mClockAutoHide && z && this.mScreenOn) {
            this.autoHideHandler.postDelayed(new Runnable() { // from class: com.android.systemui.statusbar.policy.Clock$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$updateClockVisibility$0();
                }
            }, this.mShowDuration * 1000);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: autoHideClock, reason: merged with bridge method [inline-methods] */
    public void lambda$updateClockVisibility$0() {
        setVisibility(8);
        this.autoHideHandler.postDelayed(new Runnable() { // from class: com.android.systemui.statusbar.policy.Clock$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$autoHideClock$1();
            }
        }, this.mHideDuration * 1000);
    }

    final void updateClock() {
        Calendar calendar;
        if (this.mDemoMode || (calendar = this.mCalendar) == null) {
            return;
        }
        calendar.setTimeInMillis(System.currentTimeMillis());
        setText(getSmallTime());
        setContentDescription(this.mContentDescriptionFormat.format(this.mCalendar.getTime()));
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        str.hashCode();
        switch (str) {
            case "system:status_bar_clock_auto_hide_hduration":
                this.mHideDuration = TunerService.parseInteger(str2, 60);
                break;
            case "system:status_bar_clock_auto_hide":
                this.mClockAutoHide = TunerService.parseIntegerSwitch(str2, false);
                break;
            case "system:status_bar_clock_auto_hide_sduration":
                this.mShowDuration = TunerService.parseInteger(str2, 5);
                break;
            case "system:status_bar_clock_date_style":
                this.mClockDateStyle = TunerService.parseInteger(str2, 0);
                break;
            case "system:status_bar_clock_date_format":
                this.mClockDateFormat = str2;
                break;
            case "system:status_bar_clock_seconds":
                this.mShowSeconds = TunerService.parseIntegerSwitch(str2, false);
                updateShowSeconds();
                break;
            case "system:status_bar_clock_date_position":
                this.mClockDatePosition = TunerService.parseInteger(str2, 0);
                break;
            case "system:qs_header_clock_size":
                this.mClockSizeQsHeader = TunerService.parseInteger(str2, 14);
                updateClockSize();
                break;
            case "system:status_bar_clock_date_display":
                this.mClockDateDisplay = TunerService.parseInteger(str2, 0);
                break;
            case "lineagesystem:status_bar_am_pm":
                this.mAmPmStyle = TunerService.parseInteger(str2, 2);
                break;
            case "system:status_bar_clock_size":
                this.mClockSize = TunerService.parseInteger(str2, 14);
                updateClockSize();
                break;
        }
        this.mClockFormatString = "";
        updateClock();
        lambda$autoHideClock$1();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void disable(int i, int i2, int i3, boolean z) {
        if (i != getDisplay().getDisplayId()) {
            return;
        }
        boolean z2 = (8388608 & i2) == 0;
        if (z2 != this.mClockVisibleByPolicy) {
            setClockVisibilityByPolicy(z2);
        }
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver
    public void onDarkChanged(Rect rect, float f, int i) {
        int tint = DarkIconDispatcher.getTint(rect, this, i);
        this.mNonAdaptedColor = tint;
        if (this.mUseWallpaperTextColor) {
            return;
        }
        setTextColor(tint);
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        FontSizeUtils.updateFontSize(this, R.dimen.status_bar_clock_size);
        setPaddingRelative(((TextView) this).mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_clock_starting_padding), 0, ((TextView) this).mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_clock_end_padding), 0);
    }

    public void useWallpaperTextColor(boolean z) {
        if (z == this.mUseWallpaperTextColor) {
            return;
        }
        this.mUseWallpaperTextColor = z;
        if (z) {
            setTextColor(Utils.getColorAttr(((TextView) this).mContext, R.attr.wallpaperTextColor));
        } else {
            setTextColor(this.mNonAdaptedColor);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateShowSeconds() {
        if (this.mShowSeconds) {
            if (this.mSecondsHandler != null || getDisplay() == null) {
                return;
            }
            this.mSecondsHandler = new Handler();
            if (getDisplay().getState() == 2) {
                this.mSecondsHandler.postAtTime(this.mSecondTick, ((SystemClock.uptimeMillis() / 1000) * 1000) + 1000);
            }
            this.mScreenReceiverRegistered = true;
            IntentFilter intentFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
            intentFilter.addAction("android.intent.action.SCREEN_ON");
            this.mBroadcastDispatcher.registerReceiver(this.mScreenReceiver, intentFilter);
            return;
        }
        if (this.mSecondsHandler != null) {
            this.mScreenReceiverRegistered = false;
            this.mBroadcastDispatcher.unregisterReceiver(this.mScreenReceiver);
            this.mSecondsHandler.removeCallbacks(this.mSecondTick);
            this.mSecondsHandler = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateShowClock() {
        ActivityManager.RunningTaskInfo runningTask = ActivityManagerWrapper.getInstance().getRunningTask();
        boolean z = (runningTask != null ? runningTask.configuration.windowConfiguration.getActivityType() : 0) == 2;
        if (this.mClockAutoHide != z) {
            this.mClockAutoHide = z;
            lambda$autoHideClock$1();
        }
    }

    private final CharSequence getSmallTime() {
        String str;
        SimpleDateFormat simpleDateFormat;
        String str2;
        String string;
        Context context = getContext();
        boolean zIs24HourFormat = DateFormat.is24HourFormat(context, this.mCurrentUserId);
        LocaleData localeData = LocaleData.get(context.getResources().getConfiguration().locale);
        if (this.mShowSeconds) {
            str = zIs24HourFormat ? localeData.timeFormat_Hms : localeData.timeFormat_hms;
        } else {
            str = zIs24HourFormat ? localeData.timeFormat_Hm : localeData.timeFormat_hm;
        }
        if (!str.equals(this.mClockFormatString)) {
            this.mContentDescriptionFormat = new SimpleDateFormat(str);
            if (this.mAmPmStyle != 0) {
                int i = 0;
                boolean z = false;
                while (true) {
                    if (i >= str.length()) {
                        i = -1;
                        break;
                    }
                    char cCharAt = str.charAt(i);
                    if (cCharAt == '\'') {
                        z = !z;
                    }
                    if (!z && cCharAt == 'a') {
                        break;
                    }
                    i++;
                }
                if (i >= 0) {
                    int i2 = i;
                    while (i2 > 0 && Character.isWhitespace(str.charAt(i2 - 1))) {
                        i2--;
                    }
                    str = str.substring(0, i2) + (char) 61184 + str.substring(i2, i) + "a\uef01" + str.substring(i + 1);
                }
            }
            simpleDateFormat = new SimpleDateFormat(str);
            this.mClockFormat = simpleDateFormat;
            this.mClockFormatString = str;
        } else {
            simpleDateFormat = this.mClockFormat;
        }
        CharSequence charSequence = null;
        String str3 = simpleDateFormat.format(this.mCalendar.getTime());
        if (this.mQsHeader || this.mClockDateDisplay == 0) {
            str2 = str3;
        } else {
            Date date = new Date();
            String str4 = this.mClockDateFormat;
            if (str4 == null || str4.isEmpty()) {
                charSequence = DateFormat.format("EEE", date);
            } else {
                charSequence = DateFormat.format(this.mClockDateFormat, date);
            }
            int i3 = this.mClockDateStyle;
            if (i3 == 1) {
                string = charSequence.toString().toLowerCase();
            } else if (i3 == 2) {
                string = charSequence.toString().toUpperCase();
            } else {
                string = charSequence.toString();
            }
            if (this.mClockDatePosition == 0) {
                str2 = string + " " + str3;
            } else {
                str2 = str3 + " " + string;
            }
        }
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(str2);
        if (this.mClockDateDisplay != 2 && charSequence != null) {
            int length = charSequence.length();
            int length2 = this.mClockDatePosition == 1 ? str3.length() + 1 : 0;
            int i4 = this.mClockDateDisplay;
            if (i4 == 0) {
                spannableStringBuilder.delete(0, length);
            } else if (i4 == 1) {
                spannableStringBuilder.setSpan(new RelativeSizeSpan(0.7f), length2, length + length2, 34);
            }
        }
        if (this.mAmPmStyle != 0) {
            int iIndexOf = str2.indexOf(61184);
            int iIndexOf2 = str2.indexOf(61185);
            if (iIndexOf >= 0 && iIndexOf2 > iIndexOf) {
                int i5 = this.mAmPmStyle;
                if (i5 == 2) {
                    spannableStringBuilder.delete(iIndexOf, iIndexOf2 + 1);
                } else {
                    if (i5 == 1) {
                        spannableStringBuilder.setSpan(new RelativeSizeSpan(0.7f), iIndexOf, iIndexOf2, 34);
                    }
                    spannableStringBuilder.delete(iIndexOf2, iIndexOf2 + 1);
                    spannableStringBuilder.delete(iIndexOf, iIndexOf + 1);
                }
            }
        }
        return spannableStringBuilder;
    }

    @Override // com.android.systemui.DemoMode
    public void dispatchDemoCommand(String str, Bundle bundle) throws NumberFormatException {
        if (!this.mDemoMode && str.equals("enter")) {
            this.mDemoMode = true;
            return;
        }
        if (this.mDemoMode && str.equals("exit")) {
            this.mDemoMode = false;
            updateClock();
            return;
        }
        if (this.mDemoMode && str.equals("clock")) {
            String string = bundle.getString("millis");
            String string2 = bundle.getString("hhmm");
            if (string != null) {
                this.mCalendar.setTimeInMillis(Long.parseLong(string));
            } else if (string2 != null && string2.length() == 4) {
                int i = Integer.parseInt(string2.substring(0, 2));
                int i2 = Integer.parseInt(string2.substring(2));
                if (DateFormat.is24HourFormat(getContext(), this.mCurrentUserId)) {
                    this.mCalendar.set(11, i);
                } else {
                    this.mCalendar.set(10, i);
                }
                this.mCalendar.set(12, i2);
            }
            setText(getSmallTime());
            setContentDescription(this.mContentDescriptionFormat.format(this.mCalendar.getTime()));
        }
    }

    private class TaskStackListenerImpl extends TaskStackChangeListener {
        private TaskStackListenerImpl() {
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskStackChanged() {
            Clock.this.updateShowClock();
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskRemoved(int i) {
            Clock.this.updateShowClock();
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskMovedToFront(int i) {
            Clock.this.updateShowClock();
        }
    }

    public void updateClockSize() {
        if (this.mQsHeader) {
            setTextSize(this.mClockSizeQsHeader);
        } else {
            setTextSize(this.mClockSize);
        }
    }
}
