package com.android.systemui.globalactions;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.R;

/* loaded from: classes.dex */
public class GlobalActionsColumnLayout extends GlobalActionsLayout {
    private boolean mLastSnap;

    public GlobalActionsColumnLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // android.widget.LinearLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        post(new Runnable() { // from class: com.android.systemui.globalactions.GlobalActionsColumnLayout$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$onLayout$0();
            }
        });
    }

    @Override // com.android.systemui.globalactions.GlobalActionsLayout, android.widget.LinearLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
    }

    @Override // com.android.systemui.globalactions.GlobalActionsLayout
    @VisibleForTesting
    protected boolean shouldReverseListItems() {
        int currentRotation = getCurrentRotation();
        if (currentRotation == 0) {
            return false;
        }
        return getCurrentLayoutDirection() == 1 ? currentRotation == 1 : currentRotation == 2;
    }

    @Override // com.android.systemui.globalactions.GlobalActionsLayout, com.android.systemui.MultiListLayout
    public void onUpdateList() {
        super.onUpdateList();
        updateChildOrdering();
    }

    private void updateChildOrdering() {
        if (shouldReverseListItems()) {
            getListView().bringToFront();
        } else {
            getSeparatedView().bringToFront();
        }
    }

    @VisibleForTesting
    protected void snapToPowerButton() {
        int powerButtonOffsetDistance = getPowerButtonOffsetDistance();
        int currentRotation = getCurrentRotation();
        if (currentRotation == 1) {
            setPadding(powerButtonOffsetDistance, 0, 0, 0);
            setGravity(51);
        } else if (currentRotation == 2) {
            setPadding(0, 0, powerButtonOffsetDistance, 0);
            setGravity(85);
        } else {
            setPadding(0, powerButtonOffsetDistance, 0, 0);
            setGravity(53);
        }
    }

    @VisibleForTesting
    protected void centerAlongEdge() {
        int currentRotation = getCurrentRotation();
        if (currentRotation == 1) {
            setPadding(0, 0, 0, 0);
            setGravity(49);
        } else if (currentRotation == 2) {
            setPadding(0, 0, 0, 0);
            setGravity(81);
        } else {
            setPadding(0, 0, 0, 0);
            setGravity(21);
        }
    }

    @VisibleForTesting
    protected int getPowerButtonOffsetDistance() {
        return Math.round(getContext().getResources().getDimension(R.dimen.global_actions_top_padding));
    }

    @VisibleForTesting
    protected boolean shouldSnapToPowerButton() {
        int measuredWidth;
        int measuredWidth2;
        int powerButtonOffsetDistance = getPowerButtonOffsetDistance();
        View wrapper = getWrapper();
        if (getCurrentRotation() == 0) {
            measuredWidth = wrapper.getMeasuredHeight();
            measuredWidth2 = getMeasuredHeight();
        } else {
            measuredWidth = wrapper.getMeasuredWidth();
            measuredWidth2 = getMeasuredWidth();
        }
        return measuredWidth + powerButtonOffsetDistance < measuredWidth2;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @VisibleForTesting
    /* renamed from: updateSnap, reason: merged with bridge method [inline-methods] */
    public void lambda$onLayout$0() {
        boolean zShouldSnapToPowerButton = shouldSnapToPowerButton();
        if (zShouldSnapToPowerButton != this.mLastSnap) {
            if (zShouldSnapToPowerButton) {
                snapToPowerButton();
            } else {
                centerAlongEdge();
            }
        }
        this.mLastSnap = zShouldSnapToPowerButton;
    }

    @VisibleForTesting
    protected float getGridItemSize() {
        return getContext().getResources().getDimension(R.dimen.global_actions_grid_item_height);
    }

    @VisibleForTesting
    protected float getAnimationDistance() {
        return getGridItemSize() / 2.0f;
    }

    @Override // com.android.systemui.MultiListLayout
    public float getAnimationOffsetX() {
        if (getCurrentRotation() == 0) {
            return getAnimationDistance();
        }
        return 0.0f;
    }
}
