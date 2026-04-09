package com.android.launcher3.icons;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.UserHandle;
import com.android.launcher3.icons.BitmapInfo;

/* loaded from: classes.dex */
public class BaseIconFactory implements AutoCloseable {
    protected static final boolean ATLEAST_OREO;
    static final boolean ATLEAST_P;
    private boolean mBadgeOnLeft;
    private final Canvas mCanvas;
    private final ColorExtractor mColorExtractor;
    protected final Context mContext;
    private boolean mDisableColorExtractor;
    protected final int mFillResIconDpi;
    protected final int mIconBitmapSize;
    private IconNormalizer mNormalizer;
    private final Rect mOldBounds;
    private final PackageManager mPm;
    private ShadowGenerator mShadowGenerator;
    private final boolean mShapeDetection;
    protected int mWrapperBackgroundColor;
    public Drawable mWrapperIcon;

    public static int getBadgeSizeForIconSize(int i) {
        return (int) (i * 0.444f);
    }

    static {
        int i = Build.VERSION.SDK_INT;
        ATLEAST_OREO = i >= 26;
        ATLEAST_P = i >= 28;
    }

    protected BaseIconFactory(Context context, int i, int i2, boolean z) {
        this.mOldBounds = new Rect();
        this.mBadgeOnLeft = false;
        this.mWrapperBackgroundColor = -1;
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        this.mShapeDetection = z;
        this.mFillResIconDpi = i;
        this.mIconBitmapSize = i2;
        this.mPm = applicationContext.getPackageManager();
        this.mColorExtractor = new ColorExtractor();
        Canvas canvas = new Canvas();
        this.mCanvas = canvas;
        canvas.setDrawFilter(new PaintFlagsDrawFilter(4, 2));
        clear();
    }

    protected BaseIconFactory(Context context, int i, int i2) {
        this(context, i, i2, false);
    }

    protected void clear() {
        this.mWrapperBackgroundColor = -1;
        this.mDisableColorExtractor = false;
        this.mBadgeOnLeft = false;
    }

    public ShadowGenerator getShadowGenerator() {
        if (this.mShadowGenerator == null) {
            this.mShadowGenerator = new ShadowGenerator(this.mIconBitmapSize);
        }
        return this.mShadowGenerator;
    }

    public IconNormalizer getNormalizer() {
        if (this.mNormalizer == null) {
            this.mNormalizer = new IconNormalizer(this.mContext, this.mIconBitmapSize, this.mShapeDetection);
        }
        return this.mNormalizer;
    }

    public BitmapInfo createIconBitmap(Bitmap bitmap) {
        if (this.mIconBitmapSize != bitmap.getWidth() || this.mIconBitmapSize != bitmap.getHeight()) {
            bitmap = createIconBitmap(new BitmapDrawable(this.mContext.getResources(), bitmap), 1.0f);
        }
        return BitmapInfo.of(bitmap, extractColor(bitmap));
    }

