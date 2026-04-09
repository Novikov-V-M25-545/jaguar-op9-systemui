package lineageos.weather;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lineageos.os.Concierge;

/* loaded from: classes2.dex */
public final class WeatherInfo implements Parcelable {
    public static final Parcelable.Creator<WeatherInfo> CREATOR = new Parcelable.Creator<WeatherInfo>() { // from class: lineageos.weather.WeatherInfo.1
        @Override // android.os.Parcelable.Creator
        public WeatherInfo createFromParcel(Parcel parcel) {
            return new WeatherInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public WeatherInfo[] newArray(int i) {
            return new WeatherInfo[i];
        }
    };
    private String mCity;
    private int mConditionCode;
    private List<DayForecast> mForecastList;
    private double mHumidity;
    private String mKey;
    private int mTempUnit;
    private double mTemperature;
    private long mTimestamp;
    private double mTodaysHighTemp;
    private double mTodaysLowTemp;
    private double mWindDirection;
    private double mWindSpeed;
    private int mWindSpeedUnit;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    private WeatherInfo() {
    }

    private WeatherInfo(Parcel parcel) {
        Concierge.ParcelInfo parcelInfoReceiveParcel = Concierge.receiveParcel(parcel);
        if (parcelInfoReceiveParcel.getParcelVersion() >= 5) {
            this.mKey = parcel.readString();
            this.mCity = parcel.readString();
            this.mConditionCode = parcel.readInt();
            this.mTemperature = parcel.readDouble();
            this.mTempUnit = parcel.readInt();
            this.mHumidity = parcel.readDouble();
            this.mWindSpeed = parcel.readDouble();
            this.mWindDirection = parcel.readDouble();
            this.mWindSpeedUnit = parcel.readInt();
            this.mTodaysHighTemp = parcel.readDouble();
            this.mTodaysLowTemp = parcel.readDouble();
            this.mTimestamp = parcel.readLong();
            this.mForecastList = new ArrayList();
            for (int i = parcel.readInt(); i > 0; i--) {
                this.mForecastList.add(DayForecast.CREATOR.createFromParcel(parcel));
            }
        }
        parcelInfoReceiveParcel.complete();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        Concierge.ParcelInfo parcelInfoPrepareParcel = Concierge.prepareParcel(parcel);
        parcel.writeString(this.mKey);
        parcel.writeString(this.mCity);
        parcel.writeInt(this.mConditionCode);
        parcel.writeDouble(this.mTemperature);
        parcel.writeInt(this.mTempUnit);
        parcel.writeDouble(this.mHumidity);
        parcel.writeDouble(this.mWindSpeed);
        parcel.writeDouble(this.mWindDirection);
        parcel.writeInt(this.mWindSpeedUnit);
        parcel.writeDouble(this.mTodaysHighTemp);
        parcel.writeDouble(this.mTodaysLowTemp);
        parcel.writeLong(this.mTimestamp);
        parcel.writeInt(this.mForecastList.size());
        Iterator<DayForecast> it = this.mForecastList.iterator();
        while (it.hasNext()) {
            it.next().writeToParcel(parcel, 0);
        }
        parcelInfoPrepareParcel.complete();
    }

    public static class DayForecast implements Parcelable {
        public static final Parcelable.Creator<DayForecast> CREATOR = new Parcelable.Creator<DayForecast>() { // from class: lineageos.weather.WeatherInfo.DayForecast.1
            @Override // android.os.Parcelable.Creator
            public DayForecast createFromParcel(Parcel parcel) {
                return new DayForecast(parcel);
            }

            @Override // android.os.Parcelable.Creator
            public DayForecast[] newArray(int i) {
                return new DayForecast[i];
            }
        };
        int mConditionCode;
        double mHigh;
        String mKey;
        double mLow;

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        private DayForecast() {
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            Concierge.ParcelInfo parcelInfoPrepareParcel = Concierge.prepareParcel(parcel);
            parcel.writeString(this.mKey);
            parcel.writeDouble(this.mLow);
            parcel.writeDouble(this.mHigh);
            parcel.writeInt(this.mConditionCode);
            parcelInfoPrepareParcel.complete();
        }

        private DayForecast(Parcel parcel) {
            Concierge.ParcelInfo parcelInfoReceiveParcel = Concierge.receiveParcel(parcel);
            if (parcelInfoReceiveParcel.getParcelVersion() >= 5) {
                this.mKey = parcel.readString();
                this.mLow = parcel.readDouble();
                this.mHigh = parcel.readDouble();
                this.mConditionCode = parcel.readInt();
            }
            parcelInfoReceiveParcel.complete();
        }

        public String toString() {
            return "{Low temp: " + this.mLow + " High temp: " + this.mHigh + " Condition code: " + this.mConditionCode + "}";
        }

        public int hashCode() {
            String str = this.mKey;
            return 31 + (str != null ? str.hashCode() : 0);
        }

        public boolean equals(Object obj) {
            if (obj != null && getClass() == obj.getClass()) {
                return TextUtils.equals(this.mKey, ((DayForecast) obj).mKey);
            }
            return false;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" City Name: ");
        sb.append(this.mCity);
        sb.append(" Condition Code: ");
        sb.append(this.mConditionCode);
        sb.append(" Temperature: ");
        sb.append(this.mTemperature);
        sb.append(" Temperature Unit: ");
        sb.append(this.mTempUnit);
        sb.append(" Humidity: ");
        sb.append(this.mHumidity);
        sb.append(" Wind speed: ");
        sb.append(this.mWindSpeed);
        sb.append(" Wind direction: ");
        sb.append(this.mWindDirection);
        sb.append(" Wind Speed Unit: ");
        sb.append(this.mWindSpeedUnit);
        sb.append(" Today's high temp: ");
        sb.append(this.mTodaysHighTemp);
        sb.append(" Today's low temp: ");
        sb.append(this.mTodaysLowTemp);
        sb.append(" Timestamp: ");
        sb.append(this.mTimestamp);
        sb.append(" Forecasts: [");
        Iterator<DayForecast> it = this.mForecastList.iterator();
        while (it.hasNext()) {
            sb.append(it.next().toString());
        }
        sb.append("]}");
        return sb.toString();
    }

    public int hashCode() {
        String str = this.mKey;
        return 31 + (str != null ? str.hashCode() : 0);
    }

    public boolean equals(Object obj) {
        if (obj != null && WeatherInfo.class == obj.getClass()) {
            return TextUtils.equals(this.mKey, ((WeatherInfo) obj).mKey);
        }
        return false;
    }
}
