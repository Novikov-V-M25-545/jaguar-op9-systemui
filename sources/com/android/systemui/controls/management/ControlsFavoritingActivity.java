package com.android.systemui.controls.management;

import android.R;
import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.viewpager2.widget.ViewPager2;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.systemui.Prefs;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controls.TooltipManager;
import com.android.systemui.controls.controller.ControlInfo;
import com.android.systemui.controls.controller.ControlsControllerImpl;
import com.android.systemui.controls.controller.StructureInfo;
import com.android.systemui.controls.management.ControlsModel;
import com.android.systemui.globalactions.GlobalActionsComponent;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.util.LifecycleActivity;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import kotlin.Unit;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlsFavoritingActivity.kt */
/* loaded from: classes.dex */
public final class ControlsFavoritingActivity extends LifecycleActivity {
    public static final Companion Companion = new Companion(null);
    private CharSequence appName;
    private Runnable cancelLoadRunnable;
    private Comparator<StructureContainer> comparator;
    private ComponentName component;
    private final ControlsControllerImpl controller;
    private final ControlsFavoritingActivity$controlsModelCallback$1 controlsModelCallback;
    private final ControlsFavoritingActivity$currentUserTracker$1 currentUserTracker;
    private View doneButton;
    private final Executor executor;
    private boolean fromProviderSelector;
    private final GlobalActionsComponent globalActionsComponent;
    private boolean isPagerLoaded;
    private List<StructureContainer> listOfStructures;
    private final ControlsFavoritingActivity$listingCallback$1 listingCallback;
    private final ControlsListingController listingController;
    private TooltipManager mTooltipManager;
    private View otherAppsButton;
    private ManagementPageIndicator pageIndicator;
    private TextView statusText;
    private CharSequence structureExtra;
    private ViewPager2 structurePager;
    private TextView subtitleView;
    private TextView titleView;

    public static final /* synthetic */ Comparator access$getComparator$p(ControlsFavoritingActivity controlsFavoritingActivity) {
        Comparator<StructureContainer> comparator = controlsFavoritingActivity.comparator;
        if (comparator == null) {
            Intrinsics.throwUninitializedPropertyAccessException("comparator");
        }
        return comparator;
    }

    public static final /* synthetic */ View access$getDoneButton$p(ControlsFavoritingActivity controlsFavoritingActivity) {
        View view = controlsFavoritingActivity.doneButton;
        if (view == null) {
            Intrinsics.throwUninitializedPropertyAccessException("doneButton");
        }
        return view;
    }

    public static final /* synthetic */ View access$getOtherAppsButton$p(ControlsFavoritingActivity controlsFavoritingActivity) {
        View view = controlsFavoritingActivity.otherAppsButton;
        if (view == null) {
            Intrinsics.throwUninitializedPropertyAccessException("otherAppsButton");
        }
        return view;
    }

    public static final /* synthetic */ ManagementPageIndicator access$getPageIndicator$p(ControlsFavoritingActivity controlsFavoritingActivity) {
        ManagementPageIndicator managementPageIndicator = controlsFavoritingActivity.pageIndicator;
        if (managementPageIndicator == null) {
            Intrinsics.throwUninitializedPropertyAccessException("pageIndicator");
        }
        return managementPageIndicator;
    }

    public static final /* synthetic */ TextView access$getStatusText$p(ControlsFavoritingActivity controlsFavoritingActivity) {
        TextView textView = controlsFavoritingActivity.statusText;
        if (textView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("statusText");
        }
        return textView;
    }

    public static final /* synthetic */ ViewPager2 access$getStructurePager$p(ControlsFavoritingActivity controlsFavoritingActivity) {
        ViewPager2 viewPager2 = controlsFavoritingActivity.structurePager;
        if (viewPager2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("structurePager");
        }
        return viewPager2;
    }

    public static final /* synthetic */ TextView access$getSubtitleView$p(ControlsFavoritingActivity controlsFavoritingActivity) {
        TextView textView = controlsFavoritingActivity.subtitleView;
        if (textView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("subtitleView");
        }
        return textView;
    }

    public static final /* synthetic */ TextView access$getTitleView$p(ControlsFavoritingActivity controlsFavoritingActivity) {
        TextView textView = controlsFavoritingActivity.titleView;
        if (textView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("titleView");
        }
        return textView;
    }

