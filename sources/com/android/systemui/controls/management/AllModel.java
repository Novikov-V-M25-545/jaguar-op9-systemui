package com.android.systemui.controls.management;

import android.service.controls.Control;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.android.systemui.controls.ControlStatus;
import com.android.systemui.controls.controller.ControlInfo;
import com.android.systemui.controls.management.ControlsModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kotlin.collections.CollectionsKt__MutableCollectionsKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.collections.MapsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.markers.KMutableMap;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt___SequencesKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: AllModel.kt */
/* loaded from: classes.dex */
public final class AllModel implements ControlsModel {
    private final List<ControlStatus> controls;
    private final ControlsModel.ControlsModelCallback controlsModelCallback;

    @NotNull
    private final List<ElementWrapper> elements;
    private final CharSequence emptyZoneString;
    private final List<String> favoriteIds;
    private boolean modified;

    @Nullable
    private final Void moveHelper;

    public AllModel(@NotNull List<ControlStatus> controls, @NotNull List<String> initialFavoriteIds, @NotNull CharSequence emptyZoneString, @NotNull ControlsModel.ControlsModelCallback controlsModelCallback) {
        Intrinsics.checkParameterIsNotNull(controls, "controls");
        Intrinsics.checkParameterIsNotNull(initialFavoriteIds, "initialFavoriteIds");
        Intrinsics.checkParameterIsNotNull(emptyZoneString, "emptyZoneString");
        Intrinsics.checkParameterIsNotNull(controlsModelCallback, "controlsModelCallback");
        this.controls = controls;
        this.emptyZoneString = emptyZoneString;
        this.controlsModelCallback = controlsModelCallback;
        HashSet hashSet = new HashSet();
        Iterator<T> it = controls.iterator();
        while (it.hasNext()) {
            hashSet.add(((ControlStatus) it.next()).getControl().getControlId());
        }
        ArrayList arrayList = new ArrayList();
        for (Object obj : initialFavoriteIds) {
            if (hashSet.contains((String) obj)) {
                arrayList.add(obj);
            }
        }
        this.favoriteIds = CollectionsKt___CollectionsKt.toMutableList((Collection) arrayList);
        this.elements = createWrappers(this.controls);
    }

    @Override // com.android.systemui.controls.management.ControlsModel
    public /* bridge */ /* synthetic */ ControlsModel.MoveHelper getMoveHelper() {
        return (ControlsModel.MoveHelper) m103getMoveHelper();
    }

    @Nullable
    /* renamed from: getMoveHelper, reason: collision with other method in class */
    public Void m103getMoveHelper() {
        return this.moveHelper;
    }

