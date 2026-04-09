package com.android.systemui.controls.management;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.systemui.R;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controls.CustomIconCache;
import com.android.systemui.controls.controller.ControlInfo;
import com.android.systemui.controls.controller.ControlsControllerImpl;
import com.android.systemui.controls.controller.StructureInfo;
import com.android.systemui.controls.management.FavoritesModel;
import com.android.systemui.globalactions.GlobalActionsComponent;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.util.LifecycleActivity;
import java.util.List;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlsEditingActivity.kt */
/* loaded from: classes.dex */
public final class ControlsEditingActivity extends LifecycleActivity {
    private ComponentName component;
    private final ControlsControllerImpl controller;
    private final ControlsEditingActivity$currentUserTracker$1 currentUserTracker;
    private final CustomIconCache customIconCache;
    private final ControlsEditingActivity$favoritesModelCallback$1 favoritesModelCallback;
    private final GlobalActionsComponent globalActionsComponent;
    private FavoritesModel model;
    private View saveButton;
    private CharSequence structure;
    private TextView subtitle;
    public static final Companion Companion = new Companion(null);
    private static final int SUBTITLE_ID = R.string.controls_favorite_rearrange;
    private static final int EMPTY_TEXT_ID = R.string.controls_favorite_removed;

    public static final /* synthetic */ View access$getSaveButton$p(ControlsEditingActivity controlsEditingActivity) {
        View view = controlsEditingActivity.saveButton;
        if (view == null) {
            Intrinsics.throwUninitializedPropertyAccessException("saveButton");
        }
        return view;
    }

    public static final /* synthetic */ TextView access$getSubtitle$p(ControlsEditingActivity controlsEditingActivity) {
        TextView textView = controlsEditingActivity.subtitle;
        if (textView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("subtitle");
        }
        return textView;
    }

