package com.android.systemui.privacy;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.DeviceConfig;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.Dumpable;
import com.android.systemui.appops.AppOpItem;
import com.android.systemui.appops.AppOpsController;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.privacy.PrivacyItemController;
import com.android.systemui.util.DeviceConfigProxy;
import com.android.systemui.util.concurrency.DelayableExecutor;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import kotlin.collections.ArraysKt___ArraysJvmKt;
import kotlin.collections.ArraysKt___ArraysKt;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: PrivacyItemController.kt */
/* loaded from: classes.dex */
public final class PrivacyItemController implements Dumpable {
    public static final Companion Companion = new Companion(null);

    @NotNull
    private static final int[] OPS;

    @NotNull
    private static final int[] OPS_LOCATION;

    @NotNull
    private static final int[] OPS_MIC_CAMERA;

    @NotNull
    private static final IntentFilter intentFilter;
    private boolean allIndicatorsAvailable;
    private final AppOpsController appOpsController;
    private final Executor bgExecutor;
    private final BroadcastDispatcher broadcastDispatcher;
    private final List<WeakReference<Callback>> callbacks;
    private final PrivacyItemController$cb$1 cb;
    private List<Integer> currentUserIds;
    private final DeviceConfigProxy deviceConfigProxy;
    private final PrivacyItemController$devicePropertiesChangedListener$1 devicePropertiesChangedListener;
    private final MyExecutor internalUiExecutor;
    private boolean listening;
    private boolean micCameraAvailable;
    private final Runnable notifyChanges;

    @NotNull
    private List<PrivacyItem> privacyList;
    private final Runnable updateListAndNotifyChanges;
    private final UserManager userManager;

    @NotNull
    private Receiver userSwitcherReceiver;

    /* compiled from: PrivacyItemController.kt */
    public interface Callback {
        default void onFlagAllChanged(boolean z) {
        }

        default void onFlagMicCameraChanged(boolean z) {
        }

        void onPrivacyItemsChanged(@NotNull List<PrivacyItem> list);
    }

    private final boolean isMicCameraEnabled() {
        return true;
    }

    @VisibleForTesting
    public static /* synthetic */ void privacyList$annotations() {
    }

    @VisibleForTesting
    public static /* synthetic */ void userSwitcherReceiver$annotations() {
    }

