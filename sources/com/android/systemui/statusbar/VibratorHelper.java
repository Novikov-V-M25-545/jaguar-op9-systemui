package com.android.systemui.statusbar;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioAttributes;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;

/* loaded from: classes.dex */
public class VibratorHelper {
    private static final AudioAttributes STATUS_BAR_VIBRATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    private final Context mContext;
    private boolean mHapticFeedbackEnabled;
    private final ContentObserver mVibrationObserver;
    private final Vibrator mVibrator;

    public VibratorHelper(Context context) {
        ContentObserver contentObserver = new ContentObserver(Handler.getMain()) { // from class: com.android.systemui.statusbar.VibratorHelper.1
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                VibratorHelper.this.updateHapticFeedBackEnabled();
            }
        };
        this.mVibrationObserver = contentObserver;
        this.mContext = context;
        this.mVibrator = (Vibrator) context.getSystemService(Vibrator.class);
        context.getContentResolver().registerContentObserver(Settings.System.getUriFor("haptic_feedback_enabled"), true, contentObserver);
        contentObserver.onChange(false);
    }

    public void vibrate(final int i) {
        if (this.mHapticFeedbackEnabled) {
            AsyncTask.execute(new Runnable() { // from class: com.android.systemui.statusbar.VibratorHelper$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$vibrate$0(i);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$vibrate$0(int i) {
        this.mVibrator.vibrate(VibrationEffect.get(i, false), STATUS_BAR_VIBRATION_ATTRIBUTES);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateHapticFeedBackEnabled() {
        this.mHapticFeedbackEnabled = Settings.System.getIntForUser(this.mContext.getContentResolver(), "haptic_feedback_enabled", 0, -2) != 0;
    }
}
