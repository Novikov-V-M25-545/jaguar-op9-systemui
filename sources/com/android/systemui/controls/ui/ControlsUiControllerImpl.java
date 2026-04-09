package com.android.systemui.controls.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.service.controls.Control;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.Space;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.controls.ControlsServiceInfo;
import com.android.systemui.controls.CustomIconCache;
import com.android.systemui.controls.controller.ControlInfo;
import com.android.systemui.controls.controller.ControlsController;
import com.android.systemui.controls.controller.StructureInfo;
import com.android.systemui.controls.management.ControlsEditingActivity;
import com.android.systemui.controls.management.ControlsFavoritingActivity;
import com.android.systemui.controls.management.ControlsListingController;
import com.android.systemui.controls.management.ControlsProviderSelectorActivity;
import com.android.systemui.globalactions.GlobalActionsPopupMenu;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.phone.ShadeController;
import com.android.systemui.util.concurrency.DelayableExecutor;
import dagger.Lazy;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt__MutableCollectionsJVMKt;
import kotlin.collections.MapsKt__MapsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref$ObjectRef;
import kotlin.jvm.internal.Reflection;
import kotlin.ranges.RangesKt___RangesKt;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsUiControllerImpl.kt */
/* loaded from: classes.dex */
public final class ControlsUiControllerImpl implements ControlsUiController {
    public static final Companion Companion = new Companion(null);
    private static final ComponentName EMPTY_COMPONENT;
    private static final StructureInfo EMPTY_STRUCTURE;
    private final ActivityStarter activityStarter;
    private List<StructureInfo> allStructures;

    @NotNull
    private final DelayableExecutor bgExecutor;
    private final Collator collator;

    @NotNull
    private final Context context;

    @NotNull
    private final ControlActionCoordinator controlActionCoordinator;
    private final Map<ControlKey, ControlViewHolder> controlViewsById;
    private final Map<ControlKey, ControlWithState> controlsById;

    @NotNull
    private final Lazy<ControlsController> controlsController;

    @NotNull
    private final Lazy<ControlsListingController> controlsListingController;
    private Runnable dismissGlobalActions;
    private boolean hidden;
    private final CustomIconCache iconCache;
    private ControlsListingController.ControlsListingCallback listingCallback;
    private final Comparator<SelectionItem> localeComparator;
    private final Consumer<Boolean> onSeedingComplete;
    private ViewGroup parent;
    private ListPopupWindow popup;
    private final ContextThemeWrapper popupThemedContext;
    private boolean retainCache;
    private StructureInfo selectedStructure;
    private final ShadeController shadeController;

    @NotNull
    private final SharedPreferences sharedPreferences;

    @NotNull
    private final DelayableExecutor uiExecutor;

