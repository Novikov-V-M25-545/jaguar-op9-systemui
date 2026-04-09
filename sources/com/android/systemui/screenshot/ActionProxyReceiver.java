package com.android.systemui.screenshot;

import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.statusbar.phone.StatusBar;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/* loaded from: classes.dex */
public class ActionProxyReceiver extends BroadcastReceiver {
    private final ActivityManagerWrapper mActivityManagerWrapper;
    private final ScreenshotSmartActions mScreenshotSmartActions;
    private final StatusBar mStatusBar;

    public ActionProxyReceiver(Optional<StatusBar> optional, ActivityManagerWrapper activityManagerWrapper, ScreenshotSmartActions screenshotSmartActions) {
        this.mStatusBar = optional.orElse(null);
        this.mActivityManagerWrapper = activityManagerWrapper;
        this.mScreenshotSmartActions = screenshotSmartActions;
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(final Context context, final Intent intent) {
        Runnable runnable = new Runnable() { // from class: com.android.systemui.screenshot.ActionProxyReceiver$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() throws ExecutionException, InterruptedException, TimeoutException, PendingIntent.CanceledException {
                this.f$0.lambda$onReceive$0(intent, context);
            }
        };
        StatusBar statusBar = this.mStatusBar;
        if (statusBar != null) {
            statusBar.executeRunnableDismissingKeyguard(runnable, null, true, true, true);
        } else {
            runnable.run();
        }
        if (intent.getBooleanExtra("android:smart_actions_enabled", false)) {
            this.mScreenshotSmartActions.notifyScreenshotAction(context, intent.getStringExtra("android:screenshot_id"), "android.intent.action.EDIT".equals(intent.getAction()) ? "Edit" : "Share", false);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onReceive$0(Intent intent, Context context) throws ExecutionException, InterruptedException, TimeoutException, PendingIntent.CanceledException {
        try {
            this.mActivityManagerWrapper.closeSystemWindows("screenshot").get(3000L, TimeUnit.MILLISECONDS);
            PendingIntent pendingIntent = (PendingIntent) intent.getParcelableExtra("android:screenshot_action_intent");
            ActivityOptions activityOptionsMakeBasic = ActivityOptions.makeBasic();
            activityOptionsMakeBasic.setDisallowEnterPictureInPictureWhileLaunching(intent.getBooleanExtra("android:screenshot_disallow_enter_pip", false));
            try {
                pendingIntent.send(context, 0, null, null, null, null, activityOptionsMakeBasic.toBundle());
            } catch (PendingIntent.CanceledException e) {
                Log.e("ActionProxyReceiver", "Pending intent canceled", e);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e2) {
            Log.e("ActionProxyReceiver", "Unable to share screenshot", e2);
        }
    }
}
