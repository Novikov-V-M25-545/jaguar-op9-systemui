package dagger.internal;

import java.util.Objects;

/* loaded from: classes.dex */
public final class Preconditions {
    public static <T> T checkNotNull(T reference) {
        Objects.requireNonNull(reference);
        return reference;
    }

    public static <T> T checkNotNull(T reference, String errorMessage) {
        Objects.requireNonNull(reference, errorMessage);
        return reference;
    }
}
