package com.android.systemui.privacy;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.systemui.R;
import java.util.List;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: OngoingPrivacyChip.kt */
/* loaded from: classes.dex */
public final class OngoingPrivacyChip extends FrameLayout {
    private FrameLayout back;
    private final Drawable backgroundDrawable;

    @NotNull
    private PrivacyChipBuilder builder;
    private boolean expanded;
    private final int iconColor;
    private final int iconMarginCollapsed;
    private final int iconMarginExpanded;
    private final int iconSize;
    private LinearLayout iconsContainer;

    @NotNull
    private List<PrivacyItem> privacyList;
    private final int sidePadding;

    public OngoingPrivacyChip(@NotNull Context context) {
        this(context, null, 0, 0, 14, null);
    }

    public OngoingPrivacyChip(@NotNull Context context, @Nullable AttributeSet attributeSet) {
        this(context, attributeSet, 0, 0, 12, null);
    }

    public OngoingPrivacyChip(@NotNull Context context, @Nullable AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0, 8, null);
    }

    public /* synthetic */ OngoingPrivacyChip(Context context, AttributeSet attributeSet, int i, int i2, int i3, DefaultConstructorMarker defaultConstructorMarker) {
        this(context, (i3 & 2) != 0 ? null : attributeSet, (i3 & 4) != 0 ? 0 : i, (i3 & 8) != 0 ? 0 : i2);
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public OngoingPrivacyChip(@NotNull Context context, @Nullable AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        Intrinsics.checkParameterIsNotNull(context, "context");
        this.iconMarginExpanded = context.getResources().getDimensionPixelSize(R.dimen.ongoing_appops_chip_icon_margin_expanded);
        this.iconMarginCollapsed = context.getResources().getDimensionPixelSize(R.dimen.ongoing_appops_chip_icon_margin_collapsed);
        this.iconSize = context.getResources().getDimensionPixelSize(R.dimen.ongoing_appops_chip_icon_size);
        this.iconColor = context.getResources().getColor(R.color.status_bar_clock_color, context.getTheme());
        this.sidePadding = context.getResources().getDimensionPixelSize(R.dimen.ongoing_appops_chip_side_padding);
        this.backgroundDrawable = context.getDrawable(R.drawable.privacy_chip_bg);
        this.builder = new PrivacyChipBuilder(context, CollectionsKt__CollectionsKt.emptyList());
        this.privacyList = CollectionsKt__CollectionsKt.emptyList();
    }

    public final boolean getExpanded() {
        return this.expanded;
    }

    public final void setExpanded(boolean z) {
        if (z != this.expanded) {
            this.expanded = z;
            updateView();
        }
    }

    @NotNull
    public final PrivacyChipBuilder getBuilder() {
        return this.builder;
    }

    @NotNull
    public final List<PrivacyItem> getPrivacyList() {
        return this.privacyList;
    }

    public final void setPrivacyList(@NotNull List<PrivacyItem> value) {
        Intrinsics.checkParameterIsNotNull(value, "value");
        this.privacyList = value;
        Context context = getContext();
        Intrinsics.checkExpressionValueIsNotNull(context, "context");
        this.builder = new PrivacyChipBuilder(context, value);
        updateView();
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        View viewRequireViewById = requireViewById(R.id.background);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById, "requireViewById(R.id.background)");
        this.back = (FrameLayout) viewRequireViewById;
        View viewRequireViewById2 = requireViewById(R.id.icons_container);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById2, "requireViewById(R.id.icons_container)");
        this.iconsContainer = (LinearLayout) viewRequireViewById2;
    }

    private final void updateView() {
        FrameLayout frameLayout = this.back;
        if (frameLayout == null) {
            Intrinsics.throwUninitializedPropertyAccessException("back");
        }
        frameLayout.setBackground(this.expanded ? this.backgroundDrawable : null);
        int i = this.expanded ? this.sidePadding : 0;
        FrameLayout frameLayout2 = this.back;
        if (frameLayout2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("back");
        }
        frameLayout2.setPaddingRelative(i, 0, i, 0);
        Function2<PrivacyChipBuilder, ViewGroup, Unit> function2 = new Function2<PrivacyChipBuilder, ViewGroup, Unit>() { // from class: com.android.systemui.privacy.OngoingPrivacyChip.updateView.1
            {
                super(2);
            }

            @Override // kotlin.jvm.functions.Function2
            public /* bridge */ /* synthetic */ Unit invoke(PrivacyChipBuilder privacyChipBuilder, ViewGroup viewGroup) {
                invoke2(privacyChipBuilder, viewGroup);
                return Unit.INSTANCE;
            }

            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final void invoke2(@NotNull PrivacyChipBuilder chipBuilder, @NotNull ViewGroup iconsContainer) {
                Intrinsics.checkParameterIsNotNull(chipBuilder, "chipBuilder");
                Intrinsics.checkParameterIsNotNull(iconsContainer, "iconsContainer");
                iconsContainer.removeAllViews();
                int i2 = 0;
                for (Object obj : chipBuilder.generateIcons()) {
                    int i3 = i2 + 1;
                    if (i2 < 0) {
                        CollectionsKt__CollectionsKt.throwIndexOverflow();
                    }
                    Drawable drawable = (Drawable) obj;
                    drawable.mutate();
                    drawable.setTint(OngoingPrivacyChip.this.iconColor);
                    ImageView imageView = new ImageView(OngoingPrivacyChip.this.getContext());
                    imageView.setImageDrawable(drawable);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    iconsContainer.addView(imageView, OngoingPrivacyChip.this.iconSize, OngoingPrivacyChip.this.iconSize);
                    if (i2 != 0) {
                        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                        if (layoutParams == null) {
                            throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup.MarginLayoutParams");
                        }
                        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
                        marginLayoutParams.setMarginStart(OngoingPrivacyChip.this.getExpanded() ? OngoingPrivacyChip.this.iconMarginExpanded : OngoingPrivacyChip.this.iconMarginCollapsed);
                        imageView.setLayoutParams(marginLayoutParams);
                    }
                    i2 = i3;
                }
            }
        };
        if (!this.privacyList.isEmpty()) {
            generateContentDescription();
            PrivacyChipBuilder privacyChipBuilder = this.builder;
            LinearLayout linearLayout = this.iconsContainer;
            if (linearLayout == null) {
                Intrinsics.throwUninitializedPropertyAccessException("iconsContainer");
            }
            function2.invoke2(privacyChipBuilder, (ViewGroup) linearLayout);
            LinearLayout linearLayout2 = this.iconsContainer;
            if (linearLayout2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("iconsContainer");
            }
            ViewGroup.LayoutParams layoutParams = linearLayout2.getLayoutParams();
            if (layoutParams == null) {
                throw new TypeCastException("null cannot be cast to non-null type android.widget.FrameLayout.LayoutParams");
            }
            FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) layoutParams;
            layoutParams2.gravity = (this.expanded ? 1 : 8388613) | 16;
            LinearLayout linearLayout3 = this.iconsContainer;
            if (linearLayout3 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("iconsContainer");
            }
            linearLayout3.setLayoutParams(layoutParams2);
        } else {
            LinearLayout linearLayout4 = this.iconsContainer;
            if (linearLayout4 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("iconsContainer");
            }
            linearLayout4.removeAllViews();
        }
        requestLayout();
    }

    private final void generateContentDescription() {
        setContentDescription(getContext().getString(R.string.ongoing_privacy_chip_content_multiple_apps, this.builder.joinTypes()));
    }
}
