package lineageos.profiles;

import android.os.Parcel;
import android.os.Parcelable;
import lineageos.os.Concierge;

/* loaded from: classes2.dex */
public final class BrightnessSettings implements Parcelable {
    public static final Parcelable.Creator<BrightnessSettings> CREATOR = new Parcelable.Creator<BrightnessSettings>() { // from class: lineageos.profiles.BrightnessSettings.1
        @Override // android.os.Parcelable.Creator
        public BrightnessSettings createFromParcel(Parcel parcel) {
            return new BrightnessSettings(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public BrightnessSettings[] newArray(int i) {
            return new BrightnessSettings[i];
        }
    };
    private boolean mDirty;
    private boolean mOverride;
    private int mValue;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public BrightnessSettings(Parcel parcel) {
        readFromParcel(parcel);
    }

    public BrightnessSettings() {
        this(0, false);
    }

    public BrightnessSettings(int i, boolean z) {
        this.mValue = i;
        this.mOverride = z;
        this.mDirty = false;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        Concierge.ParcelInfo parcelInfoPrepareParcel = Concierge.prepareParcel(parcel);
        parcel.writeInt(this.mOverride ? 1 : 0);
        parcel.writeInt(this.mValue);
        parcel.writeInt(this.mDirty ? 1 : 0);
        parcelInfoPrepareParcel.complete();
    }

    public void readFromParcel(Parcel parcel) {
        Concierge.ParcelInfo parcelInfoReceiveParcel = Concierge.receiveParcel(parcel);
        if (parcelInfoReceiveParcel.getParcelVersion() >= 2) {
            this.mOverride = parcel.readInt() != 0;
            this.mValue = parcel.readInt();
            this.mDirty = parcel.readInt() != 0;
        }
        parcelInfoReceiveParcel.complete();
    }
}
