package com.android.systemui.statusbar.notification.row;

import android.app.INotificationManager;
import android.app.NotificationChannel;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.util.ArraySet;
import android.util.IconDrawableFactory;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.settingslib.notification.ConversationIconFactory;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.R;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.settings.CurrentUserContextTracker;
import com.android.systemui.statusbar.NotificationLifetimeExtender;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationPresenter;
import com.android.systemui.statusbar.StatusBarStateControllerImpl;
import com.android.systemui.statusbar.notification.NotificationActivityStarter;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.provider.HighPriorityProvider;
import com.android.systemui.statusbar.notification.row.AppOpsInfo;
import com.android.systemui.statusbar.notification.row.NotificationConversationInfo;
import com.android.systemui.statusbar.notification.row.NotificationGuts;
import com.android.systemui.statusbar.notification.row.NotificationInfo;
import com.android.systemui.statusbar.notification.row.PriorityOnboardingDialogController;
import com.android.systemui.statusbar.notification.stack.NotificationListContainer;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import javax.inject.Provider;

/* loaded from: classes.dex */
public class NotificationGutsManager implements Dumpable, NotificationLifetimeExtender {
    private final AccessibilityManager mAccessibilityManager;
    private final Handler mBgHandler;
    private final BubbleController mBubbleController;
    private final Provider<PriorityOnboardingDialogController.Builder> mBuilderProvider;
    private final ChannelEditorDialogController mChannelEditorDialogController;
    private NotificationInfo.CheckSaveListener mCheckSaveListener;
    private final Context mContext;
    private final CurrentUserContextTracker mContextTracker;
    private NotificationMenuRowPlugin.MenuItem mGutsMenuItem;
    private final HighPriorityProvider mHighPriorityProvider;

    @VisibleForTesting
    protected String mKeyToRemoveOnGutsClosed;
    private final LauncherApps mLauncherApps;
    private NotificationListContainer mListContainer;
    private final Handler mMainHandler;
    private NotificationActivityStarter mNotificationActivityStarter;
    private NotificationGuts mNotificationGutsExposed;
    private NotificationLifetimeExtender.NotificationSafeToRemoveCallback mNotificationLifetimeFinishedCallback;
    private final INotificationManager mNotificationManager;
    private OnSettingsClickListener mOnSettingsClickListener;
    private Runnable mOpenRunnable;
    private NotificationPresenter mPresenter;
    private final ShortcutManager mShortcutManager;
    private final Lazy<StatusBar> mStatusBarLazy;
    private final UiEventLogger mUiEventLogger;
    private final VisualStabilityManager mVisualStabilityManager;
    private final MetricsLogger mMetricsLogger = (MetricsLogger) Dependency.get(MetricsLogger.class);
    private final NotificationLockscreenUserManager mLockscreenUserManager = (NotificationLockscreenUserManager) Dependency.get(NotificationLockscreenUserManager.class);
    private final StatusBarStateController mStatusBarStateController = (StatusBarStateController) Dependency.get(StatusBarStateController.class);
    private final DeviceProvisionedController mDeviceProvisionedController = (DeviceProvisionedController) Dependency.get(DeviceProvisionedController.class);

    public interface OnSettingsClickListener {
        void onSettingsClick(String str);
    }

    public NotificationGutsManager(Context context, VisualStabilityManager visualStabilityManager, Lazy<StatusBar> lazy, Handler handler, Handler handler2, AccessibilityManager accessibilityManager, HighPriorityProvider highPriorityProvider, INotificationManager iNotificationManager, LauncherApps launcherApps, ShortcutManager shortcutManager, ChannelEditorDialogController channelEditorDialogController, CurrentUserContextTracker currentUserContextTracker, Provider<PriorityOnboardingDialogController.Builder> provider, BubbleController bubbleController, UiEventLogger uiEventLogger) {
        this.mContext = context;
        this.mVisualStabilityManager = visualStabilityManager;
        this.mStatusBarLazy = lazy;
        this.mMainHandler = handler;
        this.mBgHandler = handler2;
        this.mAccessibilityManager = accessibilityManager;
        this.mHighPriorityProvider = highPriorityProvider;
        this.mNotificationManager = iNotificationManager;
        this.mLauncherApps = launcherApps;
        this.mShortcutManager = shortcutManager;
        this.mContextTracker = currentUserContextTracker;
        this.mBuilderProvider = provider;
        this.mChannelEditorDialogController = channelEditorDialogController;
        this.mBubbleController = bubbleController;
        this.mUiEventLogger = uiEventLogger;
    }

