package com.android.systemui.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.broadcast.BroadcastDispatcher;

/* loaded from: classes.dex */
public class BrightnessDialog extends Activity {
    private BrightnessController mBrightnessController;
    private final BroadcastDispatcher mBroadcastDispatcher;

    public BrightnessDialog(BroadcastDispatcher broadcastDispatcher) {
        this.mBroadcastDispatcher = broadcastDispatcher;
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Window window = getWindow();
        window.setGravity(48);
        window.clearFlags(2);
        window.requestFeature(1);
        setContentView(LayoutInflater.from(this).inflate(R.layout.quick_settings_brightness_dialog, (ViewGroup) null));
        this.mBrightnessController = new BrightnessController(this, (ImageView) findViewById(R.id.brightness_icon), (ToggleSliderView) findViewById(R.id.brightness_slider), this.mBroadcastDispatcher);
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        this.mBrightnessController.registerCallbacks();
        MetricsLogger.visible(this, 220);
    }

    @Override // android.app.Activity
    protected void onStop() {
        super.onStop();
        MetricsLogger.hidden(this, 220);
        this.mBrightnessController.unregisterCallbacks();
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (i == 25 || i == 24 || i == 164) {
            finish();
        }
        return super.onKeyDown(i, keyEvent);
    }
}
