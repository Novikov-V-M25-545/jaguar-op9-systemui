package com.android.systemui.qs.carrier;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.android.keyguard.CarrierTextController;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.policy.NetworkController;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public class QSCarrierGroupController {
    private final ActivityStarter mActivityStarter;
    private final Handler mBgHandler;
    private final Callback mCallback;
    private View[] mCarrierDividers;
    private QSCarrier[] mCarrierGroups;
    private final CarrierTextController mCarrierTextController;
    private final CellSignalState[] mInfos;
    private boolean mListening;
    private final H mMainHandler;
    private final NetworkController mNetworkController;
    private final TextView mNoSimTextView;
    private final NetworkController.SignalCallback mSignalCallback;

    private static class Callback implements CarrierTextController.CarrierTextCallback {
        private H mHandler;

        Callback(H h) {
            this.mHandler = h;
        }

        @Override // com.android.keyguard.CarrierTextController.CarrierTextCallback
        public void updateCarrierInfo(CarrierTextController.CarrierTextCallbackInfo carrierTextCallbackInfo) {
            this.mHandler.obtainMessage(0, carrierTextCallbackInfo).sendToTarget();
        }
    }

    private QSCarrierGroupController(QSCarrierGroup qSCarrierGroup, ActivityStarter activityStarter, Handler handler, Looper looper, NetworkController networkController, CarrierTextController.Builder builder) {
        this.mInfos = new CellSignalState[3];
        this.mCarrierDividers = new View[2];
        this.mCarrierGroups = new QSCarrier[3];
        this.mSignalCallback = new NetworkController.SignalCallback() { // from class: com.android.systemui.qs.carrier.QSCarrierGroupController.1
            @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
            public void setMobileDataIndicators(NetworkController.IconState iconState, NetworkController.IconState iconState2, int i, int i2, boolean z, boolean z2, int i3, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, boolean z3, int i4, boolean z4) {
                int slotIndex = QSCarrierGroupController.this.getSlotIndex(i4);
                if (slotIndex >= 3) {
                    Log.w("QSCarrierGroup", "setMobileDataIndicators - slot: " + slotIndex);
                    return;
                }
                if (slotIndex == -1) {
                    Log.e("QSCarrierGroup", "Invalid SIM slot index for subscription: " + i4);
                    return;
                }
                QSCarrierGroupController.this.mInfos[slotIndex] = new CellSignalState(iconState.visible, iconState.icon, iconState.contentDescription, charSequence.toString(), z4);
                QSCarrierGroupController.this.mMainHandler.obtainMessage(1).sendToTarget();
            }

            @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
            public void setNoSims(boolean z, boolean z2) {
                if (z) {
                    for (int i = 0; i < 3; i++) {
                        QSCarrierGroupController.this.mInfos[i] = QSCarrierGroupController.this.mInfos[i].changeVisibility(false);
                    }
                }
                QSCarrierGroupController.this.mMainHandler.obtainMessage(1).sendToTarget();
            }
        };
        this.mActivityStarter = activityStarter;
        this.mBgHandler = handler;
        this.mNetworkController = networkController;
        this.mCarrierTextController = builder.setShowAirplaneMode(false).setShowMissingSim(false).build();
        View.OnClickListener onClickListener = new View.OnClickListener() { // from class: com.android.systemui.qs.carrier.QSCarrierGroupController$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$new$0(view);
            }
        };
        qSCarrierGroup.setOnClickListener(onClickListener);
        TextView noSimTextView = qSCarrierGroup.getNoSimTextView();
        this.mNoSimTextView = noSimTextView;
        noSimTextView.setOnClickListener(onClickListener);
        H h = new H(looper, new Consumer() { // from class: com.android.systemui.qs.carrier.QSCarrierGroupController$$ExternalSyntheticLambda3
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.handleUpdateCarrierInfo((CarrierTextController.CarrierTextCallbackInfo) obj);
            }
        }, new Runnable() { // from class: com.android.systemui.qs.carrier.QSCarrierGroupController$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.handleUpdateState();
            }
        });
        this.mMainHandler = h;
        this.mCallback = new Callback(h);
        this.mCarrierGroups[0] = qSCarrierGroup.getCarrier1View();
        this.mCarrierGroups[1] = qSCarrierGroup.getCarrier2View();
        this.mCarrierGroups[2] = qSCarrierGroup.getCarrier3View();
        this.mCarrierDividers[0] = qSCarrierGroup.getCarrierDivider1();
        this.mCarrierDividers[1] = qSCarrierGroup.getCarrierDivider2();
        for (int i = 0; i < 3; i++) {
            this.mInfos[i] = new CellSignalState();
            this.mCarrierGroups[i].setOnClickListener(onClickListener);
        }
        qSCarrierGroup.setImportantForAccessibility(1);
        qSCarrierGroup.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() { // from class: com.android.systemui.qs.carrier.QSCarrierGroupController.2
            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewAttachedToWindow(View view) {
            }

            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewDetachedFromWindow(View view) {
                QSCarrierGroupController.this.setListening(false);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0(View view) {
        if (view.isVisibleToUser()) {
            this.mActivityStarter.postStartActivityDismissingKeyguard(new Intent("android.settings.WIRELESS_SETTINGS"), 0);
        }
    }

    protected int getSlotIndex(int i) {
        return SubscriptionManager.getSlotIndex(i);
    }

    public void setListening(boolean z) {
        if (z == this.mListening) {
            return;
        }
        this.mListening = z;
        this.mBgHandler.post(new Runnable() { // from class: com.android.systemui.qs.carrier.QSCarrierGroupController$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.updateListeners();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateListeners() {
        if (this.mListening) {
            if (this.mNetworkController.hasVoiceCallingFeature()) {
                this.mNetworkController.addCallback(this.mSignalCallback);
            }
            this.mCarrierTextController.setListening(this.mCallback);
        } else {
            this.mNetworkController.removeCallback(this.mSignalCallback);
            this.mCarrierTextController.setListening(null);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleUpdateState() {
        if (!this.mMainHandler.getLooper().isCurrentThread()) {
            this.mMainHandler.obtainMessage(1).sendToTarget();
            return;
        }
        int i = 0;
        for (int i2 = 0; i2 < 3; i2++) {
            this.mCarrierGroups[i2].updateState(this.mInfos[i2]);
        }
        View view = this.mCarrierDividers[0];
        CellSignalState[] cellSignalStateArr = this.mInfos;
        view.setVisibility((cellSignalStateArr[0].visible && cellSignalStateArr[1].visible) ? 0 : 8);
        View view2 = this.mCarrierDividers[1];
        CellSignalState[] cellSignalStateArr2 = this.mInfos;
        if ((!cellSignalStateArr2[1].visible || !cellSignalStateArr2[2].visible) && (!cellSignalStateArr2[0].visible || !cellSignalStateArr2[2].visible)) {
            i = 8;
        }
        view2.setVisibility(i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleUpdateCarrierInfo(CarrierTextController.CarrierTextCallbackInfo carrierTextCallbackInfo) {
        if (!this.mMainHandler.getLooper().isCurrentThread()) {
            this.mMainHandler.obtainMessage(0, carrierTextCallbackInfo).sendToTarget();
            return;
        }
        this.mNoSimTextView.setVisibility(8);
        if (!carrierTextCallbackInfo.airplaneMode && carrierTextCallbackInfo.anySimReady) {
            boolean[] zArr = new boolean[3];
            if (carrierTextCallbackInfo.listOfCarriers.length == carrierTextCallbackInfo.subscriptionIds.length) {
                for (int i = 0; i < 3 && i < carrierTextCallbackInfo.listOfCarriers.length; i++) {
                    int slotIndex = getSlotIndex(carrierTextCallbackInfo.subscriptionIds[i]);
                    if (slotIndex >= 3) {
                        Log.w("QSCarrierGroup", "updateInfoCarrier - slot: " + slotIndex);
                    } else if (slotIndex == -1) {
                        Log.e("QSCarrierGroup", "Invalid SIM slot index for subscription: " + carrierTextCallbackInfo.subscriptionIds[i]);
                    } else {
                        CellSignalState[] cellSignalStateArr = this.mInfos;
                        cellSignalStateArr[slotIndex] = cellSignalStateArr[slotIndex].changeVisibility(true);
                        zArr[slotIndex] = true;
                        this.mCarrierGroups[slotIndex].setCarrierText(carrierTextCallbackInfo.listOfCarriers[i].toString().trim());
                        this.mCarrierGroups[slotIndex].setVisibility(0);
                    }
                }
                for (int i2 = 0; i2 < 3; i2++) {
                    if (!zArr[i2]) {
                        CellSignalState[] cellSignalStateArr2 = this.mInfos;
                        cellSignalStateArr2[i2] = cellSignalStateArr2[i2].changeVisibility(false);
                        this.mCarrierGroups[i2].setVisibility(8);
                    }
                }
            } else {
                Log.e("QSCarrierGroup", "Carrier information arrays not of same length");
            }
        } else {
            for (int i3 = 0; i3 < 3; i3++) {
                CellSignalState[] cellSignalStateArr3 = this.mInfos;
                cellSignalStateArr3[i3] = cellSignalStateArr3[i3].changeVisibility(false);
                this.mCarrierGroups[i3].setCarrierText("");
                this.mCarrierGroups[i3].setVisibility(8);
            }
            this.mNoSimTextView.setText(carrierTextCallbackInfo.carrierText);
            if (!TextUtils.isEmpty(carrierTextCallbackInfo.carrierText)) {
                this.mNoSimTextView.setVisibility(0);
            }
        }
        handleUpdateState();
    }

    private static class H extends Handler {
        private Consumer<CarrierTextController.CarrierTextCallbackInfo> mUpdateCarrierInfo;
        private Runnable mUpdateState;

        H(Looper looper, Consumer<CarrierTextController.CarrierTextCallbackInfo> consumer, Runnable runnable) {
            super(looper);
            this.mUpdateCarrierInfo = consumer;
            this.mUpdateState = runnable;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 0) {
                this.mUpdateCarrierInfo.accept((CarrierTextController.CarrierTextCallbackInfo) message.obj);
            } else if (i == 1) {
                this.mUpdateState.run();
            } else {
                super.handleMessage(message);
            }
        }
    }

    public static class Builder {
        private final ActivityStarter mActivityStarter;
        private final CarrierTextController.Builder mCarrierTextControllerBuilder;
        private final Handler mHandler;
        private final Looper mLooper;
        private final NetworkController mNetworkController;
        private QSCarrierGroup mView;

        public Builder(ActivityStarter activityStarter, Handler handler, Looper looper, NetworkController networkController, CarrierTextController.Builder builder) {
            this.mActivityStarter = activityStarter;
            this.mHandler = handler;
            this.mLooper = looper;
            this.mNetworkController = networkController;
            this.mCarrierTextControllerBuilder = builder;
        }

        public Builder setQSCarrierGroup(QSCarrierGroup qSCarrierGroup) {
            this.mView = qSCarrierGroup;
            return this;
        }

        public QSCarrierGroupController build() {
            return new QSCarrierGroupController(this.mView, this.mActivityStarter, this.mHandler, this.mLooper, this.mNetworkController, this.mCarrierTextControllerBuilder);
        }
    }
}
