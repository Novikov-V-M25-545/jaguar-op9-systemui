package com.android.systemui.statusbar;

import android.app.Fragment;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.RemoteException;
import android.util.SparseArray;
import android.view.Display;
import android.view.IWindowManager;
import android.view.View;
import android.view.WindowManagerGlobal;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.statusbar.RegisterStatusBarResult;
import com.android.systemui.Dependency;
import com.android.systemui.assist.AssistHandleViewController;
import com.android.systemui.fragments.FragmentHostManager;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.shared.system.WindowManagerWrapper;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.AutoHideController;
import com.android.systemui.statusbar.phone.LightBarController;
import com.android.systemui.statusbar.phone.NavigationBarFragment;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.policy.BatteryController;

/* loaded from: classes.dex */
public class NavigationBarController implements CommandQueue.Callbacks {
    private final Context mContext;
    private final DisplayManager mDisplayManager;
    private final Handler mHandler;

    @VisibleForTesting
    SparseArray<NavigationBarFragment> mNavigationBars = new SparseArray<>();

    public NavigationBarController(Context context, Handler handler, CommandQueue commandQueue) {
        this.mContext = context;
        this.mHandler = handler;
        this.mDisplayManager = (DisplayManager) context.getSystemService("display");
        commandQueue.addCallback((CommandQueue.Callbacks) this);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onDisplayRemoved(int i) {
        removeNavigationBar(i);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onDisplayReady(int i) {
        createNavigationBar(this.mDisplayManager.getDisplay(i), null);
    }

    public void createNavigationBars(boolean z, RegisterStatusBarResult registerStatusBarResult) {
        for (Display display : this.mDisplayManager.getDisplays()) {
            if (z || display.getDisplayId() != 0) {
                createNavigationBar(display, registerStatusBarResult);
            }
        }
    }

    @VisibleForTesting
    void createNavigationBar(final Display display, final RegisterStatusBarResult registerStatusBarResult) {
        Context contextCreateDisplayContext;
        if (display == null) {
            return;
        }
        final int displayId = display.getDisplayId();
        final boolean z = displayId == 0;
        WindowManagerWrapper windowManagerWrapper = WindowManagerWrapper.getInstance();
        IWindowManager windowManagerService = WindowManagerGlobal.getWindowManagerService();
        if (z) {
            contextCreateDisplayContext = this.mContext;
        } else {
            contextCreateDisplayContext = this.mContext.createDisplayContext(display);
        }
        final Context context = contextCreateDisplayContext;
        if (windowManagerWrapper.hasSoftNavigationBar(context, displayId)) {
            NavigationBarFragment.create(context, new FragmentHostManager.FragmentListener() { // from class: com.android.systemui.statusbar.NavigationBarController$$ExternalSyntheticLambda0
                @Override // com.android.systemui.fragments.FragmentHostManager.FragmentListener
                public final void onFragmentViewCreated(String str, Fragment fragment) {
                    this.f$0.lambda$createNavigationBar$0(z, context, displayId, registerStatusBarResult, display, str, fragment);
                }
            });
            try {
                windowManagerService.onOverlayChanged();
            } catch (RemoteException unused) {
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$createNavigationBar$0(boolean z, Context context, int i, RegisterStatusBarResult registerStatusBarResult, Display display, String str, Fragment fragment) {
        LightBarController lightBarController;
        AutoHideController autoHideController;
        NavigationBarFragment navigationBarFragment = (NavigationBarFragment) fragment;
        if (z) {
            lightBarController = (LightBarController) Dependency.get(LightBarController.class);
        } else {
            lightBarController = new LightBarController(context, (DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class), (BatteryController) Dependency.get(BatteryController.class), (NavigationModeController) Dependency.get(NavigationModeController.class));
        }
        navigationBarFragment.setLightBarController(lightBarController);
        if (z) {
            autoHideController = (AutoHideController) Dependency.get(AutoHideController.class);
        } else {
            autoHideController = new AutoHideController(context, this.mHandler, (IWindowManager) Dependency.get(IWindowManager.class));
        }
        navigationBarFragment.setAutoHideController(autoHideController);
        navigationBarFragment.restoreAppearanceAndTransientState();
        this.mNavigationBars.put(i, navigationBarFragment);
        if (registerStatusBarResult != null) {
            navigationBarFragment.setImeWindowStatus(display.getDisplayId(), registerStatusBarResult.mImeToken, registerStatusBarResult.mImeWindowVis, registerStatusBarResult.mImeBackDisposition, registerStatusBarResult.mShowImeSwitcher);
        }
    }

    private void removeNavigationBar(int i) {
        NavigationBarFragment navigationBarFragment = this.mNavigationBars.get(i);
        if (navigationBarFragment != null) {
            navigationBarFragment.setAutoHideController(null);
            View rootView = navigationBarFragment.getView().getRootView();
            WindowManagerGlobal.getInstance().removeView(rootView, true);
            FragmentHostManager.removeAndDestroy(rootView);
            this.mNavigationBars.remove(i);
        }
    }

    public void checkNavBarModes(int i) {
        NavigationBarFragment navigationBarFragment = this.mNavigationBars.get(i);
        if (navigationBarFragment != null) {
            navigationBarFragment.checkNavBarModes();
        }
    }

    public void finishBarAnimations(int i) {
        NavigationBarFragment navigationBarFragment = this.mNavigationBars.get(i);
        if (navigationBarFragment != null) {
            navigationBarFragment.finishBarAnimations();
        }
    }

    public void touchAutoDim(int i) {
        NavigationBarFragment navigationBarFragment = this.mNavigationBars.get(i);
        if (navigationBarFragment != null) {
            navigationBarFragment.touchAutoDim();
        }
    }

    public void transitionTo(int i, int i2, boolean z) {
        NavigationBarFragment navigationBarFragment = this.mNavigationBars.get(i);
        if (navigationBarFragment != null) {
            navigationBarFragment.transitionTo(i2, z);
        }
    }

    public void disableAnimationsDuringHide(int i, long j) {
        NavigationBarFragment navigationBarFragment = this.mNavigationBars.get(i);
        if (navigationBarFragment != null) {
            navigationBarFragment.disableAnimationsDuringHide(j);
        }
    }

    public NavigationBarView getNavigationBarView(int i) {
        NavigationBarFragment navigationBarFragment = this.mNavigationBars.get(i);
        if (navigationBarFragment == null) {
            return null;
        }
        return (NavigationBarView) navigationBarFragment.getView();
    }

    public NavigationBarFragment getDefaultNavigationBarFragment() {
        return this.mNavigationBars.get(0);
    }

    public AssistHandleViewController getAssistHandlerViewController() {
        NavigationBarFragment defaultNavigationBarFragment = getDefaultNavigationBarFragment();
        if (defaultNavigationBarFragment == null) {
            return null;
        }
        return defaultNavigationBarFragment.getAssistHandlerViewController();
    }
}