    @Override // com.android.systemui.controls.management.ControlsModel
    @NotNull
    public List<ControlInfo> getFavorites() {
        Object next;
        List<String> list = this.favoriteIds;
        ArrayList arrayList = new ArrayList();
        for (String str : list) {
            Iterator<T> it = this.controls.iterator();
            while (true) {
                if (!it.hasNext()) {
                    next = null;
                    break;
                }
                next = it.next();
                if (Intrinsics.areEqual(((ControlStatus) next).getControl().getControlId(), str)) {
                    break;
                }
            }
            ControlStatus controlStatus = (ControlStatus) next;
            Control control = controlStatus != null ? controlStatus.getControl() : null;
            ControlInfo controlInfoFromControl = control != null ? ControlInfo.Companion.fromControl(control) : null;
            if (controlInfoFromControl != null) {
                arrayList.add(controlInfoFromControl);
            }
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
        Object next;
        boolean zRemove;
        ControlStatus controlStatus;
        Intrinsics.checkParameterIsNotNull(controlId, "controlId");
        Iterator<T> it = getElements().iterator();
        while (true) {
            if (!it.hasNext()) {
                next = null;
                break;
            }
            next = it.next();
            ElementWrapper elementWrapper = (ElementWrapper) next;
            if ((elementWrapper instanceof ControlStatusWrapper) && Intrinsics.areEqual(((ControlStatusWrapper) elementWrapper).getControlStatus().getControl().getControlId(), controlId)) {
                break;
            }
        }
        ControlStatusWrapper controlStatusWrapper = (ControlStatusWrapper) next;
        if (controlStatusWrapper == null || (controlStatus = controlStatusWrapper.getControlStatus()) == null || z != controlStatus.getFavorite()) {
            if (z) {
                zRemove = this.favoriteIds.add(controlId);
            } else {
                zRemove = this.favoriteIds.remove(controlId);
            }
            if (zRemove && !this.modified) {
                this.modified = true;
                this.controlsModelCallback.onFirstChange();
            }
            if (controlStatusWrapper != null) {
                controlStatusWrapper.getControlStatus().setFavorite(z);
            }
        }
    }

    private final List<ElementWrapper> createWrappers(List<ControlStatus> list) {
        OrderedMap orderedMap = new OrderedMap(new ArrayMap());
        for (Object obj : list) {
            CharSequence zone = ((ControlStatus) obj).getControl().getZone();
            if (zone == null) {
                zone = "";
            }
            Object arrayList = orderedMap.get(zone);
            if (arrayList == null) {
                arrayList = new ArrayList();
                orderedMap.put(zone, arrayList);
            }
            ((List) arrayList).add(obj);
        }
        ArrayList arrayList2 = new ArrayList();
        Sequence sequence = null;
        for (CharSequence zoneName : orderedMap.getOrderedKeys()) {
            Object value = MapsKt.getValue(orderedMap, zoneName);
            Intrinsics.checkExpressionValueIsNotNull(value, "map.getValue(zoneName)");
            Sequence map = SequencesKt___SequencesKt.map(CollectionsKt___CollectionsKt.asSequence((Iterable) value), new Function1<ControlStatus, ControlStatusWrapper>() { // from class: com.android.systemui.controls.management.AllModel$createWrappers$values$1
                @Override // kotlin.jvm.functions.Function1
                @NotNull
                public final ControlStatusWrapper invoke(@NotNull ControlStatus it) {
                    Intrinsics.checkParameterIsNotNull(it, "it");
                    return new ControlStatusWrapper(it);
                }
            });
            if (TextUtils.isEmpty(zoneName)) {
                sequence = map;
            } else {
                Intrinsics.checkExpressionValueIsNotNull(zoneName, "zoneName");
                arrayList2.add(new ZoneNameWrapper(zoneName));
                CollectionsKt__MutableCollectionsKt.addAll(arrayList2, map);
            }
        }
        if (sequence != null) {
            if (orderedMap.size() != 1) {
                arrayList2.add(new ZoneNameWrapper(this.emptyZoneString));
            }
            CollectionsKt__MutableCollectionsKt.addAll(arrayList2, sequence);
        }
        return arrayList2;
    }

    /* compiled from: AllModel.kt */
    private static final class OrderedMap<K, V> implements Map<K, V>, KMutableMap {
        private final Map<K, V> map;

        @NotNull
        private final List<K> orderedKeys;

        @Override // java.util.Map
        public boolean containsKey(Object obj) {
            return this.map.containsKey(obj);
        }

        @Override // java.util.Map
        public boolean containsValue(Object obj) {
            return this.map.containsValue(obj);
        }

        @Override // java.util.Map
        @Nullable
        public V get(Object obj) {
            return this.map.get(obj);
        }

        @NotNull
        public Set<Map.Entry<K, V>> getEntries() {
            return this.map.entrySet();
        }

        @NotNull
        public Set<K> getKeys() {
            return this.map.keySet();
        }

        public int getSize() {
            return this.map.size();
        }

        @NotNull
        public Collection<V> getValues() {
            return this.map.values();
        }

        @Override // java.util.Map
        public boolean isEmpty() {
            return this.map.isEmpty();
        }

        @Override // java.util.Map
        public void putAll(@NotNull Map<? extends K, ? extends V> from) {
            Intrinsics.checkParameterIsNotNull(from, "from");
            this.map.putAll(from);
        }

        public OrderedMap(@NotNull Map<K, V> map) {
            Intrinsics.checkParameterIsNotNull(map, "map");
            this.map = map;
            this.orderedKeys = new ArrayList();
        }

        @Override // java.util.Map
        public final /* bridge */ Set<Map.Entry<K, V>> entrySet() {
            return getEntries();
        }

        @Override // java.util.Map
        public final /* bridge */ Set<K> keySet() {
            return getKeys();
        }

        @Override // java.util.Map
        public final /* bridge */ int size() {
            return getSize();
        }

        @Override // java.util.Map
        public final /* bridge */ Collection<V> values() {
            return getValues();
        }

        @NotNull
        public final List<K> getOrderedKeys() {
            return this.orderedKeys;
        }

        @Override // java.util.Map
        @Nullable
        public V put(K k, V v) {
            if (!this.map.containsKey(k)) {
                this.orderedKeys.add(k);
            }
            return this.map.put(k, v);
        }

        @Override // java.util.Map
        public void clear() {
            this.orderedKeys.clear();
            this.map.clear();
        }

        @Override // java.util.Map
        @Nullable
        public V remove(Object obj) {
            V vRemove = this.map.remove(obj);
            if (vRemove != null) {
                this.orderedKeys.remove(obj);
            }
            return vRemove;
        }
    }
}
