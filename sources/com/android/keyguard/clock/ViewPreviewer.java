package com.android.keyguard.clock;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/* loaded from: classes.dex */
final class ViewPreviewer {
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    ViewPreviewer() {
    }

    Bitmap createPreview(final View view, final int i, final int i2) {
        if (view == null) {
            return null;
        }
        FutureTask futureTask = new FutureTask(new Callable<Bitmap>() { // from class: com.android.keyguard.clock.ViewPreviewer.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // java.util.concurrent.Callable
            public Bitmap call() {
                Bitmap bitmapCreateBitmap = Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmapCreateBitmap);
                canvas.drawColor(-16777216);
                ViewPreviewer.this.dispatchVisibilityAggregated(view, true);
                view.measure(View.MeasureSpec.makeMeasureSpec(i, 1073741824), View.MeasureSpec.makeMeasureSpec(i2, 1073741824));
                view.layout(0, 0, i, i2);
                view.draw(canvas);
                return bitmapCreateBitmap;
            }
        });
        if (Looper.myLooper() == Looper.getMainLooper()) {
            futureTask.run();
        } else {
            this.mMainHandler.post(futureTask);
        }
        try {
            return (Bitmap) futureTask.get();
        } catch (Exception e) {
            Log.e("ViewPreviewer", "Error completing task", e);
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchVisibilityAggregated(View view, boolean z) {
        boolean z2 = view.getVisibility() == 0;
        if (z2 || !z) {
            view.onVisibilityAggregated(z);
        }
        if (view instanceof ViewGroup) {
            boolean z3 = z2 && z;
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                dispatchVisibilityAggregated(viewGroup.getChildAt(i), z3);
            }
        }
    }
}
