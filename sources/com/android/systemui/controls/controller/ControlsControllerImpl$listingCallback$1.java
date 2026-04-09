package com.android.systemui.controls.controller;

import android.content.ComponentName;
import android.content.SharedPreferences;
import android.util.Log;
import com.android.systemui.controls.ControlsServiceInfo;
import com.android.systemui.controls.management.ControlsListingController;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsControllerImpl.kt */
/* loaded from: classes.dex */
public final class ControlsControllerImpl$listingCallback$1 implements ControlsListingController.ControlsListingCallback {
    final /* synthetic */ ControlsControllerImpl this$0;

    ControlsControllerImpl$listingCallback$1(ControlsControllerImpl controlsControllerImpl) {
        this.this$0 = controlsControllerImpl;
    }

    @Override // com.android.systemui.controls.management.ControlsListingController.ControlsListingCallback
    public void onServicesUpdated(@NotNull final List<ControlsServiceInfo> serviceInfos) {
        Intrinsics.checkParameterIsNotNull(serviceInfos, "serviceInfos");
        this.this$0.executor.execute(new Runnable() { // from class: com.android.systemui.controls.controller.ControlsControllerImpl$listingCallback$1$onServicesUpdated$1
            @Override // java.lang.Runnable
            public final void run() {
                List list = serviceInfos;
                ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list, 10));
                Iterator it = list.iterator();
                while (it.hasNext()) {
                    arrayList.add(((ControlsServiceInfo) it.next()).componentName);
                }
                Set<ComponentName> set = CollectionsKt___CollectionsKt.toSet(arrayList);
                List<StructureInfo> allStructures = Favorites.INSTANCE.getAllStructures();
                ArrayList arrayList2 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(allStructures, 10));
                Iterator<T> it2 = allStructures.iterator();
                while (it2.hasNext()) {
                    arrayList2.add(((StructureInfo) it2.next()).getComponentName());
                }
                Set set2 = CollectionsKt___CollectionsKt.toSet(arrayList2);
                boolean z = false;
                SharedPreferences sharedPreferences = this.this$0.this$0.userStructure.getUserContext().getSharedPreferences("controls_prefs", 0);
                Set<String> completedSeedingPackageSet = sharedPreferences.getStringSet("SeedingCompleted", new LinkedHashSet());
                ArrayList arrayList3 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(set, 10));
                for (ComponentName it3 : set) {
                    Intrinsics.checkExpressionValueIsNotNull(it3, "it");
                    arrayList3.add(it3.getPackageName());
                }
                SharedPreferences.Editor editorEdit = sharedPreferences.edit();
                Intrinsics.checkExpressionValueIsNotNull(completedSeedingPackageSet, "completedSeedingPackageSet");
                editorEdit.putStringSet("SeedingCompleted", CollectionsKt___CollectionsKt.intersect(completedSeedingPackageSet, arrayList3)).apply();
                for (ComponentName it4 : CollectionsKt___CollectionsKt.subtract(set2, set)) {
                    Favorites favorites = Favorites.INSTANCE;
                    Intrinsics.checkExpressionValueIsNotNull(it4, "it");
                    favorites.removeStructures(it4);
                    this.this$0.this$0.bindingController.onComponentRemoved(it4);
                    z = true;
                }
                if (!this.this$0.this$0.getAuxiliaryPersistenceWrapper$frameworks__base__packages__SystemUI__android_common__SystemUI_core().getFavorites().isEmpty()) {
                    for (ComponentName it5 : CollectionsKt___CollectionsKt.subtract(set, set2)) {
                        AuxiliaryPersistenceWrapper auxiliaryPersistenceWrapper$frameworks__base__packages__SystemUI__android_common__SystemUI_core = this.this$0.this$0.getAuxiliaryPersistenceWrapper$frameworks__base__packages__SystemUI__android_common__SystemUI_core();
                        Intrinsics.checkExpressionValueIsNotNull(it5, "it");
                        List<StructureInfo> cachedFavoritesAndRemoveFor = auxiliaryPersistenceWrapper$frameworks__base__packages__SystemUI__android_common__SystemUI_core.getCachedFavoritesAndRemoveFor(it5);
                        if (!cachedFavoritesAndRemoveFor.isEmpty()) {
                            Iterator<T> it6 = cachedFavoritesAndRemoveFor.iterator();
                            while (it6.hasNext()) {
                                Favorites.INSTANCE.replaceControls((StructureInfo) it6.next());
                            }
                            z = true;
                        }
                    }
                    for (ComponentName it7 : CollectionsKt___CollectionsKt.intersect(set, set2)) {
                        AuxiliaryPersistenceWrapper auxiliaryPersistenceWrapper$frameworks__base__packages__SystemUI__android_common__SystemUI_core2 = this.this$0.this$0.getAuxiliaryPersistenceWrapper$frameworks__base__packages__SystemUI__android_common__SystemUI_core();
                        Intrinsics.checkExpressionValueIsNotNull(it7, "it");
                        auxiliaryPersistenceWrapper$frameworks__base__packages__SystemUI__android_common__SystemUI_core2.getCachedFavoritesAndRemoveFor(it7);
                    }
                }
                if (z) {
                    Log.d("ControlsControllerImpl", "Detected change in available services, storing updated favorites");
                    this.this$0.this$0.persistenceWrapper.storeFavorites(Favorites.INSTANCE.getAllStructures());
                }
            }
        });
    }
}
