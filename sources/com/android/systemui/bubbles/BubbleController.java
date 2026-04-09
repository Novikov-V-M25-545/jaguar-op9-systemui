package com.android.systemui.bubbles;

import android.app.ActivityManager;
import android.app.INotificationManager;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.service.notification.NotificationListenerService;
import android.service.notification.ZenModeConfig;
import android.util.ArraySet;
import android.util.Log;
import android.util.Pair;
import android.util.SparseSetArray;
import android.view.WindowManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.systemui.Dumpable;
import com.android.systemui.bubbles.BubbleData;
import com.android.systemui.bubbles.BubbleLogger;
import com.android.systemui.bubbles.BubbleStackView;
import com.android.systemui.bubbles.BubbleViewInfoTask;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.model.SysUiState;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.PinnedStackListenerForwarder;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.shared.system.WindowManagerWrapper;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationRemoveInterceptor;
import com.android.systemui.statusbar.ScrimView;
import com.android.systemui.statusbar.notification.NotificationChannelHelper;
import com.android.systemui.statusbar.notification.NotificationEntryListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.phone.ShadeController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.util.FloatingContentCoordinator;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class BubbleController implements ConfigurationController.ConfigurationListener, Dumpable {
    private IStatusBarService mBarService;
    private BubbleData mBubbleData;
    private final BubbleData.Listener mBubbleDataListener;
    private BubbleIconFactory mBubbleIconFactory;
    private ScrimView mBubbleScrim;
    private final Context mContext;
    private int mCurrentUserId;
    private final BubbleDataRepository mDataRepository;
    private BubbleExpandListener mExpandListener;
    private final FloatingContentCoordinator mFloatingContentCoordinator;
    private INotificationManager mINotificationManager;
    private boolean mInflateSynchronously;
    private NotificationEntry mNotifEntryToExpandOnShadeUnlock;
    private final NotifPipeline mNotifPipeline;
    private final NotificationLockscreenUserManager mNotifUserManager;
    private final NotificationEntryManager mNotificationEntryManager;
    private final NotificationGroupManager mNotificationGroupManager;
    private final NotificationInterruptStateProvider mNotificationInterruptStateProvider;
    private final NotificationShadeWindowController mNotificationShadeWindowController;
    private BubbleStackView.RelativeStackPosition mPositionFromRemovedStack;
    private final SparseSetArray<String> mSavedBubbleKeysPerUser;
    private final ShadeController mShadeController;
    private BubbleStackView mStackView;
    private StatusBarStateListener mStatusBarStateListener;
    private BubbleStackView.SurfaceSynchronizer mSurfaceSynchronizer;
    private SysUiState mSysUiState;
    private final BubbleTaskStackListener mTaskStackListener;
    private NotificationListenerService.Ranking mTmpRanking;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWmLayoutParams;
    private final ZenModeController mZenModeController;
    private BubbleLogger mLogger = new BubbleLoggerImpl();
    private Runnable mOverflowCallback = null;
    private boolean mOverflowDataLoaded = false;
    private Handler mHandler = new Handler();
    private boolean mAddedToWindowManager = false;
    private int mOrientation = 0;
    private int mDensityDpi = 0;
    private int mLayoutDirection = -1;
    private final List<NotifCallback> mCallbacks = new ArrayList();
    private boolean mImeVisible = false;

    public interface BubbleExpandListener {
        void onBubbleExpandChanged(boolean z, String str);
    }

    public interface NotifCallback {
        void invalidateNotifications(String str);

        void maybeCancelSummary(NotificationEntry notificationEntry);

        void removeNotification(NotificationEntry notificationEntry, int i);
    }

    public interface NotificationSuppressionChangedListener {
        void onBubbleNotificationSuppressionChange(Bubble bubble);
    }

    public interface PendingIntentCanceledListener {
        void onPendingIntentCanceled(Bubble bubble);
    }

    private class StatusBarStateListener implements StatusBarStateController.StateListener {
        private int mState;

        private StatusBarStateListener() {
        }

        public int getCurrentState() {
            return this.mState;
        }

        @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
        public void onStateChanged(int i) {
            this.mState = i;
            if (i != 0) {
                BubbleController.this.collapseStack();
            }
            if (BubbleController.this.mNotifEntryToExpandOnShadeUnlock != null) {
                BubbleController bubbleController = BubbleController.this;
                bubbleController.expandStackAndSelectBubble(bubbleController.mNotifEntryToExpandOnShadeUnlock);
                BubbleController.this.mNotifEntryToExpandOnShadeUnlock = null;
            }
            BubbleController.this.updateStack();
        }
    }

    public BubbleController(Context context, NotificationShadeWindowController notificationShadeWindowController, StatusBarStateController statusBarStateController, ShadeController shadeController, BubbleData bubbleData, BubbleStackView.SurfaceSynchronizer surfaceSynchronizer, ConfigurationController configurationController, NotificationInterruptStateProvider notificationInterruptStateProvider, ZenModeController zenModeController, NotificationLockscreenUserManager notificationLockscreenUserManager, NotificationGroupManager notificationGroupManager, NotificationEntryManager notificationEntryManager, NotifPipeline notifPipeline, FeatureFlags featureFlags, DumpManager dumpManager, FloatingContentCoordinator floatingContentCoordinator, BubbleDataRepository bubbleDataRepository, SysUiState sysUiState, INotificationManager iNotificationManager, IStatusBarService iStatusBarService, WindowManager windowManager, LauncherApps launcherApps) {
        BubbleData.Listener listener = new BubbleData.Listener() { // from class: com.android.systemui.bubbles.BubbleController.10
            @Override // com.android.systemui.bubbles.BubbleData.Listener
            public void applyUpdate(BubbleData.Update update) {
                NotificationEntry pendingOrActiveNotif;
                BubbleController.this.ensureStackViewCreated();
                BubbleController.this.loadOverflowBubblesFromDisk();
                if (BubbleController.this.mOverflowCallback != null) {
                    BubbleController.this.mOverflowCallback.run();
                }
                if (update.expandedChanged && !update.expanded) {
                    BubbleController.this.mStackView.setExpanded(false);
                    BubbleController.this.mNotificationShadeWindowController.setRequestTopUi(false, "Bubbles");
                }
                ArrayList arrayList = new ArrayList(update.removedBubbles);
                ArrayList arrayList2 = new ArrayList();
                Iterator it = arrayList.iterator();
                while (it.hasNext()) {
                    Pair pair = (Pair) it.next();
                    Bubble bubble = (Bubble) pair.first;
                    int iIntValue = ((Integer) pair.second).intValue();
                    if (BubbleController.this.mStackView != null) {
                        BubbleController.this.mStackView.removeBubble(bubble);
                    }
                    if (iIntValue != 8 && iIntValue != 14) {
                        if (iIntValue == 5) {
                            arrayList2.add(bubble);
                        }
                        NotificationEntry pendingOrActiveNotif2 = BubbleController.this.mNotificationEntryManager.getPendingOrActiveNotif(bubble.getKey());
                        if (!BubbleController.this.mBubbleData.hasBubbleInStackWithKey(bubble.getKey())) {
                            if (!BubbleController.this.mBubbleData.hasOverflowBubbleWithKey(bubble.getKey()) && (!bubble.showInShade() || iIntValue == 5 || iIntValue == 9)) {
                                for (NotifCallback notifCallback : BubbleController.this.mCallbacks) {
                                    if (pendingOrActiveNotif2 != null) {
                                        notifCallback.removeNotification(pendingOrActiveNotif2, 2);
                                    }
                                }
                            } else {
                                if (bubble.isBubble()) {
                                    BubbleController.this.setIsBubble(bubble, false);
                                }
                                if (pendingOrActiveNotif2 != null && pendingOrActiveNotif2.getRow() != null) {
                                    pendingOrActiveNotif2.getRow().updateBubbleButton();
                                }
                            }
                        }
                        if (pendingOrActiveNotif2 != null) {
                            if (BubbleController.this.mBubbleData.getBubblesInGroup(pendingOrActiveNotif2.getSbn().getGroupKey(), BubbleController.this.mNotificationEntryManager).isEmpty()) {
                                Iterator it2 = BubbleController.this.mCallbacks.iterator();
                                while (it2.hasNext()) {
                                    ((NotifCallback) it2.next()).maybeCancelSummary(pendingOrActiveNotif2);
                                }
                            }
                        }
                    }
                }
                BubbleController.this.mDataRepository.removeBubbles(arrayList2);
                if (update.addedBubble != null && BubbleController.this.mStackView != null) {
                    BubbleController.this.mDataRepository.addBubble(update.addedBubble);
                    BubbleController.this.mStackView.addBubble(update.addedBubble);
                }
                if (update.updatedBubble != null && BubbleController.this.mStackView != null) {
                    BubbleController.this.mStackView.updateBubble(update.updatedBubble);
                }
                if (update.orderChanged && BubbleController.this.mStackView != null) {
                    BubbleController.this.mDataRepository.addBubbles(update.bubbles);
                    BubbleController.this.mStackView.updateBubbleOrder(update.bubbles);
                }
                if (update.selectionChanged && BubbleController.this.mStackView != null) {
                    BubbleController.this.mStackView.setSelectedBubble(update.selectedBubble);
                    if (update.selectedBubble != null && (pendingOrActiveNotif = BubbleController.this.mNotificationEntryManager.getPendingOrActiveNotif(update.selectedBubble.getKey())) != null) {
                        BubbleController.this.mNotificationGroupManager.updateSuppression(pendingOrActiveNotif);
                    }
                }
                if (update.expandedChanged && update.expanded && BubbleController.this.mStackView != null) {
                    BubbleController.this.mStackView.setExpanded(true);
                    BubbleController.this.mNotificationShadeWindowController.setRequestTopUi(true, "Bubbles");
                }
                Iterator it3 = BubbleController.this.mCallbacks.iterator();
                while (it3.hasNext()) {
                    ((NotifCallback) it3.next()).invalidateNotifications("BubbleData.Listener.applyUpdate");
                }
                BubbleController.this.updateStack();
            }
        };
        this.mBubbleDataListener = listener;
        dumpManager.registerDumpable("Bubbles", this);
        this.mContext = context;
        this.mShadeController = shadeController;
        this.mNotificationInterruptStateProvider = notificationInterruptStateProvider;
        this.mNotifUserManager = notificationLockscreenUserManager;
        this.mZenModeController = zenModeController;
        this.mFloatingContentCoordinator = floatingContentCoordinator;
        this.mDataRepository = bubbleDataRepository;
        this.mINotificationManager = iNotificationManager;
        zenModeController.addCallback(new ZenModeController.Callback() { // from class: com.android.systemui.bubbles.BubbleController.1
            @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
            public void onZenChanged(int i) {
                for (Bubble bubble : BubbleController.this.mBubbleData.getBubbles()) {
                    bubble.setShowDot(bubble.showInShade());
                }
            }

            @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
            public void onConfigChanged(ZenModeConfig zenModeConfig) {
                for (Bubble bubble : BubbleController.this.mBubbleData.getBubbles()) {
                    bubble.setShowDot(bubble.showInShade());
                }
            }
        });
        configurationController.addCallback(this);
        this.mSysUiState = sysUiState;
        this.mBubbleData = bubbleData;
        bubbleData.setListener(listener);
        this.mBubbleData.setSuppressionChangedListener(new NotificationSuppressionChangedListener() { // from class: com.android.systemui.bubbles.BubbleController.2
            @Override // com.android.systemui.bubbles.BubbleController.NotificationSuppressionChangedListener
            public void onBubbleNotificationSuppressionChange(Bubble bubble) {
                try {
                    BubbleController.this.mBarService.onBubbleNotificationSuppressionChanged(bubble.getKey(), !bubble.showInShade());
                } catch (RemoteException unused) {
                }
            }
        });
        this.mBubbleData.setPendingIntentCancelledListener(new PendingIntentCanceledListener() { // from class: com.android.systemui.bubbles.BubbleController$$ExternalSyntheticLambda1
            @Override // com.android.systemui.bubbles.BubbleController.PendingIntentCanceledListener
            public final void onPendingIntentCanceled(Bubble bubble) {
                this.f$0.lambda$new$1(bubble);
            }
        });
        this.mNotificationEntryManager = notificationEntryManager;
        this.mNotificationGroupManager = notificationGroupManager;
        this.mNotifPipeline = notifPipeline;
        if (!featureFlags.isNewNotifPipelineRenderingEnabled()) {
            setupNEM();
        } else {
            setupNotifPipeline();
        }
        this.mNotificationShadeWindowController = notificationShadeWindowController;
        StatusBarStateListener statusBarStateListener = new StatusBarStateListener();
        this.mStatusBarStateListener = statusBarStateListener;
        statusBarStateController.addCallback(statusBarStateListener);
        BubbleTaskStackListener bubbleTaskStackListener = new BubbleTaskStackListener();
        this.mTaskStackListener = bubbleTaskStackListener;
        ActivityManagerWrapper.getInstance().registerTaskStackListener(bubbleTaskStackListener);
        try {
            WindowManagerWrapper.getInstance().addPinnedStackListener(new BubblesImeListener());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        this.mSurfaceSynchronizer = surfaceSynchronizer;
        this.mWindowManager = windowManager;
        this.mBarService = iStatusBarService == null ? IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar")) : iStatusBarService;
        ScrimView scrimView = new ScrimView(this.mContext);
        this.mBubbleScrim = scrimView;
        scrimView.setImportantForAccessibility(2);
        this.mSavedBubbleKeysPerUser = new SparseSetArray<>();
        this.mCurrentUserId = this.mNotifUserManager.getCurrentUserId();
        this.mNotifUserManager.addUserChangedListener(new NotificationLockscreenUserManager.UserChangedListener() { // from class: com.android.systemui.bubbles.BubbleController.3
            @Override // com.android.systemui.statusbar.NotificationLockscreenUserManager.UserChangedListener
            public void onUserChanged(int i) {
                BubbleController bubbleController = BubbleController.this;
                bubbleController.saveBubbles(bubbleController.mCurrentUserId);
                BubbleController.this.mBubbleData.dismissAll(8);
                BubbleController.this.restoreBubbles(i);
                BubbleController.this.mCurrentUserId = i;
            }
        });
        this.mBubbleIconFactory = new BubbleIconFactory(context);
        launcherApps.registerCallback(new LauncherApps.Callback() { // from class: com.android.systemui.bubbles.BubbleController.4
            @Override // android.content.pm.LauncherApps.Callback
            public void onPackageAdded(String str, UserHandle userHandle) {
            }

            @Override // android.content.pm.LauncherApps.Callback
            public void onPackageChanged(String str, UserHandle userHandle) {
            }

            @Override // android.content.pm.LauncherApps.Callback
            public void onPackagesAvailable(String[] strArr, UserHandle userHandle, boolean z) {
            }

            @Override // android.content.pm.LauncherApps.Callback
            public void onPackageRemoved(String str, UserHandle userHandle) {
                BubbleController.this.mBubbleData.removeBubblesWithPackageName(str, 13);
            }

            @Override // android.content.pm.LauncherApps.Callback
            public void onPackagesUnavailable(String[] strArr, UserHandle userHandle, boolean z) {
                for (String str : strArr) {
                    BubbleController.this.mBubbleData.removeBubblesWithPackageName(str, 13);
                }
            }

            @Override // android.content.pm.LauncherApps.Callback
            public void onShortcutsChanged(String str, List<ShortcutInfo> list, UserHandle userHandle) {
                super.onShortcutsChanged(str, list, userHandle);
                BubbleController.this.mBubbleData.removeBubblesWithInvalidShortcuts(str, list, 12);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$1(final Bubble bubble) {
        if (bubble.getBubbleIntent() == null) {
            return;
        }
        if (bubble.isIntentActive() || this.mBubbleData.hasBubbleInStackWithKey(bubble.getKey())) {
            bubble.setPendingIntentCanceled();
        } else {
            this.mHandler.post(new Runnable() { // from class: com.android.systemui.bubbles.BubbleController$$ExternalSyntheticLambda6
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$new$0(bubble);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0(Bubble bubble) {
        removeBubble(bubble.getKey(), 10);
    }

    public void addNotifCallback(NotifCallback notifCallback) {
        this.mCallbacks.add(notifCallback);
    }

    public void hideCurrentInputMethod() {
        try {
            this.mBarService.hideCurrentInputMethodForBubbles();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void setupNEM() {
        this.mNotificationEntryManager.addNotificationEntryListener(new NotificationEntryListener() { // from class: com.android.systemui.bubbles.BubbleController.5
            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onPendingEntryAdded(NotificationEntry notificationEntry) {
                BubbleController.this.onEntryAdded(notificationEntry);
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onPreEntryUpdated(NotificationEntry notificationEntry) {
                BubbleController.this.onEntryUpdated(notificationEntry);
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onEntryRemoved(NotificationEntry notificationEntry, NotificationVisibility notificationVisibility, boolean z, int i) {
                BubbleController.this.onEntryRemoved(notificationEntry);
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onNotificationRankingUpdated(NotificationListenerService.RankingMap rankingMap) {
                BubbleController.this.onRankingUpdated(rankingMap);
            }
        });
        this.mNotificationEntryManager.addNotificationRemoveInterceptor(new NotificationRemoveInterceptor() { // from class: com.android.systemui.bubbles.BubbleController.6
            @Override // com.android.systemui.statusbar.NotificationRemoveInterceptor
            public boolean onNotificationRemoveRequested(String str, NotificationEntry notificationEntry, int i) {
                boolean z = true;
                boolean z2 = i == 3;
                boolean z3 = i == 2 || i == 1;
                boolean z4 = i == 8 || i == 9;
                boolean z5 = i == 12;
                if ((notificationEntry == null || !notificationEntry.isRowDismissed() || z4) && !z2 && !z3 && !z5) {
                    z = false;
                }
                if (z) {
                    return BubbleController.this.handleDismissalInterception(notificationEntry);
                }
                return false;
            }
        });
        this.mNotificationGroupManager.addOnGroupChangeListener(new NotificationGroupManager.OnGroupChangeListener() { // from class: com.android.systemui.bubbles.BubbleController.7
            @Override // com.android.systemui.statusbar.phone.NotificationGroupManager.OnGroupChangeListener
            public void onGroupSuppressionChanged(NotificationGroupManager.NotificationGroup notificationGroup, boolean z) {
                NotificationEntry notificationEntry = notificationGroup.summary;
                String groupKey = notificationEntry != null ? notificationEntry.getSbn().getGroupKey() : null;
                if (z || groupKey == null || !BubbleController.this.mBubbleData.isSummarySuppressed(groupKey)) {
                    return;
                }
                BubbleController.this.mBubbleData.removeSuppressedSummary(groupKey);
            }
        });
        addNotifCallback(new NotifCallback() { // from class: com.android.systemui.bubbles.BubbleController.8
            @Override // com.android.systemui.bubbles.BubbleController.NotifCallback
            public void removeNotification(NotificationEntry notificationEntry, int i) {
                BubbleController.this.mNotificationEntryManager.performRemoveNotification(notificationEntry.getSbn(), i);
            }

            @Override // com.android.systemui.bubbles.BubbleController.NotifCallback
            public void invalidateNotifications(String str) {
                BubbleController.this.mNotificationEntryManager.updateNotifications(str);
            }

            @Override // com.android.systemui.bubbles.BubbleController.NotifCallback
            public void maybeCancelSummary(NotificationEntry notificationEntry) {
                String groupKey = notificationEntry.getSbn().getGroupKey();
                if (BubbleController.this.mBubbleData.isSummarySuppressed(groupKey)) {
                    BubbleController.this.mBubbleData.removeSuppressedSummary(groupKey);
                    NotificationEntry activeNotificationUnfiltered = BubbleController.this.mNotificationEntryManager.getActiveNotificationUnfiltered(BubbleController.this.mBubbleData.getSummaryKey(groupKey));
                    if (activeNotificationUnfiltered != null) {
                        BubbleController.this.mNotificationEntryManager.performRemoveNotification(activeNotificationUnfiltered.getSbn(), 0);
                    }
                }
                NotificationEntry logicalGroupSummary = BubbleController.this.mNotificationGroupManager.getLogicalGroupSummary(notificationEntry.getSbn());
                if (logicalGroupSummary != null) {
                    ArrayList<NotificationEntry> logicalChildren = BubbleController.this.mNotificationGroupManager.getLogicalChildren(logicalGroupSummary.getSbn());
                    if (logicalGroupSummary.getKey().equals(notificationEntry.getKey())) {
                        return;
                    }
                    if (logicalChildren == null || logicalChildren.isEmpty()) {
                        BubbleController.this.mNotificationEntryManager.performRemoveNotification(logicalGroupSummary.getSbn(), 0);
                    }
                }
            }
        });
    }

    private void setupNotifPipeline() {
        this.mNotifPipeline.addCollectionListener(new NotifCollectionListener() { // from class: com.android.systemui.bubbles.BubbleController.9
            @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
            public void onEntryAdded(NotificationEntry notificationEntry) {
                BubbleController.this.onEntryAdded(notificationEntry);
            }

            @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
            public void onEntryUpdated(NotificationEntry notificationEntry) {
                BubbleController.this.onEntryUpdated(notificationEntry);
            }

            @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
            public void onRankingUpdate(NotificationListenerService.RankingMap rankingMap) {
                BubbleController.this.onRankingUpdated(rankingMap);
            }

            @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
            public void onEntryRemoved(NotificationEntry notificationEntry, int i) {
                BubbleController.this.onEntryRemoved(notificationEntry);
            }
        });
    }

    public ScrimView getScrimForBubble() {
        return this.mBubbleScrim;
    }

    public void onStatusBarVisibilityChanged(boolean z) {
        BubbleStackView bubbleStackView = this.mStackView;
        if (bubbleStackView != null) {
            bubbleStackView.setTemporarilyInvisible((z || isStackExpanded()) ? false : true);
        }
    }

    @VisibleForTesting
    void setInflateSynchronously(boolean z) {
        this.mInflateSynchronously = z;
    }

    void setOverflowCallback(Runnable runnable) {
        this.mOverflowCallback = runnable;
    }

    List<Bubble> getOverflowBubbles() {
        return this.mBubbleData.getOverflowBubbles();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void ensureStackViewCreated() {
        if (this.mStackView == null) {
            BubbleStackView bubbleStackView = new BubbleStackView(this.mContext, this.mBubbleData, this.mSurfaceSynchronizer, this.mFloatingContentCoordinator, this.mSysUiState, new Runnable() { // from class: com.android.systemui.bubbles.BubbleController$$ExternalSyntheticLambda5
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.onAllBubblesAnimatedOut();
                }
            }, new Consumer() { // from class: com.android.systemui.bubbles.BubbleController$$ExternalSyntheticLambda8
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    this.f$0.onImeVisibilityChanged(((Boolean) obj).booleanValue());
                }
            }, new Runnable() { // from class: com.android.systemui.bubbles.BubbleController$$ExternalSyntheticLambda4
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.hideCurrentInputMethod();
                }
            });
            this.mStackView = bubbleStackView;
            bubbleStackView.setStackStartPosition(this.mPositionFromRemovedStack);
            this.mStackView.addView(this.mBubbleScrim);
            BubbleExpandListener bubbleExpandListener = this.mExpandListener;
            if (bubbleExpandListener != null) {
                this.mStackView.setExpandListener(bubbleExpandListener);
            }
            this.mStackView.setUnbubbleConversationCallback(new Consumer() { // from class: com.android.systemui.bubbles.BubbleController$$ExternalSyntheticLambda9
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    this.f$0.lambda$ensureStackViewCreated$2((String) obj);
                }
            });
        }
        addToWindowManagerMaybe();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$ensureStackViewCreated$2(String str) {
        NotificationEntry pendingOrActiveNotif = this.mNotificationEntryManager.getPendingOrActiveNotif(str);
        if (pendingOrActiveNotif != null) {
            onUserChangedBubble(pendingOrActiveNotif, false);
        }
    }

    private void addToWindowManagerMaybe() {
        if (this.mStackView == null || this.mAddedToWindowManager) {
            return;
        }
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2038, 16777224, -3);
        this.mWmLayoutParams = layoutParams;
        layoutParams.setTrustedOverlay();
        this.mWmLayoutParams.setFitInsetsTypes(0);
        WindowManager.LayoutParams layoutParams2 = this.mWmLayoutParams;
        layoutParams2.softInputMode = 16;
        layoutParams2.token = new Binder();
        this.mWmLayoutParams.setTitle("Bubbles!");
        this.mWmLayoutParams.packageName = this.mContext.getPackageName();
        WindowManager.LayoutParams layoutParams3 = this.mWmLayoutParams;
        layoutParams3.layoutInDisplayCutoutMode = 3;
        try {
            this.mAddedToWindowManager = true;
            this.mWindowManager.addView(this.mStackView, layoutParams3);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            updateWmFlags();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onImeVisibilityChanged(boolean z) {
        this.mImeVisible = z;
        updateWmFlags();
    }

    private void removeFromWindowManagerMaybe() {
        if (this.mAddedToWindowManager) {
            try {
                this.mAddedToWindowManager = false;
                BubbleStackView bubbleStackView = this.mStackView;
                if (bubbleStackView != null) {
                    this.mPositionFromRemovedStack = bubbleStackView.getRelativeStackPosition();
                    this.mWindowManager.removeView(this.mStackView);
                    this.mStackView.removeView(this.mBubbleScrim);
                    this.mStackView = null;
                } else {
                    Log.w("Bubbles", "StackView added to WindowManager, but was null when removing!");
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateWmFlags() {
        if (this.mStackView == null) {
            return;
        }
        if (isStackExpanded() && !this.mImeVisible) {
            this.mWmLayoutParams.flags &= -9;
        } else {
            this.mWmLayoutParams.flags |= 8;
        }
        if (this.mAddedToWindowManager) {
            try {
                this.mWindowManager.updateViewLayout(this.mStackView, this.mWmLayoutParams);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onAllBubblesAnimatedOut() {
        BubbleStackView bubbleStackView = this.mStackView;
        if (bubbleStackView != null) {
            bubbleStackView.setVisibility(4);
            removeFromWindowManagerMaybe();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void saveBubbles(int i) {
        this.mSavedBubbleKeysPerUser.remove(i);
        Iterator<Bubble> it = this.mBubbleData.getBubbles().iterator();
        while (it.hasNext()) {
            this.mSavedBubbleKeysPerUser.add(i, it.next().getKey());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void restoreBubbles(int i) {
        ArraySet arraySet = this.mSavedBubbleKeysPerUser.get(i);
        if (arraySet == null) {
            return;
        }
        for (NotificationEntry notificationEntry : this.mNotificationEntryManager.getActiveNotificationsForCurrentUser()) {
            if (arraySet.contains(notificationEntry.getKey()) && this.mNotificationInterruptStateProvider.shouldBubbleUp(notificationEntry) && notificationEntry.isBubble() && canLaunchInActivityView(this.mContext, notificationEntry)) {
                updateBubble(notificationEntry, true, false);
            }
        }
        this.mSavedBubbleKeysPerUser.remove(this.mCurrentUserId);
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onUiModeChanged() {
        updateForThemeChanges();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onOverlayChanged() {
        updateForThemeChanges();
    }

    private void updateForThemeChanges() {
        BubbleStackView bubbleStackView = this.mStackView;
        if (bubbleStackView != null) {
            bubbleStackView.onThemeChanged();
        }
        this.mBubbleIconFactory = new BubbleIconFactory(this.mContext);
        Iterator<Bubble> it = this.mBubbleData.getBubbles().iterator();
        while (it.hasNext()) {
            it.next().inflate(null, this.mContext, this.mStackView, this.mBubbleIconFactory, false);
        }
        Iterator<Bubble> it2 = this.mBubbleData.getOverflowBubbles().iterator();
        while (it2.hasNext()) {
            it2.next().inflate(null, this.mContext, this.mStackView, this.mBubbleIconFactory, false);
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onConfigChanged(Configuration configuration) {
        BubbleStackView bubbleStackView = this.mStackView;
        if (bubbleStackView == null || configuration == null) {
            return;
        }
        int i = configuration.orientation;
        if (i != this.mOrientation) {
            this.mOrientation = i;
            bubbleStackView.onOrientationChanged(i);
        }
        int i2 = configuration.densityDpi;
        if (i2 != this.mDensityDpi) {
            this.mDensityDpi = i2;
            this.mBubbleIconFactory = new BubbleIconFactory(this.mContext);
            this.mStackView.onDisplaySizeChanged();
        }
        if (configuration.getLayoutDirection() != this.mLayoutDirection) {
            int layoutDirection = configuration.getLayoutDirection();
            this.mLayoutDirection = layoutDirection;
            this.mStackView.onLayoutDirectionChanged(layoutDirection);
        }
    }

    boolean inLandscape() {
        return this.mOrientation == 2;
    }

    public void setExpandListener(final BubbleExpandListener bubbleExpandListener) {
        BubbleExpandListener bubbleExpandListener2 = new BubbleExpandListener() { // from class: com.android.systemui.bubbles.BubbleController$$ExternalSyntheticLambda0
            @Override // com.android.systemui.bubbles.BubbleController.BubbleExpandListener
            public final void onBubbleExpandChanged(boolean z, String str) {
                this.f$0.lambda$setExpandListener$3(bubbleExpandListener, z, str);
            }
        };
        this.mExpandListener = bubbleExpandListener2;
        BubbleStackView bubbleStackView = this.mStackView;
        if (bubbleStackView != null) {
            bubbleStackView.setExpandListener(bubbleExpandListener2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setExpandListener$3(BubbleExpandListener bubbleExpandListener, boolean z, String str) {
        if (bubbleExpandListener != null) {
            bubbleExpandListener.onBubbleExpandChanged(z, str);
        }
        updateWmFlags();
    }

    @VisibleForTesting
    boolean hasBubbles() {
        if (this.mStackView == null) {
            return false;
        }
        return this.mBubbleData.hasBubbles();
    }

    public boolean isStackExpanded() {
        return this.mBubbleData.isExpanded();
    }

    public void collapseStack() {
        this.mBubbleData.setExpanded(false);
    }

    public boolean isBubbleNotificationSuppressedFromShade(NotificationEntry notificationEntry) {
        String key = notificationEntry.getKey();
        boolean z = this.mBubbleData.hasAnyBubbleWithKey(key) && !this.mBubbleData.getAnyBubbleWithkey(key).showInShade();
        String groupKey = notificationEntry.getSbn().getGroupKey();
        return (key.equals(this.mBubbleData.getSummaryKey(groupKey)) && this.mBubbleData.isSummarySuppressed(groupKey)) || z;
    }

    public boolean isBubbleExpanded(NotificationEntry notificationEntry) {
        BubbleData bubbleData;
        return isStackExpanded() && (bubbleData = this.mBubbleData) != null && bubbleData.getSelectedBubble() != null && this.mBubbleData.getSelectedBubble().getKey().equals(notificationEntry.getKey());
    }

    void promoteBubbleFromOverflow(Bubble bubble) {
        this.mLogger.log(bubble, BubbleLogger.Event.BUBBLE_OVERFLOW_REMOVE_BACK_TO_STACK);
        bubble.setInflateSynchronously(this.mInflateSynchronously);
        bubble.setShouldAutoExpand(true);
        bubble.markAsAccessedAt(System.currentTimeMillis());
        setIsBubble(bubble, true);
    }

    public void expandStackAndSelectBubble(NotificationEntry notificationEntry) {
        if (this.mStatusBarStateListener.getCurrentState() == 0) {
            this.mNotifEntryToExpandOnShadeUnlock = null;
            String key = notificationEntry.getKey();
            Bubble bubbleInStackWithKey = this.mBubbleData.getBubbleInStackWithKey(key);
            if (bubbleInStackWithKey != null) {
                this.mBubbleData.setSelectedBubble(bubbleInStackWithKey);
                this.mBubbleData.setExpanded(true);
                return;
            }
            Bubble overflowBubbleWithKey = this.mBubbleData.getOverflowBubbleWithKey(key);
            if (overflowBubbleWithKey != null) {
                promoteBubbleFromOverflow(overflowBubbleWithKey);
                return;
            } else {
                if (notificationEntry.canBubble()) {
                    setIsBubble(notificationEntry, true, true);
                    return;
                }
                return;
            }
        }
        this.mNotifEntryToExpandOnShadeUnlock = notificationEntry;
    }

    public void onUserChangedImportance(NotificationEntry notificationEntry) {
        try {
            this.mBarService.onNotificationBubbleChanged(notificationEntry.getKey(), true, 3);
        } catch (RemoteException e) {
            Log.e("Bubbles", e.getMessage());
        }
        this.mShadeController.collapsePanel(true);
        if (notificationEntry.getRow() != null) {
            notificationEntry.getRow().updateBubbleButton();
        }
    }

    public void performBackPressIfNeeded() {
        BubbleStackView bubbleStackView = this.mStackView;
        if (bubbleStackView != null) {
            bubbleStackView.performBackPressIfNeeded();
        }
    }

    void updateBubble(NotificationEntry notificationEntry) {
        updateBubble(notificationEntry, false, true);
    }

    void loadOverflowBubblesFromDisk() {
        if (!this.mBubbleData.getOverflowBubbles().isEmpty() || this.mOverflowDataLoaded) {
            return;
        }
        this.mOverflowDataLoaded = true;
        this.mDataRepository.loadBubbles(new Function1() { // from class: com.android.systemui.bubbles.BubbleController$$ExternalSyntheticLambda10
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return this.f$0.lambda$loadOverflowBubblesFromDisk$6((List) obj);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ Unit lambda$loadOverflowBubblesFromDisk$6(List list) {
        list.forEach(new Consumer() { // from class: com.android.systemui.bubbles.BubbleController$$ExternalSyntheticLambda7
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.lambda$loadOverflowBubblesFromDisk$5((Bubble) obj);
            }
        });
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$loadOverflowBubblesFromDisk$5(final Bubble bubble) {
        if (this.mBubbleData.hasAnyBubbleWithKey(bubble.getKey())) {
            return;
        }
        bubble.inflate(new BubbleViewInfoTask.Callback() { // from class: com.android.systemui.bubbles.BubbleController$$ExternalSyntheticLambda2
            @Override // com.android.systemui.bubbles.BubbleViewInfoTask.Callback
            public final void onBubbleViewsReady(Bubble bubble2) {
                this.f$0.lambda$loadOverflowBubblesFromDisk$4(bubble, bubble2);
            }
        }, this.mContext, this.mStackView, this.mBubbleIconFactory, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$loadOverflowBubblesFromDisk$4(Bubble bubble, Bubble bubble2) {
        this.mBubbleData.overflowBubble(2, bubble);
    }

    void updateBubble(NotificationEntry notificationEntry, boolean z, boolean z2) {
        if (notificationEntry.getImportance() >= 4) {
            notificationEntry.setInterruption();
        }
        if (!notificationEntry.getRanking().visuallyInterruptive() && notificationEntry.getBubbleMetadata() != null && !notificationEntry.getBubbleMetadata().getAutoExpandBubble() && this.mBubbleData.hasOverflowBubbleWithKey(notificationEntry.getKey())) {
            this.mBubbleData.getOverflowBubbleWithKey(notificationEntry.getKey()).setEntry(notificationEntry);
        } else {
            inflateAndAdd(this.mBubbleData.getOrCreateBubble(notificationEntry, null), z, z2);
        }
    }

    void inflateAndAdd(Bubble bubble, final boolean z, final boolean z2) {
        ensureStackViewCreated();
        bubble.setInflateSynchronously(this.mInflateSynchronously);
        bubble.inflate(new BubbleViewInfoTask.Callback() { // from class: com.android.systemui.bubbles.BubbleController$$ExternalSyntheticLambda3
            @Override // com.android.systemui.bubbles.BubbleViewInfoTask.Callback
            public final void onBubbleViewsReady(Bubble bubble2) {
                this.f$0.lambda$inflateAndAdd$7(z, z2, bubble2);
            }
        }, this.mContext, this.mStackView, this.mBubbleIconFactory, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$inflateAndAdd$7(boolean z, boolean z2, Bubble bubble) {
        this.mBubbleData.notificationEntryUpdated(bubble, z, z2);
    }

    public void onUserChangedBubble(NotificationEntry notificationEntry, boolean z) {
        NotificationChannel channel = notificationEntry.getChannel();
        String packageName = notificationEntry.getSbn().getPackageName();
        int uid = notificationEntry.getSbn().getUid();
        if (channel == null || packageName == null) {
            return;
        }
        try {
            this.mBarService.onNotificationBubbleChanged(notificationEntry.getKey(), z, 3);
        } catch (RemoteException unused) {
        }
        NotificationChannel notificationChannelCreateConversationChannelIfNeeded = NotificationChannelHelper.createConversationChannelIfNeeded(this.mContext, this.mINotificationManager, notificationEntry, channel);
        notificationChannelCreateConversationChannelIfNeeded.setAllowBubbles(z);
        try {
            int bubblePreferenceForPackage = this.mINotificationManager.getBubblePreferenceForPackage(packageName, uid);
            if (z && bubblePreferenceForPackage == 0) {
                this.mINotificationManager.setBubblesAllowed(packageName, uid, 2);
            }
            this.mINotificationManager.updateNotificationChannelForPackage(packageName, uid, notificationChannelCreateConversationChannelIfNeeded);
        } catch (RemoteException e) {
            Log.e("Bubbles", e.getMessage());
        }
        if (z) {
            this.mShadeController.collapsePanel(true);
            if (notificationEntry.getRow() != null) {
                notificationEntry.getRow().updateBubbleButton();
            }
        }
    }

    void removeBubble(String str, int i) {
        if (this.mBubbleData.hasAnyBubbleWithKey(str)) {
            this.mBubbleData.dismissBubbleWithKey(str, i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onEntryAdded(NotificationEntry notificationEntry) {
        if (this.mNotificationInterruptStateProvider.shouldBubbleUp(notificationEntry) && notificationEntry.isBubble() && canLaunchInActivityView(this.mContext, notificationEntry)) {
            updateBubble(notificationEntry);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onEntryUpdated(NotificationEntry notificationEntry) {
        boolean z = this.mNotificationInterruptStateProvider.shouldBubbleUp(notificationEntry) && canLaunchInActivityView(this.mContext, notificationEntry);
        if (!z && this.mBubbleData.hasAnyBubbleWithKey(notificationEntry.getKey())) {
            removeBubble(notificationEntry.getKey(), 7);
        } else if (z && notificationEntry.isBubble()) {
            updateBubble(notificationEntry);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onEntryRemoved(NotificationEntry notificationEntry) {
        if (isSummaryOfBubbles(notificationEntry)) {
            String groupKey = notificationEntry.getSbn().getGroupKey();
            this.mBubbleData.removeSuppressedSummary(groupKey);
            ArrayList<Bubble> bubblesInGroup = this.mBubbleData.getBubblesInGroup(groupKey, this.mNotificationEntryManager);
            for (int i = 0; i < bubblesInGroup.size(); i++) {
                removeBubble(bubblesInGroup.get(i).getKey(), 9);
            }
            return;
        }
        removeBubble(notificationEntry.getKey(), 5);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onRankingUpdated(NotificationListenerService.RankingMap rankingMap) {
        if (this.mTmpRanking == null) {
            this.mTmpRanking = new NotificationListenerService.Ranking();
        }
        for (String str : rankingMap.getOrderedKeys()) {
            NotificationEntry pendingOrActiveNotif = this.mNotificationEntryManager.getPendingOrActiveNotif(str);
            rankingMap.getRanking(str, this.mTmpRanking);
            boolean zHasAnyBubbleWithKey = this.mBubbleData.hasAnyBubbleWithKey(str);
            if (zHasAnyBubbleWithKey && !this.mTmpRanking.canBubble()) {
                this.mBubbleData.dismissBubbleWithKey(str, 4);
            } else if (zHasAnyBubbleWithKey && !this.mNotificationInterruptStateProvider.shouldBubbleUp(pendingOrActiveNotif)) {
                this.mBubbleData.dismissBubbleWithKey(str, 14);
            } else if (pendingOrActiveNotif != null && this.mTmpRanking.isBubble() && !zHasAnyBubbleWithKey) {
                pendingOrActiveNotif.setFlagBubble(true);
                onEntryUpdated(pendingOrActiveNotif);
            }
        }
    }

    private void setIsBubble(NotificationEntry notificationEntry, boolean z, boolean z2) {
        Objects.requireNonNull(notificationEntry);
        if (z) {
            notificationEntry.getSbn().getNotification().flags |= LineageHardwareManager.FEATURE_AUTO_CONTRAST;
        } else {
            notificationEntry.getSbn().getNotification().flags &= -4097;
        }
        try {
            this.mBarService.onNotificationBubbleChanged(notificationEntry.getKey(), z, z2 ? 3 : 0);
        } catch (RemoteException unused) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setIsBubble(Bubble bubble, boolean z) {
        Objects.requireNonNull(bubble);
        bubble.setIsBubble(z);
        NotificationEntry pendingOrActiveNotif = this.mNotificationEntryManager.getPendingOrActiveNotif(bubble.getKey());
        if (pendingOrActiveNotif != null) {
            setIsBubble(pendingOrActiveNotif, z, bubble.shouldAutoExpand());
        } else if (z) {
            Bubble orCreateBubble = this.mBubbleData.getOrCreateBubble(null, bubble);
            inflateAndAdd(orCreateBubble, orCreateBubble.shouldAutoExpand(), !orCreateBubble.shouldAutoExpand());
        }
    }

    public boolean handleDismissalInterception(NotificationEntry notificationEntry) {
        if (notificationEntry == null) {
            return false;
        }
        if (isSummaryOfBubbles(notificationEntry)) {
            handleSummaryDismissalInterception(notificationEntry);
        } else {
            Bubble bubbleInStackWithKey = this.mBubbleData.getBubbleInStackWithKey(notificationEntry.getKey());
            if (bubbleInStackWithKey == null || !notificationEntry.isBubble()) {
                bubbleInStackWithKey = this.mBubbleData.getOverflowBubbleWithKey(notificationEntry.getKey());
            }
            if (bubbleInStackWithKey == null) {
                return false;
            }
            bubbleInStackWithKey.setSuppressNotification(true);
            bubbleInStackWithKey.setShowDot(false);
        }
        Iterator<NotifCallback> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            it.next().invalidateNotifications("BubbleController.handleDismissalInterception");
        }
        return true;
    }

    private boolean isSummaryOfBubbles(NotificationEntry notificationEntry) {
        if (notificationEntry == null) {
            return false;
        }
        String groupKey = notificationEntry.getSbn().getGroupKey();
        ArrayList<Bubble> bubblesInGroup = this.mBubbleData.getBubblesInGroup(groupKey, this.mNotificationEntryManager);
        return ((!(this.mBubbleData.isSummarySuppressed(groupKey) && this.mBubbleData.getSummaryKey(groupKey).equals(notificationEntry.getKey())) && !notificationEntry.getSbn().getNotification().isGroupSummary()) || bubblesInGroup == null || bubblesInGroup.isEmpty()) ? false : true;
    }

    private void handleSummaryDismissalInterception(NotificationEntry notificationEntry) {
        List<NotificationEntry> attachedNotifChildren = notificationEntry.getAttachedNotifChildren();
        if (attachedNotifChildren != null) {
            for (int i = 0; i < attachedNotifChildren.size(); i++) {
                NotificationEntry notificationEntry2 = attachedNotifChildren.get(i);
                if (this.mBubbleData.hasAnyBubbleWithKey(notificationEntry2.getKey())) {
                    Bubble anyBubbleWithkey = this.mBubbleData.getAnyBubbleWithkey(notificationEntry2.getKey());
                    if (anyBubbleWithkey != null) {
                        NotificationEntry pendingOrActiveNotif = this.mNotificationEntryManager.getPendingOrActiveNotif(anyBubbleWithkey.getKey());
                        if (pendingOrActiveNotif != null) {
                            this.mNotificationGroupManager.onEntryRemoved(pendingOrActiveNotif);
                        }
                        anyBubbleWithkey.setSuppressNotification(true);
                        anyBubbleWithkey.setShowDot(false);
                    }
                } else {
                    Iterator<NotifCallback> it = this.mCallbacks.iterator();
                    while (it.hasNext()) {
                        it.next().removeNotification(notificationEntry2, 12);
                    }
                }
            }
        }
        this.mNotificationGroupManager.onEntryRemoved(notificationEntry);
        this.mBubbleData.addSummaryToSuppress(notificationEntry.getSbn().getGroupKey(), notificationEntry.getKey());
    }

    public void updateStack() {
        if (this.mStackView == null) {
            return;
        }
        if (this.mStatusBarStateListener.getCurrentState() != 0) {
            this.mStackView.setVisibility(4);
        } else if (hasBubbles()) {
            this.mStackView.setVisibility(0);
        }
        this.mStackView.updateContentDescription();
    }

    public int getExpandedDisplayId(Context context) {
        if (this.mStackView == null) {
            return -1;
        }
        boolean z = context.getDisplay() != null && context.getDisplay().getDisplayId() == 0;
        BubbleViewProvider expandedBubble = this.mStackView.getExpandedBubble();
        if (!z || expandedBubble == null || !isStackExpanded() || this.mNotificationShadeWindowController.getPanelExpanded()) {
            return -1;
        }
        return expandedBubble.getDisplayId();
    }

    @VisibleForTesting
    BubbleStackView getStackView() {
        return this.mStackView;
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("BubbleController state:");
        this.mBubbleData.dump(fileDescriptor, printWriter, strArr);
        printWriter.println();
        BubbleStackView bubbleStackView = this.mStackView;
        if (bubbleStackView != null) {
            bubbleStackView.dump(fileDescriptor, printWriter, strArr);
        }
        printWriter.println();
    }

    private class BubbleTaskStackListener extends TaskStackChangeListener {
        private BubbleTaskStackListener() {
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskMovedToFront(ActivityManager.RunningTaskInfo runningTaskInfo) {
            if (BubbleController.this.mStackView == null || runningTaskInfo.displayId != 0 || BubbleController.this.mStackView.isExpansionAnimating()) {
                return;
            }
            BubbleController.this.mBubbleData.setExpanded(false);
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onActivityRestartAttempt(ActivityManager.RunningTaskInfo runningTaskInfo, boolean z, boolean z2, boolean z3) {
            for (Bubble bubble : BubbleController.this.mBubbleData.getBubbles()) {
                if (bubble.getDisplayId() == runningTaskInfo.displayId) {
                    BubbleController.this.mBubbleData.setSelectedBubble(bubble);
                    BubbleController.this.mBubbleData.setExpanded(true);
                    return;
                }
            }
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onActivityLaunchOnSecondaryDisplayRerouted() {
            if (BubbleController.this.mStackView != null) {
                BubbleController.this.mBubbleData.setExpanded(false);
            }
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onBackPressedOnTaskRoot(ActivityManager.RunningTaskInfo runningTaskInfo) {
            if (BubbleController.this.mStackView != null) {
                int i = runningTaskInfo.displayId;
                BubbleController bubbleController = BubbleController.this;
                if (i == bubbleController.getExpandedDisplayId(bubbleController.mContext)) {
                    if (!BubbleController.this.mImeVisible) {
                        BubbleController.this.mBubbleData.setExpanded(false);
                    } else {
                        BubbleController.this.hideCurrentInputMethod();
                    }
                }
            }
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onSingleTaskDisplayDrawn(int i) {
            if (BubbleController.this.mStackView == null) {
                return;
            }
            BubbleController.this.mStackView.showExpandedViewContents(i);
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onSingleTaskDisplayEmpty(int i) {
            BubbleViewProvider expandedBubble = BubbleController.this.mStackView != null ? BubbleController.this.mStackView.getExpandedBubble() : null;
            int displayId = expandedBubble != null ? expandedBubble.getDisplayId() : -1;
            if (BubbleController.this.mStackView != null && BubbleController.this.mStackView.isExpanded() && displayId == i) {
                BubbleController.this.mBubbleData.setExpanded(false);
            }
            BubbleController.this.mBubbleData.notifyDisplayEmpty(i);
        }
    }

    static boolean canLaunchInActivityView(Context context, NotificationEntry notificationEntry) {
        PendingIntent intent = notificationEntry.getBubbleMetadata() != null ? notificationEntry.getBubbleMetadata().getIntent() : null;
        if (notificationEntry.getBubbleMetadata() != null && notificationEntry.getBubbleMetadata().getShortcutId() != null) {
            return true;
        }
        if (intent == null) {
            Log.w("Bubbles", "Unable to create bubble -- no intent: " + notificationEntry.getKey());
            return false;
        }
        ActivityInfo activityInfoResolveActivityInfo = intent.getIntent().resolveActivityInfo(StatusBar.getPackageManagerForUser(context, notificationEntry.getSbn().getUser().getIdentifier()), 0);
        if (activityInfoResolveActivityInfo == null) {
            Log.w("Bubbles", "Unable to send as bubble, " + notificationEntry.getKey() + " couldn't find activity info for intent: " + intent);
            return false;
        }
        if (ActivityInfo.isResizeableMode(activityInfoResolveActivityInfo.resizeMode)) {
            return true;
        }
        Log.w("Bubbles", "Unable to send as bubble, " + notificationEntry.getKey() + " activity is not resizable for intent: " + intent);
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    class BubblesImeListener extends PinnedStackListenerForwarder.PinnedStackListener {
        private BubblesImeListener() {
        }

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onImeVisibilityChanged(final boolean z, final int i) {
            if (BubbleController.this.mStackView != null) {
                BubbleController.this.mStackView.post(new Runnable() { // from class: com.android.systemui.bubbles.BubbleController$BubblesImeListener$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onImeVisibilityChanged$0(z, i);
                    }
                });
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onImeVisibilityChanged$0(boolean z, int i) {
            BubbleController.this.mStackView.onImeVisibilityChanged(z, i);
        }
    }
}
