package androidx.leanback.widget;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import androidx.leanback.widget.GridLayoutManager;
import androidx.leanback.widget.ItemAlignmentFacet;

/* loaded from: classes.dex */
class ItemAlignmentFacetHelper {
    private static Rect sRect = new Rect();

    static int getAlignmentPosition(View view, ItemAlignmentFacet.ItemAlignmentDef itemAlignmentDef, int i) {
        View viewFindViewById;
        int opticalTopInset;
        int width;
        int width2;
        int width3;
        GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) view.getLayoutParams();
        int i2 = itemAlignmentDef.mViewId;
        if (i2 == 0 || (viewFindViewById = view.findViewById(i2)) == null) {
            viewFindViewById = view;
        }
        int opticalHeight = itemAlignmentDef.mOffset;
        if (i == 0) {
            if (view.getLayoutDirection() == 1) {
                if (viewFindViewById == view) {
                    width2 = layoutParams.getOpticalWidth(viewFindViewById);
                } else {
                    width2 = viewFindViewById.getWidth();
                }
                int paddingLeft = width2 - opticalHeight;
                if (itemAlignmentDef.mOffsetWithPadding) {
                    float f = itemAlignmentDef.mOffsetPercent;
                    if (f == 0.0f) {
                        paddingLeft -= viewFindViewById.getPaddingRight();
                    } else if (f == 100.0f) {
                        paddingLeft += viewFindViewById.getPaddingLeft();
                    }
                }
                if (itemAlignmentDef.mOffsetPercent != -1.0f) {
                    if (viewFindViewById == view) {
                        width3 = layoutParams.getOpticalWidth(viewFindViewById);
                    } else {
                        width3 = viewFindViewById.getWidth();
                    }
                    paddingLeft -= (int) ((width3 * itemAlignmentDef.mOffsetPercent) / 100.0f);
                }
                if (view == viewFindViewById) {
                    return paddingLeft;
                }
                Rect rect = sRect;
                rect.right = paddingLeft;
                ((ViewGroup) view).offsetDescendantRectToMyCoords(viewFindViewById, rect);
                return sRect.right + layoutParams.getOpticalRightInset();
            }
            if (itemAlignmentDef.mOffsetWithPadding) {
                float f2 = itemAlignmentDef.mOffsetPercent;
                if (f2 == 0.0f) {
                    opticalHeight += viewFindViewById.getPaddingLeft();
                } else if (f2 == 100.0f) {
                    opticalHeight -= viewFindViewById.getPaddingRight();
                }
            }
            if (itemAlignmentDef.mOffsetPercent != -1.0f) {
                if (viewFindViewById == view) {
                    width = layoutParams.getOpticalWidth(viewFindViewById);
                } else {
                    width = viewFindViewById.getWidth();
                }
                opticalHeight += (int) ((width * itemAlignmentDef.mOffsetPercent) / 100.0f);
            }
            int i3 = opticalHeight;
            if (view == viewFindViewById) {
                return i3;
            }
            Rect rect2 = sRect;
            rect2.left = i3;
            ((ViewGroup) view).offsetDescendantRectToMyCoords(viewFindViewById, rect2);
            return sRect.left - layoutParams.getOpticalLeftInset();
        }
        if (itemAlignmentDef.mOffsetWithPadding) {
            float f3 = itemAlignmentDef.mOffsetPercent;
            if (f3 == 0.0f) {
                opticalHeight += viewFindViewById.getPaddingTop();
            } else if (f3 == 100.0f) {
                opticalHeight -= viewFindViewById.getPaddingBottom();
            }
        }
        if (itemAlignmentDef.mOffsetPercent != -1.0f) {
            opticalHeight += (int) (((viewFindViewById == view ? layoutParams.getOpticalHeight(viewFindViewById) : viewFindViewById.getHeight()) * itemAlignmentDef.mOffsetPercent) / 100.0f);
        }
        if (view != viewFindViewById) {
            Rect rect3 = sRect;
            rect3.top = opticalHeight;
            ((ViewGroup) view).offsetDescendantRectToMyCoords(viewFindViewById, rect3);
            opticalTopInset = sRect.top - layoutParams.getOpticalTopInset();
        } else {
            opticalTopInset = opticalHeight;
        }
        return itemAlignmentDef.isAlignedToTextViewBaseLine() ? opticalTopInset + viewFindViewById.getBaseline() : opticalTopInset;
    }
}
