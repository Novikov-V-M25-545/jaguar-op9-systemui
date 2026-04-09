package com.android.systemui.crdroid.onthego;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import com.android.internal.util.crdroid.OnTheGoUtils;
import com.android.systemui.R;

/* loaded from: classes.dex */
public class OnTheGoDialog extends Dialog {
    protected final Context mContext;
    private final Runnable mDismissDialogRunnable;
    protected final Handler mHandler;
    private final int mOnTheGoDialogLongTimeout;
    private final int mOnTheGoDialogShortTimeout;

    public OnTheGoDialog(Context context) {
        super(context);
        this.mHandler = new Handler();
        this.mDismissDialogRunnable = new Runnable() { // from class: com.android.systemui.crdroid.onthego.OnTheGoDialog.1
            @Override // java.lang.Runnable
            public void run() {
                if (OnTheGoDialog.this.isShowing()) {
                    OnTheGoDialog.this.dismiss();
                }
            }
        };
        this.mContext = context;
        Resources resources = context.getResources();
        this.mOnTheGoDialogLongTimeout = resources.getInteger(R.integer.quick_settings_onthego_dialog_long_timeout);
        this.mOnTheGoDialogShortTimeout = resources.getInteger(R.integer.quick_settings_onthego_dialog_short_timeout);
    }

    @Override // android.app.Dialog
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Window window = getWindow();
        window.setType(2020);
        window.getAttributes().privateFlags |= 16;
        window.clearFlags(2);
        window.requestFeature(1);
        setContentView(R.layout.quick_settings_onthego_dialog);
        setCanceledOnTouchOutside(true);
        final ContentResolver contentResolver = this.mContext.getContentResolver();
        SeekBar seekBar = (SeekBar) findViewById(R.id.alpha_slider);
        seekBar.setProgress((int) (Settings.System.getFloat(contentResolver, "on_the_go_alpha", 0.5f) * 100.0f));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { // from class: com.android.systemui.crdroid.onthego.OnTheGoDialog.2
            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar seekBar2, int i, boolean z) {
                OnTheGoDialog.this.sendAlphaBroadcast(String.valueOf(i + 10));
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar seekBar2) {
                OnTheGoDialog.this.removeAllOnTheGoDialogCallbacks();
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar seekBar2) {
                OnTheGoDialog onTheGoDialog = OnTheGoDialog.this;
                onTheGoDialog.dismissOnTheGoDialog(onTheGoDialog.mOnTheGoDialogShortTimeout);
            }
        });
        if (!OnTheGoUtils.hasFrontCamera(getContext())) {
            findViewById(R.id.onthego_category_1).setVisibility(8);
            return;
        }
        Switch r1 = (Switch) findViewById(R.id.onthego_service_toggle);
        r1.setChecked(Settings.System.getInt(contentResolver, "on_the_go_service_restart", 0) == 1);
        r1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.android.systemui.crdroid.onthego.OnTheGoDialog.3
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                Settings.System.putInt(contentResolver, "on_the_go_service_restart", z ? 1 : 0);
                OnTheGoDialog onTheGoDialog = OnTheGoDialog.this;
                onTheGoDialog.dismissOnTheGoDialog(onTheGoDialog.mOnTheGoDialogShortTimeout);
            }
        });
        Switch r12 = (Switch) findViewById(R.id.onthego_camera_toggle);
        r12.setChecked(Settings.System.getInt(contentResolver, "on_the_go_camera", 0) == 1);
        r12.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.android.systemui.crdroid.onthego.OnTheGoDialog.4
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                Settings.System.putInt(contentResolver, "on_the_go_camera", z ? 1 : 0);
                OnTheGoDialog.this.sendCameraBroadcast();
                OnTheGoDialog onTheGoDialog = OnTheGoDialog.this;
                onTheGoDialog.dismissOnTheGoDialog(onTheGoDialog.mOnTheGoDialogShortTimeout);
            }
        });
    }

    @Override // android.app.Dialog
    protected void onStart() {
        super.onStart();
        dismissOnTheGoDialog(this.mOnTheGoDialogLongTimeout);
    }

    @Override // android.app.Dialog
    protected void onStop() {
        super.onStop();
        removeAllOnTheGoDialogCallbacks();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dismissOnTheGoDialog(int i) {
        removeAllOnTheGoDialogCallbacks();
        this.mHandler.postDelayed(this.mDismissDialogRunnable, i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void removeAllOnTheGoDialogCallbacks() {
        this.mHandler.removeCallbacks(this.mDismissDialogRunnable);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendAlphaBroadcast(String str) {
        float f = Float.parseFloat(str) / 100.0f;
        Intent intent = new Intent();
        intent.setAction("toggle_alpha");
        intent.putExtra("extra_alpha", f);
        this.mContext.sendBroadcast(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendCameraBroadcast() {
        Intent intent = new Intent();
        intent.setAction("toggle_camera");
        this.mContext.sendBroadcast(intent);
    }
}
