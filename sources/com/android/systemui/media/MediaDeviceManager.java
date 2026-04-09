package com.android.systemui.media;

import android.media.MediaRouter2Manager;
import android.media.RoutingSessionInfo;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import com.android.settingslib.media.LocalMediaManager;
import com.android.settingslib.media.MediaDevice;
import com.android.systemui.Dumpable;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.media.MediaDataManager;
import com.android.systemui.media.MediaDeviceManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaDeviceManager.kt */
/* loaded from: classes.dex */
public final class MediaDeviceManager implements MediaDataManager.Listener, Dumpable {
    private final Executor bgExecutor;
    private final MediaControllerFactory controllerFactory;
    private final Map<String, Entry> entries;
    private final Executor fgExecutor;
    private final Set<Listener> listeners;
    private final LocalMediaManagerFactory localMediaManagerFactory;
    private final MediaRouter2Manager mr2manager;

    /* compiled from: MediaDeviceManager.kt */
    public interface Listener {
        void onKeyRemoved(@NotNull String str);

        void onMediaDeviceChanged(@NotNull String str, @Nullable String str2, @Nullable MediaDeviceData mediaDeviceData);
    }

    public MediaDeviceManager(@NotNull MediaControllerFactory controllerFactory, @NotNull LocalMediaManagerFactory localMediaManagerFactory, @NotNull MediaRouter2Manager mr2manager, @NotNull Executor fgExecutor, @NotNull Executor bgExecutor, @NotNull DumpManager dumpManager) {
        Intrinsics.checkParameterIsNotNull(controllerFactory, "controllerFactory");
        Intrinsics.checkParameterIsNotNull(localMediaManagerFactory, "localMediaManagerFactory");
        Intrinsics.checkParameterIsNotNull(mr2manager, "mr2manager");
        Intrinsics.checkParameterIsNotNull(fgExecutor, "fgExecutor");
        Intrinsics.checkParameterIsNotNull(bgExecutor, "bgExecutor");
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        this.controllerFactory = controllerFactory;
        this.localMediaManagerFactory = localMediaManagerFactory;
        this.mr2manager = mr2manager;
        this.fgExecutor = fgExecutor;
        this.bgExecutor = bgExecutor;
        this.listeners = new LinkedHashSet();
        this.entries = new LinkedHashMap();
        String name = MediaDeviceManager.class.getName();
        Intrinsics.checkExpressionValueIsNotNull(name, "javaClass.name");
        dumpManager.registerDumpable(name, this);
    }

    public final boolean addListener(@NotNull Listener listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        return this.listeners.add(listener);
    }

    @Override // com.android.systemui.media.MediaDataManager.Listener
    public void onMediaDataLoaded(@NotNull String key, @Nullable String str, @NotNull MediaData data) {
        Entry entryRemove;
        Intrinsics.checkParameterIsNotNull(key, "key");
        Intrinsics.checkParameterIsNotNull(data, "data");
        if (str != null && (!Intrinsics.areEqual(str, key)) && (entryRemove = this.entries.remove(str)) != null) {
            entryRemove.stop();
        }
        Entry entry = this.entries.get(key);
        if (entry == null || (!Intrinsics.areEqual(entry.getToken(), data.getToken()))) {
            if (entry != null) {
                entry.stop();
            }
            MediaSession.Token token = data.getToken();
            Entry entry2 = new Entry(this, key, str, token != null ? this.controllerFactory.create(token) : null, this.localMediaManagerFactory.create(data.getPackageName()));
            this.entries.put(key, entry2);
            entry2.start();
        }
    }

