package lineageos.providers;

import android.content.ContentResolver;
import android.content.IContentProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/* loaded from: classes2.dex */
public final class LineageSettings {
    private static final Validator sBooleanValidator = new DiscreteValueValidator(new String[]{"0", "1"});
    private static final Validator sNonNegativeIntegerValidator = new Validator() { // from class: lineageos.providers.LineageSettings.1
    };
    private static final Validator sNonNegativeLongValidator = new Validator() { // from class: lineageos.providers.LineageSettings.2
    };
    private static final Validator sUriValidator = new Validator() { // from class: lineageos.providers.LineageSettings.3
    };
    private static final Validator sColorValidator = new InclusiveIntegerRangeValidator(Integer.MIN_VALUE, Integer.MAX_VALUE);
    private static final Validator sAlwaysTrueValidator = new Validator() { // from class: lineageos.providers.LineageSettings.4
    };
    private static final Validator sNonNullStringValidator = new Validator() { // from class: lineageos.providers.LineageSettings.5
    };

    public interface Validator {
    }

    private static class NameValueCache {
        private static final String[] SELECT_VALUE_PROJECTION = {"value"};
        private final String mCallGetCommand;
        private final String mCallSetCommand;
        private final Uri mUri;
        private final String mVersionSystemProperty;
        private final HashMap<String, String> mValues = new HashMap<>();
        private long mValuesVersion = 0;
        private IContentProvider mContentProvider = null;

        public NameValueCache(String str, Uri uri, String str2, String str3) {
            this.mVersionSystemProperty = str;
            this.mUri = uri;
            this.mCallGetCommand = str2;
            this.mCallSetCommand = str3;
        }

        private IContentProvider lazyGetProvider(ContentResolver contentResolver) {
            IContentProvider iContentProviderAcquireProvider;
            synchronized (this) {
                iContentProviderAcquireProvider = this.mContentProvider;
                if (iContentProviderAcquireProvider == null) {
                    iContentProviderAcquireProvider = contentResolver.acquireProvider(this.mUri.getAuthority());
                    this.mContentProvider = iContentProviderAcquireProvider;
                }
            }
            return iContentProviderAcquireProvider;
        }

        public boolean putStringForUser(ContentResolver contentResolver, String str, String str2, int i) {
            try {
                Bundle bundle = new Bundle();
                bundle.putString("value", str2);
                bundle.putInt("_user", i);
                lazyGetProvider(contentResolver).call(contentResolver.getPackageName(), contentResolver.getAttributionTag(), "lineagesettings", this.mCallSetCommand, str, bundle);
                return true;
            } catch (RemoteException e) {
                Log.w("LineageSettings", "Can't set key " + str + " in " + this.mUri, e);
                return false;
            }
        }

        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Removed duplicated region for block: B:70:0x0106  */
        /* JADX WARN: Type inference failed for: r11v0 */
        /* JADX WARN: Type inference failed for: r11v1, types: [android.database.Cursor] */
        /* JADX WARN: Type inference failed for: r11v2 */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        public java.lang.String getStringForUser(android.content.ContentResolver r13, java.lang.String r14, int r15) throws java.lang.Throwable {
            /*
                Method dump skipped, instructions count: 266
                To view this dump change 'Code comments level' option to 'DEBUG'
            */
            throw new UnsupportedOperationException("Method not decompiled: lineageos.providers.LineageSettings.NameValueCache.getStringForUser(android.content.ContentResolver, java.lang.String, int):java.lang.String");
        }
    }

    private static final class DiscreteValueValidator implements Validator {
        private final String[] mValues;

        public DiscreteValueValidator(String[] strArr) {
            this.mValues = strArr;
        }
    }

    private static final class InclusiveIntegerRangeValidator implements Validator {
        private final int mMax;
        private final int mMin;

        public InclusiveIntegerRangeValidator(int i, int i2) {
            this.mMin = i;
            this.mMax = i2;
        }
    }

    private static final class InclusiveFloatRangeValidator implements Validator {
        private final float mMax;
        private final float mMin;

        public InclusiveFloatRangeValidator(float f, float f2) {
            this.mMin = f;
            this.mMax = f2;
        }
    }

    private static final class DelimitedListValidator implements Validator {
        private final boolean mAllowEmptyList;
        private final String mDelimiter;
        private final ArraySet<String> mValidValueSet;

        public DelimitedListValidator(String[] strArr, String str, boolean z) {
            this.mValidValueSet = new ArraySet<>(Arrays.asList(strArr));
            this.mDelimiter = str;
            this.mAllowEmptyList = z;
        }
    }

    public static final class System extends Settings.NameValueTable {
        public static final Validator APP_SWITCH_WAKE_SCREEN_VALIDATOR;
        public static final Validator ASSIST_WAKE_SCREEN_VALIDATOR;
        public static final Validator AUTO_BRIGHTNESS_ONE_SHOT_VALIDATOR;
        public static final Validator BACK_WAKE_SCREEN_VALIDATOR;
        public static final Validator BATTERY_LIGHT_BRIGHTNESS_LEVEL_VALIDATOR;
        public static final Validator BATTERY_LIGHT_ENABLED_VALIDATOR;
        public static final Validator BATTERY_LIGHT_FULL_COLOR_VALIDATOR;
        public static final Validator BATTERY_LIGHT_LOW_COLOR_VALIDATOR;
        public static final Validator BATTERY_LIGHT_MEDIUM_COLOR_VALIDATOR;
        public static final Validator BATTERY_LIGHT_PULSE_VALIDATOR;
        public static final Validator BERRY_BLACK_THEME_VALIDATOR;

        @Deprecated
        public static final Validator BERRY_CURRENT_ACCENT_VALIDATOR;

        @Deprecated
        public static final Validator BERRY_DARK_OVERLAY_VALIDATOR;

        @Deprecated
        public static final Validator BERRY_GLOBAL_STYLE_VALIDATOR;

