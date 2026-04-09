package com.android.systemui.controls.management;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.systemui.R;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: StructureAdapter.kt */
/* loaded from: classes.dex */
public final class StructureAdapter extends RecyclerView.Adapter<StructureHolder> {
    private final int currentUserId;
    private final List<StructureContainer> models;

    public StructureAdapter(@NotNull List<StructureContainer> models, int i) {
        Intrinsics.checkParameterIsNotNull(models, "models");
        this.models = models;
        this.currentUserId = i;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    @NotNull
    public StructureHolder onCreateViewHolder(@NotNull ViewGroup parent, int i) {
        Intrinsics.checkParameterIsNotNull(parent, "parent");
        View viewInflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.controls_structure_page, parent, false);
        Intrinsics.checkExpressionValueIsNotNull(viewInflate, "layoutInflater.inflate(R…ture_page, parent, false)");
        return new StructureHolder(viewInflate, this.currentUserId);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.models.size();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(@NotNull StructureHolder holder, int i) {
        Intrinsics.checkParameterIsNotNull(holder, "holder");
        holder.bind(this.models.get(i).getModel());
    }

    /* compiled from: StructureAdapter.kt */
    public static final class StructureHolder extends RecyclerView.ViewHolder {
        private final ControlAdapter controlAdapter;
        private final RecyclerView recyclerView;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public StructureHolder(@NotNull View view, int i) throws Resources.NotFoundException {
            super(view);
            Intrinsics.checkParameterIsNotNull(view, "view");
            View viewRequireViewById = this.itemView.requireViewById(R.id.listAll);
            Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById, "itemView.requireViewById…cyclerView>(R.id.listAll)");
            this.recyclerView = (RecyclerView) viewRequireViewById;
            View itemView = this.itemView;
            Intrinsics.checkExpressionValueIsNotNull(itemView, "itemView");
            Context context = itemView.getContext();
            Intrinsics.checkExpressionValueIsNotNull(context, "itemView.context");
            this.controlAdapter = new ControlAdapter(context.getResources().getFloat(R.dimen.control_card_elevation), i);
            setUpRecyclerView();
        }

        public final void bind(@NotNull ControlsModel model) {
            Intrinsics.checkParameterIsNotNull(model, "model");
            this.controlAdapter.changeModel(model);
        }

        private final void setUpRecyclerView() throws Resources.NotFoundException {
            View itemView = this.itemView;
            Intrinsics.checkExpressionValueIsNotNull(itemView, "itemView");
            Context context = itemView.getContext();
            Intrinsics.checkExpressionValueIsNotNull(context, "itemView.context");
            int dimensionPixelSize = context.getResources().getDimensionPixelSize(R.dimen.controls_card_margin);
            MarginItemDecorator marginItemDecorator = new MarginItemDecorator(dimensionPixelSize, dimensionPixelSize);
            RecyclerView recyclerView = this.recyclerView;
            recyclerView.setAdapter(this.controlAdapter);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this.recyclerView.getContext(), 2);
            gridLayoutManager.setSpanSizeLookup(this.controlAdapter.getSpanSizeLookup());
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.addItemDecoration(marginItemDecorator);
        }
    }
}
