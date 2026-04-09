package com.android.systemui.media;

import android.graphics.Rect;
import android.util.ArraySet;
import android.view.View;
import com.android.systemui.media.MediaDataManager;
import com.android.systemui.util.animation.DisappearParameters;
import com.android.systemui.util.animation.MeasurementInput;
import com.android.systemui.util.animation.MeasurementOutput;
import com.android.systemui.util.animation.UniqueObjectHostView;
import java.util.Iterator;
import java.util.Objects;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaHost.kt */
/* loaded from: classes.dex */
public final class MediaHost implements MediaHostState {

    @NotNull
    private final Rect currentBounds;

    @NotNull
    public UniqueObjectHostView hostView;
    private final MediaHost$listener$1 listener;
    private int location;
    private final MediaDataManager mediaDataManager;
    private final MediaHierarchyManager mediaHierarchyManager;
    private final MediaHostStatesManager mediaHostStatesManager;
    private final MediaHostStateHolder state;
    private final int[] tmpLocationOnScreen;
    private ArraySet<Function1<Boolean, Unit>> visibleChangedListeners;

    @Override // com.android.systemui.media.MediaHostState
    @NotNull
    public MediaHostState copy() {
        return this.state.copy();
    }

    @Override // com.android.systemui.media.MediaHostState
    @NotNull
    public DisappearParameters getDisappearParameters() {
        return this.state.getDisappearParameters();
    }

    @Override // com.android.systemui.media.MediaHostState
    public float getExpansion() {
        return this.state.getExpansion();
    }

    @Override // com.android.systemui.media.MediaHostState
    public boolean getFalsingProtectionNeeded() {
        return this.state.getFalsingProtectionNeeded();
    }

    @Override // com.android.systemui.media.MediaHostState
    @Nullable
    public MeasurementInput getMeasurementInput() {
        return this.state.getMeasurementInput();
    }

    @Override // com.android.systemui.media.MediaHostState
    public boolean getShowsOnlyActiveMedia() {
        return this.state.getShowsOnlyActiveMedia();
    }

    @Override // com.android.systemui.media.MediaHostState
    public boolean getVisible() {
        return this.state.getVisible();
    }

    public void setDisappearParameters(@NotNull DisappearParameters disappearParameters) {
        Intrinsics.checkParameterIsNotNull(disappearParameters, "<set-?>");
        this.state.setDisappearParameters(disappearParameters);
    }

    @Override // com.android.systemui.media.MediaHostState
    public void setExpansion(float f) {
        this.state.setExpansion(f);
    }

    public void setFalsingProtectionNeeded(boolean z) {
        this.state.setFalsingProtectionNeeded(z);
    }

    public void setShowsOnlyActiveMedia(boolean z) {
        this.state.setShowsOnlyActiveMedia(z);
    }

    public void setVisible(boolean z) {
        this.state.setVisible(z);
    }

    /* JADX WARN: Type inference failed for: r2v6, types: [com.android.systemui.media.MediaHost$listener$1] */
    public MediaHost(@NotNull MediaHostStateHolder state, @NotNull MediaHierarchyManager mediaHierarchyManager, @NotNull MediaDataManager mediaDataManager, @NotNull MediaHostStatesManager mediaHostStatesManager) {
        Intrinsics.checkParameterIsNotNull(state, "state");
        Intrinsics.checkParameterIsNotNull(mediaHierarchyManager, "mediaHierarchyManager");
        Intrinsics.checkParameterIsNotNull(mediaDataManager, "mediaDataManager");
        Intrinsics.checkParameterIsNotNull(mediaHostStatesManager, "mediaHostStatesManager");
        this.state = state;
        this.mediaHierarchyManager = mediaHierarchyManager;
        this.mediaDataManager = mediaDataManager;
        this.mediaHostStatesManager = mediaHostStatesManager;
        this.location = -1;
        this.visibleChangedListeners = new ArraySet<>();
        this.tmpLocationOnScreen = new int[]{0, 0};
        this.currentBounds = new Rect();
        this.listener = new MediaDataManager.Listener() { // from class: com.android.systemui.media.MediaHost$listener$1
            @Override // com.android.systemui.media.MediaDataManager.Listener
            public void onMediaDataLoaded(@NotNull String key, @Nullable String str, @NotNull MediaData data) {
                Intrinsics.checkParameterIsNotNull(key, "key");
                Intrinsics.checkParameterIsNotNull(data, "data");
                this.this$0.updateViewVisibility();
            }

            @Override // com.android.systemui.media.MediaDataManager.Listener
            public void onMediaDataRemoved(@NotNull String key) {
                Intrinsics.checkParameterIsNotNull(key, "key");
                this.this$0.updateViewVisibility();
            }
        };
    }

