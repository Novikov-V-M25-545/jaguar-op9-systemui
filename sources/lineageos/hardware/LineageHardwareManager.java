package lineageos.hardware;

import android.content.Context;
import android.content.res.Resources;
import android.hidl.base.V1_0.IBase;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import lineageos.hardware.ILineageHardwareService;
import vendor.lineage.livedisplay.V2_0.IAdaptiveBacklight;
import vendor.lineage.livedisplay.V2_0.IAutoContrast;
import vendor.lineage.livedisplay.V2_0.IColorBalance;
import vendor.lineage.livedisplay.V2_0.IColorEnhancement;
import vendor.lineage.livedisplay.V2_0.IDisplayColorCalibration;
import vendor.lineage.livedisplay.V2_0.IDisplayModes;
import vendor.lineage.livedisplay.V2_0.IPictureAdjustment;
import vendor.lineage.livedisplay.V2_0.IReadingEnhancement;
import vendor.lineage.livedisplay.V2_0.ISunlightEnhancement;
import vendor.lineage.livedisplay.V2_1.IAntiFlicker;
import vendor.lineage.touch.V1_0.IGloveMode;
import vendor.lineage.touch.V1_0.IKeyDisabler;
import vendor.lineage.touch.V1_0.IKeySwapper;
import vendor.lineage.touch.V1_0.IStylusMode;
import vendor.lineage.touch.V1_0.ITouchscreenGesture;

/* loaded from: classes2.dex */
public final class LineageHardwareManager {

    @VisibleForTesting
    public static final int FEATURE_ADAPTIVE_BACKLIGHT = 1;

    @VisibleForTesting
    public static final int FEATURE_COLOR_BALANCE = 131072;

    @VisibleForTesting
    public static final int FEATURE_COLOR_ENHANCEMENT = 2;

    @VisibleForTesting
    public static final int FEATURE_DISPLAY_COLOR_CALIBRATION = 4;

    @VisibleForTesting
    public static final int FEATURE_DISPLAY_MODES = 8192;

    @VisibleForTesting
    public static final int FEATURE_HIGH_TOUCH_SENSITIVITY = 16;

    @VisibleForTesting
    public static final int FEATURE_KEY_DISABLE = 32;

    @VisibleForTesting
    public static final int FEATURE_KEY_SWAP = 64;

    @VisibleForTesting
    public static final int FEATURE_PICTURE_ADJUSTMENT = 262144;

    @VisibleForTesting
    public static final int FEATURE_TOUCHSCREEN_GESTURES = 524288;

    @VisibleForTesting
    public static final int FEATURE_VIBRATOR = 1024;
    private static LineageHardwareManager sLineageHardwareManagerInstance;
    private static ILineageHardwareService sService;
    private Context mContext;
    private final boolean mFilterDisplayModes;

    @VisibleForTesting
    public static final int FEATURE_ANTI_FLICKER = 2097152;

    @VisibleForTesting
    public static final int FEATURE_AUTO_CONTRAST = 4096;

    @VisibleForTesting
    public static final int FEATURE_SUNLIGHT_ENHANCEMENT = 256;

    @VisibleForTesting
    public static final int FEATURE_TOUCH_HOVERING = 2048;

    @VisibleForTesting
    public static final int FEATURE_READING_ENHANCEMENT = 16384;
    private static final List<Integer> BOOLEAN_FEATURES = Arrays.asList(1, Integer.valueOf(FEATURE_ANTI_FLICKER), Integer.valueOf(FEATURE_AUTO_CONTRAST), 2, 16, 32, 64, Integer.valueOf(FEATURE_SUNLIGHT_ENHANCEMENT), Integer.valueOf(FEATURE_TOUCH_HOVERING), Integer.valueOf(FEATURE_READING_ENHANCEMENT));
    private final ArrayMap<String, String> mDisplayModeMappings = new ArrayMap<>();
    private HashMap<Integer, IBase> mHIDLMap = new HashMap<>();

