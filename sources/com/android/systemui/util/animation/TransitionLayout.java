package com.android.systemui.util.animation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import com.android.systemui.statusbar.CrossFadeHelper;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: TransitionLayout.kt */
/* loaded from: classes.dex */
public final class TransitionLayout extends ConstraintLayout {
    private final Rect boundsRect;
    private TransitionViewState currentState;
    private int desiredMeasureHeight;
    private int desiredMeasureWidth;
    private boolean measureAsConstraint;

    @NotNull
    private TransitionViewState measureState;
    private final Set<Integer> originalGoneChildrenSet;
    private final Map<Integer, Float> originalViewAlphas;
    private final TransitionLayout$preDrawApplicator$1 preDrawApplicator;
    private boolean updateScheduled;

    public TransitionLayout(@NotNull Context context) {
        this(context, null, 0, 6, null);
    }

    public TransitionLayout(@NotNull Context context, @Nullable AttributeSet attributeSet) {
        this(context, attributeSet, 0, 4, null);
    }

    public /* synthetic */ TransitionLayout(Context context, AttributeSet attributeSet, int i, int i2, DefaultConstructorMarker defaultConstructorMarker) {
        this(context, (i2 & 2) != 0 ? null : attributeSet, (i2 & 4) != 0 ? 0 : i);
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    /* JADX WARN: Type inference failed for: r2v6, types: [com.android.systemui.util.animation.TransitionLayout$preDrawApplicator$1] */
    public TransitionLayout(@NotNull Context context, @Nullable AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        Intrinsics.checkParameterIsNotNull(context, "context");
        this.boundsRect = new Rect();
        this.originalGoneChildrenSet = new LinkedHashSet();
        this.originalViewAlphas = new LinkedHashMap();
        this.currentState = new TransitionViewState();
        this.measureState = new TransitionViewState();
        this.preDrawApplicator = new ViewTreeObserver.OnPreDrawListener() { // from class: com.android.systemui.util.animation.TransitionLayout$preDrawApplicator$1
            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                this.this$0.updateScheduled = false;
                this.this$0.getViewTreeObserver().removeOnPreDrawListener(this);
                this.this$0.applyCurrentState();
                return true;
            }
        };
    }

    public final void setMeasureState(@NotNull TransitionViewState value) {
        Intrinsics.checkParameterIsNotNull(value, "value");
        int width = value.getWidth();
        int height = value.getHeight();
        if (width == this.desiredMeasureWidth && height == this.desiredMeasureHeight) {
            return;
        }
        this.desiredMeasureWidth = width;
        this.desiredMeasureHeight = height;
        if (isInLayout()) {
            forceLayout();
        } else {
            requestLayout();
        }
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            Intrinsics.checkExpressionValueIsNotNull(child, "child");
            if (child.getId() == -1) {
                child.setId(i);
            }
            if (child.getVisibility() == 8) {
                this.originalGoneChildrenSet.add(Integer.valueOf(child.getId()));
            }
            this.originalViewAlphas.put(Integer.valueOf(child.getId()), Float.valueOf(child.getAlpha()));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void applyCurrentState() {
        Integer numValueOf;
        int childCount = getChildCount();
        int i = (int) this.currentState.getContentTranslation().x;
        int i2 = (int) this.currentState.getContentTranslation().y;
        for (int i3 = 0; i3 < childCount; i3++) {
            View child = getChildAt(i3);
            Map<Integer, WidgetState> widgetStates = this.currentState.getWidgetStates();
            Intrinsics.checkExpressionValueIsNotNull(child, "child");
            WidgetState widgetState = widgetStates.get(Integer.valueOf(child.getId()));
            if (widgetState != null) {
                if (!(child instanceof TextView) || widgetState.getWidth() >= widgetState.getMeasureWidth()) {
                    numValueOf = null;
                } else {
                    numValueOf = Integer.valueOf(((TextView) child).getLayout().getParagraphDirection(0) == -1 ? widgetState.getMeasureWidth() - widgetState.getWidth() : 0);
                }
                if (child.getMeasuredWidth() != widgetState.getMeasureWidth() || child.getMeasuredHeight() != widgetState.getMeasureHeight()) {
                    child.measure(View.MeasureSpec.makeMeasureSpec(widgetState.getMeasureWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(widgetState.getMeasureHeight(), 1073741824));
                    child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
                }
                int iIntValue = numValueOf != null ? numValueOf.intValue() : 0;
                int x = (((int) widgetState.getX()) + i) - iIntValue;
                int y = ((int) widgetState.getY()) + i2;
                boolean z = numValueOf != null;
                child.setLeftTopRightBottom(x, y, (z ? widgetState.getMeasureWidth() : widgetState.getWidth()) + x, (z ? widgetState.getMeasureHeight() : widgetState.getHeight()) + y);
                child.setScaleX(widgetState.getScale());
                child.setScaleY(widgetState.getScale());
                Rect clipBounds = child.getClipBounds();
                if (clipBounds == null) {
                    clipBounds = new Rect();
                }
                clipBounds.set(iIntValue, 0, widgetState.getWidth() + iIntValue, widgetState.getHeight());
                child.setClipBounds(clipBounds);
                CrossFadeHelper.fadeIn(child, widgetState.getAlpha());
                child.setVisibility((widgetState.getGone() || widgetState.getAlpha() == 0.0f) ? 4 : 0);
            }
        }
        updateBounds();
        setTranslationX(this.currentState.getTranslation().x);
        setTranslationY(this.currentState.getTranslation().y);
        CrossFadeHelper.fadeIn(this, this.currentState.getAlpha());
    }

    private final void applyCurrentStateOnPredraw() {
        if (this.updateScheduled) {
            return;
        }
        this.updateScheduled = true;
        getViewTreeObserver().addOnPreDrawListener(this.preDrawApplicator);
    }

    @Override // androidx.constraintlayout.widget.ConstraintLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        if (this.measureAsConstraint) {
            super.onMeasure(i, i2);
            return;
        }
        int childCount = getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            View child = getChildAt(i3);
            Map<Integer, WidgetState> widgetStates = this.currentState.getWidgetStates();
            Intrinsics.checkExpressionValueIsNotNull(child, "child");
            WidgetState widgetState = widgetStates.get(Integer.valueOf(child.getId()));
            if (widgetState != null) {
                child.measure(View.MeasureSpec.makeMeasureSpec(widgetState.getMeasureWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(widgetState.getMeasureHeight(), 1073741824));
            }
        }
        setMeasuredDimension(this.desiredMeasureWidth, this.desiredMeasureHeight);
    }

