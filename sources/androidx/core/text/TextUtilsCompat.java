package androidx.core.text;

import android.os.Build;
import android.text.TextUtils;
import java.util.Locale;

/* loaded from: classes.dex */
public final class TextUtilsCompat {
    private static final Locale ROOT = new Locale("", "");

    public static int getLayoutDirectionFromLocale(Locale locale) {
        if (Build.VERSION.SDK_INT >= 17) {
            return TextUtils.getLayoutDirectionFromLocale(locale);
        }
        if (locale == null || locale.equals(ROOT)) {
            return 0;
        }
        String strMaximizeAndGetScript = ICUCompat.maximizeAndGetScript(locale);
        if (strMaximizeAndGetScript == null) {
            return getLayoutDirectionFromFirstChar(locale);
        }
        return (strMaximizeAndGetScript.equalsIgnoreCase("Arab") || strMaximizeAndGetScript.equalsIgnoreCase("Hebr")) ? 1 : 0;
    }

    private static int getLayoutDirectionFromFirstChar(Locale locale) {
        byte directionality = Character.getDirectionality(locale.getDisplayName(locale).charAt(0));
        return (directionality == 1 || directionality == 2) ? 1 : 0;
    }
}
