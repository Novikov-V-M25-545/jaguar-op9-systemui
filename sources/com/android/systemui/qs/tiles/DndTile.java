package com.android.systemui.qs.tiles;

import android.R;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.service.notification.ZenModeConfig;
import android.text.TextUtils;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import androidx.lifecycle.Lifecycle;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Prefs;
import com.android.systemui.SysUIToast;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.volume.ZenModePanel;

/* loaded from: classes.dex */
public class DndTile extends QSTileImpl<QSTile.BooleanState> {
    private final ActivityStarter mActivityStarter;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final ZenModeController mController;
    private final DndDetailAdapter mDetailAdapter;
    private boolean mListening;
    private final SharedPreferences.OnSharedPreferenceChangeListener mPrefListener;
    private final BroadcastReceiver mReceiver;
    private boolean mReceiverRegistered;
    private final SharedPreferences mSharedPreferences;
    private boolean mShowingDetail;
    private final ZenModeController.Callback mZenCallback;
    private final ZenModePanel.Callback mZenModePanelCallback;
    private static final Intent ZEN_SETTINGS = new Intent("android.settings.ZEN_MODE_SETTINGS");
    private static final Intent ZEN_PRIORITY_SETTINGS = new Intent("android.settings.ZEN_MODE_PRIORITY_SETTINGS");

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 118;
    }

    public DndTile(QSHost qSHost, ZenModeController zenModeController, ActivityStarter activityStarter, BroadcastDispatcher broadcastDispatcher, SharedPreferences sharedPreferences) {
        super(qSHost);
        this.mPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener() { // from class: com.android.systemui.qs.tiles.DndTile.2
            @Override // android.content.SharedPreferences.OnSharedPreferenceChangeListener
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences2, String str) {
                if ("DndTileCombinedIcon".equals(str) || "DndTileVisible".equals(str)) {
                    DndTile.this.refreshState();
                }
            }
        };
        ZenModeController.Callback callback = new ZenModeController.Callback() { // from class: com.android.systemui.qs.tiles.DndTile.3
            @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
            public void onZenChanged(int i) {
                DndTile.this.refreshState(Integer.valueOf(i));
                if (DndTile.this.isShowingDetail()) {
                    DndTile.this.mDetailAdapter.updatePanel();
                }
            }

            @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
            public void onConfigChanged(ZenModeConfig zenModeConfig) {
                DndTile.this.refreshState(zenModeConfig);
                if (DndTile.this.isShowingDetail()) {
                    DndTile.this.mDetailAdapter.updatePanel();
                }
            }
        };
        this.mZenCallback = callback;
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.qs.tiles.DndTile.4
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                DndTile.setVisible(((QSTileImpl) DndTile.this).mContext, intent.getBooleanExtra("visible", false));
                DndTile.this.refreshState();
            }
        };
        this.mReceiver = broadcastReceiver;
        this.mZenModePanelCallback = new ZenModePanel.Callback() { // from class: com.android.systemui.qs.tiles.DndTile.5
            @Override // com.android.systemui.volume.ZenModePanel.Callback
            public void onExpanded(boolean z) {
            }

            @Override // com.android.systemui.volume.ZenModePanel.Callback
            public void onInteraction() {
            }

            @Override // com.android.systemui.volume.ZenModePanel.Callback
            public void onPrioritySettings() {
                DndTile.this.mActivityStarter.postStartActivityDismissingKeyguard(DndTile.ZEN_PRIORITY_SETTINGS, 0);
            }
        };
        this.mController = zenModeController;
        this.mActivityStarter = activityStarter;
        this.mSharedPreferences = sharedPreferences;
        this.mDetailAdapter = new DndDetailAdapter();
        this.mBroadcastDispatcher = broadcastDispatcher;
        broadcastDispatcher.registerReceiver(broadcastReceiver, new IntentFilter("com.android.systemui.dndtile.SET_VISIBLE"));
        this.mReceiverRegistered = true;
        zenModeController.observe(getLifecycle(), (Lifecycle) callback);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleDestroy() {
        super.handleDestroy();
        if (this.mReceiverRegistered) {
            this.mBroadcastDispatcher.unregisterReceiver(this.mReceiver);
            this.mReceiverRegistered = false;
        }
    }

    public static void setVisible(Context context, boolean z) {
        Prefs.putBoolean(context, "DndTileVisible", z);
    }

    public static boolean isVisible(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean("DndTileVisible", false);
    }

    public static void setCombinedIcon(Context context, boolean z) {
        Prefs.putBoolean(context, "DndTileCombinedIcon", z);
    }

    public static boolean isCombinedIcon(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean("DndTileCombinedIcon", false);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public DetailAdapter getDetailAdapter() {
        return this.mDetailAdapter;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return ZEN_SETTINGS;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleClick() {
        if (((QSTile.BooleanState) this.mState).value) {
            this.mController.setZen(0, null, this.TAG);
        } else {
            turnOnDND(1);
        }
    }

    public void turnOnDND(int i) {
        int i2 = Settings.Secure.getInt(this.mContext.getContentResolver(), "zen_duration", 0);
        if ((Settings.Secure.getInt(this.mContext.getContentResolver(), "show_zen_upgrade_notification", 0) == 0 || Settings.Secure.getInt(this.mContext.getContentResolver(), "zen_settings_updated", 0) == 1) ? false : true) {
            Settings.Secure.putInt(this.mContext.getContentResolver(), "show_zen_upgrade_notification", 0);
            this.mController.setZen(1, null, this.TAG);
            Intent intent = new Intent("android.settings.ZEN_MODE_ONBOARDING");
            intent.addFlags(268468224);
            this.mActivityStarter.postStartActivityDismissingKeyguard(intent, 0);
            return;
        }
        ZenModeController.Callback callback = new ZenModeController.Callback() { // from class: com.android.systemui.qs.tiles.DndTile.1
            @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
            public void onZenChanged(int i3) {
                DndTile.this.mController.removeCallback(this);
                DndTile.this.showDetail(true);
            }
        };
        if (i == 2) {
            this.mController.addCallback(callback);
        }
        if (i2 == -1) {
            if (i == 1) {
                this.mController.addCallback(callback);
            }
            this.mController.setZen(1, null, this.TAG);
        } else if (i2 == 0) {
            this.mController.setZen(1, null, this.TAG);
        } else {
            this.mController.setZen(1, ZenModeConfig.toTimeCondition(this.mContext, i2, ActivityManager.getCurrentUser(), true).id, this.TAG);
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleSecondaryClick() {
        if (this.mController.isVolumeRestricted()) {
            this.mHost.collapsePanels();
            Context context = this.mContext;
            SysUIToast.makeText(context, context.getString(R.string.content_description_collapsed), 1).show();
        } else if (!((QSTile.BooleanState) this.mState).value) {
            turnOnDND(2);
        } else {
            showDetail(true);
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(com.android.systemui.R.string.quick_settings_dnd_label);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        ZenModeController zenModeController = this.mController;
        if (zenModeController == null) {
            return;
        }
        int iIntValue = obj instanceof Integer ? ((Integer) obj).intValue() : zenModeController.getZen();
        ZenModeConfig config = obj instanceof ZenModeConfig ? (ZenModeConfig) obj : this.mController.getConfig();
        boolean z = iIntValue != 0;
        boolean z2 = booleanState.value != z;
        if (booleanState.slash == null) {
            booleanState.slash = new QSTile.SlashState();
        }
        booleanState.dualTarget = true;
        booleanState.value = z;
        booleanState.state = z ? 2 : 1;
        booleanState.slash.isSlashed = !z;
        booleanState.secondaryLabel = TextUtils.emptyIfNull(ZenModeConfig.getDescription(this.mContext, iIntValue != 0, config, false));
        booleanState.icon = QSTileImpl.ResourceIcon.get(R.drawable.ic_menu_emoticons);
        checkIfRestrictionEnforcedByAdminOnly(booleanState, "no_adjust_volume");
        if (iIntValue == 1) {
            booleanState.icon = QSTileImpl.ResourceIcon.get(com.android.systemui.R.drawable.ic_qs_dnd_on_priority);
            booleanState.label = this.mContext.getString(com.android.systemui.R.string.quick_settings_dnd_priority_label);
            booleanState.contentDescription = this.mContext.getString(com.android.systemui.R.string.accessibility_quick_settings_dnd) + ", " + ((Object) booleanState.secondaryLabel);
        } else if (iIntValue == 2) {
            booleanState.icon = QSTileImpl.ResourceIcon.get(com.android.systemui.R.drawable.ic_qs_dnd_on_total_silence);
            booleanState.label = this.mContext.getString(com.android.systemui.R.string.quick_settings_dnd_none_label);
            booleanState.contentDescription = this.mContext.getString(com.android.systemui.R.string.accessibility_quick_settings_dnd) + ", " + this.mContext.getString(com.android.systemui.R.string.accessibility_quick_settings_dnd_none_on) + ", " + ((Object) booleanState.secondaryLabel);
        } else if (iIntValue == 3) {
            booleanState.icon = QSTileImpl.ResourceIcon.get(R.drawable.ic_menu_emoticons);
            booleanState.label = this.mContext.getString(com.android.systemui.R.string.quick_settings_dnd_alarms_label);
            booleanState.contentDescription = this.mContext.getString(com.android.systemui.R.string.accessibility_quick_settings_dnd) + ", " + this.mContext.getString(com.android.systemui.R.string.accessibility_quick_settings_dnd_alarms_on) + ", " + ((Object) booleanState.secondaryLabel);
        } else {
            booleanState.icon = QSTileImpl.ResourceIcon.get(R.drawable.ic_menu_emoticons);
            booleanState.label = this.mContext.getString(com.android.systemui.R.string.quick_settings_dnd_label);
            booleanState.contentDescription = this.mContext.getString(com.android.systemui.R.string.accessibility_quick_settings_dnd);
        }
        if (z2) {
            fireToggleStateChanged(booleanState.value);
        }
        booleanState.dualLabelContentDescription = this.mContext.getResources().getString(com.android.systemui.R.string.accessibility_quick_settings_open_settings, getTileLabel());
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(com.android.systemui.R.string.accessibility_quick_settings_dnd_changed_on);
        }
        return this.mContext.getString(com.android.systemui.R.string.accessibility_quick_settings_dnd_changed_off);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        if (this.mListening == z) {
            return;
        }
        this.mListening = z;
        if (z) {
            Prefs.registerListener(this.mContext, this.mPrefListener);
        } else {
            Prefs.unregisterListener(this.mContext, this.mPrefListener);
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return isVisible(this.mSharedPreferences);
    }

    private final class DndDetailAdapter implements DetailAdapter, View.OnAttachStateChangeListener {
        private boolean mAuto;
        private ZenModePanel mZenPanel;

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public int getMetricsCategory() {
            return 149;
        }

        private DndDetailAdapter() {
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public CharSequence getTitle() {
            return ((QSTileImpl) DndTile.this).mContext.getString(com.android.systemui.R.string.quick_settings_dnd_label);
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public Boolean getToggleState() {
            return Boolean.valueOf(((QSTile.BooleanState) ((QSTileImpl) DndTile.this).mState).value);
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public Intent getSettingsIntent() {
            return DndTile.ZEN_SETTINGS;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public void setToggleState(boolean z) {
            MetricsLogger.action(((QSTileImpl) DndTile.this).mContext, 166, z);
            if (!z) {
                DndTile.this.mController.setZen(0, null, ((QSTileImpl) DndTile.this).TAG);
                this.mAuto = false;
            } else {
                DndTile.this.turnOnDND(0);
            }
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            ZenModePanel zenModePanel;
            if (view != null) {
                zenModePanel = (ZenModePanel) view;
            } else {
                zenModePanel = (ZenModePanel) LayoutInflater.from(context).inflate(com.android.systemui.R.layout.zen_mode_panel, viewGroup, false);
            }
            this.mZenPanel = zenModePanel;
            if (view == null) {
                zenModePanel.init(DndTile.this.mController);
                this.mZenPanel.addOnAttachStateChangeListener(this);
                this.mZenPanel.setCallback(DndTile.this.mZenModePanelCallback);
                this.mZenPanel.setEmptyState(com.android.systemui.R.drawable.ic_qs_dnd_detail_empty, com.android.systemui.R.string.dnd_is_off);
            }
            updatePanel();
            return this.mZenPanel;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void updatePanel() {
            String str;
            if (this.mZenPanel == null) {
                return;
            }
            this.mAuto = false;
            if (DndTile.this.mController.getZen() != 0) {
                ZenModeConfig config = DndTile.this.mController.getConfig();
                ZenModeConfig.ZenRule zenRule = config.manualRule;
                String ownerCaption = (zenRule == null || (str = zenRule.enabler) == null) ? "" : getOwnerCaption(str);
                for (ZenModeConfig.ZenRule zenRule2 : config.automaticRules.values()) {
                    if (zenRule2.isAutomaticActive()) {
                        ownerCaption = ownerCaption.isEmpty() ? ((QSTileImpl) DndTile.this).mContext.getString(com.android.systemui.R.string.qs_dnd_prompt_auto_rule, zenRule2.name) : ((QSTileImpl) DndTile.this).mContext.getString(com.android.systemui.R.string.qs_dnd_prompt_auto_rule_app);
                    }
                }
                if (ownerCaption.isEmpty()) {
                    this.mZenPanel.setState(0);
                    return;
                }
                this.mAuto = true;
                this.mZenPanel.setState(1);
                this.mZenPanel.setAutoText(ownerCaption);
                return;
            }
            this.mZenPanel.setState(2);
        }

        private String getOwnerCaption(String str) {
            CharSequence charSequenceLoadLabel;
            PackageManager packageManager = ((QSTileImpl) DndTile.this).mContext.getPackageManager();
            try {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(str, 0);
                return (applicationInfo == null || (charSequenceLoadLabel = applicationInfo.loadLabel(packageManager)) == null) ? "" : ((QSTileImpl) DndTile.this).mContext.getString(com.android.systemui.R.string.qs_dnd_prompt_app, charSequenceLoadLabel.toString().trim());
            } catch (Throwable th) {
                Slog.w(((QSTileImpl) DndTile.this).TAG, "Error loading owner caption", th);
                return "";
            }
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewAttachedToWindow(View view) {
            DndTile.this.mShowingDetail = true;
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewDetachedFromWindow(View view) {
            DndTile.this.mShowingDetail = false;
            this.mZenPanel = null;
        }
    }
}
