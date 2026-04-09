package com.android.systemui.media.dialog;

import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class MediaOutputDialogReceiver_Factory implements Factory<MediaOutputDialogReceiver> {
    private final Provider<MediaOutputDialogFactory> mediaOutputDialogFactoryProvider;

    public MediaOutputDialogReceiver_Factory(Provider<MediaOutputDialogFactory> provider) {
        this.mediaOutputDialogFactoryProvider = provider;
    }

    @Override // javax.inject.Provider
    public MediaOutputDialogReceiver get() {
        return provideInstance(this.mediaOutputDialogFactoryProvider);
    }

    public static MediaOutputDialogReceiver provideInstance(Provider<MediaOutputDialogFactory> provider) {
        return new MediaOutputDialogReceiver(provider.get());
    }

    public static MediaOutputDialogReceiver_Factory create(Provider<MediaOutputDialogFactory> provider) {
        return new MediaOutputDialogReceiver_Factory(provider);
    }
}
