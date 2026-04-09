package com.android.systemui.tv;

import android.content.Context;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.dagger.SystemUIRootComponent;

/* loaded from: classes.dex */
public class TvSystemUIFactory extends SystemUIFactory {
    @Override // com.android.systemui.SystemUIFactory
    protected SystemUIRootComponent buildSystemUIRootComponent(Context context) {
        return DaggerTvSystemUIRootComponent.builder().context(context).build();
    }
}
