package com.android.systemui.media;

import android.content.Context;
import android.content.res.Configuration;
import androidx.constraintlayout.widget.ConstraintSet;
import com.android.systemui.R;
import com.android.systemui.media.MediaHostStatesManager;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.util.animation.MeasurementInput;
import com.android.systemui.util.animation.MeasurementOutput;
import com.android.systemui.util.animation.TransitionLayout;
import com.android.systemui.util.animation.TransitionLayoutController;
import com.android.systemui.util.animation.TransitionViewState;
import com.android.systemui.util.animation.WidgetState;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaViewController.kt */
/* loaded from: classes.dex */
public final class MediaViewController {
    public static final Companion Companion = new Companion(null);
    public static final long GUTS_ANIMATION_DURATION = 500;
    private boolean animateNextStateChange;
    private long animationDelay;
    private long animationDuration;

    @NotNull
    private final ConstraintSet collapsedLayout;
    private final ConfigurationController configurationController;
    private final MediaViewController$configurationListener$1 configurationListener;
    private int currentEndLocation;
    private int currentHeight;
    private int currentStartLocation;
    private float currentTransitionProgress;
    private int currentWidth;

    @NotNull
    private final ConstraintSet expandedLayout;
    private boolean firstRefresh;
    private boolean isGutsVisible;
    private final TransitionLayoutController layoutController;
    private final MeasurementOutput measurement;
    private final MediaHostStatesManager mediaHostStatesManager;

    @NotNull
    public Function0<Unit> sizeChangedListener;

    @NotNull
    private final MediaHostStatesManager.Callback stateCallback;
    private final CacheKey tmpKey;
    private final TransitionViewState tmpState;
    private final TransitionViewState tmpState2;
    private final TransitionViewState tmpState3;
    private TransitionLayout transitionLayout;
    private final Map<CacheKey, TransitionViewState> viewStates;

