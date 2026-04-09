package com.android.systemui.media;

import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.util.Log;
import com.android.systemui.media.MediaDataManager;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.util.concurrency.DelayableExecutor;
import java.util.LinkedHashMap;
import java.util.Map;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.TypeIntrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaTimeoutListener.kt */
/* loaded from: classes.dex */
public final class MediaTimeoutListener implements MediaDataManager.Listener {
    private final DelayableExecutor mainExecutor;
    private final MediaControllerFactory mediaControllerFactory;
    private final Map<String, PlaybackStateListener> mediaListeners;

    @NotNull
    public Function2<? super String, ? super Boolean, Unit> timeoutCallback;

    public MediaTimeoutListener(@NotNull MediaControllerFactory mediaControllerFactory, @NotNull DelayableExecutor mainExecutor) {
        Intrinsics.checkParameterIsNotNull(mediaControllerFactory, "mediaControllerFactory");
        Intrinsics.checkParameterIsNotNull(mainExecutor, "mainExecutor");
        this.mediaControllerFactory = mediaControllerFactory;
        this.mainExecutor = mainExecutor;
        this.mediaListeners = new LinkedHashMap();
    }

    @NotNull
    public final Function2<String, Boolean, Unit> getTimeoutCallback() {
        Function2 function2 = this.timeoutCallback;
        if (function2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("timeoutCallback");
        }
        return function2;
    }

    public final void setTimeoutCallback(@NotNull Function2<? super String, ? super Boolean, Unit> function2) {
        Intrinsics.checkParameterIsNotNull(function2, "<set-?>");
        this.timeoutCallback = function2;
    }

