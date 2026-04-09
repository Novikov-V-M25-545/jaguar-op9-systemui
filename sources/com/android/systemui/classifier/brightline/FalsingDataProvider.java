package com.android.systemui.classifier.brightline;

import android.util.DisplayMetrics;
import android.view.MotionEvent;
import com.android.systemui.statusbar.policy.BatteryController;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class FalsingDataProvider {
    private final BatteryController mBatteryController;
    private MotionEvent mFirstActualMotionEvent;
    private MotionEvent mFirstRecentMotionEvent;
    private final int mHeightPixels;
    private int mInteractionType;
    private MotionEvent mLastMotionEvent;
    private final int mWidthPixels;
    private final float mXdpi;
    private final float mYdpi;
    private final TimeLimitedMotionEventBuffer mRecentMotionEvents = new TimeLimitedMotionEventBuffer(1000);
    private boolean mDirty = true;
    private float mAngle = 0.0f;

    public FalsingDataProvider(DisplayMetrics displayMetrics, BatteryController batteryController) {
        this.mXdpi = displayMetrics.xdpi;
        this.mYdpi = displayMetrics.ydpi;
        this.mWidthPixels = displayMetrics.widthPixels;
        this.mHeightPixels = displayMetrics.heightPixels;
        this.mBatteryController = batteryController;
        FalsingClassifier.logInfo("xdpi, ydpi: " + getXdpi() + ", " + getYdpi());
        FalsingClassifier.logInfo("width, height: " + getWidthPixels() + ", " + getHeightPixels());
    }

    void onMotionEvent(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0) {
            this.mFirstActualMotionEvent = motionEvent;
        }
        List<MotionEvent> listUnpackMotionEvent = unpackMotionEvent(motionEvent);
        FalsingClassifier.logDebug("Unpacked into: " + listUnpackMotionEvent.size());
        if (BrightLineFalsingManager.DEBUG) {
            for (MotionEvent motionEvent2 : listUnpackMotionEvent) {
                FalsingClassifier.logDebug("x,y,t: " + motionEvent2.getX() + "," + motionEvent2.getY() + "," + motionEvent2.getEventTime());
            }
        }
        if (motionEvent.getActionMasked() == 0) {
            this.mRecentMotionEvents.clear();
        }
        this.mRecentMotionEvents.addAll(listUnpackMotionEvent);
        FalsingClassifier.logDebug("Size: " + this.mRecentMotionEvents.size());
        this.mDirty = true;
    }

    int getWidthPixels() {
        return this.mWidthPixels;
    }

    int getHeightPixels() {
        return this.mHeightPixels;
    }

    float getXdpi() {
        return this.mXdpi;
    }

    float getYdpi() {
        return this.mYdpi;
    }

    List<MotionEvent> getRecentMotionEvents() {
        return this.mRecentMotionEvents;
    }

    final void setInteractionType(int i) {
        this.mInteractionType = i;
    }

    public boolean isDirty() {
        return this.mDirty;
    }

    final int getInteractionType() {
        return this.mInteractionType;
    }

    MotionEvent getFirstRecentMotionEvent() {
        recalculateData();
        return this.mFirstRecentMotionEvent;
    }

    MotionEvent getLastMotionEvent() {
        recalculateData();
        return this.mLastMotionEvent;
    }

    float getAngle() {
        recalculateData();
        return this.mAngle;
    }

    boolean isHorizontal() {
        recalculateData();
        return !this.mRecentMotionEvents.isEmpty() && Math.abs(this.mFirstRecentMotionEvent.getX() - this.mLastMotionEvent.getX()) > Math.abs(this.mFirstRecentMotionEvent.getY() - this.mLastMotionEvent.getY());
    }

    boolean isRight() {
        recalculateData();
        return !this.mRecentMotionEvents.isEmpty() && this.mLastMotionEvent.getX() > this.mFirstRecentMotionEvent.getX();
    }

    boolean isVertical() {
        return !isHorizontal();
    }

    boolean isUp() {
        recalculateData();
        return !this.mRecentMotionEvents.isEmpty() && this.mLastMotionEvent.getY() < this.mFirstRecentMotionEvent.getY();
    }

    boolean isWirelessCharging() {
        return this.mBatteryController.isWirelessCharging();
    }

    private void recalculateData() {
        if (this.mDirty) {
            if (this.mRecentMotionEvents.isEmpty()) {
                this.mFirstRecentMotionEvent = null;
                this.mLastMotionEvent = null;
            } else {
                this.mFirstRecentMotionEvent = this.mRecentMotionEvents.get(0);
                this.mLastMotionEvent = this.mRecentMotionEvents.get(r0.size() - 1);
            }
            calculateAngleInternal();
            this.mDirty = false;
        }
    }

    private void calculateAngleInternal() {
        if (this.mRecentMotionEvents.size() < 2) {
            this.mAngle = Float.MAX_VALUE;
            return;
        }
        this.mAngle = (float) Math.atan2(this.mLastMotionEvent.getY() - this.mFirstRecentMotionEvent.getY(), this.mLastMotionEvent.getX() - this.mFirstRecentMotionEvent.getX());
        while (true) {
            float f = this.mAngle;
            if (f >= 0.0f) {
                break;
            } else {
                this.mAngle = f + 6.2831855f;
            }
        }
        while (true) {
            float f2 = this.mAngle;
            if (f2 <= 6.2831855f) {
                return;
            } else {
                this.mAngle = f2 - 6.2831855f;
            }
        }
    }

    private List<MotionEvent> unpackMotionEvent(MotionEvent motionEvent) {
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        int pointerCount = motionEvent.getPointerCount();
        int i = 0;
        for (int i2 = 0; i2 < pointerCount; i2++) {
            MotionEvent.PointerProperties pointerProperties = new MotionEvent.PointerProperties();
            motionEvent.getPointerProperties(i2, pointerProperties);
            arrayList2.add(pointerProperties);
        }
        MotionEvent.PointerProperties[] pointerPropertiesArr = new MotionEvent.PointerProperties[arrayList2.size()];
        arrayList2.toArray(pointerPropertiesArr);
        int historySize = motionEvent.getHistorySize();
        int i3 = 0;
        while (i3 < historySize) {
            ArrayList arrayList3 = new ArrayList();
            for (int i4 = i; i4 < pointerCount; i4++) {
                MotionEvent.PointerCoords pointerCoords = new MotionEvent.PointerCoords();
                motionEvent.getHistoricalPointerCoords(i4, i3, pointerCoords);
                arrayList3.add(pointerCoords);
            }
            arrayList.add(MotionEvent.obtain(motionEvent.getDownTime(), motionEvent.getHistoricalEventTime(i3), motionEvent.getAction(), pointerCount, pointerPropertiesArr, (MotionEvent.PointerCoords[]) arrayList3.toArray(new MotionEvent.PointerCoords[i]), motionEvent.getMetaState(), motionEvent.getButtonState(), motionEvent.getXPrecision(), motionEvent.getYPrecision(), motionEvent.getDeviceId(), motionEvent.getEdgeFlags(), motionEvent.getSource(), motionEvent.getFlags()));
            i3++;
            pointerPropertiesArr = pointerPropertiesArr;
            i = i;
            pointerCount = pointerCount;
        }
        arrayList.add(MotionEvent.obtainNoHistory(motionEvent));
        return arrayList;
    }

    void onSessionEnd() {
        this.mFirstActualMotionEvent = null;
        Iterator<MotionEvent> it = this.mRecentMotionEvents.iterator();
        while (it.hasNext()) {
            it.next().recycle();
        }
        this.mRecentMotionEvents.clear();
        this.mDirty = true;
    }
}
