package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.IActivityTaskManager;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.UiEventLogger;
import com.android.settingslib.R$string;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtilsInternal;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dumpable;
import com.android.systemui.GuestResumeSessionReceiver;
import com.android.systemui.Prefs;
import com.android.systemui.R;
import com.android.systemui.SystemUISecondaryUserService;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.qs.QSUserSwitcherEvent;
import com.android.systemui.qs.tiles.UserDetailView;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.user.CreateUserActivity;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/* loaded from: classes.dex */
public class UserSwitcherController implements Dumpable {
    private final ActivityStarter mActivityStarter;
    private final IActivityTaskManager mActivityTaskManager;
    private final ArrayList<WeakReference<BaseUserAdapter>> mAdapters = new ArrayList<>();
    private Dialog mAddUserDialog;
    private boolean mAddUsersWhenLocked;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final KeyguardStateController.Callback mCallback;
    protected final Context mContext;
    private Dialog mExitGuestDialog;
    private SparseBooleanArray mForcePictureLoadForUserId;
    private final GuestResumeSessionReceiver mGuestResumeSessionReceiver;
    protected final Handler mHandler;
    private final KeyguardStateController mKeyguardStateController;
    private int mLastNonGuestUser;
    private boolean mPauseRefreshUsers;
    private final PhoneStateListener mPhoneStateListener;
    private BroadcastReceiver mReceiver;
    private boolean mResumeUserOnGuestLogout;
    private int mSecondaryUser;
    private Intent mSecondaryUserServiceIntent;
    private final ContentObserver mSettingsObserver;
    private boolean mSimpleUserSwitcher;
    private final UiEventLogger mUiEventLogger;
    private final Runnable mUnpauseRefreshUsers;
    protected final UserManager mUserManager;
    private ArrayList<UserRecord> mUsers;
    public final DetailAdapter userDetailAdapter;

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void access$200(UserSwitcherController userSwitcherController) {
        userSwitcherController.notifyAdapters();
    }

