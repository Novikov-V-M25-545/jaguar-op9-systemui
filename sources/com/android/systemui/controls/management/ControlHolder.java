package com.android.systemui.controls.management;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.view.ViewCompat;
import com.android.systemui.R;
import com.android.systemui.controls.ControlInterface;
import com.android.systemui.controls.management.ControlsModel;
import com.android.systemui.controls.ui.CanUseIconPredicate;
import com.android.systemui.controls.ui.RenderInfo;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlAdapter.kt */
/* loaded from: classes.dex */
public final class ControlHolder extends Holder {
    private final ControlHolderAccessibilityDelegate accessibilityDelegate;
    private final CanUseIconPredicate canUseIconPredicate;
    private final CheckBox favorite;

    @NotNull
    private final Function2<String, Boolean, Unit> favoriteCallback;
    private final String favoriteStateDescription;
    private final ImageView icon;

    @Nullable
    private final ControlsModel.MoveHelper moveHelper;
    private final String notFavoriteStateDescription;
    private final TextView removed;
    private final TextView subtitle;
    private final TextView title;

    @NotNull
    public final Function2<String, Boolean, Unit> getFavoriteCallback() {
        return this.favoriteCallback;
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    /* JADX WARN: Multi-variable type inference failed */
    public ControlHolder(@NotNull View view, int i, @Nullable ControlsModel.MoveHelper moveHelper, @NotNull Function2<? super String, ? super Boolean, Unit> favoriteCallback) {
        super(view, null);
        Intrinsics.checkParameterIsNotNull(view, "view");
        Intrinsics.checkParameterIsNotNull(favoriteCallback, "favoriteCallback");
        this.moveHelper = moveHelper;
        this.favoriteCallback = favoriteCallback;
        View itemView = this.itemView;
        Intrinsics.checkExpressionValueIsNotNull(itemView, "itemView");
        this.favoriteStateDescription = itemView.getContext().getString(R.string.accessibility_control_favorite);
        View itemView2 = this.itemView;
        Intrinsics.checkExpressionValueIsNotNull(itemView2, "itemView");
        this.notFavoriteStateDescription = itemView2.getContext().getString(R.string.accessibility_control_not_favorite);
        View viewRequireViewById = this.itemView.requireViewById(R.id.icon);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById, "itemView.requireViewById(R.id.icon)");
        this.icon = (ImageView) viewRequireViewById;
        View viewRequireViewById2 = this.itemView.requireViewById(R.id.title);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById2, "itemView.requireViewById(R.id.title)");
        this.title = (TextView) viewRequireViewById2;
        View viewRequireViewById3 = this.itemView.requireViewById(R.id.subtitle);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById3, "itemView.requireViewById(R.id.subtitle)");
        this.subtitle = (TextView) viewRequireViewById3;
        View viewRequireViewById4 = this.itemView.requireViewById(R.id.status);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById4, "itemView.requireViewById(R.id.status)");
        this.removed = (TextView) viewRequireViewById4;
        View viewRequireViewById5 = this.itemView.requireViewById(R.id.favorite);
        CheckBox checkBox = (CheckBox) viewRequireViewById5;
        checkBox.setVisibility(0);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById5, "itemView.requireViewById…lity = View.VISIBLE\n    }");
        this.favorite = checkBox;
        this.canUseIconPredicate = new CanUseIconPredicate(i);
        ControlHolderAccessibilityDelegate controlHolderAccessibilityDelegate = new ControlHolderAccessibilityDelegate(new ControlHolder$accessibilityDelegate$1(this), new ControlHolder$accessibilityDelegate$2(this), moveHelper);
        this.accessibilityDelegate = controlHolderAccessibilityDelegate;
        ViewCompat.setAccessibilityDelegate(this.itemView, controlHolderAccessibilityDelegate);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final CharSequence stateDescription(boolean z) {
        if (!z) {
            return this.notFavoriteStateDescription;
        }
        if (this.moveHelper == null) {
            return this.favoriteStateDescription;
        }
        int layoutPosition = getLayoutPosition() + 1;
        View itemView = this.itemView;
        Intrinsics.checkExpressionValueIsNotNull(itemView, "itemView");
        return itemView.getContext().getString(R.string.accessibility_control_favorite_position, Integer.valueOf(layoutPosition));
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.android.systemui.controls.management.Holder
    public void bindData(@NotNull final ElementWrapper wrapper) throws Resources.NotFoundException {
        CharSequence text;
        Intrinsics.checkParameterIsNotNull(wrapper, "wrapper");
        ControlInterface controlInterface = (ControlInterface) wrapper;
        RenderInfo renderInfo = getRenderInfo(controlInterface.getComponent(), controlInterface.getDeviceType());
        this.title.setText(controlInterface.getTitle());
        this.subtitle.setText(controlInterface.getSubtitle());
        updateFavorite(controlInterface.getFavorite());
        TextView textView = this.removed;
        if (controlInterface.getRemoved()) {
            View itemView = this.itemView;
            Intrinsics.checkExpressionValueIsNotNull(itemView, "itemView");
            text = itemView.getContext().getText(R.string.controls_removed);
        } else {
            text = "";
        }
        textView.setText(text);
        this.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.controls.management.ControlHolder.bindData.1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ControlHolder.this.updateFavorite(!r2.favorite.isChecked());
                ControlHolder.this.getFavoriteCallback().invoke(((ControlInterface) wrapper).getControlId(), Boolean.valueOf(ControlHolder.this.favorite.isChecked()));
            }
        });
        applyRenderInfo(renderInfo, controlInterface);
    }

    @Override // com.android.systemui.controls.management.Holder
    public void updateFavorite(boolean z) {
        this.favorite.setChecked(z);
        this.accessibilityDelegate.setFavorite(z);
        View itemView = this.itemView;
        Intrinsics.checkExpressionValueIsNotNull(itemView, "itemView");
        itemView.setStateDescription(stateDescription(z));
    }

    private final RenderInfo getRenderInfo(ComponentName componentName, int i) {
        RenderInfo.Companion companion = RenderInfo.Companion;
        View itemView = this.itemView;
        Intrinsics.checkExpressionValueIsNotNull(itemView, "itemView");
        Context context = itemView.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context, "itemView.context");
        return RenderInfo.Companion.lookup$default(companion, context, componentName, i, 0, 8, null);
    }

    private final void applyRenderInfo(RenderInfo renderInfo, ControlInterface controlInterface) throws Resources.NotFoundException {
        View itemView = this.itemView;
        Intrinsics.checkExpressionValueIsNotNull(itemView, "itemView");
        Context context = itemView.getContext();
        ColorStateList colorStateList = context.getResources().getColorStateList(renderInfo.getForeground(), context.getTheme());
        this.icon.setImageTintList(null);
        Icon customIcon = controlInterface.getCustomIcon();
        if (customIcon != null) {
            Icon icon = this.canUseIconPredicate.invoke((CanUseIconPredicate) customIcon).booleanValue() ? customIcon : null;
            if (icon != null) {
                this.icon.setImageIcon(icon);
                return;
            }
        }
        this.icon.setImageDrawable(renderInfo.getIcon());
        if (controlInterface.getDeviceType() != 52) {
            this.icon.setImageTintList(colorStateList);
        }
    }
}
