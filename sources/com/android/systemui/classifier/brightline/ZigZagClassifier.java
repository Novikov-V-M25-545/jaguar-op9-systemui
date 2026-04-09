package com.android.systemui.classifier.brightline;

import android.graphics.Point;
import android.view.MotionEvent;
import com.android.systemui.util.DeviceConfigProxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
class ZigZagClassifier extends FalsingClassifier {
    private float mLastDevianceX;
    private float mLastDevianceY;
    private float mLastMaxXDeviance;
    private float mLastMaxYDeviance;
    private final float mMaxXPrimaryDeviance;
    private final float mMaxXSecondaryDeviance;
    private final float mMaxYPrimaryDeviance;
    private final float mMaxYSecondaryDeviance;

    ZigZagClassifier(FalsingDataProvider falsingDataProvider, DeviceConfigProxy deviceConfigProxy) {
        super(falsingDataProvider);
        this.mMaxXPrimaryDeviance = deviceConfigProxy.getFloat("systemui", "brightline_falsing_zigzag_x_primary_deviance", 0.05f);
        this.mMaxYPrimaryDeviance = deviceConfigProxy.getFloat("systemui", "brightline_falsing_zigzag_y_primary_deviance", 0.15f);
        this.mMaxXSecondaryDeviance = deviceConfigProxy.getFloat("systemui", "brightline_falsing_zigzag_x_secondary_deviance", 0.4f);
        this.mMaxYSecondaryDeviance = deviceConfigProxy.getFloat("systemui", "brightline_falsing_zigzag_y_secondary_deviance", 0.3f);
    }

    @Override // com.android.systemui.classifier.brightline.FalsingClassifier
    boolean isFalseTouch() {
        List<Point> listRotateVertical;
        float xdpi;
        float f;
        float ydpi;
        if (getRecentMotionEvents().size() < 3) {
            return false;
        }
        if (isHorizontal()) {
            listRotateVertical = rotateHorizontal();
        } else {
            listRotateVertical = rotateVertical();
        }
        float fAbs = Math.abs(listRotateVertical.get(0).x - listRotateVertical.get(listRotateVertical.size() - 1).x);
        float fAbs2 = Math.abs(listRotateVertical.get(0).y - listRotateVertical.get(listRotateVertical.size() - 1).y);
        FalsingClassifier.logDebug("Actual: (" + fAbs + "," + fAbs2 + ")");
        float fAbs3 = 0.0f;
        boolean z = true;
        float fAbs4 = 0.0f;
        float f2 = 0.0f;
        float f3 = 0.0f;
        for (Point point : listRotateVertical) {
            if (z) {
                f2 = point.x;
                f3 = point.y;
                z = false;
            } else {
                fAbs3 += Math.abs(point.x - f2);
                fAbs4 += Math.abs(point.y - f3);
                f2 = point.x;
                f3 = point.y;
                FalsingClassifier.logDebug("(x, y, runningAbsDx, runningAbsDy) - (" + f2 + ", " + f3 + ", " + fAbs3 + ", " + fAbs4 + ")");
            }
        }
        float f4 = fAbs3 - fAbs;
        float f5 = fAbs4 - fAbs2;
        float xdpi2 = fAbs / getXdpi();
        float ydpi2 = fAbs2 / getYdpi();
        float fSqrt = (float) Math.sqrt((xdpi2 * xdpi2) + (ydpi2 * ydpi2));
        if (fAbs > fAbs2) {
            xdpi = this.mMaxXPrimaryDeviance * fSqrt * getXdpi();
            f = this.mMaxYSecondaryDeviance * fSqrt;
            ydpi = getYdpi();
        } else {
            xdpi = this.mMaxXSecondaryDeviance * fSqrt * getXdpi();
            f = this.mMaxYPrimaryDeviance * fSqrt;
            ydpi = getYdpi();
        }
        float f6 = f * ydpi;
        this.mLastDevianceX = f4;
        this.mLastDevianceY = f5;
        this.mLastMaxXDeviance = xdpi;
        this.mLastMaxYDeviance = f6;
        FalsingClassifier.logDebug("Straightness Deviance: (" + f4 + "," + f5 + ") vs (" + xdpi + "," + f6 + ")");
        return f4 > xdpi || f5 > f6;
    }

    @Override // com.android.systemui.classifier.brightline.FalsingClassifier
    String getReason() {
        return String.format(null, "{devianceX=%f, maxDevianceX=%s, devianceY=%s, maxDevianceY=%s}", Float.valueOf(this.mLastDevianceX), Float.valueOf(this.mLastMaxXDeviance), Float.valueOf(this.mLastDevianceY), Float.valueOf(this.mLastMaxYDeviance));
    }

    private float getAtan2LastPoint() {
        MotionEvent firstMotionEvent = getFirstMotionEvent();
        MotionEvent lastMotionEvent = getLastMotionEvent();
        float x = firstMotionEvent.getX();
        return (float) Math.atan2(lastMotionEvent.getY() - firstMotionEvent.getY(), lastMotionEvent.getX() - x);
    }

    private List<Point> rotateVertical() {
        double atan2LastPoint = 1.5707963267948966d - getAtan2LastPoint();
        FalsingClassifier.logDebug("Rotating to vertical by: " + atan2LastPoint);
        return rotateMotionEvents(getRecentMotionEvents(), -atan2LastPoint);
    }

    private List<Point> rotateHorizontal() {
        double atan2LastPoint = getAtan2LastPoint();
        FalsingClassifier.logDebug("Rotating to horizontal by: " + atan2LastPoint);
        return rotateMotionEvents(getRecentMotionEvents(), atan2LastPoint);
    }

    private List<Point> rotateMotionEvents(List<MotionEvent> list, double d) {
        ArrayList arrayList = new ArrayList();
        double dCos = Math.cos(d);
        double dSin = Math.sin(d);
        MotionEvent motionEvent = list.get(0);
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        for (Iterator<MotionEvent> it = list.iterator(); it.hasNext(); it = it) {
            MotionEvent next = it.next();
            double x2 = next.getX() - x;
            double y2 = next.getY() - y;
            arrayList.add(new Point((int) ((dCos * x2) + (dSin * y2) + x), (int) (((-dSin) * x2) + (y2 * dCos) + y)));
            motionEvent = motionEvent;
        }
        MotionEvent motionEvent2 = motionEvent;
        MotionEvent motionEvent3 = list.get(list.size() - 1);
        Point point = (Point) arrayList.get(0);
        Point point2 = (Point) arrayList.get(arrayList.size() - 1);
        FalsingClassifier.logDebug("Before: (" + motionEvent2.getX() + "," + motionEvent2.getY() + "), (" + motionEvent3.getX() + "," + motionEvent3.getY() + ")");
        FalsingClassifier.logDebug("After: (" + point.x + "," + point.y + "), (" + point2.x + "," + point2.y + ")");
        return arrayList;
    }
}
