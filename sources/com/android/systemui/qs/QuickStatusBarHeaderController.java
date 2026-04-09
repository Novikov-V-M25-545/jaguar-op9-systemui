package com.android.systemui.qs;

import android.content.res.Resources;
import com.android.systemui.R;
import com.android.systemui.qs.carrier.QSCarrierGroup;
import com.android.systemui.qs.carrier.QSCarrierGroupController;

/* loaded from: classes.dex */
public class QuickStatusBarHeaderController {
    private final QSCarrierGroupController mQSCarrierGroupController;
    private final QuickStatusBarHeader mView;

    private QuickStatusBarHeaderController(QuickStatusBarHeader quickStatusBarHeader, QSCarrierGroupController.Builder builder) {
        this.mView = quickStatusBarHeader;
        this.mQSCarrierGroupController = builder.setQSCarrierGroup((QSCarrierGroup) quickStatusBarHeader.findViewById(R.id.carrier_group)).build();
    }

    public void setListening(boolean z) throws Resources.NotFoundException {
        this.mQSCarrierGroupController.setListening(z);
        this.mView.setListening(z);
    }

    public static class Builder {
        private final QSCarrierGroupController.Builder mQSCarrierGroupControllerBuilder;
        private QuickStatusBarHeader mView;

        public Builder(QSCarrierGroupController.Builder builder) {
            this.mQSCarrierGroupControllerBuilder = builder;
        }

        public Builder setQuickStatusBarHeader(QuickStatusBarHeader quickStatusBarHeader) {
            this.mView = quickStatusBarHeader;
            return this;
        }

        public QuickStatusBarHeaderController build() {
            return new QuickStatusBarHeaderController(this.mView, this.mQSCarrierGroupControllerBuilder);
        }
    }
}