    /* compiled from: ControlsUiControllerImpl.kt */
    /* renamed from: com.android.systemui.controls.ui.ControlsUiControllerImpl$show$1, reason: invalid class name and case insensitive filesystem */
    static final /* synthetic */ class C00541 extends FunctionReference implements Function1<List<? extends SelectionItem>, Unit> {
        C00541(ControlsUiControllerImpl controlsUiControllerImpl) {
            super(1, controlsUiControllerImpl);
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final String getName() {
            return "showSeedingView";
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final KDeclarationContainer getOwner() {
            return Reflection.getOrCreateKotlinClass(ControlsUiControllerImpl.class);
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final String getSignature() {
            return "showSeedingView(Ljava/util/List;)V";
        }

        @Override // kotlin.jvm.functions.Function1
        public /* bridge */ /* synthetic */ Unit invoke(List<? extends SelectionItem> list) {
            invoke2((List<SelectionItem>) list);
            return Unit.INSTANCE;
        }

        /* renamed from: invoke, reason: avoid collision after fix types in other method */
        public final void invoke2(@NotNull List<SelectionItem> p1) {
            Intrinsics.checkParameterIsNotNull(p1, "p1");
            ((ControlsUiControllerImpl) this.receiver).showSeedingView(p1);
        }
    }

    /* compiled from: ControlsUiControllerImpl.kt */
    /* renamed from: com.android.systemui.controls.ui.ControlsUiControllerImpl$show$2, reason: invalid class name */
    static final /* synthetic */ class AnonymousClass2 extends FunctionReference implements Function1<List<? extends SelectionItem>, Unit> {
        AnonymousClass2(ControlsUiControllerImpl controlsUiControllerImpl) {
            super(1, controlsUiControllerImpl);
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final String getName() {
            return "showInitialSetupView";
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final KDeclarationContainer getOwner() {
            return Reflection.getOrCreateKotlinClass(ControlsUiControllerImpl.class);
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final String getSignature() {
            return "showInitialSetupView(Ljava/util/List;)V";
        }

        @Override // kotlin.jvm.functions.Function1
        public /* bridge */ /* synthetic */ Unit invoke(List<? extends SelectionItem> list) {
            invoke2((List<SelectionItem>) list);
            return Unit.INSTANCE;
        }

        /* renamed from: invoke, reason: avoid collision after fix types in other method */
        public final void invoke2(@NotNull List<SelectionItem> p1) {
            Intrinsics.checkParameterIsNotNull(p1, "p1");
            ((ControlsUiControllerImpl) this.receiver).showInitialSetupView(p1);
        }
    }

    /* compiled from: ControlsUiControllerImpl.kt */
    /* renamed from: com.android.systemui.controls.ui.ControlsUiControllerImpl$show$5, reason: invalid class name */
    static final /* synthetic */ class AnonymousClass5 extends FunctionReference implements Function1<List<? extends SelectionItem>, Unit> {
        AnonymousClass5(ControlsUiControllerImpl controlsUiControllerImpl) {
            super(1, controlsUiControllerImpl);
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final String getName() {
            return "showControlsView";
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final KDeclarationContainer getOwner() {
            return Reflection.getOrCreateKotlinClass(ControlsUiControllerImpl.class);
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final String getSignature() {
            return "showControlsView(Ljava/util/List;)V";
        }

        @Override // kotlin.jvm.functions.Function1
        public /* bridge */ /* synthetic */ Unit invoke(List<? extends SelectionItem> list) throws Resources.NotFoundException {
            invoke2((List<SelectionItem>) list);
            return Unit.INSTANCE;
        }

        /* renamed from: invoke, reason: avoid collision after fix types in other method */
        public final void invoke2(@NotNull List<SelectionItem> p1) throws Resources.NotFoundException {
            Intrinsics.checkParameterIsNotNull(p1, "p1");
            ((ControlsUiControllerImpl) this.receiver).showControlsView(p1);
        }
    }

    public ControlsUiControllerImpl(@NotNull Lazy<ControlsController> controlsController, @NotNull Context context, @NotNull DelayableExecutor uiExecutor, @NotNull DelayableExecutor bgExecutor, @NotNull Lazy<ControlsListingController> controlsListingController, @NotNull SharedPreferences sharedPreferences, @NotNull ControlActionCoordinator controlActionCoordinator, @NotNull ActivityStarter activityStarter, @NotNull ShadeController shadeController, @NotNull CustomIconCache iconCache) {
        Intrinsics.checkParameterIsNotNull(controlsController, "controlsController");
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(uiExecutor, "uiExecutor");
        Intrinsics.checkParameterIsNotNull(bgExecutor, "bgExecutor");
        Intrinsics.checkParameterIsNotNull(controlsListingController, "controlsListingController");
        Intrinsics.checkParameterIsNotNull(sharedPreferences, "sharedPreferences");
        Intrinsics.checkParameterIsNotNull(controlActionCoordinator, "controlActionCoordinator");
        Intrinsics.checkParameterIsNotNull(activityStarter, "activityStarter");
        Intrinsics.checkParameterIsNotNull(shadeController, "shadeController");
        Intrinsics.checkParameterIsNotNull(iconCache, "iconCache");
        this.controlsController = controlsController;
        this.context = context;
        this.uiExecutor = uiExecutor;
        this.bgExecutor = bgExecutor;
        this.controlsListingController = controlsListingController;
        this.sharedPreferences = sharedPreferences;
        this.controlActionCoordinator = controlActionCoordinator;
        this.activityStarter = activityStarter;
        this.shadeController = shadeController;
        this.iconCache = iconCache;
        this.selectedStructure = EMPTY_STRUCTURE;
        this.controlsById = new LinkedHashMap();
        this.controlViewsById = new LinkedHashMap();
        this.hidden = true;
        this.popupThemedContext = new ContextThemeWrapper(context, R.style.Control_ListPopupWindow);
        Resources resources = context.getResources();
        Intrinsics.checkExpressionValueIsNotNull(resources, "context.resources");
        Configuration configuration = resources.getConfiguration();
        Intrinsics.checkExpressionValueIsNotNull(configuration, "context.resources.configuration");
        final Collator collator = Collator.getInstance(configuration.getLocales().get(0));
        this.collator = collator;
        Intrinsics.checkExpressionValueIsNotNull(collator, "collator");
        this.localeComparator = new Comparator<T>() { // from class: com.android.systemui.controls.ui.ControlsUiControllerImpl$$special$$inlined$compareBy$1
            /* JADX WARN: Multi-variable type inference failed */
            @Override // java.util.Comparator
            public final int compare(T t, T t2) {
                return collator.compare(((SelectionItem) t).getTitle(), ((SelectionItem) t2).getTitle());
            }
        };
        this.onSeedingComplete = new Consumer<Boolean>() { // from class: com.android.systemui.controls.ui.ControlsUiControllerImpl$onSeedingComplete$1
            @Override // java.util.function.Consumer
            public /* bridge */ /* synthetic */ void accept(Boolean bool) {
                accept(bool.booleanValue());
            }

            public final void accept(boolean z) {
                StructureInfo structureInfo;
                if (z) {
                    ControlsUiControllerImpl controlsUiControllerImpl = this.this$0;
                    Iterator<T> it = controlsUiControllerImpl.getControlsController().get().getFavorites().iterator();
                    if (it.hasNext()) {
                        T next = it.next();
                        if (it.hasNext()) {
                            int size = ((StructureInfo) next).getControls().size();
                            do {
                                T next2 = it.next();
                                int size2 = ((StructureInfo) next2).getControls().size();
                                if (size < size2) {
                                    next = next2;
                                    size = size2;
                                }
                            } while (it.hasNext());
                        }
                        structureInfo = next;
                    } else {
                        structureInfo = null;
                    }
                    StructureInfo structureInfo2 = structureInfo;
                    if (structureInfo2 == null) {
                        structureInfo2 = ControlsUiControllerImpl.EMPTY_STRUCTURE;
                    }
                    controlsUiControllerImpl.selectedStructure = structureInfo2;
                    ControlsUiControllerImpl controlsUiControllerImpl2 = this.this$0;
                    controlsUiControllerImpl2.updatePreferences(controlsUiControllerImpl2.selectedStructure);
                }
                ControlsUiControllerImpl controlsUiControllerImpl3 = this.this$0;
                controlsUiControllerImpl3.reload(ControlsUiControllerImpl.access$getParent$p(controlsUiControllerImpl3));
            }
        };
    }

    public static final /* synthetic */ Runnable access$getDismissGlobalActions$p(ControlsUiControllerImpl controlsUiControllerImpl) {
        Runnable runnable = controlsUiControllerImpl.dismissGlobalActions;
        if (runnable == null) {
            Intrinsics.throwUninitializedPropertyAccessException("dismissGlobalActions");
        }
        return runnable;
    }

    public static final /* synthetic */ ViewGroup access$getParent$p(ControlsUiControllerImpl controlsUiControllerImpl) {
        ViewGroup viewGroup = controlsUiControllerImpl.parent;
        if (viewGroup == null) {
            Intrinsics.throwUninitializedPropertyAccessException("parent");
        }
        return viewGroup;
    }

    @NotNull
    public final Lazy<ControlsController> getControlsController() {
        return this.controlsController;
    }

    @NotNull
    public final DelayableExecutor getUiExecutor() {
        return this.uiExecutor;
    }

    /* compiled from: ControlsUiControllerImpl.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    static {
        ComponentName componentName = new ComponentName("", "");
        EMPTY_COMPONENT = componentName;
        EMPTY_STRUCTURE = new StructureInfo(componentName, "", new ArrayList());
    }

    @Override // com.android.systemui.controls.ui.ControlsUiController
    public boolean getAvailable() {
        return this.controlsController.get().getAvailable();
    }

    /* compiled from: ControlsUiControllerImpl.kt */
    /* renamed from: com.android.systemui.controls.ui.ControlsUiControllerImpl$createCallback$1, reason: invalid class name */
    public static final class AnonymousClass1 implements ControlsListingController.ControlsListingCallback {
        final /* synthetic */ Function1 $onResult;

        AnonymousClass1(Function1 function1) {
            this.$onResult = function1;
        }

        @Override // com.android.systemui.controls.management.ControlsListingController.ControlsListingCallback
        public void onServicesUpdated(@NotNull List<ControlsServiceInfo> serviceInfos) {
            Intrinsics.checkParameterIsNotNull(serviceInfos, "serviceInfos");
            final ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(serviceInfos, 10));
            for (ControlsServiceInfo controlsServiceInfo : serviceInfos) {
                CharSequence charSequenceLoadLabel = controlsServiceInfo.loadLabel();
                Intrinsics.checkExpressionValueIsNotNull(charSequenceLoadLabel, "it.loadLabel()");
                Drawable drawableLoadIcon = controlsServiceInfo.loadIcon();
                Intrinsics.checkExpressionValueIsNotNull(drawableLoadIcon, "it.loadIcon()");
                ComponentName componentName = controlsServiceInfo.componentName;
                Intrinsics.checkExpressionValueIsNotNull(componentName, "it.componentName");
                arrayList.add(new SelectionItem(charSequenceLoadLabel, "", drawableLoadIcon, componentName));
            }
            ControlsUiControllerImpl.this.getUiExecutor().execute(new Runnable() { // from class: com.android.systemui.controls.ui.ControlsUiControllerImpl$createCallback$1$onServicesUpdated$1
                @Override // java.lang.Runnable
                public final void run() {
                    ControlsUiControllerImpl.access$getParent$p(ControlsUiControllerImpl.this).removeAllViews();
                    if (arrayList.size() > 0) {
                        this.this$0.$onResult.invoke(arrayList);
                    }
                }
            });
        }
    }

    private final ControlsListingController.ControlsListingCallback createCallback(Function1<? super List<SelectionItem>, Unit> function1) {
        return new AnonymousClass1(function1);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:16:0x0078  */
    @Override // com.android.systemui.controls.ui.ControlsUiController
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void show(@org.jetbrains.annotations.NotNull android.view.ViewGroup r5, @org.jetbrains.annotations.NotNull java.lang.Runnable r6) {
        /*
            r4 = this;
            java.lang.String r0 = "parent"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r5, r0)
            java.lang.String r0 = "dismissGlobalActions"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r6, r0)
            java.lang.String r0 = "ControlsUiController"
            java.lang.String r1 = "show()"
            android.util.Log.d(r0, r1)
            r4.parent = r5
            r4.dismissGlobalActions = r6
            r5 = 0
            r4.hidden = r5
            r4.retainCache = r5
            dagger.Lazy<com.android.systemui.controls.controller.ControlsController> r5 = r4.controlsController
            java.lang.Object r5 = r5.get()
            com.android.systemui.controls.controller.ControlsController r5 = (com.android.systemui.controls.controller.ControlsController) r5
            java.util.List r5 = r5.getFavorites()
            r4.allStructures = r5
            java.lang.String r6 = "allStructures"
            if (r5 != 0) goto L2f
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r6)
        L2f:
            com.android.systemui.controls.controller.StructureInfo r5 = r4.loadPreference(r5)
            r4.selectedStructure = r5
            dagger.Lazy<com.android.systemui.controls.controller.ControlsController> r5 = r4.controlsController
            java.lang.Object r5 = r5.get()
            com.android.systemui.controls.controller.ControlsController r5 = (com.android.systemui.controls.controller.ControlsController) r5
            java.util.function.Consumer<java.lang.Boolean> r0 = r4.onSeedingComplete
            boolean r5 = r5.addSeedingFavoritesCallback(r0)
            if (r5 == 0) goto L52
            com.android.systemui.controls.ui.ControlsUiControllerImpl$show$1 r5 = new com.android.systemui.controls.ui.ControlsUiControllerImpl$show$1
            r5.<init>(r4)
            com.android.systemui.controls.management.ControlsListingController$ControlsListingCallback r5 = r4.createCallback(r5)
            r4.listingCallback = r5
            goto Leb
        L52:
            com.android.systemui.controls.controller.StructureInfo r5 = r4.selectedStructure
            java.util.List r5 = r5.getControls()
            boolean r5 = r5.isEmpty()
            if (r5 == 0) goto L78
            java.util.List<com.android.systemui.controls.controller.StructureInfo> r5 = r4.allStructures
            if (r5 != 0) goto L65
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r6)
        L65:
            int r5 = r5.size()
            r6 = 1
            if (r5 > r6) goto L78
            com.android.systemui.controls.ui.ControlsUiControllerImpl$show$2 r5 = new com.android.systemui.controls.ui.ControlsUiControllerImpl$show$2
            r5.<init>(r4)
            com.android.systemui.controls.management.ControlsListingController$ControlsListingCallback r5 = r4.createCallback(r5)
            r4.listingCallback = r5
            goto Leb
        L78:
            com.android.systemui.controls.controller.StructureInfo r5 = r4.selectedStructure
            java.util.List r5 = r5.getControls()
            java.util.ArrayList r6 = new java.util.ArrayList
            r0 = 10
            int r0 = kotlin.collections.CollectionsKt.collectionSizeOrDefault(r5, r0)
            r6.<init>(r0)
            java.util.Iterator r5 = r5.iterator()
        L8d:
            boolean r0 = r5.hasNext()
            if (r0 == 0) goto La9
            java.lang.Object r0 = r5.next()
            com.android.systemui.controls.controller.ControlInfo r0 = (com.android.systemui.controls.controller.ControlInfo) r0
            com.android.systemui.controls.ui.ControlWithState r1 = new com.android.systemui.controls.ui.ControlWithState
            com.android.systemui.controls.controller.StructureInfo r2 = r4.selectedStructure
            android.content.ComponentName r2 = r2.getComponentName()
            r3 = 0
            r1.<init>(r2, r0, r3)
            r6.add(r1)
            goto L8d
        La9:
            java.util.Map<com.android.systemui.controls.ui.ControlKey, com.android.systemui.controls.ui.ControlWithState> r5 = r4.controlsById
            java.util.Iterator r6 = r6.iterator()
        Laf:
            boolean r0 = r6.hasNext()
            if (r0 == 0) goto Ld3
            java.lang.Object r0 = r6.next()
            r1 = r0
            com.android.systemui.controls.ui.ControlWithState r1 = (com.android.systemui.controls.ui.ControlWithState) r1
            com.android.systemui.controls.ui.ControlKey r2 = new com.android.systemui.controls.ui.ControlKey
            com.android.systemui.controls.controller.StructureInfo r3 = r4.selectedStructure
            android.content.ComponentName r3 = r3.getComponentName()
            com.android.systemui.controls.controller.ControlInfo r1 = r1.getCi()
            java.lang.String r1 = r1.getControlId()
            r2.<init>(r3, r1)
            r5.put(r2, r0)
            goto Laf
        Ld3:
            com.android.systemui.controls.ui.ControlsUiControllerImpl$show$5 r5 = new com.android.systemui.controls.ui.ControlsUiControllerImpl$show$5
            r5.<init>(r4)
            com.android.systemui.controls.management.ControlsListingController$ControlsListingCallback r5 = r4.createCallback(r5)
            r4.listingCallback = r5
            dagger.Lazy<com.android.systemui.controls.controller.ControlsController> r5 = r4.controlsController
            java.lang.Object r5 = r5.get()
            com.android.systemui.controls.controller.ControlsController r5 = (com.android.systemui.controls.controller.ControlsController) r5
            com.android.systemui.controls.controller.StructureInfo r6 = r4.selectedStructure
            r5.subscribeToFavorites(r6)
        Leb:
            dagger.Lazy<com.android.systemui.controls.management.ControlsListingController> r5 = r4.controlsListingController
            java.lang.Object r5 = r5.get()
            com.android.systemui.controls.management.ControlsListingController r5 = (com.android.systemui.controls.management.ControlsListingController) r5
            com.android.systemui.controls.management.ControlsListingController$ControlsListingCallback r4 = r4.listingCallback
            if (r4 != 0) goto Lfc
            java.lang.String r6 = "listingCallback"
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r6)
        Lfc:
            r5.addCallback(r4)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controls.ui.ControlsUiControllerImpl.show(android.view.ViewGroup, java.lang.Runnable):void");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void reload(final ViewGroup viewGroup) {
        if (this.hidden) {
            return;
        }
        ControlsListingController controlsListingController = this.controlsListingController.get();
        ControlsListingController.ControlsListingCallback controlsListingCallback = this.listingCallback;
        if (controlsListingCallback == null) {
            Intrinsics.throwUninitializedPropertyAccessException("listingCallback");
        }
        controlsListingController.removeCallback(controlsListingCallback);
        this.controlsController.get().unsubscribe();
        ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(viewGroup, "alpha", 1.0f, 0.0f);
        objectAnimatorOfFloat.setInterpolator(new AccelerateInterpolator(1.0f));
        objectAnimatorOfFloat.setDuration(200L);
        objectAnimatorOfFloat.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.controls.ui.ControlsUiControllerImpl.reload.1
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(@NotNull Animator animation) {
                Intrinsics.checkParameterIsNotNull(animation, "animation");
                ControlsUiControllerImpl.this.controlViewsById.clear();
                ControlsUiControllerImpl.this.controlsById.clear();
                ControlsUiControllerImpl controlsUiControllerImpl = ControlsUiControllerImpl.this;
                controlsUiControllerImpl.show(viewGroup, ControlsUiControllerImpl.access$getDismissGlobalActions$p(controlsUiControllerImpl));
                ObjectAnimator objectAnimatorOfFloat2 = ObjectAnimator.ofFloat(viewGroup, "alpha", 0.0f, 1.0f);
                objectAnimatorOfFloat2.setInterpolator(new DecelerateInterpolator(1.0f));
                objectAnimatorOfFloat2.setDuration(200L);
                objectAnimatorOfFloat2.start();
            }
        });
        objectAnimatorOfFloat.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void showSeedingView(List<SelectionItem> list) {
        LayoutInflater layoutInflaterFrom = LayoutInflater.from(this.context);
        int i = R.layout.controls_no_favorites;
        ViewGroup viewGroup = this.parent;
        if (viewGroup == null) {
            Intrinsics.throwUninitializedPropertyAccessException("parent");
        }
        layoutInflaterFrom.inflate(i, viewGroup, true);
        ViewGroup viewGroup2 = this.parent;
        if (viewGroup2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("parent");
        }
        ((TextView) viewGroup2.requireViewById(R.id.controls_subtitle)).setText(this.context.getResources().getString(R.string.controls_seeding_in_progress));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void showInitialSetupView(List<SelectionItem> list) {
        LayoutInflater layoutInflaterFrom = LayoutInflater.from(this.context);
        int i = R.layout.controls_no_favorites;
        ViewGroup viewGroup = this.parent;
        if (viewGroup == null) {
            Intrinsics.throwUninitializedPropertyAccessException("parent");
        }
        layoutInflaterFrom.inflate(i, viewGroup, true);
        ViewGroup viewGroup2 = this.parent;
        if (viewGroup2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("parent");
        }
        View viewRequireViewById = viewGroup2.requireViewById(R.id.controls_no_favorites_group);
        if (viewRequireViewById == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup");
        }
        ViewGroup viewGroup3 = (ViewGroup) viewRequireViewById;
        viewGroup3.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.controls.ui.ControlsUiControllerImpl.showInitialSetupView.1
            @Override // android.view.View.OnClickListener
            public final void onClick(@NotNull View v) {
                Intrinsics.checkParameterIsNotNull(v, "v");
                ControlsUiControllerImpl controlsUiControllerImpl = ControlsUiControllerImpl.this;
                Context context = v.getContext();
                Intrinsics.checkExpressionValueIsNotNull(context, "v.context");
                controlsUiControllerImpl.startProviderSelectorActivity(context);
            }
        });
        ViewGroup viewGroup4 = this.parent;
        if (viewGroup4 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("parent");
        }
        ((TextView) viewGroup4.requireViewById(R.id.controls_subtitle)).setText(this.context.getResources().getString(R.string.quick_controls_subtitle));
        ViewGroup viewGroup5 = this.parent;
        if (viewGroup5 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("parent");
        }
        View viewRequireViewById2 = viewGroup5.requireViewById(R.id.controls_icon_row);
        if (viewRequireViewById2 == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup");
        }
        ViewGroup viewGroup6 = (ViewGroup) viewRequireViewById2;
        for (SelectionItem selectionItem : list) {
            View viewInflate = layoutInflaterFrom.inflate(R.layout.controls_icon, viewGroup3, false);
            if (viewInflate == null) {
                throw new TypeCastException("null cannot be cast to non-null type android.widget.ImageView");
            }
            ImageView imageView = (ImageView) viewInflate;
            imageView.setContentDescription(selectionItem.getTitle());
            imageView.setImageDrawable(selectionItem.getIcon());
            viewGroup6.addView(imageView);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void startFavoritingActivity(Context context, StructureInfo structureInfo) {
        startTargetedActivity(context, structureInfo, ControlsFavoritingActivity.class);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void startEditingActivity(Context context, StructureInfo structureInfo) {
        startTargetedActivity(context, structureInfo, ControlsEditingActivity.class);
    }

    private final void startTargetedActivity(Context context, StructureInfo structureInfo, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        intent.addFlags(335544320);
        putIntentExtras(intent, structureInfo);
        startActivity(context, intent);
        this.retainCache = true;
    }

    private final SelectionItem findSelectionItem(StructureInfo structureInfo, List<SelectionItem> list) {
        Object next;
        Iterator<T> it = list.iterator();
        while (true) {
            if (!it.hasNext()) {
                next = null;
                break;
            }
            next = it.next();
            SelectionItem selectionItem = (SelectionItem) next;
            if (Intrinsics.areEqual(selectionItem.getComponentName(), structureInfo.getComponentName()) && Intrinsics.areEqual(selectionItem.getStructure(), structureInfo.getStructure())) {
                break;
            }
        }
        return (SelectionItem) next;
    }

    private final void putIntentExtras(Intent intent, StructureInfo structureInfo) {
        intent.putExtra("extra_app_label", this.controlsListingController.get().getAppLabel(structureInfo.getComponentName()));
        intent.putExtra("extra_structure", structureInfo.getStructure());
        intent.putExtra("android.intent.extra.COMPONENT_NAME", structureInfo.getComponentName());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void startProviderSelectorActivity(Context context) {
        Intent intent = new Intent(context, (Class<?>) ControlsProviderSelectorActivity.class);
        intent.addFlags(335544320);
        startActivity(context, intent);
    }

    private final void startActivity(final Context context, final Intent intent) {
        intent.putExtra("extra_animate", true);
        Runnable runnable = this.dismissGlobalActions;
        if (runnable == null) {
            Intrinsics.throwUninitializedPropertyAccessException("dismissGlobalActions");
        }
        runnable.run();
        this.activityStarter.dismissKeyguardThenExecute(new ActivityStarter.OnDismissAction() { // from class: com.android.systemui.controls.ui.ControlsUiControllerImpl.startActivity.1
            @Override // com.android.systemui.plugins.ActivityStarter.OnDismissAction
            public final boolean onDismiss() {
                ControlsUiControllerImpl.this.shadeController.collapsePanel(false);
                context.startActivity(intent);
                return true;
            }
        }, null, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void showControlsView(List<SelectionItem> list) throws Resources.NotFoundException {
        this.controlViewsById.clear();
        createListView();
        createDropDown(list);
        createMenu();
    }

    /* JADX WARN: Type inference failed for: r2v4, types: [T, android.widget.ArrayAdapter] */
    private final void createMenu() {
        String[] strArr = {this.context.getResources().getString(R.string.controls_menu_add), this.context.getResources().getString(R.string.controls_menu_edit)};
        Ref$ObjectRef ref$ObjectRef = new Ref$ObjectRef();
        ref$ObjectRef.element = new ArrayAdapter(this.context, R.layout.controls_more_item, strArr);
        ViewGroup viewGroup = this.parent;
        if (viewGroup == null) {
            Intrinsics.throwUninitializedPropertyAccessException("parent");
        }
        ImageView imageView = (ImageView) viewGroup.requireViewById(R.id.controls_more);
        imageView.setOnClickListener(new ViewOnClickListenerC00511(imageView, ref$ObjectRef));
    }

    /* compiled from: ControlsUiControllerImpl.kt */
    /* renamed from: com.android.systemui.controls.ui.ControlsUiControllerImpl$createMenu$1, reason: invalid class name and case insensitive filesystem */
    public static final class ViewOnClickListenerC00511 implements View.OnClickListener {
        final /* synthetic */ Ref$ObjectRef $adapter;
        final /* synthetic */ ImageView $anchor;

        ViewOnClickListenerC00511(ImageView imageView, Ref$ObjectRef ref$ObjectRef) {
            this.$anchor = imageView;
            this.$adapter = ref$ObjectRef;
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // android.view.View.OnClickListener
        public void onClick(@NotNull View v) {
            Intrinsics.checkParameterIsNotNull(v, "v");
            ControlsUiControllerImpl controlsUiControllerImpl = ControlsUiControllerImpl.this;
            final GlobalActionsPopupMenu globalActionsPopupMenu = new GlobalActionsPopupMenu(ControlsUiControllerImpl.this.popupThemedContext, false);
            globalActionsPopupMenu.setAnchorView(this.$anchor);
            globalActionsPopupMenu.setAdapter((ArrayAdapter) this.$adapter.element);
            globalActionsPopupMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.android.systemui.controls.ui.ControlsUiControllerImpl$createMenu$1$onClick$$inlined$apply$lambda$1
                @Override // android.widget.AdapterView.OnItemClickListener
                public void onItemClick(@NotNull AdapterView<?> parent, @NotNull View view, int i, long j) {
                    Intrinsics.checkParameterIsNotNull(parent, "parent");
                    Intrinsics.checkParameterIsNotNull(view, "view");
                    if (i == 0) {
                        ControlsUiControllerImpl controlsUiControllerImpl2 = ControlsUiControllerImpl.this;
                        Context context = view.getContext();
                        Intrinsics.checkExpressionValueIsNotNull(context, "view.context");
                        controlsUiControllerImpl2.startFavoritingActivity(context, ControlsUiControllerImpl.this.selectedStructure);
                    } else if (i == 1) {
                        ControlsUiControllerImpl controlsUiControllerImpl3 = ControlsUiControllerImpl.this;
                        Context context2 = view.getContext();
                        Intrinsics.checkExpressionValueIsNotNull(context2, "view.context");
                        controlsUiControllerImpl3.startEditingActivity(context2, ControlsUiControllerImpl.this.selectedStructure);
                    }
                    globalActionsPopupMenu.dismiss();
                }
            });
            globalActionsPopupMenu.show();
            controlsUiControllerImpl.popup = globalActionsPopupMenu;
        }
    }

    /* compiled from: ControlsUiControllerImpl.kt */
    /* renamed from: com.android.systemui.controls.ui.ControlsUiControllerImpl$createDropDown$3, reason: invalid class name */
    public static final class AnonymousClass3 implements View.OnClickListener {
        final /* synthetic */ Ref$ObjectRef $adapter;
        final /* synthetic */ ViewGroup $anchor;

        AnonymousClass3(ViewGroup viewGroup, Ref$ObjectRef ref$ObjectRef) {
            this.$anchor = viewGroup;
            this.$adapter = ref$ObjectRef;
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // android.view.View.OnClickListener
        public void onClick(@NotNull View v) {
            Intrinsics.checkParameterIsNotNull(v, "v");
            ControlsUiControllerImpl controlsUiControllerImpl = ControlsUiControllerImpl.this;
            final GlobalActionsPopupMenu globalActionsPopupMenu = new GlobalActionsPopupMenu(ControlsUiControllerImpl.this.popupThemedContext, true);
            globalActionsPopupMenu.setAnchorView(this.$anchor);
            globalActionsPopupMenu.setAdapter((ItemAdapter) this.$adapter.element);
            globalActionsPopupMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.android.systemui.controls.ui.ControlsUiControllerImpl$createDropDown$3$onClick$$inlined$apply$lambda$1
                @Override // android.widget.AdapterView.OnItemClickListener
                public void onItemClick(@NotNull AdapterView<?> parent, @NotNull View view, int i, long j) {
                    Intrinsics.checkParameterIsNotNull(parent, "parent");
                    Intrinsics.checkParameterIsNotNull(view, "view");
                    Object itemAtPosition = parent.getItemAtPosition(i);
                    if (itemAtPosition == null) {
                        throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.controls.ui.SelectionItem");
                    }
                    ControlsUiControllerImpl.this.switchAppOrStructure((SelectionItem) itemAtPosition);
                    globalActionsPopupMenu.dismiss();
                }
            });
            globalActionsPopupMenu.show();
            controlsUiControllerImpl.popup = globalActionsPopupMenu;
        }
    }

    private final void createListView() throws Resources.NotFoundException {
        LayoutInflater inflater = LayoutInflater.from(this.context);
        int i = R.layout.controls_with_favorites;
        ViewGroup viewGroup = this.parent;
        if (viewGroup == null) {
            Intrinsics.throwUninitializedPropertyAccessException("parent");
        }
        inflater.inflate(i, viewGroup, true);
        int iFindMaxColumns = findMaxColumns();
        ViewGroup viewGroup2 = this.parent;
        if (viewGroup2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("parent");
        }
        View viewRequireViewById = viewGroup2.requireViewById(R.id.global_actions_controls_list);
        if (viewRequireViewById == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup");
        }
        ViewGroup viewGroup3 = (ViewGroup) viewRequireViewById;
        Intrinsics.checkExpressionValueIsNotNull(inflater, "inflater");
        ViewGroup viewGroupCreateRow = createRow(inflater, viewGroup3);
        Iterator<T> it = this.selectedStructure.getControls().iterator();
        while (it.hasNext()) {
            ControlKey controlKey = new ControlKey(this.selectedStructure.getComponentName(), ((ControlInfo) it.next()).getControlId());
            ControlWithState controlWithState = this.controlsById.get(controlKey);
            if (controlWithState != null) {
                if (viewGroupCreateRow.getChildCount() == iFindMaxColumns) {
                    viewGroupCreateRow = createRow(inflater, viewGroup3);
                }
                View viewInflate = inflater.inflate(R.layout.controls_base_item, viewGroupCreateRow, false);
                if (viewInflate == null) {
                    throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup");
                }
                ViewGroup viewGroup4 = (ViewGroup) viewInflate;
                viewGroupCreateRow.addView(viewGroup4);
                ControlsController controlsController = this.controlsController.get();
                Intrinsics.checkExpressionValueIsNotNull(controlsController, "controlsController.get()");
                ControlViewHolder controlViewHolder = new ControlViewHolder(viewGroup4, controlsController, this.uiExecutor, this.bgExecutor, this.controlActionCoordinator, this.controlsController.get().getCurrentUserId());
                controlViewHolder.bindData(controlWithState);
                this.controlViewsById.put(controlKey, controlViewHolder);
            }
        }
        int size = this.selectedStructure.getControls().size() % iFindMaxColumns;
        for (int i2 = size == 0 ? 0 : iFindMaxColumns - size; i2 > 0; i2--) {
            viewGroupCreateRow.addView(new Space(this.context), new LinearLayout.LayoutParams(0, 0, 1.0f));
        }
    }

    private final int findMaxColumns() throws Resources.NotFoundException {
        int i;
        Resources res = this.context.getResources();
        int integer = res.getInteger(R.integer.controls_max_columns);
        int integer2 = res.getInteger(R.integer.controls_max_columns_adjust_below_width_dp);
        TypedValue typedValue = new TypedValue();
        res.getValue(R.dimen.controls_max_columns_adjust_above_font_scale, typedValue, true);
        float f = typedValue.getFloat();
        Intrinsics.checkExpressionValueIsNotNull(res, "res");
        Configuration configuration = res.getConfiguration();
        return (!(configuration.orientation == 1) || (i = configuration.screenWidthDp) == 0 || i > integer2 || configuration.fontScale < f) ? integer : integer - 1;
    }

    private final StructureInfo loadPreference(List<StructureInfo> list) {
        ComponentName componentNameUnflattenFromString;
        if (list.isEmpty()) {
            return EMPTY_STRUCTURE;
        }
        Object obj = null;
        String string = this.sharedPreferences.getString("controls_component", null);
        if (string == null || (componentNameUnflattenFromString = ComponentName.unflattenFromString(string)) == null) {
            componentNameUnflattenFromString = EMPTY_COMPONENT;
        }
        String string2 = this.sharedPreferences.getString("controls_structure", "");
        Iterator<T> it = list.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Object next = it.next();
            StructureInfo structureInfo = (StructureInfo) next;
            if (Intrinsics.areEqual(componentNameUnflattenFromString, structureInfo.getComponentName()) && Intrinsics.areEqual(string2, structureInfo.getStructure())) {
                obj = next;
                break;
            }
        }
        StructureInfo structureInfo2 = (StructureInfo) obj;
        return structureInfo2 != null ? structureInfo2 : list.get(0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updatePreferences(StructureInfo structureInfo) {
        if (Intrinsics.areEqual(structureInfo, EMPTY_STRUCTURE)) {
            return;
        }
        this.sharedPreferences.edit().putString("controls_component", structureInfo.getComponentName().flattenToString()).putString("controls_structure", structureInfo.getStructure().toString()).commit();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void switchAppOrStructure(SelectionItem selectionItem) {
        List<StructureInfo> list = this.allStructures;
        if (list == null) {
            Intrinsics.throwUninitializedPropertyAccessException("allStructures");
        }
        for (StructureInfo structureInfo : list) {
            if (Intrinsics.areEqual(structureInfo.getStructure(), selectionItem.getStructure()) && Intrinsics.areEqual(structureInfo.getComponentName(), selectionItem.getComponentName())) {
                if (!Intrinsics.areEqual(structureInfo, this.selectedStructure)) {
                    this.selectedStructure = structureInfo;
                    updatePreferences(structureInfo);
                    ViewGroup viewGroup = this.parent;
                    if (viewGroup == null) {
                        Intrinsics.throwUninitializedPropertyAccessException("parent");
                    }
                    reload(viewGroup);
                    return;
                }
                return;
            }
        }
        throw new NoSuchElementException("Collection contains no element matching the predicate.");
    }

    @Override // com.android.systemui.controls.ui.ControlsUiController
    public void closeDialogs(boolean z) {
        if (z) {
            ListPopupWindow listPopupWindow = this.popup;
            if (listPopupWindow != null) {
                listPopupWindow.dismissImmediate();
            }
        } else {
            ListPopupWindow listPopupWindow2 = this.popup;
            if (listPopupWindow2 != null) {
                listPopupWindow2.dismiss();
            }
        }
        this.popup = null;
        Iterator<Map.Entry<ControlKey, ControlViewHolder>> it = this.controlViewsById.entrySet().iterator();
        while (it.hasNext()) {
            it.next().getValue().dismiss();
        }
        this.controlActionCoordinator.closeDialogs();
    }

    @Override // com.android.systemui.controls.ui.ControlsUiController
    public void hide() {
        this.hidden = true;
        closeDialogs(true);
        this.controlsController.get().unsubscribe();
        ViewGroup viewGroup = this.parent;
        if (viewGroup == null) {
            Intrinsics.throwUninitializedPropertyAccessException("parent");
        }
        viewGroup.removeAllViews();
        this.controlsById.clear();
        this.controlViewsById.clear();
        ControlsListingController controlsListingController = this.controlsListingController.get();
        ControlsListingController.ControlsListingCallback controlsListingCallback = this.listingCallback;
        if (controlsListingCallback == null) {
            Intrinsics.throwUninitializedPropertyAccessException("listingCallback");
        }
        controlsListingController.removeCallback(controlsListingCallback);
        if (this.retainCache) {
            return;
        }
        RenderInfo.Companion.clearCache();
    }

    @Override // com.android.systemui.controls.ui.ControlsUiController
    public void onActionResponse(@NotNull ComponentName componentName, @NotNull String controlId, final int i) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(controlId, "controlId");
        final ControlKey controlKey = new ControlKey(componentName, controlId);
        this.uiExecutor.execute(new Runnable() { // from class: com.android.systemui.controls.ui.ControlsUiControllerImpl.onActionResponse.1
            @Override // java.lang.Runnable
            public final void run() throws Resources.NotFoundException {
                ControlViewHolder controlViewHolder = (ControlViewHolder) ControlsUiControllerImpl.this.controlViewsById.get(controlKey);
                if (controlViewHolder != null) {
                    controlViewHolder.actionResponse(i);
                }
            }
        });
    }

    private final ViewGroup createRow(LayoutInflater layoutInflater, ViewGroup viewGroup) {
        View viewInflate = layoutInflater.inflate(R.layout.controls_row, viewGroup, false);
        if (viewInflate == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup");
        }
        ViewGroup viewGroup2 = (ViewGroup) viewInflate;
        viewGroup.addView(viewGroup2);
        return viewGroup2;
    }

    /* JADX WARN: Type inference failed for: r3v1, types: [T, android.widget.ArrayAdapter, com.android.systemui.controls.ui.ItemAdapter] */
    private final void createDropDown(List<SelectionItem> list) {
        for (SelectionItem selectionItem : list) {
            RenderInfo.Companion.registerComponentIcon(selectionItem.getComponentName(), selectionItem.getIcon());
        }
        LinkedHashMap linkedHashMap = new LinkedHashMap(RangesKt___RangesKt.coerceAtLeast(MapsKt__MapsKt.mapCapacity(CollectionsKt__IterablesKt.collectionSizeOrDefault(list, 10)), 16));
        for (Object obj : list) {
            linkedHashMap.put(((SelectionItem) obj).getComponentName(), obj);
        }
        ArrayList arrayList = new ArrayList();
        List<StructureInfo> list2 = this.allStructures;
        if (list2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("allStructures");
        }
        Iterator<T> it = list2.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            StructureInfo structureInfo = (StructureInfo) it.next();
            SelectionItem selectionItem2 = (SelectionItem) linkedHashMap.get(structureInfo.getComponentName());
            SelectionItem selectionItemCopy$default = selectionItem2 != null ? SelectionItem.copy$default(selectionItem2, null, structureInfo.getStructure(), null, null, 13, null) : null;
            if (selectionItemCopy$default != null) {
                arrayList.add(selectionItemCopy$default);
            }
        }
        CollectionsKt__MutableCollectionsJVMKt.sortWith(arrayList, this.localeComparator);
        SelectionItem selectionItemFindSelectionItem = findSelectionItem(this.selectedStructure, arrayList);
        if (selectionItemFindSelectionItem == null) {
            selectionItemFindSelectionItem = list.get(0);
        }
        Ref$ObjectRef ref$ObjectRef = new Ref$ObjectRef();
        ?? itemAdapter = new ItemAdapter(this.context, R.layout.controls_spinner_item);
        itemAdapter.addAll(arrayList);
        ref$ObjectRef.element = itemAdapter;
        ViewGroup viewGroup = this.parent;
        if (viewGroup == null) {
            Intrinsics.throwUninitializedPropertyAccessException("parent");
        }
        TextView textView = (TextView) viewGroup.requireViewById(R.id.app_or_structure_spinner);
        textView.setText(selectionItemFindSelectionItem.getTitle());
        Drawable background = textView.getBackground();
        if (background == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.graphics.drawable.LayerDrawable");
        }
        Drawable drawable = ((LayerDrawable) background).getDrawable(0);
        Context context = textView.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context, "context");
        drawable.setTint(context.getResources().getColor(R.color.control_spinner_dropdown, null));
        if (arrayList.size() == 1) {
            textView.setBackground(null);
            return;
        }
        ViewGroup viewGroup2 = this.parent;
        if (viewGroup2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("parent");
        }
        ViewGroup viewGroup3 = (ViewGroup) viewGroup2.requireViewById(R.id.controls_header);
        viewGroup3.setOnClickListener(new AnonymousClass3(viewGroup3, ref$ObjectRef));
    }

    @Override // com.android.systemui.controls.ui.ControlsUiController
    public void onRefreshState(@NotNull final ComponentName componentName, @NotNull List<Control> controls) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(controls, "controls");
        for (final Control control : controls) {
            Map<ControlKey, ControlWithState> map = this.controlsById;
            String controlId = control.getControlId();
            Intrinsics.checkExpressionValueIsNotNull(controlId, "c.getControlId()");
            ControlWithState controlWithState = map.get(new ControlKey(componentName, controlId));
            if (controlWithState != null) {
                Log.d("ControlsUiController", "onRefreshState() for id: " + control.getControlId());
                CustomIconCache customIconCache = this.iconCache;
                String controlId2 = control.getControlId();
                Intrinsics.checkExpressionValueIsNotNull(controlId2, "c.controlId");
                customIconCache.store(componentName, controlId2, control.getCustomIcon());
                final ControlWithState controlWithState2 = new ControlWithState(componentName, controlWithState.getCi(), control);
                String controlId3 = control.getControlId();
                Intrinsics.checkExpressionValueIsNotNull(controlId3, "c.getControlId()");
                final ControlKey controlKey = new ControlKey(componentName, controlId3);
                this.controlsById.put(controlKey, controlWithState2);
                this.uiExecutor.execute(new Runnable() { // from class: com.android.systemui.controls.ui.ControlsUiControllerImpl$onRefreshState$$inlined$forEach$lambda$1
                    @Override // java.lang.Runnable
                    public final void run() {
                        ControlViewHolder controlViewHolder = (ControlViewHolder) this.controlViewsById.get(controlKey);
                        if (controlViewHolder != null) {
                            controlViewHolder.bindData(controlWithState2);
                        }
                    }
                });
            }
        }
    }
}
