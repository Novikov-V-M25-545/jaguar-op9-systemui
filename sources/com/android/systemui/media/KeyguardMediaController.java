package com.android.systemui.media;

import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.notification.stack.MediaHeaderView;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: KeyguardMediaController.kt */
/* loaded from: classes.dex */
public final class KeyguardMediaController {
    private final KeyguardBypassController bypassController;
    private final MediaHost mediaHost;
    private final NotificationLockscreenUserManager notifLockscreenUserManager;
    private final SysuiStatusBarStateController statusBarStateController;

    @Nullable
    private MediaHeaderView view;

    @Nullable
    private Function1<? super Boolean, Unit> visibilityChangedListener;

    public KeyguardMediaController(@NotNull MediaHost mediaHost, @NotNull KeyguardBypassController bypassController, @NotNull SysuiStatusBarStateController statusBarStateController, @NotNull NotificationLockscreenUserManager notifLockscreenUserManager) {
        Intrinsics.checkParameterIsNotNull(mediaHost, "mediaHost");
        Intrinsics.checkParameterIsNotNull(bypassController, "bypassController");
        Intrinsics.checkParameterIsNotNull(statusBarStateController, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(notifLockscreenUserManager, "notifLockscreenUserManager");
        this.mediaHost = mediaHost;
        this.bypassController = bypassController;
        this.statusBarStateController = statusBarStateController;
        this.notifLockscreenUserManager = notifLockscreenUserManager;
        statusBarStateController.addCallback(new StatusBarStateController.StateListener() { // from class: com.android.systemui.media.KeyguardMediaController.1
            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onStateChanged(int i) {
                KeyguardMediaController.this.updateVisibility();
            }
        });
    }

    public final void setVisibilityChangedListener(@Nullable Function1<? super Boolean, Unit> function1) {
        this.visibilityChangedListener = function1;
    }

    @Nullable
    public final MediaHeaderView getView() {
        return this.view;
    }

    public final void attach(@NotNull MediaHeaderView mediaView) {
        Intrinsics.checkParameterIsNotNull(mediaView, "mediaView");
        this.view = mediaView;
        this.mediaHost.addVisibilityChangeListener(new Function1<Boolean, Unit>() { // from class: com.android.systemui.media.KeyguardMediaController.attach.1
            {
                super(1);
            }

            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Unit invoke(Boolean bool) {
                invoke(bool.booleanValue());
                return Unit.INSTANCE;
            }

            public final void invoke(boolean z) {
                KeyguardMediaController.this.updateVisibility();
            }
        });
        this.mediaHost.setExpansion(0.0f);
        this.mediaHost.setShowsOnlyActiveMedia(true);
        this.mediaHost.setFalsingProtectionNeeded(true);
        this.mediaHost.init(2);
        mediaView.setContentView(this.mediaHost.getHostView());
        updateVisibility();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updateVisibility() {
        Function1<? super Boolean, Unit> function1;
        boolean z = this.mediaHost.getVisible() && !this.bypassController.getBypassEnabled() && (this.statusBarStateController.getState() == 1 || this.statusBarStateController.getState() == 3) && this.notifLockscreenUserManager.shouldShowLockscreenNotifications();
        MediaHeaderView mediaHeaderView = this.view;
        int visibility = mediaHeaderView != null ? mediaHeaderView.getVisibility() : 8;
        int i = z ? 0 : 8;
        MediaHeaderView mediaHeaderView2 = this.view;
        if (mediaHeaderView2 != null) {
            mediaHeaderView2.setVisibility(i);
        }
        if (visibility == i || (function1 = this.visibilityChangedListener) == null) {
            return;
        }
        function1.invoke(Boolean.valueOf(z));
    }
}
