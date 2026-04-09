package com.android.systemui.shared.system;

import android.content.ComponentName;
import android.content.pm.ParceledListSlice;
import android.view.DisplayInfo;
import android.view.IPinnedStackController;
import android.view.IPinnedStackListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class PinnedStackListenerForwarder extends IPinnedStackListener.Stub {
    private List<PinnedStackListener> mListeners = new ArrayList();

    public static class PinnedStackListener {
        public void onActionsChanged(ParceledListSlice parceledListSlice) {
        }

        public void onActivityHidden(ComponentName componentName) {
        }

        public void onAspectRatioChanged(float f) {
        }

        public void onConfigurationChanged() {
        }

        public void onDisplayInfoChanged(DisplayInfo displayInfo) {
        }

        public void onImeVisibilityChanged(boolean z, int i) {
        }

        public void onListenerRegistered(IPinnedStackController iPinnedStackController) {
        }

        public void onMovementBoundsChanged(boolean z) {
        }
    }

    public void addListener(PinnedStackListener pinnedStackListener) {
        this.mListeners.add(pinnedStackListener);
    }

    public void onListenerRegistered(IPinnedStackController iPinnedStackController) {
        Iterator<PinnedStackListener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            it.next().onListenerRegistered(iPinnedStackController);
        }
    }

    public void onMovementBoundsChanged(boolean z) {
        Iterator<PinnedStackListener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            it.next().onMovementBoundsChanged(z);
        }
    }

    public void onImeVisibilityChanged(boolean z, int i) {
        Iterator<PinnedStackListener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            it.next().onImeVisibilityChanged(z, i);
        }
    }

    public void onActionsChanged(ParceledListSlice parceledListSlice) {
        Iterator<PinnedStackListener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            it.next().onActionsChanged(parceledListSlice);
        }
    }

    public void onActivityHidden(ComponentName componentName) {
        Iterator<PinnedStackListener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            it.next().onActivityHidden(componentName);
        }
    }

    public void onDisplayInfoChanged(DisplayInfo displayInfo) {
        Iterator<PinnedStackListener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            it.next().onDisplayInfoChanged(displayInfo);
        }
    }

    public void onConfigurationChanged() {
        Iterator<PinnedStackListener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            it.next().onConfigurationChanged();
        }
    }

    public void onAspectRatioChanged(float f) {
        Iterator<PinnedStackListener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            it.next().onAspectRatioChanged(f);
        }
    }
}
