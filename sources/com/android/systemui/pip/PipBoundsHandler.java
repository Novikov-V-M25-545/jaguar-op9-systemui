package com.android.systemui.pip;

import android.R;
import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.DisplayInfo;
import android.view.Gravity;
import android.window.WindowContainerTransaction;
import com.android.systemui.wm.DisplayController;
import com.android.systemui.wm.DisplayLayout;
import java.io.PrintWriter;

/* loaded from: classes.dex */
public class PipBoundsHandler {
    private static final String TAG = "PipBoundsHandler";
    private float mAspectRatio;
    private final Context mContext;
    private int mCurrentMinSize;
    private float mDefaultAspectRatio;
    private int mDefaultMinSize;
    private int mDefaultStackGravity;
    private final DisplayController mDisplayController;
    private final DisplayLayout mDisplayLayout;
    private final DisplayController.OnDisplaysChangedListener mDisplaysChangedListener;
    private int mImeHeight;
    private boolean mIsImeShowing;
    private boolean mIsShelfShowing;
    private ComponentName mLastPipComponentName;
    private float mMaxAspectRatio;
    private float mMinAspectRatio;
    private Size mOverrideMinimalSize;
    private Size mReentrySize;
    private Point mScreenEdgeInsets;
    private int mShelfHeight;
    private final PipSnapAlgorithm mSnapAlgorithm;
    private final DisplayInfo mDisplayInfo = new DisplayInfo();
    private float mReentrySnapFraction = -1.0f;

    public PipBoundsHandler(Context context, PipSnapAlgorithm pipSnapAlgorithm, DisplayController displayController) throws Resources.NotFoundException {
        DisplayController.OnDisplaysChangedListener onDisplaysChangedListener = new DisplayController.OnDisplaysChangedListener() { // from class: com.android.systemui.pip.PipBoundsHandler.1
            @Override // com.android.systemui.wm.DisplayController.OnDisplaysChangedListener
            public void onDisplayAdded(int i) {
                if (i == PipBoundsHandler.this.mContext.getDisplayId()) {
                    PipBoundsHandler.this.mDisplayLayout.set(PipBoundsHandler.this.mDisplayController.getDisplayLayout(i));
                }
            }
        };
        this.mDisplaysChangedListener = onDisplaysChangedListener;
        this.mContext = context;
        this.mSnapAlgorithm = pipSnapAlgorithm;
        this.mDisplayLayout = new DisplayLayout();
        this.mDisplayController = displayController;
        displayController.addDisplayWindowListener(onDisplaysChangedListener);
        reloadResources();
        this.mAspectRatio = this.mDefaultAspectRatio;
    }

