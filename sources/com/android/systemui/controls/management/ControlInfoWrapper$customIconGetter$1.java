package com.android.systemui.controls.management;

import android.content.ComponentName;
import android.graphics.drawable.Icon;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlsModel.kt */
/* loaded from: classes.dex */
final /* synthetic */ class ControlInfoWrapper$customIconGetter$1 extends FunctionReference implements Function2<ComponentName, String, Icon> {
    public static final ControlInfoWrapper$customIconGetter$1 INSTANCE = new ControlInfoWrapper$customIconGetter$1();

    ControlInfoWrapper$customIconGetter$1() {
        super(2);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "nullIconGetter";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinPackage(ControlsModelKt.class, "frameworks__base__packages__SystemUI__android_common__SystemUI-core");
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "nullIconGetter(Landroid/content/ComponentName;Ljava/lang/String;)Landroid/graphics/drawable/Icon;";
    }

    @Override // kotlin.jvm.functions.Function2
    @Nullable
    public final Icon invoke(@NotNull ComponentName p1, @NotNull String p2) {
        Intrinsics.checkParameterIsNotNull(p1, "p1");
        Intrinsics.checkParameterIsNotNull(p2, "p2");
        return ControlsModelKt.nullIconGetter(p1, p2);
    }
}
