package com.android.systemui.statusbar.notification;

import android.app.Notification;
import android.os.SystemClock;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;
import com.android.systemui.DejankUtils;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.phone.StatusBar;
import java.util.Optional;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public final class NotificationClicker implements View.OnClickListener {
    private final BubbleController mBubbleController;
    private final NotificationClickerLogger mLogger;
    private final NotificationActivityStarter mNotificationActivityStarter;
    private final Optional<StatusBar> mStatusBar;

    private NotificationClicker(BubbleController bubbleController, NotificationClickerLogger notificationClickerLogger, Optional<StatusBar> optional, NotificationActivityStarter notificationActivityStarter) {
        this.mBubbleController = bubbleController;
        this.mLogger = notificationClickerLogger;
        this.mStatusBar = optional;
        this.mNotificationActivityStarter = notificationActivityStarter;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(final View view) {
        if (!(view instanceof ExpandableNotificationRow)) {
            Log.e("NotificationClicker", "NotificationClicker called on a view that is not a notification row.");
            return;
        }
        this.mStatusBar.ifPresent(new Consumer() { // from class: com.android.systemui.statusbar.notification.NotificationClicker$$ExternalSyntheticLambda1
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                NotificationClicker.lambda$onClick$0(view, (StatusBar) obj);
            }
        });
        final ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
        NotificationEntry entry = expandableNotificationRow.getEntry();
        this.mLogger.logOnClick(entry);
        if (isMenuVisible(expandableNotificationRow)) {
            this.mLogger.logMenuVisible(entry);
            expandableNotificationRow.animateTranslateNotification(0.0f);
            return;
        }
        if (expandableNotificationRow.isChildInGroup() && isMenuVisible(expandableNotificationRow.getNotificationParent())) {
            this.mLogger.logParentMenuVisible(entry);
            expandableNotificationRow.getNotificationParent().animateTranslateNotification(0.0f);
            return;
        }
        if (expandableNotificationRow.isSummaryWithChildren() && expandableNotificationRow.areChildrenExpanded()) {
            this.mLogger.logChildrenExpanded(entry);
            return;
        }
        if (expandableNotificationRow.areGutsExposed()) {
            this.mLogger.logGutsExposed(entry);
            return;
        }
        expandableNotificationRow.setJustClicked(true);
        DejankUtils.postAfterTraversal(new Runnable() { // from class: com.android.systemui.statusbar.notification.NotificationClicker$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                expandableNotificationRow.setJustClicked(false);
            }
        });
        if (!expandableNotificationRow.getEntry().isBubble()) {
            this.mBubbleController.collapseStack();
        }
        this.mNotificationActivityStarter.onNotificationClicked(entry.getSbn(), expandableNotificationRow);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$onClick$0(View view, StatusBar statusBar) {
        statusBar.wakeUpIfDozing(SystemClock.uptimeMillis(), view, "NOTIFICATION_CLICK");
    }

    private boolean isMenuVisible(ExpandableNotificationRow expandableNotificationRow) {
        return expandableNotificationRow.getProvider() != null && expandableNotificationRow.getProvider().isMenuVisible();
    }

    public void register(ExpandableNotificationRow expandableNotificationRow, StatusBarNotification statusBarNotification) {
        Notification notification = statusBarNotification.getNotification();
        if (notification.contentIntent != null || notification.fullScreenIntent != null || expandableNotificationRow.getEntry().isBubble()) {
            expandableNotificationRow.setOnClickListener(this);
        } else {
            expandableNotificationRow.setOnClickListener(null);
        }
    }

    public static class Builder {
        private final BubbleController mBubbleController;
        private final NotificationClickerLogger mLogger;

        public Builder(BubbleController bubbleController, NotificationClickerLogger notificationClickerLogger) {
            this.mBubbleController = bubbleController;
            this.mLogger = notificationClickerLogger;
        }

        public NotificationClicker build(Optional<StatusBar> optional, NotificationActivityStarter notificationActivityStarter) {
            return new NotificationClicker(this.mBubbleController, this.mLogger, optional, notificationActivityStarter);
        }
    }
}
