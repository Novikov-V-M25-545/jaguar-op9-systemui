package com.android.systemui.controls.controller;

import android.os.IBinder;
import android.service.controls.Control;
import android.service.controls.IControlsSubscriber;
import android.service.controls.IControlsSubscription;
import android.util.Log;
import com.android.systemui.util.concurrency.DelayableExecutor;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: StatefulControlSubscriber.kt */
/* loaded from: classes.dex */
public final class StatefulControlSubscriber extends IControlsSubscriber.Stub {
    public static final Companion Companion = new Companion(null);
    private final DelayableExecutor bgExecutor;
    private final ControlsController controller;
    private final ControlsProviderLifecycleManager provider;
    private final long requestLimit;
    private IControlsSubscription subscription;
    private boolean subscriptionOpen;

    public StatefulControlSubscriber(@NotNull ControlsController controller, @NotNull ControlsProviderLifecycleManager provider, @NotNull DelayableExecutor bgExecutor, long j) {
        Intrinsics.checkParameterIsNotNull(controller, "controller");
        Intrinsics.checkParameterIsNotNull(provider, "provider");
        Intrinsics.checkParameterIsNotNull(bgExecutor, "bgExecutor");
        this.controller = controller;
        this.provider = provider;
        this.bgExecutor = bgExecutor;
        this.requestLimit = j;
    }

    /* compiled from: StatefulControlSubscriber.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    private final void run(IBinder iBinder, final Function0<Unit> function0) {
        if (Intrinsics.areEqual(this.provider.getToken(), iBinder)) {
            this.bgExecutor.execute(new Runnable() { // from class: com.android.systemui.controls.controller.StatefulControlSubscriber.run.1
                @Override // java.lang.Runnable
                public final void run() {
                    function0.invoke();
                }
            });
        }
    }

    public void onSubscribe(@NotNull IBinder token, @NotNull final IControlsSubscription subs) {
        Intrinsics.checkParameterIsNotNull(token, "token");
        Intrinsics.checkParameterIsNotNull(subs, "subs");
        run(token, new Function0<Unit>() { // from class: com.android.systemui.controls.controller.StatefulControlSubscriber.onSubscribe.1
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
                StatefulControlSubscriber.this.subscriptionOpen = true;
                StatefulControlSubscriber.this.subscription = subs;
                StatefulControlSubscriber.this.provider.startSubscription(subs, StatefulControlSubscriber.this.requestLimit);
            }
        });
    }

    public void onNext(@NotNull final IBinder token, @NotNull final Control control) {
        Intrinsics.checkParameterIsNotNull(token, "token");
        Intrinsics.checkParameterIsNotNull(control, "control");
        run(token, new Function0<Unit>() { // from class: com.android.systemui.controls.controller.StatefulControlSubscriber.onNext.1
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
                if (StatefulControlSubscriber.this.subscriptionOpen) {
                    StatefulControlSubscriber.this.controller.refreshStatus(StatefulControlSubscriber.this.provider.getComponentName(), control);
                    return;
                }
                Log.w("StatefulControlSubscriber", "Refresh outside of window for token:" + token);
            }
        });
    }

    public void onError(@NotNull IBinder token, @NotNull final String error) {
        Intrinsics.checkParameterIsNotNull(token, "token");
        Intrinsics.checkParameterIsNotNull(error, "error");
        run(token, new Function0<Unit>() { // from class: com.android.systemui.controls.controller.StatefulControlSubscriber.onError.1
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
                if (StatefulControlSubscriber.this.subscriptionOpen) {
                    StatefulControlSubscriber.this.subscriptionOpen = false;
                    Log.e("StatefulControlSubscriber", "onError receive from '" + StatefulControlSubscriber.this.provider.getComponentName() + "': " + error);
                }
            }
        });
    }

    public void onComplete(@NotNull IBinder token) {
        Intrinsics.checkParameterIsNotNull(token, "token");
        run(token, new Function0<Unit>() { // from class: com.android.systemui.controls.controller.StatefulControlSubscriber.onComplete.1
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
                if (StatefulControlSubscriber.this.subscriptionOpen) {
                    StatefulControlSubscriber.this.subscriptionOpen = false;
                    Log.i("StatefulControlSubscriber", "onComplete receive from '" + StatefulControlSubscriber.this.provider.getComponentName() + '\'');
                }
            }
        });
    }

    public final void cancel() {
        if (this.subscriptionOpen) {
            this.bgExecutor.execute(new Runnable() { // from class: com.android.systemui.controls.controller.StatefulControlSubscriber.cancel.1
                @Override // java.lang.Runnable
                public final void run() {
                    if (StatefulControlSubscriber.this.subscriptionOpen) {
                        StatefulControlSubscriber.this.subscriptionOpen = false;
                        IControlsSubscription iControlsSubscription = StatefulControlSubscriber.this.subscription;
                        if (iControlsSubscription != null) {
                            StatefulControlSubscriber.this.provider.cancelSubscription(iControlsSubscription);
                        }
                        StatefulControlSubscriber.this.subscription = null;
                    }
                }
            });
        }
    }
}
