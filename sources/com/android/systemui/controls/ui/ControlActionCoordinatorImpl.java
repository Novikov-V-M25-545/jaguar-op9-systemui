package com.android.systemui.controls.ui;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.service.controls.Control;
import android.service.controls.actions.BooleanAction;
import android.service.controls.actions.CommandAction;
import android.service.controls.actions.FloatAction;
import android.util.Log;
import com.android.systemui.globalactions.GlobalActionsComponent;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.util.concurrency.DelayableExecutor;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref$BooleanRef;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlActionCoordinatorImpl.kt */
/* loaded from: classes.dex */
public final class ControlActionCoordinatorImpl implements ControlActionCoordinator {
    public static final Companion Companion = new Companion(null);
    private Set<String> actionsInProgress;
    private final ActivityStarter activityStarter;
    private final DelayableExecutor bgExecutor;
    private final Context context;
    private Dialog dialog;
    private final GlobalActionsComponent globalActionsComponent;
    private final KeyguardStateController keyguardStateController;
    private final DelayableExecutor uiExecutor;
    private final Vibrator vibrator;

    public ControlActionCoordinatorImpl(@NotNull Context context, @NotNull DelayableExecutor bgExecutor, @NotNull DelayableExecutor uiExecutor, @NotNull ActivityStarter activityStarter, @NotNull KeyguardStateController keyguardStateController, @NotNull GlobalActionsComponent globalActionsComponent) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(bgExecutor, "bgExecutor");
        Intrinsics.checkParameterIsNotNull(uiExecutor, "uiExecutor");
        Intrinsics.checkParameterIsNotNull(activityStarter, "activityStarter");
        Intrinsics.checkParameterIsNotNull(keyguardStateController, "keyguardStateController");
        Intrinsics.checkParameterIsNotNull(globalActionsComponent, "globalActionsComponent");
        this.context = context;
        this.bgExecutor = bgExecutor;
        this.uiExecutor = uiExecutor;
        this.activityStarter = activityStarter;
        this.keyguardStateController = keyguardStateController;
        this.globalActionsComponent = globalActionsComponent;
        Object systemService = context.getSystemService("vibrator");
        if (systemService == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.os.Vibrator");
        }
        this.vibrator = (Vibrator) systemService;
        this.actionsInProgress = new LinkedHashSet();
    }

    /* compiled from: ControlActionCoordinatorImpl.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    @Override // com.android.systemui.controls.ui.ControlActionCoordinator
    public void closeDialogs() {
        Dialog dialog = this.dialog;
        if (dialog != null) {
            dialog.dismiss();
        }
        this.dialog = null;
    }

    @Override // com.android.systemui.controls.ui.ControlActionCoordinator
    public void toggle(@NotNull final ControlViewHolder cvh, @NotNull final String templateId, final boolean z) {
        Intrinsics.checkParameterIsNotNull(cvh, "cvh");
        Intrinsics.checkParameterIsNotNull(templateId, "templateId");
        bouncerOrRun(new Action(this, cvh.getCws().getCi().getControlId(), new Function0<Unit>() { // from class: com.android.systemui.controls.ui.ControlActionCoordinatorImpl.toggle.1
            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(0);
            }

            @Override // kotlin.jvm.functions.Function0
            public /* bridge */ /* synthetic */ Unit invoke() {
                invoke2();
                return Unit.INSTANCE;
            }

            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final void invoke2() {
                cvh.getLayout().performHapticFeedback(6);
                cvh.action(new BooleanAction(templateId, !z));
            }
        }, true));
    }

    @Override // com.android.systemui.controls.ui.ControlActionCoordinator
    public void touch(@NotNull final ControlViewHolder cvh, @NotNull final String templateId, @NotNull final Control control) {
        Intrinsics.checkParameterIsNotNull(cvh, "cvh");
        Intrinsics.checkParameterIsNotNull(templateId, "templateId");
        Intrinsics.checkParameterIsNotNull(control, "control");
        bouncerOrRun(new Action(this, cvh.getCws().getCi().getControlId(), new Function0<Unit>() { // from class: com.android.systemui.controls.ui.ControlActionCoordinatorImpl.touch.1
            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(0);
            }

            @Override // kotlin.jvm.functions.Function0
            public /* bridge */ /* synthetic */ Unit invoke() {
                invoke2();
                return Unit.INSTANCE;
            }

            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final void invoke2() {
                cvh.getLayout().performHapticFeedback(6);
                if (cvh.usePanel()) {
                    ControlActionCoordinatorImpl controlActionCoordinatorImpl = ControlActionCoordinatorImpl.this;
                    ControlViewHolder controlViewHolder = cvh;
                    PendingIntent appIntent = control.getAppIntent();
                    Intrinsics.checkExpressionValueIsNotNull(appIntent, "control.getAppIntent()");
                    controlActionCoordinatorImpl.showDetail(controlViewHolder, appIntent);
                    return;
                }
                cvh.action(new CommandAction(templateId));
            }
        }, cvh.usePanel()));
    }

    @Override // com.android.systemui.controls.ui.ControlActionCoordinator
    public void drag(boolean z) {
        if (z) {
            vibrate(Vibrations.INSTANCE.getRangeEdgeEffect());
        } else {
            vibrate(Vibrations.INSTANCE.getRangeMiddleEffect());
        }
    }

    @Override // com.android.systemui.controls.ui.ControlActionCoordinator
    public void setValue(@NotNull final ControlViewHolder cvh, @NotNull final String templateId, final float f) {
        Intrinsics.checkParameterIsNotNull(cvh, "cvh");
        Intrinsics.checkParameterIsNotNull(templateId, "templateId");
        bouncerOrRun(new Action(this, cvh.getCws().getCi().getControlId(), new Function0<Unit>() { // from class: com.android.systemui.controls.ui.ControlActionCoordinatorImpl.setValue.1
            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(0);
            }

            @Override // kotlin.jvm.functions.Function0
            public /* bridge */ /* synthetic */ Unit invoke() {
                invoke2();
                return Unit.INSTANCE;
            }

            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final void invoke2() {
                cvh.action(new FloatAction(templateId, f));
            }
        }, false));
    }

    @Override // com.android.systemui.controls.ui.ControlActionCoordinator
    public void longPress(@NotNull final ControlViewHolder cvh) {
        Intrinsics.checkParameterIsNotNull(cvh, "cvh");
        bouncerOrRun(new Action(this, cvh.getCws().getCi().getControlId(), new Function0<Unit>() { // from class: com.android.systemui.controls.ui.ControlActionCoordinatorImpl.longPress.1
            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(0);
            }

            @Override // kotlin.jvm.functions.Function0
            public /* bridge */ /* synthetic */ Unit invoke() {
                invoke2();
                return Unit.INSTANCE;
            }

            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final void invoke2() {
                Control control = cvh.getCws().getControl();
                if (control != null) {
                    cvh.getLayout().performHapticFeedback(0);
                    ControlActionCoordinatorImpl controlActionCoordinatorImpl = ControlActionCoordinatorImpl.this;
                    ControlViewHolder controlViewHolder = cvh;
                    PendingIntent appIntent = control.getAppIntent();
                    Intrinsics.checkExpressionValueIsNotNull(appIntent, "it.getAppIntent()");
                    controlActionCoordinatorImpl.showDetail(controlViewHolder, appIntent);
                }
            }
        }, false));
    }

    @Override // com.android.systemui.controls.ui.ControlActionCoordinator
    public void enableActionOnTouch(@NotNull String controlId) {
        Intrinsics.checkParameterIsNotNull(controlId, "controlId");
        this.actionsInProgress.remove(controlId);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final boolean shouldRunAction(final String str) {
        if (!this.actionsInProgress.add(str)) {
            return false;
        }
        this.uiExecutor.executeDelayed(new Runnable() { // from class: com.android.systemui.controls.ui.ControlActionCoordinatorImpl.shouldRunAction.1
            @Override // java.lang.Runnable
            public final void run() {
                ControlActionCoordinatorImpl.this.actionsInProgress.remove(str);
            }
        }, 3000L);
        return true;
    }

    private final void bouncerOrRun(final Action action) {
        if (this.keyguardStateController.isShowing()) {
            final Ref$BooleanRef ref$BooleanRef = new Ref$BooleanRef();
            ref$BooleanRef.element = !this.keyguardStateController.isUnlocked();
            this.activityStarter.dismissKeyguardThenExecute(new ActivityStarter.OnDismissAction() { // from class: com.android.systemui.controls.ui.ControlActionCoordinatorImpl.bouncerOrRun.1
                @Override // com.android.systemui.plugins.ActivityStarter.OnDismissAction
                public final boolean onDismiss() {
                    Log.d("ControlsUiController", "Device unlocked, invoking controls action");
                    if (ref$BooleanRef.element) {
                        ControlActionCoordinatorImpl.this.globalActionsComponent.handleShowGlobalActionsMenu();
                        return true;
                    }
                    action.invoke();
                    return true;
                }
            }, null, true);
            return;
        }
        action.invoke();
    }

    private final void vibrate(final VibrationEffect vibrationEffect) {
        this.bgExecutor.execute(new Runnable() { // from class: com.android.systemui.controls.ui.ControlActionCoordinatorImpl.vibrate.1
            @Override // java.lang.Runnable
            public final void run() {
                ControlActionCoordinatorImpl.this.vibrator.vibrate(vibrationEffect);
            }
        });
    }

    /* compiled from: ControlActionCoordinatorImpl.kt */
    /* renamed from: com.android.systemui.controls.ui.ControlActionCoordinatorImpl$showDetail$1, reason: invalid class name and case insensitive filesystem */
    static final class RunnableC00471 implements Runnable {
        final /* synthetic */ ControlViewHolder $cvh;
        final /* synthetic */ PendingIntent $pendingIntent;

        RunnableC00471(PendingIntent pendingIntent, ControlViewHolder controlViewHolder) {
            this.$pendingIntent = pendingIntent;
            this.$cvh = controlViewHolder;
        }

        @Override // java.lang.Runnable
        public final void run() {
            List<ResolveInfo> listQueryIntentActivities = ControlActionCoordinatorImpl.this.context.getPackageManager().queryIntentActivities(this.$pendingIntent.getIntent(), 65536);
            Intrinsics.checkExpressionValueIsNotNull(listQueryIntentActivities, "context.packageManager.q…EFAULT_ONLY\n            )");
            ControlActionCoordinatorImpl.this.uiExecutor.execute(new RunnableC00041(listQueryIntentActivities));
        }

        /* compiled from: ControlActionCoordinatorImpl.kt */
        /* renamed from: com.android.systemui.controls.ui.ControlActionCoordinatorImpl$showDetail$1$1, reason: invalid class name and collision with other inner class name */
        static final class RunnableC00041 implements Runnable {
            final /* synthetic */ List $activities;

            RunnableC00041(List list) {
                this.$activities = list;
            }

            @Override // java.lang.Runnable
            public final void run() throws Resources.NotFoundException {
                if (!this.$activities.isEmpty()) {
                    ControlActionCoordinatorImpl controlActionCoordinatorImpl = ControlActionCoordinatorImpl.this;
                    RunnableC00471 runnableC00471 = RunnableC00471.this;
                    DetailDialog detailDialog = new DetailDialog(runnableC00471.$cvh, runnableC00471.$pendingIntent);
                    detailDialog.setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: com.android.systemui.controls.ui.ControlActionCoordinatorImpl$showDetail$1$1$$special$$inlined$also$lambda$1
                        @Override // android.content.DialogInterface.OnDismissListener
                        public final void onDismiss(DialogInterface dialogInterface) {
                            ControlActionCoordinatorImpl.this.dialog = null;
                        }
                    });
                    detailDialog.show();
                    controlActionCoordinatorImpl.dialog = detailDialog;
                    return;
                }
                RunnableC00471.this.$cvh.setErrorStatus();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void showDetail(ControlViewHolder controlViewHolder, PendingIntent pendingIntent) {
        this.bgExecutor.execute(new RunnableC00471(pendingIntent, controlViewHolder));
    }

    /* compiled from: ControlActionCoordinatorImpl.kt */
    public final class Action {
        private final boolean blockable;

        @NotNull
        private final String controlId;

        @NotNull
        private final Function0<Unit> f;
        final /* synthetic */ ControlActionCoordinatorImpl this$0;

        public Action(@NotNull ControlActionCoordinatorImpl controlActionCoordinatorImpl, @NotNull String controlId, Function0<Unit> f, boolean z) {
            Intrinsics.checkParameterIsNotNull(controlId, "controlId");
            Intrinsics.checkParameterIsNotNull(f, "f");
            this.this$0 = controlActionCoordinatorImpl;
            this.controlId = controlId;
            this.f = f;
            this.blockable = z;
        }

        public final void invoke() {
            if (!this.blockable || this.this$0.shouldRunAction(this.controlId)) {
                this.f.invoke();
            }
        }
    }
}