    @Override // androidx.constraintlayout.widget.ConstraintLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        if (this.measureAsConstraint) {
            super.onLayout(z, getLeft(), getTop(), getRight(), getBottom());
            return;
        }
        int childCount = getChildCount();
        for (int i5 = 0; i5 < childCount; i5++) {
            View child = getChildAt(i5);
            Intrinsics.checkExpressionValueIsNotNull(child, "child");
            child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
        }
        applyCurrentState();
    }

    @Override // androidx.constraintlayout.widget.ConstraintLayout, android.view.ViewGroup, android.view.View
    protected void dispatchDraw(@Nullable Canvas canvas) {
        if (canvas != null) {
            canvas.save();
        }
        if (canvas != null) {
            canvas.clipRect(this.boundsRect);
        }
        super.dispatchDraw(canvas);
        if (canvas != null) {
            canvas.restore();
        }
    }

    private final void updateBounds() {
        int left = getLeft();
        int top = getTop();
        setLeftTopRightBottom(left, top, this.currentState.getWidth() + left, this.currentState.getHeight() + top);
        this.boundsRect.set(0, 0, getWidth(), getHeight());
    }

    @NotNull
    public final TransitionViewState calculateViewState(@NotNull MeasurementInput input, @NotNull ConstraintSet constraintSet, @Nullable TransitionViewState transitionViewState) {
        Intrinsics.checkParameterIsNotNull(input, "input");
        Intrinsics.checkParameterIsNotNull(constraintSet, "constraintSet");
        if (transitionViewState == null) {
            transitionViewState = new TransitionViewState();
        }
        applySetToFullLayout(constraintSet);
        int measuredHeight = getMeasuredHeight();
        int measuredWidth = getMeasuredWidth();
        this.measureAsConstraint = true;
        measure(input.getWidthMeasureSpec(), input.getHeightMeasureSpec());
        int left = getLeft();
        int top = getTop();
        layout(left, top, getMeasuredWidth() + left, getMeasuredHeight() + top);
        this.measureAsConstraint = false;
        transitionViewState.initFromLayout(this);
        ensureViewsNotGone();
        setMeasuredDimension(measuredWidth, measuredHeight);
        applyCurrentStateOnPredraw();
        return transitionViewState;
    }

    private final void applySetToFullLayout(ConstraintSet constraintSet) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            Set<Integer> set = this.originalGoneChildrenSet;
            Intrinsics.checkExpressionValueIsNotNull(child, "child");
            if (set.contains(Integer.valueOf(child.getId()))) {
                child.setVisibility(8);
            }
            Float f = this.originalViewAlphas.get(Integer.valueOf(child.getId()));
            child.setAlpha(f != null ? f.floatValue() : 1.0f);
        }
        constraintSet.applyTo(this);
    }

    private final void ensureViewsNotGone() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            Map<Integer, WidgetState> widgetStates = this.currentState.getWidgetStates();
            Intrinsics.checkExpressionValueIsNotNull(child, "child");
            WidgetState widgetState = widgetStates.get(Integer.valueOf(child.getId()));
            child.setVisibility((widgetState == null || widgetState.getGone()) ? 4 : 0);
        }
    }

    public final void setState(@NotNull TransitionViewState state) {
        Intrinsics.checkParameterIsNotNull(state, "state");
        this.currentState = state;
        applyCurrentState();
    }
}
