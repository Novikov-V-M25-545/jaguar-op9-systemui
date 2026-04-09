package com.android.systemui.statusbar.notification.row;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.android.systemui.R;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ForegroundServiceDungeonView.kt */
/* loaded from: classes.dex */
public final class ForegroundServiceDungeonView extends StackScrollerDecorView {
    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView
    @Nullable
    protected View findSecondaryView() {
        return null;
    }

    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView
    public void setVisible(boolean z, boolean z2) {
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public ForegroundServiceDungeonView(@NotNull Context context, @NotNull AttributeSet attrs) {
        super(context, attrs);
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(attrs, "attrs");
    }

    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView
    @Nullable
    protected View findContentView() {
        return findViewById(R.id.foreground_service_dungeon);
    }
}
