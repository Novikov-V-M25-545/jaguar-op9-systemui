package com.android.keyguard;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.app.IStopUserCallback;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.graphics.ColorUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.omni.CurrentWeatherView;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.tuner.TunerService;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.TimeZone;

/* loaded from: classes.dex */
public class KeyguardStatusView extends GridLayout implements ConfigurationController.ConfigurationListener, TunerService.Tunable {
    private KeyguardClockSwitch mClockView;
    private float mDarkAmount;
    private Handler mHandler;
    private final IActivityManager mIActivityManager;
    private int mIconTopMargin;
    private int mIconTopMarginWithHeader;
    private KeyguardUpdateMonitorCallback mInfoCallback;
    private KeyguardSliceView mKeyguardSlice;
    private final LockPatternUtils mLockPatternUtils;
    private TextView mLogoutView;
    private View mNotificationIcons;
    private TextView mOwnerInfo;
    private Runnable mPendingMarqueeStart;
    private boolean mPixelStyle;
    private boolean mPulsing;
    private boolean mShowWeather;
    private boolean mShowingHeader;
    private LinearLayout mStatusViewContainer;
    private int mTextColor;
    private CurrentWeatherView mWeatherView;

    public KeyguardStatusView(Context context) {
        this(context, null, 0);
    }

