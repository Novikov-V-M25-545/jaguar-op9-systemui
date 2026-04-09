package com.android.systemui.media;

import android.app.PendingIntent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.session.MediaSession;
import java.util.List;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import lineageos.hardware.LineageHardwareManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaData.kt */
/* loaded from: classes.dex */
public final class MediaData {

    @NotNull
    private final List<MediaAction> actions;

    @NotNull
    private final List<Integer> actionsToShowInCompact;
    private boolean active;

    @Nullable
    private final String app;

    @Nullable
    private final Drawable appIcon;

    @Nullable
    private final CharSequence artist;

    @Nullable
    private final Icon artwork;
    private final int backgroundColor;

    @Nullable
    private final PendingIntent clickIntent;

    @Nullable
    private final MediaDeviceData device;
    private boolean hasCheckedForResume;
    private final boolean initialized;
    private final boolean isClearable;
    private boolean isLocalSession;

    @Nullable
    private final Boolean isPlaying;

    @Nullable
    private final String notificationKey;

    @NotNull
    private final String packageName;

    @Nullable
    private Runnable resumeAction;
    private boolean resumption;

    @Nullable
    private final CharSequence song;

    @Nullable
    private final MediaSession.Token token;
    private final int userId;

    @NotNull
    public final MediaData copy(int i, boolean z, int i2, @Nullable String str, @Nullable Drawable drawable, @Nullable CharSequence charSequence, @Nullable CharSequence charSequence2, @Nullable Icon icon, @NotNull List<MediaAction> actions, @NotNull List<Integer> actionsToShowInCompact, @NotNull String packageName, @Nullable MediaSession.Token token, @Nullable PendingIntent pendingIntent, @Nullable MediaDeviceData mediaDeviceData, boolean z2, @Nullable Runnable runnable, boolean z3, boolean z4, @Nullable String str2, boolean z5, @Nullable Boolean bool, boolean z6) {
        Intrinsics.checkParameterIsNotNull(actions, "actions");
        Intrinsics.checkParameterIsNotNull(actionsToShowInCompact, "actionsToShowInCompact");
        Intrinsics.checkParameterIsNotNull(packageName, "packageName");
        return new MediaData(i, z, i2, str, drawable, charSequence, charSequence2, icon, actions, actionsToShowInCompact, packageName, token, pendingIntent, mediaDeviceData, z2, runnable, z3, z4, str2, z5, bool, z6);
    }

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MediaData)) {
            return false;
        }
        MediaData mediaData = (MediaData) obj;
        return this.userId == mediaData.userId && this.initialized == mediaData.initialized && this.backgroundColor == mediaData.backgroundColor && Intrinsics.areEqual(this.app, mediaData.app) && Intrinsics.areEqual(this.appIcon, mediaData.appIcon) && Intrinsics.areEqual(this.artist, mediaData.artist) && Intrinsics.areEqual(this.song, mediaData.song) && Intrinsics.areEqual(this.artwork, mediaData.artwork) && Intrinsics.areEqual(this.actions, mediaData.actions) && Intrinsics.areEqual(this.actionsToShowInCompact, mediaData.actionsToShowInCompact) && Intrinsics.areEqual(this.packageName, mediaData.packageName) && Intrinsics.areEqual(this.token, mediaData.token) && Intrinsics.areEqual(this.clickIntent, mediaData.clickIntent) && Intrinsics.areEqual(this.device, mediaData.device) && this.active == mediaData.active && Intrinsics.areEqual(this.resumeAction, mediaData.resumeAction) && this.isLocalSession == mediaData.isLocalSession && this.resumption == mediaData.resumption && Intrinsics.areEqual(this.notificationKey, mediaData.notificationKey) && this.hasCheckedForResume == mediaData.hasCheckedForResume && Intrinsics.areEqual(this.isPlaying, mediaData.isPlaying) && this.isClearable == mediaData.isClearable;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public int hashCode() {
        int iHashCode = Integer.hashCode(this.userId) * 31;
        boolean z = this.initialized;
        int i = z;
        if (z != 0) {
            i = 1;
        }
        int iHashCode2 = (((iHashCode + i) * 31) + Integer.hashCode(this.backgroundColor)) * 31;
        String str = this.app;
        int iHashCode3 = (iHashCode2 + (str != null ? str.hashCode() : 0)) * 31;
        Drawable drawable = this.appIcon;
        int iHashCode4 = (iHashCode3 + (drawable != null ? drawable.hashCode() : 0)) * 31;
        CharSequence charSequence = this.artist;
        int iHashCode5 = (iHashCode4 + (charSequence != null ? charSequence.hashCode() : 0)) * 31;
        CharSequence charSequence2 = this.song;
        int iHashCode6 = (iHashCode5 + (charSequence2 != null ? charSequence2.hashCode() : 0)) * 31;
        Icon icon = this.artwork;
        int iHashCode7 = (iHashCode6 + (icon != null ? icon.hashCode() : 0)) * 31;
        List<MediaAction> list = this.actions;
        int iHashCode8 = (iHashCode7 + (list != null ? list.hashCode() : 0)) * 31;
        List<Integer> list2 = this.actionsToShowInCompact;
        int iHashCode9 = (iHashCode8 + (list2 != null ? list2.hashCode() : 0)) * 31;
        String str2 = this.packageName;
        int iHashCode10 = (iHashCode9 + (str2 != null ? str2.hashCode() : 0)) * 31;
        MediaSession.Token token = this.token;
        int iHashCode11 = (iHashCode10 + (token != null ? token.hashCode() : 0)) * 31;
        PendingIntent pendingIntent = this.clickIntent;
        int iHashCode12 = (iHashCode11 + (pendingIntent != null ? pendingIntent.hashCode() : 0)) * 31;
        MediaDeviceData mediaDeviceData = this.device;
        int iHashCode13 = (iHashCode12 + (mediaDeviceData != null ? mediaDeviceData.hashCode() : 0)) * 31;
        boolean z2 = this.active;
        int i2 = z2;
        if (z2 != 0) {
            i2 = 1;
        }
        int i3 = (iHashCode13 + i2) * 31;
        Runnable runnable = this.resumeAction;
        int iHashCode14 = (i3 + (runnable != null ? runnable.hashCode() : 0)) * 31;
        boolean z3 = this.isLocalSession;
        int i4 = z3;
        if (z3 != 0) {
            i4 = 1;
        }
        int i5 = (iHashCode14 + i4) * 31;
        boolean z4 = this.resumption;
        int i6 = z4;
        if (z4 != 0) {
            i6 = 1;
        }
        int i7 = (i5 + i6) * 31;
        String str3 = this.notificationKey;
        int iHashCode15 = (i7 + (str3 != null ? str3.hashCode() : 0)) * 31;
        boolean z5 = this.hasCheckedForResume;
        int i8 = z5;
        if (z5 != 0) {
            i8 = 1;
        }
        int i9 = (iHashCode15 + i8) * 31;
        Boolean bool = this.isPlaying;
        int iHashCode16 = (i9 + (bool != null ? bool.hashCode() : 0)) * 31;
        boolean z6 = this.isClearable;
        return iHashCode16 + (z6 ? 1 : z6 ? 1 : 0);
    }

    @NotNull
    public String toString() {
        return "MediaData(userId=" + this.userId + ", initialized=" + this.initialized + ", backgroundColor=" + this.backgroundColor + ", app=" + this.app + ", appIcon=" + this.appIcon + ", artist=" + this.artist + ", song=" + this.song + ", artwork=" + this.artwork + ", actions=" + this.actions + ", actionsToShowInCompact=" + this.actionsToShowInCompact + ", packageName=" + this.packageName + ", token=" + this.token + ", clickIntent=" + this.clickIntent + ", device=" + this.device + ", active=" + this.active + ", resumeAction=" + this.resumeAction + ", isLocalSession=" + this.isLocalSession + ", resumption=" + this.resumption + ", notificationKey=" + this.notificationKey + ", hasCheckedForResume=" + this.hasCheckedForResume + ", isPlaying=" + this.isPlaying + ", isClearable=" + this.isClearable + ")";
    }

    public MediaData(int i, boolean z, int i2, @Nullable String str, @Nullable Drawable drawable, @Nullable CharSequence charSequence, @Nullable CharSequence charSequence2, @Nullable Icon icon, @NotNull List<MediaAction> actions, @NotNull List<Integer> actionsToShowInCompact, @NotNull String packageName, @Nullable MediaSession.Token token, @Nullable PendingIntent pendingIntent, @Nullable MediaDeviceData mediaDeviceData, boolean z2, @Nullable Runnable runnable, boolean z3, boolean z4, @Nullable String str2, boolean z5, @Nullable Boolean bool, boolean z6) {
        Intrinsics.checkParameterIsNotNull(actions, "actions");
        Intrinsics.checkParameterIsNotNull(actionsToShowInCompact, "actionsToShowInCompact");
        Intrinsics.checkParameterIsNotNull(packageName, "packageName");
        this.userId = i;
        this.initialized = z;
        this.backgroundColor = i2;
        this.app = str;
        this.appIcon = drawable;
        this.artist = charSequence;
        this.song = charSequence2;
        this.artwork = icon;
        this.actions = actions;
        this.actionsToShowInCompact = actionsToShowInCompact;
        this.packageName = packageName;
        this.token = token;
        this.clickIntent = pendingIntent;
        this.device = mediaDeviceData;
        this.active = z2;
        this.resumeAction = runnable;
        this.isLocalSession = z3;
        this.resumption = z4;
        this.notificationKey = str2;
        this.hasCheckedForResume = z5;
        this.isPlaying = bool;
        this.isClearable = z6;
    }

    public final int getUserId() {
        return this.userId;
    }

    public final int getBackgroundColor() {
        return this.backgroundColor;
    }

    @Nullable
    public final String getApp() {
        return this.app;
    }

    @Nullable
    public final Drawable getAppIcon() {
        return this.appIcon;
    }

    @Nullable
    public final CharSequence getArtist() {
        return this.artist;
    }

    @Nullable
    public final CharSequence getSong() {
        return this.song;
    }

    @Nullable
    public final Icon getArtwork() {
        return this.artwork;
    }

    @NotNull
    public final List<MediaAction> getActions() {
        return this.actions;
    }

    @NotNull
    public final List<Integer> getActionsToShowInCompact() {
        return this.actionsToShowInCompact;
    }

    @NotNull
    public final String getPackageName() {
        return this.packageName;
    }

    @Nullable
    public final MediaSession.Token getToken() {
        return this.token;
    }

    @Nullable
    public final PendingIntent getClickIntent() {
        return this.clickIntent;
    }

    @Nullable
    public final MediaDeviceData getDevice() {
        return this.device;
    }

    public final boolean getActive() {
        return this.active;
    }

    public final void setActive(boolean z) {
        this.active = z;
    }

    @Nullable
    public final Runnable getResumeAction() {
        return this.resumeAction;
    }

    public final void setResumeAction(@Nullable Runnable runnable) {
        this.resumeAction = runnable;
    }

    public final boolean isLocalSession() {
        return this.isLocalSession;
    }

    public final boolean getResumption() {
        return this.resumption;
    }

    public final boolean getHasCheckedForResume() {
        return this.hasCheckedForResume;
    }

    public final void setHasCheckedForResume(boolean z) {
        this.hasCheckedForResume = z;
    }

    @Nullable
    public final Boolean isPlaying() {
        return this.isPlaying;
    }

    public /* synthetic */ MediaData(int i, boolean z, int i2, String str, Drawable drawable, CharSequence charSequence, CharSequence charSequence2, Icon icon, List list, List list2, String str2, MediaSession.Token token, PendingIntent pendingIntent, MediaDeviceData mediaDeviceData, boolean z2, Runnable runnable, boolean z3, boolean z4, String str3, boolean z5, Boolean bool, boolean z6, int i3, DefaultConstructorMarker defaultConstructorMarker) {
        this(i, (i3 & 2) != 0 ? false : z, i2, str, drawable, charSequence, charSequence2, icon, list, list2, str2, token, pendingIntent, mediaDeviceData, z2, runnable, (i3 & 65536) != 0 ? true : z3, (i3 & LineageHardwareManager.FEATURE_COLOR_BALANCE) != 0 ? false : z4, (i3 & LineageHardwareManager.FEATURE_PICTURE_ADJUSTMENT) != 0 ? null : str3, (i3 & LineageHardwareManager.FEATURE_TOUCHSCREEN_GESTURES) != 0 ? false : z5, (i3 & 1048576) != 0 ? null : bool, (i3 & LineageHardwareManager.FEATURE_ANTI_FLICKER) != 0 ? true : z6);
    }

    public final boolean isClearable() {
        return this.isClearable;
    }
}
