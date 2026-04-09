package com.android.systemui.controls.management;

import android.content.ComponentName;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import com.android.systemui.R;
import com.android.systemui.controls.ControlsServiceInfo;
import java.util.List;
import java.util.concurrent.Executor;
import kotlin.Unit;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: AppAdapter.kt */
/* loaded from: classes.dex */
public final class AppAdapter extends RecyclerView.Adapter<Holder> {
    private final AppAdapter$callback$1 callback;
    private final FavoritesRenderer favoritesRenderer;
    private final LayoutInflater layoutInflater;
    private List<ControlsServiceInfo> listOfServices;
    private final Function1<ComponentName, Unit> onAppSelected;
    private final Resources resources;

    /* JADX WARN: Multi-variable type inference failed */
    public AppAdapter(@NotNull Executor backgroundExecutor, @NotNull Executor uiExecutor, @NotNull Lifecycle lifecycle, @NotNull ControlsListingController controlsListingController, @NotNull LayoutInflater layoutInflater, @NotNull Function1<? super ComponentName, Unit> onAppSelected, @NotNull FavoritesRenderer favoritesRenderer, @NotNull Resources resources) {
        Intrinsics.checkParameterIsNotNull(backgroundExecutor, "backgroundExecutor");
        Intrinsics.checkParameterIsNotNull(uiExecutor, "uiExecutor");
        Intrinsics.checkParameterIsNotNull(lifecycle, "lifecycle");
        Intrinsics.checkParameterIsNotNull(controlsListingController, "controlsListingController");
        Intrinsics.checkParameterIsNotNull(layoutInflater, "layoutInflater");
        Intrinsics.checkParameterIsNotNull(onAppSelected, "onAppSelected");
        Intrinsics.checkParameterIsNotNull(favoritesRenderer, "favoritesRenderer");
        Intrinsics.checkParameterIsNotNull(resources, "resources");
        this.layoutInflater = layoutInflater;
        this.onAppSelected = onAppSelected;
        this.favoritesRenderer = favoritesRenderer;
        this.resources = resources;
        this.listOfServices = CollectionsKt__CollectionsKt.emptyList();
        AppAdapter$callback$1 appAdapter$callback$1 = new AppAdapter$callback$1(this, backgroundExecutor, uiExecutor);
        this.callback = appAdapter$callback$1;
        controlsListingController.observe(lifecycle, (Lifecycle) appAdapter$callback$1);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    @NotNull
    public Holder onCreateViewHolder(@NotNull ViewGroup parent, int i) {
        Intrinsics.checkParameterIsNotNull(parent, "parent");
        View viewInflate = this.layoutInflater.inflate(R.layout.controls_app_item, parent, false);
        Intrinsics.checkExpressionValueIsNotNull(viewInflate, "layoutInflater.inflate(R…_app_item, parent, false)");
        return new Holder(viewInflate, this.favoritesRenderer);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.listOfServices.size();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(@NotNull Holder holder, final int i) {
        Intrinsics.checkParameterIsNotNull(holder, "holder");
        holder.bindData(this.listOfServices.get(i));
        holder.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.controls.management.AppAdapter.onBindViewHolder.1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                AppAdapter.this.onAppSelected.invoke(ComponentName.unflattenFromString(((ControlsServiceInfo) AppAdapter.this.listOfServices.get(i)).getKey()));
            }
        });
    }

    /* compiled from: AppAdapter.kt */
    public static final class Holder extends RecyclerView.ViewHolder {

        @NotNull
        private final FavoritesRenderer favRenderer;
        private final TextView favorites;
        private final ImageView icon;
        private final TextView title;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public Holder(@NotNull View view, @NotNull FavoritesRenderer favRenderer) {
            super(view);
            Intrinsics.checkParameterIsNotNull(view, "view");
            Intrinsics.checkParameterIsNotNull(favRenderer, "favRenderer");
            this.favRenderer = favRenderer;
            View viewRequireViewById = this.itemView.requireViewById(android.R.id.icon);
            Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById, "itemView.requireViewById…droid.internal.R.id.icon)");
            this.icon = (ImageView) viewRequireViewById;
            View viewRequireViewById2 = this.itemView.requireViewById(android.R.id.title);
            Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById2, "itemView.requireViewById…roid.internal.R.id.title)");
            this.title = (TextView) viewRequireViewById2;
            View viewRequireViewById3 = this.itemView.requireViewById(R.id.favorites);
            Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById3, "itemView.requireViewById(R.id.favorites)");
            this.favorites = (TextView) viewRequireViewById3;
        }

        public final void bindData(@NotNull ControlsServiceInfo data) {
            Intrinsics.checkParameterIsNotNull(data, "data");
            this.icon.setImageDrawable(data.loadIcon());
            this.title.setText(data.loadLabel());
            FavoritesRenderer favoritesRenderer = this.favRenderer;
            ComponentName componentName = data.componentName;
            Intrinsics.checkExpressionValueIsNotNull(componentName, "data.componentName");
            String strRenderFavoritesForComponent = favoritesRenderer.renderFavoritesForComponent(componentName);
            this.favorites.setText(strRenderFavoritesForComponent);
            this.favorites.setVisibility(strRenderFavoritesForComponent == null ? 8 : 0);
        }
    }
}
