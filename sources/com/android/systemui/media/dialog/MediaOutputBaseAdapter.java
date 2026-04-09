package com.android.systemui.media.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.android.settingslib.bluetooth.BluetoothUtils;
import com.android.settingslib.media.MediaDevice;
import com.android.systemui.Interpolators;
import com.android.systemui.R;

/* loaded from: classes.dex */
public abstract class MediaOutputBaseAdapter extends RecyclerView.Adapter<MediaDeviceBaseViewHolder> {
    Context mContext;
    final MediaOutputController mController;
    View mHolderView;
    private boolean mIsAnimating;
    boolean mIsDragging = false;
    private int mMargin;

    public MediaOutputBaseAdapter(MediaOutputController mediaOutputController) {
        this.mController = mediaOutputController;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public MediaDeviceBaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        this.mContext = context;
        this.mMargin = context.getResources().getDimensionPixelSize(R.dimen.media_output_dialog_list_margin);
        this.mHolderView = LayoutInflater.from(this.mContext).inflate(R.layout.media_output_list_item, viewGroup, false);
        return null;
    }

    CharSequence getItemTitle(MediaDevice mediaDevice) {
        return mediaDevice.getName();
    }

    boolean isCurrentlyConnected(MediaDevice mediaDevice) {
        return TextUtils.equals(mediaDevice.getId(), this.mController.getCurrentConnectedMediaDevice().getId());
    }

    boolean isDragging() {
        return this.mIsDragging;
    }

    boolean isAnimating() {
        return this.mIsAnimating;
    }

    abstract class MediaDeviceBaseViewHolder extends RecyclerView.ViewHolder {
        final ImageView mAddIcon;
        final View mBottomDivider;
        final CheckBox mCheckBox;
        final LinearLayout mContainerLayout;
        final View mDivider;
        final ProgressBar mProgressBar;
        final SeekBar mSeekBar;
        final TextView mSubTitleText;
        final ImageView mTitleIcon;
        final TextView mTitleText;
        final RelativeLayout mTwoLineLayout;
        final TextView mTwoLineTitleText;

        MediaDeviceBaseViewHolder(View view) {
            super(view);
            this.mContainerLayout = (LinearLayout) view.requireViewById(R.id.device_container);
            this.mTitleText = (TextView) view.requireViewById(R.id.title);
            this.mSubTitleText = (TextView) view.requireViewById(R.id.subtitle);
            this.mTwoLineLayout = (RelativeLayout) view.requireViewById(R.id.two_line_layout);
            this.mTwoLineTitleText = (TextView) view.requireViewById(R.id.two_line_title);
            this.mTitleIcon = (ImageView) view.requireViewById(R.id.title_icon);
            this.mProgressBar = (ProgressBar) view.requireViewById(R.id.volume_indeterminate_progress);
            this.mSeekBar = (SeekBar) view.requireViewById(R.id.volume_seekbar);
            this.mDivider = view.requireViewById(R.id.end_divider);
            this.mBottomDivider = view.requireViewById(R.id.bottom_divider);
            this.mAddIcon = (ImageView) view.requireViewById(R.id.add_icon);
            this.mCheckBox = (CheckBox) view.requireViewById(R.id.check_box);
        }

        void onBind(MediaDevice mediaDevice, boolean z, boolean z2) {
            this.mTitleIcon.setImageIcon(MediaOutputBaseAdapter.this.mController.getDeviceIconCompat(mediaDevice).toIcon(MediaOutputBaseAdapter.this.mContext));
            setMargin(z, z2);
        }

        void onBind(int i, boolean z, boolean z2) {
            setMargin(z, z2);
        }

        private void setMargin(boolean z, boolean z2) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mContainerLayout.getLayoutParams();
            marginLayoutParams.topMargin = z ? MediaOutputBaseAdapter.this.mMargin : 0;
            marginLayoutParams.bottomMargin = z2 ? MediaOutputBaseAdapter.this.mMargin : 0;
            this.mContainerLayout.setLayoutParams(marginLayoutParams);
        }

        void setSingleLineLayout(CharSequence charSequence, boolean z) {
            this.mTwoLineLayout.setVisibility(8);
            this.mProgressBar.setVisibility(8);
            this.mTitleText.setVisibility(0);
            this.mTitleText.setTranslationY(0.0f);
            this.mTitleText.setText(charSequence);
            if (z) {
                this.mTitleText.setTypeface(Typeface.create(MediaOutputBaseAdapter.this.mContext.getString(android.R.string.config_defaultCredentialManagerHybridService), 0));
            } else {
                this.mTitleText.setTypeface(Typeface.create(MediaOutputBaseAdapter.this.mContext.getString(android.R.string.config_defaultCredentialManagerAutofillService), 0));
            }
        }

        void setTwoLineLayout(MediaDevice mediaDevice, boolean z, boolean z2, boolean z3, boolean z4) {
            setTwoLineLayout(mediaDevice, null, z, z2, z3, z4);
        }

        void setTwoLineLayout(CharSequence charSequence, boolean z, boolean z2, boolean z3, boolean z4) {
            setTwoLineLayout(null, charSequence, z, z2, z3, z4);
        }

