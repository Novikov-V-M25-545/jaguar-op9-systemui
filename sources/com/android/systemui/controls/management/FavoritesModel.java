package com.android.systemui.controls.management;

import android.content.ComponentName;
import android.util.Log;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.android.systemui.controls.ControlInterface;
import com.android.systemui.controls.CustomIconCache;
import com.android.systemui.controls.controller.ControlInfo;
import com.android.systemui.controls.management.ControlsModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import kotlin.TypeCastException;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: FavoritesModel.kt */
/* loaded from: classes.dex */
public final class FavoritesModel implements ControlsModel {
    public static final Companion Companion = new Companion(null);
    private RecyclerView.Adapter<?> adapter;
    private final ComponentName componentName;
    private final CustomIconCache customIconCache;
    private int dividerPosition;

    @NotNull
    private final List<ElementWrapper> elements;
    private final FavoritesModelCallback favoritesModelCallback;

    @NotNull
    private final ItemTouchHelper.SimpleCallback itemTouchHelperCallback;
    private boolean modified;

    @NotNull
    private final ControlsModel.MoveHelper moveHelper;

    /* compiled from: FavoritesModel.kt */
    public interface FavoritesModelCallback extends ControlsModel.ControlsModelCallback {
        void onNoneChanged(boolean z);
    }