    public UserSwitcherController(Context context, KeyguardStateController keyguardStateController, Handler handler, ActivityStarter activityStarter, BroadcastDispatcher broadcastDispatcher, UiEventLogger uiEventLogger, IActivityTaskManager iActivityTaskManager) {
        GuestResumeSessionReceiver guestResumeSessionReceiver = new GuestResumeSessionReceiver();
        this.mGuestResumeSessionReceiver = guestResumeSessionReceiver;
        this.mUsers = new ArrayList<>();
        this.mLastNonGuestUser = 0;
        this.mResumeUserOnGuestLogout = true;
        this.mSecondaryUser = -10000;
        this.mForcePictureLoadForUserId = new SparseBooleanArray(2);
        this.mPhoneStateListener = new PhoneStateListener() { // from class: com.android.systemui.statusbar.policy.UserSwitcherController.2
            private int mCallState;

            @Override // android.telephony.PhoneStateListener
            public void onCallStateChanged(int i, String str) {
                if (this.mCallState == i) {
                    return;
                }
                this.mCallState = i;
                UserSwitcherController.this.refreshUsers(-10000);
            }
        };
        this.mReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.policy.UserSwitcherController.3
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                boolean z = true;
                int intExtra = -10000;
                if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                    if (UserSwitcherController.this.mExitGuestDialog != null && UserSwitcherController.this.mExitGuestDialog.isShowing()) {
                        UserSwitcherController.this.mExitGuestDialog.cancel();
                        UserSwitcherController.this.mExitGuestDialog = null;
                    }
                    int intExtra2 = intent.getIntExtra("android.intent.extra.user_handle", -1);
                    UserInfo userInfo = UserSwitcherController.this.mUserManager.getUserInfo(intExtra2);
                    int size = UserSwitcherController.this.mUsers.size();
                    int i = 0;
                    while (i < size) {
                        UserRecord userRecord = (UserRecord) UserSwitcherController.this.mUsers.get(i);
                        UserInfo userInfo2 = userRecord.info;
                        if (userInfo2 != null) {
                            boolean z2 = userInfo2.id == intExtra2;
                            if (userRecord.isCurrent != z2) {
                                UserSwitcherController.this.mUsers.set(i, userRecord.copyWithIsCurrent(z2));
                            }
                            if (z2 && !userRecord.isGuest) {
                                UserSwitcherController.this.mLastNonGuestUser = userRecord.info.id;
                            }
                            if ((userInfo == null || !userInfo.isAdmin()) && userRecord.isRestricted) {
                                UserSwitcherController.this.mUsers.remove(i);
                                i--;
                            }
                        }
                        i++;
                    }
                    UserSwitcherController.this.notifyAdapters();
                    if (UserSwitcherController.this.mSecondaryUser != -10000) {
                        context2.stopServiceAsUser(UserSwitcherController.this.mSecondaryUserServiceIntent, UserHandle.of(UserSwitcherController.this.mSecondaryUser));
                        UserSwitcherController.this.mSecondaryUser = -10000;
                    }
                    if (userInfo != null && userInfo.id != 0) {
                        context2.startServiceAsUser(UserSwitcherController.this.mSecondaryUserServiceIntent, UserHandle.of(userInfo.id));
                        UserSwitcherController.this.mSecondaryUser = userInfo.id;
                    }
                } else {
                    if ("android.intent.action.USER_INFO_CHANGED".equals(intent.getAction())) {
                        intExtra = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                    } else if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction()) && intent.getIntExtra("android.intent.extra.user_handle", -10000) != 0) {
                        return;
                    }
                    z = false;
                }
                UserSwitcherController.this.refreshUsers(intExtra);
                if (z) {
                    UserSwitcherController.this.mUnpauseRefreshUsers.run();
                }
            }
        };
        this.mUnpauseRefreshUsers = new Runnable() { // from class: com.android.systemui.statusbar.policy.UserSwitcherController.4
            @Override // java.lang.Runnable
            public void run() {
                UserSwitcherController.this.mHandler.removeCallbacks(this);
                UserSwitcherController.this.mPauseRefreshUsers = false;
                UserSwitcherController.this.refreshUsers(-10000);
            }
        };
        ContentObserver contentObserver = new ContentObserver(new Handler()) { // from class: com.android.systemui.statusbar.policy.UserSwitcherController.5
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                UserSwitcherController userSwitcherController = UserSwitcherController.this;
                userSwitcherController.mSimpleUserSwitcher = userSwitcherController.shouldUseSimpleUserSwitcher();
                UserSwitcherController userSwitcherController2 = UserSwitcherController.this;
                userSwitcherController2.mAddUsersWhenLocked = Settings.Global.getInt(userSwitcherController2.mContext.getContentResolver(), "add_users_when_locked", 0) != 0;
                UserSwitcherController.this.refreshUsers(-10000);
            }
        };
        this.mSettingsObserver = contentObserver;
        this.userDetailAdapter = new DetailAdapter() { // from class: com.android.systemui.statusbar.policy.UserSwitcherController.6
            private final Intent USER_SETTINGS_INTENT = new Intent("android.settings.USER_SETTINGS");

            @Override // com.android.systemui.plugins.qs.DetailAdapter
            public int getMetricsCategory() {
                return 125;
            }

            @Override // com.android.systemui.plugins.qs.DetailAdapter
            public Boolean getToggleState() {
                return null;
            }

            @Override // com.android.systemui.plugins.qs.DetailAdapter
            public void setToggleState(boolean z) {
            }

            @Override // com.android.systemui.plugins.qs.DetailAdapter
            public CharSequence getTitle() {
                return UserSwitcherController.this.mContext.getString(R.string.quick_settings_user_title);
            }

            @Override // com.android.systemui.plugins.qs.DetailAdapter
            public View createDetailView(Context context2, View view, ViewGroup viewGroup) {
                UserDetailView userDetailViewInflate;
                if (!(view instanceof UserDetailView)) {
                    userDetailViewInflate = UserDetailView.inflate(context2, viewGroup, false);
                    UserSwitcherController userSwitcherController = UserSwitcherController.this;
                    userDetailViewInflate.createAndSetAdapter(userSwitcherController, userSwitcherController.mUiEventLogger);
                } else {
                    userDetailViewInflate = (UserDetailView) view;
                }
                userDetailViewInflate.refreshAdapter();
                return userDetailViewInflate;
            }

            @Override // com.android.systemui.plugins.qs.DetailAdapter
            public Intent getSettingsIntent() {
                return this.USER_SETTINGS_INTENT;
            }

            @Override // com.android.systemui.plugins.qs.DetailAdapter
            public UiEventLogger.UiEventEnum openDetailEvent() {
                return QSUserSwitcherEvent.QS_USER_DETAIL_OPEN;
            }

            @Override // com.android.systemui.plugins.qs.DetailAdapter
            public UiEventLogger.UiEventEnum closeDetailEvent() {
                return QSUserSwitcherEvent.QS_USER_DETAIL_CLOSE;
            }

            @Override // com.android.systemui.plugins.qs.DetailAdapter
            public UiEventLogger.UiEventEnum moreSettingsEvent() {
                return QSUserSwitcherEvent.QS_USER_MORE_SETTINGS;
            }
        };
        AnonymousClass7 anonymousClass7 = new AnonymousClass7();
        this.mCallback = anonymousClass7;
        this.mContext = context;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mActivityTaskManager = iActivityTaskManager;
        this.mUiEventLogger = uiEventLogger;
        if (!UserManager.isGuestUserEphemeral()) {
            guestResumeSessionReceiver.register(broadcastDispatcher);
        }
        this.mKeyguardStateController = keyguardStateController;
        this.mHandler = handler;
        this.mActivityStarter = activityStarter;
        this.mUserManager = UserManager.get(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_ADDED");
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        intentFilter.addAction("android.intent.action.USER_INFO_CHANGED");
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.USER_STOPPED");
        intentFilter.addAction("android.intent.action.USER_UNLOCKED");
        broadcastDispatcher.registerReceiver(this.mReceiver, intentFilter, null, UserHandle.SYSTEM);
        this.mSimpleUserSwitcher = shouldUseSimpleUserSwitcher();
        this.mSecondaryUserServiceIntent = new Intent(context, (Class<?>) SystemUISecondaryUserService.class);
        context.registerReceiverAsUser(this.mReceiver, UserHandle.SYSTEM, new IntentFilter(), "com.android.systemui.permission.SELF", null);
        context.getContentResolver().registerContentObserver(Settings.Global.getUriFor("lockscreenSimpleUserSwitcher"), true, contentObserver);
        context.getContentResolver().registerContentObserver(Settings.Global.getUriFor("add_users_when_locked"), true, contentObserver);
        context.getContentResolver().registerContentObserver(Settings.Global.getUriFor("allow_user_switching_when_system_user_locked"), true, contentObserver);
        contentObserver.onChange(false);
        keyguardStateController.addCallback(anonymousClass7);
        listenForCallState();
        refreshUsers(-10000);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void refreshUsers(int i) {
        UserInfo userInfo;
        if (i != -10000) {
            this.mForcePictureLoadForUserId.put(i, true);
        }
        if (this.mPauseRefreshUsers) {
            return;
        }
        boolean z = this.mForcePictureLoadForUserId.get(-1);
        SparseArray<Bitmap> sparseArray = new SparseArray<>(this.mUsers.size());
        int size = this.mUsers.size();
        for (int i2 = 0; i2 < size; i2++) {
            UserRecord userRecord = this.mUsers.get(i2);
            if (userRecord != null && userRecord.picture != null && (userInfo = userRecord.info) != null && !z && !this.mForcePictureLoadForUserId.get(userInfo.id)) {
                sparseArray.put(userRecord.info.id, userRecord.picture);
            }
        }
        this.mForcePictureLoadForUserId.clear();
        final boolean z2 = this.mAddUsersWhenLocked;
        new AsyncTask<SparseArray<Bitmap>, Void, ArrayList<UserRecord>>() { // from class: com.android.systemui.statusbar.policy.UserSwitcherController.1
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public ArrayList<UserRecord> doInBackground(SparseArray<Bitmap>... sparseArrayArr) throws Resources.NotFoundException {
                boolean z3 = false;
                SparseArray<Bitmap> sparseArray2 = sparseArrayArr[0];
                List<UserInfo> users = UserSwitcherController.this.mUserManager.getUsers(true);
                UserRecord userRecord2 = null;
                if (users == null) {
                    return null;
                }
                ArrayList<UserRecord> arrayList = new ArrayList<>(users.size());
                int currentUser = ActivityManager.getCurrentUser();
                boolean z4 = UserSwitcherController.this.mUserManager.getUserSwitchability(UserHandle.of(ActivityManager.getCurrentUser())) == 0;
                UserInfo userInfo2 = null;
                for (UserInfo userInfo3 : users) {
                    boolean z5 = currentUser == userInfo3.id;
                    UserInfo userInfo4 = z5 ? userInfo3 : userInfo2;
                    boolean z6 = z4 || z5;
                    if (userInfo3.isEnabled()) {
                        if (userInfo3.isGuest()) {
                            userRecord2 = new UserRecord(userInfo3, null, true, z5, false, false, z4);
                        } else if (userInfo3.supportsSwitchToByUser()) {
                            Bitmap userIcon = sparseArray2.get(userInfo3.id);
                            if (userIcon == null && (userIcon = UserSwitcherController.this.mUserManager.getUserIcon(userInfo3.id)) != null) {
                                int dimensionPixelSize = UserSwitcherController.this.mContext.getResources().getDimensionPixelSize(R.dimen.max_avatar_size);
                                userIcon = Bitmap.createScaledBitmap(userIcon, dimensionPixelSize, dimensionPixelSize, true);
                            }
                            arrayList.add(new UserRecord(userInfo3, userIcon, false, z5, false, false, z6));
                        }
                    }
                    userInfo2 = userInfo4;
                }
                if (arrayList.size() > 1 || userRecord2 != null) {
                    Prefs.putBoolean(UserSwitcherController.this.mContext, "HasSeenMultiUser", true);
                }
                boolean z7 = !UserSwitcherController.this.mUserManager.hasBaseUserRestriction("no_add_user", UserHandle.SYSTEM);
                boolean z8 = userInfo2 != null && (userInfo2.isAdmin() || userInfo2.id == 0) && z7;
                boolean z9 = z7 && z2;
                boolean z10 = (z8 || z9) && userRecord2 == null;
                if ((z8 || z9) && UserSwitcherController.this.mUserManager.canAddMoreUsers()) {
                    z3 = true;
                }
                boolean z11 = !z2;
                if (userRecord2 != null) {
                    arrayList.add(userRecord2);
                } else if (z10) {
                    UserRecord userRecord3 = new UserRecord(null, null, true, false, false, z11, z4);
                    UserSwitcherController.this.checkIfAddUserDisallowedByAdminOnly(userRecord3);
                    arrayList.add(userRecord3);
                }
                if (z3) {
                    UserRecord userRecord4 = new UserRecord(null, null, false, false, true, z11, z4);
                    UserSwitcherController.this.checkIfAddUserDisallowedByAdminOnly(userRecord4);
                    arrayList.add(userRecord4);
                }
                return arrayList;
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(ArrayList<UserRecord> arrayList) {
                if (arrayList != null) {
                    UserSwitcherController.this.mUsers = arrayList;
                    UserSwitcherController.this.notifyAdapters();
                }
            }
        }.execute(sparseArray);
    }

    private void pauseRefreshUsers() {
        if (this.mPauseRefreshUsers) {
            return;
        }
        this.mHandler.postDelayed(this.mUnpauseRefreshUsers, 3000L);
        this.mPauseRefreshUsers = true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyAdapters() {
        for (int size = this.mAdapters.size() - 1; size >= 0; size--) {
            BaseUserAdapter baseUserAdapter = this.mAdapters.get(size).get();
            if (baseUserAdapter != null) {
                baseUserAdapter.notifyDataSetChanged();
            } else {
                this.mAdapters.remove(size);
            }
        }
    }

    public boolean isSimpleUserSwitcher() {
        return this.mSimpleUserSwitcher;
    }

    public boolean useFullscreenUserSwitcher() {
        int iIntValue = ((Integer) DejankUtils.whitelistIpcs(new Supplier() { // from class: com.android.systemui.statusbar.policy.UserSwitcherController$$ExternalSyntheticLambda0
            @Override // java.util.function.Supplier
            public final Object get() {
                return this.f$0.lambda$useFullscreenUserSwitcher$0();
            }
        })).intValue();
        if (iIntValue != -1) {
            return iIntValue != 0;
        }
        return this.mContext.getResources().getBoolean(R.bool.config_enableFullscreenUserSwitcher);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ Integer lambda$useFullscreenUserSwitcher$0() {
        return Integer.valueOf(Settings.System.getInt(this.mContext.getContentResolver(), "enable_fullscreen_user_switcher", -1));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onUserListItemClicked(UserRecord userRecord) {
        int i;
        UserInfo userInfo;
        if (userRecord.isGuest && userRecord.info == null) {
            try {
                UserManager userManager = this.mUserManager;
                Context context = this.mContext;
                UserInfo userInfoCreateGuest = userManager.createGuest(context, context.getString(R$string.guest_nickname));
                if (userInfoCreateGuest == null) {
                    return;
                } else {
                    i = userInfoCreateGuest.id;
                }
            } catch (UserManager.UserOperationException e) {
                Log.e("UserSwitcherController", "Couldn't create guest user", e);
                return;
            }
        } else {
            if (userRecord.isAddUser) {
                showAddUserDialog();
                return;
            }
            i = userRecord.info.id;
        }
        int currentUser = ActivityManager.getCurrentUser();
        if (currentUser == i) {
            if (userRecord.isGuest) {
                showExitGuestDialog(i);
            }
        } else if (UserManager.isGuestUserEphemeral() && (userInfo = this.mUserManager.getUserInfo(currentUser)) != null && userInfo.isGuest()) {
            showExitGuestDialog(currentUser, userRecord.resolveId());
        } else {
            switchToUserId(i);
        }
    }

    protected void switchToUserId(int i) {
        try {
            pauseRefreshUsers();
            ActivityManager.getService().switchUser(i);
        } catch (RemoteException e) {
            Log.e("UserSwitcherController", "Couldn't switch user.", e);
        }
    }

    private void showExitGuestDialog(int i) {
        int i2;
        UserInfo userInfo;
        showExitGuestDialog(i, (this.mResumeUserOnGuestLogout && (i2 = this.mLastNonGuestUser) != 0 && (userInfo = this.mUserManager.getUserInfo(i2)) != null && userInfo.isEnabled() && userInfo.supportsSwitchToByUser()) ? userInfo.id : 0);
    }

    protected void showExitGuestDialog(int i, int i2) {
        Dialog dialog = this.mExitGuestDialog;
        if (dialog != null && dialog.isShowing()) {
            this.mExitGuestDialog.cancel();
        }
        ExitGuestDialog exitGuestDialog = new ExitGuestDialog(this.mContext, i, i2);
        this.mExitGuestDialog = exitGuestDialog;
        exitGuestDialog.show();
    }

    public void showAddUserDialog() {
        Dialog dialog = this.mAddUserDialog;
        if (dialog != null && dialog.isShowing()) {
            this.mAddUserDialog.cancel();
        }
        AddUserDialog addUserDialog = new AddUserDialog(this.mContext);
        this.mAddUserDialog = addUserDialog;
        addUserDialog.show();
    }

    protected void exitGuest(int i, int i2) {
        switchToUserId(i2);
        this.mUserManager.removeUser(i);
    }

    private void listenForCallState() {
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        if (telephonyManager != null) {
            telephonyManager.listen(this.mPhoneStateListener, 32);
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("UserSwitcherController state:");
        printWriter.println("  mLastNonGuestUser=" + this.mLastNonGuestUser);
        printWriter.print("  mUsers.size=");
        printWriter.println(this.mUsers.size());
        for (int i = 0; i < this.mUsers.size(); i++) {
            UserRecord userRecord = this.mUsers.get(i);
            printWriter.print("    ");
            printWriter.println(userRecord.toString());
        }
        printWriter.println("mSimpleUserSwitcher=" + this.mSimpleUserSwitcher);
    }

    public String getCurrentUserName(Context context) {
        UserRecord userRecord;
        UserInfo userInfo;
        if (this.mUsers.isEmpty() || (userRecord = this.mUsers.get(0)) == null || (userInfo = userRecord.info) == null) {
            return null;
        }
        return userRecord.isGuest ? context.getString(R$string.guest_nickname) : userInfo.name;
    }

    public void onDensityOrFontScaleChanged() {
        refreshUsers(-1);
    }

    @VisibleForTesting
    public void addAdapter(WeakReference<BaseUserAdapter> weakReference) {
        this.mAdapters.add(weakReference);
    }

    @VisibleForTesting
    public ArrayList<UserRecord> getUsers() {
        return this.mUsers;
    }

    public static abstract class BaseUserAdapter extends BaseAdapter {
        final UserSwitcherController mController;
        private final KeyguardStateController mKeyguardStateController;

        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return i;
        }

        protected BaseUserAdapter(UserSwitcherController userSwitcherController) {
            this.mController = userSwitcherController;
            this.mKeyguardStateController = userSwitcherController.mKeyguardStateController;
            userSwitcherController.addAdapter(new WeakReference<>(this));
        }

        @Override // android.widget.Adapter
        public int getCount() {
            if (!(this.mKeyguardStateController.isShowing() && this.mKeyguardStateController.isMethodSecure() && !this.mKeyguardStateController.canDismissLockScreen())) {
                return this.mController.getUsers().size();
            }
            int size = this.mController.getUsers().size();
            int i = 0;
            for (int i2 = 0; i2 < size && !this.mController.getUsers().get(i2).isRestricted; i2++) {
                i++;
            }
            return i;
        }

        @Override // android.widget.Adapter
        public UserRecord getItem(int i) {
            return this.mController.getUsers().get(i);
        }

        public void onUserListItemClicked(UserRecord userRecord) {
            this.mController.onUserListItemClicked(userRecord);
        }

        public String getName(Context context, UserRecord userRecord) {
            if (userRecord.isGuest) {
                if (userRecord.isCurrent) {
                    return context.getString(R$string.guest_exit_guest);
                }
                return context.getString(userRecord.info == null ? R$string.guest_new_guest : R$string.guest_nickname);
            }
            if (userRecord.isAddUser) {
                return context.getString(R.string.user_add_user);
            }
            return userRecord.info.name;
        }

        protected static ColorFilter getDisabledUserAvatarColorFilter() {
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0.0f);
            return new ColorMatrixColorFilter(colorMatrix);
        }

        protected static Drawable getIconDrawable(Context context, UserRecord userRecord) {
            int i;
            if (userRecord.isAddUser) {
                i = R.drawable.ic_add_circle;
            } else if (userRecord.isGuest) {
                i = R.drawable.ic_avatar_guest_user;
            } else {
                i = R.drawable.ic_avatar_user;
            }
            return context.getDrawable(i);
        }

        public void refresh() {
            this.mController.refreshUsers(-10000);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void checkIfAddUserDisallowedByAdminOnly(UserRecord userRecord) {
        RestrictedLockUtils.EnforcedAdmin enforcedAdminCheckIfRestrictionEnforced = RestrictedLockUtilsInternal.checkIfRestrictionEnforced(this.mContext, "no_add_user", ActivityManager.getCurrentUser());
        if (enforcedAdminCheckIfRestrictionEnforced != null && !RestrictedLockUtilsInternal.hasBaseUserRestriction(this.mContext, "no_add_user", ActivityManager.getCurrentUser())) {
            userRecord.isDisabledByAdmin = true;
            userRecord.enforcedAdmin = enforcedAdminCheckIfRestrictionEnforced;
        } else {
            userRecord.isDisabledByAdmin = false;
            userRecord.enforcedAdmin = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean shouldUseSimpleUserSwitcher() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "lockscreenSimpleUserSwitcher", this.mContext.getResources().getBoolean(android.R.bool.config_cecPowerStateChangeOnActiveSourceLost_userConfigurable) ? 1 : 0) != 0;
    }

    public void startActivity(Intent intent) {
        this.mActivityStarter.startActivity(intent, true);
    }

    public static final class UserRecord {
        public RestrictedLockUtils.EnforcedAdmin enforcedAdmin;
        public final UserInfo info;
        public final boolean isAddUser;
        public final boolean isCurrent;
        public boolean isDisabledByAdmin;
        public final boolean isGuest;
        public final boolean isRestricted;
        public boolean isSwitchToEnabled;
        public final Bitmap picture;

        public UserRecord(UserInfo userInfo, Bitmap bitmap, boolean z, boolean z2, boolean z3, boolean z4, boolean z5) {
            this.info = userInfo;
            this.picture = bitmap;
            this.isGuest = z;
            this.isCurrent = z2;
            this.isAddUser = z3;
            this.isRestricted = z4;
            this.isSwitchToEnabled = z5;
        }

        public UserRecord copyWithIsCurrent(boolean z) {
            return new UserRecord(this.info, this.picture, this.isGuest, z, this.isAddUser, this.isRestricted, this.isSwitchToEnabled);
        }

        public int resolveId() {
            UserInfo userInfo;
            if (this.isGuest || (userInfo = this.info) == null) {
                return -10000;
            }
            return userInfo.id;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("UserRecord(");
            if (this.info != null) {
                sb.append("name=\"");
                sb.append(this.info.name);
                sb.append("\" id=");
                sb.append(this.info.id);
            } else if (this.isGuest) {
                sb.append("<add guest placeholder>");
            } else if (this.isAddUser) {
                sb.append("<add user placeholder>");
            }
            if (this.isGuest) {
                sb.append(" <isGuest>");
            }
            if (this.isAddUser) {
                sb.append(" <isAddUser>");
            }
            if (this.isCurrent) {
                sb.append(" <isCurrent>");
            }
            if (this.picture != null) {
                sb.append(" <hasPicture>");
            }
            if (this.isRestricted) {
                sb.append(" <isRestricted>");
            }
            if (this.isDisabledByAdmin) {
                sb.append(" <isDisabledByAdmin>");
                sb.append(" enforcedAdmin=");
                sb.append(this.enforcedAdmin);
            }
            if (this.isSwitchToEnabled) {
                sb.append(" <isSwitchToEnabled>");
            }
            sb.append(')');
            return sb.toString();
        }
    }

    /* renamed from: com.android.systemui.statusbar.policy.UserSwitcherController$7, reason: invalid class name */
    class AnonymousClass7 implements KeyguardStateController.Callback {
        AnonymousClass7() {
        }

        @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
        public void onKeyguardShowingChanged() {
            if (UserSwitcherController.this.mKeyguardStateController.isShowing()) {
                UserSwitcherController.this.notifyAdapters();
            } else {
                final UserSwitcherController userSwitcherController = UserSwitcherController.this;
                userSwitcherController.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.policy.UserSwitcherController$7$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        UserSwitcherController.access$200(userSwitcherController);
                    }
                });
            }
        }
    }

    private final class ExitGuestDialog extends SystemUIDialog implements DialogInterface.OnClickListener {
        private final int mGuestId;
        private final int mTargetId;

        public ExitGuestDialog(Context context, int i, int i2) {
            super(context);
            setTitle(R.string.guest_exit_guest_dialog_title);
            setMessage(context.getString(R.string.guest_exit_guest_dialog_message));
            setButton(-2, context.getString(android.R.string.cancel), this);
            setButton(-1, context.getString(R.string.guest_exit_guest_dialog_remove), this);
            SystemUIDialog.setWindowOnTop(this);
            setCanceledOnTouchOutside(false);
            this.mGuestId = i;
            this.mTargetId = i2;
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
            if (i == -2) {
                cancel();
            } else {
                dismiss();
                UserSwitcherController.this.exitGuest(this.mGuestId, this.mTargetId);
            }
        }
    }

    private final class AddUserDialog extends SystemUIDialog implements DialogInterface.OnClickListener {
        public AddUserDialog(Context context) {
            super(context);
            setTitle(R.string.user_add_user_title);
            setMessage(context.getString(R.string.user_add_user_message_short));
            setButton(-2, context.getString(android.R.string.cancel), this);
            setButton(-1, context.getString(android.R.string.ok), this);
            SystemUIDialog.setWindowOnTop(this);
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
            if (i == -2) {
                cancel();
                return;
            }
            dismiss();
            if (ActivityManager.isUserAMonkey()) {
                return;
            }
            Intent intentCreateIntentForStart = CreateUserActivity.createIntentForStart(getContext());
            if (UserSwitcherController.this.mKeyguardStateController.isUnlocked() || UserSwitcherController.this.mKeyguardStateController.canDismissLockScreen()) {
                UserSwitcherController.this.mActivityStarter.startActivity(intentCreateIntentForStart, true);
                return;
            }
            try {
                UserSwitcherController.this.mActivityTaskManager.startActivity((IApplicationThread) null, UserSwitcherController.this.mContext.getBasePackageName(), UserSwitcherController.this.mContext.getAttributionTag(), intentCreateIntentForStart, intentCreateIntentForStart.resolveTypeIfNeeded(UserSwitcherController.this.mContext.getContentResolver()), (IBinder) null, (String) null, 0, 0, (ProfilerInfo) null, (Bundle) null);
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.e("UserSwitcherController", "Couldn't start create user activity", e);
            }
        }
    }
}
