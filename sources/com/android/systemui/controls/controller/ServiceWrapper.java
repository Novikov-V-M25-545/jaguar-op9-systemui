package com.android.systemui.controls.controller;

import android.service.controls.IControlsActionCallback;
import android.service.controls.IControlsProvider;
import android.service.controls.IControlsSubscriber;
import android.service.controls.IControlsSubscription;
import android.service.controls.actions.ControlAction;
import android.service.controls.actions.ControlActionWrapper;
import android.util.Log;
import java.util.List;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ServiceWrapper.kt */
/* loaded from: classes.dex */
public final class ServiceWrapper {
    public static final Companion Companion = new Companion(null);

    @NotNull
    private final IControlsProvider service;

    /* compiled from: ServiceWrapper.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    public ServiceWrapper(@NotNull IControlsProvider service) {
        Intrinsics.checkParameterIsNotNull(service, "service");
        this.service = service;
    }

    public final boolean load(@NotNull IControlsSubscriber subscriber) {
        Intrinsics.checkParameterIsNotNull(subscriber, "subscriber");
        try {
            this.service.load(subscriber);
            return true;
        } catch (Exception e) {
            Log.e("ServiceWrapper", "Caught exception from ControlsProviderService", e);
            return false;
        }
    }

    public final boolean loadSuggested(@NotNull IControlsSubscriber subscriber) {
        Intrinsics.checkParameterIsNotNull(subscriber, "subscriber");
        try {
            this.service.loadSuggested(subscriber);
            return true;
        } catch (Exception e) {
            Log.e("ServiceWrapper", "Caught exception from ControlsProviderService", e);
            return false;
        }
    }

    public final boolean subscribe(@NotNull List<String> controlIds, @NotNull IControlsSubscriber subscriber) {
        Intrinsics.checkParameterIsNotNull(controlIds, "controlIds");
        Intrinsics.checkParameterIsNotNull(subscriber, "subscriber");
        try {
            this.service.subscribe(controlIds, subscriber);
            return true;
        } catch (Exception e) {
            Log.e("ServiceWrapper", "Caught exception from ControlsProviderService", e);
            return false;
        }
    }

    public final boolean request(@NotNull IControlsSubscription subscription, long j) {
        Intrinsics.checkParameterIsNotNull(subscription, "subscription");
        try {
            subscription.request(j);
            return true;
        } catch (Exception e) {
            Log.e("ServiceWrapper", "Caught exception from ControlsProviderService", e);
            return false;
        }
    }

    public final boolean cancel(@NotNull IControlsSubscription subscription) {
        Intrinsics.checkParameterIsNotNull(subscription, "subscription");
        try {
            subscription.cancel();
            return true;
        } catch (Exception e) {
            Log.e("ServiceWrapper", "Caught exception from ControlsProviderService", e);
            return false;
        }
    }

    public final boolean action(@NotNull String controlId, @NotNull ControlAction action, @NotNull IControlsActionCallback cb) {
        Intrinsics.checkParameterIsNotNull(controlId, "controlId");
        Intrinsics.checkParameterIsNotNull(action, "action");
        Intrinsics.checkParameterIsNotNull(cb, "cb");
        try {
            this.service.action(controlId, new ControlActionWrapper(action), cb);
            return true;
        } catch (Exception e) {
            Log.e("ServiceWrapper", "Caught exception from ControlsProviderService", e);
            return false;
        }
    }
}