        @Deprecated
        public static final Validator BERRY_MANAGED_BY_APP_VALIDATOR;
        public static final Validator BLUETOOTH_ACCEPT_ALL_FILES_VALIDATOR;
        public static final Validator BUTTON_BACKLIGHT_ONLY_WHEN_PRESSED_VALIDATOR;
        public static final Validator CALL_RECORDING_FORMAT_VALIDATOR;
        public static final Validator CAMERA_LAUNCH_VALIDATOR;
        public static final Validator CAMERA_SLEEP_ON_RELEASE_VALIDATOR;
        public static final Validator CAMERA_WAKE_SCREEN_VALIDATOR;
        public static final Validator CLICK_PARTIAL_SCREENSHOT_VALIDATOR;
        public static final Uri CONTENT_URI;
        public static final Validator DIALER_OPENCNAM_ACCOUNT_SID_VALIDATOR;
        public static final Validator DIALER_OPENCNAM_AUTH_TOKEN_VALIDATOR;
        public static final Validator DISPLAY_ANTI_FLICKER_VALIDATOR;
        public static final Validator DISPLAY_AUTO_CONTRAST_VALIDATOR;
        public static final Validator DISPLAY_AUTO_OUTDOOR_MODE_VALIDATOR;
        public static final Validator DISPLAY_CABC_VALIDATOR;
        public static final Validator DISPLAY_COLOR_ADJUSTMENT_VALIDATOR;
        public static final Validator DISPLAY_COLOR_ENHANCE_VALIDATOR;
        public static final Validator DISPLAY_PICTURE_ADJUSTMENT_VALIDATOR;
        public static final Validator DISPLAY_READING_MODE_VALIDATOR;
        public static final Validator DISPLAY_TEMPERATURE_DAY_VALIDATOR;
        public static final Validator DISPLAY_TEMPERATURE_MODE_VALIDATOR;
        public static final Validator DISPLAY_TEMPERATURE_NIGHT_VALIDATOR;
        public static final Validator DOUBLE_TAP_SLEEP_GESTURE_VALIDATOR;
        public static final Validator ENABLE_FORWARD_LOOKUP_VALIDATOR;
        public static final Validator ENABLE_MWI_NOTIFICATION_VALIDATOR;
        public static final Validator ENABLE_PEOPLE_LOOKUP_VALIDATOR;
        public static final Validator ENABLE_REVERSE_LOOKUP_VALIDATOR;
        public static final Validator FORCE_SHOW_NAVBAR_VALIDATOR;
        public static final Validator FORWARD_LOOKUP_PROVIDER_VALIDATOR;
        public static final Validator HEADSET_CONNECT_PLAYER_VALIDATOR;
        public static final Validator HIGH_TOUCH_SENSITIVITY_ENABLE_VALIDATOR;
        public static final Validator HOME_WAKE_SCREEN_VALIDATOR;
        public static final Validator INCREASING_RING_RAMP_UP_TIME_VALIDATOR;
        public static final Validator INCREASING_RING_START_VOLUME_VALIDATOR;
        public static final Validator INCREASING_RING_VALIDATOR;
        public static final Validator KEY_APP_SWITCH_ACTION_VALIDATOR;
        public static final Validator KEY_APP_SWITCH_LONG_PRESS_ACTION_VALIDATOR;
        public static final Validator KEY_ASSIST_ACTION_VALIDATOR;
        public static final Validator KEY_ASSIST_LONG_PRESS_ACTION_VALIDATOR;
        public static final Validator KEY_BACK_DOUBLE_TAP_ACTION_VALIDATOR;
        public static final Validator KEY_BACK_LONG_PRESS_ACTION_VALIDATOR;
        public static final Validator KEY_EDGE_LONG_SWIPE_ACTION_VALIDATOR;
        public static final Validator KEY_HOME_DOUBLE_TAP_ACTION_VALIDATOR;
        public static final Validator KEY_HOME_LONG_PRESS_ACTION_VALIDATOR;
        public static final Validator KEY_MENU_ACTION_VALIDATOR;
        public static final Validator KEY_MENU_LONG_PRESS_ACTION_VALIDATOR;
        public static final String[] LEGACY_SYSTEM_SETTINGS;
        public static final Validator LIVE_DISPLAY_HINTED_VALIDATOR;
        public static final Validator LOCKSCREEN_PIN_SCRAMBLE_LAYOUT_VALIDATOR;
        public static final Validator LOCKSCREEN_ROTATION_VALIDATOR;
        public static final Validator LONG_SCREEN_APPS_VALIDATOR;
        public static final Validator MENU_WAKE_SCREENN_VALIDATOR;
        protected static final ArraySet<String> MOVED_TO_SECURE;
        public static final Validator NAVBAR_LEFT_IN_LANDSCAPE_VALIDATOR;
        public static final Validator NAVIGATION_BAR_HINT_VALIDATOR;
        public static final Validator NAVIGATION_BAR_MENU_ARROW_KEYS_VALIDATOR;
        public static final Validator NAV_BUTTONS_VALIDATOR;
        public static final Validator NOTIFICATION_LIGHT_BRIGHTNESS_LEVEL_VALIDATOR;
        public static final Validator NOTIFICATION_LIGHT_COLOR_AUTO_VALIDATOR;
        public static final Validator NOTIFICATION_LIGHT_PULSE_CALL_COLOR_VALIDATOR;
        public static final Validator NOTIFICATION_LIGHT_PULSE_CALL_LED_OFF_VALIDATOR;
        public static final Validator NOTIFICATION_LIGHT_PULSE_CALL_LED_ON_VALIDATOR;
        public static final Validator NOTIFICATION_LIGHT_PULSE_CUSTOM_ENABLE_VALIDATOR;
        public static final Validator NOTIFICATION_LIGHT_PULSE_CUSTOM_VALUES_VALIDATOR;
        public static final Validator NOTIFICATION_LIGHT_PULSE_DEFAULT_COLOR_VALIDATOR;
        public static final Validator NOTIFICATION_LIGHT_PULSE_DEFAULT_LED_OFF_VALIDATOR;
        public static final Validator NOTIFICATION_LIGHT_PULSE_DEFAULT_LED_ON_VALIDATOR;
        public static final Validator NOTIFICATION_LIGHT_PULSE_OVERRIDE_VALIDATOR;
        public static final Validator NOTIFICATION_LIGHT_PULSE_VMAIL_COLOR_VALIDATOR;
        public static final Validator NOTIFICATION_LIGHT_PULSE_VMAIL_LED_OFF_VALIDATOR;
        public static final Validator NOTIFICATION_LIGHT_PULSE_VMAIL_LED_ON_VALIDATOR;
        public static final Validator NOTIFICATION_LIGHT_SCREEN_ON_VALIDATOR;
        public static final Validator NOTIFICATION_PLAY_QUEUE_VALIDATOR;
        public static final Validator PEOPLE_LOOKUP_PROVIDER_VALIDATOR;
        public static final Validator PROXIMITY_ON_WAKE_VALIDATOR;
        public static final Validator QS_SHOW_BRIGHTNESS_SLIDER_VALIDATOR;
        public static final Validator RECENTS_SHOW_SEARCH_BAR_VALIDATOR;
        public static final Validator REVERSE_LOOKUP_PROVIDER_VALIDATOR;
        public static final Validator SHOW_ALARM_ICON_VALIDATOR;
        public static final Validator STATUS_BAR_AM_PM_VALIDATOR;
        public static final Validator STATUS_BAR_BATTERY_STYLE_VALIDATOR;
        public static final Validator STATUS_BAR_BRIGHTNESS_CONTROL_VALIDATOR;
        public static final Validator STATUS_BAR_CLOCK_VALIDATOR;
        public static final Validator STATUS_BAR_IME_SWITCHER_VALIDATOR;
        public static final Validator STATUS_BAR_NOTIF_COUNT_VALIDATOR;
        public static final Validator STATUS_BAR_QUICK_QS_PULLDOWN_VALIDATOR;
        public static final Validator STATUS_BAR_SHOW_BATTERY_PERCENT_VALIDATOR;
        public static final Validator STATUS_BAR_SHOW_WEATHER_VALIDATOR;
        public static final Validator STYLUS_ICON_ENABLED_VALIDATOR;
        public static final Validator SWAP_VOLUME_KEYS_ON_ROTATION_VALIDATOR;
        public static final Validator SYSTEM_PROFILES_ENABLED_VALIDATOR;
        public static final Validator T9_SEARCH_INPUT_LOCALE_VALIDATOR;
        public static final Validator TORCH_LONG_PRESS_POWER_GESTURE_VALIDATOR;
        public static final Validator TORCH_LONG_PRESS_POWER_TIMEOUT_VALIDATOR;
        public static final Validator TOUCHSCREEN_GESTURE_HAPTIC_FEEDBACK_VALIDATOR;
        public static final Validator TRUST_INTERFACE_HINTED_VALIDATOR;
        public static final Validator USE_EDGE_SERVICE_FOR_GESTURES_VALIDATOR;
        public static final Map<String, Validator> VALIDATORS;
        public static final Validator VOLBTN_MUSIC_CONTROLS_VALIDATOR;
        public static final Validator VOLUME_ADJUST_SOUNDS_ENABLED_VALIDATOR;
        public static final Validator VOLUME_ANSWER_CALL_VALIDATOR;
        public static final Validator VOLUME_WAKE_SCREEN_VALIDATOR;
        public static final Validator ZEN_ALLOW_LIGHTS_VALIDATOR;
        public static final Validator ZEN_PRIORITY_ALLOW_LIGHTS_VALIDATOR;
        public static final Validator ZEN_PRIORITY_VIBRATION_VALIDATOR;
        public static final Validator __MAGICAL_TEST_PASSING_ENABLER_VALIDATOR;
        private static final NameValueCache sNameValueCache;

