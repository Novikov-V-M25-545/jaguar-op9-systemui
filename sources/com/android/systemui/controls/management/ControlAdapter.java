package com.android.systemui.controls.management;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.systemui.R;
import com.android.systemui.controls.ControlInterface;
import java.util.List;
import kotlin.NoWhenBranchMatchedException;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlAdapter.kt */
/* loaded from: classes.dex */
public final class ControlAdapter extends RecyclerView.Adapter<Holder> {
    public static final Companion Companion = new Companion(null);
    private final int currentUserId;
    private final float elevation;
    private ControlsModel model;

    @NotNull
    private final GridLayoutManager.SpanSizeLookup spanSizeLookup = new GridLayoutManager.SpanSizeLookup() { // from class: com.android.systemui.controls.management.ControlAdapter$spanSizeLookup$1
        @Override // androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
        public int getSpanSize(int i) {
            return this.this$0.getItemViewType(i) != 1 ? 2 : 1;
        }
    };

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public /* bridge */ /* synthetic */ void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i, List list) {
        onBindViewHolder((Holder) viewHolder, i, (List<Object>) list);
    }

    public ControlAdapter(float f, int i) {
        this.elevation = f;
        this.currentUserId = i;
    }

    /* compiled from: ControlAdapter.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    @NotNull
    public final GridLayoutManager.SpanSizeLookup getSpanSizeLookup() {
        return this.spanSizeLookup;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    @NotNull
    public Holder onCreateViewHolder(@NotNull ViewGroup parent, int i) {
        Intrinsics.checkParameterIsNotNull(parent, "parent");
        LayoutInflater layoutInflaterFrom = LayoutInflater.from(parent.getContext());
        if (i == 0) {
            View viewInflate = layoutInflaterFrom.inflate(R.layout.controls_zone_header, parent, false);
            Intrinsics.checkExpressionValueIsNotNull(viewInflate, "layoutInflater.inflate(R…ne_header, parent, false)");
            return new ZoneHolder(viewInflate);
        }
        if (i != 1) {
            if (i == 2) {
                View viewInflate2 = layoutInflaterFrom.inflate(R.layout.controls_horizontal_divider_with_empty, parent, false);
                Intrinsics.checkExpressionValueIsNotNull(viewInflate2, "layoutInflater.inflate(\n…ith_empty, parent, false)");
                return new DividerHolder(viewInflate2);
            }
            throw new IllegalStateException("Wrong viewType: " + i);
        }
        View viewInflate3 = layoutInflaterFrom.inflate(R.layout.controls_base_item, parent, false);
        ViewGroup.LayoutParams layoutParams = viewInflate3.getLayoutParams();
        if (layoutParams == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup.MarginLayoutParams");
        }
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
        marginLayoutParams.width = -1;
        marginLayoutParams.topMargin = 0;
        marginLayoutParams.bottomMargin = 0;
        marginLayoutParams.leftMargin = 0;
        marginLayoutParams.rightMargin = 0;
        viewInflate3.setElevation(this.elevation);
        viewInflate3.setBackground(parent.getContext().getDrawable(R.drawable.control_background_ripple));
        Intrinsics.checkExpressionValueIsNotNull(viewInflate3, "layoutInflater.inflate(R…le)\n                    }");
        int i2 = this.currentUserId;
        ControlsModel controlsModel = this.model;
        return new ControlHolder(viewInflate3, i2, controlsModel != null ? controlsModel.getMoveHelper() : null, new Function2<String, Boolean, Unit>() { // from class: com.android.systemui.controls.management.ControlAdapter.onCreateViewHolder.2
            {
                super(2);
            }

            @Override // kotlin.jvm.functions.Function2
            public /* bridge */ /* synthetic */ Unit invoke(String str, Boolean bool) {
                invoke(str, bool.booleanValue());
                return Unit.INSTANCE;
            }

            public final void invoke(@NotNull String id, boolean z) {
                Intrinsics.checkParameterIsNotNull(id, "id");
                ControlsModel controlsModel2 = ControlAdapter.this.model;
                if (controlsModel2 != null) {
                    controlsModel2.changeFavoriteStatus(id, z);
                }
            }
        });
    }

    public final void changeModel(@NotNull ControlsModel model) {
        Intrinsics.checkParameterIsNotNull(model, "model");
        this.model = model;
        notifyDataSetChanged();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        List<ElementWrapper> elements;
        ControlsModel controlsModel = this.model;
        if (controlsModel == null || (elements = controlsModel.getElements()) == null) {
            return 0;
        }
        return elements.size();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(@NotNull Holder holder, int i) {
        Intrinsics.checkParameterIsNotNull(holder, "holder");
        ControlsModel controlsModel = this.model;
        if (controlsModel != null) {
            holder.bindData(controlsModel.getElements().get(i));
        }
    }

    public void onBindViewHolder(@NotNull Holder holder, int i, @NotNull List<Object> payloads) {
        Intrinsics.checkParameterIsNotNull(holder, "holder");
        Intrinsics.checkParameterIsNotNull(payloads, "payloads");
        if (payloads.isEmpty()) {
            super.onBindViewHolder((ControlAdapter) holder, i, payloads);
            return;
        }
        ControlsModel controlsModel = this.model;
        if (controlsModel != null) {
            Object obj = (ElementWrapper) controlsModel.getElements().get(i);
            if (obj instanceof ControlInterface) {
                holder.updateFavorite(((ControlInterface) obj).getFavorite());
            }
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemViewType(int i) {
        ControlsModel controlsModel = this.model;
        if (controlsModel != null) {
            ElementWrapper elementWrapper = controlsModel.getElements().get(i);
            if (elementWrapper instanceof ZoneNameWrapper) {
                return 0;
            }
            if ((elementWrapper instanceof ControlStatusWrapper) || (elementWrapper instanceof ControlInfoWrapper)) {
                return 1;
            }
            if (elementWrapper instanceof DividerWrapper) {
                return 2;
            }
            throw new NoWhenBranchMatchedException();
        }
        throw new IllegalStateException("Getting item type for null model");
    }
}
