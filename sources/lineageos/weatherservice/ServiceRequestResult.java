package lineageos.weatherservice;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lineageos.os.Concierge;
import lineageos.weather.WeatherInfo;
import lineageos.weather.WeatherLocation;

/* loaded from: classes2.dex */
public final class ServiceRequestResult implements Parcelable {
    public static final Parcelable.Creator<ServiceRequestResult> CREATOR = new Parcelable.Creator<ServiceRequestResult>() { // from class: lineageos.weatherservice.ServiceRequestResult.1
        @Override // android.os.Parcelable.Creator
        public ServiceRequestResult createFromParcel(Parcel parcel) {
            return new ServiceRequestResult(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public ServiceRequestResult[] newArray(int i) {
            return new ServiceRequestResult[i];
        }
    };
    private String mKey;
    private List<WeatherLocation> mLocationLookupList;
    private WeatherInfo mWeatherInfo;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    private ServiceRequestResult() {
    }

    private ServiceRequestResult(Parcel parcel) {
        Concierge.ParcelInfo parcelInfoReceiveParcel = Concierge.receiveParcel(parcel);
        if (parcelInfoReceiveParcel.getParcelVersion() >= 5) {
            this.mKey = parcel.readString();
            if (parcel.readInt() == 1) {
                this.mWeatherInfo = WeatherInfo.CREATOR.createFromParcel(parcel);
            }
            if (parcel.readInt() == 1) {
                this.mLocationLookupList = new ArrayList();
                for (int i = parcel.readInt(); i > 0; i--) {
                    this.mLocationLookupList.add(WeatherLocation.CREATOR.createFromParcel(parcel));
                }
            }
        }
        parcelInfoReceiveParcel.complete();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        Concierge.ParcelInfo parcelInfoPrepareParcel = Concierge.prepareParcel(parcel);
        parcel.writeString(this.mKey);
        if (this.mWeatherInfo != null) {
            parcel.writeInt(1);
            this.mWeatherInfo.writeToParcel(parcel, 0);
        } else {
            parcel.writeInt(0);
        }
        if (this.mLocationLookupList != null) {
            parcel.writeInt(1);
            parcel.writeInt(this.mLocationLookupList.size());
            Iterator<WeatherLocation> it = this.mLocationLookupList.iterator();
            while (it.hasNext()) {
                it.next().writeToParcel(parcel, 0);
            }
        } else {
            parcel.writeInt(0);
        }
        parcelInfoPrepareParcel.complete();
    }

    public int hashCode() {
        String str = this.mKey;
        return 31 + (str != null ? str.hashCode() : 0);
    }

    public boolean equals(Object obj) {
        if (obj != null && ServiceRequestResult.class == obj.getClass()) {
            return TextUtils.equals(this.mKey, ((ServiceRequestResult) obj).mKey);
        }
        return false;
    }
}
