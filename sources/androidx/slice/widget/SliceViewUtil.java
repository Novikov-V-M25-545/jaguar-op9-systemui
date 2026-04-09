package androidx.slice.widget;

import android.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.widget.ProgressBar;
import androidx.appcompat.R$attr;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.graphics.drawable.IconCompat;
import java.util.Calendar;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class SliceViewUtil {
    public static int resolveLayoutDirection(int i) {
        if (i == 2 || i == 3 || i == 1 || i == 0) {
            return i;
        }
        return -1;
    }

    public static int getColorAccent(Context context) {
        return getColorAttr(context, R.attr.colorAccent);
    }

    public static int getColorAttr(Context context, int i) {
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(new int[]{i});
        int color = typedArrayObtainStyledAttributes.getColor(0, 0);
        typedArrayObtainStyledAttributes.recycle();
        return color;
    }

    public static Drawable getDrawable(Context context, int i) {
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(new int[]{i});
        Drawable drawable = typedArrayObtainStyledAttributes.getDrawable(0);
        typedArrayObtainStyledAttributes.recycle();
        return drawable;
    }

    public static IconCompat createIconFromDrawable(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return IconCompat.createWithBitmap(((BitmapDrawable) drawable).getBitmap());
        }
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapCreateBitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return IconCompat.createWithBitmap(bitmapCreateBitmap);
    }

    public static Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapCreateBitmap);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return bitmapCreateBitmap;
    }

    public static CharSequence getTimestampString(Context context, long j) {
        if (j < System.currentTimeMillis() || DateUtils.isToday(j)) {
            return DateUtils.getRelativeTimeSpanString(j, Calendar.getInstance().getTimeInMillis(), 60000L, LineageHardwareManager.FEATURE_PICTURE_ADJUSTMENT);
        }
        return DateUtils.formatDateTime(context, j, 8);
    }

    public static void tintIndeterminateProgressBar(Context context, ProgressBar progressBar) {
        int colorAttr = getColorAttr(context, R$attr.colorControlHighlight);
        Drawable drawableWrap = DrawableCompat.wrap(progressBar.getIndeterminateDrawable());
        if (drawableWrap == null || colorAttr == 0) {
            return;
        }
        drawableWrap.setColorFilter(colorAttr, PorterDuff.Mode.MULTIPLY);
        progressBar.setProgressDrawable(drawableWrap);
    }
}