        static {
            Uri uri = Uri.parse("content://lineagesettings/system");
            CONTENT_URI = uri;
            sNameValueCache = new NameValueCache("sys.lineage_settings_system_version", uri, "GET_system", "PUT_system");
            MOVED_TO_SECURE = new ArraySet<>(1);
            Validator validator = LineageSettings.sBooleanValidator;
            NOTIFICATION_PLAY_QUEUE_VALIDATOR = validator;
            Validator validator2 = LineageSettings.sBooleanValidator;
            HIGH_TOUCH_SENSITIVITY_ENABLE_VALIDATOR = validator2;
            Validator validator3 = LineageSettings.sBooleanValidator;
            SYSTEM_PROFILES_ENABLED_VALIDATOR = validator3;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator = new InclusiveIntegerRangeValidator(0, 3);
            STATUS_BAR_CLOCK_VALIDATOR = inclusiveIntegerRangeValidator;
            Validator validator4 = LineageSettings.sBooleanValidator;
            ZEN_ALLOW_LIGHTS_VALIDATOR = validator4;
            Validator validator5 = LineageSettings.sBooleanValidator;
            ZEN_PRIORITY_ALLOW_LIGHTS_VALIDATOR = validator5;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator2 = new InclusiveIntegerRangeValidator(0, 2);
            ZEN_PRIORITY_VIBRATION_VALIDATOR = inclusiveIntegerRangeValidator2;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator3 = new InclusiveIntegerRangeValidator(0, 2);
            STATUS_BAR_AM_PM_VALIDATOR = inclusiveIntegerRangeValidator3;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator4 = new InclusiveIntegerRangeValidator(0, 2);
            STATUS_BAR_BATTERY_STYLE_VALIDATOR = inclusiveIntegerRangeValidator4;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator5 = new InclusiveIntegerRangeValidator(0, 2);
            STATUS_BAR_SHOW_BATTERY_PERCENT_VALIDATOR = inclusiveIntegerRangeValidator5;
            Validator validator6 = LineageSettings.sBooleanValidator;
            INCREASING_RING_VALIDATOR = validator6;
            InclusiveFloatRangeValidator inclusiveFloatRangeValidator = new InclusiveFloatRangeValidator(0.0f, 1.0f);
            INCREASING_RING_START_VOLUME_VALIDATOR = inclusiveFloatRangeValidator;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator6 = new InclusiveIntegerRangeValidator(5, 60);
            INCREASING_RING_RAMP_UP_TIME_VALIDATOR = inclusiveIntegerRangeValidator6;
            Validator validator7 = LineageSettings.sBooleanValidator;
            VOLUME_ADJUST_SOUNDS_ENABLED_VALIDATOR = validator7;
            DelimitedListValidator delimitedListValidator = new DelimitedListValidator(new String[]{"empty", "home", "back", "search", "recent", "menu0", "menu1", "menu2", "dpad_left", "dpad_right"}, "|", true);
            NAV_BUTTONS_VALIDATOR = delimitedListValidator;
            Validator validator8 = LineageSettings.sBooleanValidator;
            NAVIGATION_BAR_MENU_ARROW_KEYS_VALIDATOR = validator8;
            Validator validator9 = LineageSettings.sBooleanValidator;
            NAVIGATION_BAR_HINT_VALIDATOR = validator9;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator7 = new InclusiveIntegerRangeValidator(0, 19);
            KEY_HOME_LONG_PRESS_ACTION_VALIDATOR = inclusiveIntegerRangeValidator7;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator8 = new InclusiveIntegerRangeValidator(0, 19);
            KEY_HOME_DOUBLE_TAP_ACTION_VALIDATOR = inclusiveIntegerRangeValidator8;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator9 = new InclusiveIntegerRangeValidator(0, 19);
            KEY_BACK_DOUBLE_TAP_ACTION_VALIDATOR = inclusiveIntegerRangeValidator9;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator10 = new InclusiveIntegerRangeValidator(0, 19);
            KEY_BACK_LONG_PRESS_ACTION_VALIDATOR = inclusiveIntegerRangeValidator10;
            Validator validator10 = LineageSettings.sBooleanValidator;
            BACK_WAKE_SCREEN_VALIDATOR = validator10;
            Validator validator11 = LineageSettings.sBooleanValidator;
            MENU_WAKE_SCREENN_VALIDATOR = validator11;
            Validator validator12 = LineageSettings.sBooleanValidator;
            VOLUME_ANSWER_CALL_VALIDATOR = validator12;
            Validator validator13 = LineageSettings.sBooleanValidator;
            VOLUME_WAKE_SCREEN_VALIDATOR = validator13;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator11 = new InclusiveIntegerRangeValidator(0, 19);
            KEY_MENU_ACTION_VALIDATOR = inclusiveIntegerRangeValidator11;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator12 = new InclusiveIntegerRangeValidator(0, 19);
            KEY_MENU_LONG_PRESS_ACTION_VALIDATOR = inclusiveIntegerRangeValidator12;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator13 = new InclusiveIntegerRangeValidator(0, 19);
            KEY_ASSIST_ACTION_VALIDATOR = inclusiveIntegerRangeValidator13;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator14 = new InclusiveIntegerRangeValidator(0, 19);
            KEY_ASSIST_LONG_PRESS_ACTION_VALIDATOR = inclusiveIntegerRangeValidator14;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator15 = new InclusiveIntegerRangeValidator(0, 19);
            KEY_APP_SWITCH_ACTION_VALIDATOR = inclusiveIntegerRangeValidator15;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator16 = new InclusiveIntegerRangeValidator(0, 19);
            KEY_APP_SWITCH_LONG_PRESS_ACTION_VALIDATOR = inclusiveIntegerRangeValidator16;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator17 = new InclusiveIntegerRangeValidator(0, 19);
            KEY_EDGE_LONG_SWIPE_ACTION_VALIDATOR = inclusiveIntegerRangeValidator17;
            Validator validator14 = LineageSettings.sBooleanValidator;
            HOME_WAKE_SCREEN_VALIDATOR = validator14;
            Validator validator15 = LineageSettings.sBooleanValidator;
            ASSIST_WAKE_SCREEN_VALIDATOR = validator15;
            Validator validator16 = LineageSettings.sBooleanValidator;
            APP_SWITCH_WAKE_SCREEN_VALIDATOR = validator16;
            Validator validator17 = LineageSettings.sBooleanValidator;
            CAMERA_WAKE_SCREEN_VALIDATOR = validator17;
            Validator validator18 = LineageSettings.sBooleanValidator;
            CAMERA_SLEEP_ON_RELEASE_VALIDATOR = validator18;
            Validator validator19 = LineageSettings.sBooleanValidator;
            CAMERA_LAUNCH_VALIDATOR = validator19;
            Validator validator20 = LineageSettings.sBooleanValidator;
            STYLUS_ICON_ENABLED_VALIDATOR = validator20;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator18 = new InclusiveIntegerRangeValidator(0, 2);
            SWAP_VOLUME_KEYS_ON_ROTATION_VALIDATOR = inclusiveIntegerRangeValidator18;
            Validator validator21 = LineageSettings.sBooleanValidator;
            TORCH_LONG_PRESS_POWER_GESTURE_VALIDATOR = validator21;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator19 = new InclusiveIntegerRangeValidator(0, 3600);
            TORCH_LONG_PRESS_POWER_TIMEOUT_VALIDATOR = inclusiveIntegerRangeValidator19;
            Validator validator22 = LineageSettings.sBooleanValidator;
            BUTTON_BACKLIGHT_ONLY_WHEN_PRESSED_VALIDATOR = validator22;
            Validator validator23 = LineageSettings.sBooleanValidator;
            BATTERY_LIGHT_ENABLED_VALIDATOR = validator23;
            Validator validator24 = LineageSettings.sBooleanValidator;
            BATTERY_LIGHT_PULSE_VALIDATOR = validator24;
            Validator validator25 = LineageSettings.sColorValidator;
            BATTERY_LIGHT_LOW_COLOR_VALIDATOR = validator25;
            Validator validator26 = LineageSettings.sColorValidator;
            BATTERY_LIGHT_MEDIUM_COLOR_VALIDATOR = validator26;
            Validator validator27 = LineageSettings.sColorValidator;
            BATTERY_LIGHT_FULL_COLOR_VALIDATOR = validator27;
            Validator validator28 = LineageSettings.sBooleanValidator;
            ENABLE_MWI_NOTIFICATION_VALIDATOR = validator28;
            Validator validator29 = LineageSettings.sBooleanValidator;
            PROXIMITY_ON_WAKE_VALIDATOR = validator29;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator20 = new InclusiveIntegerRangeValidator(0, 3);
            BERRY_GLOBAL_STYLE_VALIDATOR = inclusiveIntegerRangeValidator20;
            Validator validator30 = LineageSettings.sNonNullStringValidator;
            BERRY_CURRENT_ACCENT_VALIDATOR = validator30;
            Validator validator31 = LineageSettings.sNonNullStringValidator;
            BERRY_DARK_OVERLAY_VALIDATOR = validator31;
            Validator validator32 = LineageSettings.sNonNullStringValidator;
            BERRY_MANAGED_BY_APP_VALIDATOR = validator32;
            Validator validator33 = LineageSettings.sBooleanValidator;
            BERRY_BLACK_THEME_VALIDATOR = validator33;
            Validator validator34 = LineageSettings.sBooleanValidator;
            ENABLE_FORWARD_LOOKUP_VALIDATOR = validator34;
            Validator validator35 = LineageSettings.sBooleanValidator;
            ENABLE_PEOPLE_LOOKUP_VALIDATOR = validator35;
            Validator validator36 = LineageSettings.sBooleanValidator;
            ENABLE_REVERSE_LOOKUP_VALIDATOR = validator36;
            Validator validator37 = LineageSettings.sAlwaysTrueValidator;
            FORWARD_LOOKUP_PROVIDER_VALIDATOR = validator37;
            Validator validator38 = LineageSettings.sAlwaysTrueValidator;
            PEOPLE_LOOKUP_PROVIDER_VALIDATOR = validator38;
            Validator validator39 = LineageSettings.sAlwaysTrueValidator;
            REVERSE_LOOKUP_PROVIDER_VALIDATOR = validator39;
            Validator validator40 = LineageSettings.sAlwaysTrueValidator;
            DIALER_OPENCNAM_ACCOUNT_SID_VALIDATOR = validator40;
            Validator validator41 = LineageSettings.sAlwaysTrueValidator;
            DIALER_OPENCNAM_AUTH_TOKEN_VALIDATOR = validator41;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator21 = new InclusiveIntegerRangeValidator(0, 100000);
            DISPLAY_TEMPERATURE_DAY_VALIDATOR = inclusiveIntegerRangeValidator21;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator22 = new InclusiveIntegerRangeValidator(0, 100000);
            DISPLAY_TEMPERATURE_NIGHT_VALIDATOR = inclusiveIntegerRangeValidator22;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator23 = new InclusiveIntegerRangeValidator(0, 4);
            DISPLAY_TEMPERATURE_MODE_VALIDATOR = inclusiveIntegerRangeValidator23;
            Validator validator42 = LineageSettings.sBooleanValidator;
            DISPLAY_AUTO_OUTDOOR_MODE_VALIDATOR = validator42;
            Validator validator43 = LineageSettings.sBooleanValidator;
            DISPLAY_ANTI_FLICKER_VALIDATOR = validator43;
            Validator validator44 = LineageSettings.sBooleanValidator;
            DISPLAY_READING_MODE_VALIDATOR = validator44;
            Validator validator45 = LineageSettings.sBooleanValidator;
            DISPLAY_CABC_VALIDATOR = validator45;
            Validator validator46 = LineageSettings.sBooleanValidator;
            DISPLAY_COLOR_ENHANCE_VALIDATOR = validator46;
            Validator validator47 = LineageSettings.sBooleanValidator;
            DISPLAY_AUTO_CONTRAST_VALIDATOR = validator47;
            Validator validator48 = new Validator() { // from class: lineageos.providers.LineageSettings.System.1
            };
            DISPLAY_COLOR_ADJUSTMENT_VALIDATOR = validator48;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator24 = new InclusiveIntegerRangeValidator(-3, 1);
            LIVE_DISPLAY_HINTED_VALIDATOR = inclusiveIntegerRangeValidator24;
            Validator validator49 = LineageSettings.sBooleanValidator;
            TRUST_INTERFACE_HINTED_VALIDATOR = validator49;
            Validator validator50 = LineageSettings.sBooleanValidator;
            DOUBLE_TAP_SLEEP_GESTURE_VALIDATOR = validator50;
            Validator validator51 = LineageSettings.sBooleanValidator;
            STATUS_BAR_SHOW_WEATHER_VALIDATOR = validator51;
            Validator validator52 = LineageSettings.sBooleanValidator;
            RECENTS_SHOW_SEARCH_BAR_VALIDATOR = validator52;
            Validator validator53 = LineageSettings.sBooleanValidator;
            NAVBAR_LEFT_IN_LANDSCAPE_VALIDATOR = validator53;
            Validator validator54 = new Validator() { // from class: lineageos.providers.LineageSettings.System.2
            };
            T9_SEARCH_INPUT_LOCALE_VALIDATOR = validator54;
            Validator validator55 = LineageSettings.sBooleanValidator;
            BLUETOOTH_ACCEPT_ALL_FILES_VALIDATOR = validator55;
            Validator validator56 = LineageSettings.sBooleanValidator;
            LOCKSCREEN_PIN_SCRAMBLE_LAYOUT_VALIDATOR = validator56;
            Validator validator57 = LineageSettings.sBooleanValidator;
            LOCKSCREEN_ROTATION_VALIDATOR = validator57;
            Validator validator58 = LineageSettings.sBooleanValidator;
            SHOW_ALARM_ICON_VALIDATOR = validator58;
            Validator validator59 = LineageSettings.sBooleanValidator;
            STATUS_BAR_IME_SWITCHER_VALIDATOR = validator59;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator25 = new InclusiveIntegerRangeValidator(0, 3);
            STATUS_BAR_QUICK_QS_PULLDOWN_VALIDATOR = inclusiveIntegerRangeValidator25;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator26 = new InclusiveIntegerRangeValidator(0, 2);
            QS_SHOW_BRIGHTNESS_SLIDER_VALIDATOR = inclusiveIntegerRangeValidator26;
            Validator validator60 = LineageSettings.sBooleanValidator;
            STATUS_BAR_BRIGHTNESS_CONTROL_VALIDATOR = validator60;
            Validator validator61 = LineageSettings.sBooleanValidator;
            VOLBTN_MUSIC_CONTROLS_VALIDATOR = validator61;
            Validator validator62 = LineageSettings.sBooleanValidator;
            USE_EDGE_SERVICE_FOR_GESTURES_VALIDATOR = validator62;
            Validator validator63 = LineageSettings.sBooleanValidator;
            STATUS_BAR_NOTIF_COUNT_VALIDATOR = validator63;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator27 = new InclusiveIntegerRangeValidator(0, 1);
            CALL_RECORDING_FORMAT_VALIDATOR = inclusiveIntegerRangeValidator27;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator28 = new InclusiveIntegerRangeValidator(1, 255);
            BATTERY_LIGHT_BRIGHTNESS_LEVEL_VALIDATOR = inclusiveIntegerRangeValidator28;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator29 = new InclusiveIntegerRangeValidator(1, 255);
            NOTIFICATION_LIGHT_BRIGHTNESS_LEVEL_VALIDATOR = inclusiveIntegerRangeValidator29;
            Validator validator64 = LineageSettings.sBooleanValidator;
            NOTIFICATION_LIGHT_SCREEN_ON_VALIDATOR = validator64;
            Validator validator65 = LineageSettings.sColorValidator;
            NOTIFICATION_LIGHT_PULSE_DEFAULT_COLOR_VALIDATOR = validator65;
            Validator validator66 = LineageSettings.sNonNegativeIntegerValidator;
            NOTIFICATION_LIGHT_PULSE_DEFAULT_LED_ON_VALIDATOR = validator66;
            Validator validator67 = LineageSettings.sNonNegativeIntegerValidator;
            NOTIFICATION_LIGHT_PULSE_DEFAULT_LED_OFF_VALIDATOR = validator67;
            Validator validator68 = LineageSettings.sColorValidator;
            NOTIFICATION_LIGHT_PULSE_CALL_COLOR_VALIDATOR = validator68;
            Validator validator69 = LineageSettings.sNonNegativeIntegerValidator;
            NOTIFICATION_LIGHT_PULSE_CALL_LED_ON_VALIDATOR = validator69;
            Validator validator70 = LineageSettings.sNonNegativeIntegerValidator;
            NOTIFICATION_LIGHT_PULSE_CALL_LED_OFF_VALIDATOR = validator70;
            Validator validator71 = LineageSettings.sColorValidator;
            NOTIFICATION_LIGHT_PULSE_VMAIL_COLOR_VALIDATOR = validator71;
            Validator validator72 = LineageSettings.sNonNegativeIntegerValidator;
            NOTIFICATION_LIGHT_PULSE_VMAIL_LED_ON_VALIDATOR = validator72;
            Validator validator73 = LineageSettings.sNonNegativeIntegerValidator;
            NOTIFICATION_LIGHT_PULSE_VMAIL_LED_OFF_VALIDATOR = validator73;
            Validator validator74 = LineageSettings.sBooleanValidator;
            NOTIFICATION_LIGHT_PULSE_CUSTOM_ENABLE_VALIDATOR = validator74;
            Validator validator75 = new Validator() { // from class: lineageos.providers.LineageSettings.System.3
            };
            NOTIFICATION_LIGHT_PULSE_CUSTOM_VALUES_VALIDATOR = validator75;
            Validator validator76 = LineageSettings.sBooleanValidator;
            NOTIFICATION_LIGHT_COLOR_AUTO_VALIDATOR = validator76;
            Validator validator77 = LineageSettings.sBooleanValidator;
            AUTO_BRIGHTNESS_ONE_SHOT_VALIDATOR = validator77;
            Validator validator78 = LineageSettings.sBooleanValidator;
            NOTIFICATION_LIGHT_PULSE_OVERRIDE_VALIDATOR = validator78;
            Validator validator79 = LineageSettings.sBooleanValidator;
            HEADSET_CONNECT_PLAYER_VALIDATOR = validator79;
            Validator validator80 = LineageSettings.sBooleanValidator;
            TOUCHSCREEN_GESTURE_HAPTIC_FEEDBACK_VALIDATOR = validator80;
            Validator validator81 = new Validator() { // from class: lineageos.providers.LineageSettings.System.4
            };
            DISPLAY_PICTURE_ADJUSTMENT_VALIDATOR = validator81;
            Validator validator82 = LineageSettings.sAlwaysTrueValidator;
            LONG_SCREEN_APPS_VALIDATOR = validator82;
            Validator validator83 = LineageSettings.sBooleanValidator;
            FORCE_SHOW_NAVBAR_VALIDATOR = validator83;
            Validator validator84 = LineageSettings.sBooleanValidator;
            CLICK_PARTIAL_SCREENSHOT_VALIDATOR = validator84;
            Validator validator85 = LineageSettings.sAlwaysTrueValidator;
            __MAGICAL_TEST_PASSING_ENABLER_VALIDATOR = validator85;
            LEGACY_SYSTEM_SETTINGS = new String[]{"nav_buttons", "key_home_long_press_action", "key_home_double_tap_action", "key_back_long_press_action", "key_back_double_tap_action", "back_wake_screen", "menu_wake_screen", "volume_wake_screen", "key_menu_action", "key_menu_long_press_action", "key_assist_action", "key_assist_long_press_action", "key_app_switch_action", "key_app_switch_long_press_action", "home_wake_screen", "assist_wake_screen", "app_switch_wake_screen", "camera_wake_screen", "camera_sleep_on_release", "camera_launch", "stylus_icon_enabled", "swap_volume_keys_on_rotation", "battery_light_enabled", "battery_light_pulse", "battery_light_low_color", "battery_light_medium_color", "battery_light_full_color", "battery_light_really_full_color", "enable_mwi_notification", "proximity_on_wake", "enable_forward_lookup", "enable_people_lookup", "enable_reverse_lookup", "forward_lookup_provider", "people_lookup_provider", "reverse_lookup_provider", "dialer_opencnam_account_sid", "dialer_opencnam_auth_token", "display_temperature_day", "display_temperature_night", "display_temperature_mode", "display_auto_outdoor_mode", "display_anti_flicker", "display_reading_mode", "display_low_power", "display_color_enhance", "display_color_adjustment", "live_display_hinted", "double_tap_sleep_gesture", "status_bar_show_weather", "recents_show_search_bar", "navigation_bar_left", "t9_search_input_locale", "bluetooth_accept_all_files", "lockscreen_scramble_pin_layout", "show_alarm_icon", "status_bar_ime_switcher", "qs_show_brightness_slider", "status_bar_brightness_control", "volbtn_music_controls", "edge_service_for_gestures", "status_bar_notif_count", "call_recording_format", "notification_light_brightness_level", "notification_light_screen_on_enable", "notification_light_pulse_default_color", "notification_light_pulse_default_led_on", "notification_light_pulse_default_led_off", "notification_light_pulse_call_color", "notification_light_pulse_call_led_on", "notification_light_pulse_call_led_off", "notification_light_pulse_vmail_color", "notification_light_pulse_vmail_led_on", "notification_light_pulse_vmail_led_off", "notification_light_pulse_custom_enable", "notification_light_pulse_custom_values", "qs_quick_pulldown", "volume_adjust_sounds_enabled", "system_profiles_enabled", "increasing_ring", "increasing_ring_start_vol", "increasing_ring_ramp_up_time", "status_bar_clock", "status_bar_am_pm", "status_bar_battery_style", "status_bar_show_battery_percent", "navigation_bar_menu_arrow_keys", "headset_connect_player", "allow_lights", "touchscreen_gesture_haptic_feedback"};
            ArrayMap arrayMap = new ArrayMap();
            VALIDATORS = arrayMap;
            arrayMap.put("notification_play_queue", validator);
            arrayMap.put("high_touch_sensitivity_enable", validator2);
            arrayMap.put("system_profiles_enabled", validator3);
            arrayMap.put("status_bar_clock", inclusiveIntegerRangeValidator);
            arrayMap.put("status_bar_am_pm", inclusiveIntegerRangeValidator3);
            arrayMap.put("status_bar_battery_style", inclusiveIntegerRangeValidator4);
            arrayMap.put("status_bar_show_battery_percent", inclusiveIntegerRangeValidator5);
            arrayMap.put("increasing_ring", validator6);
            arrayMap.put("increasing_ring_start_vol", inclusiveFloatRangeValidator);
            arrayMap.put("increasing_ring_ramp_up_time", inclusiveIntegerRangeValidator6);
            arrayMap.put("volume_adjust_sounds_enabled", validator7);
            arrayMap.put("nav_buttons", delimitedListValidator);
            arrayMap.put("navigation_bar_menu_arrow_keys", validator8);
            arrayMap.put("navigation_bar_hint", validator9);
            arrayMap.put("key_home_long_press_action", inclusiveIntegerRangeValidator7);
            arrayMap.put("key_home_double_tap_action", inclusiveIntegerRangeValidator8);
            arrayMap.put("key_back_long_press_action", inclusiveIntegerRangeValidator10);
            arrayMap.put("key_back_double_tap_action", inclusiveIntegerRangeValidator9);
            arrayMap.put("back_wake_screen", validator10);
            arrayMap.put("menu_wake_screen", validator11);
            arrayMap.put("volume_answer_call", validator12);
            arrayMap.put("volume_wake_screen", validator13);
            arrayMap.put("key_menu_action", inclusiveIntegerRangeValidator11);
            arrayMap.put("key_menu_long_press_action", inclusiveIntegerRangeValidator12);
            arrayMap.put("key_assist_action", inclusiveIntegerRangeValidator13);
            arrayMap.put("key_assist_long_press_action", inclusiveIntegerRangeValidator14);
            arrayMap.put("key_app_switch_action", inclusiveIntegerRangeValidator15);
            arrayMap.put("key_app_switch_long_press_action", inclusiveIntegerRangeValidator16);
            arrayMap.put("key_edge_long_swipe_action", inclusiveIntegerRangeValidator17);
            arrayMap.put("home_wake_screen", validator14);
            arrayMap.put("assist_wake_screen", validator15);
            arrayMap.put("app_switch_wake_screen", validator16);
            arrayMap.put("camera_wake_screen", validator17);
            arrayMap.put("camera_sleep_on_release", validator18);
            arrayMap.put("camera_launch", validator19);
            arrayMap.put("stylus_icon_enabled", validator20);
            arrayMap.put("swap_volume_keys_on_rotation", inclusiveIntegerRangeValidator18);
            arrayMap.put("torch_long_press_power_gesture", validator21);
            arrayMap.put("torch_long_press_power_timeout", inclusiveIntegerRangeValidator19);
            arrayMap.put("button_backlight_only_when_pressed", validator22);
            arrayMap.put("battery_light_enabled", validator23);
            arrayMap.put("battery_light_pulse", validator24);
            arrayMap.put("battery_light_low_color", validator25);
            arrayMap.put("battery_light_medium_color", validator26);
            arrayMap.put("battery_light_full_color", validator27);
            arrayMap.put("battery_light_really_full_color", validator27);
            arrayMap.put("enable_mwi_notification", validator28);
            arrayMap.put("proximity_on_wake", validator29);
            arrayMap.put("berry_global_style", inclusiveIntegerRangeValidator20);
            arrayMap.put("berry_current_accent", validator30);
            arrayMap.put("berry_dark_overlay", validator31);
            arrayMap.put("berry_managed_by_app", validator32);
            arrayMap.put("berry_black_theme", validator33);
            arrayMap.put("enable_forward_lookup", validator34);
            arrayMap.put("enable_people_lookup", validator35);
            arrayMap.put("enable_reverse_lookup", validator36);
            arrayMap.put("forward_lookup_provider", validator37);
            arrayMap.put("people_lookup_provider", validator38);
            arrayMap.put("reverse_lookup_provider", validator39);
            arrayMap.put("dialer_opencnam_account_sid", validator40);
            arrayMap.put("dialer_opencnam_auth_token", validator41);
            arrayMap.put("display_temperature_day", inclusiveIntegerRangeValidator21);
            arrayMap.put("display_temperature_night", inclusiveIntegerRangeValidator22);
            arrayMap.put("display_temperature_mode", inclusiveIntegerRangeValidator23);
            arrayMap.put("display_auto_contrast", validator47);
            arrayMap.put("display_auto_outdoor_mode", validator42);
            arrayMap.put("display_anti_flicker", validator43);
            arrayMap.put("display_reading_mode", validator44);
            arrayMap.put("display_low_power", validator45);
            arrayMap.put("display_color_enhance", validator46);
            arrayMap.put("display_color_adjustment", validator48);
            arrayMap.put("live_display_hinted", inclusiveIntegerRangeValidator24);
            arrayMap.put("trust_interface_hinted", validator49);
            arrayMap.put("double_tap_sleep_gesture", validator50);
            arrayMap.put("status_bar_show_weather", validator51);
            arrayMap.put("recents_show_search_bar", validator52);
            arrayMap.put("navigation_bar_left", validator53);
            arrayMap.put("t9_search_input_locale", validator54);
            arrayMap.put("bluetooth_accept_all_files", validator55);
            arrayMap.put("lockscreen_scramble_pin_layout", validator56);
            arrayMap.put("lockscreen_rotation", validator57);
            arrayMap.put("show_alarm_icon", validator58);
            arrayMap.put("status_bar_ime_switcher", validator59);
            arrayMap.put("qs_quick_pulldown", inclusiveIntegerRangeValidator25);
            arrayMap.put("qs_show_brightness_slider", inclusiveIntegerRangeValidator26);
            arrayMap.put("status_bar_brightness_control", validator60);
            arrayMap.put("volbtn_music_controls", validator61);
            arrayMap.put("edge_service_for_gestures", validator62);
            arrayMap.put("status_bar_notif_count", validator63);
            arrayMap.put("call_recording_format", inclusiveIntegerRangeValidator27);
            arrayMap.put("battery_light_brightness_level", inclusiveIntegerRangeValidator28);
            arrayMap.put("battery_light_brightness_level_zen", inclusiveIntegerRangeValidator28);
            arrayMap.put("notification_light_brightness_level", inclusiveIntegerRangeValidator29);
            arrayMap.put("notification_light_brightness_level_zen", inclusiveIntegerRangeValidator29);
            arrayMap.put("notification_light_screen_on_enable", validator64);
            arrayMap.put("notification_light_pulse_default_color", validator65);
            arrayMap.put("notification_light_pulse_default_led_on", validator66);
            arrayMap.put("notification_light_pulse_default_led_off", validator67);
            arrayMap.put("notification_light_pulse_call_color", validator68);
            arrayMap.put("notification_light_pulse_call_led_on", validator69);
            arrayMap.put("notification_light_pulse_call_led_off", validator70);
            arrayMap.put("notification_light_pulse_vmail_color", validator71);
            arrayMap.put("notification_light_pulse_vmail_led_on", validator72);
            arrayMap.put("notification_light_pulse_vmail_led_off", validator73);
            arrayMap.put("notification_light_pulse_custom_enable", validator74);
            arrayMap.put("notification_light_pulse_custom_values", validator75);
            arrayMap.put("notification_light_color_auto", validator76);
            arrayMap.put("auto_brightness_one_shot", validator77);
            arrayMap.put("notification_light_pulse_override", validator78);
            arrayMap.put("headset_connect_player", validator79);
            arrayMap.put("allow_lights", validator4);
            arrayMap.put("zen_priority_allow_lights", validator5);
            arrayMap.put("zen_priority_vibration_mode", inclusiveIntegerRangeValidator2);
            arrayMap.put("touchscreen_gesture_haptic_feedback", validator80);
            arrayMap.put("display_picture_adjustment", validator81);
            arrayMap.put("long_screen_apps", validator82);
            arrayMap.put("force_show_navbar", validator83);
            arrayMap.put("click_partial_screenshot", validator84);
            arrayMap.put("___magical_test_passing_enabler", validator85);
        }

