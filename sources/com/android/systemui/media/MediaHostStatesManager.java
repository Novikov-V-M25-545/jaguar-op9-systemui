package com.android.systemui.media;

import com.android.systemui.util.animation.MeasurementOutput;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MediaHostStatesManager.kt */
/* loaded from: classes.dex */
public final class MediaHostStatesManager {
    private final Set<Callback> callbacks = new LinkedHashSet();
    private final Set<MediaViewController> controllers = new LinkedHashSet();

    @NotNull
    private final Map<Integer, MeasurementOutput> carouselSizes = new LinkedHashMap();

    @NotNull
    private final Map<Integer, MediaHostState> mediaHostStates = new LinkedHashMap();

    /* compiled from: MediaHostStatesManager.kt */
    public interface Callback {
        void onHostStateChanged(int i, @NotNull MediaHostState mediaHostState);
    }

    @NotNull
    public final Map<Integer, MeasurementOutput> getCarouselSizes() {
        return this.carouselSizes;
    }

    @NotNull
    public final Map<Integer, MediaHostState> getMediaHostStates() {
        return this.mediaHostStates;
    }

    public final void updateHostState(int i, @NotNull MediaHostState hostState) {
        Intrinsics.checkParameterIsNotNull(hostState, "hostState");
        if (hostState.equals(this.mediaHostStates.get(Integer.valueOf(i)))) {
            return;
        }
        MediaHostState mediaHostStateCopy = hostState.copy();
        this.mediaHostStates.put(Integer.valueOf(i), mediaHostStateCopy);
        updateCarouselDimensions(i, hostState);
        Iterator<MediaViewController> it = this.controllers.iterator();
        while (it.hasNext()) {
            it.next().getStateCallback().onHostStateChanged(i, mediaHostStateCopy);
        }
        Iterator<Callback> it2 = this.callbacks.iterator();
        while (it2.hasNext()) {
            it2.next().onHostStateChanged(i, mediaHostStateCopy);
        }
    }

    @NotNull
    public final MeasurementOutput updateCarouselDimensions(int i, @NotNull MediaHostState hostState) {
        Intrinsics.checkParameterIsNotNull(hostState, "hostState");
        MeasurementOutput measurementOutput = new MeasurementOutput(0, 0);
        Iterator<MediaViewController> it = this.controllers.iterator();
        while (it.hasNext()) {
            MeasurementOutput measurementsForState = it.next().getMeasurementsForState(hostState);
            if (measurementsForState != null) {
                if (measurementsForState.getMeasuredHeight() > measurementOutput.getMeasuredHeight()) {
                    measurementOutput.setMeasuredHeight(measurementsForState.getMeasuredHeight());
                }
                if (measurementsForState.getMeasuredWidth() > measurementOutput.getMeasuredWidth()) {
                    measurementOutput.setMeasuredWidth(measurementsForState.getMeasuredWidth());
                }
            }
        }
        this.carouselSizes.put(Integer.valueOf(i), measurementOutput);
        return measurementOutput;
    }

    public final void addCallback(@NotNull Callback callback) {
        Intrinsics.checkParameterIsNotNull(callback, "callback");
        this.callbacks.add(callback);
    }

    public final void addController(@NotNull MediaViewController controller) {
        Intrinsics.checkParameterIsNotNull(controller, "controller");
        this.controllers.add(controller);
    }

    public final void removeController(@NotNull MediaViewController controller) {
        Intrinsics.checkParameterIsNotNull(controller, "controller");
        this.controllers.remove(controller);
    }
}
