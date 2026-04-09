package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import android.service.dreams.IDreamManager;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.EventLog;
import android.view.RemoteAnimationAdapter;
import android.view.View;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.internal.widget.LockPatternUtils;
import com.android.systemui.ActivityIntentHelper;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.NotificationClickNotifier;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationPresenter;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.RemoteInputController;
import com.android.systemui.statusbar.notification.ActivityLaunchAnimator;
import com.android.systemui.statusbar.notification.NotificationActivityStarter;
import com.android.systemui.statusbar.notification.NotificationEntryListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotifCollection;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.notifcollection.DismissedByUserStats;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.policy.HeadsUpUtil;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import dagger.Lazy;
import java.util.Objects;
import java.util.concurrent.Executor;

/* loaded from: classes.dex */
public class StatusBarNotificationActivityStarter implements NotificationActivityStarter {
    private final ActivityIntentHelper mActivityIntentHelper;
    private final ActivityLaunchAnimator mActivityLaunchAnimator;
    private final ActivityStarter mActivityStarter;
    private final Lazy<AssistManager> mAssistManagerLazy;
    private final Handler mBackgroundHandler;
    private final BubbleController mBubbleController;
    private final NotificationClickNotifier mClickNotifier;
    private final CommandQueue mCommandQueue;
    private final Context mContext;
    private final IDreamManager mDreamManager;
    private final NotificationEntryManager mEntryManager;
    private final FeatureFlags mFeatureFlags;
    private final NotificationGroupManager mGroupManager;
    private final HeadsUpManagerPhone mHeadsUpManager;
    private boolean mIsCollapsingToShowActivityOverLockscreen;
    private final KeyguardManager mKeyguardManager;
    private final KeyguardStateController mKeyguardStateController;
    private final LockPatternUtils mLockPatternUtils;
    private final NotificationLockscreenUserManager mLockscreenUserManager;
    private final StatusBarNotificationActivityStarterLogger mLogger;
    private final Handler mMainThreadHandler;
    private final MetricsLogger mMetricsLogger;
    private final NotifCollection mNotifCollection;
    private final NotifPipeline mNotifPipeline;
    private final NotificationInterruptStateProvider mNotificationInterruptStateProvider;
    private final NotificationPanelViewController mNotificationPanel;
    private final NotificationPresenter mPresenter;
    private final NotificationRemoteInputManager mRemoteInputManager;
    private final ShadeController mShadeController;
    private final StatusBar mStatusBar;
    private final StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private final StatusBarRemoteInputCallback mStatusBarRemoteInputCallback;
    private final StatusBarStateController mStatusBarStateController;
    private final Executor mUiBgExecutor;

