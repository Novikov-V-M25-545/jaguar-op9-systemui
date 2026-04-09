package com.android.systemui.statusbar.notification.interruption;

import android.app.Notification;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.hardware.display.AmbientDisplayConfiguration;
import android.os.Handler;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.provider.Telephony;
import android.service.dreams.IDreamManager;
import android.service.notification.StatusBarNotification;
import android.telecom.TelecomManager;
import android.util.EventLog;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.NotificationFilter;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class NotificationInterruptStateProviderImpl implements NotificationInterruptStateProvider {
    private final AmbientDisplayConfiguration mAmbientDisplayConfiguration;
    private final BatteryController mBatteryController;
    private final ContentResolver mContentResolver;
    private Context mContext;
    private final IDreamManager mDreamManager;
    private HeadsUpManager mHeadsUpManager;
    private final ContentObserver mHeadsUpObserver;
    private final NotificationFilter mNotificationFilter;
    private final PowerManager mPowerManager;
    private final StatusBarStateController mStatusBarStateController;
    private TelecomManager mTm;
    private final List<NotificationInterruptSuppressor> mSuppressors = new ArrayList();

    @VisibleForTesting
    protected boolean mUseHeadsUp = false;
    private boolean mSkipHeadsUp = false;
    private boolean mLessBoringHeadsUp = false;

    public NotificationInterruptStateProviderImpl(Context context, ContentResolver contentResolver, PowerManager powerManager, IDreamManager iDreamManager, AmbientDisplayConfiguration ambientDisplayConfiguration, NotificationFilter notificationFilter, BatteryController batteryController, StatusBarStateController statusBarStateController, HeadsUpManager headsUpManager, Handler handler) {
        this.mContext = context;
        this.mTm = (TelecomManager) context.getSystemService("telecom");
        this.mContentResolver = contentResolver;
        this.mPowerManager = powerManager;
        this.mDreamManager = iDreamManager;
        this.mBatteryController = batteryController;
        this.mAmbientDisplayConfiguration = ambientDisplayConfiguration;
        this.mNotificationFilter = notificationFilter;
        this.mStatusBarStateController = statusBarStateController;
        this.mHeadsUpManager = headsUpManager;
        ContentObserver contentObserver = new ContentObserver(handler) { // from class: com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProviderImpl.1
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                NotificationInterruptStateProviderImpl notificationInterruptStateProviderImpl = NotificationInterruptStateProviderImpl.this;
                boolean z2 = notificationInterruptStateProviderImpl.mUseHeadsUp;
                notificationInterruptStateProviderImpl.mUseHeadsUp = Settings.Global.getInt(notificationInterruptStateProviderImpl.mContentResolver, "heads_up_notifications_enabled", 0) != 0;
                StringBuilder sb = new StringBuilder();
                sb.append("heads up is ");
                sb.append(NotificationInterruptStateProviderImpl.this.mUseHeadsUp ? "enabled" : "disabled");
                Log.d("InterruptionStateProvider", sb.toString());
                boolean z3 = NotificationInterruptStateProviderImpl.this.mUseHeadsUp;
                if (z2 == z3 || z3) {
                    return;
                }
                Log.d("InterruptionStateProvider", "dismissing any existing heads up notification on disable event");
                NotificationInterruptStateProviderImpl.this.mHeadsUpManager.releaseAllImmediately();
            }
        };
        this.mHeadsUpObserver = contentObserver;
        contentResolver.registerContentObserver(Settings.Global.getUriFor("heads_up_notifications_enabled"), true, contentObserver);
        contentResolver.registerContentObserver(Settings.Global.getUriFor("ticker_gets_heads_up"), true, contentObserver);
        contentObserver.onChange(true);
    }

    @Override // com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider
    public void addSuppressor(NotificationInterruptSuppressor notificationInterruptSuppressor) {
        this.mSuppressors.add(notificationInterruptSuppressor);
    }

    @Override // com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider
    public boolean shouldBubbleUp(NotificationEntry notificationEntry) {
        if (notificationEntry.isAppLocked()) {
            return false;
        }
        notificationEntry.getSbn();
        return canAlertCommon(notificationEntry) && canAlertAwakeCommon(notificationEntry) && notificationEntry.canBubble() && notificationEntry.getBubbleMetadata() != null && !(notificationEntry.getBubbleMetadata().getShortcutId() == null && notificationEntry.getBubbleMetadata().getIntent() == null);
    }

    @Override // com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider
    public boolean shouldHeadsUp(NotificationEntry notificationEntry) {
        if (this.mStatusBarStateController.isDozing()) {
            return shouldHeadsUpWhenDozing(notificationEntry);
        }
        return shouldHeadsUpWhenAwake(notificationEntry);
    }

    @Override // com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider
    public boolean shouldLaunchFullScreenIntentWhenAdded(NotificationEntry notificationEntry) {
        if (notificationEntry.getSbn().getNotification().fullScreenIntent == null || notificationEntry.shouldSuppressFullScreenIntent() || notificationEntry.getImportance() < 4) {
            return false;
        }
        StatusBarNotification sbn = notificationEntry.getSbn();
        if (sbn.isGroup() && sbn.getNotification().suppressAlertingDueToGrouping()) {
            EventLog.writeEvent(1397638484, "231322873", Integer.valueOf(notificationEntry.getSbn().getUid()), "groupAlertBehavior");
            return false;
        }
        Notification.BubbleMetadata bubbleMetadata = sbn.getNotification().getBubbleMetadata();
        if (bubbleMetadata == null || !bubbleMetadata.isNotificationSuppressed()) {
            return !this.mPowerManager.isInteractive() || isDreaming() || this.mStatusBarStateController.getState() == 1 || !shouldHeadsUp(notificationEntry);
        }
        EventLog.writeEvent(1397638484, "274759612", Integer.valueOf(notificationEntry.getSbn().getUid()), "bubbleMetadata");
        return false;
    }

    private boolean isDreaming() {
        try {
            return this.mDreamManager.isDreaming();
        } catch (RemoteException e) {
            Log.e("InterruptionStateProvider", "Failed to query dream manager.", e);
            return false;
        }
    }

    private boolean shouldHeadsUpWhenAwake(NotificationEntry notificationEntry) {
        if (this.mStatusBarStateController.getState() != 1 && notificationEntry.secureContent()) {
            return false;
        }
        StatusBarNotification sbn = notificationEntry.getSbn();
        if (shouldSkipHeadsUp(sbn) || !this.mUseHeadsUp || !canAlertCommon(notificationEntry) || !canAlertAwakeCommon(notificationEntry) || isSnoozedPackage(sbn)) {
            return false;
        }
        boolean z = this.mStatusBarStateController.getState() == 0;
        if ((notificationEntry.isBubble() && z) || notificationEntry.shouldSuppressPeek() || notificationEntry.getImportance() < 4) {
            return false;
        }
        if (!(this.mPowerManager.isScreenOn() && !isDreaming())) {
            return false;
        }
        for (int i = 0; i < this.mSuppressors.size(); i++) {
            if (this.mSuppressors.get(i).suppressAwakeHeadsUp(notificationEntry)) {
                return false;
            }
        }
        return true;
    }

    private boolean shouldHeadsUpWhenDozing(NotificationEntry notificationEntry) {
        notificationEntry.getSbn();
        return this.mAmbientDisplayConfiguration.pulseOnNotificationEnabled(-2) && !this.mBatteryController.isAodPowerSave() && canAlertCommon(notificationEntry) && !notificationEntry.shouldSuppressAmbient() && notificationEntry.getImportance() >= 3;
    }

    @Override // com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider
    public void setGamingPeekMode(boolean z) {
        this.mSkipHeadsUp = z;
    }

    @Override // com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider
    public void setUseLessBoringHeadsUp(boolean z) {
        this.mLessBoringHeadsUp = z;
    }

    public boolean shouldSkipHeadsUp(StatusBarNotification statusBarNotification) {
        String packageName = statusBarNotification.getPackageName();
        if (this.mSkipHeadsUp) {
            return (this.mStatusBarStateController.isDozing() || !this.mSkipHeadsUp || (packageName.equals(getDefaultDialerPackage(this.mTm)) || packageName.toLowerCase().contains("clock"))) ? false : true;
        }
        return (this.mStatusBarStateController.isDozing() || !this.mLessBoringHeadsUp || (packageName.equals(getDefaultDialerPackage(this.mTm)) || packageName.equals(getDefaultSmsPackage(this.mContext)) || packageName.toLowerCase().contains("clock"))) ? false : true;
    }

    private static String getDefaultSmsPackage(Context context) {
        return Telephony.Sms.getDefaultSmsPackage(context);
    }

    private static String getDefaultDialerPackage(TelecomManager telecomManager) {
        return telecomManager != null ? telecomManager.getDefaultDialerPackage() : "";
    }

    private boolean canAlertCommon(NotificationEntry notificationEntry) {
        StatusBarNotification sbn = notificationEntry.getSbn();
        if (this.mNotificationFilter.shouldFilterOut(notificationEntry)) {
            return false;
        }
        if (sbn.isGroup() && sbn.getNotification().suppressAlertingDueToGrouping()) {
            return false;
        }
        for (int i = 0; i < this.mSuppressors.size(); i++) {
            if (this.mSuppressors.get(i).suppressInterruptions(notificationEntry)) {
                return false;
            }
        }
        return !notificationEntry.hasJustLaunchedFullScreenIntent();
    }

    private boolean canAlertAwakeCommon(NotificationEntry notificationEntry) {
        notificationEntry.getSbn();
        for (int i = 0; i < this.mSuppressors.size(); i++) {
            if (this.mSuppressors.get(i).suppressAwakeInterruptions(notificationEntry)) {
                return false;
            }
        }
        return true;
    }

    private boolean isSnoozedPackage(StatusBarNotification statusBarNotification) {
        return this.mHeadsUpManager.isSnoozed(statusBarNotification.getPackageName());
    }
}
