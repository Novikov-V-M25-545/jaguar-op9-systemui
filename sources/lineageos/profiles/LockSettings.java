package lineageos.profiles;

import android.os.Parcel;
import android.os.Parcelable;
import lineageos.os.Concierge;

/* loaded from: classes2.dex */
public final class LockSettings implements Parcelable {
    private boolean mDirty;
    private int mValue;
    private static final String TAG = LockSettings.class.getSimpleName();
    public static final Parcelable.Creator<LockSettings> CREATOR = new Parcelable.Creator<LockSettings>() { // from class: lineageos.profiles.LockSettings.1
        @Override // android.os.Parcelable.Creator
        public LockSettings createFromParcel(Parcel parcel) {
            return new LockSettings(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public LockSettings[] newArray(int i) {
            return new LockSettings[i];
        }
    };

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public LockSettings(Parcel parcel) {
        readFromParcel(parcel);
    }

    public LockSettings() {
        this(0);
    }

    public LockSettings(int i) {
        this.mValue = i;
        this.mDirty = false;
    }

    public int getValue() {
        return this.mValue;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        Concierge.ParcelInfo parcelInfoPrepareParcel = Concierge.prepareParcel(parcel);
        parcel.writeInt(this.mValue);
        parcel.writeInt(this.mDirty ? 1 : 0);
        parcelInfoPrepareParcel.complete();
    }

    public void readFromParcel(Parcel parcel) {
        Concierge.ParcelInfo parcelInfoReceiveParcel = Concierge.receiveParcel(parcel);
        if (parcelInfoReceiveParcel.getParcelVersion() >= 2) {
            this.mValue = parcel.readInt();
            this.mDirty = parcel.readInt() != 0;
        }
        parcelInfoReceiveParcel.complete();
    }
}
