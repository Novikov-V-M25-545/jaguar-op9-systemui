package androidx.slice.widget;

import android.text.TextUtils;
import android.util.Log;
import androidx.slice.SliceItem;
import androidx.slice.core.SliceAction;
import androidx.slice.core.SliceActionImpl;
import androidx.slice.core.SliceQuery;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class RowContent extends SliceContent {
    private ArrayList<SliceItem> mEndItems;
    private boolean mIsHeader;
    private int mLineCount;
    private SliceItem mPrimaryAction;
    private SliceItem mRange;
    private SliceItem mSelection;
    private boolean mShowActionDivider;
    private boolean mShowBottomDivider;
    private boolean mShowTitleItems;
    private SliceItem mStartItem;
    private SliceItem mSubtitleItem;
    private SliceItem mSummaryItem;
    private SliceItem mTitleItem;
    private ArrayList<SliceAction> mToggleItems;

    public RowContent(SliceItem sliceItem, int i) {
        super(sliceItem, i);
        this.mEndItems = new ArrayList<>();
        this.mToggleItems = new ArrayList<>();
        this.mLineCount = 0;
        populate(sliceItem, i == 0);
    }

    private boolean populate(SliceItem sliceItem, boolean z) {
        boolean z2;
        this.mIsHeader = z;
        if (!isValidRow(sliceItem)) {
            Log.w("RowContent", "Provided SliceItem is invalid for RowContent");
            return false;
        }
        determineStartAndPrimaryAction(sliceItem);
        ArrayList<SliceItem> arrayListFilterInvalidItems = filterInvalidItems(sliceItem);
        if (arrayListFilterInvalidItems.size() == 1 && (("action".equals(arrayListFilterInvalidItems.get(0).getFormat()) || "slice".equals(arrayListFilterInvalidItems.get(0).getFormat())) && !arrayListFilterInvalidItems.get(0).hasAnyHints("shortcut", "title") && isValidRow(arrayListFilterInvalidItems.get(0)))) {
            sliceItem = arrayListFilterInvalidItems.get(0);
            arrayListFilterInvalidItems = filterInvalidItems(sliceItem);
            z2 = true;
        } else {
            z2 = false;
        }
        if ("range".equals(sliceItem.getSubType())) {
            if (SliceQuery.findSubtype(sliceItem, "action", "range") == null || z2) {
                this.mRange = sliceItem;
            } else {
                arrayListFilterInvalidItems.remove(this.mStartItem);
                if (arrayListFilterInvalidItems.size() == 1) {
                    if (isValidRow(arrayListFilterInvalidItems.get(0))) {
                        sliceItem = arrayListFilterInvalidItems.get(0);
                        arrayListFilterInvalidItems = filterInvalidItems(sliceItem);
                        this.mRange = sliceItem;
                        arrayListFilterInvalidItems.remove(getInputRangeThumb());
                    }
                } else {
                    SliceItem sliceItemFindSubtype = SliceQuery.findSubtype(sliceItem, "action", "range");
                    this.mRange = sliceItemFindSubtype;
                    ArrayList<SliceItem> arrayListFilterInvalidItems2 = filterInvalidItems(sliceItemFindSubtype);
                    arrayListFilterInvalidItems2.remove(getInputRangeThumb());
                    arrayListFilterInvalidItems.remove(this.mRange);
                    arrayListFilterInvalidItems.addAll(arrayListFilterInvalidItems2);
                }
            }
        }
        if ("selection".equals(sliceItem.getSubType())) {
            this.mSelection = sliceItem;
        }
        if (arrayListFilterInvalidItems.size() > 0) {
            SliceItem sliceItem2 = this.mStartItem;
            if (sliceItem2 != null) {
                arrayListFilterInvalidItems.remove(sliceItem2);
            }
            SliceItem sliceItem3 = this.mPrimaryAction;
            if (sliceItem3 != null) {
                arrayListFilterInvalidItems.remove(sliceItem3);
            }
            ArrayList arrayList = new ArrayList();
            for (int i = 0; i < arrayListFilterInvalidItems.size(); i++) {
                SliceItem sliceItem4 = arrayListFilterInvalidItems.get(i);
                if ("text".equals(sliceItem4.getFormat())) {
                    SliceItem sliceItem5 = this.mTitleItem;
                    if ((sliceItem5 == null || !sliceItem5.hasHint("title")) && sliceItem4.hasHint("title") && !sliceItem4.hasHint("summary")) {
                        this.mTitleItem = sliceItem4;
                    } else if (this.mSubtitleItem == null && !sliceItem4.hasHint("summary")) {
                        this.mSubtitleItem = sliceItem4;
                    } else if (this.mSummaryItem == null && sliceItem4.hasHint("summary")) {
                        this.mSummaryItem = sliceItem4;
                    }
                } else {
                    arrayList.add(sliceItem4);
                }
            }
            if (hasText(this.mTitleItem)) {
                this.mLineCount++;
            }
            if (hasText(this.mSubtitleItem)) {
                this.mLineCount++;
            }
            SliceItem sliceItem6 = this.mStartItem;
            boolean z3 = sliceItem6 != null && "long".equals(sliceItem6.getFormat());
            for (int i2 = 0; i2 < arrayList.size(); i2++) {
                SliceItem sliceItem7 = (SliceItem) arrayList.get(i2);
                boolean z4 = SliceQuery.find(sliceItem7, "action") != null;
                if (!"long".equals(sliceItem7.getFormat())) {
                    processContent(sliceItem7, z4);
                } else if (!z3) {
                    this.mEndItems.add(sliceItem7);
                    z3 = true;
                }
            }
        }
        return isValid();
    }

    private void processContent(SliceItem sliceItem, boolean z) {
        if (z) {
            SliceActionImpl sliceActionImpl = new SliceActionImpl(sliceItem);
            if (sliceActionImpl.isToggle()) {
                this.mToggleItems.add(sliceActionImpl);
            }
        }
        this.mEndItems.add(sliceItem);
    }

    private void determineStartAndPrimaryAction(SliceItem sliceItem) {
        List<SliceItem> listFindAll = SliceQuery.findAll(sliceItem, (String) null, "title", (String) null);
        if (listFindAll.size() > 0) {
            String format = listFindAll.get(0).getFormat();
            if (("action".equals(format) && SliceQuery.find(listFindAll.get(0), "image") != null) || "slice".equals(format) || "long".equals(format) || "image".equals(format)) {
                this.mStartItem = listFindAll.get(0);
            }
        }
        String[] strArr = {"shortcut", "title"};
        List<SliceItem> listFindAll2 = SliceQuery.findAll(sliceItem, "slice", strArr, (String[]) null);
        listFindAll2.addAll(SliceQuery.findAll(sliceItem, "action", strArr, (String[]) null));
        if (listFindAll2.isEmpty() && "action".equals(sliceItem.getFormat()) && sliceItem.getSlice().getItems().size() == 1) {
            this.mPrimaryAction = sliceItem;
            return;
        }
        if (this.mStartItem != null && listFindAll2.size() > 1 && listFindAll2.get(0) == this.mStartItem) {
            this.mPrimaryAction = listFindAll2.get(1);
        } else if (listFindAll2.size() > 0) {
            this.mPrimaryAction = listFindAll2.get(0);
        }
    }

    @Override // androidx.slice.widget.SliceContent
    public boolean isValid() {
        return super.isValid() && !(this.mStartItem == null && this.mPrimaryAction == null && this.mTitleItem == null && this.mSubtitleItem == null && this.mEndItems.size() <= 0 && this.mRange == null && this.mSelection == null && !isDefaultSeeMore());
    }

    public boolean getIsHeader() {
        return this.mIsHeader;
    }

    public void setIsHeader(boolean z) {
        this.mIsHeader = z;
    }

    public SliceItem getRange() {
        return this.mRange;
    }

    public SliceItem getSelection() {
        return this.mSelection;
    }

    public SliceItem getInputRangeThumb() {
        SliceItem sliceItem = this.mRange;
        if (sliceItem == null) {
            return null;
        }
        List<SliceItem> items = sliceItem.getSlice().getItems();
        for (int i = 0; i < items.size(); i++) {
            if ("image".equals(items.get(i).getFormat())) {
                return items.get(i);
            }
        }
        return null;
    }

    public SliceItem getPrimaryAction() {
        return this.mPrimaryAction;
    }

    public SliceItem getStartItem() {
        if (!this.mIsHeader || this.mShowTitleItems) {
            return this.mStartItem;
        }
        return null;
    }

    public SliceItem getTitleItem() {
        return this.mTitleItem;
    }

    public SliceItem getSubtitleItem() {
        return this.mSubtitleItem;
    }

    public SliceItem getSummaryItem() {
        SliceItem sliceItem = this.mSummaryItem;
        return sliceItem == null ? this.mSubtitleItem : sliceItem;
    }

    public ArrayList<SliceItem> getEndItems() {
        return this.mEndItems;
    }

    public ArrayList<SliceAction> getToggleItems() {
        return this.mToggleItems;
    }

    public int getLineCount() {
        return this.mLineCount;
    }

    @Override // androidx.slice.widget.SliceContent
    public int getHeight(SliceStyle sliceStyle, SliceViewPolicy sliceViewPolicy) {
        return sliceStyle.getRowHeight(this, sliceViewPolicy);
    }

    public boolean isDefaultSeeMore() {
        return "action".equals(this.mSliceItem.getFormat()) && this.mSliceItem.getSlice().hasHint("see_more") && this.mSliceItem.getSlice().getItems().isEmpty();
    }

    public void showTitleItems(boolean z) {
        this.mShowTitleItems = z;
    }

    public boolean hasTitleItems() {
        return this.mShowTitleItems;
    }

    public void showBottomDivider(boolean z) {
        this.mShowBottomDivider = z;
    }

    public boolean hasBottomDivider() {
        return this.mShowBottomDivider;
    }

    public void showActionDivider(boolean z) {
        this.mShowActionDivider = z;
    }

    public boolean hasActionDivider() {
        return this.mShowActionDivider;
    }

    private static boolean hasText(SliceItem sliceItem) {
        return sliceItem != null && (sliceItem.hasHint("partial") || !TextUtils.isEmpty(sliceItem.getText()));
    }

    private static boolean isValidRow(SliceItem sliceItem) {
        if (sliceItem == null) {
            return false;
        }
        if ("slice".equals(sliceItem.getFormat()) || "action".equals(sliceItem.getFormat())) {
            List<SliceItem> items = sliceItem.getSlice().getItems();
            if (sliceItem.hasHint("see_more") && items.isEmpty()) {
                return true;
            }
            for (int i = 0; i < items.size(); i++) {
                if (isValidRowContent(sliceItem, items.get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static ArrayList<SliceItem> filterInvalidItems(SliceItem sliceItem) {
        ArrayList<SliceItem> arrayList = new ArrayList<>();
        for (SliceItem sliceItem2 : sliceItem.getSlice().getItems()) {
            if (isValidRowContent(sliceItem, sliceItem2)) {
                arrayList.add(sliceItem2);
            }
        }
        return arrayList;
    }

    private static boolean isValidRowContent(SliceItem sliceItem, SliceItem sliceItem2) {
        if (sliceItem2.hasAnyHints("keywords", "ttl", "last_updated", "horizontal") || "content_description".equals(sliceItem2.getSubType()) || "selection_option_key".equals(sliceItem2.getSubType()) || "selection_option_value".equals(sliceItem2.getSubType())) {
            return false;
        }
        String format = sliceItem2.getFormat();
        return "image".equals(format) || "text".equals(format) || "long".equals(format) || "action".equals(format) || "input".equals(format) || "slice".equals(format) || ("int".equals(format) && "range".equals(sliceItem.getSubType()));
    }
}
