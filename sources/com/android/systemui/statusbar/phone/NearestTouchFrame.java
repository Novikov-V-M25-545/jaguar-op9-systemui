package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/* loaded from: classes.dex */
public class NearestTouchFrame extends FrameLayout {
    private final ArrayList<View> mClickableChildren;
    private final boolean mIsActive;
    private final int[] mOffset;
    private final int[] mTmpInt;
    private View mTouchingChild;

    public NearestTouchFrame(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, context.getResources().getConfiguration());
    }

    NearestTouchFrame(Context context, AttributeSet attributeSet, Configuration configuration) {
        super(context, attributeSet);
        this.mClickableChildren = new ArrayList<>();
        this.mTmpInt = new int[2];
        this.mOffset = new int[2];
        this.mIsActive = configuration.smallestScreenWidthDp < 600;
    }

    @Override // android.widget.FrameLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        this.mClickableChildren.clear();
        addClickableChildren(this);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        getLocationInWindow(this.mOffset);
    }

    private void addClickableChildren(ViewGroup viewGroup) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt.isClickable()) {
                this.mClickableChildren.add(childAt);
            } else if (childAt instanceof ViewGroup) {
                addClickableChildren((ViewGroup) childAt);
            }
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mIsActive) {
            if (motionEvent.getAction() == 0) {
                this.mTouchingChild = findNearestChild(motionEvent);
            }
            if (this.mTouchingChild != null) {
                motionEvent.offsetLocation((r0.getWidth() / 2) - motionEvent.getX(), (this.mTouchingChild.getHeight() / 2) - motionEvent.getY());
                return this.mTouchingChild.getVisibility() == 0 && this.mTouchingChild.dispatchTouchEvent(motionEvent);
            }
        }
        return super.onTouchEvent(motionEvent);
    }

    private View findNearestChild(final MotionEvent motionEvent) {
        if (this.mClickableChildren.isEmpty()) {
            return null;
        }
        return (View) this.mClickableChildren.stream().filter(new Predicate() { // from class: com.android.systemui.statusbar.phone.NearestTouchFrame$$ExternalSyntheticLambda2
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return ((View) obj).isAttachedToWindow();
            }
        }).map(new Function() { // from class: com.android.systemui.statusbar.phone.NearestTouchFrame$$ExternalSyntheticLambda0
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return this.f$0.lambda$findNearestChild$0(motionEvent, (View) obj);
            }
        }).min(Comparator.comparingInt(new ToIntFunction() { // from class: com.android.systemui.statusbar.phone.NearestTouchFrame$$ExternalSyntheticLambda3
            @Override // java.util.function.ToIntFunction
            public final int applyAsInt(Object obj) {
                return NearestTouchFrame.lambda$findNearestChild$1((Pair) obj);
            }
        })).map(new Function() { // from class: com.android.systemui.statusbar.phone.NearestTouchFrame$$ExternalSyntheticLambda1
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return NearestTouchFrame.lambda$findNearestChild$2((Pair) obj);
            }
        }).orElse(null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ Pair lambda$findNearestChild$0(MotionEvent motionEvent, View view) {
        return new Pair(Integer.valueOf(distance(view, motionEvent)), view);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ int lambda$findNearestChild$1(Pair pair) {
        return ((Integer) pair.first).intValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ View lambda$findNearestChild$2(Pair pair) {
        return (View) pair.second;
    }

    private int distance(View view, MotionEvent motionEvent) {
        view.getLocationInWindow(this.mTmpInt);
        int[] iArr = this.mTmpInt;
        int i = iArr[0];
        int[] iArr2 = this.mOffset;
        int i2 = i - iArr2[0];
        int i3 = iArr[1] - iArr2[1];
        return Math.max(Math.min(Math.abs(i2 - ((int) motionEvent.getX())), Math.abs(((int) motionEvent.getX()) - (view.getWidth() + i2))), Math.min(Math.abs(i3 - ((int) motionEvent.getY())), Math.abs(((int) motionEvent.getY()) - (view.getHeight() + i3))));
    }
}
