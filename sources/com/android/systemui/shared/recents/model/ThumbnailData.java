package com.android.systemui.shared.recents.model;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.GraphicBuffer;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

/* loaded from: classes.dex */
public class ThumbnailData {
    public Rect insets;
    public boolean isRealSnapshot;
    public boolean isTranslucent;
    public int orientation;
    public boolean reducedResolution;
    public int rotation;
    public float scale;
    public long snapshotId;
    public int systemUiVisibility;
    public final Bitmap thumbnail;
    public int windowingMode;

    public ThumbnailData(ActivityManager.TaskSnapshot taskSnapshot) {
        GraphicBuffer snapshot = taskSnapshot.getSnapshot();
        if (snapshot == null || (snapshot.getUsage() & 256) == 0) {
            Log.e("ThumbnailData", "Unexpected snapshot without USAGE_GPU_SAMPLED_IMAGE: " + snapshot);
            Point taskSize = taskSnapshot.getTaskSize();
            Bitmap bitmapCreateBitmap = Bitmap.createBitmap(taskSize.x, taskSize.y, Bitmap.Config.ARGB_8888);
            this.thumbnail = bitmapCreateBitmap;
            bitmapCreateBitmap.eraseColor(-16777216);
        } else {
            this.thumbnail = Bitmap.wrapHardwareBuffer(snapshot, taskSnapshot.getColorSpace());
        }
        this.insets = new Rect(taskSnapshot.getContentInsets());
        this.orientation = taskSnapshot.getOrientation();
        this.rotation = taskSnapshot.getRotation();
        this.reducedResolution = taskSnapshot.isLowResolution();
        this.scale = this.thumbnail.getWidth() / taskSnapshot.getTaskSize().x;
        this.isRealSnapshot = taskSnapshot.isRealSnapshot();
        this.isTranslucent = taskSnapshot.isTranslucent();
        this.windowingMode = taskSnapshot.getWindowingMode();
        this.systemUiVisibility = taskSnapshot.getSystemUiVisibility();
        this.snapshotId = taskSnapshot.getId();
    }
}
