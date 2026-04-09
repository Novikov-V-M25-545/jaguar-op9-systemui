package kotlinx.coroutines;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt___RangesKt;
import kotlin.text.StringsKt__StringNumberConversionsKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: CommonPool.kt */
/* loaded from: classes2.dex */
public final class CommonPool extends ExecutorCoroutineDispatcher {
    public static final CommonPool INSTANCE = new CommonPool();
    private static volatile Executor pool;
    private static final int requestedParallelism;
    private static boolean usePrivatePool;

    @Override // kotlinx.coroutines.CoroutineDispatcher
    @NotNull
    public String toString() {
        return "CommonPool";
    }

    static {
        String property;
        int iIntValue;
        try {
            property = System.getProperty("kotlinx.coroutines.default.parallelism");
        } catch (Throwable unused) {
            property = null;
        }
        if (property != null) {
            Integer intOrNull = StringsKt__StringNumberConversionsKt.toIntOrNull(property);
            if (intOrNull == null || intOrNull.intValue() < 1) {
                throw new IllegalStateException(("Expected positive number in kotlinx.coroutines.default.parallelism, but has " + property).toString());
            }
            iIntValue = intOrNull.intValue();
        } else {
            iIntValue = -1;
        }
        requestedParallelism = iIntValue;
    }

    private CommonPool() {
    }

    private final int getParallelism() {
        Integer numValueOf = Integer.valueOf(requestedParallelism);
        if (!(numValueOf.intValue() > 0)) {
            numValueOf = null;
        }
        return numValueOf != null ? numValueOf.intValue() : RangesKt___RangesKt.coerceAtLeast(Runtime.getRuntime().availableProcessors() - 1, 1);
    }

    private final ExecutorService createPool() {
        Class<?> cls;
        ExecutorService executorService;
        if (System.getSecurityManager() != null) {
            return createPlainPool();
        }
        ExecutorService executorService2 = null;
        try {
            cls = Class.forName("java.util.concurrent.ForkJoinPool");
        } catch (Throwable unused) {
            cls = null;
        }
        if (cls == null) {
            return createPlainPool();
        }
        if (!usePrivatePool && requestedParallelism < 0) {
            try {
                Method method = cls.getMethod("commonPool", new Class[0]);
                Object objInvoke = method != null ? method.invoke(null, new Object[0]) : null;
                if (!(objInvoke instanceof ExecutorService)) {
                    objInvoke = null;
                }
                executorService = (ExecutorService) objInvoke;
            } catch (Throwable unused2) {
                executorService = null;
            }
            if (executorService != null) {
                if (!INSTANCE.isGoodCommonPool$kotlinx_coroutines_core(cls, executorService)) {
                    executorService = null;
                }
                if (executorService != null) {
                    return executorService;
                }
            }
        }
        try {
            Object objNewInstance = cls.getConstructor(Integer.TYPE).newInstance(Integer.valueOf(INSTANCE.getParallelism()));
            if (!(objNewInstance instanceof ExecutorService)) {
                objNewInstance = null;
            }
            executorService2 = (ExecutorService) objNewInstance;
        } catch (Throwable unused3) {
        }
        return executorService2 != null ? executorService2 : createPlainPool();
    }

    public final boolean isGoodCommonPool$kotlinx_coroutines_core(@NotNull Class<?> fjpClass, @NotNull ExecutorService executor) {
        Intrinsics.checkParameterIsNotNull(fjpClass, "fjpClass");
        Intrinsics.checkParameterIsNotNull(executor, "executor");
        executor.submit(new Runnable() { // from class: kotlinx.coroutines.CommonPool$isGoodCommonPool$1
            @Override // java.lang.Runnable
            public final void run() {
            }
        });
        Integer num = null;
        try {
            Object objInvoke = fjpClass.getMethod("getPoolSize", new Class[0]).invoke(executor, new Object[0]);
            if (!(objInvoke instanceof Integer)) {
                objInvoke = null;
            }
            num = (Integer) objInvoke;
        } catch (Throwable unused) {
        }
        return num != null && num.intValue() >= 1;
    }

    private final ExecutorService createPlainPool() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        ExecutorService executorServiceNewFixedThreadPool = Executors.newFixedThreadPool(getParallelism(), new ThreadFactory() { // from class: kotlinx.coroutines.CommonPool.createPlainPool.1
            @Override // java.util.concurrent.ThreadFactory
            @NotNull
            public final Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "CommonPool-worker-" + atomicInteger.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            }
        });
        Intrinsics.checkExpressionValueIsNotNull(executorServiceNewFixedThreadPool, "Executors.newFixedThread…Daemon = true }\n        }");
        return executorServiceNewFixedThreadPool;
    }

    private final synchronized Executor getOrCreatePoolSync() {
        Executor executorCreatePool;
        executorCreatePool = pool;
        if (executorCreatePool == null) {
            executorCreatePool = createPool();
            pool = executorCreatePool;
        }
        return executorCreatePool;
    }

    @Override // kotlinx.coroutines.CoroutineDispatcher
    /* renamed from: dispatch */
    public void mo415dispatch(@NotNull CoroutineContext context, @NotNull Runnable block) {
        Runnable runnableWrapTask;
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(block, "block");
        try {
            Executor orCreatePoolSync = pool;
            if (orCreatePoolSync == null) {
                orCreatePoolSync = getOrCreatePoolSync();
            }
            TimeSource timeSource = TimeSourceKt.getTimeSource();
            if (timeSource == null || (runnableWrapTask = timeSource.wrapTask(block)) == null) {
                runnableWrapTask = block;
            }
            orCreatePoolSync.execute(runnableWrapTask);
        } catch (RejectedExecutionException unused) {
            TimeSource timeSource2 = TimeSourceKt.getTimeSource();
            if (timeSource2 != null) {
                timeSource2.unTrackTask();
            }
            DefaultExecutor.INSTANCE.enqueue(block);
        }
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        throw new IllegalStateException("Close cannot be invoked on CommonPool".toString());
    }
}
