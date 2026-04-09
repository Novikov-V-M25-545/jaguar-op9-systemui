package com.android.systemui.volume;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.media.IAudioService;
import android.media.IVolumeController;
import android.media.VolumePolicy;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.service.notification.Condition;
import android.service.notification.ZenModeConfig;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityManager;
import androidx.lifecycle.Observer;
import com.android.internal.annotations.GuardedBy;
import com.android.settingslib.volume.MediaSessions;
import com.android.systemui.Dumpable;
import com.android.systemui.R;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.plugins.SensorManagerPlugin;
import com.android.systemui.plugins.VolumeDialogController;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.qs.tiles.DndTile;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.util.RingerModeLiveData;
import com.android.systemui.util.RingerModeTracker;
import com.android.systemui.volume.VolumeDialogControllerImpl;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class VolumeDialogControllerImpl implements VolumeDialogController, Dumpable {
    static final ArrayMap<Integer, Integer> STREAMS;
    private boolean isResumable;
    private AudioManager mAudio;
    private IAudioService mAudioService;
    protected final BroadcastDispatcher mBroadcastDispatcher;
    protected C mCallbacks;
    private final Context mContext;
    private boolean mDestroyed;
    private final boolean mHasVibrator;
    private long mLastToggledRingerOn;
    private final MediaSessions mMediaSessions;
    protected final MediaSessionsCallbacks mMediaSessionsCallbacksW;
    private Handler mMediaStateHandler;
    private final NotificationManager mNoMan;
    private final NotificationManager mNotificationManager;
    private final SettingObserver mObserver;
    private final Receiver mReceiver;
    private final RingerModeObservers mRingerModeObservers;
    private boolean mShowA11yStream;
    private boolean mShowDndTile;
    private boolean mShowSafetyWarning;
    private boolean mShowVolumeDialog;
    private final VolumeDialogController.State mState;
    private final Optional<Lazy<StatusBar>> mStatusBarOptionalLazy;

    @GuardedBy({"this"})
    private UserActivityListener mUserActivityListener;
    private final Vibrator mVibrator;
    protected final VC mVolumeController;
    private VolumePolicy mVolumePolicy;
    private final W mWorker;
    private final HandlerThread mWorkerThread;
    private static final String TAG = Util.logTag(VolumeDialogControllerImpl.class);
    private static final AudioAttributes SONIFICIATION_VIBRATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();

    public interface UserActivityListener {
        void onUserActivity();
    }

    private static boolean isLogWorthy(int i) {
        return i == 0 || i == 1 || i == 2 || i == 3 || i == 4 || i == 6;
    }

    private static boolean isRinger(int i) {
        return i == 2 || i == 5;
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public boolean isCaptionStreamOptedOut() {
        return false;
    }

    static {
        ArrayMap<Integer, Integer> arrayMap = new ArrayMap<>();
        STREAMS = arrayMap;
        arrayMap.put(4, Integer.valueOf(R.string.stream_alarm));
        arrayMap.put(6, Integer.valueOf(R.string.stream_bluetooth_sco));
        arrayMap.put(8, Integer.valueOf(R.string.stream_dtmf));
        arrayMap.put(3, Integer.valueOf(R.string.stream_music));
        arrayMap.put(10, Integer.valueOf(R.string.stream_accessibility));
        arrayMap.put(5, Integer.valueOf(R.string.stream_notification));
        arrayMap.put(2, Integer.valueOf(R.string.stream_ring));
        arrayMap.put(1, Integer.valueOf(R.string.stream_system));
        arrayMap.put(7, Integer.valueOf(R.string.stream_system_enforced));
        arrayMap.put(9, Integer.valueOf(R.string.stream_tts));
        arrayMap.put(0, Integer.valueOf(R.string.stream_voice_call));
    }

    public VolumeDialogControllerImpl(Context context, BroadcastDispatcher broadcastDispatcher, Optional<Lazy<StatusBar>> optional, RingerModeTracker ringerModeTracker) {
        Receiver receiver = new Receiver();
        this.mReceiver = receiver;
        this.mCallbacks = new C();
        this.mState = new VolumeDialogController.State();
        MediaSessionsCallbacks mediaSessionsCallbacks = new MediaSessionsCallbacks();
        this.mMediaSessionsCallbacksW = mediaSessionsCallbacks;
        this.mShowDndTile = true;
        VC vc = new VC();
        this.mVolumeController = vc;
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        if (applicationContext.getPackageManager().hasSystemFeature("android.software.leanback")) {
            this.mStatusBarOptionalLazy = Optional.empty();
        } else {
            this.mStatusBarOptionalLazy = optional;
        }
        this.mNotificationManager = (NotificationManager) applicationContext.getSystemService("notification");
        Events.writeEvent(5, new Object[0]);
        HandlerThread handlerThread = new HandlerThread(VolumeDialogControllerImpl.class.getSimpleName());
        this.mWorkerThread = handlerThread;
        handlerThread.start();
        W w = new W(handlerThread.getLooper());
        this.mWorker = w;
        this.mMediaSessions = createMediaSessions(applicationContext, handlerThread.getLooper(), mediaSessionsCallbacks);
        this.mAudio = (AudioManager) applicationContext.getSystemService("audio");
        this.mNoMan = (NotificationManager) applicationContext.getSystemService("notification");
        SettingObserver settingObserver = new SettingObserver(w);
        this.mObserver = settingObserver;
        RingerModeObservers ringerModeObservers = new RingerModeObservers((RingerModeLiveData) ringerModeTracker.getRingerMode(), (RingerModeLiveData) ringerModeTracker.getRingerModeInternal());
        this.mRingerModeObservers = ringerModeObservers;
        ringerModeObservers.init();
        this.mBroadcastDispatcher = broadcastDispatcher;
        settingObserver.init();
        receiver.init();
        Vibrator vibrator = (Vibrator) applicationContext.getSystemService("vibrator");
        this.mVibrator = vibrator;
        this.mHasVibrator = vibrator != null && vibrator.hasVibrator();
        this.mAudioService = IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
        this.mMediaStateHandler = new Handler();
        vc.setA11yMode(((AccessibilityManager) context.getSystemService(AccessibilityManager.class)).isAccessibilityVolumeStreamActive() ? 1 : 0);
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public AudioManager getAudioManager() {
        return this.mAudio;
    }

    public void dismiss() {
        this.mCallbacks.onDismissRequested(2);
    }

    protected void setVolumeController() {
        try {
            this.mAudio.setVolumeController(this.mVolumeController);
        } catch (SecurityException e) {
            Log.w(TAG, "Unable to set the volume controller", e);
        }
    }

    protected void setAudioManagerStreamVolume(int i, int i2, int i3) {
        this.mAudio.setStreamVolume(i, i2, i3);
    }

    protected int getAudioManagerStreamVolume(int i) {
        return this.mAudio.getLastAudibleStreamVolume(i);
    }

    protected int getAudioManagerStreamMaxVolume(int i) {
        return this.mAudio.getStreamMaxVolume(i);
    }

    protected int getAudioManagerStreamMinVolume(int i) {
        return this.mAudio.getStreamMinVolumeInt(i);
    }

    public void register() {
        setVolumeController();
        setVolumePolicy(this.mVolumePolicy);
        showDndTile(this.mShowDndTile);
        try {
            this.mMediaSessions.init();
        } catch (SecurityException e) {
            Log.w(TAG, "No access to media sessions", e);
        }
    }

    public void setVolumePolicy(VolumePolicy volumePolicy) {
        this.mVolumePolicy = volumePolicy;
        if (volumePolicy == null) {
            return;
        }
        try {
            this.mAudio.setVolumePolicy(volumePolicy);
        } catch (NoSuchMethodError unused) {
            Log.w(TAG, "No volume policy api");
        }
    }

    protected MediaSessions createMediaSessions(Context context, Looper looper, MediaSessions.Callbacks callbacks) {
        return new MediaSessions(context, looper, callbacks);
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println(VolumeDialogControllerImpl.class.getSimpleName() + " state:");
        printWriter.print("  mDestroyed: ");
        printWriter.println(this.mDestroyed);
        printWriter.print("  mVolumePolicy: ");
        printWriter.println(this.mVolumePolicy);
        printWriter.print("  mState: ");
        printWriter.println(this.mState.toString(4));
        printWriter.print("  mShowDndTile: ");
        printWriter.println(this.mShowDndTile);
        printWriter.print("  mHasVibrator: ");
        printWriter.println(this.mHasVibrator);
        synchronized (this.mMediaSessionsCallbacksW.mRemoteStreams) {
            printWriter.print("  mRemoteStreams: ");
            printWriter.println(this.mMediaSessionsCallbacksW.mRemoteStreams.values());
        }
        printWriter.print("  mShowA11yStream: ");
        printWriter.println(this.mShowA11yStream);
        printWriter.println();
        this.mMediaSessions.dump(printWriter);
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public void addCallback(VolumeDialogController.Callbacks callbacks, Handler handler) {
        this.mCallbacks.add(callbacks, handler);
        callbacks.onAccessibilityModeChanged(Boolean.valueOf(this.mShowA11yStream));
    }

    public void setUserActivityListener(UserActivityListener userActivityListener) {
        if (this.mDestroyed) {
            return;
        }
        synchronized (this) {
            this.mUserActivityListener = userActivityListener;
        }
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public void removeCallback(VolumeDialogController.Callbacks callbacks) {
        this.mCallbacks.remove(callbacks);
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public void getState() {
        if (this.mDestroyed) {
            return;
        }
        this.mWorker.sendEmptyMessage(3);
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public boolean areCaptionsEnabled() {
        return Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "odi_captions_enabled", 0, -2) == 1;
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public void setCaptionsEnabled(boolean z) {
        Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "odi_captions_enabled", z ? 1 : 0, -2);
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public void getCaptionsComponentState(boolean z) {
        if (this.mDestroyed) {
            return;
        }
        this.mWorker.obtainMessage(16, Boolean.valueOf(z)).sendToTarget();
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public void notifyVisible(boolean z) {
        if (this.mDestroyed) {
            return;
        }
        this.mWorker.obtainMessage(12, z ? 1 : 0, 0).sendToTarget();
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public void userActivity() {
        if (this.mDestroyed) {
            return;
        }
        this.mWorker.removeMessages(13);
        this.mWorker.sendEmptyMessage(13);
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public void setRingerMode(int i, boolean z) {
        if (this.mDestroyed) {
            return;
        }
        this.mWorker.obtainMessage(4, i, z ? 1 : 0).sendToTarget();
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public void setStreamVolume(int i, int i2) {
        if (this.mDestroyed) {
            return;
        }
        this.mWorker.obtainMessage(10, i, i2).sendToTarget();
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public void setActiveStream(int i) {
        if (this.mDestroyed) {
            return;
        }
        this.mWorker.obtainMessage(11, i, 0).sendToTarget();
    }

    public void setEnableDialogs(boolean z, boolean z2) {
        this.mShowVolumeDialog = z;
        this.mShowSafetyWarning = z2;
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public void scheduleTouchFeedback() {
        this.mLastToggledRingerOn = System.currentTimeMillis();
    }

    private void playTouchFeedback() {
        if (System.currentTimeMillis() - this.mLastToggledRingerOn < 1000) {
            try {
                this.mAudioService.playSoundEffect(5);
            } catch (RemoteException unused) {
            }
        }
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public void vibrate(VibrationEffect vibrationEffect) {
        if (this.mHasVibrator) {
            this.mVibrator.vibrate(vibrationEffect, SONIFICIATION_VIBRATION_ATTRIBUTES);
        }
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public boolean hasVibrator() {
        return this.mHasVibrator;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onNotifyVisibleW(boolean z) {
        if (this.mDestroyed) {
            return;
        }
        this.mAudio.notifyVolumeControllerVisible(this.mVolumeController, z);
        if (z || !updateActiveStreamW(-1)) {
            return;
        }
        this.mCallbacks.onStateChanged(this.mState);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onUserActivityW() {
        synchronized (this) {
            UserActivityListener userActivityListener = this.mUserActivityListener;
            if (userActivityListener != null) {
                userActivityListener.onUserActivity();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onShowSafetyWarningW(int i) {
        if (this.mShowSafetyWarning) {
            this.mCallbacks.onShowSafetyWarning(i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onGetCaptionsComponentStateW(boolean z) {
        try {
            String string = this.mContext.getString(android.R.string.config_credentialManagerReceiverComponent);
            if (TextUtils.isEmpty(string)) {
                this.mCallbacks.onCaptionComponentStateChanged(Boolean.FALSE, Boolean.valueOf(z));
                return;
            }
            if (D.BUG) {
                Log.i(TAG, String.format("isCaptionsServiceEnabled componentNameString=%s", string));
            }
            ComponentName componentNameUnflattenFromString = ComponentName.unflattenFromString(string);
            if (componentNameUnflattenFromString == null) {
                this.mCallbacks.onCaptionComponentStateChanged(Boolean.FALSE, Boolean.valueOf(z));
            } else {
                this.mCallbacks.onCaptionComponentStateChanged(Boolean.valueOf(this.mContext.getPackageManager().getComponentEnabledSetting(componentNameUnflattenFromString) == 1), Boolean.valueOf(z));
            }
        } catch (Exception unused) {
            if (D.BUG) {
                Log.i(TAG, "isCaptionsServiceEnabled failed to check for captions component");
            }
            this.mCallbacks.onCaptionComponentStateChanged(Boolean.FALSE, Boolean.valueOf(z));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onAccessibilityModeChanged(Boolean bool) {
        this.mCallbacks.onAccessibilityModeChanged(bool);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean checkRoutedToBluetoothW(int i) {
        if (i == 3) {
            return false | updateStreamRoutedToBluetoothW(i, (this.mAudio.getDevicesForStream(3) & 896) != 0);
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean shouldShowUI(final int i) {
        return ((Boolean) this.mStatusBarOptionalLazy.map(new Function() { // from class: com.android.systemui.volume.VolumeDialogControllerImpl$$ExternalSyntheticLambda1
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return this.f$0.lambda$shouldShowUI$0(i, (Lazy) obj);
            }
        }).orElse(Boolean.valueOf(this.mShowVolumeDialog && (i & 1) != 0))).booleanValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ Boolean lambda$shouldShowUI$0(int i, Lazy lazy) {
        StatusBar statusBar = (StatusBar) lazy.get();
        return Boolean.valueOf((statusBar.getWakefulnessState() == 0 || statusBar.getWakefulnessState() == 3 || !statusBar.isDeviceInteractive() || (i & 1) == 0 || !this.mShowVolumeDialog) ? false : true);
    }

    boolean onVolumeChangedW(int i, int i2) {
        boolean zShouldShowUI = shouldShowUI(i2);
        boolean z = (i2 & LineageHardwareManager.FEATURE_AUTO_CONTRAST) != 0;
        boolean z2 = (i2 & LineageHardwareManager.FEATURE_TOUCH_HOVERING) != 0;
        boolean z3 = (i2 & 128) != 0;
        boolean zUpdateActiveStreamW = zShouldShowUI ? updateActiveStreamW(i) | false : false;
        int audioManagerStreamVolume = getAudioManagerStreamVolume(i);
        boolean zUpdateStreamLevelW = zUpdateActiveStreamW | updateStreamLevelW(i, audioManagerStreamVolume) | checkRoutedToBluetoothW(zShouldShowUI ? 3 : i);
        if (zUpdateStreamLevelW) {
            this.mCallbacks.onStateChanged(this.mState);
        }
        if (zShouldShowUI) {
            this.mCallbacks.onShowRequested(1);
        }
        if (z2) {
            this.mCallbacks.onShowVibrateHint();
        }
        if (z3) {
            this.mCallbacks.onShowSilentHint();
        }
        if (zUpdateStreamLevelW && z) {
            Events.writeEvent(4, Integer.valueOf(i), Integer.valueOf(audioManagerStreamVolume));
        }
        return zUpdateStreamLevelW;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean updateActiveStreamW(int i) {
        VolumeDialogController.State state = this.mState;
        if (i == state.activeStream) {
            return false;
        }
        state.activeStream = i;
        Events.writeEvent(2, Integer.valueOf(i));
        if (D.BUG) {
            Log.d(TAG, "updateActiveStreamW " + i);
        }
        if (i >= 100) {
            i = -1;
        }
        if (D.BUG) {
            Log.d(TAG, "forceVolumeControlStream " + i);
        }
        this.mAudio.forceVolumeControlStream(i);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public VolumeDialogController.StreamState streamStateW(int i) {
        VolumeDialogController.StreamState streamState = this.mState.states.get(i);
        if (streamState != null) {
            return streamState;
        }
        VolumeDialogController.StreamState streamState2 = new VolumeDialogController.StreamState();
        this.mState.states.put(i, streamState2);
        return streamState2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onGetStateW() {
        Iterator<Integer> it = STREAMS.keySet().iterator();
        while (it.hasNext()) {
            int iIntValue = it.next().intValue();
            updateStreamLevelW(iIntValue, getAudioManagerStreamVolume(iIntValue));
            streamStateW(iIntValue).levelMin = getAudioManagerStreamMinVolume(iIntValue);
            streamStateW(iIntValue).levelMax = Math.max(1, getAudioManagerStreamMaxVolume(iIntValue));
            updateStreamMuteW(iIntValue, this.mAudio.isStreamMute(iIntValue));
            VolumeDialogController.StreamState streamStateStreamStateW = streamStateW(iIntValue);
            streamStateStreamStateW.muteSupported = this.mAudio.isStreamAffectedByMute(iIntValue);
            streamStateStreamStateW.name = STREAMS.get(Integer.valueOf(iIntValue)).intValue();
            checkRoutedToBluetoothW(iIntValue);
        }
        updateRingerModeExternalW(this.mRingerModeObservers.mRingerMode.getValue().intValue());
        updateZenModeW();
        updateZenConfig();
        updateEffectsSuppressorW(this.mNoMan.getEffectsSuppressor());
        this.mCallbacks.onStateChanged(this.mState);
    }

    private boolean updateStreamRoutedToBluetoothW(int i, boolean z) {
        VolumeDialogController.StreamState streamStateStreamStateW = streamStateW(i);
        if (streamStateStreamStateW.routedToBluetooth == z) {
            return false;
        }
        streamStateStreamStateW.routedToBluetooth = z;
        if (!D.BUG) {
            return true;
        }
        Log.d(TAG, "updateStreamRoutedToBluetoothW stream=" + i + " routedToBluetooth=" + z);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean updateStreamLevelW(int i, int i2) {
        VolumeDialogController.StreamState streamStateStreamStateW = streamStateW(i);
        if (streamStateStreamStateW.level == i2) {
            return false;
        }
        streamStateStreamStateW.level = i2;
        if (i == 3 && i2 == 0 && this.mAudio.isMusicActive()) {
            if (Settings.System.getIntForUser(this.mContext.getContentResolver(), "adaptive_playback_enabled", 0, -2) == 1) {
                int intForUser = Settings.System.getIntForUser(this.mContext.getContentResolver(), "adaptive_playback_timeout", 30, -2);
                this.mAudio.dispatchMediaKeyEvent(new KeyEvent(0, 127));
                this.mAudio.dispatchMediaKeyEvent(new KeyEvent(1, 127));
                this.isResumable = true;
                this.mMediaStateHandler.removeCallbacksAndMessages(null);
                this.mMediaStateHandler.postDelayed(new Runnable() { // from class: com.android.systemui.volume.VolumeDialogControllerImpl$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$updateStreamLevelW$1();
                    }
                }, intForUser * 1000);
            }
        }
        if (i == 3 && i2 > 0 && this.isResumable) {
            this.mMediaStateHandler.removeCallbacksAndMessages(null);
            this.mAudio.dispatchMediaKeyEvent(new KeyEvent(0, 126));
            this.mAudio.dispatchMediaKeyEvent(new KeyEvent(1, 126));
            this.isResumable = false;
        }
        if (isLogWorthy(i)) {
            Events.writeEvent(10, Integer.valueOf(i), Integer.valueOf(i2));
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateStreamLevelW$1() {
        this.isResumable = false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean updateStreamMuteW(int i, boolean z) {
        VolumeDialogController.StreamState streamStateStreamStateW = streamStateW(i);
        if (streamStateStreamStateW.muted == z) {
            return false;
        }
        streamStateStreamStateW.muted = z;
        if (isLogWorthy(i)) {
            Events.writeEvent(15, Integer.valueOf(i), Boolean.valueOf(z));
        }
        if (z && isRinger(i)) {
            updateRingerModeInternalW(this.mRingerModeObservers.mRingerModeInternal.getValue().intValue());
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean updateLinkNotificationConfigW() {
        boolean z = Settings.Secure.getInt(this.mContext.getContentResolver(), "volume_link_notification", 1) == 1;
        VolumeDialogController.State state = this.mState;
        if (state.linkedNotification == z) {
            return false;
        }
        state.linkedNotification = z;
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean updateEffectsSuppressorW(ComponentName componentName) {
        if (Objects.equals(this.mState.effectsSuppressor, componentName)) {
            return false;
        }
        VolumeDialogController.State state = this.mState;
        state.effectsSuppressor = componentName;
        state.effectsSuppressorName = getApplicationName(this.mContext, componentName);
        VolumeDialogController.State state2 = this.mState;
        Events.writeEvent(14, state2.effectsSuppressor, state2.effectsSuppressorName);
        return true;
    }

    private static String getApplicationName(Context context, ComponentName componentName) {
        String strTrim;
        if (componentName == null) {
            return null;
        }
        PackageManager packageManager = context.getPackageManager();
        String packageName = componentName.getPackageName();
        try {
            strTrim = Objects.toString(packageManager.getApplicationInfo(packageName, 0).loadLabel(packageManager), "").trim();
        } catch (PackageManager.NameNotFoundException unused) {
        }
        return strTrim.length() > 0 ? strTrim : packageName;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean updateZenModeW() {
        int i = Settings.Global.getInt(this.mContext.getContentResolver(), "zen_mode", 0);
        VolumeDialogController.State state = this.mState;
        if (state.zenMode == i) {
            return false;
        }
        state.zenMode = i;
        Events.writeEvent(13, Integer.valueOf(i));
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean updateZenConfig() {
        NotificationManager.Policy consolidatedNotificationPolicy = this.mNotificationManager.getConsolidatedNotificationPolicy();
        int i = consolidatedNotificationPolicy.priorityCategories;
        boolean z = (i & 32) == 0;
        boolean z2 = (i & 64) == 0;
        boolean z3 = (i & 128) == 0;
        boolean zAreAllPriorityOnlyRingerSoundsMuted = ZenModeConfig.areAllPriorityOnlyRingerSoundsMuted(consolidatedNotificationPolicy);
        VolumeDialogController.State state = this.mState;
        if (state.disallowAlarms == z && state.disallowMedia == z2 && state.disallowRinger == zAreAllPriorityOnlyRingerSoundsMuted && state.disallowSystem == z3) {
            return false;
        }
        state.disallowAlarms = z;
        state.disallowMedia = z2;
        state.disallowSystem = z3;
        state.disallowRinger = zAreAllPriorityOnlyRingerSoundsMuted;
        Events.writeEvent(17, "disallowAlarms=" + z + " disallowMedia=" + z2 + " disallowSystem=" + z3 + " disallowRinger=" + zAreAllPriorityOnlyRingerSoundsMuted);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean updateRingerModeExternalW(int i) {
        VolumeDialogController.State state = this.mState;
        if (i == state.ringerModeExternal) {
            return false;
        }
        state.ringerModeExternal = i;
        Events.writeEvent(12, Integer.valueOf(i));
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean updateRingerModeInternalW(int i) {
        VolumeDialogController.State state = this.mState;
        if (i == state.ringerModeInternal) {
            return false;
        }
        state.ringerModeInternal = i;
        Events.writeEvent(11, Integer.valueOf(i));
        if (this.mState.ringerModeInternal == 2) {
            playTouchFeedback();
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSetRingerModeW(int i, boolean z) {
        if (z) {
            this.mAudio.setRingerMode(i);
        } else {
            this.mAudio.setRingerModeInternal(i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSetStreamMuteW(int i, boolean z) {
        this.mAudio.adjustStreamVolume(i, z ? -100 : 100, 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSetStreamVolumeW(int i, int i2) {
        if (D.BUG) {
            Log.d(TAG, "onSetStreamVolume " + i + " level=" + i2);
        }
        if (i >= 100) {
            this.mMediaSessionsCallbacksW.setStreamVolume(i, i2);
        } else {
            setAudioManagerStreamVolume(i, i2, 0);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSetActiveStreamW(int i) {
        if (updateActiveStreamW(i)) {
            this.mCallbacks.onStateChanged(this.mState);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSetExitConditionW(Condition condition) {
        this.mNoMan.setZenMode(this.mState.zenMode, condition != null ? condition.id : null, TAG);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSetZenModeW(int i) {
        if (D.BUG) {
            Log.d(TAG, "onSetZenModeW " + i);
        }
        this.mNoMan.setZenMode(i, null, TAG);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onDismissRequestedW(int i) {
        this.mCallbacks.onDismissRequested(i);
    }

    public void showDndTile(boolean z) {
        if (D.BUG) {
            Log.d(TAG, "showDndTile");
        }
        DndTile.setVisible(this.mContext, z);
    }

    private final class VC extends IVolumeController.Stub {
        private final String TAG;

        private VC() {
            this.TAG = VolumeDialogControllerImpl.TAG + ".VC";
        }

        public void displaySafeVolumeWarning(int i) throws RemoteException {
            if (D.BUG) {
                Log.d(this.TAG, "displaySafeVolumeWarning " + com.android.settingslib.volume.Util.audioManagerFlagsToString(i));
            }
            if (VolumeDialogControllerImpl.this.mDestroyed) {
                return;
            }
            VolumeDialogControllerImpl.this.mWorker.obtainMessage(14, i, 0).sendToTarget();
        }

        public void volumeChanged(int i, int i2) throws RemoteException {
            if (D.BUG) {
                Log.d(this.TAG, "volumeChanged " + AudioSystem.streamToString(i) + " " + com.android.settingslib.volume.Util.audioManagerFlagsToString(i2));
            }
            if (VolumeDialogControllerImpl.this.mDestroyed) {
                return;
            }
            VolumeDialogControllerImpl.this.mWorker.obtainMessage(1, i, i2).sendToTarget();
        }

        public void masterMuteChanged(int i) throws RemoteException {
            if (D.BUG) {
                Log.d(this.TAG, "masterMuteChanged");
            }
        }

        public void setLayoutDirection(int i) throws RemoteException {
            if (D.BUG) {
                Log.d(this.TAG, "setLayoutDirection");
            }
            if (VolumeDialogControllerImpl.this.mDestroyed) {
                return;
            }
            VolumeDialogControllerImpl.this.mWorker.obtainMessage(8, i, 0).sendToTarget();
        }

        public void dismiss() throws RemoteException {
            if (D.BUG) {
                Log.d(this.TAG, "dismiss requested");
            }
            if (VolumeDialogControllerImpl.this.mDestroyed) {
                return;
            }
            VolumeDialogControllerImpl.this.mWorker.obtainMessage(2, 2, 0).sendToTarget();
            VolumeDialogControllerImpl.this.mWorker.sendEmptyMessage(2);
        }

        public void setA11yMode(int i) {
            if (D.BUG) {
                Log.d(this.TAG, "setA11yMode to " + i);
            }
            if (VolumeDialogControllerImpl.this.mDestroyed) {
                return;
            }
            if (i == 0) {
                VolumeDialogControllerImpl.this.mShowA11yStream = false;
            } else if (i == 1) {
                VolumeDialogControllerImpl.this.mShowA11yStream = true;
            } else {
                Log.e(this.TAG, "Invalid accessibility mode " + i);
            }
            VolumeDialogControllerImpl.this.mWorker.obtainMessage(15, Boolean.valueOf(VolumeDialogControllerImpl.this.mShowA11yStream)).sendToTarget();
        }
    }

    private final class W extends Handler {
        W(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    VolumeDialogControllerImpl.this.onVolumeChangedW(message.arg1, message.arg2);
                    break;
                case 2:
                    VolumeDialogControllerImpl.this.onDismissRequestedW(message.arg1);
                    break;
                case SensorManagerPlugin.Sensor.TYPE_SWIPE /* 3 */:
                    VolumeDialogControllerImpl.this.onGetStateW();
                    break;
                case 4:
                    VolumeDialogControllerImpl.this.onSetRingerModeW(message.arg1, message.arg2 != 0);
                    break;
                case 5:
                    VolumeDialogControllerImpl.this.onSetZenModeW(message.arg1);
                    break;
                case 6:
                    VolumeDialogControllerImpl.this.onSetExitConditionW((Condition) message.obj);
                    break;
                case 7:
                    VolumeDialogControllerImpl.this.onSetStreamMuteW(message.arg1, message.arg2 != 0);
                    break;
                case QS.VERSION /* 8 */:
                    VolumeDialogControllerImpl.this.mCallbacks.onLayoutDirectionChanged(message.arg1);
                    break;
                case 9:
                    VolumeDialogControllerImpl.this.mCallbacks.onConfigurationChanged();
                    break;
                case 10:
                    VolumeDialogControllerImpl.this.onSetStreamVolumeW(message.arg1, message.arg2);
                    break;
                case 11:
                    VolumeDialogControllerImpl.this.onSetActiveStreamW(message.arg1);
                    break;
                case 12:
                    VolumeDialogControllerImpl.this.onNotifyVisibleW(message.arg1 != 0);
                    break;
                case 13:
                    VolumeDialogControllerImpl.this.onUserActivityW();
                    break;
                case 14:
                    VolumeDialogControllerImpl.this.onShowSafetyWarningW(message.arg1);
                    break;
                case 15:
                    VolumeDialogControllerImpl.this.onAccessibilityModeChanged((Boolean) message.obj);
                    break;
                case LineageHardwareManager.FEATURE_HIGH_TOUCH_SENSITIVITY /* 16 */:
                    VolumeDialogControllerImpl.this.onGetCaptionsComponentStateW(((Boolean) message.obj).booleanValue());
                    break;
            }
        }
    }

    class C implements VolumeDialogController.Callbacks {
        private final HashMap<VolumeDialogController.Callbacks, Handler> mCallbackMap = new HashMap<>();

        C() {
        }

        public void add(VolumeDialogController.Callbacks callbacks, Handler handler) {
            if (callbacks == null || handler == null) {
                throw new IllegalArgumentException();
            }
            this.mCallbackMap.put(callbacks, handler);
        }

        public void remove(VolumeDialogController.Callbacks callbacks) {
            this.mCallbackMap.remove(callbacks);
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onShowRequested(final int i) {
            for (final Map.Entry<VolumeDialogController.Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                entry.getValue().post(new Runnable() { // from class: com.android.systemui.volume.VolumeDialogControllerImpl.C.1
                    @Override // java.lang.Runnable
                    public void run() {
                        ((VolumeDialogController.Callbacks) entry.getKey()).onShowRequested(i);
                    }
                });
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onDismissRequested(final int i) {
            for (final Map.Entry<VolumeDialogController.Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                entry.getValue().post(new Runnable() { // from class: com.android.systemui.volume.VolumeDialogControllerImpl.C.2
                    @Override // java.lang.Runnable
                    public void run() {
                        ((VolumeDialogController.Callbacks) entry.getKey()).onDismissRequested(i);
                    }
                });
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onStateChanged(VolumeDialogController.State state) {
            long jCurrentTimeMillis = System.currentTimeMillis();
            final VolumeDialogController.State stateCopy = state.copy();
            for (final Map.Entry<VolumeDialogController.Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                entry.getValue().post(new Runnable() { // from class: com.android.systemui.volume.VolumeDialogControllerImpl.C.3
                    @Override // java.lang.Runnable
                    public void run() {
                        ((VolumeDialogController.Callbacks) entry.getKey()).onStateChanged(stateCopy);
                    }
                });
            }
            Events.writeState(jCurrentTimeMillis, stateCopy);
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onLayoutDirectionChanged(final int i) {
            for (final Map.Entry<VolumeDialogController.Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                entry.getValue().post(new Runnable() { // from class: com.android.systemui.volume.VolumeDialogControllerImpl.C.4
                    @Override // java.lang.Runnable
                    public void run() {
                        ((VolumeDialogController.Callbacks) entry.getKey()).onLayoutDirectionChanged(i);
                    }
                });
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onConfigurationChanged() {
            for (final Map.Entry<VolumeDialogController.Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                entry.getValue().post(new Runnable() { // from class: com.android.systemui.volume.VolumeDialogControllerImpl.C.5
                    @Override // java.lang.Runnable
                    public void run() {
                        ((VolumeDialogController.Callbacks) entry.getKey()).onConfigurationChanged();
                    }
                });
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onShowVibrateHint() {
            for (final Map.Entry<VolumeDialogController.Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                entry.getValue().post(new Runnable() { // from class: com.android.systemui.volume.VolumeDialogControllerImpl.C.6
                    @Override // java.lang.Runnable
                    public void run() {
                        ((VolumeDialogController.Callbacks) entry.getKey()).onShowVibrateHint();
                    }
                });
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onShowSilentHint() {
            for (final Map.Entry<VolumeDialogController.Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                entry.getValue().post(new Runnable() { // from class: com.android.systemui.volume.VolumeDialogControllerImpl.C.7
                    @Override // java.lang.Runnable
                    public void run() {
                        ((VolumeDialogController.Callbacks) entry.getKey()).onShowSilentHint();
                    }
                });
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onScreenOff() {
            for (final Map.Entry<VolumeDialogController.Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                entry.getValue().post(new Runnable() { // from class: com.android.systemui.volume.VolumeDialogControllerImpl.C.8
                    @Override // java.lang.Runnable
                    public void run() {
                        ((VolumeDialogController.Callbacks) entry.getKey()).onScreenOff();
                    }
                });
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onShowSafetyWarning(final int i) {
            for (final Map.Entry<VolumeDialogController.Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                entry.getValue().post(new Runnable() { // from class: com.android.systemui.volume.VolumeDialogControllerImpl.C.9
                    @Override // java.lang.Runnable
                    public void run() {
                        ((VolumeDialogController.Callbacks) entry.getKey()).onShowSafetyWarning(i);
                    }
                });
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onAccessibilityModeChanged(Boolean bool) {
            final boolean zBooleanValue = bool == null ? false : bool.booleanValue();
            for (final Map.Entry<VolumeDialogController.Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                entry.getValue().post(new Runnable() { // from class: com.android.systemui.volume.VolumeDialogControllerImpl.C.10
                    @Override // java.lang.Runnable
                    public void run() {
                        ((VolumeDialogController.Callbacks) entry.getKey()).onAccessibilityModeChanged(Boolean.valueOf(zBooleanValue));
                    }
                });
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onCaptionComponentStateChanged(Boolean bool, final Boolean bool2) {
            final boolean zBooleanValue = bool == null ? false : bool.booleanValue();
            for (final Map.Entry<VolumeDialogController.Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                entry.getValue().post(new Runnable() { // from class: com.android.systemui.volume.VolumeDialogControllerImpl$C$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        VolumeDialogControllerImpl.C.lambda$onCaptionComponentStateChanged$0(entry, zBooleanValue, bool2);
                    }
                });
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static /* synthetic */ void lambda$onCaptionComponentStateChanged$0(Map.Entry entry, boolean z, Boolean bool) {
            ((VolumeDialogController.Callbacks) entry.getKey()).onCaptionComponentStateChanged(Boolean.valueOf(z), bool);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    final class RingerModeObservers {
        private final RingerModeLiveData mRingerMode;
        private final RingerModeLiveData mRingerModeInternal;
        private final Observer<Integer> mRingerModeObserver = new AnonymousClass1();
        private final Observer<Integer> mRingerModeInternalObserver = new AnonymousClass2();

        /* renamed from: com.android.systemui.volume.VolumeDialogControllerImpl$RingerModeObservers$1, reason: invalid class name */
        class AnonymousClass1 implements Observer<Integer> {
            AnonymousClass1() {
            }

            @Override // androidx.lifecycle.Observer
            public void onChanged(final Integer num) {
                VolumeDialogControllerImpl.this.mWorker.post(new Runnable() { // from class: com.android.systemui.volume.VolumeDialogControllerImpl$RingerModeObservers$1$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onChanged$0(num);
                    }
                });
            }

            /* JADX INFO: Access modifiers changed from: private */
            public /* synthetic */ void lambda$onChanged$0(Integer num) {
                int iIntValue = num.intValue();
                if (RingerModeObservers.this.mRingerMode.getInitialSticky()) {
                    VolumeDialogControllerImpl.this.mState.ringerModeExternal = iIntValue;
                }
                if (D.BUG) {
                    Log.d(VolumeDialogControllerImpl.TAG, "onChange ringer_mode rm=" + Util.ringerModeToString(iIntValue));
                }
                if (VolumeDialogControllerImpl.this.updateRingerModeExternalW(iIntValue)) {
                    VolumeDialogControllerImpl volumeDialogControllerImpl = VolumeDialogControllerImpl.this;
                    volumeDialogControllerImpl.mCallbacks.onStateChanged(volumeDialogControllerImpl.mState);
                }
            }
        }

        /* renamed from: com.android.systemui.volume.VolumeDialogControllerImpl$RingerModeObservers$2, reason: invalid class name */
        class AnonymousClass2 implements Observer<Integer> {
            AnonymousClass2() {
            }

            @Override // androidx.lifecycle.Observer
            public void onChanged(final Integer num) {
                VolumeDialogControllerImpl.this.mWorker.post(new Runnable() { // from class: com.android.systemui.volume.VolumeDialogControllerImpl$RingerModeObservers$2$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onChanged$0(num);
                    }
                });
            }

            /* JADX INFO: Access modifiers changed from: private */
            public /* synthetic */ void lambda$onChanged$0(Integer num) {
                int iIntValue = num.intValue();
                if (RingerModeObservers.this.mRingerModeInternal.getInitialSticky()) {
                    VolumeDialogControllerImpl.this.mState.ringerModeInternal = iIntValue;
                }
                if (D.BUG) {
                    Log.d(VolumeDialogControllerImpl.TAG, "onChange internal_ringer_mode rm=" + Util.ringerModeToString(iIntValue));
                }
                if (VolumeDialogControllerImpl.this.updateRingerModeInternalW(iIntValue)) {
                    VolumeDialogControllerImpl volumeDialogControllerImpl = VolumeDialogControllerImpl.this;
                    volumeDialogControllerImpl.mCallbacks.onStateChanged(volumeDialogControllerImpl.mState);
                }
            }
        }

        RingerModeObservers(RingerModeLiveData ringerModeLiveData, RingerModeLiveData ringerModeLiveData2) {
            this.mRingerMode = ringerModeLiveData;
            this.mRingerModeInternal = ringerModeLiveData2;
        }

        public void init() {
            int iIntValue = this.mRingerMode.getValue().intValue();
            if (iIntValue != -1) {
                VolumeDialogControllerImpl.this.mState.ringerModeExternal = iIntValue;
            }
            this.mRingerMode.observeForever(this.mRingerModeObserver);
            int iIntValue2 = this.mRingerModeInternal.getValue().intValue();
            if (iIntValue2 != -1) {
                VolumeDialogControllerImpl.this.mState.ringerModeInternal = iIntValue2;
            }
            this.mRingerModeInternal.observeForever(this.mRingerModeInternalObserver);
        }
    }

    private final class SettingObserver extends ContentObserver {
        private final Uri VOLUME_LINK_NOTIFICATION_URI;
        private final Uri ZEN_MODE_CONFIG_URI;
        private final Uri ZEN_MODE_URI;

        public SettingObserver(Handler handler) {
            super(handler);
            this.ZEN_MODE_URI = Settings.Global.getUriFor("zen_mode");
            this.ZEN_MODE_CONFIG_URI = Settings.Global.getUriFor("zen_mode_config_etag");
            this.VOLUME_LINK_NOTIFICATION_URI = Settings.Secure.getUriFor("volume_link_notification");
        }

        public void init() {
            VolumeDialogControllerImpl.this.mContext.getContentResolver().registerContentObserver(this.ZEN_MODE_URI, false, this);
            VolumeDialogControllerImpl.this.mContext.getContentResolver().registerContentObserver(this.ZEN_MODE_CONFIG_URI, false, this);
            VolumeDialogControllerImpl.this.mContext.getContentResolver().registerContentObserver(this.VOLUME_LINK_NOTIFICATION_URI, false, this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            boolean zUpdateZenModeW = this.ZEN_MODE_URI.equals(uri) ? VolumeDialogControllerImpl.this.updateZenModeW() : false;
            if (this.ZEN_MODE_CONFIG_URI.equals(uri)) {
                zUpdateZenModeW |= VolumeDialogControllerImpl.this.updateZenConfig();
            }
            if (this.VOLUME_LINK_NOTIFICATION_URI.equals(uri)) {
                zUpdateZenModeW = VolumeDialogControllerImpl.this.updateLinkNotificationConfigW();
            }
            if (zUpdateZenModeW) {
                VolumeDialogControllerImpl volumeDialogControllerImpl = VolumeDialogControllerImpl.this;
                volumeDialogControllerImpl.mCallbacks.onStateChanged(volumeDialogControllerImpl.mState);
            }
        }
    }

    private final class Receiver extends BroadcastReceiver {
        private Receiver() {
        }

        public void init() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
            intentFilter.addAction("android.media.STREAM_DEVICES_CHANGED_ACTION");
            intentFilter.addAction("android.media.STREAM_MUTE_CHANGED_ACTION");
            intentFilter.addAction("android.media.VOLUME_STEPS_CHANGED_ACTION");
            intentFilter.addAction("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED");
            intentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            intentFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
            VolumeDialogControllerImpl volumeDialogControllerImpl = VolumeDialogControllerImpl.this;
            volumeDialogControllerImpl.mBroadcastDispatcher.registerReceiverWithHandler(this, intentFilter, volumeDialogControllerImpl.mWorker);
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean zUpdateEffectsSuppressorW = false;
            if (action.equals("android.media.VOLUME_CHANGED_ACTION")) {
                int intExtra = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
                int intExtra2 = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", -1);
                int intExtra3 = intent.getIntExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", -1);
                if (D.BUG) {
                    Log.d(VolumeDialogControllerImpl.TAG, "onReceive VOLUME_CHANGED_ACTION stream=" + intExtra + " level=" + intExtra2 + " oldLevel=" + intExtra3);
                }
                zUpdateEffectsSuppressorW = VolumeDialogControllerImpl.this.updateStreamLevelW(intExtra, intExtra2);
            } else if (action.equals("android.media.STREAM_DEVICES_CHANGED_ACTION")) {
                int intExtra4 = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
                int intExtra5 = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_DEVICES", -1);
                int intExtra6 = intent.getIntExtra("android.media.EXTRA_PREV_VOLUME_STREAM_DEVICES", -1);
                if (D.BUG) {
                    Log.d(VolumeDialogControllerImpl.TAG, "onReceive STREAM_DEVICES_CHANGED_ACTION stream=" + intExtra4 + " devices=" + intExtra5 + " oldDevices=" + intExtra6);
                }
                zUpdateEffectsSuppressorW = VolumeDialogControllerImpl.this.checkRoutedToBluetoothW(intExtra4) | VolumeDialogControllerImpl.this.onVolumeChangedW(intExtra4, 0);
            } else if (action.equals("android.media.STREAM_MUTE_CHANGED_ACTION")) {
                int intExtra7 = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
                boolean booleanExtra = intent.getBooleanExtra("android.media.EXTRA_STREAM_VOLUME_MUTED", false);
                if (D.BUG) {
                    Log.d(VolumeDialogControllerImpl.TAG, "onReceive STREAM_MUTE_CHANGED_ACTION stream=" + intExtra7 + " muted=" + booleanExtra);
                }
                zUpdateEffectsSuppressorW = VolumeDialogControllerImpl.this.updateStreamMuteW(intExtra7, booleanExtra);
            } else if (action.equals("android.media.VOLUME_STEPS_CHANGED_ACTION")) {
                VolumeDialogControllerImpl.this.getState();
            } else if (action.equals("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED")) {
                if (D.BUG) {
                    Log.d(VolumeDialogControllerImpl.TAG, "onReceive ACTION_EFFECTS_SUPPRESSOR_CHANGED");
                }
                VolumeDialogControllerImpl volumeDialogControllerImpl = VolumeDialogControllerImpl.this;
                zUpdateEffectsSuppressorW = volumeDialogControllerImpl.updateEffectsSuppressorW(volumeDialogControllerImpl.mNoMan.getEffectsSuppressor());
            } else if (action.equals("android.intent.action.CONFIGURATION_CHANGED")) {
                if (D.BUG) {
                    Log.d(VolumeDialogControllerImpl.TAG, "onReceive ACTION_CONFIGURATION_CHANGED");
                }
                VolumeDialogControllerImpl.this.mCallbacks.onConfigurationChanged();
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                if (D.BUG) {
                    Log.d(VolumeDialogControllerImpl.TAG, "onReceive ACTION_SCREEN_OFF");
                }
                VolumeDialogControllerImpl.this.mCallbacks.onScreenOff();
            } else if (action.equals("android.intent.action.CLOSE_SYSTEM_DIALOGS")) {
                if (D.BUG) {
                    Log.d(VolumeDialogControllerImpl.TAG, "onReceive ACTION_CLOSE_SYSTEM_DIALOGS");
                }
                VolumeDialogControllerImpl.this.dismiss();
            }
            if (zUpdateEffectsSuppressorW) {
                VolumeDialogControllerImpl volumeDialogControllerImpl2 = VolumeDialogControllerImpl.this;
                volumeDialogControllerImpl2.mCallbacks.onStateChanged(volumeDialogControllerImpl2.mState);
            }
        }
    }

    protected final class MediaSessionsCallbacks implements MediaSessions.Callbacks {
        private final HashMap<MediaSession.Token, Integer> mRemoteStreams = new HashMap<>();
        private int mNextStream = 100;

        protected MediaSessionsCallbacks() {
        }

        @Override // com.android.settingslib.volume.MediaSessions.Callbacks
        public void onRemoteUpdate(MediaSession.Token token, String str, MediaController.PlaybackInfo playbackInfo) {
            int iIntValue;
            addStream(token, "onRemoteUpdate");
            synchronized (this.mRemoteStreams) {
                iIntValue = this.mRemoteStreams.get(token).intValue();
            }
            Slog.d(VolumeDialogControllerImpl.TAG, "onRemoteUpdate: stream: " + iIntValue + " volume: " + playbackInfo.getCurrentVolume());
            boolean z = true;
            boolean z2 = VolumeDialogControllerImpl.this.mState.states.indexOfKey(iIntValue) < 0;
            VolumeDialogController.StreamState streamStateStreamStateW = VolumeDialogControllerImpl.this.streamStateW(iIntValue);
            streamStateStreamStateW.dynamic = true;
            streamStateStreamStateW.levelMin = 0;
            streamStateStreamStateW.levelMax = playbackInfo.getMaxVolume();
            if (streamStateStreamStateW.level != playbackInfo.getCurrentVolume()) {
                streamStateStreamStateW.level = playbackInfo.getCurrentVolume();
                z2 = true;
            }
            if (Objects.equals(streamStateStreamStateW.remoteLabel, str)) {
                z = z2;
            } else {
                streamStateStreamStateW.name = -1;
                streamStateStreamStateW.remoteLabel = str;
            }
            if (z) {
                Log.d(VolumeDialogControllerImpl.TAG, "onRemoteUpdate: " + str + ": " + streamStateStreamStateW.level + " of " + streamStateStreamStateW.levelMax);
                VolumeDialogControllerImpl volumeDialogControllerImpl = VolumeDialogControllerImpl.this;
                volumeDialogControllerImpl.mCallbacks.onStateChanged(volumeDialogControllerImpl.mState);
            }
        }

        @Override // com.android.settingslib.volume.MediaSessions.Callbacks
        public void onRemoteVolumeChanged(MediaSession.Token token, int i) {
            int iIntValue;
            addStream(token, "onRemoteVolumeChanged");
            synchronized (this.mRemoteStreams) {
                iIntValue = this.mRemoteStreams.get(token).intValue();
            }
            boolean zShouldShowUI = VolumeDialogControllerImpl.this.shouldShowUI(i);
            Slog.d(VolumeDialogControllerImpl.TAG, "onRemoteVolumeChanged: stream: " + iIntValue + " showui? " + zShouldShowUI);
            boolean zUpdateActiveStreamW = VolumeDialogControllerImpl.this.updateActiveStreamW(iIntValue);
            if (zShouldShowUI) {
                zUpdateActiveStreamW |= VolumeDialogControllerImpl.this.checkRoutedToBluetoothW(3);
            }
            if (zUpdateActiveStreamW) {
                Slog.d(VolumeDialogControllerImpl.TAG, "onRemoteChanged: updatingState");
                VolumeDialogControllerImpl volumeDialogControllerImpl = VolumeDialogControllerImpl.this;
                volumeDialogControllerImpl.mCallbacks.onStateChanged(volumeDialogControllerImpl.mState);
            }
            if (zShouldShowUI) {
                VolumeDialogControllerImpl.this.mCallbacks.onShowRequested(2);
            }
        }

        @Override // com.android.settingslib.volume.MediaSessions.Callbacks
        public void onRemoteRemoved(MediaSession.Token token) {
            synchronized (this.mRemoteStreams) {
                if (!this.mRemoteStreams.containsKey(token)) {
                    Log.d(VolumeDialogControllerImpl.TAG, "onRemoteRemoved: stream doesn't exist, aborting remote removed for token:" + token.toString());
                    return;
                }
                int iIntValue = this.mRemoteStreams.get(token).intValue();
                VolumeDialogControllerImpl.this.mState.states.remove(iIntValue);
                if (VolumeDialogControllerImpl.this.mState.activeStream == iIntValue) {
                    VolumeDialogControllerImpl.this.updateActiveStreamW(-1);
                }
                VolumeDialogControllerImpl volumeDialogControllerImpl = VolumeDialogControllerImpl.this;
                volumeDialogControllerImpl.mCallbacks.onStateChanged(volumeDialogControllerImpl.mState);
            }
        }

        public void setStreamVolume(int i, int i2) {
            MediaSession.Token tokenFindToken = findToken(i);
            if (tokenFindToken == null) {
                Log.w(VolumeDialogControllerImpl.TAG, "setStreamVolume: No token found for stream: " + i);
                return;
            }
            VolumeDialogControllerImpl.this.mMediaSessions.setVolume(tokenFindToken, i2);
        }

        private MediaSession.Token findToken(int i) {
            synchronized (this.mRemoteStreams) {
                for (Map.Entry<MediaSession.Token, Integer> entry : this.mRemoteStreams.entrySet()) {
                    if (entry.getValue().equals(Integer.valueOf(i))) {
                        return entry.getKey();
                    }
                }
                return null;
            }
        }

        private void addStream(MediaSession.Token token, String str) {
            synchronized (this.mRemoteStreams) {
                if (!this.mRemoteStreams.containsKey(token)) {
                    this.mRemoteStreams.put(token, Integer.valueOf(this.mNextStream));
                    Log.d(VolumeDialogControllerImpl.TAG, str + ": added stream " + this.mNextStream + " from token + " + token.toString());
                    this.mNextStream = this.mNextStream + 1;
                }
            }
        }
    }
}