    @Override // com.android.systemui.media.MediaDataManager.Listener
    public void onMediaDataLoaded(@NotNull final String key, @Nullable String str, @NotNull MediaData data) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        Intrinsics.checkParameterIsNotNull(data, "data");
        if (this.mediaListeners.containsKey(key)) {
            return;
        }
        if (str != null && (Intrinsics.areEqual(key, str) ^ true)) {
            Map<String, PlaybackStateListener> map = this.mediaListeners;
            if (map == null) {
                throw new TypeCastException("null cannot be cast to non-null type kotlin.collections.MutableMap<K, V>");
            }
            PlaybackStateListener playbackStateListener = (PlaybackStateListener) TypeIntrinsics.asMutableMap(map).remove(str);
            if (playbackStateListener != null) {
                Boolean playing = playbackStateListener.getPlaying();
                boolean zBooleanValue = playing != null ? playing.booleanValue() : false;
                Log.d("MediaTimeout", "migrating key " + str + " to " + key + ", for resumption");
                playbackStateListener.setMediaData(data);
                playbackStateListener.setKey(key);
                this.mediaListeners.put(key, playbackStateListener);
                if (!Intrinsics.areEqual(Boolean.valueOf(zBooleanValue), playbackStateListener.getPlaying())) {
                    this.mainExecutor.execute(new Runnable() { // from class: com.android.systemui.media.MediaTimeoutListener.onMediaDataLoaded.1
                        @Override // java.lang.Runnable
                        public final void run() {
                            PlaybackStateListener playbackStateListener2 = (PlaybackStateListener) MediaTimeoutListener.this.mediaListeners.get(key);
                            if (Intrinsics.areEqual(playbackStateListener2 != null ? playbackStateListener2.getPlaying() : null, Boolean.TRUE)) {
                                Log.d("MediaTimeout", "deliver delayed playback state for " + key);
                                MediaTimeoutListener.this.getTimeoutCallback().invoke(key, Boolean.FALSE);
                            }
                        }
                    });
                    return;
                }
                return;
            }
            Log.w("MediaTimeout", "Old key " + str + " for player " + key + " doesn't exist. Continuing...");
        }
        this.mediaListeners.put(key, new PlaybackStateListener(this, key, data));
    }

    @Override // com.android.systemui.media.MediaDataManager.Listener
    public void onMediaDataRemoved(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        PlaybackStateListener playbackStateListenerRemove = this.mediaListeners.remove(key);
        if (playbackStateListenerRemove != null) {
            playbackStateListenerRemove.destroy();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* compiled from: MediaTimeoutListener.kt */
    final class PlaybackStateListener extends MediaController.Callback {
        private Runnable cancellation;

        @NotNull
        private String key;
        private MediaController mediaController;

        @NotNull
        private MediaData mediaData;

        @Nullable
        private Boolean playing;
        final /* synthetic */ MediaTimeoutListener this$0;
        private boolean timedOut;

        public PlaybackStateListener(@NotNull MediaTimeoutListener mediaTimeoutListener, @NotNull String key, MediaData data) {
            Intrinsics.checkParameterIsNotNull(key, "key");
            Intrinsics.checkParameterIsNotNull(data, "data");
            this.this$0 = mediaTimeoutListener;
            this.key = key;
            this.mediaData = data;
            setMediaData(data);
        }

        @NotNull
        public final String getKey() {
            return this.key;
        }

        public final void setKey(@NotNull String str) {
            Intrinsics.checkParameterIsNotNull(str, "<set-?>");
            this.key = str;
        }

        public final boolean getTimedOut() {
            return this.timedOut;
        }

        public final void setTimedOut(boolean z) {
            this.timedOut = z;
        }

        @Nullable
        public final Boolean getPlaying() {
            return this.playing;
        }

        public final void setMediaData(@NotNull MediaData value) {
            Intrinsics.checkParameterIsNotNull(value, "value");
            MediaController mediaController = this.mediaController;
            if (mediaController != null) {
                mediaController.unregisterCallback(this);
            }
            this.mediaData = value;
            MediaController mediaControllerCreate = value.getToken() != null ? this.this$0.mediaControllerFactory.create(this.mediaData.getToken()) : null;
            this.mediaController = mediaControllerCreate;
            if (mediaControllerCreate != null) {
                mediaControllerCreate.registerCallback(this);
            }
            MediaController mediaController2 = this.mediaController;
            processState(mediaController2 != null ? mediaController2.getPlaybackState() : null, false);
        }

        public final void destroy() {
            MediaController mediaController = this.mediaController;
            if (mediaController != null) {
                mediaController.unregisterCallback(this);
            }
            Runnable runnable = this.cancellation;
            if (runnable != null) {
                runnable.run();
            }
        }

        @Override // android.media.session.MediaController.Callback
        public void onPlaybackStateChanged(@Nullable PlaybackState playbackState) {
            processState(playbackState, true);
        }

        private final void processState(PlaybackState playbackState, boolean z) {
            Log.v("MediaTimeout", "processState: " + playbackState);
            boolean z2 = playbackState != null && NotificationMediaManager.isPlayingState(playbackState.getState());
            if (!Intrinsics.areEqual(this.playing, Boolean.valueOf(z2)) || this.playing == null) {
                this.playing = Boolean.valueOf(z2);
                if (!z2) {
                    Log.v("MediaTimeout", "schedule timeout for " + this.key);
                    if (this.cancellation != null) {
                        Log.d("MediaTimeout", "cancellation already exists, continuing.");
                        return;
                    }
                    expireMediaTimeout(this.key, "PLAYBACK STATE CHANGED - " + playbackState);
                    this.cancellation = this.this$0.mainExecutor.executeDelayed(new Runnable() { // from class: com.android.systemui.media.MediaTimeoutListener$PlaybackStateListener$processState$1
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.this$0.cancellation = null;
                            Log.v("MediaTimeout", "Execute timeout for " + this.this$0.getKey());
                            this.this$0.setTimedOut(true);
                            this.this$0.this$0.getTimeoutCallback().invoke(this.this$0.getKey(), Boolean.valueOf(this.this$0.getTimedOut()));
                        }
                    }, MediaTimeoutListenerKt.PAUSED_MEDIA_TIMEOUT);
                    return;
                }
                expireMediaTimeout(this.key, "playback started - " + playbackState + ", " + this.key);
                this.timedOut = false;
                if (z) {
                    this.this$0.getTimeoutCallback().invoke(this.key, Boolean.valueOf(this.timedOut));
                }
            }
        }

        private final void expireMediaTimeout(String str, String str2) {
            Runnable runnable = this.cancellation;
            if (runnable != null) {
                Log.v("MediaTimeout", "media timeout cancelled for  " + str + ", reason: " + str2);
                runnable.run();
            }
            this.cancellation = null;
        }
    }
}