        public static Uri getUriFor(String str) {
            return Settings.NameValueTable.getUriFor(CONTENT_URI, str);
        }

        public static String getString(ContentResolver contentResolver, String str) {
            return getStringForUser(contentResolver, str, UserHandle.myUserId());
        }

        public static String getString(ContentResolver contentResolver, String str, String str2) {
            String stringForUser = getStringForUser(contentResolver, str, UserHandle.myUserId());
            return stringForUser == null ? str2 : stringForUser;
        }

        public static String getStringForUser(ContentResolver contentResolver, String str, int i) {
            if (MOVED_TO_SECURE.contains(str)) {
                Log.w("LineageSettings", "Setting " + str + " has moved from LineageSettings.System to LineageSettings.Secure, value is unchanged.");
                return Secure.getStringForUser(contentResolver, str, i);
            }
            return sNameValueCache.getStringForUser(contentResolver, str, i);
        }

        public static boolean putString(ContentResolver contentResolver, String str, String str2) {
            return putStringForUser(contentResolver, str, str2, UserHandle.myUserId());
        }

        public static boolean putStringForUser(ContentResolver contentResolver, String str, String str2, int i) {
            if (MOVED_TO_SECURE.contains(str)) {
                Log.w("LineageSettings", "Setting " + str + " has moved from LineageSettings.System to LineageSettings.Secure, value is unchanged.");
                return false;
            }
            return sNameValueCache.putStringForUser(contentResolver, str, str2, i);
        }

