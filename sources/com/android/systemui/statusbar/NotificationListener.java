package com.android.systemui.statusbar;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import com.android.systemui.statusbar.phone.NotificationListenerWithPlugins;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressLint({"OverrideAbstract"})
/* loaded from: classes.dex */
public class NotificationListener extends NotificationListenerWithPlugins {
    private final Context mContext;
    private final Handler mMainHandler;
    private final NotificationManager mNotificationManager;
    private final List<NotificationHandler> mNotificationHandlers = new ArrayList();
    private final ArrayList<NotificationSettingsListener> mSettingsListeners = new ArrayList<>();

    public interface NotificationHandler {
        void onNotificationPosted(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap);

        void onNotificationRankingUpdate(NotificationListenerService.RankingMap rankingMap);

        void onNotificationRemoved(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap, int i);

        void onNotificationsInitialized();
    }

    public interface NotificationSettingsListener {
        default void onStatusBarIconsBehaviorChanged(boolean z) {
        }
    }

    public NotificationListener(Context context, NotificationManager notificationManager, Handler handler) {
        this.mContext = context;
        this.mNotificationManager = notificationManager;
        this.mMainHandler = handler;
    }

    public void addNotificationHandler(NotificationHandler notificationHandler) {
        if (this.mNotificationHandlers.contains(notificationHandler)) {
            throw new IllegalArgumentException("Listener is already added");
        }
        this.mNotificationHandlers.add(notificationHandler);
    }

    public void addNotificationSettingsListener(NotificationSettingsListener notificationSettingsListener) {
        this.mSettingsListeners.add(notificationSettingsListener);
    }

    @Override // android.service.notification.NotificationListenerService
    public void onListenerConnected() {
        onPluginConnected();
        final StatusBarNotification[] activeNotifications = getActiveNotifications();
        if (activeNotifications == null) {
            Log.w("NotificationListener", "onListenerConnected unable to get active notifications.");
            return;
        }
        final NotificationListenerService.RankingMap currentRanking = getCurrentRanking();
        this.mMainHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.NotificationListener$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$onListenerConnected$0(activeNotifications, currentRanking);
            }
        });
        onSilentStatusBarIconsVisibilityChanged(this.mNotificationManager.shouldHideSilentStatusBarIcons());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onListenerConnected$0(StatusBarNotification[] statusBarNotificationArr, NotificationListenerService.RankingMap rankingMap) {
        ArrayList arrayList = new ArrayList();
        for (StatusBarNotification statusBarNotification : statusBarNotificationArr) {
            arrayList.add(getRankingOrTemporaryStandIn(rankingMap, statusBarNotification.getKey()));
        }
        NotificationListenerService.RankingMap rankingMap2 = new NotificationListenerService.RankingMap((NotificationListenerService.Ranking[]) arrayList.toArray(new NotificationListenerService.Ranking[0]));
        for (StatusBarNotification statusBarNotification2 : statusBarNotificationArr) {
            Iterator<NotificationHandler> it = this.mNotificationHandlers.iterator();
            while (it.hasNext()) {
                it.next().onNotificationPosted(statusBarNotification2, rankingMap2);
            }
        }
        Iterator<NotificationHandler> it2 = this.mNotificationHandlers.iterator();
        while (it2.hasNext()) {
            it2.next().onNotificationsInitialized();
        }
    }

    @Override // android.service.notification.NotificationListenerService
    public void onNotificationPosted(final StatusBarNotification statusBarNotification, final NotificationListenerService.RankingMap rankingMap) {
        if (statusBarNotification == null || onPluginNotificationPosted(statusBarNotification, rankingMap)) {
            return;
        }
        this.mMainHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.NotificationListener$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$onNotificationPosted$1(statusBarNotification, rankingMap);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onNotificationPosted$1(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap) {
        RemoteInputController.processForRemoteInput(statusBarNotification.getNotification(), this.mContext);
        Iterator<NotificationHandler> it = this.mNotificationHandlers.iterator();
        while (it.hasNext()) {
            it.next().onNotificationPosted(statusBarNotification, rankingMap);
        }
    }

    @Override // android.service.notification.NotificationListenerService
    public void onNotificationRemoved(final StatusBarNotification statusBarNotification, final NotificationListenerService.RankingMap rankingMap, final int i) {
        if (statusBarNotification == null || onPluginNotificationRemoved(statusBarNotification, rankingMap)) {
            return;
        }
        this.mMainHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.NotificationListener$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$onNotificationRemoved$2(statusBarNotification, rankingMap, i);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onNotificationRemoved$2(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap, int i) {
        Iterator<NotificationHandler> it = this.mNotificationHandlers.iterator();
        while (it.hasNext()) {
            it.next().onNotificationRemoved(statusBarNotification, rankingMap, i);
        }
    }

    @Override // android.service.notification.NotificationListenerService
    public void onNotificationRemoved(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap) {
        onNotificationRemoved(statusBarNotification, rankingMap, 0);
    }

    @Override // android.service.notification.NotificationListenerService
    public void onNotificationRankingUpdate(NotificationListenerService.RankingMap rankingMap) {
        if (rankingMap != null) {
            final NotificationListenerService.RankingMap rankingMapOnPluginRankingUpdate = onPluginRankingUpdate(rankingMap);
            this.mMainHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.NotificationListener$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onNotificationRankingUpdate$3(rankingMapOnPluginRankingUpdate);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onNotificationRankingUpdate$3(NotificationListenerService.RankingMap rankingMap) {
        Iterator<NotificationHandler> it = this.mNotificationHandlers.iterator();
        while (it.hasNext()) {
            it.next().onNotificationRankingUpdate(rankingMap);
        }
    }

    @Override // android.service.notification.NotificationListenerService
    public void onSilentStatusBarIconsVisibilityChanged(boolean z) {
        Iterator<NotificationSettingsListener> it = this.mSettingsListeners.iterator();
        while (it.hasNext()) {
            it.next().onStatusBarIconsBehaviorChanged(z);
        }
    }

    public final void unsnoozeNotification(String str) {
        if (isBound()) {
            try {
                getNotificationInterface().unsnoozeNotificationFromSystemListener(((NotificationListenerService) this).mWrapper, str);
            } catch (RemoteException e) {
                Log.v("NotificationListener", "Unable to contact notification manager", e);
            }
        }
    }

    public void registerAsSystemService() {
        try {
            registerAsSystemService(this.mContext, new ComponentName(this.mContext.getPackageName(), NotificationListener.class.getCanonicalName()), -1);
        } catch (RemoteException e) {
            Log.e("NotificationListener", "Unable to register notification listener", e);
        }
    }

    private static NotificationListenerService.Ranking getRankingOrTemporaryStandIn(NotificationListenerService.RankingMap rankingMap, String str) {
        NotificationListenerService.Ranking ranking = new NotificationListenerService.Ranking();
        if (rankingMap.getRanking(str, ranking)) {
            return ranking;
        }
        ranking.populate(str, 0, false, 0, 0, 0, null, null, null, new ArrayList(), new ArrayList(), false, 0, false, 0L, false, new ArrayList(), new ArrayList(), false, false, false, null, false);
        return ranking;
    }
}
