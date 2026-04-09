package androidx.slice;

import android.net.Uri;
import java.util.Iterator;

/* loaded from: classes.dex */
public class SliceStructure {
    private final String mStructure;
    private final Uri mUri;

    public SliceStructure(Slice slice) {
        StringBuilder sb = new StringBuilder();
        getStructure(slice, sb);
        this.mStructure = sb.toString();
        this.mUri = slice.getUri();
    }

    public SliceStructure(SliceItem sliceItem) {
        StringBuilder sb = new StringBuilder();
        getStructure(sliceItem, sb);
        this.mStructure = sb.toString();
        if ("action".equals(sliceItem.getFormat()) || "slice".equals(sliceItem.getFormat())) {
            this.mUri = sliceItem.getSlice().getUri();
        } else {
            this.mUri = null;
        }
    }

    public Uri getUri() {
        return this.mUri;
    }

    public int hashCode() {
        return this.mStructure.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof SliceStructure) {
            return this.mStructure.equals(((SliceStructure) obj).mStructure);
        }
        return false;
    }

    private static void getStructure(Slice slice, StringBuilder sb) {
        sb.append("s{");
        Iterator<SliceItem> it = slice.getItems().iterator();
        while (it.hasNext()) {
            getStructure(it.next(), sb);
        }
        sb.append("}");
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue */
    /* JADX WARN: Removed duplicated region for block: B:29:0x005f  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private static void getStructure(androidx.slice.SliceItem r5, java.lang.StringBuilder r6) {
        /*
            java.lang.String r0 = r5.getFormat()
            int r1 = r0.hashCode()
            r2 = 3
            r3 = 2
            r4 = 1
            switch(r1) {
                case -1422950858: goto L55;
                case -1377881982: goto L4b;
                case 104431: goto L41;
                case 3327612: goto L37;
                case 3556653: goto L2d;
                case 100313435: goto L23;
                case 100358090: goto L19;
                case 109526418: goto Lf;
                default: goto Le;
            }
        Le:
            goto L5f
        Lf:
            java.lang.String r1 = "slice"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L5f
            r0 = 0
            goto L60
        L19:
            java.lang.String r1 = "input"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L5f
            r0 = 6
            goto L60
        L23:
            java.lang.String r1 = "image"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L5f
            r0 = r2
            goto L60
        L2d:
            java.lang.String r1 = "text"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L5f
            r0 = r3
            goto L60
        L37:
            java.lang.String r1 = "long"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L5f
            r0 = 5
            goto L60
        L41:
            java.lang.String r1 = "int"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L5f
            r0 = 4
            goto L60
        L4b:
            java.lang.String r1 = "bundle"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L5f
            r0 = 7
            goto L60
        L55:
            java.lang.String r1 = "action"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L5f
            r0 = r4
            goto L60
        L5f:
            r0 = -1
        L60:
            if (r0 == 0) goto L93
            if (r0 == r4) goto L75
            if (r0 == r3) goto L6f
            if (r0 == r2) goto L69
            goto L9a
        L69:
            r5 = 105(0x69, float:1.47E-43)
            r6.append(r5)
            goto L9a
        L6f:
            r5 = 116(0x74, float:1.63E-43)
            r6.append(r5)
            goto L9a
        L75:
            r0 = 97
            r6.append(r0)
            java.lang.String r0 = r5.getSubType()
            java.lang.String r1 = "range"
            boolean r0 = r1.equals(r0)
            if (r0 == 0) goto L8b
            r0 = 114(0x72, float:1.6E-43)
            r6.append(r0)
        L8b:
            androidx.slice.Slice r5 = r5.getSlice()
            getStructure(r5, r6)
            goto L9a
        L93:
            androidx.slice.Slice r5 = r5.getSlice()
            getStructure(r5, r6)
        L9a:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.slice.SliceStructure.getStructure(androidx.slice.SliceItem, java.lang.StringBuilder):void");
    }
}