        public static int getInt(ContentResolver contentResolver, String str, int i) {
            return getIntForUser(contentResolver, str, i, UserHandle.myUserId());
        }

        public static int getIntForUser(ContentResolver contentResolver, String str, int i, int i2) {
            String stringForUser = getStringForUser(contentResolver, str, i2);
            if (stringForUser == null) {
                return i;
            }
            try {
                return Integer.parseInt(stringForUser);
            } catch (NumberFormatException unused) {
                return i;
            }
        }

        public static boolean putInt(ContentResolver contentResolver, String str, int i) {
            return putIntForUser(contentResolver, str, i, UserHandle.myUserId());
        }

        public static boolean putIntForUser(ContentResolver contentResolver, String str, int i, int i2) {
            return putStringForUser(contentResolver, str, Integer.toString(i), i2);
        }
    }

    public static final class Secure extends Settings.NameValueTable {
        public static final Uri CONTENT_URI;
        public static final Validator GESTURE_BACK_EXCLUDE_TOP_VALIDATOR;
        public static final String[] LEGACY_SECURE_SETTINGS;
        protected static final ArraySet<String> MOVED_TO_GLOBAL;
        public static final String[] NAVIGATION_RING_TARGETS;
        public static final Validator NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD_VALIDATOR;
        public static final Validator NETWORK_TRAFFIC_AUTOHIDE_VALIDATOR;
        public static final Validator NETWORK_TRAFFIC_MODE_VALIDATOR;
        public static final Validator NETWORK_TRAFFIC_SHOW_UNITS_VALIDATOR;
        public static final Validator NETWORK_TRAFFIC_UNITS_VALIDATOR;
        public static final Validator PROTECTED_COMPONENTS_MANAGER_VALIDATOR;
        public static final Validator PROTECTED_COMPONENTS_VALIDATOR;
        public static final Validator TETHERING_ALLOW_VPN_UPSTREAMS_VALIDATOR;

