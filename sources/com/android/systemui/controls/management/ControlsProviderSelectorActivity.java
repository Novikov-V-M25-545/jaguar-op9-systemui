package com.android.systemui.controls.management;

import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.systemui.R;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controls.controller.ControlsController;
import com.android.systemui.globalactions.GlobalActionsComponent;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.util.LifecycleActivity;
import java.util.concurrent.Executor;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlsProviderSelectorActivity.kt */
/* loaded from: classes.dex */
public final class ControlsProviderSelectorActivity extends LifecycleActivity {
    public static final Companion Companion = new Companion(null);
    private final Executor backExecutor;
    private final ControlsController controlsController;
    private final ControlsProviderSelectorActivity$currentUserTracker$1 currentUserTracker;
    private final Executor executor;
    private final GlobalActionsComponent globalActionsComponent;
    private final ControlsListingController listingController;
    private RecyclerView recyclerView;

    /* compiled from: ControlsProviderSelectorActivity.kt */
    /* renamed from: com.android.systemui.controls.management.ControlsProviderSelectorActivity$onStart$1, reason: invalid class name and case insensitive filesystem */
    static final /* synthetic */ class C00431 extends FunctionReference implements Function1<ComponentName, Unit> {
        C00431(ControlsProviderSelectorActivity controlsProviderSelectorActivity) {
            super(1, controlsProviderSelectorActivity);
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final String getName() {
            return "launchFavoritingActivity";
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final KDeclarationContainer getOwner() {
            return Reflection.getOrCreateKotlinClass(ControlsProviderSelectorActivity.class);
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final String getSignature() {
            return "launchFavoritingActivity(Landroid/content/ComponentName;)V";
        }

        @Override // kotlin.jvm.functions.Function1
        public /* bridge */ /* synthetic */ Unit invoke(ComponentName componentName) {
            invoke2(componentName);
            return Unit.INSTANCE;
        }

        /* renamed from: invoke, reason: avoid collision after fix types in other method */
        public final void invoke2(@Nullable ComponentName componentName) {
            ((ControlsProviderSelectorActivity) this.receiver).launchFavoritingActivity(componentName);
        }
    }

    /* compiled from: ControlsProviderSelectorActivity.kt */
    /* renamed from: com.android.systemui.controls.management.ControlsProviderSelectorActivity$onStart$2, reason: invalid class name */
    static final /* synthetic */ class AnonymousClass2 extends FunctionReference implements Function1<ComponentName, Integer> {
        AnonymousClass2(ControlsController controlsController) {
            super(1, controlsController);
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final String getName() {
            return "countFavoritesForComponent";
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final KDeclarationContainer getOwner() {
            return Reflection.getOrCreateKotlinClass(ControlsController.class);
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final String getSignature() {
            return "countFavoritesForComponent(Landroid/content/ComponentName;)I";
        }

        @Override // kotlin.jvm.functions.Function1
        public /* bridge */ /* synthetic */ Integer invoke(ComponentName componentName) {
            return Integer.valueOf(invoke2(componentName));
        }

        /* renamed from: invoke, reason: avoid collision after fix types in other method */
        public final int invoke2(@NotNull ComponentName p1) {
            Intrinsics.checkParameterIsNotNull(p1, "p1");
            return ((ControlsController) this.receiver).countFavoritesForComponent(p1);
        }
    }

    public static final /* synthetic */ RecyclerView access$getRecyclerView$p(ControlsProviderSelectorActivity controlsProviderSelectorActivity) {
        RecyclerView recyclerView = controlsProviderSelectorActivity.recyclerView;
        if (recyclerView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("recyclerView");
        }
        return recyclerView;
    }

    /* JADX WARN: Type inference failed for: r2v1, types: [com.android.systemui.controls.management.ControlsProviderSelectorActivity$currentUserTracker$1] */
    public ControlsProviderSelectorActivity(@NotNull Executor executor, @NotNull Executor backExecutor, @NotNull ControlsListingController listingController, @NotNull ControlsController controlsController, @NotNull GlobalActionsComponent globalActionsComponent, @NotNull final BroadcastDispatcher broadcastDispatcher) {
        Intrinsics.checkParameterIsNotNull(executor, "executor");
        Intrinsics.checkParameterIsNotNull(backExecutor, "backExecutor");
        Intrinsics.checkParameterIsNotNull(listingController, "listingController");
        Intrinsics.checkParameterIsNotNull(controlsController, "controlsController");
        Intrinsics.checkParameterIsNotNull(globalActionsComponent, "globalActionsComponent");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        this.executor = executor;
        this.backExecutor = backExecutor;
        this.listingController = listingController;
        this.controlsController = controlsController;
        this.globalActionsComponent = globalActionsComponent;
        this.currentUserTracker = new CurrentUserTracker(broadcastDispatcher) { // from class: com.android.systemui.controls.management.ControlsProviderSelectorActivity$currentUserTracker$1
            private final int startingUser;

            {
                this.startingUser = this.this$0.listingController.getCurrentUserId();
            }

            @Override // com.android.systemui.settings.CurrentUserTracker
            public void onUserSwitched(int i) {
                if (i != this.startingUser) {
                    stopTracking();
                    this.this$0.finish();
                }
            }
        };
    }

    /* compiled from: ControlsProviderSelectorActivity.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    protected void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.controls_management);
        Lifecycle lifecycle = getLifecycle();
        ControlsAnimations controlsAnimations = ControlsAnimations.INSTANCE;
        View viewRequireViewById = requireViewById(R.id.controls_management_root);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById, "requireViewById<ViewGrou…controls_management_root)");
        Window window = getWindow();
        Intrinsics.checkExpressionValueIsNotNull(window, "window");
        Intent intent = getIntent();
        Intrinsics.checkExpressionValueIsNotNull(intent, "intent");
        lifecycle.addObserver(controlsAnimations.observerForAnimations((ViewGroup) viewRequireViewById, window, intent));
        ViewStub viewStub = (ViewStub) requireViewById(R.id.stub);
        viewStub.setLayoutResource(R.layout.controls_management_apps);
        viewStub.inflate();
        View viewRequireViewById2 = requireViewById(R.id.list);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById2, "requireViewById(R.id.list)");
        RecyclerView recyclerView = (RecyclerView) viewRequireViewById2;
        this.recyclerView = recyclerView;
        if (recyclerView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("recyclerView");
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        TextView textView = (TextView) requireViewById(R.id.title);
        textView.setText(textView.getResources().getText(R.string.controls_providers_title));
        Button button = (Button) requireViewById(R.id.other_apps);
        button.setVisibility(0);
        button.setText(android.R.string.cancel);
        button.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.controls.management.ControlsProviderSelectorActivity$onCreate$$inlined$apply$lambda$1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.this$0.onBackPressed();
            }
        });
        View viewRequireViewById3 = requireViewById(R.id.done);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById3, "requireViewById<View>(R.id.done)");
        viewRequireViewById3.setVisibility(8);
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        this.globalActionsComponent.handleShowGlobalActionsMenu();
        animateExitAndFinish();
    }

    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    protected void onStart() {
        super.onStart();
        startTracking();
        RecyclerView recyclerView = this.recyclerView;
        if (recyclerView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("recyclerView");
        }
        recyclerView.setAlpha(0.0f);
        RecyclerView recyclerView2 = this.recyclerView;
        if (recyclerView2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("recyclerView");
        }
        Executor executor = this.backExecutor;
        Executor executor2 = this.executor;
        Lifecycle lifecycle = getLifecycle();
        ControlsListingController controlsListingController = this.listingController;
        LayoutInflater layoutInflaterFrom = LayoutInflater.from(this);
        Intrinsics.checkExpressionValueIsNotNull(layoutInflaterFrom, "LayoutInflater.from(this)");
        C00431 c00431 = new C00431(this);
        Resources resources = getResources();
        Intrinsics.checkExpressionValueIsNotNull(resources, "resources");
        FavoritesRenderer favoritesRenderer = new FavoritesRenderer(resources, new AnonymousClass2(this.controlsController));
        Resources resources2 = getResources();
        Intrinsics.checkExpressionValueIsNotNull(resources2, "resources");
        AppAdapter appAdapter = new AppAdapter(executor, executor2, lifecycle, controlsListingController, layoutInflaterFrom, c00431, favoritesRenderer, resources2);
        appAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() { // from class: com.android.systemui.controls.management.ControlsProviderSelectorActivity$onStart$$inlined$apply$lambda$1
            private boolean hasAnimated;

            @Override // androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
            public void onChanged() {
                if (this.hasAnimated) {
                    return;
                }
                this.hasAnimated = true;
                ControlsAnimations.INSTANCE.enterAnimation(ControlsProviderSelectorActivity.access$getRecyclerView$p(this.this$0)).start();
            }
        });
        recyclerView2.setAdapter(appAdapter);
    }

    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    protected void onStop() {
        super.onStop();
        stopTracking();
    }

    public final void launchFavoritingActivity(@Nullable final ComponentName componentName) {
        this.executor.execute(new Runnable() { // from class: com.android.systemui.controls.management.ControlsProviderSelectorActivity.launchFavoritingActivity.1
            @Override // java.lang.Runnable
            public final void run() {
                ComponentName componentName2 = componentName;
                if (componentName2 != null) {
                    Intent intent = new Intent(ControlsProviderSelectorActivity.this.getApplicationContext(), (Class<?>) ControlsFavoritingActivity.class);
                    intent.putExtra("extra_app_label", ControlsProviderSelectorActivity.this.listingController.getAppLabel(componentName2));
                    intent.putExtra("android.intent.extra.COMPONENT_NAME", componentName2);
                    intent.putExtra("extra_from_provider_selector", true);
                    ControlsProviderSelectorActivity controlsProviderSelectorActivity = ControlsProviderSelectorActivity.this;
                    controlsProviderSelectorActivity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(controlsProviderSelectorActivity, new Pair[0]).toBundle());
                }
            }
        });
    }

    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    protected void onDestroy() {
        stopTracking();
        super.onDestroy();
    }

    private final void animateExitAndFinish() {
        ViewGroup rootView = (ViewGroup) requireViewById(R.id.controls_management_root);
        Intrinsics.checkExpressionValueIsNotNull(rootView, "rootView");
        ControlsAnimations.exitAnimation(rootView, new Runnable() { // from class: com.android.systemui.controls.management.ControlsProviderSelectorActivity.animateExitAndFinish.1
            @Override // java.lang.Runnable
            public void run() {
                ControlsProviderSelectorActivity.this.finish();
            }
        }).start();
    }
}
