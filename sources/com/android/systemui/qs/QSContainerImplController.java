package com.android.systemui.qs;

import com.android.systemui.R;
import com.android.systemui.qs.QuickStatusBarHeaderController;

/* loaded from: classes.dex */
public class QSContainerImplController {
    private final QuickStatusBarHeaderController mQuickStatusBarHeaderController;
    private final QSContainerImpl mView;

    private QSContainerImplController(QSContainerImpl qSContainerImpl, QuickStatusBarHeaderController.Builder builder) {
        this.mView = qSContainerImpl;
        this.mQuickStatusBarHeaderController = builder.setQuickStatusBarHeader((QuickStatusBarHeader) qSContainerImpl.findViewById(R.id.header)).build();
    }

    public void setListening(boolean z) {
        this.mQuickStatusBarHeaderController.setListening(z);
    }

    public static class Builder {
        private final QuickStatusBarHeaderController.Builder mQuickStatusBarHeaderControllerBuilder;
        private QSContainerImpl mView;

        public Builder(QuickStatusBarHeaderController.Builder builder) {
            this.mQuickStatusBarHeaderControllerBuilder = builder;
        }

        public Builder setQSContainerImpl(QSContainerImpl qSContainerImpl) {
            this.mView = qSContainerImpl;
            return this;
        }

        public QSContainerImplController build() {
            return new QSContainerImplController(this.mView, this.mQuickStatusBarHeaderControllerBuilder);
        }
    }
}