    /* JADX WARN: Type inference failed for: r2v2, types: [com.android.systemui.controls.management.ControlsFavoritingActivity$currentUserTracker$1] */
    /* JADX WARN: Type inference failed for: r2v4, types: [com.android.systemui.controls.management.ControlsFavoritingActivity$controlsModelCallback$1] */
    public ControlsFavoritingActivity(@NotNull Executor executor, @NotNull ControlsControllerImpl controller, @NotNull ControlsListingController listingController, @NotNull final BroadcastDispatcher broadcastDispatcher, @NotNull GlobalActionsComponent globalActionsComponent) {
        Intrinsics.checkParameterIsNotNull(executor, "executor");
        Intrinsics.checkParameterIsNotNull(controller, "controller");
        Intrinsics.checkParameterIsNotNull(listingController, "listingController");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(globalActionsComponent, "globalActionsComponent");
        this.executor = executor;
        this.controller = controller;
        this.listingController = listingController;
        this.globalActionsComponent = globalActionsComponent;
        this.listOfStructures = CollectionsKt__CollectionsKt.emptyList();
        this.currentUserTracker = new CurrentUserTracker(broadcastDispatcher) { // from class: com.android.systemui.controls.management.ControlsFavoritingActivity$currentUserTracker$1
            private final int startingUser;

            {
                this.startingUser = this.this$0.controller.getCurrentUserId();
            }

            @Override // com.android.systemui.settings.CurrentUserTracker
            public void onUserSwitched(int i) {
                if (i != this.startingUser) {
                    stopTracking();
                    this.this$0.finish();
                }
            }
        };
        this.listingCallback = new ControlsFavoritingActivity$listingCallback$1(this);
        this.controlsModelCallback = new ControlsModel.ControlsModelCallback() { // from class: com.android.systemui.controls.management.ControlsFavoritingActivity$controlsModelCallback$1
            @Override // com.android.systemui.controls.management.ControlsModel.ControlsModelCallback
            public void onFirstChange() {
                ControlsFavoritingActivity.access$getDoneButton$p(this.this$0).setEnabled(true);
            }
        };
    }

