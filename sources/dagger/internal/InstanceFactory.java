package dagger.internal;

import dagger.Lazy;

/* loaded from: classes.dex */
public final class InstanceFactory<T> implements Factory<T>, Lazy<T> {
    private static final InstanceFactory<Object> NULL_INSTANCE_FACTORY = new InstanceFactory<>(null);
    private final T instance;

    public static <T> Factory<T> create(T instance) {
        return new InstanceFactory(Preconditions.checkNotNull(instance, "instance cannot be null"));
    }

    private InstanceFactory(T instance) {
        this.instance = instance;
    }

    @Override // javax.inject.Provider
    public T get() {
        return this.instance;
    }
}
