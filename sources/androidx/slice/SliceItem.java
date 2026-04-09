package androidx.slice;

import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import androidx.core.graphics.drawable.IconCompat;
import androidx.core.util.Pair;
import androidx.versionedparcelable.CustomVersionedParcelable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public final class SliceItem extends CustomVersionedParcelable {
    String mFormat;
    protected String[] mHints;
    SliceItemHolder mHolder;
    Object mObj;
    CharSequence mSanitizedText;
    String mSubType;

    public interface ActionHandler {
        void onAction(SliceItem sliceItem, Context context, Intent intent);
    }

    public SliceItem(Object obj, String str, String str2, String[] strArr) {
        this.mHints = Slice.NO_HINTS;
        this.mFormat = "text";
        this.mSubType = null;
        this.mHints = strArr;
        this.mFormat = str;
        this.mSubType = str2;
        this.mObj = obj;
    }

    public SliceItem(Object obj, String str, String str2, List<String> list) {
        this(obj, str, str2, (String[]) list.toArray(new String[list.size()]));
    }

    public SliceItem() {
        this.mHints = Slice.NO_HINTS;
        this.mFormat = "text";
        this.mSubType = null;
    }

    public SliceItem(PendingIntent pendingIntent, Slice slice, String str, String str2, String[] strArr) {
        this(new Pair(pendingIntent, slice), str, str2, strArr);
    }

    public List<String> getHints() {
        return Arrays.asList(this.mHints);
    }

    public void addHint(String str) {
        this.mHints = (String[]) ArrayUtils.appendElement(String.class, this.mHints, str);
    }

    public String getFormat() {
        return this.mFormat;
    }

    public String getSubType() {
        return this.mSubType;
    }

    public CharSequence getText() {
        return (CharSequence) this.mObj;
    }

    public CharSequence getSanitizedText() {
        if (this.mSanitizedText == null) {
            this.mSanitizedText = sanitizeText(getText());
        }
        return this.mSanitizedText;
    }

    public IconCompat getIcon() {
        return (IconCompat) this.mObj;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public PendingIntent getAction() {
        F f = ((Pair) this.mObj).first;
        if (f instanceof PendingIntent) {
            return (PendingIntent) f;
        }
        return null;
    }

    public void fireAction(Context context, Intent intent) throws PendingIntent.CanceledException {
        fireActionInternal(context, intent);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean fireActionInternal(Context context, Intent intent) throws PendingIntent.CanceledException {
        F f = ((Pair) this.mObj).first;
        if (f instanceof PendingIntent) {
            ((PendingIntent) f).send(context, 0, intent, null, null);
            return false;
        }
        ((ActionHandler) f).onAction(this, context, intent);
        return true;
    }

    public RemoteInput getRemoteInput() {
        return (RemoteInput) this.mObj;
    }

    public int getInt() {
        return ((Integer) this.mObj).intValue();
    }

    /* JADX WARN: Multi-variable type inference failed */
    public Slice getSlice() {
        if ("action".equals(getFormat())) {
            return (Slice) ((Pair) this.mObj).second;
        }
        return (Slice) this.mObj;
    }

    public long getLong() {
        return ((Long) this.mObj).longValue();
    }

    public boolean hasHint(String str) {
        return ArrayUtils.contains(this.mHints, str);
    }

    public SliceItem(Bundle bundle) {
        this.mHints = Slice.NO_HINTS;
        this.mFormat = "text";
        this.mSubType = null;
        this.mHints = bundle.getStringArray("hints");
        this.mFormat = bundle.getString("format");
        this.mSubType = bundle.getString("subtype");
        this.mObj = readObj(this.mFormat, bundle);
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putStringArray("hints", this.mHints);
        bundle.putString("format", this.mFormat);
        bundle.putString("subtype", this.mSubType);
        writeObj(bundle, this.mObj, this.mFormat);
        return bundle;
    }

    public boolean hasAnyHints(String... strArr) {
        if (strArr == null) {
            return false;
        }
        for (String str : strArr) {
            if (ArrayUtils.contains(this.mHints, str)) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void writeObj(Bundle bundle, Object obj, String str) {
        str.hashCode();
        switch (str) {
            case "action":
                Pair pair = (Pair) obj;
                bundle.putParcelable("obj", (PendingIntent) pair.first);
                bundle.putBundle("obj_2", ((Slice) pair.second).toBundle());
                break;
            case "int":
                bundle.putInt("obj", ((Integer) this.mObj).intValue());
                break;
            case "long":
                bundle.putLong("obj", ((Long) this.mObj).longValue());
                break;
            case "text":
                bundle.putCharSequence("obj", (CharSequence) obj);
                break;
            case "image":
                bundle.putBundle("obj", ((IconCompat) obj).toBundle());
                break;
            case "input":
                bundle.putParcelable("obj", (Parcelable) obj);
                break;
            case "slice":
                bundle.putParcelable("obj", ((Slice) obj).toBundle());
                break;
        }
    }

    private static Object readObj(String str, Bundle bundle) {
        str.hashCode();
        switch (str) {
            case "action":
                return new Pair(bundle.getParcelable("obj"), new Slice(bundle.getBundle("obj_2")));
            case "int":
                return Integer.valueOf(bundle.getInt("obj"));
            case "long":
                return Long.valueOf(bundle.getLong("obj"));
            case "text":
                return bundle.getCharSequence("obj");
            case "image":
                return IconCompat.createFromBundle(bundle.getBundle("obj"));
            case "input":
                return bundle.getParcelable("obj");
            case "slice":
                return new Slice(bundle.getBundle("obj"));
            default:
                throw new RuntimeException("Unsupported type " + str);
        }
    }

    public static String typeToString(String str) {
        str.hashCode();
        switch (str) {
            case "action":
                return "Action";
            case "int":
                return "Int";
            case "long":
                return "Long";
            case "text":
                return "Text";
            case "image":
                return "Image";
            case "input":
                return "RemoteInput";
            case "slice":
                return "Slice";
            default:
                return "Unrecognized format: " + str;
        }
    }

    public String toString() {
        return toString("");
    }

    public String toString(String str) {
        StringBuilder sb;
        String str2;
        sb = new StringBuilder();
        sb.append(str);
        sb.append(getFormat());
        if (getSubType() != null) {
            sb.append('<');
            sb.append(getSubType());
            sb.append('>');
        }
        sb.append(' ');
        String[] strArr = this.mHints;
        if (strArr.length > 0) {
            Slice.appendHints(sb, strArr);
            sb.append(' ');
        }
        str2 = str + "  ";
        String format = getFormat();
        format.hashCode();
        switch (format) {
            case "action":
                Object obj = ((Pair) this.mObj).first;
                sb.append('[');
                sb.append(obj);
                sb.append("] ");
                sb.append("{\n");
                sb.append(getSlice().toString(str2));
                sb.append('\n');
                sb.append(str);
                sb.append('}');
                break;
            case "int":
                if ("color".equals(getSubType())) {
                    int i = getInt();
                    sb.append(String.format("a=0x%02x r=0x%02x g=0x%02x b=0x%02x", Integer.valueOf(Color.alpha(i)), Integer.valueOf(Color.red(i)), Integer.valueOf(Color.green(i)), Integer.valueOf(Color.blue(i))));
                    break;
                } else if ("layout_direction".equals(getSubType())) {
                    sb.append(layoutDirectionToString(getInt()));
                    break;
                } else {
                    sb.append(getInt());
                    break;
                }
            case "long":
                if (!"millis".equals(getSubType())) {
                    sb.append(getLong());
                    sb.append('L');
                    break;
                } else if (getLong() == -1) {
                    sb.append("INFINITY");
                    break;
                } else {
                    sb.append(DateUtils.getRelativeTimeSpanString(getLong(), Calendar.getInstance().getTimeInMillis(), 1000L, LineageHardwareManager.FEATURE_PICTURE_ADJUSTMENT));
                    break;
                }
            case "text":
                sb.append('\"');
                sb.append(getText());
                sb.append('\"');
                break;
            case "image":
                sb.append(getIcon());
                break;
            case "slice":
                sb.append("{\n");
                sb.append(getSlice().toString(str2));
                sb.append('\n');
                sb.append(str);
                sb.append('}');
                break;
            default:
                sb.append(typeToString(getFormat()));
                break;
        }
        sb.append("\n");
        return sb.toString();
    }

    public void onPreParceling(boolean z) {
        this.mHolder = new SliceItemHolder(this.mFormat, this.mObj, z);
    }

    public void onPostParceling() {
        SliceItemHolder sliceItemHolder = this.mHolder;
        if (sliceItemHolder != null) {
            this.mObj = sliceItemHolder.getObj(this.mFormat);
            this.mHolder.release();
        } else {
            this.mObj = null;
        }
        this.mHolder = null;
    }

    private static String layoutDirectionToString(int i) {
        return i != 0 ? i != 1 ? i != 2 ? i != 3 ? Integer.toString(i) : "LOCALE" : "INHERIT" : "RTL" : "LTR";
    }

    private static CharSequence sanitizeText(CharSequence charSequence) {
        if (charSequence instanceof Spannable) {
            fixSpannableText((Spannable) charSequence);
            return charSequence;
        }
        if (!(charSequence instanceof Spanned) || checkSpannedText((Spanned) charSequence)) {
            return charSequence;
        }
        SpannableString spannableString = new SpannableString(charSequence);
        fixSpannableText(spannableString);
        return spannableString;
    }

    private static boolean checkSpannedText(Spanned spanned) {
        for (Object obj : spanned.getSpans(0, spanned.length(), Object.class)) {
            if (!checkSpan(obj)) {
                return false;
            }
        }
        return true;
    }

    private static void fixSpannableText(Spannable spannable) {
        for (Object obj : spannable.getSpans(0, spannable.length(), Object.class)) {
            Object objFixSpan = fixSpan(obj);
            if (objFixSpan != obj) {
                if (objFixSpan != null) {
                    spannable.setSpan(objFixSpan, spannable.getSpanStart(obj), spannable.getSpanEnd(obj), spannable.getSpanFlags(obj));
                }
                spannable.removeSpan(obj);
            }
        }
    }

    private static boolean checkSpan(Object obj) {
        return (obj instanceof AlignmentSpan) || (obj instanceof ForegroundColorSpan) || (obj instanceof RelativeSizeSpan) || (obj instanceof StyleSpan);
    }

    private static Object fixSpan(Object obj) {
        if (checkSpan(obj)) {
            return obj;
        }
        return null;
    }
}