        @Deprecated
        public static final Validator TRUST_NOTIFICATIONS_VALIDATOR;
        public static final Validator TRUST_RESTRICT_USB_KEYGUARD_VALIDATOR;
        public static final Validator TRUST_WARNINGS_VALIDATOR;
        public static final Validator USER_ACTIVITY_END_TIME_VALIDATOR;
        public static final Map<String, Validator> VALIDATORS;
        public static final Validator VOLUME_PANEL_ON_LEFT_VALIDATOR;
        private static final NameValueCache sNameValueCache;

        static {
            Uri uri = Uri.parse("content://lineagesettings/secure");
            CONTENT_URI = uri;
            sNameValueCache = new NameValueCache("sys.lineage_settings_secure_version", uri, "GET_secure", "PUT_secure");
            MOVED_TO_GLOBAL = new ArraySet<>(1);
            Validator validator = LineageSettings.sNonNegativeLongValidator;
            USER_ACTIVITY_END_TIME_VALIDATOR = validator;
            String[] strArr = {"navigation_ring_targets_0", "navigation_ring_targets_1", "navigation_ring_targets_2"};
            NAVIGATION_RING_TARGETS = strArr;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator = new InclusiveIntegerRangeValidator(0, 50);
            GESTURE_BACK_EXCLUDE_TOP_VALIDATOR = inclusiveIntegerRangeValidator;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator2 = new InclusiveIntegerRangeValidator(0, 2);
            NETWORK_TRAFFIC_MODE_VALIDATOR = inclusiveIntegerRangeValidator2;
            Validator validator2 = LineageSettings.sBooleanValidator;
            NETWORK_TRAFFIC_AUTOHIDE_VALIDATOR = validator2;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator3 = new InclusiveIntegerRangeValidator(0, 10000);
            NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD_VALIDATOR = inclusiveIntegerRangeValidator3;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator4 = new InclusiveIntegerRangeValidator(0, 3);
            NETWORK_TRAFFIC_UNITS_VALIDATOR = inclusiveIntegerRangeValidator4;
            Validator validator3 = LineageSettings.sBooleanValidator;
            NETWORK_TRAFFIC_SHOW_UNITS_VALIDATOR = validator3;
            Validator validator4 = LineageSettings.sBooleanValidator;
            TRUST_NOTIFICATIONS_VALIDATOR = validator4;
            Validator validator5 = LineageSettings.sBooleanValidator;
            TRUST_RESTRICT_USB_KEYGUARD_VALIDATOR = validator5;
            InclusiveIntegerRangeValidator inclusiveIntegerRangeValidator5 = new InclusiveIntegerRangeValidator(0, 5);
            TRUST_WARNINGS_VALIDATOR = inclusiveIntegerRangeValidator5;
            Validator validator6 = LineageSettings.sBooleanValidator;
            VOLUME_PANEL_ON_LEFT_VALIDATOR = validator6;
            Validator validator7 = LineageSettings.sBooleanValidator;
            TETHERING_ALLOW_VPN_UPSTREAMS_VALIDATOR = validator7;
            LEGACY_SECURE_SETTINGS = new String[]{"advanced_mode", "button_backlight_timeout", "button_brightness", "dev_force_show_navbar", "keyboard_brightness", "power_menu_actions", "stats_collection", "qs_show_brightness_slider", strArr[0], strArr[1], strArr[2], "recents_long_press_activity", "adb_notify", "adb_port", "device_hostname", "kill_app_longpress_back", "protected_components", "live_display_color_matrix", "advanced_reboot", "lockscreen_target_actions", "ring_home_button_behavior", "privacy_guard_default", "privacy_guard_notification", "development_shortcut", "performance_profile", "app_perf_profiles_enabled", "qs_location_advanced", "lockscreen_visualizer", "lock_screen_pass_to_security_view"};
            Validator validator8 = new Validator() { // from class: lineageos.providers.LineageSettings.Secure.1
                private final String mDelimiter = "|";
            };
            PROTECTED_COMPONENTS_VALIDATOR = validator8;
            Validator validator9 = new Validator() { // from class: lineageos.providers.LineageSettings.Secure.2
                private final String mDelimiter = "|";
            };
            PROTECTED_COMPONENTS_MANAGER_VALIDATOR = validator9;
            ArrayMap arrayMap = new ArrayMap();
            VALIDATORS = arrayMap;
            arrayMap.put("gesture_back_exclude_top", inclusiveIntegerRangeValidator);
            arrayMap.put("protected_components", validator8);
            arrayMap.put("protected_component_managers", validator9);
            arrayMap.put("network_traffic_mode", inclusiveIntegerRangeValidator2);
            arrayMap.put("network_traffic_autohide", validator2);
            arrayMap.put("network_traffic_autohide_threshold", inclusiveIntegerRangeValidator3);
            arrayMap.put("network_traffic_units", inclusiveIntegerRangeValidator4);
            arrayMap.put("network_traffic_show_units", validator3);
            arrayMap.put("tethering_allow_vpn_upstreams", validator7);
            arrayMap.put("trust_notifications", validator4);
            arrayMap.put("trust_restrict_usb", validator5);
            arrayMap.put("trust_warnings", inclusiveIntegerRangeValidator5);
            arrayMap.put("volume_panel_on_left", validator6);
            arrayMap.put("user_activity_end_time", validator);
        }

