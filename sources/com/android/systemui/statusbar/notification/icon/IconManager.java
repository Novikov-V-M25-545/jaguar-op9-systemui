package com.android.systemui.statusbar.notification.icon;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.Person;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.UserHandle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.ImageView;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.R;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.notification.InflationException;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.notifcollection.CommonNotifCollection;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import java.util.List;
import kotlin.Pair;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: IconManager.kt */
/* loaded from: classes.dex */
public final class IconManager {
    private final IconManager$entryListener$1 entryListener;
    private final IconBuilder iconBuilder;
    private final LauncherApps launcherApps;
    private final CommonNotifCollection notifCollection;
    private final NotificationEntry.OnSensitivityChangedListener sensitivityListener;

    /* JADX WARN: Type inference failed for: r2v1, types: [com.android.systemui.statusbar.notification.icon.IconManager$entryListener$1] */
    public IconManager(@NotNull CommonNotifCollection notifCollection, @NotNull LauncherApps launcherApps, @NotNull IconBuilder iconBuilder) {
        Intrinsics.checkParameterIsNotNull(notifCollection, "notifCollection");
        Intrinsics.checkParameterIsNotNull(launcherApps, "launcherApps");
        Intrinsics.checkParameterIsNotNull(iconBuilder, "iconBuilder");
        this.notifCollection = notifCollection;
        this.launcherApps = launcherApps;
        this.iconBuilder = iconBuilder;
        this.entryListener = new NotifCollectionListener() { // from class: com.android.systemui.statusbar.notification.icon.IconManager$entryListener$1
            @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
            public void onEntryInit(@NotNull NotificationEntry entry) {
                Intrinsics.checkParameterIsNotNull(entry, "entry");
                entry.addOnSensitivityChangedListener(this.this$0.sensitivityListener);
            }

            @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
            public void onEntryCleanUp(@NotNull NotificationEntry entry) {
                Intrinsics.checkParameterIsNotNull(entry, "entry");
                entry.removeOnSensitivityChangedListener(this.this$0.sensitivityListener);
            }

            @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
            public void onRankingApplied() {
                for (NotificationEntry entry : this.this$0.notifCollection.getAllNotifs()) {
                    IconManager iconManager = this.this$0;
                    Intrinsics.checkExpressionValueIsNotNull(entry, "entry");
                    boolean zIsImportantConversation = iconManager.isImportantConversation(entry);
                    IconPack icons = entry.getIcons();
                    Intrinsics.checkExpressionValueIsNotNull(icons, "entry.icons");
                    if (icons.getAreIconsAvailable()) {
                        IconPack icons2 = entry.getIcons();
                        Intrinsics.checkExpressionValueIsNotNull(icons2, "entry.icons");
                        if (zIsImportantConversation != icons2.isImportantConversation()) {
                            this.this$0.updateIconsSafe(entry);
                        }
                    }
                    IconPack icons3 = entry.getIcons();
                    Intrinsics.checkExpressionValueIsNotNull(icons3, "entry.icons");
                    icons3.setImportantConversation(zIsImportantConversation);
                }
            }
        };
        this.sensitivityListener = new NotificationEntry.OnSensitivityChangedListener() { // from class: com.android.systemui.statusbar.notification.icon.IconManager$sensitivityListener$1
            @Override // com.android.systemui.statusbar.notification.collection.NotificationEntry.OnSensitivityChangedListener
            public final void onSensitivityChanged(@NotNull NotificationEntry entry) {
                Intrinsics.checkParameterIsNotNull(entry, "entry");
                this.this$0.updateIconsSafe(entry);
            }
        };
    }

    public final void attach() {
        this.notifCollection.addCollectionListener(this.entryListener);
    }

