package com.android.systemui.controls.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.service.controls.IControlsActionCallback;
import android.service.controls.IControlsProvider;
import android.service.controls.IControlsSubscriber;
import android.service.controls.IControlsSubscription;
import android.service.controls.actions.ControlAction;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.systemui.util.concurrency.DelayableExecutor;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlsProviderLifecycleManager.kt */
/* loaded from: classes.dex */
public final class ControlsProviderLifecycleManager implements IBinder.DeathRecipient {
    private final String TAG;
    private final IControlsActionCallback.Stub actionCallbackService;
    private int bindTryCount;

    @NotNull
    private final ComponentName componentName;
    private final Context context;
    private final DelayableExecutor executor;
    private final Intent intent;
    private Runnable onLoadCanceller;

    @GuardedBy({"queuedServiceMethods"})
    private final Set<ServiceMethod> queuedServiceMethods;
    private boolean requiresBound;
    private final ControlsProviderLifecycleManager$serviceConnection$1 serviceConnection;

    @NotNull
    private final IBinder token;

    @NotNull
    private final UserHandle user;
    private ServiceWrapper wrapper;
    public static final Companion Companion = new Companion(null);
    private static final int BIND_FLAGS = 67109121;

    /* JADX WARN: Type inference failed for: r2v3, types: [com.android.systemui.controls.controller.ControlsProviderLifecycleManager$serviceConnection$1] */
    public ControlsProviderLifecycleManager(@NotNull Context context, @NotNull DelayableExecutor executor, @NotNull IControlsActionCallback.Stub actionCallbackService, @NotNull UserHandle user, @NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(executor, "executor");
        Intrinsics.checkParameterIsNotNull(actionCallbackService, "actionCallbackService");
        Intrinsics.checkParameterIsNotNull(user, "user");
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        this.context = context;
        this.executor = executor;
        this.actionCallbackService = actionCallbackService;
        this.user = user;
        this.componentName = componentName;
        Binder binder = new Binder();
        this.token = binder;
        this.queuedServiceMethods = new ArraySet();
        this.TAG = ControlsProviderLifecycleManager.class.getSimpleName();
        Intent intent = new Intent();
        intent.setComponent(componentName);
        Bundle bundle = new Bundle();
        bundle.putBinder("CALLBACK_TOKEN", binder);
        intent.putExtra("CALLBACK_BUNDLE", bundle);
        this.intent = intent;
        this.serviceConnection = new ServiceConnection() { // from class: com.android.systemui.controls.controller.ControlsProviderLifecycleManager$serviceConnection$1
            @Override // android.content.ServiceConnection
            public void onServiceConnected(@NotNull ComponentName name, @NotNull IBinder service) throws RemoteException {
                Intrinsics.checkParameterIsNotNull(name, "name");
                Intrinsics.checkParameterIsNotNull(service, "service");
                Log.d(this.this$0.TAG, "onServiceConnected " + name);
                this.this$0.bindTryCount = 0;
                ControlsProviderLifecycleManager controlsProviderLifecycleManager = this.this$0;
                IControlsProvider iControlsProviderAsInterface = IControlsProvider.Stub.asInterface(service);
                Intrinsics.checkExpressionValueIsNotNull(iControlsProviderAsInterface, "IControlsProvider.Stub.asInterface(service)");
                controlsProviderLifecycleManager.wrapper = new ServiceWrapper(iControlsProviderAsInterface);
                try {
                    service.linkToDeath(this.this$0, 0);
                } catch (RemoteException unused) {
                }
                this.this$0.handlePendingServiceMethods();
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(@Nullable ComponentName componentName2) {
                Log.d(this.this$0.TAG, "onServiceDisconnected " + componentName2);
                this.this$0.wrapper = null;
                this.this$0.bindService(false);
            }

            @Override // android.content.ServiceConnection
            public void onNullBinding(@Nullable ComponentName componentName2) {
                Log.d(this.this$0.TAG, "onNullBinding " + componentName2);
                this.this$0.wrapper = null;
                this.this$0.context.unbindService(this);
            }
        };
    }

    @NotNull
    public final UserHandle getUser() {
        return this.user;
    }

    @NotNull
    public final ComponentName getComponentName() {
        return this.componentName;
    }

    @NotNull
    public final IBinder getToken() {
        return this.token;
    }

    /* compiled from: ControlsProviderLifecycleManager.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void bindService(final boolean z) {
        this.executor.execute(new Runnable() { // from class: com.android.systemui.controls.controller.ControlsProviderLifecycleManager.bindService.1
            @Override // java.lang.Runnable
            public final void run() {
                ControlsProviderLifecycleManager.this.requiresBound = z;
                if (z) {
                    if (ControlsProviderLifecycleManager.this.bindTryCount != 5) {
                        Log.d(ControlsProviderLifecycleManager.this.TAG, "Binding service " + ControlsProviderLifecycleManager.this.intent);
                        ControlsProviderLifecycleManager controlsProviderLifecycleManager = ControlsProviderLifecycleManager.this;
                        controlsProviderLifecycleManager.bindTryCount = controlsProviderLifecycleManager.bindTryCount + 1;
                        try {
                            ControlsProviderLifecycleManager.this.context.bindServiceAsUser(ControlsProviderLifecycleManager.this.intent, ControlsProviderLifecycleManager.this.serviceConnection, ControlsProviderLifecycleManager.BIND_FLAGS, ControlsProviderLifecycleManager.this.getUser());
                            return;
                        } catch (SecurityException e) {
                            Log.e(ControlsProviderLifecycleManager.this.TAG, "Failed to bind to service", e);
                            return;
                        }
                    }
                    return;
                }
                Log.d(ControlsProviderLifecycleManager.this.TAG, "Unbinding service " + ControlsProviderLifecycleManager.this.intent);
                ControlsProviderLifecycleManager.this.bindTryCount = 0;
                if (ControlsProviderLifecycleManager.this.wrapper != null) {
                    ControlsProviderLifecycleManager.this.context.unbindService(ControlsProviderLifecycleManager.this.serviceConnection);
                }
                ControlsProviderLifecycleManager.this.wrapper = null;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void handlePendingServiceMethods() {
        ArraySet arraySet;
        synchronized (this.queuedServiceMethods) {
            arraySet = new ArraySet(this.queuedServiceMethods);
            this.queuedServiceMethods.clear();
        }
        Iterator it = arraySet.iterator();
        while (it.hasNext()) {
            ((ServiceMethod) it.next()).run();
        }
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        if (this.wrapper == null) {
            return;
        }
        this.wrapper = null;
        if (this.requiresBound) {
            Log.d(this.TAG, "binderDied");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void queueServiceMethod(ServiceMethod serviceMethod) {
        synchronized (this.queuedServiceMethods) {
            this.queuedServiceMethods.add(serviceMethod);
        }
    }

    private final void invokeOrQueue(ServiceMethod serviceMethod) {
        if (this.wrapper != null) {
            serviceMethod.run();
        } else {
            queueServiceMethod(serviceMethod);
            bindService(true);
        }
    }

    public final void maybeBindAndLoad(@NotNull final IControlsSubscriber.Stub subscriber) {
        Intrinsics.checkParameterIsNotNull(subscriber, "subscriber");
        this.onLoadCanceller = this.executor.executeDelayed(new Runnable() { // from class: com.android.systemui.controls.controller.ControlsProviderLifecycleManager.maybeBindAndLoad.1
            @Override // java.lang.Runnable
            public final void run() {
                Log.d(ControlsProviderLifecycleManager.this.TAG, "Timeout waiting onLoad for " + ControlsProviderLifecycleManager.this.getComponentName());
                subscriber.onError(ControlsProviderLifecycleManager.this.getToken(), "Timeout waiting onLoad");
                ControlsProviderLifecycleManager.this.unbindService();
            }
        }, 20L, TimeUnit.SECONDS);
        invokeOrQueue(new Load(this, subscriber));
    }

    public final void maybeBindAndLoadSuggested(@NotNull final IControlsSubscriber.Stub subscriber) {
        Intrinsics.checkParameterIsNotNull(subscriber, "subscriber");
        this.onLoadCanceller = this.executor.executeDelayed(new Runnable() { // from class: com.android.systemui.controls.controller.ControlsProviderLifecycleManager.maybeBindAndLoadSuggested.1
            @Override // java.lang.Runnable
            public final void run() {
                Log.d(ControlsProviderLifecycleManager.this.TAG, "Timeout waiting onLoadSuggested for " + ControlsProviderLifecycleManager.this.getComponentName());
                subscriber.onError(ControlsProviderLifecycleManager.this.getToken(), "Timeout waiting onLoadSuggested");
                ControlsProviderLifecycleManager.this.unbindService();
            }
        }, 20L, TimeUnit.SECONDS);
        invokeOrQueue(new Suggest(this, subscriber));
    }

    public final void cancelLoadTimeout() {
        Runnable runnable = this.onLoadCanceller;
        if (runnable != null) {
            runnable.run();
        }
        this.onLoadCanceller = null;
    }

    public final void maybeBindAndSubscribe(@NotNull List<String> controlIds, @NotNull IControlsSubscriber subscriber) {
        Intrinsics.checkParameterIsNotNull(controlIds, "controlIds");
        Intrinsics.checkParameterIsNotNull(subscriber, "subscriber");
        invokeOrQueue(new Subscribe(this, controlIds, subscriber));
    }

    public final void maybeBindAndSendAction(@NotNull String controlId, @NotNull ControlAction action) {
        Intrinsics.checkParameterIsNotNull(controlId, "controlId");
        Intrinsics.checkParameterIsNotNull(action, "action");
        invokeOrQueue(new Action(this, controlId, action));
    }

    public final void startSubscription(@NotNull IControlsSubscription subscription, long j) {
        Intrinsics.checkParameterIsNotNull(subscription, "subscription");
        Log.d(this.TAG, "startSubscription: " + subscription);
        ServiceWrapper serviceWrapper = this.wrapper;
        if (serviceWrapper != null) {
            serviceWrapper.request(subscription, j);
        }
    }

    public final void cancelSubscription(@NotNull IControlsSubscription subscription) {
        Intrinsics.checkParameterIsNotNull(subscription, "subscription");
        Log.d(this.TAG, "cancelSubscription: " + subscription);
        ServiceWrapper serviceWrapper = this.wrapper;
        if (serviceWrapper != null) {
            serviceWrapper.cancel(subscription);
        }
    }

    public final void unbindService() {
        Runnable runnable = this.onLoadCanceller;
        if (runnable != null) {
            runnable.run();
        }
        this.onLoadCanceller = null;
        bindService(false);
    }

    @NotNull
    public String toString() {
        StringBuilder sb = new StringBuilder("ControlsProviderLifecycleManager(");
        sb.append("component=" + this.componentName);
        sb.append(", user=" + this.user);
        sb.append(")");
        String string = sb.toString();
        Intrinsics.checkExpressionValueIsNotNull(string, "StringBuilder(\"ControlsP…\")\")\n        }.toString()");
        return string;
    }

    /* compiled from: ControlsProviderLifecycleManager.kt */
    public abstract class ServiceMethod {
        public abstract boolean callWrapper$frameworks__base__packages__SystemUI__android_common__SystemUI_core();

        public ServiceMethod() {
        }

        public final void run() {
            if (callWrapper$frameworks__base__packages__SystemUI__android_common__SystemUI_core()) {
                return;
            }
            ControlsProviderLifecycleManager.this.queueServiceMethod(this);
            ControlsProviderLifecycleManager.this.binderDied();
        }
    }

    /* compiled from: ControlsProviderLifecycleManager.kt */
    public final class Load extends ServiceMethod {

        @NotNull
        private final IControlsSubscriber.Stub subscriber;
        final /* synthetic */ ControlsProviderLifecycleManager this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public Load(@NotNull ControlsProviderLifecycleManager controlsProviderLifecycleManager, IControlsSubscriber.Stub subscriber) {
            super();
            Intrinsics.checkParameterIsNotNull(subscriber, "subscriber");
            this.this$0 = controlsProviderLifecycleManager;
            this.subscriber = subscriber;
        }

        @Override // com.android.systemui.controls.controller.ControlsProviderLifecycleManager.ServiceMethod
        public boolean callWrapper$frameworks__base__packages__SystemUI__android_common__SystemUI_core() {
            Log.d(this.this$0.TAG, "load " + this.this$0.getComponentName());
            ServiceWrapper serviceWrapper = this.this$0.wrapper;
            if (serviceWrapper != null) {
                return serviceWrapper.load(this.subscriber);
            }
            return false;
        }
    }

    /* compiled from: ControlsProviderLifecycleManager.kt */
    public final class Suggest extends ServiceMethod {

        @NotNull
        private final IControlsSubscriber.Stub subscriber;
        final /* synthetic */ ControlsProviderLifecycleManager this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public Suggest(@NotNull ControlsProviderLifecycleManager controlsProviderLifecycleManager, IControlsSubscriber.Stub subscriber) {
            super();
            Intrinsics.checkParameterIsNotNull(subscriber, "subscriber");
            this.this$0 = controlsProviderLifecycleManager;
            this.subscriber = subscriber;
        }

        @Override // com.android.systemui.controls.controller.ControlsProviderLifecycleManager.ServiceMethod
        public boolean callWrapper$frameworks__base__packages__SystemUI__android_common__SystemUI_core() {
            Log.d(this.this$0.TAG, "suggest " + this.this$0.getComponentName());
            ServiceWrapper serviceWrapper = this.this$0.wrapper;
            if (serviceWrapper != null) {
                return serviceWrapper.loadSuggested(this.subscriber);
            }
            return false;
        }
    }

    /* compiled from: ControlsProviderLifecycleManager.kt */
    public final class Subscribe extends ServiceMethod {

        @NotNull
        private final List<String> list;

        @NotNull
        private final IControlsSubscriber subscriber;
        final /* synthetic */ ControlsProviderLifecycleManager this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public Subscribe(@NotNull ControlsProviderLifecycleManager controlsProviderLifecycleManager, @NotNull List<String> list, IControlsSubscriber subscriber) {
            super();
            Intrinsics.checkParameterIsNotNull(list, "list");
            Intrinsics.checkParameterIsNotNull(subscriber, "subscriber");
            this.this$0 = controlsProviderLifecycleManager;
            this.list = list;
            this.subscriber = subscriber;
        }

        @Override // com.android.systemui.controls.controller.ControlsProviderLifecycleManager.ServiceMethod
        public boolean callWrapper$frameworks__base__packages__SystemUI__android_common__SystemUI_core() {
            Log.d(this.this$0.TAG, "subscribe " + this.this$0.getComponentName() + " - " + this.list);
            ServiceWrapper serviceWrapper = this.this$0.wrapper;
            if (serviceWrapper != null) {
                return serviceWrapper.subscribe(this.list, this.subscriber);
            }
            return false;
        }
    }

    /* compiled from: ControlsProviderLifecycleManager.kt */
    public final class Action extends ServiceMethod {

        @NotNull
        private final ControlAction action;

        @NotNull
        private final String id;
        final /* synthetic */ ControlsProviderLifecycleManager this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public Action(@NotNull ControlsProviderLifecycleManager controlsProviderLifecycleManager, @NotNull String id, ControlAction action) {
            super();
            Intrinsics.checkParameterIsNotNull(id, "id");
            Intrinsics.checkParameterIsNotNull(action, "action");
            this.this$0 = controlsProviderLifecycleManager;
            this.id = id;
            this.action = action;
        }

        @Override // com.android.systemui.controls.controller.ControlsProviderLifecycleManager.ServiceMethod
        public boolean callWrapper$frameworks__base__packages__SystemUI__android_common__SystemUI_core() {
            Log.d(this.this$0.TAG, "onAction " + this.this$0.getComponentName() + " - " + this.id);
            ServiceWrapper serviceWrapper = this.this$0.wrapper;
            if (serviceWrapper != null) {
                return serviceWrapper.action(this.id, this.action, this.this$0.actionCallbackService);
            }
            return false;
        }
    }
}
