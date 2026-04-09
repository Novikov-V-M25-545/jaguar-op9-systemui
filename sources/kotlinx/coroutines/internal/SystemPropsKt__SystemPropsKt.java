package kotlinx.coroutines.internal;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: SystemProps.kt */
/* loaded from: classes2.dex */
final /* synthetic */ class SystemPropsKt__SystemPropsKt {
    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    public static final int getAVAILABLE_PROCESSORS() {
        return AVAILABLE_PROCESSORS;
    }

    @Nullable
    public static final String systemProp(@NotNull String propertyName) {
        Intrinsics.checkParameterIsNotNull(propertyName, "propertyName");
        try {
            return System.getProperty(propertyName);
        } catch (SecurityException unused) {
            return null;
        }
    }
}
