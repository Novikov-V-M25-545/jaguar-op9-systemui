package com.android.systemui.pip.tv;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.IActivityTaskManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ParceledListSlice;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.os.Debug;
import android.os.Handler;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.DisplayInfo;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.UiOffloadThread;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.pip.BasePipManager;
import com.android.systemui.pip.PipBoundsHandler;
import com.android.systemui.pip.PipSurfaceTransactionHelper;
import com.android.systemui.pip.PipTaskOrganizer;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.PinnedStackListenerForwarder;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.shared.system.WindowManagerWrapper;
import com.android.systemui.stackdivider.Divider;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class PipManager implements BasePipManager, PipTaskOrganizer.PipTransitionCallback {
    static final boolean DEBUG = Log.isLoggable("PipManager", 3);
    private static List<Pair<String, String>> sSettingsPackageAndClassNamePairList;
    private final MediaSessionManager.OnActiveSessionsChangedListener mActiveMediaSessionListener;
    private IActivityTaskManager mActivityTaskManager;
    private final BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private Rect mCurrentPipBounds;
    private ParceledListSlice mCustomActions;
    private int mImeHeightAdjustment;
    private boolean mImeVisible;
    private boolean mInitialized;
    private int mLastOrientation;
    private String[] mLastPackagesResourceGranted;
    private MediaSessionManager mMediaSessionManager;
    private Rect mMenuModePipBounds;
    private Rect mPipBounds;
    private PipBoundsHandler mPipBoundsHandler;
    private ComponentName mPipComponentName;
    private MediaController mPipMediaController;
    private PipNotification mPipNotification;
    private PipTaskOrganizer mPipTaskOrganizer;
    private int mResizeAnimationDuration;
    private Rect mSettingsPipBounds;
    private int mSuspendPipResizingReason;
    private TaskStackChangeListener mTaskStackListener;
    private int mState = 0;
    private int mResumeResizePinnedStackRunnableState = 0;
    private final Handler mHandler = new Handler();
    private List<Listener> mListeners = new ArrayList();
    private List<MediaListener> mMediaListeners = new ArrayList();
    private Rect mDefaultPipBounds = new Rect();
    private int mPipTaskId = -1;
    private int mPinnedStackId = -1;
    private final DisplayInfo mTmpDisplayInfo = new DisplayInfo();
    private final Rect mTmpInsetBounds = new Rect();
    private final Rect mTmpNormalBounds = new Rect();
    private final PinnedStackListenerForwarder.PinnedStackListener mPinnedStackListener = new PipManagerPinnedStackListener();
    private final Runnable mResizePinnedStackRunnable = new Runnable() { // from class: com.android.systemui.pip.tv.PipManager.1
        @Override // java.lang.Runnable
        public void run() {
            PipManager pipManager = PipManager.this;
            pipManager.resizePinnedStack(pipManager.mResumeResizePinnedStackRunnableState);
        }
    };
    private final Runnable mClosePipRunnable = new Runnable() { // from class: com.android.systemui.pip.tv.PipManager.2
        @Override // java.lang.Runnable
        public void run() {
            PipManager.this.closePip();
        }
    };

    public interface Listener {
        void onMoveToFullscreen();

        void onPipActivityClosed();

        void onPipEntered(String str);

        void onPipMenuActionsChanged(ParceledListSlice parceledListSlice);

        void onPipResizeAboutToStart();

        void onShowPipMenu();
    }

    public interface MediaListener {
        void onMediaControllerChanged();
    }

    @Override // com.android.systemui.pip.PipTaskOrganizer.PipTransitionCallback
    public void onPipTransitionStarted(ComponentName componentName, int i) {
    }

    /* JADX INFO: Access modifiers changed from: private */
    class PipManagerPinnedStackListener extends PinnedStackListenerForwarder.PinnedStackListener {
        private PipManagerPinnedStackListener() {
        }

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onImeVisibilityChanged(boolean z, int i) {
            if (PipManager.this.mState != 1 || PipManager.this.mImeVisible == z) {
                return;
            }
            if (z) {
                PipManager.this.mPipBounds.offset(0, -i);
                PipManager.this.mImeHeightAdjustment = i;
            } else {
                PipManager.this.mPipBounds.offset(0, PipManager.this.mImeHeightAdjustment);
            }
            PipManager.this.mImeVisible = z;
            PipManager.this.resizePinnedStack(1);
        }

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onMovementBoundsChanged(boolean z) {
            PipManager.this.mHandler.post(new Runnable() { // from class: com.android.systemui.pip.tv.PipManager$PipManagerPinnedStackListener$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onMovementBoundsChanged$0();
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onMovementBoundsChanged$0() {
            Rect rect = new Rect();
            PipManager.this.mPipBoundsHandler.onMovementBoundsChanged(PipManager.this.mTmpInsetBounds, PipManager.this.mTmpNormalBounds, rect, PipManager.this.mTmpDisplayInfo);
            PipManager.this.mDefaultPipBounds.set(rect);
        }

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onActionsChanged(ParceledListSlice parceledListSlice) {
            PipManager.this.mCustomActions = parceledListSlice;
            PipManager.this.mHandler.post(new Runnable() { // from class: com.android.systemui.pip.tv.PipManager$PipManagerPinnedStackListener$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onActionsChanged$1();
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onActionsChanged$1() {
            for (int size = PipManager.this.mListeners.size() - 1; size >= 0; size--) {
                ((Listener) PipManager.this.mListeners.get(size)).onPipMenuActionsChanged(PipManager.this.mCustomActions);
            }
        }
    }

    public PipManager(Context context, BroadcastDispatcher broadcastDispatcher, PipBoundsHandler pipBoundsHandler, PipTaskOrganizer pipTaskOrganizer, PipSurfaceTransactionHelper pipSurfaceTransactionHelper, Divider divider) throws Resources.NotFoundException {
        Pair<String, String> pairCreate;
        this.mLastOrientation = 0;
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.pip.tv.PipManager.3
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                if ("android.intent.action.MEDIA_RESOURCE_GRANTED".equals(intent.getAction())) {
                    String[] stringArrayExtra = intent.getStringArrayExtra("android.intent.extra.PACKAGES");
                    int intExtra = intent.getIntExtra("android.intent.extra.MEDIA_RESOURCE_TYPE", -1);
                    if (stringArrayExtra == null || stringArrayExtra.length <= 0 || intExtra != 0) {
                        return;
                    }
                    PipManager.this.handleMediaResourceGranted(stringArrayExtra);
                }
            }
        };
        this.mBroadcastReceiver = broadcastReceiver;
        this.mActiveMediaSessionListener = new MediaSessionManager.OnActiveSessionsChangedListener() { // from class: com.android.systemui.pip.tv.PipManager.4
            @Override // android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
            public void onActiveSessionsChanged(List<MediaController> list) {
                PipManager.this.updateMediaController(list);
            }
        };
        this.mTaskStackListener = new TaskStackChangeListener() { // from class: com.android.systemui.pip.tv.PipManager.5
            @Override // com.android.systemui.shared.system.TaskStackChangeListener
            public void onTaskStackChanged() {
                int[] iArr;
                if (PipManager.DEBUG) {
                    Log.d("PipManager", "onTaskStackChanged()");
                }
                if (PipManager.this.getState() != 0) {
                    ActivityManager.StackInfo pinnedStackInfo = PipManager.this.getPinnedStackInfo();
                    boolean z = false;
                    if (pinnedStackInfo == null || (iArr = pinnedStackInfo.taskIds) == null) {
                        Log.w("PipManager", "There is nothing in pinned stack");
                        PipManager.this.closePipInternal(false);
                        return;
                    }
                    int length = iArr.length - 1;
                    while (true) {
                        if (length < 0) {
                            break;
                        }
                        if (pinnedStackInfo.taskIds[length] == PipManager.this.mPipTaskId) {
                            z = true;
                            break;
                        }
                        length--;
                    }
                    if (!z) {
                        PipManager.this.closePipInternal(true);
                        return;
                    }
                }
                if (PipManager.this.getState() == 1) {
                    Rect rect = PipManager.this.isSettingsShown() ? PipManager.this.mSettingsPipBounds : PipManager.this.mDefaultPipBounds;
                    if (PipManager.this.mPipBounds != rect) {
                        PipManager.this.mPipBounds = rect;
                        PipManager.this.resizePinnedStack(1);
                    }
                }
            }

            @Override // com.android.systemui.shared.system.TaskStackChangeListener
            public void onActivityPinned(String str, int i, int i2, int i3) {
                boolean z = PipManager.DEBUG;
                if (z) {
                    Log.d("PipManager", "onActivityPinned()");
                }
                ActivityManager.StackInfo pinnedStackInfo = PipManager.this.getPinnedStackInfo();
                if (pinnedStackInfo == null) {
                    Log.w("PipManager", "Cannot find pinned stack");
                    return;
                }
                if (z) {
                    Log.d("PipManager", "PINNED_STACK:" + pinnedStackInfo);
                }
                PipManager.this.mPinnedStackId = pinnedStackInfo.stackId;
                PipManager pipManager = PipManager.this;
                int[] iArr = pinnedStackInfo.taskIds;
                pipManager.mPipTaskId = iArr[iArr.length - 1];
                PipManager pipManager2 = PipManager.this;
                String[] strArr = pinnedStackInfo.taskNames;
                pipManager2.mPipComponentName = ComponentName.unflattenFromString(strArr[strArr.length - 1]);
                PipManager.this.mState = 1;
                PipManager pipManager3 = PipManager.this;
                pipManager3.mCurrentPipBounds = pipManager3.mPipBounds;
                PipManager.this.mMediaSessionManager.addOnActiveSessionsChangedListener(PipManager.this.mActiveMediaSessionListener, null);
                PipManager pipManager4 = PipManager.this;
                pipManager4.updateMediaController(pipManager4.mMediaSessionManager.getActiveSessions(null));
                for (int size = PipManager.this.mListeners.size() - 1; size >= 0; size--) {
                    ((Listener) PipManager.this.mListeners.get(size)).onPipEntered(str);
                }
                PipManager.this.updatePipVisibility(true);
            }

            @Override // com.android.systemui.shared.system.TaskStackChangeListener
            public void onActivityRestartAttempt(ActivityManager.RunningTaskInfo runningTaskInfo, boolean z, boolean z2, boolean z3) {
                if (runningTaskInfo.configuration.windowConfiguration.getWindowingMode() != 2) {
                    return;
                }
                if (PipManager.DEBUG) {
                    Log.d("PipManager", "onPinnedActivityRestartAttempt()");
                }
                PipManager.this.movePipToFullscreen();
            }
        };
        if (this.mInitialized) {
            return;
        }
        this.mInitialized = true;
        this.mContext = context;
        this.mPipBoundsHandler = pipBoundsHandler;
        DisplayInfo displayInfo = new DisplayInfo();
        context.getDisplay().getDisplayInfo(displayInfo);
        this.mPipBoundsHandler.onDisplayInfoChanged(displayInfo);
        this.mResizeAnimationDuration = context.getResources().getInteger(R.integer.config_pipResizeAnimationDuration);
        this.mPipTaskOrganizer = pipTaskOrganizer;
        pipTaskOrganizer.registerPipTransitionCallback(this);
        this.mActivityTaskManager = ActivityTaskManager.getService();
        ActivityManagerWrapper.getInstance().registerTaskStackListener(this.mTaskStackListener);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.MEDIA_RESOURCE_GRANTED");
        broadcastDispatcher.registerReceiver(broadcastReceiver, intentFilter, null, UserHandle.ALL);
        if (sSettingsPackageAndClassNamePairList == null) {
            String[] stringArray = this.mContext.getResources().getStringArray(R.array.tv_pip_settings_class_name);
            sSettingsPackageAndClassNamePairList = new ArrayList();
            if (stringArray != null) {
                for (int i = 0; i < stringArray.length; i++) {
                    String[] strArrSplit = stringArray[i].split("/");
                    int length = strArrSplit.length;
                    if (length == 1) {
                        pairCreate = Pair.create(strArrSplit[0], null);
                    } else if (length == 2 && strArrSplit[1] != null) {
                        pairCreate = Pair.create(strArrSplit[0], strArrSplit[1].startsWith(".") ? strArrSplit[0] + strArrSplit[1] : strArrSplit[1]);
                    } else {
                        pairCreate = null;
                    }
                    if (pairCreate != null) {
                        sSettingsPackageAndClassNamePairList.add(pairCreate);
                    } else {
                        Log.w("PipManager", "Ignoring malformed settings name " + stringArray[i]);
                    }
                }
            }
        }
        Configuration configuration = this.mContext.getResources().getConfiguration();
        this.mLastOrientation = configuration.orientation;
        loadConfigurationsAndApply(configuration);
        this.mMediaSessionManager = (MediaSessionManager) this.mContext.getSystemService("media_session");
        try {
            WindowManagerWrapper.getInstance().addPinnedStackListener(this.mPinnedStackListener);
            this.mPipTaskOrganizer.registerOrganizer(2);
        } catch (RemoteException | UnsupportedOperationException e) {
            Log.e("PipManager", "Failed to register pinned stack listener", e);
        }
        this.mPipNotification = new PipNotification(context, broadcastDispatcher, this);
    }

    private void loadConfigurationsAndApply(Configuration configuration) {
        int i = this.mLastOrientation;
        int i2 = configuration.orientation;
        if (i != i2) {
            this.mLastOrientation = i2;
            return;
        }
        Resources resources = this.mContext.getResources();
        this.mSettingsPipBounds = Rect.unflattenFromString(resources.getString(R.string.pip_settings_bounds));
        this.mMenuModePipBounds = Rect.unflattenFromString(resources.getString(R.string.pip_menu_bounds));
        this.mPipBounds = isSettingsShown() ? this.mSettingsPipBounds : this.mDefaultPipBounds;
        resizePinnedStack(getPinnedStackInfo() == null ? 0 : 1);
    }

    @Override // com.android.systemui.pip.BasePipManager
    public void onConfigurationChanged(Configuration configuration) {
        loadConfigurationsAndApply(configuration);
        this.mPipNotification.onConfigurationChanged(this.mContext);
    }

    @Override // com.android.systemui.pip.BasePipManager
    public void showPictureInPictureMenu() {
        if (DEBUG) {
            Log.d("PipManager", "showPictureInPictureMenu(), current state=" + getStateDescription());
        }
        if (getState() == 1) {
            resizePinnedStack(2);
        }
    }

    public void closePip() {
        if (DEBUG) {
            Log.d("PipManager", "closePip(), current state=" + getStateDescription());
        }
        closePipInternal(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void closePipInternal(boolean z) {
        if (DEBUG) {
            Log.d("PipManager", "closePipInternal() removePipStack=" + z + ", current state=" + getStateDescription());
        }
        this.mState = 0;
        this.mPipTaskId = -1;
        this.mPipMediaController = null;
        this.mMediaSessionManager.removeOnActiveSessionsChangedListener(this.mActiveMediaSessionListener);
        try {
            if (z) {
                try {
                    this.mActivityTaskManager.removeStack(this.mPinnedStackId);
                } catch (RemoteException e) {
                    Log.e("PipManager", "removeStack failed", e);
                }
            }
            for (int size = this.mListeners.size() - 1; size >= 0; size--) {
                this.mListeners.get(size).onPipActivityClosed();
            }
            this.mHandler.removeCallbacks(this.mClosePipRunnable);
            updatePipVisibility(false);
        } finally {
            this.mPinnedStackId = -1;
        }
    }

    void movePipToFullscreen() {
        if (DEBUG) {
            Log.d("PipManager", "movePipToFullscreen(), current state=" + getStateDescription());
        }
        this.mPipTaskId = -1;
        for (int size = this.mListeners.size() - 1; size >= 0; size--) {
            this.mListeners.get(size).onMoveToFullscreen();
        }
        resizePinnedStack(0);
        updatePipVisibility(false);
    }

    public void suspendPipResizing(int i) {
        if (DEBUG) {
            Log.d("PipManager", "suspendPipResizing() reason=" + i + " callers=" + Debug.getCallers(2));
        }
        this.mSuspendPipResizingReason = i | this.mSuspendPipResizingReason;
    }

    public void resumePipResizing(int i) {
        if ((this.mSuspendPipResizingReason & i) == 0) {
            return;
        }
        if (DEBUG) {
            Log.d("PipManager", "resumePipResizing() reason=" + i + " callers=" + Debug.getCallers(2));
        }
        this.mSuspendPipResizingReason = (~i) & this.mSuspendPipResizingReason;
        this.mHandler.post(this.mResizePinnedStackRunnable);
    }

    void resizePinnedStack(int i) {
        if (DEBUG) {
            Log.d("PipManager", "resizePinnedStack() state=" + stateToName(i) + ", current state=" + getStateDescription(), new Exception());
        }
        boolean z = this.mState == 0;
        for (int size = this.mListeners.size() - 1; size >= 0; size--) {
            this.mListeners.get(size).onPipResizeAboutToStart();
        }
        if (this.mSuspendPipResizingReason != 0) {
            this.mResumeResizePinnedStackRunnableState = i;
            if (DEBUG) {
                Log.d("PipManager", "resizePinnedStack() deferring mSuspendPipResizingReason=" + this.mSuspendPipResizingReason + " mResumeResizePinnedStackRunnableState=" + stateToName(this.mResumeResizePinnedStackRunnableState));
                return;
            }
            return;
        }
        this.mState = i;
        if (i == 0) {
            this.mCurrentPipBounds = null;
            if (z) {
                return;
            }
        } else if (i == 2) {
            this.mCurrentPipBounds = this.mMenuModePipBounds;
        } else {
            this.mCurrentPipBounds = this.mPipBounds;
        }
        Rect rect = this.mCurrentPipBounds;
        if (rect != null) {
            this.mPipTaskOrganizer.scheduleAnimateResizePip(rect, this.mResizeAnimationDuration, null);
        } else {
            this.mPipTaskOrganizer.exitPip(this.mResizeAnimationDuration);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getState() {
        if (this.mSuspendPipResizingReason != 0) {
            return this.mResumeResizePinnedStackRunnableState;
        }
        return this.mState;
    }

    private void showPipMenu() {
        if (DEBUG) {
            Log.d("PipManager", "showPipMenu(), current state=" + getStateDescription());
        }
        this.mState = 2;
        for (int size = this.mListeners.size() - 1; size >= 0; size--) {
            this.mListeners.get(size).onShowPipMenu();
        }
        Intent intent = new Intent(this.mContext, (Class<?>) PipMenuActivity.class);
        intent.setFlags(268435456);
        intent.putExtra("custom_actions", (Parcelable) this.mCustomActions);
        this.mContext.startActivity(intent);
    }

    public void addListener(Listener listener) {
        this.mListeners.add(listener);
    }

    public void removeListener(Listener listener) {
        this.mListeners.remove(listener);
    }

    public void addMediaListener(MediaListener mediaListener) {
        this.mMediaListeners.add(mediaListener);
    }

    public void removeMediaListener(MediaListener mediaListener) {
        this.mMediaListeners.remove(mediaListener);
    }

    public boolean isPipShown() {
        return this.mState != 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public ActivityManager.StackInfo getPinnedStackInfo() {
        try {
            return ActivityTaskManager.getService().getStackInfo(2, 0);
        } catch (RemoteException e) {
            Log.e("PipManager", "getStackInfo failed", e);
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleMediaResourceGranted(String[] strArr) {
        if (getState() == 0) {
            this.mLastPackagesResourceGranted = strArr;
            return;
        }
        String[] strArr2 = this.mLastPackagesResourceGranted;
        boolean z = false;
        if (strArr2 != null) {
            boolean z2 = false;
            for (String str : strArr2) {
                int length = strArr.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    if (TextUtils.equals(strArr[i], str)) {
                        z2 = true;
                        break;
                    }
                    i++;
                }
            }
            z = z2;
        }
        this.mLastPackagesResourceGranted = strArr;
        if (z) {
            return;
        }
        closePip();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateMediaController(List<MediaController> list) {
        MediaController mediaController;
        if (list == null || getState() == 0 || this.mPipComponentName == null) {
            mediaController = null;
        } else {
            for (int size = list.size() - 1; size >= 0; size--) {
                mediaController = list.get(size);
                if (mediaController.getPackageName().equals(this.mPipComponentName.getPackageName())) {
                    break;
                }
            }
            mediaController = null;
        }
        if (this.mPipMediaController != mediaController) {
            this.mPipMediaController = mediaController;
            for (int size2 = this.mMediaListeners.size() - 1; size2 >= 0; size2--) {
                this.mMediaListeners.get(size2).onMediaControllerChanged();
            }
            if (this.mPipMediaController == null) {
                this.mHandler.postDelayed(this.mClosePipRunnable, 3000L);
            } else {
                this.mHandler.removeCallbacks(this.mClosePipRunnable);
            }
        }
    }

    MediaController getMediaController() {
        return this.mPipMediaController;
    }

    int getPlaybackState() {
        MediaController mediaController = this.mPipMediaController;
        if (mediaController != null && mediaController.getPlaybackState() != null) {
            int state = this.mPipMediaController.getPlaybackState().getState();
            boolean z = state == 6 || state == 8 || state == 3 || state == 4 || state == 5 || state == 9 || state == 10;
            long actions = this.mPipMediaController.getPlaybackState().getActions();
            if (!z && (4 & actions) != 0) {
                return 1;
            }
            if (z && (actions & 2) != 0) {
                return 0;
            }
        }
        return 2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isSettingsShown() {
        String str;
        try {
            List tasks = this.mActivityTaskManager.getTasks(1);
            if (tasks.isEmpty()) {
                return false;
            }
            ComponentName componentName = ((ActivityManager.RunningTaskInfo) tasks.get(0)).topActivity;
            for (Pair<String, String> pair : sSettingsPackageAndClassNamePairList) {
                if (componentName.getPackageName().equals((String) pair.first) && ((str = (String) pair.second) == null || componentName.getClassName().equals(str))) {
                    return true;
                }
            }
            return false;
        } catch (RemoteException e) {
            Log.d("PipManager", "Failed to detect top activity", e);
            return false;
        }
    }

    @Override // com.android.systemui.pip.PipTaskOrganizer.PipTransitionCallback
    public void onPipTransitionFinished(ComponentName componentName, int i) {
        onPipTransitionFinishedOrCanceled();
    }

    @Override // com.android.systemui.pip.PipTaskOrganizer.PipTransitionCallback
    public void onPipTransitionCanceled(ComponentName componentName, int i) {
        onPipTransitionFinishedOrCanceled();
    }

    private void onPipTransitionFinishedOrCanceled() {
        if (DEBUG) {
            Log.d("PipManager", "onPipTransitionFinishedOrCanceled()");
        }
        if (getState() == 2) {
            showPipMenu();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updatePipVisibility(final boolean z) {
        ((UiOffloadThread) Dependency.get(UiOffloadThread.class)).execute(new Runnable() { // from class: com.android.systemui.pip.tv.PipManager$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                PipManager.lambda$updatePipVisibility$0(z);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$updatePipVisibility$0(boolean z) {
        WindowManagerWrapper.getInstance().setPipVisibility(z);
    }

    private String getStateDescription() {
        if (this.mSuspendPipResizingReason == 0) {
            return stateToName(this.mState);
        }
        return stateToName(this.mResumeResizePinnedStackRunnableState) + " (while " + stateToName(this.mState) + " is suspended)";
    }

    private static String stateToName(int i) {
        if (i == 0) {
            return "NO_PIP";
        }
        if (i == 1) {
            return "PIP";
        }
        if (i == 2) {
            return "PIP_MENU";
        }
        return "UNKNOWN(" + i + ")";
    }
}