    public final void createIcons(@NotNull final NotificationEntry entry) throws InflationException {
        StatusBarIconView statusBarIconViewCreateIconView;
        Intrinsics.checkParameterIsNotNull(entry, "entry");
        StatusBarIconView statusBarIconViewCreateIconView2 = this.iconBuilder.createIconView(entry);
        statusBarIconViewCreateIconView2.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        StatusBarIconView statusBarIconViewCreateIconView3 = this.iconBuilder.createIconView(entry);
        statusBarIconViewCreateIconView3.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        statusBarIconViewCreateIconView3.setOnVisibilityChangedListener(new StatusBarIconView.OnVisibilityChangedListener() { // from class: com.android.systemui.statusbar.notification.icon.IconManager.createIcons.1
            @Override // com.android.systemui.statusbar.StatusBarIconView.OnVisibilityChangedListener
            public final void onVisibilityChanged(int i) {
                entry.setShelfIconVisible(i == 0);
            }
        });
        statusBarIconViewCreateIconView3.setVisibility(4);
        StatusBarIconView statusBarIconViewCreateIconView4 = this.iconBuilder.createIconView(entry);
        statusBarIconViewCreateIconView4.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        statusBarIconViewCreateIconView4.setIncreasedSize(true);
        StatusBarNotification sbn = entry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
        Notification notification = sbn.getNotification();
        Intrinsics.checkExpressionValueIsNotNull(notification, "entry.sbn.notification");
        if (notification.isMediaNotification()) {
            statusBarIconViewCreateIconView = this.iconBuilder.createIconView(entry);
            statusBarIconViewCreateIconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        } else {
            statusBarIconViewCreateIconView = null;
        }
        Pair<StatusBarIcon, StatusBarIcon> iconDescriptors = getIconDescriptors(entry);
        StatusBarIcon statusBarIconComponent1 = iconDescriptors.component1();
        StatusBarIcon statusBarIconComponent2 = iconDescriptors.component2();
        try {
            setIcon(entry, statusBarIconComponent1, statusBarIconViewCreateIconView2);
            setIcon(entry, statusBarIconComponent2, statusBarIconViewCreateIconView3);
            setIcon(entry, statusBarIconComponent2, statusBarIconViewCreateIconView4);
            if (statusBarIconViewCreateIconView != null) {
                setIcon(entry, statusBarIconComponent1, statusBarIconViewCreateIconView);
            }
            entry.setIcons(IconPack.buildPack(statusBarIconViewCreateIconView2, statusBarIconViewCreateIconView3, statusBarIconViewCreateIconView4, statusBarIconViewCreateIconView, entry.getIcons()));
        } catch (InflationException e) {
            entry.setIcons(IconPack.buildEmptyPack(entry.getIcons()));
            throw e;
        }
    }

