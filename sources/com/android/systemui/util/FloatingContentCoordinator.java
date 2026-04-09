package com.android.systemui.util;

import android.graphics.Rect;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kotlin.Lazy;
import kotlin.LazyKt__LazyJVMKt;
import kotlin.Pair;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.comparisons.ComparisonsKt__ComparisonsKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.PropertyReference0Impl;
import kotlin.jvm.internal.Ref$ObjectRef;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KProperty;
import org.jetbrains.annotations.NotNull;

/* compiled from: FloatingContentCoordinator.kt */
/* loaded from: classes.dex */
public final class FloatingContentCoordinator {
    public static final Companion Companion = new Companion(null);
    private final Map<FloatingContent, Rect> allContentBounds = new HashMap();
    private boolean currentlyResolvingConflicts;

    /* compiled from: FloatingContentCoordinator.kt */
    public interface FloatingContent {
        @NotNull
        Rect getAllowedFloatingBoundsRegion();

        @NotNull
        Rect getFloatingBoundsOnScreen();

        void moveToBounds(@NotNull Rect rect);

        @NotNull
        default Rect calculateNewBoundsOnOverlap(@NotNull Rect overlappingContentBounds, @NotNull List<Rect> otherContentBounds) {
            Intrinsics.checkParameterIsNotNull(overlappingContentBounds, "overlappingContentBounds");
            Intrinsics.checkParameterIsNotNull(otherContentBounds, "otherContentBounds");
            return FloatingContentCoordinator.Companion.findAreaForContentVertically(getFloatingBoundsOnScreen(), overlappingContentBounds, otherContentBounds, getAllowedFloatingBoundsRegion());
        }
    }

    public final void onContentAdded(@NotNull FloatingContent newContent) {
        Intrinsics.checkParameterIsNotNull(newContent, "newContent");
        updateContentBounds();
        this.allContentBounds.put(newContent, newContent.getFloatingBoundsOnScreen());
        maybeMoveConflictingContent(newContent);
    }

    public final void onContentMoved(@NotNull FloatingContent content) {
        Intrinsics.checkParameterIsNotNull(content, "content");
        if (this.currentlyResolvingConflicts) {
            return;
        }
        if (!this.allContentBounds.containsKey(content)) {
            Log.wtf("FloatingCoordinator", "Received onContentMoved call before onContentAdded! This should never happen.");
        } else {
            updateContentBounds();
            maybeMoveConflictingContent(content);
        }
    }

    public final void onContentRemoved(@NotNull FloatingContent removedContent) {
        Intrinsics.checkParameterIsNotNull(removedContent, "removedContent");
        this.allContentBounds.remove(removedContent);
    }

    private final void maybeMoveConflictingContent(FloatingContent floatingContent) {
        this.currentlyResolvingConflicts = true;
        Rect rect = this.allContentBounds.get(floatingContent);
        if (rect == null) {
            Intrinsics.throwNpe();
        }
        Rect rect2 = rect;
        Map<FloatingContent, Rect> map = this.allContentBounds;
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        Iterator<Map.Entry<FloatingContent, Rect>> it = map.entrySet().iterator();
        while (true) {
            boolean z = false;
            if (!it.hasNext()) {
                break;
            }
            Map.Entry<FloatingContent, Rect> next = it.next();
            FloatingContent key = next.getKey();
            Rect value = next.getValue();
            if ((!Intrinsics.areEqual(key, floatingContent)) && Rect.intersects(rect2, value)) {
                z = true;
            }
            if (z) {
                linkedHashMap.put(next.getKey(), next.getValue());
            }
        }
        for (Map.Entry entry : linkedHashMap.entrySet()) {
            FloatingContent floatingContent2 = (FloatingContent) entry.getKey();
            Rect rectCalculateNewBoundsOnOverlap = floatingContent2.calculateNewBoundsOnOverlap(rect2, CollectionsKt___CollectionsKt.minus(CollectionsKt___CollectionsKt.minus(this.allContentBounds.values(), (Rect) entry.getValue()), rect2));
            if (!rectCalculateNewBoundsOnOverlap.isEmpty()) {
                floatingContent2.moveToBounds(rectCalculateNewBoundsOnOverlap);
                this.allContentBounds.put(floatingContent2, floatingContent2.getFloatingBoundsOnScreen());
            }
        }
        this.currentlyResolvingConflicts = false;
    }

