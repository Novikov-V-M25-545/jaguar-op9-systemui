package com.android.systemui.classifier.brightline;

/* loaded from: classes.dex */
public class TypeClassifier extends FalsingClassifier {
    TypeClassifier(FalsingDataProvider falsingDataProvider) {
        super(falsingDataProvider);
    }

    @Override // com.android.systemui.classifier.brightline.FalsingClassifier
    public boolean isFalseTouch() {
        boolean zIsVertical = isVertical();
        boolean zIsUp = isUp();
        boolean zIsRight = isRight();
        int interactionType = getInteractionType();
        if (interactionType != 0) {
            if (interactionType == 1) {
                return zIsVertical;
            }
            if (interactionType != 2) {
                if (interactionType != 4) {
                    if (interactionType == 5) {
                        return (zIsRight && zIsUp) ? false : true;
                    }
                    if (interactionType == 6) {
                        return zIsRight || !zIsUp;
                    }
                    if (interactionType != 8) {
                        if (interactionType != 9) {
                            return true;
                        }
                    }
                }
                return (zIsVertical && zIsUp) ? false : true;
            }
        }
        return !zIsVertical || zIsUp;
    }

    @Override // com.android.systemui.classifier.brightline.FalsingClassifier
    String getReason() {
        return String.format("{vertical=%s, up=%s, right=%s}", Boolean.valueOf(isVertical()), Boolean.valueOf(isUp()), Boolean.valueOf(isRight()));
    }
}