    public final void updateIcons(@NotNull NotificationEntry entry) throws InflationException {
        Intrinsics.checkParameterIsNotNull(entry, "entry");
        IconPack icons = entry.getIcons();
        Intrinsics.checkExpressionValueIsNotNull(icons, "entry.icons");
        if (icons.getAreIconsAvailable()) {
            IconPack icons2 = entry.getIcons();
            Intrinsics.checkExpressionValueIsNotNull(icons2, "entry.icons");
            icons2.setSmallIconDescriptor(null);
            IconPack icons3 = entry.getIcons();
            Intrinsics.checkExpressionValueIsNotNull(icons3, "entry.icons");
            icons3.setPeopleAvatarDescriptor(null);
            Pair<StatusBarIcon, StatusBarIcon> iconDescriptors = getIconDescriptors(entry);
            StatusBarIcon statusBarIconComponent1 = iconDescriptors.component1();
            StatusBarIcon statusBarIconComponent2 = iconDescriptors.component2();
            IconPack icons4 = entry.getIcons();
            Intrinsics.checkExpressionValueIsNotNull(icons4, "entry.icons");
            StatusBarIconView it = icons4.getStatusBarIcon();
            if (it != null) {
                Intrinsics.checkExpressionValueIsNotNull(it, "it");
                it.setNotification(entry.getSbn());
                setIcon(entry, statusBarIconComponent1, it);
            }
            IconPack icons5 = entry.getIcons();
            Intrinsics.checkExpressionValueIsNotNull(icons5, "entry.icons");
            StatusBarIconView it2 = icons5.getShelfIcon();
            if (it2 != null) {
                Intrinsics.checkExpressionValueIsNotNull(it2, "it");
                it2.setNotification(entry.getSbn());
                setIcon(entry, statusBarIconComponent1, it2);
            }
            IconPack icons6 = entry.getIcons();
            Intrinsics.checkExpressionValueIsNotNull(icons6, "entry.icons");
            StatusBarIconView it3 = icons6.getAodIcon();
            if (it3 != null) {
                Intrinsics.checkExpressionValueIsNotNull(it3, "it");
                it3.setNotification(entry.getSbn());
                setIcon(entry, statusBarIconComponent2, it3);
            }
            IconPack icons7 = entry.getIcons();
            Intrinsics.checkExpressionValueIsNotNull(icons7, "entry.icons");
            StatusBarIconView it4 = icons7.getCenteredIcon();
            if (it4 != null) {
                Intrinsics.checkExpressionValueIsNotNull(it4, "it");
                it4.setNotification(entry.getSbn());
                setIcon(entry, statusBarIconComponent2, it4);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updateIconsSafe(NotificationEntry notificationEntry) {
        try {
            updateIcons(notificationEntry);
        } catch (InflationException e) {
            Log.e("IconManager", "Unable to update icon", e);
        }
    }

    private final Pair<StatusBarIcon, StatusBarIcon> getIconDescriptors(NotificationEntry notificationEntry) throws InflationException {
        StatusBarIcon iconDescriptor = getIconDescriptor(notificationEntry, false);
        return new Pair<>(iconDescriptor, notificationEntry.isSensitive() ? getIconDescriptor(notificationEntry, true) : iconDescriptor);
    }

    private final StatusBarIcon getIconDescriptor(NotificationEntry notificationEntry, boolean z) throws InflationException {
        Icon smallIcon;
        StatusBarNotification sbn = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
        Notification n = sbn.getNotification();
        boolean z2 = isImportantConversation(notificationEntry) && !z;
        IconPack icons = notificationEntry.getIcons();
        Intrinsics.checkExpressionValueIsNotNull(icons, "entry.icons");
        StatusBarIcon peopleAvatarDescriptor = icons.getPeopleAvatarDescriptor();
        IconPack icons2 = notificationEntry.getIcons();
        Intrinsics.checkExpressionValueIsNotNull(icons2, "entry.icons");
        StatusBarIcon smallIconDescriptor = icons2.getSmallIconDescriptor();
        if (z2 && peopleAvatarDescriptor != null) {
            return peopleAvatarDescriptor;
        }
        if (!z2 && smallIconDescriptor != null) {
            return smallIconDescriptor;
        }
        if (z2) {
            smallIcon = createPeopleAvatar(notificationEntry);
        } else {
            Intrinsics.checkExpressionValueIsNotNull(n, "n");
            smallIcon = n.getSmallIcon();
        }
        Icon icon = smallIcon;
        if (icon == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("No icon in notification from ");
            StatusBarNotification sbn2 = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn2, "entry.sbn");
            sb.append(sbn2.getPackageName());
            throw new InflationException(sb.toString());
        }
        StatusBarNotification sbn3 = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn3, "entry.sbn");
        UserHandle user = sbn3.getUser();
        StatusBarNotification sbn4 = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn4, "entry.sbn");
        String packageName = sbn4.getPackageName();
        int i = n.iconLevel;
        int i2 = n.number;
        IconBuilder iconBuilder = this.iconBuilder;
        Intrinsics.checkExpressionValueIsNotNull(n, "n");
        StatusBarIcon statusBarIcon = new StatusBarIcon(user, packageName, icon, i, i2, iconBuilder.getIconContentDescription(n));
        if (isImportantConversation(notificationEntry)) {
            if (z2) {
                IconPack icons3 = notificationEntry.getIcons();
                Intrinsics.checkExpressionValueIsNotNull(icons3, "entry.icons");
                icons3.setPeopleAvatarDescriptor(statusBarIcon);
            } else {
                IconPack icons4 = notificationEntry.getIcons();
                Intrinsics.checkExpressionValueIsNotNull(icons4, "entry.icons");
                icons4.setSmallIconDescriptor(statusBarIcon);
            }
        }
        return statusBarIcon;
    }

    private final void setIcon(NotificationEntry notificationEntry, StatusBarIcon statusBarIcon, StatusBarIconView statusBarIconView) throws InflationException {
        statusBarIconView.setShowsConversation(showsConversation(notificationEntry, statusBarIconView, statusBarIcon));
        statusBarIconView.setTag(R.id.icon_is_pre_L, Boolean.valueOf(notificationEntry.targetSdk < 21));
        if (statusBarIconView.set(statusBarIcon)) {
            return;
        }
        throw new InflationException("Couldn't create icon " + statusBarIcon);
    }

