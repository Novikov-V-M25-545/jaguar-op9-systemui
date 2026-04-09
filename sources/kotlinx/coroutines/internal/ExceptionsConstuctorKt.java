package kotlinx.coroutines.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.collections.ArraysKt___ArraysKt;
import kotlin.comparisons.ComparisonsKt__ComparisonsKt;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CopyableThrowable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ExceptionsConstuctor.kt */
/* loaded from: classes2.dex */
public final class ExceptionsConstuctorKt {
    private static final int throwableFields = fieldsCountOrDefault(Throwable.class, -1);
    private static final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock();
    private static final WeakHashMap<Class<? extends Throwable>, Function1<Throwable, Throwable>> exceptionCtors = new WeakHashMap<>();

    @Nullable
    public static final <E extends Throwable> E tryCopyException(@NotNull E exception) throws SecurityException {
        Object objM407constructorimpl;
        ReentrantReadWriteLock.ReadLock lock;
        int readHoldCount;
        ReentrantReadWriteLock.WriteLock writeLock;
        Intrinsics.checkParameterIsNotNull(exception, "exception");
        if (exception instanceof CopyableThrowable) {
            try {
                Result.Companion companion = Result.Companion;
                objM407constructorimpl = Result.m407constructorimpl(((CopyableThrowable) exception).createCopy());
            } catch (Throwable th) {
                Result.Companion companion2 = Result.Companion;
                objM407constructorimpl = Result.m407constructorimpl(ResultKt.createFailure(th));
            }
            return (E) (Result.m411isFailureimpl(objM407constructorimpl) ? null : objM407constructorimpl);
        }
        ReentrantReadWriteLock reentrantReadWriteLock = cacheLock;
        ReentrantReadWriteLock.ReadLock lock2 = reentrantReadWriteLock.readLock();
        lock2.lock();
        try {
            Function1<Throwable, Throwable> function1 = exceptionCtors.get(exception.getClass());
            if (function1 != null) {
                return (E) function1.invoke(exception);
            }
            int i = 0;
            if (throwableFields != fieldsCountOrDefault(exception.getClass(), 0)) {
                lock = reentrantReadWriteLock.readLock();
                readHoldCount = reentrantReadWriteLock.getWriteHoldCount() == 0 ? reentrantReadWriteLock.getReadHoldCount() : 0;
                for (int i2 = 0; i2 < readHoldCount; i2++) {
                    lock.unlock();
                }
                writeLock = reentrantReadWriteLock.writeLock();
                writeLock.lock();
                try {
                    exceptionCtors.put(exception.getClass(), new Function1() { // from class: kotlinx.coroutines.internal.ExceptionsConstuctorKt$tryCopyException$4$1
                        @Override // kotlin.jvm.functions.Function1
                        @Nullable
                        public final Void invoke(@NotNull Throwable it) {
                            Intrinsics.checkParameterIsNotNull(it, "it");
                            return null;
                        }
                    });
                    Unit unit = Unit.INSTANCE;
                    return null;
                } finally {
                    while (i < readHoldCount) {
                        lock.lock();
                        i++;
                    }
                    writeLock.unlock();
                }
            }
            Constructor<?>[] constructors = exception.getClass().getConstructors();
            Intrinsics.checkExpressionValueIsNotNull(constructors, "exception.javaClass.constructors");
            Function1<Throwable, Throwable> function1CreateConstructor = null;
            for (Constructor constructor : ArraysKt___ArraysKt.sortedWith(constructors, new Comparator<T>() { // from class: kotlinx.coroutines.internal.ExceptionsConstuctorKt$tryCopyException$$inlined$sortedByDescending$1
                /* JADX WARN: Multi-variable type inference failed */
                @Override // java.util.Comparator
                public final int compare(T t, T t2) {
                    Constructor it = (Constructor) t2;
                    Intrinsics.checkExpressionValueIsNotNull(it, "it");
                    Integer numValueOf = Integer.valueOf(it.getParameterTypes().length);
                    Constructor it2 = (Constructor) t;
                    Intrinsics.checkExpressionValueIsNotNull(it2, "it");
                    return ComparisonsKt__ComparisonsKt.compareValues(numValueOf, Integer.valueOf(it2.getParameterTypes().length));
                }
            })) {
                Intrinsics.checkExpressionValueIsNotNull(constructor, "constructor");
                function1CreateConstructor = createConstructor(constructor);
                if (function1CreateConstructor != null) {
                    break;
                }
            }
            ReentrantReadWriteLock reentrantReadWriteLock2 = cacheLock;
            lock = reentrantReadWriteLock2.readLock();
            readHoldCount = reentrantReadWriteLock2.getWriteHoldCount() == 0 ? reentrantReadWriteLock2.getReadHoldCount() : 0;
            for (int i3 = 0; i3 < readHoldCount; i3++) {
                lock.unlock();
            }
            writeLock = reentrantReadWriteLock2.writeLock();
            writeLock.lock();
            try {
                exceptionCtors.put(exception.getClass(), function1CreateConstructor != null ? function1CreateConstructor : new Function1() { // from class: kotlinx.coroutines.internal.ExceptionsConstuctorKt$tryCopyException$5$1
                    @Override // kotlin.jvm.functions.Function1
                    @Nullable
                    public final Void invoke(@NotNull Throwable it) {
                        Intrinsics.checkParameterIsNotNull(it, "it");
                        return null;
                    }
                });
                Unit unit2 = Unit.INSTANCE;
                while (i < readHoldCount) {
                    lock.lock();
                    i++;
                }
                writeLock.unlock();
                if (function1CreateConstructor != null) {
                    return (E) function1CreateConstructor.invoke(exception);
                }
                return null;
            } finally {
                while (i < readHoldCount) {
                    lock.lock();
                    i++;
                }
                writeLock.unlock();
            }
        } finally {
            lock2.unlock();
        }
    }

