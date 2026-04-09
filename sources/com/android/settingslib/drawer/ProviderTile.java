package com.android.settingslib.drawer;

import android.content.pm.ProviderInfo;
import android.os.Parcel;

/* loaded from: classes.dex */
public class ProviderTile extends Tile {
    private String mAuthority;
    private String mKey;

    ProviderTile(Parcel parcel) {
        super(parcel);
        this.mAuthority = ((ProviderInfo) this.mComponentInfo).authority;
        this.mKey = getMetaData().getString("com.android.settings.keyhint");
    }
}
