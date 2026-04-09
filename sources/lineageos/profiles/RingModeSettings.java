package lineageos.profiles;

import android.os.Parcel;
import android.os.Parcelable;
import lineageos.os.Concierge;

/* loaded from: classes2.dex */
public final class RingModeSettings implements Parcelable {
    public static final Parcelable.Creator<RingModeSettings> CREATOR = new Parcelable.Creator<RingModeSettings>() { // from class: lineageos.profiles.RingModeSettings.1
        @Override // android.os.Parcelable.Creator
        public RingModeSettings createFromParcel(Parcel parcel) {
            return new RingModeSettings(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RingModeSettings[] newArray(int i) {
            return new RingModeSettings[i];
        }
    };
    private boolean mDirty;
    private boolean mOverride;
    private String mValue;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public RingModeSettings(Parcel parcel) {
        readFromParcel(parcel);
    }

    public RingModeSettings() {
        this("normal", false);
    }

    public RingModeSettings(String str, boolean z) {
        this.mValue = str;
        this.mOverride = z;
        this.mDirty = false;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        Concierge.ParcelInfo parcelInfoPrepareParcel = Concierge.prepareParcel(parcel);
        parcel.writeInt(this.mOverride ? 1 : 0);
        parcel.writeString(this.mValue);
        parcel.writeInt(this.mDirty ? 1 : 0);
        parcelInfoPrepareParcel.complete();
    }

    public void readFromParcel(Parcel parcel) {
        Concierge.ParcelInfo parcelInfoReceiveParcel = Concierge.receiveParcel(parcel);
        if (parcelInfoReceiveParcel.getParcelVersion() >= 2) {
            this.mOverride = parcel.readInt() != 0;
            this.mValue = parcel.readString();
            this.mDirty = parcel.readInt() != 0;
        }
        parcelInfoReceiveParcel.complete();
    }
}
