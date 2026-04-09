package com.android.systemui.statusbar.notification;

import android.graphics.drawable.Icon;
import android.util.Pools;
import android.view.View;
import android.widget.ImageView;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.TransformableView;
import com.android.systemui.statusbar.notification.TransformState;
import com.android.systemui.statusbar.notification.row.HybridNotificationView;

/* loaded from: classes.dex */
public class ImageTransformState extends TransformState {
    public static final int ICON_TAG = R.id.image_icon_tag;
    private static Pools.SimplePool<ImageTransformState> sInstancePool = new Pools.SimplePool<>(40);
    private Icon mIcon;

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void initFrom(View view, TransformState.TransformInfo transformInfo) {
        super.initFrom(view, transformInfo);
        if (view instanceof ImageView) {
            this.mIcon = (Icon) view.getTag(ICON_TAG);
        }
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    protected boolean sameAs(TransformState transformState) {
        if (super.sameAs(transformState)) {
            return true;
        }
        if (!(transformState instanceof ImageTransformState)) {
            return false;
        }
        Icon icon = ((ImageTransformState) transformState).mIcon;
        Icon icon2 = this.mIcon;
        if (icon2 != icon) {
            return (icon2 == null || icon == null || !icon2.sameAs(icon)) ? false : true;
        }
        return true;
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void appear(float f, TransformableView transformableView) {
        if (transformableView instanceof HybridNotificationView) {
            if (f == 0.0f) {
                this.mTransformedView.setPivotY(0.0f);
                this.mTransformedView.setPivotX(r3.getWidth() / 2);
                prepareFadeIn();
            }
            float fMapToDuration = mapToDuration(f);
            CrossFadeHelper.fadeIn(this.mTransformedView, fMapToDuration, false);
            float interpolation = Interpolators.LINEAR_OUT_SLOW_IN.getInterpolation(fMapToDuration);
            this.mTransformedView.setScaleX(interpolation);
            this.mTransformedView.setScaleY(interpolation);
            return;
        }
        super.appear(f, transformableView);
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void disappear(float f, TransformableView transformableView) {
        if (transformableView instanceof HybridNotificationView) {
            if (f == 0.0f) {
                this.mTransformedView.setPivotY(0.0f);
                this.mTransformedView.setPivotX(r4.getWidth() / 2);
            }
            float fMapToDuration = mapToDuration(1.0f - f);
            CrossFadeHelper.fadeOut(this.mTransformedView, 1.0f - fMapToDuration, false);
            float interpolation = Interpolators.LINEAR_OUT_SLOW_IN.getInterpolation(fMapToDuration);
            this.mTransformedView.setScaleX(interpolation);
            this.mTransformedView.setScaleY(interpolation);
            return;
        }
        super.disappear(f, transformableView);
    }

    private static float mapToDuration(float f) {
        return Math.max(Math.min(((f * 360.0f) - 150.0f) / 210.0f, 1.0f), 0.0f);
    }

    public static ImageTransformState obtain() {
        ImageTransformState imageTransformState = (ImageTransformState) sInstancePool.acquire();
        return imageTransformState != null ? imageTransformState : new ImageTransformState();
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void recycle() {
        super.recycle();
        if (getClass() == ImageTransformState.class) {
            sInstancePool.release(this);
        }
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    protected void reset() {
        super.reset();
        this.mIcon = null;
    }
}
