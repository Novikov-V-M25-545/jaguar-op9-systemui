package com.android.systemui.tuner;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.internal.util.ArrayUtils;
import com.android.systemui.DejankUtils;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.leak.LeakDetector;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import lineageos.providers.LineageSettings;

/* loaded from: classes.dex */
public class TunerServiceImpl extends TunerService {
    private static final String[] RESET_BLACKLIST = {"system:status_bar_battery_style", "system:status_bar_show_battery_percent", "system:status_bar_battery_text_charging", "system:status_bar_clock_seconds", "system:status_bar_clock_date_display", "system:status_bar_clock_date_style", "system:status_bar_clock_date_position", "system:status_bar_clock_date_format", "system:status_bar_clock_auto_hide", "system:status_bar_clock_auto_hide_hduration", "system:status_bar_clock_auto_hide_sduration", "system:status_bar_clock_size", "system:qs_header_clock_size", "sysui_qs_tiles", "doze_always_on", "doze_on_charge_now", "qs_media_resumption", "system:screen_brightness_mode", "qs_media_resumption_blocked"};
    private ContentResolver mContentResolver;
    private final Context mContext;
    private int mCurrentUser;
    private final LeakDetector mLeakDetector;
    private final HashSet<TunerService.Tunable> mTunables;
    private CurrentUserTracker mUserTracker;
    private final Observer mObserver = new Observer();
    private final ArrayMap<Uri, String> mListeningUris = new ArrayMap<>();
    private final ConcurrentHashMap<String, Set<TunerService.Tunable>> mTunableLookup = new ConcurrentHashMap<>();

    public TunerServiceImpl(Context context, Handler handler, LeakDetector leakDetector, BroadcastDispatcher broadcastDispatcher) {
        this.mTunables = LeakDetector.ENABLED ? new HashSet<>() : null;
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
        this.mLeakDetector = leakDetector;
        Iterator it = UserManager.get(context).getUsers().iterator();
        while (it.hasNext()) {
            this.mCurrentUser = ((UserInfo) it.next()).getUserHandle().getIdentifier();
            if (getValue("sysui_tuner_version", 0) != 4) {
                upgradeTuner(getValue("sysui_tuner_version", 0), 4, handler);
            }
        }
        this.mCurrentUser = ActivityManager.getCurrentUser();
        CurrentUserTracker currentUserTracker = new CurrentUserTracker(broadcastDispatcher) { // from class: com.android.systemui.tuner.TunerServiceImpl.1
            @Override // com.android.systemui.settings.CurrentUserTracker
            public void onUserSwitched(int i) {
                TunerServiceImpl.this.mCurrentUser = i;
                TunerServiceImpl.this.reloadAll();
                TunerServiceImpl.this.reregisterAll();
            }
        };
        this.mUserTracker = currentUserTracker;
        currentUserTracker.startTracking();
    }