        private void setTwoLineLayout(MediaDevice mediaDevice, CharSequence charSequence, boolean z, boolean z2, boolean z3, boolean z4) {
            this.mTitleText.setVisibility(8);
            this.mTwoLineLayout.setVisibility(0);
            this.mSeekBar.setAlpha(1.0f);
            this.mSeekBar.setVisibility(z2 ? 0 : 8);
            this.mProgressBar.setVisibility(z3 ? 0 : 8);
            this.mSubTitleText.setVisibility(z4 ? 0 : 8);
            this.mTwoLineTitleText.setTranslationY(0.0f);
            if (mediaDevice == null) {
                this.mTwoLineTitleText.setText(charSequence);
            } else {
                this.mTwoLineTitleText.setText(MediaOutputBaseAdapter.this.getItemTitle(mediaDevice));
            }
            if (z) {
                this.mTwoLineTitleText.setTypeface(Typeface.create(MediaOutputBaseAdapter.this.mContext.getString(android.R.string.config_defaultCredentialManagerHybridService), 0));
            } else {
                this.mTwoLineTitleText.setTypeface(Typeface.create(MediaOutputBaseAdapter.this.mContext.getString(android.R.string.config_defaultCredentialManagerAutofillService), 0));
            }
        }

        void initSeekbar(final MediaDevice mediaDevice) {
            this.mSeekBar.setMax(mediaDevice.getMaxVolume());
            this.mSeekBar.setMin(0);
            int currentVolume = mediaDevice.getCurrentVolume();
            if (this.mSeekBar.getProgress() != currentVolume) {
                this.mSeekBar.setProgress(currentVolume);
            }
            this.mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { // from class: com.android.systemui.media.dialog.MediaOutputBaseAdapter.MediaDeviceBaseViewHolder.1
                @Override // android.widget.SeekBar.OnSeekBarChangeListener
                public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                    MediaDevice mediaDevice2 = mediaDevice;
                    if (mediaDevice2 == null || !z) {
                        return;
                    }
                    MediaOutputBaseAdapter.this.mController.adjustVolume(mediaDevice2, i);
                }

                @Override // android.widget.SeekBar.OnSeekBarChangeListener
                public void onStartTrackingTouch(SeekBar seekBar) {
                    MediaOutputBaseAdapter.this.mIsDragging = true;
                }

                @Override // android.widget.SeekBar.OnSeekBarChangeListener
                public void onStopTrackingTouch(SeekBar seekBar) {
                    MediaOutputBaseAdapter.this.mIsDragging = false;
                }
            });
        }

        void initSessionSeekbar() {
            this.mSeekBar.setMax(MediaOutputBaseAdapter.this.mController.getSessionVolumeMax());
            this.mSeekBar.setMin(0);
            int sessionVolume = MediaOutputBaseAdapter.this.mController.getSessionVolume();
            if (this.mSeekBar.getProgress() != sessionVolume) {
                this.mSeekBar.setProgress(sessionVolume);
            }
            this.mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { // from class: com.android.systemui.media.dialog.MediaOutputBaseAdapter.MediaDeviceBaseViewHolder.2
                @Override // android.widget.SeekBar.OnSeekBarChangeListener
                public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                    if (z) {
                        MediaOutputBaseAdapter.this.mController.adjustSessionVolume(i);
                    }
                }

                @Override // android.widget.SeekBar.OnSeekBarChangeListener
                public void onStartTrackingTouch(SeekBar seekBar) {
                    MediaOutputBaseAdapter.this.mIsDragging = true;
                }

                @Override // android.widget.SeekBar.OnSeekBarChangeListener
                public void onStopTrackingTouch(SeekBar seekBar) {
                    MediaOutputBaseAdapter.this.mIsDragging = false;
                }
            });
        }

        void playSwitchingAnim(final View view, final View view2) {
            final float dimensionPixelSize = MediaOutputBaseAdapter.this.mContext.getResources().getDimensionPixelSize(R.dimen.media_output_dialog_title_anim_y_delta);
            SeekBar seekBar = (SeekBar) view.requireViewById(R.id.volume_seekbar);
            TextView textView = (TextView) view2.requireViewById(R.id.title);
            if (seekBar.getVisibility() == 0 && textView.getVisibility() == 0) {
                MediaOutputBaseAdapter.this.mIsAnimating = true;
                textView.setTypeface(Typeface.create(MediaOutputBaseAdapter.this.mContext.getString(android.R.string.config_defaultCredentialManagerHybridService), 0));
                textView.animate().setDuration(200L).translationY(-dimensionPixelSize).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.media.dialog.MediaOutputBaseAdapter.MediaDeviceBaseViewHolder.3
                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator) {
                        view2.requireViewById(R.id.volume_indeterminate_progress).setVisibility(0);
                    }
                });
                seekBar.animate().alpha(0.0f).setDuration(200L).setListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.media.dialog.MediaOutputBaseAdapter.MediaDeviceBaseViewHolder.4
                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator) {
                        TextView textView2 = (TextView) view.requireViewById(R.id.two_line_title);
                        textView2.setTypeface(Typeface.create(MediaOutputBaseAdapter.this.mContext.getString(android.R.string.config_defaultCredentialManagerAutofillService), 0));
                        textView2.animate().setDuration(200L).translationY(dimensionPixelSize).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.media.dialog.MediaOutputBaseAdapter.MediaDeviceBaseViewHolder.4.1
                            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                            public void onAnimationEnd(Animator animator2) {
                                MediaOutputBaseAdapter.this.mIsAnimating = false;
                                MediaOutputBaseAdapter.this.notifyDataSetChanged();
                            }
                        });
                    }
                });
            }
        }

        Drawable getSpeakerDrawable() {
            Drawable drawableMutate = MediaOutputBaseAdapter.this.mContext.getDrawable(R.drawable.ic_speaker_group_black_24dp).mutate();
            drawableMutate.setColorFilter(new PorterDuffColorFilter(MediaOutputBaseAdapter.this.mContext.getResources().getColorStateList(R.color.advanced_icon_color, MediaOutputBaseAdapter.this.mContext.getTheme()).getDefaultColor(), PorterDuff.Mode.SRC_IN));
            return BluetoothUtils.buildAdvancedDrawable(MediaOutputBaseAdapter.this.mContext, drawableMutate);
        }
    }
}
