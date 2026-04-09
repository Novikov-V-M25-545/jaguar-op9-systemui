package com.airbnb.lottie.parser;

import android.graphics.PointF;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import androidx.collection.SparseArrayCompat;
import androidx.core.view.animation.PathInterpolatorCompat;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.utils.MiscUtils;
import com.airbnb.lottie.utils.Utils;
import com.airbnb.lottie.value.Keyframe;
import com.android.systemui.plugins.SensorManagerPlugin;
import java.io.IOException;
import java.lang.ref.WeakReference;

/* loaded from: classes.dex */
class KeyframeParser {
    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    static JsonReader.Options NAMES = JsonReader.Options.of("t", "s", "e", "o", "i", "h", "to", "ti");
    private static SparseArrayCompat<WeakReference<Interpolator>> pathInterpolatorCache;

    KeyframeParser() {
    }

    private static SparseArrayCompat<WeakReference<Interpolator>> pathInterpolatorCache() {
        if (pathInterpolatorCache == null) {
            pathInterpolatorCache = new SparseArrayCompat<>();
        }
        return pathInterpolatorCache;
    }

    private static WeakReference<Interpolator> getInterpolator(int i) {
        WeakReference<Interpolator> weakReference;
        synchronized (KeyframeParser.class) {
            weakReference = pathInterpolatorCache().get(i);
        }
        return weakReference;
    }

    private static void putInterpolator(int i, WeakReference<Interpolator> weakReference) {
        synchronized (KeyframeParser.class) {
            pathInterpolatorCache.put(i, weakReference);
        }
    }

    static <T> Keyframe<T> parse(JsonReader jsonReader, LottieComposition lottieComposition, float f, ValueParser<T> valueParser, boolean z) throws IOException {
        if (z) {
            return parseKeyframe(lottieComposition, jsonReader, f, valueParser);
        }
        return parseStaticValue(jsonReader, f, valueParser);
    }

    private static <T> Keyframe<T> parseKeyframe(LottieComposition lottieComposition, JsonReader jsonReader, float f, ValueParser<T> valueParser) throws IOException {
        Interpolator interpolator;
        jsonReader.beginObject();
        PointF pointFJsonToPoint = null;
        PointF pointFJsonToPoint2 = null;
        T t = null;
        T t2 = null;
        PointF pointFJsonToPoint3 = null;
        PointF pointFJsonToPoint4 = null;
        float fNextDouble = 0.0f;
        while (true) {
            boolean z = false;
            while (jsonReader.hasNext()) {
                switch (jsonReader.selectName(NAMES)) {
                    case 0:
                        fNextDouble = (float) jsonReader.nextDouble();
                        break;
                    case 1:
                        t = valueParser.parse(jsonReader, f);
                        break;
                    case 2:
                        t2 = valueParser.parse(jsonReader, f);
                        break;
                    case SensorManagerPlugin.Sensor.TYPE_SWIPE /* 3 */:
                        pointFJsonToPoint = JsonUtils.jsonToPoint(jsonReader, f);
                        break;
                    case 4:
                        pointFJsonToPoint2 = JsonUtils.jsonToPoint(jsonReader, f);
                        break;
                    case 5:
                        if (jsonReader.nextInt() == 1) {
                            z = true;
                            break;
                        }
                        break;
                    case 6:
                        pointFJsonToPoint3 = JsonUtils.jsonToPoint(jsonReader, f);
                        break;
                    case 7:
                        pointFJsonToPoint4 = JsonUtils.jsonToPoint(jsonReader, f);
                        break;
                    default:
                        jsonReader.skipValue();
                        break;
                }
            }
            jsonReader.endObject();
            if (z) {
                interpolator = LINEAR_INTERPOLATOR;
                t2 = t;
            } else if (pointFJsonToPoint != null && pointFJsonToPoint2 != null) {
                float f2 = -f;
                pointFJsonToPoint.x = MiscUtils.clamp(pointFJsonToPoint.x, f2, f);
                pointFJsonToPoint.y = MiscUtils.clamp(pointFJsonToPoint.y, -100.0f, 100.0f);
                pointFJsonToPoint2.x = MiscUtils.clamp(pointFJsonToPoint2.x, f2, f);
                float fClamp = MiscUtils.clamp(pointFJsonToPoint2.y, -100.0f, 100.0f);
                pointFJsonToPoint2.y = fClamp;
                int iHashFor = Utils.hashFor(pointFJsonToPoint.x, pointFJsonToPoint.y, pointFJsonToPoint2.x, fClamp);
                WeakReference<Interpolator> interpolator2 = getInterpolator(iHashFor);
                Interpolator interpolatorCreate = interpolator2 != null ? interpolator2.get() : null;
                if (interpolator2 == null || interpolatorCreate == null) {
                    interpolatorCreate = PathInterpolatorCompat.create(pointFJsonToPoint.x / f, pointFJsonToPoint.y / f, pointFJsonToPoint2.x / f, pointFJsonToPoint2.y / f);
                    try {
                        putInterpolator(iHashFor, new WeakReference(interpolatorCreate));
                    } catch (ArrayIndexOutOfBoundsException unused) {
                    }
                }
                interpolator = interpolatorCreate;
            } else {
                interpolator = LINEAR_INTERPOLATOR;
            }
            Keyframe<T> keyframe = new Keyframe<>(lottieComposition, t, t2, interpolator, fNextDouble, null);
            keyframe.pathCp1 = pointFJsonToPoint3;
            keyframe.pathCp2 = pointFJsonToPoint4;
            return keyframe;
        }
    }

    private static <T> Keyframe<T> parseStaticValue(JsonReader jsonReader, float f, ValueParser<T> valueParser) throws IOException {
        return new Keyframe<>(valueParser.parse(jsonReader, f));
    }
}