    @NotNull
    public final UniqueObjectHostView getHostView() {
        UniqueObjectHostView uniqueObjectHostView = this.hostView;
        if (uniqueObjectHostView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hostView");
        }
        return uniqueObjectHostView;
    }

    public final void setHostView(@NotNull UniqueObjectHostView uniqueObjectHostView) {
        Intrinsics.checkParameterIsNotNull(uniqueObjectHostView, "<set-?>");
        this.hostView = uniqueObjectHostView;
    }

    public final int getLocation() {
        return this.location;
    }

    @NotNull
    public final Rect getCurrentBounds() {
        UniqueObjectHostView uniqueObjectHostView = this.hostView;
        if (uniqueObjectHostView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hostView");
        }
        uniqueObjectHostView.getLocationOnScreen(this.tmpLocationOnScreen);
        int i = 0;
        int i2 = this.tmpLocationOnScreen[0];
        UniqueObjectHostView uniqueObjectHostView2 = this.hostView;
        if (uniqueObjectHostView2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hostView");
        }
        int paddingLeft = i2 + uniqueObjectHostView2.getPaddingLeft();
        int i3 = this.tmpLocationOnScreen[1];
        UniqueObjectHostView uniqueObjectHostView3 = this.hostView;
        if (uniqueObjectHostView3 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hostView");
        }
        int paddingTop = i3 + uniqueObjectHostView3.getPaddingTop();
        int i4 = this.tmpLocationOnScreen[0];
        UniqueObjectHostView uniqueObjectHostView4 = this.hostView;
        if (uniqueObjectHostView4 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hostView");
        }
        int width = i4 + uniqueObjectHostView4.getWidth();
        UniqueObjectHostView uniqueObjectHostView5 = this.hostView;
        if (uniqueObjectHostView5 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hostView");
        }
        int paddingRight = width - uniqueObjectHostView5.getPaddingRight();
        int i5 = this.tmpLocationOnScreen[1];
        UniqueObjectHostView uniqueObjectHostView6 = this.hostView;
        if (uniqueObjectHostView6 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hostView");
        }
        int height = i5 + uniqueObjectHostView6.getHeight();
        UniqueObjectHostView uniqueObjectHostView7 = this.hostView;
        if (uniqueObjectHostView7 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hostView");
        }
        int paddingBottom = height - uniqueObjectHostView7.getPaddingBottom();
        if (paddingRight < paddingLeft) {
            paddingLeft = 0;
            paddingRight = 0;
        }
        if (paddingBottom < paddingTop) {
            paddingBottom = 0;
        } else {
            i = paddingTop;
        }
        this.currentBounds.set(paddingLeft, i, paddingRight, paddingBottom);
        return this.currentBounds;
    }

    public final void addVisibilityChangeListener(@NotNull Function1<? super Boolean, Unit> listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        this.visibleChangedListeners.add(listener);
    }

