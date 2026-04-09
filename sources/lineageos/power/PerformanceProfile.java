package lineageos.power;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;
import lineageos.os.Concierge;

/* loaded from: classes2.dex */
public class PerformanceProfile implements Parcelable, Comparable<PerformanceProfile> {
    public static final Parcelable.Creator<PerformanceProfile> CREATOR = new Parcelable.Creator<PerformanceProfile>() { // from class: lineageos.power.PerformanceProfile.1
        @Override // android.os.Parcelable.Creator
        public PerformanceProfile createFromParcel(Parcel parcel) {
            return new PerformanceProfile(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public PerformanceProfile[] newArray(int i) {
            return new PerformanceProfile[i];
        }
    };
    private final String mDescription;
    private final int mId;
    private final String mName;
    private final float mWeight;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    private PerformanceProfile(Parcel parcel) {
        Concierge.ParcelInfo parcelInfoReceiveParcel = Concierge.receiveParcel(parcel);
        parcelInfoReceiveParcel.getParcelVersion();
        this.mId = parcel.readInt();
        this.mWeight = parcel.readFloat();
        this.mName = parcel.readString();
        this.mDescription = parcel.readString();
        parcelInfoReceiveParcel.complete();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        Concierge.ParcelInfo parcelInfoPrepareParcel = Concierge.prepareParcel(parcel);
        parcel.writeInt(this.mId);
        parcel.writeFloat(this.mWeight);
        parcel.writeString(this.mName);
        parcel.writeString(this.mDescription);
        parcelInfoPrepareParcel.complete();
    }

    @Override // java.lang.Comparable
    public int compareTo(PerformanceProfile performanceProfile) {
        return Float.compare(this.mWeight, performanceProfile.mWeight);
    }

    public boolean equals(Object obj) {
        if (obj != null && getClass().equals(obj.getClass())) {
            return Objects.equals(Integer.valueOf(this.mId), Integer.valueOf(((PerformanceProfile) obj).mId));
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mId));
    }

    public String toString() {
        return String.format("PerformanceProfile[id=%d, weight=%f, name=%s desc=%s]", Integer.valueOf(this.mId), Float.valueOf(this.mWeight), this.mName, this.mDescription);
    }
}
