package com.android.systemui.util;

import android.view.View;
import android.view.ViewGroup;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.RestrictedSuspendLambda;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequenceScope;
import kotlin.sequences.SequencesKt__SequenceBuilderKt;
import lineageos.hardware.LineageHardwareManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ConvenienceExtensions.kt */
/* loaded from: classes.dex */
public final class ConvenienceExtensionsKt {
    @NotNull
    public static final Sequence<View> getChildren(@NotNull ViewGroup children) {
        Intrinsics.checkParameterIsNotNull(children, "$this$children");
        return SequencesKt__SequenceBuilderKt.sequence(new ConvenienceExtensionsKt$children$1(children, null));
    }

    /* JADX INFO: Add missing generic type declarations: [T] */
    /* compiled from: ConvenienceExtensions.kt */
    @DebugMetadata(c = "com.android.systemui.util.ConvenienceExtensionsKt$takeUntil$1", f = "ConvenienceExtensions.kt", l = {LineageHardwareManager.FEATURE_KEY_DISABLE}, m = "invokeSuspend")
    /* renamed from: com.android.systemui.util.ConvenienceExtensionsKt$takeUntil$1, reason: invalid class name */
    static final class AnonymousClass1<T> extends RestrictedSuspendLambda implements Function2<SequenceScope<? super T>, Continuation<? super Unit>, Object> {
        final /* synthetic */ Function1 $pred;
        final /* synthetic */ Sequence $this_takeUntil;
        Object L$0;
        Object L$1;
        Object L$2;
        int label;
        private SequenceScope p$;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass1(Sequence sequence, Function1 function1, Continuation continuation) {
            super(2, continuation);
            this.$this_takeUntil = sequence;
            this.$pred = function1;
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        @NotNull
        public final Continuation<Unit> create(@Nullable Object obj, @NotNull Continuation<?> completion) {
            Intrinsics.checkParameterIsNotNull(completion, "completion");
            AnonymousClass1 anonymousClass1 = new AnonymousClass1(this.$this_takeUntil, this.$pred, completion);
            anonymousClass1.p$ = (SequenceScope) obj;
            return anonymousClass1;
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(Object obj, Continuation<? super Unit> continuation) {
            return ((AnonymousClass1) create(obj, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        /* JADX WARN: Removed duplicated region for block: B:11:0x0033  */
        /* JADX WARN: Removed duplicated region for block: B:9:0x002d A[PHI: r1 r4
  0x002d: PHI (r1v3 java.util.Iterator<T>) = (r1v2 java.util.Iterator<T>), (r1v4 java.util.Iterator<T>) binds: [B:8:0x0021, B:15:0x0052] A[DONT_GENERATE, DONT_INLINE]
  0x002d: PHI (r4v1 kotlin.sequences.SequenceScope) = (r4v0 kotlin.sequences.SequenceScope), (r4v2 kotlin.sequences.SequenceScope) binds: [B:8:0x0021, B:15:0x0052] A[DONT_GENERATE, DONT_INLINE]] */
        /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:12:0x0043 -> B:14:0x0046). Please report as a decompilation issue!!! */
        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        @org.jetbrains.annotations.Nullable
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        public final java.lang.Object invokeSuspend(@org.jetbrains.annotations.NotNull java.lang.Object r6) throws java.lang.Throwable {
            /*
                r5 = this;
                java.lang.Object r0 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
                int r1 = r5.label
                r2 = 1
                if (r1 == 0) goto L21
                if (r1 != r2) goto L19
                java.lang.Object r1 = r5.L$2
                java.util.Iterator r1 = (java.util.Iterator) r1
                java.lang.Object r3 = r5.L$1
                java.lang.Object r4 = r5.L$0
                kotlin.sequences.SequenceScope r4 = (kotlin.sequences.SequenceScope) r4
                kotlin.ResultKt.throwOnFailure(r6)
                goto L46
            L19:
                java.lang.IllegalStateException r5 = new java.lang.IllegalStateException
                java.lang.String r6 = "call to 'resume' before 'invoke' with coroutine"
                r5.<init>(r6)
                throw r5
            L21:
                kotlin.ResultKt.throwOnFailure(r6)
                kotlin.sequences.SequenceScope r6 = r5.p$
                kotlin.sequences.Sequence r1 = r5.$this_takeUntil
                java.util.Iterator r1 = r1.iterator()
                r4 = r6
            L2d:
                boolean r6 = r1.hasNext()
                if (r6 == 0) goto L54
                java.lang.Object r3 = r1.next()
                r5.L$0 = r4
                r5.L$1 = r3
                r5.L$2 = r1
                r5.label = r2
                java.lang.Object r6 = r4.yield(r3, r5)
                if (r6 != r0) goto L46
                return r0
            L46:
                kotlin.jvm.functions.Function1 r6 = r5.$pred
                java.lang.Object r6 = r6.invoke(r3)
                java.lang.Boolean r6 = (java.lang.Boolean) r6
                boolean r6 = r6.booleanValue()
                if (r6 == 0) goto L2d
            L54:
                kotlin.Unit r5 = kotlin.Unit.INSTANCE
                return r5
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.util.ConvenienceExtensionsKt.AnonymousClass1.invokeSuspend(java.lang.Object):java.lang.Object");
        }
    }

    @NotNull
    public static final <T> Sequence<T> takeUntil(@NotNull Sequence<? extends T> takeUntil, @NotNull Function1<? super T, Boolean> pred) {
        Intrinsics.checkParameterIsNotNull(takeUntil, "$this$takeUntil");
        Intrinsics.checkParameterIsNotNull(pred, "pred");
        return SequencesKt__SequenceBuilderKt.sequence(new AnonymousClass1(takeUntil, pred, null));
    }
}
