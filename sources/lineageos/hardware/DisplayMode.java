package lineageos.hardware;

import android.os.Parcel;
import android.os.Parcelable;
import lineageos.os.Concierge;

/* loaded from: classes2.dex */
public class DisplayMode implements Parcelable {
    public static final Parcelable.Creator<DisplayMode> CREATOR = new Parcelable.Creator<DisplayMode>() { // from class: lineageos.hardware.DisplayMode.1
        @Override // android.os.Parcelable.Creator
        public DisplayMode createFromParcel(Parcel parcel) {
            return new DisplayMode(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public DisplayMode[] newArray(int i) {
            return new DisplayMode[i];
        }
    };
    public final int id;
    public final String name;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    private DisplayMode(Parcel parcel) {
        int i;
        Concierge.ParcelInfo parcelInfoReceiveParcel = Concierge.receiveParcel(parcel);
        String string = null;
        if (parcelInfoReceiveParcel.getParcelVersion() >= 2) {
            i = parcel.readInt();
            if (parcel.readInt() != 0) {
                string = parcel.readString();
            }
        } else {
            i = -1;
        }
        this.id = i;
        this.name = string;
        parcelInfoReceiveParcel.complete();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        Concierge.ParcelInfo parcelInfoPrepareParcel = Concierge.prepareParcel(parcel);
        parcel.writeInt(this.id);
        if (this.name != null) {
            parcel.writeInt(1);
            parcel.writeString(this.name);
        } else {
            parcel.writeInt(0);
        }
        parcelInfoPrepareParcel.complete();
    }
}
