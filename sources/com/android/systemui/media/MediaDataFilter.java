package com.android.systemui.media;

import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.media.MediaDataManager;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaDataFilter.kt */
/* loaded from: classes.dex */
public final class MediaDataFilter implements MediaDataManager.Listener {
    private final Set<MediaDataManager.Listener> _listeners;
    private final LinkedHashMap<String, MediaData> allEntries;
    private final BroadcastDispatcher broadcastDispatcher;
    private final Executor executor;
    private final NotificationLockscreenUserManager lockscreenUserManager;

    @NotNull
    public MediaDataManager mediaDataManager;
    private final MediaResumeListener mediaResumeListener;
    private final LinkedHashMap<String, MediaData> userEntries;
    private final CurrentUserTracker userTracker;

    public MediaDataFilter(@NotNull BroadcastDispatcher broadcastDispatcher, @NotNull MediaResumeListener mediaResumeListener, @NotNull NotificationLockscreenUserManager lockscreenUserManager, @NotNull Executor executor) {
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(mediaResumeListener, "mediaResumeListener");
        Intrinsics.checkParameterIsNotNull(lockscreenUserManager, "lockscreenUserManager");
        Intrinsics.checkParameterIsNotNull(executor, "executor");
        this.broadcastDispatcher = broadcastDispatcher;
        this.mediaResumeListener = mediaResumeListener;
        this.lockscreenUserManager = lockscreenUserManager;
        this.executor = executor;
        this._listeners = new LinkedHashSet();
        this.allEntries = new LinkedHashMap<>();
        this.userEntries = new LinkedHashMap<>();
        AnonymousClass1 anonymousClass1 = new AnonymousClass1(broadcastDispatcher);
        this.userTracker = anonymousClass1;
        anonymousClass1.startTracking();
    }

    @NotNull
    public final Set<MediaDataManager.Listener> getListeners$frameworks__base__packages__SystemUI__android_common__SystemUI_core() {
        return CollectionsKt___CollectionsKt.toSet(this._listeners);
    }

    public final void setMediaDataManager$frameworks__base__packages__SystemUI__android_common__SystemUI_core(@NotNull MediaDataManager mediaDataManager) {
        Intrinsics.checkParameterIsNotNull(mediaDataManager, "<set-?>");
        this.mediaDataManager = mediaDataManager;
    }

    /* compiled from: MediaDataFilter.kt */
    /* renamed from: com.android.systemui.media.MediaDataFilter$1, reason: invalid class name */
    public static final class AnonymousClass1 extends CurrentUserTracker {
        AnonymousClass1(BroadcastDispatcher broadcastDispatcher) {
            super(broadcastDispatcher);
        }

        @Override // com.android.systemui.settings.CurrentUserTracker
        public void onUserSwitched(final int i) {
            MediaDataFilter.this.executor.execute(new Runnable() { // from class: com.android.systemui.media.MediaDataFilter$1$onUserSwitched$1
                @Override // java.lang.Runnable
                public final void run() {
                    MediaDataFilter.this.handleUserSwitched$frameworks__base__packages__SystemUI__android_common__SystemUI_core(i);
                }
            });
        }
    }

    @Override // com.android.systemui.media.MediaDataManager.Listener
    public void onMediaDataLoaded(@NotNull String key, @Nullable String str, @NotNull MediaData data) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        Intrinsics.checkParameterIsNotNull(data, "data");
        if (str != null && (!Intrinsics.areEqual(str, key))) {
            this.allEntries.remove(str);
        }
        this.allEntries.put(key, data);
        if (this.lockscreenUserManager.isCurrentProfile(data.getUserId())) {
            if (str != null && (!Intrinsics.areEqual(str, key))) {
                this.userEntries.remove(str);
            }
            this.userEntries.put(key, data);
            Iterator<T> it = getListeners$frameworks__base__packages__SystemUI__android_common__SystemUI_core().iterator();
            while (it.hasNext()) {
                ((MediaDataManager.Listener) it.next()).onMediaDataLoaded(key, str, data);
            }
        }
    }

    @Override // com.android.systemui.media.MediaDataManager.Listener
    public void onMediaDataRemoved(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        this.allEntries.remove(key);
        if (this.userEntries.remove(key) != null) {
            Iterator<T> it = getListeners$frameworks__base__packages__SystemUI__android_common__SystemUI_core().iterator();
            while (it.hasNext()) {
                ((MediaDataManager.Listener) it.next()).onMediaDataRemoved(key);
            }
        }
    }

    @VisibleForTesting
    public final void handleUserSwitched$frameworks__base__packages__SystemUI__android_common__SystemUI_core(int i) {
        Set<MediaDataManager.Listener> listeners$frameworks__base__packages__SystemUI__android_common__SystemUI_core = getListeners$frameworks__base__packages__SystemUI__android_common__SystemUI_core();
        Set<String> setKeySet = this.userEntries.keySet();
        Intrinsics.checkExpressionValueIsNotNull(setKeySet, "userEntries.keys");
        List<String> mutableList = CollectionsKt___CollectionsKt.toMutableList((Collection) setKeySet);
        this.userEntries.clear();
        for (String it : mutableList) {
            Log.d("MediaDataFilter", "Removing " + it + " after user change");
            for (MediaDataManager.Listener listener : listeners$frameworks__base__packages__SystemUI__android_common__SystemUI_core) {
                Intrinsics.checkExpressionValueIsNotNull(it, "it");
                listener.onMediaDataRemoved(it);
            }
        }
        for (Map.Entry<String, MediaData> entry : this.allEntries.entrySet()) {
            String key = entry.getKey();
            MediaData value = entry.getValue();
            if (this.lockscreenUserManager.isCurrentProfile(value.getUserId())) {
                Log.d("MediaDataFilter", "Re-adding " + key + " after user change");
                this.userEntries.put(key, value);
                Iterator<T> it2 = listeners$frameworks__base__packages__SystemUI__android_common__SystemUI_core.iterator();
                while (it2.hasNext()) {
                    ((MediaDataManager.Listener) it2.next()).onMediaDataLoaded(key, null, value);
                }
            }
        }
    }

    public final void onSwipeToDismiss() {
        Log.d("MediaDataFilter", "Media carousel swiped away");
        Set<String> setKeySet = this.userEntries.keySet();
        Intrinsics.checkExpressionValueIsNotNull(setKeySet, "userEntries.keys");
        for (String it : CollectionsKt___CollectionsKt.toSet(setKeySet)) {
            MediaDataManager mediaDataManager = this.mediaDataManager;
            if (mediaDataManager == null) {
                Intrinsics.throwUninitializedPropertyAccessException("mediaDataManager");
            }
            Intrinsics.checkExpressionValueIsNotNull(it, "it");
            mediaDataManager.setTimedOut$frameworks__base__packages__SystemUI__android_common__SystemUI_core(it, true);
        }
    }

    public final boolean hasActiveMedia() {
        LinkedHashMap<String, MediaData> linkedHashMap = this.userEntries;
        if (linkedHashMap.isEmpty()) {
            return false;
        }
        Iterator<Map.Entry<String, MediaData>> it = linkedHashMap.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().getActive()) {
                return true;
            }
        }
        return false;
    }

    public final boolean hasAnyMedia() {
        return !this.userEntries.isEmpty();
    }

    public final boolean addListener(@NotNull MediaDataManager.Listener listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        return this._listeners.add(listener);
    }

    public final boolean removeListener(@NotNull MediaDataManager.Listener listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        return this._listeners.remove(listener);
    }
}
