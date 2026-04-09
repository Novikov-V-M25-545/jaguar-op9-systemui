package com.android.systemui.controls.management;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.android.systemui.qs.PageIndicator;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ManagementPageIndicator.kt */
/* loaded from: classes.dex */
public final class ManagementPageIndicator extends PageIndicator {

    @NotNull
    private Function1<? super Integer, Unit> visibilityListener;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public ManagementPageIndicator(@NotNull Context context, @NotNull AttributeSet attrs) {
        super(context, attrs);
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(attrs, "attrs");
        this.visibilityListener = new Function1<Integer, Unit>() { // from class: com.android.systemui.controls.management.ManagementPageIndicator$visibilityListener$1
            public final void invoke(int i) {
            }

            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Unit invoke(Integer num) {
                invoke(num.intValue());
                return Unit.INSTANCE;
            }
        };
    }

    @Override // com.android.systemui.qs.PageIndicator
    public void setLocation(float f) {
        if (getLayoutDirection() == 1) {
            super.setLocation((getChildCount() - 1) - f);
        } else {
            super.setLocation(f);
        }
    }

    public final void setVisibilityListener(@NotNull Function1<? super Integer, Unit> function1) {
        Intrinsics.checkParameterIsNotNull(function1, "<set-?>");
        this.visibilityListener = function1;
    }

    @Override // android.view.View
    protected void onVisibilityChanged(@NotNull View changedView, int i) {
        Intrinsics.checkParameterIsNotNull(changedView, "changedView");
        super.onVisibilityChanged(changedView, i);
        if (Intrinsics.areEqual(changedView, this)) {
            this.visibilityListener.invoke(Integer.valueOf(i));
        }
    }
}
