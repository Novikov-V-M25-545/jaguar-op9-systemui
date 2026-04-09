package com.android.systemui.classifier.brightline;

import android.view.MotionEvent;
import com.android.systemui.util.sensors.ThresholdSensor;
import java.util.List;

/* loaded from: classes.dex */
abstract class FalsingClassifier {
    private final FalsingDataProvider mDataProvider;

    abstract String getReason();

    abstract boolean isFalseTouch();

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onProximityEvent(ThresholdSensor.ThresholdSensorEvent thresholdSensorEvent) {
    }

    void onSessionEnded() {
    }

    void onSessionStarted() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onTouchEvent(MotionEvent motionEvent) {
    }

    FalsingClassifier(FalsingDataProvider falsingDataProvider) {
        this.mDataProvider = falsingDataProvider;
    }

    List<MotionEvent> getRecentMotionEvents() {
        return this.mDataProvider.getRecentMotionEvents();
    }

    MotionEvent getFirstMotionEvent() {
        return this.mDataProvider.getFirstRecentMotionEvent();
    }

    MotionEvent getLastMotionEvent() {
        return this.mDataProvider.getLastMotionEvent();
    }

    boolean isHorizontal() {
        return this.mDataProvider.isHorizontal();
    }

    boolean isRight() {
        return this.mDataProvider.isRight();
    }

    boolean isVertical() {
        return this.mDataProvider.isVertical();
    }

    boolean isUp() {
        return this.mDataProvider.isUp();
    }

    float getAngle() {
        return this.mDataProvider.getAngle();
    }

    int getWidthPixels() {
        return this.mDataProvider.getWidthPixels();
    }

    int getHeightPixels() {
        return this.mDataProvider.getHeightPixels();
    }

    float getXdpi() {
        return this.mDataProvider.getXdpi();
    }

    float getYdpi() {
        return this.mDataProvider.getYdpi();
    }

    final int getInteractionType() {
        return this.mDataProvider.getInteractionType();
    }

    static void logDebug(String str) {
        BrightLineFalsingManager.logDebug(str);
    }

    static void logInfo(String str) {
        BrightLineFalsingManager.logInfo(str);
    }
}
