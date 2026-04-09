package lineageos.weather;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import lineageos.os.Concierge;

/* loaded from: classes2.dex */
public final class WeatherLocation implements Parcelable {
    public static final Parcelable.Creator<WeatherLocation> CREATOR = new Parcelable.Creator<WeatherLocation>() { // from class: lineageos.weather.WeatherLocation.1
        @Override // android.os.Parcelable.Creator
        public WeatherLocation createFromParcel(Parcel parcel) {
            return new WeatherLocation(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public WeatherLocation[] newArray(int i) {
            return new WeatherLocation[i];
        }
    };
    private String mCity;
    private String mCityId;
    private String mCountry;
    private String mCountryId;
    private String mKey;
    private String mPostal;
    private String mState;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    private WeatherLocation() {
    }

    private WeatherLocation(Parcel parcel) {
        Concierge.ParcelInfo parcelInfoReceiveParcel = Concierge.receiveParcel(parcel);
        if (parcelInfoReceiveParcel.getParcelVersion() >= 5) {
            this.mKey = parcel.readString();
            this.mCityId = parcel.readString();
            this.mCity = parcel.readString();
            this.mState = parcel.readString();
            this.mPostal = parcel.readString();
            this.mCountryId = parcel.readString();
            this.mCountry = parcel.readString();
        }
        parcelInfoReceiveParcel.complete();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        Concierge.ParcelInfo parcelInfoPrepareParcel = Concierge.prepareParcel(parcel);
        parcel.writeString(this.mKey);
        parcel.writeString(this.mCityId);
        parcel.writeString(this.mCity);
        parcel.writeString(this.mState);
        parcel.writeString(this.mPostal);
        parcel.writeString(this.mCountryId);
        parcel.writeString(this.mCountry);
        parcelInfoPrepareParcel.complete();
    }

    public String toString() {
        return "{ City ID: " + this.mCityId + " City: " + this.mCity + " State: " + this.mState + " Postal/ZIP Code: " + this.mPostal + " Country Id: " + this.mCountryId + " Country: " + this.mCountry + "}";
    }

    public int hashCode() {
        String str = this.mKey;
        return 31 + (str != null ? str.hashCode() : 0);
    }

    public boolean equals(Object obj) {
        if (obj != null && WeatherLocation.class == obj.getClass()) {
            return TextUtils.equals(this.mKey, ((WeatherLocation) obj).mKey);
        }
        return false;
    }
}