    private void upgradeTuner(int i, int i2, Handler handler) {
        String strLambda$addTunable$1;
        if (i < 1 && (strLambda$addTunable$1 = lambda$addTunable$1("icon_blacklist")) != null) {
            ArraySet<String> iconBlacklist = StatusBarIconController.getIconBlacklist(this.mContext, strLambda$addTunable$1);
            iconBlacklist.add("rotate");
            iconBlacklist.add("headset");
            Settings.Secure.putStringForUser(this.mContentResolver, "icon_blacklist", TextUtils.join(",", iconBlacklist), this.mCurrentUser);
        }
        if (i < 2) {
            TunerService.setTunerEnabled(this.mContext, false);
        }
        if (i < 4) {
            final int i3 = this.mCurrentUser;
            handler.postDelayed(new Runnable() { // from class: com.android.systemui.tuner.TunerServiceImpl$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$upgradeTuner$0(i3);
                }
            }, 5000L);
        }
        setValue("sysui_tuner_version", i2);
    }

    private boolean isLineageSetting(String str) {
        return isLineageGlobal(str) || isLineageSystem(str) || isLineageSecure(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isLineageGlobal(String str) {
        return str.startsWith("lineageglobal:");
    }

    private boolean isLineageSystem(String str) {
        return str.startsWith("lineagesystem:");
    }

    private boolean isLineageSecure(String str) {
        return str.startsWith("lineagesecure:");
    }

    private boolean isSystem(String str) {
        return str.startsWith("system:");
    }

    private boolean isGlobal(String str) {
        return str.startsWith("global:");
    }

    private String chomp(String str) {
        return str.replaceFirst("^(lineageglobal|lineagesecure|lineagesystem|system|global):", "");
    }

    @Override // com.android.systemui.tuner.TunerService
    /* renamed from: getValue, reason: merged with bridge method [inline-methods] */
    public String lambda$addTunable$1(String str) {
        if (isLineageGlobal(str)) {
            return LineageSettings.Global.getString(this.mContentResolver, chomp(str));
        }
        if (isLineageSecure(str)) {
            return LineageSettings.Secure.getStringForUser(this.mContentResolver, chomp(str), this.mCurrentUser);
        }
        if (isLineageSystem(str)) {
            return LineageSettings.System.getStringForUser(this.mContentResolver, chomp(str), this.mCurrentUser);
        }
        if (isSystem(str)) {
            return Settings.System.getStringForUser(this.mContentResolver, chomp(str), this.mCurrentUser);
        }
        if (isGlobal(str)) {
            return Settings.Global.getStringForUser(this.mContentResolver, chomp(str), this.mCurrentUser);
        }
        return Settings.Secure.getStringForUser(this.mContentResolver, str, this.mCurrentUser);
    }

    @Override // com.android.systemui.tuner.TunerService
    public void setValue(String str, String str2) {
        if (isLineageGlobal(str)) {
            LineageSettings.Global.putString(this.mContentResolver, chomp(str), str2);
            return;
        }
        if (isLineageSecure(str)) {
            LineageSettings.Secure.putStringForUser(this.mContentResolver, chomp(str), str2, this.mCurrentUser);
            return;
        }
        if (isLineageSystem(str)) {
            LineageSettings.System.putStringForUser(this.mContentResolver, chomp(str), str2, this.mCurrentUser);
            return;
        }
        if (isSystem(str)) {
            Settings.System.putStringForUser(this.mContentResolver, chomp(str), str2, this.mCurrentUser);
        } else if (isGlobal(str)) {
            Settings.Global.putStringForUser(this.mContentResolver, chomp(str), str2, this.mCurrentUser);
        } else {
            Settings.Secure.putStringForUser(this.mContentResolver, str, str2, this.mCurrentUser);
        }
    }

    @Override // com.android.systemui.tuner.TunerService
    public int getValue(String str, int i) {
        if (isLineageGlobal(str)) {
            return LineageSettings.Global.getInt(this.mContentResolver, chomp(str), i);
        }
        if (isLineageSecure(str)) {
            return LineageSettings.Secure.getIntForUser(this.mContentResolver, chomp(str), i, this.mCurrentUser);
        }
        if (isLineageSystem(str)) {
            return LineageSettings.System.getIntForUser(this.mContentResolver, chomp(str), i, this.mCurrentUser);
        }
        if (isSystem(str)) {
            return Settings.System.getIntForUser(this.mContentResolver, chomp(str), i, this.mCurrentUser);
        }
        if (isGlobal(str)) {
            return Settings.Global.getInt(this.mContentResolver, chomp(str), i);
        }
        return Settings.Secure.getIntForUser(this.mContentResolver, str, i, this.mCurrentUser);
    }

    public void setValue(String str, int i) {
        if (isLineageGlobal(str)) {
            LineageSettings.Global.putInt(this.mContentResolver, chomp(str), i);
            return;
        }
        if (isLineageSecure(str)) {
            LineageSettings.Secure.putIntForUser(this.mContentResolver, chomp(str), i, this.mCurrentUser);
            return;
        }
        if (isLineageSystem(str)) {
            LineageSettings.System.putIntForUser(this.mContentResolver, chomp(str), i, this.mCurrentUser);
            return;
        }
        if (isSystem(str)) {
            Settings.System.putIntForUser(this.mContentResolver, chomp(str), i, this.mCurrentUser);
        } else if (isGlobal(str)) {
            Settings.Global.putInt(this.mContentResolver, chomp(str), i);
        } else {
            Settings.Secure.putIntForUser(this.mContentResolver, str, i, this.mCurrentUser);
        }
    }

    @Override // com.android.systemui.tuner.TunerService
    public void addTunable(TunerService.Tunable tunable, String... strArr) {
        for (String str : strArr) {
            addTunable(tunable, str);
        }
    }

    private void addTunable(TunerService.Tunable tunable, final String str) {
        Uri uriFor;
        if (!this.mTunableLookup.containsKey(str)) {
            this.mTunableLookup.put(str, new ArraySet());
        }
        this.mTunableLookup.get(str).add(tunable);
        if (LeakDetector.ENABLED) {
            this.mTunables.add(tunable);
            this.mLeakDetector.trackCollection(this.mTunables, "TunerService.mTunables");
        }
        if (isLineageGlobal(str)) {
            uriFor = LineageSettings.Global.getUriFor(chomp(str));
        } else if (isLineageSecure(str)) {
            uriFor = LineageSettings.Secure.getUriFor(chomp(str));
        } else if (isLineageSystem(str)) {
            uriFor = LineageSettings.System.getUriFor(chomp(str));
        } else if (isSystem(str)) {
            uriFor = Settings.System.getUriFor(chomp(str));
        } else if (isGlobal(str)) {
            uriFor = Settings.Global.getUriFor(chomp(str));
        } else {
            uriFor = Settings.Secure.getUriFor(str);
        }
        synchronized (this) {
            if (!this.mListeningUris.containsKey(uriFor)) {
                this.mListeningUris.put(uriFor, str);
                this.mContentResolver.registerContentObserver(uriFor, false, this.mObserver, isLineageGlobal(str) ? -1 : this.mCurrentUser);
            }
        }
        tunable.onTuningChanged(str, (String) DejankUtils.whitelistIpcs(new Supplier() { // from class: com.android.systemui.tuner.TunerServiceImpl$$ExternalSyntheticLambda1
            @Override // java.util.function.Supplier
            public final Object get() {
                return this.f$0.lambda$addTunable$1(str);
            }
        }));
    }

    @Override // com.android.systemui.tuner.TunerService
    public void removeTunable(TunerService.Tunable tunable) {
        Iterator<Set<TunerService.Tunable>> it = this.mTunableLookup.values().iterator();
        while (it.hasNext()) {
            it.next().remove(tunable);
        }
        if (LeakDetector.ENABLED) {
            this.mTunables.remove(tunable);
        }
    }

    protected void reregisterAll() {
        if (this.mListeningUris.size() == 0) {
            return;
        }
        this.mContentResolver.unregisterContentObserver(this.mObserver);
        for (Uri uri : this.mListeningUris.keySet()) {
            this.mContentResolver.registerContentObserver(uri, false, this.mObserver, isLineageGlobal(this.mListeningUris.get(uri)) ? -1 : this.mCurrentUser);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reloadSetting(Uri uri) {
        String str = this.mListeningUris.get(uri);
        Set<TunerService.Tunable> set = this.mTunableLookup.get(str);
        if (set == null) {
            return;
        }
        String strLambda$addTunable$1 = lambda$addTunable$1(str);
        for (TunerService.Tunable tunable : set) {
            if (tunable != null) {
                tunable.onTuningChanged(str, strLambda$addTunable$1);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reloadAll() {
        for (String str : this.mTunableLookup.keySet()) {
            String strLambda$addTunable$1 = lambda$addTunable$1(str);
            for (TunerService.Tunable tunable : this.mTunableLookup.get(str)) {
                if (tunable != null) {
                    tunable.onTuningChanged(str, strLambda$addTunable$1);
                }
            }
        }
    }

    @Override // com.android.systemui.tuner.TunerService
    public void clearAll() {
        lambda$upgradeTuner$0(this.mCurrentUser);
    }

    /* renamed from: clearAllFromUser, reason: merged with bridge method [inline-methods] */
    public void lambda$upgradeTuner$0(int i) {
        Settings.Global.putString(this.mContentResolver, "sysui_demo_allowed", null);
        Intent intent = new Intent("com.android.systemui.demo");
        intent.putExtra("command", "exit");
        this.mContext.sendBroadcast(intent);
        for (String str : this.mTunableLookup.keySet()) {
            if (!ArrayUtils.contains(RESET_BLACKLIST, str) && !isLineageSetting(str)) {
                setValue(str, (String) null);
            }
        }
    }

    private class Observer extends ContentObserver {
        public Observer() {
            super(new Handler(Looper.getMainLooper()));
        }

        public void onChange(boolean z, Collection<Uri> collection, int i, int i2) {
            for (Uri uri : collection) {
                String str = (String) TunerServiceImpl.this.mListeningUris.get(uri);
                if (i2 == ActivityManager.getCurrentUser() || TunerServiceImpl.this.isLineageGlobal(str)) {
                    TunerServiceImpl.this.reloadSetting(uri);
                }
            }
        }
    }
}