    public void setUpWithPresenter(NotificationPresenter notificationPresenter, NotificationListContainer notificationListContainer, NotificationInfo.CheckSaveListener checkSaveListener, OnSettingsClickListener onSettingsClickListener) {
        this.mPresenter = notificationPresenter;
        this.mListContainer = notificationListContainer;
        this.mCheckSaveListener = checkSaveListener;
        this.mOnSettingsClickListener = onSettingsClickListener;
    }

    public void setNotificationActivityStarter(NotificationActivityStarter notificationActivityStarter) {
        this.mNotificationActivityStarter = notificationActivityStarter;
    }

    public void onDensityOrFontScaleChanged(NotificationEntry notificationEntry) {
        setExposedGuts(notificationEntry.getGuts());
        bindGuts(notificationEntry.getRow());
    }

    private void startAppNotificationSettingsActivity(String str, int i, NotificationChannel notificationChannel, ExpandableNotificationRow expandableNotificationRow) {
        Intent intent = new Intent("android.settings.APP_NOTIFICATION_SETTINGS");
        intent.putExtra("android.provider.extra.APP_PACKAGE", str);
        intent.putExtra("app_uid", i);
        if (notificationChannel != null) {
            Bundle bundle = new Bundle();
            intent.putExtra(":settings:fragment_args_key", notificationChannel.getId());
            bundle.putString(":settings:fragment_args_key", notificationChannel.getId());
            intent.putExtra(":settings:show_fragment_args", bundle);
        }
        this.mNotificationActivityStarter.startNotificationGutsIntent(intent, i, expandableNotificationRow);
    }

