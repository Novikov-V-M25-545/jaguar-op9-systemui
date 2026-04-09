package com.android.systemui.qs;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.tileimpl.QSIconViewImpl;
import com.android.systemui.qs.tileimpl.SlashImageView;

/* loaded from: classes.dex */
public class SignalTileView extends QSIconViewImpl {
    private static final long DEFAULT_DURATION;
    private static final long SHORT_DURATION;
    protected FrameLayout mIconFrame;
    private ImageView mIn;
    private ImageView mOut;
    private ImageView mOverlay;
    protected ImageView mSignal;
    private int mSignalIndicatorToIconFrameSpacing;
    private int mWideOverlayIconStartPadding;

    @Override // com.android.systemui.qs.tileimpl.QSIconViewImpl
    protected int getIconMeasureMode() {
        return Integer.MIN_VALUE;
    }

    static {
        long duration = new ValueAnimator().getDuration();
        DEFAULT_DURATION = duration;
        SHORT_DURATION = duration / 3;
    }

    public SignalTileView(Context context) {
        super(context);
        this.mIn = addTrafficView(R.drawable.ic_qs_signal_in);
        this.mOut = addTrafficView(R.drawable.ic_qs_signal_out);
        setClipChildren(false);
        setClipToPadding(false);
        this.mWideOverlayIconStartPadding = context.getResources().getDimensionPixelSize(R.dimen.wide_type_icon_start_padding_qs);
        this.mSignalIndicatorToIconFrameSpacing = context.getResources().getDimensionPixelSize(R.dimen.signal_indicator_to_icon_frame_spacing);
    }

    private ImageView addTrafficView(int i) {
        ImageView imageView = new ImageView(((ViewGroup) this).mContext);
        imageView.setImageResource(i);
        imageView.setAlpha(0.0f);
        addView(imageView);
        return imageView;
    }

    @Override // com.android.systemui.qs.tileimpl.QSIconViewImpl
    protected View createIcon() {
        this.mIconFrame = new FrameLayout(((ViewGroup) this).mContext);
        SlashImageView slashImageViewCreateSlashImageView = createSlashImageView(((ViewGroup) this).mContext);
        this.mSignal = slashImageViewCreateSlashImageView;
        this.mIconFrame.addView(slashImageViewCreateSlashImageView);
        ImageView imageView = new ImageView(((ViewGroup) this).mContext);
        this.mOverlay = imageView;
        this.mIconFrame.addView(imageView, -2, -2);
        return this.mIconFrame;
    }

    protected SlashImageView createSlashImageView(Context context) {
        return new SlashImageView(context);
    }

    @Override // com.android.systemui.qs.tileimpl.QSIconViewImpl, android.view.View
    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        int iMakeMeasureSpec = View.MeasureSpec.makeMeasureSpec(this.mIconFrame.getMeasuredHeight(), 1073741824);
        int iMakeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(this.mIconFrame.getMeasuredHeight(), Integer.MIN_VALUE);
        this.mIn.measure(iMakeMeasureSpec2, iMakeMeasureSpec);
        this.mOut.measure(iMakeMeasureSpec2, iMakeMeasureSpec);
    }

    @Override // com.android.systemui.qs.tileimpl.QSIconViewImpl, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        layoutIndicator(this.mIn);
        layoutIndicator(this.mOut);
    }

    private void layoutIndicator(View view) {
        int right;
        int measuredWidth;
        if (getLayoutDirection() == 1) {
            measuredWidth = getLeft() - this.mSignalIndicatorToIconFrameSpacing;
            right = measuredWidth - view.getMeasuredWidth();
        } else {
            right = this.mSignalIndicatorToIconFrameSpacing + getRight();
            measuredWidth = view.getMeasuredWidth() + right;
        }
        view.layout(right, this.mIconFrame.getBottom() - view.getMeasuredHeight(), measuredWidth, this.mIconFrame.getBottom());
    }

    @Override // com.android.systemui.qs.tileimpl.QSIconViewImpl, com.android.systemui.plugins.qs.QSIconView
    public void setIcon(QSTile.State state, boolean z) throws Resources.NotFoundException {
        QSTile.SignalState signalState = (QSTile.SignalState) state;
        setIcon(this.mSignal, signalState, z);
        boolean z2 = false;
        if (signalState.overlayIconId > 0) {
            this.mOverlay.setVisibility(0);
            this.mOverlay.setImageResource(signalState.overlayIconId);
        } else {
            this.mOverlay.setVisibility(8);
        }
        if (signalState.overlayIconId > 0 && signalState.isOverlayIconWide) {
            this.mSignal.setPaddingRelative(this.mWideOverlayIconStartPadding, 0, 0, 0);
        } else {
            this.mSignal.setPaddingRelative(0, 0, 0, 0);
        }
        if (z && isShown()) {
            z2 = true;
        }
        setVisibility(this.mIn, z2, signalState.activityIn);
        setVisibility(this.mOut, z2, signalState.activityOut);
    }

    private void setVisibility(View view, boolean z, boolean z2) {
        float f = (z && z2) ? 1.0f : 0.0f;
        if (view.getAlpha() == f) {
            return;
        }
        if (z) {
            view.animate().setDuration(z2 ? SHORT_DURATION : DEFAULT_DURATION).alpha(f).start();
        } else {
            view.setAlpha(f);
        }
    }
}
