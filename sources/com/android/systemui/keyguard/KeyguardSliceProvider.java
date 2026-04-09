package com.android.systemui.keyguard;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.drawable.Icon;
import android.icu.text.DateFormat;
import android.icu.text.DisplayContext;
import android.media.MediaMetadata;
import android.net.Uri;
import android.os.Handler;
import android.os.Trace;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.notification.ZenModeConfig;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import androidx.core.graphics.drawable.IconCompat;
import androidx.slice.Slice;
import androidx.slice.SliceProvider;
import androidx.slice.builders.ListBuilder;
import androidx.slice.builders.SliceAction;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.crdroid.OmniJawsClient;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.SystemUIAppComponentFactory;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.util.wakelock.SettableWakeLock;
import com.android.systemui.util.wakelock.WakeLock;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/* loaded from: classes.dex */
public class KeyguardSliceProvider extends SliceProvider implements NextAlarmController.NextAlarmChangeCallback, ZenModeController.Callback, NotificationMediaManager.MediaListener, StatusBarStateController.StateListener, SystemUIAppComponentFactory.ContextInitializer, OmniJawsClient.OmniJawsObserver {

    @VisibleForTesting
    static final int ALARM_VISIBILITY_HOURS = 12;
    private static KeyguardSliceProvider sInstance;
    public AlarmManager mAlarmManager;
    public ContentResolver mContentResolver;
    private SystemUIAppComponentFactory.ContextAvailableCallback mContextAvailableCallback;
    private DateFormat mDateFormat;
    private String mDatePattern;
    public DozeParameters mDozeParameters;
    protected boolean mDozing;
    public KeyguardBypassController mKeyguardBypassController;
    private String mLastText;
    private CharSequence mMediaArtist;
    private boolean mMediaIsVisible;
    public NotificationMediaManager mMediaManager;
    private CharSequence mMediaTitle;

    @VisibleForTesting
    protected SettableWakeLock mMediaWakeLock;
    private String mNextAlarm;
    public NextAlarmController mNextAlarmController;
    private AlarmManager.AlarmClockInfo mNextAlarmInfo;
    private OmniJawsClient.PackageInfo mPackageInfo;
    private PendingIntent mPendingIntent;
    private boolean mPulseOnNewTracks;
    private boolean mRegistered;
    private boolean mShowWeatherSlice;
    private int mStatusBarState;
    public StatusBarStateController mStatusBarStateController;
    private OmniJawsClient mWeatherClient;
    private boolean mWeatherEnabled;
    private OmniJawsClient.WeatherInfo mWeatherInfo;
    private WeatherSettingsObserver mWeatherSettingsObserver;
    public ZenModeController mZenModeController;
    private static final StyleSpan BOLD_STYLE = new StyleSpan(1);
    private static final Object sInstanceLock = new Object();
    private String TAG = KeyguardSliceProvider.class.getSimpleName();
    private final Date mCurrentTime = new Date();
    private final AlarmManager.OnAlarmListener mUpdateNextAlarm = new AlarmManager.OnAlarmListener() { // from class: com.android.systemui.keyguard.KeyguardSliceProvider$$ExternalSyntheticLambda0
        @Override // android.app.AlarmManager.OnAlarmListener
        public final void onAlarm() {
            this.f$0.updateNextAlarm();
        }
    };