    public KeyguardStatusView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public KeyguardStatusView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mDarkAmount = 0.0f;
        this.mInfoCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.keyguard.KeyguardStatusView.1
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onTimeChanged() {
                KeyguardStatusView.this.refreshTime();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onTimeZoneChanged(TimeZone timeZone) {
                KeyguardStatusView.this.updateTimeZone(timeZone);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onKeyguardVisibilityChanged(boolean z) {
                if (z) {
                    KeyguardStatusView.this.refreshTime();
                    KeyguardStatusView.this.updateOwnerInfo();
                    KeyguardStatusView.this.updateLogoutView();
                    KeyguardStatusView.this.updateWeatherView();
                    KeyguardStatusView.this.mClockView.updateClockColor();
                    KeyguardStatusView.this.updateClockDateColor();
                    KeyguardStatusView.this.updateOwnerInfoColor();
                }
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onStartedWakingUp() {
                KeyguardStatusView.this.setEnableMarquee(true);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onFinishedGoingToSleep(int i2) {
                KeyguardStatusView.this.setEnableMarquee(false);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onUserSwitchComplete(int i2) throws Resources.NotFoundException {
                KeyguardStatusView.this.refreshFormat();
                KeyguardStatusView.this.updateOwnerInfo();
                KeyguardStatusView.this.updateLogoutView();
                KeyguardStatusView.this.updateWeatherView();
                KeyguardStatusView.this.mClockView.updateClockColor();
                KeyguardStatusView.this.updateClockDateColor();
                KeyguardStatusView.this.updateOwnerInfoColor();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onLogoutEnabledChanged() {
                KeyguardStatusView.this.updateLogoutView();
            }
        };
        this.mIActivityManager = ActivityManager.getService();
        this.mLockPatternUtils = new LockPatternUtils(getContext());
        this.mHandler = new Handler();
        TunerService tunerService = (TunerService) Dependency.get(TunerService.class);
        tunerService.addTunable(this, "system:lockscreen_weather_enabled");
        tunerService.addTunable(this, "system:lockscreen_weather_style");
        onDensityOrFontScaleChanged();
    }

    public boolean hasCustomClock() {
        return this.mClockView.hasCustomClock();
    }

    public void setHasVisibleNotifications(boolean z) {
        this.mClockView.setHasVisibleNotifications(z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setEnableMarquee(boolean z) {
        if (z) {
            if (this.mPendingMarqueeStart == null) {
                Runnable runnable = new Runnable() { // from class: com.android.keyguard.KeyguardStatusView$$ExternalSyntheticLambda2
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$setEnableMarquee$0();
                    }
                };
                this.mPendingMarqueeStart = runnable;
                this.mHandler.postDelayed(runnable, 2000L);
                return;
            }
            return;
        }
        Runnable runnable2 = this.mPendingMarqueeStart;
        if (runnable2 != null) {
            this.mHandler.removeCallbacks(runnable2);
            this.mPendingMarqueeStart = null;
        }
        setEnableMarqueeImpl(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setEnableMarquee$0() {
        setEnableMarqueeImpl(true);
        this.mPendingMarqueeStart = null;
    }

    private void setEnableMarqueeImpl(boolean z) {
        TextView textView = this.mOwnerInfo;
        if (textView != null) {
            textView.setSelected(z);
        }
    }

    @Override // android.view.View
    protected void onFinishInflate() throws Resources.NotFoundException {
        super.onFinishInflate();
        this.mStatusViewContainer = (LinearLayout) findViewById(R.id.status_view_container);
        this.mLogoutView = (TextView) findViewById(R.id.logout);
        this.mNotificationIcons = findViewById(R.id.clock_notification_icon_container);
        TextView textView = this.mLogoutView;
        if (textView != null) {
            textView.setOnClickListener(new View.OnClickListener() { // from class: com.android.keyguard.KeyguardStatusView$$ExternalSyntheticLambda0
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.f$0.onLogoutClicked(view);
                }
            });
        }
        KeyguardClockSwitch keyguardClockSwitch = (KeyguardClockSwitch) findViewById(R.id.keyguard_clock_container);
        this.mClockView = keyguardClockSwitch;
        keyguardClockSwitch.setShowCurrentUserTime(true);
        this.mOwnerInfo = (TextView) findViewById(R.id.owner_info);
        this.mKeyguardSlice = (KeyguardSliceView) findViewById(R.id.keyguard_status_area);
        this.mWeatherView = (CurrentWeatherView) findViewById(R.id.weather_container);
        this.mTextColor = this.mClockView.getCurrentTextColor();
        this.mKeyguardSlice.setContentChangeListener(new Runnable() { // from class: com.android.keyguard.KeyguardStatusView$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() throws Resources.NotFoundException {
                this.f$0.onSliceContentChanged();
            }
        });
        onSliceContentChanged();
        this.mClockView.updateClockColor();
        updateClockDateColor();
        updateOwnerInfoColor();
        setEnableMarquee(((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isDeviceInteractive());
        refreshFormat();
        updateOwnerInfo();
        updateLogoutView();
        updateDark();
        updateWeatherView();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSliceContentChanged() throws Resources.NotFoundException {
        boolean zHasHeader = this.mKeyguardSlice.hasHeader();
        this.mClockView.setKeyguardShowingHeader(zHasHeader);
        if (this.mShowingHeader == zHasHeader) {
            return;
        }
        this.mShowingHeader = zHasHeader;
        View view = this.mNotificationIcons;
        if (view != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            marginLayoutParams.setMargins(marginLayoutParams.leftMargin, zHasHeader ? this.mIconTopMarginWithHeader : this.mIconTopMargin, marginLayoutParams.rightMargin, marginLayoutParams.bottomMargin);
            this.mNotificationIcons.setLayoutParams(marginLayoutParams);
        }
    }

    @Override // android.widget.GridLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        layoutOwnerInfo();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        KeyguardClockSwitch keyguardClockSwitch = this.mClockView;
        if (keyguardClockSwitch != null) {
            keyguardClockSwitch.setTextSize(0, getResources().getDimensionPixelSize(R.dimen.widget_big_font_size));
        }
        TextView textView = this.mOwnerInfo;
        if (textView != null) {
            textView.setTextSize(0, getResources().getDimensionPixelSize(R.dimen.widget_label_font_size));
        }
        CurrentWeatherView currentWeatherView = this.mWeatherView;
        if (currentWeatherView != null) {
            currentWeatherView.onDensityOrFontScaleChanged();
        }
        loadBottomMargin();
    }

    public void dozeTimeTick() {
        refreshTime();
        this.mKeyguardSlice.refresh();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void refreshTime() {
        this.mClockView.refresh();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateTimeZone(TimeZone timeZone) {
        this.mClockView.onTimeZoneChanged(timeZone);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateClockDateColor() {
        int i = Settings.System.getInt(getContext().getContentResolver(), "lockscreen_clock_date_color", -1);
        KeyguardSliceView keyguardSliceView = this.mKeyguardSlice;
        if (keyguardSliceView != null) {
            keyguardSliceView.setTextColor(i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateOwnerInfoColor() {
        int i = Settings.System.getInt(getContext().getContentResolver(), "lockscreen_owner_info_color", -1);
        TextView textView = this.mOwnerInfo;
        if (textView != null) {
            textView.setTextColor(i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void refreshFormat() throws Resources.NotFoundException {
        Patterns.update(((GridLayout) this).mContext);
        this.mClockView.setFormat12Hour(Patterns.clockView12);
        this.mClockView.setFormat24Hour(Patterns.clockView24);
    }

    public int getLogoutButtonHeight() {
        TextView textView = this.mLogoutView;
        if (textView != null && textView.getVisibility() == 0) {
            return this.mLogoutView.getHeight();
        }
        return 0;
    }

    public float getClockTextSize() {
        return this.mClockView.getTextSize();
    }

    public int getClockPreferredY(int i) {
        return this.mClockView.getPreferredY(i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateLogoutView() {
        TextView textView = this.mLogoutView;
        if (textView == null) {
            return;
        }
        textView.setVisibility(shouldShowLogout() ? 0 : 8);
        this.mLogoutView.setText(((GridLayout) this).mContext.getResources().getString(android.R.string.ext_media_move_failure_message));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateOwnerInfo() {
        if (this.mOwnerInfo == null) {
            return;
        }
        String deviceOwnerInfo = this.mLockPatternUtils.getDeviceOwnerInfo();
        if (deviceOwnerInfo == null && this.mLockPatternUtils.isOwnerInfoEnabled(KeyguardUpdateMonitor.getCurrentUser())) {
            deviceOwnerInfo = this.mLockPatternUtils.getOwnerInfo(KeyguardUpdateMonitor.getCurrentUser());
        }
        this.mOwnerInfo.setText(deviceOwnerInfo);
        updateOwnerInfoColor();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mInfoCallback);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).removeCallback(this.mInfoCallback);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onLocaleListChanged() throws Resources.NotFoundException {
        refreshFormat();
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        Object objValueOf;
        printWriter.println("KeyguardStatusView:");
        StringBuilder sb = new StringBuilder();
        sb.append("  mOwnerInfo: ");
        TextView textView = this.mOwnerInfo;
        if (textView == null) {
            objValueOf = "null";
        } else {
            objValueOf = Boolean.valueOf(textView.getVisibility() == 0);
        }
        sb.append(objValueOf);
        printWriter.println(sb.toString());
        printWriter.println("  mPulsing: " + this.mPulsing);
        printWriter.println("  mDarkAmount: " + this.mDarkAmount);
        printWriter.println("  mTextColor: " + Integer.toHexString(this.mTextColor));
        if (this.mLogoutView != null) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("  logout visible: ");
            sb2.append(this.mLogoutView.getVisibility() == 0);
            printWriter.println(sb2.toString());
        }
        KeyguardClockSwitch keyguardClockSwitch = this.mClockView;
        if (keyguardClockSwitch != null) {
            keyguardClockSwitch.dump(fileDescriptor, printWriter, strArr);
        }
        KeyguardSliceView keyguardSliceView = this.mKeyguardSlice;
        if (keyguardSliceView != null) {
            keyguardSliceView.dump(fileDescriptor, printWriter, strArr);
        }
    }

    private void loadBottomMargin() {
        this.mIconTopMargin = getResources().getDimensionPixelSize(R.dimen.widget_vertical_padding);
        this.mIconTopMarginWithHeader = getResources().getDimensionPixelSize(R.dimen.widget_vertical_padding_with_header);
    }

    private static final class Patterns {
        static String cacheKey;
        static String clockView12;
        static String clockView24;

        static void update(Context context) throws Resources.NotFoundException {
            Locale locale = Locale.getDefault();
            Resources resources = context.getResources();
            String string = resources.getString(R.string.clock_12hr_format);
            String string2 = resources.getString(R.string.clock_24hr_format);
            String str = locale.toString() + string + string2;
            if (str.equals(cacheKey)) {
                return;
            }
            clockView12 = DateFormat.getBestDateTimePattern(locale, string);
            if (!string.contains("a")) {
                clockView12 = clockView12.replaceAll("a", "").trim();
            }
            clockView24 = DateFormat.getBestDateTimePattern(locale, string2);
            cacheKey = str;
        }
    }

    public void setDarkAmount(float f) {
        if (this.mDarkAmount == f) {
            return;
        }
        this.mDarkAmount = f;
        this.mClockView.setDarkAmount(f);
        updateDark();
    }

    private void updateDark() {
        boolean z = this.mDarkAmount == 1.0f;
        TextView textView = this.mLogoutView;
        if (textView != null) {
            textView.setAlpha(z ? 0.0f : 1.0f);
        }
        TextView textView2 = this.mOwnerInfo;
        if (textView2 != null) {
            this.mOwnerInfo.setVisibility(TextUtils.isEmpty(textView2.getText()) ^ true ? 0 : 8);
            layoutOwnerInfo();
        }
        ColorUtils.blendARGB(this.mTextColor, -1, this.mDarkAmount);
    }

    private void layoutOwnerInfo() {
        TextView textView = this.mOwnerInfo;
        if (textView != null && textView.getVisibility() != 8) {
            this.mOwnerInfo.setAlpha(1.0f - this.mDarkAmount);
            int bottom = (int) (((this.mOwnerInfo.getBottom() + this.mOwnerInfo.getPaddingBottom()) - (this.mOwnerInfo.getTop() - this.mOwnerInfo.getPaddingTop())) * this.mDarkAmount);
            setBottom(getMeasuredHeight() - bottom);
            View view = this.mNotificationIcons;
            if (view != null) {
                view.setScrollY(bottom);
                return;
            }
            return;
        }
        View view2 = this.mNotificationIcons;
        if (view2 != null) {
            view2.setScrollY(0);
        }
    }

    public void setPulsing(boolean z) {
        if (this.mPulsing == z) {
            return;
        }
        this.mPulsing = z;
    }

    private boolean shouldShowLogout() {
        return ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isLogoutEnabled() && KeyguardUpdateMonitor.getCurrentUser() != 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onLogoutClicked(View view) {
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        try {
            this.mIActivityManager.switchUser(0);
            this.mIActivityManager.stopUser(currentUser, true, (IStopUserCallback) null);
        } catch (RemoteException e) {
            Log.e("KeyguardStatusView", "Failed to logout user", e);
        }
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        str.hashCode();
        if (str.equals("system:lockscreen_weather_enabled")) {
            this.mShowWeather = TunerService.parseIntegerSwitch(str2, false);
            updateWeatherView();
        } else if (str.equals("system:lockscreen_weather_style")) {
            this.mPixelStyle = TunerService.parseIntegerSwitch(str2, false);
            updateWeatherView();
        }
    }

    public void updateWeatherView() {
        if (this.mWeatherView != null) {
            if (this.mShowWeather && (!this.mPixelStyle || this.mKeyguardSlice.getVisibility() != 0)) {
                this.mWeatherView.enableUpdates();
            } else if (!this.mShowWeather || this.mPixelStyle) {
                this.mWeatherView.disableUpdates();
            }
        }
    }
}