    public final boolean isAllIndicatorsEnabled() {
        return true;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v9, types: [android.provider.DeviceConfig$OnPropertiesChangedListener, com.android.systemui.privacy.PrivacyItemController$devicePropertiesChangedListener$1] */
    /* JADX WARN: Type inference failed for: r4v2, types: [com.android.systemui.privacy.PrivacyItemController$cb$1] */
    public PrivacyItemController(@NotNull AppOpsController appOpsController, @NotNull final DelayableExecutor uiExecutor, @NotNull Executor bgExecutor, @NotNull BroadcastDispatcher broadcastDispatcher, @NotNull DeviceConfigProxy deviceConfigProxy, @NotNull UserManager userManager, @NotNull DumpManager dumpManager) {
        Intrinsics.checkParameterIsNotNull(appOpsController, "appOpsController");
        Intrinsics.checkParameterIsNotNull(uiExecutor, "uiExecutor");
        Intrinsics.checkParameterIsNotNull(bgExecutor, "bgExecutor");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(deviceConfigProxy, "deviceConfigProxy");
        Intrinsics.checkParameterIsNotNull(userManager, "userManager");
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        this.appOpsController = appOpsController;
        this.bgExecutor = bgExecutor;
        this.broadcastDispatcher = broadcastDispatcher;
        this.deviceConfigProxy = deviceConfigProxy;
        this.userManager = userManager;
        this.privacyList = CollectionsKt__CollectionsKt.emptyList();
        this.currentUserIds = CollectionsKt__CollectionsKt.emptyList();
        this.callbacks = new ArrayList();
        this.internalUiExecutor = new MyExecutor(new WeakReference(this), uiExecutor);
        this.notifyChanges = new Runnable() { // from class: com.android.systemui.privacy.PrivacyItemController$notifyChanges$1
            @Override // java.lang.Runnable
            public final void run() {
                List<PrivacyItem> privacyList$frameworks__base__packages__SystemUI__android_common__SystemUI_core = this.this$0.getPrivacyList$frameworks__base__packages__SystemUI__android_common__SystemUI_core();
                Iterator it = this.this$0.callbacks.iterator();
                while (it.hasNext()) {
                    PrivacyItemController.Callback callback = (PrivacyItemController.Callback) ((WeakReference) it.next()).get();
                    if (callback != null) {
                        callback.onPrivacyItemsChanged(privacyList$frameworks__base__packages__SystemUI__android_common__SystemUI_core);
                    }
                }
            }
        };
        this.updateListAndNotifyChanges = new Runnable() { // from class: com.android.systemui.privacy.PrivacyItemController$updateListAndNotifyChanges$1
            @Override // java.lang.Runnable
            public final void run() {
                this.this$0.updatePrivacyList();
                uiExecutor.execute(this.this$0.notifyChanges);
            }
        };
        this.allIndicatorsAvailable = isAllIndicatorsEnabled();
        this.micCameraAvailable = isMicCameraEnabled();
        ?? r2 = new DeviceConfig.OnPropertiesChangedListener() { // from class: com.android.systemui.privacy.PrivacyItemController$devicePropertiesChangedListener$1
            public void onPropertiesChanged(@NotNull DeviceConfig.Properties properties) {
                Intrinsics.checkParameterIsNotNull(properties, "properties");
                if ("privacy".equals(properties.getNamespace())) {
                    if (properties.getKeyset().contains("permissions_hub_2_enabled") || properties.getKeyset().contains("camera_mic_icons_enabled")) {
                        if (properties.getKeyset().contains("permissions_hub_2_enabled")) {
                            this.this$0.allIndicatorsAvailable = properties.getBoolean("permissions_hub_2_enabled", false);
                            Iterator it = this.this$0.callbacks.iterator();
                            while (it.hasNext()) {
                                PrivacyItemController.Callback callback = (PrivacyItemController.Callback) ((WeakReference) it.next()).get();
                                if (callback != null) {
                                    callback.onFlagAllChanged(this.this$0.getAllIndicatorsAvailable());
                                }
                            }
                        }
                        if (properties.getKeyset().contains("camera_mic_icons_enabled")) {
                            this.this$0.micCameraAvailable = properties.getBoolean("camera_mic_icons_enabled", true);
                            Iterator it2 = this.this$0.callbacks.iterator();
                            while (it2.hasNext()) {
                                PrivacyItemController.Callback callback2 = (PrivacyItemController.Callback) ((WeakReference) it2.next()).get();
                                if (callback2 != null) {
                                    callback2.onFlagMicCameraChanged(this.this$0.getMicCameraAvailable());
                                }
                            }
                        }
                        this.this$0.internalUiExecutor.updateListeningState();
                    }
                }
            }
        };
        this.devicePropertiesChangedListener = r2;
        this.cb = new AppOpsController.Callback() { // from class: com.android.systemui.privacy.PrivacyItemController$cb$1
            @Override // com.android.systemui.appops.AppOpsController.Callback
            public void onActiveStateChanged(int i, int i2, @NotNull String packageName, boolean z) {
                Intrinsics.checkParameterIsNotNull(packageName, "packageName");
                if (this.this$0.getAllIndicatorsAvailable() || !ArraysKt___ArraysKt.contains(PrivacyItemController.Companion.getOPS_LOCATION(), i)) {
                    if (this.this$0.currentUserIds.contains(Integer.valueOf(UserHandle.getUserId(i2))) || i == 100 || i == 101) {
                        this.this$0.update(false);
                    }
                }
            }
        };
        this.userSwitcherReceiver = new Receiver();
        deviceConfigProxy.addOnPropertiesChangedListener("privacy", uiExecutor, r2);
        dumpManager.registerDumpable("PrivacyItemController", this);
    }

    /* compiled from: PrivacyItemController.kt */
    @VisibleForTesting
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final int[] getOPS_LOCATION() {
            return PrivacyItemController.OPS_LOCATION;
        }

        @NotNull
        public final IntentFilter getIntentFilter() {
            return PrivacyItemController.intentFilter;
        }
    }

    static {
        int[] iArr = {26, 101, 27, 100};
        OPS_MIC_CAMERA = iArr;
        int[] iArr2 = {0, 1};
        OPS_LOCATION = iArr2;
        OPS = ArraysKt___ArraysJvmKt.plus(iArr, iArr2);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.USER_SWITCHED");
        intentFilter2.addAction("android.intent.action.MANAGED_PROFILE_AVAILABLE");
        intentFilter2.addAction("android.intent.action.MANAGED_PROFILE_UNAVAILABLE");
        intentFilter = intentFilter2;
    }

    @NotNull
    public final synchronized List<PrivacyItem> getPrivacyList$frameworks__base__packages__SystemUI__android_common__SystemUI_core() {
        return CollectionsKt___CollectionsKt.toList(this.privacyList);
    }

    public final boolean getAllIndicatorsAvailable() {
        return this.allIndicatorsAvailable;
    }

    public final boolean getMicCameraAvailable() {
        return this.micCameraAvailable;
    }

    private final void unregisterReceiver() {
        this.broadcastDispatcher.unregisterReceiver(this.userSwitcherReceiver);
    }

