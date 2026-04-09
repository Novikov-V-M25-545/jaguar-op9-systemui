package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.statusbar.AlertingNotificationManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;

/* loaded from: classes.dex */
public abstract class HeadsUpManager extends AlertingNotificationManager {
    protected final Context mContext;
    protected boolean mHasPinnedNotification;
    protected int mSnoozeLengthMs;
    private final ArrayMap<String, Long> mSnoozedPackages;
    protected int mTouchAcceptanceDelay;
    protected int mUser;
    protected final HashSet<OnHeadsUpChangedListener> mListeners = new HashSet<>();
    private final AccessibilityManagerWrapper mAccessibilityMgr = (AccessibilityManagerWrapper) Dependency.get(AccessibilityManagerWrapper.class);

    @Override // com.android.systemui.statusbar.AlertingNotificationManager
    public int getContentFlag() {
        return 4;
    }

    public boolean isEntryAutoHeadsUpped(String str) {
        return false;
    }

    public boolean isTrackingHeadsUp() {
        return false;
    }

    public void onDensityOrFontScaleChanged() {
    }

    public HeadsUpManager(final Context context) throws Resources.NotFoundException {
        this.mContext = context;
        Resources resources = context.getResources();
        this.mMinimumDisplayTime = resources.getInteger(R.integer.heads_up_notification_minimum_time);
        this.mAutoDismissNotificationDecay = resources.getInteger(R.integer.heads_up_notification_decay);
        this.mTouchAcceptanceDelay = resources.getInteger(R.integer.touch_acceptance_delay);
        this.mSnoozedPackages = new ArrayMap<>();
        this.mSnoozeLengthMs = Settings.Global.getInt(context.getContentResolver(), "heads_up_snooze_length_ms", resources.getInteger(R.integer.heads_up_default_snooze_length_ms));
        context.getContentResolver().registerContentObserver(Settings.Global.getUriFor("heads_up_snooze_length_ms"), false, new ContentObserver(this.mHandler) { // from class: com.android.systemui.statusbar.policy.HeadsUpManager.1
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                int i = Settings.Global.getInt(context.getContentResolver(), "heads_up_snooze_length_ms", -1);
                if (i > -1) {
                    HeadsUpManager headsUpManager = HeadsUpManager.this;
                    if (i != headsUpManager.mSnoozeLengthMs) {
                        headsUpManager.mSnoozeLengthMs = i;
                        if (Log.isLoggable("HeadsUpManager", 2)) {
                            Log.v("HeadsUpManager", "mSnoozeLengthMs = " + HeadsUpManager.this.mSnoozeLengthMs);
                        }
                    }
                }
            }
        });
    }

    public void addListener(OnHeadsUpChangedListener onHeadsUpChangedListener) {
        this.mListeners.add(onHeadsUpChangedListener);
    }

    public void removeListener(OnHeadsUpChangedListener onHeadsUpChangedListener) {
        this.mListeners.remove(onHeadsUpChangedListener);
    }

    @Override // com.android.systemui.statusbar.AlertingNotificationManager
    public void updateNotification(String str, boolean z) {
        super.updateNotification(str, z);
        HeadsUpEntry headsUpEntry = getHeadsUpEntry(str);
        if (!z || headsUpEntry == null) {
            return;
        }
        setEntryPinned(headsUpEntry, shouldHeadsUpBecomePinned(headsUpEntry.mEntry));
    }

    protected boolean shouldHeadsUpBecomePinned(NotificationEntry notificationEntry) {
        return hasFullScreenIntent(notificationEntry);
    }

    protected boolean hasFullScreenIntent(NotificationEntry notificationEntry) {
        return notificationEntry.getSbn().getNotification().fullScreenIntent != null;
    }

    protected void setEntryPinned(HeadsUpEntry headsUpEntry, boolean z) {
        if (Log.isLoggable("HeadsUpManager", 2)) {
            Log.v("HeadsUpManager", "setEntryPinned: " + z);
        }
        NotificationEntry notificationEntry = headsUpEntry.mEntry;
        if (notificationEntry.isRowPinned() != z) {
            notificationEntry.setRowPinned(z);
            updatePinnedMode();
            Iterator<OnHeadsUpChangedListener> it = this.mListeners.iterator();
            while (it.hasNext()) {
                OnHeadsUpChangedListener next = it.next();
                if (z) {
                    next.onHeadsUpPinned(notificationEntry);
                } else {
                    next.onHeadsUpUnPinned(notificationEntry);
                }
            }
        }
    }

    @Override // com.android.systemui.statusbar.AlertingNotificationManager
    protected void onAlertEntryAdded(AlertingNotificationManager.AlertEntry alertEntry) {
        NotificationEntry notificationEntry = alertEntry.mEntry;
        notificationEntry.setHeadsUp(true);
        setEntryPinned((HeadsUpEntry) alertEntry, shouldHeadsUpBecomePinned(notificationEntry));
        Iterator<OnHeadsUpChangedListener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            it.next().onHeadsUpStateChanged(notificationEntry, true);
        }
    }

    @Override // com.android.systemui.statusbar.AlertingNotificationManager
    protected void onAlertEntryRemoved(AlertingNotificationManager.AlertEntry alertEntry) {
        NotificationEntry notificationEntry = alertEntry.mEntry;
        notificationEntry.setHeadsUp(false);
        setEntryPinned((HeadsUpEntry) alertEntry, false);
        Iterator<OnHeadsUpChangedListener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            it.next().onHeadsUpStateChanged(notificationEntry, false);
        }
    }

    protected void updatePinnedMode() {
        boolean zHasPinnedNotificationInternal = hasPinnedNotificationInternal();
        if (zHasPinnedNotificationInternal == this.mHasPinnedNotification) {
            return;
        }
        if (Log.isLoggable("HeadsUpManager", 2)) {
            Log.v("HeadsUpManager", "Pinned mode changed: " + this.mHasPinnedNotification + " -> " + zHasPinnedNotificationInternal);
        }
        this.mHasPinnedNotification = zHasPinnedNotificationInternal;
        if (zHasPinnedNotificationInternal) {
            MetricsLogger.count(this.mContext, "note_peek", 1);
        }
        Iterator<OnHeadsUpChangedListener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            it.next().onHeadsUpPinnedModeChanged(zHasPinnedNotificationInternal);
        }
    }

    public boolean isSnoozed(String str) {
        String strSnoozeKey = snoozeKey(str, this.mUser);
        Long l = this.mSnoozedPackages.get(strSnoozeKey);
        if (l == null) {
            return false;
        }
        if (l.longValue() > this.mClock.currentTimeMillis()) {
            if (!Log.isLoggable("HeadsUpManager", 2)) {
                return true;
            }
            Log.v("HeadsUpManager", strSnoozeKey + " snoozed");
            return true;
        }
        this.mSnoozedPackages.remove(str);
        return false;
    }

    public void snooze() {
        Iterator<String> it = this.mAlertEntries.keySet().iterator();
        while (it.hasNext()) {
            this.mSnoozedPackages.put(snoozeKey(getHeadsUpEntry(it.next()).mEntry.getSbn().getPackageName(), this.mUser), Long.valueOf(this.mClock.currentTimeMillis() + this.mSnoozeLengthMs));
        }
    }

    private static String snoozeKey(String str, int i) {
        return i + "," + str;
    }

    protected HeadsUpEntry getHeadsUpEntry(String str) {
        return (HeadsUpEntry) this.mAlertEntries.get(str);
    }

    public NotificationEntry getTopEntry() {
        HeadsUpEntry topHeadsUpEntry = getTopHeadsUpEntry();
        if (topHeadsUpEntry != null) {
            return topHeadsUpEntry.mEntry;
        }
        return null;
    }

    protected HeadsUpEntry getTopHeadsUpEntry() {
        HeadsUpEntry headsUpEntry = null;
        if (this.mAlertEntries.isEmpty()) {
            return null;
        }
        for (AlertingNotificationManager.AlertEntry alertEntry : this.mAlertEntries.values()) {
            if (headsUpEntry == null || alertEntry.compareTo((AlertingNotificationManager.AlertEntry) headsUpEntry) < 0) {
                headsUpEntry = (HeadsUpEntry) alertEntry;
            }
        }
        return headsUpEntry;
    }

    public void setUser(int i) {
        this.mUser = i;
    }

    protected void dumpInternal(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print("  mTouchAcceptanceDelay=");
        printWriter.println(this.mTouchAcceptanceDelay);
        printWriter.print("  mSnoozeLengthMs=");
        printWriter.println(this.mSnoozeLengthMs);
        printWriter.print("  now=");
        printWriter.println(this.mClock.currentTimeMillis());
        printWriter.print("  mUser=");
        printWriter.println(this.mUser);
        for (AlertingNotificationManager.AlertEntry alertEntry : this.mAlertEntries.values()) {
            printWriter.print("  HeadsUpEntry=");
            printWriter.println(alertEntry.mEntry);
        }
        int size = this.mSnoozedPackages.size();
        printWriter.println("  snoozed packages: " + size);
        for (int i = 0; i < size; i++) {
            printWriter.print("    ");
            printWriter.print(this.mSnoozedPackages.valueAt(i));
            printWriter.print(", ");
            printWriter.println(this.mSnoozedPackages.keyAt(i));
        }
    }

    public boolean hasPinnedHeadsUp() {
        return this.mHasPinnedNotification;
    }

    private boolean hasPinnedNotificationInternal() {
        Iterator<String> it = this.mAlertEntries.keySet().iterator();
        while (it.hasNext()) {
            if (getHeadsUpEntry(it.next()).mEntry.isRowPinned()) {
                return true;
            }
        }
        return false;
    }

    public void unpinAll(boolean z) {
        NotificationEntry notificationEntry;
        Iterator<String> it = this.mAlertEntries.keySet().iterator();
        while (it.hasNext()) {
            HeadsUpEntry headsUpEntry = getHeadsUpEntry(it.next());
            setEntryPinned(headsUpEntry, false);
            headsUpEntry.updateEntry(false);
            if (z && (notificationEntry = headsUpEntry.mEntry) != null && notificationEntry.mustStayOnScreen()) {
                headsUpEntry.mEntry.setHeadsUpIsVisible();
            }
        }
    }

    public int compare(NotificationEntry notificationEntry, NotificationEntry notificationEntry2) {
        HeadsUpEntry headsUpEntry = getHeadsUpEntry(notificationEntry.getKey());
        HeadsUpEntry headsUpEntry2 = getHeadsUpEntry(notificationEntry2.getKey());
        if (headsUpEntry == null || headsUpEntry2 == null) {
            return headsUpEntry == null ? 1 : -1;
        }
        return headsUpEntry.compareTo((AlertingNotificationManager.AlertEntry) headsUpEntry2);
    }

    public void setExpanded(NotificationEntry notificationEntry, boolean z) {
        HeadsUpEntry headsUpEntry = getHeadsUpEntry(notificationEntry.getKey());
        if (headsUpEntry == null || !notificationEntry.isRowPinned()) {
            return;
        }
        headsUpEntry.setExpanded(z);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.AlertingNotificationManager
    public HeadsUpEntry createAlertEntry() {
        return new HeadsUpEntry();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean isOngoingCallNotif(NotificationEntry notificationEntry) {
        return notificationEntry.getSbn().isOngoing() && "call".equals(notificationEntry.getSbn().getNotification().category);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public class HeadsUpEntry extends AlertingNotificationManager.AlertEntry {
        protected boolean expanded;
        public boolean remoteInputActive;

        protected HeadsUpEntry() {
            super();
        }

        @Override // com.android.systemui.statusbar.AlertingNotificationManager.AlertEntry
        public boolean isSticky() {
            return (this.mEntry.isRowPinned() && this.expanded) || this.remoteInputActive || HeadsUpManager.this.hasFullScreenIntent(this.mEntry);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // com.android.systemui.statusbar.AlertingNotificationManager.AlertEntry, java.lang.Comparable
        public int compareTo(AlertingNotificationManager.AlertEntry alertEntry) {
            HeadsUpEntry headsUpEntry = (HeadsUpEntry) alertEntry;
            boolean zIsRowPinned = this.mEntry.isRowPinned();
            boolean zIsRowPinned2 = headsUpEntry.mEntry.isRowPinned();
            if (zIsRowPinned && !zIsRowPinned2) {
                return -1;
            }
            if (!zIsRowPinned && zIsRowPinned2) {
                return 1;
            }
            boolean zHasFullScreenIntent = HeadsUpManager.this.hasFullScreenIntent(this.mEntry);
            boolean zHasFullScreenIntent2 = HeadsUpManager.this.hasFullScreenIntent(headsUpEntry.mEntry);
            if (zHasFullScreenIntent && !zHasFullScreenIntent2) {
                return -1;
            }
            if (!zHasFullScreenIntent && zHasFullScreenIntent2) {
                return 1;
            }
            boolean zIsOngoingCallNotif = HeadsUpManager.isOngoingCallNotif(this.mEntry);
            boolean zIsOngoingCallNotif2 = HeadsUpManager.isOngoingCallNotif(headsUpEntry.mEntry);
            if (zIsOngoingCallNotif && !zIsOngoingCallNotif2) {
                return -1;
            }
            if (!zIsOngoingCallNotif && zIsOngoingCallNotif2) {
                return 1;
            }
            boolean z = this.remoteInputActive;
            if (z && !headsUpEntry.remoteInputActive) {
                return -1;
            }
            if (z || !headsUpEntry.remoteInputActive) {
                return super.compareTo((AlertingNotificationManager.AlertEntry) headsUpEntry);
            }
            return 1;
        }

        public void setExpanded(boolean z) {
            this.expanded = z;
        }

        @Override // com.android.systemui.statusbar.AlertingNotificationManager.AlertEntry
        public void reset() {
            super.reset();
            this.expanded = false;
            this.remoteInputActive = false;
        }

        @Override // com.android.systemui.statusbar.AlertingNotificationManager.AlertEntry
        protected long calculatePostTime() {
            return super.calculatePostTime() + HeadsUpManager.this.mTouchAcceptanceDelay;
        }

        @Override // com.android.systemui.statusbar.AlertingNotificationManager.AlertEntry
        protected long calculateFinishTime() {
            return this.mPostTime + getRecommendedHeadsUpTimeoutMs(((AlertingNotificationManager) HeadsUpManager.this).mAutoDismissNotificationDecay);
        }

        protected int getRecommendedHeadsUpTimeoutMs(int i) {
            return HeadsUpManager.this.mAccessibilityMgr.getRecommendedTimeoutMillis(i, 7);
        }
    }
}
