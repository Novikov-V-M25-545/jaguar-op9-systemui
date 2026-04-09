package lineageos.hardware;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Locale;

/* loaded from: classes2.dex */
public class HSIC implements Parcelable {
    public static final Parcelable.Creator<HSIC> CREATOR = new Parcelable.Creator<HSIC>() { // from class: lineageos.hardware.HSIC.1
        @Override // android.os.Parcelable.Creator
        public HSIC createFromParcel(Parcel parcel) {
            float[] fArr = new float[5];
            parcel.readFloatArray(fArr);
            return HSIC.fromFloatArray(fArr);
        }

        @Override // android.os.Parcelable.Creator
        public HSIC[] newArray(int i) {
            return new HSIC[i];
        }
    };
    private final float mContrast;
    private final float mHue;
    private final float mIntensity;
    private final float mSaturation;
    private final float mSaturationThreshold;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public HSIC(float f, float f2, float f3, float f4, float f5) {
        this.mHue = f;
        this.mSaturation = f2;
        this.mIntensity = f3;
        this.mContrast = f4;
        this.mSaturationThreshold = f5;
    }

    public float[] toFloatArray() {
        return new float[]{this.mHue, this.mSaturation, this.mIntensity, this.mContrast, this.mSaturationThreshold};
    }

    public static HSIC fromFloatArray(float[] fArr) {
        if (fArr.length == 5) {
            return new HSIC(fArr[0], fArr[1], fArr[2], fArr[3], fArr[4]);
        }
        if (fArr.length == 4) {
            return new HSIC(fArr[0], fArr[1], fArr[2], fArr[3], 0.0f);
        }
        return null;
    }

    public String toString() {
        return String.format(Locale.US, "HSIC={ hue=%f saturation=%f intensity=%f contrast=%f saturationThreshold=%f }", Float.valueOf(this.mHue), Float.valueOf(this.mSaturation), Float.valueOf(this.mIntensity), Float.valueOf(this.mContrast), Float.valueOf(this.mSaturationThreshold));
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeFloatArray(toFloatArray());
    }
}
