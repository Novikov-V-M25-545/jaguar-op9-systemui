package androidx.slice;

import android.content.Context;
import androidx.slice.core.SliceAction;
import androidx.slice.core.SliceActionImpl;
import androidx.slice.core.SliceQuery;
import androidx.slice.widget.ListContent;
import androidx.slice.widget.RowContent;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class SliceMetadata {
    private Context mContext;
    private long mExpiry;
    private RowContent mHeaderContent;
    private long mLastUpdated;
    private ListContent mListContent;
    private SliceAction mPrimaryAction;
    private Slice mSlice;
    private List<SliceAction> mSliceActions;
    private int mTemplateType;

    public static SliceMetadata from(Context context, Slice slice) {
        return new SliceMetadata(context, slice);
    }

    private SliceMetadata(Context context, Slice slice) {
        RowContent rowContent;
        this.mSlice = slice;
        this.mContext = context;
        SliceItem sliceItemFind = SliceQuery.find(slice, "long", "ttl", (String) null);
        if (sliceItemFind != null) {
            this.mExpiry = sliceItemFind.getLong();
        }
        SliceItem sliceItemFind2 = SliceQuery.find(slice, "long", "last_updated", (String) null);
        if (sliceItemFind2 != null) {
            this.mLastUpdated = sliceItemFind2.getLong();
        }
        ListContent listContent = new ListContent(slice);
        this.mListContent = listContent;
        this.mHeaderContent = listContent.getHeader();
        this.mTemplateType = this.mListContent.getHeaderTemplateType();
        this.mPrimaryAction = this.mListContent.getShortcut(this.mContext);
        List<SliceAction> sliceActions = this.mListContent.getSliceActions();
        this.mSliceActions = sliceActions;
        if (sliceActions == null && (rowContent = this.mHeaderContent) != null && SliceQuery.hasHints(rowContent.getSliceItem(), "list_item")) {
            ArrayList<SliceItem> endItems = this.mHeaderContent.getEndItems();
            ArrayList arrayList = new ArrayList();
            for (int i = 0; i < endItems.size(); i++) {
                if (SliceQuery.find(endItems.get(i), "action") != null) {
                    arrayList.add(new SliceActionImpl(endItems.get(i)));
                }
            }
            if (arrayList.size() > 0) {
                this.mSliceActions = arrayList;
            }
        }
    }

    public List<SliceAction> getSliceActions() {
        return this.mSliceActions;
    }

    public int getLoadingState() {
        boolean z = SliceQuery.find(this.mSlice, (String) null, "partial", (String) null) != null;
        if (this.mListContent.isValid()) {
            return z ? 1 : 2;
        }
        return 0;
    }

    public long getLastUpdatedTime() {
        return this.mLastUpdated;
    }

    public boolean isPermissionSlice() {
        return this.mSlice.hasHint("permission_request");
    }

    public static List<SliceAction> getSliceActions(Slice slice) {
        SliceItem sliceItemFind = SliceQuery.find(slice, "slice", "actions", (String) null);
        List<SliceItem> listFindAll = sliceItemFind != null ? SliceQuery.findAll(sliceItemFind, "slice", new String[]{"actions", "shortcut"}, (String[]) null) : null;
        if (listFindAll == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList(listFindAll.size());
        for (int i = 0; i < listFindAll.size(); i++) {
            arrayList.add(new SliceActionImpl(listFindAll.get(i)));
        }
        return arrayList;
    }

    public boolean isExpired() {
        long jCurrentTimeMillis = System.currentTimeMillis();
        long j = this.mExpiry;
        return (j == 0 || j == -1 || jCurrentTimeMillis <= j) ? false : true;
    }

    public boolean neverExpires() {
        return this.mExpiry == -1;
    }

    public long getTimeToExpiry() {
        long jCurrentTimeMillis = System.currentTimeMillis();
        long j = this.mExpiry;
        if (j == 0 || j == -1 || jCurrentTimeMillis > j) {
            return 0L;
        }
        return j - jCurrentTimeMillis;
    }

    public ListContent getListContent() {
        return this.mListContent;
    }
}
