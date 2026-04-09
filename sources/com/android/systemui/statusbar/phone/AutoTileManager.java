package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.display.ColorDisplayManager;
import android.hardware.display.NightDisplayListener;
import android.os.Handler;
import android.os.UserHandle;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.R;
import com.android.systemui.qs.AutoAddTracker;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.SecureSetting;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.statusbar.phone.ManagedProfileController;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.util.UserAwareController;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

/* loaded from: classes.dex */
public class AutoTileManager implements UserAwareController {
    protected final AutoAddTracker mAutoTracker;
    private final CastController mCastController;
    protected final Context mContext;
    private UserHandle mCurrentUser;
    private final DataSaverController mDataSaverController;
    protected final Handler mHandler;
    protected final QSTileHost mHost;
    private final HotspotController mHotspotController;
    private boolean mInitialized;
    private final ManagedProfileController mManagedProfileController;
    private final NightDisplayListener mNightDisplayListener;
    private final ArrayList<AutoAddSetting> mAutoAddSettingList = new ArrayList<>();
    private final ManagedProfileController.Callback mProfileCallback = new ManagedProfileController.Callback() { // from class: com.android.systemui.statusbar.phone.AutoTileManager.1
        @Override // com.android.systemui.statusbar.phone.ManagedProfileController.Callback
        public void onManagedProfileRemoved() {
        }

        @Override // com.android.systemui.statusbar.phone.ManagedProfileController.Callback
        public void onManagedProfileChanged() {
            if (!AutoTileManager.this.mAutoTracker.isAdded("work") && AutoTileManager.this.mManagedProfileController.hasActiveProfile()) {
                AutoTileManager.this.mHost.addTile("work");
                AutoTileManager.this.mAutoTracker.setTileAdded("work");
            }
        }
    };
    private final DataSaverController.Listener mDataSaverListener = new AnonymousClass2();
    private final HotspotController.Callback mHotspotCallback = new AnonymousClass3();

    @VisibleForTesting
    final NightDisplayListener.Callback mNightDisplayCallback = new AnonymousClass4();

    @VisibleForTesting
    final CastController.Callback mCastCallback = new AnonymousClass5();

    public AutoTileManager(Context context, AutoAddTracker.Builder builder, QSTileHost qSTileHost, Handler handler, HotspotController hotspotController, DataSaverController dataSaverController, ManagedProfileController managedProfileController, NightDisplayListener nightDisplayListener, CastController castController) {
        this.mContext = context;
        this.mHost = qSTileHost;
        UserHandle user = qSTileHost.getUserContext().getUser();
        this.mCurrentUser = user;
        this.mAutoTracker = builder.setUserId(user.getIdentifier()).build();
        this.mHandler = handler;
        this.mHotspotController = hotspotController;
        this.mDataSaverController = dataSaverController;
        this.mManagedProfileController = managedProfileController;
        this.mNightDisplayListener = nightDisplayListener;
        this.mCastController = castController;
    }

    public void init() throws Resources.NotFoundException {
        if (this.mInitialized) {
            Log.w("AutoTileManager", "Trying to re-initialize");
            return;
        }
        this.mAutoTracker.initialize();
        populateSettingsList();
        startControllersAndSettingsListeners();
        this.mInitialized = true;
    }

    protected void startControllersAndSettingsListeners() {
        if (!this.mAutoTracker.isAdded("hotspot")) {
            this.mHotspotController.addCallback(this.mHotspotCallback);
        }
        if (!this.mAutoTracker.isAdded("saver")) {
            this.mDataSaverController.addCallback(this.mDataSaverListener);
        }
        if (!this.mAutoTracker.isAdded("work")) {
            this.mManagedProfileController.addCallback(this.mProfileCallback);
        }
        if (!this.mAutoTracker.isAdded("night") && ColorDisplayManager.isNightDisplayAvailable(this.mContext)) {
            this.mNightDisplayListener.setCallback(this.mNightDisplayCallback);
        }
        if (!this.mAutoTracker.isAdded("cast")) {
            this.mCastController.addCallback(this.mCastCallback);
        }
        int size = this.mAutoAddSettingList.size();
        for (int i = 0; i < size; i++) {
            if (!this.mAutoTracker.isAdded(this.mAutoAddSettingList.get(i).mSpec)) {
                this.mAutoAddSettingList.get(i).setListening(true);
            }
        }
    }

