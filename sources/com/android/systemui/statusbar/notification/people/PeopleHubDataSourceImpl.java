package com.android.systemui.statusbar.notification.people;

import android.app.NotificationChannel;
import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.IconDrawableFactory;
import android.util.SparseArray;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.settingslib.notification.ConversationIconFactory;
import com.android.systemui.R;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.notification.NotificationEntryListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: PeopleHubNotificationListener.kt */
/* loaded from: classes.dex */
public final class PeopleHubDataSourceImpl implements DataSource<PeopleHubModel> {
    private final Executor bgExecutor;
    private final List<DataListener<PeopleHubModel>> dataListeners;
    private final NotificationPersonExtractor extractor;
    private final ConversationIconFactory iconFactory;
    private final Executor mainExecutor;
    private final NotificationLockscreenUserManager notifLockscreenUserMgr;
    private final PeopleHubDataSourceImpl$notificationEntryListener$1 notificationEntryListener;
    private final NotificationEntryManager notificationEntryManager;
    private final NotificationListener notificationListener;
    private final SparseArray<PeopleHubManager> peopleHubManagerForUser;
    private final PeopleNotificationIdentifier peopleNotificationIdentifier;
    private final UserManager userManager;

    /* JADX WARN: Type inference failed for: r2v4, types: [com.android.systemui.statusbar.notification.people.PeopleHubDataSourceImpl$notificationEntryListener$1] */
    public PeopleHubDataSourceImpl(@NotNull NotificationEntryManager notificationEntryManager, @NotNull NotificationPersonExtractor extractor, @NotNull UserManager userManager, @NotNull LauncherApps launcherApps, @NotNull PackageManager packageManager, @NotNull Context context, @NotNull NotificationListener notificationListener, @NotNull Executor bgExecutor, @NotNull Executor mainExecutor, @NotNull NotificationLockscreenUserManager notifLockscreenUserMgr, @NotNull PeopleNotificationIdentifier peopleNotificationIdentifier) {
        Intrinsics.checkParameterIsNotNull(notificationEntryManager, "notificationEntryManager");
        Intrinsics.checkParameterIsNotNull(extractor, "extractor");
        Intrinsics.checkParameterIsNotNull(userManager, "userManager");
        Intrinsics.checkParameterIsNotNull(launcherApps, "launcherApps");
        Intrinsics.checkParameterIsNotNull(packageManager, "packageManager");
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(notificationListener, "notificationListener");
        Intrinsics.checkParameterIsNotNull(bgExecutor, "bgExecutor");
        Intrinsics.checkParameterIsNotNull(mainExecutor, "mainExecutor");
        Intrinsics.checkParameterIsNotNull(notifLockscreenUserMgr, "notifLockscreenUserMgr");
        Intrinsics.checkParameterIsNotNull(peopleNotificationIdentifier, "peopleNotificationIdentifier");
        this.notificationEntryManager = notificationEntryManager;
        this.extractor = extractor;
        this.userManager = userManager;
        this.notificationListener = notificationListener;
        this.bgExecutor = bgExecutor;
        this.mainExecutor = mainExecutor;
        this.notifLockscreenUserMgr = notifLockscreenUserMgr;
        this.peopleNotificationIdentifier = peopleNotificationIdentifier;
        this.dataListeners = new ArrayList();
        this.peopleHubManagerForUser = new SparseArray<>();
        Context appContext = context.getApplicationContext();
        IconDrawableFactory iconDrawableFactoryNewInstance = IconDrawableFactory.newInstance(appContext);
        Intrinsics.checkExpressionValueIsNotNull(appContext, "appContext");
        this.iconFactory = new ConversationIconFactory(appContext, launcherApps, packageManager, iconDrawableFactoryNewInstance, appContext.getResources().getDimensionPixelSize(R.dimen.notification_guts_conversation_icon_size));
        this.notificationEntryListener = new NotificationEntryListener() { // from class: com.android.systemui.statusbar.notification.people.PeopleHubDataSourceImpl$notificationEntryListener$1
            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onEntryInflated(@NotNull NotificationEntry entry) {
                Intrinsics.checkParameterIsNotNull(entry, "entry");
                this.this$0.addVisibleEntry(entry);
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onEntryReinflated(@NotNull NotificationEntry entry) {
                Intrinsics.checkParameterIsNotNull(entry, "entry");
                this.this$0.addVisibleEntry(entry);
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onPostEntryUpdated(@NotNull NotificationEntry entry) {
                Intrinsics.checkParameterIsNotNull(entry, "entry");
                this.this$0.addVisibleEntry(entry);
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onEntryRemoved(@NotNull NotificationEntry entry, @Nullable NotificationVisibility notificationVisibility, boolean z, int i) {
                Intrinsics.checkParameterIsNotNull(entry, "entry");
                this.this$0.removeVisibleEntry(entry, i);
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void removeVisibleEntry(final NotificationEntry notificationEntry, final int i) {
        NotificationPersonExtractor notificationPersonExtractor = this.extractor;
        StatusBarNotification sbn = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
        String strExtractPersonKey = notificationPersonExtractor.extractPersonKey(sbn);
        if (strExtractPersonKey == null) {
            strExtractPersonKey = extractPersonKey(notificationEntry);
        }
        final String str = strExtractPersonKey;
        if (str != null) {
            StatusBarNotification sbn2 = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn2, "entry.sbn");
            UserHandle user = sbn2.getUser();
            Intrinsics.checkExpressionValueIsNotNull(user, "entry.sbn.user");
            final int identifier = user.getIdentifier();
            this.bgExecutor.execute(new Runnable() { // from class: com.android.systemui.statusbar.notification.people.PeopleHubDataSourceImpl$removeVisibleEntry$$inlined$let$lambda$1
                @Override // java.lang.Runnable
                public final void run() {
                    UserInfo profileParent = this.userManager.getProfileParent(identifier);
                    final int i2 = profileParent != null ? profileParent.id : identifier;
                    this.mainExecutor.execute(new Runnable() { // from class: com.android.systemui.statusbar.notification.people.PeopleHubDataSourceImpl$removeVisibleEntry$$inlined$let$lambda$1.1
                        @Override // java.lang.Runnable
                        public final void run() {
                            PeopleHubDataSourceImpl$removeVisibleEntry$$inlined$let$lambda$1 peopleHubDataSourceImpl$removeVisibleEntry$$inlined$let$lambda$1 = PeopleHubDataSourceImpl$removeVisibleEntry$$inlined$let$lambda$1.this;
                            if (i == 18) {
                                PeopleHubManager peopleHubManager = (PeopleHubManager) this.peopleHubManagerForUser.get(i2);
                                if (peopleHubManager == null || !peopleHubManager.migrateActivePerson(str)) {
                                    return;
                                }
                                this.updateUi();
                                return;
                            }
                            PeopleHubManager peopleHubManager2 = (PeopleHubManager) this.peopleHubManagerForUser.get(i2);
                            if (peopleHubManager2 != null) {
                                peopleHubManager2.removeActivePerson(str);
                            }
                        }
                    });
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void addVisibleEntry(final NotificationEntry notificationEntry) {
        final PersonModel personModelExtractPerson = extractPerson(notificationEntry);
        if (personModelExtractPerson != null) {
            StatusBarNotification sbn = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
            UserHandle user = sbn.getUser();
            Intrinsics.checkExpressionValueIsNotNull(user, "entry.sbn.user");
            final int identifier = user.getIdentifier();
            this.bgExecutor.execute(new Runnable() { // from class: com.android.systemui.statusbar.notification.people.PeopleHubDataSourceImpl$addVisibleEntry$$inlined$let$lambda$1
                @Override // java.lang.Runnable
                public final void run() {
                    UserInfo profileParent = this.userManager.getProfileParent(identifier);
                    final int i = profileParent != null ? profileParent.id : identifier;
                    this.mainExecutor.execute(new Runnable() { // from class: com.android.systemui.statusbar.notification.people.PeopleHubDataSourceImpl$addVisibleEntry$$inlined$let$lambda$1.1
                        @Override // java.lang.Runnable
                        public final void run() {
                            PeopleHubManager peopleHubManager = (PeopleHubManager) this.peopleHubManagerForUser.get(i);
                            if (peopleHubManager == null) {
                                peopleHubManager = new PeopleHubManager();
                                this.peopleHubManagerForUser.put(i, peopleHubManager);
                            }
                            if (peopleHubManager.addActivePerson(personModelExtractPerson)) {
                                this.updateUi();
                            }
                        }
                    });
                }
            });
        }
    }

    private final PeopleHubModel getPeopleHubModelForCurrentUser() {
        PeopleHubModel peopleHubModel;
        PeopleHubManager peopleHubManager = this.peopleHubManagerForUser.get(this.notifLockscreenUserMgr.getCurrentUserId());
        if (peopleHubManager == null || (peopleHubModel = peopleHubManager.getPeopleHubModel()) == null) {
            return null;
        }
        SparseArray<UserInfo> currentProfiles = this.notifLockscreenUserMgr.getCurrentProfiles();
        Collection<PersonModel> people = peopleHubModel.getPeople();
        ArrayList arrayList = new ArrayList();
        for (Object obj : people) {
            UserInfo userInfo = currentProfiles.get(((PersonModel) obj).getUserId());
            if ((userInfo == null || userInfo.isQuietModeEnabled()) ? false : true) {
                arrayList.add(obj);
            }
        }
        return peopleHubModel.copy(arrayList);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updateUi() {
        PeopleHubModel peopleHubModelForCurrentUser = getPeopleHubModelForCurrentUser();
        if (peopleHubModelForCurrentUser != null) {
            Iterator<DataListener<PeopleHubModel>> it = this.dataListeners.iterator();
            while (it.hasNext()) {
                it.next().onDataChanged(peopleHubModelForCurrentUser);
            }
        }
    }

    private final PersonModel extractPerson(@NotNull final NotificationEntry notificationEntry) {
        CharSequence charSequence;
        Drawable drawable;
        PeopleNotificationIdentifier peopleNotificationIdentifier = this.peopleNotificationIdentifier;
        StatusBarNotification sbn = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "sbn");
        NotificationListenerService.Ranking ranking = notificationEntry.getRanking();
        Intrinsics.checkExpressionValueIsNotNull(ranking, "ranking");
        if (peopleNotificationIdentifier.getPeopleNotificationType(sbn, ranking) == 0) {
            return null;
        }
        Runnable runnable = new Runnable() { // from class: com.android.systemui.statusbar.notification.people.PeopleHubDataSourceImpl$extractPerson$clickRunnable$1
            @Override // java.lang.Runnable
            public final void run() {
                this.this$0.notificationListener.unsnoozeNotification(notificationEntry.getKey());
            }
        };
        StatusBarNotification sbn2 = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn2, "sbn");
        Bundle bundle = sbn2.getNotification().extras;
        NotificationListenerService.Ranking ranking2 = notificationEntry.getRanking();
        Intrinsics.checkExpressionValueIsNotNull(ranking2, "ranking");
        ShortcutInfo shortcutInfo = ranking2.getShortcutInfo();
        if (shortcutInfo == null || (charSequence = shortcutInfo.getLabel()) == null) {
            charSequence = bundle.getCharSequence("android.conversationTitle");
        }
        CharSequence charSequence2 = charSequence != null ? charSequence : bundle.getCharSequence("android.title");
        if (charSequence2 == null) {
            return null;
        }
        NotificationListenerService.Ranking ranking3 = notificationEntry.getRanking();
        Intrinsics.checkExpressionValueIsNotNull(ranking3, "ranking");
        ConversationIconFactory conversationIconFactory = this.iconFactory;
        StatusBarNotification sbn3 = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn3, "sbn");
        Drawable icon = getIcon(ranking3, conversationIconFactory, sbn3);
        if (icon != null) {
            drawable = icon;
        } else {
            ConversationIconFactory conversationIconFactory2 = this.iconFactory;
            Drawable drawableExtractAvatarFromRow = PeopleHubNotificationListenerKt.extractAvatarFromRow(notificationEntry);
            StatusBarNotification sbn4 = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn4, "sbn");
            String packageName = sbn4.getPackageName();
            StatusBarNotification sbn5 = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn5, "sbn");
            int uid = sbn5.getUid();
            NotificationListenerService.Ranking ranking4 = notificationEntry.getRanking();
            Intrinsics.checkExpressionValueIsNotNull(ranking4, "ranking");
            NotificationChannel channel = ranking4.getChannel();
            Intrinsics.checkExpressionValueIsNotNull(channel, "ranking.channel");
            drawable = conversationIconFactory2.getConversationDrawable(drawableExtractAvatarFromRow, packageName, uid, channel.isImportantConversation());
        }
        String key = notificationEntry.getKey();
        Intrinsics.checkExpressionValueIsNotNull(key, "key");
        StatusBarNotification sbn6 = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn6, "sbn");
        UserHandle user = sbn6.getUser();
        Intrinsics.checkExpressionValueIsNotNull(user, "sbn.user");
        int identifier = user.getIdentifier();
        Intrinsics.checkExpressionValueIsNotNull(drawable, "drawable");
        return new PersonModel(key, identifier, charSequence2, drawable, runnable);
    }

    private final Drawable getIcon(@NotNull NotificationListenerService.Ranking ranking, ConversationIconFactory conversationIconFactory, StatusBarNotification statusBarNotification) {
        ShortcutInfo shortcutInfo = ranking.getShortcutInfo();
        if (shortcutInfo == null) {
            return null;
        }
        String packageName = statusBarNotification.getPackageName();
        int uid = statusBarNotification.getUid();
        NotificationChannel channel = ranking.getChannel();
        Intrinsics.checkExpressionValueIsNotNull(channel, "channel");
        return conversationIconFactory.getConversationDrawable(shortcutInfo, packageName, uid, channel.isImportantConversation());
    }

    private final String extractPersonKey(@NotNull NotificationEntry notificationEntry) {
        PeopleNotificationIdentifier peopleNotificationIdentifier = this.peopleNotificationIdentifier;
        StatusBarNotification sbn = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "sbn");
        NotificationListenerService.Ranking ranking = notificationEntry.getRanking();
        Intrinsics.checkExpressionValueIsNotNull(ranking, "ranking");
        if (peopleNotificationIdentifier.getPeopleNotificationType(sbn, ranking) != 0) {
            return notificationEntry.getKey();
        }
        return null;
    }
}
