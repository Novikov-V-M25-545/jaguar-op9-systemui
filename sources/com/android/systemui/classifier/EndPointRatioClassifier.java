package com.android.systemui.classifier;

/* loaded from: classes.dex */
public class EndPointRatioClassifier extends StrokeClassifier {
    @Override // com.android.systemui.classifier.Classifier
    public String getTag() {
        return "END_RTIO";
    }

    public EndPointRatioClassifier(ClassifierData classifierData) {
        this.mClassifierData = classifierData;
    }

    @Override // com.android.systemui.classifier.StrokeClassifier
    public float getFalseTouchEvaluation(int i, Stroke stroke) {
        return EndPointRatioEvaluator.evaluate(stroke.getTotalLength() == 0.0f ? 1.0f : stroke.getEndPointLength() / stroke.getTotalLength());
    }
}
