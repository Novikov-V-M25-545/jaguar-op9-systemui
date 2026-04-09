package com.android.systemui.media;

import dagger.internal.Factory;

/* loaded from: classes.dex */
public final class MediaDataCombineLatest_Factory implements Factory<MediaDataCombineLatest> {
    private static final MediaDataCombineLatest_Factory INSTANCE = new MediaDataCombineLatest_Factory();

    @Override // javax.inject.Provider
    public MediaDataCombineLatest get() {
        return provideInstance();
    }

    public static MediaDataCombineLatest provideInstance() {
        return new MediaDataCombineLatest();
    }

    public static MediaDataCombineLatest_Factory create() {
        return INSTANCE;
    }
}
