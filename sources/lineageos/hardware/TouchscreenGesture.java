package lineageos.hardware;

import android.os.Parcel;
import android.os.Parcelable;

/* loaded from: classes2.dex */
public class TouchscreenGesture implements Parcelable {
    public static final Parcelable.Creator<TouchscreenGesture> CREATOR = new Parcelable.Creator<TouchscreenGesture>() { // from class: lineageos.hardware.TouchscreenGesture.1
        @Override // android.os.Parcelable.Creator
        public TouchscreenGesture createFromParcel(Parcel parcel) {
            return new TouchscreenGesture(parcel.readInt(), parcel.readString(), parcel.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public TouchscreenGesture[] newArray(int i) {
            return new TouchscreenGesture[i];
        }
    };
    public final int id;
    public final int keycode;
    public final String name;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public TouchscreenGesture(int i, String str, int i2) {
        this.id = i;
        this.name = str;
        this.keycode = i2;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.id);
        parcel.writeString(this.name);
        parcel.writeInt(this.keycode);
    }
}