    protected void stopListening() {
        this.mHotspotController.removeCallback(this.mHotspotCallback);
        this.mDataSaverController.removeCallback(this.mDataSaverListener);
        this.mManagedProfileController.removeCallback(this.mProfileCallback);
        if (ColorDisplayManager.isNightDisplayAvailable(this.mContext)) {
            this.mNightDisplayListener.setCallback((NightDisplayListener.Callback) null);
        }
        this.mCastController.removeCallback(this.mCastCallback);
        int size = this.mAutoAddSettingList.size();
        for (int i = 0; i < size; i++) {
            this.mAutoAddSettingList.get(i).setListening(false);
        }
    }

    private void populateSettingsList() throws Resources.NotFoundException {
        try {
            for (String str : this.mContext.getResources().getStringArray(R.array.config_quickSettingsAutoAdd)) {
                String[] strArrSplit = str.split(":");
                if (strArrSplit.length == 2) {
                    this.mAutoAddSettingList.add(new AutoAddSetting(this.mContext, this.mHandler, strArrSplit[0], strArrSplit[1]));
                } else {
                    Log.w("AutoTileManager", "Malformed item in array: " + str);
                }
            }
        } catch (Resources.NotFoundException unused) {
            Log.w("AutoTileManager", "Missing config resource");
        }
    }

