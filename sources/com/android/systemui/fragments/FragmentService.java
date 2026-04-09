package com.android.systemui.fragments;

import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Handler;
import android.util.ArrayMap;
import android.view.View;
import com.android.systemui.Dumpable;
import com.android.systemui.dagger.SystemUIRootComponent;
import com.android.systemui.qs.QSFragment;
import com.android.systemui.statusbar.phone.NavigationBarFragment;
import com.android.systemui.statusbar.policy.ConfigurationController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Iterator;

/* loaded from: classes.dex */
public class FragmentService implements Dumpable {
    private final FragmentCreator mFragmentCreator;
    private final ArrayMap<View, FragmentHostState> mHosts = new ArrayMap<>();
    private final ArrayMap<String, Method> mInjectionMap = new ArrayMap<>();
    private final Handler mHandler = new Handler();
    private ConfigurationController.ConfigurationListener mConfigurationListener = new ConfigurationController.ConfigurationListener() { // from class: com.android.systemui.fragments.FragmentService.1
        @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
        public void onConfigChanged(Configuration configuration) {
            Iterator it = FragmentService.this.mHosts.values().iterator();
            while (it.hasNext()) {
                ((FragmentHostState) it.next()).sendConfigurationChange(configuration);
            }
        }
    };

    public interface FragmentCreator {
        NavigationBarFragment createNavigationBarFragment();

        QSFragment createQSFragment();
    }

    public FragmentService(SystemUIRootComponent systemUIRootComponent, ConfigurationController configurationController) throws SecurityException {
        this.mFragmentCreator = systemUIRootComponent.createFragmentCreator();
        initInjectionMap();
        configurationController.addCallback(this.mConfigurationListener);
    }

    ArrayMap<String, Method> getInjectionMap() {
        return this.mInjectionMap;
    }

    FragmentCreator getFragmentCreator() {
        return this.mFragmentCreator;
    }

    private void initInjectionMap() throws SecurityException {
        for (Method method : FragmentCreator.class.getDeclaredMethods()) {
            if (Fragment.class.isAssignableFrom(method.getReturnType()) && (method.getModifiers() & 1) != 0) {
                this.mInjectionMap.put(method.getReturnType().getName(), method);
            }
        }
    }

    public FragmentHostManager getFragmentHostManager(View view) {
        View rootView = view.getRootView();
        FragmentHostState fragmentHostState = this.mHosts.get(rootView);
        if (fragmentHostState == null) {
            fragmentHostState = new FragmentHostState(rootView);
            this.mHosts.put(rootView, fragmentHostState);
        }
        return fragmentHostState.getFragmentHostManager();
    }

    public void removeAndDestroy(View view) {
        FragmentHostState fragmentHostStateRemove = this.mHosts.remove(view.getRootView());
        if (fragmentHostStateRemove != null) {
            fragmentHostStateRemove.mFragmentHostManager.destroy();
        }
    }

    public void destroyAll() {
        Iterator<FragmentHostState> it = this.mHosts.values().iterator();
        while (it.hasNext()) {
            it.next().mFragmentHostManager.destroy();
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("Dumping fragments:");
        Iterator<FragmentHostState> it = this.mHosts.values().iterator();
        while (it.hasNext()) {
            it.next().mFragmentHostManager.getFragmentManager().dump("  ", fileDescriptor, printWriter, strArr);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    class FragmentHostState {
        private FragmentHostManager mFragmentHostManager;
        private final View mView;

        public FragmentHostState(View view) {
            this.mView = view;
            this.mFragmentHostManager = new FragmentHostManager(FragmentService.this, view);
        }

        public void sendConfigurationChange(final Configuration configuration) {
            FragmentService.this.mHandler.post(new Runnable() { // from class: com.android.systemui.fragments.FragmentService$FragmentHostState$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$sendConfigurationChange$0(configuration);
                }
            });
        }

        public FragmentHostManager getFragmentHostManager() {
            return this.mFragmentHostManager;
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* renamed from: handleSendConfigurationChange, reason: merged with bridge method [inline-methods] */
        public void lambda$sendConfigurationChange$0(Configuration configuration) {
            this.mFragmentHostManager.onConfigurationChanged(configuration);
        }
    }
}
