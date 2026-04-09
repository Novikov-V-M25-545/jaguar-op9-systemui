package com.android.systemui.statusbar.phone;

import com.android.internal.annotations.VisibleForTesting;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class StatusBarIconList {
    private ArrayList<Slot> mSlots;

    public StatusBarIconList(String[] strArr) {
        ArrayList<Slot> arrayList = new ArrayList<>();
        this.mSlots = arrayList;
        arrayList.add(0, new Slot("networktraffic", StatusBarIconHolder.fromNetworkTraffic()));
        for (String str : strArr) {
            this.mSlots.add(new Slot(str, null));
        }
    }

    public int getSlotIndex(String str) {
        int size = this.mSlots.size();
        for (int i = 0; i < size; i++) {
            if (this.mSlots.get(i).getName().equals(str)) {
                return i;
            }
        }
        this.mSlots.add(1, new Slot(str, null));
        return 1;
    }

    protected ArrayList<Slot> getSlots() {
        return new ArrayList<>(this.mSlots);
    }

    protected Slot getSlot(String str) {
        return this.mSlots.get(getSlotIndex(str));
    }

    public void setIcon(int i, StatusBarIconHolder statusBarIconHolder) {
        this.mSlots.get(i).addHolder(statusBarIconHolder);
    }

    public void removeIcon(int i, int i2) {
        this.mSlots.get(i).removeForTag(i2);
    }

    public String getSlotName(int i) {
        return this.mSlots.get(i).getName();
    }

    public StatusBarIconHolder getIcon(int i, int i2) {
        return this.mSlots.get(i).getHolderForTag(i2);
    }

    public int getViewIndex(int i, int i2) {
        int iNumberOfIcons = 0;
        for (int i3 = 0; i3 < i; i3++) {
            Slot slot = this.mSlots.get(i3);
            if (slot.hasIconsInSlot()) {
                iNumberOfIcons += slot.numberOfIcons();
            }
        }
        return iNumberOfIcons + this.mSlots.get(i).viewIndexOffsetForTag(i2);
    }

    public void dump(PrintWriter printWriter) {
        printWriter.println("StatusBarIconList state:");
        int size = this.mSlots.size();
        printWriter.println("  icon slots: " + size);
        for (int i = 0; i < size; i++) {
            printWriter.printf("    %2d:%s\n", Integer.valueOf(i), this.mSlots.get(i).toString());
        }
    }

    public static class Slot {
        private StatusBarIconHolder mHolder;
        private final String mName;
        private ArrayList<StatusBarIconHolder> mSubSlots;

        public Slot(String str, StatusBarIconHolder statusBarIconHolder) {
            this.mName = str;
            this.mHolder = statusBarIconHolder;
        }

        public String getName() {
            return this.mName;
        }

        public StatusBarIconHolder getHolderForTag(int i) {
            if (i == 0) {
                return this.mHolder;
            }
            ArrayList<StatusBarIconHolder> arrayList = this.mSubSlots;
            if (arrayList == null) {
                return null;
            }
            Iterator<StatusBarIconHolder> it = arrayList.iterator();
            while (it.hasNext()) {
                StatusBarIconHolder next = it.next();
                if (next.getTag() == i) {
                    return next;
                }
            }
            return null;
        }

        public void addHolder(StatusBarIconHolder statusBarIconHolder) {
            int tag = statusBarIconHolder.getTag();
            if (tag == 0) {
                this.mHolder = statusBarIconHolder;
            } else {
                setSubSlot(statusBarIconHolder, tag);
            }
        }

        public void removeForTag(int i) {
            if (i == 0) {
                this.mHolder = null;
                return;
            }
            int indexForTag = getIndexForTag(i);
            if (indexForTag != -1) {
                this.mSubSlots.remove(indexForTag);
            }
        }

        @VisibleForTesting
        public void clear() {
            this.mHolder = null;
            if (this.mSubSlots != null) {
                this.mSubSlots = null;
            }
        }

        private void setSubSlot(StatusBarIconHolder statusBarIconHolder, int i) {
            if (this.mSubSlots == null) {
                ArrayList<StatusBarIconHolder> arrayList = new ArrayList<>();
                this.mSubSlots = arrayList;
                arrayList.add(statusBarIconHolder);
            } else {
                if (getIndexForTag(i) != -1) {
                    return;
                }
                this.mSubSlots.add(statusBarIconHolder);
            }
        }

        private int getIndexForTag(int i) {
            for (int i2 = 0; i2 < this.mSubSlots.size(); i2++) {
                if (this.mSubSlots.get(i2).getTag() == i) {
                    return i2;
                }
            }
            return -1;
        }

        public boolean hasIconsInSlot() {
            if (this.mHolder != null) {
                return true;
            }
            ArrayList<StatusBarIconHolder> arrayList = this.mSubSlots;
            return arrayList != null && arrayList.size() > 0;
        }

        public int numberOfIcons() {
            int i = this.mHolder == null ? 0 : 1;
            ArrayList<StatusBarIconHolder> arrayList = this.mSubSlots;
            return arrayList == null ? i : i + arrayList.size();
        }

        public int viewIndexOffsetForTag(int i) {
            ArrayList<StatusBarIconHolder> arrayList = this.mSubSlots;
            if (arrayList == null) {
                return 0;
            }
            return i == 0 ? arrayList.size() : (r0 - getIndexForTag(i)) - 1;
        }

        public List<StatusBarIconHolder> getHolderListInViewOrder() {
            ArrayList arrayList = new ArrayList();
            ArrayList<StatusBarIconHolder> arrayList2 = this.mSubSlots;
            if (arrayList2 != null) {
                for (int size = arrayList2.size() - 1; size >= 0; size--) {
                    arrayList.add(this.mSubSlots.get(size));
                }
            }
            StatusBarIconHolder statusBarIconHolder = this.mHolder;
            if (statusBarIconHolder != null) {
                arrayList.add(statusBarIconHolder);
            }
            return arrayList;
        }

        public List<StatusBarIconHolder> getHolderList() {
            ArrayList arrayList = new ArrayList();
            StatusBarIconHolder statusBarIconHolder = this.mHolder;
            if (statusBarIconHolder != null) {
                arrayList.add(statusBarIconHolder);
            }
            ArrayList<StatusBarIconHolder> arrayList2 = this.mSubSlots;
            if (arrayList2 != null) {
                arrayList.addAll(arrayList2);
            }
            return arrayList;
        }

        public String toString() {
            return String.format("(%s) %s", this.mName, subSlotsString());
        }

        private String subSlotsString() {
            if (this.mSubSlots == null) {
                return "";
            }
            return "" + this.mSubSlots.size() + " subSlots";
        }
    }
}
