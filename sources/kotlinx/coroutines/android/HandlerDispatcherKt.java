package kotlinx.coroutines.android;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.VisibleForTesting;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: HandlerDispatcher.kt */
/* loaded from: classes2.dex */
public final class HandlerDispatcherKt {

    @Nullable
    public static final HandlerDispatcher Main;

    @VisibleForTesting
    @NotNull
    public static final Handler asHandler(@NotNull Looper asHandler, boolean z) throws IllegalAccessException, NoSuchMethodException, InstantiationException, SecurityException, IllegalArgumentException, InvocationTargetException {
        int i;
        Intrinsics.checkParameterIsNotNull(asHandler, "$this$asHandler");
        if (!z || (i = Build.VERSION.SDK_INT) < 16) {
            return new Handler(asHandler);
        }
        if (i >= 28) {
            Object objInvoke = Handler.class.getDeclaredMethod("createAsync", Looper.class).invoke(null, asHandler);
            if (objInvoke != null) {
                return (Handler) objInvoke;
            }
            throw new TypeCastException("null cannot be cast to non-null type android.os.Handler");
        }
        try {
            Constructor declaredConstructor = Handler.class.getDeclaredConstructor(Looper.class, Handler.Callback.class, Boolean.TYPE);
            Intrinsics.checkExpressionValueIsNotNull(declaredConstructor, "Handler::class.java.getD…:class.javaPrimitiveType)");
            Object objNewInstance = declaredConstructor.newInstance(asHandler, null, Boolean.TRUE);
            Intrinsics.checkExpressionValueIsNotNull(objNewInstance, "constructor.newInstance(this, null, true)");
            return (Handler) objNewInstance;
        } catch (NoSuchMethodException unused) {
            return new Handler(asHandler);
        }
    }

    static {
        Object objM407constructorimpl;
        try {
            Result.Companion companion = Result.Companion;
            Looper mainLooper = Looper.getMainLooper();
            Intrinsics.checkExpressionValueIsNotNull(mainLooper, "Looper.getMainLooper()");
            objM407constructorimpl = Result.m407constructorimpl(new HandlerContext(asHandler(mainLooper, true), "Main"));
        } catch (Throwable th) {
            Result.Companion companion2 = Result.Companion;
            objM407constructorimpl = Result.m407constructorimpl(ResultKt.createFailure(th));
        }
        if (Result.m411isFailureimpl(objM407constructorimpl)) {
            objM407constructorimpl = null;
        }
        Main = (HandlerDispatcher) objM407constructorimpl;
    }
}