    private LineageHardwareManager(Context context) throws Resources.NotFoundException {
        Context applicationContext = context.getApplicationContext();
        if (applicationContext != null) {
            this.mContext = applicationContext;
        } else {
            this.mContext = context;
        }
        sService = getService();
        if (context.getPackageManager().hasSystemFeature("org.lineageos.hardware") && !checkService()) {
            Log.wtf("LineageHardwareManager", "Unable to get LineageHardwareService. The service either crashed, was not started, or the interface has been called to early in SystemServer init");
        }
        String[] stringArray = this.mContext.getResources().getStringArray(1057161218);
        if (stringArray != null && stringArray.length > 0) {
            for (String str : stringArray) {
                String[] strArrSplit = str.split(":");
                if (strArrSplit.length == 2) {
                    this.mDisplayModeMappings.put(strArrSplit[0], strArrSplit[1]);
                }
            }
        }
        this.mFilterDisplayModes = this.mContext.getResources().getBoolean(1057226760);
    }

    public static LineageHardwareManager getInstance(Context context) {
        if (sLineageHardwareManagerInstance == null) {
            sLineageHardwareManagerInstance = new LineageHardwareManager(context);
        }
        return sLineageHardwareManagerInstance;
    }

    public static ILineageHardwareService getService() {
        ILineageHardwareService iLineageHardwareService = sService;
        if (iLineageHardwareService != null) {
            return iLineageHardwareService;
        }
        IBinder service = ServiceManager.getService("lineagehardware");
        if (service == null) {
            return null;
        }
        ILineageHardwareService iLineageHardwareServiceAsInterface = ILineageHardwareService.Stub.asInterface(service);
        sService = iLineageHardwareServiceAsInterface;
        return iLineageHardwareServiceAsInterface;
    }

    public boolean isSupported(int i) {
        return isSupportedHIDL(i) || isSupportedHWC2(i);
    }

    private boolean isSupportedHIDL(int i) {
        if (!this.mHIDLMap.containsKey(Integer.valueOf(i))) {
            this.mHIDLMap.put(Integer.valueOf(i), getHIDLService(i));
        }
        return this.mHIDLMap.get(Integer.valueOf(i)) != null;
    }

    private boolean isSupportedHWC2(int i) {
        try {
            if (checkService()) {
                return i == (sService.getSupportedFeatures() & i);
            }
            return false;
        } catch (RemoteException unused) {
            return false;
        }
    }

    private IBase getHIDLService(int i) {
        try {
            if (i == 1) {
                return IAdaptiveBacklight.getService(true);
            }
            if (i == 2) {
                return IColorEnhancement.getService(true);
            }
            switch (i) {
                case 4:
                    return IDisplayColorCalibration.getService(true);
                case FEATURE_HIGH_TOUCH_SENSITIVITY /* 16 */:
                    return IGloveMode.getService(true);
                case FEATURE_KEY_DISABLE /* 32 */:
                    return IKeyDisabler.getService(true);
                case FEATURE_KEY_SWAP /* 64 */:
                    return IKeySwapper.getService(true);
                case FEATURE_SUNLIGHT_ENHANCEMENT /* 256 */:
                    return ISunlightEnhancement.getService(true);
                case FEATURE_TOUCH_HOVERING /* 2048 */:
                    return IStylusMode.getService(true);
                case FEATURE_AUTO_CONTRAST /* 4096 */:
                    return IAutoContrast.getService(true);
                case FEATURE_DISPLAY_MODES /* 8192 */:
                    return IDisplayModes.getService(true);
                case FEATURE_READING_ENHANCEMENT /* 16384 */:
                    return IReadingEnhancement.getService(true);
                case FEATURE_COLOR_BALANCE /* 131072 */:
                    return IColorBalance.getService(true);
                case FEATURE_PICTURE_ADJUSTMENT /* 262144 */:
                    return IPictureAdjustment.getService(true);
                case FEATURE_TOUCHSCREEN_GESTURES /* 524288 */:
                    return ITouchscreenGesture.getService(true);
                case FEATURE_ANTI_FLICKER /* 2097152 */:
                    return IAntiFlicker.getService(true);
                default:
                    return null;
            }
        } catch (RemoteException | NoSuchElementException unused) {
            return null;
        }
    }