    private StatusBarNotificationActivityStarter(Context context, CommandQueue commandQueue, Handler handler, Handler handler2, Executor executor, NotificationEntryManager notificationEntryManager, NotifPipeline notifPipeline, NotifCollection notifCollection, HeadsUpManagerPhone headsUpManagerPhone, ActivityStarter activityStarter, NotificationClickNotifier notificationClickNotifier, StatusBarStateController statusBarStateController, StatusBarKeyguardViewManager statusBarKeyguardViewManager, KeyguardManager keyguardManager, IDreamManager iDreamManager, BubbleController bubbleController, Lazy<AssistManager> lazy, NotificationRemoteInputManager notificationRemoteInputManager, NotificationGroupManager notificationGroupManager, NotificationLockscreenUserManager notificationLockscreenUserManager, ShadeController shadeController, KeyguardStateController keyguardStateController, NotificationInterruptStateProvider notificationInterruptStateProvider, LockPatternUtils lockPatternUtils, StatusBarRemoteInputCallback statusBarRemoteInputCallback, ActivityIntentHelper activityIntentHelper, FeatureFlags featureFlags, MetricsLogger metricsLogger, StatusBarNotificationActivityStarterLogger statusBarNotificationActivityStarterLogger, StatusBar statusBar, NotificationPresenter notificationPresenter, NotificationPanelViewController notificationPanelViewController, ActivityLaunchAnimator activityLaunchAnimator) {
        this.mContext = context;
        this.mCommandQueue = commandQueue;
        this.mMainThreadHandler = handler;
        this.mBackgroundHandler = handler2;
        this.mUiBgExecutor = executor;
        this.mEntryManager = notificationEntryManager;
        this.mNotifPipeline = notifPipeline;
        this.mNotifCollection = notifCollection;
        this.mHeadsUpManager = headsUpManagerPhone;
        this.mActivityStarter = activityStarter;
        this.mClickNotifier = notificationClickNotifier;
        this.mStatusBarStateController = statusBarStateController;
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
        this.mKeyguardManager = keyguardManager;
        this.mDreamManager = iDreamManager;
        this.mBubbleController = bubbleController;
        this.mAssistManagerLazy = lazy;
        this.mRemoteInputManager = notificationRemoteInputManager;
        this.mGroupManager = notificationGroupManager;
        this.mLockscreenUserManager = notificationLockscreenUserManager;
        this.mShadeController = shadeController;
        this.mKeyguardStateController = keyguardStateController;
        this.mNotificationInterruptStateProvider = notificationInterruptStateProvider;
        this.mLockPatternUtils = lockPatternUtils;
        this.mStatusBarRemoteInputCallback = statusBarRemoteInputCallback;
        this.mActivityIntentHelper = activityIntentHelper;
        this.mFeatureFlags = featureFlags;
        this.mMetricsLogger = metricsLogger;
        this.mLogger = statusBarNotificationActivityStarterLogger;
        this.mStatusBar = statusBar;
        this.mPresenter = notificationPresenter;
        this.mNotificationPanel = notificationPanelViewController;
        this.mActivityLaunchAnimator = activityLaunchAnimator;
        if (!featureFlags.isNewNotifPipelineRenderingEnabled()) {
            notificationEntryManager.addNotificationEntryListener(new NotificationEntryListener() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter.1
                @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
                public void onPendingEntryAdded(NotificationEntry notificationEntry) throws PendingIntent.CanceledException {
                    StatusBarNotificationActivityStarter.this.handleFullScreenIntent(notificationEntry);
                }
            });
        } else {
            notifPipeline.addCollectionListener(new NotifCollectionListener() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter.2
                @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
                public void onEntryAdded(NotificationEntry notificationEntry) throws PendingIntent.CanceledException {
                    StatusBarNotificationActivityStarter.this.handleFullScreenIntent(notificationEntry);
                }
            });
        }
    }

    @Override // com.android.systemui.statusbar.notification.NotificationActivityStarter
    public void onNotificationClicked(final StatusBarNotification statusBarNotification, final ExpandableNotificationRow expandableNotificationRow) {
        this.mLogger.logStartingActivityFromClick(statusBarNotification.getKey());
        final RemoteInputController controller = this.mRemoteInputManager.getController();
        if (controller.isRemoteInputActive(expandableNotificationRow.getEntry()) && !TextUtils.isEmpty(expandableNotificationRow.getActiveRemoteInputText())) {
            controller.closeRemoteInputs();
            return;
        }
        Notification notification = statusBarNotification.getNotification();
        PendingIntent pendingIntent = notification.contentIntent;
        final PendingIntent pendingIntent2 = pendingIntent != null ? pendingIntent : notification.fullScreenIntent;
        boolean zIsBubble = expandableNotificationRow.getEntry().isBubble();
        if (pendingIntent2 == null && !zIsBubble) {
            this.mLogger.logNonClickableNotification(statusBarNotification.getKey());
            return;
        }
        final boolean z = (pendingIntent2 == null || !pendingIntent2.isActivity() || zIsBubble) ? false : true;
        boolean z2 = z && this.mActivityIntentHelper.wouldLaunchResolverActivity(pendingIntent2.getIntent(), this.mLockscreenUserManager.getCurrentUserId());
        final boolean zIsOccluded = this.mStatusBar.isOccluded();
        boolean z3 = this.mKeyguardStateController.isShowing() && pendingIntent2 != null && this.mActivityIntentHelper.wouldShowOverLockscreen(pendingIntent2.getIntent(), this.mLockscreenUserManager.getCurrentUserId());
        final boolean z4 = z3;
        ActivityStarter.OnDismissAction onDismissAction = new ActivityStarter.OnDismissAction() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter$$ExternalSyntheticLambda1
            @Override // com.android.systemui.plugins.ActivityStarter.OnDismissAction
            public final boolean onDismiss() {
                return this.f$0.lambda$onNotificationClicked$0(statusBarNotification, expandableNotificationRow, controller, pendingIntent2, z, zIsOccluded, z4);
            }
        };
        if (z3) {
            this.mIsCollapsingToShowActivityOverLockscreen = true;
            onDismissAction.onDismiss();
        } else {
            this.mActivityStarter.dismissKeyguardThenExecute(onDismissAction, null, z2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:9:0x002e  */
    /* renamed from: handleNotificationClickAfterKeyguardDismissed, reason: merged with bridge method [inline-methods] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean lambda$onNotificationClicked$0(final android.service.notification.StatusBarNotification r12, final com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r13, final com.android.systemui.statusbar.RemoteInputController r14, final android.app.PendingIntent r15, final boolean r16, final boolean r17, boolean r18) {
        /*
            r11 = this;
            r9 = r11
            r2 = r12
            com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarterLogger r0 = r9.mLogger
            java.lang.String r1 = r12.getKey()
            r0.logHandleClickAfterKeyguardDismissed(r1)
            r3 = r13
            r11.removeHUN(r13)
            boolean r0 = shouldAutoCancel(r12)
            if (r0 == 0) goto L2e
            com.android.systemui.statusbar.phone.NotificationGroupManager r0 = r9.mGroupManager
            boolean r0 = r0.isOnlyChildInGroup(r12)
            if (r0 == 0) goto L2e
            com.android.systemui.statusbar.phone.NotificationGroupManager r0 = r9.mGroupManager
            com.android.systemui.statusbar.notification.collection.NotificationEntry r0 = r0.getLogicalGroupSummary(r12)
            android.service.notification.StatusBarNotification r1 = r0.getSbn()
            boolean r1 = shouldAutoCancel(r1)
            if (r1 == 0) goto L2e
            goto L2f
        L2e:
            r0 = 0
        L2f:
            r8 = r0
            com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter$$ExternalSyntheticLambda10 r10 = new com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter$$ExternalSyntheticLambda10
            r0 = r10
            r1 = r11
            r2 = r12
            r3 = r13
            r4 = r14
            r5 = r15
            r6 = r16
            r7 = r17
            r0.<init>()
            r0 = 1
            if (r18 == 0) goto L4d
            com.android.systemui.statusbar.phone.ShadeController r1 = r9.mShadeController
            r1.addPostCollapseAction(r10)
            com.android.systemui.statusbar.phone.ShadeController r1 = r9.mShadeController
            r1.collapsePanel(r0)
            goto L6d
        L4d:
            com.android.systemui.statusbar.policy.KeyguardStateController r1 = r9.mKeyguardStateController
            boolean r1 = r1.isShowing()
            if (r1 == 0) goto L68
            com.android.systemui.statusbar.phone.StatusBar r1 = r9.mStatusBar
            boolean r1 = r1.isOccluded()
            if (r1 == 0) goto L68
            com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager r1 = r9.mStatusBarKeyguardViewManager
            r1.addAfterKeyguardGoneRunnable(r10)
            com.android.systemui.statusbar.phone.ShadeController r1 = r9.mShadeController
            r1.collapsePanel()
            goto L6d
        L68:
            android.os.Handler r1 = r9.mBackgroundHandler
            r1.postAtFrontOfQueue(r10)
        L6d:
            com.android.systemui.statusbar.phone.NotificationPanelViewController r1 = r9.mNotificationPanel
            boolean r1 = r1.isFullyCollapsed()
            r0 = r0 ^ r1
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter.lambda$onNotificationClicked$0(android.service.notification.StatusBarNotification, com.android.systemui.statusbar.notification.row.ExpandableNotificationRow, com.android.systemui.statusbar.RemoteInputController, android.app.PendingIntent, boolean, boolean, boolean):boolean");
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: handleNotificationClickAfterPanelCollapsed, reason: merged with bridge method [inline-methods] */
    public void lambda$handleNotificationClickAfterKeyguardDismissed$1(StatusBarNotification statusBarNotification, ExpandableNotificationRow expandableNotificationRow, RemoteInputController remoteInputController, PendingIntent pendingIntent, boolean z, boolean z2, NotificationEntry notificationEntry) {
        this.mLogger.logHandleClickAfterPanelCollapsed(statusBarNotification.getKey());
        String key = statusBarNotification.getKey();
        try {
            ActivityManager.getService().resumeAppSwitches();
        } catch (RemoteException unused) {
        }
        if (z) {
            int identifier = pendingIntent.getCreatorUserHandle().getIdentifier();
            if (this.mLockPatternUtils.isSeparateProfileChallengeEnabled(identifier) && this.mKeyguardManager.isDeviceLocked(identifier) && this.mStatusBarRemoteInputCallback.startWorkChallengeIfNecessary(identifier, pendingIntent.getIntentSender(), key)) {
                collapseOnMainThread();
                return;
            }
        }
        NotificationEntry entry = expandableNotificationRow.getEntry();
        CharSequence charSequence = !TextUtils.isEmpty(entry.remoteInputText) ? entry.remoteInputText : null;
        Intent intentPutExtra = (TextUtils.isEmpty(charSequence) || remoteInputController.isSpinning(key)) ? null : new Intent().putExtra("android.remoteInputDraft", charSequence.toString());
        boolean zCanBubble = entry.canBubble();
        if (zCanBubble) {
            this.mLogger.logExpandingBubble(key);
            expandBubbleStackOnMainThread(entry);
        } else {
            startNotificationIntent(pendingIntent, intentPutExtra, entry, expandableNotificationRow, z2, z);
        }
        if (z || zCanBubble) {
            this.mAssistManagerLazy.get().hideAssist();
        }
        if (shouldCollapse()) {
            collapseOnMainThread();
        }
        this.mClickNotifier.onNotificationClick(key, NotificationVisibility.obtain(key, entry.getRanking().getRank(), getVisibleNotificationsCount(), true, NotificationLogger.getNotificationLocation(entry)));
        if (!zCanBubble) {
            if (notificationEntry != null) {
                removeNotification(notificationEntry);
            }
            if (shouldAutoCancel(statusBarNotification) || this.mRemoteInputManager.isNotificationKeptForRemoteInputHistory(key)) {
                removeNotification(expandableNotificationRow.getEntry());
            }
        }
        this.mIsCollapsingToShowActivityOverLockscreen = false;
    }

    private void expandBubbleStackOnMainThread(final NotificationEntry notificationEntry) {
        if (Looper.getMainLooper().isCurrentThread()) {
            this.mBubbleController.expandStackAndSelectBubble(notificationEntry);
        } else {
            this.mMainThreadHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter$$ExternalSyntheticLambda12
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$expandBubbleStackOnMainThread$2(notificationEntry);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$expandBubbleStackOnMainThread$2(NotificationEntry notificationEntry) {
        this.mBubbleController.expandStackAndSelectBubble(notificationEntry);
    }

    private void startNotificationIntent(PendingIntent pendingIntent, Intent intent, NotificationEntry notificationEntry, View view, boolean z, final boolean z2) {
        RemoteAnimationAdapter launchAnimation = this.mActivityLaunchAnimator.getLaunchAnimation(view, z);
        this.mLogger.logStartNotificationIntent(notificationEntry.getKey(), pendingIntent);
        if (launchAnimation != null) {
            try {
                ActivityTaskManager.getService().registerRemoteAnimationForNextActivityStart(pendingIntent.getCreatorPackage(), launchAnimation);
            } catch (PendingIntent.CanceledException | RemoteException e) {
                this.mLogger.logSendingIntentFailed(e);
                return;
            }
        }
        final int iSendAndReturnResult = pendingIntent.sendAndReturnResult(this.mContext, 0, intent, null, null, null, StatusBar.getActivityOptions(launchAnimation));
        this.mMainThreadHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter$$ExternalSyntheticLambda8
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$startNotificationIntent$3(iSendAndReturnResult, z2);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startNotificationIntent$3(int i, boolean z) {
        this.mActivityLaunchAnimator.setLaunchResult(i, z);
    }

    @Override // com.android.systemui.statusbar.notification.NotificationActivityStarter
    public void startNotificationGutsIntent(final Intent intent, final int i, final ExpandableNotificationRow expandableNotificationRow) {
        this.mActivityStarter.dismissKeyguardThenExecute(new ActivityStarter.OnDismissAction() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter$$ExternalSyntheticLambda0
            @Override // com.android.systemui.plugins.ActivityStarter.OnDismissAction
            public final boolean onDismiss() {
                return this.f$0.lambda$startNotificationGutsIntent$7(intent, expandableNotificationRow, i);
            }
        }, null, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$startNotificationGutsIntent$7(final Intent intent, final ExpandableNotificationRow expandableNotificationRow, final int i) {
        AsyncTask.execute(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter$$ExternalSyntheticLambda9
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$startNotificationGutsIntent$6(intent, expandableNotificationRow, i);
            }
        });
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startNotificationGutsIntent$6(Intent intent, final ExpandableNotificationRow expandableNotificationRow, int i) {
        final int iStartActivities = TaskStackBuilder.create(this.mContext).addNextIntentWithParentStack(intent).startActivities(StatusBar.getActivityOptions(this.mActivityLaunchAnimator.getLaunchAnimation(expandableNotificationRow, this.mStatusBar.isOccluded())), new UserHandle(UserHandle.getUserId(i)));
        this.mMainThreadHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter$$ExternalSyntheticLambda7
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$startNotificationGutsIntent$4(iStartActivities, expandableNotificationRow);
            }
        });
        if (shouldCollapse()) {
            this.mMainThreadHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter$$ExternalSyntheticLambda5
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$startNotificationGutsIntent$5();
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startNotificationGutsIntent$4(int i, ExpandableNotificationRow expandableNotificationRow) {
        this.mActivityLaunchAnimator.setLaunchResult(i, true);
        removeHUN(expandableNotificationRow);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startNotificationGutsIntent$5() {
        this.mCommandQueue.animateCollapsePanels(2, true);
    }

    @Override // com.android.systemui.statusbar.notification.NotificationActivityStarter
    public void startHistoryIntent(final boolean z) {
        this.mActivityStarter.dismissKeyguardThenExecute(new ActivityStarter.OnDismissAction() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter$$ExternalSyntheticLambda2
            @Override // com.android.systemui.plugins.ActivityStarter.OnDismissAction
            public final boolean onDismiss() {
                return this.f$0.lambda$startHistoryIntent$10(z);
            }
        }, null, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$startHistoryIntent$10(final boolean z) {
        AsyncTask.execute(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter$$ExternalSyntheticLambda13
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$startHistoryIntent$9(z);
            }
        });
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startHistoryIntent$9(boolean z) {
        Intent intent;
        if (z) {
            intent = new Intent("android.settings.NOTIFICATION_HISTORY");
        } else {
            intent = new Intent("android.settings.NOTIFICATION_SETTINGS");
        }
        TaskStackBuilder taskStackBuilderAddNextIntent = TaskStackBuilder.create(this.mContext).addNextIntent(new Intent("android.settings.NOTIFICATION_SETTINGS"));
        if (z) {
            taskStackBuilderAddNextIntent.addNextIntent(intent);
        }
        taskStackBuilderAddNextIntent.startActivities(null, UserHandle.CURRENT);
        if (shouldCollapse()) {
            this.mMainThreadHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter$$ExternalSyntheticLambda6
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$startHistoryIntent$8();
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startHistoryIntent$8() {
        this.mCommandQueue.animateCollapsePanels(2, true);
    }

    private void removeHUN(ExpandableNotificationRow expandableNotificationRow) {
        String key = expandableNotificationRow.getEntry().getSbn().getKey();
        HeadsUpManagerPhone headsUpManagerPhone = this.mHeadsUpManager;
        if (headsUpManagerPhone == null || !headsUpManagerPhone.isAlerting(key)) {
            return;
        }
        if (this.mPresenter.isPresenterFullyCollapsed()) {
            HeadsUpUtil.setIsClickedHeadsUpNotification(expandableNotificationRow, true);
        }
        this.mHeadsUpManager.removeNotification(key, true);
    }

    void handleFullScreenIntent(NotificationEntry notificationEntry) throws PendingIntent.CanceledException {
        if (this.mNotificationInterruptStateProvider.shouldLaunchFullScreenIntentWhenAdded(notificationEntry)) {
            if (shouldSuppressFullScreenIntent(notificationEntry)) {
                this.mLogger.logFullScreenIntentSuppressedByDnD(notificationEntry.getKey());
                return;
            }
            if (notificationEntry.getImportance() < 4) {
                this.mLogger.logFullScreenIntentNotImportantEnough(notificationEntry.getKey());
                return;
            }
            this.mUiBgExecutor.execute(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter$$ExternalSyntheticLambda4
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$handleFullScreenIntent$11();
                }
            });
            PendingIntent pendingIntent = notificationEntry.getSbn().getNotification().fullScreenIntent;
            this.mLogger.logSendingFullScreenIntent(notificationEntry.getKey(), pendingIntent);
            try {
                EventLog.writeEvent(36002, notificationEntry.getKey());
                this.mStatusBar.wakeUpForFullScreenIntent();
                pendingIntent.send();
                notificationEntry.notifyFullScreenIntentLaunched();
                this.mMetricsLogger.count("note_fullscreen", 1);
            } catch (PendingIntent.CanceledException unused) {
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleFullScreenIntent$11() {
        try {
            this.mDreamManager.awaken();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override // com.android.systemui.statusbar.notification.NotificationActivityStarter
    public boolean isCollapsingToShowActivityOverLockscreen() {
        return this.mIsCollapsingToShowActivityOverLockscreen;
    }

    private static boolean shouldAutoCancel(StatusBarNotification statusBarNotification) {
        int i = statusBarNotification.getNotification().flags;
        return (i & 16) == 16 && (i & 64) == 0;
    }

    private void collapseOnMainThread() {
        if (Looper.getMainLooper().isCurrentThread()) {
            this.mShadeController.collapsePanel();
            return;
        }
        Handler handler = this.mMainThreadHandler;
        final ShadeController shadeController = this.mShadeController;
        Objects.requireNonNull(shadeController);
        handler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                shadeController.collapsePanel();
            }
        });
    }

    private boolean shouldCollapse() {
        return (this.mStatusBarStateController.getState() == 0 && this.mActivityLaunchAnimator.isAnimationPending()) ? false : true;
    }

    private boolean shouldSuppressFullScreenIntent(NotificationEntry notificationEntry) {
        if (this.mPresenter.isDeviceInVrMode()) {
            return true;
        }
        return notificationEntry.shouldSuppressFullScreenIntent();
    }

    private void removeNotification(final NotificationEntry notificationEntry) {
        this.mMainThreadHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter$$ExternalSyntheticLambda11
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$removeNotification$12(notificationEntry);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$removeNotification$12(NotificationEntry notificationEntry) {
        Runnable runnableCreateRemoveRunnable = createRemoveRunnable(notificationEntry);
        if (this.mPresenter.isCollapsing()) {
            this.mShadeController.addPostCollapseAction(runnableCreateRemoveRunnable);
        } else {
            runnableCreateRemoveRunnable.run();
        }
    }

    private int getVisibleNotificationsCount() {
        if (this.mFeatureFlags.isNewNotifPipelineRenderingEnabled()) {
            return this.mNotifPipeline.getShadeListCount();
        }
        return this.mEntryManager.getActiveNotificationsCount();
    }

    private Runnable createRemoveRunnable(final NotificationEntry notificationEntry) {
        if (this.mFeatureFlags.isNewNotifPipelineRenderingEnabled()) {
            return new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter.3
                @Override // java.lang.Runnable
                public void run() {
                    int i;
                    if (StatusBarNotificationActivityStarter.this.mHeadsUpManager.isAlerting(notificationEntry.getKey())) {
                        i = 1;
                    } else {
                        i = StatusBarNotificationActivityStarter.this.mNotificationPanel.hasPulsingNotifications() ? 2 : 3;
                    }
                    NotifCollection notifCollection = StatusBarNotificationActivityStarter.this.mNotifCollection;
                    NotificationEntry notificationEntry2 = notificationEntry;
                    notifCollection.dismissNotification(notificationEntry2, new DismissedByUserStats(i, 1, NotificationVisibility.obtain(notificationEntry2.getKey(), notificationEntry.getRanking().getRank(), StatusBarNotificationActivityStarter.this.mNotifPipeline.getShadeListCount(), true, NotificationLogger.getNotificationLocation(notificationEntry))));
                }
            };
        }
        return new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter.4
            @Override // java.lang.Runnable
            public void run() {
                StatusBarNotificationActivityStarter.this.mEntryManager.performRemoveNotification(notificationEntry.getSbn(), 1);
            }
        };
    }

    public static class Builder {
        private final ActivityIntentHelper mActivityIntentHelper;
        private ActivityLaunchAnimator mActivityLaunchAnimator;
        private final ActivityStarter mActivityStarter;
        private final Lazy<AssistManager> mAssistManagerLazy;
        private final Handler mBackgroundHandler;
        private final BubbleController mBubbleController;
        private final NotificationClickNotifier mClickNotifier;
        private final CommandQueue mCommandQueue;
        private final Context mContext;
        private final IDreamManager mDreamManager;
        private final NotificationEntryManager mEntryManager;
        private final FeatureFlags mFeatureFlags;
        private final NotificationGroupManager mGroupManager;
        private final HeadsUpManagerPhone mHeadsUpManager;
        private final KeyguardManager mKeyguardManager;
        private final KeyguardStateController mKeyguardStateController;
        private final LockPatternUtils mLockPatternUtils;
        private final NotificationLockscreenUserManager mLockscreenUserManager;
        private final StatusBarNotificationActivityStarterLogger mLogger;
        private final Handler mMainThreadHandler;
        private final MetricsLogger mMetricsLogger;
        private final NotifCollection mNotifCollection;
        private final NotifPipeline mNotifPipeline;
        private final NotificationInterruptStateProvider mNotificationInterruptStateProvider;
        private NotificationPanelViewController mNotificationPanelViewController;
        private NotificationPresenter mNotificationPresenter;
        private final StatusBarRemoteInputCallback mRemoteInputCallback;
        private final NotificationRemoteInputManager mRemoteInputManager;
        private final ShadeController mShadeController;
        private StatusBar mStatusBar;
        private final StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
        private final StatusBarStateController mStatusBarStateController;
        private final Executor mUiBgExecutor;

        public Builder(Context context, CommandQueue commandQueue, Handler handler, Handler handler2, Executor executor, NotificationEntryManager notificationEntryManager, NotifPipeline notifPipeline, NotifCollection notifCollection, HeadsUpManagerPhone headsUpManagerPhone, ActivityStarter activityStarter, NotificationClickNotifier notificationClickNotifier, StatusBarStateController statusBarStateController, StatusBarKeyguardViewManager statusBarKeyguardViewManager, KeyguardManager keyguardManager, IDreamManager iDreamManager, BubbleController bubbleController, Lazy<AssistManager> lazy, NotificationRemoteInputManager notificationRemoteInputManager, NotificationGroupManager notificationGroupManager, NotificationLockscreenUserManager notificationLockscreenUserManager, ShadeController shadeController, KeyguardStateController keyguardStateController, NotificationInterruptStateProvider notificationInterruptStateProvider, LockPatternUtils lockPatternUtils, StatusBarRemoteInputCallback statusBarRemoteInputCallback, ActivityIntentHelper activityIntentHelper, FeatureFlags featureFlags, MetricsLogger metricsLogger, StatusBarNotificationActivityStarterLogger statusBarNotificationActivityStarterLogger) {
            this.mContext = context;
            this.mCommandQueue = commandQueue;
            this.mMainThreadHandler = handler;
            this.mBackgroundHandler = handler2;
            this.mUiBgExecutor = executor;
            this.mEntryManager = notificationEntryManager;
            this.mNotifPipeline = notifPipeline;
            this.mNotifCollection = notifCollection;
            this.mHeadsUpManager = headsUpManagerPhone;
            this.mActivityStarter = activityStarter;
            this.mClickNotifier = notificationClickNotifier;
            this.mStatusBarStateController = statusBarStateController;
            this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
            this.mKeyguardManager = keyguardManager;
            this.mDreamManager = iDreamManager;
            this.mBubbleController = bubbleController;
            this.mAssistManagerLazy = lazy;
            this.mRemoteInputManager = notificationRemoteInputManager;
            this.mGroupManager = notificationGroupManager;
            this.mLockscreenUserManager = notificationLockscreenUserManager;
            this.mShadeController = shadeController;
            this.mKeyguardStateController = keyguardStateController;
            this.mNotificationInterruptStateProvider = notificationInterruptStateProvider;
            this.mLockPatternUtils = lockPatternUtils;
            this.mRemoteInputCallback = statusBarRemoteInputCallback;
            this.mActivityIntentHelper = activityIntentHelper;
            this.mFeatureFlags = featureFlags;
            this.mMetricsLogger = metricsLogger;
            this.mLogger = statusBarNotificationActivityStarterLogger;
        }

        public Builder setStatusBar(StatusBar statusBar) {
            this.mStatusBar = statusBar;
            return this;
        }

        public Builder setNotificationPresenter(NotificationPresenter notificationPresenter) {
            this.mNotificationPresenter = notificationPresenter;
            return this;
        }

        public Builder setActivityLaunchAnimator(ActivityLaunchAnimator activityLaunchAnimator) {
            this.mActivityLaunchAnimator = activityLaunchAnimator;
            return this;
        }

        public Builder setNotificationPanelViewController(NotificationPanelViewController notificationPanelViewController) {
            this.mNotificationPanelViewController = notificationPanelViewController;
            return this;
        }

        public StatusBarNotificationActivityStarter build() {
            return new StatusBarNotificationActivityStarter(this.mContext, this.mCommandQueue, this.mMainThreadHandler, this.mBackgroundHandler, this.mUiBgExecutor, this.mEntryManager, this.mNotifPipeline, this.mNotifCollection, this.mHeadsUpManager, this.mActivityStarter, this.mClickNotifier, this.mStatusBarStateController, this.mStatusBarKeyguardViewManager, this.mKeyguardManager, this.mDreamManager, this.mBubbleController, this.mAssistManagerLazy, this.mRemoteInputManager, this.mGroupManager, this.mLockscreenUserManager, this.mShadeController, this.mKeyguardStateController, this.mNotificationInterruptStateProvider, this.mLockPatternUtils, this.mRemoteInputCallback, this.mActivityIntentHelper, this.mFeatureFlags, this.mMetricsLogger, this.mLogger, this.mStatusBar, this.mNotificationPresenter, this.mNotificationPanelViewController, this.mActivityLaunchAnimator);
        }
    }
}
