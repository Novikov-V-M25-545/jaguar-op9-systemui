package com.android.systemui.screenshot;

import android.app.ActivityManager;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.screenshot.ScreenshotNotificationSmartActionsProvider;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/* loaded from: classes.dex */
public class ScreenshotSmartActions {
    @VisibleForTesting
    CompletableFuture<List<Notification.Action>> getSmartActionsFuture(String str, Uri uri, Bitmap bitmap, ScreenshotNotificationSmartActionsProvider screenshotNotificationSmartActionsProvider, boolean z, UserHandle userHandle) {
        ComponentName componentName;
        if (!z) {
            Slog.i("ScreenshotSmartActions", "Screenshot Intelligence not enabled, returning empty list.");
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        if (bitmap.getConfig() != Bitmap.Config.HARDWARE) {
            Slog.w("ScreenshotSmartActions", String.format("Bitmap expected: Hardware, Bitmap found: %s. Returning empty list.", bitmap.getConfig()));
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        Slog.d("ScreenshotSmartActions", "Screenshot from user profile: " + userHandle.getIdentifier());
        long jUptimeMillis = SystemClock.uptimeMillis();
        try {
            ActivityManager.RunningTaskInfo runningTask = ActivityManagerWrapper.getInstance().getRunningTask();
            if (runningTask == null || (componentName = runningTask.topActivity) == null) {
                componentName = new ComponentName("", "");
            }
            return screenshotNotificationSmartActionsProvider.getActions(str, uri, bitmap, componentName, userHandle);
        } catch (Throwable th) {
            long jUptimeMillis2 = SystemClock.uptimeMillis() - jUptimeMillis;
            CompletableFuture<List<Notification.Action>> completableFutureCompletedFuture = CompletableFuture.completedFuture(Collections.emptyList());
            Slog.e("ScreenshotSmartActions", "Failed to get future for screenshot notification smart actions.", th);
            notifyScreenshotOp(str, screenshotNotificationSmartActionsProvider, ScreenshotNotificationSmartActionsProvider.ScreenshotOp.REQUEST_SMART_ACTIONS, ScreenshotNotificationSmartActionsProvider.ScreenshotOpStatus.ERROR, jUptimeMillis2);
            return completableFutureCompletedFuture;
        }
    }

    @VisibleForTesting
    List<Notification.Action> getSmartActions(String str, CompletableFuture<List<Notification.Action>> completableFuture, int i, ScreenshotNotificationSmartActionsProvider screenshotNotificationSmartActionsProvider) {
        ScreenshotNotificationSmartActionsProvider.ScreenshotOpStatus screenshotOpStatus;
        long jUptimeMillis = SystemClock.uptimeMillis();
        try {
            List<Notification.Action> list = completableFuture.get(i, TimeUnit.MILLISECONDS);
            long jUptimeMillis2 = SystemClock.uptimeMillis() - jUptimeMillis;
            Slog.d("ScreenshotSmartActions", String.format("Got %d smart actions. Wait time: %d ms", Integer.valueOf(list.size()), Long.valueOf(jUptimeMillis2)));
            notifyScreenshotOp(str, screenshotNotificationSmartActionsProvider, ScreenshotNotificationSmartActionsProvider.ScreenshotOp.WAIT_FOR_SMART_ACTIONS, ScreenshotNotificationSmartActionsProvider.ScreenshotOpStatus.SUCCESS, jUptimeMillis2);
            return list;
        } catch (Throwable th) {
            long jUptimeMillis3 = SystemClock.uptimeMillis() - jUptimeMillis;
            Slog.e("ScreenshotSmartActions", String.format("Error getting smart actions. Wait time: %d ms", Long.valueOf(jUptimeMillis3)), th);
            if (th instanceof TimeoutException) {
                screenshotOpStatus = ScreenshotNotificationSmartActionsProvider.ScreenshotOpStatus.TIMEOUT;
            } else {
                screenshotOpStatus = ScreenshotNotificationSmartActionsProvider.ScreenshotOpStatus.ERROR;
            }
            notifyScreenshotOp(str, screenshotNotificationSmartActionsProvider, ScreenshotNotificationSmartActionsProvider.ScreenshotOp.WAIT_FOR_SMART_ACTIONS, screenshotOpStatus, jUptimeMillis3);
            return Collections.emptyList();
        }
    }

    void notifyScreenshotOp(String str, ScreenshotNotificationSmartActionsProvider screenshotNotificationSmartActionsProvider, ScreenshotNotificationSmartActionsProvider.ScreenshotOp screenshotOp, ScreenshotNotificationSmartActionsProvider.ScreenshotOpStatus screenshotOpStatus, long j) {
        try {
            screenshotNotificationSmartActionsProvider.notifyOp(str, screenshotOp, screenshotOpStatus, j);
        } catch (Throwable th) {
            Slog.e("ScreenshotSmartActions", "Error in notifyScreenshotOp: ", th);
        }
    }

    void notifyScreenshotAction(Context context, String str, String str2, boolean z) {
        try {
            SystemUIFactory.getInstance().createScreenshotNotificationSmartActionsProvider(context, AsyncTask.THREAD_POOL_EXECUTOR, new Handler()).notifyAction(str, str2, z);
            ScreenshotNotificationsController.cancelScreenshotNotification(context);
        } catch (Throwable th) {
            Slog.e("ScreenshotSmartActions", "Error in notifyScreenshotAction: ", th);
        }
    }
}
