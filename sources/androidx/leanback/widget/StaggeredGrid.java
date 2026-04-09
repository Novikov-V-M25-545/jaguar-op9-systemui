package androidx.leanback.widget;

import androidx.collection.CircularArray;
import androidx.collection.CircularIntArray;
import androidx.leanback.widget.Grid;

/* loaded from: classes.dex */
abstract class StaggeredGrid extends Grid {
    protected Object mPendingItem;
    protected int mPendingItemSize;
    protected CircularArray<Location> mLocations = new CircularArray<>(64);
    protected int mFirstIndex = -1;

    protected abstract boolean appendVisibleItemsWithoutCache(int i, boolean z);

    protected abstract boolean prependVisibleItemsWithoutCache(int i, boolean z);

    StaggeredGrid() {
    }

    public static class Location extends Grid.Location {
        public int offset;
        public int size;

        public Location(int i, int i2, int i3) {
            super(i);
            this.offset = i2;
            this.size = i3;
        }
    }

    public final int getFirstIndex() {
        return this.mFirstIndex;
    }

    public final int getLastIndex() {
        return (this.mFirstIndex + this.mLocations.size()) - 1;
    }

    @Override // androidx.leanback.widget.Grid
    public final Location getLocation(int i) {
        int i2 = i - this.mFirstIndex;
        if (i2 < 0 || i2 >= this.mLocations.size()) {
            return null;
        }
        return this.mLocations.get(i2);
    }

    @Override // androidx.leanback.widget.Grid
    protected final boolean prependVisibleItems(int i, boolean z) {
        boolean zPrependVisibleItemsWithoutCache;
        if (this.mProvider.getCount() == 0) {
            return false;
        }
        if (!z && checkPrependOverLimit(i)) {
            return false;
        }
        try {
            if (!prependVisbleItemsWithCache(i, z)) {
                zPrependVisibleItemsWithoutCache = prependVisibleItemsWithoutCache(i, z);
                this.mTmpItem[0] = null;
            } else {
                zPrependVisibleItemsWithoutCache = true;
                this.mTmpItem[0] = null;
            }
            this.mPendingItem = null;
            return zPrependVisibleItemsWithoutCache;
        } catch (Throwable th) {
            this.mTmpItem[0] = null;
            this.mPendingItem = null;
            throw th;
        }
    }

    protected final boolean prependVisbleItemsWithCache(int i, boolean z) {
        int edge;
        int i2;
        int i3;
        if (this.mLocations.size() == 0) {
            return false;
        }
        int i4 = this.mFirstVisibleIndex;
        if (i4 >= 0) {
            edge = this.mProvider.getEdge(i4);
            i3 = getLocation(this.mFirstVisibleIndex).offset;
            i2 = this.mFirstVisibleIndex - 1;
        } else {
            edge = Integer.MAX_VALUE;
            int i5 = this.mStartIndex;
            i2 = i5 != -1 ? i5 : 0;
            if (i2 > getLastIndex() || i2 < getFirstIndex() - 1) {
                this.mLocations.clear();
                return false;
            }
            if (i2 < getFirstIndex()) {
                return false;
            }
            i3 = 0;
        }
        int iMax = Math.max(this.mProvider.getMinIndex(), this.mFirstIndex);
        while (i2 >= iMax) {
            Location location = getLocation(i2);
            int i6 = location.row;
            int iCreateItem = this.mProvider.createItem(i2, false, this.mTmpItem, false);
            if (iCreateItem != location.size) {
                this.mLocations.removeFromStart((i2 + 1) - this.mFirstIndex);
                this.mFirstIndex = this.mFirstVisibleIndex;
                this.mPendingItem = this.mTmpItem[0];
                this.mPendingItemSize = iCreateItem;
                return false;
            }
            this.mFirstVisibleIndex = i2;
            if (this.mLastVisibleIndex < 0) {
                this.mLastVisibleIndex = i2;
            }
            this.mProvider.addItem(this.mTmpItem[0], i2, iCreateItem, i6, edge - i3);
            if (!z && checkPrependOverLimit(i)) {
                return true;
            }
            edge = this.mProvider.getEdge(i2);
            i3 = location.offset;
            if (i6 == 0 && z) {
                return true;
            }
            i2--;
        }
        return false;
    }

