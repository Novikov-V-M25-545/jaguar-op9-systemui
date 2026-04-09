package com.android.systemui.statusbar;

import android.R;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.FeatureFlagUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.media.InfoMediaManager;
import com.android.settingslib.media.LocalMediaManager;
import com.android.settingslib.media.MediaDevice;
import com.android.settingslib.widget.AdaptiveIcon;
import com.android.systemui.Dependency;
import com.android.systemui.media.dialog.MediaOutputDialogFactory;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class MediaTransferManager {
    private final Context mContext;
    private MediaDevice mDevice;
    private LocalMediaManager mLocalMediaManager;
    private List<View> mViews = new ArrayList();
    private final View.OnClickListener mOnClickHandler = new View.OnClickListener() { // from class: com.android.systemui.statusbar.MediaTransferManager.1
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            handleMediaTransfer(view);
        }

        private boolean handleMediaTransfer(View view) {
            if (view.findViewById(R.id.input_mode) == null) {
                return false;
            }
            MediaTransferManager.this.mMediaOutputDialogFactory.create(MediaTransferManager.this.getRowForParent(view.getParent()).getEntry().getSbn().getPackageName(), true);
            return true;
        }
    };
    private final LocalMediaManager.DeviceCallback mMediaDeviceCallback = new LocalMediaManager.DeviceCallback() { // from class: com.android.systemui.statusbar.MediaTransferManager.2
        @Override // com.android.settingslib.media.LocalMediaManager.DeviceCallback
        public void onDeviceListUpdate(List<MediaDevice> list) {
            MediaDevice currentConnectedDevice = MediaTransferManager.this.mLocalMediaManager.getCurrentConnectedDevice();
            if (MediaTransferManager.this.mDevice == null || !MediaTransferManager.this.mDevice.equals(currentConnectedDevice)) {
                MediaTransferManager.this.mDevice = currentConnectedDevice;
                MediaTransferManager.this.updateAllChips();
            }
        }

        @Override // com.android.settingslib.media.LocalMediaManager.DeviceCallback
        public void onSelectedDeviceStateChanged(MediaDevice mediaDevice, int i) {
            if (MediaTransferManager.this.mDevice == null || !MediaTransferManager.this.mDevice.equals(mediaDevice)) {
                MediaTransferManager.this.mDevice = mediaDevice;
                MediaTransferManager.this.updateAllChips();
            }
        }
    };
    private final MediaOutputDialogFactory mMediaOutputDialogFactory = (MediaOutputDialogFactory) Dependency.get(MediaOutputDialogFactory.class);

    public MediaTransferManager(Context context) {
        this.mContext = context;
        LocalBluetoothManager localBluetoothManager = (LocalBluetoothManager) Dependency.get(LocalBluetoothManager.class);
        this.mLocalMediaManager = new LocalMediaManager(context, localBluetoothManager, new InfoMediaManager(context, null, null, localBluetoothManager), null);
    }

    public void setRemoved(View view) {
        if (!FeatureFlagUtils.isEnabled(this.mContext, "settings_seamless_transfer") || this.mLocalMediaManager == null || view == null) {
            return;
        }
        View viewFindViewById = view.findViewById(R.id.input_mode);
        if (this.mViews.remove(viewFindViewById)) {
            if (this.mViews.size() == 0) {
                this.mLocalMediaManager.unregisterCallback(this.mMediaDeviceCallback);
            }
        } else {
            Log.e("MediaTransferManager", "Tried to remove unknown view " + viewFindViewById);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public ExpandableNotificationRow getRowForParent(ViewParent viewParent) {
        while (viewParent != null) {
            if (viewParent instanceof ExpandableNotificationRow) {
                return (ExpandableNotificationRow) viewParent;
            }
            viewParent = viewParent.getParent();
        }
        return null;
    }

    public void applyMediaTransferView(ViewGroup viewGroup, NotificationEntry notificationEntry) {
        View viewFindViewById;
        if (!FeatureFlagUtils.isEnabled(this.mContext, "settings_seamless_transfer") || this.mLocalMediaManager == null || viewGroup == null || (viewFindViewById = viewGroup.findViewById(R.id.input_mode)) == null) {
            return;
        }
        viewFindViewById.setVisibility(0);
        viewFindViewById.setOnClickListener(this.mOnClickHandler);
        if (!this.mViews.contains(viewFindViewById)) {
            this.mViews.add(viewFindViewById);
            if (this.mViews.size() == 1) {
                this.mLocalMediaManager.registerCallback(this.mMediaDeviceCallback);
            }
        }
        this.mLocalMediaManager.startScan();
        this.mDevice = this.mLocalMediaManager.getCurrentConnectedDevice();
        updateChip(viewFindViewById);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateAllChips() {
        Iterator<View> it = this.mViews.iterator();
        while (it.hasNext()) {
            updateChip(it.next());
        }
    }

    private void updateChip(View view) {
        ExpandableNotificationRow rowForParent = getRowForParent(view.getParent());
        if (rowForParent != null) {
            int originalIconColor = rowForParent.getNotificationHeader().getOriginalIconColor();
            ColorStateList colorStateListValueOf = ColorStateList.valueOf(originalIconColor);
            int currentBackgroundTint = rowForParent.getCurrentBackgroundTint();
            GradientDrawable gradientDrawable = (GradientDrawable) ((RippleDrawable) ((LinearLayout) view).getBackground()).getDrawable(0);
            gradientDrawable.setStroke(2, originalIconColor);
            gradientDrawable.setColor(currentBackgroundTint);
            ImageView imageView = (ImageView) view.findViewById(R.id.input_separator);
            TextView textView = (TextView) view.findViewById(R.id.insertion_handle);
            textView.setTextColor(colorStateListValueOf);
            MediaDevice mediaDevice = this.mDevice;
            if (mediaDevice != null) {
                Drawable icon = mediaDevice.getIcon();
                imageView.setVisibility(0);
                imageView.setImageTintList(colorStateListValueOf);
                if (icon instanceof AdaptiveIcon) {
                    AdaptiveIcon adaptiveIcon = (AdaptiveIcon) icon;
                    adaptiveIcon.setBackgroundColor(currentBackgroundTint);
                    imageView.setImageDrawable(adaptiveIcon);
                } else {
                    imageView.setImageDrawable(icon);
                }
                textView.setText(this.mDevice.getName());
                return;
            }
            imageView.setVisibility(8);
            textView.setText(R.string.date_picker_decrement_month_button);
        }
    }
}