    public final void init(final int i) {
        this.location = i;
        UniqueObjectHostView uniqueObjectHostViewRegister = this.mediaHierarchyManager.register(this);
        this.hostView = uniqueObjectHostViewRegister;
        if (uniqueObjectHostViewRegister == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hostView");
        }
        uniqueObjectHostViewRegister.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() { // from class: com.android.systemui.media.MediaHost.init.1
            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewAttachedToWindow(@Nullable View view) {
                MediaHost.this.mediaDataManager.addListener(MediaHost.this.listener);
                MediaHost.this.updateViewVisibility();
            }

            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewDetachedFromWindow(@Nullable View view) {
                MediaHost.this.mediaDataManager.removeListener(MediaHost.this.listener);
            }
        });
        UniqueObjectHostView uniqueObjectHostView = this.hostView;
        if (uniqueObjectHostView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hostView");
        }
        uniqueObjectHostView.setMeasurementManager(new UniqueObjectHostView.MeasurementManager() { // from class: com.android.systemui.media.MediaHost.init.2
            @Override // com.android.systemui.util.animation.UniqueObjectHostView.MeasurementManager
            @NotNull
            public MeasurementOutput onMeasure(@NotNull MeasurementInput input) {
                Intrinsics.checkParameterIsNotNull(input, "input");
                if (View.MeasureSpec.getMode(input.getWidthMeasureSpec()) == Integer.MIN_VALUE) {
                    input.setWidthMeasureSpec(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(input.getWidthMeasureSpec()), 1073741824));
                }
                MediaHost.this.state.setMeasurementInput(input);
                return MediaHost.this.mediaHostStatesManager.updateCarouselDimensions(i, MediaHost.this.state);
            }
        });
        this.state.setChangedListener(new Function0<Unit>() { // from class: com.android.systemui.media.MediaHost.init.3
            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(0);
            }

            @Override // kotlin.jvm.functions.Function0
            public /* bridge */ /* synthetic */ Unit invoke() {
                invoke2();
                return Unit.INSTANCE;
            }

            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final void invoke2() {
                MediaHost.this.mediaHostStatesManager.updateHostState(i, MediaHost.this.state);
            }
        });
        updateViewVisibility();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updateViewVisibility() {
        boolean zHasAnyMedia;
        if (getShowsOnlyActiveMedia()) {
            zHasAnyMedia = this.mediaDataManager.hasActiveMedia();
        } else {
            zHasAnyMedia = this.mediaDataManager.hasAnyMedia();
        }
        setVisible(zHasAnyMedia);
        int i = getVisible() ? 0 : 8;
        UniqueObjectHostView uniqueObjectHostView = this.hostView;
        if (uniqueObjectHostView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hostView");
        }
        if (i != uniqueObjectHostView.getVisibility()) {
            UniqueObjectHostView uniqueObjectHostView2 = this.hostView;
            if (uniqueObjectHostView2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("hostView");
            }
            uniqueObjectHostView2.setVisibility(i);
            Iterator<T> it = this.visibleChangedListeners.iterator();
            while (it.hasNext()) {
                ((Function1) it.next()).invoke(Boolean.valueOf(getVisible()));
            }
        }
    }

    /* compiled from: MediaHost.kt */
    public static final class MediaHostStateHolder implements MediaHostState {

        @Nullable
        private Function0<Unit> changedListener;
        private float expansion;
        private boolean falsingProtectionNeeded;

        @Nullable
        private MeasurementInput measurementInput;
        private boolean showsOnlyActiveMedia;
        private boolean visible = true;

        @NotNull
        private DisappearParameters disappearParameters = new DisappearParameters();
        private int lastDisappearHash = getDisappearParameters().hashCode();

        @Override // com.android.systemui.media.MediaHostState
        @Nullable
        public MeasurementInput getMeasurementInput() {
            return this.measurementInput;
        }

        public void setMeasurementInput(@Nullable MeasurementInput measurementInput) {
            if (measurementInput == null || !measurementInput.equals(this.measurementInput)) {
                this.measurementInput = measurementInput;
                Function0<Unit> function0 = this.changedListener;
                if (function0 != null) {
                    function0.invoke();
                }
            }
        }

        @Override // com.android.systemui.media.MediaHostState
        public float getExpansion() {
            return this.expansion;
        }

        @Override // com.android.systemui.media.MediaHostState
        public void setExpansion(float f) {
            if (Float.valueOf(f).equals(Float.valueOf(this.expansion))) {
                return;
            }
            this.expansion = f;
            Function0<Unit> function0 = this.changedListener;
            if (function0 != null) {
                function0.invoke();
            }
        }

        @Override // com.android.systemui.media.MediaHostState
        public boolean getShowsOnlyActiveMedia() {
            return this.showsOnlyActiveMedia;
        }

        public void setShowsOnlyActiveMedia(boolean z) {
            if (Boolean.valueOf(z).equals(Boolean.valueOf(this.showsOnlyActiveMedia))) {
                return;
            }
            this.showsOnlyActiveMedia = z;
            Function0<Unit> function0 = this.changedListener;
            if (function0 != null) {
                function0.invoke();
            }
        }

        @Override // com.android.systemui.media.MediaHostState
        public boolean getVisible() {
            return this.visible;
        }

        public void setVisible(boolean z) {
            if (this.visible == z) {
                return;
            }
            this.visible = z;
            Function0<Unit> function0 = this.changedListener;
            if (function0 != null) {
                function0.invoke();
            }
        }

        @Override // com.android.systemui.media.MediaHostState
        public boolean getFalsingProtectionNeeded() {
            return this.falsingProtectionNeeded;
        }

        public void setFalsingProtectionNeeded(boolean z) {
            if (this.falsingProtectionNeeded == z) {
                return;
            }
            this.falsingProtectionNeeded = z;
            Function0<Unit> function0 = this.changedListener;
            if (function0 != null) {
                function0.invoke();
            }
        }

        @Override // com.android.systemui.media.MediaHostState
        @NotNull
        public DisappearParameters getDisappearParameters() {
            return this.disappearParameters;
        }

        public void setDisappearParameters(@NotNull DisappearParameters value) {
            Intrinsics.checkParameterIsNotNull(value, "value");
            int iHashCode = value.hashCode();
            if (Integer.valueOf(this.lastDisappearHash).equals(Integer.valueOf(iHashCode))) {
                return;
            }
            this.disappearParameters = value;
            this.lastDisappearHash = iHashCode;
            Function0<Unit> function0 = this.changedListener;
            if (function0 != null) {
                function0.invoke();
            }
        }

        public final void setChangedListener(@Nullable Function0<Unit> function0) {
            this.changedListener = function0;
        }

        @Override // com.android.systemui.media.MediaHostState
        @NotNull
        public MediaHostState copy() {
            MediaHostStateHolder mediaHostStateHolder = new MediaHostStateHolder();
            mediaHostStateHolder.setExpansion(getExpansion());
            mediaHostStateHolder.setShowsOnlyActiveMedia(getShowsOnlyActiveMedia());
            MeasurementInput measurementInput = getMeasurementInput();
            mediaHostStateHolder.setMeasurementInput(measurementInput != null ? MeasurementInput.copy$default(measurementInput, 0, 0, 3, null) : null);
            mediaHostStateHolder.setVisible(getVisible());
            mediaHostStateHolder.setDisappearParameters(getDisappearParameters().deepCopy());
            mediaHostStateHolder.setFalsingProtectionNeeded(getFalsingProtectionNeeded());
            return mediaHostStateHolder;
        }

        public boolean equals(@Nullable Object obj) {
            if (!(obj instanceof MediaHostState)) {
                return false;
            }
            MediaHostState mediaHostState = (MediaHostState) obj;
            return Objects.equals(getMeasurementInput(), mediaHostState.getMeasurementInput()) && getExpansion() == mediaHostState.getExpansion() && getShowsOnlyActiveMedia() == mediaHostState.getShowsOnlyActiveMedia() && getVisible() == mediaHostState.getVisible() && getFalsingProtectionNeeded() == mediaHostState.getFalsingProtectionNeeded() && getDisappearParameters().equals(mediaHostState.getDisappearParameters());
        }

        public int hashCode() {
            MeasurementInput measurementInput = getMeasurementInput();
            return ((((((((((measurementInput != null ? measurementInput.hashCode() : 0) * 31) + Float.hashCode(getExpansion())) * 31) + Boolean.hashCode(getFalsingProtectionNeeded())) * 31) + Boolean.hashCode(getShowsOnlyActiveMedia())) * 31) + (getVisible() ? 1 : 2)) * 31) + getDisappearParameters().hashCode();
        }
    }
}
