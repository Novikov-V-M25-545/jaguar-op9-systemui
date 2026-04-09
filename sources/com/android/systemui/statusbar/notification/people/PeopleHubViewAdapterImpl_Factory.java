package com.android.systemui.statusbar.notification.people;

import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class PeopleHubViewAdapterImpl_Factory implements Factory<PeopleHubViewAdapterImpl> {
    private final Provider<DataSource<?>> dataSourceProvider;

    public PeopleHubViewAdapterImpl_Factory(Provider<DataSource<?>> provider) {
        this.dataSourceProvider = provider;
    }

    @Override // javax.inject.Provider
    public PeopleHubViewAdapterImpl get() {
        return provideInstance(this.dataSourceProvider);
    }

    public static PeopleHubViewAdapterImpl provideInstance(Provider<DataSource<?>> provider) {
        return new PeopleHubViewAdapterImpl(provider.get());
    }

    public static PeopleHubViewAdapterImpl_Factory create(Provider<DataSource<?>> provider) {
        return new PeopleHubViewAdapterImpl_Factory(provider);
    }
}
