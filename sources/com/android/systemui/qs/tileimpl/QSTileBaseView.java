package com.android.systemui.qs.tileimpl;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.PathParser;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import com.android.settingslib.Utils;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;

/* loaded from: classes.dex */
public class QSTileBaseView extends com.android.systemui.plugins.qs.QSTileView {
    private String mAccessibilityClass;
    private final ImageView mBg;
    private int mCircleColor;
    private boolean mCollapsedView;
    private int mColorActive;
    private int mColorActiveAlpha;
    private final int mColorDisabled;
    private final int mColorInactive;
    private final H mHandler;
    protected QSIconView mIcon;
    private final FrameLayout mIconFrame;
    private final int[] mLocInScreen;
    protected RippleDrawable mRipple;
    private boolean mShowRippleEffect;
    private float mStrokeWidthActive;
    private float mStrokeWidthInactive;
    private Drawable mTileBackground;
    private boolean mTileState;

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // com.android.systemui.plugins.qs.QSTileView
    public void textVisibility() {
    }

    public QSTileBaseView(Context context, QSIconView qSIconView, boolean z) throws Resources.NotFoundException {
        super(context);
        this.mHandler = new H();
        this.mLocInScreen = new int[2];
        this.mShowRippleEffect = true;
        context.getResources().getDimensionPixelSize(R.dimen.qs_quick_tile_padding);
        FrameLayout frameLayout = new FrameLayout(context);
        this.mIconFrame = frameLayout;
        this.mStrokeWidthActive = context.getResources().getDimension(android.R.dimen.config_alertDialogSelectionScrollOffset);
        this.mStrokeWidthInactive = context.getResources().getDimension(android.R.dimen.config_am_pssToRssThresholdModifier);
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(R.dimen.qs_quick_tile_size);
        addView(frameLayout, new LinearLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize));
        ImageView imageView = new ImageView(getContext());
        this.mBg = imageView;
        if (context.getResources().getBoolean(R.bool.config_useMaskForQs)) {
            ShapeDrawable shapeDrawable = new ShapeDrawable(new PathShape(new Path(PathParser.createPathFromPathData(context.getResources().getString(android.R.string.config_defaultDndDeniedPackages))), 100.0f, 100.0f));
            shapeDrawable.setTintList(ColorStateList.valueOf(0));
            int dimensionPixelSize2 = context.getResources().getDimensionPixelSize(R.dimen.qs_tile_background_size);
            shapeDrawable.setIntrinsicHeight(dimensionPixelSize2);
            shapeDrawable.setIntrinsicWidth(dimensionPixelSize2);
            imageView.setImageDrawable(shapeDrawable);
            ViewGroup.LayoutParams layoutParams = new FrameLayout.LayoutParams(dimensionPixelSize2, dimensionPixelSize2, 17);
            frameLayout.addView(imageView, layoutParams);
            imageView.setLayoutParams(layoutParams);
        } else {
            imageView.setImageResource(R.drawable.ic_qs_circle);
            frameLayout.addView(imageView);
        }
        this.mIcon = qSIconView;
        frameLayout.addView(this.mIcon, new FrameLayout.LayoutParams(-2, -2, 17));
        frameLayout.setClipChildren(false);
        frameLayout.setClipToPadding(false);
        Drawable drawableNewTileBackground = newTileBackground();
        this.mTileBackground = drawableNewTileBackground;
        if (drawableNewTileBackground instanceof RippleDrawable) {
            setRipple((RippleDrawable) drawableNewTileBackground);
        }
        setImportantForAccessibility(1);
        setBackground(this.mTileBackground);
        this.mColorActive = Utils.getColorAttrDefaultColor(context, android.R.attr.colorAccent);
        this.mColorDisabled = Utils.getDisabled(context, Utils.getColorAttrDefaultColor(context, android.R.attr.textColorTertiary));
        this.mColorActiveAlpha = adjustAlpha(this.mColorActive, 0.2f);
        boolean z2 = Settings.System.getIntForUser(context.getContentResolver(), "qs_panel_bg_use_new_tint", 0, -2) == 1;
        boolean z3 = context.getResources().getBoolean(R.bool.config_enable_qs_tile_tinting);
        if (z2 || z3) {
            this.mColorActive = this.mColorActiveAlpha;
        }
        this.mColorInactive = Utils.getColorAttrDefaultColor(context, android.R.attr.textColorSecondary);
        setPadding(0, 0, 0, 0);
        setClipChildren(false);
        setClipToPadding(false);
        this.mCollapsedView = z;
        setFocusable(true);
    }

    protected Drawable newTileBackground() {
        TypedArray typedArrayObtainStyledAttributes = getContext().obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackgroundBorderless});
        Drawable drawable = typedArrayObtainStyledAttributes.getDrawable(0);
        typedArrayObtainStyledAttributes.recycle();
        return drawable;
    }

    private void setRipple(RippleDrawable rippleDrawable) {
        this.mRipple = rippleDrawable;
        if (getWidth() != 0) {
            updateRippleSize();
        }
    }

    private void updateRippleSize() {
        int measuredWidth = (this.mIconFrame.getMeasuredWidth() / 2) + this.mIconFrame.getLeft();
        int measuredHeight = (this.mIconFrame.getMeasuredHeight() / 2) + this.mIconFrame.getTop();
        int height = (int) (this.mIcon.getHeight() * 0.85f);
        this.mRipple.setHotspotBounds(measuredWidth - height, measuredHeight - height, measuredWidth + height, measuredHeight + height);
    }

    @Override // com.android.systemui.plugins.qs.QSTileView
    public void init(final QSTile qSTile) {
        init(new View.OnClickListener() { // from class: com.android.systemui.qs.tileimpl.QSTileBaseView$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                qSTile.click();
            }
        }, new View.OnClickListener() { // from class: com.android.systemui.qs.tileimpl.QSTileBaseView$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                qSTile.secondaryClick();
            }
        }, new View.OnLongClickListener() { // from class: com.android.systemui.qs.tileimpl.QSTileBaseView$$ExternalSyntheticLambda3
            @Override // android.view.View.OnLongClickListener
            public final boolean onLongClick(View view) {
                return QSTileBaseView.lambda$init$2(qSTile, view);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$init$2(QSTile qSTile, View view) {
        qSTile.longClick();
        return true;
    }

    public void init(View.OnClickListener onClickListener, View.OnClickListener onClickListener2, View.OnLongClickListener onLongClickListener) {
        setOnClickListener(onClickListener);
        setOnLongClickListener(onLongClickListener);
    }

    @Override // android.widget.LinearLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (this.mRipple != null) {
            updateRippleSize();
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTileView
    public View updateAccessibilityOrder(View view) {
        setAccessibilityTraversalAfter(view.getId());
        return this;
    }

    @Override // com.android.systemui.plugins.qs.QSTileView
    public void onStateChanged(QSTile.State state) {
        this.mHandler.obtainMessage(1, state).sendToTarget();
    }

    private void updateStrokeShapeWidth(QSTile.State state) {
        getContext().getResources();
        if (this.mBg.getDrawable() instanceof ShapeDrawable) {
            ShapeDrawable shapeDrawable = (ShapeDrawable) this.mBg.getDrawable();
            shapeDrawable.getPaint().setStyle(Paint.Style.FILL);
            int i = state.state;
            if (i == 1) {
                if (this.mStrokeWidthInactive >= 0.0f) {
                    shapeDrawable.getPaint().setStyle(Paint.Style.STROKE);
                    shapeDrawable.getPaint().setStrokeWidth(this.mStrokeWidthInactive);
                    return;
                }
                return;
            }
            if (i == 2 && this.mStrokeWidthActive >= 0.0f) {
                shapeDrawable.getPaint().setStyle(Paint.Style.STROKE);
                shapeDrawable.getPaint().setStrokeWidth(this.mStrokeWidthActive);
            }
        }
    }

    private static int adjustAlpha(int i, float f) {
        return Color.argb(Math.round(Color.alpha(i) * f), Color.red(i), Color.green(i), Color.blue(i));
    }

    protected void handleStateChanged(QSTile.State state) {
        boolean z;
        updateStrokeShapeWidth(state);
        int circleColor = getCircleColor(state.state);
        boolean zAnimationsEnabled = animationsEnabled();
        int i = this.mCircleColor;
        if (circleColor != i) {
            if (zAnimationsEnabled) {
                ValueAnimator duration = ValueAnimator.ofArgb(i, circleColor).setDuration(350L);
                duration.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.qs.tileimpl.QSTileBaseView$$ExternalSyntheticLambda0
                    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                        this.f$0.lambda$handleStateChanged$3(valueAnimator);
                    }
                });
                duration.start();
            } else {
                QSIconViewImpl.setTint(this.mBg, circleColor);
            }
            this.mCircleColor = circleColor;
        }
        this.mShowRippleEffect = state.showRippleEffect;
        setClickable(state.state != 0);
        setLongClickable(state.handlesLongClick);
        this.mIcon.setIcon(state, zAnimationsEnabled);
        setContentDescription(state.contentDescription);
        StringBuilder sb = new StringBuilder();
        int i2 = state.state;
        if (i2 == 0) {
            sb.append(((LinearLayout) this).mContext.getString(R.string.tile_unavailable));
        } else if (i2 == 1) {
            if (state instanceof QSTile.BooleanState) {
                sb.append(((LinearLayout) this).mContext.getString(R.string.switch_bar_off));
            }
        } else if (i2 == 2 && (state instanceof QSTile.BooleanState)) {
            sb.append(((LinearLayout) this).mContext.getString(R.string.switch_bar_on));
        }
        if (!TextUtils.isEmpty(state.stateDescription)) {
            sb.append(", ");
            sb.append(state.stateDescription);
        }
        setStateDescription(sb.toString());
        this.mAccessibilityClass = state.state == 0 ? null : state.expandedAccessibilityClassName;
        if (!(state instanceof QSTile.BooleanState) || this.mTileState == (z = ((QSTile.BooleanState) state).value)) {
            return;
        }
        this.mTileState = z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleStateChanged$3(ValueAnimator valueAnimator) {
        this.mBg.setImageTintList(ColorStateList.valueOf(((Integer) valueAnimator.getAnimatedValue()).intValue()));
    }

    protected boolean animationsEnabled() {
        if (!isShown() || getAlpha() != 1.0f) {
            return false;
        }
        getLocationOnScreen(this.mLocInScreen);
        return this.mLocInScreen[1] >= (-getHeight());
    }

    private int getCircleColor(int i) {
        if (i == 0 || i == 1) {
            return this.mColorDisabled;
        }
        if (i == 2) {
            return this.mColorActive;
        }
        Log.e("QSTileBaseView", "Invalid state " + i);
        return 0;
    }

    @Override // android.view.View
    public void setClickable(boolean z) {
        super.setClickable(z);
        setBackground((z && this.mShowRippleEffect) ? this.mRipple : null);
    }

    @Override // com.android.systemui.plugins.qs.QSTileView
    public int getDetailY() {
        return getTop() + (getHeight() / 2);
    }

    @Override // com.android.systemui.plugins.qs.QSTileView
    public QSIconView getIcon() {
        return this.mIcon;
    }

    @Override // com.android.systemui.plugins.qs.QSTileView
    public View getIconWithBackground() {
        return this.mIconFrame;
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        if (TextUtils.isEmpty(this.mAccessibilityClass)) {
            return;
        }
        accessibilityEvent.setClassName(this.mAccessibilityClass);
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setSelected(false);
        if (TextUtils.isEmpty(this.mAccessibilityClass)) {
            return;
        }
        accessibilityNodeInfo.setClassName(this.mAccessibilityClass);
        if (Switch.class.getName().equals(this.mAccessibilityClass)) {
            accessibilityNodeInfo.setText(getResources().getString(this.mTileState ? R.string.switch_bar_on : R.string.switch_bar_off));
            accessibilityNodeInfo.setChecked(this.mTileState);
            accessibilityNodeInfo.setCheckable(true);
            if (isLongClickable()) {
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_LONG_CLICK.getId(), getResources().getString(R.string.accessibility_long_click_tile)));
            }
        }
    }

    @Override // android.view.View
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append('[');
        sb.append("locInScreen=(" + this.mLocInScreen[0] + ", " + this.mLocInScreen[1] + ")");
        StringBuilder sb2 = new StringBuilder();
        sb2.append(", iconView=");
        sb2.append(this.mIcon.toString());
        sb.append(sb2.toString());
        sb.append(", tileState=" + this.mTileState);
        sb.append("]");
        return sb.toString();
    }

    private class H extends Handler {
        public H() {
            super(Looper.getMainLooper());
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what == 1) {
                QSTileBaseView.this.handleStateChanged((QSTile.State) message.obj);
            }
        }
    }
}