    @Override // com.android.systemui.util.UserAwareController
    /* renamed from: changeUser, reason: merged with bridge method [inline-methods] */
    public void lambda$changeUser$0(final UserHandle userHandle) {
        if (!this.mInitialized) {
            throw new IllegalStateException("AutoTileManager not initialized");
        }
        if (!Thread.currentThread().equals(this.mHandler.getLooper().getThread())) {
            this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.AutoTileManager$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$changeUser$0(userHandle);
                }
            });
            return;
        }
        if (userHandle.getIdentifier() == this.mCurrentUser.getIdentifier()) {
            return;
        }
        stopListening();
        this.mCurrentUser = userHandle;
        int size = this.mAutoAddSettingList.size();
        for (int i = 0; i < size; i++) {
            this.mAutoAddSettingList.get(i).setUserId(userHandle.getIdentifier());
        }
        this.mAutoTracker.lambda$changeUser$0(userHandle);
        startControllersAndSettingsListeners();
    }

    public void unmarkTileAsAutoAdded(String str) {
        this.mAutoTracker.setTileRemoved(str);
    }

    /* renamed from: com.android.systemui.statusbar.phone.AutoTileManager$2, reason: invalid class name */
    class AnonymousClass2 implements DataSaverController.Listener {
        AnonymousClass2() {
        }

        @Override // com.android.systemui.statusbar.policy.DataSaverController.Listener
        public void onDataSaverChanged(boolean z) {
            if (!AutoTileManager.this.mAutoTracker.isAdded("saver") && z) {
                AutoTileManager.this.mHost.addTile("saver");
                AutoTileManager.this.mAutoTracker.setTileAdded("saver");
                AutoTileManager.this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.AutoTileManager$2$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onDataSaverChanged$0();
                    }
                });
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onDataSaverChanged$0() {
            AutoTileManager.this.mDataSaverController.removeCallback(AutoTileManager.this.mDataSaverListener);
        }
    }

    /* renamed from: com.android.systemui.statusbar.phone.AutoTileManager$3, reason: invalid class name */
    class AnonymousClass3 implements HotspotController.Callback {
        AnonymousClass3() {
        }

        @Override // com.android.systemui.statusbar.policy.HotspotController.Callback
        public void onHotspotChanged(boolean z, int i) {
            if (!AutoTileManager.this.mAutoTracker.isAdded("hotspot") && z) {
                AutoTileManager.this.mHost.addTile("hotspot");
                AutoTileManager.this.mAutoTracker.setTileAdded("hotspot");
                AutoTileManager.this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.AutoTileManager$3$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onHotspotChanged$0();
                    }
                });
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onHotspotChanged$0() {
            AutoTileManager.this.mHotspotController.removeCallback(AutoTileManager.this.mHotspotCallback);
        }
    }

    /* renamed from: com.android.systemui.statusbar.phone.AutoTileManager$4, reason: invalid class name */
    class AnonymousClass4 implements NightDisplayListener.Callback {
        AnonymousClass4() {
        }

        public void onActivated(boolean z) {
            if (z) {
                addNightTile();
            }
        }

        public void onAutoModeChanged(int i) {
            if (i == 1 || i == 2) {
                addNightTile();
            }
        }

        private void addNightTile() {
            if (AutoTileManager.this.mAutoTracker.isAdded("night")) {
                return;
            }
            AutoTileManager.this.mHost.addTile("night");
            AutoTileManager.this.mAutoTracker.setTileAdded("night");
            AutoTileManager.this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.AutoTileManager$4$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$addNightTile$0();
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$addNightTile$0() {
            AutoTileManager.this.mNightDisplayListener.setCallback((NightDisplayListener.Callback) null);
        }
    }

    /* renamed from: com.android.systemui.statusbar.phone.AutoTileManager$5, reason: invalid class name */
    class AnonymousClass5 implements CastController.Callback {
        AnonymousClass5() {
        }

        @Override // com.android.systemui.statusbar.policy.CastController.Callback
        public void onCastDevicesChanged() {
            if (AutoTileManager.this.mAutoTracker.isAdded("cast")) {
                return;
            }
            boolean z = false;
            Iterator<CastController.CastDevice> it = AutoTileManager.this.mCastController.getCastDevices().iterator();
            while (it.hasNext()) {
                int i = it.next().state;
                if (i == 2 || i == 1) {
                    z = true;
                    break;
                }
            }
            if (z) {
                AutoTileManager.this.mHost.addTile("cast");
                AutoTileManager.this.mAutoTracker.setTileAdded("cast");
                AutoTileManager.this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.AutoTileManager$5$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onCastDevicesChanged$0();
                    }
                });
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onCastDevicesChanged$0() {
            AutoTileManager.this.mCastController.removeCallback(AutoTileManager.this.mCastCallback);
        }
    }

    @VisibleForTesting
    protected SecureSetting getSecureSettingForKey(String str) {
        Iterator<AutoAddSetting> it = this.mAutoAddSettingList.iterator();
        while (it.hasNext()) {
            AutoAddSetting next = it.next();
            if (Objects.equals(str, next.getKey())) {
                return next;
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    class AutoAddSetting extends SecureSetting {
        private final String mSpec;

        AutoAddSetting(Context context, Handler handler, String str, String str2) {
            super(context, handler, str);
            this.mSpec = str2;
        }

        @Override // com.android.systemui.qs.SecureSetting
        protected void handleValueChanged(int i, boolean z) {
            if (AutoTileManager.this.mAutoTracker.isAdded(this.mSpec)) {
                AutoTileManager.this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.AutoTileManager$AutoAddSetting$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$handleValueChanged$0();
                    }
                });
                return;
            }
            if (i != 0) {
                if (this.mSpec.startsWith("custom(")) {
                    AutoTileManager.this.mHost.addTile(CustomTile.getComponentFromSpec(this.mSpec), true);
                } else {
                    AutoTileManager.this.mHost.addTile(this.mSpec);
                }
                AutoTileManager.this.mAutoTracker.setTileAdded(this.mSpec);
                AutoTileManager.this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.AutoTileManager$AutoAddSetting$$ExternalSyntheticLambda1
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$handleValueChanged$1();
                    }
                });
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$handleValueChanged$0() {
            setListening(false);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$handleValueChanged$1() {
            setListening(false);
        }
    }
}