    /* JADX WARN: Type inference failed for: r2v1, types: [com.android.systemui.controls.management.ControlsEditingActivity$currentUserTracker$1] */
    /* JADX WARN: Type inference failed for: r2v2, types: [com.android.systemui.controls.management.ControlsEditingActivity$favoritesModelCallback$1] */
    public ControlsEditingActivity(@NotNull ControlsControllerImpl controller, @NotNull final BroadcastDispatcher broadcastDispatcher, @NotNull GlobalActionsComponent globalActionsComponent, @NotNull CustomIconCache customIconCache) {
        Intrinsics.checkParameterIsNotNull(controller, "controller");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(globalActionsComponent, "globalActionsComponent");
        Intrinsics.checkParameterIsNotNull(customIconCache, "customIconCache");
        this.controller = controller;
        this.globalActionsComponent = globalActionsComponent;
        this.customIconCache = customIconCache;
        this.currentUserTracker = new CurrentUserTracker(broadcastDispatcher) { // from class: com.android.systemui.controls.management.ControlsEditingActivity$currentUserTracker$1
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
        this.favoritesModelCallback = new FavoritesModel.FavoritesModelCallback() { // from class: com.android.systemui.controls.management.ControlsEditingActivity$favoritesModelCallback$1
            @Override // com.android.systemui.controls.management.FavoritesModel.FavoritesModelCallback
            public void onNoneChanged(boolean z) {
                if (z) {
                    ControlsEditingActivity.access$getSubtitle$p(this.this$0).setText(ControlsEditingActivity.EMPTY_TEXT_ID);
                } else {
                    ControlsEditingActivity.access$getSubtitle$p(this.this$0).setText(ControlsEditingActivity.SUBTITLE_ID);
                }
            }

            @Override // com.android.systemui.controls.management.ControlsModel.ControlsModelCallback
            public void onFirstChange() {
                ControlsEditingActivity.access$getSaveButton$p(this.this$0).setEnabled(true);
            }
        };
    }

    /* compiled from: ControlsEditingActivity.kt */
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
        ComponentName componentName = (ComponentName) getIntent().getParcelableExtra("android.intent.extra.COMPONENT_NAME");
        if (componentName != null) {
            this.component = componentName;
        } else {
            finish();
        }
        CharSequence charSequenceExtra = getIntent().getCharSequenceExtra("extra_structure");
        if (charSequenceExtra != null) {
            this.structure = charSequenceExtra;
        } else {
            finish();
        }
        bindViews();
        bindButtons();
    }

    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    protected void onStart() throws Resources.NotFoundException {
        super.onStart();
        setUpList();
        startTracking();
    }

    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    protected void onStop() {
        super.onStop();
        stopTracking();
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        this.globalActionsComponent.handleShowGlobalActionsMenu();
        animateExitAndFinish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void animateExitAndFinish() {
        ViewGroup rootView = (ViewGroup) requireViewById(R.id.controls_management_root);
        Intrinsics.checkExpressionValueIsNotNull(rootView, "rootView");
        ControlsAnimations.exitAnimation(rootView, new Runnable() { // from class: com.android.systemui.controls.management.ControlsEditingActivity.animateExitAndFinish.1
            @Override // java.lang.Runnable
            public void run() {
                ControlsEditingActivity.this.finish();
            }
        }).start();
    }

    private final void bindViews() {
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
        viewStub.setLayoutResource(R.layout.controls_management_editing);
        viewStub.inflate();
        View viewRequireViewById2 = requireViewById(R.id.title);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById2, "requireViewById<TextView>(R.id.title)");
        TextView textView = (TextView) viewRequireViewById2;
        CharSequence charSequence = this.structure;
        if (charSequence == null) {
            Intrinsics.throwUninitializedPropertyAccessException("structure");
        }
        textView.setText(charSequence);
        CharSequence charSequence2 = this.structure;
        if (charSequence2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("structure");
        }
        setTitle(charSequence2);
        View viewRequireViewById3 = requireViewById(R.id.subtitle);
        TextView textView2 = (TextView) viewRequireViewById3;
        textView2.setText(SUBTITLE_ID);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById3, "requireViewById<TextView…xt(SUBTITLE_ID)\n        }");
        this.subtitle = textView2;
    }

    private final void bindButtons() {
        View viewRequireViewById = requireViewById(R.id.done);
        Button button = (Button) viewRequireViewById;
        button.setEnabled(false);
        button.setText(R.string.save);
        button.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.controls.management.ControlsEditingActivity$bindButtons$$inlined$apply$lambda$1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.this$0.saveFavorites();
                this.this$0.animateExitAndFinish();
                this.this$0.globalActionsComponent.handleShowGlobalActionsMenu();
            }
        });
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById, "requireViewById<Button>(…)\n            }\n        }");
        this.saveButton = viewRequireViewById;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void saveFavorites() {
        ControlsControllerImpl controlsControllerImpl = this.controller;
        ComponentName componentName = this.component;
        if (componentName == null) {
            Intrinsics.throwUninitializedPropertyAccessException("component");
        }
        CharSequence charSequence = this.structure;
        if (charSequence == null) {
            Intrinsics.throwUninitializedPropertyAccessException("structure");
        }
        FavoritesModel favoritesModel = this.model;
        if (favoritesModel == null) {
            Intrinsics.throwUninitializedPropertyAccessException("model");
        }
        controlsControllerImpl.replaceFavoritesForStructure(new StructureInfo(componentName, charSequence, favoritesModel.getFavorites()));
    }

    private final void setUpList() throws Resources.NotFoundException {
        ControlsControllerImpl controlsControllerImpl = this.controller;
        ComponentName componentName = this.component;
        if (componentName == null) {
            Intrinsics.throwUninitializedPropertyAccessException("component");
        }
        CharSequence charSequence = this.structure;
        if (charSequence == null) {
            Intrinsics.throwUninitializedPropertyAccessException("structure");
        }
        List<ControlInfo> favoritesForStructure = controlsControllerImpl.getFavoritesForStructure(componentName, charSequence);
        CustomIconCache customIconCache = this.customIconCache;
        ComponentName componentName2 = this.component;
        if (componentName2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("component");
        }
        this.model = new FavoritesModel(customIconCache, componentName2, favoritesForStructure, this.favoritesModelCallback);
        float f = getResources().getFloat(R.dimen.control_card_elevation);
        final RecyclerView recyclerView = (RecyclerView) requireViewById(R.id.list);
        Intrinsics.checkExpressionValueIsNotNull(recyclerView, "recyclerView");
        recyclerView.setAlpha(0.0f);
        final ControlAdapter controlAdapter = new ControlAdapter(f, getCurrentUserId());
        controlAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() { // from class: com.android.systemui.controls.management.ControlsEditingActivity$setUpList$$inlined$apply$lambda$1
            private boolean hasAnimated;

            @Override // androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
            public void onChanged() {
                if (this.hasAnimated) {
                    return;
                }
                this.hasAnimated = true;
                ControlsAnimations controlsAnimations = ControlsAnimations.INSTANCE;
                RecyclerView recyclerView2 = recyclerView;
                Intrinsics.checkExpressionValueIsNotNull(recyclerView2, "recyclerView");
                controlsAnimations.enterAnimation(recyclerView2).start();
            }
        });
        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.controls_card_margin);
        final MarginItemDecorator marginItemDecorator = new MarginItemDecorator(dimensionPixelSize, dimensionPixelSize);
        recyclerView.setAdapter(controlAdapter);
        final Context context = recyclerView.getContext();
        final int i = 2;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, i) { // from class: com.android.systemui.controls.management.ControlsEditingActivity$setUpList$$inlined$apply$lambda$2
            @Override // androidx.recyclerview.widget.GridLayoutManager, androidx.recyclerview.widget.RecyclerView.LayoutManager
            public int getRowCountForAccessibility(@NotNull RecyclerView.Recycler recycler, @NotNull RecyclerView.State state) {
                Intrinsics.checkParameterIsNotNull(recycler, "recycler");
                Intrinsics.checkParameterIsNotNull(state, "state");
                int rowCountForAccessibility = super.getRowCountForAccessibility(recycler, state);
                return rowCountForAccessibility > 0 ? rowCountForAccessibility - 1 : rowCountForAccessibility;
            }
        };
        gridLayoutManager.setSpanSizeLookup(controlAdapter.getSpanSizeLookup());
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.addItemDecoration(marginItemDecorator);
        FavoritesModel favoritesModel = this.model;
        if (favoritesModel == null) {
            Intrinsics.throwUninitializedPropertyAccessException("model");
        }
        controlAdapter.changeModel(favoritesModel);
        FavoritesModel favoritesModel2 = this.model;
        if (favoritesModel2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("model");
        }
        favoritesModel2.attachAdapter(controlAdapter);
        FavoritesModel favoritesModel3 = this.model;
        if (favoritesModel3 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("model");
        }
        new ItemTouchHelper(favoritesModel3.getItemTouchHelperCallback()).attachToRecyclerView(recyclerView);
    }

    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    protected void onDestroy() {
        stopTracking();
        super.onDestroy();
    }
}
