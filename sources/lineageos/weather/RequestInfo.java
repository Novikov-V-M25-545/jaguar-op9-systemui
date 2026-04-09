package lineageos.weather;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import lineageos.os.Concierge;
import lineageos.weather.IRequestInfoListener;

/* loaded from: classes2.dex */
public final class RequestInfo implements Parcelable {
    public static final Parcelable.Creator<RequestInfo> CREATOR = new Parcelable.Creator<RequestInfo>() { // from class: lineageos.weather.RequestInfo.1
        @Override // android.os.Parcelable.Creator
        public RequestInfo createFromParcel(Parcel parcel) {
            return new RequestInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RequestInfo[] newArray(int i) {
            return new RequestInfo[i];
        }
    };
    private String mCityName;
    private boolean mIsQueryOnly;
    private String mKey;
    private IRequestInfoListener mListener;
    private Location mLocation;
    private int mRequestType;
    private int mTempUnit;
    private WeatherLocation mWeatherLocation;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    private RequestInfo() {
    }

    private RequestInfo(Parcel parcel) {
        Concierge.ParcelInfo parcelInfoReceiveParcel = Concierge.receiveParcel(parcel);
        if (parcelInfoReceiveParcel.getParcelVersion() >= 5) {
            this.mKey = parcel.readString();
            int i = parcel.readInt();
            this.mRequestType = i;
            if (i == 1) {
                this.mLocation = (Location) Location.CREATOR.createFromParcel(parcel);
                this.mTempUnit = parcel.readInt();
            } else if (i == 2) {
                this.mWeatherLocation = WeatherLocation.CREATOR.createFromParcel(parcel);
                this.mTempUnit = parcel.readInt();
            } else if (i == 3) {
                this.mCityName = parcel.readString();
            }
            this.mIsQueryOnly = parcel.readInt() == 1;
            this.mListener = IRequestInfoListener.Stub.asInterface(parcel.readStrongBinder());
        }
        parcelInfoReceiveParcel.complete();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        Concierge.ParcelInfo parcelInfoPrepareParcel = Concierge.prepareParcel(parcel);
        parcel.writeString(this.mKey);
        parcel.writeInt(this.mRequestType);
        int i2 = this.mRequestType;
        if (i2 == 1) {
            this.mLocation.writeToParcel(parcel, 0);
            parcel.writeInt(this.mTempUnit);
        } else if (i2 == 2) {
            this.mWeatherLocation.writeToParcel(parcel, 0);
            parcel.writeInt(this.mTempUnit);
        } else if (i2 == 3) {
            parcel.writeString(this.mCityName);
        }
        parcel.writeInt(this.mIsQueryOnly ? 1 : 0);
        parcel.writeStrongBinder(this.mListener.asBinder());
        parcelInfoPrepareParcel.complete();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ Request for ");
        int i = this.mRequestType;
        if (i == 1) {
            sb.append("Location: ");
            sb.append(this.mLocation);
            sb.append(" Temp Unit: ");
            if (this.mTempUnit == 2) {
                sb.append("Fahrenheit");
            } else {
                sb.append(" Celsius");
            }
        } else if (i == 2) {
            sb.append("WeatherLocation: ");
            sb.append(this.mWeatherLocation);
            sb.append(" Temp Unit: ");
            if (this.mTempUnit == 2) {
                sb.append("Fahrenheit");
            } else {
                sb.append(" Celsius");
            }
        } else if (i == 3) {
            sb.append("Lookup City: ");
            sb.append(this.mCityName);
        }
        sb.append(" }");
        return sb.toString();
    }

    public int hashCode() {
        String str = this.mKey;
        return 31 + (str != null ? str.hashCode() : 0);
    }

    public boolean equals(Object obj) {
        if (obj != null && RequestInfo.class == obj.getClass()) {
            return TextUtils.equals(this.mKey, ((RequestInfo) obj).mKey);
        }
        return false;
    }
}
