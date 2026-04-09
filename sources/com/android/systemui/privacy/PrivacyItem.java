package com.android.systemui.privacy;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: PrivacyItem.kt */
/* loaded from: classes.dex */
public final class PrivacyItem {

    @NotNull
    private final PrivacyApplication application;

    @NotNull
    private final PrivacyType privacyType;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PrivacyItem)) {
            return false;
        }
        PrivacyItem privacyItem = (PrivacyItem) obj;
        return Intrinsics.areEqual(this.privacyType, privacyItem.privacyType) && Intrinsics.areEqual(this.application, privacyItem.application);
    }

    public int hashCode() {
        PrivacyType privacyType = this.privacyType;
        int iHashCode = (privacyType != null ? privacyType.hashCode() : 0) * 31;
        PrivacyApplication privacyApplication = this.application;
        return iHashCode + (privacyApplication != null ? privacyApplication.hashCode() : 0);
    }

    @NotNull
    public String toString() {
        return "PrivacyItem(privacyType=" + this.privacyType + ", application=" + this.application + ")";
    }

    public PrivacyItem(@NotNull PrivacyType privacyType, @NotNull PrivacyApplication application) {
        Intrinsics.checkParameterIsNotNull(privacyType, "privacyType");
        Intrinsics.checkParameterIsNotNull(application, "application");
        this.privacyType = privacyType;
        this.application = application;
    }

    @NotNull
    public final PrivacyApplication getApplication() {
        return this.application;
    }

    @NotNull
    public final PrivacyType getPrivacyType() {
        return this.privacyType;
    }
}
