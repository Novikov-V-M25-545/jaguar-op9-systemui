package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.StatusIconDisplayable;

/* loaded from: classes.dex */
public class StatusBarNetworkTraffic extends NetworkTraffic implements DarkIconDispatcher.DarkReceiver, StatusIconDisplayable {
    private boolean mColorIsStatic;
    private boolean mKeyguardShowing;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private boolean mSystemIconVisible;
    private final KeyguardUpdateMonitorCallback mUpdateCallback;
    private int mVisibleState;

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public String getSlot() {
        return "networktraffic";
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public void setDecorColor(int i) {
    }

    public StatusBarNetworkTraffic(Context context) {
        this(context, null);
    }

    public StatusBarNetworkTraffic(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public StatusBarNetworkTraffic(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mVisibleState = -1;
        this.mSystemIconVisible = true;
        KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.systemui.statusbar.policy.StatusBarNetworkTraffic.1
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onKeyguardVisibilityChanged(boolean z) {
                StatusBarNetworkTraffic.this.mKeyguardShowing = z;
                StatusBarNetworkTraffic.this.updateVisibility();
            }
        };
        this.mUpdateCallback = keyguardUpdateMonitorCallback;
        setVisibleState(0);
        KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        keyguardUpdateMonitor.registerCallback(keyguardUpdateMonitorCallback);
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver
    public void onDarkChanged(Rect rect, float f, int i) {
        if (this.mColorIsStatic) {
            return;
        }
        this.newTint = DarkIconDispatcher.getTint(rect, this, i);
        checkUpdateTrafficDrawable();
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public void setStaticDrawableColor(int i) {
        this.mColorIsStatic = true;
        this.newTint = i;
        checkUpdateTrafficDrawable();
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public boolean isIconVisible() {
        return this.mEnabled;
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public int getVisibleState() {
        return this.mVisibleState;
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public void setVisibleState(int i, boolean z) {
        if (i != this.mVisibleState && this.mEnabled && this.mAttached) {
            this.mVisibleState = i;
            if (i == 0) {
                this.mSystemIconVisible = true;
            } else {
                this.mSystemIconVisible = false;
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkTraffic
    protected void setEnabled() {
        this.mEnabled = this.mLocation == 1;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkTraffic
    protected void updateVisibility() {
        boolean z = this.mEnabled && this.mIsActive && !this.mKeyguardShowing && this.mSystemIconVisible && getText() != "";
        if (z != this.mVisible) {
            this.mVisible = z;
            setVisibility(z ? 0 : 8);
            checkUpdateTrafficDrawable();
        }
    }

    private void checkUpdateTrafficDrawable() {
        if (this.mVisible) {
            int i = this.mIconTint;
            int i2 = this.newTint;
            if (i != i2) {
                this.mIconTint = i2;
                updateTrafficDrawable();
            }
        }
    }
}
