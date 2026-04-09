package com.android.systemui.media.dialog;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.android.settingslib.Utils;
import com.android.settingslib.media.MediaDevice;
import com.android.systemui.R;
import com.android.systemui.media.dialog.MediaOutputBaseAdapter;
import java.util.List;

/* loaded from: classes.dex */
public class MediaOutputAdapter extends MediaOutputBaseAdapter {
    private static final boolean DEBUG = Log.isLoggable("MediaOutputAdapter", 3);
    private ViewGroup mConnectedItem;
    private boolean mInclueDynamicGroup;

    public MediaOutputAdapter(MediaOutputController mediaOutputController) {
        super(mediaOutputController);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public MediaOutputBaseAdapter.MediaDeviceBaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        super.onCreateViewHolder(viewGroup, i);
        return new MediaDeviceViewHolder(this.mHolderView);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(MediaOutputBaseAdapter.MediaDeviceBaseViewHolder mediaDeviceBaseViewHolder, int i) {
        int size = this.mController.getMediaDevices().size();
        if (i == size && this.mController.isZeroMode()) {
            mediaDeviceBaseViewHolder.onBind(1, false, true);
            return;
        }
        if (this.mInclueDynamicGroup) {
            if (i == 0) {
                mediaDeviceBaseViewHolder.onBind(3, true, false);
                return;
            } else {
                mediaDeviceBaseViewHolder.onBind((MediaDevice) ((List) this.mController.getMediaDevices()).get(i - 1), false, i == size);
                return;
            }
        }
        if (i < size) {
            mediaDeviceBaseViewHolder.onBind((MediaDevice) ((List) this.mController.getMediaDevices()).get(i), i == 0, i == size - 1);
        } else if (DEBUG) {
            Log.d("MediaOutputAdapter", "Incorrect position: " + i);
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        this.mInclueDynamicGroup = this.mController.getSelectedMediaDevice().size() > 1;
        if (this.mController.isZeroMode() || this.mInclueDynamicGroup) {
            return this.mController.getMediaDevices().size() + 1;
        }
        return this.mController.getMediaDevices().size();
    }

    @Override // com.android.systemui.media.dialog.MediaOutputBaseAdapter
    CharSequence getItemTitle(MediaDevice mediaDevice) {
        if (mediaDevice.getDeviceType() == 4 && !mediaDevice.isConnected()) {
            String name = mediaDevice.getName();
            SpannableString spannableString = new SpannableString(this.mContext.getString(R.string.media_output_dialog_disconnected, name));
            spannableString.setSpan(new ForegroundColorSpan(Utils.getColorAttrDefaultColor(this.mContext, android.R.attr.textColorSecondary)), name.length(), spannableString.length(), 33);
            return spannableString;
        }
        return super.getItemTitle(mediaDevice);
    }

    class MediaDeviceViewHolder extends MediaOutputBaseAdapter.MediaDeviceBaseViewHolder {
        MediaDeviceViewHolder(View view) {
            super(view);
        }

        @Override // com.android.systemui.media.dialog.MediaOutputBaseAdapter.MediaDeviceBaseViewHolder
        void onBind(final MediaDevice mediaDevice, boolean z, boolean z2) {
            super.onBind(mediaDevice, z, z2);
            boolean z3 = !MediaOutputAdapter.this.mInclueDynamicGroup && MediaOutputAdapter.this.isCurrentlyConnected(mediaDevice);
            if (z3) {
                MediaOutputAdapter.this.mConnectedItem = this.mContainerLayout;
            }
            this.mBottomDivider.setVisibility(8);
            this.mCheckBox.setVisibility(8);
            if (z3 && MediaOutputAdapter.this.mController.isActiveRemoteDevice(mediaDevice)) {
                this.mDivider.setVisibility(0);
                this.mDivider.setTransitionAlpha(1.0f);
                this.mAddIcon.setVisibility(0);
                this.mAddIcon.setTransitionAlpha(1.0f);
                this.mAddIcon.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.media.dialog.MediaOutputAdapter$MediaDeviceViewHolder$$ExternalSyntheticLambda0
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        this.f$0.lambda$onBind$0(view);
                    }
                });
            } else {
                this.mDivider.setVisibility(8);
                this.mAddIcon.setVisibility(8);
            }
            if (MediaOutputAdapter.this.mController.isTransferring()) {
                if (mediaDevice.getState() == 1 && !MediaOutputAdapter.this.mController.hasAdjustVolumeUserRestriction()) {
                    setTwoLineLayout(mediaDevice, true, false, true, false);
                    return;
                } else {
                    setSingleLineLayout(MediaOutputAdapter.this.getItemTitle(mediaDevice), false);
                    return;
                }
            }
            if (mediaDevice.getState() == 3) {
                setTwoLineLayout(mediaDevice, false, false, false, true);
                this.mSubTitleText.setText(R.string.media_output_dialog_connect_failed);
                this.mContainerLayout.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.media.dialog.MediaOutputAdapter$MediaDeviceViewHolder$$ExternalSyntheticLambda3
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        this.f$0.lambda$onBind$1(mediaDevice, view);
                    }
                });
            } else if (!MediaOutputAdapter.this.mController.hasAdjustVolumeUserRestriction() && z3) {
                setTwoLineLayout(mediaDevice, true, true, false, false);
                initSeekbar(mediaDevice);
            } else {
                setSingleLineLayout(MediaOutputAdapter.this.getItemTitle(mediaDevice), false);
                this.mContainerLayout.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.media.dialog.MediaOutputAdapter$MediaDeviceViewHolder$$ExternalSyntheticLambda4
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        this.f$0.lambda$onBind$2(mediaDevice, view);
                    }
                });
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onBind$0(View view) {
            onEndItemClick();
        }

        @Override // com.android.systemui.media.dialog.MediaOutputBaseAdapter.MediaDeviceBaseViewHolder
        void onBind(int i, boolean z, boolean z2) {
            super.onBind(i, z, z2);
            if (i == 1) {
                this.mCheckBox.setVisibility(8);
                this.mDivider.setVisibility(8);
                this.mAddIcon.setVisibility(8);
                this.mBottomDivider.setVisibility(8);
                setSingleLineLayout(MediaOutputAdapter.this.mContext.getText(R.string.media_output_dialog_pairing_new), false);
                Drawable drawable = MediaOutputAdapter.this.mContext.getDrawable(R.drawable.ic_add);
                drawable.setColorFilter(new PorterDuffColorFilter(Utils.getColorAccentDefaultColor(MediaOutputAdapter.this.mContext), PorterDuff.Mode.SRC_IN));
                this.mTitleIcon.setImageDrawable(drawable);
                this.mContainerLayout.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.media.dialog.MediaOutputAdapter$MediaDeviceViewHolder$$ExternalSyntheticLambda1
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        this.f$0.lambda$onBind$3(view);
                    }
                });
                return;
            }
            if (i == 3) {
                MediaOutputAdapter.this.mConnectedItem = this.mContainerLayout;
                this.mBottomDivider.setVisibility(8);
                this.mCheckBox.setVisibility(8);
                this.mDivider.setVisibility(0);
                this.mDivider.setTransitionAlpha(1.0f);
                this.mAddIcon.setVisibility(0);
                this.mAddIcon.setTransitionAlpha(1.0f);
                this.mAddIcon.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.media.dialog.MediaOutputAdapter$MediaDeviceViewHolder$$ExternalSyntheticLambda2
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        this.f$0.lambda$onBind$4(view);
                    }
                });
                this.mTitleIcon.setImageDrawable(getSpeakerDrawable());
                CharSequence sessionName = MediaOutputAdapter.this.mController.getSessionName();
                if (TextUtils.isEmpty(sessionName)) {
                    sessionName = MediaOutputAdapter.this.mContext.getString(R.string.media_output_dialog_group);
                }
                setTwoLineLayout(sessionName, true, true, false, false);
                initSessionSeekbar();
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onBind$3(View view) {
            onItemClick(1);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onBind$4(View view) {
            onEndItemClick();
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* renamed from: onItemClick, reason: merged with bridge method [inline-methods] and merged with bridge method [inline-methods] */
        public void lambda$onBind$2(View view, MediaDevice mediaDevice) {
            if (MediaOutputAdapter.this.mController.isTransferring()) {
                return;
            }
            playSwitchingAnim(MediaOutputAdapter.this.mConnectedItem, view);
            MediaOutputAdapter.this.mController.connectDevice(mediaDevice);
            mediaDevice.setState(1);
            if (MediaOutputAdapter.this.isAnimating()) {
                return;
            }
            MediaOutputAdapter.this.notifyDataSetChanged();
        }

        private void onItemClick(int i) {
            if (i == 1) {
                MediaOutputAdapter.this.mController.launchBluetoothPairing();
            }
        }

        private void onEndItemClick() {
            MediaOutputAdapter.this.mController.launchMediaOutputGroupDialog();
        }
    }
}
