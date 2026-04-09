package androidx.slice;

import android.app.PendingIntent;
import android.os.Parcelable;
import android.text.Spanned;
import androidx.core.text.HtmlCompat;
import androidx.core.util.Pair;
import androidx.versionedparcelable.VersionedParcelable;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class SliceItemHolder implements VersionedParcelable {
    public static HolderHandler sHandler;
    public static final Object sSerializeLock = new Object();
    Object mCallback;
    int mInt;
    long mLong;
    Parcelable mParcelable;
    private SliceItemPool mPool;
    String mStr;
    public VersionedParcelable mVersionedParcelable;

    public interface HolderHandler {
        void handle(SliceItemHolder sliceItemHolder, String str);
    }

    SliceItemHolder(SliceItemPool sliceItemPool) {
        this.mVersionedParcelable = null;
        this.mParcelable = null;
        this.mStr = null;
        this.mInt = 0;
        this.mLong = 0L;
        this.mPool = sliceItemPool;
    }

    public void release() {
        SliceItemPool sliceItemPool = this.mPool;
        if (sliceItemPool != null) {
            sliceItemPool.release(this);
        }
    }

    public SliceItemHolder(String str, Object obj, boolean z) {
        this.mVersionedParcelable = null;
        this.mParcelable = null;
        this.mStr = null;
        this.mInt = 0;
        this.mLong = 0L;
        str.hashCode();
        switch (str) {
            case "action":
                Pair pair = (Pair) obj;
                F f = pair.first;
                if (f instanceof PendingIntent) {
                    this.mParcelable = (Parcelable) f;
                } else if (!z) {
                    throw new IllegalArgumentException("Cannot write callback to parcel");
                }
                this.mVersionedParcelable = (VersionedParcelable) pair.second;
                break;
            case "int":
                this.mInt = ((Integer) obj).intValue();
                break;
            case "long":
                this.mLong = ((Long) obj).longValue();
                break;
            case "text":
                this.mStr = obj instanceof Spanned ? HtmlCompat.toHtml((Spanned) obj, 0) : (String) obj;
                break;
            case "image":
            case "slice":
                this.mVersionedParcelable = (VersionedParcelable) obj;
                break;
            case "input":
                this.mParcelable = (Parcelable) obj;
                break;
        }
        HolderHandler holderHandler = sHandler;
        if (holderHandler != null) {
            holderHandler.handle(this, str);
        }
    }

    public Object getObj(String str) {
        HolderHandler holderHandler = sHandler;
        if (holderHandler != null) {
            holderHandler.handle(this, str);
        }
        str.hashCode();
        switch (str) {
            case "action":
                Object obj = this.mParcelable;
                if (obj == null && this.mVersionedParcelable == null) {
                    return null;
                }
                if (obj == null) {
                    obj = this.mCallback;
                }
                return new Pair(obj, (Slice) this.mVersionedParcelable);
            case "int":
                return Integer.valueOf(this.mInt);
            case "long":
                return Long.valueOf(this.mLong);
            case "text":
                String str2 = this.mStr;
                return (str2 == null || str2.length() == 0) ? "" : HtmlCompat.fromHtml(this.mStr, 0);
            case "image":
            case "slice":
                return this.mVersionedParcelable;
            case "input":
                return this.mParcelable;
            default:
                throw new IllegalArgumentException("Unrecognized format " + str);
        }
    }

    public static class SliceItemPool {
        private final ArrayList<SliceItemHolder> mCached = new ArrayList<>();

        public SliceItemHolder get() {
            if (this.mCached.size() > 0) {
                return this.mCached.remove(r1.size() - 1);
            }
            return new SliceItemHolder(this);
        }

        public void release(SliceItemHolder sliceItemHolder) {
            sliceItemHolder.mParcelable = null;
            sliceItemHolder.mCallback = null;
            sliceItemHolder.mVersionedParcelable = null;
            sliceItemHolder.mInt = 0;
            sliceItemHolder.mLong = 0L;
            sliceItemHolder.mStr = null;
            this.mCached.add(sliceItemHolder);
        }
    }
}
