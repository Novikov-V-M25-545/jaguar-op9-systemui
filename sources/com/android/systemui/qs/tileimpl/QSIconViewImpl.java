package com.android.systemui.qs.tileimpl;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.AlphaControlledSignalTileView;
import java.util.Objects;
import java.util.function.Supplier;

/* loaded from: classes.dex */
public class QSIconViewImpl extends QSIconView {
    private boolean mAnimationEnabled;
    protected final View mIcon;
    protected final int mIconSizePx;
    private QSTile.Icon mLastIcon;
    private int mState;
    private int mTint;

    protected int getIconMeasureMode() {
        return 1073741824;
    }

    public QSIconViewImpl(Context context) {
        super(context);
        this.mAnimationEnabled = true;
        this.mState = -1;
        this.mIconSizePx = context.getResources().getDimensionPixelSize(R.dimen.qs_tile_icon_size);
        View viewCreateIcon = createIcon();
        this.mIcon = viewCreateIcon;
        addView(viewCreateIcon);
    }

    @Override // com.android.systemui.plugins.qs.QSIconView
    public void disableAnimation() {
        this.mAnimationEnabled = false;
    }

    @Override // com.android.systemui.plugins.qs.QSIconView
    public View getIconView() {
        return this.mIcon;
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int size = View.MeasureSpec.getSize(i);
        this.mIcon.measure(View.MeasureSpec.makeMeasureSpec(size, getIconMeasureMode()), exactly(this.mIconSizePx));
        setMeasuredDimension(size, this.mIcon.getMeasuredHeight());
    }

