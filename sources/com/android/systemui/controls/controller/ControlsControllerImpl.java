package com.android.systemui.controls.controller;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.backup.BackupManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.controls.Control;
import android.service.controls.actions.ControlAction;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.Dumpable;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controls.ControlStatus;
import com.android.systemui.controls.controller.ControlsBindingController;
import com.android.systemui.controls.controller.ControlsController;
import com.android.systemui.controls.controller.ControlsControllerImpl;
import com.android.systemui.controls.management.ControlsListingController;
import com.android.systemui.controls.ui.ControlsUiController;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.util.concurrency.DelayableExecutor;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import kotlin.collections.CollectionsKt__CollectionsJVMKt;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt__MutableCollectionsKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.collections.SetsKt___SetsKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsControllerImpl.kt */
/* loaded from: classes.dex */
public final class ControlsControllerImpl implements Dumpable, ControlsController {
    public static final Companion Companion = new Companion(null);
    private static final Uri URI = Settings.Secure.getUriFor("controls_enabled");

    @NotNull
    private AuxiliaryPersistenceWrapper auxiliaryPersistenceWrapper;
    private boolean available;
    private final ControlsBindingController bindingController;
    private final BroadcastDispatcher broadcastDispatcher;
    private final Context context;
    private UserHandle currentUser;
    private final DelayableExecutor executor;
    private final ControlsControllerImpl$listingCallback$1 listingCallback;
    private final ControlsListingController listingController;
    private final ControlsFavoritePersistenceWrapper persistenceWrapper;

    @NotNull
    private final BroadcastReceiver restoreFinishedReceiver;
    private final List<Consumer<Boolean>> seedingCallbacks;
    private boolean seedingInProgress;

    @NotNull
    private final ContentObserver settingObserver;
    private final ControlsUiController uiController;
    private boolean userChanging;
    private UserStructure userStructure;
    private final ControlsControllerImpl$userSwitchReceiver$1 userSwitchReceiver;

    @VisibleForTesting
    public static /* synthetic */ void auxiliaryPersistenceWrapper$annotations() {
    }

    @VisibleForTesting
    public static /* synthetic */ void restoreFinishedReceiver$annotations() {
    }

