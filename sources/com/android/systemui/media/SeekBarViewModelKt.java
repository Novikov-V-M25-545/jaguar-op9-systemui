package com.android.systemui.media;

import android.media.session.PlaybackState;
import android.os.SystemClock;
import org.jetbrains.annotations.NotNull;

/* compiled from: SeekBarViewModel.kt */
/* loaded from: classes.dex */
public final class SeekBarViewModelKt {
    /* JADX INFO: Access modifiers changed from: private */
    public static final boolean isInMotion(@NotNull PlaybackState playbackState) {
        return playbackState.getState() == 3 || playbackState.getState() == 4 || playbackState.getState() == 5;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final long computePosition(@NotNull PlaybackState playbackState, long j) {
        long position = playbackState.getPosition();
        if (!isInMotion(playbackState)) {
            return position;
        }
        long lastPositionUpdateTime = playbackState.getLastPositionUpdateTime();
        long jElapsedRealtime = SystemClock.elapsedRealtime();
        if (lastPositionUpdateTime <= 0) {
            return position;
        }
        long playbackSpeed = ((long) (playbackState.getPlaybackSpeed() * (jElapsedRealtime - lastPositionUpdateTime))) + playbackState.getPosition();
        if (j < 0 || playbackSpeed <= j) {
            j = playbackSpeed < 0 ? 0L : playbackSpeed;
        }
        return j;
    }
}
