package com.android.systemui.controls.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.R;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlsUiControllerImpl.kt */
/* loaded from: classes.dex */
final class ItemAdapter extends ArrayAdapter<SelectionItem> {
    private final LayoutInflater layoutInflater;

    @NotNull
    private final Context parentContext;
    private final int resource;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public ItemAdapter(@NotNull Context parentContext, int i) {
        super(parentContext, i);
        Intrinsics.checkParameterIsNotNull(parentContext, "parentContext");
        this.parentContext = parentContext;
        this.resource = i;
        this.layoutInflater = LayoutInflater.from(getContext());
    }

    @Override // android.widget.ArrayAdapter, android.widget.Adapter
    @NotNull
    public View getView(int i, @Nullable View view, @NotNull ViewGroup parent) {
        Intrinsics.checkParameterIsNotNull(parent, "parent");
        SelectionItem item = getItem(i);
        if (view == null) {
            view = this.layoutInflater.inflate(this.resource, parent, false);
        }
        ((TextView) view.requireViewById(R.id.controls_spinner_item)).setText(item.getTitle());
        ((ImageView) view.requireViewById(R.id.app_icon)).setImageDrawable(item.getIcon());
        Intrinsics.checkExpressionValueIsNotNull(view, "view");
        return view;
    }
}
