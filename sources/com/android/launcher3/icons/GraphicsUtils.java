package com.android.launcher3.icons;

import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.RegionIterator;

/* loaded from: classes.dex */
public class GraphicsUtils {
    public static Runnable sOnNewBitmapRunnable = new Runnable() { // from class: com.android.launcher3.icons.GraphicsUtils$$ExternalSyntheticLambda0
        @Override // java.lang.Runnable
        public final void run() {
            GraphicsUtils.lambda$static$0();
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$static$0() {
    }

    public static int setColorAlphaBound(int i, int i2) {
        if (i2 < 0) {
            i2 = 0;
        } else if (i2 > 255) {
            i2 = 255;
        }
        return (i & 16777215) | (i2 << 24);
    }

    public static int getArea(Region region) {
        RegionIterator regionIterator = new RegionIterator(region);
        Rect rect = new Rect();
        int iWidth = 0;
        while (regionIterator.next(rect)) {
            iWidth += rect.width() * rect.height();
        }
        return iWidth;
    }

    public static void noteNewBitmapCreated() {
        sOnNewBitmapRunnable.run();
    }
}
