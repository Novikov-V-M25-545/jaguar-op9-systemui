package com.android.systemui.biometrics;

import android.R;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

/* loaded from: classes.dex */
public class FODAnimation extends ImageView {
    private String[] ANIMATION_STYLES_NAMES;
    private final WindowManager.LayoutParams mAnimParams;
    private int mAnimationOffset;
    private int mAnimationSize;
    private Context mContext;
    private final String mFodAnimationPackage;
    private boolean mIsKeyguard;
    private boolean mShowing;
    private WindowManager mWindowManager;
    private AnimationDrawable recognizingAnim;

    public FODAnimation(Context context, int i, int i2) throws Resources.NotFoundException {
        super(context);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        this.mAnimParams = layoutParams;
        this.mShowing = false;
        this.ANIMATION_STYLES_NAMES = new String[]{"fod_miui_normal_recognizing_anim", "fod_miui_aod_recognizing_anim", "fod_miui_aurora_recognizing_anim", "fod_miui_aurora_cas_recognizing_anim", "fod_miui_light_recognizing_anim", "fod_miui_pop_recognizing_anim", "fod_miui_pulse_recognizing_anim", "fod_miui_pulse_recognizing_white_anim", "fod_miui_rhythm_recognizing_anim", "fod_miui_star_cas_recognizing_anim", "fod_op_cosmos_recognizing_anim", "fod_op_energy_recognizing_anim", "fod_op_mclaren_recognizing_anim", "fod_op_ripple_recognizing_anim", "fod_op_scanning_recognizing_anim", "fod_op_stripe_recognizing_anim", "fod_op_wave_recognizing_anim", "fod_pureview_dna_recognizing_anim", "fod_pureview_future_recognizing_anim", "fod_pureview_halo_ring_recognizing_anim", "fod_pureview_molecular_recognizing_anim", "fod_rog_fusion_recognizing_anim", "fod_rog_pulsar_recognizing_anim", "fod_rog_supernova_recognizing_anim", "fod_recog_animation_shine", "fod_recog_animation_smoke", "fod_recog_animation_strings", "fod_recog_animation_quantum"};
        this.mContext = context;
        this.mWindowManager = (WindowManager) context.getSystemService(WindowManager.class);
        this.mFodAnimationPackage = this.mContext.getResources().getString(R.string.config_defaultContentProtectionService);
        this.mAnimationSize = this.mContext.getResources().getDimensionPixelSize(com.android.systemui.R.dimen.fod_animation_size);
        int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(com.android.systemui.R.dimen.fod_animation_offset);
        this.mAnimationOffset = dimensionPixelSize;
        int i3 = this.mAnimationSize;
        layoutParams.height = i3;
        layoutParams.width = i3;
        layoutParams.format = -3;
        layoutParams.type = 2020;
        layoutParams.flags = 552;
        layoutParams.gravity = 49;
        layoutParams.y = (i2 - (i3 / 2)) + dimensionPixelSize;
        setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }

    private void updateAnimationStyle(String str) throws PackageManager.NameNotFoundException {
        Log.i("FODAnimations", "Updating animation style to:" + str);
        try {
            Resources resourcesForApplication = this.mContext.getPackageManager().getResourcesForApplication(this.mFodAnimationPackage);
            int identifier = resourcesForApplication.getIdentifier(str, "drawable", this.mFodAnimationPackage);
            Log.i("FODAnimations", "Got resource id: " + identifier + " from package");
            setBackgroundDrawable(resourcesForApplication.getDrawable(identifier));
            this.recognizingAnim = (AnimationDrawable) getBackground();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void update(boolean z, int i) throws PackageManager.NameNotFoundException {
        if (z) {
            setAlpha(1.0f);
        } else {
            setAlpha(0.0f);
        }
        updateAnimationStyle(this.ANIMATION_STYLES_NAMES[i]);
    }

    public void updateParams(int i) {
        this.mAnimParams.y = (i - (this.mAnimationSize / 2)) + this.mAnimationOffset;
    }

    public void setAnimationKeyguard(boolean z) {
        this.mIsKeyguard = z;
    }

    public void showFODanimation() {
        if (this.mAnimParams == null || this.mShowing || !this.mIsKeyguard) {
            return;
        }
        this.mShowing = true;
        if (getWindowToken() == null) {
            this.mWindowManager.addView(this, this.mAnimParams);
        } else {
            this.mWindowManager.updateViewLayout(this, this.mAnimParams);
        }
        AnimationDrawable animationDrawable = this.recognizingAnim;
        if (animationDrawable != null) {
            animationDrawable.start();
        }
    }

    public void hideFODanimation() {
        if (this.mShowing) {
            this.mShowing = false;
            if (this.recognizingAnim != null) {
                clearAnimation();
                this.recognizingAnim.stop();
                this.recognizingAnim.selectDrawable(0);
            }
            if (getWindowToken() != null) {
                this.mWindowManager.removeView(this);
            }
        }
    }
}