    private final void registerReceiver() {
        BroadcastDispatcher broadcastDispatcher = this.broadcastDispatcher;
        Receiver receiver = this.userSwitcherReceiver;
        IntentFilter intentFilter2 = intentFilter;
        UserHandle userHandle = UserHandle.ALL;
        Intrinsics.checkExpressionValueIsNotNull(userHandle, "UserHandle.ALL");
        broadcastDispatcher.registerReceiver(receiver, intentFilter2, null, userHandle);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void update(final boolean z) {
        this.bgExecutor.execute(new Runnable() { // from class: com.android.systemui.privacy.PrivacyItemController.update.1
            @Override // java.lang.Runnable
            public final void run() {
                if (z) {
                    int currentUser = ActivityManager.getCurrentUser();
                    PrivacyItemController privacyItemController = PrivacyItemController.this;
                    List profiles = privacyItemController.userManager.getProfiles(currentUser);
                    Intrinsics.checkExpressionValueIsNotNull(profiles, "userManager.getProfiles(currentUser)");
                    ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(profiles, 10));
                    Iterator it = profiles.iterator();
                    while (it.hasNext()) {
                        arrayList.add(Integer.valueOf(((UserInfo) it.next()).id));
                    }
                    privacyItemController.currentUserIds = arrayList;
                }
                PrivacyItemController.this.updateListAndNotifyChanges.run();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void setListeningState() {
        boolean z = (!this.callbacks.isEmpty()) & (this.allIndicatorsAvailable || this.micCameraAvailable);
        if (this.listening == z) {
            return;
        }
        this.listening = z;
        if (z) {
            this.appOpsController.addCallback(OPS, this.cb);
            registerReceiver();
            update(true);
        } else {
            this.appOpsController.removeCallback(OPS, this.cb);
            unregisterReceiver();
            update(false);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void addCallback(WeakReference<Callback> weakReference) {
        this.callbacks.add(weakReference);
        if ((!this.callbacks.isEmpty()) && !this.listening) {
            this.internalUiExecutor.updateListeningState();
        } else if (this.listening) {
            this.internalUiExecutor.execute(new NotifyChangesToCallback(weakReference.get(), getPrivacyList$frameworks__base__packages__SystemUI__android_common__SystemUI_core()));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void removeCallback(final WeakReference<Callback> weakReference) {
        this.callbacks.removeIf(new Predicate<WeakReference<Callback>>() { // from class: com.android.systemui.privacy.PrivacyItemController.removeCallback.1
            @Override // java.util.function.Predicate
            public final boolean test(@NotNull WeakReference<Callback> it) {
                Intrinsics.checkParameterIsNotNull(it, "it");
                Callback callback = it.get();
                if (callback != null) {
                    return callback.equals(weakReference.get());
                }
                return true;
            }
        });
        if (this.callbacks.isEmpty()) {
            this.internalUiExecutor.updateListeningState();
        }
    }

    public final void addCallback(@NotNull Callback callback) {
        Intrinsics.checkParameterIsNotNull(callback, "callback");
        this.internalUiExecutor.addCallback(callback);
    }

    public final void removeCallback(@NotNull Callback callback) {
        Intrinsics.checkParameterIsNotNull(callback, "callback");
        this.internalUiExecutor.removeCallback(callback);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updatePrivacyList() {
        if (!this.listening) {
            this.privacyList = CollectionsKt__CollectionsKt.emptyList();
            return;
        }
        List<AppOpItem> activeAppOpsForUser = this.appOpsController.getActiveAppOpsForUser(-1);
        Intrinsics.checkExpressionValueIsNotNull(activeAppOpsForUser, "appOpsController.getActi…User(UserHandle.USER_ALL)");
        ArrayList<AppOpItem> arrayList = new ArrayList();
        for (Object obj : activeAppOpsForUser) {
            AppOpItem it = (AppOpItem) obj;
            List<Integer> list = this.currentUserIds;
            Intrinsics.checkExpressionValueIsNotNull(it, "it");
            if (list.contains(Integer.valueOf(UserHandle.getUserId(it.getUid()))) || it.getCode() == 100 || it.getCode() == 101) {
                arrayList.add(obj);
            }
        }
        ArrayList arrayList2 = new ArrayList();
        for (AppOpItem it2 : arrayList) {
            Intrinsics.checkExpressionValueIsNotNull(it2, "it");
            PrivacyItem privacyItem = toPrivacyItem(it2);
            if (privacyItem != null) {
                arrayList2.add(privacyItem);
            }
        }
        this.privacyList = CollectionsKt___CollectionsKt.distinct(arrayList2);
    }

    private final PrivacyItem toPrivacyItem(AppOpItem appOpItem) {
        PrivacyType privacyType;
        int code = appOpItem.getCode();
        if (code == 0 || code == 1) {
            privacyType = PrivacyType.TYPE_LOCATION;
        } else if (code == 26) {
            privacyType = PrivacyType.TYPE_CAMERA;
        } else if (code != 27 && code != 100) {
            if (code != 101) {
                return null;
            }
            privacyType = PrivacyType.TYPE_CAMERA;
        } else {
            privacyType = PrivacyType.TYPE_MICROPHONE;
        }
        if (privacyType == PrivacyType.TYPE_LOCATION && !this.allIndicatorsAvailable) {
            return null;
        }
        String packageName = appOpItem.getPackageName();
        Intrinsics.checkExpressionValueIsNotNull(packageName, "appOpItem.packageName");
        return new PrivacyItem(privacyType, new PrivacyApplication(packageName, appOpItem.getUid()));
    }

    /* compiled from: PrivacyItemController.kt */
    public final class Receiver extends BroadcastReceiver {
        public Receiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(@NotNull Context context, @NotNull Intent intent) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            Intrinsics.checkParameterIsNotNull(intent, "intent");
            if (PrivacyItemController.Companion.getIntentFilter().hasAction(intent.getAction())) {
                PrivacyItemController.this.update(true);
            }
        }
    }

    /* compiled from: PrivacyItemController.kt */
    private static final class NotifyChangesToCallback implements Runnable {
        private final Callback callback;
        private final List<PrivacyItem> list;

        public NotifyChangesToCallback(@Nullable Callback callback, @NotNull List<PrivacyItem> list) {
            Intrinsics.checkParameterIsNotNull(list, "list");
            this.callback = callback;
            this.list = list;
        }

        @Override // java.lang.Runnable
        public void run() {
            Callback callback = this.callback;
            if (callback != null) {
                callback.onPrivacyItemsChanged(this.list);
            }
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(@NotNull FileDescriptor fd, @NotNull PrintWriter pw, @NotNull String[] args) {
        Intrinsics.checkParameterIsNotNull(fd, "fd");
        Intrinsics.checkParameterIsNotNull(pw, "pw");
        Intrinsics.checkParameterIsNotNull(args, "args");
        pw.println("PrivacyItemController state:");
        pw.println("  Listening: " + this.listening);
        pw.println("  Current user ids: " + this.currentUserIds);
        pw.println("  Privacy Items:");
        for (PrivacyItem privacyItem : getPrivacyList$frameworks__base__packages__SystemUI__android_common__SystemUI_core()) {
            pw.print("    ");
            pw.println(privacyItem.toString());
        }
        pw.println("  Callbacks:");
        Iterator<T> it = this.callbacks.iterator();
        while (it.hasNext()) {
            Callback callback = (Callback) ((WeakReference) it.next()).get();
            if (callback != null) {
                pw.print("    ");
                pw.println(callback.toString());
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* compiled from: PrivacyItemController.kt */
    static final class MyExecutor implements Executor {
        private final DelayableExecutor delegate;
        private Runnable listeningCanceller;
        private final WeakReference<PrivacyItemController> outerClass;

        public MyExecutor(@NotNull WeakReference<PrivacyItemController> outerClass, @NotNull DelayableExecutor delegate) {
            Intrinsics.checkParameterIsNotNull(outerClass, "outerClass");
            Intrinsics.checkParameterIsNotNull(delegate, "delegate");
            this.outerClass = outerClass;
            this.delegate = delegate;
        }

        @Override // java.util.concurrent.Executor
        public void execute(@NotNull Runnable command) {
            Intrinsics.checkParameterIsNotNull(command, "command");
            this.delegate.execute(command);
        }

        public final void updateListeningState() {
            Runnable runnable = this.listeningCanceller;
            if (runnable != null) {
                runnable.run();
            }
            this.listeningCanceller = this.delegate.executeDelayed(new Runnable() { // from class: com.android.systemui.privacy.PrivacyItemController$MyExecutor$updateListeningState$1
                @Override // java.lang.Runnable
                public final void run() {
                    PrivacyItemController privacyItemController = (PrivacyItemController) this.this$0.outerClass.get();
                    if (privacyItemController != null) {
                        privacyItemController.setListeningState();
                    }
                }
            }, 0L);
        }

        public final void addCallback(@NotNull Callback callback) {
            Intrinsics.checkParameterIsNotNull(callback, "callback");
            PrivacyItemController privacyItemController = this.outerClass.get();
            if (privacyItemController != null) {
                privacyItemController.addCallback((WeakReference<Callback>) new WeakReference(callback));
            }
        }

        public final void removeCallback(@NotNull Callback callback) {
            Intrinsics.checkParameterIsNotNull(callback, "callback");
            PrivacyItemController privacyItemController = this.outerClass.get();
            if (privacyItemController != null) {
                privacyItemController.removeCallback((WeakReference<Callback>) new WeakReference(callback));
            }
        }
    }
}
