package com.android.systemui.controls.management;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.os.UserHandle;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.applications.ServiceListing;
import com.android.systemui.controls.ControlsServiceInfo;
import com.android.systemui.controls.management.ControlsListingController;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.collections.SetsKt__SetsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlsListingControllerImpl.kt */
/* loaded from: classes.dex */
public final class ControlsListingControllerImpl implements ControlsListingController {
    public static final Companion Companion = new Companion(null);
    private Set<ComponentName> availableComponents;
    private List<? extends ServiceInfo> availableServices;
    private final Executor backgroundExecutor;
    private final Set<ControlsListingController.ControlsListingCallback> callbacks;
    private final Context context;
    private int currentUserId;
    private ServiceListing serviceListing;
    private final Function1<Context, ServiceListing> serviceListingBuilder;
    private final ServiceListing.Callback serviceListingCallback;
    private AtomicInteger userChangeInProgress;

    /* JADX WARN: Multi-variable type inference failed */
    @VisibleForTesting
    public ControlsListingControllerImpl(@NotNull Context context, @NotNull Executor backgroundExecutor, @NotNull Function1<? super Context, ? extends ServiceListing> serviceListingBuilder) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(backgroundExecutor, "backgroundExecutor");
        Intrinsics.checkParameterIsNotNull(serviceListingBuilder, "serviceListingBuilder");
        this.context = context;
        this.backgroundExecutor = backgroundExecutor;
        this.serviceListingBuilder = serviceListingBuilder;
        this.serviceListing = (ServiceListing) serviceListingBuilder.invoke(context);
        this.callbacks = new LinkedHashSet();
        this.availableComponents = SetsKt__SetsKt.emptySet();
        this.availableServices = CollectionsKt__CollectionsKt.emptyList();
        this.userChangeInProgress = new AtomicInteger(0);
        this.currentUserId = ActivityManager.getCurrentUser();
        ServiceListing.Callback callback = new ServiceListing.Callback() { // from class: com.android.systemui.controls.management.ControlsListingControllerImpl$serviceListingCallback$1
            @Override // com.android.settingslib.applications.ServiceListing.Callback
            public final void onServicesReloaded(List<ServiceInfo> it) {
                Intrinsics.checkExpressionValueIsNotNull(it, "it");
                final List list = CollectionsKt___CollectionsKt.toList(it);
                final LinkedHashSet linkedHashSet = new LinkedHashSet();
                Iterator it2 = list.iterator();
                while (it2.hasNext()) {
                    ComponentName componentName = ((ServiceInfo) it2.next()).getComponentName();
                    Intrinsics.checkExpressionValueIsNotNull(componentName, "s.getComponentName()");
                    linkedHashSet.add(componentName);
                }
                this.this$0.backgroundExecutor.execute(new Runnable() { // from class: com.android.systemui.controls.management.ControlsListingControllerImpl$serviceListingCallback$1.1
                    @Override // java.lang.Runnable
                    public final void run() {
                        if (ControlsListingControllerImpl$serviceListingCallback$1.this.this$0.userChangeInProgress.get() <= 0 && !linkedHashSet.equals(ControlsListingControllerImpl$serviceListingCallback$1.this.this$0.availableComponents)) {
                            Log.d("ControlsListingControllerImpl", "ServiceConfig reloaded, count: " + linkedHashSet.size());
                            ControlsListingControllerImpl$serviceListingCallback$1.this.this$0.availableComponents = linkedHashSet;
                            ControlsListingControllerImpl$serviceListingCallback$1.this.this$0.availableServices = list;
                            List<ControlsServiceInfo> currentServices = ControlsListingControllerImpl$serviceListingCallback$1.this.this$0.getCurrentServices();
                            Iterator it3 = ControlsListingControllerImpl$serviceListingCallback$1.this.this$0.callbacks.iterator();
                            while (it3.hasNext()) {
                                ((ControlsListingController.ControlsListingCallback) it3.next()).onServicesUpdated(currentServices);
                            }
                        }
                    }
                });
            }
        };
        this.serviceListingCallback = callback;
        Log.d("ControlsListingControllerImpl", "Initializing");
        this.serviceListing.addCallback(callback);
        this.serviceListing.setListening(true);
        this.serviceListing.reload();
    }

    /* compiled from: ControlsListingControllerImpl.kt */
    /* renamed from: com.android.systemui.controls.management.ControlsListingControllerImpl$1, reason: invalid class name */
    static final /* synthetic */ class AnonymousClass1 extends FunctionReference implements Function1<Context, ServiceListing> {
        public static final AnonymousClass1 INSTANCE = new AnonymousClass1();

        AnonymousClass1() {
            super(1);
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final String getName() {
            return "createServiceListing";
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final KDeclarationContainer getOwner() {
            return Reflection.getOrCreateKotlinPackage(ControlsListingControllerImplKt.class, "frameworks__base__packages__SystemUI__android_common__SystemUI-core");
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final String getSignature() {
            return "createServiceListing(Landroid/content/Context;)Lcom/android/settingslib/applications/ServiceListing;";
        }

        @Override // kotlin.jvm.functions.Function1
        @NotNull
        public final ServiceListing invoke(@NotNull Context p1) {
            Intrinsics.checkParameterIsNotNull(p1, "p1");
            return ControlsListingControllerImplKt.createServiceListing(p1);
        }
    }

    /* JADX WARN: 'this' call moved to the top of the method (can break code semantics) */
    public ControlsListingControllerImpl(@NotNull Context context, @NotNull Executor executor) {
        this(context, executor, AnonymousClass1.INSTANCE);
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(executor, "executor");
    }

    /* compiled from: ControlsListingControllerImpl.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    @Override // com.android.systemui.util.UserAwareController
    public int getCurrentUserId() {
        return this.currentUserId;
    }

    @Override // com.android.systemui.util.UserAwareController
    public void changeUser(@NotNull final UserHandle newUser) {
        Intrinsics.checkParameterIsNotNull(newUser, "newUser");
        this.userChangeInProgress.incrementAndGet();
        this.serviceListing.setListening(false);
        this.backgroundExecutor.execute(new Runnable() { // from class: com.android.systemui.controls.management.ControlsListingControllerImpl.changeUser.1
            @Override // java.lang.Runnable
            public final void run() {
                if (ControlsListingControllerImpl.this.userChangeInProgress.decrementAndGet() == 0) {
                    ControlsListingControllerImpl.this.currentUserId = newUser.getIdentifier();
                    Context contextForUser = ControlsListingControllerImpl.this.context.createContextAsUser(newUser, 0);
                    ControlsListingControllerImpl controlsListingControllerImpl = ControlsListingControllerImpl.this;
                    Function1 function1 = controlsListingControllerImpl.serviceListingBuilder;
                    Intrinsics.checkExpressionValueIsNotNull(contextForUser, "contextForUser");
                    controlsListingControllerImpl.serviceListing = (ServiceListing) function1.invoke(contextForUser);
                    ControlsListingControllerImpl.this.serviceListing.addCallback(ControlsListingControllerImpl.this.serviceListingCallback);
                    ControlsListingControllerImpl.this.serviceListing.setListening(true);
                    ControlsListingControllerImpl.this.serviceListing.reload();
                }
            }
        });
    }

    @Override // com.android.systemui.statusbar.policy.CallbackController
    public void addCallback(@NotNull final ControlsListingController.ControlsListingCallback listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        this.backgroundExecutor.execute(new Runnable() { // from class: com.android.systemui.controls.management.ControlsListingControllerImpl.addCallback.1
            @Override // java.lang.Runnable
            public final void run() {
                if (ControlsListingControllerImpl.this.userChangeInProgress.get() > 0) {
                    ControlsListingControllerImpl.this.addCallback(listener);
                    return;
                }
                List<ControlsServiceInfo> currentServices = ControlsListingControllerImpl.this.getCurrentServices();
                Log.d("ControlsListingControllerImpl", "Subscribing callback, service count: " + currentServices.size());
                ControlsListingControllerImpl.this.callbacks.add(listener);
                listener.onServicesUpdated(currentServices);
            }
        });
    }

    @Override // com.android.systemui.statusbar.policy.CallbackController
    public void removeCallback(@NotNull final ControlsListingController.ControlsListingCallback listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        this.backgroundExecutor.execute(new Runnable() { // from class: com.android.systemui.controls.management.ControlsListingControllerImpl.removeCallback.1
            @Override // java.lang.Runnable
            public final void run() {
                Log.d("ControlsListingControllerImpl", "Unsubscribing callback");
                ControlsListingControllerImpl.this.callbacks.remove(listener);
            }
        });
    }

    @NotNull
    public List<ControlsServiceInfo> getCurrentServices() {
        List<? extends ServiceInfo> list = this.availableServices;
        ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list, 10));
        Iterator<T> it = list.iterator();
        while (it.hasNext()) {
            arrayList.add(new ControlsServiceInfo(this.context, (ServiceInfo) it.next()));
        }
        return arrayList;
    }

    @Override // com.android.systemui.controls.management.ControlsListingController
    @Nullable
    public CharSequence getAppLabel(@NotNull ComponentName name) {
        Object next;
        Intrinsics.checkParameterIsNotNull(name, "name");
        Iterator<T> it = getCurrentServices().iterator();
        while (true) {
            if (!it.hasNext()) {
                next = null;
                break;
            }
            next = it.next();
            if (Intrinsics.areEqual(((ControlsServiceInfo) next).componentName, name)) {
                break;
            }
        }
        ControlsServiceInfo controlsServiceInfo = (ControlsServiceInfo) next;
        if (controlsServiceInfo != null) {
            return controlsServiceInfo.loadLabel();
        }
        return null;
    }
}