    private final void updateContentBounds() {
        for (FloatingContent floatingContent : this.allContentBounds.keySet()) {
            this.allContentBounds.put(floatingContent, floatingContent.getFloatingBoundsOnScreen());
        }
    }

    /* compiled from: FloatingContentCoordinator.kt */
    public static final class Companion {
        static final /* synthetic */ KProperty[] $$delegatedProperties = {Reflection.property0(new PropertyReference0Impl(Reflection.getOrCreateKotlinClass(Companion.class), "newContentBoundsAbove", "<v#0>")), Reflection.property0(new PropertyReference0Impl(Reflection.getOrCreateKotlinClass(Companion.class), "newContentBoundsBelow", "<v#1>")), Reflection.property0(new PropertyReference0Impl(Reflection.getOrCreateKotlinClass(Companion.class), "positionAboveInBounds", "<v#2>")), Reflection.property0(new PropertyReference0Impl(Reflection.getOrCreateKotlinClass(Companion.class), "positionBelowInBounds", "<v#3>"))};

        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        /* JADX WARN: Type inference failed for: r12v4, types: [T, java.util.List] */
        /* JADX WARN: Type inference failed for: r12v6, types: [T, java.util.List] */
        @NotNull
        public final Rect findAreaForContentVertically(@NotNull final Rect contentRect, @NotNull final Rect newlyOverlappingRect, @NotNull Collection<Rect> exclusionRects, @NotNull final Rect allowedBounds) {
            Intrinsics.checkParameterIsNotNull(contentRect, "contentRect");
            Intrinsics.checkParameterIsNotNull(newlyOverlappingRect, "newlyOverlappingRect");
            Intrinsics.checkParameterIsNotNull(exclusionRects, "exclusionRects");
            Intrinsics.checkParameterIsNotNull(allowedBounds, "allowedBounds");
            boolean z = true;
            boolean z2 = newlyOverlappingRect.centerY() < contentRect.centerY();
            final Ref$ObjectRef ref$ObjectRef = new Ref$ObjectRef();
            final Ref$ObjectRef ref$ObjectRef2 = new Ref$ObjectRef();
            ArrayList arrayList = new ArrayList();
            for (Object obj : exclusionRects) {
                if (FloatingContentCoordinator.Companion.rectsIntersectVertically((Rect) obj, contentRect)) {
                    arrayList.add(obj);
                }
            }
            ArrayList arrayList2 = new ArrayList();
            ArrayList arrayList3 = new ArrayList();
            for (Object obj2 : arrayList) {
                if (((Rect) obj2).top < contentRect.top) {
                    arrayList2.add(obj2);
                } else {
                    arrayList3.add(obj2);
                }
            }
            Pair pair = new Pair(arrayList2, arrayList3);
            ref$ObjectRef.element = (List) pair.component1();
            ref$ObjectRef2.element = (List) pair.component2();
            final Lazy lazy = LazyKt__LazyJVMKt.lazy(new Function0<Rect>() { // from class: com.android.systemui.util.FloatingContentCoordinator$Companion$findAreaForContentVertically$newContentBoundsAbove$2
                /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
                {
                    super(0);
                }

                /* JADX WARN: Can't rename method to resolve collision */
                @Override // kotlin.jvm.functions.Function0
                @NotNull
                public final Rect invoke() {
                    return FloatingContentCoordinator.Companion.findAreaForContentAboveOrBelow(contentRect, CollectionsKt___CollectionsKt.plus((List) ref$ObjectRef.element, newlyOverlappingRect), true);
                }
            });
            KProperty[] kPropertyArr = $$delegatedProperties;
            final KProperty kProperty = kPropertyArr[0];
            final Lazy lazy2 = LazyKt__LazyJVMKt.lazy(new Function0<Rect>() { // from class: com.android.systemui.util.FloatingContentCoordinator$Companion$findAreaForContentVertically$newContentBoundsBelow$2
                /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
                {
                    super(0);
                }

                /* JADX WARN: Can't rename method to resolve collision */
                @Override // kotlin.jvm.functions.Function0
                @NotNull
                public final Rect invoke() {
                    return FloatingContentCoordinator.Companion.findAreaForContentAboveOrBelow(contentRect, CollectionsKt___CollectionsKt.plus((List) ref$ObjectRef2.element, newlyOverlappingRect), false);
                }
            });
            final KProperty kProperty2 = kPropertyArr[1];
            Lazy lazy3 = LazyKt__LazyJVMKt.lazy(new Function0<Boolean>() { // from class: com.android.systemui.util.FloatingContentCoordinator$Companion$findAreaForContentVertically$positionAboveInBounds$2
                /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
                {
                    super(0);
                }

                @Override // kotlin.jvm.functions.Function0
                public /* bridge */ /* synthetic */ Boolean invoke() {
                    return Boolean.valueOf(invoke2());
                }

                /* renamed from: invoke, reason: avoid collision after fix types in other method */
                public final boolean invoke2() {
                    return allowedBounds.contains((Rect) lazy.getValue());
                }
            });
            KProperty kProperty3 = kPropertyArr[2];
            Lazy lazy4 = LazyKt__LazyJVMKt.lazy(new Function0<Boolean>() { // from class: com.android.systemui.util.FloatingContentCoordinator$Companion$findAreaForContentVertically$positionBelowInBounds$2
                /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
                {
                    super(0);
                }

                @Override // kotlin.jvm.functions.Function0
                public /* bridge */ /* synthetic */ Boolean invoke() {
                    return Boolean.valueOf(invoke2());
                }

                /* renamed from: invoke, reason: avoid collision after fix types in other method */
                public final boolean invoke2() {
                    return allowedBounds.contains((Rect) lazy2.getValue());
                }
            });
            KProperty kProperty4 = kPropertyArr[3];
            if ((!z2 || !((Boolean) lazy4.getValue()).booleanValue()) && (z2 || ((Boolean) lazy3.getValue()).booleanValue())) {
                z = false;
            }
            Rect rect = (Rect) (z ? lazy2.getValue() : lazy.getValue());
            return allowedBounds.contains(rect) ? rect : new Rect();
        }

