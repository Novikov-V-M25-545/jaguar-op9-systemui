package com.android.systemui.statusbar.notification.init;

import android.service.notification.SnoozeCriterion;
import android.service.notification.StatusBarNotification;
import com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.NotificationPresenter;
import com.android.systemui.statusbar.notification.NotificationActivityStarter;
import com.android.systemui.statusbar.notification.NotificationClicker;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationListController;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.TargetSdkResolver;
import com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinderImpl;
import com.android.systemui.statusbar.notification.collection.init.NotifPipelineInitializer;
import com.android.systemui.statusbar.notification.interruption.HeadsUpController;
import com.android.systemui.statusbar.notification.interruption.HeadsUpViewBinder;
import com.android.systemui.statusbar.notification.row.NotifBindPipelineInitializer;
import com.android.systemui.statusbar.notification.stack.NotificationListContainer;
import com.android.systemui.statusbar.phone.NotificationGroupAlertTransferHelper;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.RemoteInputUriController;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Optional;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotificationsControllerImpl.kt */
/* loaded from: classes.dex */
public final class NotificationsControllerImpl implements NotificationsController {
    private final NotificationClicker.Builder clickerBuilder;
    private final DeviceProvisionedController deviceProvisionedController;
    private final NotificationEntryManager entryManager;
    private final FeatureFlags featureFlags;
    private final NotificationGroupAlertTransferHelper groupAlertTransferHelper;
    private final NotificationGroupManager groupManager;
    private final HeadsUpController headsUpController;
    private final HeadsUpManager headsUpManager;
    private final HeadsUpViewBinder headsUpViewBinder;
    private final Lazy<NotifPipelineInitializer> newNotifPipeline;
    private final NotifBindPipelineInitializer notifBindPipelineInitializer;
    private final Lazy<NotifPipeline> notifPipeline;
    private final NotificationListener notificationListener;
    private final NotificationRowBinderImpl notificationRowBinder;
    private final RemoteInputUriController remoteInputUriController;
    private final TargetSdkResolver targetSdkResolver;

    public NotificationsControllerImpl(@NotNull FeatureFlags featureFlags, @NotNull NotificationListener notificationListener, @NotNull NotificationEntryManager entryManager, @NotNull Lazy<NotifPipeline> notifPipeline, @NotNull TargetSdkResolver targetSdkResolver, @NotNull Lazy<NotifPipelineInitializer> newNotifPipeline, @NotNull NotifBindPipelineInitializer notifBindPipelineInitializer, @NotNull DeviceProvisionedController deviceProvisionedController, @NotNull NotificationRowBinderImpl notificationRowBinder, @NotNull RemoteInputUriController remoteInputUriController, @NotNull NotificationGroupManager groupManager, @NotNull NotificationGroupAlertTransferHelper groupAlertTransferHelper, @NotNull HeadsUpManager headsUpManager, @NotNull HeadsUpController headsUpController, @NotNull HeadsUpViewBinder headsUpViewBinder, @NotNull NotificationClicker.Builder clickerBuilder) {
        Intrinsics.checkParameterIsNotNull(featureFlags, "featureFlags");
        Intrinsics.checkParameterIsNotNull(notificationListener, "notificationListener");
        Intrinsics.checkParameterIsNotNull(entryManager, "entryManager");
        Intrinsics.checkParameterIsNotNull(notifPipeline, "notifPipeline");
        Intrinsics.checkParameterIsNotNull(targetSdkResolver, "targetSdkResolver");
        Intrinsics.checkParameterIsNotNull(newNotifPipeline, "newNotifPipeline");
        Intrinsics.checkParameterIsNotNull(notifBindPipelineInitializer, "notifBindPipelineInitializer");
        Intrinsics.checkParameterIsNotNull(deviceProvisionedController, "deviceProvisionedController");
        Intrinsics.checkParameterIsNotNull(notificationRowBinder, "notificationRowBinder");
        Intrinsics.checkParameterIsNotNull(remoteInputUriController, "remoteInputUriController");
        Intrinsics.checkParameterIsNotNull(groupManager, "groupManager");
        Intrinsics.checkParameterIsNotNull(groupAlertTransferHelper, "groupAlertTransferHelper");
        Intrinsics.checkParameterIsNotNull(headsUpManager, "headsUpManager");
        Intrinsics.checkParameterIsNotNull(headsUpController, "headsUpController");
        Intrinsics.checkParameterIsNotNull(headsUpViewBinder, "headsUpViewBinder");
        Intrinsics.checkParameterIsNotNull(clickerBuilder, "clickerBuilder");
        this.featureFlags = featureFlags;
        this.notificationListener = notificationListener;
        this.entryManager = entryManager;
        this.notifPipeline = notifPipeline;
        this.targetSdkResolver = targetSdkResolver;
        this.newNotifPipeline = newNotifPipeline;
        this.notifBindPipelineInitializer = notifBindPipelineInitializer;
        this.deviceProvisionedController = deviceProvisionedController;
        this.notificationRowBinder = notificationRowBinder;
        this.remoteInputUriController = remoteInputUriController;
        this.groupManager = groupManager;
        this.groupAlertTransferHelper = groupAlertTransferHelper;
        this.headsUpManager = headsUpManager;
        this.headsUpController = headsUpController;
        this.headsUpViewBinder = headsUpViewBinder;
        this.clickerBuilder = clickerBuilder;
    }