    @VisibleForTesting
    public static /* synthetic */ void settingObserver$annotations() {
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r8v12, types: [android.content.BroadcastReceiver, com.android.systemui.controls.controller.ControlsControllerImpl$userSwitchReceiver$1] */
    public ControlsControllerImpl(@NotNull Context context, @NotNull DelayableExecutor executor, @NotNull ControlsUiController uiController, @NotNull ControlsBindingController bindingController, @NotNull ControlsListingController listingController, @NotNull BroadcastDispatcher broadcastDispatcher, @NotNull Optional<ControlsFavoritePersistenceWrapper> optionalWrapper, @NotNull DumpManager dumpManager) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(executor, "executor");
        Intrinsics.checkParameterIsNotNull(uiController, "uiController");
        Intrinsics.checkParameterIsNotNull(bindingController, "bindingController");
        Intrinsics.checkParameterIsNotNull(listingController, "listingController");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(optionalWrapper, "optionalWrapper");
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        this.context = context;
        this.executor = executor;
        this.uiController = uiController;
        this.bindingController = bindingController;
        this.listingController = listingController;
        this.broadcastDispatcher = broadcastDispatcher;
        this.userChanging = true;
        this.seedingCallbacks = new ArrayList();
        this.currentUser = UserHandle.of(ActivityManager.getCurrentUser());
        this.available = Companion.isAvailable(getCurrentUserId(), getContentResolver());
        UserHandle currentUser = this.currentUser;
        Intrinsics.checkExpressionValueIsNotNull(currentUser, "currentUser");
        this.userStructure = new UserStructure(context, currentUser);
        ControlsFavoritePersistenceWrapper controlsFavoritePersistenceWrapperOrElseGet = optionalWrapper.orElseGet(new Supplier<ControlsFavoritePersistenceWrapper>() { // from class: com.android.systemui.controls.controller.ControlsControllerImpl.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // java.util.function.Supplier
            @NotNull
            public final ControlsFavoritePersistenceWrapper get() {
                File file = ControlsControllerImpl.this.userStructure.getFile();
                Intrinsics.checkExpressionValueIsNotNull(file, "userStructure.file");
                return new ControlsFavoritePersistenceWrapper(file, ControlsControllerImpl.this.executor, new BackupManager(ControlsControllerImpl.this.userStructure.getUserContext()));
            }
        });
        Intrinsics.checkExpressionValueIsNotNull(controlsFavoritePersistenceWrapperOrElseGet, "optionalWrapper.orElseGe…)\n            )\n        }");
        this.persistenceWrapper = controlsFavoritePersistenceWrapperOrElseGet;
        File auxiliaryFile = this.userStructure.getAuxiliaryFile();
        Intrinsics.checkExpressionValueIsNotNull(auxiliaryFile, "userStructure.auxiliaryFile");
        this.auxiliaryPersistenceWrapper = new AuxiliaryPersistenceWrapper(auxiliaryFile, executor);
        ?? r8 = new BroadcastReceiver() { // from class: com.android.systemui.controls.controller.ControlsControllerImpl$userSwitchReceiver$1
            @Override // android.content.BroadcastReceiver
            public void onReceive(@NotNull Context context2, @NotNull Intent intent) {
                Intrinsics.checkParameterIsNotNull(context2, "context");
                Intrinsics.checkParameterIsNotNull(intent, "intent");
                if (Intrinsics.areEqual(intent.getAction(), "android.intent.action.USER_SWITCHED")) {
                    this.this$0.userChanging = true;
                    UserHandle newUser = UserHandle.of(intent.getIntExtra("android.intent.extra.user_handle", getSendingUserId()));
                    if (Intrinsics.areEqual(this.this$0.currentUser, newUser)) {
                        this.this$0.userChanging = false;
                        return;
                    }
                    ControlsControllerImpl controlsControllerImpl = this.this$0;
                    Intrinsics.checkExpressionValueIsNotNull(newUser, "newUser");
                    controlsControllerImpl.setValuesForUser(newUser);
                }
            }
        };
        this.userSwitchReceiver = r8;
        ControlsControllerImpl$restoreFinishedReceiver$1 controlsControllerImpl$restoreFinishedReceiver$1 = new ControlsControllerImpl$restoreFinishedReceiver$1(this);
        this.restoreFinishedReceiver = controlsControllerImpl$restoreFinishedReceiver$1;
        final Handler handler = null;
        ContentObserver contentObserver = new ContentObserver(handler) { // from class: com.android.systemui.controls.controller.ControlsControllerImpl$settingObserver$1
            public void onChange(boolean z, @NotNull Collection<? extends Uri> uris, int i, int i2) {
                Intrinsics.checkParameterIsNotNull(uris, "uris");
                if (this.this$0.userChanging || i2 != this.this$0.getCurrentUserId()) {
                    return;
                }
                ControlsControllerImpl controlsControllerImpl = this.this$0;
                controlsControllerImpl.available = ControlsControllerImpl.Companion.isAvailable(controlsControllerImpl.getCurrentUserId(), this.this$0.getContentResolver());
                ControlsControllerImpl controlsControllerImpl2 = this.this$0;
                controlsControllerImpl2.resetFavorites(controlsControllerImpl2.getAvailable());
            }
        };
        this.settingObserver = contentObserver;
        ControlsControllerImpl$listingCallback$1 controlsControllerImpl$listingCallback$1 = new ControlsControllerImpl$listingCallback$1(this);
        this.listingCallback = controlsControllerImpl$listingCallback$1;
        String name = ControlsControllerImpl.class.getName();
        Intrinsics.checkExpressionValueIsNotNull(name, "javaClass.name");
        dumpManager.registerDumpable(name, this);
        resetFavorites(getAvailable());
        this.userChanging = false;
        IntentFilter intentFilter = new IntentFilter("android.intent.action.USER_SWITCHED");
        UserHandle userHandle = UserHandle.ALL;
        Intrinsics.checkExpressionValueIsNotNull(userHandle, "UserHandle.ALL");
        broadcastDispatcher.registerReceiver(r8, intentFilter, executor, userHandle);
        context.registerReceiver(controlsControllerImpl$restoreFinishedReceiver$1, new IntentFilter("com.android.systemui.backup.RESTORE_FINISHED"), "com.android.systemui.permission.SELF", null);
        getContentResolver().registerContentObserver(URI, false, contentObserver, -1);
        listingController.addCallback(controlsControllerImpl$listingCallback$1);
    }