    public FavoritesModel(@NotNull CustomIconCache customIconCache, @NotNull ComponentName componentName, @NotNull List<ControlInfo> favorites, @NotNull FavoritesModelCallback favoritesModelCallback) {
        Intrinsics.checkParameterIsNotNull(customIconCache, "customIconCache");
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(favorites, "favorites");
        Intrinsics.checkParameterIsNotNull(favoritesModelCallback, "favoritesModelCallback");
        this.customIconCache = customIconCache;
        this.componentName = componentName;
        this.favoritesModelCallback = favoritesModelCallback;
        this.moveHelper = new ControlsModel.MoveHelper() { // from class: com.android.systemui.controls.management.FavoritesModel$moveHelper$1
            @Override // com.android.systemui.controls.management.ControlsModel.MoveHelper
            public boolean canMoveBefore(int i) {
                return i > 0 && i < this.this$0.dividerPosition;
            }

            @Override // com.android.systemui.controls.management.ControlsModel.MoveHelper
            public boolean canMoveAfter(int i) {
                return i >= 0 && i < this.this$0.dividerPosition - 1;
            }

            @Override // com.android.systemui.controls.management.ControlsModel.MoveHelper
            public void moveBefore(int i) {
                if (!canMoveBefore(i)) {
                    Log.w("FavoritesModel", "Cannot move position " + i + " before");
                    return;
                }
                this.this$0.onMoveItem(i, i - 1);
            }

            @Override // com.android.systemui.controls.management.ControlsModel.MoveHelper
            public void moveAfter(int i) {
                if (!canMoveAfter(i)) {
                    Log.w("FavoritesModel", "Cannot move position " + i + " after");
                    return;
                }
                this.this$0.onMoveItem(i, i + 1);
            }
        };
        ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(favorites, 10));
        Iterator<T> it = favorites.iterator();
        while (it.hasNext()) {
            arrayList.add(new ControlInfoWrapper(this.componentName, (ControlInfo) it.next(), true, new FavoritesModel$elements$1$1(this.customIconCache)));
        }
        final int i = 0;
        this.elements = CollectionsKt___CollectionsKt.plus(arrayList, new DividerWrapper(false, false, 3, null));
        this.dividerPosition = getElements().size() - 1;
        this.itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(i, i) { // from class: com.android.systemui.controls.management.FavoritesModel$itemTouchHelperCallback$1
            private final int MOVEMENT = 15;

            @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
            public boolean isItemViewSwipeEnabled() {
                return false;
            }

            @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
            public void onSwiped(@NotNull RecyclerView.ViewHolder viewHolder, int i2) {
                Intrinsics.checkParameterIsNotNull(viewHolder, "viewHolder");
            }

            @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
            public boolean onMove(@NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder, @NotNull RecyclerView.ViewHolder target) {
                Intrinsics.checkParameterIsNotNull(recyclerView, "recyclerView");
                Intrinsics.checkParameterIsNotNull(viewHolder, "viewHolder");
                Intrinsics.checkParameterIsNotNull(target, "target");
                this.this$0.onMoveItem(viewHolder.getBindingAdapterPosition(), target.getBindingAdapterPosition());
                return true;
            }

            @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
            public int getMovementFlags(@NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder) {
                Intrinsics.checkParameterIsNotNull(recyclerView, "recyclerView");
                Intrinsics.checkParameterIsNotNull(viewHolder, "viewHolder");
                if (viewHolder.getBindingAdapterPosition() < this.this$0.dividerPosition) {
                    return ItemTouchHelper.Callback.makeMovementFlags(this.MOVEMENT, 0);
                }
                return ItemTouchHelper.Callback.makeMovementFlags(0, 0);
            }

            @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
            public boolean canDropOver(@NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder current, @NotNull RecyclerView.ViewHolder target) {
                Intrinsics.checkParameterIsNotNull(recyclerView, "recyclerView");
                Intrinsics.checkParameterIsNotNull(current, "current");
                Intrinsics.checkParameterIsNotNull(target, "target");
                return target.getBindingAdapterPosition() < this.this$0.dividerPosition;
            }
        };
    }

    /* compiled from: FavoritesModel.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    @Override // com.android.systemui.controls.management.ControlsModel
    @NotNull
    public ControlsModel.MoveHelper getMoveHelper() {
        return this.moveHelper;
    }

    public void attachAdapter(@NotNull RecyclerView.Adapter<?> adapter) {
        Intrinsics.checkParameterIsNotNull(adapter, "adapter");
        this.adapter = adapter;
    }

    @Override // com.android.systemui.controls.management.ControlsModel
    @NotNull
    public List<ControlInfo> getFavorites() {
        List<ElementWrapper> listTake = CollectionsKt___CollectionsKt.take(getElements(), this.dividerPosition);
        ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(listTake, 10));
        for (ElementWrapper elementWrapper : listTake) {
            if (elementWrapper == null) {
                throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.controls.management.ControlInfoWrapper");
            }
            arrayList.add(((ControlInfoWrapper) elementWrapper).getControlInfo());
        }
        return arrayList;
    }

    @Override // com.android.systemui.controls.management.ControlsModel
    @NotNull
    public List<ElementWrapper> getElements() {
        return this.elements;
    }

    @Override // com.android.systemui.controls.management.ControlsModel
    public void changeFavoriteStatus(@NotNull String controlId, boolean z) {
        Intrinsics.checkParameterIsNotNull(controlId, "controlId");
        Iterator<ElementWrapper> it = getElements().iterator();
        int i = 0;
        while (true) {
            if (!it.hasNext()) {
                i = -1;
                break;
            }
            Object obj = (ElementWrapper) it.next();
            if ((obj instanceof ControlInterface) && Intrinsics.areEqual(((ControlInterface) obj).getControlId(), controlId)) {
                break;
            } else {
                i++;
            }
        }
        if (i == -1) {
            return;
        }
        int i2 = this.dividerPosition;
        if (i >= i2 || !z) {
            if (i <= i2 || z) {
                if (z) {
                    onMoveItemInternal(i, i2);
                } else {
                    onMoveItemInternal(i, getElements().size() - 1);
                }
            }
        }
    }

    public void onMoveItem(int i, int i2) {
        onMoveItemInternal(i, i2);
    }

    private final void updateDividerNone(int i, boolean z) {
        ElementWrapper elementWrapper = getElements().get(i);
        if (elementWrapper == null) {
            throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.controls.management.DividerWrapper");
        }
        ((DividerWrapper) elementWrapper).setShowNone(z);
        this.favoritesModelCallback.onNoneChanged(z);
    }

    private final void updateDividerShow(int i, boolean z) {
        ElementWrapper elementWrapper = getElements().get(i);
        if (elementWrapper == null) {
            throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.controls.management.DividerWrapper");
        }
        ((DividerWrapper) elementWrapper).setShowDivider(z);
    }

    private final void onMoveItemInternal(int i, int i2) {
        RecyclerView.Adapter<?> adapter;
        int i3 = this.dividerPosition;
        if (i == i3) {
            return;
        }
        boolean z = false;
        if ((i < i3 && i2 >= i3) || (i > i3 && i2 <= i3)) {
            if (i < i3 && i2 >= i3) {
                ElementWrapper elementWrapper = getElements().get(i);
                if (elementWrapper == null) {
                    throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.controls.management.ControlInfoWrapper");
                }
                ((ControlInfoWrapper) elementWrapper).setFavorite(false);
            } else if (i > i3 && i2 <= i3) {
                ElementWrapper elementWrapper2 = getElements().get(i);
                if (elementWrapper2 == null) {
                    throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.controls.management.ControlInfoWrapper");
                }
                ((ControlInfoWrapper) elementWrapper2).setFavorite(true);
            }
            updateDivider(i, i2);
            z = true;
        }
        moveElement(i, i2);
        RecyclerView.Adapter<?> adapter2 = this.adapter;
        if (adapter2 != null) {
            adapter2.notifyItemMoved(i, i2);
        }
        if (z && (adapter = this.adapter) != null) {
            adapter.notifyItemChanged(i2, new Object());
        }
        if (this.modified) {
            return;
        }
        this.modified = true;
        this.favoritesModelCallback.onFirstChange();
    }

    private final void updateDivider(int i, int i2) {
        boolean z;
        RecyclerView.Adapter<?> adapter;
        int i3 = this.dividerPosition;
        boolean z2 = false;
        if (i < i3 && i2 >= i3) {
            int i4 = i3 - 1;
            this.dividerPosition = i4;
            if (i4 == 0) {
                updateDividerNone(i3, true);
                z2 = true;
            }
            if (this.dividerPosition == getElements().size() - 2) {
                updateDividerShow(i3, true);
                z2 = true;
            }
        } else if (i > i3 && i2 <= i3) {
            int i5 = i3 + 1;
            this.dividerPosition = i5;
            if (i5 == 1) {
                updateDividerNone(i3, false);
                z = true;
            } else {
                z = false;
            }
            if (this.dividerPosition == getElements().size() - 1) {
                updateDividerShow(i3, false);
                z2 = true;
            } else {
                z2 = z;
            }
        }
        if (!z2 || (adapter = this.adapter) == null) {
            return;
        }
        adapter.notifyItemChanged(i3);
    }

    private final void moveElement(int i, int i2) {
        if (i < i2) {
            while (i < i2) {
                int i3 = i + 1;
                Collections.swap(getElements(), i, i3);
                i = i3;
            }
            return;
        }
        int i4 = i2 + 1;
        if (i < i4) {
            return;
        }
        while (true) {
            Collections.swap(getElements(), i, i - 1);
            if (i == i4) {
                return;
            } else {
                i--;
            }
        }
    }

    @NotNull
    public final ItemTouchHelper.SimpleCallback getItemTouchHelperCallback() {
        return this.itemTouchHelperCallback;
    }
}
