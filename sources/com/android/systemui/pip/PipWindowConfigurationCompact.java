package com.android.systemui.pip;

import android.app.WindowConfiguration;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;

/* loaded from: classes.dex */
class PipWindowConfigurationCompact {
    private Rect mBounds;
    private int mRotation;

    PipWindowConfigurationCompact(WindowConfiguration windowConfiguration) {
        this.mRotation = windowConfiguration.getRotation();
        this.mBounds = windowConfiguration.getBounds();
    }

    int getRotation() {
        return this.mRotation;
    }

    Rect getBounds() {
        return this.mBounds;
    }

    void syncWithScreenOrientation(int i, int i2) {
        int i3;
        int i4;
        Rect rect = this.mBounds;
        if (rect.top == 0 && rect.left == 0) {
            boolean z = true;
            if (ActivityInfo.isFixedOrientationPortrait(i) && ((i4 = this.mRotation) == 1 || i4 == 3)) {
                this.mRotation = 0;
            } else if (ActivityInfo.isFixedOrientationLandscape(i) && ((i3 = this.mRotation) == 0 || i3 == 2)) {
                this.mRotation = 1;
            } else if (i != -1 || this.mRotation == i2) {
                z = false;
            } else {
                this.mRotation = i2;
            }
            if (z) {
                Rect rect2 = this.mBounds;
                rect2.set(0, 0, rect2.height(), this.mBounds.width());
            }
        }
    }

    public String toString() {
        return "PipWindowConfigurationCompact(rotation=" + this.mRotation + " bounds=" + this.mBounds + ")";
    }
}
