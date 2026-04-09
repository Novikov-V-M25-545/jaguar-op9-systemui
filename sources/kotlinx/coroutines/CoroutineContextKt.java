package kotlinx.coroutines;

import kotlin.coroutines.ContinuationInterceptor;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.scheduling.DefaultScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: CoroutineContext.kt */
/* loaded from: classes2.dex */
public final class CoroutineContextKt {
    private static final boolean useCoroutinesScheduler;

    /* JADX WARN: Code restructure failed: missing block: B:15:0x0028, code lost:
    
        if (r0.equals("on") != false) goto L19;
     */
    /* JADX WARN: Code restructure failed: missing block: B:18:0x0031, code lost:
    
        if (r0.equals("") != false) goto L19;
     */
    static {
        /*
            java.lang.String r0 = "kotlinx.coroutines.scheduler"
            java.lang.String r0 = kotlinx.coroutines.internal.SystemPropsKt.systemProp(r0)
            if (r0 != 0) goto L9
            goto L33
        L9:
            int r1 = r0.hashCode()
            if (r1 == 0) goto L2b
            r2 = 3551(0xddf, float:4.976E-42)
            if (r1 == r2) goto L22
            r2 = 109935(0x1ad6f, float:1.54052E-40)
            if (r1 != r2) goto L37
            java.lang.String r1 = "off"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L37
            r0 = 0
            goto L34
        L22:
            java.lang.String r1 = "on"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L37
            goto L33
        L2b:
            java.lang.String r1 = ""
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L37
        L33:
            r0 = 1
        L34:
            kotlinx.coroutines.CoroutineContextKt.useCoroutinesScheduler = r0
            return
        L37:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "System property 'kotlinx.coroutines.scheduler' has unrecognized value '"
            r1.append(r2)
            r1.append(r0)
            r0 = 39
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            java.lang.IllegalStateException r1 = new java.lang.IllegalStateException
            java.lang.String r0 = r0.toString()
            r1.<init>(r0)
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: kotlinx.coroutines.CoroutineContextKt.<clinit>():void");
    }

    @NotNull
    public static final CoroutineDispatcher createDefaultDispatcher() {
        return useCoroutinesScheduler ? DefaultScheduler.INSTANCE : CommonPool.INSTANCE;
    }

    @NotNull
    public static final CoroutineContext newCoroutineContext(@NotNull CoroutineScope newCoroutineContext, @NotNull CoroutineContext context) {
        Intrinsics.checkParameterIsNotNull(newCoroutineContext, "$this$newCoroutineContext");
        Intrinsics.checkParameterIsNotNull(context, "context");
        CoroutineContext coroutineContextPlus = newCoroutineContext.getCoroutineContext().plus(context);
        CoroutineContext coroutineContextPlus2 = DebugKt.getDEBUG() ? coroutineContextPlus.plus(new CoroutineId(DebugKt.getCOROUTINE_ID().incrementAndGet())) : coroutineContextPlus;
        return (coroutineContextPlus == Dispatchers.getDefault() || coroutineContextPlus.get(ContinuationInterceptor.Key) != null) ? coroutineContextPlus2 : coroutineContextPlus2.plus(Dispatchers.getDefault());
    }

    @Nullable
    public static final String getCoroutineName(@NotNull CoroutineContext coroutineName) {
        CoroutineId coroutineId;
        String name;
        Intrinsics.checkParameterIsNotNull(coroutineName, "$this$coroutineName");
        if (!DebugKt.getDEBUG() || (coroutineId = (CoroutineId) coroutineName.get(CoroutineId.Key)) == null) {
            return null;
        }
        CoroutineName coroutineName2 = (CoroutineName) coroutineName.get(CoroutineName.Key);
        if (coroutineName2 == null || (name = coroutineName2.getName()) == null) {
            name = "coroutine";
        }
        return name + '#' + coroutineId.getId();
    }
}
