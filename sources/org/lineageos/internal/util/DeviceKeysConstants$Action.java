package org.lineageos.internal.util;

/* loaded from: classes2.dex */
public enum DeviceKeysConstants$Action {
    NOTHING,
    MENU,
    APP_SWITCH,
    SEARCH,
    VOICE_SEARCH,
    IN_APP_SEARCH,
    LAUNCH_CAMERA,
    SLEEP,
    LAST_APP,
    SPLIT_SCREEN,
    CLOSE_APP,
    TORCH,
    SCREENSHOT,
    VOLUME_PANEL,
    CLEAR_ALL_NOTIFICATIONS,
    NOTIFICATIONS,
    QS_PANEL,
    RINGER_MODES,
    SINGLE_HAND_LEFT,
    SINGLE_HAND_RIGHT;

    public static DeviceKeysConstants$Action fromIntSafe(int i) {
        DeviceKeysConstants$Action deviceKeysConstants$Action = NOTHING;
        return (i < deviceKeysConstants$Action.ordinal() || i > values().length) ? deviceKeysConstants$Action : values()[i];
    }
}
