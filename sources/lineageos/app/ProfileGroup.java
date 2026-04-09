package lineageos.app;

import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import java.util.UUID;
import lineageos.os.Concierge;

/* loaded from: classes2.dex */
public final class ProfileGroup implements Parcelable {
    public static final Parcelable.Creator<ProfileGroup> CREATOR = new Parcelable.Creator<ProfileGroup>() { // from class: lineageos.app.ProfileGroup.1
        @Override // android.os.Parcelable.Creator
        public ProfileGroup createFromParcel(Parcel parcel) {
            return new ProfileGroup(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public ProfileGroup[] newArray(int i) {
            return new ProfileGroup[i];
        }
    };
    private boolean mDefaultGroup;
    private boolean mDirty;
    private Mode mLightsMode;
    private String mName;
    private Mode mRingerMode;
    private Uri mRingerOverride;
    private Mode mSoundMode;
    private Uri mSoundOverride;
    private UUID mUuid;
    private Mode mVibrateMode;

    public enum Mode {
        SUPPRESS,
        DEFAULT,
        OVERRIDE
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    private ProfileGroup(Parcel parcel) {
        this.mSoundOverride = RingtoneManager.getDefaultUri(2);
        this.mRingerOverride = RingtoneManager.getDefaultUri(1);
        Mode mode = Mode.DEFAULT;
        this.mSoundMode = mode;
        this.mRingerMode = mode;
        this.mVibrateMode = mode;
        this.mLightsMode = mode;
        this.mDefaultGroup = false;
        readFromParcel(parcel);
    }

    public UUID getUuid() {
        return this.mUuid;
    }

    public boolean isDefaultGroup() {
        return this.mDefaultGroup;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        Concierge.ParcelInfo parcelInfoPrepareParcel = Concierge.prepareParcel(parcel);
        parcel.writeString(this.mName);
        new ParcelUuid(this.mUuid).writeToParcel(parcel, 0);
        parcel.writeInt(this.mDefaultGroup ? 1 : 0);
        parcel.writeInt(this.mDirty ? 1 : 0);
        parcel.writeParcelable(this.mSoundOverride, i);
        parcel.writeParcelable(this.mRingerOverride, i);
        parcel.writeString(this.mSoundMode.name());
        parcel.writeString(this.mRingerMode.name());
        parcel.writeString(this.mVibrateMode.name());
        parcel.writeString(this.mLightsMode.name());
        parcelInfoPrepareParcel.complete();
    }

    public void readFromParcel(Parcel parcel) {
        Concierge.ParcelInfo parcelInfoReceiveParcel = Concierge.receiveParcel(parcel);
        if (parcelInfoReceiveParcel.getParcelVersion() >= 2) {
            this.mName = parcel.readString();
            this.mUuid = ((ParcelUuid) ParcelUuid.CREATOR.createFromParcel(parcel)).getUuid();
            this.mDefaultGroup = parcel.readInt() != 0;
            this.mDirty = parcel.readInt() != 0;
            this.mSoundOverride = (Uri) parcel.readParcelable(null);
            this.mRingerOverride = (Uri) parcel.readParcelable(null);
            this.mSoundMode = (Mode) Enum.valueOf(Mode.class, parcel.readString());
            this.mRingerMode = (Mode) Enum.valueOf(Mode.class, parcel.readString());
            this.mVibrateMode = (Mode) Enum.valueOf(Mode.class, parcel.readString());
            this.mLightsMode = (Mode) Enum.valueOf(Mode.class, parcel.readString());
        }
        parcelInfoReceiveParcel.complete();
    }
}
