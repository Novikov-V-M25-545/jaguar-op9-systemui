package com.android.systemui.pulse;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import com.android.internal.util.ContrastColorUtil;
import com.android.settingslib.Utils;
import com.android.systemui.Dependency;
import com.android.systemui.pulse.ColorAnimator;
import com.android.systemui.statusbar.policy.ConfigurationController;

/* loaded from: classes.dex */
public class ColorController extends ContentObserver implements ColorAnimator.ColorAnimationListener, ConfigurationController.ConfigurationListener {
    private int mAccentColor;
    private int mAlbumColor;
    private int mColor;
    private int mColorType;
    private final Context mContext;
    private final ColorAnimator mLavaLamp;
    private Renderer mRenderer;

    public ColorController(Context context, Handler handler) {
        super(handler);
        this.mContext = context;
        ColorAnimator colorAnimator = new ColorAnimator();
        this.mLavaLamp = colorAnimator;
        colorAnimator.setColorAnimatorListener(this);
        int accentColor = getAccentColor();
        this.mAccentColor = accentColor;
        this.mAlbumColor = accentColor;
        updateSettings();
        startListening();
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    void setRenderer(Renderer renderer) {
        this.mRenderer = renderer;
        notifyRenderer();
    }

    void startListening() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        contentResolver.registerContentObserver(Settings.Secure.getUriFor("pulse_color_mode"), false, this, -1);
        contentResolver.registerContentObserver(Settings.Secure.getUriFor("pulse_color_user"), false, this, -1);
        contentResolver.registerContentObserver(Settings.Secure.getUriFor("pulse_lavalamp_speed"), false, this, -1);
    }

    void updateSettings() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        if (this.mColorType == 2) {
            stopLavaLamp();
        }
        this.mColorType = Settings.Secure.getIntForUser(contentResolver, "pulse_color_mode", 2, -2);
        this.mColor = Settings.Secure.getIntForUser(contentResolver, "pulse_color_user", -1828716545, -2);
        this.mLavaLamp.setAnimationTime(Settings.Secure.getIntForUser(contentResolver, "pulse_lavalamp_speed", 10000, -2));
        notifyRenderer();
    }

    void notifyRenderer() {
        Renderer renderer = this.mRenderer;
        if (renderer != null) {
            int i = this.mColorType;
            if (i == 0) {
                renderer.onUpdateColor(this.mAccentColor);
                return;
            }
            if (i == 1) {
                renderer.onUpdateColor(this.mColor);
                return;
            }
            if (i == 2 && renderer.isValidStream()) {
                startLavaLamp();
            } else if (this.mColorType == 3) {
                this.mRenderer.onUpdateColor(this.mAlbumColor);
            }
        }
    }

    void startLavaLamp() {
        if (this.mColorType == 2) {
            this.mLavaLamp.start();
        }
    }

    void stopLavaLamp() {
        this.mLavaLamp.stop();
    }

    int getAccentColor() {
        return Utils.getColorAccentDefaultColor(this.mContext);
    }

    @Override // android.database.ContentObserver
    public void onChange(boolean z, Uri uri) {
        updateSettings();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onConfigChanged(Configuration configuration) {
        int i = this.mAccentColor;
        int accentColor = getAccentColor();
        if (i != accentColor) {
            this.mAccentColor = accentColor;
            Renderer renderer = this.mRenderer;
            if (renderer == null || this.mColorType != 0) {
                return;
            }
            renderer.onUpdateColor(accentColor);
        }
    }

    @Override // com.android.systemui.pulse.ColorAnimator.ColorAnimationListener
    public void onColorChanged(ColorAnimator colorAnimator, int i) {
        Renderer renderer = this.mRenderer;
        if (renderer != null) {
            renderer.onUpdateColor(i);
        }
    }

    public void setMediaNotificationColor(boolean z, int i) {
        if (z) {
            try {
                int iFindContrastColorAgainstDark = ContrastColorUtil.findContrastColorAgainstDark(i, 0, true, 2.0d);
                this.mAlbumColor = iFindContrastColorAgainstDark;
                this.mAlbumColor = ContrastColorUtil.findContrastColor(iFindContrastColorAgainstDark, 16777215, true, 2.0d);
            } catch (Exception unused) {
                this.mAlbumColor = this.mAccentColor;
            }
        } else {
            this.mAlbumColor = this.mAccentColor;
        }
        Renderer renderer = this.mRenderer;
        if (renderer == null || this.mColorType != 3) {
            return;
        }
        renderer.onUpdateColor(this.mAlbumColor);
    }
}
