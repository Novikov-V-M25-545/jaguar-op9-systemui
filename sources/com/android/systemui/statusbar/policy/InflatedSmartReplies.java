package com.android.systemui.statusbar.policy;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.util.Pair;
import android.widget.Button;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.systemui.Dependency;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.DevicePolicyManagerWrapper;
import com.android.systemui.shared.system.PackageManagerWrapper;
import com.android.systemui.statusbar.NotificationUiAdjustment;
import com.android.systemui.statusbar.SmartReplyController;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.policy.SmartReplyView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/* loaded from: classes.dex */
public class InflatedSmartReplies {
    private static final boolean DEBUG = Log.isLoggable("InflatedSmartReplies", 3);
    private final SmartRepliesAndActions mSmartRepliesAndActions;
    private final SmartReplyView mSmartReplyView;
    private final List<Button> mSmartSuggestionButtons;

    private InflatedSmartReplies(SmartReplyView smartReplyView, List<Button> list, SmartRepliesAndActions smartRepliesAndActions) {
        this.mSmartReplyView = smartReplyView;
        this.mSmartSuggestionButtons = list;
        this.mSmartRepliesAndActions = smartRepliesAndActions;
    }

    public SmartReplyView getSmartReplyView() {
        return this.mSmartReplyView;
    }

    public List<Button> getSmartSuggestionButtons() {
        return this.mSmartSuggestionButtons;
    }

    public SmartRepliesAndActions getSmartRepliesAndActions() {
        return this.mSmartRepliesAndActions;
    }

    public static InflatedSmartReplies inflate(Context context, Context context2, NotificationEntry notificationEntry, SmartReplyConstants smartReplyConstants, SmartReplyController smartReplyController, HeadsUpManager headsUpManager, SmartRepliesAndActions smartRepliesAndActions) {
        SmartRepliesAndActions smartRepliesAndActionsChooseSmartRepliesAndActions = chooseSmartRepliesAndActions(smartReplyConstants, notificationEntry);
        if (!shouldShowSmartReplyView(notificationEntry, smartRepliesAndActionsChooseSmartRepliesAndActions)) {
            return new InflatedSmartReplies(null, null, smartRepliesAndActionsChooseSmartRepliesAndActions);
        }
        boolean z = !areSuggestionsSimilar(smartRepliesAndActions, smartRepliesAndActionsChooseSmartRepliesAndActions);
        SmartReplyView smartReplyViewInflate = SmartReplyView.inflate(context);
        ArrayList arrayList = new ArrayList();
        SmartReplyView.SmartReplies smartReplies = smartRepliesAndActionsChooseSmartRepliesAndActions.smartReplies;
        if (smartReplies != null) {
            arrayList.addAll(smartReplyViewInflate.inflateRepliesFromRemoteInput(smartReplies, smartReplyController, notificationEntry, z));
        }
        SmartReplyView.SmartActions smartActions = smartRepliesAndActionsChooseSmartRepliesAndActions.smartActions;
        if (smartActions != null) {
            arrayList.addAll(smartReplyViewInflate.inflateSmartActions(context2, smartActions, smartReplyController, notificationEntry, headsUpManager, z));
        }
        return new InflatedSmartReplies(smartReplyViewInflate, arrayList, smartRepliesAndActionsChooseSmartRepliesAndActions);
    }

    @VisibleForTesting
    static boolean areSuggestionsSimilar(SmartRepliesAndActions smartRepliesAndActions, SmartRepliesAndActions smartRepliesAndActions2) {
        if (smartRepliesAndActions == smartRepliesAndActions2) {
            return true;
        }
        if (smartRepliesAndActions == null || smartRepliesAndActions2 == null || !smartRepliesAndActions.getSmartReplies().equals(smartRepliesAndActions2.getSmartReplies())) {
            return false;
        }
        return !NotificationUiAdjustment.areDifferent(smartRepliesAndActions.getSmartActions(), smartRepliesAndActions2.getSmartActions());
    }

    public static boolean shouldShowSmartReplyView(NotificationEntry notificationEntry, SmartRepliesAndActions smartRepliesAndActions) {
        return ((smartRepliesAndActions.smartReplies == null && smartRepliesAndActions.smartActions == null) || notificationEntry.getSbn().getNotification().extras.getBoolean("android.remoteInputSpinner", false) || notificationEntry.getSbn().getNotification().extras.getBoolean("android.hideSmartReplies", false)) ? false : true;
    }

