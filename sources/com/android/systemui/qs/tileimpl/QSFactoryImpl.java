package com.android.systemui.qs.tileimpl;

import android.util.Log;
import android.view.ContextThemeWrapper;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSFactory;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.qs.tiles.AODTile;
import com.android.systemui.qs.tiles.AirplaneModeTile;
import com.android.systemui.qs.tiles.AmbientDisplayTile;
import com.android.systemui.qs.tiles.AntiFlickerTile;
import com.android.systemui.qs.tiles.BatterySaverTile;
import com.android.systemui.qs.tiles.BluetoothTile;
import com.android.systemui.qs.tiles.CPUInfoTile;
import com.android.systemui.qs.tiles.CaffeineTile;
import com.android.systemui.qs.tiles.CastTile;
import com.android.systemui.qs.tiles.CellularTile;
import com.android.systemui.qs.tiles.ColorInversionTile;
import com.android.systemui.qs.tiles.CompassTile;
import com.android.systemui.qs.tiles.DataSaverTile;
import com.android.systemui.qs.tiles.DataSwitchTile;
import com.android.systemui.qs.tiles.DndTile;
import com.android.systemui.qs.tiles.FPSInfoTile;
import com.android.systemui.qs.tiles.FlashlightTile;
import com.android.systemui.qs.tiles.GamingModeTile;
import com.android.systemui.qs.tiles.HeadsUpTile;
import com.android.systemui.qs.tiles.HotspotTile;
import com.android.systemui.qs.tiles.LiveDisplayTile;
import com.android.systemui.qs.tiles.LocaleTile;
import com.android.systemui.qs.tiles.LocationTile;
import com.android.systemui.qs.tiles.NfcTile;
import com.android.systemui.qs.tiles.NightDisplayTile;
import com.android.systemui.qs.tiles.OnTheGoTile;
import com.android.systemui.qs.tiles.PowerMenuTile;
import com.android.systemui.qs.tiles.PowerShareTile;
import com.android.systemui.qs.tiles.ProfilesTile;
import com.android.systemui.qs.tiles.ReadingModeTile;
import com.android.systemui.qs.tiles.RebootTile;
import com.android.systemui.qs.tiles.RotationLockTile;
import com.android.systemui.qs.tiles.ScreenRecordTile;
import com.android.systemui.qs.tiles.SensorPrivacyTile;
import com.android.systemui.qs.tiles.SleepModeTile;
import com.android.systemui.qs.tiles.SmartPixelsTile;
import com.android.systemui.qs.tiles.SoundTile;
import com.android.systemui.qs.tiles.SyncTile;
import com.android.systemui.qs.tiles.UiModeNightTile;
import com.android.systemui.qs.tiles.UsbTetherTile;
import com.android.systemui.qs.tiles.UserTile;
import com.android.systemui.qs.tiles.VolumeTile;
import com.android.systemui.qs.tiles.VpnTile;
import com.android.systemui.qs.tiles.WeatherTile;
import com.android.systemui.qs.tiles.WifiTile;
import com.android.systemui.qs.tiles.WorkModeTile;
import com.android.systemui.util.leak.GarbageMonitor;
import dagger.Lazy;
import javax.inject.Provider;

/* loaded from: classes.dex */
public class QSFactoryImpl implements QSFactory {
    private final Provider<AODTile> mAODTileProvider;
    private final Provider<AirplaneModeTile> mAirplaneModeTileProvider;
    private final Provider<AmbientDisplayTile> mAmbientDisplayTileProvider;
    private final Provider<AntiFlickerTile> mAntiFlickerTileProvider;
    private final Provider<BatterySaverTile> mBatterySaverTileProvider;
    private final Provider<BluetoothTile> mBluetoothTileProvider;
    private final Provider<CPUInfoTile> mCPUInfoTileProvider;
    private final Provider<CaffeineTile> mCaffeineTileProvider;
    private final Provider<CastTile> mCastTileProvider;
    private final Provider<CellularTile> mCellularTileProvider;
    private final Provider<ColorInversionTile> mColorInversionTileProvider;
    private final Provider<CompassTile> mCompassTileProvider;
    private final Provider<DataSaverTile> mDataSaverTileProvider;
    private final Provider<DataSwitchTile> mDataSwitchTileProvider;
    private final Provider<DndTile> mDndTileProvider;
    private final Provider<FPSInfoTile> mFPSInfoTileProvider;
    private final Provider<FlashlightTile> mFlashlightTileProvider;
    private final Provider<GamingModeTile> mGamingModeTileProvider;
    private final Provider<HeadsUpTile> mHeadsUpTileProvider;
    private final Provider<HotspotTile> mHotspotTileProvider;
    private final Provider<LiveDisplayTile> mLiveDisplayTileProvider;
    private final Provider<LocaleTile> mLocaleTileProvider;
    private final Provider<LocationTile> mLocationTileProvider;
    private final Provider<GarbageMonitor.MemoryTile> mMemoryTileProvider;
    private final Provider<NfcTile> mNfcTileProvider;
    private final Provider<NightDisplayTile> mNightDisplayTileProvider;
    private final Provider<OnTheGoTile> mOnTheGoTileProvider;
    private final Provider<PowerMenuTile> mPowerMenuTileProvider;
    private final Provider<PowerShareTile> mPowerShareTileProvider;
    private final Provider<ProfilesTile> mProfilesTileProvider;
    private final Lazy<QSHost> mQsHostLazy;
    private final Provider<ReadingModeTile> mReadingModeTileProvider;
    private final Provider<RebootTile> mRebootTileProvider;
    private final Provider<RotationLockTile> mRotationLockTileProvider;
    private final Provider<ScreenRecordTile> mScreenRecordTileProvider;
    private final Provider<SensorPrivacyTile> mSensorPrivacyTileProvider;
    private final Provider<SleepModeTile> mSleepModeTileProvider;
    private final Provider<SmartPixelsTile> mSmartPixelsTileProvider;
    private final Provider<SoundTile> mSoundTileProvider;
    private final Provider<SyncTile> mSyncTileProvider;
    private final Provider<UiModeNightTile> mUiModeNightTileProvider;
    private final Provider<UsbTetherTile> mUsbTetherTileProvider;
    private final Provider<UserTile> mUserTileProvider;
    private final Provider<VolumeTile> mVolumeTileProvider;
    private final Provider<VpnTile> mVpnTileProvider;
    private final Provider<WeatherTile> mWeatherTileProvider;
    private final Provider<WifiTile> mWifiTileProvider;
    private final Provider<WorkModeTile> mWorkModeTileProvider;

