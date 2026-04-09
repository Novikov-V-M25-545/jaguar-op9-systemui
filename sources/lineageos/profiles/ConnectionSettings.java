package lineageos.profiles;

import android.os.Parcel;
import android.os.Parcelable;
import lineageos.os.Concierge;

/* loaded from: classes2.dex */
public final class ConnectionSettings implements Parcelable {
    public static final Parcelable.Creator<ConnectionSettings> CREATOR = new Parcelable.Creator<ConnectionSettings>() { // from class: lineageos.profiles.ConnectionSettings.1
        @Override // android.os.Parcelable.Creator
        public ConnectionSettings createFromParcel(Parcel parcel) {
            return new ConnectionSettings(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public ConnectionSettings[] newArray(int i) {
            return new ConnectionSettings[i];
        }
    };
    private int mConnectionId;
    private boolean mDirty;
    private boolean mOverride;
    private int mSubId = -1;
    private int mValue;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public ConnectionSettings(Parcel parcel) {
        readFromParcel(parcel);
    }

    public int getConnectionId() {
        return this.mConnectionId;
    }

    public int getSubId() {
        return this.mSubId;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        Concierge.ParcelInfo parcelInfoPrepareParcel = Concierge.prepareParcel(parcel);
        parcel.writeInt(this.mConnectionId);
        parcel.writeInt(this.mOverride ? 1 : 0);
        parcel.writeInt(this.mValue);
        parcel.writeInt(this.mDirty ? 1 : 0);
        if (this.mConnectionId == 9) {
            parcel.writeInt(this.mSubId);
        }
        parcelInfoPrepareParcel.complete();
    }

    public void readFromParcel(Parcel parcel) {
        Concierge.ParcelInfo parcelInfoReceiveParcel = Concierge.receiveParcel(parcel);
        int parcelVersion = parcelInfoReceiveParcel.getParcelVersion();
        if (parcelVersion >= 2) {
            this.mConnectionId = parcel.readInt();
            this.mOverride = parcel.readInt() != 0;
            this.mValue = parcel.readInt();
            this.mDirty = parcel.readInt() != 0;
        }
        if (parcelVersion >= 5 && this.mConnectionId == 9) {
            this.mSubId = parcel.readInt();
        }
        parcelInfoReceiveParcel.complete();
    }
}
