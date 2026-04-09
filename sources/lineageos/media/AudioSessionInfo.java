package lineageos.media;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;
import lineageos.os.Concierge;

/* loaded from: classes2.dex */
public final class AudioSessionInfo implements Parcelable {
    public static final Parcelable.Creator<AudioSessionInfo> CREATOR = new Parcelable.Creator<AudioSessionInfo>() { // from class: lineageos.media.AudioSessionInfo.1
        @Override // android.os.Parcelable.Creator
        public AudioSessionInfo createFromParcel(Parcel parcel) {
            return new AudioSessionInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public AudioSessionInfo[] newArray(int i) {
            return new AudioSessionInfo[i];
        }
    };
    private final int mChannelMask;
    private final int mFlags;
    private final int mSessionId;
    private final int mStream;
    private final int mUid;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    private AudioSessionInfo(Parcel parcel) {
        Concierge.ParcelInfo parcelInfoReceiveParcel = Concierge.receiveParcel(parcel);
        parcelInfoReceiveParcel.getParcelVersion();
        this.mSessionId = parcel.readInt();
        this.mStream = parcel.readInt();
        this.mFlags = parcel.readInt();
        this.mChannelMask = parcel.readInt();
        this.mUid = parcel.readInt();
        parcelInfoReceiveParcel.complete();
    }

    public String toString() {
        return String.format("audioSessionInfo[sessionId=%d, stream=%d, flags=%d, channelMask=%d, uid=%d", Integer.valueOf(this.mSessionId), Integer.valueOf(this.mStream), Integer.valueOf(this.mFlags), Integer.valueOf(this.mChannelMask), Integer.valueOf(this.mUid));
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mSessionId), Integer.valueOf(this.mStream), Integer.valueOf(this.mFlags), Integer.valueOf(this.mChannelMask), Integer.valueOf(this.mUid));
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof AudioSessionInfo)) {
            return false;
        }
        AudioSessionInfo audioSessionInfo = (AudioSessionInfo) obj;
        return this == audioSessionInfo || (this.mSessionId == audioSessionInfo.mSessionId && this.mStream == audioSessionInfo.mStream && this.mFlags == audioSessionInfo.mFlags && this.mChannelMask == audioSessionInfo.mChannelMask && this.mUid == audioSessionInfo.mUid);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        Concierge.ParcelInfo parcelInfoPrepareParcel = Concierge.prepareParcel(parcel);
        parcel.writeInt(this.mSessionId);
        parcel.writeInt(this.mStream);
        parcel.writeInt(this.mFlags);
        parcel.writeInt(this.mChannelMask);
        parcel.writeInt(this.mUid);
        parcelInfoPrepareParcel.complete();
    }
}
