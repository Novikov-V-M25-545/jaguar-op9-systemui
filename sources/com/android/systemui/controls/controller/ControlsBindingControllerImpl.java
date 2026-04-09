package com.android.systemui.controls.controller;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;
import android.os.UserHandle;
import android.service.controls.Control;
import android.service.controls.IControlsActionCallback;
import android.service.controls.IControlsSubscriber;
import android.service.controls.IControlsSubscription;
import android.service.controls.actions.ControlAction;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.controls.controller.ControlsBindingController;
import com.android.systemui.controls.controller.ControlsBindingControllerImpl;
import com.android.systemui.util.concurrency.DelayableExecutor;
import dagger.Lazy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import kotlin.Unit;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlsBindingControllerImpl.kt */
@VisibleForTesting
/* loaded from: classes.dex */
public class ControlsBindingControllerImpl implements ControlsBindingController {
    public static final Companion Companion = new Companion(null);
    private static final ControlsBindingControllerImpl$Companion$emptyCallback$1 emptyCallback = new ControlsBindingController.LoadCallback() { // from class: com.android.systemui.controls.controller.ControlsBindingControllerImpl$Companion$emptyCallback$1
        /* renamed from: accept, reason: avoid collision after fix types in other method */
        public void accept2(@NotNull List<Control> controls) {
            Intrinsics.checkParameterIsNotNull(controls, "controls");
        }

        @Override // com.android.systemui.controls.controller.ControlsBindingController.LoadCallback
        public void error(@NotNull String message) {
            Intrinsics.checkParameterIsNotNull(message, "message");
        }

        @Override // java.util.function.Consumer
        public /* bridge */ /* synthetic */ void accept(List<? extends Control> list) {
            accept2((List<Control>) list);
        }
    };
    private final ControlsBindingControllerImpl$actionCallbackService$1 actionCallbackService;
    private final DelayableExecutor backgroundExecutor;
    private final Context context;
    private ControlsProviderLifecycleManager currentProvider;
    private UserHandle currentUser;
    private final Lazy<ControlsController> lazyController;
    private LoadSubscriber loadSubscriber;
    private StatefulControlSubscriber statefulControlSubscriber;

