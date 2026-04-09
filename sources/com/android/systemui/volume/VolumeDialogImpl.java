package com.android.systemui.volume;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.media.AppTrackData;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.provider.Settings;
import android.text.InputFilter;
import android.util.Log;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settingslib.Utils;
import com.android.systemui.Dependency;
import com.android.systemui.Prefs;
import com.android.systemui.R;
import com.android.systemui.media.dialog.MediaOutputDialogFactory;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.SensorManagerPlugin;
import com.android.systemui.plugins.VolumeDialog;
import com.android.systemui.plugins.VolumeDialogController;
import com.android.systemui.statusbar.phone.ExpandableIndicator;
import com.android.systemui.statusbar.policy.AccessibilityManagerWrapper;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.volume.CaptionsToggleImageButton;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class VolumeDialogImpl implements VolumeDialog, ConfigurationController.ConfigurationListener, TunerService.Tunable {
    private static final String TAG = Util.logTag(VolumeDialogImpl.class);
    private final AccessibilityManagerWrapper mAccessibilityMgr;
    private int mActiveStream;
    private final ActivityManager mActivityManager;
    private int mAllyStream;
    private final ColorFilter mAppIconMuteColorFilter;
    private ConfigurableTexts mConfigurableTexts;
    private final Context mContext;
    private final VolumeDialogController mController;
    private final DeviceProvisionedController mDeviceProvisionedController;
    private View mDialog;
    private ViewGroup mDialogMainView;
    private ViewGroup mDialogRowsView;
    private ViewGroup mDialogView;
    private ExpandableIndicator mExpandRows;
    private View mExpandRowsView;
    private boolean mExpanded;
    private boolean mHasSeenODICaptionsTooltip;
    private final KeyguardManager mKeyguard;
    private ImageButton mMediaOutputIcon;
    private View mMediaOutputView;
    private boolean mMusicHidden;
    private CaptionsToggleImageButton mODICaptionsIcon;
    private ViewStub mODICaptionsTooltipViewStub;
    private ViewGroup mODICaptionsView;
    private int mPrevActiveStream;
    private ViewGroup mRinger;
    private ImageButton mRingerIcon;
    private SafetyWarningDialog mSafetyWarning;
    private boolean mShowA11yStream;
    private boolean mShowActiveStreamOnly;
    private boolean mShowing;
    private VolumeDialogController.State mState;
    private int mTimeOut;
    private int mTimeOutDesired;
    private boolean mVolumePanelOnLeft;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    private FrameLayout mZenIcon;
    private final H mHandler = new H();
    private final List<VolumeRow> mRows = new ArrayList();
    private final List<VolumeRow> mAppRows = new ArrayList();
    private final SparseBooleanArray mDynamic = new SparseBooleanArray();
    private final Object mSafetyWarningLock = new Object();
    private final Accessibility mAccessibility = new Accessibility();
    private boolean mAutomute = true;
    private boolean mSilentMode = true;
    private boolean mHovering = false;
    private boolean mConfigChanged = false;
    private boolean mIsAnimatingDismiss = false;
    private View mODICaptionsTooltipView = null;
    private final VolumeDialogController.Callbacks mControllerCallbackH = new VolumeDialogController.Callbacks() { // from class: com.android.systemui.volume.VolumeDialogImpl.5
        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onShowRequested(int i) {
            VolumeDialogImpl.this.showH(i);
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onDismissRequested(int i) {
            VolumeDialogImpl.this.dismissH(i);
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onScreenOff() {
            VolumeDialogImpl.this.dismissH(4);
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onStateChanged(VolumeDialogController.State state) {
            VolumeDialogImpl.this.onStateChangedH(state);
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onLayoutDirectionChanged(int i) {
            VolumeDialogImpl.this.mDialogView.setLayoutDirection(i);
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onConfigurationChanged() {
            if (VolumeDialogImpl.this.mDialog.isShown()) {
                VolumeDialogImpl.this.mWindowManager.removeViewImmediate(VolumeDialogImpl.this.mDialog);
            }
            VolumeDialogImpl.this.mConfigChanged = true;
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onShowVibrateHint() {
            if (VolumeDialogImpl.this.mSilentMode) {
                VolumeDialogImpl.this.mController.setRingerMode(0, false);
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onShowSilentHint() {
            if (VolumeDialogImpl.this.mSilentMode) {
                VolumeDialogImpl.this.mController.setRingerMode(2, false);
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onShowSafetyWarning(int i) {
            VolumeDialogImpl.this.showSafetyWarningH(i);
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onAccessibilityModeChanged(Boolean bool) {
            VolumeDialogImpl.this.mShowA11yStream = bool == null ? false : bool.booleanValue();
            VolumeRow activeRow = VolumeDialogImpl.this.getActiveRow();
            if (VolumeDialogImpl.this.mShowA11yStream || 10 != activeRow.stream) {
                VolumeDialogImpl.this.updateRowsH(activeRow);
            } else {
                VolumeDialogImpl.this.dismissH(7);
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onCaptionComponentStateChanged(Boolean bool, Boolean bool2) {
            VolumeDialogImpl.this.updateODICaptionsH(bool.booleanValue(), bool2.booleanValue());
        }
    };

    public VolumeDialogImpl(Context context) {
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, R.style.qs_theme);
        this.mContext = contextThemeWrapper;
        this.mController = (VolumeDialogController) Dependency.get(VolumeDialogController.class);
        this.mKeyguard = (KeyguardManager) contextThemeWrapper.getSystemService("keyguard");
        this.mActivityManager = (ActivityManager) contextThemeWrapper.getSystemService("activity");
        this.mWindowManager = (WindowManager) contextThemeWrapper.getSystemService("window");
        this.mAccessibilityMgr = (AccessibilityManagerWrapper) Dependency.get(AccessibilityManagerWrapper.class);
        this.mDeviceProvisionedController = (DeviceProvisionedController) Dependency.get(DeviceProvisionedController.class);
        this.mShowActiveStreamOnly = showActiveStreamOnly();
        this.mHasSeenODICaptionsTooltip = Prefs.getBoolean(context, "HasSeenODICaptionsTooltip", false);
        TunerService tunerService = (TunerService) Dependency.get(TunerService.class);
        tunerService.addTunable(this, "lineagesecure:volume_panel_on_left");
        tunerService.addTunable(this, "system:audio_panel_view_timeout");
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0.0f);
        this.mAppIconMuteColorFilter = new ColorMatrixColorFilter(colorMatrix);
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onUiModeChanged() {
        this.mContext.getTheme().applyStyle(this.mContext.getThemeResId(), true);
    }

    @Override // com.android.systemui.plugins.VolumeDialog
    public void init(int i, VolumeDialog.Callback callback) {
        initDialog();
        this.mAccessibility.init();
        this.mController.addCallback(this.mControllerCallbackH, this.mHandler);
        this.mController.getState();
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    @Override // com.android.systemui.plugins.VolumeDialog
    public void destroy() {
        this.mController.removeCallback(this.mControllerCallbackH);
        this.mHandler.removeCallbacksAndMessages(null);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
    }

    private void initDialog() {
        int i = this.mVolumePanelOnLeft ? 3 : 5;
        this.mConfigurableTexts = new ConfigurableTexts(this.mContext);
        this.mHovering = false;
        this.mShowing = false;
        this.mExpanded = false;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        this.mWindowParams = layoutParams;
        int i2 = layoutParams.flags & (-3);
        layoutParams.flags = i2;
        int i3 = i2 & (-65537);
        layoutParams.flags = i3;
        layoutParams.flags = i3 | android.R.interpolator.launch_task_micro_alpha;
        layoutParams.type = 2020;
        layoutParams.format = -3;
        layoutParams.windowAnimations = -1;
        View viewInflate = LayoutInflater.from(this.mContext).inflate(R.layout.volume_dialog, (ViewGroup) null, false);
        this.mDialog = viewInflate;
        viewInflate.setOnTouchListener(new View.OnTouchListener() { // from class: com.android.systemui.volume.VolumeDialogImpl$$ExternalSyntheticLambda7
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return this.f$0.lambda$initDialog$0(view, motionEvent);
            }
        });
        ViewGroup viewGroup = (ViewGroup) this.mDialog.findViewById(R.id.volume_dialog);
        this.mDialogView = viewGroup;
        viewGroup.setAlpha(0.0f);
        this.mDialogView.setLayoutDirection(!this.mVolumePanelOnLeft ? 1 : 0);
        this.mDialogView.setOnHoverListener(new View.OnHoverListener() { // from class: com.android.systemui.volume.VolumeDialogImpl$$ExternalSyntheticLambda6
            @Override // android.view.View.OnHoverListener
            public final boolean onHover(View view, MotionEvent motionEvent) {
                return this.f$0.lambda$initDialog$1(view, motionEvent);
            }
        });
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) this.mDialogView.getLayoutParams();
        layoutParams2.gravity = 16;
        this.mDialogView.setLayoutParams(layoutParams2);
        ViewGroup viewGroup2 = (ViewGroup) this.mDialog.findViewById(R.id.main);
        this.mDialogMainView = viewGroup2;
        if (viewGroup2 != null) {
            setLayoutGravity(viewGroup2.getLayoutParams(), i);
        }
        this.mDialogRowsView = (ViewGroup) this.mDialog.findViewById(R.id.volume_dialog_rows);
        ViewGroup viewGroup3 = (ViewGroup) this.mDialog.findViewById(R.id.ringer);
        this.mRinger = viewGroup3;
        if (viewGroup3 != null) {
            this.mRingerIcon = (ImageButton) viewGroup3.findViewById(R.id.ringer_icon);
            this.mZenIcon = (FrameLayout) this.mRinger.findViewById(R.id.dnd_icon);
            setLayoutGravity(this.mRinger.getLayoutParams(), i);
        }
        ViewGroup viewGroup4 = (ViewGroup) this.mDialog.findViewById(R.id.odi_captions);
        this.mODICaptionsView = viewGroup4;
        if (viewGroup4 != null) {
            this.mODICaptionsIcon = (CaptionsToggleImageButton) viewGroup4.findViewById(R.id.odi_captions_icon);
            setLayoutGravity(this.mODICaptionsView.getLayoutParams(), i);
        }
        ViewStub viewStub = (ViewStub) this.mDialog.findViewById(R.id.odi_captions_tooltip_stub);
        this.mODICaptionsTooltipViewStub = viewStub;
        if (this.mHasSeenODICaptionsTooltip && viewStub != null) {
            this.mDialogView.removeView(viewStub);
            this.mODICaptionsTooltipViewStub = null;
        }
        this.mMediaOutputView = this.mDialog.findViewById(R.id.media_output_container);
        ImageButton imageButton = (ImageButton) this.mDialog.findViewById(R.id.media_output);
        this.mMediaOutputIcon = imageButton;
        if (imageButton != null) {
            setLayoutGravity(imageButton.getLayoutParams(), i);
        }
        this.mExpandRowsView = this.mDialog.findViewById(R.id.expandable_indicator_container);
        ExpandableIndicator expandableIndicator = (ExpandableIndicator) this.mDialog.findViewById(R.id.expandable_indicator);
        this.mExpandRows = expandableIndicator;
        if (expandableIndicator != null) {
            setLayoutGravity(expandableIndicator.getLayoutParams(), i);
            this.mExpandRows.setRotation(this.mVolumePanelOnLeft ? -90.0f : 90.0f);
        }
        if (this.mRows.isEmpty()) {
            if (!AudioSystem.isSingleVolume(this.mContext)) {
                int i4 = R.drawable.ic_volume_accessibility;
                addRow(10, i4, i4, true, false);
            }
            addRow(3, R.drawable.ic_volume_media, R.drawable.ic_volume_media_mute, true, true);
            if (!AudioSystem.isSingleVolume(this.mContext)) {
                if (com.android.settingslib.volume.Util.isVoiceCapable(this.mContext)) {
                    addRow(2, R.drawable.ic_volume_ringer, R.drawable.ic_volume_ringer_mute, true, false);
                } else {
                    addRow(2, R.drawable.ic_volume_notification, R.drawable.ic_volume_notification_mute, true, false);
                }
                addRow(4, R.drawable.ic_volume_alarm, R.drawable.ic_volume_alarm_mute, true, false);
                addRow(0, android.R.drawable.ic_menu_blocked_user, android.R.drawable.ic_menu_blocked_user, false, false);
                int i5 = R.drawable.ic_volume_bt_sco;
                addRow(6, i5, i5, false, false);
                addRow(1, R.drawable.ic_volume_system, R.drawable.ic_volume_system_mute, false, false);
            }
        } else {
            addExistingRows();
        }
        updateRowsH(getActiveRow());
        initRingerH();
        initSettingsH();
        initODICaptionsH();
        this.mAllyStream = -1;
        this.mMusicHidden = false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$initDialog$0(View view, MotionEvent motionEvent) {
        if (!this.mShowing) {
            return false;
        }
        int action = motionEvent.getAction();
        if (action != 0 && action != 4) {
            return false;
        }
        dismissH(1);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$initDialog$1(View view, MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        this.mHovering = actionMasked == 9 || actionMasked == 7;
        rescheduleTimeoutH();
        return true;
    }

    private void setLayoutGravity(Object obj, int i) {
        if (obj instanceof FrameLayout.LayoutParams) {
            ((FrameLayout.LayoutParams) obj).gravity = i;
        } else if (obj instanceof LinearLayout.LayoutParams) {
            ((LinearLayout.LayoutParams) obj).gravity = i;
        }
    }

    private float getAnimatorX() {
        float width = this.mDialogView.getWidth() / 2.0f;
        return this.mVolumePanelOnLeft ? -width : width;
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        boolean integerSwitch;
        str.hashCode();
        if (!str.equals("system:audio_panel_view_timeout")) {
            if (str.equals("lineagesecure:volume_panel_on_left") && this.mVolumePanelOnLeft != (integerSwitch = TunerService.parseIntegerSwitch(str2, isAudioPanelOnLeftSide()))) {
                this.mVolumePanelOnLeft = integerSwitch;
                this.mHandler.post(new Runnable() { // from class: com.android.systemui.volume.VolumeDialogImpl$$ExternalSyntheticLambda14
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onTuningChanged$2();
                    }
                });
                return;
            }
            return;
        }
        int integer = TunerService.parseInteger(str2, 3);
        this.mTimeOutDesired = integer;
        this.mTimeOut = integer * 1000;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onTuningChanged$2() {
        this.mControllerCallbackH.onConfigurationChanged();
    }

    private boolean isAudioPanelOnLeftSide() {
        return this.mContext.getResources().getBoolean(R.bool.config_audioPanelOnLeftSide);
    }

    private int getAlphaAttr(int i) {
        TypedArray typedArrayObtainStyledAttributes = this.mContext.obtainStyledAttributes(new int[]{i});
        float f = typedArrayObtainStyledAttributes.getFloat(0, 0.0f);
        typedArrayObtainStyledAttributes.recycle();
        return (int) (f * 255.0f);
    }

    private boolean isLandscape() {
        return this.mContext.getResources().getConfiguration().orientation == 2;
    }

    public void setStreamImportant(int i, boolean z) {
        this.mHandler.obtainMessage(5, i, z ? 1 : 0).sendToTarget();
    }

    public void setAutomute(boolean z) {
        if (this.mAutomute == z) {
            return;
        }
        this.mAutomute = z;
        this.mHandler.sendEmptyMessage(4);
    }

    public void setSilentMode(boolean z) {
        if (this.mSilentMode == z) {
            return;
        }
        this.mSilentMode = z;
        this.mHandler.sendEmptyMessage(4);
    }

    private void addRow(int i, int i2, int i3, boolean z, boolean z2) {
        addRow(i, i2, i3, z, z2, false);
    }

    private void addRow(int i, int i2, int i3, boolean z, boolean z2, boolean z3) {
        if (D.BUG) {
            Slog.d(TAG, "Adding row for stream " + i);
        }
        VolumeRow volumeRow = new VolumeRow();
        initRow(volumeRow, i, i2, i3, z, z2);
        this.mDialogRowsView.addView(volumeRow.view);
        this.mRows.add(volumeRow);
    }

    private void addAppRow(AppTrackData appTrackData) {
        VolumeRow volumeRow = new VolumeRow();
        initAppRow(volumeRow, appTrackData);
        this.mDialogRowsView.addView(volumeRow.view);
        this.mAppRows.add(volumeRow);
    }

    @SuppressLint({"InflateParams"})
    private void initAppRow(final VolumeRow volumeRow, AppTrackData appTrackData) {
        volumeRow.view = LayoutInflater.from(this.mContext).inflate(R.layout.volume_dialog_row, (ViewGroup) null);
        volumeRow.packageName = appTrackData.getPackageName();
        volumeRow.isAppVolumeRow = true;
        volumeRow.view.setTag(volumeRow);
        volumeRow.slider = (SeekBar) volumeRow.view.findViewById(R.id.volume_row_slider);
        volumeRow.slider.setOnSeekBarChangeListener(new VolumeSeekBarChangeListener(volumeRow));
        volumeRow.appMuted = appTrackData.isMuted();
        volumeRow.slider.setProgress((int) (appTrackData.getVolume() * 100.0f));
        volumeRow.dndIcon = (FrameLayout) volumeRow.view.findViewById(R.id.dnd_icon);
        volumeRow.dndIcon.setVisibility(8);
        volumeRow.icon = (ImageButton) volumeRow.view.findViewById(R.id.volume_row_app_icon);
        volumeRow.icon.setVisibility(0);
        PackageManager packageManager = this.mContext.getPackageManager();
        try {
            volumeRow.icon.setImageDrawable(packageManager.getApplicationIcon(volumeRow.packageName));
        } catch (PackageManager.NameNotFoundException e) {
            volumeRow.icon.setImageDrawable(packageManager.getDefaultActivityIcon());
            Log.e(TAG, "Failed to get icon of " + volumeRow.packageName, e);
        }
        volumeRow.icon.setColorFilter(volumeRow.appMuted ? this.mAppIconMuteColorFilter : null);
        volumeRow.icon.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.volume.VolumeDialogImpl$$ExternalSyntheticLambda4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$initAppRow$3(volumeRow, view);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$initAppRow$3(VolumeRow volumeRow, View view) {
        rescheduleTimeoutH();
        AudioManager audioManager = this.mController.getAudioManager();
        volumeRow.appMuted = !volumeRow.appMuted;
        audioManager.setAppMute(volumeRow.packageName, volumeRow.appMuted);
        volumeRow.icon.setColorFilter(volumeRow.appMuted ? this.mAppIconMuteColorFilter : null);
    }

    private void addExistingRows() {
        int size = this.mRows.size();
        for (int i = 0; i < size; i++) {
            VolumeRow volumeRow = this.mRows.get(i);
            initRow(volumeRow, volumeRow.stream, volumeRow.iconRes, volumeRow.iconMuteRes, volumeRow.important, volumeRow.defaultStream);
            this.mDialogRowsView.addView(volumeRow.view);
            updateVolumeRowH(volumeRow);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public VolumeRow getActiveRow() {
        for (VolumeRow volumeRow : this.mRows) {
            if (volumeRow.stream == this.mActiveStream) {
                return volumeRow;
            }
        }
        for (VolumeRow volumeRow2 : this.mRows) {
            if (volumeRow2.stream == 3) {
                return volumeRow2;
            }
        }
        return this.mRows.get(0);
    }

    private VolumeRow findRow(int i) {
        for (VolumeRow volumeRow : this.mRows) {
            if (volumeRow.stream == i) {
                return volumeRow;
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int getImpliedLevel(SeekBar seekBar, int i) {
        int max = seekBar.getMax();
        int i2 = max / 100;
        int i3 = i2 - 1;
        if (i == 0) {
            return 0;
        }
        return i == max ? i2 : ((int) ((i / max) * i3)) + 1;
    }

    @SuppressLint({"InflateParams"})
    private void initRow(final VolumeRow volumeRow, final int i, int i2, int i3, boolean z, boolean z2) {
        volumeRow.stream = i;
        volumeRow.iconRes = i2;
        volumeRow.iconMuteRes = i3;
        volumeRow.important = z;
        volumeRow.defaultStream = z2;
        volumeRow.view = LayoutInflater.from(this.mContext).inflate(R.layout.volume_dialog_row, (ViewGroup) null);
        volumeRow.view.setId(volumeRow.stream);
        volumeRow.view.setTag(volumeRow);
        volumeRow.header = (TextView) volumeRow.view.findViewById(R.id.volume_row_header);
        volumeRow.header.setId(volumeRow.stream * 20);
        if (i == 10) {
            volumeRow.header.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
        }
        volumeRow.dndIcon = (FrameLayout) volumeRow.view.findViewById(R.id.dnd_icon);
        volumeRow.slider = (SeekBar) volumeRow.view.findViewById(R.id.volume_row_slider);
        volumeRow.slider.setOnSeekBarChangeListener(new VolumeSeekBarChangeListener(volumeRow));
        volumeRow.anim = null;
        volumeRow.icon = (ImageButton) volumeRow.view.findViewById(R.id.volume_row_icon);
        volumeRow.icon.setImageResource(i2);
        volumeRow.icon.setVisibility(0);
        if (volumeRow.stream != 10) {
            volumeRow.icon.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.volume.VolumeDialogImpl$$ExternalSyntheticLambda5
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.f$0.lambda$initRow$4(volumeRow, i, view);
                }
            });
        } else {
            volumeRow.icon.setImportantForAccessibility(2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$initRow$4(VolumeRow volumeRow, int i, View view) {
        Events.writeEvent(7, Integer.valueOf(volumeRow.stream), Integer.valueOf(volumeRow.iconState));
        rescheduleTimeoutH();
        this.mController.setActiveStream(volumeRow.stream);
        this.mController.setStreamVolume(i, volumeRow.ss.level == volumeRow.ss.levelMin ? volumeRow.lastAudibleLevel : volumeRow.ss.levelMin);
        volumeRow.userAttempt = 0L;
    }

    private boolean isNotificationVolumeLinked() {
        return Settings.Secure.getInt(this.mContext.getContentResolver(), "volume_link_notification", 1) == 1;
    }

    private static boolean isBluetoothA2dpConnected() {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        return defaultAdapter != null && defaultAdapter.isEnabled() && defaultAdapter.getProfileConnectionState(2) == 2;
    }

    private void setVisOrGone(int i, boolean z) {
        if (z || i != this.mAllyStream) {
            Util.setVisOrGone(findRow(i).view, z);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateExpandedRows(boolean z) {
        if (!z) {
            this.mController.setActiveStream(this.mAllyStream);
        }
        if (this.mMusicHidden) {
            setVisOrGone(3, z);
        }
        setVisOrGone(2, z);
        setVisOrGone(4, z);
        if (!isNotificationVolumeLinked()) {
            setVisOrGone(5, z);
        }
        updateAppRows(z);
    }

    private void updateAppRows(boolean z) {
        for (int size = this.mAppRows.size() - 1; size >= 0; size--) {
            removeAppRow(this.mAppRows.get(size));
        }
        if (z) {
            for (AppTrackData appTrackData : this.mController.getAudioManager().listAppTrackDatas()) {
                if (appTrackData.isActive()) {
                    addAppRow(appTrackData);
                }
            }
        }
    }

    private void animateExpandedRowsChange(final boolean z) throws Resources.NotFoundException {
        int dimensionPixelSize;
        int i = this.mDialogRowsView.getLayoutParams().width;
        if (z) {
            updateExpandedRows(z);
            this.mDialogRowsView.measure(-2, -2);
            dimensionPixelSize = this.mDialogRowsView.getMeasuredWidth();
        } else {
            dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.volume_dialog_panel_width);
        }
        ValueAnimator valueAnimatorOfInt = ValueAnimator.ofInt(i, dimensionPixelSize);
        valueAnimatorOfInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.volume.VolumeDialogImpl.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                VolumeDialogImpl.this.mDialogRowsView.getLayoutParams().width = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                VolumeDialogImpl.this.mDialogRowsView.requestLayout();
            }
        });
        valueAnimatorOfInt.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.volume.VolumeDialogImpl.2
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                boolean z2 = z;
                if (z2) {
                    return;
                }
                VolumeDialogImpl.this.updateExpandedRows(z2);
            }
        });
        valueAnimatorOfInt.setInterpolator(new SystemUIInterpolators$LogDecelerateInterpolator());
        valueAnimatorOfInt.setDuration(z ? 300L : 250L);
        valueAnimatorOfInt.start();
    }

    public void updateMediaOutputH() {
        View view = this.mMediaOutputView;
        if (view != null) {
            view.setVisibility((this.mDeviceProvisionedController.isCurrentUserSetup() && this.mActivityManager.getLockTaskModeState() == 0 && isBluetoothA2dpConnected() && this.mExpanded) ? 0 : 8);
        }
        ImageButton imageButton = this.mMediaOutputIcon;
        if (imageButton != null) {
            imageButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.volume.VolumeDialogImpl$$ExternalSyntheticLambda1
                @Override // android.view.View.OnClickListener
                public final void onClick(View view2) {
                    this.f$0.lambda$updateMediaOutputH$5(view2);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateMediaOutputH$5(View view) {
        rescheduleTimeoutH();
        Events.writeEvent(8, new Object[0]);
        Intent intent = new Intent("com.android.settings.panel.action.MEDIA_OUTPUT");
        dismissH(5);
        ((MediaOutputDialogFactory) Dependency.get(MediaOutputDialogFactory.class)).dismiss();
        ((ActivityStarter) Dependency.get(ActivityStarter.class)).startActivity(intent, true);
    }

    public void initSettingsH() {
        updateMediaOutputH();
        if (this.mAllyStream == -1) {
            this.mAllyStream = this.mActiveStream;
        }
        View view = this.mExpandRowsView;
        if (view != null) {
            view.setVisibility((this.mDeviceProvisionedController.isCurrentUserSetup() && this.mActivityManager.getLockTaskModeState() == 0) ? 0 : 8);
        }
        ExpandableIndicator expandableIndicator = this.mExpandRows;
        if (expandableIndicator != null) {
            expandableIndicator.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.volume.VolumeDialogImpl$$ExternalSyntheticLambda0
                @Override // android.view.View.OnClickListener
                public final void onClick(View view2) throws Resources.NotFoundException {
                    this.f$0.lambda$initSettingsH$6(view2);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$initSettingsH$6(View view) throws Resources.NotFoundException {
        rescheduleTimeoutH();
        animateExpandedRowsChange(!this.mExpanded);
        this.mExpandRows.setExpanded(!this.mExpanded);
        this.mExpanded = !this.mExpanded;
        updateMediaOutputH();
    }

    public void initRingerH() {
        ImageButton imageButton = this.mRingerIcon;
        if (imageButton != null) {
            imageButton.setAccessibilityLiveRegion(1);
            this.mRingerIcon.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.volume.VolumeDialogImpl$$ExternalSyntheticLambda3
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.f$0.lambda$initRingerH$7(view);
                }
            });
        }
        updateRingerH();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:10:0x002c  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public /* synthetic */ void lambda$initRingerH$7(android.view.View r6) {
        /*
            r5 = this;
            r5.rescheduleTimeoutH()
            android.content.Context r6 = r5.mContext
            java.lang.String r0 = "TouchedRingerToggle"
            r1 = 1
            com.android.systemui.Prefs.putBoolean(r6, r0, r1)
            com.android.systemui.plugins.VolumeDialogController$State r6 = r5.mState
            android.util.SparseArray<com.android.systemui.plugins.VolumeDialogController$StreamState> r6 = r6.states
            r0 = 2
            java.lang.Object r6 = r6.get(r0)
            com.android.systemui.plugins.VolumeDialogController$StreamState r6 = (com.android.systemui.plugins.VolumeDialogController.StreamState) r6
            if (r6 != 0) goto L19
            return
        L19:
            com.android.systemui.plugins.VolumeDialogController r2 = r5.mController
            boolean r2 = r2.hasVibrator()
            com.android.systemui.plugins.VolumeDialogController$State r3 = r5.mState
            int r3 = r3.ringerModeInternal
            r4 = 0
            if (r3 != r0) goto L2a
            if (r2 == 0) goto L2c
            r0 = r1
            goto L37
        L2a:
            if (r3 != r1) goto L2e
        L2c:
            r0 = r4
            goto L37
        L2e:
            int r6 = r6.level
            if (r6 != 0) goto L37
            com.android.systemui.plugins.VolumeDialogController r6 = r5.mController
            r6.setStreamVolume(r0, r1)
        L37:
            r6 = 18
            java.lang.Object[] r1 = new java.lang.Object[r1]
            java.lang.Integer r2 = java.lang.Integer.valueOf(r0)
            r1[r4] = r2
            com.android.systemui.volume.Events.writeEvent(r6, r1)
            r5.incrementManualToggleCount()
            r5.updateRingerH()
            r5.provideTouchFeedbackH(r0)
            com.android.systemui.plugins.VolumeDialogController r6 = r5.mController
            r6.setRingerMode(r0, r4)
            r5.maybeShowToastH(r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.volume.VolumeDialogImpl.lambda$initRingerH$7(android.view.View):void");
    }

    private void initODICaptionsH() {
        CaptionsToggleImageButton captionsToggleImageButton = this.mODICaptionsIcon;
        if (captionsToggleImageButton != null) {
            captionsToggleImageButton.setOnConfirmedTapListener(new CaptionsToggleImageButton.ConfirmedTapListener() { // from class: com.android.systemui.volume.VolumeDialogImpl$$ExternalSyntheticLambda8
                @Override // com.android.systemui.volume.CaptionsToggleImageButton.ConfirmedTapListener
                public final void onConfirmedTap() {
                    this.f$0.lambda$initODICaptionsH$8();
                }
            }, this.mHandler);
        }
        this.mController.getCaptionsComponentState(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$initODICaptionsH$8() {
        rescheduleTimeoutH();
        onCaptionIconClicked();
        Events.writeEvent(21, new Object[0]);
    }

    private void checkODICaptionsTooltip(boolean z) {
        boolean z2 = this.mHasSeenODICaptionsTooltip;
        if (!z2 && !z && this.mODICaptionsTooltipViewStub != null) {
            this.mController.getCaptionsComponentState(true);
        } else if (z2 && z && this.mODICaptionsTooltipView != null) {
            hideCaptionsTooltip();
        }
    }

    protected void showCaptionsTooltip() {
        ViewStub viewStub;
        if (!this.mHasSeenODICaptionsTooltip && (viewStub = this.mODICaptionsTooltipViewStub) != null) {
            View viewInflate = viewStub.inflate();
            this.mODICaptionsTooltipView = viewInflate;
            viewInflate.findViewById(R.id.dismiss).setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.volume.VolumeDialogImpl$$ExternalSyntheticLambda2
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.f$0.lambda$showCaptionsTooltip$9(view);
                }
            });
            this.mODICaptionsTooltipViewStub = null;
        }
        View view = this.mODICaptionsTooltipView;
        if (view != null) {
            view.setAlpha(0.0f);
            this.mODICaptionsTooltipView.animate().alpha(1.0f).setStartDelay(300L).withEndAction(new Runnable() { // from class: com.android.systemui.volume.VolumeDialogImpl$$ExternalSyntheticLambda16
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$showCaptionsTooltip$10();
                }
            }).start();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showCaptionsTooltip$9(View view) {
        rescheduleTimeoutH();
        hideCaptionsTooltip();
        Events.writeEvent(22, new Object[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showCaptionsTooltip$10() {
        if (D.BUG) {
            Log.d(TAG, "tool:checkODICaptionsTooltip() putBoolean true");
        }
        Prefs.putBoolean(this.mContext, "HasSeenODICaptionsTooltip", true);
        this.mHasSeenODICaptionsTooltip = true;
        CaptionsToggleImageButton captionsToggleImageButton = this.mODICaptionsIcon;
        if (captionsToggleImageButton != null) {
            captionsToggleImageButton.postOnAnimation(getSinglePressFor(captionsToggleImageButton));
        }
    }

    private void hideCaptionsTooltip() {
        View view = this.mODICaptionsTooltipView;
        if (view == null || view.getVisibility() != 0) {
            return;
        }
        this.mODICaptionsTooltipView.animate().cancel();
        this.mODICaptionsTooltipView.setAlpha(1.0f);
        this.mODICaptionsTooltipView.animate().alpha(0.0f).setStartDelay(0L).setDuration(250L).withEndAction(new Runnable() { // from class: com.android.systemui.volume.VolumeDialogImpl$$ExternalSyntheticLambda11
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$hideCaptionsTooltip$11();
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$hideCaptionsTooltip$11() {
        this.mODICaptionsTooltipView.setVisibility(4);
    }

    protected void tryToRemoveCaptionsTooltip() {
        if (!this.mHasSeenODICaptionsTooltip || this.mODICaptionsTooltipView == null) {
            return;
        }
        ((ViewGroup) this.mDialog.findViewById(R.id.volume_dialog_container)).removeView(this.mODICaptionsTooltipView);
        this.mODICaptionsTooltipView = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateODICaptionsH(boolean z, boolean z2) {
        ViewGroup viewGroup = this.mODICaptionsView;
        if (viewGroup != null) {
            viewGroup.setVisibility(z ? 0 : 8);
        }
        if (z) {
            updateCaptionsIcon();
            if (z2) {
                showCaptionsTooltip();
            }
        }
    }

    private void updateCaptionsIcon() {
        boolean zAreCaptionsEnabled = this.mController.areCaptionsEnabled();
        if (this.mODICaptionsIcon.getCaptionsEnabled() != zAreCaptionsEnabled) {
            this.mHandler.post(this.mODICaptionsIcon.setCaptionsEnabled(zAreCaptionsEnabled));
        }
        final boolean zIsCaptionStreamOptedOut = this.mController.isCaptionStreamOptedOut();
        if (this.mODICaptionsIcon.getOptedOut() != zIsCaptionStreamOptedOut) {
            this.mHandler.post(new Runnable() { // from class: com.android.systemui.volume.VolumeDialogImpl$$ExternalSyntheticLambda18
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$updateCaptionsIcon$12(zIsCaptionStreamOptedOut);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateCaptionsIcon$12(boolean z) {
        this.mODICaptionsIcon.setOptedOut(z);
    }

    private void onCaptionIconClicked() {
        this.mController.setCaptionsEnabled(!this.mController.areCaptionsEnabled());
        updateCaptionsIcon();
    }

    private void incrementManualToggleCount() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Settings.Secure.putInt(contentResolver, "manual_ringer_toggle_count", Settings.Secure.getInt(contentResolver, "manual_ringer_toggle_count", 0) + 1);
    }

    private void provideTouchFeedbackH(int i) {
        VibrationEffect vibrationEffect;
        if (i == 0) {
            vibrationEffect = VibrationEffect.get(0);
        } else if (i == 2) {
            this.mController.scheduleTouchFeedback();
            vibrationEffect = null;
        } else {
            vibrationEffect = VibrationEffect.get(1);
        }
        if (vibrationEffect != null) {
            this.mController.vibrate(vibrationEffect);
        }
    }

    private void maybeShowToastH(int i) {
        int i2 = Prefs.getInt(this.mContext, "RingerGuidanceCount", 0);
        if (i2 > 12) {
            return;
        }
        String string = null;
        if (i == 0) {
            string = this.mContext.getString(android.R.string.policydesc_disableCamera);
        } else if (i == 2) {
            if (this.mState.states.get(2) != null) {
                string = this.mContext.getString(R.string.volume_dialog_ringer_guidance_ring, Utils.formatPercentage(r12.level, r12.levelMax));
            }
        } else {
            string = this.mContext.getString(android.R.string.policydesc_disableKeyguardFeatures);
        }
        Toast.makeText(this.mContext, string, 0).show();
        Prefs.putInt(this.mContext, "RingerGuidanceCount", i2 + 1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showH(int i) {
        if (D.BUG) {
            Log.d(TAG, "showH r=" + Events.SHOW_REASONS[i]);
        }
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        rescheduleTimeoutH();
        if (this.mConfigChanged) {
            initDialog();
            this.mConfigurableTexts.update();
            this.mConfigChanged = false;
        }
        initSettingsH();
        this.mIsAnimatingDismiss = false;
        if (!this.mShowing && !this.mDialog.isShown()) {
            if (!isLandscape()) {
                this.mDialogView.setTranslationX(((this.mVolumePanelOnLeft ? -1 : 1) * r0.getWidth()) / 2.0f);
            }
            this.mDialogView.setAlpha(0.0f);
            this.mDialogView.animate().alpha(1.0f).translationX(0.0f).setDuration(300L).setInterpolator(new SystemUIInterpolators$LogDecelerateInterpolator()).withStartAction(new Runnable() { // from class: com.android.systemui.volume.VolumeDialogImpl$$ExternalSyntheticLambda10
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$showH$13();
                }
            }).withEndAction(new Runnable() { // from class: com.android.systemui.volume.VolumeDialogImpl$$ExternalSyntheticLambda13
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$showH$14();
                }
            }).start();
        }
        Events.writeEvent(0, Integer.valueOf(i), Boolean.valueOf(this.mKeyguard.isKeyguardLocked()));
        this.mController.notifyVisible(true);
        this.mController.getCaptionsComponentState(false);
        checkODICaptionsTooltip(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showH$13() {
        if (this.mDialog.isShown()) {
            return;
        }
        this.mWindowManager.addView(this.mDialog, this.mWindowParams);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showH$14() {
        ImageButton imageButton;
        if (!Prefs.getBoolean(this.mContext, "TouchedRingerToggle", false) && (imageButton = this.mRingerIcon) != null) {
            imageButton.postOnAnimationDelayed(getSinglePressFor(imageButton), 1500L);
        }
        this.mShowing = true;
    }

    protected void rescheduleTimeoutH() {
        this.mHandler.removeMessages(2);
        int iComputeTimeoutH = computeTimeoutH();
        H h = this.mHandler;
        h.sendMessageDelayed(h.obtainMessage(2, 3, 0), iComputeTimeoutH);
        if (D.BUG) {
            Log.d(TAG, "rescheduleTimeout " + iComputeTimeoutH + " " + Debug.getCaller());
        }
        this.mController.userActivity();
    }

    private int computeTimeoutH() {
        if (this.mHovering) {
            return this.mAccessibilityMgr.getRecommendedTimeoutMillis(16000, 4);
        }
        if (this.mSafetyWarning != null) {
            return this.mAccessibilityMgr.getRecommendedTimeoutMillis(5000, 6);
        }
        if (!this.mHasSeenODICaptionsTooltip && this.mODICaptionsTooltipView != null) {
            return this.mAccessibilityMgr.getRecommendedTimeoutMillis(5000, 6);
        }
        return this.mTimeOut;
    }

    protected void dismissH(int i) {
        if (D.BUG) {
            Log.d(TAG, "mDialog.dismiss() reason: " + Events.DISMISS_REASONS[i] + " from: " + Debug.getCaller());
        }
        if (this.mShowing) {
            this.mHandler.removeMessages(2);
            this.mHandler.removeMessages(1);
            if (this.mIsAnimatingDismiss) {
                return;
            }
            this.mIsAnimatingDismiss = true;
            this.mDialogView.animate().cancel();
            if (this.mShowing) {
                this.mShowing = false;
                Events.writeEvent(1, Integer.valueOf(i));
            }
            this.mDialogView.setTranslationX(0.0f);
            this.mDialogView.setAlpha(1.0f);
            ViewPropertyAnimator viewPropertyAnimatorWithEndAction = this.mDialogView.animate().alpha(0.0f).setDuration(250L).setInterpolator(new SystemUIInterpolators$LogAccelerateInterpolator()).withEndAction(new Runnable() { // from class: com.android.systemui.volume.VolumeDialogImpl$$ExternalSyntheticLambda12
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$dismissH$16();
                }
            });
            if (!isLandscape() || !this.mShowActiveStreamOnly) {
                viewPropertyAnimatorWithEndAction.translationX(getAnimatorX());
            }
            viewPropertyAnimatorWithEndAction.start();
            checkODICaptionsTooltip(true);
            synchronized (this.mSafetyWarningLock) {
                if (this.mSafetyWarning != null) {
                    if (D.BUG) {
                        Log.d(TAG, "SafetyWarning dismissed");
                    }
                    this.mSafetyWarning.dismiss();
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$dismissH$16() {
        this.mHandler.postDelayed(new Runnable() { // from class: com.android.systemui.volume.VolumeDialogImpl$$ExternalSyntheticLambda15
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$dismissH$15();
            }
        }, 50L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$dismissH$15() {
        this.mIsAnimatingDismiss = false;
        if (this.mDialog.isShown()) {
            this.mWindowManager.removeViewImmediate(this.mDialog);
        }
        this.mExpanded = false;
        this.mDialogRowsView.getLayoutParams().width = this.mContext.getResources().getDimensionPixelSize(R.dimen.volume_dialog_panel_width);
        updateExpandedRows(this.mExpanded);
        this.mExpandRows.setExpanded(this.mExpanded);
        this.mAllyStream = -1;
        this.mMusicHidden = false;
        tryToRemoveCaptionsTooltip();
        this.mController.notifyVisible(false);
    }

    private boolean showActiveStreamOnly() {
        return this.mContext.getPackageManager().hasSystemFeature("android.software.leanback") || this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.television");
    }

    private boolean shouldBeVisibleH(VolumeRow volumeRow, VolumeRow volumeRow2) {
        boolean z = volumeRow.stream == volumeRow2.stream;
        if (volumeRow.stream == 3 && volumeRow2.stream != 3 && !this.mExpanded) {
            this.mMusicHidden = true;
            return false;
        }
        if (z) {
            return true;
        }
        if (this.mShowActiveStreamOnly) {
            return false;
        }
        if (volumeRow.stream == 10) {
            return this.mShowA11yStream;
        }
        if (volumeRow2.stream == 10 && volumeRow.stream == this.mPrevActiveStream) {
            return true;
        }
        if (volumeRow.defaultStream) {
            return volumeRow2.stream == 2 || volumeRow2.stream == 5 || volumeRow2.stream == 4 || volumeRow2.stream == 0 || volumeRow2.stream == 10 || this.mDynamic.get(volumeRow2.stream);
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateRowsH(VolumeRow volumeRow) {
        if (D.BUG) {
            Log.d(TAG, "updateRowsH");
        }
        if (!this.mShowing) {
            trimObsoleteH();
        }
        Iterator<VolumeRow> it = this.mRows.iterator();
        while (it.hasNext()) {
            VolumeRow next = it.next();
            boolean z = next == volumeRow;
            boolean zShouldBeVisibleH = shouldBeVisibleH(next, volumeRow);
            if (!this.mExpanded) {
                Util.setVisOrGone(next.view, zShouldBeVisibleH);
            }
            if (next.view.isShown()) {
                updateVolumeRowTintH(next, z);
            }
        }
    }

    protected void updateRingerH() {
        VolumeDialogController.State state;
        VolumeDialogController.StreamState streamState;
        if (this.mRinger == null || (state = this.mState) == null || (streamState = state.states.get(2)) == null) {
            return;
        }
        VolumeDialogController.State state2 = this.mState;
        int i = state2.zenMode;
        boolean z = i == 3 || i == 2 || (i == 1 && state2.disallowRinger);
        enableRingerViewsH(!z);
        int i2 = this.mState.ringerModeInternal;
        if (i2 == 0) {
            this.mRingerIcon.setImageResource(R.drawable.ic_volume_ringer_mute);
            this.mRingerIcon.setTag(2);
            addAccessibilityDescription(this.mRingerIcon, 0, this.mContext.getString(R.string.volume_ringer_hint_unmute));
            return;
        }
        if (i2 == 1) {
            this.mRingerIcon.setImageResource(R.drawable.ic_volume_ringer_vibrate);
            addAccessibilityDescription(this.mRingerIcon, 1, this.mContext.getString(R.string.volume_ringer_hint_mute));
            this.mRingerIcon.setTag(3);
            return;
        }
        boolean z2 = (this.mAutomute && streamState.level == 0) || streamState.muted;
        if (!z && z2) {
            this.mRingerIcon.setImageResource(R.drawable.ic_volume_ringer_mute);
            addAccessibilityDescription(this.mRingerIcon, 2, this.mContext.getString(R.string.volume_ringer_hint_unmute));
            this.mRingerIcon.setTag(2);
        } else {
            this.mRingerIcon.setImageResource(R.drawable.ic_volume_ringer);
            if (this.mController.hasVibrator()) {
                addAccessibilityDescription(this.mRingerIcon, 2, this.mContext.getString(R.string.volume_ringer_hint_vibrate));
            } else {
                addAccessibilityDescription(this.mRingerIcon, 2, this.mContext.getString(R.string.volume_ringer_hint_mute));
            }
            this.mRingerIcon.setTag(1);
        }
    }

    private void addAccessibilityDescription(View view, int i, final String str) {
        int i2;
        if (i == 0) {
            i2 = R.string.volume_ringer_status_silent;
        } else if (i == 1) {
            i2 = R.string.volume_ringer_status_vibrate;
        } else {
            i2 = R.string.volume_ringer_status_normal;
        }
        view.setContentDescription(this.mContext.getString(i2));
        view.setAccessibilityDelegate(new View.AccessibilityDelegate() { // from class: com.android.systemui.volume.VolumeDialogImpl.3
            @Override // android.view.View.AccessibilityDelegate
            public void onInitializeAccessibilityNodeInfo(View view2, AccessibilityNodeInfo accessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(view2, accessibilityNodeInfo);
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, str));
            }
        });
    }

    private void enableVolumeRowViewsH(VolumeRow volumeRow, boolean z) {
        volumeRow.dndIcon.setVisibility(z ^ true ? 0 : 8);
    }

    private void enableRingerViewsH(boolean z) {
        ImageButton imageButton = this.mRingerIcon;
        if (imageButton != null) {
            imageButton.setEnabled(z);
        }
        FrameLayout frameLayout = this.mZenIcon;
        if (frameLayout != null) {
            frameLayout.setVisibility(z ? 8 : 0);
        }
    }

    private void trimObsoleteH() {
        if (D.BUG) {
            Log.d(TAG, "trimObsoleteH");
        }
        for (int size = this.mRows.size() - 1; size >= 0; size--) {
            VolumeRow volumeRow = this.mRows.get(size);
            if (volumeRow.ss != null && volumeRow.ss.dynamic && !this.mDynamic.get(volumeRow.stream)) {
                removeRow(volumeRow);
                this.mConfigurableTexts.remove(volumeRow.header);
            }
        }
    }

    private void removeRow(VolumeRow volumeRow) {
        this.mRows.remove(volumeRow);
        this.mDialogRowsView.removeView(volumeRow.view);
    }

    private void removeAppRow(VolumeRow volumeRow) {
        this.mAppRows.remove(volumeRow);
        this.mDialogRowsView.removeView(volumeRow.view);
    }

    protected void onStateChangedH(VolumeDialogController.State state) {
        int i;
        int i2;
        if (D.BUG) {
            Log.d(TAG, "onStateChangedH() state: " + state.toString());
        }
        VolumeDialogController.State state2 = this.mState;
        if (state2 != null && state != null && (i = state2.ringerModeInternal) != -1 && i != (i2 = state.ringerModeInternal) && i2 == 1) {
            this.mController.vibrate(VibrationEffect.get(5));
        }
        this.mState = state;
        this.mDynamic.clear();
        for (int i3 = 0; i3 < state.states.size(); i3++) {
            int iKeyAt = state.states.keyAt(i3);
            if (state.states.valueAt(i3).dynamic) {
                this.mDynamic.put(iKeyAt, true);
                if (findRow(iKeyAt) == null) {
                    addRow(iKeyAt, R.drawable.ic_volume_remote, R.drawable.ic_volume_remote_mute, true, false, true);
                }
            }
        }
        if (com.android.settingslib.volume.Util.isVoiceCapable(this.mContext)) {
            updateNotificationRowH();
        }
        int i4 = this.mActiveStream;
        int i5 = state.activeStream;
        if (i4 != i5) {
            this.mPrevActiveStream = i4;
            this.mActiveStream = i5;
            updateRowsH(getActiveRow());
            if (this.mShowing) {
                rescheduleTimeoutH();
            }
        }
        Iterator<VolumeRow> it = this.mRows.iterator();
        while (it.hasNext()) {
            updateVolumeRowH(it.next());
        }
        updateRingerH();
    }

    CharSequence composeWindowTitle() {
        return this.mContext.getString(R.string.volume_dialog_title, getStreamLabelH(getActiveRow().ss));
    }

    private void updateNotificationRowH() {
        VolumeRow volumeRowFindRow = findRow(5);
        if (volumeRowFindRow != null && this.mState.linkedNotification) {
            removeRow(volumeRowFindRow);
        } else {
            if (volumeRowFindRow != null || this.mState.linkedNotification) {
                return;
            }
            addRow(5, R.drawable.ic_volume_notification, R.drawable.ic_volume_notification_mute, true, false);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:67:0x00c2  */
    /* JADX WARN: Removed duplicated region for block: B:91:0x00ef  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private void updateVolumeRowH(com.android.systemui.volume.VolumeDialogImpl.VolumeRow r20) {
        /*
            Method dump skipped, instructions count: 681
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.volume.VolumeDialogImpl.updateVolumeRowH(com.android.systemui.volume.VolumeDialogImpl$VolumeRow):void");
    }

    private boolean isStreamMuted(VolumeDialogController.StreamState streamState) {
        return (this.mAutomute && streamState.level == streamState.levelMin) || streamState.muted;
    }

    private void updateVolumeRowTintH(VolumeRow volumeRow, boolean z) {
        ColorStateList colorAttr;
        int alphaAttr;
        if (z) {
            volumeRow.slider.requestFocus();
        }
        boolean z2 = z && volumeRow.slider.isEnabled();
        if (z2) {
            colorAttr = Utils.getColorAccent(this.mContext);
        } else {
            colorAttr = Utils.getColorAttr(this.mContext, android.R.attr.colorForeground);
        }
        if (z2) {
            alphaAttr = Color.alpha(colorAttr.getDefaultColor());
        } else {
            alphaAttr = getAlphaAttr(android.R.attr.secondaryContentAlpha);
        }
        if (colorAttr == volumeRow.cachedTint && this.mExpanded) {
            return;
        }
        volumeRow.slider.setProgressTintList(colorAttr);
        volumeRow.slider.setThumbTintList(colorAttr);
        volumeRow.slider.setProgressBackgroundTintList(colorAttr);
        volumeRow.slider.setAlpha(alphaAttr / 255.0f);
        volumeRow.icon.setImageTintList(colorAttr);
        volumeRow.icon.setImageAlpha(alphaAttr);
        volumeRow.cachedTint = colorAttr;
    }

    private void updateVolumeRowSliderH(VolumeRow volumeRow, boolean z, int i, boolean z2) {
        volumeRow.slider.setEnabled(z);
        updateVolumeRowTintH(volumeRow, volumeRow.stream == this.mActiveStream);
        if (volumeRow.tracking) {
            return;
        }
        int progress = volumeRow.slider.getProgress();
        int impliedLevel = getImpliedLevel(volumeRow.slider, progress);
        boolean z3 = volumeRow.view.getVisibility() == 0;
        boolean z4 = SystemClock.uptimeMillis() - volumeRow.userAttempt < 1000;
        this.mHandler.removeMessages(3, volumeRow);
        boolean z5 = this.mShowing;
        if (z5 && z3 && z4) {
            if (D.BUG) {
                Log.d(TAG, "inGracePeriod");
            }
            H h = this.mHandler;
            h.sendMessageAtTime(h.obtainMessage(3, volumeRow), volumeRow.userAttempt + 1000);
            return;
        }
        if (i == impliedLevel && z5 && z3) {
            return;
        }
        int i2 = i * 100;
        if (progress != i2 || z2) {
            if (!z5 || !z3) {
                if (volumeRow.anim != null) {
                    volumeRow.anim.cancel();
                }
                volumeRow.slider.setProgress(i2, true);
            } else {
                if (volumeRow.anim != null && volumeRow.anim.isRunning() && volumeRow.animTargetProgress == i2) {
                    return;
                }
                if (volumeRow.anim == null) {
                    volumeRow.anim = ObjectAnimator.ofInt(volumeRow.slider, "progress", progress, i2);
                    volumeRow.anim.setInterpolator(new DecelerateInterpolator());
                } else {
                    volumeRow.anim.cancel();
                    volumeRow.anim.setIntValues(progress, i2);
                }
                volumeRow.animTargetProgress = i2;
                volumeRow.anim.setDuration(80L);
                volumeRow.anim.start();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void recheckH(VolumeRow volumeRow) {
        if (volumeRow == null) {
            if (D.BUG) {
                Log.d(TAG, "recheckH ALL");
            }
            trimObsoleteH();
            Iterator<VolumeRow> it = this.mRows.iterator();
            while (it.hasNext()) {
                updateVolumeRowH(it.next());
            }
            return;
        }
        if (D.BUG) {
            Log.d(TAG, "recheckH " + volumeRow.stream);
        }
        updateVolumeRowH(volumeRow);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setStreamImportantH(int i, boolean z) {
        for (VolumeRow volumeRow : this.mRows) {
            if (volumeRow.stream == i) {
                volumeRow.important = z;
                return;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showSafetyWarningH(int i) {
        if ((i & 1025) != 0 || this.mShowing) {
            synchronized (this.mSafetyWarningLock) {
                if (this.mSafetyWarning != null) {
                    return;
                }
                SafetyWarningDialog safetyWarningDialog = new SafetyWarningDialog(this.mContext, this.mController.getAudioManager()) { // from class: com.android.systemui.volume.VolumeDialogImpl.4
                    @Override // com.android.systemui.volume.SafetyWarningDialog
                    protected void cleanUp() {
                        synchronized (VolumeDialogImpl.this.mSafetyWarningLock) {
                            VolumeDialogImpl.this.mSafetyWarning = null;
                        }
                        VolumeDialogImpl.this.recheckH(null);
                    }
                };
                this.mSafetyWarning = safetyWarningDialog;
                safetyWarningDialog.show();
                recheckH(null);
            }
        }
        rescheduleTimeoutH();
    }

    private String getStreamLabelH(VolumeDialogController.StreamState streamState) {
        if (streamState == null) {
            return "";
        }
        String str = streamState.remoteLabel;
        if (str != null) {
            return str;
        }
        try {
            return this.mContext.getResources().getString(streamState.name);
        } catch (Resources.NotFoundException unused) {
            Slog.e(TAG, "Can't find translation for stream " + streamState);
            return "";
        }
    }

    private Runnable getSinglePressFor(final ImageButton imageButton) {
        return new Runnable() { // from class: com.android.systemui.volume.VolumeDialogImpl$$ExternalSyntheticLambda17
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$getSinglePressFor$17(imageButton);
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$getSinglePressFor$17(ImageButton imageButton) {
        if (imageButton != null) {
            imageButton.setPressed(true);
            imageButton.postOnAnimationDelayed(getSingleUnpressFor(imageButton), 200L);
        }
    }

    private Runnable getSingleUnpressFor(final ImageButton imageButton) {
        return new Runnable() { // from class: com.android.systemui.volume.VolumeDialogImpl$$ExternalSyntheticLambda9
            @Override // java.lang.Runnable
            public final void run() {
                VolumeDialogImpl.lambda$getSingleUnpressFor$18(imageButton);
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$getSingleUnpressFor$18(ImageButton imageButton) {
        if (imageButton != null) {
            imageButton.setPressed(false);
        }
    }

    private final class H extends Handler {
        public H() {
            super(Looper.getMainLooper());
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    VolumeDialogImpl.this.showH(message.arg1);
                    break;
                case 2:
                    VolumeDialogImpl.this.dismissH(message.arg1);
                    break;
                case SensorManagerPlugin.Sensor.TYPE_SWIPE /* 3 */:
                    VolumeDialogImpl.this.recheckH((VolumeRow) message.obj);
                    break;
                case 4:
                    VolumeDialogImpl.this.recheckH(null);
                    break;
                case 5:
                    VolumeDialogImpl.this.setStreamImportantH(message.arg1, message.arg2 != 0);
                    break;
                case 6:
                    VolumeDialogImpl.this.rescheduleTimeoutH();
                    break;
                case 7:
                    VolumeDialogImpl volumeDialogImpl = VolumeDialogImpl.this;
                    volumeDialogImpl.onStateChangedH(volumeDialogImpl.mState);
                    break;
            }
        }
    }

    private final class VolumeSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        private final VolumeRow mRow;

        private VolumeSeekBarChangeListener(VolumeRow volumeRow) {
            this.mRow = volumeRow;
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
            int i2;
            VolumeDialogImpl.this.rescheduleTimeoutH();
            if (D.BUG) {
                Log.d(VolumeDialogImpl.TAG, AudioSystem.streamToString(this.mRow.stream) + " onProgressChanged " + i + " fromUser=" + z);
            }
            if (z) {
                if (this.mRow.isAppVolumeRow) {
                    VolumeDialogImpl.this.mController.getAudioManager().setAppVolume(this.mRow.packageName, i * 0.01f);
                    return;
                }
                if (this.mRow.ss == null) {
                    return;
                }
                if (this.mRow.ss.levelMin > 0 && i < (i2 = this.mRow.ss.levelMin * 100)) {
                    seekBar.setProgress(i2);
                    i = i2;
                }
                int impliedLevel = VolumeDialogImpl.getImpliedLevel(seekBar, i);
                if (this.mRow.ss.level != impliedLevel || (this.mRow.ss.muted && impliedLevel > 0)) {
                    this.mRow.userAttempt = SystemClock.uptimeMillis();
                    if (this.mRow.requestedLevel != impliedLevel) {
                        VolumeDialogImpl.this.mController.setActiveStream(this.mRow.stream);
                        VolumeDialogImpl.this.mController.setStreamVolume(this.mRow.stream, impliedLevel);
                        this.mRow.requestedLevel = impliedLevel;
                        Events.writeEvent(9, Integer.valueOf(this.mRow.stream), Integer.valueOf(impliedLevel));
                    }
                }
            }
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onStartTrackingTouch(SeekBar seekBar) {
            this.mRow.tracking = true;
            if (this.mRow.isAppVolumeRow) {
                return;
            }
            if (D.BUG) {
                Log.d(VolumeDialogImpl.TAG, "onStartTrackingTouch " + this.mRow.stream);
            }
            VolumeDialogImpl.this.mController.setActiveStream(this.mRow.stream);
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (D.BUG) {
                Log.d(VolumeDialogImpl.TAG, "onStopTrackingTouch " + this.mRow.stream);
            }
            this.mRow.tracking = false;
            if (this.mRow.isAppVolumeRow) {
                return;
            }
            this.mRow.userAttempt = SystemClock.uptimeMillis();
            int impliedLevel = VolumeDialogImpl.getImpliedLevel(seekBar, seekBar.getProgress());
            Events.writeEvent(16, Integer.valueOf(this.mRow.stream), Integer.valueOf(impliedLevel));
            if (this.mRow.ss.level != impliedLevel) {
                VolumeDialogImpl.this.mHandler.sendMessageDelayed(VolumeDialogImpl.this.mHandler.obtainMessage(3, this.mRow), 1000L);
            }
        }
    }

    private final class Accessibility extends View.AccessibilityDelegate {
        private Accessibility() {
        }

        public void init() {
            VolumeDialogImpl.this.mDialogView.setAccessibilityDelegate(this);
        }

        @Override // android.view.View.AccessibilityDelegate
        public boolean dispatchPopulateAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
            accessibilityEvent.getText().add(VolumeDialogImpl.this.composeWindowTitle());
            return true;
        }

        @Override // android.view.View.AccessibilityDelegate
        public boolean onRequestSendAccessibilityEvent(ViewGroup viewGroup, View view, AccessibilityEvent accessibilityEvent) {
            VolumeDialogImpl.this.rescheduleTimeoutH();
            return super.onRequestSendAccessibilityEvent(viewGroup, view, accessibilityEvent);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    static class VolumeRow {
        private ObjectAnimator anim;
        private int animTargetProgress;
        private boolean appMuted;
        private ColorStateList cachedTint;
        private boolean defaultStream;
        private FrameLayout dndIcon;
        private TextView header;
        private ImageButton icon;
        private int iconMuteRes;
        private int iconRes;
        private int iconState;
        private boolean important;
        private boolean isAppVolumeRow;
        private int lastAudibleLevel;
        private String packageName;
        private int requestedLevel;
        private SeekBar slider;
        private VolumeDialogController.StreamState ss;
        private int stream;
        private boolean tracking;
        private long userAttempt;
        private View view;

        private VolumeRow() {
            this.requestedLevel = -1;
            this.lastAudibleLevel = 2;
            this.isAppVolumeRow = false;
        }
    }
}
