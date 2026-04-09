package com.android.systemui.media;

import com.android.systemui.media.MediaDataManager;
import com.android.systemui.media.MediaDeviceManager;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import lineageos.hardware.LineageHardwareManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaDataCombineLatest.kt */
/* loaded from: classes.dex */
public final class MediaDataCombineLatest implements MediaDataManager.Listener, MediaDeviceManager.Listener {
    private final Set<MediaDataManager.Listener> listeners = new LinkedHashSet();
    private final Map<String, Pair<MediaData, MediaDeviceData>> entries = new LinkedHashMap();

    @Override // com.android.systemui.media.MediaDataManager.Listener
    public void onMediaDataLoaded(@NotNull String key, @Nullable String str, @NotNull MediaData data) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        Intrinsics.checkParameterIsNotNull(data, "data");
        if (str != null && (!Intrinsics.areEqual(str, key)) && this.entries.containsKey(str)) {
            Map<String, Pair<MediaData, MediaDeviceData>> map = this.entries;
            Pair<MediaData, MediaDeviceData> pairRemove = map.remove(str);
            map.put(key, TuplesKt.to(data, pairRemove != null ? pairRemove.getSecond() : null));
            update(key, str);
            return;
        }
        Map<String, Pair<MediaData, MediaDeviceData>> map2 = this.entries;
        Pair<MediaData, MediaDeviceData> pair = map2.get(key);
        map2.put(key, TuplesKt.to(data, pair != null ? pair.getSecond() : null));
        update(key, key);
    }

    @Override // com.android.systemui.media.MediaDataManager.Listener
    public void onMediaDataRemoved(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        remove(key);
    }

    @Override // com.android.systemui.media.MediaDeviceManager.Listener
    public void onMediaDeviceChanged(@NotNull String key, @Nullable String str, @Nullable MediaDeviceData mediaDeviceData) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        if (str != null && (!Intrinsics.areEqual(str, key)) && this.entries.containsKey(str)) {
            Map<String, Pair<MediaData, MediaDeviceData>> map = this.entries;
            Pair<MediaData, MediaDeviceData> pairRemove = map.remove(str);
            map.put(key, TuplesKt.to(pairRemove != null ? pairRemove.getFirst() : null, mediaDeviceData));
            update(key, str);
            return;
        }
        Map<String, Pair<MediaData, MediaDeviceData>> map2 = this.entries;
        Pair<MediaData, MediaDeviceData> pair = map2.get(key);
        map2.put(key, TuplesKt.to(pair != null ? pair.getFirst() : null, mediaDeviceData));
        update(key, key);
    }

    @Override // com.android.systemui.media.MediaDeviceManager.Listener
    public void onKeyRemoved(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        remove(key);
    }

    public final boolean addListener(@NotNull MediaDataManager.Listener listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        return this.listeners.add(listener);
    }

    private final void update(String str, String str2) {
        Pair<MediaData, MediaDeviceData> pair = this.entries.get(str);
        if (pair == null) {
            pair = TuplesKt.to(null, null);
        }
        MediaData mediaDataComponent1 = pair.component1();
        MediaDeviceData mediaDeviceDataComponent2 = pair.component2();
        if (mediaDataComponent1 == null || mediaDeviceDataComponent2 == null) {
            return;
        }
        MediaData mediaDataCopy = mediaDataComponent1.copy((3636223 & 1) != 0 ? mediaDataComponent1.userId : 0, (3636223 & 2) != 0 ? mediaDataComponent1.initialized : false, (3636223 & 4) != 0 ? mediaDataComponent1.backgroundColor : 0, (3636223 & 8) != 0 ? mediaDataComponent1.app : null, (3636223 & 16) != 0 ? mediaDataComponent1.appIcon : null, (3636223 & 32) != 0 ? mediaDataComponent1.artist : null, (3636223 & 64) != 0 ? mediaDataComponent1.song : null, (3636223 & 128) != 0 ? mediaDataComponent1.artwork : null, (3636223 & LineageHardwareManager.FEATURE_SUNLIGHT_ENHANCEMENT) != 0 ? mediaDataComponent1.actions : null, (3636223 & 512) != 0 ? mediaDataComponent1.actionsToShowInCompact : null, (3636223 & LineageHardwareManager.FEATURE_VIBRATOR) != 0 ? mediaDataComponent1.packageName : null, (3636223 & LineageHardwareManager.FEATURE_TOUCH_HOVERING) != 0 ? mediaDataComponent1.token : null, (3636223 & LineageHardwareManager.FEATURE_AUTO_CONTRAST) != 0 ? mediaDataComponent1.clickIntent : null, (3636223 & LineageHardwareManager.FEATURE_DISPLAY_MODES) != 0 ? mediaDataComponent1.device : mediaDeviceDataComponent2, (3636223 & LineageHardwareManager.FEATURE_READING_ENHANCEMENT) != 0 ? mediaDataComponent1.active : false, (3636223 & 32768) != 0 ? mediaDataComponent1.resumeAction : null, (3636223 & 65536) != 0 ? mediaDataComponent1.isLocalSession : false, (3636223 & LineageHardwareManager.FEATURE_COLOR_BALANCE) != 0 ? mediaDataComponent1.resumption : false, (3636223 & LineageHardwareManager.FEATURE_PICTURE_ADJUSTMENT) != 0 ? mediaDataComponent1.notificationKey : null, (3636223 & LineageHardwareManager.FEATURE_TOUCHSCREEN_GESTURES) != 0 ? mediaDataComponent1.hasCheckedForResume : false, (3636223 & 1048576) != 0 ? mediaDataComponent1.isPlaying : null, (3636223 & LineageHardwareManager.FEATURE_ANTI_FLICKER) != 0 ? mediaDataComponent1.isClearable : false);
        Iterator it = CollectionsKt___CollectionsKt.toSet(this.listeners).iterator();
        while (it.hasNext()) {
            ((MediaDataManager.Listener) it.next()).onMediaDataLoaded(str, str2, mediaDataCopy);
        }
    }

    private final void remove(String str) {
        if (this.entries.remove(str) != null) {
            Iterator it = CollectionsKt___CollectionsKt.toSet(this.listeners).iterator();
            while (it.hasNext()) {
                ((MediaDataManager.Listener) it.next()).onMediaDataRemoved(str);
            }
        }
    }
}
