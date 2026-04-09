package com.android.systemui.statusbar.notification.row;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import com.android.settingslib.Utils;
import com.android.systemui.R;
import com.android.systemui.statusbar.notification.AnimatableProperty;
import com.android.systemui.statusbar.notification.PropertyAnimator;
import com.android.systemui.statusbar.notification.stack.AnimationProperties;
import java.util.function.BiConsumer;
import java.util.function.Function;

/* loaded from: classes.dex */
public abstract class ExpandableOutlineView extends ExpandableView {
    private boolean mAlwaysRoundBothCorners;
    private int mBackgroundTop;
    private float mBottomRoundness;
    private final Path mClipPath;
    private float mCurrentBottomRoundness;
    private float mCurrentTopRoundness;
    private boolean mCustomOutline;
    private float mDistanceToTopRoundness;
    private float mOutlineAlpha;
    protected float mOutlineRadius;
    private final Rect mOutlineRect;
    private final ViewOutlineProvider mProvider;
    protected boolean mShouldTranslateContents;
    private float[] mTmpCornerRadii;
    private Path mTmpPath;
    private boolean mTopAmountRounded;
    private float mTopRoundness;
    private static final AnimatableProperty TOP_ROUNDNESS = AnimatableProperty.from("topRoundness", new BiConsumer() { // from class: com.android.systemui.statusbar.notification.row.ExpandableOutlineView$$ExternalSyntheticLambda0
        @Override // java.util.function.BiConsumer
        public final void accept(Object obj, Object obj2) {
            ((ExpandableOutlineView) obj).setTopRoundnessInternal(((Float) obj2).floatValue());
        }
    }, new Function() { // from class: com.android.systemui.statusbar.notification.row.ExpandableOutlineView$$ExternalSyntheticLambda3
        @Override // java.util.function.Function
        public final Object apply(Object obj) {
            return Float.valueOf(((ExpandableOutlineView) obj).getCurrentTopRoundness());
        }
    }, R.id.top_roundess_animator_tag, R.id.top_roundess_animator_end_tag, R.id.top_roundess_animator_start_tag);
    private static final AnimatableProperty BOTTOM_ROUNDNESS = AnimatableProperty.from("bottomRoundness", new BiConsumer() { // from class: com.android.systemui.statusbar.notification.row.ExpandableOutlineView$$ExternalSyntheticLambda1
        @Override // java.util.function.BiConsumer
        public final void accept(Object obj, Object obj2) {
            ((ExpandableOutlineView) obj).setBottomRoundnessInternal(((Float) obj2).floatValue());
        }
    }, new Function() { // from class: com.android.systemui.statusbar.notification.row.ExpandableOutlineView$$ExternalSyntheticLambda2
        @Override // java.util.function.Function
        public final Object apply(Object obj) {
            return Float.valueOf(((ExpandableOutlineView) obj).getCurrentBottomRoundness());
        }
    }, R.id.bottom_roundess_animator_tag, R.id.bottom_roundess_animator_end_tag, R.id.bottom_roundess_animator_start_tag);
    private static final AnimationProperties ROUNDNESS_PROPERTIES = new AnimationProperties().setDuration(360);
    private static final Path EMPTY_PATH = new Path();

    protected boolean childNeedsClipping(View view) {
        return false;
    }

    public Path getCustomClipPath(View view) {
        return null;
    }

    public boolean topAmountNeedsClipping() {
        return true;
    }

    protected Path getClipPath(boolean z) {
        int iMax;
        int i;
        int width;
        int iMax2;
        float currentBackgroundRadiusTop = this.mAlwaysRoundBothCorners ? this.mOutlineRadius : getCurrentBackgroundRadiusTop();
        if (!this.mCustomOutline) {
            int translation = (!this.mShouldTranslateContents || z) ? 0 : (int) getTranslation();
            int i2 = (int) (this.mExtraWidthForClipping / 2.0f);
            iMax = Math.max(translation, 0) - i2;
            i = this.mClipTopAmount + this.mBackgroundTop;
            width = getWidth() + i2 + Math.min(translation, 0);
            iMax2 = Math.max(this.mMinimumHeightForClipping, Math.max(getActualHeight() - this.mClipBottomAmount, (int) (i + currentBackgroundRadiusTop)));
        } else {
            Rect rect = this.mOutlineRect;
            iMax = rect.left;
            i = rect.top;
            width = rect.right;
            iMax2 = rect.bottom;
        }
        int i3 = iMax2;
        int i4 = iMax;
        int i5 = i;
        int i6 = width;
        int i7 = i3 - i5;
        if (i7 == 0) {
            return EMPTY_PATH;
        }
        float currentBackgroundRadiusBottom = this.mAlwaysRoundBothCorners ? this.mOutlineRadius : getCurrentBackgroundRadiusBottom();
        float f = currentBackgroundRadiusTop + currentBackgroundRadiusBottom;
        float f2 = i7;
        if (f > f2) {
            float f3 = f - f2;
            float f4 = this.mCurrentTopRoundness;
            float f5 = this.mCurrentBottomRoundness;
            currentBackgroundRadiusTop -= (f3 * f4) / (f4 + f5);
            currentBackgroundRadiusBottom -= (f3 * f5) / (f4 + f5);
        }
        getRoundedRectPath(i4, i5, i6, i3, currentBackgroundRadiusTop, currentBackgroundRadiusBottom, this.mTmpPath);
        return this.mTmpPath;
    }

