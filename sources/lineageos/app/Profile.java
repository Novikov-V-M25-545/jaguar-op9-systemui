package lineageos.app;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import lineageos.os.Concierge;
import lineageos.profiles.AirplaneModeSettings;
import lineageos.profiles.BrightnessSettings;
import lineageos.profiles.ConnectionSettings;
import lineageos.profiles.LockSettings;
import lineageos.profiles.RingModeSettings;
import lineageos.profiles.StreamSettings;

/* loaded from: classes2.dex */
public final class Profile implements Parcelable, Comparable {
    public static final Parcelable.Creator<Profile> CREATOR = new Parcelable.Creator<Profile>() { // from class: lineageos.app.Profile.1
        @Override // android.os.Parcelable.Creator
        public Profile createFromParcel(Parcel parcel) {
            return new Profile(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public Profile[] newArray(int i) {
            return new Profile[i];
        }
    };
    private Map<Integer, ConnectionSettings> connections;
    private AirplaneModeSettings mAirplaneMode;
    private BrightnessSettings mBrightness;
    private ProfileGroup mDefaultGroup;
    private boolean mDirty;
    private int mDozeMode;
    private String mName;
    private int mNameResId;
    private int mNotificationLightMode;
    private int mProfileType;
    private RingModeSettings mRingMode;
    private LockSettings mScreenLockMode;
    private ArrayList<UUID> mSecondaryUuids;
    private boolean mStatusBarIndicator;
    private Map<String, ProfileTrigger> mTriggers;
    private UUID mUuid;
    private Map<Integer, ConnectionSettings> networkConnectionSubIds;
    private Map<UUID, ProfileGroup> profileGroups;
    private Map<Integer, StreamSettings> streams;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public static class ProfileTrigger implements Parcelable {
        public static final Parcelable.Creator<ProfileTrigger> CREATOR = new Parcelable.Creator<ProfileTrigger>() { // from class: lineageos.app.Profile.ProfileTrigger.1
            @Override // android.os.Parcelable.Creator
            public ProfileTrigger createFromParcel(Parcel parcel) {
                return new ProfileTrigger(parcel);
            }

            @Override // android.os.Parcelable.Creator
            public ProfileTrigger[] newArray(int i) {
                return new ProfileTrigger[i];
            }
        };
        private String mId;
        private String mName;
        private int mState;
        private int mType;

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        private ProfileTrigger(Parcel parcel) {
            Concierge.ParcelInfo parcelInfoReceiveParcel = Concierge.receiveParcel(parcel);
            if (parcelInfoReceiveParcel.getParcelVersion() >= 2) {
                this.mType = parcel.readInt();
                this.mId = parcel.readString();
                this.mState = parcel.readInt();
                this.mName = parcel.readString();
            }
            parcelInfoReceiveParcel.complete();
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            Concierge.ParcelInfo parcelInfoPrepareParcel = Concierge.prepareParcel(parcel);
            parcel.writeInt(this.mType);
            parcel.writeString(this.mId);
            parcel.writeInt(this.mState);
            parcel.writeString(this.mName);
            parcelInfoPrepareParcel.complete();
        }
    }

    private Profile(Parcel parcel) {
        this.mSecondaryUuids = new ArrayList<>();
        this.profileGroups = new HashMap();
        this.mStatusBarIndicator = false;
        this.streams = new HashMap();
        this.mTriggers = new HashMap();
        this.connections = new HashMap();
        this.networkConnectionSubIds = new HashMap();
        this.mRingMode = new RingModeSettings();
        this.mAirplaneMode = new AirplaneModeSettings();
        this.mBrightness = new BrightnessSettings();
        this.mScreenLockMode = new LockSettings();
        this.mDozeMode = 0;
        this.mNotificationLightMode = 0;
        readFromParcel(parcel);
    }

    @Override // java.lang.Comparable
    public int compareTo(Object obj) {
        Profile profile = (Profile) obj;
        if (this.mName.compareTo(profile.mName) < 0) {
            return -1;
        }
        return this.mName.compareTo(profile.mName) > 0 ? 1 : 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        Concierge.ParcelInfo parcelInfoPrepareParcel = Concierge.prepareParcel(parcel);
        if (!TextUtils.isEmpty(this.mName)) {
            parcel.writeInt(1);
            parcel.writeString(this.mName);
        } else {
            parcel.writeInt(0);
        }
        if (this.mNameResId != 0) {
            parcel.writeInt(1);
            parcel.writeInt(this.mNameResId);
        } else {
            parcel.writeInt(0);
        }
        if (this.mUuid != null) {
            parcel.writeInt(1);
            new ParcelUuid(this.mUuid).writeToParcel(parcel, 0);
        } else {
            parcel.writeInt(0);
        }
        ArrayList<UUID> arrayList = this.mSecondaryUuids;
        if (arrayList != null && !arrayList.isEmpty()) {
            ArrayList arrayList2 = new ArrayList(this.mSecondaryUuids.size());
            Iterator<UUID> it = this.mSecondaryUuids.iterator();
            while (it.hasNext()) {
                arrayList2.add(new ParcelUuid(it.next()));
            }
            parcel.writeInt(1);
            parcel.writeParcelableArray((Parcelable[]) arrayList2.toArray(new Parcelable[0]), i);
        } else {
            parcel.writeInt(0);
        }
        parcel.writeInt(this.mStatusBarIndicator ? 1 : 0);
        parcel.writeInt(this.mProfileType);
        parcel.writeInt(this.mDirty ? 1 : 0);
        Map<UUID, ProfileGroup> map = this.profileGroups;
        if (map != null && !map.isEmpty()) {
            parcel.writeInt(1);
            parcel.writeTypedArray((ProfileGroup[]) this.profileGroups.values().toArray(new ProfileGroup[0]), i);
        } else {
            parcel.writeInt(0);
        }
        Map<Integer, StreamSettings> map2 = this.streams;
        if (map2 != null && !map2.isEmpty()) {
            parcel.writeInt(1);
            parcel.writeTypedArray((StreamSettings[]) this.streams.values().toArray(new StreamSettings[0]), i);
        } else {
            parcel.writeInt(0);
        }
        Map<Integer, ConnectionSettings> map3 = this.connections;
        if (map3 != null && !map3.isEmpty()) {
            parcel.writeInt(1);
            parcel.writeTypedArray((ConnectionSettings[]) this.connections.values().toArray(new ConnectionSettings[0]), i);
        } else {
            parcel.writeInt(0);
        }
        if (this.mRingMode != null) {
            parcel.writeInt(1);
            this.mRingMode.writeToParcel(parcel, 0);
        } else {
            parcel.writeInt(0);
        }
        if (this.mAirplaneMode != null) {
            parcel.writeInt(1);
            this.mAirplaneMode.writeToParcel(parcel, 0);
        } else {
            parcel.writeInt(0);
        }
        if (this.mBrightness != null) {
            parcel.writeInt(1);
            this.mBrightness.writeToParcel(parcel, 0);
        } else {
            parcel.writeInt(0);
        }
        if (this.mScreenLockMode != null) {
            parcel.writeInt(1);
            this.mScreenLockMode.writeToParcel(parcel, 0);
        } else {
            parcel.writeInt(0);
        }
        parcel.writeTypedArray((ProfileTrigger[]) this.mTriggers.values().toArray(new ProfileTrigger[0]), i);
        parcel.writeInt(this.mDozeMode);
        parcel.writeInt(this.mNotificationLightMode);
        Map<Integer, ConnectionSettings> map4 = this.networkConnectionSubIds;
        if (map4 != null && !map4.isEmpty()) {
            parcel.writeInt(1);
            parcel.writeTypedArray((ConnectionSettings[]) this.networkConnectionSubIds.values().toArray(new ConnectionSettings[0]), i);
        } else {
            parcel.writeInt(0);
        }
        parcelInfoPrepareParcel.complete();
    }

    public void readFromParcel(Parcel parcel) {
        Concierge.ParcelInfo parcelInfoReceiveParcel = Concierge.receiveParcel(parcel);
        int parcelVersion = parcelInfoReceiveParcel.getParcelVersion();
        if (parcelVersion >= 2) {
            if (parcel.readInt() != 0) {
                this.mName = parcel.readString();
            }
            if (parcel.readInt() != 0) {
                this.mNameResId = parcel.readInt();
            }
            if (parcel.readInt() != 0) {
                this.mUuid = ((ParcelUuid) ParcelUuid.CREATOR.createFromParcel(parcel)).getUuid();
            }
            if (parcel.readInt() != 0) {
                for (Parcelable parcelable : parcel.readParcelableArray(null)) {
                    this.mSecondaryUuids.add(((ParcelUuid) parcelable).getUuid());
                }
            }
            this.mStatusBarIndicator = parcel.readInt() == 1;
            this.mProfileType = parcel.readInt();
            this.mDirty = parcel.readInt() == 1;
            if (parcel.readInt() != 0) {
                for (ProfileGroup profileGroup : (ProfileGroup[]) parcel.createTypedArray(ProfileGroup.CREATOR)) {
                    this.profileGroups.put(profileGroup.getUuid(), profileGroup);
                    if (profileGroup.isDefaultGroup()) {
                        this.mDefaultGroup = profileGroup;
                    }
                }
            }
            if (parcel.readInt() != 0) {
                for (StreamSettings streamSettings : (StreamSettings[]) parcel.createTypedArray(StreamSettings.CREATOR)) {
                    this.streams.put(Integer.valueOf(streamSettings.getStreamId()), streamSettings);
                }
            }
            if (parcel.readInt() != 0) {
                for (ConnectionSettings connectionSettings : (ConnectionSettings[]) parcel.createTypedArray(ConnectionSettings.CREATOR)) {
                    this.connections.put(Integer.valueOf(connectionSettings.getConnectionId()), connectionSettings);
                }
            }
            if (parcel.readInt() != 0) {
                this.mRingMode = RingModeSettings.CREATOR.createFromParcel(parcel);
            }
            if (parcel.readInt() != 0) {
                this.mAirplaneMode = AirplaneModeSettings.CREATOR.createFromParcel(parcel);
            }
            if (parcel.readInt() != 0) {
                this.mBrightness = BrightnessSettings.CREATOR.createFromParcel(parcel);
            }
            if (parcel.readInt() != 0) {
                this.mScreenLockMode = LockSettings.CREATOR.createFromParcel(parcel);
            }
            for (ProfileTrigger profileTrigger : (ProfileTrigger[]) parcel.createTypedArray(ProfileTrigger.CREATOR)) {
                this.mTriggers.put(profileTrigger.mId, profileTrigger);
            }
            this.mDozeMode = parcel.readInt();
        }
        if (parcelVersion >= 5) {
            this.mNotificationLightMode = parcel.readInt();
            if (parcel.readInt() != 0) {
                for (ConnectionSettings connectionSettings2 : (ConnectionSettings[]) parcel.createTypedArray(ConnectionSettings.CREATOR)) {
                    this.networkConnectionSubIds.put(Integer.valueOf(connectionSettings2.getSubId()), connectionSettings2);
                }
            }
        }
        parcelInfoReceiveParcel.complete();
    }

    public String getName() {
        return this.mName;
    }

    public UUID getUuid() {
        if (this.mUuid == null) {
            this.mUuid = UUID.randomUUID();
        }
        return this.mUuid;
    }

    public LockSettings getScreenLockMode() {
        return this.mScreenLockMode;
    }
}