    private int calculateOffsetAfterLastItem(int i) {
        boolean z;
        int lastIndex = getLastIndex();
        while (true) {
            if (lastIndex < this.mFirstIndex) {
                z = false;
                break;
            }
            if (getLocation(lastIndex).row == i) {
                z = true;
                break;
            }
            lastIndex--;
        }
        if (!z) {
            lastIndex = getLastIndex();
        }
        int i2 = isReversedFlow() ? (-getLocation(lastIndex).size) - this.mSpacing : getLocation(lastIndex).size + this.mSpacing;
        for (int i3 = lastIndex + 1; i3 <= getLastIndex(); i3++) {
            i2 -= getLocation(i3).offset;
        }
        return i2;
    }

    protected final int prependVisibleItemToRow(int i, int i2, int i3) {
        int i4 = this.mFirstVisibleIndex;
        if (i4 >= 0 && (i4 != getFirstIndex() || this.mFirstVisibleIndex != i + 1)) {
            throw new IllegalStateException();
        }
        int i5 = this.mFirstIndex;
        Location location = i5 >= 0 ? getLocation(i5) : null;
        int edge = this.mProvider.getEdge(this.mFirstIndex);
        Location location2 = new Location(i2, 0, 0);
        this.mLocations.addFirst(location2);
        Object obj = this.mPendingItem;
        if (obj != null) {
            location2.size = this.mPendingItemSize;
            this.mPendingItem = null;
        } else {
            location2.size = this.mProvider.createItem(i, false, this.mTmpItem, false);
            obj = this.mTmpItem[0];
        }
        Object obj2 = obj;
        this.mFirstVisibleIndex = i;
        this.mFirstIndex = i;
        if (this.mLastVisibleIndex < 0) {
            this.mLastVisibleIndex = i;
        }
        int i6 = !this.mReversedFlow ? i3 - location2.size : i3 + location2.size;
        if (location != null) {
            location.offset = edge - i6;
        }
        this.mProvider.addItem(obj2, i, location2.size, i2, i6);
        return location2.size;
    }

    @Override // androidx.leanback.widget.Grid
    protected final boolean appendVisibleItems(int i, boolean z) {
        boolean zAppendVisibleItemsWithoutCache;
        if (this.mProvider.getCount() == 0) {
            return false;
        }
        if (!z && checkAppendOverLimit(i)) {
            return false;
        }
        try {
            if (!appendVisbleItemsWithCache(i, z)) {
                zAppendVisibleItemsWithoutCache = appendVisibleItemsWithoutCache(i, z);
                this.mTmpItem[0] = null;
            } else {
                zAppendVisibleItemsWithoutCache = true;
                this.mTmpItem[0] = null;
            }
            this.mPendingItem = null;
            return zAppendVisibleItemsWithoutCache;
        } catch (Throwable th) {
            this.mTmpItem[0] = null;
            this.mPendingItem = null;
            throw th;
        }
    }

