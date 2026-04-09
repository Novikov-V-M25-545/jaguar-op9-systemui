package com.android.systemui.bubbles;

import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import com.android.launcher3.icons.BaseIconFactory;
import com.android.launcher3.icons.BitmapInfo;
import com.android.launcher3.icons.R$dimen;
import com.android.launcher3.icons.ShadowGenerator;
import com.android.settingslib.R$color;
import com.android.systemui.R;

/* loaded from: classes.dex */
public class BubbleIconFactory extends BaseIconFactory {
    private int mBadgeSize;

    protected BubbleIconFactory(Context context) {
        super(context, context.getResources().getConfiguration().densityDpi, context.getResources().getDimensionPixelSize(R.dimen.individual_bubble_size));
        this.mBadgeSize = this.mContext.getResources().getDimensionPixelSize(R$dimen.profile_badge_size);
    }

    Drawable getBubbleDrawable(Context context, ShortcutInfo shortcutInfo, Icon icon) {
        if (shortcutInfo != null) {
            return ((LauncherApps) context.getSystemService("launcherapps")).getShortcutIconDrawable(shortcutInfo, context.getResources().getConfiguration().densityDpi);
        }
        if (icon == null) {
            return null;
        }
        if (icon.getType() == 4 || icon.getType() == 6) {
            context.grantUriPermission(context.getPackageName(), icon.getUri(), 1);
        }
        return icon.loadDrawable(context);
    }

    BitmapInfo getBadgeBitmap(Drawable drawable, boolean z) throws Resources.NotFoundException {
        ShadowGenerator shadowGenerator = new ShadowGenerator(this.mBadgeSize);
        Bitmap bitmapCreateIconBitmap = createIconBitmap(drawable, 1.0f, this.mBadgeSize);
        if (drawable instanceof AdaptiveIconDrawable) {
            Bitmap circleBitmap = getCircleBitmap((AdaptiveIconDrawable) drawable, drawable.getIntrinsicWidth());
            int i = this.mBadgeSize;
            bitmapCreateIconBitmap = Bitmap.createScaledBitmap(circleBitmap, i, i, true);
        }
        if (z) {
            float dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(android.R.dimen.docked_stack_divider_thickness);
            int color = this.mContext.getResources().getColor(R$color.important_conversation, null);
            Bitmap bitmapCreateBitmap = Bitmap.createBitmap(bitmapCreateIconBitmap.getWidth(), bitmapCreateIconBitmap.getHeight(), bitmapCreateIconBitmap.getConfig());
            Canvas canvas = new Canvas(bitmapCreateBitmap);
            int i2 = (int) dimensionPixelSize;
            int i3 = i2 * 2;
            float f = i2;
            canvas.drawBitmap(Bitmap.createScaledBitmap(bitmapCreateIconBitmap, canvas.getWidth() - i3, canvas.getHeight() - i3, true), f, f, (Paint) null);
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(color);
            paint.setAntiAlias(true);
            paint.setStrokeWidth(dimensionPixelSize);
            canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, (canvas.getWidth() / 2) - dimensionPixelSize, paint);
            shadowGenerator.recreateIcon(Bitmap.createBitmap(bitmapCreateBitmap), canvas);
            return createIconBitmap(bitmapCreateBitmap);
        }
        Canvas canvas2 = new Canvas();
        canvas2.setBitmap(bitmapCreateIconBitmap);
        shadowGenerator.recreateIcon(Bitmap.createBitmap(bitmapCreateIconBitmap), canvas2);
        return createIconBitmap(bitmapCreateIconBitmap);
    }

    public Bitmap getCircleBitmap(AdaptiveIconDrawable adaptiveIconDrawable, int i) {
        Drawable foreground = adaptiveIconDrawable.getForeground();
        Drawable background = adaptiveIconDrawable.getBackground();
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(i, i, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmapCreateBitmap);
        Path path = new Path();
        float f = i / 2.0f;
        path.addCircle(f, f, f, Path.Direction.CW);
        canvas.clipPath(path);
        background.setBounds(0, 0, i, i);
        background.draw(canvas);
        int i2 = i / 5;
        int i3 = -i2;
        int i4 = i + i2;
        foreground.setBounds(i3, i3, i4, i4);
        foreground.draw(canvas);
        canvas.setBitmap(null);
        return bitmapCreateBitmap;
    }

    BitmapInfo getBubbleBitmap(Drawable drawable, BitmapInfo bitmapInfo) {
        BitmapInfo bitmapInfoCreateBadgedIconBitmap = createBadgedIconBitmap(drawable, null, true);
        badgeWithDrawable(bitmapInfoCreateBadgedIconBitmap.icon, new BitmapDrawable(this.mContext.getResources(), bitmapInfo.icon));
        return bitmapInfoCreateBadgedIconBitmap;
    }
}
