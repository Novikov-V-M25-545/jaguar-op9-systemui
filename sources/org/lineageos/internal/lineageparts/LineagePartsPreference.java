package org.lineageos.internal.lineageparts;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import androidx.preference.R$attr;
import lineageos.preference.RemotePreference;

/* loaded from: classes2.dex */
public class LineagePartsPreference extends RemotePreference {
    private final Context mContext;
    private final PartInfo mPart;

    public LineagePartsPreference(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mContext = context;
        PartInfo partInfo = PartsList.get(context).getPartInfo(getKey());
        this.mPart = partInfo;
        if (partInfo == null) {
            throw new RuntimeException("Part not found: " + getKey());
        }
        updatePreference();
        setIntent(partInfo.getIntentForActivity());
    }

    public LineagePartsPreference(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public LineagePartsPreference(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R$attr.preferenceScreenStyle);
    }

    @Override // lineageos.preference.RemotePreference, lineageos.preference.RemotePreferenceManager.OnRemoteUpdateListener
    public void onRemoteUpdated(Bundle bundle) {
        PartInfo partInfo;
        if (!bundle.containsKey(":lineage:part") || (partInfo = (PartInfo) bundle.getParcelable(":lineage:part")) == null) {
            return;
        }
        this.mPart.updateFrom(partInfo);
        updatePreference();
    }

    @Override // lineageos.preference.RemotePreference
    protected String getRemoteKey(Bundle bundle) {
        return getKey();
    }

    private void updatePreference() {
        if (isAvailable() != this.mPart.isAvailable()) {
            setAvailable(this.mPart.isAvailable());
        }
        if (isAvailable()) {
            setTitle(this.mPart.getTitle());
            setSummary(this.mPart.getSummary());
        }
    }
}
