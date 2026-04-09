package com.android.systemui.tuner;

import android.view.View;
import com.android.systemui.tuner.TunerService;

/* loaded from: classes.dex */
public class TunablePadding implements TunerService.Tunable {
    private final int mDefaultSize;
    private final float mDensity;
    private final int mFlags;
    private final View mView;

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        int i = this.mDefaultSize;
        if (str2 != null) {
            try {
                i = (int) (Integer.parseInt(str2) * this.mDensity);
            } catch (NumberFormatException unused) {
            }
        }
        this.mView.setPadding(getPadding(i, this.mView.isLayoutRtl() ? 2 : 1), getPadding(i, 4), getPadding(i, this.mView.isLayoutRtl() ? 1 : 2), getPadding(i, 8));
    }

    private int getPadding(int i, int i2) {
        if ((this.mFlags & i2) != 0) {
            return i;
        }
        return 0;
    }

    public static class TunablePaddingService {
        private final TunerService mTunerService;

        public TunablePaddingService(TunerService tunerService) {
            this.mTunerService = tunerService;
        }
    }
}