    /* JADX WARN: Type inference failed for: r1v8, types: [com.android.systemui.media.MediaViewController$configurationListener$1, java.lang.Object] */
    public MediaViewController(@NotNull Context context, @NotNull ConfigurationController configurationController, @NotNull MediaHostStatesManager mediaHostStatesManager) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(configurationController, "configurationController");
        Intrinsics.checkParameterIsNotNull(mediaHostStatesManager, "mediaHostStatesManager");
        this.configurationController = configurationController;
        this.mediaHostStatesManager = mediaHostStatesManager;
        this.firstRefresh = true;
        TransitionLayoutController transitionLayoutController = new TransitionLayoutController();
        this.layoutController = transitionLayoutController;
        this.measurement = new MeasurementOutput(0, 0);
        this.viewStates = new LinkedHashMap();
        this.currentEndLocation = -1;
        this.currentStartLocation = -1;
        this.currentTransitionProgress = 1.0f;
        this.tmpState = new TransitionViewState();
        this.tmpState2 = new TransitionViewState();
        this.tmpState3 = new TransitionViewState();
        this.tmpKey = new CacheKey(0, 0, 0.0f, false, 15, null);
        ?? r1 = new ConfigurationController.ConfigurationListener() { // from class: com.android.systemui.media.MediaViewController$configurationListener$1
            @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
            public void onConfigChanged(@Nullable Configuration configuration) {
                if (configuration != null) {
                    TransitionLayout transitionLayout = this.this$0.transitionLayout;
                    if (transitionLayout == null || transitionLayout.getRawLayoutDirection() != configuration.getLayoutDirection()) {
                        TransitionLayout transitionLayout2 = this.this$0.transitionLayout;
                        if (transitionLayout2 != null) {
                            transitionLayout2.setLayoutDirection(configuration.getLayoutDirection());
                        }
                        this.this$0.refreshState();
                    }
                }
            }
        };
        this.configurationListener = r1;
        this.stateCallback = new MediaHostStatesManager.Callback() { // from class: com.android.systemui.media.MediaViewController$stateCallback$1
            @Override // com.android.systemui.media.MediaHostStatesManager.Callback
            public void onHostStateChanged(int i, @NotNull MediaHostState mediaHostState) {
                Intrinsics.checkParameterIsNotNull(mediaHostState, "mediaHostState");
                if (i == this.this$0.currentEndLocation || i == this.this$0.currentStartLocation) {
                    MediaViewController mediaViewController = this.this$0;
                    mediaViewController.setCurrentState(mediaViewController.currentStartLocation, this.this$0.currentEndLocation, this.this$0.currentTransitionProgress, false);
                }
            }
        };
        ConstraintSet constraintSet = new ConstraintSet();
        this.collapsedLayout = constraintSet;
        ConstraintSet constraintSet2 = new ConstraintSet();
        this.expandedLayout = constraintSet2;
        constraintSet.load(context, R.xml.media_collapsed);
        constraintSet2.load(context, R.xml.media_expanded);
        mediaHostStatesManager.addController(this);
        transitionLayoutController.setSizeChangedListener(new Function2<Integer, Integer, Unit>() { // from class: com.android.systemui.media.MediaViewController.1
            {
                super(2);
            }

            @Override // kotlin.jvm.functions.Function2
            public /* bridge */ /* synthetic */ Unit invoke(Integer num, Integer num2) {
                invoke(num.intValue(), num2.intValue());
                return Unit.INSTANCE;
            }

            public final void invoke(int i, int i2) {
                MediaViewController.this.setCurrentWidth(i);
                MediaViewController.this.setCurrentHeight(i2);
                MediaViewController.this.getSizeChangedListener().invoke();
            }
        });
        configurationController.addCallback(r1);
    }

    /* compiled from: MediaViewController.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    @NotNull
    public final Function0<Unit> getSizeChangedListener() {
        Function0<Unit> function0 = this.sizeChangedListener;
        if (function0 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("sizeChangedListener");
        }
        return function0;
    }

    public final void setSizeChangedListener(@NotNull Function0<Unit> function0) {
        Intrinsics.checkParameterIsNotNull(function0, "<set-?>");
        this.sizeChangedListener = function0;
    }

    public final int getCurrentWidth() {
        return this.currentWidth;
    }

    public final void setCurrentWidth(int i) {
        this.currentWidth = i;
    }

    public final int getCurrentHeight() {
        return this.currentHeight;
    }

    public final void setCurrentHeight(int i) {
        this.currentHeight = i;
    }

    public final float getTranslationX() {
        TransitionLayout transitionLayout = this.transitionLayout;
        if (transitionLayout != null) {
            return transitionLayout.getTranslationX();
        }
        return 0.0f;
    }

    public final float getTranslationY() {
        TransitionLayout transitionLayout = this.transitionLayout;
        if (transitionLayout != null) {
            return transitionLayout.getTranslationY();
        }
        return 0.0f;
    }

    @NotNull
    public final MediaHostStatesManager.Callback getStateCallback() {
        return this.stateCallback;
    }

    @NotNull
    public final ConstraintSet getCollapsedLayout() {
        return this.collapsedLayout;
    }

    @NotNull
    public final ConstraintSet getExpandedLayout() {
        return this.expandedLayout;
    }

    public final boolean isGutsVisible() {
        return this.isGutsVisible;
    }

    public final void onDestroy() {
        this.mediaHostStatesManager.removeController(this);
        this.configurationController.removeCallback(this.configurationListener);
    }

    public final void openGuts() {
        if (this.isGutsVisible) {
            return;
        }
        this.isGutsVisible = true;
        animatePendingStateChange(GUTS_ANIMATION_DURATION, 0L);
        setCurrentState(this.currentStartLocation, this.currentEndLocation, this.currentTransitionProgress, false);
    }

    public final void closeGuts(boolean z) {
        if (this.isGutsVisible) {
            this.isGutsVisible = false;
            if (!z) {
                animatePendingStateChange(GUTS_ANIMATION_DURATION, 0L);
            }
            setCurrentState(this.currentStartLocation, this.currentEndLocation, this.currentTransitionProgress, z);
        }
    }

    private final void ensureAllMeasurements() {
        Iterator<Map.Entry<Integer, MediaHostState>> it = this.mediaHostStatesManager.getMediaHostStates().entrySet().iterator();
        while (it.hasNext()) {
            obtainViewState(it.next().getValue());
        }
    }

    private final ConstraintSet constraintSetForExpansion(float f) {
        return f > ((float) 0) ? this.expandedLayout : this.collapsedLayout;
    }

    private final void setGutsViewState(TransitionViewState transitionViewState) {
        Iterator<T> it = PlayerViewHolder.Companion.getControlsIds().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            WidgetState widgetState = transitionViewState.getWidgetStates().get(Integer.valueOf(((Number) it.next()).intValue()));
            if (widgetState != null) {
                widgetState.setAlpha(this.isGutsVisible ? 0.0f : widgetState.getAlpha());
                widgetState.setGone(this.isGutsVisible ? true : widgetState.getGone());
            }
        }
        Iterator<T> it2 = PlayerViewHolder.Companion.getGutsIds().iterator();
        while (it2.hasNext()) {
            int iIntValue = ((Number) it2.next()).intValue();
            WidgetState widgetState2 = transitionViewState.getWidgetStates().get(Integer.valueOf(iIntValue));
            if (widgetState2 != null) {
                widgetState2.setAlpha(this.isGutsVisible ? 1.0f : 0.0f);
            }
            WidgetState widgetState3 = transitionViewState.getWidgetStates().get(Integer.valueOf(iIntValue));
            if (widgetState3 != null) {
                widgetState3.setGone(!this.isGutsVisible);
            }
        }
    }

    private final TransitionViewState obtainViewState(MediaHostState mediaHostState) {
        if (mediaHostState == null || mediaHostState.getMeasurementInput() == null) {
            return null;
        }
        CacheKey key = getKey(mediaHostState, this.isGutsVisible, this.tmpKey);
        TransitionViewState transitionViewState = this.viewStates.get(key);
        if (transitionViewState != null) {
            return transitionViewState;
        }
        CacheKey cacheKeyCopy$default = CacheKey.copy$default(key, 0, 0, 0.0f, false, 15, null);
        if (this.transitionLayout == null) {
            return null;
        }
        if (mediaHostState.getExpansion() == 0.0f || mediaHostState.getExpansion() == 1.0f) {
            TransitionLayout transitionLayout = this.transitionLayout;
            if (transitionLayout == null) {
                Intrinsics.throwNpe();
            }
            MeasurementInput measurementInput = mediaHostState.getMeasurementInput();
            if (measurementInput == null) {
                Intrinsics.throwNpe();
            }
            TransitionViewState transitionViewStateCalculateViewState = transitionLayout.calculateViewState(measurementInput, constraintSetForExpansion(mediaHostState.getExpansion()), new TransitionViewState());
            setGutsViewState(transitionViewStateCalculateViewState);
            this.viewStates.put(cacheKeyCopy$default, transitionViewStateCalculateViewState);
            return transitionViewStateCalculateViewState;
        }
        MediaHostState mediaHostStateCopy = mediaHostState.copy();
        mediaHostStateCopy.setExpansion(0.0f);
        TransitionViewState transitionViewStateObtainViewState = obtainViewState(mediaHostStateCopy);
        if (transitionViewStateObtainViewState == null) {
            throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.util.animation.TransitionViewState");
        }
        MediaHostState mediaHostStateCopy2 = mediaHostState.copy();
        mediaHostStateCopy2.setExpansion(1.0f);
        TransitionViewState transitionViewStateObtainViewState2 = obtainViewState(mediaHostStateCopy2);
        if (transitionViewStateObtainViewState2 == null) {
            throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.util.animation.TransitionViewState");
        }
        return TransitionLayoutController.getInterpolatedState$default(this.layoutController, transitionViewStateObtainViewState, transitionViewStateObtainViewState2, mediaHostState.getExpansion(), null, 8, null);
    }

    private final CacheKey getKey(MediaHostState mediaHostState, boolean z, CacheKey cacheKey) {
        MeasurementInput measurementInput = mediaHostState.getMeasurementInput();
        cacheKey.setHeightMeasureSpec(measurementInput != null ? measurementInput.getHeightMeasureSpec() : 0);
        MeasurementInput measurementInput2 = mediaHostState.getMeasurementInput();
        cacheKey.setWidthMeasureSpec(measurementInput2 != null ? measurementInput2.getWidthMeasureSpec() : 0);
        cacheKey.setExpansion(mediaHostState.getExpansion());
        cacheKey.setGutsVisible(z);
        return cacheKey;
    }

    public final void attach(@NotNull TransitionLayout transitionLayout) {
        Intrinsics.checkParameterIsNotNull(transitionLayout, "transitionLayout");
        this.transitionLayout = transitionLayout;
        this.layoutController.attach(transitionLayout);
        int i = this.currentEndLocation;
        if (i == -1) {
            return;
        }
        setCurrentState(this.currentStartLocation, i, this.currentTransitionProgress, true);
    }

    @Nullable
    public final MeasurementOutput getMeasurementsForState(@NotNull MediaHostState hostState) {
        Intrinsics.checkParameterIsNotNull(hostState, "hostState");
        TransitionViewState transitionViewStateObtainViewState = obtainViewState(hostState);
        if (transitionViewStateObtainViewState == null) {
            return null;
        }
        this.measurement.setMeasuredWidth(transitionViewStateObtainViewState.getWidth());
        this.measurement.setMeasuredHeight(transitionViewStateObtainViewState.getHeight());
        return this.measurement;
    }

    public final void setCurrentState(int i, int i2, float f, boolean z) {
        TransitionViewState transitionViewState;
        this.currentEndLocation = i2;
        this.currentStartLocation = i;
        this.currentTransitionProgress = f;
        boolean z2 = this.animateNextStateChange && !z;
        MediaHostState mediaHostState = this.mediaHostStatesManager.getMediaHostStates().get(Integer.valueOf(i2));
        if (mediaHostState != null) {
            MediaHostState mediaHostState2 = this.mediaHostStatesManager.getMediaHostStates().get(Integer.valueOf(i));
            TransitionViewState transitionViewStateObtainViewState = obtainViewState(mediaHostState);
            if (transitionViewStateObtainViewState != null) {
                TransitionViewState transitionViewStateUpdateViewStateToCarouselSize = updateViewStateToCarouselSize(transitionViewStateObtainViewState, i2, this.tmpState2);
                if (transitionViewStateUpdateViewStateToCarouselSize == null) {
                    Intrinsics.throwNpe();
                }
                this.layoutController.setMeasureState(transitionViewStateUpdateViewStateToCarouselSize);
                this.animateNextStateChange = false;
                if (this.transitionLayout == null) {
                    return;
                }
                TransitionViewState transitionViewStateUpdateViewStateToCarouselSize2 = updateViewStateToCarouselSize(obtainViewState(mediaHostState2), i, this.tmpState3);
                if (!mediaHostState.getVisible()) {
                    if (transitionViewStateUpdateViewStateToCarouselSize2 != null && mediaHostState2 != null && mediaHostState2.getVisible()) {
                        transitionViewStateUpdateViewStateToCarouselSize2 = this.layoutController.getGoneState(transitionViewStateUpdateViewStateToCarouselSize2, mediaHostState2.getDisappearParameters(), f, this.tmpState);
                        transitionViewState = transitionViewStateUpdateViewStateToCarouselSize2;
                    }
                    transitionViewState = transitionViewStateUpdateViewStateToCarouselSize;
                } else {
                    if (mediaHostState2 == null || mediaHostState2.getVisible()) {
                        if (f != 1.0f && transitionViewStateUpdateViewStateToCarouselSize2 != null) {
                            if (f != 0.0f) {
                                transitionViewStateUpdateViewStateToCarouselSize2 = this.layoutController.getInterpolatedState(transitionViewStateUpdateViewStateToCarouselSize2, transitionViewStateUpdateViewStateToCarouselSize, f, this.tmpState);
                            }
                        }
                        transitionViewState = transitionViewStateUpdateViewStateToCarouselSize;
                    } else {
                        transitionViewStateUpdateViewStateToCarouselSize2 = this.layoutController.getGoneState(transitionViewStateUpdateViewStateToCarouselSize, mediaHostState.getDisappearParameters(), 1.0f - f, this.tmpState);
                    }
                    transitionViewState = transitionViewStateUpdateViewStateToCarouselSize2;
                }
                this.layoutController.setState(transitionViewState, z, z2, this.animationDuration, this.animationDelay);
            }
        }
    }

    private final TransitionViewState updateViewStateToCarouselSize(TransitionViewState transitionViewState, int i, TransitionViewState transitionViewState2) {
        TransitionViewState transitionViewStateCopy;
        if (transitionViewState == null || (transitionViewStateCopy = transitionViewState.copy(transitionViewState2)) == null) {
            return null;
        }
        MeasurementOutput measurementOutput = this.mediaHostStatesManager.getCarouselSizes().get(Integer.valueOf(i));
        if (measurementOutput != null) {
            transitionViewStateCopy.setHeight(Math.max(measurementOutput.getMeasuredHeight(), transitionViewStateCopy.getHeight()));
            transitionViewStateCopy.setWidth(Math.max(measurementOutput.getMeasuredWidth(), transitionViewStateCopy.getWidth()));
        }
        return transitionViewStateCopy;
    }

    private final TransitionViewState obtainViewStateForLocation(int i) {
        MediaHostState mediaHostState = this.mediaHostStatesManager.getMediaHostStates().get(Integer.valueOf(i));
        if (mediaHostState != null) {
            return obtainViewState(mediaHostState);
        }
        return null;
    }

    public final void onLocationPreChange(int i) {
        TransitionViewState transitionViewStateObtainViewStateForLocation = obtainViewStateForLocation(i);
        if (transitionViewStateObtainViewStateForLocation != null) {
            this.layoutController.setMeasureState(transitionViewStateObtainViewStateForLocation);
        }
    }

    public final void animatePendingStateChange(long j, long j2) {
        this.animateNextStateChange = true;
        this.animationDuration = j;
        this.animationDelay = j2;
    }

    public final void refreshState() {
        this.viewStates.clear();
        if (this.firstRefresh) {
            ensureAllMeasurements();
            this.firstRefresh = false;
        }
        setCurrentState(this.currentStartLocation, this.currentEndLocation, this.currentTransitionProgress, true);
    }
}