    private void reloadResources() throws Resources.NotFoundException {
        Point point;
        Resources resources = this.mContext.getResources();
        this.mDefaultAspectRatio = resources.getFloat(R.dimen.chooser_preview_image_font_size);
        this.mDefaultStackGravity = resources.getInteger(R.integer.config_carDockRotation);
        int dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.controls_thumbnail_image_max_height);
        this.mDefaultMinSize = dimensionPixelSize;
        this.mCurrentMinSize = dimensionPixelSize;
        String string = resources.getString(R.string.config_clockFontFamily);
        if ((!string.isEmpty() ? Size.parseSize(string) : null) == null) {
            point = new Point();
        } else {
            point = new Point(dpToPx(r1.getWidth(), resources.getDisplayMetrics()), dpToPx(r1.getHeight(), resources.getDisplayMetrics()));
        }
        this.mScreenEdgeInsets = point;
        this.mMinAspectRatio = resources.getFloat(R.dimen.chooser_row_text_option_translate);
        this.mMaxAspectRatio = resources.getFloat(R.dimen.chooser_preview_width);
    }

    public void setMinEdgeSize(int i) {
        this.mCurrentMinSize = i;
    }

    public boolean setShelfHeight(boolean z, int i) {
        if ((z && i > 0) == this.mIsShelfShowing && i == this.mShelfHeight) {
            return false;
        }
        this.mIsShelfShowing = z;
        this.mShelfHeight = i;
        return true;
    }

    public void onImeVisibilityChanged(boolean z, int i) {
        this.mIsImeShowing = z;
        this.mImeHeight = i;
    }

    public void onMovementBoundsChanged(Rect rect, Rect rect2, Rect rect3, DisplayInfo displayInfo) {
        getInsetBounds(rect);
        Rect defaultBounds = getDefaultBounds(-1.0f, null);
        rect2.set(defaultBounds);
        if (rect3.isEmpty()) {
            rect3.set(defaultBounds);
        }
        if (isValidPictureInPictureAspectRatio(this.mAspectRatio)) {
            transformBoundsToAspectRatio(rect2, this.mAspectRatio, false, false);
        }
        displayInfo.copyFrom(this.mDisplayInfo);
    }

    public void onSaveReentryBounds(ComponentName componentName, Rect rect) {
        this.mReentrySnapFraction = getSnapFraction(rect);
        this.mReentrySize = new Size(rect.width(), rect.height());
        this.mLastPipComponentName = componentName;
    }

    public void onResetReentryBounds(ComponentName componentName) {
        if (componentName.equals(this.mLastPipComponentName)) {
            onResetReentryBoundsUnchecked();
        }
    }

    private void onResetReentryBoundsUnchecked() {
        this.mReentrySnapFraction = -1.0f;
        this.mReentrySize = null;
        this.mLastPipComponentName = null;
    }

    public boolean hasSaveReentryBounds() {
        return this.mReentrySnapFraction != -1.0f;
    }

    public Rect getDisplayBounds() {
        DisplayInfo displayInfo = this.mDisplayInfo;
        return new Rect(0, 0, displayInfo.logicalWidth, displayInfo.logicalHeight);
    }

    public int getDisplayRotation() {
        return this.mDisplayInfo.rotation;
    }

    public void onDisplayInfoChanged(DisplayInfo displayInfo) {
        this.mDisplayInfo.copyFrom(displayInfo);
    }

    public void onConfigurationChanged() throws Resources.NotFoundException {
        reloadResources();
    }

    public void onAspectRatioChanged(float f) {
        this.mAspectRatio = f;
    }

    Rect getDestinationBounds(ComponentName componentName, float f, Rect rect, Size size) {
        return getDestinationBounds(componentName, f, rect, size, false);
    }

    Rect getDestinationBounds(ComponentName componentName, float f, Rect rect, Size size, boolean z) {
        Rect rect2;
        if (!componentName.equals(this.mLastPipComponentName)) {
            onResetReentryBoundsUnchecked();
            this.mLastPipComponentName = componentName;
        }
        if (rect == null) {
            rect2 = new Rect(getDefaultBounds(this.mReentrySnapFraction, this.mReentrySize));
            if (this.mReentrySnapFraction == -1.0f && this.mReentrySize == null) {
                this.mOverrideMinimalSize = size;
            }
        } else {
            rect2 = new Rect(rect);
        }
        if (isValidPictureInPictureAspectRatio(f)) {
            transformBoundsToAspectRatio(rect2, f, z, rect == null && this.mReentrySize != null);
        }
        this.mAspectRatio = f;
        return rect2;
    }

    float getDefaultAspectRatio() {
        return this.mDefaultAspectRatio;
    }

    public void onDisplayRotationChangedNotInPip(int i) {
        this.mDisplayLayout.rotateTo(this.mContext.getResources(), i);
        this.mDisplayInfo.rotation = i;
        updateDisplayInfoIfNeeded();
    }

    public boolean onDisplayRotationChanged(Rect rect, Rect rect2, Rect rect3, int i, int i2, int i3, WindowContainerTransaction windowContainerTransaction) {
        if (i == this.mDisplayInfo.displayId && i2 != i3) {
            try {
                ActivityManager.StackInfo stackInfo = ActivityTaskManager.getService().getStackInfo(2, 0);
                if (stackInfo == null) {
                    return false;
                }
                Rect rect4 = new Rect(rect2);
                float snapFraction = getSnapFraction(rect4);
                this.mDisplayLayout.rotateTo(this.mContext.getResources(), i3);
                this.mDisplayInfo.rotation = i3;
                updateDisplayInfoIfNeeded();
                this.mSnapAlgorithm.applySnapFraction(rect4, getMovementBounds(rect4, false), snapFraction);
                getInsetBounds(rect3);
                rect.set(rect4);
                windowContainerTransaction.setBounds(stackInfo.stackToken, rect);
                return true;
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to get StackInfo for pinned stack", e);
            }
        }
        return false;
    }

    private void updateDisplayInfoIfNeeded() {
        DisplayInfo displayInfo = this.mDisplayInfo;
        int i = displayInfo.rotation;
        boolean z = true;
        if (i == 0 || i == 2 ? displayInfo.logicalWidth <= displayInfo.logicalHeight : displayInfo.logicalWidth >= displayInfo.logicalHeight) {
            z = false;
        }
        if (z) {
            int i2 = displayInfo.logicalWidth;
            displayInfo.logicalWidth = displayInfo.logicalHeight;
            displayInfo.logicalHeight = i2;
        }
    }

    private boolean isValidPictureInPictureAspectRatio(float f) {
        return Float.compare(this.mMinAspectRatio, f) <= 0 && Float.compare(f, this.mMaxAspectRatio) <= 0;
    }

    public void transformBoundsToAspectRatio(Rect rect) {
        transformBoundsToAspectRatio(rect, this.mAspectRatio, true, true);
    }

    private void transformBoundsToAspectRatio(Rect rect, float f, boolean z, boolean z2) {
        Size sizeForAspectRatio;
        float snapFraction = this.mSnapAlgorithm.getSnapFraction(rect, getMovementBounds(rect));
        int i = z ? this.mCurrentMinSize : this.mDefaultMinSize;
        if (z || z2) {
            sizeForAspectRatio = this.mSnapAlgorithm.getSizeForAspectRatio(new Size(rect.width(), rect.height()), f, i);
        } else {
            PipSnapAlgorithm pipSnapAlgorithm = this.mSnapAlgorithm;
            float f2 = i;
            DisplayInfo displayInfo = this.mDisplayInfo;
            sizeForAspectRatio = pipSnapAlgorithm.getSizeForAspectRatio(f, f2, displayInfo.logicalWidth, displayInfo.logicalHeight);
        }
        int iCenterX = (int) (rect.centerX() - (sizeForAspectRatio.getWidth() / 2.0f));
        int iCenterY = (int) (rect.centerY() - (sizeForAspectRatio.getHeight() / 2.0f));
        rect.set(iCenterX, iCenterY, sizeForAspectRatio.getWidth() + iCenterX, sizeForAspectRatio.getHeight() + iCenterY);
        Size size = this.mOverrideMinimalSize;
        if (size != null) {
            transformBoundsToMinimalSize(rect, f, size);
        }
        this.mSnapAlgorithm.applySnapFraction(rect, getMovementBounds(rect), snapFraction);
    }

    private void transformBoundsToMinimalSize(Rect rect, float f, Size size) {
        Size size2;
        if (size == null) {
            return;
        }
        if (size.getWidth() / size.getHeight() > f) {
            size2 = new Size(size.getWidth(), (int) (size.getWidth() / f));
        } else {
            size2 = new Size((int) (size.getHeight() * f), size.getHeight());
        }
        Gravity.apply(this.mDefaultStackGravity, size2.getWidth(), size2.getHeight(), new Rect(rect), rect);
    }

    private Rect getDefaultBounds(float f, Size size) {
        Rect rect = new Rect();
        if (f != -1.0f && size != null) {
            rect.set(0, 0, size.getWidth(), size.getHeight());
            this.mSnapAlgorithm.applySnapFraction(rect, getMovementBounds(rect), f);
        } else {
            Rect rect2 = new Rect();
            getInsetBounds(rect2);
            PipSnapAlgorithm pipSnapAlgorithm = this.mSnapAlgorithm;
            float f2 = this.mDefaultAspectRatio;
            float f3 = this.mDefaultMinSize;
            DisplayInfo displayInfo = this.mDisplayInfo;
            Size sizeForAspectRatio = pipSnapAlgorithm.getSizeForAspectRatio(f2, f3, displayInfo.logicalWidth, displayInfo.logicalHeight);
            Gravity.apply(this.mDefaultStackGravity, sizeForAspectRatio.getWidth(), sizeForAspectRatio.getHeight(), rect2, 0, Math.max(this.mIsImeShowing ? this.mImeHeight : 0, this.mIsShelfShowing ? this.mShelfHeight : 0), rect);
        }
        return rect;
    }

    protected void getInsetBounds(Rect rect) {
        Rect rectStableInsets = this.mDisplayLayout.stableInsets();
        int i = rectStableInsets.left;
        Point point = this.mScreenEdgeInsets;
        int i2 = point.x;
        int i3 = rectStableInsets.top;
        int i4 = point.y;
        DisplayInfo displayInfo = this.mDisplayInfo;
        rect.set(i + i2, i3 + i4, (displayInfo.logicalWidth - rectStableInsets.right) - i2, (displayInfo.logicalHeight - rectStableInsets.bottom) - i4);
    }

    private Rect getMovementBounds(Rect rect) {
        return getMovementBounds(rect, true);
    }

    private Rect getMovementBounds(Rect rect, boolean z) {
        Rect rect2 = new Rect();
        getInsetBounds(rect2);
        this.mSnapAlgorithm.getMovementBounds(rect, rect2, rect2, (z && this.mIsImeShowing) ? this.mImeHeight : 0);
        return rect2;
    }

    public float getSnapFraction(Rect rect) {
        return this.mSnapAlgorithm.getSnapFraction(rect, getMovementBounds(rect));
    }

    public void applySnapFraction(Rect rect, float f) {
        this.mSnapAlgorithm.applySnapFraction(rect, getMovementBounds(rect), f);
    }

    private int dpToPx(float f, DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(1, f, displayMetrics);
    }

    public void dump(PrintWriter printWriter, String str) {
        String str2 = str + "  ";
        printWriter.println(str + TAG);
        printWriter.println(str2 + "mLastPipComponentName=" + this.mLastPipComponentName);
        printWriter.println(str2 + "mReentrySnapFraction=" + this.mReentrySnapFraction);
        printWriter.println(str2 + "mReentrySize=" + this.mReentrySize);
        printWriter.println(str2 + "mDisplayInfo=" + this.mDisplayInfo);
        printWriter.println(str2 + "mDefaultAspectRatio=" + this.mDefaultAspectRatio);
        printWriter.println(str2 + "mMinAspectRatio=" + this.mMinAspectRatio);
        printWriter.println(str2 + "mMaxAspectRatio=" + this.mMaxAspectRatio);
        printWriter.println(str2 + "mAspectRatio=" + this.mAspectRatio);
        printWriter.println(str2 + "mDefaultStackGravity=" + this.mDefaultStackGravity);
        printWriter.println(str2 + "mIsImeShowing=" + this.mIsImeShowing);
        printWriter.println(str2 + "mImeHeight=" + this.mImeHeight);
        printWriter.println(str2 + "mIsShelfShowing=" + this.mIsShelfShowing);
        printWriter.println(str2 + "mShelfHeight=" + this.mShelfHeight);
    }
}