    @VisibleForTesting
    final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() { // from class: com.android.systemui.keyguard.KeyguardSliceProvider.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.DATE_CHANGED".equals(action)) {
                synchronized (this) {
                    KeyguardSliceProvider.this.updateClockLocked();
                }
            } else if ("android.intent.action.LOCALE_CHANGED".equals(action)) {
                synchronized (this) {
                    KeyguardSliceProvider.this.cleanDateFormatLocked();
                }
            }
        }
    };

    @VisibleForTesting
    final KeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.systemui.keyguard.KeyguardSliceProvider.2
        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onTimeChanged() {
            synchronized (this) {
                KeyguardSliceProvider.this.updateClockLocked();
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onTimeZoneChanged(TimeZone timeZone) {
            synchronized (this) {
                KeyguardSliceProvider.this.cleanDateFormatLocked();
            }
        }
    };
    private final Handler mHandler = new Handler();
    private final Handler mMediaHandler = new Handler();
    protected final Uri mSliceUri = Uri.parse("content://com.android.systemui.keyguard/main");
    protected final Uri mHeaderUri = Uri.parse("content://com.android.systemui.keyguard/header");
    protected final Uri mDateUri = Uri.parse("content://com.android.systemui.keyguard/date");
    protected final Uri mAlarmUri = Uri.parse("content://com.android.systemui.keyguard/alarm");
    protected final Uri mDndUri = Uri.parse("content://com.android.systemui.keyguard/dnd");
    protected final Uri mMediaUri = Uri.parse("content://com.android.systemui.keyguard/media");
    protected final Uri mWeatherUri = Uri.parse("content://com.android.systemui.keyguard/weather");

    public void weatherError(int i) {
    }

    public static KeyguardSliceProvider getAttachedInstance() {
        return sInstance;
    }

    @Override // androidx.slice.SliceProvider
    public Slice onBindSlice(Uri uri) {
        Slice sliceBuild;
        Trace.beginSection("KeyguardSliceProvider#onBindSlice");
        synchronized (this) {
            ListBuilder listBuilder = new ListBuilder(getContext(), this.mSliceUri, -1L);
            if (needsMediaLocked() && this.mDozing) {
                addMediaLocked(listBuilder);
            } else {
                listBuilder.addRow(new ListBuilder.RowBuilder(this.mDateUri).setTitle(this.mLastText));
            }
            addNextAlarmLocked(listBuilder);
            addZenModeLocked(listBuilder);
            addPrimaryActionLocked(listBuilder);
            addWeather(listBuilder);
            sliceBuild = listBuilder.build();
        }
        Trace.endSection();
        return sliceBuild;
    }

    protected boolean needsMediaLocked() {
        KeyguardBypassController keyguardBypassController = this.mKeyguardBypassController;
        boolean z = keyguardBypassController != null && keyguardBypassController.getBypassEnabled() && this.mDozeParameters.getAlwaysOn();
        String string = Settings.Secure.getString(this.mContentResolver, "lock_screen_custom_clock_face");
        boolean zContains = string == null ? false : string.contains("Type");
        boolean z2 = string != null && string.contains("Android") && string.contains("S");
        return !TextUtils.isEmpty(this.mMediaTitle) && (this.mMediaIsVisible || z2) && !((!this.mDozing && !z && !(this.mStatusBarState == 0 && this.mMediaIsVisible) && !z2) || z2 || zContains);
    }

    protected void addMediaLocked(ListBuilder listBuilder) {
        if (TextUtils.isEmpty(this.mMediaTitle)) {
            return;
        }
        listBuilder.setHeader(new ListBuilder.HeaderBuilder(this.mHeaderUri).setTitle(this.mMediaTitle));
        if (TextUtils.isEmpty(this.mMediaArtist)) {
            return;
        }
        ListBuilder.RowBuilder rowBuilder = new ListBuilder.RowBuilder(this.mMediaUri);
        rowBuilder.setTitle(this.mMediaArtist);
        NotificationMediaManager notificationMediaManager = this.mMediaManager;
        Icon mediaIcon = notificationMediaManager == null ? null : notificationMediaManager.getMediaIcon();
        IconCompat iconCompatCreateFromIcon = mediaIcon != null ? IconCompat.createFromIcon(getContext(), mediaIcon) : null;
        if (iconCompatCreateFromIcon != null) {
            rowBuilder.addEndItem(iconCompatCreateFromIcon, 0);
        }
        listBuilder.addRow(rowBuilder);
    }

    protected void addPrimaryActionLocked(ListBuilder listBuilder) {
        listBuilder.addRow(new ListBuilder.RowBuilder(Uri.parse("content://com.android.systemui.keyguard/action")).setPrimaryAction(SliceAction.createDeeplink(this.mPendingIntent, IconCompat.createWithResource(getContext(), R.drawable.ic_access_alarms_big), 0, this.mLastText)));
    }

    protected void addNextAlarmLocked(ListBuilder listBuilder) {
        if (TextUtils.isEmpty(this.mNextAlarm)) {
            return;
        }
        listBuilder.addRow(new ListBuilder.RowBuilder(this.mAlarmUri).setTitle(this.mNextAlarm).addEndItem(IconCompat.createWithResource(getContext(), R.drawable.ic_access_alarms_big), 0));
    }

    protected void addZenModeLocked(ListBuilder listBuilder) {
        String string = Settings.Secure.getString(this.mContentResolver, "lock_screen_custom_clock_face");
        if (string != null) {
            string.contains("AndroidS");
        }
        if (isDndOn()) {
            listBuilder.addRow(new ListBuilder.RowBuilder(this.mDndUri).setContentDescription(getContext().getResources().getString(R.string.accessibility_quick_settings_dnd)).addEndItem(IconCompat.createWithResource(getContext(), getDndResource()), 0));
        }
    }

    protected boolean isDndOn() {
        return this.mZenModeController.getZen() != 0;
    }

    protected int getDndResource() {
        int zen = this.mZenModeController.getZen();
        if (zen != 1) {
            return zen != 2 ? android.R.drawable.ic_menu_emoticons : R.drawable.ic_qs_dnd_on_total_silence;
        }
        return R.drawable.ic_qs_dnd_on_priority;
    }

    protected void addWeather(ListBuilder listBuilder) {
        if (this.mWeatherEnabled && this.mShowWeatherSlice && this.mWeatherClient.isOmniJawsEnabled() && this.mWeatherInfo != null && this.mPackageInfo != null) {
            String str = this.mWeatherInfo.temp + " " + this.mWeatherInfo.tempUnits;
            OmniJawsClient.PackageInfo packageInfo = this.mPackageInfo;
            listBuilder.addRow(new ListBuilder.RowBuilder(this.mWeatherUri).setTitle(str).addEndItem(IconCompat.createFromIcon(Icon.createWithResource(packageInfo.packageName, packageInfo.resourceID)), 0));
        }
    }

    public void weatherUpdated() {
        queryAndUpdateWeather();
        this.mContentResolver.notifyChange(this.mSliceUri, null);
    }

    public void updateSettings() {
        queryAndUpdateWeather();
        this.mContentResolver.notifyChange(this.mSliceUri, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void queryAndUpdateWeather() {
        if (this.mWeatherEnabled) {
            try {
                this.mWeatherClient.queryWeather();
                OmniJawsClient.WeatherInfo weatherInfo = this.mWeatherClient.getWeatherInfo();
                this.mWeatherInfo = weatherInfo;
                if (weatherInfo != null) {
                    this.mWeatherClient.getWeatherConditionImage(weatherInfo.conditionCode);
                    this.mPackageInfo = this.mWeatherClient.getPackageInfo();
                } else {
                    this.mPackageInfo = null;
                }
            } catch (Exception unused) {
            }
        }
    }

    private class WeatherSettingsObserver extends ContentObserver {
        WeatherSettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            KeyguardSliceProvider.this.mContentResolver.registerContentObserver(Settings.System.getUriFor("lockscreen_weather_enabled"), false, this, -1);
            KeyguardSliceProvider.this.mContentResolver.registerContentObserver(Settings.System.getUriFor("lockscreen_weather_style"), false, this, -1);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            super.onChange(z, uri);
            if (uri.equals(Settings.System.getUriFor("lockscreen_weather_enabled"))) {
                KeyguardSliceProvider keyguardSliceProvider = KeyguardSliceProvider.this;
                keyguardSliceProvider.mWeatherEnabled = Settings.System.getIntForUser(keyguardSliceProvider.mContentResolver, "lockscreen_weather_enabled", 0, -2) != 0;
                KeyguardSliceProvider.this.queryAndUpdateWeather();
                KeyguardSliceProvider keyguardSliceProvider2 = KeyguardSliceProvider.this;
                keyguardSliceProvider2.mContentResolver.notifyChange(keyguardSliceProvider2.mSliceUri, null);
                return;
            }
            if (uri.equals(Settings.System.getUriFor("lockscreen_weather_style"))) {
                KeyguardSliceProvider keyguardSliceProvider3 = KeyguardSliceProvider.this;
                keyguardSliceProvider3.mShowWeatherSlice = Settings.System.getIntForUser(keyguardSliceProvider3.mContentResolver, "lockscreen_weather_style", 1, -2) != 0;
                KeyguardSliceProvider keyguardSliceProvider4 = KeyguardSliceProvider.this;
                keyguardSliceProvider4.mContentResolver.notifyChange(keyguardSliceProvider4.mSliceUri, null);
            }
        }
    }

    @Override // androidx.slice.SliceProvider
    public boolean onCreateSliceProvider() {
        this.mContextAvailableCallback.onContextAvailable(getContext());
        inject();
        synchronized (sInstanceLock) {
            KeyguardSliceProvider keyguardSliceProvider = sInstance;
            if (keyguardSliceProvider != null) {
                keyguardSliceProvider.onDestroy();
            }
            this.mDatePattern = getContext().getString(R.string.system_ui_aod_date_pattern);
            this.mPendingIntent = PendingIntent.getActivity(getContext(), 0, new Intent(getContext(), (Class<?>) KeyguardSliceProvider.class), 0);
            this.mMediaManager.addCallback(this);
            this.mStatusBarStateController.addCallback(this);
            this.mNextAlarmController.addCallback(this);
            this.mZenModeController.addCallback(this);
            this.mWeatherClient = new OmniJawsClient(getContext());
            this.mWeatherEnabled = Settings.System.getIntForUser(this.mContentResolver, "lockscreen_weather_enabled", 0, -2) != 0;
            this.mShowWeatherSlice = Settings.System.getIntForUser(this.mContentResolver, "lockscreen_weather_style", 1, -2) != 0;
            this.mWeatherClient.addSettingsObserver();
            this.mWeatherClient.addObserver(this);
            WeatherSettingsObserver weatherSettingsObserver = new WeatherSettingsObserver(this.mHandler);
            this.mWeatherSettingsObserver = weatherSettingsObserver;
            weatherSettingsObserver.observe();
            queryAndUpdateWeather();
            sInstance = this;
            registerClockUpdate();
            updateClockLocked();
        }
        return true;
    }

    @VisibleForTesting
    protected void inject() {
        SystemUIFactory.getInstance().getRootComponent().inject(this);
        this.mMediaWakeLock = new SettableWakeLock(WakeLock.createPartial(getContext(), "media"), "media");
    }

    @VisibleForTesting
    protected void onDestroy() {
        synchronized (sInstanceLock) {
            this.mNextAlarmController.removeCallback(this);
            this.mZenModeController.removeCallback(this);
            this.mMediaWakeLock.setAcquired(false);
            this.mAlarmManager.cancel(this.mUpdateNextAlarm);
            if (this.mRegistered) {
                this.mRegistered = false;
                getKeyguardUpdateMonitor().removeCallback(this.mKeyguardUpdateMonitorCallback);
                getContext().unregisterReceiver(this.mIntentReceiver);
            }
            sInstance = null;
        }
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
    public void onZenChanged(int i) {
        notifyChange();
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
    public void onConfigChanged(ZenModeConfig zenModeConfig) {
        notifyChange();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateNextAlarm() {
        synchronized (this) {
            if (withinNHoursLocked(this.mNextAlarmInfo, ALARM_VISIBILITY_HOURS)) {
                this.mNextAlarm = android.text.format.DateFormat.format(android.text.format.DateFormat.is24HourFormat(getContext(), ActivityManager.getCurrentUser()) ? "HH:mm" : "h:mm", this.mNextAlarmInfo.getTriggerTime()).toString();
            } else {
                this.mNextAlarm = "";
            }
        }
        notifyChange();
    }

    private boolean withinNHoursLocked(AlarmManager.AlarmClockInfo alarmClockInfo, int i) {
        if (alarmClockInfo == null) {
            return false;
        }
        return this.mNextAlarmInfo.getTriggerTime() <= System.currentTimeMillis() + TimeUnit.HOURS.toMillis((long) i);
    }

    @VisibleForTesting
    protected void registerClockUpdate() {
        synchronized (this) {
            if (this.mRegistered) {
                return;
            }
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.DATE_CHANGED");
            intentFilter.addAction("android.intent.action.LOCALE_CHANGED");
            getContext().registerReceiver(this.mIntentReceiver, intentFilter, null, null);
            getKeyguardUpdateMonitor().registerCallback(this.mKeyguardUpdateMonitorCallback);
            this.mRegistered = true;
        }
    }

    @VisibleForTesting
    boolean isRegistered() {
        boolean z;
        synchronized (this) {
            z = this.mRegistered;
        }
        return z;
    }

    protected void updateClockLocked() {
        String formattedDateLocked = getFormattedDateLocked();
        if (formattedDateLocked.equals(this.mLastText)) {
            return;
        }
        this.mLastText = formattedDateLocked;
        notifyChange();
    }

    protected String getFormattedDateLocked() {
        if (this.mDateFormat == null) {
            DateFormat instanceForSkeleton = DateFormat.getInstanceForSkeleton(this.mDatePattern, Locale.getDefault());
            instanceForSkeleton.setContext(DisplayContext.CAPITALIZATION_FOR_STANDALONE);
            this.mDateFormat = instanceForSkeleton;
        }
        this.mCurrentTime.setTime(System.currentTimeMillis());
        return this.mDateFormat.format(this.mCurrentTime);
    }

    @VisibleForTesting
    void cleanDateFormatLocked() {
        this.mDateFormat = null;
    }

    @Override // com.android.systemui.statusbar.policy.NextAlarmController.NextAlarmChangeCallback
    public void onNextAlarmChanged(AlarmManager.AlarmClockInfo alarmClockInfo) {
        synchronized (this) {
            this.mNextAlarmInfo = alarmClockInfo;
            this.mAlarmManager.cancel(this.mUpdateNextAlarm);
            AlarmManager.AlarmClockInfo alarmClockInfo2 = this.mNextAlarmInfo;
            long triggerTime = alarmClockInfo2 == null ? -1L : alarmClockInfo2.getTriggerTime() - TimeUnit.HOURS.toMillis(12L);
            if (triggerTime > 0) {
                this.mAlarmManager.setExact(1, triggerTime, "lock_screen_next_alarm", this.mUpdateNextAlarm, this.mHandler);
            }
        }
        updateNextAlarm();
    }

    private KeyguardUpdateMonitor getKeyguardUpdateMonitor() {
        return (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
    }

    @Override // com.android.systemui.statusbar.NotificationMediaManager.MediaListener
    public void onPrimaryMetadataOrStateChanged(final MediaMetadata mediaMetadata, final int i) {
        synchronized (this) {
            boolean zIsPlayingState = NotificationMediaManager.isPlayingState(i);
            this.mMediaHandler.removeCallbacksAndMessages(null);
            if (this.mMediaIsVisible && !zIsPlayingState && this.mStatusBarState != 0) {
                this.mMediaWakeLock.setAcquired(true);
                this.mMediaHandler.postDelayed(new Runnable() { // from class: com.android.systemui.keyguard.KeyguardSliceProvider$$ExternalSyntheticLambda1
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onPrimaryMetadataOrStateChanged$0(mediaMetadata, i);
                    }
                }, 2000L);
            } else {
                this.mMediaWakeLock.setAcquired(false);
                updateMediaStateLocked(mediaMetadata, i);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onPrimaryMetadataOrStateChanged$0(MediaMetadata mediaMetadata, int i) {
        synchronized (this) {
            updateMediaStateLocked(mediaMetadata, i);
            this.mMediaWakeLock.setAcquired(false);
        }
    }

    private void updateMediaStateLocked(MediaMetadata mediaMetadata, int i) throws Resources.NotFoundException {
        CharSequence text;
        String string = Settings.Secure.getString(this.mContentResolver, "lock_screen_custom_clock_face");
        if (string != null) {
            string.contains("AndroidS");
        }
        boolean zIsPlayingState = NotificationMediaManager.isPlayingState(i);
        if (mediaMetadata != null) {
            text = mediaMetadata.getText("android.media.metadata.TITLE");
            if (TextUtils.isEmpty(text)) {
                text = getContext().getResources().getString(R.string.music_controls_no_title);
            }
        } else {
            text = null;
        }
        CharSequence text2 = mediaMetadata != null ? mediaMetadata.getText("android.media.metadata.ARTIST") : null;
        if (zIsPlayingState == this.mMediaIsVisible && TextUtils.equals(text, this.mMediaTitle) && TextUtils.equals(text2, this.mMediaArtist)) {
            return;
        }
        this.mMediaTitle = text;
        this.mMediaArtist = text2;
        this.mMediaIsVisible = zIsPlayingState;
        notifyChange();
        if (this.mPulseOnNewTracks && this.mMediaIsVisible && !this.mDozeParameters.getAlwaysOn() && this.mDozing) {
            getContext().sendBroadcastAsUser(new Intent("com.android.systemui.doze.pulse"), new UserHandle(-2));
        }
    }

    public void setPulseOnNewTracks(boolean z) {
        this.mPulseOnNewTracks = z;
    }

    protected void notifyChange() {
        this.mContentResolver.notifyChange(this.mSliceUri, null);
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozingChanged(boolean z) {
        boolean z2;
        synchronized (this) {
            boolean zNeedsMediaLocked = needsMediaLocked();
            this.mDozing = z;
            z2 = zNeedsMediaLocked != needsMediaLocked();
        }
        if (z2) {
            notifyChange();
        }
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
        boolean z;
        synchronized (this) {
            boolean zNeedsMediaLocked = needsMediaLocked();
            this.mStatusBarState = i;
            z = zNeedsMediaLocked != needsMediaLocked();
        }
        if (z) {
            notifyChange();
        }
    }

    @Override // com.android.systemui.SystemUIAppComponentFactory.ContextInitializer
    public void setContextAvailableCallback(SystemUIAppComponentFactory.ContextAvailableCallback contextAvailableCallback) {
        this.mContextAvailableCallback = contextAvailableCallback;
    }
}