    public boolean isSupported(String str) throws NoSuchFieldException {
        if (!str.startsWith("FEATURE_")) {
            return false;
        }
        try {
            Field field = LineageHardwareManager.class.getField(str);
            if (field != null) {
                return isSupported(((Integer) field.get(null)).intValue());
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Log.d("LineageHardwareManager", e.getMessage(), e);
        }
        return false;
    }

    public boolean get(int i) {
        if (!BOOLEAN_FEATURES.contains(Integer.valueOf(i))) {
            throw new IllegalArgumentException(i + " is not a boolean");
        }
        try {
            if (isSupportedHIDL(i)) {
                IBase iBase = this.mHIDLMap.get(Integer.valueOf(i));
                if (i == 1) {
                    return ((IAdaptiveBacklight) iBase).isEnabled();
                }
                if (i == 2) {
                    return ((IColorEnhancement) iBase).isEnabled();
                }
                if (i == 16) {
                    return ((IGloveMode) iBase).isEnabled();
                }
                if (i == 32) {
                    return ((IKeyDisabler) iBase).isEnabled();
                }
                if (i == 64) {
                    return ((IKeySwapper) iBase).isEnabled();
                }
                if (i == 256) {
                    return ((ISunlightEnhancement) iBase).isEnabled();
                }
                if (i == 2048) {
                    return ((IStylusMode) iBase).isEnabled();
                }
                if (i == 4096) {
                    return ((IAutoContrast) iBase).isEnabled();
                }
                if (i == 16384) {
                    return ((IReadingEnhancement) iBase).isEnabled();
                }
                if (i != 2097152) {
                    return false;
                }
                return ((IAntiFlicker) iBase).isEnabled();
            }
            if (checkService()) {
                return sService.get(i);
            }
            return false;
        } catch (RemoteException unused) {
            return false;
        }
    }

    public boolean set(int i, boolean z) {
        if (!BOOLEAN_FEATURES.contains(Integer.valueOf(i))) {
            throw new IllegalArgumentException(i + " is not a boolean");
        }
        try {
            if (isSupportedHIDL(i)) {
                IBase iBase = this.mHIDLMap.get(Integer.valueOf(i));
                if (i == 1) {
                    return ((IAdaptiveBacklight) iBase).setEnabled(z);
                }
                if (i == 2) {
                    return ((IColorEnhancement) iBase).setEnabled(z);
                }
                if (i == 16) {
                    return ((IGloveMode) iBase).setEnabled(z);
                }
                if (i == 32) {
                    return ((IKeyDisabler) iBase).setEnabled(z);
                }
                if (i == 64) {
                    return ((IKeySwapper) iBase).setEnabled(z);
                }
                if (i == 256) {
                    return ((ISunlightEnhancement) iBase).setEnabled(z);
                }
                if (i == 2048) {
                    return ((IStylusMode) iBase).setEnabled(z);
                }
                if (i == 4096) {
                    return ((IAutoContrast) iBase).setEnabled(z);
                }
                if (i == 16384) {
                    return ((IReadingEnhancement) iBase).setEnabled(z);
                }
                if (i != 2097152) {
                    return false;
                }
                return ((IAntiFlicker) iBase).setEnabled(z);
            }
            if (checkService()) {
                return sService.set(i, z);
            }
            return false;
        } catch (RemoteException unused) {
            return false;
        }
    }

    private boolean checkService() {
        if (sService != null) {
            return true;
        }
        Log.w("LineageHardwareManager", "not connected to LineageHardwareManagerService");
        return false;
    }
}
