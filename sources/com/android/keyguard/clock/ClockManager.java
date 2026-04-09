package com.android.keyguard.clock;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import androidx.lifecycle.Observer;
import com.android.keyguard.clock.ClockManager;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.dock.DockManager;
import com.android.systemui.plugins.ClockPlugin;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.settings.CurrentUserObservable;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.util.InjectionInflationController;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/* loaded from: classes.dex */
public final class ClockManager {
    private final List<Supplier<ClockPlugin>> mBuiltinClocks;
    private final ContentObserver mContentObserver;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private final CurrentUserObservable mCurrentUserObservable;
    private final Observer<Integer> mCurrentUserObserver;
    private final DockManager.DockEventListener mDockEventListener;
    private final DockManager mDockManager;
    private final int mHeight;
    private boolean mIsDocked;
    private final Map<ClockChangedListener, AvailableClocks> mListeners;
    private final Handler mMainHandler;
    private final PluginManager mPluginManager;
    private final AvailableClocks mPreviewClocks;
    private final SettingsWrapper mSettingsWrapper;
    private final int mWidth;

    public interface ClockChangedListener {
        void onClockChanged(ClockPlugin clockPlugin);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0(Integer num) {
        reload();
    }

    public ClockManager(Context context, InjectionInflationController injectionInflationController, PluginManager pluginManager, SysuiColorExtractor sysuiColorExtractor, DockManager dockManager, BroadcastDispatcher broadcastDispatcher) {
        this(context, injectionInflationController, pluginManager, sysuiColorExtractor, context.getContentResolver(), new CurrentUserObservable(broadcastDispatcher), new SettingsWrapper(context.getContentResolver()), dockManager);
    }