    public BitmapInfo createBadgedIconBitmap(Drawable drawable, UserHandle userHandle, boolean z) {
        return createBadgedIconBitmap(drawable, userHandle, z, false, null);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public BitmapInfo createBadgedIconBitmap(Drawable drawable, UserHandle userHandle, boolean z, boolean z2, float[] fArr) {
        if (fArr == null) {
            fArr = new float[1];
        }
        Drawable drawableNormalizeAndWrapToAdaptiveIcon = normalizeAndWrapToAdaptiveIcon(drawable, z, null, fArr);
        Bitmap bitmapCreateIconBitmap = createIconBitmap(drawableNormalizeAndWrapToAdaptiveIcon, fArr[0]);
        if (ATLEAST_OREO && (drawableNormalizeAndWrapToAdaptiveIcon instanceof AdaptiveIconDrawable)) {
            this.mCanvas.setBitmap(bitmapCreateIconBitmap);
            getShadowGenerator().recreateIcon(Bitmap.createBitmap(bitmapCreateIconBitmap), this.mCanvas);
            this.mCanvas.setBitmap(null);
        }
        if (z2) {
            badgeWithDrawable(bitmapCreateIconBitmap, this.mContext.getDrawable(R$drawable.ic_instant_app_badge));
        }
        if (userHandle != null) {
            Drawable userBadgedIcon = this.mPm.getUserBadgedIcon(new FixedSizeBitmapDrawable(bitmapCreateIconBitmap), userHandle);
            if (userBadgedIcon instanceof BitmapDrawable) {
                bitmapCreateIconBitmap = ((BitmapDrawable) userBadgedIcon).getBitmap();
            } else {
                bitmapCreateIconBitmap = createIconBitmap(userBadgedIcon, 1.0f);
            }
        }
        int iExtractColor = extractColor(bitmapCreateIconBitmap);
        if (drawableNormalizeAndWrapToAdaptiveIcon instanceof BitmapInfo.Extender) {
            return ((BitmapInfo.Extender) drawableNormalizeAndWrapToAdaptiveIcon).getExtendedInfo(bitmapCreateIconBitmap, iExtractColor, this);
        }
        return BitmapInfo.of(bitmapCreateIconBitmap, iExtractColor);
    }

    protected Drawable normalizeAndWrapToAdaptiveIcon(Drawable drawable, boolean z, RectF rectF, float[] fArr) {
        float scale;
        if (drawable == null) {
            return null;
        }
        if (z && ATLEAST_OREO) {
            if (this.mWrapperIcon == null) {
                this.mWrapperIcon = this.mContext.getDrawable(R$drawable.adaptive_icon_drawable_wrapper).mutate();
            }
            AdaptiveIconDrawable adaptiveIconDrawable = (AdaptiveIconDrawable) this.mWrapperIcon;
            adaptiveIconDrawable.setBounds(0, 0, 1, 1);
            boolean[] zArr = new boolean[1];
            scale = getNormalizer().getScale(drawable, rectF, adaptiveIconDrawable.getIconMask(), zArr);
            if (!(drawable instanceof AdaptiveIconDrawable) && !zArr[0] && (drawable.getChangingConfigurations() & 16777216) == 0) {
                adaptiveIconDrawable.setAlpha(drawable.getAlpha());
                drawable.setAlpha(255);
                FixedScaleDrawable fixedScaleDrawable = (FixedScaleDrawable) adaptiveIconDrawable.getForeground();
                fixedScaleDrawable.setDrawable(drawable);
                fixedScaleDrawable.setScale(scale);
                scale = getNormalizer().getScale(adaptiveIconDrawable, rectF, null, null);
                ((ColorDrawable) adaptiveIconDrawable.getBackground()).setColor(this.mWrapperBackgroundColor);
                drawable = adaptiveIconDrawable;
            }
        } else {
            scale = getNormalizer().getScale(drawable, rectF, null, null);
        }
        fArr[0] = scale;
        return drawable;
    }

    public void badgeWithDrawable(Bitmap bitmap, Drawable drawable) {
        this.mCanvas.setBitmap(bitmap);
        badgeWithDrawable(this.mCanvas, drawable);
        this.mCanvas.setBitmap(null);
    }

    public void badgeWithDrawable(Canvas canvas, Drawable drawable) {
        int badgeSizeForIconSize = getBadgeSizeForIconSize(this.mIconBitmapSize);
        if (this.mBadgeOnLeft) {
            int i = this.mIconBitmapSize;
            drawable.setBounds(0, i - badgeSizeForIconSize, badgeSizeForIconSize, i);
        } else {
            int i2 = this.mIconBitmapSize;
            drawable.setBounds(i2 - badgeSizeForIconSize, i2 - badgeSizeForIconSize, i2, i2);
        }
        drawable.draw(canvas);
    }

    private Bitmap createIconBitmap(Drawable drawable, float f) {
        return createIconBitmap(drawable, f, this.mIconBitmapSize);
    }

    /* JADX WARN: Removed duplicated region for block: B:25:0x0081  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public android.graphics.Bitmap createIconBitmap(android.graphics.drawable.Drawable r6, float r7, int r8) {
        /*
            r5 = this;
            android.graphics.Bitmap$Config r0 = android.graphics.Bitmap.Config.ARGB_8888
            android.graphics.Bitmap r0 = android.graphics.Bitmap.createBitmap(r8, r8, r0)
            if (r6 != 0) goto L9
            return r0
        L9:
            android.graphics.Canvas r1 = r5.mCanvas
            r1.setBitmap(r0)
            android.graphics.Rect r1 = r5.mOldBounds
            android.graphics.Rect r2 = r6.getBounds()
            r1.set(r2)
            boolean r1 = com.android.launcher3.icons.BaseIconFactory.ATLEAST_OREO
            if (r1 == 0) goto L43
            boolean r1 = r6 instanceof android.graphics.drawable.AdaptiveIconDrawable
            if (r1 == 0) goto L43
            r1 = 1009429163(0x3c2aaaab, float:0.010416667)
            float r2 = (float) r8
            float r1 = r1 * r2
            double r3 = (double) r1
            double r3 = java.lang.Math.ceil(r3)
            int r1 = (int) r3
            r3 = 1065353216(0x3f800000, float:1.0)
            float r3 = r3 - r7
            float r2 = r2 * r3
            r7 = 1073741824(0x40000000, float:2.0)
            float r2 = r2 / r7
            int r7 = java.lang.Math.round(r2)
            int r7 = java.lang.Math.max(r1, r7)
            int r8 = r8 - r7
            r6.setBounds(r7, r7, r8, r8)
            android.graphics.Canvas r7 = r5.mCanvas
            r6.draw(r7)
            goto La7
        L43:
            boolean r1 = r6 instanceof android.graphics.drawable.BitmapDrawable
            if (r1 == 0) goto L63
            r1 = r6
            android.graphics.drawable.BitmapDrawable r1 = (android.graphics.drawable.BitmapDrawable) r1
            android.graphics.Bitmap r2 = r1.getBitmap()
            if (r0 == 0) goto L63
            int r2 = r2.getDensity()
            if (r2 != 0) goto L63
            android.content.Context r2 = r5.mContext
            android.content.res.Resources r2 = r2.getResources()
            android.util.DisplayMetrics r2 = r2.getDisplayMetrics()
            r1.setTargetDensity(r2)
        L63:
            int r1 = r6.getIntrinsicWidth()
            int r2 = r6.getIntrinsicHeight()
            if (r1 <= 0) goto L81
            if (r2 <= 0) goto L81
            float r3 = (float) r1
            float r4 = (float) r2
            float r3 = r3 / r4
            if (r1 <= r2) goto L7a
            float r1 = (float) r8
            float r1 = r1 / r3
            int r1 = (int) r1
            r2 = r1
            r1 = r8
            goto L83
        L7a:
            if (r2 <= r1) goto L81
            float r1 = (float) r8
            float r1 = r1 * r3
            int r1 = (int) r1
            r2 = r8
            goto L83
        L81:
            r1 = r8
            r2 = r1
        L83:
            int r3 = r8 - r1
            int r3 = r3 / 2
            int r4 = r8 - r2
            int r4 = r4 / 2
            int r1 = r1 + r3
            int r2 = r2 + r4
            r6.setBounds(r3, r4, r1, r2)
            android.graphics.Canvas r1 = r5.mCanvas
            r1.save()
            android.graphics.Canvas r1 = r5.mCanvas
            int r8 = r8 / 2
            float r8 = (float) r8
            r1.scale(r7, r7, r8, r8)
            android.graphics.Canvas r7 = r5.mCanvas
            r6.draw(r7)
            android.graphics.Canvas r7 = r5.mCanvas
            r7.restore()
        La7:
            android.graphics.Rect r7 = r5.mOldBounds
            r6.setBounds(r7)
            android.graphics.Canvas r5 = r5.mCanvas
            r6 = 0
            r5.setBitmap(r6)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.launcher3.icons.BaseIconFactory.createIconBitmap(android.graphics.drawable.Drawable, float, int):android.graphics.Bitmap");
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        clear();
    }

    private int extractColor(Bitmap bitmap) {
        if (this.mDisableColorExtractor) {
            return 0;
        }
        return this.mColorExtractor.findDominantColorByHue(bitmap);
    }

    private static class FixedSizeBitmapDrawable extends BitmapDrawable {
        public FixedSizeBitmapDrawable(Bitmap bitmap) {
            super((Resources) null, bitmap);
        }

        @Override // android.graphics.drawable.BitmapDrawable, android.graphics.drawable.Drawable
        public int getIntrinsicHeight() {
            return getBitmap().getWidth();
        }

        @Override // android.graphics.drawable.BitmapDrawable, android.graphics.drawable.Drawable
        public int getIntrinsicWidth() {
            return getBitmap().getWidth();
        }
    }
}