    protected final boolean appendVisbleItemsWithCache(int i, boolean z) {
        int i2;
        int edge;
        int i3;
        if (this.mLocations.size() == 0) {
            return false;
        }
        int count = this.mProvider.getCount();
        int i4 = this.mLastVisibleIndex;
        if (i4 >= 0) {
            i2 = i4 + 1;
            edge = this.mProvider.getEdge(i4);
        } else {
            int i5 = this.mStartIndex;
            i2 = i5 != -1 ? i5 : 0;
            if (i2 > getLastIndex() + 1 || i2 < getFirstIndex()) {
                this.mLocations.clear();
                return false;
            }
            if (i2 > getLastIndex()) {
                return false;
            }
            edge = Integer.MAX_VALUE;
        }
        int lastIndex = getLastIndex();
        int i6 = i2;
        while (i6 < count && i6 <= lastIndex) {
            Location location = getLocation(i6);
            if (edge != Integer.MAX_VALUE) {
                edge += location.offset;
            }
            int i7 = location.row;
            int iCreateItem = this.mProvider.createItem(i6, true, this.mTmpItem, false);
            if (iCreateItem != location.size) {
                location.size = iCreateItem;
                this.mLocations.removeFromEnd(lastIndex - i6);
                i3 = i6;
            } else {
                i3 = lastIndex;
            }
            this.mLastVisibleIndex = i6;
            if (this.mFirstVisibleIndex < 0) {
                this.mFirstVisibleIndex = i6;
            }
            this.mProvider.addItem(this.mTmpItem[0], i6, iCreateItem, i7, edge);
            if (!z && checkAppendOverLimit(i)) {
                return true;
            }
            if (edge == Integer.MAX_VALUE) {
                edge = this.mProvider.getEdge(i6);
            }
            if (i7 == this.mNumRows - 1 && z) {
                return true;
            }
            i6++;
            lastIndex = i3;
        }
        return false;
    }

    protected final int appendVisibleItemToRow(int i, int i2, int i3) {
        int edge;
        int i4 = this.mLastVisibleIndex;
        if (i4 >= 0 && (i4 != getLastIndex() || this.mLastVisibleIndex != i - 1)) {
            throw new IllegalStateException();
        }
        int i5 = this.mLastVisibleIndex;
        if (i5 < 0) {
            edge = (this.mLocations.size() <= 0 || i != getLastIndex() + 1) ? 0 : calculateOffsetAfterLastItem(i2);
        } else {
            edge = i3 - this.mProvider.getEdge(i5);
        }
        Location location = new Location(i2, edge, 0);
        this.mLocations.addLast(location);
        Object obj = this.mPendingItem;
        if (obj != null) {
            location.size = this.mPendingItemSize;
            this.mPendingItem = null;
        } else {
            location.size = this.mProvider.createItem(i, true, this.mTmpItem, false);
            obj = this.mTmpItem[0];
        }
        Object obj2 = obj;
        if (this.mLocations.size() == 1) {
            this.mLastVisibleIndex = i;
            this.mFirstVisibleIndex = i;
            this.mFirstIndex = i;
        } else {
            int i6 = this.mLastVisibleIndex;
            if (i6 < 0) {
                this.mLastVisibleIndex = i;
                this.mFirstVisibleIndex = i;
            } else {
                this.mLastVisibleIndex = i6 + 1;
            }
        }
        this.mProvider.addItem(obj2, i, location.size, i2, i3);
        return location.size;
    }

    @Override // androidx.leanback.widget.Grid
    public final CircularIntArray[] getItemPositionsInRows(int i, int i2) {
        for (int i3 = 0; i3 < this.mNumRows; i3++) {
            this.mTmpItemPositionsInRows[i3].clear();
        }
        if (i >= 0) {
            while (i <= i2) {
                CircularIntArray circularIntArray = this.mTmpItemPositionsInRows[getLocation(i).row];
                if (circularIntArray.size() > 0 && circularIntArray.getLast() == i - 1) {
                    circularIntArray.popLast();
                    circularIntArray.addLast(i);
                } else {
                    circularIntArray.addLast(i);
                    circularIntArray.addLast(i);
                }
                i++;
            }
        }
        return this.mTmpItemPositionsInRows;
    }

    @Override // androidx.leanback.widget.Grid
    public void invalidateItemsAfter(int i) {
        super.invalidateItemsAfter(i);
        this.mLocations.removeFromEnd((getLastIndex() - i) + 1);
        if (this.mLocations.size() == 0) {
            this.mFirstIndex = -1;
        }
    }
}
