package com.android.systemui.bubbles.storage;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: BubbleEntity.kt */
/* loaded from: classes.dex */
public final class BubbleEntity {
    private final int desiredHeight;
    private final int desiredHeightResId;

    @NotNull
    private final String key;

    @NotNull
    private final String packageName;

    @NotNull
    private final String shortcutId;

    @Nullable
    private final String title;
    private final int userId;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BubbleEntity)) {
            return false;
        }
        BubbleEntity bubbleEntity = (BubbleEntity) obj;
        return this.userId == bubbleEntity.userId && Intrinsics.areEqual(this.packageName, bubbleEntity.packageName) && Intrinsics.areEqual(this.shortcutId, bubbleEntity.shortcutId) && Intrinsics.areEqual(this.key, bubbleEntity.key) && this.desiredHeight == bubbleEntity.desiredHeight && this.desiredHeightResId == bubbleEntity.desiredHeightResId && Intrinsics.areEqual(this.title, bubbleEntity.title);
    }

    public int hashCode() {
        int iHashCode = Integer.hashCode(this.userId) * 31;
        String str = this.packageName;
        int iHashCode2 = (iHashCode + (str != null ? str.hashCode() : 0)) * 31;
        String str2 = this.shortcutId;
        int iHashCode3 = (iHashCode2 + (str2 != null ? str2.hashCode() : 0)) * 31;
        String str3 = this.key;
        int iHashCode4 = (((((iHashCode3 + (str3 != null ? str3.hashCode() : 0)) * 31) + Integer.hashCode(this.desiredHeight)) * 31) + Integer.hashCode(this.desiredHeightResId)) * 31;
        String str4 = this.title;
        return iHashCode4 + (str4 != null ? str4.hashCode() : 0);
    }

    @NotNull
    public String toString() {
        return "BubbleEntity(userId=" + this.userId + ", packageName=" + this.packageName + ", shortcutId=" + this.shortcutId + ", key=" + this.key + ", desiredHeight=" + this.desiredHeight + ", desiredHeightResId=" + this.desiredHeightResId + ", title=" + this.title + ")";
    }

    public BubbleEntity(int i, @NotNull String packageName, @NotNull String shortcutId, @NotNull String key, int i2, int i3, @Nullable String str) {
        Intrinsics.checkParameterIsNotNull(packageName, "packageName");
        Intrinsics.checkParameterIsNotNull(shortcutId, "shortcutId");
        Intrinsics.checkParameterIsNotNull(key, "key");
        this.userId = i;
        this.packageName = packageName;
        this.shortcutId = shortcutId;
        this.key = key;
        this.desiredHeight = i2;
        this.desiredHeightResId = i3;
        this.title = str;
    }

    public final int getUserId() {
        return this.userId;
    }

    @NotNull
    public final String getPackageName() {
        return this.packageName;
    }

    @NotNull
    public final String getShortcutId() {
        return this.shortcutId;
    }

    @NotNull
    public final String getKey() {
        return this.key;
    }

    public final int getDesiredHeight() {
        return this.desiredHeight;
    }

    public final int getDesiredHeightResId() {
        return this.desiredHeightResId;
    }

    @Nullable
    public final String getTitle() {
        return this.title;
    }
}