    public QSFactoryImpl(Lazy<QSHost> lazy, Provider<WifiTile> provider, Provider<BluetoothTile> provider2, Provider<CellularTile> provider3, Provider<DndTile> provider4, Provider<ColorInversionTile> provider5, Provider<AirplaneModeTile> provider6, Provider<WorkModeTile> provider7, Provider<RotationLockTile> provider8, Provider<FlashlightTile> provider9, Provider<LocationTile> provider10, Provider<CastTile> provider11, Provider<HotspotTile> provider12, Provider<UserTile> provider13, Provider<BatterySaverTile> provider14, Provider<DataSaverTile> provider15, Provider<NightDisplayTile> provider16, Provider<NfcTile> provider17, Provider<SensorPrivacyTile> provider18, Provider<GarbageMonitor.MemoryTile> provider19, Provider<UiModeNightTile> provider20, Provider<ScreenRecordTile> provider21, Provider<AmbientDisplayTile> provider22, Provider<AODTile> provider23, Provider<CaffeineTile> provider24, Provider<PowerMenuTile> provider25, Provider<DataSwitchTile> provider26, Provider<HeadsUpTile> provider27, Provider<LiveDisplayTile> provider28, Provider<PowerShareTile> provider29, Provider<ProfilesTile> provider30, Provider<ReadingModeTile> provider31, Provider<SyncTile> provider32, Provider<UsbTetherTile> provider33, Provider<VolumeTile> provider34, Provider<VpnTile> provider35, Provider<WeatherTile> provider36, Provider<CPUInfoTile> provider37, Provider<FPSInfoTile> provider38, Provider<GamingModeTile> provider39, Provider<SmartPixelsTile> provider40, Provider<RebootTile> provider41, Provider<SoundTile> provider42, Provider<CompassTile> provider43, Provider<AntiFlickerTile> provider44, Provider<LocaleTile> provider45, Provider<SleepModeTile> provider46, Provider<OnTheGoTile> provider47) {
        this.mQsHostLazy = lazy;
        this.mWifiTileProvider = provider;
        this.mBluetoothTileProvider = provider2;
        this.mCellularTileProvider = provider3;
        this.mDndTileProvider = provider4;
        this.mColorInversionTileProvider = provider5;
        this.mAirplaneModeTileProvider = provider6;
        this.mWorkModeTileProvider = provider7;
        this.mRotationLockTileProvider = provider8;
        this.mFlashlightTileProvider = provider9;
        this.mLocationTileProvider = provider10;
        this.mCastTileProvider = provider11;
        this.mHotspotTileProvider = provider12;
        this.mUserTileProvider = provider13;
        this.mBatterySaverTileProvider = provider14;
        this.mDataSaverTileProvider = provider15;
        this.mNightDisplayTileProvider = provider16;
        this.mNfcTileProvider = provider17;
        this.mSensorPrivacyTileProvider = provider18;
        this.mMemoryTileProvider = provider19;
        this.mUiModeNightTileProvider = provider20;
        this.mScreenRecordTileProvider = provider21;
        this.mAmbientDisplayTileProvider = provider22;
        this.mAODTileProvider = provider23;
        this.mCaffeineTileProvider = provider24;
        this.mPowerMenuTileProvider = provider25;
        this.mDataSwitchTileProvider = provider26;
        this.mHeadsUpTileProvider = provider27;
        this.mLiveDisplayTileProvider = provider28;
        this.mPowerShareTileProvider = provider29;
        this.mProfilesTileProvider = provider30;
        this.mReadingModeTileProvider = provider31;
        this.mSyncTileProvider = provider32;
        this.mUsbTetherTileProvider = provider33;
        this.mVolumeTileProvider = provider34;
        this.mVpnTileProvider = provider35;
        this.mWeatherTileProvider = provider36;
        this.mCPUInfoTileProvider = provider37;
        this.mFPSInfoTileProvider = provider38;
        this.mGamingModeTileProvider = provider39;
        this.mSmartPixelsTileProvider = provider40;
        this.mRebootTileProvider = provider41;
        this.mSoundTileProvider = provider42;
        this.mCompassTileProvider = provider43;
        this.mLocaleTileProvider = provider45;
        this.mAntiFlickerTileProvider = provider44;
        this.mOnTheGoTileProvider = provider47;
        this.mSleepModeTileProvider = provider46;
    }