        private final boolean rectsIntersectVertically(Rect rect, Rect rect2) {
            int i;
            int i2 = rect.left;
            int i3 = rect2.left;
            return (i2 >= i3 && i2 <= rect2.right) || ((i = rect.right) <= rect2.right && i >= i3);
        }

        @NotNull
        public final Rect findAreaForContentAboveOrBelow(@NotNull Rect contentRect, @NotNull Collection<Rect> exclusionRects, final boolean z) {
            Intrinsics.checkParameterIsNotNull(contentRect, "contentRect");
            Intrinsics.checkParameterIsNotNull(exclusionRects, "exclusionRects");
            List<Rect> listSortedWith = CollectionsKt___CollectionsKt.sortedWith(exclusionRects, new Comparator<T>() { // from class: com.android.systemui.util.FloatingContentCoordinator$Companion$findAreaForContentAboveOrBelow$$inlined$sortedBy$1
                /* JADX WARN: Multi-variable type inference failed */
                @Override // java.util.Comparator
                public final int compare(T t, T t2) {
                    boolean z2 = z;
                    int i = ((Rect) t).top;
                    if (z2) {
                        i = -i;
                    }
                    Rect rect = (Rect) t2;
                    return ComparisonsKt__ComparisonsKt.compareValues(Integer.valueOf(i), Integer.valueOf(z ? -rect.top : rect.top));
                }
            });
            Rect rect = new Rect(contentRect);
            for (Rect rect2 : listSortedWith) {
                if (!Rect.intersects(rect, rect2)) {
                    break;
                }
                rect.offsetTo(rect.left, rect2.top + (z ? -contentRect.height() : rect2.height()));
            }
            return rect;
        }
    }
}