    public void getRoundedRectPath(int i, int i2, int i3, int i4, float f, float f2, Path path) {
        path.reset();
        float[] fArr = this.mTmpCornerRadii;
        fArr[0] = f;
        fArr[1] = f;
        fArr[2] = f;
        fArr[3] = f;
        fArr[4] = f2;
        fArr[5] = f2;
        fArr[6] = f2;
        fArr[7] = f2;
        path.addRoundRect(i, i2, i3, i4, fArr, Path.Direction.CW);
    }

    public ExpandableOutlineView(Context context, AttributeSet attributeSet) throws Resources.NotFoundException {
        super(context, attributeSet);
        this.mOutlineRect = new Rect();
        this.mClipPath = new Path();
        this.mOutlineAlpha = -1.0f;
        this.mTmpPath = new Path();
        this.mDistanceToTopRoundness = -1.0f;
        this.mTmpCornerRadii = new float[8];
        ViewOutlineProvider viewOutlineProvider = new ViewOutlineProvider() { // from class: com.android.systemui.statusbar.notification.row.ExpandableOutlineView.1
            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view, Outline outline) {
                if (!ExpandableOutlineView.this.mCustomOutline && ExpandableOutlineView.this.mCurrentTopRoundness == 0.0f && ExpandableOutlineView.this.mCurrentBottomRoundness == 0.0f && !ExpandableOutlineView.this.mAlwaysRoundBothCorners && !ExpandableOutlineView.this.mTopAmountRounded) {
                    ExpandableOutlineView expandableOutlineView = ExpandableOutlineView.this;
                    int translation = expandableOutlineView.mShouldTranslateContents ? (int) expandableOutlineView.getTranslation() : 0;
                    int iMax = Math.max(translation, 0);
                    ExpandableOutlineView expandableOutlineView2 = ExpandableOutlineView.this;
                    int i = expandableOutlineView2.mClipTopAmount + expandableOutlineView2.mBackgroundTop;
                    outline.setRect(iMax, i, ExpandableOutlineView.this.getWidth() + Math.min(translation, 0), Math.max(ExpandableOutlineView.this.getActualHeight() - ExpandableOutlineView.this.mClipBottomAmount, i));
                } else {
                    Path clipPath = ExpandableOutlineView.this.getClipPath(false);
                    if (clipPath != null) {
                        outline.setPath(clipPath);
                    }
                }
                outline.setAlpha(ExpandableOutlineView.this.mOutlineAlpha);
            }
        };
        this.mProvider = viewOutlineProvider;
        setOutlineProvider(viewOutlineProvider);
        initDimens();
    }

    @Override // android.view.ViewGroup
    protected boolean drawChild(Canvas canvas, View view, long j) {
        Path path;
        canvas.save();
        if (this.mTopAmountRounded && topAmountNeedsClipping()) {
            int i = (int) ((-this.mExtraWidthForClipping) / 2.0f);
            int i2 = (int) (this.mClipTopAmount - this.mDistanceToTopRoundness);
            getRoundedRectPath(i, i2, getWidth() + ((int) (this.mExtraWidthForClipping + i)), (int) Math.max(this.mMinimumHeightForClipping, Math.max(getActualHeight() - this.mClipBottomAmount, i2 + this.mOutlineRadius)), this.mOutlineRadius, 0.0f, this.mClipPath);
            path = this.mClipPath;
        } else {
            path = null;
        }
        boolean z = false;
        if (childNeedsClipping(view)) {
            Path customClipPath = getCustomClipPath(view);
            if (customClipPath == null) {
                customClipPath = getClipPath(false);
            }
            if (customClipPath != null) {
                if (path != null) {
                    customClipPath.op(path, Path.Op.INTERSECT);
                }
                canvas.clipPath(customClipPath);
                z = true;
            }
        }
        if (!z && path != null) {
            canvas.clipPath(path);
        }
        boolean zDrawChild = super.drawChild(canvas, view, j);
        canvas.restore();
        return zDrawChild;
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void setExtraWidthForClipping(float f) {
        super.setExtraWidthForClipping(f);
        invalidate();
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void setMinimumHeightForClipping(int i) {
        super.setMinimumHeightForClipping(i);
        invalidate();
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void setDistanceToTopRoundness(float f) {
        super.setDistanceToTopRoundness(f);
        if (f != this.mDistanceToTopRoundness) {
            this.mTopAmountRounded = f >= 0.0f;
            this.mDistanceToTopRoundness = f;
            applyRoundness();
        }
    }

    protected boolean isClippingNeeded() {
        return this.mAlwaysRoundBothCorners || this.mCustomOutline || getTranslation() != 0.0f;
    }

    private void initDimens() throws Resources.NotFoundException {
        Resources resources = getResources();
        this.mShouldTranslateContents = resources.getBoolean(R.bool.config_translateNotificationContentsOnSwipe);
        this.mOutlineRadius = resources.getDimension(R.dimen.notification_shadow_radius);
        boolean z = resources.getBoolean(R.bool.config_clipNotificationsToOutline);
        this.mAlwaysRoundBothCorners = z;
        if (!z) {
            this.mOutlineRadius = resources.getDimensionPixelSize(Utils.getThemeAttr(((FrameLayout) this).mContext, android.R.attr.dialogCornerRadius));
        }
        setClipToOutline(this.mAlwaysRoundBothCorners);
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public boolean setTopRoundness(float f, boolean z) {
        if (this.mTopRoundness == f) {
            return false;
        }
        this.mTopRoundness = f;
        PropertyAnimator.setProperty(this, TOP_ROUNDNESS, f, ROUNDNESS_PROPERTIES, z);
        return true;
    }

    protected void applyRoundness() {
        invalidateOutline();
        invalidate();
    }

    public float getCurrentBackgroundRadiusTop() {
        if (this.mTopAmountRounded) {
            return this.mOutlineRadius;
        }
        return this.mCurrentTopRoundness * this.mOutlineRadius;
    }

    public float getCurrentTopRoundness() {
        return this.mCurrentTopRoundness;
    }

    public float getCurrentBottomRoundness() {
        return this.mCurrentBottomRoundness;
    }

    protected float getCurrentBackgroundRadiusBottom() {
        return this.mCurrentBottomRoundness * this.mOutlineRadius;
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public boolean setBottomRoundness(float f, boolean z) {
        if (this.mBottomRoundness == f) {
            return false;
        }
        this.mBottomRoundness = f;
        PropertyAnimator.setProperty(this, BOTTOM_ROUNDNESS, f, ROUNDNESS_PROPERTIES, z);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setTopRoundnessInternal(float f) {
        this.mCurrentTopRoundness = f;
        applyRoundness();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setBottomRoundnessInternal(float f) {
        this.mCurrentBottomRoundness = f;
        applyRoundness();
    }

    public void onDensityOrFontScaleChanged() throws Resources.NotFoundException {
        initDimens();
        applyRoundness();
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void setActualHeight(int i, boolean z) {
        int actualHeight = getActualHeight();
        super.setActualHeight(i, z);
        if (actualHeight != i) {
            applyRoundness();
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void setClipTopAmount(int i) {
        int clipTopAmount = getClipTopAmount();
        super.setClipTopAmount(i);
        if (clipTopAmount != i) {
            applyRoundness();
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void setClipBottomAmount(int i) {
        int clipBottomAmount = getClipBottomAmount();
        super.setClipBottomAmount(i);
        if (clipBottomAmount != i) {
            applyRoundness();
        }
    }

    protected void setOutlineAlpha(float f) {
        if (f != this.mOutlineAlpha) {
            this.mOutlineAlpha = f;
            applyRoundness();
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public float getOutlineAlpha() {
        return this.mOutlineAlpha;
    }

    protected void setOutlineRect(RectF rectF) {
        if (rectF != null) {
            setOutlineRect(rectF.left, rectF.top, rectF.right, rectF.bottom);
        } else {
            this.mCustomOutline = false;
            applyRoundness();
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public int getOutlineTranslation() {
        return this.mCustomOutline ? this.mOutlineRect.left : (int) getTranslation();
    }

    public void updateOutline() {
        if (this.mCustomOutline) {
            return;
        }
        setOutlineProvider(needsOutline() ? this.mProvider : null);
    }

    protected boolean needsOutline() {
        if (isChildInGroup()) {
            return isGroupExpanded() && !isGroupExpansionChanging();
        }
        if (isSummaryWithChildren()) {
            return !isGroupExpanded() || isGroupExpansionChanging();
        }
        return true;
    }

    protected void setOutlineRect(float f, float f2, float f3, float f4) {
        this.mCustomOutline = true;
        this.mOutlineRect.set((int) f, (int) f2, (int) f3, (int) f4);
        this.mOutlineRect.bottom = (int) Math.max(f2, r6.bottom);
        this.mOutlineRect.right = (int) Math.max(f, r5.right);
        applyRoundness();
    }
}
