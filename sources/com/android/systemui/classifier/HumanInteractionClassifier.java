package com.android.systemui.classifier;

import android.content.Context;
import android.database.ContentObserver;
import android.hardware.SensorEvent;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import com.android.systemui.R;
import java.util.ArrayDeque;

/* loaded from: classes.dex */
public class HumanInteractionClassifier extends Classifier {
    private static HumanInteractionClassifier sInstance;
    private final ArrayDeque<MotionEvent> mBufferedEvents;
    private final Context mContext;
    private int mCurrentType;
    private final float mDpi;
    private boolean mEnableClassifier;
    private final GestureClassifier[] mGestureClassifiers;
    private final Handler mHandler;
    private final HistoryEvaluator mHistoryEvaluator;
    protected final ContentObserver mSettingsObserver;
    private final StrokeClassifier[] mStrokeClassifiers;

    private HumanInteractionClassifier(Context context) {
        Handler handler = new Handler(Looper.getMainLooper());
        this.mHandler = handler;
        this.mBufferedEvents = new ArrayDeque<>();
        this.mEnableClassifier = false;
        this.mCurrentType = 7;
        ContentObserver contentObserver = new ContentObserver(handler) { // from class: com.android.systemui.classifier.HumanInteractionClassifier.1
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                HumanInteractionClassifier.this.updateConfiguration();
            }
        };
        this.mSettingsObserver = contentObserver;
        this.mContext = context;
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float f = (displayMetrics.xdpi + displayMetrics.ydpi) / 2.0f;
        this.mDpi = f;
        this.mClassifierData = new ClassifierData(f);
        this.mHistoryEvaluator = new HistoryEvaluator();
        this.mStrokeClassifiers = new StrokeClassifier[]{new AnglesClassifier(this.mClassifierData), new SpeedClassifier(this.mClassifierData), new DurationCountClassifier(this.mClassifierData), new EndPointRatioClassifier(this.mClassifierData), new EndPointLengthClassifier(this.mClassifierData), new AccelerationClassifier(this.mClassifierData), new SpeedAnglesClassifier(this.mClassifierData), new LengthCountClassifier(this.mClassifierData), new DirectionClassifier(this.mClassifierData)};
        this.mGestureClassifiers = new GestureClassifier[]{new PointerCountClassifier(this.mClassifierData), new ProximityClassifier(this.mClassifierData)};
        context.getContentResolver().registerContentObserver(Settings.Global.getUriFor("HIC_enable"), false, contentObserver, -1);
        updateConfiguration();
    }

    public static HumanInteractionClassifier getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new HumanInteractionClassifier(context);
        }
        return sInstance;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateConfiguration() {
        this.mEnableClassifier = Settings.Global.getInt(this.mContext.getContentResolver(), "HIC_enable", this.mContext.getResources().getBoolean(R.bool.config_lockscreenAntiFalsingClassifierEnabled) ? 1 : 0) != 0;
    }

    public void setType(int i) {
        this.mCurrentType = i;
    }

    @Override // com.android.systemui.classifier.Classifier
    public void onTouchEvent(MotionEvent motionEvent) {
        if (this.mEnableClassifier) {
            int i = this.mCurrentType;
            if (i == 2 || i == 9) {
                this.mBufferedEvents.add(MotionEvent.obtain(motionEvent));
                Point point = new Point(motionEvent.getX() / this.mDpi, motionEvent.getY() / this.mDpi);
                while (point.dist(new Point(this.mBufferedEvents.getFirst().getX() / this.mDpi, this.mBufferedEvents.getFirst().getY() / this.mDpi)) > 0.1f) {
                    addTouchEvent(this.mBufferedEvents.getFirst());
                    this.mBufferedEvents.remove();
                }
                if (motionEvent.getActionMasked() == 1) {
                    this.mBufferedEvents.getFirst().setAction(1);
                    addTouchEvent(this.mBufferedEvents.getFirst());
                    this.mBufferedEvents.clear();
                    return;
                }
                return;
            }
            addTouchEvent(motionEvent);
        }
    }

    private void addTouchEvent(MotionEvent motionEvent) {
        float f;
        StringBuilder sb;
        if (this.mClassifierData.update(motionEvent)) {
            for (StrokeClassifier strokeClassifier : this.mStrokeClassifiers) {
                strokeClassifier.onTouchEvent(motionEvent);
            }
            for (GestureClassifier gestureClassifier : this.mGestureClassifiers) {
                gestureClassifier.onTouchEvent(motionEvent);
            }
            int size = this.mClassifierData.getEndingStrokes().size();
            int i = 0;
            while (true) {
                f = 0.0f;
                if (i >= size) {
                    break;
                }
                Stroke stroke = this.mClassifierData.getEndingStrokes().get(i);
                sb = FalsingLog.ENABLED ? new StringBuilder("stroke") : null;
                for (StrokeClassifier strokeClassifier2 : this.mStrokeClassifiers) {
                    float falseTouchEvaluation = strokeClassifier2.getFalseTouchEvaluation(this.mCurrentType, stroke);
                    if (FalsingLog.ENABLED) {
                        String tag = strokeClassifier2.getTag();
                        sb.append(" ");
                        if (falseTouchEvaluation < 1.0f) {
                            tag = tag.toLowerCase();
                        }
                        sb.append(tag);
                        sb.append("=");
                        sb.append(falseTouchEvaluation);
                    }
                    f += falseTouchEvaluation;
                }
                if (FalsingLog.ENABLED) {
                    FalsingLog.i(" addTouchEvent", sb.toString());
                }
                this.mHistoryEvaluator.addStroke(f);
                i++;
            }
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked == 1 || actionMasked == 3) {
                sb = FalsingLog.ENABLED ? new StringBuilder("gesture") : null;
                for (GestureClassifier gestureClassifier2 : this.mGestureClassifiers) {
                    float falseTouchEvaluation2 = gestureClassifier2.getFalseTouchEvaluation(this.mCurrentType);
                    if (FalsingLog.ENABLED) {
                        String tag2 = gestureClassifier2.getTag();
                        sb.append(" ");
                        if (falseTouchEvaluation2 < 1.0f) {
                            tag2 = tag2.toLowerCase();
                        }
                        sb.append(tag2);
                        sb.append("=");
                        sb.append(falseTouchEvaluation2);
                    }
                    f += falseTouchEvaluation2;
                }
                if (FalsingLog.ENABLED) {
                    FalsingLog.i(" addTouchEvent", sb.toString());
                }
                this.mHistoryEvaluator.addGesture(f);
                setType(7);
            }
            this.mClassifierData.cleanUp(motionEvent);
        }
    }

    @Override // com.android.systemui.classifier.Classifier
    public void onSensorChanged(SensorEvent sensorEvent) {
        for (StrokeClassifier strokeClassifier : this.mStrokeClassifiers) {
            strokeClassifier.onSensorChanged(sensorEvent);
        }
        for (GestureClassifier gestureClassifier : this.mGestureClassifiers) {
            gestureClassifier.onSensorChanged(sensorEvent);
        }
    }

    public boolean isFalseTouch() {
        boolean z = false;
        if (this.mEnableClassifier) {
            float evaluation = this.mHistoryEvaluator.getEvaluation();
            z = evaluation >= 5.0f;
            if (FalsingLog.ENABLED) {
                FalsingLog.i("isFalseTouch", "eval=" + evaluation + " result=" + (z ? 1 : 0));
            }
        }
        return z;
    }

    public boolean isEnabled() {
        return this.mEnableClassifier;
    }
}
