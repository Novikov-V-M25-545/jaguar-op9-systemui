package com.android.systemui.media.dialog;

import android.content.Context;
import android.media.MediaRouter2Manager;
import android.media.session.MediaSessionManager;
import com.android.internal.logging.UiEventLogger;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.phone.ShadeController;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaOutputDialogFactory.kt */
/* loaded from: classes.dex */
public final class MediaOutputDialogFactory {
    public static final Companion Companion = new Companion(null);

    @Nullable
    private static MediaOutputDialog mediaOutputDialog;
    private final Context context;
    private final LocalBluetoothManager lbm;
    private final MediaSessionManager mediaSessionManager;
    private final NotificationEntryManager notificationEntryManager;
    private final MediaRouter2Manager routerManager;
    private final ShadeController shadeController;
    private final ActivityStarter starter;
    private final UiEventLogger uiEventLogger;

    public MediaOutputDialogFactory(@NotNull Context context, @NotNull MediaSessionManager mediaSessionManager, @Nullable LocalBluetoothManager localBluetoothManager, @NotNull ShadeController shadeController, @NotNull ActivityStarter starter, @NotNull NotificationEntryManager notificationEntryManager, @NotNull UiEventLogger uiEventLogger, @NotNull MediaRouter2Manager routerManager) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(mediaSessionManager, "mediaSessionManager");
        Intrinsics.checkParameterIsNotNull(shadeController, "shadeController");
        Intrinsics.checkParameterIsNotNull(starter, "starter");
        Intrinsics.checkParameterIsNotNull(notificationEntryManager, "notificationEntryManager");
        Intrinsics.checkParameterIsNotNull(uiEventLogger, "uiEventLogger");
        Intrinsics.checkParameterIsNotNull(routerManager, "routerManager");
        this.context = context;
        this.mediaSessionManager = mediaSessionManager;
        this.lbm = localBluetoothManager;
        this.shadeController = shadeController;
        this.starter = starter;
        this.notificationEntryManager = notificationEntryManager;
        this.uiEventLogger = uiEventLogger;
        this.routerManager = routerManager;
    }

    /* compiled from: MediaOutputDialogFactory.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    public final void create(@NotNull String packageName, boolean z) {
        Intrinsics.checkParameterIsNotNull(packageName, "packageName");
        MediaOutputDialog mediaOutputDialog2 = mediaOutputDialog;
        if (mediaOutputDialog2 != null) {
            mediaOutputDialog2.dismiss();
        }
        mediaOutputDialog = new MediaOutputDialog(this.context, z, new MediaOutputController(this.context, packageName, z, this.mediaSessionManager, this.lbm, this.shadeController, this.starter, this.notificationEntryManager, this.uiEventLogger, this.routerManager), this.uiEventLogger);
    }

    public final void dismiss() {
        MediaOutputDialog mediaOutputDialog2 = mediaOutputDialog;
        if (mediaOutputDialog2 != null) {
            mediaOutputDialog2.dismiss();
        }
        mediaOutputDialog = null;
    }
}