    private final Icon createPeopleAvatar(NotificationEntry notificationEntry) throws InflationException {
        NotificationListenerService.Ranking ranking = notificationEntry.getRanking();
        Intrinsics.checkExpressionValueIsNotNull(ranking, "entry.ranking");
        ShortcutInfo shortcutInfo = ranking.getShortcutInfo();
        Icon shortcutIcon = shortcutInfo != null ? this.launcherApps.getShortcutIcon(shortcutInfo) : null;
        if (shortcutIcon == null) {
            StatusBarNotification sbn = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
            Bundle bundle = sbn.getNotification().extras;
            Intrinsics.checkExpressionValueIsNotNull(bundle, "entry.sbn.notification.extras");
            List<Notification.MessagingStyle.Message> messages = Notification.MessagingStyle.Message.getMessagesFromBundleArray(bundle.getParcelableArray("android.messages"));
            Person person = (Person) bundle.getParcelable("android.messagingUser");
            Intrinsics.checkExpressionValueIsNotNull(messages, "messages");
            int size = messages.size();
            while (true) {
                size--;
                if (size < 0) {
                    break;
                }
                Notification.MessagingStyle.Message message = messages.get(size);
                Intrinsics.checkExpressionValueIsNotNull(message, "message");
                Person senderPerson = message.getSenderPerson();
                if (senderPerson != null && senderPerson != person) {
                    Person senderPerson2 = message.getSenderPerson();
                    if (senderPerson2 == null) {
                        Intrinsics.throwNpe();
                    }
                    shortcutIcon = senderPerson2.getIcon();
                }
            }
        }
        if (shortcutIcon == null) {
            StatusBarNotification sbn2 = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn2, "entry.sbn");
            shortcutIcon = sbn2.getNotification().getLargeIcon();
        }
        if (shortcutIcon == null) {
            StatusBarNotification sbn3 = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn3, "entry.sbn");
            Notification notification = sbn3.getNotification();
            Intrinsics.checkExpressionValueIsNotNull(notification, "entry.sbn.notification");
            shortcutIcon = notification.getSmallIcon();
        }
        if (shortcutIcon != null) {
            return shortcutIcon;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("No icon in notification from ");
        StatusBarNotification sbn4 = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn4, "entry.sbn");
        sb.append(sbn4.getPackageName());
        throw new InflationException(sb.toString());
    }

    /* JADX WARN: Removed duplicated region for block: B:8:0x0021  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private final boolean showsConversation(com.android.systemui.statusbar.notification.collection.NotificationEntry r5, com.android.systemui.statusbar.StatusBarIconView r6, com.android.internal.statusbar.StatusBarIcon r7) {
        /*
            r4 = this;
            com.android.systemui.statusbar.notification.icon.IconPack r0 = r5.getIcons()
            java.lang.String r1 = "entry.icons"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r0, r1)
            com.android.systemui.statusbar.StatusBarIconView r0 = r0.getShelfIcon()
            r2 = 0
            r3 = 1
            if (r6 == r0) goto L21
            com.android.systemui.statusbar.notification.icon.IconPack r0 = r5.getIcons()
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r0, r1)
            com.android.systemui.statusbar.StatusBarIconView r0 = r0.getAodIcon()
            if (r6 != r0) goto L1f
            goto L21
        L1f:
            r6 = r2
            goto L22
        L21:
            r6 = r3
        L22:
            android.graphics.drawable.Icon r7 = r7.icon
            android.service.notification.StatusBarNotification r0 = r5.getSbn()
            java.lang.String r1 = "entry.sbn"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r0, r1)
            android.app.Notification r0 = r0.getNotification()
            java.lang.String r1 = "entry.sbn.notification"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r0, r1)
            android.graphics.drawable.Icon r0 = r0.getSmallIcon()
            boolean r7 = r7.equals(r0)
            boolean r4 = r4.isImportantConversation(r5)
            if (r4 == 0) goto L4f
            if (r7 != 0) goto L4f
            if (r6 == 0) goto L4e
            boolean r4 = r5.isSensitive()
            if (r4 != 0) goto L4f
        L4e:
            r2 = r3
        L4f:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.icon.IconManager.showsConversation(com.android.systemui.statusbar.notification.collection.NotificationEntry, com.android.systemui.statusbar.StatusBarIconView, com.android.internal.statusbar.StatusBarIcon):boolean");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final boolean isImportantConversation(NotificationEntry notificationEntry) {
        NotificationListenerService.Ranking ranking = notificationEntry.getRanking();
        Intrinsics.checkExpressionValueIsNotNull(ranking, "entry.ranking");
        if (ranking.getChannel() != null) {
            NotificationListenerService.Ranking ranking2 = notificationEntry.getRanking();
            Intrinsics.checkExpressionValueIsNotNull(ranking2, "entry.ranking");
            NotificationChannel channel = ranking2.getChannel();
            Intrinsics.checkExpressionValueIsNotNull(channel, "entry.ranking.channel");
            if (channel.isImportantConversation()) {
                return true;
            }
        }
        return false;
    }
}