        public static Uri getUriFor(String str) {
            return Settings.NameValueTable.getUriFor(CONTENT_URI, str);
        }

        public static String getString(ContentResolver contentResolver, String str) {
            return getStringForUser(contentResolver, str, UserHandle.myUserId());
        }

        public static String getString(ContentResolver contentResolver, String str, String str2) {
            String stringForUser = getStringForUser(contentResolver, str, UserHandle.myUserId());
            return stringForUser == null ? str2 : stringForUser;
        }

        public static String getStringForUser(ContentResolver contentResolver, String str, int i) {
            if (MOVED_TO_GLOBAL.contains(str)) {
                Log.w("LineageSettings", "Setting " + str + " has moved from LineageSettings.Secure to LineageSettings.Global, value is unchanged.");
                return Global.getStringForUser(contentResolver, str, i);
            }
            return sNameValueCache.getStringForUser(contentResolver, str, i);
        }

        public static boolean putString(ContentResolver contentResolver, String str, String str2) {
            return putStringForUser(contentResolver, str, str2, UserHandle.myUserId());
        }

        public static boolean putStringForUser(ContentResolver contentResolver, String str, String str2, int i) {
            if (MOVED_TO_GLOBAL.contains(str)) {
                Log.w("LineageSettings", "Setting " + str + " has moved from LineageSettings.Secure to LineageSettings.Global, value is unchanged.");
                return false;
            }
            return sNameValueCache.putStringForUser(contentResolver, str, str2, i);
        }