    private static final Function1<Throwable, Throwable> createConstructor(final Constructor<?> constructor) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        int length = parameterTypes.length;
        if (length == 0) {
            return new Function1<Throwable, Throwable>() { // from class: kotlinx.coroutines.internal.ExceptionsConstuctorKt$createConstructor$$inlined$safeCtor$4
                /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
                {
                    super(1);
                }

                @Override // kotlin.jvm.functions.Function1
                @Nullable
                public final Throwable invoke(@NotNull Throwable e) {
                    Object objM407constructorimpl;
                    Object objNewInstance;
                    Intrinsics.checkParameterIsNotNull(e, "e");
                    try {
                        Result.Companion companion = Result.Companion;
                        objNewInstance = constructor.newInstance(new Object[0]);
                    } catch (Throwable th) {
                        Result.Companion companion2 = Result.Companion;
                        objM407constructorimpl = Result.m407constructorimpl(ResultKt.createFailure(th));
                    }
                    if (objNewInstance == null) {
                        throw new TypeCastException("null cannot be cast to non-null type kotlin.Throwable");
                    }
                    Throwable th2 = (Throwable) objNewInstance;
                    th2.initCause(e);
                    objM407constructorimpl = Result.m407constructorimpl(th2);
                    if (Result.m411isFailureimpl(objM407constructorimpl)) {
                        objM407constructorimpl = null;
                    }
                    return (Throwable) objM407constructorimpl;
                }
            };
        }
        if (length != 1) {
            if (length == 2 && Intrinsics.areEqual(parameterTypes[0], String.class) && Intrinsics.areEqual(parameterTypes[1], Throwable.class)) {
                return new Function1<Throwable, Throwable>() { // from class: kotlinx.coroutines.internal.ExceptionsConstuctorKt$createConstructor$$inlined$safeCtor$1
                    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
                    {
                        super(1);
                    }

                    @Override // kotlin.jvm.functions.Function1
                    @Nullable
                    public final Throwable invoke(@NotNull Throwable e) {
                        Object objM407constructorimpl;
                        Object objNewInstance;
                        Intrinsics.checkParameterIsNotNull(e, "e");
                        try {
                            Result.Companion companion = Result.Companion;
                            objNewInstance = constructor.newInstance(e.getMessage(), e);
                        } catch (Throwable th) {
                            Result.Companion companion2 = Result.Companion;
                            objM407constructorimpl = Result.m407constructorimpl(ResultKt.createFailure(th));
                        }
                        if (objNewInstance == null) {
                            throw new TypeCastException("null cannot be cast to non-null type kotlin.Throwable");
                        }
                        objM407constructorimpl = Result.m407constructorimpl((Throwable) objNewInstance);
                        if (Result.m411isFailureimpl(objM407constructorimpl)) {
                            objM407constructorimpl = null;
                        }
                        return (Throwable) objM407constructorimpl;
                    }
                };
            }
            return null;
        }
        Class<?> cls = parameterTypes[0];
        if (Intrinsics.areEqual(cls, Throwable.class)) {
            return new Function1<Throwable, Throwable>() { // from class: kotlinx.coroutines.internal.ExceptionsConstuctorKt$createConstructor$$inlined$safeCtor$2
                /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
                {
                    super(1);
                }

                @Override // kotlin.jvm.functions.Function1
                @Nullable
                public final Throwable invoke(@NotNull Throwable e) {
                    Object objM407constructorimpl;
                    Object objNewInstance;
                    Intrinsics.checkParameterIsNotNull(e, "e");
                    try {
                        Result.Companion companion = Result.Companion;
                        objNewInstance = constructor.newInstance(e);
                    } catch (Throwable th) {
                        Result.Companion companion2 = Result.Companion;
                        objM407constructorimpl = Result.m407constructorimpl(ResultKt.createFailure(th));
                    }
                    if (objNewInstance == null) {
                        throw new TypeCastException("null cannot be cast to non-null type kotlin.Throwable");
                    }
                    objM407constructorimpl = Result.m407constructorimpl((Throwable) objNewInstance);
                    if (Result.m411isFailureimpl(objM407constructorimpl)) {
                        objM407constructorimpl = null;
                    }
                    return (Throwable) objM407constructorimpl;
                }
            };
        }
        if (Intrinsics.areEqual(cls, String.class)) {
            return new Function1<Throwable, Throwable>() { // from class: kotlinx.coroutines.internal.ExceptionsConstuctorKt$createConstructor$$inlined$safeCtor$3
                /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
                {
                    super(1);
                }

                @Override // kotlin.jvm.functions.Function1
                @Nullable
                public final Throwable invoke(@NotNull Throwable e) {
                    Object objM407constructorimpl;
                    Object objNewInstance;
                    Intrinsics.checkParameterIsNotNull(e, "e");
                    try {
                        Result.Companion companion = Result.Companion;
                        objNewInstance = constructor.newInstance(e.getMessage());
                    } catch (Throwable th) {
                        Result.Companion companion2 = Result.Companion;
                        objM407constructorimpl = Result.m407constructorimpl(ResultKt.createFailure(th));
                    }
                    if (objNewInstance == null) {
                        throw new TypeCastException("null cannot be cast to non-null type kotlin.Throwable");
                    }
                    Throwable th2 = (Throwable) objNewInstance;
                    th2.initCause(e);
                    objM407constructorimpl = Result.m407constructorimpl(th2);
                    if (Result.m411isFailureimpl(objM407constructorimpl)) {
                        objM407constructorimpl = null;
                    }
                    return (Throwable) objM407constructorimpl;
                }
            };
        }
        return null;
    }

    private static final int fieldsCountOrDefault(@NotNull Class<?> cls, int i) {
        Object objM407constructorimpl;
        JvmClassMappingKt.getKotlinClass(cls);
        try {
            Result.Companion companion = Result.Companion;
            objM407constructorimpl = Result.m407constructorimpl(Integer.valueOf(fieldsCount$default(cls, 0, 1, null)));
        } catch (Throwable th) {
            Result.Companion companion2 = Result.Companion;
            objM407constructorimpl = Result.m407constructorimpl(ResultKt.createFailure(th));
        }
        Integer numValueOf = Integer.valueOf(i);
        if (Result.m411isFailureimpl(objM407constructorimpl)) {
            objM407constructorimpl = numValueOf;
        }
        return ((Number) objM407constructorimpl).intValue();
    }

    static /* synthetic */ int fieldsCount$default(Class cls, int i, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            i = 0;
        }
        return fieldsCount(cls, i);
    }

    private static final int fieldsCount(@NotNull Class<?> cls, int i) {
        do {
            Field[] declaredFields = cls.getDeclaredFields();
            Intrinsics.checkExpressionValueIsNotNull(declaredFields, "declaredFields");
            int i2 = 0;
            for (Field it : declaredFields) {
                Intrinsics.checkExpressionValueIsNotNull(it, "it");
                if (!Modifier.isStatic(it.getModifiers())) {
                    i2++;
                }
            }
            i += i2;
            cls = cls.getSuperclass();
        } while (cls != null);
        return i;
    }
}
