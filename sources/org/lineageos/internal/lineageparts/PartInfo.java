package org.lineageos.internal.lineageparts;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;
import lineageos.os.Concierge;

/* loaded from: classes2.dex */
public class PartInfo implements Parcelable {
    private boolean mAvailable;
    private String mFragmentClass;
    private int mIconRes;
    private final String mName;
    private String mSummary;
    private String mTitle;
    private int mXmlRes;
    private static final String TAG = PartInfo.class.getSimpleName();
    public static final Parcelable.Creator<PartInfo> CREATOR = new Parcelable.Creator<PartInfo>() { // from class: org.lineageos.internal.lineageparts.PartInfo.1
        @Override // android.os.Parcelable.Creator
        public PartInfo createFromParcel(Parcel parcel) {
            return new PartInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public PartInfo[] newArray(int i) {
            return new PartInfo[i];
        }
    };

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public PartInfo(String str, String str2, String str3) {
        this.mAvailable = true;
        this.mXmlRes = 0;
        this.mName = str;
        this.mTitle = str2;
        this.mSummary = str3;
    }

    public PartInfo(String str) {
        this(str, null, null);
    }

    public PartInfo(Parcel parcel) {
        this.mAvailable = true;
        this.mXmlRes = 0;
        Concierge.receiveParcel(parcel).getParcelVersion();
        this.mName = parcel.readString();
        this.mTitle = parcel.readString();
        this.mSummary = parcel.readString();
        this.mFragmentClass = parcel.readString();
        this.mIconRes = parcel.readInt();
        this.mAvailable = parcel.readInt() == 1;
        this.mXmlRes = parcel.readInt();
    }

    public void setTitle(String str) {
        this.mTitle = str;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public void setSummary(String str) {
        this.mSummary = str;
    }

    public String getSummary() {
        return this.mSummary;
    }

    public String getFragmentClass() {
        return this.mFragmentClass;
    }

    public void setFragmentClass(String str) {
        this.mFragmentClass = str;
    }

    public int getIconRes() {
        return this.mIconRes;
    }

    public void setIconRes(int i) {
        this.mIconRes = i;
    }

    public boolean isAvailable() {
        return this.mAvailable;
    }

    public void setAvailable(boolean z) {
        this.mAvailable = z;
    }

    public int getXmlRes() {
        return this.mXmlRes;
    }

    public void setXmlRes(int i) {
        this.mXmlRes = i;
    }

    public boolean updateFrom(PartInfo partInfo) {
        if (partInfo == null || partInfo.equals(this)) {
            return false;
        }
        setTitle(partInfo.getTitle());
        setSummary(partInfo.getSummary());
        setFragmentClass(partInfo.getFragmentClass());
        setIconRes(partInfo.getIconRes());
        setAvailable(partInfo.isAvailable());
        setXmlRes(partInfo.getXmlRes());
        return true;
    }

    public String toString() {
        return String.format("PartInfo=[ name=%s title=%s summary=%s fragment=%s xmlRes=%x ]", this.mName, this.mTitle, this.mSummary, this.mFragmentClass, Integer.valueOf(this.mXmlRes));
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        Concierge.ParcelInfo parcelInfoPrepareParcel = Concierge.prepareParcel(parcel);
        parcel.writeString(this.mName);
        parcel.writeString(this.mTitle);
        parcel.writeString(this.mSummary);
        parcel.writeString(this.mFragmentClass);
        parcel.writeInt(this.mIconRes);
        parcel.writeInt(this.mAvailable ? 1 : 0);
        parcel.writeInt(this.mXmlRes);
        parcelInfoPrepareParcel.complete();
    }

    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PartInfo partInfo = (PartInfo) obj;
        return Objects.equals(this.mName, partInfo.mName) && Objects.equals(this.mTitle, partInfo.mTitle) && Objects.equals(this.mSummary, partInfo.mSummary) && Objects.equals(this.mFragmentClass, partInfo.mFragmentClass) && Objects.equals(Integer.valueOf(this.mIconRes), Integer.valueOf(partInfo.mIconRes)) && Objects.equals(Boolean.valueOf(this.mAvailable), Boolean.valueOf(partInfo.mAvailable)) && Objects.equals(Integer.valueOf(this.mXmlRes), Integer.valueOf(partInfo.mXmlRes));
    }

    public String getAction() {
        return "org.lineageos.lineageparts.parts." + this.mName;
    }

    public Intent getIntentForActivity() {
        Intent intent = new Intent(getAction());
        intent.setComponent(PartsList.LINEAGEPARTS_ACTIVITY);
        return intent;
    }
}
