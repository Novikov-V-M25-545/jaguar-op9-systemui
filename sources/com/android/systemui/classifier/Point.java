package com.android.systemui.classifier;

/* loaded from: classes.dex */
public class Point {
    public long timeOffsetNano;
    public float x;
    public float y;

    public Point(float f, float f2) {
        this.x = f;
        this.y = f2;
        this.timeOffsetNano = 0L;
    }

    public Point(float f, float f2, long j) {
        this.x = f;
        this.y = f2;
        this.timeOffsetNano = j;
    }

    public boolean equals(Point point) {
        return this.x == point.x && this.y == point.y;
    }

    public float dist(Point point) {
        return (float) Math.hypot(point.x - this.x, point.y - this.y);
    }

    public float crossProduct(Point point, Point point2) {
        float f = point.x;
        float f2 = this.x;
        float f3 = point2.y;
        float f4 = this.y;
        return ((f - f2) * (f3 - f4)) - ((point.y - f4) * (point2.x - f2));
    }

    public float dotProduct(Point point, Point point2) {
        float f = point.x;
        float f2 = this.x;
        float f3 = (f - f2) * (point2.x - f2);
        float f4 = point.y;
        float f5 = this.y;
        return f3 + ((f4 - f5) * (point2.y - f5));
    }

    public float getAngle(Point point, Point point2) {
        float fDist = dist(point);
        float fDist2 = dist(point2);
        if (fDist == 0.0f || fDist2 == 0.0f) {
            return 0.0f;
        }
        float fCrossProduct = crossProduct(point, point2);
        float fAcos = (float) Math.acos(Math.min(1.0f, Math.max(-1.0f, (dotProduct(point, point2) / fDist) / fDist2)));
        return ((double) fCrossProduct) < 0.0d ? 6.2831855f - fAcos : fAcos;
    }
}
