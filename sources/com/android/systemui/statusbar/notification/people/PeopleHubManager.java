package com.android.systemui.statusbar.notification.people;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: PeopleHubNotificationListener.kt */
/* loaded from: classes.dex */
public final class PeopleHubManager {
    private final Map<String, PersonModel> activePeople = new LinkedHashMap();
    private final ArrayDeque<PersonModel> inactivePeople = new ArrayDeque<>(10);

    public final boolean migrateActivePerson(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        PersonModel personModelRemove = this.activePeople.remove(key);
        if (personModelRemove == null) {
            return false;
        }
        if (this.inactivePeople.size() >= 10) {
            this.inactivePeople.removeLast();
        }
        this.inactivePeople.addFirst(personModelRemove);
        return true;
    }

    public final void removeActivePerson(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        this.activePeople.remove(key);
    }

    public final boolean addActivePerson(@NotNull final PersonModel person) {
        Intrinsics.checkParameterIsNotNull(person, "person");
        this.activePeople.put(person.getKey(), person);
        return this.inactivePeople.removeIf(new Predicate<PersonModel>() { // from class: com.android.systemui.statusbar.notification.people.PeopleHubManager.addActivePerson.1
            @Override // java.util.function.Predicate
            public final boolean test(PersonModel personModel) {
                return Intrinsics.areEqual(personModel.getKey(), person.getKey());
            }
        });
    }

    @NotNull
    public final PeopleHubModel getPeopleHubModel() {
        return new PeopleHubModel(this.inactivePeople);
    }
}
