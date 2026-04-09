package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.net.ConnectivityManager;
import android.net.IConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.VpnConfig;
import com.android.internal.net.VpnProfile;
import com.android.systemui.R;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.policy.SecurityController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

/* loaded from: classes.dex */
public class SecurityControllerImpl extends CurrentUserTracker implements SecurityController {
    private static final boolean DEBUG = Log.isLoggable("SecurityController", 3);
    private static final NetworkRequest REQUEST = new NetworkRequest.Builder().removeCapability(15).removeCapability(13).removeCapability(14).setUids(null).build();
    private final AppOpsManager mAppOpsManager;
    private final Executor mBgExecutor;
    private final BroadcastReceiver mBroadcastReceiver;

    @GuardedBy({"mCallbacks"})
    private final ArrayList<SecurityController.SecurityControllerCallback> mCallbacks;
    private final ConnectivityManager mConnectivityManager;
    private final IConnectivityManager mConnectivityManagerService;
    private final Context mContext;
    private int mCurrentUserId;
    private SparseArray<VpnConfig> mCurrentVpns;
    private final DevicePolicyManager mDevicePolicyManager;
    private ArrayMap<Integer, Boolean> mHasCACerts;
    private final ConnectivityManager.NetworkCallback mNetworkCallback;
    private final PackageManager mPackageManager;
    private final UserManager mUserManager;
    private int mVpnUserId;

