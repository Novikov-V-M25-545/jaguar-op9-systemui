package com.android.systemui.media.dialog;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import com.android.settingslib.media.MediaDevice;
import com.android.systemui.R;
import com.android.systemui.media.dialog.MediaOutputBaseAdapter;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class MediaOutputGroupAdapter extends MediaOutputBaseAdapter {
    private static final boolean DEBUG = Log.isLoggable("MediaOutputGroupAdapter", 3);
    private final List<MediaDevice> mGroupMediaDevices;

    public MediaOutputGroupAdapter(MediaOutputController mediaOutputController) {
        super(mediaOutputController);
        this.mGroupMediaDevices = mediaOutputController.getGroupMediaDevices();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public MediaOutputBaseAdapter.MediaDeviceBaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        super.onCreateViewHolder(viewGroup, i);
        return new GroupViewHolder(this.mHolderView);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(MediaOutputBaseAdapter.MediaDeviceBaseViewHolder mediaDeviceBaseViewHolder, int i) {
        if (i == 0) {
            mediaDeviceBaseViewHolder.onBind(2, true, false);
            return;
        }
        int i2 = i - 1;
        int size = this.mGroupMediaDevices.size();
        if (i2 < size) {
            mediaDeviceBaseViewHolder.onBind(this.mGroupMediaDevices.get(i2), false, i2 == size - 1);
        } else if (DEBUG) {
            Log.d("MediaOutputGroupAdapter", "Incorrect position: " + i);
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.mGroupMediaDevices.size() + 1;
    }

    @Override // com.android.systemui.media.dialog.MediaOutputBaseAdapter
    CharSequence getItemTitle(MediaDevice mediaDevice) {
        return super.getItemTitle(mediaDevice);
    }

    class GroupViewHolder extends MediaOutputBaseAdapter.MediaDeviceBaseViewHolder {
        GroupViewHolder(View view) {
            super(view);
        }

        @Override // com.android.systemui.media.dialog.MediaOutputBaseAdapter.MediaDeviceBaseViewHolder
        void onBind(final MediaDevice mediaDevice, boolean z, boolean z2) {
            super.onBind(mediaDevice, z, z2);
            this.mDivider.setVisibility(8);
            this.mAddIcon.setVisibility(8);
            this.mBottomDivider.setVisibility(8);
            this.mCheckBox.setVisibility(0);
            this.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.android.systemui.media.dialog.MediaOutputGroupAdapter$GroupViewHolder$$ExternalSyntheticLambda0
                @Override // android.widget.CompoundButton.OnCheckedChangeListener
                public final void onCheckedChanged(CompoundButton compoundButton, boolean z3) {
                    this.f$0.lambda$onBind$0(mediaDevice, compoundButton, z3);
                }
            });
            setTwoLineLayout(mediaDevice, false, true, false, false);
            initSeekbar(mediaDevice);
            List<MediaDevice> selectedMediaDevice = MediaOutputGroupAdapter.this.mController.getSelectedMediaDevice();
            if (isDeviceIncluded(MediaOutputGroupAdapter.this.mController.getSelectableMediaDevice(), mediaDevice)) {
                this.mCheckBox.setButtonDrawable(R.drawable.ic_check_box);
                this.mCheckBox.setChecked(false);
                this.mCheckBox.setEnabled(true);
            } else if (isDeviceIncluded(selectedMediaDevice, mediaDevice)) {
                if (selectedMediaDevice.size() == 1 || !isDeviceIncluded(MediaOutputGroupAdapter.this.mController.getDeselectableMediaDevice(), mediaDevice)) {
                    this.mCheckBox.setButtonDrawable(getDisabledCheckboxDrawable());
                    this.mCheckBox.setChecked(true);
                    this.mCheckBox.setEnabled(false);
                } else {
                    this.mCheckBox.setButtonDrawable(R.drawable.ic_check_box);
                    this.mCheckBox.setChecked(true);
                    this.mCheckBox.setEnabled(true);
                }
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onBind$0(MediaDevice mediaDevice, CompoundButton compoundButton, boolean z) {
            onCheckBoxClicked(z, mediaDevice);
        }

        @Override // com.android.systemui.media.dialog.MediaOutputBaseAdapter.MediaDeviceBaseViewHolder
        void onBind(int i, boolean z, boolean z2) {
            super.onBind(i, z, z2);
            if (i == 2) {
                setTwoLineLayout(MediaOutputGroupAdapter.this.mContext.getText(R.string.media_output_dialog_group), true, true, false, false);
                this.mTitleIcon.setImageDrawable(getSpeakerDrawable());
                this.mBottomDivider.setVisibility(0);
                this.mCheckBox.setVisibility(8);
                this.mDivider.setVisibility(8);
                this.mAddIcon.setVisibility(8);
                initSessionSeekbar();
            }
        }

        private void onCheckBoxClicked(boolean z, MediaDevice mediaDevice) {
            if (z && isDeviceIncluded(MediaOutputGroupAdapter.this.mController.getSelectableMediaDevice(), mediaDevice)) {
                MediaOutputGroupAdapter.this.mController.addDeviceToPlayMedia(mediaDevice);
            } else {
                if (z || !isDeviceIncluded(MediaOutputGroupAdapter.this.mController.getDeselectableMediaDevice(), mediaDevice)) {
                    return;
                }
                MediaOutputGroupAdapter.this.mController.removeDeviceFromPlayMedia(mediaDevice);
            }
        }

        private Drawable getDisabledCheckboxDrawable() {
            Drawable drawableMutate = MediaOutputGroupAdapter.this.mContext.getDrawable(R.drawable.ic_check_box_blue_24dp).mutate();
            Canvas canvas = new Canvas(Bitmap.createBitmap(drawableMutate.getIntrinsicWidth(), drawableMutate.getIntrinsicHeight(), Bitmap.Config.ARGB_8888));
            TypedValue typedValue = new TypedValue();
            MediaOutputGroupAdapter.this.mContext.getTheme().resolveAttribute(android.R.attr.disabledAlpha, typedValue, true);
            drawableMutate.setAlpha((int) (typedValue.getFloat() * 255.0f));
            drawableMutate.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawableMutate.draw(canvas);
            return drawableMutate;
        }

        private boolean isDeviceIncluded(List<MediaDevice> list, MediaDevice mediaDevice) {
            Iterator<MediaDevice> it = list.iterator();
            while (it.hasNext()) {
                if (TextUtils.equals(it.next().getId(), mediaDevice.getId())) {
                    return true;
                }
            }
            return false;
        }
    }
}
