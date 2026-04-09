package com.android.systemui.pip.tv;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.content.res.Resources;
import android.os.Bundle;
import com.android.systemui.R;
import com.android.systemui.pip.tv.PipManager;
import com.android.systemui.pip.tv.dagger.TvPipComponent;
import java.util.Collections;

/* loaded from: classes.dex */
public class PipMenuActivity extends Activity implements PipManager.Listener {
    private Animator mFadeInAnimation;
    private Animator mFadeOutAnimation;
    private final TvPipComponent.Builder mPipComponentBuilder;
    private PipControlsViewController mPipControlsViewController;
    private final PipManager mPipManager;
    private boolean mRestorePipSizeWhenClose;
    private TvPipComponent mTvPipComponent;

    @Override // com.android.systemui.pip.tv.PipManager.Listener
    public void onPipEntered(String str) {
    }

    @Override // com.android.systemui.pip.tv.PipManager.Listener
    public void onShowPipMenu() {
    }

    public PipMenuActivity(TvPipComponent.Builder builder, PipManager pipManager) {
        this.mPipComponentBuilder = builder;
        this.mPipManager = pipManager;
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) throws Resources.NotFoundException {
        super.onCreate(bundle);
        if (!this.mPipManager.isPipShown()) {
            finish();
        }
        setContentView(R.layout.tv_pip_menu);
        TvPipComponent tvPipComponentBuild = this.mPipComponentBuilder.pipControlsView((PipControlsView) findViewById(R.id.pip_controls)).build();
        this.mTvPipComponent = tvPipComponentBuild;
        this.mPipControlsViewController = tvPipComponentBuild.getPipControlsViewController();
        this.mPipManager.addListener(this);
        this.mRestorePipSizeWhenClose = true;
        Animator animatorLoadAnimator = AnimatorInflater.loadAnimator(this, R.anim.tv_pip_menu_fade_in_animation);
        this.mFadeInAnimation = animatorLoadAnimator;
        animatorLoadAnimator.setTarget(this.mPipControlsViewController.getView());
        Animator animatorLoadAnimator2 = AnimatorInflater.loadAnimator(this, R.anim.tv_pip_menu_fade_out_animation);
        this.mFadeOutAnimation = animatorLoadAnimator2;
        animatorLoadAnimator2.setTarget(this.mPipControlsViewController.getView());
        onPipMenuActionsChanged((ParceledListSlice) getIntent().getParcelableExtra("custom_actions"));
    }

    @Override // android.app.Activity
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        onPipMenuActionsChanged((ParceledListSlice) getIntent().getParcelableExtra("custom_actions"));
    }

    private void restorePipAndFinish() {
        if (this.mRestorePipSizeWhenClose) {
            this.mPipManager.resizePinnedStack(1);
        }
        finish();
    }

    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        this.mFadeInAnimation.start();
    }

    @Override // android.app.Activity
    public void onPause() {
        super.onPause();
        this.mFadeOutAnimation.start();
        restorePipAndFinish();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        this.mPipManager.removeListener(this);
        this.mPipManager.resumePipResizing(1);
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        restorePipAndFinish();
    }

    @Override // com.android.systemui.pip.tv.PipManager.Listener
    public void onPipActivityClosed() {
        finish();
    }

    @Override // com.android.systemui.pip.tv.PipManager.Listener
    public void onPipMenuActionsChanged(ParceledListSlice parceledListSlice) {
        this.mPipControlsViewController.setActions(parceledListSlice != null && !parceledListSlice.getList().isEmpty() ? parceledListSlice.getList() : Collections.EMPTY_LIST);
    }

    @Override // com.android.systemui.pip.tv.PipManager.Listener
    public void onMoveToFullscreen() {
        this.mRestorePipSizeWhenClose = false;
        finish();
    }

    @Override // com.android.systemui.pip.tv.PipManager.Listener
    public void onPipResizeAboutToStart() {
        finish();
        this.mPipManager.suspendPipResizing(1);
    }

    @Override // android.app.Activity
    public void finish() {
        super.finish();
    }
}