    public SecurityControllerImpl(Context context, Handler handler, BroadcastDispatcher broadcastDispatcher, Executor executor) {
        super(broadcastDispatcher);
        this.mCallbacks = new ArrayList<>();
        this.mCurrentVpns = new SparseArray<>();
        this.mHasCACerts = new ArrayMap<>();
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() { // from class: com.android.systemui.statusbar.policy.SecurityControllerImpl.1
            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onAvailable(Network network) {
                if (SecurityControllerImpl.DEBUG) {
                    Log.d("SecurityController", "onAvailable " + network.netId);
                }
                SecurityControllerImpl.this.updateState();
                SecurityControllerImpl.this.fireCallbacks();
            }

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onLost(Network network) {
                if (SecurityControllerImpl.DEBUG) {
                    Log.d("SecurityController", "onLost " + network.netId);
                }
                SecurityControllerImpl.this.updateState();
                SecurityControllerImpl.this.fireCallbacks();
            }
        };
        this.mNetworkCallback = networkCallback;
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.policy.SecurityControllerImpl.2
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                int intExtra;
                if ("android.security.action.TRUST_STORE_CHANGED".equals(intent.getAction())) {
                    SecurityControllerImpl.this.refreshCACerts(getSendingUserId());
                } else {
                    if (!"android.intent.action.USER_UNLOCKED".equals(intent.getAction()) || (intExtra = intent.getIntExtra("android.intent.extra.user_handle", -10000)) == -10000) {
                        return;
                    }
                    SecurityControllerImpl.this.refreshCACerts(intExtra);
                }
            }
        };
        this.mBroadcastReceiver = broadcastReceiver;
        this.mContext = context;
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        this.mDevicePolicyManager = (DevicePolicyManager) context.getSystemService("device_policy");
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        this.mConnectivityManager = connectivityManager;
        this.mConnectivityManagerService = IConnectivityManager.Stub.asInterface(ServiceManager.getService("connectivity"));
        this.mPackageManager = context.getPackageManager();
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mBgExecutor = executor;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.security.action.TRUST_STORE_CHANGED");
        intentFilter.addAction("android.intent.action.USER_UNLOCKED");
        broadcastDispatcher.registerReceiverWithHandler(broadcastReceiver, intentFilter, handler, UserHandle.ALL);
        connectivityManager.registerNetworkCallback(REQUEST, networkCallback);
        onUserSwitched(ActivityManager.getCurrentUser());
        startTracking();
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("SecurityController state:");
        printWriter.print("  mCurrentVpns={");
        for (int i = 0; i < this.mCurrentVpns.size(); i++) {
            if (i > 0) {
                printWriter.print(", ");
            }
            printWriter.print(this.mCurrentVpns.keyAt(i));
            printWriter.print('=');
            printWriter.print(this.mCurrentVpns.valueAt(i).user);
        }
        printWriter.println("}");
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public boolean isDeviceManaged() {
        return this.mDevicePolicyManager.isDeviceManaged();
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public CharSequence getDeviceOwnerOrganizationName() {
        return this.mDevicePolicyManager.getDeviceOwnerOrganizationName();
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public CharSequence getWorkProfileOrganizationName() {
        int workProfileUserId = getWorkProfileUserId(this.mCurrentUserId);
        if (workProfileUserId == -10000) {
            return null;
        }
        return this.mDevicePolicyManager.getOrganizationNameForUser(workProfileUserId);
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public String getPrimaryVpnName() {
        VpnConfig vpnConfig = this.mCurrentVpns.get(this.mVpnUserId);
        if (vpnConfig != null) {
            return getNameForVpnConfig(vpnConfig, new UserHandle(this.mVpnUserId));
        }
        return null;
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public List<VpnProfile> getConfiguredLegacyVpns() {
        try {
            return Arrays.asList(this.mConnectivityManagerService.getAllLegacyVpns());
        } catch (RemoteException e) {
            Log.e("SecurityController", "Failed to connect", e);
            return new ArrayList();
        }
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public List<String> getVpnAppPackageNames() {
        boolean z;
        ArrayList arrayList = new ArrayList();
        List<AppOpsManager.PackageOps> packagesForOps = this.mAppOpsManager.getPackagesForOps(new int[]{47});
        if (packagesForOps != null) {
            for (AppOpsManager.PackageOps packageOps : packagesForOps) {
                if (this.mVpnUserId == UserHandle.getUserId(packageOps.getUid())) {
                    Iterator it = packageOps.getOps().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            z = false;
                            break;
                        }
                        AppOpsManager.OpEntry opEntry = (AppOpsManager.OpEntry) it.next();
                        if (opEntry.getOp() == 47 && opEntry.getMode() == 0) {
                            z = true;
                            break;
                        }
                    }
                    if (z) {
                        arrayList.add(packageOps.getPackageName());
                    }
                }
            }
        }
        return arrayList;
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public void connectLegacyVpn(VpnProfile vpnProfile) {
        try {
            this.mConnectivityManagerService.startLegacyVpn(vpnProfile);
        } catch (RemoteException | IllegalStateException e) {
            Log.e("SecurityController", "Failed to connect", e);
        }
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public void launchVpnApp(String str) {
        try {
            UserHandle userHandleOf = UserHandle.of(this.mCurrentUserId);
            Context context = this.mContext;
            Context contextCreatePackageContextAsUser = context.createPackageContextAsUser(context.getPackageName(), 0, userHandleOf);
            Intent launchIntentForPackage = contextCreatePackageContextAsUser.getPackageManager().getLaunchIntentForPackage(str);
            if (launchIntentForPackage != null) {
                contextCreatePackageContextAsUser.startActivityAsUser(launchIntentForPackage, userHandleOf);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.w("SecurityController", "VPN provider does not exist: " + str, e);
        }
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public void disconnectPrimaryVpn() {
        VpnConfig vpnConfig = this.mCurrentVpns.get(this.mVpnUserId);
        if (vpnConfig != null) {
            try {
                this.mConnectivityManagerService.prepareVpn(vpnConfig.legacy ? "[Legacy VPN]" : vpnConfig.user, "[Legacy VPN]", this.mVpnUserId);
            } catch (RemoteException e) {
                Log.e("SecurityController", "Failed to disconnect", e);
            }
        }
    }

    private int getWorkProfileUserId(int i) {
        for (UserInfo userInfo : this.mUserManager.getProfiles(i)) {
            if (userInfo.isManagedProfile()) {
                return userInfo.id;
            }
        }
        return -10000;
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public boolean hasWorkProfile() {
        return getWorkProfileUserId(this.mCurrentUserId) != -10000;
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public boolean isProfileOwnerOfOrganizationOwnedDevice() {
        return this.mDevicePolicyManager.isOrganizationOwnedDeviceWithManagedProfile();
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public String getWorkProfileVpnName() {
        VpnConfig vpnConfig;
        int workProfileUserId = getWorkProfileUserId(this.mVpnUserId);
        if (workProfileUserId == -10000 || (vpnConfig = this.mCurrentVpns.get(workProfileUserId)) == null) {
            return null;
        }
        return getNameForVpnConfig(vpnConfig, UserHandle.of(workProfileUserId));
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public boolean isNetworkLoggingEnabled() {
        return this.mDevicePolicyManager.isNetworkLoggingEnabled(null);
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public boolean isVpnEnabled() {
        for (int i : this.mUserManager.getProfileIdsWithDisabled(this.mVpnUserId)) {
            if (this.mCurrentVpns.get(i) != null) {
                return true;
            }
        }
        return false;
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public boolean isVpnRestricted() {
        return this.mUserManager.getUserInfo(this.mCurrentUserId).isRestricted() || this.mUserManager.hasUserRestriction("no_config_vpn", new UserHandle(this.mCurrentUserId));
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public boolean isVpnBranded() {
        String packageNameForVpnConfig;
        VpnConfig vpnConfig = this.mCurrentVpns.get(this.mVpnUserId);
        if (vpnConfig == null || (packageNameForVpnConfig = getPackageNameForVpnConfig(vpnConfig)) == null) {
            return false;
        }
        return isVpnPackageBranded(packageNameForVpnConfig);
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public boolean hasCACertInCurrentUser() {
        Boolean bool = this.mHasCACerts.get(Integer.valueOf(this.mCurrentUserId));
        return bool != null && bool.booleanValue();
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public boolean hasCACertInWorkProfile() {
        Boolean bool;
        int workProfileUserId = getWorkProfileUserId(this.mCurrentUserId);
        return (workProfileUserId == -10000 || (bool = this.mHasCACerts.get(Integer.valueOf(workProfileUserId))) == null || !bool.booleanValue()) ? false : true;
    }

    @Override // com.android.systemui.statusbar.policy.CallbackController
    public void removeCallback(SecurityController.SecurityControllerCallback securityControllerCallback) {
        synchronized (this.mCallbacks) {
            if (securityControllerCallback == null) {
                return;
            }
            if (DEBUG) {
                Log.d("SecurityController", "removeCallback " + securityControllerCallback);
            }
            this.mCallbacks.remove(securityControllerCallback);
        }
    }

    @Override // com.android.systemui.statusbar.policy.CallbackController
    public void addCallback(SecurityController.SecurityControllerCallback securityControllerCallback) {
        synchronized (this.mCallbacks) {
            if (securityControllerCallback != null) {
                if (!this.mCallbacks.contains(securityControllerCallback)) {
                    if (DEBUG) {
                        Log.d("SecurityController", "addCallback " + securityControllerCallback);
                    }
                    this.mCallbacks.add(securityControllerCallback);
                }
            }
        }
    }

    @Override // com.android.systemui.settings.CurrentUserTracker
    public void onUserSwitched(int i) {
        this.mCurrentUserId = i;
        String string = Settings.Global.getString(this.mContext.getContentResolver(), "global_vpn_app");
        UserInfo userInfo = this.mUserManager.getUserInfo(i);
        if (this.mCurrentVpns.get(0) != null && this.mCurrentVpns.get(0).user.equals(string)) {
            this.mVpnUserId = 0;
        } else if (userInfo.isRestricted()) {
            this.mVpnUserId = userInfo.restrictedProfileParentId;
        } else {
            this.mVpnUserId = this.mCurrentUserId;
        }
        fireCallbacks();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void refreshCACerts(final int i) {
        this.mBgExecutor.execute(new Runnable() { // from class: com.android.systemui.statusbar.policy.SecurityControllerImpl$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() throws Throwable {
                this.f$0.lambda$refreshCACerts$0(i);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:42:0x00a2  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public /* synthetic */ void lambda$refreshCACerts$0(int r8) throws java.lang.Throwable {
        /*
            r7 = this;
            java.lang.String r0 = "Refreshing CA Certs "
            java.lang.String r1 = "SecurityController"
            r2 = 0
            android.content.Context r3 = r7.mContext     // Catch: java.lang.Throwable -> L61 java.lang.Throwable -> L63
            android.os.UserHandle r4 = android.os.UserHandle.of(r8)     // Catch: java.lang.Throwable -> L61 java.lang.Throwable -> L63
            android.security.KeyChain$KeyChainConnection r3 = android.security.KeyChain.bindAsUser(r3, r4)     // Catch: java.lang.Throwable -> L61 java.lang.Throwable -> L63
            android.security.IKeyChainService r4 = r3.getService()     // Catch: java.lang.Throwable -> L55
            android.content.pm.StringParceledListSlice r4 = r4.getUserCaAliases()     // Catch: java.lang.Throwable -> L55
            java.util.List r4 = r4.getList()     // Catch: java.lang.Throwable -> L55
            boolean r4 = r4.isEmpty()     // Catch: java.lang.Throwable -> L55
            if (r4 != 0) goto L23
            r4 = 1
            goto L24
        L23:
            r4 = 0
        L24:
            android.util.Pair r5 = new android.util.Pair     // Catch: java.lang.Throwable -> L55
            java.lang.Integer r6 = java.lang.Integer.valueOf(r8)     // Catch: java.lang.Throwable -> L55
            java.lang.Boolean r4 = java.lang.Boolean.valueOf(r4)     // Catch: java.lang.Throwable -> L55
            r5.<init>(r6, r4)     // Catch: java.lang.Throwable -> L55
            r3.close()     // Catch: java.lang.Throwable -> L53 java.lang.Throwable -> L9c
            boolean r8 = com.android.systemui.statusbar.policy.SecurityControllerImpl.DEBUG
            if (r8 == 0) goto L4a
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            r8.append(r0)
            r8.append(r5)
            java.lang.String r8 = r8.toString()
            android.util.Log.d(r1, r8)
        L4a:
            java.lang.Object r8 = r5.second
            if (r8 == 0) goto L9b
            android.util.ArrayMap<java.lang.Integer, java.lang.Boolean> r0 = r7.mHasCACerts
            java.lang.Object r1 = r5.first
            goto L91
        L53:
            r3 = move-exception
            goto L65
        L55:
            r4 = move-exception
            if (r3 == 0) goto L60
            r3.close()     // Catch: java.lang.Throwable -> L5c
            goto L60
        L5c:
            r3 = move-exception
            r4.addSuppressed(r3)     // Catch: java.lang.Throwable -> L61 java.lang.Throwable -> L63 java.lang.Throwable -> L63 java.lang.Throwable -> L63
        L60:
            throw r4     // Catch: java.lang.Throwable -> L61 java.lang.Throwable -> L63 java.lang.Throwable -> L63 java.lang.Throwable -> L63
        L61:
            r8 = move-exception
            goto L9e
        L63:
            r3 = move-exception
            r5 = r2
        L65:
            java.lang.String r4 = "failed to get CA certs"
            android.util.Log.i(r1, r4, r3)     // Catch: java.lang.Throwable -> L9c
            android.util.Pair r3 = new android.util.Pair     // Catch: java.lang.Throwable -> L9c
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)     // Catch: java.lang.Throwable -> L9c
            r3.<init>(r8, r2)     // Catch: java.lang.Throwable -> L9c
            boolean r8 = com.android.systemui.statusbar.policy.SecurityControllerImpl.DEBUG
            if (r8 == 0) goto L89
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            r8.append(r0)
            r8.append(r3)
            java.lang.String r8 = r8.toString()
            android.util.Log.d(r1, r8)
        L89:
            java.lang.Object r8 = r3.second
            if (r8 == 0) goto L9b
            android.util.ArrayMap<java.lang.Integer, java.lang.Boolean> r0 = r7.mHasCACerts
            java.lang.Object r1 = r3.first
        L91:
            java.lang.Integer r1 = (java.lang.Integer) r1
            java.lang.Boolean r8 = (java.lang.Boolean) r8
            r0.put(r1, r8)
            r7.fireCallbacks()
        L9b:
            return
        L9c:
            r8 = move-exception
            r2 = r5
        L9e:
            boolean r3 = com.android.systemui.statusbar.policy.SecurityControllerImpl.DEBUG
            if (r3 == 0) goto Lb4
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r3.append(r0)
            r3.append(r2)
            java.lang.String r0 = r3.toString()
            android.util.Log.d(r1, r0)
        Lb4:
            if (r2 == 0) goto Lc8
            java.lang.Object r0 = r2.second
            if (r0 == 0) goto Lc8
            android.util.ArrayMap<java.lang.Integer, java.lang.Boolean> r1 = r7.mHasCACerts
            java.lang.Object r2 = r2.first
            java.lang.Integer r2 = (java.lang.Integer) r2
            java.lang.Boolean r0 = (java.lang.Boolean) r0
            r1.put(r2, r0)
            r7.fireCallbacks()
        Lc8:
            throw r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.SecurityControllerImpl.lambda$refreshCACerts$0(int):void");
    }

    private String getNameForVpnConfig(VpnConfig vpnConfig, UserHandle userHandle) {
        if (vpnConfig.legacy) {
            String str = vpnConfig.session;
            return str != null ? str : this.mContext.getString(R.string.legacy_vpn_name);
        }
        String str2 = vpnConfig.user;
        try {
            Context context = this.mContext;
            return VpnConfig.getVpnLabel(context.createPackageContextAsUser(context.getPackageName(), 0, userHandle), str2).toString();
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("SecurityController", "Package " + str2 + " is not present", e);
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fireCallbacks() {
        synchronized (this.mCallbacks) {
            Iterator<SecurityController.SecurityControllerCallback> it = this.mCallbacks.iterator();
            while (it.hasNext()) {
                it.next().onStateChanged();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateState() {
        LegacyVpnInfo legacyVpnInfo;
        SparseArray<VpnConfig> sparseArray = new SparseArray<>();
        try {
            for (UserInfo userInfo : this.mUserManager.getUsers()) {
                VpnConfig vpnConfig = this.mConnectivityManagerService.getVpnConfig(userInfo.id);
                if (vpnConfig != null && (!vpnConfig.legacy || ((legacyVpnInfo = this.mConnectivityManagerService.getLegacyVpnInfo(userInfo.id)) != null && legacyVpnInfo.state == 3))) {
                    sparseArray.put(userInfo.id, vpnConfig);
                }
            }
            this.mCurrentVpns = sparseArray;
        } catch (RemoteException e) {
            Log.e("SecurityController", "Unable to list active VPNs", e);
        }
    }

    private String getPackageNameForVpnConfig(VpnConfig vpnConfig) {
        if (vpnConfig.legacy) {
            return null;
        }
        return vpnConfig.user;
    }

    private boolean isVpnPackageBranded(String str) throws PackageManager.NameNotFoundException {
        try {
            ApplicationInfo applicationInfo = this.mPackageManager.getApplicationInfo(str, 128);
            if (applicationInfo != null && applicationInfo.metaData != null && applicationInfo.isSystemApp()) {
                return applicationInfo.metaData.getBoolean("com.android.systemui.IS_BRANDED", false);
            }
        } catch (PackageManager.NameNotFoundException unused) {
        }
        return false;
    }
}