    ClockManager(Context context, InjectionInflationController injectionInflationController, PluginManager pluginManager, final SysuiColorExtractor sysuiColorExtractor, ContentResolver contentResolver, CurrentUserObservable currentUserObservable, SettingsWrapper settingsWrapper, DockManager dockManager) {
        this.mBuiltinClocks = new ArrayList();
        Handler handler = new Handler(Looper.getMainLooper());
        this.mMainHandler = handler;
        this.mContentObserver = new ContentObserver(handler) { // from class: com.android.keyguard.clock.ClockManager.1
            public void onChange(boolean z, Collection<Uri> collection, int i, int i2) {
                if (Objects.equals(Integer.valueOf(i2), ClockManager.this.mCurrentUserObservable.getCurrentUser().getValue())) {
                    ClockManager.this.reload();
                }
            }
        };
        this.mCurrentUserObserver = new Observer() { // from class: com.android.keyguard.clock.ClockManager$$ExternalSyntheticLambda0
            @Override // androidx.lifecycle.Observer
            public final void onChanged(Object obj) {
                this.f$0.lambda$new$0((Integer) obj);
            }
        };
        this.mDockEventListener = new DockManager.DockEventListener() { // from class: com.android.keyguard.clock.ClockManager.2
        };
        this.mListeners = new ArrayMap();
        this.mContext = context;
        this.mPluginManager = pluginManager;
        this.mContentResolver = contentResolver;
        this.mSettingsWrapper = settingsWrapper;
        this.mCurrentUserObservable = currentUserObservable;
        this.mDockManager = dockManager;
        this.mPreviewClocks = new AvailableClocks();
        final Resources resources = context.getResources();
        final LayoutInflater layoutInflaterInjectable = injectionInflationController.injectable(LayoutInflater.from(context));
        addBuiltinClock(new Supplier() { // from class: com.android.keyguard.clock.ClockManager$$ExternalSyntheticLambda19
            @Override // java.util.function.Supplier
            public final Object get() {
                return ClockManager.lambda$new$1(resources, layoutInflaterInjectable, sysuiColorExtractor);
            }
        });
        addBuiltinClock(new Supplier() { // from class: com.android.keyguard.clock.ClockManager$$ExternalSyntheticLambda15
            @Override // java.util.function.Supplier
            public final Object get() {
                return ClockManager.lambda$new$2(resources, layoutInflaterInjectable, sysuiColorExtractor);
            }
        });
        addBuiltinClock(new Supplier() { // from class: com.android.keyguard.clock.ClockManager$$ExternalSyntheticLambda8
            @Override // java.util.function.Supplier
            public final Object get() {
                return ClockManager.lambda$new$3(resources, layoutInflaterInjectable, sysuiColorExtractor);
            }
        });
        addBuiltinClock(new Supplier() { // from class: com.android.keyguard.clock.ClockManager$$ExternalSyntheticLambda16
            @Override // java.util.function.Supplier
            public final Object get() {
                return ClockManager.lambda$new$4(resources, layoutInflaterInjectable, sysuiColorExtractor);
            }
        });
        addBuiltinClock(new Supplier() { // from class: com.android.keyguard.clock.ClockManager$$ExternalSyntheticLambda4
            @Override // java.util.function.Supplier
            public final Object get() {
                return ClockManager.lambda$new$5(resources, layoutInflaterInjectable, sysuiColorExtractor);
            }
        });
        addBuiltinClock(new Supplier() { // from class: com.android.keyguard.clock.ClockManager$$ExternalSyntheticLambda11
            @Override // java.util.function.Supplier
            public final Object get() {
                return ClockManager.lambda$new$6(resources, layoutInflaterInjectable, sysuiColorExtractor);
            }
        });
        addBuiltinClock(new Supplier() { // from class: com.android.keyguard.clock.ClockManager$$ExternalSyntheticLambda5
            @Override // java.util.function.Supplier
            public final Object get() {
                return ClockManager.lambda$new$7(resources, layoutInflaterInjectable, sysuiColorExtractor);
            }
        });
        addBuiltinClock(new Supplier() { // from class: com.android.keyguard.clock.ClockManager$$ExternalSyntheticLambda3
            @Override // java.util.function.Supplier
            public final Object get() {
                return ClockManager.lambda$new$8(resources, layoutInflaterInjectable, sysuiColorExtractor);
            }
        });
        addBuiltinClock(new Supplier() { // from class: com.android.keyguard.clock.ClockManager$$ExternalSyntheticLambda6
            @Override // java.util.function.Supplier
            public final Object get() {
                return ClockManager.lambda$new$9(resources, layoutInflaterInjectable, sysuiColorExtractor);
            }
        });
        addBuiltinClock(new Supplier() { // from class: com.android.keyguard.clock.ClockManager$$ExternalSyntheticLambda13
            @Override // java.util.function.Supplier
            public final Object get() {
                return ClockManager.lambda$new$10(resources, layoutInflaterInjectable, sysuiColorExtractor);
            }
        });
        addBuiltinClock(new Supplier() { // from class: com.android.keyguard.clock.ClockManager$$ExternalSyntheticLambda20
            @Override // java.util.function.Supplier
            public final Object get() {
                return ClockManager.lambda$new$11(resources, layoutInflaterInjectable, sysuiColorExtractor);
            }
        });
        addBuiltinClock(new Supplier() { // from class: com.android.keyguard.clock.ClockManager$$ExternalSyntheticLambda12
            @Override // java.util.function.Supplier
            public final Object get() {
                return ClockManager.lambda$new$12(resources, layoutInflaterInjectable, sysuiColorExtractor);
            }
        });
        addBuiltinClock(new Supplier() { // from class: com.android.keyguard.clock.ClockManager$$ExternalSyntheticLambda9
            @Override // java.util.function.Supplier
            public final Object get() {
                return ClockManager.lambda$new$13(resources, layoutInflaterInjectable, sysuiColorExtractor);
            }
        });
        addBuiltinClock(new Supplier() { // from class: com.android.keyguard.clock.ClockManager$$ExternalSyntheticLambda10
            @Override // java.util.function.Supplier
            public final Object get() {
                return ClockManager.lambda$new$14(resources, layoutInflaterInjectable, sysuiColorExtractor);
            }
        });
        addBuiltinClock(new Supplier() { // from class: com.android.keyguard.clock.ClockManager$$ExternalSyntheticLambda18
            @Override // java.util.function.Supplier
            public final Object get() {
                return ClockManager.lambda$new$15(resources, layoutInflaterInjectable, sysuiColorExtractor);
            }
        });
        addBuiltinClock(new Supplier() { // from class: com.android.keyguard.clock.ClockManager$$ExternalSyntheticLambda17
            @Override // java.util.function.Supplier
            public final Object get() {
                return ClockManager.lambda$new$16(resources, layoutInflaterInjectable, sysuiColorExtractor);
            }
        });
        addBuiltinClock(new Supplier() { // from class: com.android.keyguard.clock.ClockManager$$ExternalSyntheticLambda14
            @Override // java.util.function.Supplier
            public final Object get() {
                return ClockManager.lambda$new$17(resources, layoutInflaterInjectable, sysuiColorExtractor);
            }
        });
        addBuiltinClock(new Supplier() { // from class: com.android.keyguard.clock.ClockManager$$ExternalSyntheticLambda7
            @Override // java.util.function.Supplier
            public final Object get() {
                return ClockManager.lambda$new$18(resources, layoutInflaterInjectable, sysuiColorExtractor);
            }
        });
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        this.mWidth = displayMetrics.widthPixels;
        this.mHeight = displayMetrics.heightPixels;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ ClockPlugin lambda$new$1(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        return new DefaultClockController(resources, layoutInflater, sysuiColorExtractor);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ ClockPlugin lambda$new$2(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        return new DefaultBoldClockController(resources, layoutInflater, sysuiColorExtractor);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ ClockPlugin lambda$new$3(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        return new SamsungClockController(resources, layoutInflater, sysuiColorExtractor);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ ClockPlugin lambda$new$4(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        return new SamsungBoldClockController(resources, layoutInflater, sysuiColorExtractor);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ ClockPlugin lambda$new$5(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        return new SamsungHighlightClockController(resources, layoutInflater, sysuiColorExtractor);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ ClockPlugin lambda$new$6(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        return new BubbleClockController(resources, layoutInflater, sysuiColorExtractor);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ ClockPlugin lambda$new$7(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        return new AnalogClockController(resources, layoutInflater, sysuiColorExtractor);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ ClockPlugin lambda$new$8(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        return new TypeClockController(resources, layoutInflater, sysuiColorExtractor);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ ClockPlugin lambda$new$9(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        return new TypeClockAltController(resources, layoutInflater, sysuiColorExtractor);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ ClockPlugin lambda$new$10(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        return new BinaryClockController(resources, layoutInflater, sysuiColorExtractor);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ ClockPlugin lambda$new$11(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        return new DividedLinesClockController(resources, layoutInflater, sysuiColorExtractor);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ ClockPlugin lambda$new$12(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        return new SfunyClockController(resources, layoutInflater, sysuiColorExtractor);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ ClockPlugin lambda$new$13(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        return new MNMLBoxClockController(resources, layoutInflater, sysuiColorExtractor);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ ClockPlugin lambda$new$14(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        return new MNMLMinimalClockController(resources, layoutInflater, sysuiColorExtractor);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ ClockPlugin lambda$new$15(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        return new TuxClockController(resources, layoutInflater, sysuiColorExtractor);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ ClockPlugin lambda$new$16(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        return new AndroidSDP3ClockController(resources, layoutInflater, sysuiColorExtractor);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ ClockPlugin lambda$new$17(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        return new AndroidSClockController(resources, layoutInflater, sysuiColorExtractor);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ ClockPlugin lambda$new$18(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        return new OctaviDigitalClockController(resources, layoutInflater, sysuiColorExtractor);
    }

    public void addOnClockChangedListener(ClockChangedListener clockChangedListener) {
        if (this.mListeners.isEmpty()) {
            register();
        }
        AvailableClocks availableClocks = new AvailableClocks();
        for (int i = 0; i < this.mBuiltinClocks.size(); i++) {
            availableClocks.addClockPlugin(this.mBuiltinClocks.get(i).get());
        }
        this.mListeners.put(clockChangedListener, availableClocks);
        this.mPluginManager.addPluginListener((PluginListener) availableClocks, ClockPlugin.class, true);
        reload();
    }

    public void removeOnClockChangedListener(ClockChangedListener clockChangedListener) {
        this.mPluginManager.removePluginListener(this.mListeners.remove(clockChangedListener));
        if (this.mListeners.isEmpty()) {
            unregister();
        }
    }

    List<ClockInfo> getClockInfos() {
        return this.mPreviewClocks.getInfo();
    }

    boolean isDocked() {
        return this.mIsDocked;
    }

    ContentObserver getContentObserver() {
        return this.mContentObserver;
    }

    void addBuiltinClock(Supplier<ClockPlugin> supplier) {
        this.mPreviewClocks.addClockPlugin(supplier.get());
        this.mBuiltinClocks.add(supplier);
    }

    private void register() {
        this.mPluginManager.addPluginListener((PluginListener) this.mPreviewClocks, ClockPlugin.class, true);
        this.mContentResolver.registerContentObserver(Settings.Secure.getUriFor("lock_screen_custom_clock_face"), false, this.mContentObserver, -1);
        this.mContentResolver.registerContentObserver(Settings.Secure.getUriFor("docked_clock_face"), false, this.mContentObserver, -1);
        this.mCurrentUserObservable.getCurrentUser().observeForever(this.mCurrentUserObserver);
        DockManager dockManager = this.mDockManager;
        if (dockManager != null) {
            dockManager.addListener(this.mDockEventListener);
        }
    }

    private void unregister() {
        this.mPluginManager.removePluginListener(this.mPreviewClocks);
        this.mContentResolver.unregisterContentObserver(this.mContentObserver);
        this.mCurrentUserObservable.getCurrentUser().removeObserver(this.mCurrentUserObserver);
        DockManager dockManager = this.mDockManager;
        if (dockManager != null) {
            dockManager.removeListener(this.mDockEventListener);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reload() {
        this.mPreviewClocks.reloadCurrentClock();
        this.mListeners.forEach(new BiConsumer() { // from class: com.android.keyguard.clock.ClockManager$$ExternalSyntheticLambda2
            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                this.f$0.lambda$reload$20((ClockManager.ClockChangedListener) obj, (ClockManager.AvailableClocks) obj2);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$reload$20(final ClockChangedListener clockChangedListener, AvailableClocks availableClocks) {
        availableClocks.reloadCurrentClock();
        final ClockPlugin currentClock = availableClocks.getCurrentClock();
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (currentClock instanceof DefaultClockController) {
                currentClock = null;
            }
            clockChangedListener.onClockChanged(currentClock);
            return;
        }
        this.mMainHandler.post(new Runnable() { // from class: com.android.keyguard.clock.ClockManager$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                ClockManager.lambda$reload$19(clockChangedListener, currentClock);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$reload$19(ClockChangedListener clockChangedListener, ClockPlugin clockPlugin) {
        if (clockPlugin instanceof DefaultClockController) {
            clockPlugin = null;
        }
        clockChangedListener.onClockChanged(clockPlugin);
    }

    /* JADX INFO: Access modifiers changed from: private */
    final class AvailableClocks implements PluginListener<ClockPlugin> {
        private final List<ClockInfo> mClockInfo;
        private final Map<String, ClockPlugin> mClocks;
        private ClockPlugin mCurrentClock;

        private AvailableClocks() {
            this.mClocks = new ArrayMap();
            this.mClockInfo = new ArrayList();
        }

        @Override // com.android.systemui.plugins.PluginListener
        public void onPluginConnected(ClockPlugin clockPlugin, Context context) {
            addClockPlugin(clockPlugin);
            reloadIfNeeded(clockPlugin);
        }

        @Override // com.android.systemui.plugins.PluginListener
        public void onPluginDisconnected(ClockPlugin clockPlugin) {
            removeClockPlugin(clockPlugin);
            reloadIfNeeded(clockPlugin);
        }

        ClockPlugin getCurrentClock() {
            return this.mCurrentClock;
        }

        List<ClockInfo> getInfo() {
            return this.mClockInfo;
        }

        void addClockPlugin(final ClockPlugin clockPlugin) {
            String name = clockPlugin.getClass().getName();
            this.mClocks.put(clockPlugin.getClass().getName(), clockPlugin);
            this.mClockInfo.add(ClockInfo.builder().setName(clockPlugin.getName()).setTitle(new Supplier() { // from class: com.android.keyguard.clock.ClockManager$AvailableClocks$$ExternalSyntheticLambda2
                @Override // java.util.function.Supplier
                public final Object get() {
                    return clockPlugin.getTitle();
                }
            }).setId(name).setThumbnail(new Supplier() { // from class: com.android.keyguard.clock.ClockManager$AvailableClocks$$ExternalSyntheticLambda1
                @Override // java.util.function.Supplier
                public final Object get() {
                    return clockPlugin.getThumbnail();
                }
            }).setPreview(new Supplier() { // from class: com.android.keyguard.clock.ClockManager$AvailableClocks$$ExternalSyntheticLambda0
                @Override // java.util.function.Supplier
                public final Object get() {
                    return this.f$0.lambda$addClockPlugin$0(clockPlugin);
                }
            }).build());
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ Bitmap lambda$addClockPlugin$0(ClockPlugin clockPlugin) {
            return clockPlugin.getPreview(ClockManager.this.mWidth, ClockManager.this.mHeight);
        }

        private void removeClockPlugin(ClockPlugin clockPlugin) {
            String name = clockPlugin.getClass().getName();
            this.mClocks.remove(name);
            for (int i = 0; i < this.mClockInfo.size(); i++) {
                if (name.equals(this.mClockInfo.get(i).getId())) {
                    this.mClockInfo.remove(i);
                    return;
                }
            }
        }

        private void reloadIfNeeded(ClockPlugin clockPlugin) {
            boolean z = clockPlugin == this.mCurrentClock;
            reloadCurrentClock();
            boolean z2 = clockPlugin == this.mCurrentClock;
            if (z || z2) {
                ClockManager.this.reload();
            }
        }

        void reloadCurrentClock() {
            this.mCurrentClock = getClockPlugin();
        }

        private ClockPlugin getClockPlugin() {
            ClockPlugin clockPlugin;
            String dockedClockFace;
            if (!ClockManager.this.isDocked() || (dockedClockFace = ClockManager.this.mSettingsWrapper.getDockedClockFace(ClockManager.this.mCurrentUserObservable.getCurrentUser().getValue().intValue())) == null) {
                clockPlugin = null;
            } else {
                clockPlugin = this.mClocks.get(dockedClockFace);
                if (clockPlugin != null) {
                    return clockPlugin;
                }
            }
            String lockScreenCustomClockFace = ClockManager.this.mSettingsWrapper.getLockScreenCustomClockFace(ClockManager.this.mCurrentUserObservable.getCurrentUser().getValue().intValue());
            return lockScreenCustomClockFace != null ? this.mClocks.get(lockScreenCustomClockFace) : clockPlugin;
        }
    }
}
