package com.android.systemui.statusbar.phone;

import com.android.systemui.stackdivider.Divider;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public final /* synthetic */ class StatusBar$$ExternalSyntheticLambda43 implements Consumer {
    public static final /* synthetic */ StatusBar$$ExternalSyntheticLambda43 INSTANCE = new StatusBar$$ExternalSyntheticLambda43();

    private /* synthetic */ StatusBar$$ExternalSyntheticLambda43() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((Divider) obj).onAppTransitionFinished();
    }
}
