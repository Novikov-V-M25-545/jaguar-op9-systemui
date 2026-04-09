package com.android.systemui;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.AppComponentFactory;
import com.android.systemui.dagger.ContextComponentHelper;

/* loaded from: classes.dex */
public class SystemUIAppComponentFactory extends AppComponentFactory {
    public ContextComponentHelper mComponentHelper;

    public interface ContextAvailableCallback {
        void onContextAvailable(Context context);
    }

    public interface ContextInitializer {
        void setContextAvailableCallback(ContextAvailableCallback contextAvailableCallback);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // androidx.core.app.AppComponentFactory
    public Application instantiateApplicationCompat(ClassLoader classLoader, String str) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        Application applicationInstantiateApplicationCompat = super.instantiateApplicationCompat(classLoader, str);
        if (applicationInstantiateApplicationCompat instanceof ContextInitializer) {
            ((ContextInitializer) applicationInstantiateApplicationCompat).setContextAvailableCallback(new ContextAvailableCallback() { // from class: com.android.systemui.SystemUIAppComponentFactory$$ExternalSyntheticLambda1
                @Override // com.android.systemui.SystemUIAppComponentFactory.ContextAvailableCallback
                public final void onContextAvailable(Context context) {
                    this.f$0.lambda$instantiateApplicationCompat$0(context);
                }
            });
        }
        return applicationInstantiateApplicationCompat;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$instantiateApplicationCompat$0(Context context) {
        SystemUIFactory.createFromConfig(context);
        SystemUIFactory.getInstance().getRootComponent().inject(this);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // androidx.core.app.AppComponentFactory
    public ContentProvider instantiateProviderCompat(ClassLoader classLoader, String str) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        final ContentProvider contentProviderInstantiateProviderCompat = super.instantiateProviderCompat(classLoader, str);
        if (contentProviderInstantiateProviderCompat instanceof ContextInitializer) {
            ((ContextInitializer) contentProviderInstantiateProviderCompat).setContextAvailableCallback(new ContextAvailableCallback() { // from class: com.android.systemui.SystemUIAppComponentFactory$$ExternalSyntheticLambda0
                @Override // com.android.systemui.SystemUIAppComponentFactory.ContextAvailableCallback
                public final void onContextAvailable(Context context) {
                    SystemUIAppComponentFactory.lambda$instantiateProviderCompat$1(contentProviderInstantiateProviderCompat, context);
                }
            });
        }
        return contentProviderInstantiateProviderCompat;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$instantiateProviderCompat$1(ContentProvider contentProvider, Context context) {
        SystemUIFactory.createFromConfig(context);
        SystemUIFactory.getInstance().getRootComponent().inject(contentProvider);
    }

    @Override // androidx.core.app.AppComponentFactory
    public Activity instantiateActivityCompat(ClassLoader classLoader, String str, Intent intent) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        if (this.mComponentHelper == null) {
            SystemUIFactory.getInstance().getRootComponent().inject(this);
        }
        Activity activityResolveActivity = this.mComponentHelper.resolveActivity(str);
        return activityResolveActivity != null ? activityResolveActivity : super.instantiateActivityCompat(classLoader, str, intent);
    }

    @Override // androidx.core.app.AppComponentFactory
    public Service instantiateServiceCompat(ClassLoader classLoader, String str, Intent intent) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        if (this.mComponentHelper == null) {
            SystemUIFactory.getInstance().getRootComponent().inject(this);
        }
        Service serviceResolveService = this.mComponentHelper.resolveService(str);
        return serviceResolveService != null ? serviceResolveService : super.instantiateServiceCompat(classLoader, str, intent);
    }

    @Override // androidx.core.app.AppComponentFactory
    public BroadcastReceiver instantiateReceiverCompat(ClassLoader classLoader, String str, Intent intent) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        if (this.mComponentHelper == null) {
            SystemUIFactory.getInstance().getRootComponent().inject(this);
        }
        BroadcastReceiver broadcastReceiverResolveBroadcastReceiver = this.mComponentHelper.resolveBroadcastReceiver(str);
        return broadcastReceiverResolveBroadcastReceiver != null ? broadcastReceiverResolveBroadcastReceiver : super.instantiateReceiverCompat(classLoader, str, intent);
    }
}