    public static SmartRepliesAndActions chooseSmartRepliesAndActions(SmartReplyConstants smartReplyConstants, NotificationEntry notificationEntry) {
        Notification notification = notificationEntry.getSbn().getNotification();
        boolean z = false;
        Pair<RemoteInput, Notification.Action> pairFindRemoteInputActionPair = notification.findRemoteInputActionPair(false);
        Pair<RemoteInput, Notification.Action> pairFindRemoteInputActionPair2 = notification.findRemoteInputActionPair(true);
        if (!smartReplyConstants.isEnabled()) {
            if (DEBUG) {
                Log.d("InflatedSmartReplies", "Smart suggestions not enabled, not adding suggestions for " + notificationEntry.getSbn().getKey());
            }
            return new SmartRepliesAndActions(null, null);
        }
        boolean z2 = (!(!smartReplyConstants.requiresTargetingP() || notificationEntry.targetSdk >= 28) || pairFindRemoteInputActionPair == null || ArrayUtils.isEmpty(((RemoteInput) pairFindRemoteInputActionPair.first).getChoices()) || ((Notification.Action) pairFindRemoteInputActionPair.second).actionIntent == null) ? false : true;
        List<Notification.Action> contextualActions = notification.getContextualActions();
        boolean z3 = !contextualActions.isEmpty();
        SmartReplyView.SmartReplies smartReplies = z2 ? new SmartReplyView.SmartReplies(Arrays.asList(((RemoteInput) pairFindRemoteInputActionPair.first).getChoices()), (RemoteInput) pairFindRemoteInputActionPair.first, ((Notification.Action) pairFindRemoteInputActionPair.second).actionIntent, false) : null;
        SmartReplyView.SmartActions smartActions = z3 ? new SmartReplyView.SmartActions(contextualActions, false) : null;
        if (!z2 && !z3) {
            if ((ArrayUtils.isEmpty(notificationEntry.getSmartReplies()) || pairFindRemoteInputActionPair2 == null || !((Notification.Action) pairFindRemoteInputActionPair2.second).getAllowGeneratedReplies() || ((Notification.Action) pairFindRemoteInputActionPair2.second).actionIntent == null) ? false : true) {
                smartReplies = new SmartReplyView.SmartReplies(notificationEntry.getSmartReplies(), (RemoteInput) pairFindRemoteInputActionPair2.first, ((Notification.Action) pairFindRemoteInputActionPair2.second).actionIntent, true);
            }
            if (!ArrayUtils.isEmpty(notificationEntry.getSmartActions()) && notification.getAllowSystemGeneratedContextualActions()) {
                z = true;
            }
            if (z) {
                List<Notification.Action> smartActions2 = notificationEntry.getSmartActions();
                if (((ActivityManagerWrapper) Dependency.get(ActivityManagerWrapper.class)).isLockTaskKioskModeActive()) {
                    smartActions2 = filterWhiteListedLockTaskApps(smartActions2);
                }
                smartActions = new SmartReplyView.SmartActions(smartActions2, true);
            }
        }
        return new SmartRepliesAndActions(smartReplies, smartActions);
    }

    private static List<Notification.Action> filterWhiteListedLockTaskApps(List<Notification.Action> list) {
        ResolveInfo resolveInfoResolveActivity;
        PackageManagerWrapper packageManagerWrapper = (PackageManagerWrapper) Dependency.get(PackageManagerWrapper.class);
        DevicePolicyManagerWrapper devicePolicyManagerWrapper = (DevicePolicyManagerWrapper) Dependency.get(DevicePolicyManagerWrapper.class);
        ArrayList arrayList = new ArrayList();
        for (Notification.Action action : list) {
            PendingIntent pendingIntent = action.actionIntent;
            if (pendingIntent != null && (resolveInfoResolveActivity = packageManagerWrapper.resolveActivity(pendingIntent.getIntent(), 0)) != null && devicePolicyManagerWrapper.isLockTaskPermitted(resolveInfoResolveActivity.activityInfo.packageName)) {
                arrayList.add(action);
            }
        }
        return arrayList;
    }

    public static boolean hasFreeformRemoteInput(NotificationEntry notificationEntry) {
        return notificationEntry.getSbn().getNotification().findRemoteInputActionPair(true) != null;
    }

    public static class SmartRepliesAndActions {
        public final SmartReplyView.SmartActions smartActions;
        public final SmartReplyView.SmartReplies smartReplies;

        SmartRepliesAndActions(SmartReplyView.SmartReplies smartReplies, SmartReplyView.SmartActions smartActions) {
            this.smartReplies = smartReplies;
            this.smartActions = smartActions;
        }

        public List<CharSequence> getSmartReplies() {
            SmartReplyView.SmartReplies smartReplies = this.smartReplies;
            return smartReplies == null ? Collections.emptyList() : smartReplies.choices;
        }

        public List<Notification.Action> getSmartActions() {
            SmartReplyView.SmartActions smartActions = this.smartActions;
            return smartActions == null ? Collections.emptyList() : smartActions.actions;
        }
    }
}
