package lineageos.hardware;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Range;
import java.util.BitSet;
import lineageos.os.Concierge;

/* loaded from: classes2.dex */
public class LiveDisplayConfig implements Parcelable {
    public static final Parcelable.Creator<LiveDisplayConfig> CREATOR = new Parcelable.Creator<LiveDisplayConfig>() { // from class: lineageos.hardware.LiveDisplayConfig.1
        @Override // android.os.Parcelable.Creator
        public LiveDisplayConfig createFromParcel(Parcel parcel) {
            return new LiveDisplayConfig(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public LiveDisplayConfig[] newArray(int i) {
            return new LiveDisplayConfig[i];
        }
    };
    private final BitSet mAllModes;
    private final BitSet mCapabilities;
    private final Range<Integer> mColorBalanceRange;
    private final Range<Integer> mColorTemperatureRange;
    private final Range<Float> mContrastRange;
    private final boolean mDefaultAutoContrast;
    private final boolean mDefaultAutoOutdoorMode;
    private final boolean mDefaultCABC;
    private final boolean mDefaultColorEnhancement;
    private final int mDefaultDayTemperature;
    private final int mDefaultMode;
    private final int mDefaultNightTemperature;
    private final Range<Float> mHueRange;
    private final Range<Float> mIntensityRange;
    private final Range<Float> mSaturationRange;
    private final Range<Float> mSaturationThresholdRange;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    private LiveDisplayConfig(Parcel parcel) {
        long j;
        int i;
        int i2;
        boolean z;
        boolean z2;
        boolean z3;
        boolean z4;
        int i3;
        int i4;
        int i5;
        int i6;
        BitSet bitSet = new BitSet();
        this.mAllModes = bitSet;
        Concierge.ParcelInfo parcelInfoReceiveParcel = Concierge.receiveParcel(parcel);
        float[] fArr = new float[10];
        int i7 = -1;
        if (parcelInfoReceiveParcel.getParcelVersion() >= 6) {
            j = parcel.readLong();
            i2 = parcel.readInt();
            i7 = parcel.readInt();
            i = parcel.readInt();
            z = parcel.readInt() == 1;
            z2 = parcel.readInt() == 1;
            z3 = parcel.readInt() == 1;
            z4 = parcel.readInt() == 1;
            i3 = parcel.readInt();
            i4 = parcel.readInt();
            i5 = parcel.readInt();
            i6 = parcel.readInt();
            parcel.readFloatArray(fArr);
        } else {
            j = 0;
            i = -1;
            i2 = 0;
            z = false;
            z2 = false;
            z3 = false;
            z4 = false;
            i3 = 0;
            i4 = 0;
            i5 = 0;
            i6 = 0;
        }
        this.mCapabilities = BitSet.valueOf(new long[]{j});
        bitSet.set(0, 4);
        this.mDefaultMode = i2;
        this.mDefaultDayTemperature = i7;
        this.mDefaultNightTemperature = i;
        this.mDefaultAutoContrast = z;
        this.mDefaultAutoOutdoorMode = z2;
        this.mDefaultCABC = z3;
        this.mDefaultColorEnhancement = z4;
        this.mColorTemperatureRange = Range.create(Integer.valueOf(i3), Integer.valueOf(i4));
        this.mColorBalanceRange = Range.create(Integer.valueOf(i5), Integer.valueOf(i6));
        this.mHueRange = Range.create(Float.valueOf(fArr[0]), Float.valueOf(fArr[1]));
        this.mSaturationRange = Range.create(Float.valueOf(fArr[2]), Float.valueOf(fArr[3]));
        this.mIntensityRange = Range.create(Float.valueOf(fArr[4]), Float.valueOf(fArr[5]));
        this.mContrastRange = Range.create(Float.valueOf(fArr[6]), Float.valueOf(fArr[7]));
        this.mSaturationThresholdRange = Range.create(Float.valueOf(fArr[8]), Float.valueOf(fArr[9]));
        parcelInfoReceiveParcel.complete();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("capabilities=");
        sb.append(this.mCapabilities.toString());
        sb.append(" defaultMode=");
        sb.append(this.mDefaultMode);
        sb.append(" defaultDayTemperature=");
        sb.append(this.mDefaultDayTemperature);
        sb.append(" defaultNightTemperature=");
        sb.append(this.mDefaultNightTemperature);
        sb.append(" defaultAutoOutdoorMode=");
        sb.append(this.mDefaultAutoOutdoorMode);
        sb.append(" defaultAutoContrast=");
        sb.append(this.mDefaultAutoContrast);
        sb.append(" defaultCABC=");
        sb.append(this.mDefaultCABC);
        sb.append(" defaultColorEnhancement=");
        sb.append(this.mDefaultColorEnhancement);
        sb.append(" colorTemperatureRange=");
        sb.append(this.mColorTemperatureRange);
        if (this.mCapabilities.get(16)) {
            sb.append(" colorBalanceRange=");
            sb.append(this.mColorBalanceRange);
        }
        if (this.mCapabilities.get(17)) {
            sb.append(" hueRange=");
            sb.append(this.mHueRange);
            sb.append(" saturationRange=");
            sb.append(this.mSaturationRange);
            sb.append(" intensityRange=");
            sb.append(this.mIntensityRange);
            sb.append(" contrastRange=");
            sb.append(this.mContrastRange);
            sb.append(" saturationThresholdRange=");
            sb.append(this.mSaturationThresholdRange);
        }
        return sb.toString();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        Concierge.ParcelInfo parcelInfoPrepareParcel = Concierge.prepareParcel(parcel);
        long[] longArray = this.mCapabilities.toLongArray();
        parcel.writeLong((longArray == null || longArray.length <= 0) ? 0L : longArray[0]);
        parcel.writeInt(this.mDefaultMode);
        parcel.writeInt(this.mDefaultDayTemperature);
        parcel.writeInt(this.mDefaultNightTemperature);
        parcel.writeInt(this.mDefaultAutoContrast ? 1 : 0);
        parcel.writeInt(this.mDefaultAutoOutdoorMode ? 1 : 0);
        parcel.writeInt(this.mDefaultCABC ? 1 : 0);
        parcel.writeInt(this.mDefaultColorEnhancement ? 1 : 0);
        parcel.writeInt(((Integer) this.mColorTemperatureRange.getLower()).intValue());
        parcel.writeInt(((Integer) this.mColorTemperatureRange.getUpper()).intValue());
        parcel.writeInt(((Integer) this.mColorBalanceRange.getLower()).intValue());
        parcel.writeInt(((Integer) this.mColorBalanceRange.getUpper()).intValue());
        parcel.writeFloatArray(new float[]{((Float) this.mHueRange.getLower()).floatValue(), ((Float) this.mHueRange.getUpper()).floatValue(), ((Float) this.mSaturationRange.getLower()).floatValue(), ((Float) this.mSaturationRange.getUpper()).floatValue(), ((Float) this.mIntensityRange.getLower()).floatValue(), ((Float) this.mIntensityRange.getUpper()).floatValue(), ((Float) this.mContrastRange.getLower()).floatValue(), ((Float) this.mContrastRange.getUpper()).floatValue(), ((Float) this.mSaturationThresholdRange.getLower()).floatValue(), ((Float) this.mSaturationThresholdRange.getUpper()).floatValue()});
        parcelInfoPrepareParcel.complete();
    }

    public boolean hasFeature(int i) {
        return ((i >= 0 && i <= 4) || (i >= 10 && i <= 19)) && (i == 0 || this.mCapabilities.get(i));
    }
}