    @Override // android.view.View
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append('[');
        sb.append("state=" + this.mState);
        sb.append(", tint=" + this.mTint);
        if (this.mLastIcon != null) {
            sb.append(", lastIcon=" + this.mLastIcon.toString());
        }
        sb.append("]");
        return sb.toString();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        layout(this.mIcon, (getMeasuredWidth() - this.mIcon.getMeasuredWidth()) / 2, 0);
    }

    @Override // com.android.systemui.plugins.qs.QSIconView
    public void setIcon(QSTile.State state, boolean z) throws Resources.NotFoundException {
        setIcon((ImageView) this.mIcon, state, z);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Multi-variable type inference failed */
    /* renamed from: updateIcon, reason: merged with bridge method [inline-methods] */
    public void lambda$setIcon$0(ImageView imageView, QSTile.State state, boolean z) {
        Drawable invisibleDrawable;
        Supplier<QSTile.Icon> supplier = state.iconSupplier;
        QSTile.Icon icon = supplier != null ? supplier.get() : state.icon;
        int i = R.id.qs_icon_tag;
        if (Objects.equals(icon, imageView.getTag(i)) && Objects.equals(state.slash, imageView.getTag(R.id.qs_slash_tag))) {
            return;
        }
        boolean z2 = z && shouldAnimate(imageView);
        this.mLastIcon = icon;
        if (icon == null) {
            invisibleDrawable = 0;
        } else if (z2) {
            invisibleDrawable = icon.getDrawable(((ViewGroup) this).mContext);
        } else {
            invisibleDrawable = icon.getInvisibleDrawable(((ViewGroup) this).mContext);
        }
        int padding = icon != null ? icon.getPadding() : 0;
        if (invisibleDrawable != 0) {
            invisibleDrawable.setAutoMirrored(false);
            invisibleDrawable.setLayoutDirection(getLayoutDirection());
        }
        if (imageView instanceof SlashImageView) {
            SlashImageView slashImageView = (SlashImageView) imageView;
            slashImageView.setAnimationEnabled(z2);
            slashImageView.setState(null, invisibleDrawable);
        } else {
            imageView.setImageDrawable(invisibleDrawable);
        }
        imageView.setTag(i, icon);
        imageView.setTag(R.id.qs_slash_tag, state.slash);
        imageView.setPadding(0, padding, 0, padding);
        if (invisibleDrawable instanceof Animatable2) {
            final Animatable2 animatable2 = (Animatable2) invisibleDrawable;
            animatable2.start();
            if (state.isTransient) {
                animatable2.registerAnimationCallback(new Animatable2.AnimationCallback() { // from class: com.android.systemui.qs.tileimpl.QSIconViewImpl.1
                    @Override // android.graphics.drawable.Animatable2.AnimationCallback
                    public void onAnimationEnd(Drawable drawable) {
                        animatable2.start();
                    }
                });
            }
        }
    }

    private boolean shouldAnimate(ImageView imageView) {
        return this.mAnimationEnabled && imageView.isShown() && imageView.getDrawable() != null;
    }

    protected void setIcon(final ImageView imageView, final QSTile.State state, final boolean z) throws Resources.NotFoundException {
        if (state.disabledByPolicy) {
            imageView.setColorFilter(getContext().getColor(R.color.qs_tile_disabled_color));
        } else {
            imageView.clearColorFilter();
        }
        int i = state.state;
        if (i != this.mState) {
            int color = getColor(i);
            this.mState = state.state;
            if (this.mTint != 0 && z && shouldAnimate(imageView)) {
                animateGrayScale(this.mTint, color, imageView, new Runnable() { // from class: com.android.systemui.qs.tileimpl.QSIconViewImpl$$ExternalSyntheticLambda1
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$setIcon$0(imageView, state, z);
                    }
                });
                this.mTint = color;
                return;
            }
            if (imageView instanceof AlphaControlledSignalTileView.AlphaControlledSlashImageView) {
                ((AlphaControlledSignalTileView.AlphaControlledSlashImageView) imageView).setFinalImageTintList(ColorStateList.valueOf(color));
            } else {
                setTint(imageView, color);
            }
            this.mTint = color;
            lambda$setIcon$0(imageView, state, z);
            return;
        }
        lambda$setIcon$0(imageView, state, z);
    }

    protected int getColor(int i) {
        return QSTileImpl.getColorForState(getContext(), i);
    }

    private void animateGrayScale(int i, final int i2, final ImageView imageView, final Runnable runnable) throws Resources.NotFoundException {
        if (imageView instanceof AlphaControlledSignalTileView.AlphaControlledSlashImageView) {
            ((AlphaControlledSignalTileView.AlphaControlledSlashImageView) imageView).setFinalImageTintList(ColorStateList.valueOf(i2));
        }
        final boolean z = Settings.System.getIntForUser(getContext().getContentResolver(), "qs_panel_bg_use_new_tint", 0, -2) == 1;
        final boolean z2 = getContext().getResources().getBoolean(R.bool.config_enable_qs_tile_tinting);
        if (this.mAnimationEnabled && ValueAnimator.areAnimatorsEnabled()) {
            final float fAlpha = Color.alpha(i);
            final float fAlpha2 = Color.alpha(i2);
            final float fRed = Color.red(i);
            final float fRed2 = Color.red(i2);
            ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            valueAnimatorOfFloat.setDuration(350L);
            valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.qs.tileimpl.QSIconViewImpl$$ExternalSyntheticLambda0
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    QSIconViewImpl.lambda$animateGrayScale$1(fAlpha, fAlpha2, fRed, fRed2, z, z2, imageView, i2, valueAnimator);
                }
            });
            valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.qs.tileimpl.QSIconViewImpl.2
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    runnable.run();
                }
            });
            valueAnimatorOfFloat.start();
            return;
        }
        setTint(imageView, i2);
        runnable.run();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$animateGrayScale$1(float f, float f2, float f3, float f4, boolean z, boolean z2, ImageView imageView, int i, ValueAnimator valueAnimator) {
        float animatedFraction = valueAnimator.getAnimatedFraction();
        int i2 = (int) (f + ((f2 - f) * animatedFraction));
        int i3 = (int) (f3 + ((f4 - f3) * animatedFraction));
        if (z || z2) {
            setTint(imageView, i);
        } else {
            setTint(imageView, Color.argb(i2, i3, i3, i3));
        }
    }

    public static void setTint(ImageView imageView, int i) {
        imageView.setImageTintList(ColorStateList.valueOf(i));
    }

    protected View createIcon() {
        SlashImageView slashImageView = new SlashImageView(((ViewGroup) this).mContext);
        slashImageView.setId(android.R.id.icon);
        slashImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        return slashImageView;
    }

    protected final int exactly(int i) {
        return View.MeasureSpec.makeMeasureSpec(i, 1073741824);
    }

    protected final void layout(View view, int i, int i2) {
        view.layout(i, i2, view.getMeasuredWidth() + i, view.getMeasuredHeight() + i2);
    }
}
