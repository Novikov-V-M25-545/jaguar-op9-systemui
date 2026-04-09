package com.android.systemui.controls.controller;

import android.content.ComponentName;
import android.service.controls.Control;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kotlin.Pair;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt__MutableCollectionsKt;
import kotlin.collections.MapsKt__MapsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt___RangesKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsControllerImpl.kt */
/* loaded from: classes.dex */
final class Favorites {
    public static final Favorites INSTANCE = new Favorites();
    private static Map<ComponentName, ? extends List<StructureInfo>> favMap = MapsKt__MapsKt.emptyMap();

    private Favorites() {
    }

    @NotNull
    public final List<StructureInfo> getAllStructures() {
        Map<ComponentName, ? extends List<StructureInfo>> map = favMap;
        ArrayList arrayList = new ArrayList();
        Iterator<Map.Entry<ComponentName, ? extends List<StructureInfo>>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            CollectionsKt__MutableCollectionsKt.addAll(arrayList, it.next().getValue());
        }
        return arrayList;
    }

    @NotNull
    public final List<StructureInfo> getStructuresForComponent(@NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        List<StructureInfo> list = favMap.get(componentName);
        return list != null ? list : CollectionsKt__CollectionsKt.emptyList();
    }

    @NotNull
    public final List<ControlInfo> getControlsForStructure(@NotNull StructureInfo structure) {
        Object next;
        List<ControlInfo> controls;
        Intrinsics.checkParameterIsNotNull(structure, "structure");
        Iterator<T> it = getStructuresForComponent(structure.getComponentName()).iterator();
        while (true) {
            if (!it.hasNext()) {
                next = null;
                break;
            }
            next = it.next();
            if (Intrinsics.areEqual(((StructureInfo) next).getStructure(), structure.getStructure())) {
                break;
            }
        }
        StructureInfo structureInfo = (StructureInfo) next;
        return (structureInfo == null || (controls = structureInfo.getControls()) == null) ? CollectionsKt__CollectionsKt.emptyList() : controls;
    }

    @NotNull
    public final List<ControlInfo> getControlsForComponent(@NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        List<StructureInfo> structuresForComponent = getStructuresForComponent(componentName);
        ArrayList arrayList = new ArrayList();
        Iterator<T> it = structuresForComponent.iterator();
        while (it.hasNext()) {
            CollectionsKt__MutableCollectionsKt.addAll(arrayList, ((StructureInfo) it.next()).getControls());
        }
        return arrayList;
    }

    public final void removeStructures(@NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Map<ComponentName, ? extends List<StructureInfo>> mutableMap = MapsKt__MapsKt.toMutableMap(favMap);
        mutableMap.remove(componentName);
        favMap = mutableMap;
    }

    /* JADX WARN: Removed duplicated region for block: B:27:0x006f  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public final boolean addFavorite(@org.jetbrains.annotations.NotNull android.content.ComponentName r10, @org.jetbrains.annotations.NotNull java.lang.CharSequence r11, @org.jetbrains.annotations.NotNull com.android.systemui.controls.controller.ControlInfo r12) {
        /*
            r9 = this;
            java.lang.String r0 = "componentName"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r10, r0)
            java.lang.String r0 = "structureName"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r11, r0)
            java.lang.String r0 = "controlInfo"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r12, r0)
            java.util.List r0 = r9.getControlsForComponent(r10)
            boolean r1 = r0 instanceof java.util.Collection
            r2 = 1
            r3 = 0
            if (r1 == 0) goto L21
            boolean r1 = r0.isEmpty()
            if (r1 == 0) goto L21
        L1f:
            r0 = r3
            goto L40
        L21:
            java.util.Iterator r0 = r0.iterator()
        L25:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L1f
            java.lang.Object r1 = r0.next()
            com.android.systemui.controls.controller.ControlInfo r1 = (com.android.systemui.controls.controller.ControlInfo) r1
            java.lang.String r1 = r1.getControlId()
            java.lang.String r4 = r12.getControlId()
            boolean r1 = kotlin.jvm.internal.Intrinsics.areEqual(r1, r4)
            if (r1 == 0) goto L25
            r0 = r2
        L40:
            if (r0 == 0) goto L43
            return r3
        L43:
            java.util.Map<android.content.ComponentName, ? extends java.util.List<com.android.systemui.controls.controller.StructureInfo>> r0 = com.android.systemui.controls.controller.Favorites.favMap
            java.lang.Object r0 = r0.get(r10)
            java.util.List r0 = (java.util.List) r0
            if (r0 == 0) goto L6f
            java.util.Iterator r0 = r0.iterator()
        L51:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L69
            java.lang.Object r1 = r0.next()
            r3 = r1
            com.android.systemui.controls.controller.StructureInfo r3 = (com.android.systemui.controls.controller.StructureInfo) r3
            java.lang.CharSequence r3 = r3.getStructure()
            boolean r3 = kotlin.jvm.internal.Intrinsics.areEqual(r3, r11)
            if (r3 == 0) goto L51
            goto L6a
        L69:
            r1 = 0
        L6a:
            com.android.systemui.controls.controller.StructureInfo r1 = (com.android.systemui.controls.controller.StructureInfo) r1
            if (r1 == 0) goto L6f
            goto L78
        L6f:
            com.android.systemui.controls.controller.StructureInfo r1 = new com.android.systemui.controls.controller.StructureInfo
            java.util.List r0 = kotlin.collections.CollectionsKt.emptyList()
            r1.<init>(r10, r11, r0)
        L78:
            r3 = r1
            r4 = 0
            r5 = 0
            java.util.List r10 = r3.getControls()
            java.util.List r6 = kotlin.collections.CollectionsKt.plus(r10, r12)
            r7 = 3
            r8 = 0
            com.android.systemui.controls.controller.StructureInfo r10 = com.android.systemui.controls.controller.StructureInfo.copy$default(r3, r4, r5, r6, r7, r8)
            r9.replaceControls(r10)
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controls.controller.Favorites.addFavorite(android.content.ComponentName, java.lang.CharSequence, com.android.systemui.controls.controller.ControlInfo):boolean");
    }

    public final void replaceControls(@NotNull StructureInfo updatedStructure) {
        Intrinsics.checkParameterIsNotNull(updatedStructure, "updatedStructure");
        Map<ComponentName, ? extends List<StructureInfo>> mutableMap = MapsKt__MapsKt.toMutableMap(favMap);
        ArrayList arrayList = new ArrayList();
        ComponentName componentName = updatedStructure.getComponentName();
        boolean z = false;
        for (StructureInfo structureInfo : getStructuresForComponent(componentName)) {
            if (Intrinsics.areEqual(structureInfo.getStructure(), updatedStructure.getStructure())) {
                z = true;
                structureInfo = updatedStructure;
            }
            if (!structureInfo.getControls().isEmpty()) {
                arrayList.add(structureInfo);
            }
        }
        if (!z && !updatedStructure.getControls().isEmpty()) {
            arrayList.add(updatedStructure);
        }
        mutableMap.put(componentName, arrayList);
        favMap = mutableMap;
    }

    public final void clear() {
        favMap = MapsKt__MapsKt.emptyMap();
    }

    public final boolean updateControls(@NotNull ComponentName componentName, @NotNull List<Control> controls) {
        Pair pair;
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(controls, "controls");
        LinkedHashMap linkedHashMap = new LinkedHashMap(RangesKt___RangesKt.coerceAtLeast(MapsKt__MapsKt.mapCapacity(CollectionsKt__IterablesKt.collectionSizeOrDefault(controls, 10)), 16));
        for (Object obj : controls) {
            linkedHashMap.put(((Control) obj).getControlId(), obj);
        }
        LinkedHashMap linkedHashMap2 = new LinkedHashMap();
        boolean z = false;
        for (StructureInfo structureInfo : getStructuresForComponent(componentName)) {
            for (ControlInfo controlInfoCopy$default : structureInfo.getControls()) {
                Control control = (Control) linkedHashMap.get(controlInfoCopy$default.getControlId());
                if (control != null) {
                    if ((!Intrinsics.areEqual(control.getTitle(), controlInfoCopy$default.getControlTitle())) || (!Intrinsics.areEqual(control.getSubtitle(), controlInfoCopy$default.getControlSubtitle())) || control.getDeviceType() != controlInfoCopy$default.getDeviceType()) {
                        CharSequence title = control.getTitle();
                        Intrinsics.checkExpressionValueIsNotNull(title, "updatedControl.title");
                        CharSequence subtitle = control.getSubtitle();
                        Intrinsics.checkExpressionValueIsNotNull(subtitle, "updatedControl.subtitle");
                        controlInfoCopy$default = ControlInfo.copy$default(controlInfoCopy$default, null, title, subtitle, control.getDeviceType(), 1, null);
                        z = true;
                    }
                    CharSequence structure = control.getStructure();
                    if (structure == null) {
                        structure = "";
                    }
                    if (!Intrinsics.areEqual(structureInfo.getStructure(), structure)) {
                        z = true;
                    }
                    pair = new Pair(structure, controlInfoCopy$default);
                } else {
                    pair = new Pair(structureInfo.getStructure(), controlInfoCopy$default);
                }
                CharSequence charSequence = (CharSequence) pair.component1();
                ControlInfo controlInfo = (ControlInfo) pair.component2();
                Object arrayList = linkedHashMap2.get(charSequence);
                if (arrayList == null) {
                    arrayList = new ArrayList();
                    linkedHashMap2.put(charSequence, arrayList);
                }
                ((List) arrayList).add(controlInfo);
            }
        }
        if (!z) {
            return false;
        }
        ArrayList arrayList2 = new ArrayList(linkedHashMap2.size());
        for (Map.Entry entry : linkedHashMap2.entrySet()) {
            arrayList2.add(new StructureInfo(componentName, (CharSequence) entry.getKey(), (List) entry.getValue()));
        }
        Map<ComponentName, ? extends List<StructureInfo>> mutableMap = MapsKt__MapsKt.toMutableMap(favMap);
        mutableMap.put(componentName, arrayList2);
        favMap = mutableMap;
        return true;
    }

    public final void load(@NotNull List<StructureInfo> structures) {
        Intrinsics.checkParameterIsNotNull(structures, "structures");
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        for (Object obj : structures) {
            ComponentName componentName = ((StructureInfo) obj).getComponentName();
            Object arrayList = linkedHashMap.get(componentName);
            if (arrayList == null) {
                arrayList = new ArrayList();
                linkedHashMap.put(componentName, arrayList);
            }
            ((List) arrayList).add(obj);
        }
        favMap = linkedHashMap;
    }
}