    /* compiled from: ControlsFavoritingActivity.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        if (!this.fromProviderSelector) {
            this.globalActionsComponent.handleShowGlobalActionsMenu();
        }
        animateExitAndFinish();
    }

    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    protected void onCreate(@Nullable Bundle bundle) throws Resources.NotFoundException {
        super.onCreate(bundle);
        Resources resources = getResources();
        Intrinsics.checkExpressionValueIsNotNull(resources, "resources");
        Configuration configuration = resources.getConfiguration();
        Intrinsics.checkExpressionValueIsNotNull(configuration, "resources.configuration");
        final Collator collator = Collator.getInstance(configuration.getLocales().get(0));
        Intrinsics.checkExpressionValueIsNotNull(collator, "collator");
        this.comparator = new Comparator<T>() { // from class: com.android.systemui.controls.management.ControlsFavoritingActivity$onCreate$$inlined$compareBy$1
            /* JADX WARN: Multi-variable type inference failed */
            @Override // java.util.Comparator
            public final int compare(T t, T t2) {
                return collator.compare(((StructureContainer) t).getStructureName(), ((StructureContainer) t2).getStructureName());
            }
        };
        this.appName = getIntent().getCharSequenceExtra("extra_app_label");
        this.structureExtra = getIntent().getCharSequenceExtra("extra_structure");
        this.component = (ComponentName) getIntent().getParcelableExtra("android.intent.extra.COMPONENT_NAME");
        this.fromProviderSelector = getIntent().getBooleanExtra("extra_from_provider_selector", false);
        bindViews();
    }

    private final void loadControls() throws Resources.NotFoundException {
        ComponentName componentName = this.component;
        if (componentName != null) {
            TextView textView = this.statusText;
            if (textView == null) {
                Intrinsics.throwUninitializedPropertyAccessException("statusText");
            }
            textView.setText(getResources().getText(R.string.global_action_screenshot));
            this.controller.loadForComponent(componentName, new ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1(getResources().getText(com.android.systemui.R.string.controls_favorite_other_zone_header), this), new Consumer<Runnable>() { // from class: com.android.systemui.controls.management.ControlsFavoritingActivity$loadControls$$inlined$let$lambda$2
                @Override // java.util.function.Consumer
                public final void accept(@NotNull Runnable runnable) {
                    Intrinsics.checkParameterIsNotNull(runnable, "runnable");
                    this.this$0.cancelLoadRunnable = runnable;
                }
            });
        }
    }

    private final void setUpPager() {
        ViewPager2 viewPager2 = this.structurePager;
        if (viewPager2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("structurePager");
        }
        viewPager2.setAlpha(0.0f);
        ManagementPageIndicator managementPageIndicator = this.pageIndicator;
        if (managementPageIndicator == null) {
            Intrinsics.throwUninitializedPropertyAccessException("pageIndicator");
        }
        managementPageIndicator.setAlpha(0.0f);
        ViewPager2 viewPager22 = this.structurePager;
        if (viewPager22 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("structurePager");
        }
        viewPager22.setAdapter(new StructureAdapter(CollectionsKt__CollectionsKt.emptyList(), getCurrentUserId()));
        viewPager22.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() { // from class: com.android.systemui.controls.management.ControlsFavoritingActivity$setUpPager$$inlined$apply$lambda$1
            @Override // androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
            public void onPageSelected(int i) {
                super.onPageSelected(i);
                CharSequence structureName = ((StructureContainer) this.this$0.listOfStructures.get(i)).getStructureName();
                if (TextUtils.isEmpty(structureName)) {
                    structureName = this.this$0.appName;
                }
                ControlsFavoritingActivity.access$getTitleView$p(this.this$0).setText(structureName);
                ControlsFavoritingActivity.access$getTitleView$p(this.this$0).requestFocus();
            }

            @Override // androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
            public void onPageScrolled(int i, float f, int i2) {
                super.onPageScrolled(i, f, i2);
                ControlsFavoritingActivity.access$getPageIndicator$p(this.this$0).setLocation(i + f);
            }
        });
    }

    private final void bindViews() throws Resources.NotFoundException {
        setContentView(com.android.systemui.R.layout.controls_management);
        Lifecycle lifecycle = getLifecycle();
        ControlsAnimations controlsAnimations = ControlsAnimations.INSTANCE;
        View viewRequireViewById = requireViewById(com.android.systemui.R.id.controls_management_root);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById, "requireViewById<ViewGrou…controls_management_root)");
        Window window = getWindow();
        Intrinsics.checkExpressionValueIsNotNull(window, "window");
        Intent intent = getIntent();
        Intrinsics.checkExpressionValueIsNotNull(intent, "intent");
        lifecycle.addObserver(controlsAnimations.observerForAnimations((ViewGroup) viewRequireViewById, window, intent));
        ViewStub viewStub = (ViewStub) requireViewById(com.android.systemui.R.id.stub);
        viewStub.setLayoutResource(com.android.systemui.R.layout.controls_management_favorites);
        viewStub.inflate();
        View viewRequireViewById2 = requireViewById(com.android.systemui.R.id.status_message);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById2, "requireViewById(R.id.status_message)");
        this.statusText = (TextView) viewRequireViewById2;
        if (shouldShowTooltip()) {
            TextView textView = this.statusText;
            if (textView == null) {
                Intrinsics.throwUninitializedPropertyAccessException("statusText");
            }
            Context context = textView.getContext();
            Intrinsics.checkExpressionValueIsNotNull(context, "statusText.context");
            TooltipManager tooltipManager = new TooltipManager(context, "ControlsStructureSwipeTooltipCount", 2, false, 8, null);
            this.mTooltipManager = tooltipManager;
            addContentView(tooltipManager.getLayout(), new FrameLayout.LayoutParams(-2, -2, 51));
        }
        View viewRequireViewById3 = requireViewById(com.android.systemui.R.id.structure_page_indicator);
        ManagementPageIndicator managementPageIndicator = (ManagementPageIndicator) viewRequireViewById3;
        managementPageIndicator.setVisibilityListener(new Function1<Integer, Unit>() { // from class: com.android.systemui.controls.management.ControlsFavoritingActivity$bindViews$$inlined$apply$lambda$1
            {
                super(1);
            }

            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Unit invoke(Integer num) {
                invoke(num.intValue());
                return Unit.INSTANCE;
            }

            public final void invoke(int i) {
                TooltipManager tooltipManager2;
                if (i == 0 || (tooltipManager2 = this.this$0.mTooltipManager) == null) {
                    return;
                }
                tooltipManager2.hide(true);
            }
        });
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById3, "requireViewById<Manageme…}\n            }\n        }");
        this.pageIndicator = managementPageIndicator;
        CharSequence text = this.structureExtra;
        if (text == null && (text = this.appName) == null) {
            text = getResources().getText(com.android.systemui.R.string.controls_favorite_default_title);
        }
        View viewRequireViewById4 = requireViewById(com.android.systemui.R.id.title);
        TextView textView2 = (TextView) viewRequireViewById4;
        textView2.setText(text);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById4, "requireViewById<TextView…   text = title\n        }");
        this.titleView = textView2;
        View viewRequireViewById5 = requireViewById(com.android.systemui.R.id.subtitle);
        TextView textView3 = (TextView) viewRequireViewById5;
        textView3.setText(textView3.getResources().getText(com.android.systemui.R.string.controls_favorite_subtitle));
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById5, "requireViewById<TextView…orite_subtitle)\n        }");
        this.subtitleView = textView3;
        View viewRequireViewById6 = requireViewById(com.android.systemui.R.id.structure_pager);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById6, "requireViewById<ViewPager2>(R.id.structure_pager)");
        ViewPager2 viewPager2 = (ViewPager2) viewRequireViewById6;
        this.structurePager = viewPager2;
        if (viewPager2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("structurePager");
        }
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() { // from class: com.android.systemui.controls.management.ControlsFavoritingActivity.bindViews.5
            @Override // androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
            public void onPageSelected(int i) {
                super.onPageSelected(i);
                TooltipManager tooltipManager2 = ControlsFavoritingActivity.this.mTooltipManager;
                if (tooltipManager2 != null) {
                    tooltipManager2.hide(true);
                }
            }
        });
        bindButtons();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void animateExitAndFinish() {
        ViewGroup rootView = (ViewGroup) requireViewById(com.android.systemui.R.id.controls_management_root);
        Intrinsics.checkExpressionValueIsNotNull(rootView, "rootView");
        ControlsAnimations.exitAnimation(rootView, new Runnable() { // from class: com.android.systemui.controls.management.ControlsFavoritingActivity.animateExitAndFinish.1
            @Override // java.lang.Runnable
            public void run() {
                ControlsFavoritingActivity.this.finish();
            }
        }).start();
    }

    private final void bindButtons() {
        View viewRequireViewById = requireViewById(com.android.systemui.R.id.other_apps);
        final Button button = (Button) viewRequireViewById;
        button.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.controls.management.ControlsFavoritingActivity$bindButtons$$inlined$apply$lambda$1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(button.getContext(), (Class<?>) ControlsProviderSelectorActivity.class));
                if (ControlsFavoritingActivity.access$getDoneButton$p(this).isEnabled()) {
                    Toast.makeText(this.getApplicationContext(), com.android.systemui.R.string.controls_favorite_toast_no_changes, 0).show();
                }
                ControlsFavoritingActivity controlsFavoritingActivity = this;
                controlsFavoritingActivity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(controlsFavoritingActivity, new Pair[0]).toBundle());
                this.animateExitAndFinish();
            }
        });
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById, "requireViewById<Button>(…)\n            }\n        }");
        this.otherAppsButton = viewRequireViewById;
        View viewRequireViewById2 = requireViewById(com.android.systemui.R.id.done);
        Button button2 = (Button) viewRequireViewById2;
        button2.setEnabled(false);
        button2.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.controls.management.ControlsFavoritingActivity$bindButtons$$inlined$apply$lambda$2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                if (this.this$0.component == null) {
                    return;
                }
                for (StructureContainer structureContainer : this.this$0.listOfStructures) {
                    List<ControlInfo> favorites = structureContainer.getModel().getFavorites();
                    ControlsControllerImpl controlsControllerImpl = this.this$0.controller;
                    ComponentName componentName = this.this$0.component;
                    if (componentName == null) {
                        Intrinsics.throwNpe();
                    }
                    controlsControllerImpl.replaceFavoritesForStructure(new StructureInfo(componentName, structureContainer.getStructureName(), favorites));
                }
                this.this$0.animateExitAndFinish();
                this.this$0.globalActionsComponent.handleShowGlobalActionsMenu();
            }
        });
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById2, "requireViewById<Button>(…)\n            }\n        }");
        this.doneButton = viewRequireViewById2;
    }

    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    protected void onPause() {
        super.onPause();
        TooltipManager tooltipManager = this.mTooltipManager;
        if (tooltipManager != null) {
            tooltipManager.hide(false);
        }
    }

    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    protected void onStart() {
        super.onStart();
        this.listingController.addCallback(this.listingCallback);
        startTracking();
    }

    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    protected void onResume() throws Resources.NotFoundException {
        super.onResume();
        if (this.isPagerLoaded) {
            return;
        }
        setUpPager();
        loadControls();
        this.isPagerLoaded = true;
    }

    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    protected void onStop() {
        super.onStop();
        this.listingController.removeCallback(this.listingCallback);
        stopTracking();
    }

    @Override // android.app.Activity, android.content.ComponentCallbacks
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        Intrinsics.checkParameterIsNotNull(newConfig, "newConfig");
        super.onConfigurationChanged(newConfig);
        TooltipManager tooltipManager = this.mTooltipManager;
        if (tooltipManager != null) {
            tooltipManager.hide(false);
        }
    }

    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    protected void onDestroy() {
        Runnable runnable = this.cancelLoadRunnable;
        if (runnable != null) {
            runnable.run();
        }
        super.onDestroy();
    }

    private final boolean shouldShowTooltip() {
        return Prefs.getInt(getApplicationContext(), "ControlsStructureSwipeTooltipCount", 0) < 2;
    }
}