    @Override // com.android.systemui.statusbar.notification.init.NotificationsController
    public void initialize(@NotNull StatusBar statusBar, @NotNull NotificationPresenter presenter, @NotNull NotificationListContainer listContainer, @NotNull NotificationActivityStarter notificationActivityStarter, @NotNull NotificationRowBinderImpl.BindRowCallback bindRowCallback) {
        Intrinsics.checkParameterIsNotNull(statusBar, "statusBar");
        Intrinsics.checkParameterIsNotNull(presenter, "presenter");
        Intrinsics.checkParameterIsNotNull(listContainer, "listContainer");
        Intrinsics.checkParameterIsNotNull(notificationActivityStarter, "notificationActivityStarter");
        Intrinsics.checkParameterIsNotNull(bindRowCallback, "bindRowCallback");
        this.notificationListener.registerAsSystemService();
        new NotificationListController(this.entryManager, listContainer, this.deviceProvisionedController).bind();
        this.notificationRowBinder.setNotificationClicker(this.clickerBuilder.build(Optional.of(statusBar), notificationActivityStarter));
        this.notificationRowBinder.setUpWithPresenter(presenter, listContainer, bindRowCallback);
        this.headsUpViewBinder.setPresenter(presenter);
        this.notifBindPipelineInitializer.initialize();
        if (this.featureFlags.isNewNotifPipelineEnabled()) {
            this.newNotifPipeline.get().initialize(this.notificationListener, this.notificationRowBinder, listContainer);
        }
        if (this.featureFlags.isNewNotifPipelineRenderingEnabled()) {
            TargetSdkResolver targetSdkResolver = this.targetSdkResolver;
            NotifPipeline notifPipeline = this.notifPipeline.get();
            Intrinsics.checkExpressionValueIsNotNull(notifPipeline, "notifPipeline.get()");
            targetSdkResolver.initialize(notifPipeline);
            return;
        }
        this.targetSdkResolver.initialize(this.entryManager);
        this.remoteInputUriController.attach(this.entryManager);
        this.groupAlertTransferHelper.bind(this.entryManager, this.groupManager);
        this.headsUpManager.addListener(this.groupManager);
        this.headsUpManager.addListener(this.groupAlertTransferHelper);
        this.headsUpController.attach(this.entryManager, this.headsUpManager);
        this.groupManager.setHeadsUpManager(this.headsUpManager);
        this.groupAlertTransferHelper.setHeadsUpManager(this.headsUpManager);
        this.entryManager.attach(this.notificationListener);
    }

    @Override // com.android.systemui.statusbar.notification.init.NotificationsController
    public void dump(@NotNull FileDescriptor fd, @NotNull PrintWriter pw, @NotNull String[] args, boolean z) {
        Intrinsics.checkParameterIsNotNull(fd, "fd");
        Intrinsics.checkParameterIsNotNull(pw, "pw");
        Intrinsics.checkParameterIsNotNull(args, "args");
        if (z) {
            this.entryManager.dump(pw, "  ");
        }
        this.groupManager.dump(fd, pw, args);
    }

    @Override // com.android.systemui.statusbar.notification.init.NotificationsController
    public void requestNotificationUpdate(@NotNull String reason) {
        Intrinsics.checkParameterIsNotNull(reason, "reason");
        this.entryManager.updateNotifications(reason);
    }

    @Override // com.android.systemui.statusbar.notification.init.NotificationsController
    public void resetUserExpandedStates() {
        Iterator<NotificationEntry> it = this.entryManager.getVisibleNotifications().iterator();
        while (it.hasNext()) {
            it.next().resetUserExpansion();
        }
    }

    @Override // com.android.systemui.statusbar.notification.init.NotificationsController
    public void setNotificationSnoozed(@NotNull StatusBarNotification sbn, @NotNull NotificationSwipeActionHelper.SnoozeOption snoozeOption) {
        Intrinsics.checkParameterIsNotNull(sbn, "sbn");
        Intrinsics.checkParameterIsNotNull(snoozeOption, "snoozeOption");
        if (snoozeOption.getSnoozeCriterion() != null) {
            NotificationListener notificationListener = this.notificationListener;
            String key = sbn.getKey();
            SnoozeCriterion snoozeCriterion = snoozeOption.getSnoozeCriterion();
            Intrinsics.checkExpressionValueIsNotNull(snoozeCriterion, "snoozeOption.snoozeCriterion");
            notificationListener.snoozeNotification(key, snoozeCriterion.getId());
            return;
        }
        this.notificationListener.snoozeNotification(sbn.getKey(), snoozeOption.getMinutesToSnoozeFor() * 60 * 1000);
    }

    @Override // com.android.systemui.statusbar.notification.init.NotificationsController
    public int getActiveNotificationsCount() {
        return this.entryManager.getActiveNotificationsCount();
    }

    @Override // com.android.systemui.statusbar.notification.init.NotificationsController
    public void setNotificationSnoozed(@NotNull StatusBarNotification sbn, int i) {
        Intrinsics.checkParameterIsNotNull(sbn, "sbn");
        this.notificationListener.snoozeNotification(sbn.getKey(), i * 60 * 60 * 1000);
    }
}
