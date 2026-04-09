package com.android.systemui.recents;

import android.app.ActivityManager;
import android.app.trust.TrustManager;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.shared.recents.IOverviewProxy;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.phone.StatusBar;
import dagger.Lazy;
import java.util.Optional;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public class OverviewProxyRecentsImpl implements RecentsImplementation {
    private Context mContext;
    private final Optional<Divider> mDividerOptional;
    private Handler mHandler;
    private OverviewProxyService mOverviewProxyService;
    private final Lazy<StatusBar> mStatusBarLazy;
    private TrustManager mTrustManager;

    public OverviewProxyRecentsImpl(Optional<Lazy<StatusBar>> optional, Optional<Divider> optional2) {
        this.mStatusBarLazy = optional.orElse(null);
        this.mDividerOptional = optional2;
    }

    @Override // com.android.systemui.recents.RecentsImplementation
    public void onStart(Context context) {
        this.mContext = context;
        this.mHandler = new Handler();
        this.mTrustManager = (TrustManager) context.getSystemService("trust");
        this.mOverviewProxyService = (OverviewProxyService) Dependency.get(OverviewProxyService.class);
    }

    @Override // com.android.systemui.recents.RecentsImplementation
    public void showRecentApps(boolean z) {
        IOverviewProxy proxy = this.mOverviewProxyService.getProxy();
        if (proxy != null) {
            try {
                proxy.onOverviewShown(z);
            } catch (RemoteException e) {
                Log.e("OverviewProxyRecentsImpl", "Failed to send overview show event to launcher.", e);
            }
        }
    }

    @Override // com.android.systemui.recents.RecentsImplementation
    public void hideRecentApps(boolean z, boolean z2) {
        IOverviewProxy proxy = this.mOverviewProxyService.getProxy();
        if (proxy != null) {
            try {
                proxy.onOverviewHidden(z, z2);
            } catch (RemoteException e) {
                Log.e("OverviewProxyRecentsImpl", "Failed to send overview hide event to launcher.", e);
            }
        }
    }

    @Override // com.android.systemui.recents.RecentsImplementation
    public void toggleRecentApps() {
        if (this.mOverviewProxyService.getProxy() != null) {
            final Runnable runnable = new Runnable() { // from class: com.android.systemui.recents.OverviewProxyRecentsImpl$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$toggleRecentApps$0();
                }
            };
            Lazy<StatusBar> lazy = this.mStatusBarLazy;
            if (lazy != null && lazy.get().isKeyguardShowing()) {
                this.mStatusBarLazy.get().executeRunnableDismissingKeyguard(new Runnable() { // from class: com.android.systemui.recents.OverviewProxyRecentsImpl$$ExternalSyntheticLambda1
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$toggleRecentApps$1(runnable);
                    }
                }, null, true, false, true);
            } else {
                runnable.run();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$toggleRecentApps$0() {
        try {
            if (this.mOverviewProxyService.getProxy() != null) {
                this.mOverviewProxyService.getProxy().onOverviewToggle();
                this.mOverviewProxyService.notifyToggleRecentApps();
            }
        } catch (RemoteException e) {
            Log.e("OverviewProxyRecentsImpl", "Cannot send toggle recents through proxy service.", e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$toggleRecentApps$1(Runnable runnable) {
        this.mTrustManager.reportKeyguardShowingChanged();
        this.mHandler.post(runnable);
    }

    @Override // com.android.systemui.recents.RecentsImplementation
    public boolean splitPrimaryTask(int i, Rect rect, int i2) {
        Point point = new Point();
        if (rect == null) {
            ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).getDisplay(0).getRealSize(point);
            rect = new Rect(0, 0, point.x, point.y);
        }
        ActivityManager.RunningTaskInfo runningTask = ActivityManagerWrapper.getInstance().getRunningTask();
        int activityType = runningTask != null ? runningTask.configuration.windowConfiguration.getActivityType() : 0;
        boolean zIsScreenPinningActive = ActivityManagerWrapper.getInstance().isScreenPinningActive();
        boolean z = activityType == 2 || activityType == 3;
        if (runningTask != null && !z && !zIsScreenPinningActive) {
            if (runningTask.supportsSplitScreenMultiWindow) {
                if (ActivityManagerWrapper.getInstance().setTaskWindowingModeSplitScreenPrimary(runningTask.id, i, rect)) {
                    this.mDividerOptional.ifPresent(new Consumer() { // from class: com.android.systemui.recents.OverviewProxyRecentsImpl$$ExternalSyntheticLambda2
                        @Override // java.util.function.Consumer
                        public final void accept(Object obj) {
                            ((Divider) obj).onDockedTopTask();
                        }
                    });
                    this.mDividerOptional.ifPresent(new Consumer() { // from class: com.android.systemui.recents.OverviewProxyRecentsImpl$$ExternalSyntheticLambda3
                        @Override // java.util.function.Consumer
                        public final void accept(Object obj) {
                            ((Divider) obj).onRecentsDrawn();
                        }
                    });
                    return true;
                }
            } else {
                Toast.makeText(this.mContext, R.string.dock_non_resizeble_failed_to_dock_text, 0).show();
            }
        }
        return false;
    }
}