    @Override // com.android.systemui.plugins.qs.QSFactory
    public QSTile createTile(String str) {
        QSTileImpl qSTileImplCreateTileInternal = createTileInternal(str);
        if (qSTileImplCreateTileInternal != null) {
            qSTileImplCreateTileInternal.handleStale();
        }
        return qSTileImplCreateTileInternal;
    }

    private QSTileImpl createTileInternal(String str) {
        str.hashCode();
        switch (str) {
            case "ambient_display":
                return this.mAmbientDisplayTileProvider.get();
            case "dataswitch":
                return this.mDataSwitchTileProvider.get();
            case "inversion":
                return this.mColorInversionTileProvider.get();
            case "volume_panel":
                return this.mVolumeTileProvider.get();
            case "reading_mode":
                return this.mReadingModeTileProvider.get();
            case "onthego":
                return this.mOnTheGoTileProvider.get();
            case "gaming":
                return this.mGamingModeTileProvider.get();
            case "flashlight":
                return this.mFlashlightTileProvider.get();
            case "livedisplay":
                return this.mLiveDisplayTileProvider.get();
            case "heads_up":
                return this.mHeadsUpTileProvider.get();
            case "locale":
                return this.mLocaleTileProvider.get();
            case "profiles":
                return this.mProfilesTileProvider.get();
            case "anti_flicker":
                return this.mAntiFlickerTileProvider.get();
            case "reboot":
                return this.mRebootTileProvider.get();
            case "screenrecord":
                return this.mScreenRecordTileProvider.get();
            case "airplane":
                return this.mAirplaneModeTileProvider.get();
            case "fpsinfo":
                return this.mFPSInfoTileProvider.get();
            case "sleep_mode":
                return this.mSleepModeTileProvider.get();
            case "caffeine":
                return this.mCaffeineTileProvider.get();
            case "battery":
                return this.mBatterySaverTileProvider.get();
            case "rotation":
                return this.mRotationLockTileProvider.get();
            case "bt":
                return this.mBluetoothTileProvider.get();
            case "aod":
                return this.mAODTileProvider.get();
            case "dnd":
                return this.mDndTileProvider.get();
            case "nfc":
                return this.mNfcTileProvider.get();
            case "vpn":
                return this.mVpnTileProvider.get();
            case "cast":
                return this.mCastTileProvider.get();
            case "cell":
                return this.mCellularTileProvider.get();
            case "dark":
                return this.mUiModeNightTileProvider.get();
            case "sync":
                return this.mSyncTileProvider.get();
            case "user":
                return this.mUserTileProvider.get();
            case "wifi":
                return this.mWifiTileProvider.get();
            case "work":
                return this.mWorkModeTileProvider.get();
            case "night":
                return this.mNightDisplayTileProvider.get();
            case "saver":
                return this.mDataSaverTileProvider.get();
            case "sound":
                return this.mSoundTileProvider.get();
            case "smartpixels":
                return this.mSmartPixelsTileProvider.get();
            case "powershare":
                return this.mPowerShareTileProvider.get();
            case "powermenu":
                return this.mPowerMenuTileProvider.get();
            case "compass":
                return this.mCompassTileProvider.get();
            case "cpuinfo":
                return this.mCPUInfoTileProvider.get();
            case "hotspot":
                return this.mHotspotTileProvider.get();
            case "weather":
                return this.mWeatherTileProvider.get();
            case "sensorprivacy":
                return this.mSensorPrivacyTileProvider.get();
            case "location":
                return this.mLocationTileProvider.get();
            case "usb_tether":
                return this.mUsbTetherTileProvider.get();
            default:
                if (str.startsWith("custom(")) {
                    return CustomTile.create(this.mQsHostLazy.get(), str, this.mQsHostLazy.get().getUserContext());
                }
                Log.w("QSFactory", "No stock tile spec: " + str);
                return null;
        }
    }

    @Override // com.android.systemui.plugins.qs.QSFactory
    public com.android.systemui.plugins.qs.QSTileView createTileView(QSTile qSTile, boolean z) {
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(this.mQsHostLazy.get().getContext(), R.style.qs_theme);
        QSIconView qSIconViewCreateTileView = qSTile.createTileView(contextThemeWrapper);
        if (z) {
            return new QSTileBaseView(contextThemeWrapper, qSIconViewCreateTileView, z);
        }
        return new QSTileView(contextThemeWrapper, qSIconViewCreateTileView);
    }
}
