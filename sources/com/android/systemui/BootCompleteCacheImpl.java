package com.android.systemui;

import com.android.internal.annotations.GuardedBy;
import com.android.systemui.BootCompleteCache;
import com.android.systemui.dump.DumpManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import kotlin.Unit;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: BootCompleteCacheImpl.kt */
/* loaded from: classes.dex */
public final class BootCompleteCacheImpl implements BootCompleteCache, Dumpable {
    public static final Companion Companion = new Companion(null);
    private final AtomicBoolean bootComplete;

    @GuardedBy({"listeners"})
    private final List<WeakReference<BootCompleteCache.BootCompleteListener>> listeners;

    public BootCompleteCacheImpl(@NotNull DumpManager dumpManager) {
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        dumpManager.registerDumpable("BootCompleteCacheImpl", this);
        this.listeners = new ArrayList();
        this.bootComplete = new AtomicBoolean(false);
    }

    /* compiled from: BootCompleteCacheImpl.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    @Override // com.android.systemui.BootCompleteCache
    public boolean isBootComplete() {
        return this.bootComplete.get();
    }

    public final void setBootComplete() {
        if (this.bootComplete.compareAndSet(false, true)) {
            synchronized (this.listeners) {
                Iterator<T> it = this.listeners.iterator();
                while (it.hasNext()) {
                    BootCompleteCache.BootCompleteListener bootCompleteListener = (BootCompleteCache.BootCompleteListener) ((WeakReference) it.next()).get();
                    if (bootCompleteListener != null) {
                        bootCompleteListener.onBootComplete();
                    }
                }
                this.listeners.clear();
                Unit unit = Unit.INSTANCE;
            }
        }
    }

    @Override // com.android.systemui.BootCompleteCache
    public boolean addListener(@NotNull BootCompleteCache.BootCompleteListener listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        if (this.bootComplete.get()) {
            return true;
        }
        synchronized (this.listeners) {
            if (this.bootComplete.get()) {
                return true;
            }
            this.listeners.add(new WeakReference<>(listener));
            return false;
        }
    }

    @Override // com.android.systemui.BootCompleteCache
    public void removeListener(@NotNull final BootCompleteCache.BootCompleteListener listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        if (this.bootComplete.get()) {
            return;
        }
        synchronized (this.listeners) {
            this.listeners.removeIf(new Predicate<WeakReference<BootCompleteCache.BootCompleteListener>>() { // from class: com.android.systemui.BootCompleteCacheImpl$removeListener$$inlined$synchronized$lambda$1
                @Override // java.util.function.Predicate
                public final boolean test(@NotNull WeakReference<BootCompleteCache.BootCompleteListener> it) {
                    Intrinsics.checkParameterIsNotNull(it, "it");
                    return it.get() == null || it.get() == listener;
                }
            });
            Unit unit = Unit.INSTANCE;
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(@NotNull FileDescriptor fd, @NotNull PrintWriter pw, @NotNull String[] args) {
        Intrinsics.checkParameterIsNotNull(fd, "fd");
        Intrinsics.checkParameterIsNotNull(pw, "pw");
        Intrinsics.checkParameterIsNotNull(args, "args");
        pw.println("BootCompleteCache state:");
        pw.println("  boot complete: " + isBootComplete());
        if (isBootComplete()) {
            return;
        }
        pw.println("  listeners:");
        synchronized (this.listeners) {
            Iterator<T> it = this.listeners.iterator();
            while (it.hasNext()) {
                pw.println("    " + ((WeakReference) it.next()));
            }
            Unit unit = Unit.INSTANCE;
        }
    }
}
