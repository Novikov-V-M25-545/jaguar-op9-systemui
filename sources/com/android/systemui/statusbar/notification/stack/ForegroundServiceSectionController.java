package com.android.systemui.statusbar.notification.stack;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.systemui.R;
import com.android.systemui.statusbar.NotificationRemoveInterceptor;
import com.android.systemui.statusbar.notification.ForegroundServiceDismissalFeatureController;
import com.android.systemui.statusbar.notification.NotificationEntryListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.DungeonRow;
import com.android.systemui.util.Assert;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import kotlin.TypeCastException;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.comparisons.ComparisonsKt__ComparisonsKt;
import kotlin.jvm.functions.Function3;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ForegroundServiceSectionController.kt */
/* loaded from: classes.dex */
public final class ForegroundServiceSectionController {
    private final String TAG;
    private final Set<NotificationEntry> entries;
    private View entriesView;

    @NotNull
    private final NotificationEntryManager entryManager;

    @NotNull
    private final ForegroundServiceDismissalFeatureController featureController;

    /* compiled from: ForegroundServiceSectionController.kt */
    /* renamed from: com.android.systemui.statusbar.notification.stack.ForegroundServiceSectionController$1, reason: invalid class name */
    static final /* synthetic */ class AnonymousClass1 extends FunctionReference implements Function3<String, NotificationEntry, Integer, Boolean> {
        AnonymousClass1(ForegroundServiceSectionController foregroundServiceSectionController) {
            super(3, foregroundServiceSectionController);
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final String getName() {
            return "shouldInterceptRemoval";
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final KDeclarationContainer getOwner() {
            return Reflection.getOrCreateKotlinClass(ForegroundServiceSectionController.class);
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final String getSignature() {
            return "shouldInterceptRemoval(Ljava/lang/String;Lcom/android/systemui/statusbar/notification/collection/NotificationEntry;I)Z";
        }

        @Override // kotlin.jvm.functions.Function3
        public /* bridge */ /* synthetic */ Boolean invoke(String str, NotificationEntry notificationEntry, Integer num) {
            return Boolean.valueOf(invoke(str, notificationEntry, num.intValue()));
        }

        public final boolean invoke(@NotNull String p1, @Nullable NotificationEntry notificationEntry, int i) {
            Intrinsics.checkParameterIsNotNull(p1, "p1");
            return ((ForegroundServiceSectionController) this.receiver).shouldInterceptRemoval(p1, notificationEntry, i);
        }
    }

    public ForegroundServiceSectionController(@NotNull NotificationEntryManager entryManager, @NotNull ForegroundServiceDismissalFeatureController featureController) {
        Intrinsics.checkParameterIsNotNull(entryManager, "entryManager");
        Intrinsics.checkParameterIsNotNull(featureController, "featureController");
        this.entryManager = entryManager;
        this.featureController = featureController;
        this.TAG = "FgsSectionController";
        this.entries = new LinkedHashSet();
        if (featureController.isForegroundServiceDismissalEnabled()) {
            final AnonymousClass1 anonymousClass1 = new AnonymousClass1(this);
            entryManager.addNotificationRemoveInterceptor(new NotificationRemoveInterceptor() { // from class: com.android.systemui.statusbar.notification.stack.ForegroundServiceSectionController$sam$com_android_systemui_statusbar_NotificationRemoveInterceptor$0
                @Override // com.android.systemui.statusbar.NotificationRemoveInterceptor
                public final /* synthetic */ boolean onNotificationRemoveRequested(@NotNull String key, @Nullable NotificationEntry notificationEntry, int i) {
                    Intrinsics.checkParameterIsNotNull(key, "key");
                    Object objInvoke = anonymousClass1.invoke(key, notificationEntry, Integer.valueOf(i));
                    Intrinsics.checkExpressionValueIsNotNull(objInvoke, "invoke(...)");
                    return ((Boolean) objInvoke).booleanValue();
                }
            });
            entryManager.addNotificationEntryListener(new NotificationEntryListener() { // from class: com.android.systemui.statusbar.notification.stack.ForegroundServiceSectionController.2
                @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
                public void onPostEntryUpdated(@NotNull NotificationEntry entry) {
                    Intrinsics.checkParameterIsNotNull(entry, "entry");
                    if (ForegroundServiceSectionController.this.entries.contains(entry)) {
                        ForegroundServiceSectionController.this.removeEntry(entry);
                        ForegroundServiceSectionController.this.addEntry(entry);
                        ForegroundServiceSectionController.this.update();
                    }
                }
            });
        }
    }

    @NotNull
    public final NotificationEntryManager getEntryManager() {
        return this.entryManager;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final boolean shouldInterceptRemoval(String str, NotificationEntry notificationEntry, int i) {
        Assert.isMainThread();
        boolean z = i == 3;
        boolean z2 = i == 2 || i == 1;
        if (i != 8) {
        }
        boolean z3 = i == 12;
        if (notificationEntry == null) {
            return false;
        }
        if (z2) {
            StatusBarNotification sbn = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
            if (!sbn.isClearable()) {
                if (!hasEntry(notificationEntry)) {
                    addEntry(notificationEntry);
                    update();
                }
                this.entryManager.updateNotifications("FgsSectionController.onNotificationRemoveRequested");
                return true;
            }
        }
        if (z || z3) {
            StatusBarNotification sbn2 = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn2, "entry.sbn");
            if (!sbn2.isClearable()) {
                return true;
            }
        }
        if (hasEntry(notificationEntry)) {
            removeEntry(notificationEntry);
            update();
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void removeEntry(NotificationEntry notificationEntry) {
        Assert.isMainThread();
        this.entries.remove(notificationEntry);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void addEntry(NotificationEntry notificationEntry) {
        Assert.isMainThread();
        this.entries.add(notificationEntry);
    }

    public final boolean hasEntry(@NotNull NotificationEntry entry) {
        Intrinsics.checkParameterIsNotNull(entry, "entry");
        Assert.isMainThread();
        return this.entries.contains(entry);
    }

    @NotNull
    public final View createView(@NotNull LayoutInflater li) {
        Intrinsics.checkParameterIsNotNull(li, "li");
        View viewInflate = li.inflate(R.layout.foreground_service_dungeon, (ViewGroup) null);
        this.entriesView = viewInflate;
        if (viewInflate == null) {
            Intrinsics.throwNpe();
        }
        viewInflate.setVisibility(8);
        View view = this.entriesView;
        if (view == null) {
            Intrinsics.throwNpe();
        }
        return view;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void update() {
        Assert.isMainThread();
        View view = this.entriesView;
        if (view == null) {
            throw new IllegalStateException("ForegroundServiceSectionController is trying to show dismissed fgs notifications without having been initialized!");
        }
        if (view == null) {
            Intrinsics.throwNpe();
        }
        View viewFindViewById = view.findViewById(R.id.entry_list);
        if (viewFindViewById == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.widget.LinearLayout");
        }
        final LinearLayout linearLayout = (LinearLayout) viewFindViewById;
        linearLayout.removeAllViews();
        for (final NotificationEntry notificationEntry : CollectionsKt___CollectionsKt.sortedWith(this.entries, new Comparator<T>() { // from class: com.android.systemui.statusbar.notification.stack.ForegroundServiceSectionController$$special$$inlined$sortedBy$1
            /* JADX WARN: Multi-variable type inference failed */
            @Override // java.util.Comparator
            public final int compare(T t, T t2) {
                NotificationListenerService.Ranking ranking = ((NotificationEntry) t).getRanking();
                Intrinsics.checkExpressionValueIsNotNull(ranking, "it.ranking");
                Integer numValueOf = Integer.valueOf(ranking.getRank());
                NotificationListenerService.Ranking ranking2 = ((NotificationEntry) t2).getRanking();
                Intrinsics.checkExpressionValueIsNotNull(ranking2, "it.ranking");
                return ComparisonsKt__ComparisonsKt.compareValues(numValueOf, Integer.valueOf(ranking2.getRank()));
            }
        })) {
            View viewInflate = LayoutInflater.from(linearLayout.getContext()).inflate(R.layout.foreground_service_dungeon_row, (ViewGroup) null);
            if (viewInflate == null) {
                throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.statusbar.notification.row.DungeonRow");
            }
            final DungeonRow dungeonRow = (DungeonRow) viewInflate;
            dungeonRow.setEntry(notificationEntry);
            dungeonRow.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.stack.ForegroundServiceSectionController$update$$inlined$apply$lambda$1
                @Override // android.view.View.OnClickListener
                public final void onClick(View view2) {
                    ForegroundServiceSectionController foregroundServiceSectionController = this;
                    NotificationEntry entry = dungeonRow.getEntry();
                    if (entry == null) {
                        Intrinsics.throwNpe();
                    }
                    foregroundServiceSectionController.removeEntry(entry);
                    this.update();
                    notificationEntry.getRow().unDismiss();
                    notificationEntry.getRow().resetTranslation();
                    this.getEntryManager().updateNotifications("ForegroundServiceSectionController.onClick");
                }
            });
            linearLayout.addView(dungeonRow);
        }
        if (this.entries.isEmpty()) {
            View view2 = this.entriesView;
            if (view2 != null) {
                view2.setVisibility(8);
                return;
            }
            return;
        }
        View view3 = this.entriesView;
        if (view3 != null) {
            view3.setVisibility(0);
        }
    }
}