        public static int getInt(ContentResolver contentResolver, String str, int i) {
            return getIntForUser(contentResolver, str, i, UserHandle.myUserId());
        }

        public static int getIntForUser(ContentResolver contentResolver, String str, int i, int i2) {
            String stringForUser = getStringForUser(contentResolver, str, i2);
            if (stringForUser == null) {
                return i;
            }
            try {
                return Integer.parseInt(stringForUser);
            } catch (NumberFormatException unused) {
                return i;
            }
        }

        public static boolean putInt(ContentResolver contentResolver, String str, int i) {
            return putIntForUser(contentResolver, str, i, UserHandle.myUserId());
        }

        public static boolean putIntForUser(ContentResolver contentResolver, String str, int i, int i2) {
            return putStringForUser(contentResolver, str, Integer.toString(i), i2);
        }
    }

    public static final class Global extends Settings.NameValueTable {
        public static final Uri CONTENT_URI;
        public static final String[] LEGACY_GLOBAL_SETTINGS;
        private static final NameValueCache sNameValueCache;

        static {
            Uri uri = Uri.parse("content://lineagesettings/global");
            CONTENT_URI = uri;
            sNameValueCache = new NameValueCache("sys.lineage_settings_global_version", uri, "GET_global", "PUT_global");
            LEGACY_GLOBAL_SETTINGS = new String[]{"wake_when_plugged_or_unplugged", "power_notifications_vibrate", "power_notifications_ringtone", "zen_disable_ducking_during_media_playback", "wifi_auto_priority"};
        }

        public static Uri getUriFor(String str) {
            return Settings.NameValueTable.getUriFor(CONTENT_URI, str);
        }

        public static String getString(ContentResolver contentResolver, String str) {
            return getStringForUser(contentResolver, str, UserHandle.myUserId());
        }

        public static String getStringForUser(ContentResolver contentResolver, String str, int i) {
            return sNameValueCache.getStringForUser(contentResolver, str, i);
        }

        public static boolean putString(ContentResolver contentResolver, String str, String str2) {
            return putStringForUser(contentResolver, str, str2, UserHandle.myUserId());
        }

        public static boolean putStringForUser(ContentResolver contentResolver, String str, String str2, int i) {
            return sNameValueCache.putStringForUser(contentResolver, str, str2, i);
        }

        public static int getInt(ContentResolver contentResolver, String str, int i) {
            return getIntForUser(contentResolver, str, i, UserHandle.myUserId());
        }

        public static int getIntForUser(ContentResolver contentResolver, String str, int i, int i2) {
            String stringForUser = getStringForUser(contentResolver, str, i2);
            if (stringForUser == null) {
                return i;
            }
            try {
                return Integer.parseInt(stringForUser);
            } catch (NumberFormatException unused) {
                return i;
            }
        }

        public static boolean putInt(ContentResolver contentResolver, String str, int i) {
            return putIntForUser(contentResolver, str, i, UserHandle.myUserId());
        }

        public static boolean putIntForUser(ContentResolver contentResolver, String str, int i, int i2) {
            return putStringForUser(contentResolver, str, Integer.toString(i), i2);
        }
    }
}
