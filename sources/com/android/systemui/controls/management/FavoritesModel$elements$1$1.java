package com.android.systemui.controls.management;

import android.content.ComponentName;
import android.graphics.drawable.Icon;
import com.android.systemui.controls.CustomIconCache;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: FavoritesModel.kt */
/* loaded from: classes.dex */
final /* synthetic */ class FavoritesModel$elements$1$1 extends FunctionReference implements Function2<ComponentName, String, Icon> {
    FavoritesModel$elements$1$1(CustomIconCache customIconCache) {
        super(2, customIconCache);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "retrieve";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(CustomIconCache.class);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "retrieve(Landroid/content/ComponentName;Ljava/lang/String;)Landroid/graphics/drawable/Icon;";
    }

    @Override // kotlin.jvm.functions.Function2
    @Nullable
    public final Icon invoke(@NotNull ComponentName p1, @NotNull String p2) {
        Intrinsics.checkParameterIsNotNull(p1, "p1");
        Intrinsics.checkParameterIsNotNull(p2, "p2");
        return ((CustomIconCache) this.receiver).retrieve(p1, p2);
    }
}