    /* JADX WARN: Type inference failed for: r2v3, types: [com.android.systemui.controls.controller.ControlsBindingControllerImpl$actionCallbackService$1] */
    public ControlsBindingControllerImpl(@NotNull Context context, @NotNull DelayableExecutor backgroundExecutor, @NotNull Lazy<ControlsController> lazyController) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(backgroundExecutor, "backgroundExecutor");
        Intrinsics.checkParameterIsNotNull(lazyController, "lazyController");
        this.context = context;
        this.backgroundExecutor = backgroundExecutor;
        this.lazyController = lazyController;
        this.currentUser = UserHandle.of(ActivityManager.getCurrentUser());
        this.actionCallbackService = new IControlsActionCallback.Stub() { // from class: com.android.systemui.controls.controller.ControlsBindingControllerImpl$actionCallbackService$1
            public void accept(@NotNull IBinder token, @NotNull String controlId, int i) {
                Intrinsics.checkParameterIsNotNull(token, "token");
                Intrinsics.checkParameterIsNotNull(controlId, "controlId");
                this.this$0.backgroundExecutor.execute(new ControlsBindingControllerImpl.OnActionResponseRunnable(this.this$0, token, controlId, i));
            }
        };
    }

    /* compiled from: ControlsBindingControllerImpl.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    @VisibleForTesting
    @NotNull
    public ControlsProviderLifecycleManager createProviderManager$frameworks__base__packages__SystemUI__android_common__SystemUI_core(@NotNull ComponentName component) {
        Intrinsics.checkParameterIsNotNull(component, "component");
        Context context = this.context;
        DelayableExecutor delayableExecutor = this.backgroundExecutor;
        ControlsBindingControllerImpl$actionCallbackService$1 controlsBindingControllerImpl$actionCallbackService$1 = this.actionCallbackService;
        UserHandle currentUser = this.currentUser;
        Intrinsics.checkExpressionValueIsNotNull(currentUser, "currentUser");
        return new ControlsProviderLifecycleManager(context, delayableExecutor, controlsBindingControllerImpl$actionCallbackService$1, currentUser, component);
    }

    private final ControlsProviderLifecycleManager retrieveLifecycleManager(ComponentName componentName) {
        ControlsProviderLifecycleManager controlsProviderLifecycleManager = this.currentProvider;
        if (controlsProviderLifecycleManager != null) {
            if (!Intrinsics.areEqual(controlsProviderLifecycleManager != null ? controlsProviderLifecycleManager.getComponentName() : null, componentName)) {
                unbind();
            }
        }
        ControlsProviderLifecycleManager controlsProviderLifecycleManagerCreateProviderManager$frameworks__base__packages__SystemUI__android_common__SystemUI_core = this.currentProvider;
        if (controlsProviderLifecycleManagerCreateProviderManager$frameworks__base__packages__SystemUI__android_common__SystemUI_core == null) {
            controlsProviderLifecycleManagerCreateProviderManager$frameworks__base__packages__SystemUI__android_common__SystemUI_core = createProviderManager$frameworks__base__packages__SystemUI__android_common__SystemUI_core(componentName);
        }
        this.currentProvider = controlsProviderLifecycleManagerCreateProviderManager$frameworks__base__packages__SystemUI__android_common__SystemUI_core;
        return controlsProviderLifecycleManagerCreateProviderManager$frameworks__base__packages__SystemUI__android_common__SystemUI_core;
    }

    @Override // com.android.systemui.controls.controller.ControlsBindingController
    @NotNull
    public Runnable bindAndLoad(@NotNull ComponentName component, @NotNull ControlsBindingController.LoadCallback callback) {
        Intrinsics.checkParameterIsNotNull(component, "component");
        Intrinsics.checkParameterIsNotNull(callback, "callback");
        LoadSubscriber loadSubscriber = this.loadSubscriber;
        if (loadSubscriber != null) {
            loadSubscriber.loadCancel();
        }
        LoadSubscriber loadSubscriber2 = new LoadSubscriber(this, callback, 100000L);
        this.loadSubscriber = loadSubscriber2;
        retrieveLifecycleManager(component).maybeBindAndLoad(loadSubscriber2);
        return loadSubscriber2.loadCancel();
    }

    @Override // com.android.systemui.controls.controller.ControlsBindingController
    public void bindAndLoadSuggested(@NotNull ComponentName component, @NotNull ControlsBindingController.LoadCallback callback) {
        Intrinsics.checkParameterIsNotNull(component, "component");
        Intrinsics.checkParameterIsNotNull(callback, "callback");
        LoadSubscriber loadSubscriber = this.loadSubscriber;
        if (loadSubscriber != null) {
            loadSubscriber.loadCancel();
        }
        LoadSubscriber loadSubscriber2 = new LoadSubscriber(this, callback, 36L);
        this.loadSubscriber = loadSubscriber2;
        retrieveLifecycleManager(component).maybeBindAndLoadSuggested(loadSubscriber2);
    }

    @Override // com.android.systemui.controls.controller.ControlsBindingController
    public void subscribe(@NotNull StructureInfo structureInfo) {
        Intrinsics.checkParameterIsNotNull(structureInfo, "structureInfo");
        unsubscribe();
        ControlsProviderLifecycleManager controlsProviderLifecycleManagerRetrieveLifecycleManager = retrieveLifecycleManager(structureInfo.getComponentName());
        ControlsController controlsController = this.lazyController.get();
        Intrinsics.checkExpressionValueIsNotNull(controlsController, "lazyController.get()");
        StatefulControlSubscriber statefulControlSubscriber = new StatefulControlSubscriber(controlsController, controlsProviderLifecycleManagerRetrieveLifecycleManager, this.backgroundExecutor, 100000L);
        this.statefulControlSubscriber = statefulControlSubscriber;
        List<ControlInfo> controls = structureInfo.getControls();
        ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(controls, 10));
        Iterator<T> it = controls.iterator();
        while (it.hasNext()) {
            arrayList.add(((ControlInfo) it.next()).getControlId());
        }
        controlsProviderLifecycleManagerRetrieveLifecycleManager.maybeBindAndSubscribe(arrayList, statefulControlSubscriber);
    }

    @Override // com.android.systemui.controls.controller.ControlsBindingController
    public void unsubscribe() {
        StatefulControlSubscriber statefulControlSubscriber = this.statefulControlSubscriber;
        if (statefulControlSubscriber != null) {
            statefulControlSubscriber.cancel();
        }
        this.statefulControlSubscriber = null;
    }

    @Override // com.android.systemui.controls.controller.ControlsBindingController
    public void action(@NotNull ComponentName componentName, @NotNull ControlInfo controlInfo, @NotNull ControlAction action) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(controlInfo, "controlInfo");
        Intrinsics.checkParameterIsNotNull(action, "action");
        if (this.statefulControlSubscriber == null) {
            Log.w("ControlsBindingControllerImpl", "No actions can occur outside of an active subscription. Ignoring.");
        } else {
            retrieveLifecycleManager(componentName).maybeBindAndSendAction(controlInfo.getControlId(), action);
        }
    }

    @Override // com.android.systemui.util.UserAwareController
    /* renamed from: changeUser */
    public void lambda$changeUser$0(@NotNull UserHandle newUser) {
        Intrinsics.checkParameterIsNotNull(newUser, "newUser");
        if (Intrinsics.areEqual(newUser, this.currentUser)) {
            return;
        }
        unbind();
        this.currentUser = newUser;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void unbind() {
        unsubscribe();
        LoadSubscriber loadSubscriber = this.loadSubscriber;
        if (loadSubscriber != null) {
            loadSubscriber.loadCancel();
        }
        this.loadSubscriber = null;
        ControlsProviderLifecycleManager controlsProviderLifecycleManager = this.currentProvider;
        if (controlsProviderLifecycleManager != null) {
            controlsProviderLifecycleManager.unbindService();
        }
        this.currentProvider = null;
    }

    @Override // com.android.systemui.controls.controller.ControlsBindingController
    public void onComponentRemoved(@NotNull final ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        this.backgroundExecutor.execute(new Runnable() { // from class: com.android.systemui.controls.controller.ControlsBindingControllerImpl.onComponentRemoved.1
            @Override // java.lang.Runnable
            public final void run() {
                ControlsProviderLifecycleManager controlsProviderLifecycleManager = ControlsBindingControllerImpl.this.currentProvider;
                if (controlsProviderLifecycleManager == null || !Intrinsics.areEqual(controlsProviderLifecycleManager.getComponentName(), componentName)) {
                    return;
                }
                ControlsBindingControllerImpl.this.unbind();
            }
        });
    }

    @NotNull
    public String toString() {
        StringBuilder sb = new StringBuilder("  ControlsBindingController:\n");
        sb.append("    currentUser=" + this.currentUser + '\n');
        StringBuilder sb2 = new StringBuilder();
        sb2.append("    StatefulControlSubscriber=");
        sb2.append(this.statefulControlSubscriber);
        sb.append(sb2.toString());
        sb.append("    Providers=" + this.currentProvider + '\n');
        String string = sb.toString();
        Intrinsics.checkExpressionValueIsNotNull(string, "StringBuilder(\"  Control…\\n\")\n        }.toString()");
        return string;
    }

    /* compiled from: ControlsBindingControllerImpl.kt */
    private abstract class CallbackRunnable implements Runnable {

        @Nullable
        private final ControlsProviderLifecycleManager provider;
        final /* synthetic */ ControlsBindingControllerImpl this$0;

        @NotNull
        private final IBinder token;

        public abstract void doRun();

        public CallbackRunnable(@NotNull ControlsBindingControllerImpl controlsBindingControllerImpl, IBinder token) {
            Intrinsics.checkParameterIsNotNull(token, "token");
            this.this$0 = controlsBindingControllerImpl;
            this.token = token;
            this.provider = controlsBindingControllerImpl.currentProvider;
        }

        @Nullable
        protected final ControlsProviderLifecycleManager getProvider() {
            return this.provider;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.provider != null) {
                if (!Intrinsics.areEqual(r0.getUser(), this.this$0.currentUser)) {
                    Log.e("ControlsBindingControllerImpl", "User " + this.provider.getUser() + " is not current user");
                    return;
                }
                if (!Intrinsics.areEqual(this.token, this.provider.getToken())) {
                    Log.e("ControlsBindingControllerImpl", "Provider for token:" + this.token + " does not exist anymore");
                    return;
                }
                doRun();
                return;
            }
            Log.e("ControlsBindingControllerImpl", "No current provider set");
        }
    }

    /* compiled from: ControlsBindingControllerImpl.kt */
    private final class OnLoadRunnable extends CallbackRunnable {

        @NotNull
        private final ControlsBindingController.LoadCallback callback;

        @NotNull
        private final List<Control> list;
        final /* synthetic */ ControlsBindingControllerImpl this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public OnLoadRunnable(@NotNull ControlsBindingControllerImpl controlsBindingControllerImpl, @NotNull IBinder token, @NotNull List<Control> list, ControlsBindingController.LoadCallback callback) {
            super(controlsBindingControllerImpl, token);
            Intrinsics.checkParameterIsNotNull(token, "token");
            Intrinsics.checkParameterIsNotNull(list, "list");
            Intrinsics.checkParameterIsNotNull(callback, "callback");
            this.this$0 = controlsBindingControllerImpl;
            this.list = list;
            this.callback = callback;
        }

        @Override // com.android.systemui.controls.controller.ControlsBindingControllerImpl.CallbackRunnable
        public void doRun() {
            Log.d("ControlsBindingControllerImpl", "LoadSubscription: Complete and loading controls");
            this.callback.accept(this.list);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* compiled from: ControlsBindingControllerImpl.kt */
    final class OnCancelAndLoadRunnable extends CallbackRunnable {

        @NotNull
        private final ControlsBindingController.LoadCallback callback;

        @NotNull
        private final List<Control> list;

        @NotNull
        private final IControlsSubscription subscription;
        final /* synthetic */ ControlsBindingControllerImpl this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public OnCancelAndLoadRunnable(@NotNull ControlsBindingControllerImpl controlsBindingControllerImpl, @NotNull IBinder token, @NotNull List<Control> list, @NotNull IControlsSubscription subscription, ControlsBindingController.LoadCallback callback) {
            super(controlsBindingControllerImpl, token);
            Intrinsics.checkParameterIsNotNull(token, "token");
            Intrinsics.checkParameterIsNotNull(list, "list");
            Intrinsics.checkParameterIsNotNull(subscription, "subscription");
            Intrinsics.checkParameterIsNotNull(callback, "callback");
            this.this$0 = controlsBindingControllerImpl;
            this.list = list;
            this.subscription = subscription;
            this.callback = callback;
        }

        @Override // com.android.systemui.controls.controller.ControlsBindingControllerImpl.CallbackRunnable
        public void doRun() {
            Log.d("ControlsBindingControllerImpl", "LoadSubscription: Canceling and loading controls");
            ControlsProviderLifecycleManager provider = getProvider();
            if (provider != null) {
                provider.cancelSubscription(this.subscription);
            }
            this.callback.accept(this.list);
        }
    }

    /* compiled from: ControlsBindingControllerImpl.kt */
    private final class OnSubscribeRunnable extends CallbackRunnable {
        private final long requestLimit;

        @NotNull
        private final IControlsSubscription subscription;
        final /* synthetic */ ControlsBindingControllerImpl this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public OnSubscribeRunnable(@NotNull ControlsBindingControllerImpl controlsBindingControllerImpl, @NotNull IBinder token, IControlsSubscription subscription, long j) {
            super(controlsBindingControllerImpl, token);
            Intrinsics.checkParameterIsNotNull(token, "token");
            Intrinsics.checkParameterIsNotNull(subscription, "subscription");
            this.this$0 = controlsBindingControllerImpl;
            this.subscription = subscription;
            this.requestLimit = j;
        }

        @Override // com.android.systemui.controls.controller.ControlsBindingControllerImpl.CallbackRunnable
        public void doRun() {
            Log.d("ControlsBindingControllerImpl", "LoadSubscription: Starting subscription");
            ControlsProviderLifecycleManager provider = getProvider();
            if (provider != null) {
                provider.startSubscription(this.subscription, this.requestLimit);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* compiled from: ControlsBindingControllerImpl.kt */
    final class OnActionResponseRunnable extends CallbackRunnable {

        @NotNull
        private final String controlId;
        private final int response;
        final /* synthetic */ ControlsBindingControllerImpl this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public OnActionResponseRunnable(@NotNull ControlsBindingControllerImpl controlsBindingControllerImpl, @NotNull IBinder token, String controlId, int i) {
            super(controlsBindingControllerImpl, token);
            Intrinsics.checkParameterIsNotNull(token, "token");
            Intrinsics.checkParameterIsNotNull(controlId, "controlId");
            this.this$0 = controlsBindingControllerImpl;
            this.controlId = controlId;
            this.response = i;
        }

        @Override // com.android.systemui.controls.controller.ControlsBindingControllerImpl.CallbackRunnable
        public void doRun() {
            ControlsProviderLifecycleManager provider = getProvider();
            if (provider != null) {
                ((ControlsController) this.this$0.lazyController.get()).onActionResponse(provider.getComponentName(), this.controlId, this.response);
            }
        }
    }

    /* compiled from: ControlsBindingControllerImpl.kt */
    private final class OnLoadErrorRunnable extends CallbackRunnable {

        @NotNull
        private final ControlsBindingController.LoadCallback callback;

        @NotNull
        private final String error;
        final /* synthetic */ ControlsBindingControllerImpl this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public OnLoadErrorRunnable(@NotNull ControlsBindingControllerImpl controlsBindingControllerImpl, @NotNull IBinder token, @NotNull String error, ControlsBindingController.LoadCallback callback) {
            super(controlsBindingControllerImpl, token);
            Intrinsics.checkParameterIsNotNull(token, "token");
            Intrinsics.checkParameterIsNotNull(error, "error");
            Intrinsics.checkParameterIsNotNull(callback, "callback");
            this.this$0 = controlsBindingControllerImpl;
            this.error = error;
            this.callback = callback;
        }

        @Override // com.android.systemui.controls.controller.ControlsBindingControllerImpl.CallbackRunnable
        public void doRun() {
            this.callback.error(this.error);
            ControlsProviderLifecycleManager provider = getProvider();
            if (provider != null) {
                Log.e("ControlsBindingControllerImpl", "onError receive from '" + provider.getComponentName() + "': " + this.error);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* compiled from: ControlsBindingControllerImpl.kt */
    final class LoadSubscriber extends IControlsSubscriber.Stub {
        private Function0<Unit> _loadCancelInternal;

        @NotNull
        private ControlsBindingController.LoadCallback callback;
        private AtomicBoolean isTerminated;

        @NotNull
        private final ArrayList<Control> loadedControls;
        private final long requestLimit;
        private IControlsSubscription subscription;
        final /* synthetic */ ControlsBindingControllerImpl this$0;

        public LoadSubscriber(@NotNull ControlsBindingControllerImpl controlsBindingControllerImpl, ControlsBindingController.LoadCallback callback, long j) {
            Intrinsics.checkParameterIsNotNull(callback, "callback");
            this.this$0 = controlsBindingControllerImpl;
            this.callback = callback;
            this.requestLimit = j;
            this.loadedControls = new ArrayList<>();
            this.isTerminated = new AtomicBoolean(false);
        }

        public static final /* synthetic */ IControlsSubscription access$getSubscription$p(LoadSubscriber loadSubscriber) {
            IControlsSubscription iControlsSubscription = loadSubscriber.subscription;
            if (iControlsSubscription == null) {
                Intrinsics.throwUninitializedPropertyAccessException("subscription");
            }
            return iControlsSubscription;
        }

        @NotNull
        public final ControlsBindingController.LoadCallback getCallback() {
            return this.callback;
        }

        public final long getRequestLimit() {
            return this.requestLimit;
        }

        @NotNull
        public final ArrayList<Control> getLoadedControls() {
            return this.loadedControls;
        }

        @NotNull
        public final Runnable loadCancel() {
            return new Runnable() { // from class: com.android.systemui.controls.controller.ControlsBindingControllerImpl$LoadSubscriber$loadCancel$1
                @Override // java.lang.Runnable
                public final void run() {
                    Function0 function0 = this.this$0._loadCancelInternal;
                    if (function0 != null) {
                        Log.d("ControlsBindingControllerImpl", "Canceling loadSubscribtion");
                        function0.invoke();
                    }
                }
            };
        }

        public void onSubscribe(@NotNull IBinder token, @NotNull IControlsSubscription subs) {
            Intrinsics.checkParameterIsNotNull(token, "token");
            Intrinsics.checkParameterIsNotNull(subs, "subs");
            this.subscription = subs;
            this._loadCancelInternal = new Function0<Unit>() { // from class: com.android.systemui.controls.controller.ControlsBindingControllerImpl$LoadSubscriber$onSubscribe$1
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
                    ControlsProviderLifecycleManager controlsProviderLifecycleManager = this.this$0.this$0.currentProvider;
                    if (controlsProviderLifecycleManager != null) {
                        controlsProviderLifecycleManager.cancelSubscription(ControlsBindingControllerImpl.LoadSubscriber.access$getSubscription$p(this.this$0));
                    }
                }
            };
            this.this$0.backgroundExecutor.execute(new OnSubscribeRunnable(this.this$0, token, subs, this.requestLimit));
        }

        public void onNext(@NotNull final IBinder token, @NotNull final Control c) {
            Intrinsics.checkParameterIsNotNull(token, "token");
            Intrinsics.checkParameterIsNotNull(c, "c");
            this.this$0.backgroundExecutor.execute(new Runnable() { // from class: com.android.systemui.controls.controller.ControlsBindingControllerImpl$LoadSubscriber$onNext$1
                @Override // java.lang.Runnable
                public final void run() {
                    if (this.this$0.isTerminated.get()) {
                        return;
                    }
                    this.this$0.getLoadedControls().add(c);
                    if (this.this$0.getLoadedControls().size() >= this.this$0.getRequestLimit()) {
                        ControlsBindingControllerImpl.LoadSubscriber loadSubscriber = this.this$0;
                        loadSubscriber.maybeTerminateAndRun(new ControlsBindingControllerImpl.OnCancelAndLoadRunnable(loadSubscriber.this$0, token, loadSubscriber.getLoadedControls(), ControlsBindingControllerImpl.LoadSubscriber.access$getSubscription$p(this.this$0), this.this$0.getCallback()));
                    }
                }
            });
        }

        public void onError(@NotNull IBinder token, @NotNull String s) {
            Intrinsics.checkParameterIsNotNull(token, "token");
            Intrinsics.checkParameterIsNotNull(s, "s");
            maybeTerminateAndRun(new OnLoadErrorRunnable(this.this$0, token, s, this.callback));
        }

        public void onComplete(@NotNull IBinder token) {
            Intrinsics.checkParameterIsNotNull(token, "token");
            maybeTerminateAndRun(new OnLoadRunnable(this.this$0, token, this.loadedControls, this.callback));
        }

        /* JADX INFO: Access modifiers changed from: private */
        public final void maybeTerminateAndRun(final Runnable runnable) {
            if (this.isTerminated.get()) {
                return;
            }
            this._loadCancelInternal = new Function0<Unit>() { // from class: com.android.systemui.controls.controller.ControlsBindingControllerImpl$LoadSubscriber$maybeTerminateAndRun$1
                /* renamed from: invoke, reason: avoid collision after fix types in other method */
                public final void invoke2() {
                }

                @Override // kotlin.jvm.functions.Function0
                public /* bridge */ /* synthetic */ Unit invoke() {
                    invoke2();
                    return Unit.INSTANCE;
                }
            };
            this.callback = ControlsBindingControllerImpl.emptyCallback;
            ControlsProviderLifecycleManager controlsProviderLifecycleManager = this.this$0.currentProvider;
            if (controlsProviderLifecycleManager != null) {
                controlsProviderLifecycleManager.cancelLoadTimeout();
            }
            this.this$0.backgroundExecutor.execute(new Runnable() { // from class: com.android.systemui.controls.controller.ControlsBindingControllerImpl$LoadSubscriber$maybeTerminateAndRun$2
                @Override // java.lang.Runnable
                public final void run() {
                    this.this$0.isTerminated.compareAndSet(false, true);
                    runnable.run();
                }
            });
        }
    }
}
