package com.android.systemui.media;

import android.app.Notification;
import android.service.notification.StatusBarNotification;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MediaDataManager.kt */
/* loaded from: classes.dex */
public final class MediaDataManagerKt {
    private static final String[] ART_URIS = {"android.media.metadata.ALBUM_ART_URI", "android.media.metadata.ART_URI", "android.media.metadata.DISPLAY_ICON_URI"};
    private static final MediaData LOADING = new MediaData(-1, false, 0, null, null, null, null, null, CollectionsKt__CollectionsKt.emptyList(), CollectionsKt__CollectionsKt.emptyList(), "INVALID", null, null, null, true, null, false, false, null, false, null, false, 4128768, null);

    public static final boolean isMediaNotification(@NotNull StatusBarNotification sbn) {
        Intrinsics.checkParameterIsNotNull(sbn, "sbn");
        if (!sbn.getNotification().hasMediaSession()) {
            return false;
        }
        Notification notification = sbn.getNotification();
        Intrinsics.checkExpressionValueIsNotNull(notification, "sbn.notification");
        Class notificationStyle = notification.getNotificationStyle();
        return Notification.DecoratedMediaCustomViewStyle.class.equals(notificationStyle) || Notification.MediaStyle.class.equals(notificationStyle);
    }
}
