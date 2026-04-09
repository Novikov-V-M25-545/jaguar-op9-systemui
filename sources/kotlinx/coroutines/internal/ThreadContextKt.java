package kotlinx.coroutines.internal;

import kotlin.TypeCastException;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.ThreadContextElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ThreadContext.kt */
/* loaded from: classes2.dex */
public final class ThreadContextKt {
    private static final Symbol ZERO = new Symbol("ZERO");
    private static final Function2<Object, CoroutineContext.Element, Object> countAll = new Function2<Object, CoroutineContext.Element, Object>() { // from class: kotlinx.coroutines.internal.ThreadContextKt$countAll$1
        @Override // kotlin.jvm.functions.Function2
        @Nullable
        public final Object invoke(@Nullable Object obj, @NotNull CoroutineContext.Element element) {
            Intrinsics.checkParameterIsNotNull(element, "element");
            if (!(element instanceof ThreadContextElement)) {
                return obj;
            }
            if (!(obj instanceof Integer)) {
                obj = null;
            }
            Integer num = (Integer) obj;
            int iIntValue = num != null ? num.intValue() : 1;
            return iIntValue == 0 ? element : Integer.valueOf(iIntValue + 1);
        }
    };
    private static final Function2<ThreadContextElement<?>, CoroutineContext.Element, ThreadContextElement<?>> findOne = new Function2<ThreadContextElement<?>, CoroutineContext.Element, ThreadContextElement<?>>() { // from class: kotlinx.coroutines.internal.ThreadContextKt$findOne$1
        @Override // kotlin.jvm.functions.Function2
        @Nullable
        public final ThreadContextElement<?> invoke(@Nullable ThreadContextElement<?> threadContextElement, @NotNull CoroutineContext.Element element) {
            Intrinsics.checkParameterIsNotNull(element, "element");
            if (threadContextElement != null) {
                return threadContextElement;
            }
            if (!(element instanceof ThreadContextElement)) {
                element = null;
            }
            return (ThreadContextElement) element;
        }
    };
    private static final Function2<ThreadState, CoroutineContext.Element, ThreadState> updateState = new Function2<ThreadState, CoroutineContext.Element, ThreadState>() { // from class: kotlinx.coroutines.internal.ThreadContextKt$updateState$1
        @Override // kotlin.jvm.functions.Function2
        @NotNull
        public final ThreadState invoke(@NotNull ThreadState state, @NotNull CoroutineContext.Element element) {
            Intrinsics.checkParameterIsNotNull(state, "state");
            Intrinsics.checkParameterIsNotNull(element, "element");
            if (element instanceof ThreadContextElement) {
                state.append(((ThreadContextElement) element).updateThreadContext(state.getContext()));
            }
            return state;
        }
    };
    private static final Function2<ThreadState, CoroutineContext.Element, ThreadState> restoreState = new Function2<ThreadState, CoroutineContext.Element, ThreadState>() { // from class: kotlinx.coroutines.internal.ThreadContextKt$restoreState$1
        @Override // kotlin.jvm.functions.Function2
        @NotNull
        public final ThreadState invoke(@NotNull ThreadState state, @NotNull CoroutineContext.Element element) {
            Intrinsics.checkParameterIsNotNull(state, "state");
            Intrinsics.checkParameterIsNotNull(element, "element");
            if (element instanceof ThreadContextElement) {
                ((ThreadContextElement) element).restoreThreadContext(state.getContext(), state.take());
            }
            return state;
        }
    };

    @NotNull
    public static final Object threadContextElements(@NotNull CoroutineContext context) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Object objFold = context.fold(0, countAll);
        if (objFold == null) {
            Intrinsics.throwNpe();
        }
        return objFold;
    }

    @Nullable
    public static final Object updateThreadContext(@NotNull CoroutineContext context, @Nullable Object obj) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        if (obj == null) {
            obj = threadContextElements(context);
        }
        if (obj == 0) {
            return ZERO;
        }
        if (obj instanceof Integer) {
            return context.fold(new ThreadState(context, ((Number) obj).intValue()), updateState);
        }
        if (obj == null) {
            throw new TypeCastException("null cannot be cast to non-null type kotlinx.coroutines.ThreadContextElement<kotlin.Any?>");
        }
        return ((ThreadContextElement) obj).updateThreadContext(context);
    }

    public static final void restoreThreadContext(@NotNull CoroutineContext context, @Nullable Object obj) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        if (obj == ZERO) {
            return;
        }
        if (obj instanceof ThreadState) {
            ((ThreadState) obj).start();
            context.fold(obj, restoreState);
        } else {
            Object objFold = context.fold(null, findOne);
            if (objFold == null) {
                throw new TypeCastException("null cannot be cast to non-null type kotlinx.coroutines.ThreadContextElement<kotlin.Any?>");
            }
            ((ThreadContextElement) objFold).restoreThreadContext(context, obj);
        }
    }
}