    @Override // com.android.systemui.media.MediaDataManager.Listener
    public void onMediaDataRemoved(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        Entry entryRemove = this.entries.remove(key);
        if (entryRemove != null) {
            entryRemove.stop();
        }
        if (entryRemove != null) {
            Iterator<T> it = this.listeners.iterator();
            while (it.hasNext()) {
                ((Listener) it.next()).onKeyRemoved(key);
            }
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(@NotNull final FileDescriptor fd, @NotNull final PrintWriter pw, @NotNull final String[] args) {
        Intrinsics.checkParameterIsNotNull(fd, "fd");
        Intrinsics.checkParameterIsNotNull(pw, "pw");
        Intrinsics.checkParameterIsNotNull(args, "args");
        pw.println("MediaDeviceManager state:");
        this.entries.forEach(new BiConsumer<String, Entry>() { // from class: com.android.systemui.media.MediaDeviceManager$dump$$inlined$with$lambda$1
            @Override // java.util.function.BiConsumer
            public final void accept(@NotNull String key, @NotNull MediaDeviceManager.Entry entry) {
                Intrinsics.checkParameterIsNotNull(key, "key");
                Intrinsics.checkParameterIsNotNull(entry, "entry");
                pw.println("  key=" + key);
                entry.dump(fd, pw, args);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void processDevice(String str, String str2, MediaDevice mediaDevice) {
        MediaDeviceData mediaDeviceData = new MediaDeviceData(mediaDevice != null, mediaDevice != null ? mediaDevice.getIconWithoutBackground() : null, mediaDevice != null ? mediaDevice.getName() : null);
        Iterator<T> it = this.listeners.iterator();
        while (it.hasNext()) {
            ((Listener) it.next()).onMediaDeviceChanged(str, str2, mediaDeviceData);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* compiled from: MediaDeviceManager.kt */
    final class Entry extends MediaController.Callback implements LocalMediaManager.DeviceCallback {

        @Nullable
        private final MediaController controller;
        private MediaDevice current;

        @NotNull
        private final String key;

        @NotNull
        private final LocalMediaManager localMediaManager;

        @Nullable
        private final String oldKey;
        private int playbackType;
        private boolean started;
        final /* synthetic */ MediaDeviceManager this$0;

        public Entry(@NotNull MediaDeviceManager mediaDeviceManager, @Nullable String key, @Nullable String str, @NotNull MediaController mediaController, LocalMediaManager localMediaManager) {
            Intrinsics.checkParameterIsNotNull(key, "key");
            Intrinsics.checkParameterIsNotNull(localMediaManager, "localMediaManager");
            this.this$0 = mediaDeviceManager;
            this.key = key;
            this.oldKey = str;
            this.controller = mediaController;
            this.localMediaManager = localMediaManager;
        }

        @NotNull
        public final String getKey() {
            return this.key;
        }

        @Nullable
        public final String getOldKey() {
            return this.oldKey;
        }

        @Nullable
        public final MediaController getController() {
            return this.controller;
        }

        @NotNull
        public final LocalMediaManager getLocalMediaManager() {
            return this.localMediaManager;
        }

        @Nullable
        public final MediaSession.Token getToken() {
            MediaController mediaController = this.controller;
            if (mediaController != null) {
                return mediaController.getSessionToken();
            }
            return null;
        }

        private final void setCurrent(final MediaDevice mediaDevice) {
            if (!this.started || (!Intrinsics.areEqual(mediaDevice, this.current))) {
                this.current = mediaDevice;
                this.this$0.fgExecutor.execute(new Runnable() { // from class: com.android.systemui.media.MediaDeviceManager$Entry$current$1
                    @Override // java.lang.Runnable
                    public final void run() {
                        MediaDeviceManager.Entry entry = this.this$0;
                        entry.this$0.processDevice(entry.getKey(), this.this$0.getOldKey(), mediaDevice);
                    }
                });
            }
        }

        public final void start() {
            this.this$0.bgExecutor.execute(new Runnable() { // from class: com.android.systemui.media.MediaDeviceManager$Entry$start$1
                @Override // java.lang.Runnable
                public final void run() {
                    MediaController.PlaybackInfo playbackInfo;
                    this.this$0.getLocalMediaManager().registerCallback(this.this$0);
                    this.this$0.getLocalMediaManager().startScan();
                    MediaDeviceManager.Entry entry = this.this$0;
                    MediaController controller = entry.getController();
                    entry.playbackType = (controller == null || (playbackInfo = controller.getPlaybackInfo()) == null) ? 0 : playbackInfo.getPlaybackType();
                    MediaController controller2 = this.this$0.getController();
                    if (controller2 != null) {
                        controller2.registerCallback(this.this$0);
                    }
                    this.this$0.updateCurrent();
                    this.this$0.started = true;
                }
            });
        }

        public final void stop() {
            this.this$0.bgExecutor.execute(new Runnable() { // from class: com.android.systemui.media.MediaDeviceManager$Entry$stop$1
                @Override // java.lang.Runnable
                public final void run() {
                    this.this$0.started = false;
                    MediaController controller = this.this$0.getController();
                    if (controller != null) {
                        controller.unregisterCallback(this.this$0);
                    }
                    this.this$0.getLocalMediaManager().stopScan();
                    this.this$0.getLocalMediaManager().unregisterCallback(this.this$0);
                }
            });
        }

        public final void dump(@NotNull FileDescriptor fd, @NotNull PrintWriter pw, @NotNull String[] args) {
            MediaController.PlaybackInfo playbackInfo;
            Intrinsics.checkParameterIsNotNull(fd, "fd");
            Intrinsics.checkParameterIsNotNull(pw, "pw");
            Intrinsics.checkParameterIsNotNull(args, "args");
            MediaController mediaController = this.controller;
            Integer numValueOf = null;
            RoutingSessionInfo routingSessionForMediaController = mediaController != null ? this.this$0.mr2manager.getRoutingSessionForMediaController(mediaController) : null;
            List selectedRoutes = routingSessionForMediaController != null ? this.this$0.mr2manager.getSelectedRoutes(routingSessionForMediaController) : null;
            StringBuilder sb = new StringBuilder();
            sb.append("    current device is ");
            MediaDevice mediaDevice = this.current;
            sb.append(mediaDevice != null ? mediaDevice.getName() : null);
            pw.println(sb.toString());
            MediaController mediaController2 = this.controller;
            if (mediaController2 != null && (playbackInfo = mediaController2.getPlaybackInfo()) != null) {
                numValueOf = Integer.valueOf(playbackInfo.getPlaybackType());
            }
            pw.println("    PlaybackType=" + numValueOf + " (1 for local, 2 for remote) cached=" + this.playbackType);
            StringBuilder sb2 = new StringBuilder();
            sb2.append("    routingSession=");
            sb2.append(routingSessionForMediaController);
            pw.println(sb2.toString());
            pw.println("    selectedRoutes=" + selectedRoutes);
        }

        @Override // android.media.session.MediaController.Callback
        public void onAudioInfoChanged(@Nullable MediaController.PlaybackInfo playbackInfo) {
            int playbackType = playbackInfo != null ? playbackInfo.getPlaybackType() : 0;
            if (playbackType == this.playbackType) {
                return;
            }
            this.playbackType = playbackType;
            updateCurrent();
        }

        @Override // com.android.settingslib.media.LocalMediaManager.DeviceCallback
        public void onDeviceListUpdate(@Nullable List<? extends MediaDevice> list) {
            this.this$0.bgExecutor.execute(new Runnable() { // from class: com.android.systemui.media.MediaDeviceManager$Entry$onDeviceListUpdate$1
                @Override // java.lang.Runnable
                public final void run() {
                    this.this$0.updateCurrent();
                }
            });
        }

        @Override // com.android.settingslib.media.LocalMediaManager.DeviceCallback
        public void onSelectedDeviceStateChanged(@NotNull MediaDevice device, int i) {
            Intrinsics.checkParameterIsNotNull(device, "device");
            this.this$0.bgExecutor.execute(new Runnable() { // from class: com.android.systemui.media.MediaDeviceManager$Entry$onSelectedDeviceStateChanged$1
                @Override // java.lang.Runnable
                public final void run() {
                    this.this$0.updateCurrent();
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public final void updateCurrent() {
            MediaDevice currentConnectedDevice = this.localMediaManager.getCurrentConnectedDevice();
            MediaController mediaController = this.controller;
            if (mediaController != null) {
                if (this.this$0.mr2manager.getRoutingSessionForMediaController(mediaController) == null) {
                    currentConnectedDevice = null;
                }
                setCurrent(currentConnectedDevice);
                return;
            }
            setCurrent(currentConnectedDevice);
        }
    }
}
