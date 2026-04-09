package lineageos.profiles;

import android.os.Parcel;
import android.os.Parcelable;
import lineageos.os.Concierge;

/* loaded from: classes2.dex */
public final class StreamSettings implements Parcelable {
    public static final Parcelable.Creator<StreamSettings> CREATOR = new Parcelable.Creator<StreamSettings>() { // from class: lineageos.profiles.StreamSettings.1
        @Override // android.os.Parcelable.Creator
        public StreamSettings createFromParcel(Parcel parcel) {
            return new StreamSettings(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public StreamSettings[] newArray(int i) {
            return new StreamSettings[i];
        }
    };
    private boolean mDirty;
    private boolean mOverride;
    private int mStreamId;
    private int mValue;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public StreamSettings(Parcel parcel) {
        readFromParcel(parcel);
    }

    public int getStreamId() {
        return this.mStreamId;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        Concierge.ParcelInfo parcelInfoPrepareParcel = Concierge.prepareParcel(parcel);
        parcel.writeInt(this.mStreamId);
        parcel.writeInt(this.mOverride ? 1 : 0);
        parcel.writeInt(this.mValue);
        parcel.writeInt(this.mDirty ? 1 : 0);
        parcelInfoPrepareParcel.complete();
    }

    public void readFromParcel(Parcel parcel) {
        Concierge.ParcelInfo parcelInfoReceiveParcel = Concierge.receiveParcel(parcel);
        if (parcelInfoReceiveParcel.getParcelVersion() >= 2) {
            this.mStreamId = parcel.readInt();
            this.mOverride = parcel.readInt() != 0;
            this.mValue = parcel.readInt();
            this.mDirty = parcel.readInt() != 0;
        }
        parcelInfoReceiveParcel.complete();
    }
}
