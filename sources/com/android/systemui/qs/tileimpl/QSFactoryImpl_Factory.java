package com.android.systemui.qs.tileimpl;

import com.android.systemui.qs.QSHost;
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
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class QSFactoryImpl_Factory implements Factory<QSFactoryImpl> {
    private final Provider<AirplaneModeTile> airplaneModeTileProvider;
    private final Provider<AmbientDisplayTile> ambientDisplayTileProvider;
    private final Provider<AntiFlickerTile> antiFlickerTileProvider;
    private final Provider<AODTile> aodTileProvider;
    private final Provider<BatterySaverTile> batterySaverTileProvider;
    private final Provider<BluetoothTile> bluetoothTileProvider;
    private final Provider<CaffeineTile> caffeineTileProvider;
    private final Provider<CastTile> castTileProvider;
    private final Provider<CellularTile> cellularTileProvider;
    private final Provider<ColorInversionTile> colorInversionTileProvider;
    private final Provider<CompassTile> compassTileProvider;
    private final Provider<CPUInfoTile> cpuInfoTileProvider;
    private final Provider<DataSaverTile> dataSaverTileProvider;
    private final Provider<DataSwitchTile> dataSwitchTileProvider;
    private final Provider<DndTile> dndTileProvider;
    private final Provider<FlashlightTile> flashlightTileProvider;
    private final Provider<FPSInfoTile> fpsInfoTileProvider;
    private final Provider<GamingModeTile> gamingModeTileProvider;
    private final Provider<HeadsUpTile> headsUpTileProvider;
    private final Provider<HotspotTile> hotspotTileProvider;
    private final Provider<LiveDisplayTile> liveDisplayTileProvider;
    private final Provider<LocaleTile> localeTileProvider;
    private final Provider<LocationTile> locationTileProvider;
    private final Provider<GarbageMonitor.MemoryTile> memoryTileProvider;
    private final Provider<NfcTile> nfcTileProvider;
    private final Provider<NightDisplayTile> nightDisplayTileProvider;
    private final Provider<OnTheGoTile> onTheGoTileProvider;
    private final Provider<PowerMenuTile> powerMenuTileProvider;
    private final Provider<PowerShareTile> powerShareTileProvider;
    private final Provider<ProfilesTile> profilesTileProvider;
    private final Provider<QSHost> qsHostLazyProvider;
    private final Provider<ReadingModeTile> readingModeTileProvider;
    private final Provider<RebootTile> rebootTileProvider;
    private final Provider<RotationLockTile> rotationLockTileProvider;
    private final Provider<ScreenRecordTile> screenRecordTileProvider;
    private final Provider<SensorPrivacyTile> sensorPrivacyTileProvider;
    private final Provider<SleepModeTile> sleepModeTileProvider;
    private final Provider<SmartPixelsTile> smartPixelsTileProvider;
    private final Provider<SoundTile> soundTileProvider;
    private final Provider<SyncTile> syncTileProvider;
    private final Provider<UiModeNightTile> uiModeNightTileProvider;
    private final Provider<UsbTetherTile> usbTetherTileProvider;
    private final Provider<UserTile> userTileProvider;
    private final Provider<VolumeTile> volumeTileProvider;
    private final Provider<VpnTile> vpnTileProvider;
    private final Provider<WeatherTile> weatherTileProvider;
    private final Provider<WifiTile> wifiTileProvider;
    private final Provider<WorkModeTile> workModeTileProvider;

    public QSFactoryImpl_Factory(Provider<QSHost> provider, Provider<WifiTile> provider2, Provider<BluetoothTile> provider3, Provider<CellularTile> provider4, Provider<DndTile> provider5, Provider<ColorInversionTile> provider6, Provider<AirplaneModeTile> provider7, Provider<WorkModeTile> provider8, Provider<RotationLockTile> provider9, Provider<FlashlightTile> provider10, Provider<LocationTile> provider11, Provider<CastTile> provider12, Provider<HotspotTile> provider13, Provider<UserTile> provider14, Provider<BatterySaverTile> provider15, Provider<DataSaverTile> provider16, Provider<NightDisplayTile> provider17, Provider<NfcTile> provider18, Provider<SensorPrivacyTile> provider19, Provider<GarbageMonitor.MemoryTile> provider20, Provider<UiModeNightTile> provider21, Provider<ScreenRecordTile> provider22, Provider<AmbientDisplayTile> provider23, Provider<AODTile> provider24, Provider<CaffeineTile> provider25, Provider<PowerMenuTile> provider26, Provider<DataSwitchTile> provider27, Provider<HeadsUpTile> provider28, Provider<LiveDisplayTile> provider29, Provider<PowerShareTile> provider30, Provider<ProfilesTile> provider31, Provider<ReadingModeTile> provider32, Provider<SyncTile> provider33, Provider<UsbTetherTile> provider34, Provider<VolumeTile> provider35, Provider<VpnTile> provider36, Provider<WeatherTile> provider37, Provider<CPUInfoTile> provider38, Provider<FPSInfoTile> provider39, Provider<GamingModeTile> provider40, Provider<SmartPixelsTile> provider41, Provider<RebootTile> provider42, Provider<SoundTile> provider43, Provider<CompassTile> provider44, Provider<AntiFlickerTile> provider45, Provider<LocaleTile> provider46, Provider<SleepModeTile> provider47, Provider<OnTheGoTile> provider48) {
        this.qsHostLazyProvider = provider;
        this.wifiTileProvider = provider2;
        this.bluetoothTileProvider = provider3;
        this.cellularTileProvider = provider4;
        this.dndTileProvider = provider5;
        this.colorInversionTileProvider = provider6;
        this.airplaneModeTileProvider = provider7;
        this.workModeTileProvider = provider8;
        this.rotationLockTileProvider = provider9;
        this.flashlightTileProvider = provider10;
        this.locationTileProvider = provider11;
        this.castTileProvider = provider12;
        this.hotspotTileProvider = provider13;
        this.userTileProvider = provider14;
        this.batterySaverTileProvider = provider15;
        this.dataSaverTileProvider = provider16;
        this.nightDisplayTileProvider = provider17;
        this.nfcTileProvider = provider18;
        this.sensorPrivacyTileProvider = provider19;
        this.memoryTileProvider = provider20;
        this.uiModeNightTileProvider = provider21;
        this.screenRecordTileProvider = provider22;
        this.ambientDisplayTileProvider = provider23;
        this.aodTileProvider = provider24;
        this.caffeineTileProvider = provider25;
        this.powerMenuTileProvider = provider26;
        this.dataSwitchTileProvider = provider27;
        this.headsUpTileProvider = provider28;
        this.liveDisplayTileProvider = provider29;
        this.powerShareTileProvider = provider30;
        this.profilesTileProvider = provider31;
        this.readingModeTileProvider = provider32;
        this.syncTileProvider = provider33;
        this.usbTetherTileProvider = provider34;
        this.volumeTileProvider = provider35;
        this.vpnTileProvider = provider36;
        this.weatherTileProvider = provider37;
        this.cpuInfoTileProvider = provider38;
        this.fpsInfoTileProvider = provider39;
        this.gamingModeTileProvider = provider40;
        this.smartPixelsTileProvider = provider41;
        this.rebootTileProvider = provider42;
        this.soundTileProvider = provider43;
        this.compassTileProvider = provider44;
        this.antiFlickerTileProvider = provider45;
        this.localeTileProvider = provider46;
        this.sleepModeTileProvider = provider47;
        this.onTheGoTileProvider = provider48;
    }

    @Override // javax.inject.Provider
    public QSFactoryImpl get() {
        return provideInstance(this.qsHostLazyProvider, this.wifiTileProvider, this.bluetoothTileProvider, this.cellularTileProvider, this.dndTileProvider, this.colorInversionTileProvider, this.airplaneModeTileProvider, this.workModeTileProvider, this.rotationLockTileProvider, this.flashlightTileProvider, this.locationTileProvider, this.castTileProvider, this.hotspotTileProvider, this.userTileProvider, this.batterySaverTileProvider, this.dataSaverTileProvider, this.nightDisplayTileProvider, this.nfcTileProvider, this.sensorPrivacyTileProvider, this.memoryTileProvider, this.uiModeNightTileProvider, this.screenRecordTileProvider, this.ambientDisplayTileProvider, this.aodTileProvider, this.caffeineTileProvider, this.powerMenuTileProvider, this.dataSwitchTileProvider, this.headsUpTileProvider, this.liveDisplayTileProvider, this.powerShareTileProvider, this.profilesTileProvider, this.readingModeTileProvider, this.syncTileProvider, this.usbTetherTileProvider, this.volumeTileProvider, this.vpnTileProvider, this.weatherTileProvider, this.cpuInfoTileProvider, this.fpsInfoTileProvider, this.gamingModeTileProvider, this.smartPixelsTileProvider, this.rebootTileProvider, this.soundTileProvider, this.compassTileProvider, this.antiFlickerTileProvider, this.localeTileProvider, this.sleepModeTileProvider, this.onTheGoTileProvider);
    }

    public static QSFactoryImpl provideInstance(Provider<QSHost> provider, Provider<WifiTile> provider2, Provider<BluetoothTile> provider3, Provider<CellularTile> provider4, Provider<DndTile> provider5, Provider<ColorInversionTile> provider6, Provider<AirplaneModeTile> provider7, Provider<WorkModeTile> provider8, Provider<RotationLockTile> provider9, Provider<FlashlightTile> provider10, Provider<LocationTile> provider11, Provider<CastTile> provider12, Provider<HotspotTile> provider13, Provider<UserTile> provider14, Provider<BatterySaverTile> provider15, Provider<DataSaverTile> provider16, Provider<NightDisplayTile> provider17, Provider<NfcTile> provider18, Provider<SensorPrivacyTile> provider19, Provider<GarbageMonitor.MemoryTile> provider20, Provider<UiModeNightTile> provider21, Provider<ScreenRecordTile> provider22, Provider<AmbientDisplayTile> provider23, Provider<AODTile> provider24, Provider<CaffeineTile> provider25, Provider<PowerMenuTile> provider26, Provider<DataSwitchTile> provider27, Provider<HeadsUpTile> provider28, Provider<LiveDisplayTile> provider29, Provider<PowerShareTile> provider30, Provider<ProfilesTile> provider31, Provider<ReadingModeTile> provider32, Provider<SyncTile> provider33, Provider<UsbTetherTile> provider34, Provider<VolumeTile> provider35, Provider<VpnTile> provider36, Provider<WeatherTile> provider37, Provider<CPUInfoTile> provider38, Provider<FPSInfoTile> provider39, Provider<GamingModeTile> provider40, Provider<SmartPixelsTile> provider41, Provider<RebootTile> provider42, Provider<SoundTile> provider43, Provider<CompassTile> provider44, Provider<AntiFlickerTile> provider45, Provider<LocaleTile> provider46, Provider<SleepModeTile> provider47, Provider<OnTheGoTile> provider48) {
        return new QSFactoryImpl(DoubleCheck.lazy(provider), provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13, provider14, provider15, provider16, provider17, provider18, provider19, provider20, provider21, provider22, provider23, provider24, provider25, provider26, provider27, provider28, provider29, provider30, provider31, provider32, provider33, provider34, provider35, provider36, provider37, provider38, provider39, provider40, provider41, provider42, provider43, provider44, provider45, provider46, provider47, provider48);
    }

    public static QSFactoryImpl_Factory create(Provider<QSHost> provider, Provider<WifiTile> provider2, Provider<BluetoothTile> provider3, Provider<CellularTile> provider4, Provider<DndTile> provider5, Provider<ColorInversionTile> provider6, Provider<AirplaneModeTile> provider7, Provider<WorkModeTile> provider8, Provider<RotationLockTile> provider9, Provider<FlashlightTile> provider10, Provider<LocationTile> provider11, Provider<CastTile> provider12, Provider<HotspotTile> provider13, Provider<UserTile> provider14, Provider<BatterySaverTile> provider15, Provider<DataSaverTile> provider16, Provider<NightDisplayTile> provider17, Provider<NfcTile> provider18, Provider<SensorPrivacyTile> provider19, Provider<GarbageMonitor.MemoryTile> provider20, Provider<UiModeNightTile> provider21, Provider<ScreenRecordTile> provider22, Provider<AmbientDisplayTile> provider23, Provider<AODTile> provider24, Provider<CaffeineTile> provider25, Provider<PowerMenuTile> provider26, Provider<DataSwitchTile> provider27, Provider<HeadsUpTile> provider28, Provider<LiveDisplayTile> provider29, Provider<PowerShareTile> provider30, Provider<ProfilesTile> provider31, Provider<ReadingModeTile> provider32, Provider<SyncTile> provider33, Provider<UsbTetherTile> provider34, Provider<VolumeTile> provider35, Provider<VpnTile> provider36, Provider<WeatherTile> provider37, Provider<CPUInfoTile> provider38, Provider<FPSInfoTile> provider39, Provider<GamingModeTile> provider40, Provider<SmartPixelsTile> provider41, Provider<RebootTile> provider42, Provider<SoundTile> provider43, Provider<CompassTile> provider44, Provider<AntiFlickerTile> provider45, Provider<LocaleTile> provider46, Provider<SleepModeTile> provider47, Provider<OnTheGoTile> provider48) {
        return new QSFactoryImpl_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13, provider14, provider15, provider16, provider17, provider18, provider19, provider20, provider21, provider22, provider23, provider24, provider25, provider26, provider27, provider28, provider29, provider30, provider31, provider32, provider33, provider34, provider35, provider36, provider37, provider38, provider39, provider40, provider41, provider42, provider43, provider44, provider45, provider46, provider47, provider48);
    }
}
