package com.android.systemui.recents;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.Dependency;
import com.android.systemui.Prefs;
import com.android.systemui.R;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.shared.recents.IOverviewProxy;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.shared.system.TaskStackChangeListener;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lineageos.hardware.LineageHardwareManager;

@TargetApi(28)
/* loaded from: classes.dex */
public class RecentsOnboarding {
    private final View mArrowView;
    private Set<String> mBlacklistedPackages;
    private final Context mContext;
    private final ImageView mDismissView;
    private boolean mHasDismissedQuickScrubTip;
    private boolean mHasDismissedSwipeUpTip;
    private final View mLayout;
    private boolean mLayoutAttachedToWindow;
    private int mNavBarHeight;
    private int mNumAppsLaunchedSinceSwipeUpTipDismiss;
    private final View.OnAttachStateChangeListener mOnAttachStateChangeListener;
    private final int mOnboardingToastArrowRadius;
    private final int mOnboardingToastColor;
    private int mOverviewOpenedCountSinceQuickScrubTipDismiss;
    private boolean mOverviewProxyListenerRegistered;
    private final OverviewProxyService mOverviewProxyService;
    private final BroadcastReceiver mReceiver;
    private boolean mTaskListenerRegistered;
    private final TextView mTextView;
    private final WindowManager mWindowManager;
    private int mNavBarMode = 0;
    private final TaskStackChangeListener mTaskListener = new TaskStackChangeListener() { // from class: com.android.systemui.recents.RecentsOnboarding.1
        private String mLastPackageName;

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskCreated(int i, ComponentName componentName) throws Resources.NotFoundException {
            onAppLaunch();
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskMovedToFront(int i) throws Resources.NotFoundException {
            onAppLaunch();
        }

        private void onAppLaunch() throws Resources.NotFoundException {
            boolean zShow;
            boolean zShow2;
            ActivityManager.RunningTaskInfo runningTask = ActivityManagerWrapper.getInstance().getRunningTask();
            if (runningTask == null) {
                return;
            }
            if (RecentsOnboarding.this.mBlacklistedPackages.contains(runningTask.baseActivity.getPackageName())) {
                RecentsOnboarding.this.hide(true);
                return;
            }
            if (runningTask.baseActivity.getPackageName().equals(this.mLastPackageName)) {
                return;
            }
            this.mLastPackageName = runningTask.baseActivity.getPackageName();
            if (runningTask.configuration.windowConfiguration.getActivityType() == 1) {
                boolean zHasSeenSwipeUpOnboarding = RecentsOnboarding.this.hasSeenSwipeUpOnboarding();
                boolean zHasSeenQuickScrubOnboarding = RecentsOnboarding.this.hasSeenQuickScrubOnboarding();
                if (zHasSeenSwipeUpOnboarding && zHasSeenQuickScrubOnboarding) {
                    RecentsOnboarding.this.onDisconnectedFromLauncher();
                    return;
                }
                if (!zHasSeenSwipeUpOnboarding) {
                    if (RecentsOnboarding.this.getOpenedOverviewFromHomeCount() >= 3) {
                        if (RecentsOnboarding.this.mHasDismissedSwipeUpTip) {
                            int dismissedSwipeUpOnboardingCount = RecentsOnboarding.this.getDismissedSwipeUpOnboardingCount();
                            if (dismissedSwipeUpOnboardingCount > 2) {
                                return;
                            }
                            int i = dismissedSwipeUpOnboardingCount <= 1 ? 5 : 40;
                            RecentsOnboarding.access$608(RecentsOnboarding.this);
                            if (RecentsOnboarding.this.mNumAppsLaunchedSinceSwipeUpTipDismiss >= i) {
                                RecentsOnboarding.this.mNumAppsLaunchedSinceSwipeUpTipDismiss = 0;
                                zShow2 = RecentsOnboarding.this.show(R.string.recents_swipe_up_onboarding);
                            } else {
                                zShow2 = false;
                            }
                        } else {
                            zShow2 = RecentsOnboarding.this.show(R.string.recents_swipe_up_onboarding);
                        }
                        if (zShow2) {
                            RecentsOnboarding.this.notifyOnTip(0, 0);
                            return;
                        }
                        return;
                    }
                    return;
                }
                if (RecentsOnboarding.this.getOpenedOverviewCount() >= 10) {
                    if (RecentsOnboarding.this.mHasDismissedQuickScrubTip) {
                        if (RecentsOnboarding.this.mOverviewOpenedCountSinceQuickScrubTipDismiss >= 10) {
                            RecentsOnboarding.this.mOverviewOpenedCountSinceQuickScrubTipDismiss = 0;
                            zShow = RecentsOnboarding.this.show(R.string.recents_quick_scrub_onboarding);
                        } else {
                            zShow = false;
                        }
                    } else {
                        zShow = RecentsOnboarding.this.show(R.string.recents_quick_scrub_onboarding);
                    }
                    if (zShow) {
                        RecentsOnboarding.this.notifyOnTip(0, 1);
                        return;
                    }
                    return;
                }
                return;
            }
            RecentsOnboarding.this.hide(false);
        }
    };
    private OverviewProxyService.OverviewProxyListener mOverviewProxyListener = new OverviewProxyService.OverviewProxyListener() { // from class: com.android.systemui.recents.RecentsOnboarding.2
        @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyListener
        public void onOverviewShown(boolean z) {
            if (!RecentsOnboarding.this.hasSeenSwipeUpOnboarding() && !z) {
                RecentsOnboarding.this.setHasSeenSwipeUpOnboarding(true);
            }
            if (z) {
                RecentsOnboarding.this.incrementOpenedOverviewFromHomeCount();
            }
            RecentsOnboarding.this.incrementOpenedOverviewCount();
            if (RecentsOnboarding.this.getOpenedOverviewCount() < 10 || !RecentsOnboarding.this.mHasDismissedQuickScrubTip) {
                return;
            }
            RecentsOnboarding.access$1008(RecentsOnboarding.this);
        }
    };

    static /* synthetic */ int access$1008(RecentsOnboarding recentsOnboarding) {
        int i = recentsOnboarding.mOverviewOpenedCountSinceQuickScrubTipDismiss;
        recentsOnboarding.mOverviewOpenedCountSinceQuickScrubTipDismiss = i + 1;
        return i;
    }

    static /* synthetic */ int access$608(RecentsOnboarding recentsOnboarding) {
        int i = recentsOnboarding.mNumAppsLaunchedSinceSwipeUpTipDismiss;
        recentsOnboarding.mNumAppsLaunchedSinceSwipeUpTipDismiss = i + 1;
        return i;
    }

    public RecentsOnboarding(Context context, OverviewProxyService overviewProxyService) throws Resources.NotFoundException {
        View.OnAttachStateChangeListener onAttachStateChangeListener = new View.OnAttachStateChangeListener() { // from class: com.android.systemui.recents.RecentsOnboarding.3
            private final BroadcastDispatcher mBroadcastDispatcher = (BroadcastDispatcher) Dependency.get(BroadcastDispatcher.class);

            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewAttachedToWindow(View view) {
                if (view == RecentsOnboarding.this.mLayout) {
                    this.mBroadcastDispatcher.registerReceiver(RecentsOnboarding.this.mReceiver, new IntentFilter("android.intent.action.SCREEN_OFF"));
                    RecentsOnboarding.this.mLayoutAttachedToWindow = true;
                    if (view.getTag().equals(Integer.valueOf(R.string.recents_swipe_up_onboarding))) {
                        RecentsOnboarding.this.mHasDismissedSwipeUpTip = false;
                    } else {
                        RecentsOnboarding.this.mHasDismissedQuickScrubTip = false;
                    }
                }
            }

            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewDetachedFromWindow(View view) {
                if (view == RecentsOnboarding.this.mLayout) {
                    RecentsOnboarding.this.mLayoutAttachedToWindow = false;
                    if (view.getTag().equals(Integer.valueOf(R.string.recents_quick_scrub_onboarding))) {
                        RecentsOnboarding.this.mHasDismissedQuickScrubTip = true;
                        if (RecentsOnboarding.this.hasDismissedQuickScrubOnboardingOnce()) {
                            RecentsOnboarding.this.setHasSeenQuickScrubOnboarding(true);
                        } else {
                            RecentsOnboarding.this.setHasDismissedQuickScrubOnboardingOnce(true);
                        }
                        RecentsOnboarding.this.mOverviewOpenedCountSinceQuickScrubTipDismiss = 0;
                    }
                    this.mBroadcastDispatcher.unregisterReceiver(RecentsOnboarding.this.mReceiver);
                }
            }
        };
        this.mOnAttachStateChangeListener = onAttachStateChangeListener;
        this.mReceiver = new BroadcastReceiver() { // from class: com.android.systemui.recents.RecentsOnboarding.4
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                    RecentsOnboarding.this.hide(false);
                }
            }
        };
        this.mContext = context;
        this.mOverviewProxyService = overviewProxyService;
        Resources resources = context.getResources();
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        HashSet hashSet = new HashSet();
        this.mBlacklistedPackages = hashSet;
        Collections.addAll(hashSet, resources.getStringArray(R.array.recents_onboarding_blacklisted_packages));
        View viewInflate = LayoutInflater.from(context).inflate(R.layout.recents_onboarding, (ViewGroup) null);
        this.mLayout = viewInflate;
        this.mTextView = (TextView) viewInflate.findViewById(R.id.onboarding_text);
        ImageView imageView = (ImageView) viewInflate.findViewById(R.id.dismiss);
        this.mDismissView = imageView;
        View viewFindViewById = viewInflate.findViewById(R.id.arrow);
        this.mArrowView = viewFindViewById;
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorAccent, typedValue, true);
        int color = resources.getColor(typedValue.resourceId);
        this.mOnboardingToastColor = color;
        int dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.recents_onboarding_toast_arrow_corner_radius);
        this.mOnboardingToastArrowRadius = dimensionPixelSize;
        viewInflate.addOnAttachStateChangeListener(onAttachStateChangeListener);
        imageView.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.recents.RecentsOnboarding$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$new$0(view);
            }
        });
        ViewGroup.LayoutParams layoutParams = viewFindViewById.getLayoutParams();
        ShapeDrawable shapeDrawable = new ShapeDrawable(TriangleShape.create(layoutParams.width, layoutParams.height, false));
        Paint paint = shapeDrawable.getPaint();
        paint.setColor(color);
        paint.setPathEffect(new CornerPathEffect(dimensionPixelSize));
        viewFindViewById.setBackground(shapeDrawable);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0(View view) {
        hide(true);
        if (view.getTag().equals(Integer.valueOf(R.string.recents_swipe_up_onboarding))) {
            this.mHasDismissedSwipeUpTip = true;
            this.mNumAppsLaunchedSinceSwipeUpTipDismiss = 0;
            setDismissedSwipeUpOnboardingCount(getDismissedSwipeUpOnboardingCount() + 1);
            if (getDismissedSwipeUpOnboardingCount() > 2) {
                setHasSeenSwipeUpOnboarding(true);
            }
            notifyOnTip(1, 0);
            return;
        }
        notifyOnTip(1, 1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyOnTip(int i, int i2) {
        try {
            IOverviewProxy proxy = this.mOverviewProxyService.getProxy();
            if (proxy != null) {
                proxy.onTip(i, i2);
            }
        } catch (RemoteException unused) {
        }
    }

    public void onNavigationModeChanged(int i) {
        this.mNavBarMode = i;
    }

    public void onConnectedToLauncher() {
        if (QuickStepContract.isGesturalMode(this.mNavBarMode)) {
            return;
        }
        if (hasSeenSwipeUpOnboarding() && hasSeenQuickScrubOnboarding()) {
            return;
        }
        if (!this.mOverviewProxyListenerRegistered) {
            this.mOverviewProxyService.addCallback(this.mOverviewProxyListener);
            this.mOverviewProxyListenerRegistered = true;
        }
        if (this.mTaskListenerRegistered) {
            return;
        }
        ActivityManagerWrapper.getInstance().registerTaskStackListener(this.mTaskListener);
        this.mTaskListenerRegistered = true;
    }

    public void onDisconnectedFromLauncher() {
        if (this.mOverviewProxyListenerRegistered) {
            this.mOverviewProxyService.removeCallback(this.mOverviewProxyListener);
            this.mOverviewProxyListenerRegistered = false;
        }
        if (this.mTaskListenerRegistered) {
            ActivityManagerWrapper.getInstance().unregisterTaskStackListener(this.mTaskListener);
            this.mTaskListenerRegistered = false;
        }
        this.mHasDismissedSwipeUpTip = false;
        this.mHasDismissedQuickScrubTip = false;
        this.mNumAppsLaunchedSinceSwipeUpTipDismiss = 0;
        this.mOverviewOpenedCountSinceQuickScrubTipDismiss = 0;
        hide(true);
    }

    public void onConfigurationChanged(Configuration configuration) {
        if (configuration.orientation != 1) {
            hide(false);
        }
    }

    public boolean show(int i) throws Resources.NotFoundException {
        int i2;
        int dimensionPixelSize = 0;
        if (!shouldShow()) {
            return false;
        }
        this.mDismissView.setTag(Integer.valueOf(i));
        this.mLayout.setTag(Integer.valueOf(i));
        this.mTextView.setText(i);
        int i3 = this.mContext.getResources().getConfiguration().orientation;
        if (this.mLayoutAttachedToWindow || i3 != 1) {
            return false;
        }
        this.mLayout.setSystemUiVisibility(LineageHardwareManager.FEATURE_SUNLIGHT_ENHANCEMENT);
        if (i == R.string.recents_swipe_up_onboarding) {
            i2 = 81;
        } else {
            i2 = (this.mContext.getResources().getConfiguration().getLayoutDirection() == 0 ? 3 : 5) | 80;
            dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.recents_quick_scrub_onboarding_margin_start);
        }
        this.mWindowManager.addView(this.mLayout, getWindowLayoutParams(i2, dimensionPixelSize));
        this.mLayout.setAlpha(0.0f);
        this.mLayout.animate().alpha(1.0f).withLayer().setStartDelay(500L).setDuration(300L).setInterpolator(new DecelerateInterpolator()).start();
        return true;
    }

    private boolean shouldShow() {
        return SystemProperties.getBoolean("persist.quickstep.onboarding.enabled", (((UserManager) this.mContext.getSystemService(UserManager.class)).isDemoUser() || ActivityManager.isRunningInTestHarness()) ? false : true);
    }

    public void hide(boolean z) {
        if (this.mLayoutAttachedToWindow) {
            if (z) {
                this.mLayout.animate().alpha(0.0f).withLayer().setStartDelay(0L).setDuration(100L).setInterpolator(new AccelerateInterpolator()).withEndAction(new Runnable() { // from class: com.android.systemui.recents.RecentsOnboarding$$ExternalSyntheticLambda1
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$hide$1();
                    }
                }).start();
            } else {
                this.mLayout.animate().cancel();
                this.mWindowManager.removeViewImmediate(this.mLayout);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$hide$1() {
        this.mWindowManager.removeViewImmediate(this.mLayout);
    }

    public void setNavBarHeight(int i) {
        this.mNavBarHeight = i;
    }

    public void dump(PrintWriter printWriter) {
        printWriter.println("RecentsOnboarding {");
        printWriter.println("      mTaskListenerRegistered: " + this.mTaskListenerRegistered);
        printWriter.println("      mOverviewProxyListenerRegistered: " + this.mOverviewProxyListenerRegistered);
        printWriter.println("      mLayoutAttachedToWindow: " + this.mLayoutAttachedToWindow);
        printWriter.println("      mHasDismissedSwipeUpTip: " + this.mHasDismissedSwipeUpTip);
        printWriter.println("      mHasDismissedQuickScrubTip: " + this.mHasDismissedQuickScrubTip);
        printWriter.println("      mNumAppsLaunchedSinceSwipeUpTipDismiss: " + this.mNumAppsLaunchedSinceSwipeUpTipDismiss);
        printWriter.println("      hasSeenSwipeUpOnboarding: " + hasSeenSwipeUpOnboarding());
        printWriter.println("      hasSeenQuickScrubOnboarding: " + hasSeenQuickScrubOnboarding());
        printWriter.println("      getDismissedSwipeUpOnboardingCount: " + getDismissedSwipeUpOnboardingCount());
        printWriter.println("      hasDismissedQuickScrubOnboardingOnce: " + hasDismissedQuickScrubOnboardingOnce());
        printWriter.println("      getOpenedOverviewCount: " + getOpenedOverviewCount());
        printWriter.println("      getOpenedOverviewFromHomeCount: " + getOpenedOverviewFromHomeCount());
        printWriter.println("    }");
    }

    private WindowManager.LayoutParams getWindowLayoutParams(int i, int i2) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-2, -2, i2, (-this.mNavBarHeight) / 2, 2038, 520, -3);
        layoutParams.privateFlags |= 16;
        layoutParams.setTitle("RecentsOnboarding");
        layoutParams.gravity = i;
        return layoutParams;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean hasSeenSwipeUpOnboarding() {
        return Prefs.getBoolean(this.mContext, "HasSeenRecentsSwipeUpOnboarding", false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setHasSeenSwipeUpOnboarding(boolean z) {
        Prefs.putBoolean(this.mContext, "HasSeenRecentsSwipeUpOnboarding", z);
        if (z && hasSeenQuickScrubOnboarding()) {
            onDisconnectedFromLauncher();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean hasSeenQuickScrubOnboarding() {
        return Prefs.getBoolean(this.mContext, "HasSeenRecentsQuickScrubOnboarding", false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setHasSeenQuickScrubOnboarding(boolean z) {
        Prefs.putBoolean(this.mContext, "HasSeenRecentsQuickScrubOnboarding", z);
        if (z && hasSeenSwipeUpOnboarding()) {
            onDisconnectedFromLauncher();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getDismissedSwipeUpOnboardingCount() {
        return Prefs.getInt(this.mContext, "DismissedRecentsSwipeUpOnboardingCount", 0);
    }

    private void setDismissedSwipeUpOnboardingCount(int i) {
        Prefs.putInt(this.mContext, "DismissedRecentsSwipeUpOnboardingCount", i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean hasDismissedQuickScrubOnboardingOnce() {
        return Prefs.getBoolean(this.mContext, "HasDismissedRecentsQuickScrubOnboardingOnce", false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setHasDismissedQuickScrubOnboardingOnce(boolean z) {
        Prefs.putBoolean(this.mContext, "HasDismissedRecentsQuickScrubOnboardingOnce", z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getOpenedOverviewFromHomeCount() {
        return Prefs.getInt(this.mContext, "OverviewOpenedFromHomeCount", 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void incrementOpenedOverviewFromHomeCount() {
        int openedOverviewFromHomeCount = getOpenedOverviewFromHomeCount();
        if (openedOverviewFromHomeCount >= 3) {
            return;
        }
        setOpenedOverviewFromHomeCount(openedOverviewFromHomeCount + 1);
    }

    private void setOpenedOverviewFromHomeCount(int i) {
        Prefs.putInt(this.mContext, "OverviewOpenedFromHomeCount", i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getOpenedOverviewCount() {
        return Prefs.getInt(this.mContext, "OverviewOpenedCount", 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void incrementOpenedOverviewCount() {
        int openedOverviewCount = getOpenedOverviewCount();
        if (openedOverviewCount >= 10) {
            return;
        }
        setOpenedOverviewCount(openedOverviewCount + 1);
    }

    private void setOpenedOverviewCount(int i) {
        Prefs.putInt(this.mContext, "OverviewOpenedCount", i);
    }
}
