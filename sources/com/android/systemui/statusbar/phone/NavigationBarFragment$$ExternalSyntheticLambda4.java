package com.android.systemui.statusbar.phone;

import android.view.View;

/* loaded from: classes.dex */
public final /* synthetic */ class NavigationBarFragment$$ExternalSyntheticLambda4 implements View.OnLongClickListener {
    public final /* synthetic */ NavigationBarFragment f$0;

    public /* synthetic */ NavigationBarFragment$$ExternalSyntheticLambda4(NavigationBarFragment navigationBarFragment) {
        this.f$0 = navigationBarFragment;
    }

    @Override // android.view.View.OnLongClickListener
    public final boolean onLongClick(View view) {
        return this.f$0.onLongPressBackRecents(view);
    }
}
