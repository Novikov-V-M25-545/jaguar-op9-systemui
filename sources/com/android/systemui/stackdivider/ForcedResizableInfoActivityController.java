package com.android.systemui.stackdivider;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.UserHandle;
import android.util.ArraySet;
import android.widget.Toast;
import com.android.systemui.R;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.TaskStackChangeListener;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public class ForcedResizableInfoActivityController {
    private final Context mContext;
    private boolean mDividerDragging;
    private final Consumer<Boolean> mDockedStackExistsListener;
    private final Handler mHandler = new Handler();
    private final ArraySet<PendingTaskRecord> mPendingTasks = new ArraySet<>();
    private final ArraySet<String> mPackagesShownInSession = new ArraySet<>();
    private final Runnable mTimeoutRunnable = new Runnable() { // from class: com.android.systemui.stackdivider.ForcedResizableInfoActivityController.1
        @Override // java.lang.Runnable
        public void run() {
            ForcedResizableInfoActivityController.this.showPending();
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0(Boolean bool) {
        if (bool.booleanValue()) {
            return;
        }
        this.mPackagesShownInSession.clear();
    }

    private class PendingTaskRecord {
        int reason;
        int taskId;

        PendingTaskRecord(int i, int i2) {
            this.taskId = i;
            this.reason = i2;
        }
    }

    public ForcedResizableInfoActivityController(Context context, Divider divider) {
        Consumer<Boolean> consumer = new Consumer() { // from class: com.android.systemui.stackdivider.ForcedResizableInfoActivityController$$ExternalSyntheticLambda0
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.lambda$new$0((Boolean) obj);
            }
        };
        this.mDockedStackExistsListener = consumer;
        this.mContext = context;
        ActivityManagerWrapper.getInstance().registerTaskStackListener(new TaskStackChangeListener() { // from class: com.android.systemui.stackdivider.ForcedResizableInfoActivityController.2
            @Override // com.android.systemui.shared.system.TaskStackChangeListener
            public void onActivityForcedResizable(String str, int i, int i2) {
                ForcedResizableInfoActivityController.this.activityForcedResizable(str, i, i2);
            }

            @Override // com.android.systemui.shared.system.TaskStackChangeListener
            public void onActivityDismissingDockedStack() {
                ForcedResizableInfoActivityController.this.activityDismissingDockedStack();
            }

            @Override // com.android.systemui.shared.system.TaskStackChangeListener
            public void onActivityLaunchOnSecondaryDisplayFailed() {
                ForcedResizableInfoActivityController.this.activityLaunchOnSecondaryDisplayFailed();
            }
        });
        divider.registerInSplitScreenListener(consumer);
    }

    public void onAppTransitionFinished() {
        if (this.mDividerDragging) {
            return;
        }
        showPending();
    }

    void onDraggingStart() {
        this.mDividerDragging = true;
        this.mHandler.removeCallbacks(this.mTimeoutRunnable);
    }

    void onDraggingEnd() {
        this.mDividerDragging = false;
        showPending();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void activityForcedResizable(String str, int i, int i2) {
        if (debounce(str)) {
            return;
        }
        this.mPendingTasks.add(new PendingTaskRecord(i, i2));
        postTimeout();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void activityDismissingDockedStack() {
        Toast.makeText(this.mContext, R.string.dock_non_resizeble_failed_to_dock_text, 0).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void activityLaunchOnSecondaryDisplayFailed() {
        Toast.makeText(this.mContext, R.string.activity_launch_on_secondary_display_failed_text, 0).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showPending() {
        this.mHandler.removeCallbacks(this.mTimeoutRunnable);
        for (int size = this.mPendingTasks.size() - 1; size >= 0; size--) {
            PendingTaskRecord pendingTaskRecordValueAt = this.mPendingTasks.valueAt(size);
            Intent intent = new Intent(this.mContext, (Class<?>) ForcedResizableInfoActivity.class);
            ActivityOptions activityOptionsMakeBasic = ActivityOptions.makeBasic();
            activityOptionsMakeBasic.setLaunchTaskId(pendingTaskRecordValueAt.taskId);
            activityOptionsMakeBasic.setTaskOverlay(true, true);
            intent.putExtra("extra_forced_resizeable_reason", pendingTaskRecordValueAt.reason);
            this.mContext.startActivityAsUser(intent, activityOptionsMakeBasic.toBundle(), UserHandle.CURRENT);
        }
        this.mPendingTasks.clear();
    }

    private void postTimeout() {
        this.mHandler.removeCallbacks(this.mTimeoutRunnable);
        this.mHandler.postDelayed(this.mTimeoutRunnable, 1000L);
    }

    private boolean debounce(String str) {
        if (str == null) {
            return false;
        }
        if ("com.android.systemui".equals(str)) {
            return true;
        }
        boolean zContains = this.mPackagesShownInSession.contains(str);
        this.mPackagesShownInSession.add(str);
        return zContains;
    }
}
