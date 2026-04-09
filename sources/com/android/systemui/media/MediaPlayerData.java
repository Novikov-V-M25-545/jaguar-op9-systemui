package com.android.systemui.media;

import com.android.systemui.media.MediaPlayerData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import kotlin.Pair;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.comparisons.ComparisonsKt__ComparisonsKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaCarouselController.kt */
/* loaded from: classes.dex */
public final class MediaPlayerData {
    public static final MediaPlayerData INSTANCE = new MediaPlayerData();
    private static final Comparator<MediaSortKey> comparator;
    private static final Map<String, MediaSortKey> mediaData;
    private static final TreeMap<MediaSortKey, MediaControlPanel> mediaPlayers;

    /* JADX INFO: Access modifiers changed from: private */
    /* compiled from: MediaCarouselController.kt */
    static final class MediaSortKey {

        @NotNull
        private final MediaData data;
        private final long updateTime;

        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof MediaSortKey)) {
                return false;
            }
            MediaSortKey mediaSortKey = (MediaSortKey) obj;
            return Intrinsics.areEqual(this.data, mediaSortKey.data) && this.updateTime == mediaSortKey.updateTime;
        }

        public int hashCode() {
            MediaData mediaData = this.data;
            return ((mediaData != null ? mediaData.hashCode() : 0) * 31) + Long.hashCode(this.updateTime);
        }

        @NotNull
        public String toString() {
            return "MediaSortKey(data=" + this.data + ", updateTime=" + this.updateTime + ")";
        }

        public MediaSortKey(@NotNull MediaData data, long j) {
            Intrinsics.checkParameterIsNotNull(data, "data");
            this.data = data;
            this.updateTime = j;
        }

        @NotNull
        public final MediaData getData() {
            return this.data;
        }

        public final long getUpdateTime() {
            return this.updateTime;
        }
    }

    static {
        final Comparator<T> comparator2 = new Comparator<T>() { // from class: com.android.systemui.media.MediaPlayerData$$special$$inlined$compareByDescending$1
            /* JADX WARN: Multi-variable type inference failed */
            @Override // java.util.Comparator
            public final int compare(T t, T t2) {
                return ComparisonsKt__ComparisonsKt.compareValues(((MediaPlayerData.MediaSortKey) t2).getData().isPlaying(), ((MediaPlayerData.MediaSortKey) t).getData().isPlaying());
            }
        };
        final Comparator<T> comparator3 = new Comparator<T>() { // from class: com.android.systemui.media.MediaPlayerData$$special$$inlined$thenByDescending$1
            /* JADX WARN: Multi-variable type inference failed */
            @Override // java.util.Comparator
            public final int compare(T t, T t2) {
                int iCompare = comparator2.compare(t, t2);
                return iCompare != 0 ? iCompare : ComparisonsKt__ComparisonsKt.compareValues(Boolean.valueOf(((MediaPlayerData.MediaSortKey) t2).getData().isLocalSession()), Boolean.valueOf(((MediaPlayerData.MediaSortKey) t).getData().isLocalSession()));
            }
        };
        final Comparator<T> comparator4 = new Comparator<T>() { // from class: com.android.systemui.media.MediaPlayerData$$special$$inlined$thenByDescending$2
            /* JADX WARN: Multi-variable type inference failed */
            @Override // java.util.Comparator
            public final int compare(T t, T t2) {
                int iCompare = comparator3.compare(t, t2);
                return iCompare != 0 ? iCompare : ComparisonsKt__ComparisonsKt.compareValues(Boolean.valueOf(!((MediaPlayerData.MediaSortKey) t2).getData().getResumption()), Boolean.valueOf(!((MediaPlayerData.MediaSortKey) t).getData().getResumption()));
            }
        };
        Comparator comparator5 = new Comparator<T>() { // from class: com.android.systemui.media.MediaPlayerData$$special$$inlined$thenByDescending$3
            /* JADX WARN: Multi-variable type inference failed */
            @Override // java.util.Comparator
            public final int compare(T t, T t2) {
                int iCompare = comparator4.compare(t, t2);
                return iCompare != 0 ? iCompare : ComparisonsKt__ComparisonsKt.compareValues(Long.valueOf(((MediaPlayerData.MediaSortKey) t2).getUpdateTime()), Long.valueOf(((MediaPlayerData.MediaSortKey) t).getUpdateTime()));
            }
        };
        comparator = comparator5;
        mediaPlayers = new TreeMap<>(comparator5);
        mediaData = new LinkedHashMap();
    }

    private MediaPlayerData() {
    }

    public final void addMediaPlayer(@NotNull String key, @NotNull MediaData data, @NotNull MediaControlPanel player) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        Intrinsics.checkParameterIsNotNull(data, "data");
        Intrinsics.checkParameterIsNotNull(player, "player");
        removeMediaPlayer(key);
        MediaSortKey mediaSortKey = new MediaSortKey(data, System.currentTimeMillis());
        mediaData.put(key, mediaSortKey);
        mediaPlayers.put(mediaSortKey, player);
    }

    @Nullable
    public final MediaControlPanel getMediaPlayer(@NotNull String key, @Nullable String str) {
        Map<String, MediaSortKey> map;
        MediaSortKey mediaSortKeyRemove;
        Intrinsics.checkParameterIsNotNull(key, "key");
        if (str != null && (!Intrinsics.areEqual(str, key)) && (mediaSortKeyRemove = (map = mediaData).remove(str)) != null) {
            map.put(key, mediaSortKeyRemove);
        }
        MediaSortKey mediaSortKey = mediaData.get(key);
        if (mediaSortKey != null) {
            return mediaPlayers.get(mediaSortKey);
        }
        return null;
    }

    @Nullable
    public final MediaControlPanel removeMediaPlayer(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        MediaSortKey mediaSortKeyRemove = mediaData.remove(key);
        if (mediaSortKeyRemove != null) {
            return mediaPlayers.remove(mediaSortKeyRemove);
        }
        return null;
    }

    @NotNull
    public final List<Pair<String, MediaData>> mediaData() {
        Set<Map.Entry<String, MediaSortKey>> setEntrySet = mediaData.entrySet();
        ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(setEntrySet, 10));
        Iterator<T> it = setEntrySet.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            arrayList.add(new Pair(entry.getKey(), ((MediaSortKey) entry.getValue()).getData()));
        }
        return arrayList;
    }

    @NotNull
    public final Collection<MediaControlPanel> players() {
        Collection<MediaControlPanel> collectionValues = mediaPlayers.values();
        Intrinsics.checkExpressionValueIsNotNull(collectionValues, "mediaPlayers.values");
        return collectionValues;
    }

    public final void clear() {
        mediaData.clear();
        mediaPlayers.clear();
    }
}
