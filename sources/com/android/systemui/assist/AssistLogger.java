package com.android.systemui.assist;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import com.android.internal.app.AssistUtils;
import com.android.internal.logging.InstanceId;
import com.android.internal.logging.InstanceIdSequence;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.util.FrameworkStatsLog;
import com.android.keyguard.KeyguardUpdateMonitor;
import java.util.Set;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.collections.SetsKt__SetsKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: AssistLogger.kt */
/* loaded from: classes.dex */
public class AssistLogger {
    public static final Companion Companion = new Companion(null);
    private static final Set<AssistantSessionEvent> SESSION_END_EVENTS = SetsKt__SetsKt.setOf((Object[]) new AssistantSessionEvent[]{AssistantSessionEvent.ASSISTANT_SESSION_INVOCATION_CANCELLED, AssistantSessionEvent.ASSISTANT_SESSION_CLOSE});
    private final AssistHandleBehaviorController assistHandleBehaviorController;
    private final AssistUtils assistUtils;

    @NotNull
    private final Context context;
    private InstanceId currentInstanceId;
    private final InstanceIdSequence instanceIdSequence;
    private final PhoneStateMonitor phoneStateMonitor;

    @NotNull
    private final UiEventLogger uiEventLogger;

    protected void reportAssistantInvocationExtraData() {
    }

    public AssistLogger(@NotNull Context context, @NotNull UiEventLogger uiEventLogger, @NotNull AssistUtils assistUtils, @NotNull PhoneStateMonitor phoneStateMonitor, @NotNull AssistHandleBehaviorController assistHandleBehaviorController) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(uiEventLogger, "uiEventLogger");
        Intrinsics.checkParameterIsNotNull(assistUtils, "assistUtils");
        Intrinsics.checkParameterIsNotNull(phoneStateMonitor, "phoneStateMonitor");
        Intrinsics.checkParameterIsNotNull(assistHandleBehaviorController, "assistHandleBehaviorController");
        this.context = context;
        this.uiEventLogger = uiEventLogger;
        this.assistUtils = assistUtils;
        this.phoneStateMonitor = phoneStateMonitor;
        this.assistHandleBehaviorController = assistHandleBehaviorController;
        this.instanceIdSequence = new InstanceIdSequence(1048576);
    }

    public final void reportAssistantInvocationEventFromLegacy(int i, boolean z, @Nullable ComponentName componentName, @Nullable Integer num) {
        reportAssistantInvocationEvent(AssistantInvocationEvent.Companion.eventFromLegacyInvocationType(i, z), componentName, num == null ? null : Integer.valueOf(AssistantInvocationEvent.Companion.deviceStateFromLegacyDeviceState(num.intValue())));
    }

    public final void reportAssistantInvocationEvent(@NotNull UiEventLogger.UiEventEnum invocationEvent, @Nullable ComponentName componentName, @Nullable Integer num) {
        int iDeviceStateFromLegacyDeviceState;
        Intrinsics.checkParameterIsNotNull(invocationEvent, "invocationEvent");
        if (componentName == null) {
            componentName = getAssistantComponentForCurrentUser();
        }
        int assistantUid = getAssistantUid(componentName);
        if (num != null) {
            iDeviceStateFromLegacyDeviceState = num.intValue();
        } else {
            iDeviceStateFromLegacyDeviceState = AssistantInvocationEvent.Companion.deviceStateFromLegacyDeviceState(this.phoneStateMonitor.getPhoneState());
        }
        FrameworkStatsLog.write(281, invocationEvent.getId(), assistantUid, componentName.flattenToString(), getOrCreateInstanceId().getId(), iDeviceStateFromLegacyDeviceState, this.assistHandleBehaviorController.areHandlesShowing());
        reportAssistantInvocationExtraData();
    }

    public final void reportAssistantSessionEvent(@NotNull UiEventLogger.UiEventEnum sessionEvent) {
        Intrinsics.checkParameterIsNotNull(sessionEvent, "sessionEvent");
        ComponentName assistantComponentForCurrentUser = getAssistantComponentForCurrentUser();
        this.uiEventLogger.logWithInstanceId(sessionEvent, getAssistantUid(assistantComponentForCurrentUser), assistantComponentForCurrentUser.flattenToString(), getOrCreateInstanceId());
        if (CollectionsKt___CollectionsKt.contains(SESSION_END_EVENTS, sessionEvent)) {
            clearInstanceId();
        }
    }

    @NotNull
    protected final InstanceId getOrCreateInstanceId() {
        InstanceId instanceId = this.currentInstanceId;
        if (instanceId == null) {
            instanceId = this.instanceIdSequence.newInstanceId();
        }
        this.currentInstanceId = instanceId;
        Intrinsics.checkExpressionValueIsNotNull(instanceId, "instanceId");
        return instanceId;
    }

    protected final void clearInstanceId() {
        this.currentInstanceId = null;
    }

    @NotNull
    protected final ComponentName getAssistantComponentForCurrentUser() {
        ComponentName assistComponentForUser = this.assistUtils.getAssistComponentForUser(KeyguardUpdateMonitor.getCurrentUser());
        Intrinsics.checkExpressionValueIsNotNull(assistComponentForUser, "assistUtils.getAssistCom…Monitor.getCurrentUser())");
        return assistComponentForUser;
    }

    protected final int getAssistantUid(@NotNull ComponentName assistantComponent) {
        Intrinsics.checkParameterIsNotNull(assistantComponent, "assistantComponent");
        try {
            return this.context.getPackageManager().getApplicationInfo(assistantComponent.getPackageName(), 0).uid;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("AssistLogger", "Unable to find Assistant UID", e);
            return 0;
        }
    }

    /* compiled from: AssistLogger.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }
}
