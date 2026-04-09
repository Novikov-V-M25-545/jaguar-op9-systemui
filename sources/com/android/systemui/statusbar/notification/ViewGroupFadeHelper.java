package com.android.systemui.statusbar.notification;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import java.util.LinkedHashSet;
import java.util.Set;
import kotlin.TypeCastException;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.TypeIntrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ViewGroupFadeHelper.kt */
/* loaded from: classes.dex */
public final class ViewGroupFadeHelper {
    public static final Companion Companion = new Companion(null);
    private static final Function1<View, Boolean> visibilityIncluder = new Function1<View, Boolean>() { // from class: com.android.systemui.statusbar.notification.ViewGroupFadeHelper$Companion$visibilityIncluder$1
        @Override // kotlin.jvm.functions.Function1
        public /* bridge */ /* synthetic */ Boolean invoke(View view) {
            return Boolean.valueOf(invoke2(view));
        }

        /* renamed from: invoke, reason: avoid collision after fix types in other method */
        public final boolean invoke2(@NotNull View view) {
            Intrinsics.checkParameterIsNotNull(view, "view");
            return view.getVisibility() == 0;
        }
    };

    public static final void fadeOutAllChildrenExcept(@NotNull ViewGroup viewGroup, @NotNull View view, long j, @Nullable Runnable runnable) {
        Companion.fadeOutAllChildrenExcept(viewGroup, view, j, runnable);
    }

    public static final void reset(@NotNull ViewGroup viewGroup) {
        Companion.reset(viewGroup);
    }

    /* compiled from: ViewGroupFadeHelper.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public final void fadeOutAllChildrenExcept(@NotNull final ViewGroup root, @NotNull View excludedView, final long j, @Nullable final Runnable runnable) {
            Intrinsics.checkParameterIsNotNull(root, "root");
            Intrinsics.checkParameterIsNotNull(excludedView, "excludedView");
            final Set<View> setGatherViews = gatherViews(root, excludedView, ViewGroupFadeHelper.visibilityIncluder);
            for (View view : setGatherViews) {
                if (view.getHasOverlappingRendering() && view.getLayerType() == 0) {
                    view.setLayerType(2, null);
                    view.setTag(R.id.view_group_fade_helper_hardware_layer, Boolean.TRUE);
                }
            }
            ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(1.0f, 0.0f);
            Intrinsics.checkExpressionValueIsNotNull(valueAnimatorOfFloat, "this");
            valueAnimatorOfFloat.setDuration(j);
            valueAnimatorOfFloat.setInterpolator(Interpolators.ALPHA_OUT);
            valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.notification.ViewGroupFadeHelper$Companion$fadeOutAllChildrenExcept$$inlined$apply$lambda$1
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator animation) {
                    Float f = (Float) root.getTag(R.id.view_group_fade_helper_previous_value_tag);
                    Intrinsics.checkExpressionValueIsNotNull(animation, "animation");
                    Object animatedValue = animation.getAnimatedValue();
                    if (animatedValue == null) {
                        throw new TypeCastException("null cannot be cast to non-null type kotlin.Float");
                    }
                    float fFloatValue = ((Float) animatedValue).floatValue();
                    for (View view2 : setGatherViews) {
                        if (!Intrinsics.areEqual(view2.getAlpha(), f)) {
                            view2.setTag(R.id.view_group_fade_helper_restore_tag, Float.valueOf(view2.getAlpha()));
                        }
                        view2.setAlpha(fFloatValue);
                    }
                    root.setTag(R.id.view_group_fade_helper_previous_value_tag, Float.valueOf(fFloatValue));
                }
            });
            valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.notification.ViewGroupFadeHelper$Companion$fadeOutAllChildrenExcept$$inlined$apply$lambda$2
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(@Nullable Animator animator) {
                    Runnable runnable2 = runnable;
                    if (runnable2 != null) {
                        runnable2.run();
                    }
                }
            });
            valueAnimatorOfFloat.start();
            root.setTag(R.id.view_group_fade_helper_modified_views, setGatherViews);
            root.setTag(R.id.view_group_fade_helper_animator, valueAnimatorOfFloat);
        }

        private final Set<View> gatherViews(ViewGroup viewGroup, View view, Function1<? super View, Boolean> function1) {
            LinkedHashSet linkedHashSet = new LinkedHashSet();
            ViewParent parent = view.getParent();
            View view2 = view;
            while (true) {
                ViewGroup viewGroup2 = (ViewGroup) parent;
                View view3 = view2;
                ViewGroup viewGroup3 = viewGroup2;
                if (viewGroup3 == null) {
                    break;
                }
                int childCount = viewGroup3.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = viewGroup3.getChildAt(i);
                    Intrinsics.checkExpressionValueIsNotNull(child, "child");
                    if (function1.invoke(child).booleanValue() && (!Intrinsics.areEqual(view3, child))) {
                        linkedHashSet.add(child);
                    }
                }
                if (Intrinsics.areEqual(viewGroup3, viewGroup)) {
                    break;
                }
                parent = viewGroup3.getParent();
                view2 = viewGroup3;
            }
            return linkedHashSet;
        }

        public final void reset(@NotNull ViewGroup root) {
            Intrinsics.checkParameterIsNotNull(root, "root");
            Set<View> setAsMutableSet = TypeIntrinsics.asMutableSet(root.getTag(R.id.view_group_fade_helper_modified_views));
            Animator animator = (Animator) root.getTag(R.id.view_group_fade_helper_animator);
            if (setAsMutableSet == null || animator == null) {
                return;
            }
            animator.cancel();
            Float f = (Float) root.getTag(R.id.view_group_fade_helper_previous_value_tag);
            for (View view : setAsMutableSet) {
                int i = R.id.view_group_fade_helper_restore_tag;
                Float f2 = (Float) view.getTag(i);
                if (f2 != null) {
                    if (Intrinsics.areEqual(f, view.getAlpha())) {
                        view.setAlpha(f2.floatValue());
                    }
                    int i2 = R.id.view_group_fade_helper_hardware_layer;
                    if (Intrinsics.areEqual((Boolean) view.getTag(i2), Boolean.TRUE)) {
                        view.setLayerType(0, null);
                        view.setTag(i2, null);
                    }
                    view.setTag(i, null);
                }
            }
            root.setTag(R.id.view_group_fade_helper_modified_views, null);
            root.setTag(R.id.view_group_fade_helper_previous_value_tag, null);
            root.setTag(R.id.view_group_fade_helper_animator, null);
        }
    }
}