    /* compiled from: ControlsControllerImpl.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public final boolean isAvailable(int i, ContentResolver contentResolver) {
            return Settings.Secure.getIntForUser(contentResolver, "controls_enabled", 1, i) != 0;
        }
    }

    @Override // com.android.systemui.util.UserAwareController
    public int getCurrentUserId() {
        UserHandle currentUser = this.currentUser;
        Intrinsics.checkExpressionValueIsNotNull(currentUser, "currentUser");
        return currentUser.getIdentifier();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final ContentResolver getContentResolver() {
        ContentResolver contentResolver = this.context.getContentResolver();
        Intrinsics.checkExpressionValueIsNotNull(contentResolver, "context.contentResolver");
        return contentResolver;
    }

    @Override // com.android.systemui.controls.controller.ControlsController
    public boolean getAvailable() {
        return this.available;
    }

    @NotNull
    public final AuxiliaryPersistenceWrapper getAuxiliaryPersistenceWrapper$frameworks__base__packages__SystemUI__android_common__SystemUI_core() {
        return this.auxiliaryPersistenceWrapper;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void setValuesForUser(UserHandle currentUser) {
        Log.d("ControlsControllerImpl", "Changing to user: " + currentUser);
        this.currentUser = currentUser;
        Context context = this.context;
        Intrinsics.checkExpressionValueIsNotNull(currentUser, "currentUser");
        UserStructure userStructure = new UserStructure(context, currentUser);
        this.userStructure = userStructure;
        ControlsFavoritePersistenceWrapper controlsFavoritePersistenceWrapper = this.persistenceWrapper;
        File file = userStructure.getFile();
        Intrinsics.checkExpressionValueIsNotNull(file, "userStructure.file");
        controlsFavoritePersistenceWrapper.changeFileAndBackupManager(file, new BackupManager(this.userStructure.getUserContext()));
        AuxiliaryPersistenceWrapper auxiliaryPersistenceWrapper = this.auxiliaryPersistenceWrapper;
        File auxiliaryFile = this.userStructure.getAuxiliaryFile();
        Intrinsics.checkExpressionValueIsNotNull(auxiliaryFile, "userStructure.auxiliaryFile");
        auxiliaryPersistenceWrapper.changeFile(auxiliaryFile);
        this.available = Companion.isAvailable(currentUser.getIdentifier(), getContentResolver());
        resetFavorites(getAvailable());
        this.bindingController.lambda$changeUser$0(currentUser);
        this.listingController.lambda$changeUser$0(currentUser);
        this.userChanging = false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void resetFavorites(boolean z) {
        Favorites favorites = Favorites.INSTANCE;
        favorites.clear();
        if (z) {
            favorites.load(this.persistenceWrapper.readFavorites());
        }
    }

    private final boolean confirmAvailability() {
        if (this.userChanging) {
            Log.w("ControlsControllerImpl", "Controls not available while user is changing");
            return false;
        }
        if (getAvailable()) {
            return true;
        }
        Log.d("ControlsControllerImpl", "Controls not available");
        return false;
    }

    public void loadForComponent(@NotNull final ComponentName componentName, @NotNull final Consumer<ControlsController.LoadData> dataCallback, @NotNull final Consumer<Runnable> cancelWrapper) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(dataCallback, "dataCallback");
        Intrinsics.checkParameterIsNotNull(cancelWrapper, "cancelWrapper");
        if (!confirmAvailability()) {
            if (this.userChanging) {
                this.executor.executeDelayed(new Runnable() { // from class: com.android.systemui.controls.controller.ControlsControllerImpl.loadForComponent.1
                    @Override // java.lang.Runnable
                    public final void run() {
                        ControlsControllerImpl.this.loadForComponent(componentName, dataCallback, cancelWrapper);
                    }
                }, 500L, TimeUnit.MILLISECONDS);
            }
            dataCallback.accept(ControlsControllerKt.createLoadDataObject(CollectionsKt__CollectionsKt.emptyList(), CollectionsKt__CollectionsKt.emptyList(), true));
        }
        cancelWrapper.accept(this.bindingController.bindAndLoad(componentName, new AnonymousClass2(componentName, dataCallback)));
    }

    /* compiled from: ControlsControllerImpl.kt */
    /* renamed from: com.android.systemui.controls.controller.ControlsControllerImpl$loadForComponent$2, reason: invalid class name */
    public static final class AnonymousClass2 implements ControlsBindingController.LoadCallback {
        final /* synthetic */ ComponentName $componentName;
        final /* synthetic */ Consumer $dataCallback;

