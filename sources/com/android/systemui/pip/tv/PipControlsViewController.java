package com.android.systemui.pip.tv;

import android.app.PendingIntent;
import android.app.RemoteAction;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.R;
import com.android.systemui.pip.tv.PipManager;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class PipControlsViewController {
    private static final String TAG = "PipControlsViewController";
    private final View.OnFocusChangeListener mFocusChangeListener;
    private PipControlButtonView mFocusedChild;
    private final Handler mHandler;
    private final LayoutInflater mLayoutInflater;
    private Listener mListener;
    private MediaController mMediaController;
    private final PipManager mPipManager;
    private final PipControlButtonView mPlayPauseButtonView;
    private final PipControlsView mView;
    private ArrayList<PipControlButtonView> mCustomButtonViews = new ArrayList<>();
    private List<RemoteAction> mCustomActions = new ArrayList();
    private View.OnAttachStateChangeListener mOnAttachStateChangeListener = new View.OnAttachStateChangeListener() { // from class: com.android.systemui.pip.tv.PipControlsViewController.1
        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewAttachedToWindow(View view) {
            PipControlsViewController.this.updateMediaController();
            PipControlsViewController.this.mPipManager.addMediaListener(PipControlsViewController.this.mPipMediaListener);
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewDetachedFromWindow(View view) {
            PipControlsViewController.this.mPipManager.removeMediaListener(PipControlsViewController.this.mPipMediaListener);
        }
    };
    private MediaController.Callback mMediaControllerCallback = new MediaController.Callback() { // from class: com.android.systemui.pip.tv.PipControlsViewController.2
        @Override // android.media.session.MediaController.Callback
        public void onPlaybackStateChanged(PlaybackState playbackState) {
            PipControlsViewController.this.updateUserActions();
        }
    };
    private final PipManager.MediaListener mPipMediaListener = new PipManager.MediaListener() { // from class: com.android.systemui.pip.tv.PipControlsViewController$$ExternalSyntheticLambda5
        @Override // com.android.systemui.pip.tv.PipManager.MediaListener
        public final void onMediaControllerChanged() {
            this.f$0.updateMediaController();
        }
    };

    public interface Listener {
        void onClosed();
    }

    public PipControlsView getView() {
        return this.mView;
    }

    public PipControlsViewController(PipControlsView pipControlsView, PipManager pipManager, LayoutInflater layoutInflater, Handler handler) {
        View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() { // from class: com.android.systemui.pip.tv.PipControlsViewController.3
            @Override // android.view.View.OnFocusChangeListener
            public void onFocusChange(View view, boolean z) {
                if (z) {
                    PipControlsViewController.this.mFocusedChild = (PipControlButtonView) view;
                } else if (PipControlsViewController.this.mFocusedChild == view) {
                    PipControlsViewController.this.mFocusedChild = null;
                }
            }
        };
        this.mFocusChangeListener = onFocusChangeListener;
        this.mView = pipControlsView;
        this.mPipManager = pipManager;
        this.mLayoutInflater = layoutInflater;
        this.mHandler = handler;
        pipControlsView.addOnAttachStateChangeListener(this.mOnAttachStateChangeListener);
        if (pipControlsView.isAttachedToWindow()) {
            this.mOnAttachStateChangeListener.onViewAttachedToWindow(pipControlsView);
        }
        PipControlButtonView fullButtonView = pipControlsView.getFullButtonView();
        fullButtonView.setOnFocusChangeListener(onFocusChangeListener);
        fullButtonView.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.pip.tv.PipControlsViewController$$ExternalSyntheticLambda4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$new$0(view);
            }
        });
        PipControlButtonView closeButtonView = pipControlsView.getCloseButtonView();
        closeButtonView.setOnFocusChangeListener(onFocusChangeListener);
        closeButtonView.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.pip.tv.PipControlsViewController$$ExternalSyntheticLambda3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$new$1(view);
            }
        });
        PipControlButtonView playPauseButtonView = pipControlsView.getPlayPauseButtonView();
        this.mPlayPauseButtonView = playPauseButtonView;
        playPauseButtonView.setOnFocusChangeListener(onFocusChangeListener);
        playPauseButtonView.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.pip.tv.PipControlsViewController$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$new$2(view);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0(View view) {
        this.mPipManager.movePipToFullscreen();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$1(View view) {
        this.mPipManager.closePip();
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onClosed();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$2(View view) {
        MediaController mediaController = this.mMediaController;
        if (mediaController == null || mediaController.getPlaybackState() == null) {
            return;
        }
        if (this.mPipManager.getPlaybackState() == 1) {
            this.mMediaController.getTransportControls().play();
        } else if (this.mPipManager.getPlaybackState() == 0) {
            this.mMediaController.getTransportControls().pause();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateMediaController() {
        MediaController mediaController = this.mPipManager.getMediaController();
        MediaController mediaController2 = this.mMediaController;
        if (mediaController2 == mediaController) {
            return;
        }
        if (mediaController2 != null) {
            mediaController2.unregisterCallback(this.mMediaControllerCallback);
        }
        this.mMediaController = mediaController;
        if (mediaController != null) {
            mediaController.registerCallback(this.mMediaControllerCallback);
        }
        updateUserActions();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateUserActions() {
        int i = 0;
        if (!this.mCustomActions.isEmpty()) {
            while (this.mCustomButtonViews.size() < this.mCustomActions.size()) {
                PipControlButtonView pipControlButtonView = (PipControlButtonView) this.mLayoutInflater.inflate(R.layout.tv_pip_custom_control, (ViewGroup) this.mView, false);
                this.mView.addView(pipControlButtonView);
                this.mCustomButtonViews.add(pipControlButtonView);
            }
            int i2 = 0;
            while (i2 < this.mCustomButtonViews.size()) {
                this.mCustomButtonViews.get(i2).setVisibility(i2 < this.mCustomActions.size() ? 0 : 8);
                i2++;
            }
            while (i < this.mCustomActions.size()) {
                final RemoteAction remoteAction = this.mCustomActions.get(i);
                final PipControlButtonView pipControlButtonView2 = this.mCustomButtonViews.get(i);
                remoteAction.getIcon().loadDrawableAsync(this.mView.getContext(), new Icon.OnDrawableLoadedListener() { // from class: com.android.systemui.pip.tv.PipControlsViewController$$ExternalSyntheticLambda0
                    @Override // android.graphics.drawable.Icon.OnDrawableLoadedListener
                    public final void onDrawableLoaded(Drawable drawable) {
                        PipControlsViewController.lambda$updateUserActions$3(pipControlButtonView2, drawable);
                    }
                }, this.mHandler);
                pipControlButtonView2.setText(remoteAction.getContentDescription());
                if (remoteAction.isEnabled()) {
                    pipControlButtonView2.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.pip.tv.PipControlsViewController$$ExternalSyntheticLambda1
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) throws PendingIntent.CanceledException {
                            PipControlsViewController.lambda$updateUserActions$4(remoteAction, view);
                        }
                    });
                }
                pipControlButtonView2.setEnabled(remoteAction.isEnabled());
                pipControlButtonView2.setAlpha(remoteAction.isEnabled() ? 1.0f : 0.54f);
                i++;
            }
            this.mPlayPauseButtonView.setVisibility(8);
            return;
        }
        int playbackState = this.mPipManager.getPlaybackState();
        if (playbackState == 2) {
            this.mPlayPauseButtonView.setVisibility(8);
        } else {
            this.mPlayPauseButtonView.setVisibility(0);
            if (playbackState == 0) {
                this.mPlayPauseButtonView.setImageResource(R.drawable.ic_pause_white);
                this.mPlayPauseButtonView.setText(R.string.pip_pause);
            } else {
                this.mPlayPauseButtonView.setImageResource(R.drawable.ic_play_arrow_white);
                this.mPlayPauseButtonView.setText(R.string.pip_play);
            }
        }
        while (i < this.mCustomButtonViews.size()) {
            this.mCustomButtonViews.get(i).setVisibility(8);
            i++;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$updateUserActions$3(PipControlButtonView pipControlButtonView, Drawable drawable) {
        drawable.setTint(-1);
        pipControlButtonView.setImageDrawable(drawable);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$updateUserActions$4(RemoteAction remoteAction, View view) throws PendingIntent.CanceledException {
        try {
            remoteAction.getActionIntent().send();
        } catch (PendingIntent.CanceledException e) {
            Log.w(TAG, "Failed to send action", e);
        }
    }

    public void setActions(List<RemoteAction> list) {
        this.mCustomActions.clear();
        this.mCustomActions.addAll(list);
        updateUserActions();
    }
}
