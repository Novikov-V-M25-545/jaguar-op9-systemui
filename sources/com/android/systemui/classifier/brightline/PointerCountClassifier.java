package com.android.systemui.classifier.brightline;

import android.view.MotionEvent;

/* loaded from: classes.dex */
class PointerCountClassifier extends FalsingClassifier {
    private int mMaxPointerCount;

    PointerCountClassifier(FalsingDataProvider falsingDataProvider) {
        super(falsingDataProvider);
    }

    @Override // com.android.systemui.classifier.brightline.FalsingClassifier
    public void onTouchEvent(MotionEvent motionEvent) {
        int i = this.mMaxPointerCount;
        if (motionEvent.getActionMasked() == 0) {
            this.mMaxPointerCount = motionEvent.getPointerCount();
        } else {
            this.mMaxPointerCount = Math.max(this.mMaxPointerCount, motionEvent.getPointerCount());
        }
        if (i != this.mMaxPointerCount) {
            FalsingClassifier.logDebug("Pointers observed:" + this.mMaxPointerCount);
        }
    }

    @Override // com.android.systemui.classifier.brightline.FalsingClassifier
    public boolean isFalseTouch() {
        int interactionType = getInteractionType();
        return (interactionType == 0 || interactionType == 2) ? this.mMaxPointerCount > 2 : this.mMaxPointerCount > 1;
    }

    @Override // com.android.systemui.classifier.brightline.FalsingClassifier
    String getReason() {
        return String.format(null, "{pointersObserved=%d, threshold=%d}", Integer.valueOf(this.mMaxPointerCount), 1);
    }
}
