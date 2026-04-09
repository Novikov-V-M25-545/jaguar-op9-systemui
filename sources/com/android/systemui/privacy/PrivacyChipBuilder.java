package com.android.systemui.privacy;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.android.systemui.R;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import kotlin.Pair;
import kotlin.collections.CollectionsKt;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.collections.MapsKt___MapsKt;
import kotlin.comparisons.ComparisonsKt__ComparisonsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: PrivacyChipBuilder.kt */
/* loaded from: classes.dex */
public final class PrivacyChipBuilder {

    @NotNull
    private final List<Pair<PrivacyApplication, List<PrivacyType>>> appsAndTypes;
    private final Context context;
    private final String lastSeparator;
    private final String separator;

    @NotNull
    private final List<PrivacyType> types;

    public PrivacyChipBuilder(@NotNull Context context, @NotNull List<PrivacyItem> itemsList) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(itemsList, "itemsList");
        this.context = context;
        this.separator = context.getString(R.string.ongoing_privacy_dialog_separator);
        this.lastSeparator = context.getString(R.string.ongoing_privacy_dialog_last_separator);
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        for (PrivacyItem privacyItem : itemsList) {
            PrivacyApplication application = privacyItem.getApplication();
            Object arrayList = linkedHashMap.get(application);
            if (arrayList == null) {
                arrayList = new ArrayList();
                linkedHashMap.put(application, arrayList);
            }
            ((List) arrayList).add(privacyItem.getPrivacyType());
        }
        this.appsAndTypes = CollectionsKt___CollectionsKt.sortedWith(MapsKt___MapsKt.toList(linkedHashMap), ComparisonsKt__ComparisonsKt.compareBy(new Function1<Pair<? extends PrivacyApplication, ? extends List<? extends PrivacyType>>, Integer>() { // from class: com.android.systemui.privacy.PrivacyChipBuilder.3
            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Integer invoke(Pair<? extends PrivacyApplication, ? extends List<? extends PrivacyType>> pair) {
                return Integer.valueOf(invoke2((Pair<PrivacyApplication, ? extends List<? extends PrivacyType>>) pair));
            }

            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final int invoke2(@NotNull Pair<PrivacyApplication, ? extends List<? extends PrivacyType>> it) {
                Intrinsics.checkParameterIsNotNull(it, "it");
                return -it.getSecond().size();
            }
        }, new Function1<Pair<? extends PrivacyApplication, ? extends List<? extends PrivacyType>>, PrivacyType>() { // from class: com.android.systemui.privacy.PrivacyChipBuilder.4
            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ PrivacyType invoke(Pair<? extends PrivacyApplication, ? extends List<? extends PrivacyType>> pair) {
                return invoke2((Pair<PrivacyApplication, ? extends List<? extends PrivacyType>>) pair);
            }

            @Nullable
            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final PrivacyType invoke2(@NotNull Pair<PrivacyApplication, ? extends List<? extends PrivacyType>> it) {
                Intrinsics.checkParameterIsNotNull(it, "it");
                return (PrivacyType) CollectionsKt.min(it.getSecond());
            }
        }));
        ArrayList arrayList2 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(itemsList, 10));
        Iterator<T> it = itemsList.iterator();
        while (it.hasNext()) {
            arrayList2.add(((PrivacyItem) it.next()).getPrivacyType());
        }
        this.types = CollectionsKt___CollectionsKt.sorted(CollectionsKt___CollectionsKt.distinct(arrayList2));
    }

    @NotNull
    public final List<Pair<PrivacyApplication, List<PrivacyType>>> getAppsAndTypes() {
        return this.appsAndTypes;
    }

    @NotNull
    public final List<Drawable> generateIcons() {
        List<PrivacyType> list = this.types;
        ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list, 10));
        Iterator<T> it = list.iterator();
        while (it.hasNext()) {
            arrayList.add(((PrivacyType) it.next()).getIcon(this.context));
        }
        return arrayList;
    }

    private final <T> StringBuilder joinWithAnd(@NotNull List<? extends T> list) {
        List<? extends T> listSubList = list.subList(0, list.size() - 1);
        StringBuilder sb = new StringBuilder();
        String separator = this.separator;
        Intrinsics.checkExpressionValueIsNotNull(separator, "separator");
        StringBuilder sb2 = (StringBuilder) CollectionsKt___CollectionsKt.joinTo(listSubList, sb, (124 & 2) != 0 ? ", " : separator, (124 & 4) != 0 ? "" : null, (124 & 8) == 0 ? null : "", (124 & 16) != 0 ? -1 : 0, (124 & 32) != 0 ? "..." : null, (124 & 64) != 0 ? null : null);
        sb2.append(this.lastSeparator);
        sb2.append(CollectionsKt.last((List) list));
        return sb2;
    }

    @NotNull
    public final String joinTypes() {
        int size = this.types.size();
        if (size == 0) {
            return "";
        }
        if (size == 1) {
            String name = this.types.get(0).getName(this.context);
            Intrinsics.checkExpressionValueIsNotNull(name, "types[0].getName(context)");
            return name;
        }
        List<PrivacyType> list = this.types;
        ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list, 10));
        Iterator<T> it = list.iterator();
        while (it.hasNext()) {
            arrayList.add(((PrivacyType) it.next()).getName(this.context));
        }
        String string = joinWithAnd(arrayList).toString();
        Intrinsics.checkExpressionValueIsNotNull(string, "types.map { it.getName(c….joinWithAnd().toString()");
        return string;
    }
}