        AnonymousClass2(ComponentName componentName, Consumer consumer) {
            this.$componentName = componentName;
            this.$dataCallback = consumer;
        }

        @Override // java.util.function.Consumer
        public /* bridge */ /* synthetic */ void accept(List<? extends Control> list) {
            accept2((List<Control>) list);
        }

        /* renamed from: accept, reason: avoid collision after fix types in other method */
        public void accept2(@NotNull final List<Control> controls) {
            Intrinsics.checkParameterIsNotNull(controls, "controls");
            ControlsControllerImpl.this.executor.execute(new Runnable() { // from class: com.android.systemui.controls.controller.ControlsControllerImpl$loadForComponent$2$accept$1
                @Override // java.lang.Runnable
                public final void run() {
                    List<ControlInfo> controlsForComponent = Favorites.INSTANCE.getControlsForComponent(this.this$0.$componentName);
                    ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(controlsForComponent, 10));
                    Iterator<T> it = controlsForComponent.iterator();
                    while (it.hasNext()) {
                        arrayList.add(((ControlInfo) it.next()).getControlId());
                    }
                    Favorites favorites = Favorites.INSTANCE;
                    if (favorites.updateControls(this.this$0.$componentName, controls)) {
                        ControlsControllerImpl.this.persistenceWrapper.storeFavorites(favorites.getAllStructures());
                    }
                    Set setFindRemoved = ControlsControllerImpl.this.findRemoved(CollectionsKt___CollectionsKt.toSet(arrayList), controls);
                    List<Control> list = controls;
                    ArrayList arrayList2 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list, 10));
                    for (Control control : list) {
                        arrayList2.add(new ControlStatus(control, this.this$0.$componentName, arrayList.contains(control.getControlId()), false, 8, null));
                    }
                    ArrayList arrayList3 = new ArrayList();
                    for (StructureInfo structureInfo : Favorites.INSTANCE.getStructuresForComponent(this.this$0.$componentName)) {
                        for (ControlInfo controlInfo : structureInfo.getControls()) {
                            if (setFindRemoved.contains(controlInfo.getControlId())) {
                                ControlsControllerImpl.AnonymousClass2 anonymousClass2 = this.this$0;
                                arrayList3.add(ControlsControllerImpl.createRemovedStatus$default(ControlsControllerImpl.this, anonymousClass2.$componentName, controlInfo, structureInfo.getStructure(), false, 8, null));
                            }
                        }
                    }
                    this.this$0.$dataCallback.accept(ControlsControllerKt.createLoadDataObject$default(CollectionsKt___CollectionsKt.plus((Collection) arrayList3, (Iterable) arrayList2), arrayList, false, 4, null));
                }
            });
        }

        @Override // com.android.systemui.controls.controller.ControlsBindingController.LoadCallback
        public void error(@NotNull String message) {
            Intrinsics.checkParameterIsNotNull(message, "message");
            ControlsControllerImpl.this.executor.execute(new Runnable() { // from class: com.android.systemui.controls.controller.ControlsControllerImpl$loadForComponent$2$error$1
                @Override // java.lang.Runnable
                public final void run() {
                    List<StructureInfo> structuresForComponent = Favorites.INSTANCE.getStructuresForComponent(this.this$0.$componentName);
                    ArrayList arrayList = new ArrayList();
                    for (StructureInfo structureInfo : structuresForComponent) {
                        List<ControlInfo> controls = structureInfo.getControls();
                        ArrayList arrayList2 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(controls, 10));
                        for (ControlInfo controlInfo : controls) {
                            ControlsControllerImpl.AnonymousClass2 anonymousClass2 = this.this$0;
                            arrayList2.add(ControlsControllerImpl.this.createRemovedStatus(anonymousClass2.$componentName, controlInfo, structureInfo.getStructure(), false));
                        }
                        CollectionsKt__MutableCollectionsKt.addAll(arrayList, arrayList2);
                    }
                    ArrayList arrayList3 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(arrayList, 10));
                    Iterator it = arrayList.iterator();
                    while (it.hasNext()) {
                        arrayList3.add(((ControlStatus) it.next()).getControl().getControlId());
                    }
                    this.this$0.$dataCallback.accept(ControlsControllerKt.createLoadDataObject(arrayList, arrayList3, true));
                }
            });
        }
    }

    @Override // com.android.systemui.controls.controller.ControlsController
    public boolean addSeedingFavoritesCallback(@NotNull final Consumer<Boolean> callback) {
        Intrinsics.checkParameterIsNotNull(callback, "callback");
        if (!this.seedingInProgress) {
            return false;
        }
        this.executor.execute(new Runnable() { // from class: com.android.systemui.controls.controller.ControlsControllerImpl.addSeedingFavoritesCallback.1
            @Override // java.lang.Runnable
            public final void run() {
                if (ControlsControllerImpl.this.seedingInProgress) {
                    ControlsControllerImpl.this.seedingCallbacks.add(callback);
                } else {
                    callback.accept(Boolean.FALSE);
                }
            }
        });
        return true;
    }

    @Override // com.android.systemui.controls.controller.ControlsController
    public void seedFavoritesForComponents(@NotNull final List<ComponentName> componentNames, @NotNull final Consumer<SeedResponse> callback) {
        Intrinsics.checkParameterIsNotNull(componentNames, "componentNames");
        Intrinsics.checkParameterIsNotNull(callback, "callback");
        if (this.seedingInProgress || componentNames.isEmpty()) {
            return;
        }
        if (!confirmAvailability()) {
            if (this.userChanging) {
                this.executor.executeDelayed(new Runnable() { // from class: com.android.systemui.controls.controller.ControlsControllerImpl.seedFavoritesForComponents.1
                    @Override // java.lang.Runnable
                    public final void run() {
                        ControlsControllerImpl.this.seedFavoritesForComponents(componentNames, callback);
                    }
                }, 500L, TimeUnit.MILLISECONDS);
                return;
            }
            Iterator<T> it = componentNames.iterator();
            while (it.hasNext()) {
                String packageName = ((ComponentName) it.next()).getPackageName();
                Intrinsics.checkExpressionValueIsNotNull(packageName, "it.packageName");
                callback.accept(new SeedResponse(packageName, false));
            }
            return;
        }
        this.seedingInProgress = true;
        startSeeding(componentNames, callback, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void startSeeding(List<ComponentName> list, Consumer<SeedResponse> consumer, boolean z) {
        if (list.isEmpty()) {
            endSeedingCall(!z);
            return;
        }
        ComponentName componentName = list.get(0);
        Log.d("ControlsControllerImpl", "Beginning request to seed favorites for: " + componentName);
        this.bindingController.bindAndLoadSuggested(componentName, new C00291(componentName, consumer, CollectionsKt___CollectionsKt.drop(list, 1), z));
    }

    /* compiled from: ControlsControllerImpl.kt */
    /* renamed from: com.android.systemui.controls.controller.ControlsControllerImpl$startSeeding$1, reason: invalid class name and case insensitive filesystem */
    public static final class C00291 implements ControlsBindingController.LoadCallback {
        final /* synthetic */ Consumer $callback;
        final /* synthetic */ ComponentName $componentName;
        final /* synthetic */ boolean $didAnyFail;
        final /* synthetic */ List $remaining;

        C00291(ComponentName componentName, Consumer consumer, List list, boolean z) {
            this.$componentName = componentName;
            this.$callback = consumer;
            this.$remaining = list;
            this.$didAnyFail = z;
        }

        @Override // java.util.function.Consumer
        public /* bridge */ /* synthetic */ void accept(List<? extends Control> list) {
            accept2((List<Control>) list);
        }

        /* renamed from: accept, reason: avoid collision after fix types in other method */
        public void accept2(@NotNull final List<Control> controls) {
            Intrinsics.checkParameterIsNotNull(controls, "controls");
            ControlsControllerImpl.this.executor.execute(new Runnable() { // from class: com.android.systemui.controls.controller.ControlsControllerImpl$startSeeding$1$accept$1
                @Override // java.lang.Runnable
                public final void run() {
                    ArrayMap arrayMap = new ArrayMap();
                    for (Control control : controls) {
                        CharSequence structure = control.getStructure();
                        if (structure == null) {
                            structure = "";
                        }
                        List arrayList = (List) arrayMap.get(structure);
                        if (arrayList == null) {
                            arrayList = new ArrayList();
                        }
                        Intrinsics.checkExpressionValueIsNotNull(arrayList, "structureToControls.get(…ableListOf<ControlInfo>()");
                        if (arrayList.size() < 6) {
                            String controlId = control.getControlId();
                            Intrinsics.checkExpressionValueIsNotNull(controlId, "it.controlId");
                            CharSequence title = control.getTitle();
                            Intrinsics.checkExpressionValueIsNotNull(title, "it.title");
                            CharSequence subtitle = control.getSubtitle();
                            Intrinsics.checkExpressionValueIsNotNull(subtitle, "it.subtitle");
                            arrayList.add(new ControlInfo(controlId, title, subtitle, control.getDeviceType()));
                            arrayMap.put(structure, arrayList);
                        }
                    }
                    for (Map.Entry entry : arrayMap.entrySet()) {
                        CharSequence s = (CharSequence) entry.getKey();
                        List cs = (List) entry.getValue();
                        Favorites favorites = Favorites.INSTANCE;
                        ComponentName componentName = this.this$0.$componentName;
                        Intrinsics.checkExpressionValueIsNotNull(s, "s");
                        Intrinsics.checkExpressionValueIsNotNull(cs, "cs");
                        favorites.replaceControls(new StructureInfo(componentName, s, cs));
                    }
                    ControlsControllerImpl.this.persistenceWrapper.storeFavorites(Favorites.INSTANCE.getAllStructures());
                    ControlsControllerImpl.C00291 c00291 = this.this$0;
                    Consumer consumer = c00291.$callback;
                    String packageName = c00291.$componentName.getPackageName();
                    Intrinsics.checkExpressionValueIsNotNull(packageName, "componentName.packageName");
                    consumer.accept(new SeedResponse(packageName, true));
                    ControlsControllerImpl.C00291 c002912 = this.this$0;
                    ControlsControllerImpl.this.startSeeding(c002912.$remaining, c002912.$callback, c002912.$didAnyFail);
                }
            });
        }

        @Override // com.android.systemui.controls.controller.ControlsBindingController.LoadCallback
        public void error(@NotNull String message) {
            Intrinsics.checkParameterIsNotNull(message, "message");
            Log.e("ControlsControllerImpl", "Unable to seed favorites: " + message);
            ControlsControllerImpl.this.executor.execute(new Runnable() { // from class: com.android.systemui.controls.controller.ControlsControllerImpl$startSeeding$1$error$1
                @Override // java.lang.Runnable
                public final void run() {
                    ControlsControllerImpl.C00291 c00291 = this.this$0;
                    Consumer consumer = c00291.$callback;
                    String packageName = c00291.$componentName.getPackageName();
                    Intrinsics.checkExpressionValueIsNotNull(packageName, "componentName.packageName");
                    consumer.accept(new SeedResponse(packageName, false));
                    ControlsControllerImpl.C00291 c002912 = this.this$0;
                    ControlsControllerImpl.this.startSeeding(c002912.$remaining, c002912.$callback, true);
                }
            });
        }
    }

    private final void endSeedingCall(boolean z) {
        this.seedingInProgress = false;
        Iterator<T> it = this.seedingCallbacks.iterator();
        while (it.hasNext()) {
            ((Consumer) it.next()).accept(Boolean.valueOf(z));
        }
        this.seedingCallbacks.clear();
    }

    static /* synthetic */ ControlStatus createRemovedStatus$default(ControlsControllerImpl controlsControllerImpl, ComponentName componentName, ControlInfo controlInfo, CharSequence charSequence, boolean z, int i, Object obj) {
        if ((i & 8) != 0) {
            z = true;
        }
        return controlsControllerImpl.createRemovedStatus(componentName, controlInfo, charSequence, z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final ControlStatus createRemovedStatus(ComponentName componentName, ControlInfo controlInfo, CharSequence charSequence, boolean z) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setPackage(componentName.getPackageName());
        Control control = new Control.StatelessBuilder(controlInfo.getControlId(), PendingIntent.getActivity(this.context, componentName.hashCode(), intent, 0)).setTitle(controlInfo.getControlTitle()).setSubtitle(controlInfo.getControlSubtitle()).setStructure(charSequence).setDeviceType(controlInfo.getDeviceType()).build();
        Intrinsics.checkExpressionValueIsNotNull(control, "control");
        return new ControlStatus(control, componentName, true, z);
    }

    @Override // com.android.systemui.controls.controller.ControlsController
    public void subscribeToFavorites(@NotNull StructureInfo structureInfo) {
        Intrinsics.checkParameterIsNotNull(structureInfo, "structureInfo");
        if (confirmAvailability()) {
            this.bindingController.subscribe(structureInfo);
        }
    }

    @Override // com.android.systemui.controls.controller.ControlsController
    public void unsubscribe() {
        if (confirmAvailability()) {
            this.bindingController.unsubscribe();
        }
    }

    @Override // com.android.systemui.controls.controller.ControlsController
    public void addFavorite(@NotNull final ComponentName componentName, @NotNull final CharSequence structureName, @NotNull final ControlInfo controlInfo) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(structureName, "structureName");
        Intrinsics.checkParameterIsNotNull(controlInfo, "controlInfo");
        if (confirmAvailability()) {
            this.executor.execute(new Runnable() { // from class: com.android.systemui.controls.controller.ControlsControllerImpl.addFavorite.1
                @Override // java.lang.Runnable
                public final void run() {
                    Favorites favorites = Favorites.INSTANCE;
                    if (favorites.addFavorite(componentName, structureName, controlInfo)) {
                        ControlsControllerImpl.this.persistenceWrapper.storeFavorites(favorites.getAllStructures());
                    }
                }
            });
        }
    }

    public void replaceFavoritesForStructure(@NotNull final StructureInfo structureInfo) {
        Intrinsics.checkParameterIsNotNull(structureInfo, "structureInfo");
        if (confirmAvailability()) {
            this.executor.execute(new Runnable() { // from class: com.android.systemui.controls.controller.ControlsControllerImpl.replaceFavoritesForStructure.1
                @Override // java.lang.Runnable
                public final void run() {
                    Favorites favorites = Favorites.INSTANCE;
                    favorites.replaceControls(structureInfo);
                    ControlsControllerImpl.this.persistenceWrapper.storeFavorites(favorites.getAllStructures());
                }
            });
        }
    }

    @Override // com.android.systemui.controls.controller.ControlsController
    public void refreshStatus(@NotNull final ComponentName componentName, @NotNull final Control control) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(control, "control");
        if (!confirmAvailability()) {
            Log.d("ControlsControllerImpl", "Controls not available");
            return;
        }
        if (control.getStatus() == 1) {
            this.executor.execute(new Runnable() { // from class: com.android.systemui.controls.controller.ControlsControllerImpl.refreshStatus.1
                @Override // java.lang.Runnable
                public final void run() {
                    Favorites favorites = Favorites.INSTANCE;
                    if (favorites.updateControls(componentName, CollectionsKt__CollectionsJVMKt.listOf(control))) {
                        ControlsControllerImpl.this.persistenceWrapper.storeFavorites(favorites.getAllStructures());
                    }
                }
            });
        }
        this.uiController.onRefreshState(componentName, CollectionsKt__CollectionsJVMKt.listOf(control));
    }

    @Override // com.android.systemui.controls.controller.ControlsController
    public void onActionResponse(@NotNull ComponentName componentName, @NotNull String controlId, int i) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(controlId, "controlId");
        if (confirmAvailability()) {
            this.uiController.onActionResponse(componentName, controlId, i);
        }
    }

    @Override // com.android.systemui.controls.controller.ControlsController
    public void action(@NotNull ComponentName componentName, @NotNull ControlInfo controlInfo, @NotNull ControlAction action) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(controlInfo, "controlInfo");
        Intrinsics.checkParameterIsNotNull(action, "action");
        if (confirmAvailability()) {
            this.bindingController.action(componentName, controlInfo, action);
        }
    }

    @Override // com.android.systemui.controls.controller.ControlsController
    @NotNull
    public List<StructureInfo> getFavorites() {
        return Favorites.INSTANCE.getAllStructures();
    }

    @Override // com.android.systemui.controls.controller.ControlsController
    public int countFavoritesForComponent(@NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        return Favorites.INSTANCE.getControlsForComponent(componentName).size();
    }

    @Override // com.android.systemui.controls.controller.ControlsController
    @NotNull
    public List<StructureInfo> getFavoritesForComponent(@NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        return Favorites.INSTANCE.getStructuresForComponent(componentName);
    }

    @NotNull
    public List<ControlInfo> getFavoritesForStructure(@NotNull ComponentName componentName, @NotNull CharSequence structureName) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(structureName, "structureName");
        return Favorites.INSTANCE.getControlsForStructure(new StructureInfo(componentName, structureName, CollectionsKt__CollectionsKt.emptyList()));
    }

    @Override // com.android.systemui.Dumpable
    public void dump(@NotNull FileDescriptor fd, @NotNull PrintWriter pw, @NotNull String[] args) {
        Intrinsics.checkParameterIsNotNull(fd, "fd");
        Intrinsics.checkParameterIsNotNull(pw, "pw");
        Intrinsics.checkParameterIsNotNull(args, "args");
        pw.println("ControlsController state:");
        pw.println("  Available: " + getAvailable());
        pw.println("  Changing users: " + this.userChanging);
        StringBuilder sb = new StringBuilder();
        sb.append("  Current user: ");
        UserHandle currentUser = this.currentUser;
        Intrinsics.checkExpressionValueIsNotNull(currentUser, "currentUser");
        sb.append(currentUser.getIdentifier());
        pw.println(sb.toString());
        pw.println("  Favorites:");
        for (StructureInfo structureInfo : Favorites.INSTANCE.getAllStructures()) {
            pw.println("    " + structureInfo);
            Iterator<T> it = structureInfo.getControls().iterator();
            while (it.hasNext()) {
                pw.println("      " + ((ControlInfo) it.next()));
            }
        }
        pw.println(this.bindingController.toString());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final Set<String> findRemoved(Set<String> set, List<Control> list) {
        ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list, 10));
        Iterator<T> it = list.iterator();
        while (it.hasNext()) {
            arrayList.add(((Control) it.next()).getControlId());
        }
        return SetsKt___SetsKt.minus(set, arrayList);
    }
}
