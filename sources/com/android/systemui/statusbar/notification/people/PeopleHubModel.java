package com.android.systemui.statusbar.notification.people;

import java.util.Collection;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: PeopleHub.kt */
/* loaded from: classes.dex */
public final class PeopleHubModel {

    @NotNull
    private final Collection<PersonModel> people;

    @NotNull
    public final PeopleHubModel copy(@NotNull Collection<PersonModel> people) {
        Intrinsics.checkParameterIsNotNull(people, "people");
        return new PeopleHubModel(people);
    }

    public boolean equals(@Nullable Object obj) {
        if (this != obj) {
            return (obj instanceof PeopleHubModel) && Intrinsics.areEqual(this.people, ((PeopleHubModel) obj).people);
        }
        return true;
    }

    public int hashCode() {
        Collection<PersonModel> collection = this.people;
        if (collection != null) {
            return collection.hashCode();
        }
        return 0;
    }

    @NotNull
    public String toString() {
        return "PeopleHubModel(people=" + this.people + ")";
    }

    public PeopleHubModel(@NotNull Collection<PersonModel> people) {
        Intrinsics.checkParameterIsNotNull(people, "people");
        this.people = people;
    }

    @NotNull
    public final Collection<PersonModel> getPeople() {
        return this.people;
    }
}
