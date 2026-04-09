package com.android.systemui.controls.management;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.res.Resources;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.controls.ControlStatus;
import com.android.systemui.controls.TooltipManager;
import com.android.systemui.controls.controller.ControlsController;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import kotlin.collections.CollectionsKt__CollectionsJVMKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlsFavoritingActivity.kt */
/* loaded from: classes.dex */
final class ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1<T> implements Consumer<ControlsController.LoadData> {
    final /* synthetic */ CharSequence $emptyZoneString;
    final /* synthetic */ ControlsFavoritingActivity this$0;

    ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1(CharSequence charSequence, ControlsFavoritingActivity controlsFavoritingActivity) {
        this.$emptyZoneString = charSequence;
        this.this$0 = controlsFavoritingActivity;
    }

    @Override // java.util.function.Consumer
    public final void accept(@NotNull ControlsController.LoadData data) {
        Intrinsics.checkParameterIsNotNull(data, "data");
        List<ControlStatus> allControls = data.getAllControls();
        List<String> favoritesIds = data.getFavoritesIds();
        final boolean errorOnLoad = data.getErrorOnLoad();
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        for (T t : allControls) {
            CharSequence structure = ((ControlStatus) t).getControl().getStructure();
            if (structure == null) {
                structure = "";
            }
            Object arrayList = linkedHashMap.get(structure);
            if (arrayList == null) {
                arrayList = new ArrayList();
                linkedHashMap.put(structure, arrayList);
            }
            ((List) arrayList).add(t);
        }
        ControlsFavoritingActivity controlsFavoritingActivity = this.this$0;
        ArrayList arrayList2 = new ArrayList(linkedHashMap.size());
        for (Map.Entry entry : linkedHashMap.entrySet()) {
            CharSequence charSequence = (CharSequence) entry.getKey();
            List list = (List) entry.getValue();
            CharSequence emptyZoneString = this.$emptyZoneString;
            Intrinsics.checkExpressionValueIsNotNull(emptyZoneString, "emptyZoneString");
            arrayList2.add(new StructureContainer(charSequence, new AllModel(list, favoritesIds, emptyZoneString, this.this$0.controlsModelCallback)));
        }
        controlsFavoritingActivity.listOfStructures = CollectionsKt___CollectionsKt.sortedWith(arrayList2, ControlsFavoritingActivity.access$getComparator$p(this.this$0));
        Iterator it = this.this$0.listOfStructures.iterator();
        final int i = 0;
        while (true) {
            if (!it.hasNext()) {
                i = -1;
                break;
            } else if (Intrinsics.areEqual(((StructureContainer) it.next()).getStructureName(), this.this$0.structureExtra)) {
                break;
            } else {
                i++;
            }
        }
        if (i == -1) {
            i = 0;
        }
        if (this.this$0.getIntent().getBooleanExtra("extra_single_structure", false)) {
            ControlsFavoritingActivity controlsFavoritingActivity2 = this.this$0;
            controlsFavoritingActivity2.listOfStructures = CollectionsKt__CollectionsJVMKt.listOf(controlsFavoritingActivity2.listOfStructures.get(i));
        }
        this.this$0.executor.execute(new Runnable() { // from class: com.android.systemui.controls.management.ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.1
            @Override // java.lang.Runnable
            public final void run() {
                ControlsFavoritingActivity.access$getStructurePager$p(ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.this.this$0).setAdapter(new StructureAdapter(ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.this.this$0.listOfStructures, getCurrentUserId()));
                ControlsFavoritingActivity.access$getStructurePager$p(ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.this.this$0).setCurrentItem(i);
                if (!errorOnLoad) {
                    if (ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.this.this$0.listOfStructures.isEmpty()) {
                        ControlsFavoritingActivity.access$getStatusText$p(ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.this.this$0).setText(ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.this.this$0.getResources().getString(R.string.controls_favorite_load_none));
                        ControlsFavoritingActivity.access$getSubtitleView$p(ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.this.this$0).setVisibility(8);
                        return;
                    }
                    ControlsFavoritingActivity.access$getStatusText$p(ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.this.this$0).setVisibility(8);
                    ControlsFavoritingActivity.access$getPageIndicator$p(ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.this.this$0).setNumPages(ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.this.this$0.listOfStructures.size());
                    ControlsFavoritingActivity.access$getPageIndicator$p(ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.this.this$0).setLocation(0.0f);
                    ControlsFavoritingActivity.access$getPageIndicator$p(ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.this.this$0).setVisibility(ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.this.this$0.listOfStructures.size() <= 1 ? 4 : 0);
                    ControlsAnimations controlsAnimations = ControlsAnimations.INSTANCE;
                    Animator animatorEnterAnimation = controlsAnimations.enterAnimation(ControlsFavoritingActivity.access$getPageIndicator$p(ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.this.this$0));
                    animatorEnterAnimation.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.controls.management.ControlsFavoritingActivity$loadControls$.inlined.let.lambda.1.1.1
                        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                        public void onAnimationEnd(@Nullable Animator animator) {
                            if (ControlsFavoritingActivity.access$getPageIndicator$p(ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.this.this$0).getVisibility() != 0 || ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.this.this$0.mTooltipManager == null) {
                                return;
                            }
                            int[] iArr = new int[2];
                            ControlsFavoritingActivity.access$getPageIndicator$p(ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.this.this$0).getLocationOnScreen(iArr);
                            int width = iArr[0] + (ControlsFavoritingActivity.access$getPageIndicator$p(ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.this.this$0).getWidth() / 2);
                            int height = iArr[1] + ControlsFavoritingActivity.access$getPageIndicator$p(ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.this.this$0).getHeight();
                            TooltipManager tooltipManager = ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.this.this$0.mTooltipManager;
                            if (tooltipManager != null) {
                                tooltipManager.show(R.string.controls_structure_tooltip, width, height);
                            }
                        }
                    });
                    animatorEnterAnimation.start();
                    controlsAnimations.enterAnimation(ControlsFavoritingActivity.access$getStructurePager$p(ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.this.this$0)).start();
                    return;
                }
                TextView textViewAccess$getStatusText$p = ControlsFavoritingActivity.access$getStatusText$p(ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.this.this$0);
                Resources resources = ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.this.this$0.getResources();
                int i2 = R.string.controls_favorite_load_error;
                Object[] objArr = new Object[1];
                Object obj = ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.this.this$0.appName;
                if (obj == null) {
                    obj = "";
                }
                objArr[0] = obj;
                textViewAccess$getStatusText$p.setText(resources.getString(i2, objArr));
                ControlsFavoritingActivity.access$getSubtitleView$p(ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.this.this$0).setVisibility(8);
            }
        });
    }
}
