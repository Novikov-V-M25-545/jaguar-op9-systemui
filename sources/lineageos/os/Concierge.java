package lineageos.os;

import android.os.Parcel;

/* loaded from: classes2.dex */
public final class Concierge {
    public static ParcelInfo receiveParcel(Parcel parcel) {
        return new ParcelInfo(parcel);
    }

    public static ParcelInfo prepareParcel(Parcel parcel) {
        return new ParcelInfo(parcel, 9);
    }

    public static final class ParcelInfo {
        private boolean mCreation;
        private Parcel mParcel;
        private int mParcelableSize;
        private int mParcelableVersion;
        private int mSizePosition;
        private int mStartPosition;

        ParcelInfo(Parcel parcel) {
            this.mCreation = false;
            this.mCreation = false;
            this.mParcel = parcel;
            this.mParcelableVersion = parcel.readInt();
            this.mParcelableSize = parcel.readInt();
            this.mStartPosition = parcel.dataPosition();
        }

        ParcelInfo(Parcel parcel, int i) {
            this.mCreation = false;
            this.mCreation = true;
            this.mParcel = parcel;
            this.mParcelableVersion = i;
            parcel.writeInt(i);
            this.mSizePosition = parcel.dataPosition();
            this.mParcel.writeInt(0);
            this.mStartPosition = parcel.dataPosition();
        }

        public int getParcelVersion() {
            return this.mParcelableVersion;
        }

        public void complete() {
            if (this.mCreation) {
                this.mParcelableSize = this.mParcel.dataPosition() - this.mStartPosition;
                this.mParcel.setDataPosition(this.mSizePosition);
                this.mParcel.writeInt(this.mParcelableSize);
                this.mParcel.setDataPosition(this.mStartPosition + this.mParcelableSize);
                return;
            }
            this.mParcel.setDataPosition(this.mStartPosition + this.mParcelableSize);
        }
    }
}
