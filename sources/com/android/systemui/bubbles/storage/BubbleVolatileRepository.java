package com.android.systemui.bubbles.storage;

import android.content.pm.LauncherApps;
import android.os.UserHandle;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.bubbles.ShortcutKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: BubbleVolatileRepository.kt */
/* loaded from: classes.dex */
public final class BubbleVolatileRepository {
    private int capacity;
    private Set<BubbleEntity> entities;
    private final LauncherApps launcherApps;

    @VisibleForTesting
    public static /* synthetic */ void capacity$annotations() {
    }

    public BubbleVolatileRepository(@NotNull LauncherApps launcherApps) {
        Intrinsics.checkParameterIsNotNull(launcherApps, "launcherApps");
        this.launcherApps = launcherApps;
        this.entities = new LinkedHashSet();
        this.capacity = 16;
    }

    @NotNull
    public final synchronized List<BubbleEntity> getBubbles() {
        return CollectionsKt___CollectionsKt.toList(this.entities);
    }

    public final synchronized void addBubbles(@NotNull List<BubbleEntity> bubbles) {
        Intrinsics.checkParameterIsNotNull(bubbles, "bubbles");
        if (bubbles.isEmpty()) {
            return;
        }
        List listTakeLast = CollectionsKt___CollectionsKt.takeLast(bubbles, this.capacity);
        ArrayList arrayList = new ArrayList();
        for (Object obj : listTakeLast) {
            final BubbleEntity bubbleEntity = (BubbleEntity) obj;
            if (!this.entities.removeIf(new Predicate<BubbleEntity>() { // from class: com.android.systemui.bubbles.storage.BubbleVolatileRepository$addBubbles$uniqueBubbles$1$1
                @Override // java.util.function.Predicate
                public final boolean test(@NotNull BubbleEntity e) {
                    Intrinsics.checkParameterIsNotNull(e, "e");
                    return Intrinsics.areEqual(bubbleEntity.getKey(), e.getKey());
                }
            })) {
                arrayList.add(obj);
            }
        }
        int size = (this.entities.size() + listTakeLast.size()) - this.capacity;
        if (size > 0) {
            uncache(CollectionsKt___CollectionsKt.take(this.entities, size));
            this.entities = CollectionsKt___CollectionsKt.toMutableSet(CollectionsKt___CollectionsKt.drop(this.entities, size));
        }
        this.entities.addAll(listTakeLast);
        cache(arrayList);
    }

    public final synchronized void removeBubbles(@NotNull List<BubbleEntity> bubbles) {
        Intrinsics.checkParameterIsNotNull(bubbles, "bubbles");
        ArrayList arrayList = new ArrayList();
        for (Object obj : bubbles) {
            final BubbleEntity bubbleEntity = (BubbleEntity) obj;
            if (this.entities.removeIf(new Predicate<BubbleEntity>() { // from class: com.android.systemui.bubbles.storage.BubbleVolatileRepository$removeBubbles$1$1
                @Override // java.util.function.Predicate
                public final boolean test(@NotNull BubbleEntity e) {
                    Intrinsics.checkParameterIsNotNull(e, "e");
                    return Intrinsics.areEqual(bubbleEntity.getKey(), e.getKey());
                }
            })) {
                arrayList.add(obj);
            }
        }
        uncache(arrayList);
    }

    private final void cache(List<BubbleEntity> list) {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        for (Object obj : list) {
            BubbleEntity bubbleEntity = (BubbleEntity) obj;
            ShortcutKey shortcutKey = new ShortcutKey(bubbleEntity.getUserId(), bubbleEntity.getPackageName());
            Object arrayList = linkedHashMap.get(shortcutKey);
            if (arrayList == null) {
                arrayList = new ArrayList();
                linkedHashMap.put(shortcutKey, arrayList);
            }
            ((List) arrayList).add(obj);
        }
        for (Map.Entry entry : linkedHashMap.entrySet()) {
            ShortcutKey shortcutKey2 = (ShortcutKey) entry.getKey();
            List list2 = (List) entry.getValue();
            LauncherApps launcherApps = this.launcherApps;
            String pkg = shortcutKey2.getPkg();
            ArrayList arrayList2 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list2, 10));
            Iterator it = list2.iterator();
            while (it.hasNext()) {
                arrayList2.add(((BubbleEntity) it.next()).getShortcutId());
            }
            launcherApps.cacheShortcuts(pkg, arrayList2, UserHandle.of(shortcutKey2.getUserId()), 1);
        }
    }

    private final void uncache(List<BubbleEntity> list) {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        for (Object obj : list) {
            BubbleEntity bubbleEntity = (BubbleEntity) obj;
            ShortcutKey shortcutKey = new ShortcutKey(bubbleEntity.getUserId(), bubbleEntity.getPackageName());
            Object arrayList = linkedHashMap.get(shortcutKey);
            if (arrayList == null) {
                arrayList = new ArrayList();
                linkedHashMap.put(shortcutKey, arrayList);
            }
            ((List) arrayList).add(obj);
        }
        for (Map.Entry entry : linkedHashMap.entrySet()) {
            ShortcutKey shortcutKey2 = (ShortcutKey) entry.getKey();
            List list2 = (List) entry.getValue();
            LauncherApps launcherApps = this.launcherApps;
            String pkg = shortcutKey2.getPkg();
            ArrayList arrayList2 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list2, 10));
            Iterator it = list2.iterator();
            while (it.hasNext()) {
                arrayList2.add(((BubbleEntity) it.next()).getShortcutId());
            }
            launcherApps.uncacheShortcuts(pkg, arrayList2, UserHandle.of(shortcutKey2.getUserId()), 1);
        }
    }
}
