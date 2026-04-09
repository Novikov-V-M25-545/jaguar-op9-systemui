package com.android.systemui.statusbar.notification.people;

import com.android.systemui.plugins.ActivityStarter;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: PeopleHubViewController.kt */
/* loaded from: classes.dex */
public final class PeopleHubViewModelFactoryDataSourceImpl implements DataSource<?> {
    private final ActivityStarter activityStarter;
    private final DataSource<PeopleHubModel> dataSource;

    public PeopleHubViewModelFactoryDataSourceImpl(@NotNull ActivityStarter activityStarter, @NotNull DataSource<PeopleHubModel> dataSource) {
        Intrinsics.checkParameterIsNotNull(activityStarter, "activityStarter");
        Intrinsics.checkParameterIsNotNull(dataSource, "dataSource");
        this.activityStarter = activityStarter;
        this.dataSource = dataSource;
    }
}
