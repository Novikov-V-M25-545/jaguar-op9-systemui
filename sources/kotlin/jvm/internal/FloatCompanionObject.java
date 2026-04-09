package kotlin.jvm.internal;

/* compiled from: PrimitiveCompanionObjects.kt */
/* loaded from: classes.dex */
public final class FloatCompanionObject {
    public static final FloatCompanionObject INSTANCE = new FloatCompanionObject();
    private static final float MIN_VALUE = Float.MIN_VALUE;
    private static final float MAX_VALUE = Float.MAX_VALUE;
    private static final float POSITIVE_INFINITY = Float.POSITIVE_INFINITY;
    private static final float NEGATIVE_INFINITY = Float.NEGATIVE_INFINITY;
    private static final float NaN = Float.NaN;

    private FloatCompanionObject() {
    }

    public final float getMAX_VALUE() {
        return MAX_VALUE;
    }
}
