package com.android.systemui.media;

import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.SeekBar;
import androidx.core.view.GestureDetectorCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.android.systemui.util.concurrency.RepeatableExecutor;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: SeekBarViewModel.kt */
/* loaded from: classes.dex */
public final class SeekBarViewModel {
    private Progress _data;
    private final MutableLiveData<Progress> _progress;
    private final RepeatableExecutor bgExecutor;
    private SeekBarViewModel$callback$1 callback;
    private Runnable cancel;
    private MediaController controller;
    private boolean isFalseSeek;
    private boolean listening;
    private PlaybackState playbackState;
    private boolean scrubbing;

    /* compiled from: SeekBarViewModel.kt */
    /* renamed from: com.android.systemui.media.SeekBarViewModel$checkIfPollingNeeded$1, reason: invalid class name */
    static final /* synthetic */ class AnonymousClass1 extends FunctionReference implements Function0<Unit> {
        AnonymousClass1(SeekBarViewModel seekBarViewModel) {
            super(0, seekBarViewModel);
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final String getName() {
            return "checkPlaybackPosition";
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final KDeclarationContainer getOwner() {
            return Reflection.getOrCreateKotlinClass(SeekBarViewModel.class);
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final String getSignature() {
            return "checkPlaybackPosition()V";
        }

        @Override // kotlin.jvm.functions.Function0
        public /* bridge */ /* synthetic */ Unit invoke() {
            invoke2();
            return Unit.INSTANCE;
        }

        /* renamed from: invoke, reason: avoid collision after fix types in other method */
        public final void invoke2() {
            ((SeekBarViewModel) this.receiver).checkPlaybackPosition();
        }
    }

    /* JADX WARN: Type inference failed for: r3v3, types: [com.android.systemui.media.SeekBarViewModel$callback$1] */
    public SeekBarViewModel(@NotNull RepeatableExecutor bgExecutor) {
        Intrinsics.checkParameterIsNotNull(bgExecutor, "bgExecutor");
        this.bgExecutor = bgExecutor;
        this._data = new Progress(false, false, null, 0);
        MutableLiveData<Progress> mutableLiveData = new MutableLiveData<>();
        mutableLiveData.postValue(this._data);
        this._progress = mutableLiveData;
        this.callback = new MediaController.Callback() { // from class: com.android.systemui.media.SeekBarViewModel$callback$1
            @Override // android.media.session.MediaController.Callback
            public void onPlaybackStateChanged(@Nullable PlaybackState playbackState) {
                this.this$0.playbackState = playbackState;
                if (this.this$0.playbackState != null) {
                    Integer num = 0;
                    if (!num.equals(this.this$0.playbackState)) {
                        this.this$0.checkIfPollingNeeded();
                        return;
                    }
                }
                this.this$0.clearController();
            }

            @Override // android.media.session.MediaController.Callback
            public void onSessionDestroyed() {
                this.this$0.clearController();
            }
        };
        this.listening = true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void set_data(Progress progress) {
        this._data = progress;
        this._progress.postValue(progress);
    }

    @NotNull
    public final LiveData<Progress> getProgress() {
        return this._progress;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void setController(MediaController mediaController) {
        if (!Intrinsics.areEqual(this.controller != null ? r0.getSessionToken() : null, mediaController != null ? mediaController.getSessionToken() : null)) {
            MediaController mediaController2 = this.controller;
            if (mediaController2 != null) {
                mediaController2.unregisterCallback(this.callback);
            }
            if (mediaController != null) {
                mediaController.registerCallback(this.callback);
            }
            this.controller = mediaController;
        }
    }

    public final void setListening(final boolean z) {
        this.bgExecutor.execute(new Runnable() { // from class: com.android.systemui.media.SeekBarViewModel$listening$1
            @Override // java.lang.Runnable
            public final void run() {
                boolean z2 = this.this$0.listening;
                boolean z3 = z;
                if (z2 != z3) {
                    this.this$0.listening = z3;
                    this.this$0.checkIfPollingNeeded();
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void setScrubbing(boolean z) {
        if (this.scrubbing != z) {
            this.scrubbing = z;
            checkIfPollingNeeded();
        }
    }

    public final void onSeekStarting() {
        this.bgExecutor.execute(new Runnable() { // from class: com.android.systemui.media.SeekBarViewModel.onSeekStarting.1
            @Override // java.lang.Runnable
            public final void run() {
                SeekBarViewModel.this.setScrubbing(true);
                SeekBarViewModel.this.isFalseSeek = false;
            }
        });
    }

    public final void onSeekProgress(final long j) {
        this.bgExecutor.execute(new Runnable() { // from class: com.android.systemui.media.SeekBarViewModel.onSeekProgress.1
            @Override // java.lang.Runnable
            public final void run() {
                if (SeekBarViewModel.this.scrubbing) {
                    SeekBarViewModel seekBarViewModel = SeekBarViewModel.this;
                    seekBarViewModel.set_data(Progress.copy$default(seekBarViewModel._data, false, false, Integer.valueOf((int) j), 0, 11, null));
                }
            }
        });
    }

    public final void onSeekFalse() {
        this.bgExecutor.execute(new Runnable() { // from class: com.android.systemui.media.SeekBarViewModel.onSeekFalse.1
            @Override // java.lang.Runnable
            public final void run() {
                if (SeekBarViewModel.this.scrubbing) {
                    SeekBarViewModel.this.isFalseSeek = true;
                }
            }
        });
    }

    public final void onSeek(final long j) {
        this.bgExecutor.execute(new Runnable() { // from class: com.android.systemui.media.SeekBarViewModel.onSeek.1
            @Override // java.lang.Runnable
            public final void run() {
                MediaController.TransportControls transportControls;
                if (SeekBarViewModel.this.isFalseSeek) {
                    SeekBarViewModel.this.setScrubbing(false);
                    SeekBarViewModel.this.checkPlaybackPosition();
                    return;
                }
                MediaController mediaController = SeekBarViewModel.this.controller;
                if (mediaController != null && (transportControls = mediaController.getTransportControls()) != null) {
                    transportControls.seekTo(j);
                }
                SeekBarViewModel.this.playbackState = null;
                SeekBarViewModel.this.setScrubbing(false);
            }
        });
    }

    public final void updateController(@Nullable MediaController mediaController) {
        setController(mediaController);
        MediaController mediaController2 = this.controller;
        this.playbackState = mediaController2 != null ? mediaController2.getPlaybackState() : null;
        MediaController mediaController3 = this.controller;
        MediaMetadata metadata = mediaController3 != null ? mediaController3.getMetadata() : null;
        PlaybackState playbackState = this.playbackState;
        boolean z = ((playbackState != null ? playbackState.getActions() : 0L) & 256) != 0;
        PlaybackState playbackState2 = this.playbackState;
        Integer numValueOf = playbackState2 != null ? Integer.valueOf((int) playbackState2.getPosition()) : null;
        int i = metadata != null ? (int) metadata.getLong("android.media.metadata.DURATION") : 0;
        PlaybackState playbackState3 = this.playbackState;
        set_data(new Progress(playbackState3 != null && (playbackState3 == null || playbackState3.getState() != 0) && i > 0, z, numValueOf, i));
        checkIfPollingNeeded();
    }

    public final void clearController() {
        this.bgExecutor.execute(new Runnable() { // from class: com.android.systemui.media.SeekBarViewModel.clearController.1
            @Override // java.lang.Runnable
            public final void run() {
                SeekBarViewModel.this.setController(null);
                SeekBarViewModel.this.playbackState = null;
                Runnable runnable = SeekBarViewModel.this.cancel;
                if (runnable != null) {
                    runnable.run();
                }
                SeekBarViewModel.this.cancel = null;
                SeekBarViewModel seekBarViewModel = SeekBarViewModel.this;
                seekBarViewModel.set_data(Progress.copy$default(seekBarViewModel._data, false, false, null, 0, 14, null));
            }
        });
    }

    public final void onDestroy() {
        this.bgExecutor.execute(new Runnable() { // from class: com.android.systemui.media.SeekBarViewModel.onDestroy.1
            @Override // java.lang.Runnable
            public final void run() {
                SeekBarViewModel.this.setController(null);
                SeekBarViewModel.this.playbackState = null;
                Runnable runnable = SeekBarViewModel.this.cancel;
                if (runnable != null) {
                    runnable.run();
                }
                SeekBarViewModel.this.cancel = null;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void checkPlaybackPosition() {
        int duration = this._data.getDuration();
        PlaybackState playbackState = this.playbackState;
        Integer numValueOf = playbackState != null ? Integer.valueOf((int) SeekBarViewModelKt.computePosition(playbackState, duration)) : null;
        if (numValueOf == null || !(!Intrinsics.areEqual(this._data.getElapsedTime(), numValueOf))) {
            return;
        }
        set_data(Progress.copy$default(this._data, false, false, numValueOf, 0, 11, null));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void checkIfPollingNeeded() {
        boolean z = false;
        if (this.listening && !this.scrubbing) {
            PlaybackState playbackState = this.playbackState;
            if (playbackState != null ? SeekBarViewModelKt.isInMotion(playbackState) : false) {
                z = true;
            }
        }
        if (z) {
            if (this.cancel == null) {
                RepeatableExecutor repeatableExecutor = this.bgExecutor;
                final AnonymousClass1 anonymousClass1 = new AnonymousClass1(this);
                this.cancel = repeatableExecutor.executeRepeatedly(new Runnable() { // from class: com.android.systemui.media.SeekBarViewModelKt$sam$java_lang_Runnable$0
                    @Override // java.lang.Runnable
                    public final /* synthetic */ void run() {
                        Intrinsics.checkExpressionValueIsNotNull(anonymousClass1.invoke(), "invoke(...)");
                    }
                }, 0L, 100L);
                return;
            }
            return;
        }
        Runnable runnable = this.cancel;
        if (runnable != null) {
            runnable.run();
        }
        this.cancel = null;
    }

    @NotNull
    public final SeekBar.OnSeekBarChangeListener getSeekBarListener() {
        return new SeekBarChangeListener(this);
    }

    public final void attachTouchHandlers(@NotNull SeekBar bar) {
        Intrinsics.checkParameterIsNotNull(bar, "bar");
        bar.setOnSeekBarChangeListener(getSeekBarListener());
        bar.setOnTouchListener(new SeekBarTouchListener(this, bar));
    }

    /* compiled from: SeekBarViewModel.kt */
    private static final class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @NotNull
        private final SeekBarViewModel viewModel;

        public SeekBarChangeListener(@NotNull SeekBarViewModel viewModel) {
            Intrinsics.checkParameterIsNotNull(viewModel, "viewModel");
            this.viewModel = viewModel;
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onProgressChanged(@NotNull SeekBar bar, int i, boolean z) {
            Intrinsics.checkParameterIsNotNull(bar, "bar");
            if (z) {
                this.viewModel.onSeekProgress(i);
            }
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onStartTrackingTouch(@NotNull SeekBar bar) {
            Intrinsics.checkParameterIsNotNull(bar, "bar");
            this.viewModel.onSeekStarting();
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onStopTrackingTouch(@NotNull SeekBar bar) {
            Intrinsics.checkParameterIsNotNull(bar, "bar");
            this.viewModel.onSeek(bar.getProgress());
        }
    }

    /* compiled from: SeekBarViewModel.kt */
    private static final class SeekBarTouchListener implements View.OnTouchListener, GestureDetector.OnGestureListener {
        private final SeekBar bar;
        private final GestureDetectorCompat detector;
        private final int flingVelocity;
        private boolean shouldGoToSeekBar;
        private final SeekBarViewModel viewModel;

        @Override // android.view.GestureDetector.OnGestureListener
        public void onLongPress(@NotNull MotionEvent event) {
            Intrinsics.checkParameterIsNotNull(event, "event");
        }

        @Override // android.view.GestureDetector.OnGestureListener
        public void onShowPress(@NotNull MotionEvent event) {
            Intrinsics.checkParameterIsNotNull(event, "event");
        }

        public SeekBarTouchListener(@NotNull SeekBarViewModel viewModel, @NotNull SeekBar bar) {
            Intrinsics.checkParameterIsNotNull(viewModel, "viewModel");
            Intrinsics.checkParameterIsNotNull(bar, "bar");
            this.viewModel = viewModel;
            this.bar = bar;
            this.detector = new GestureDetectorCompat(bar.getContext(), this);
            this.flingVelocity = ViewConfiguration.get(bar.getContext()).getScaledMinimumFlingVelocity() * 10;
        }

        @Override // android.view.View.OnTouchListener
        public boolean onTouch(@NotNull View view, @NotNull MotionEvent event) {
            Intrinsics.checkParameterIsNotNull(view, "view");
            Intrinsics.checkParameterIsNotNull(event, "event");
            if (!Intrinsics.areEqual(view, this.bar)) {
                return false;
            }
            this.detector.onTouchEvent(event);
            return !this.shouldGoToSeekBar;
        }

        @Override // android.view.GestureDetector.OnGestureListener
        public boolean onDown(@NotNull MotionEvent event) {
            double d;
            double d2;
            ViewParent parent;
            Intrinsics.checkParameterIsNotNull(event, "event");
            int paddingLeft = this.bar.getPaddingLeft();
            int paddingRight = this.bar.getPaddingRight();
            int progress = this.bar.getProgress();
            int max = this.bar.getMax() - this.bar.getMin();
            double min = max > 0 ? (progress - this.bar.getMin()) / max : 0.0d;
            int width = (this.bar.getWidth() - paddingLeft) - paddingRight;
            if (this.bar.isLayoutRtl()) {
                d = paddingLeft;
                d2 = width * (1 - min);
            } else {
                d = paddingLeft;
                d2 = width * min;
            }
            double d3 = d + d2;
            long height = this.bar.getHeight() / 2;
            int iRound = (int) (Math.round(d3) - height);
            int iRound2 = (int) (Math.round(d3) + height);
            int iRound3 = Math.round(event.getX());
            boolean z = iRound3 >= iRound && iRound3 <= iRound2;
            this.shouldGoToSeekBar = z;
            if (z && (parent = this.bar.getParent()) != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
            return this.shouldGoToSeekBar;
        }

        @Override // android.view.GestureDetector.OnGestureListener
        public boolean onSingleTapUp(@NotNull MotionEvent event) {
            Intrinsics.checkParameterIsNotNull(event, "event");
            this.shouldGoToSeekBar = true;
            return true;
        }

        @Override // android.view.GestureDetector.OnGestureListener
        public boolean onScroll(@NotNull MotionEvent eventStart, @NotNull MotionEvent event, float f, float f2) {
            Intrinsics.checkParameterIsNotNull(eventStart, "eventStart");
            Intrinsics.checkParameterIsNotNull(event, "event");
            return this.shouldGoToSeekBar;
        }

        @Override // android.view.GestureDetector.OnGestureListener
        public boolean onFling(@NotNull MotionEvent eventStart, @NotNull MotionEvent event, float f, float f2) {
            Intrinsics.checkParameterIsNotNull(eventStart, "eventStart");
            Intrinsics.checkParameterIsNotNull(event, "event");
            if (Math.abs(f) > this.flingVelocity || Math.abs(f2) > this.flingVelocity) {
                this.viewModel.onSeekFalse();
            }
            return this.shouldGoToSeekBar;
        }
    }

    /* compiled from: SeekBarViewModel.kt */
    public static final class Progress {
        private final int duration;

        @Nullable
        private final Integer elapsedTime;
        private final boolean enabled;
        private final boolean seekAvailable;

        public static /* synthetic */ Progress copy$default(Progress progress, boolean z, boolean z2, Integer num, int i, int i2, Object obj) {
            if ((i2 & 1) != 0) {
                z = progress.enabled;
            }
            if ((i2 & 2) != 0) {
                z2 = progress.seekAvailable;
            }
            if ((i2 & 4) != 0) {
                num = progress.elapsedTime;
            }
            if ((i2 & 8) != 0) {
                i = progress.duration;
            }
            return progress.copy(z, z2, num, i);
        }

        @NotNull
        public final Progress copy(boolean z, boolean z2, @Nullable Integer num, int i) {
            return new Progress(z, z2, num, i);
        }

        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Progress)) {
                return false;
            }
            Progress progress = (Progress) obj;
            return this.enabled == progress.enabled && this.seekAvailable == progress.seekAvailable && Intrinsics.areEqual(this.elapsedTime, progress.elapsedTime) && this.duration == progress.duration;
        }

        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r0v1, types: [int] */
        /* JADX WARN: Type inference failed for: r0v8 */
        /* JADX WARN: Type inference failed for: r0v9 */
        public int hashCode() {
            boolean z = this.enabled;
            ?? r0 = z;
            if (z) {
                r0 = 1;
            }
            int i = r0 * 31;
            boolean z2 = this.seekAvailable;
            int i2 = (i + (z2 ? 1 : z2 ? 1 : 0)) * 31;
            Integer num = this.elapsedTime;
            return ((i2 + (num != null ? num.hashCode() : 0)) * 31) + Integer.hashCode(this.duration);
        }

        @NotNull
        public String toString() {
            return "Progress(enabled=" + this.enabled + ", seekAvailable=" + this.seekAvailable + ", elapsedTime=" + this.elapsedTime + ", duration=" + this.duration + ")";
        }

        public Progress(boolean z, boolean z2, @Nullable Integer num, int i) {
            this.enabled = z;
            this.seekAvailable = z2;
            this.elapsedTime = num;
            this.duration = i;
        }

        public final boolean getEnabled() {
            return this.enabled;
        }

        public final boolean getSeekAvailable() {
            return this.seekAvailable;
        }

        @Nullable
        public final Integer getElapsedTime() {
            return this.elapsedTime;
        }

        public final int getDuration() {
            return this.duration;
        }
    }
}
