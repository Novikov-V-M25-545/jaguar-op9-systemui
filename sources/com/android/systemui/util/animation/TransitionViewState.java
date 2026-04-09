package com.android.systemui.util.animation;

import android.graphics.PointF;
import android.view.View;
import java.util.LinkedHashMap;
import java.util.Map;
import kotlin.jvm.internal.Intrinsics;
import lineageos.hardware.LineageHardwareManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: TransitionLayout.kt */
/* loaded from: classes.dex */
public final class TransitionViewState {
    private int height;
    private int width;

    @NotNull
    private Map<Integer, WidgetState> widgetStates = new LinkedHashMap();
    private float alpha = 1.0f;

    @NotNull
    private final PointF translation = new PointF();

    @NotNull
    private final PointF contentTranslation = new PointF();

    @NotNull
    public final Map<Integer, WidgetState> getWidgetStates() {
        return this.widgetStates;
    }

    public final int getWidth() {
        return this.width;
    }

    public final void setWidth(int i) {
        this.width = i;
    }

    public final int getHeight() {
        return this.height;
    }

    public final void setHeight(int i) {
        this.height = i;
    }

    public final float getAlpha() {
        return this.alpha;
    }

    public final void setAlpha(float f) {
        this.alpha = f;
    }

    @NotNull
    public final PointF getTranslation() {
        return this.translation;
    }

    @NotNull
    public final PointF getContentTranslation() {
        return this.contentTranslation;
    }

    public static /* synthetic */ TransitionViewState copy$default(TransitionViewState transitionViewState, TransitionViewState transitionViewState2, int i, Object obj) {
        if ((i & 1) != 0) {
            transitionViewState2 = null;
        }
        return transitionViewState.copy(transitionViewState2);
    }

    @NotNull
    public final TransitionViewState copy(@Nullable TransitionViewState transitionViewState) {
        TransitionViewState transitionViewState2 = transitionViewState != null ? transitionViewState : new TransitionViewState();
        transitionViewState2.width = this.width;
        transitionViewState2.height = this.height;
        transitionViewState2.alpha = this.alpha;
        PointF pointF = transitionViewState2.translation;
        PointF pointF2 = this.translation;
        pointF.set(pointF2.x, pointF2.y);
        PointF pointF3 = transitionViewState2.contentTranslation;
        PointF pointF4 = this.contentTranslation;
        pointF3.set(pointF4.x, pointF4.y);
        for (Map.Entry<Integer, WidgetState> entry : this.widgetStates.entrySet()) {
            Map<Integer, WidgetState> map = transitionViewState2.widgetStates;
            Integer key = entry.getKey();
            WidgetState value = entry.getValue();
            map.put(key, value.copy((511 & 1) != 0 ? value.x : 0.0f, (511 & 2) != 0 ? value.y : 0.0f, (511 & 4) != 0 ? value.width : 0, (511 & 8) != 0 ? value.height : 0, (511 & 16) != 0 ? value.measureWidth : 0, (511 & 32) != 0 ? value.measureHeight : 0, (511 & 64) != 0 ? value.alpha : 0.0f, (511 & 128) != 0 ? value.scale : 0.0f, (511 & LineageHardwareManager.FEATURE_SUNLIGHT_ENHANCEMENT) != 0 ? value.gone : false));
        }
        return transitionViewState2;
    }

    public final void initFromLayout(@NotNull TransitionLayout transitionLayout) {
        Intrinsics.checkParameterIsNotNull(transitionLayout, "transitionLayout");
        int childCount = transitionLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = transitionLayout.getChildAt(i);
            Map<Integer, WidgetState> map = this.widgetStates;
            Intrinsics.checkExpressionValueIsNotNull(child, "child");
            Integer numValueOf = Integer.valueOf(child.getId());
            WidgetState widgetState = map.get(numValueOf);
            if (widgetState == null) {
                widgetState = new WidgetState(0.0f, 0.0f, 0, 0, 0, 0, 0.0f, 0.0f, false, 384, null);
                map.put(numValueOf, widgetState);
            }
            widgetState.initFromLayout(child);
        }
        this.width = transitionLayout.getMeasuredWidth();
        this.height = transitionLayout.getMeasuredHeight();
        this.translation.set(0.0f, 0.0f);
        this.contentTranslation.set(0.0f, 0.0f);
        this.alpha = 1.0f;
    }
}
