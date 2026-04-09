package com.android.systemui.biometrics;

import android.R;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.display.ColorDisplayManager;
import android.provider.Settings;
import android.util.Slog;
import com.android.internal.util.crdroid.FodUtils;
import com.android.systemui.SystemUI;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.util.Assert;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.function.Predicate;

/* loaded from: classes.dex */
public class FODCircleViewImpl extends SystemUI implements CommandQueue.Callbacks {
    private int mAutoModeState;
    private final ArrayList<WeakReference<FODCircleViewImplCallback>> mCallbacks;
    private final CommandQueue mCommandQueue;
    private boolean mDisableNightMode;
    private FODCircleView mFodCircleView;
    private boolean mIsFODVisible;
    private boolean mNightModeActive;

    public FODCircleViewImpl(Context context, CommandQueue commandQueue) {
        super(context);
        this.mCallbacks = new ArrayList<>();
        this.mCommandQueue = commandQueue;
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        PackageManager packageManager = this.mContext.getPackageManager();
        if (packageManager.hasSystemFeature("android.hardware.fingerprint")) {
            if (packageManager.hasSystemFeature("vendor.lineage.biometrics.fingerprint.inscreen") || FodUtils.hasFodSupport(this.mContext)) {
                this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
                try {
                    this.mFodCircleView = new FODCircleView(this.mContext);
                    for (int i = 0; i < this.mCallbacks.size(); i++) {
                        FODCircleViewImplCallback fODCircleViewImplCallback = this.mCallbacks.get(i).get();
                        if (fODCircleViewImplCallback != null) {
                            fODCircleViewImplCallback.onFODStart();
                        }
                    }
                } catch (RuntimeException e) {
                    Slog.e("FODCircleViewImpl", "Failed to initialize FODCircleView", e);
                }
                this.mDisableNightMode = this.mContext.getResources().getBoolean(R.bool.config_customUserSwitchUi);
            }
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void showInDisplayFingerprintView() {
        if (this.mFodCircleView != null) {
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                FODCircleViewImplCallback fODCircleViewImplCallback = this.mCallbacks.get(i).get();
                if (fODCircleViewImplCallback != null) {
                    fODCircleViewImplCallback.onFODStatusChange(true);
                }
            }
            this.mIsFODVisible = true;
            if (this.mDisableNightMode && isNightLightEnabled()) {
                disableNightMode();
            }
            this.mFodCircleView.show();
        }
    }

    private boolean isNightLightEnabled() {
        return Settings.System.getInt(this.mContext.getContentResolver(), "fod_night_light", 1) == 1;
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void hideInDisplayFingerprintView() {
        if (this.mFodCircleView != null) {
            if (!this.mDisableNightMode) {
                for (int i = 0; i < this.mCallbacks.size(); i++) {
                    FODCircleViewImplCallback fODCircleViewImplCallback = this.mCallbacks.get(i).get();
                    if (fODCircleViewImplCallback != null) {
                        fODCircleViewImplCallback.onFODStatusChange(false);
                    }
                }
            }
            this.mIsFODVisible = false;
            if (this.mDisableNightMode && isNightLightEnabled()) {
                setNightMode(this.mNightModeActive, this.mAutoModeState);
            }
            this.mFodCircleView.hide();
        }
    }

    public void registerCallback(FODCircleViewImplCallback fODCircleViewImplCallback) {
        Assert.isMainThread();
        Slog.v("FODCircleViewImpl", "*** register callback for " + fODCircleViewImplCallback);
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            if (this.mCallbacks.get(i).get() == fODCircleViewImplCallback) {
                Slog.e("FODCircleViewImpl", "Object tried to add another callback", new Exception("Called by"));
                return;
            }
        }
        this.mCallbacks.add(new WeakReference<>(fODCircleViewImplCallback));
        removeCallback(null);
        sendUpdates(fODCircleViewImplCallback);
    }

    public void removeCallback(final FODCircleViewImplCallback fODCircleViewImplCallback) {
        Assert.isMainThread();
        Slog.v("FODCircleViewImpl", "*** unregister callback for " + fODCircleViewImplCallback);
        this.mCallbacks.removeIf(new Predicate() { // from class: com.android.systemui.biometrics.FODCircleViewImpl$$ExternalSyntheticLambda0
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return FODCircleViewImpl.lambda$removeCallback$0(fODCircleViewImplCallback, (WeakReference) obj);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$removeCallback$0(FODCircleViewImplCallback fODCircleViewImplCallback, WeakReference weakReference) {
        return weakReference.get() == fODCircleViewImplCallback;
    }

    private void sendUpdates(FODCircleViewImplCallback fODCircleViewImplCallback) {
        fODCircleViewImplCallback.onFODStart();
        fODCircleViewImplCallback.onFODStatusChange(this.mIsFODVisible);
    }

    private void disableNightMode() {
        ColorDisplayManager colorDisplayManager = (ColorDisplayManager) this.mContext.getSystemService(ColorDisplayManager.class);
        this.mAutoModeState = colorDisplayManager.getNightDisplayAutoMode();
        this.mNightModeActive = colorDisplayManager.isNightDisplayActivated();
        colorDisplayManager.setNightDisplayActivated(false);
    }

    private void setNightMode(boolean z, int i) {
        ColorDisplayManager colorDisplayManager = (ColorDisplayManager) this.mContext.getSystemService(ColorDisplayManager.class);
        colorDisplayManager.setNightDisplayAutoMode(0);
        if (i == 0) {
            colorDisplayManager.setNightDisplayActivated(z);
        } else if (i == 1 || i == 2) {
            colorDisplayManager.setNightDisplayAutoMode(i);
        }
    }
}
