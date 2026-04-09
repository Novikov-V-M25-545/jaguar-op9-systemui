package com.android.systemui.usb;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.PermissionChecker;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.systemui.R;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class UsbConfirmActivity extends AlertActivity implements DialogInterface.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private UsbAccessory mAccessory;
    private CheckBox mAlwaysUse;
    private TextView mClearDefaultHint;
    private UsbDevice mDevice;
    private UsbDisconnectedReceiver mDisconnectedReceiver;
    private ResolveInfo mResolveInfo;

    /* JADX WARN: Multi-variable type inference failed */
    public void onCreate(Bundle bundle) {
        boolean z;
        int i;
        getWindow().addSystemFlags(LineageHardwareManager.FEATURE_TOUCHSCREEN_GESTURES);
        super.onCreate(bundle);
        Intent intent = getIntent();
        this.mDevice = (UsbDevice) intent.getParcelableExtra("device");
        this.mAccessory = (UsbAccessory) intent.getParcelableExtra("accessory");
        this.mResolveInfo = (ResolveInfo) intent.getParcelableExtra("rinfo");
        String stringExtra = intent.getStringExtra("android.hardware.usb.extra.PACKAGE");
        String string = this.mResolveInfo.loadLabel(getPackageManager()).toString();
        AlertController.AlertParams alertParams = ((AlertActivity) this).mAlertParams;
        alertParams.mTitle = string;
        if (this.mDevice == null) {
            alertParams.mMessage = getString(R.string.usb_accessory_confirm_prompt, new Object[]{string, this.mAccessory.getDescription()});
            this.mDisconnectedReceiver = new UsbDisconnectedReceiver((Activity) this, this.mAccessory);
            z = false;
        } else {
            z = this.mDevice.getHasAudioCapture() && !(PermissionChecker.checkPermissionForPreflight(this, "android.permission.RECORD_AUDIO", -1, intent.getIntExtra("android.intent.extra.UID", -1), stringExtra) == 0);
            if (z) {
                i = R.string.usb_device_confirm_prompt_warn;
            } else {
                i = R.string.usb_device_confirm_prompt;
            }
            alertParams.mMessage = getString(i, new Object[]{string, this.mDevice.getProductName()});
            this.mDisconnectedReceiver = new UsbDisconnectedReceiver((Activity) this, this.mDevice);
        }
        alertParams.mPositiveButtonText = getString(android.R.string.ok);
        alertParams.mNegativeButtonText = getString(android.R.string.cancel);
        alertParams.mPositiveButtonListener = this;
        alertParams.mNegativeButtonListener = this;
        if (!z) {
            View viewInflate = ((LayoutInflater) getSystemService("layout_inflater")).inflate(android.R.layout.alert_dialog_progress_material, (ViewGroup) null);
            alertParams.mView = viewInflate;
            CheckBox checkBox = (CheckBox) viewInflate.findViewById(android.R.id.actions_container_layout);
            this.mAlwaysUse = checkBox;
            UsbDevice usbDevice = this.mDevice;
            if (usbDevice == null) {
                checkBox.setText(getString(R.string.always_use_accessory, new Object[]{string, this.mAccessory.getDescription()}));
            } else {
                checkBox.setText(getString(R.string.always_use_device, new Object[]{string, usbDevice.getProductName()}));
            }
            this.mAlwaysUse.setOnCheckedChangeListener(this);
            TextView textView = (TextView) alertParams.mView.findViewById(android.R.id.bundle_array);
            this.mClearDefaultHint = textView;
            textView.setVisibility(8);
        }
        setupAlert();
    }

    protected void onDestroy() {
        UsbDisconnectedReceiver usbDisconnectedReceiver = this.mDisconnectedReceiver;
        if (usbDisconnectedReceiver != null) {
            unregisterReceiver(usbDisconnectedReceiver);
        }
        super.onDestroy();
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        Intent intent;
        if (i == -1) {
            try {
                IUsbManager iUsbManagerAsInterface = IUsbManager.Stub.asInterface(ServiceManager.getService("usb"));
                int i2 = this.mResolveInfo.activityInfo.applicationInfo.uid;
                int iMyUserId = UserHandle.myUserId();
                CheckBox checkBox = this.mAlwaysUse;
                boolean zIsChecked = checkBox != null ? checkBox.isChecked() : false;
                Intent intent2 = null;
                if (this.mDevice != null) {
                    intent = new Intent("android.hardware.usb.action.USB_DEVICE_ATTACHED");
                    intent.putExtra("device", this.mDevice);
                    iUsbManagerAsInterface.grantDevicePermission(this.mDevice, i2);
                    if (zIsChecked) {
                        iUsbManagerAsInterface.setDevicePackage(this.mDevice, this.mResolveInfo.activityInfo.packageName, iMyUserId);
                    } else {
                        iUsbManagerAsInterface.setDevicePackage(this.mDevice, (String) null, iMyUserId);
                    }
                } else {
                    if (this.mAccessory != null) {
                        intent = new Intent("android.hardware.usb.action.USB_ACCESSORY_ATTACHED");
                        intent.putExtra("accessory", this.mAccessory);
                        iUsbManagerAsInterface.grantAccessoryPermission(this.mAccessory, i2);
                        if (zIsChecked) {
                            iUsbManagerAsInterface.setAccessoryPackage(this.mAccessory, this.mResolveInfo.activityInfo.packageName, iMyUserId);
                        } else {
                            iUsbManagerAsInterface.setAccessoryPackage(this.mAccessory, (String) null, iMyUserId);
                        }
                    }
                    intent2.addFlags(268435456);
                    ActivityInfo activityInfo = this.mResolveInfo.activityInfo;
                    intent2.setComponent(new ComponentName(activityInfo.packageName, activityInfo.name));
                    startActivityAsUser(intent2, new UserHandle(iMyUserId));
                }
                intent2 = intent;
                intent2.addFlags(268435456);
                ActivityInfo activityInfo2 = this.mResolveInfo.activityInfo;
                intent2.setComponent(new ComponentName(activityInfo2.packageName, activityInfo2.name));
                startActivityAsUser(intent2, new UserHandle(iMyUserId));
            } catch (Exception e) {
                Log.e("UsbConfirmActivity", "Unable to start activity", e);
            }
        }
        finish();
    }

    @Override // android.widget.CompoundButton.OnCheckedChangeListener
    public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
        TextView textView = this.mClearDefaultHint;
        if (textView == null) {
            return;
        }
        if (z) {
            textView.setVisibility(0);
        } else {
            textView.setVisibility(8);
        }
    }
}
