package com.android.systemui.statusbar.notification.people;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: PeopleHubViewController.kt */
/* loaded from: classes.dex */
public final class PeopleHubViewAdapterImpl implements PeopleHubViewAdapter {
    private final DataSource<?> dataSource;

    public PeopleHubViewAdapterImpl(@NotNull DataSource<?> dataSource) {
        Intrinsics.checkParameterIsNotNull(dataSource, "dataSource");
        this.dataSource = dataSource;
    }
}