    private void startAppDetailsSettingsActivity(String str, int i, NotificationChannel notificationChannel, ExpandableNotificationRow expandableNotificationRow) {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.fromParts("package", str, null));
        intent.putExtra("android.provider.extra.APP_PACKAGE", str);
        intent.putExtra("app_uid", i);
        if (notificationChannel != null) {
            intent.putExtra(":settings:fragment_args_key", notificationChannel.getId());
        }
        this.mNotificationActivityStarter.startNotificationGutsIntent(intent, i, expandableNotificationRow);
    }

    protected void startAppOpsSettingsActivity(String str, int i, ArraySet<Integer> arraySet, ExpandableNotificationRow expandableNotificationRow) {
        if (arraySet.contains(24)) {
            if (arraySet.contains(26) || arraySet.contains(27)) {
                startAppDetailsSettingsActivity(str, i, null, expandableNotificationRow);
                return;
            }
            Intent intent = new Intent("android.settings.MANAGE_APP_OVERLAY_PERMISSION");
            intent.setData(Uri.fromParts("package", str, null));
            this.mNotificationActivityStarter.startNotificationGutsIntent(intent, i, expandableNotificationRow);
            return;
        }
        if (arraySet.contains(26) || arraySet.contains(27)) {
            Intent intent2 = new Intent("android.intent.action.MANAGE_APP_PERMISSIONS");
            intent2.putExtra("android.intent.extra.PACKAGE_NAME", str);
            this.mNotificationActivityStarter.startNotificationGutsIntent(intent2, i, expandableNotificationRow);
        }
    }

    private void startConversationSettingsActivity(int i, ExpandableNotificationRow expandableNotificationRow) {
        this.mNotificationActivityStarter.startNotificationGutsIntent(new Intent("android.settings.CONVERSATION_SETTINGS"), i, expandableNotificationRow);
    }

    private boolean bindGuts(ExpandableNotificationRow expandableNotificationRow) {
        expandableNotificationRow.ensureGutsInflated();
        return bindGuts(expandableNotificationRow, this.mGutsMenuItem);
    }

    @VisibleForTesting
    protected boolean bindGuts(final ExpandableNotificationRow expandableNotificationRow, NotificationMenuRowPlugin.MenuItem menuItem) {
        final StatusBarNotification sbn = expandableNotificationRow.getEntry().getSbn();
        expandableNotificationRow.setGutsView(menuItem);
        expandableNotificationRow.setTag(sbn.getPackageName());
        expandableNotificationRow.getGuts().setClosedListener(new NotificationGuts.OnGutsClosedListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationGutsManager$$ExternalSyntheticLambda4
            @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.OnGutsClosedListener
            public final void onGutsClosed(NotificationGuts notificationGuts) {
                this.f$0.lambda$bindGuts$0(expandableNotificationRow, sbn, notificationGuts);
            }
        });
        View gutsView = menuItem.getGutsView();
        try {
            if (gutsView instanceof NotificationSnooze) {
                initializeSnoozeView(expandableNotificationRow, (NotificationSnooze) gutsView);
                return true;
            }
            if (gutsView instanceof AppOpsInfo) {
                initializeAppOpsInfo(expandableNotificationRow, (AppOpsInfo) gutsView);
                return true;
            }
            if (gutsView instanceof NotificationInfo) {
                initializeNotificationInfo(expandableNotificationRow, (NotificationInfo) gutsView);
                return true;
            }
            if (gutsView instanceof NotificationConversationInfo) {
                initializeConversationNotificationInfo(expandableNotificationRow, (NotificationConversationInfo) gutsView);
                return true;
            }
            if (!(gutsView instanceof PartialConversationInfo)) {
                return true;
            }
            initializePartialConversationNotificationInfo(expandableNotificationRow, (PartialConversationInfo) gutsView);
            return true;
        } catch (Exception e) {
            Log.e("NotificationGutsManager", "error binding guts", e);
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$bindGuts$0(ExpandableNotificationRow expandableNotificationRow, StatusBarNotification statusBarNotification, NotificationGuts notificationGuts) {
        expandableNotificationRow.onGutsClosed();
        if (!notificationGuts.willBeRemoved() && !expandableNotificationRow.isRemoved()) {
            this.mListContainer.onHeightChanged(expandableNotificationRow, !this.mPresenter.isPresenterFullyCollapsed());
        }
        if (this.mNotificationGutsExposed == notificationGuts) {
            this.mNotificationGutsExposed = null;
            this.mGutsMenuItem = null;
        }
        String key = statusBarNotification.getKey();
        if (key.equals(this.mKeyToRemoveOnGutsClosed)) {
            this.mKeyToRemoveOnGutsClosed = null;
            NotificationLifetimeExtender.NotificationSafeToRemoveCallback notificationSafeToRemoveCallback = this.mNotificationLifetimeFinishedCallback;
            if (notificationSafeToRemoveCallback != null) {
                notificationSafeToRemoveCallback.onSafeToRemove(key);
            }
        }
    }

    private void initializeSnoozeView(final ExpandableNotificationRow expandableNotificationRow, NotificationSnooze notificationSnooze) {
        NotificationGuts guts = expandableNotificationRow.getGuts();
        StatusBarNotification sbn = expandableNotificationRow.getEntry().getSbn();
        notificationSnooze.setSnoozeListener(this.mListContainer.getSwipeActionHelper());
        notificationSnooze.setStatusBarNotification(sbn);
        notificationSnooze.setSnoozeOptions(expandableNotificationRow.getEntry().getSnoozeCriteria());
        guts.setHeightChangedListener(new NotificationGuts.OnHeightChangedListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationGutsManager$$ExternalSyntheticLambda5
            @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.OnHeightChangedListener
            public final void onHeightChanged(NotificationGuts notificationGuts) {
                this.f$0.lambda$initializeSnoozeView$1(expandableNotificationRow, notificationGuts);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$initializeSnoozeView$1(ExpandableNotificationRow expandableNotificationRow, NotificationGuts notificationGuts) {
        this.mListContainer.onHeightChanged(expandableNotificationRow, expandableNotificationRow.isShown());
    }

    private void initializeAppOpsInfo(final ExpandableNotificationRow expandableNotificationRow, AppOpsInfo appOpsInfo) {
        final NotificationGuts guts = expandableNotificationRow.getGuts();
        final StatusBarNotification sbn = expandableNotificationRow.getEntry().getSbn();
        PackageManager packageManagerForUser = StatusBar.getPackageManagerForUser(this.mContext, sbn.getUser().getIdentifier());
        AppOpsInfo.OnSettingsClickListener onSettingsClickListener = new AppOpsInfo.OnSettingsClickListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationGutsManager$$ExternalSyntheticLambda0
            @Override // com.android.systemui.statusbar.notification.row.AppOpsInfo.OnSettingsClickListener
            public final void onClick(View view, String str, int i, ArraySet arraySet) {
                this.f$0.lambda$initializeAppOpsInfo$2(sbn, guts, expandableNotificationRow, view, str, i, arraySet);
            }
        };
        if (expandableNotificationRow.getEntry().mActiveAppOps.isEmpty()) {
            return;
        }
        appOpsInfo.bindGuts(packageManagerForUser, onSettingsClickListener, sbn, this.mUiEventLogger, expandableNotificationRow.getEntry().mActiveAppOps);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$initializeAppOpsInfo$2(StatusBarNotification statusBarNotification, NotificationGuts notificationGuts, ExpandableNotificationRow expandableNotificationRow, View view, String str, int i, ArraySet arraySet) {
        this.mUiEventLogger.logWithInstanceId(NotificationAppOpsEvent.NOTIFICATION_APP_OPS_SETTINGS_CLICK, statusBarNotification.getUid(), statusBarNotification.getPackageName(), statusBarNotification.getInstanceId());
        this.mMetricsLogger.action(1346);
        notificationGuts.resetFalsingCheck();
        startAppOpsSettingsActivity(str, i, arraySet, expandableNotificationRow);
    }

    @VisibleForTesting
    void initializeNotificationInfo(final ExpandableNotificationRow expandableNotificationRow, NotificationInfo notificationInfo) throws Exception {
        final NotificationGuts guts = expandableNotificationRow.getGuts();
        final StatusBarNotification sbn = expandableNotificationRow.getEntry().getSbn();
        final String packageName = sbn.getPackageName();
        UserHandle user = sbn.getUser();
        notificationInfo.bindNotification(StatusBar.getPackageManagerForUser(this.mContext, user.getIdentifier()), this.mNotificationManager, this.mVisualStabilityManager, this.mChannelEditorDialogController, packageName, expandableNotificationRow.getEntry().getChannel(), expandableNotificationRow.getUniqueChannels(), expandableNotificationRow.getEntry(), (!user.equals(UserHandle.ALL) || this.mLockscreenUserManager.getCurrentUserId() == 0) ? new NotificationInfo.OnSettingsClickListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationGutsManager$$ExternalSyntheticLambda8
            @Override // com.android.systemui.statusbar.notification.row.NotificationInfo.OnSettingsClickListener
            public final void onClick(View view, NotificationChannel notificationChannel, int i) {
                this.f$0.lambda$initializeNotificationInfo$4(guts, sbn, packageName, expandableNotificationRow, view, notificationChannel, i);
            }
        } : null, new NotificationInfo.OnAppSettingsClickListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationGutsManager$$ExternalSyntheticLambda6
            @Override // com.android.systemui.statusbar.notification.row.NotificationInfo.OnAppSettingsClickListener
            public final void onClick(View view, Intent intent) {
                this.f$0.lambda$initializeNotificationInfo$3(guts, sbn, expandableNotificationRow, view, intent);
            }
        }, this.mUiEventLogger, this.mDeviceProvisionedController.isDeviceProvisioned(), expandableNotificationRow.getIsNonblockable(), this.mHighPriorityProvider.isHighPriority(expandableNotificationRow.getEntry()));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$initializeNotificationInfo$3(NotificationGuts notificationGuts, StatusBarNotification statusBarNotification, ExpandableNotificationRow expandableNotificationRow, View view, Intent intent) {
        this.mMetricsLogger.action(206);
        notificationGuts.resetFalsingCheck();
        this.mNotificationActivityStarter.startNotificationGutsIntent(intent, statusBarNotification.getUid(), expandableNotificationRow);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$initializeNotificationInfo$4(NotificationGuts notificationGuts, StatusBarNotification statusBarNotification, String str, ExpandableNotificationRow expandableNotificationRow, View view, NotificationChannel notificationChannel, int i) {
        this.mMetricsLogger.action(205);
        notificationGuts.resetFalsingCheck();
        this.mOnSettingsClickListener.onSettingsClick(statusBarNotification.getKey());
        startAppNotificationSettingsActivity(str, i, notificationChannel, expandableNotificationRow);
    }

    @VisibleForTesting
    void initializePartialConversationNotificationInfo(final ExpandableNotificationRow expandableNotificationRow, PartialConversationInfo partialConversationInfo) throws Exception {
        final NotificationGuts guts = expandableNotificationRow.getGuts();
        final StatusBarNotification sbn = expandableNotificationRow.getEntry().getSbn();
        final String packageName = sbn.getPackageName();
        UserHandle user = sbn.getUser();
        partialConversationInfo.bindNotification(StatusBar.getPackageManagerForUser(this.mContext, user.getIdentifier()), this.mNotificationManager, this.mChannelEditorDialogController, packageName, expandableNotificationRow.getEntry().getChannel(), expandableNotificationRow.getUniqueChannels(), expandableNotificationRow.getEntry(), (!user.equals(UserHandle.ALL) || this.mLockscreenUserManager.getCurrentUserId() == 0) ? new NotificationInfo.OnSettingsClickListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationGutsManager$$ExternalSyntheticLambda7
            @Override // com.android.systemui.statusbar.notification.row.NotificationInfo.OnSettingsClickListener
            public final void onClick(View view, NotificationChannel notificationChannel, int i) {
                this.f$0.lambda$initializePartialConversationNotificationInfo$5(guts, sbn, packageName, expandableNotificationRow, view, notificationChannel, i);
            }
        } : null, this.mDeviceProvisionedController.isDeviceProvisioned(), expandableNotificationRow.getIsNonblockable());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$initializePartialConversationNotificationInfo$5(NotificationGuts notificationGuts, StatusBarNotification statusBarNotification, String str, ExpandableNotificationRow expandableNotificationRow, View view, NotificationChannel notificationChannel, int i) {
        this.mMetricsLogger.action(205);
        notificationGuts.resetFalsingCheck();
        this.mOnSettingsClickListener.onSettingsClick(statusBarNotification.getKey());
        startAppNotificationSettingsActivity(str, i, notificationChannel, expandableNotificationRow);
    }

    @VisibleForTesting
    void initializeConversationNotificationInfo(final ExpandableNotificationRow expandableNotificationRow, NotificationConversationInfo notificationConversationInfo) throws Exception {
        final NotificationGuts guts = expandableNotificationRow.getGuts();
        NotificationEntry entry = expandableNotificationRow.getEntry();
        final StatusBarNotification sbn = entry.getSbn();
        final String packageName = sbn.getPackageName();
        UserHandle user = sbn.getUser();
        PackageManager packageManagerForUser = StatusBar.getPackageManagerForUser(this.mContext, user.getIdentifier());
        NotificationConversationInfo.OnSnoozeClickListener onSnoozeClickListener = new NotificationConversationInfo.OnSnoozeClickListener(this, sbn) { // from class: com.android.systemui.statusbar.notification.row.NotificationGutsManager$$ExternalSyntheticLambda3
        };
        NotificationConversationInfo.OnConversationSettingsClickListener onConversationSettingsClickListener = new NotificationConversationInfo.OnConversationSettingsClickListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationGutsManager$$ExternalSyntheticLambda1
            @Override // com.android.systemui.statusbar.notification.row.NotificationConversationInfo.OnConversationSettingsClickListener
            public final void onClick() {
                this.f$0.lambda$initializeConversationNotificationInfo$8(sbn, expandableNotificationRow);
            }
        };
        NotificationConversationInfo.OnSettingsClickListener onSettingsClickListener = (!user.equals(UserHandle.ALL) || this.mLockscreenUserManager.getCurrentUserId() == 0) ? new NotificationConversationInfo.OnSettingsClickListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationGutsManager$$ExternalSyntheticLambda2
            @Override // com.android.systemui.statusbar.notification.row.NotificationConversationInfo.OnSettingsClickListener
            public final void onClick(View view, NotificationChannel notificationChannel, int i) {
                this.f$0.lambda$initializeConversationNotificationInfo$9(guts, sbn, packageName, expandableNotificationRow, view, notificationChannel, i);
            }
        } : null;
        Context context = this.mContext;
        notificationConversationInfo.bindNotification(this.mShortcutManager, packageManagerForUser, this.mNotificationManager, this.mVisualStabilityManager, packageName, entry.getChannel(), entry, entry.getBubbleMetadata(), onSettingsClickListener, onSnoozeClickListener, new ConversationIconFactory(context, this.mLauncherApps, packageManagerForUser, IconDrawableFactory.newInstance(context, false), this.mContext.getResources().getDimensionPixelSize(R.dimen.notification_guts_conversation_icon_size)), this.mContextTracker.getCurrentUserContext(), this.mBuilderProvider, this.mDeviceProvisionedController.isDeviceProvisioned(), this.mMainHandler, this.mBgHandler, onConversationSettingsClickListener, this.mBubbleController);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$initializeConversationNotificationInfo$8(StatusBarNotification statusBarNotification, ExpandableNotificationRow expandableNotificationRow) {
        startConversationSettingsActivity(statusBarNotification.getUid(), expandableNotificationRow);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$initializeConversationNotificationInfo$9(NotificationGuts notificationGuts, StatusBarNotification statusBarNotification, String str, ExpandableNotificationRow expandableNotificationRow, View view, NotificationChannel notificationChannel, int i) {
        this.mMetricsLogger.action(205);
        notificationGuts.resetFalsingCheck();
        this.mOnSettingsClickListener.onSettingsClick(statusBarNotification.getKey());
        startAppNotificationSettingsActivity(str, i, notificationChannel, expandableNotificationRow);
    }

    public void closeAndSaveGuts(boolean z, boolean z2, boolean z3, int i, int i2, boolean z4) {
        NotificationGuts notificationGuts = this.mNotificationGutsExposed;
        if (notificationGuts != null) {
            notificationGuts.removeCallbacks(this.mOpenRunnable);
            this.mNotificationGutsExposed.closeControls(z, z3, i, i2, z2);
        }
        if (z4) {
            this.mListContainer.resetExposedMenuView(false, true);
        }
    }

    public NotificationGuts getExposedGuts() {
        return this.mNotificationGutsExposed;
    }

    public void setExposedGuts(NotificationGuts notificationGuts) {
        this.mNotificationGutsExposed = notificationGuts;
    }

    public boolean openGuts(final View view, final int i, final int i2, final NotificationMenuRowPlugin.MenuItem menuItem) {
        if ((menuItem.getGutsView() instanceof NotificationGuts.GutsContent) && ((NotificationGuts.GutsContent) menuItem.getGutsView()).needsFalsingProtection()) {
            StatusBarStateController statusBarStateController = this.mStatusBarStateController;
            if (statusBarStateController instanceof StatusBarStateControllerImpl) {
                ((StatusBarStateControllerImpl) statusBarStateController).setLeaveOpenOnKeyguardHide(true);
            }
            this.mStatusBarLazy.get().executeRunnableDismissingKeyguard(new Runnable() { // from class: com.android.systemui.statusbar.notification.row.NotificationGutsManager$$ExternalSyntheticLambda10
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$openGuts$11(view, i, i2, menuItem);
                }
            }, null, false, true, true);
            return true;
        }
        return lambda$openGuts$10(view, i, i2, menuItem);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$openGuts$11(final View view, final int i, final int i2, final NotificationMenuRowPlugin.MenuItem menuItem) {
        this.mMainHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.notification.row.NotificationGutsManager$$ExternalSyntheticLambda9
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$openGuts$10(view, i, i2, menuItem);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @VisibleForTesting
    /* renamed from: openGutsInternal, reason: merged with bridge method [inline-methods] */
    public boolean lambda$openGuts$10(View view, final int i, final int i2, final NotificationMenuRowPlugin.MenuItem menuItem) {
        if (!(view instanceof ExpandableNotificationRow)) {
            return false;
        }
        if (view.getWindowToken() == null) {
            Log.e("NotificationGutsManager", "Trying to show notification guts, but not attached to window");
            return false;
        }
        final ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
        view.performHapticFeedback(0);
        if (expandableNotificationRow.areGutsExposed()) {
            closeAndSaveGuts(false, false, true, -1, -1, true);
            return false;
        }
        expandableNotificationRow.ensureGutsInflated();
        final NotificationGuts guts = expandableNotificationRow.getGuts();
        this.mNotificationGutsExposed = guts;
        if (!bindGuts(expandableNotificationRow, menuItem) || guts == null) {
            return false;
        }
        guts.setVisibility(4);
        Runnable runnable = new Runnable() { // from class: com.android.systemui.statusbar.notification.row.NotificationGutsManager.1
            @Override // java.lang.Runnable
            public void run() {
                if (expandableNotificationRow.getWindowToken() == null) {
                    Log.e("NotificationGutsManager", "Trying to show notification guts in post(), but not attached to window");
                    return;
                }
                guts.setVisibility(0);
                boolean z = NotificationGutsManager.this.mStatusBarStateController.getState() == 1 && !NotificationGutsManager.this.mAccessibilityManager.isTouchExplorationEnabled();
                expandableNotificationRow.onGutsOpened();
                guts.openControls(!expandableNotificationRow.isBlockingHelperShowing(), i, i2, z, null);
                expandableNotificationRow.closeRemoteInput();
                NotificationGutsManager.this.mListContainer.onHeightChanged(expandableNotificationRow, true);
                NotificationGutsManager.this.mGutsMenuItem = menuItem;
            }
        };
        this.mOpenRunnable = runnable;
        guts.post(runnable);
        return true;
    }

    @Override // com.android.systemui.statusbar.NotificationLifetimeExtender
    public void setCallback(NotificationLifetimeExtender.NotificationSafeToRemoveCallback notificationSafeToRemoveCallback) {
        this.mNotificationLifetimeFinishedCallback = notificationSafeToRemoveCallback;
    }

    @Override // com.android.systemui.statusbar.NotificationLifetimeExtender
    public boolean shouldExtendLifetime(NotificationEntry notificationEntry) {
        return (notificationEntry == null || this.mNotificationGutsExposed == null || notificationEntry.getGuts() == null || this.mNotificationGutsExposed != notificationEntry.getGuts() || this.mNotificationGutsExposed.isLeavebehind()) ? false : true;
    }

    @Override // com.android.systemui.statusbar.NotificationLifetimeExtender
    public void setShouldManageLifetime(NotificationEntry notificationEntry, boolean z) {
        if (z) {
            this.mKeyToRemoveOnGutsClosed = notificationEntry.getKey();
            if (Log.isLoggable("NotificationGutsManager", 3)) {
                Log.d("NotificationGutsManager", "Keeping notification because it's showing guts. " + notificationEntry.getKey());
                return;
            }
            return;
        }
        String str = this.mKeyToRemoveOnGutsClosed;
        if (str == null || !str.equals(notificationEntry.getKey())) {
            return;
        }
        this.mKeyToRemoveOnGutsClosed = null;
        if (Log.isLoggable("NotificationGutsManager", 3)) {
            Log.d("NotificationGutsManager", "Notification that was kept for guts was updated. " + notificationEntry.getKey());
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("NotificationGutsManager state:");
        printWriter.print("  mKeyToRemoveOnGutsClosed: ");
        printWriter.println(this.mKeyToRemoveOnGutsClosed);
    }
}
