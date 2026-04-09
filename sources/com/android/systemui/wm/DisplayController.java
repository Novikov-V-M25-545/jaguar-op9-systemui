package com.android.systemui.wm;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Slog;
import android.util.SparseArray;
import android.view.Display;
import android.view.IDisplayWindowListener;
import android.view.IWindowManager;
import com.android.systemui.wm.DisplayChangeController;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class DisplayController {
    private final DisplayChangeController mChangeController;
    private final Context mContext;
    private final IDisplayWindowListener mDisplayContainerListener;
    private final Handler mHandler;
    private final IWindowManager mWmService;
    private final SparseArray<DisplayRecord> mDisplays = new SparseArray<>();
    private final ArrayList<OnDisplaysChangedListener> mDisplayChangedListeners = new ArrayList<>();

    public interface OnDisplaysChangedListener {
        default void onDisplayAdded(int i) {
        }

        default void onDisplayConfigurationChanged(int i, Configuration configuration) {
        }

        default void onDisplayRemoved(int i) {
        }

        default void onFixedRotationFinished(int i) {
        }

        default void onFixedRotationStarted(int i, int i2) {
        }
    }

    public Display getDisplay(int i) {
        return ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).getDisplay(i);
    }

    /* renamed from: com.android.systemui.wm.DisplayController$1, reason: invalid class name */
    class AnonymousClass1 extends IDisplayWindowListener.Stub {
        AnonymousClass1() {
        }

        public void onDisplayAdded(final int i) {
            DisplayController.this.mHandler.post(new Runnable() { // from class: com.android.systemui.wm.DisplayController$1$$ExternalSyntheticLambda2
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onDisplayAdded$0(i);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onDisplayAdded$0(int i) {
            synchronized (DisplayController.this.mDisplays) {
                if (DisplayController.this.mDisplays.get(i) != null) {
                    return;
                }
                Display display = DisplayController.this.getDisplay(i);
                if (display == null) {
                    return;
                }
                DisplayRecord displayRecord = new DisplayRecord(null);
                displayRecord.mDisplayId = i;
                Context contextCreateDisplayContext = i == 0 ? DisplayController.this.mContext : DisplayController.this.mContext.createDisplayContext(display);
                displayRecord.mContext = contextCreateDisplayContext;
                displayRecord.mDisplayLayout = new DisplayLayout(contextCreateDisplayContext, display);
                DisplayController.this.mDisplays.put(i, displayRecord);
                for (int i2 = 0; i2 < DisplayController.this.mDisplayChangedListeners.size(); i2++) {
                    ((OnDisplaysChangedListener) DisplayController.this.mDisplayChangedListeners.get(i2)).onDisplayAdded(i);
                }
            }
        }

        public void onDisplayConfigurationChanged(final int i, final Configuration configuration) {
            DisplayController.this.mHandler.post(new Runnable() { // from class: com.android.systemui.wm.DisplayController$1$$ExternalSyntheticLambda4
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onDisplayConfigurationChanged$1(i, configuration);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onDisplayConfigurationChanged$1(int i, Configuration configuration) {
            synchronized (DisplayController.this.mDisplays) {
                DisplayRecord displayRecord = (DisplayRecord) DisplayController.this.mDisplays.get(i);
                if (displayRecord == null) {
                    Slog.w("DisplayController", "Skipping Display Configuration change on non-added display.");
                    return;
                }
                Display display = DisplayController.this.getDisplay(i);
                if (display != null) {
                    Context contextCreateDisplayContext = DisplayController.this.mContext;
                    if (i != 0) {
                        contextCreateDisplayContext = DisplayController.this.mContext.createDisplayContext(display);
                    }
                    Context contextCreateConfigurationContext = contextCreateDisplayContext.createConfigurationContext(configuration);
                    displayRecord.mContext = contextCreateConfigurationContext;
                    displayRecord.mDisplayLayout = new DisplayLayout(contextCreateConfigurationContext, display);
                    for (int i2 = 0; i2 < DisplayController.this.mDisplayChangedListeners.size(); i2++) {
                        ((OnDisplaysChangedListener) DisplayController.this.mDisplayChangedListeners.get(i2)).onDisplayConfigurationChanged(i, configuration);
                    }
                    return;
                }
                Slog.w("DisplayController", "Skipping Display Configuration change on invalid display. It may have been removed.");
            }
        }

        public void onDisplayRemoved(final int i) {
            DisplayController.this.mHandler.post(new Runnable() { // from class: com.android.systemui.wm.DisplayController$1$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onDisplayRemoved$2(i);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onDisplayRemoved$2(int i) {
            synchronized (DisplayController.this.mDisplays) {
                if (DisplayController.this.mDisplays.get(i) == null) {
                    return;
                }
                for (int size = DisplayController.this.mDisplayChangedListeners.size() - 1; size >= 0; size--) {
                    ((OnDisplaysChangedListener) DisplayController.this.mDisplayChangedListeners.get(size)).onDisplayRemoved(i);
                }
                DisplayController.this.mDisplays.remove(i);
            }
        }

        public void onFixedRotationStarted(final int i, final int i2) {
            DisplayController.this.mHandler.post(new Runnable() { // from class: com.android.systemui.wm.DisplayController$1$$ExternalSyntheticLambda3
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onFixedRotationStarted$3(i, i2);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onFixedRotationStarted$3(int i, int i2) {
            synchronized (DisplayController.this.mDisplays) {
                if (DisplayController.this.mDisplays.get(i) != null && DisplayController.this.getDisplay(i) != null) {
                    for (int size = DisplayController.this.mDisplayChangedListeners.size() - 1; size >= 0; size--) {
                        ((OnDisplaysChangedListener) DisplayController.this.mDisplayChangedListeners.get(size)).onFixedRotationStarted(i, i2);
                    }
                    return;
                }
                Slog.w("DisplayController", "Skipping onFixedRotationStarted on unknown display, displayId=" + i);
            }
        }

        public void onFixedRotationFinished(final int i) {
            DisplayController.this.mHandler.post(new Runnable() { // from class: com.android.systemui.wm.DisplayController$1$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onFixedRotationFinished$4(i);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onFixedRotationFinished$4(int i) {
            synchronized (DisplayController.this.mDisplays) {
                if (DisplayController.this.mDisplays.get(i) != null && DisplayController.this.getDisplay(i) != null) {
                    for (int size = DisplayController.this.mDisplayChangedListeners.size() - 1; size >= 0; size--) {
                        ((OnDisplaysChangedListener) DisplayController.this.mDisplayChangedListeners.get(size)).onFixedRotationFinished(i);
                    }
                    return;
                }
                Slog.w("DisplayController", "Skipping onFixedRotationFinished on unknown display, displayId=" + i);
            }
        }
    }

    public DisplayController(Context context, Handler handler, IWindowManager iWindowManager) {
        AnonymousClass1 anonymousClass1 = new AnonymousClass1();
        this.mDisplayContainerListener = anonymousClass1;
        this.mHandler = handler;
        this.mContext = context;
        this.mWmService = iWindowManager;
        this.mChangeController = new DisplayChangeController(handler, iWindowManager);
        try {
            iWindowManager.registerDisplayWindowListener(anonymousClass1);
        } catch (RemoteException unused) {
            throw new RuntimeException("Unable to register hierarchy listener");
        }
    }

    public DisplayLayout getDisplayLayout(int i) {
        DisplayRecord displayRecord = this.mDisplays.get(i);
        if (displayRecord != null) {
            return displayRecord.mDisplayLayout;
        }
        return null;
    }

    public Context getDisplayContext(int i) {
        DisplayRecord displayRecord = this.mDisplays.get(i);
        if (displayRecord != null) {
            return displayRecord.mContext;
        }
        return null;
    }

    public void addDisplayWindowListener(OnDisplaysChangedListener onDisplaysChangedListener) {
        synchronized (this.mDisplays) {
            if (this.mDisplayChangedListeners.contains(onDisplaysChangedListener)) {
                return;
            }
            this.mDisplayChangedListeners.add(onDisplaysChangedListener);
            for (int i = 0; i < this.mDisplays.size(); i++) {
                onDisplaysChangedListener.onDisplayAdded(this.mDisplays.keyAt(i));
            }
        }
    }

    public void addDisplayChangingController(DisplayChangeController.OnDisplayChangingListener onDisplayChangingListener) {
        this.mChangeController.addRotationListener(onDisplayChangingListener);
    }

    private static class DisplayRecord {
        Context mContext;
        int mDisplayId;
        DisplayLayout mDisplayLayout;

        private DisplayRecord() {
        }

        /* synthetic */ DisplayRecord(AnonymousClass1 anonymousClass1) {
            this();
        }
    }
}
