package com.android.systemui.statusbar.notification.stack;

import android.content.Intent;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.R;
import com.android.systemui.media.KeyguardMediaController;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.NotificationSectionsFeatureManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.people.PeopleHubViewAdapter;
import com.android.systemui.statusbar.notification.people.PeopleHubViewBoundary;
import com.android.systemui.statusbar.notification.people.Subscription;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.row.StackScrollerDecorView;
import com.android.systemui.statusbar.notification.stack.StackScrollAlgorithm;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.util.ConvenienceExtensionsKt;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import kotlin.NoWhenBranchMatchedException;
import kotlin.TypeCastException;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.collections.Grouping;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: NotificationSectionsManager.kt */
/* loaded from: classes.dex */
public final class NotificationSectionsManager implements StackScrollAlgorithm.SectionProvider {
    public static final Companion Companion = new Companion(null);
    private final ActivityStarter activityStarter;

    @Nullable
    private SectionHeaderView alertingHeaderView;
    private final ConfigurationController configurationController;
    private final NotificationSectionsManager$configurationListener$1 configurationListener;

    @Nullable
    private SectionHeaderView incomingHeaderView;
    private boolean initialized;
    private final KeyguardMediaController keyguardMediaController;
    private final NotificationSectionsLogger logger;

    @Nullable
    private MediaHeaderView mediaControlsView;
    private View.OnClickListener onClearSilentNotifsClickListener;
    private NotificationStackScrollLayout parent;

    @Nullable
    private PeopleHubView peopleHeaderView;
    private Subscription peopleHubSubscription;
    private final PeopleHubViewAdapter peopleHubViewAdapter;
    private final PeopleHubViewBoundary peopleHubViewBoundary;
    private boolean peopleHubVisible;
    private final NotificationSectionsFeatureManager sectionsFeatureManager;

    @Nullable
    private SectionHeaderView silentHeaderView;
    private final StatusBarStateController statusBarStateController;

    /* JADX INFO: Access modifiers changed from: private */
    /* compiled from: NotificationSectionsManager.kt */
    interface SectionUpdateState<T extends ExpandableView> {
        void adjustViewPosition();

        @Nullable
        Integer getCurrentPosition();

        @Nullable
        Integer getTargetPosition();

        void setCurrentPosition(@Nullable Integer num);

        void setTargetPosition(@Nullable Integer num);
    }

    /* JADX WARN: Type inference failed for: r2v1, types: [com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$configurationListener$1] */
    public NotificationSectionsManager(@NotNull ActivityStarter activityStarter, @NotNull StatusBarStateController statusBarStateController, @NotNull ConfigurationController configurationController, @NotNull PeopleHubViewAdapter peopleHubViewAdapter, @NotNull KeyguardMediaController keyguardMediaController, @NotNull NotificationSectionsFeatureManager sectionsFeatureManager, @NotNull NotificationSectionsLogger logger) {
        Intrinsics.checkParameterIsNotNull(activityStarter, "activityStarter");
        Intrinsics.checkParameterIsNotNull(statusBarStateController, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(configurationController, "configurationController");
        Intrinsics.checkParameterIsNotNull(peopleHubViewAdapter, "peopleHubViewAdapter");
        Intrinsics.checkParameterIsNotNull(keyguardMediaController, "keyguardMediaController");
        Intrinsics.checkParameterIsNotNull(sectionsFeatureManager, "sectionsFeatureManager");
        Intrinsics.checkParameterIsNotNull(logger, "logger");
        this.activityStarter = activityStarter;
        this.statusBarStateController = statusBarStateController;
        this.configurationController = configurationController;
        this.peopleHubViewAdapter = peopleHubViewAdapter;
        this.keyguardMediaController = keyguardMediaController;
        this.sectionsFeatureManager = sectionsFeatureManager;
        this.logger = logger;
        this.configurationListener = new ConfigurationController.ConfigurationListener() { // from class: com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$configurationListener$1
            @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
            public void onLocaleListChanged() {
                NotificationSectionsManager notificationSectionsManager = this.this$0;
                LayoutInflater layoutInflaterFrom = LayoutInflater.from(NotificationSectionsManager.access$getParent$p(notificationSectionsManager).getContext());
                Intrinsics.checkExpressionValueIsNotNull(layoutInflaterFrom, "LayoutInflater.from(parent.context)");
                notificationSectionsManager.reinflateViews(layoutInflaterFrom);
            }
        };
        this.peopleHubViewBoundary = new PeopleHubViewBoundary() { // from class: com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$peopleHubViewBoundary$1
        };
    }

    public static final /* synthetic */ NotificationStackScrollLayout access$getParent$p(NotificationSectionsManager notificationSectionsManager) {
        NotificationStackScrollLayout notificationStackScrollLayout = notificationSectionsManager.parent;
        if (notificationStackScrollLayout == null) {
            Intrinsics.throwUninitializedPropertyAccessException("parent");
        }
        return notificationStackScrollLayout;
    }

    @VisibleForTesting
    @Nullable
    public final SectionHeaderView getSilentHeaderView() {
        return this.silentHeaderView;
    }

    @VisibleForTesting
    @Nullable
    public final SectionHeaderView getAlertingHeaderView() {
        return this.alertingHeaderView;
    }

    @VisibleForTesting
    @Nullable
    public final SectionHeaderView getIncomingHeaderView() {
        return this.incomingHeaderView;
    }

    @VisibleForTesting
    @Nullable
    public final PeopleHubView getPeopleHeaderView() {
        return this.peopleHeaderView;
    }

    @VisibleForTesting
    public final void setPeopleHubVisible(boolean z) {
        this.peopleHubVisible = z;
    }

    @VisibleForTesting
    @Nullable
    public final MediaHeaderView getMediaControlsView() {
        return this.mediaControlsView;
    }

    public final void initialize(@NotNull NotificationStackScrollLayout parent, @NotNull LayoutInflater layoutInflater) {
        Intrinsics.checkParameterIsNotNull(parent, "parent");
        Intrinsics.checkParameterIsNotNull(layoutInflater, "layoutInflater");
        if (!(!this.initialized)) {
            throw new IllegalStateException("NotificationSectionsManager already initialized".toString());
        }
        this.initialized = true;
        this.parent = parent;
        reinflateViews(layoutInflater);
        this.configurationController.addCallback(this.configurationListener);
    }

    /* JADX WARN: Removed duplicated region for block: B:18:0x0031  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private final <T extends com.android.systemui.statusbar.notification.row.ExpandableView> T reinflateView(T r5, android.view.LayoutInflater r6, int r7) {
        /*
            r4 = this;
            r0 = -1
            java.lang.String r1 = "parent"
            if (r5 == 0) goto L31
            android.view.ViewGroup r2 = r5.getTransientContainer()
            if (r2 == 0) goto Le
            r2.removeView(r5)
        Le:
            android.view.ViewParent r2 = r5.getParent()
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r3 = r4.parent
            if (r3 != 0) goto L19
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r1)
        L19:
            if (r2 != r3) goto L31
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r2 = r4.parent
            if (r2 != 0) goto L22
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r1)
        L22:
            int r2 = r2.indexOfChild(r5)
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r3 = r4.parent
            if (r3 != 0) goto L2d
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r1)
        L2d:
            r3.removeView(r5)
            goto L32
        L31:
            r2 = r0
        L32:
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r5 = r4.parent
            if (r5 != 0) goto L39
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r1)
        L39:
            r3 = 0
            android.view.View r5 = r6.inflate(r7, r5, r3)
            if (r5 == 0) goto L4f
            com.android.systemui.statusbar.notification.row.ExpandableView r5 = (com.android.systemui.statusbar.notification.row.ExpandableView) r5
            if (r2 == r0) goto L4e
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r4 = r4.parent
            if (r4 != 0) goto L4b
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r1)
        L4b:
            r4.addView(r5, r2)
        L4e:
            return r5
        L4f:
            kotlin.TypeCastException r4 = new kotlin.TypeCastException
            java.lang.String r5 = "null cannot be cast to non-null type T"
            r4.<init>(r5)
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.reinflateView(com.android.systemui.statusbar.notification.row.ExpandableView, android.view.LayoutInflater, int):com.android.systemui.statusbar.notification.row.ExpandableView");
    }

    @NotNull
    public final NotificationSection[] createSectionsForBuckets() {
        int[] notificationBuckets = this.sectionsFeatureManager.getNotificationBuckets();
        ArrayList arrayList = new ArrayList(notificationBuckets.length);
        for (int i : notificationBuckets) {
            NotificationStackScrollLayout notificationStackScrollLayout = this.parent;
            if (notificationStackScrollLayout == null) {
                Intrinsics.throwUninitializedPropertyAccessException("parent");
            }
            arrayList.add(new NotificationSection(notificationStackScrollLayout, i));
        }
        Object[] array = arrayList.toArray(new NotificationSection[0]);
        if (array != null) {
            return (NotificationSection[]) array;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Array<T>");
    }

    public final void reinflateViews(@NotNull LayoutInflater layoutInflater) {
        Intrinsics.checkParameterIsNotNull(layoutInflater, "layoutInflater");
        SectionHeaderView sectionHeaderView = this.silentHeaderView;
        int i = R.layout.status_bar_notification_section_header;
        SectionHeaderView sectionHeaderView2 = (SectionHeaderView) reinflateView(sectionHeaderView, layoutInflater, i);
        sectionHeaderView2.setHeaderText(R.string.notification_section_header_gentle);
        sectionHeaderView2.setOnHeaderClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$reinflateViews$$inlined$apply$lambda$1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.this$0.onGentleHeaderClick();
            }
        });
        sectionHeaderView2.setOnClearAllClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$reinflateViews$$inlined$apply$lambda$2
            @Override // android.view.View.OnClickListener
            public final void onClick(View it) {
                NotificationSectionsManager notificationSectionsManager = this.this$0;
                Intrinsics.checkExpressionValueIsNotNull(it, "it");
                notificationSectionsManager.onClearGentleNotifsClick(it);
            }
        });
        this.silentHeaderView = sectionHeaderView2;
        SectionHeaderView sectionHeaderView3 = (SectionHeaderView) reinflateView(this.alertingHeaderView, layoutInflater, i);
        sectionHeaderView3.setHeaderText(R.string.notification_section_header_alerting);
        sectionHeaderView3.setOnHeaderClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$reinflateViews$$inlined$apply$lambda$3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.this$0.onGentleHeaderClick();
            }
        });
        this.alertingHeaderView = sectionHeaderView3;
        Subscription subscription = this.peopleHubSubscription;
        if (subscription != null) {
            subscription.unsubscribe();
        }
        this.peopleHubSubscription = null;
        PeopleHubView peopleHubView = (PeopleHubView) reinflateView(this.peopleHeaderView, layoutInflater, R.layout.people_strip);
        peopleHubView.setOnHeaderClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$reinflateViews$$inlined$apply$lambda$4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.this$0.onPeopleHeaderClick();
            }
        });
        this.peopleHeaderView = peopleHubView;
        SectionHeaderView sectionHeaderView4 = (SectionHeaderView) reinflateView(this.incomingHeaderView, layoutInflater, i);
        sectionHeaderView4.setHeaderText(R.string.notification_section_header_incoming);
        sectionHeaderView4.setOnHeaderClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$reinflateViews$$inlined$apply$lambda$5
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.this$0.onGentleHeaderClick();
            }
        });
        this.incomingHeaderView = sectionHeaderView4;
        MediaHeaderView mediaHeaderView = (MediaHeaderView) reinflateView(this.mediaControlsView, layoutInflater, R.layout.keyguard_media_header);
        this.keyguardMediaController.attach(mediaHeaderView);
        this.mediaControlsView = mediaHeaderView;
    }

    @Override // com.android.systemui.statusbar.notification.stack.StackScrollAlgorithm.SectionProvider
    public boolean beginsSection(@NotNull View view, @Nullable View view2) {
        Intrinsics.checkParameterIsNotNull(view, "view");
        return view == this.silentHeaderView || view == this.mediaControlsView || view == this.peopleHeaderView || view == this.alertingHeaderView || view == this.incomingHeaderView || (Intrinsics.areEqual(getBucket(view), getBucket(view2)) ^ true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final Integer getBucket(View view) {
        if (view == this.silentHeaderView) {
            return 6;
        }
        if (view == this.incomingHeaderView) {
            return 2;
        }
        if (view == this.mediaControlsView) {
            return 1;
        }
        if (view == this.peopleHeaderView) {
            return 4;
        }
        if (view == this.alertingHeaderView) {
            return 5;
        }
        if (!(view instanceof ExpandableNotificationRow)) {
            return null;
        }
        NotificationEntry entry = ((ExpandableNotificationRow) view).getEntry();
        Intrinsics.checkExpressionValueIsNotNull(entry, "view.entry");
        return Integer.valueOf(entry.getBucket());
    }

    private final void logShadeChild(int i, View view) {
        if (view == this.incomingHeaderView) {
            this.logger.logIncomingHeader(i);
            return;
        }
        if (view == this.mediaControlsView) {
            this.logger.logMediaControls(i);
            return;
        }
        if (view == this.peopleHeaderView) {
            this.logger.logConversationsHeader(i);
            return;
        }
        if (view == this.alertingHeaderView) {
            this.logger.logAlertingHeader(i);
            return;
        }
        if (view == this.silentHeaderView) {
            this.logger.logSilentHeader(i);
            return;
        }
        if (!(view instanceof ExpandableNotificationRow)) {
            this.logger.logOther(i, view.getClass());
            return;
        }
        ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
        boolean zIsHeadsUp = expandableNotificationRow.isHeadsUp();
        NotificationEntry entry = expandableNotificationRow.getEntry();
        Intrinsics.checkExpressionValueIsNotNull(entry, "child.entry");
        int bucket = entry.getBucket();
        if (bucket == 2) {
            this.logger.logHeadsUp(i, zIsHeadsUp);
            return;
        }
        if (bucket == 4) {
            this.logger.logConversation(i, zIsHeadsUp);
        } else if (bucket == 5) {
            this.logger.logAlerting(i, zIsHeadsUp);
        } else {
            if (bucket != 6) {
                return;
            }
            this.logger.logSilent(i, zIsHeadsUp);
        }
    }

    private final void logShadeContents() {
        NotificationStackScrollLayout notificationStackScrollLayout = this.parent;
        if (notificationStackScrollLayout == null) {
            Intrinsics.throwUninitializedPropertyAccessException("parent");
        }
        int i = 0;
        for (View view : ConvenienceExtensionsKt.getChildren(notificationStackScrollLayout)) {
            int i2 = i + 1;
            if (i < 0) {
                CollectionsKt__CollectionsKt.throwIndexOverflow();
            }
            logShadeChild(i, view);
            i = i2;
        }
    }

    private final boolean isUsingMultipleSections() {
        return this.sectionsFeatureManager.getNumberOfBuckets() > 1;
    }

    @VisibleForTesting
    public final void updateSectionBoundaries() {
        updateSectionBoundaries("test");
    }

    private final <T extends ExpandableView> SectionUpdateState<T> expandableViewHeaderState(T t) {
        return (SectionUpdateState<T>) new SectionUpdateState<T>(t) { // from class: com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.expandableViewHeaderState.1
            final /* synthetic */ ExpandableView $header;

            @Nullable
            private Integer currentPosition;

            /* JADX INFO: Incorrect field signature: TT; */
            @NotNull
            private final ExpandableView header;

            @Nullable
            private Integer targetPosition;

            {
                this.$header = t;
                this.header = t;
            }

            @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState
            @Nullable
            public Integer getCurrentPosition() {
                return this.currentPosition;
            }

            @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState
            public void setCurrentPosition(@Nullable Integer num) {
                this.currentPosition = num;
            }

            @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState
            @Nullable
            public Integer getTargetPosition() {
                return this.targetPosition;
            }

            @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState
            public void setTargetPosition(@Nullable Integer num) {
                this.targetPosition = num;
            }

            @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState
            public void adjustViewPosition() {
                Integer targetPosition = getTargetPosition();
                Integer currentPosition = getCurrentPosition();
                if (targetPosition == null) {
                    if (currentPosition != null) {
                        NotificationSectionsManager.access$getParent$p(NotificationSectionsManager.this).removeView(this.$header);
                    }
                } else {
                    if (currentPosition == null) {
                        ViewGroup transientContainer = this.$header.getTransientContainer();
                        if (transientContainer != null) {
                            transientContainer.removeTransientView(this.$header);
                        }
                        this.$header.setTransientContainer(null);
                        NotificationSectionsManager.access$getParent$p(NotificationSectionsManager.this).addView(this.$header, targetPosition.intValue());
                        return;
                    }
                    NotificationSectionsManager.access$getParent$p(NotificationSectionsManager.this).changeViewPosition(this.$header, targetPosition.intValue());
                }
            }
        };
    }

    private final <T extends StackScrollerDecorView> SectionUpdateState<T> decorViewHeaderState(T t) {
        return (SectionUpdateState<T>) new SectionUpdateState<T>(t) { // from class: com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.decorViewHeaderState.1
            private final /* synthetic */ SectionUpdateState $$delegate_0;
            final /* synthetic */ StackScrollerDecorView $header;

            @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState
            @Nullable
            public Integer getCurrentPosition() {
                return this.$$delegate_0.getCurrentPosition();
            }

            @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState
            @Nullable
            public Integer getTargetPosition() {
                return this.$$delegate_0.getTargetPosition();
            }

            @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState
            public void setCurrentPosition(@Nullable Integer num) {
                this.$$delegate_0.setCurrentPosition(num);
            }

            @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState
            public void setTargetPosition(@Nullable Integer num) {
                this.$$delegate_0.setTargetPosition(num);
            }

            {
                this.$header = t;
                this.$$delegate_0 = this.$inner;
            }

            @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState
            public void adjustViewPosition() {
                this.$inner.adjustViewPosition();
                if (getTargetPosition() == null || getCurrentPosition() != null) {
                    return;
                }
                this.$header.setContentVisible(true);
            }
        };
    }

    /* JADX WARN: Removed duplicated region for block: B:130:0x01e5  */
    /* JADX WARN: Removed duplicated region for block: B:163:0x0251  */
    /* JADX WARN: Removed duplicated region for block: B:165:0x0257  */
    /* JADX WARN: Removed duplicated region for block: B:63:0x0116  */
    /* JADX WARN: Removed duplicated region for block: B:75:0x0135  */
    /* JADX WARN: Removed duplicated region for block: B:80:0x0142  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public final void updateSectionBoundaries(@org.jetbrains.annotations.NotNull java.lang.String r24) {
        /*
            Method dump skipped, instructions count: 829
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.updateSectionBoundaries(java.lang.String):void");
    }

    /* compiled from: NotificationSectionsManager.kt */
    private static abstract class SectionBounds {
        private SectionBounds() {
        }

        public /* synthetic */ SectionBounds(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        /* compiled from: NotificationSectionsManager.kt */
        public static final class Many extends SectionBounds {

            @NotNull
            private final ExpandableView first;

            @NotNull
            private final ExpandableView last;

            public static /* synthetic */ Many copy$default(Many many, ExpandableView expandableView, ExpandableView expandableView2, int i, Object obj) {
                if ((i & 1) != 0) {
                    expandableView = many.first;
                }
                if ((i & 2) != 0) {
                    expandableView2 = many.last;
                }
                return many.copy(expandableView, expandableView2);
            }

            @NotNull
            public final Many copy(@NotNull ExpandableView first, @NotNull ExpandableView last) {
                Intrinsics.checkParameterIsNotNull(first, "first");
                Intrinsics.checkParameterIsNotNull(last, "last");
                return new Many(first, last);
            }

            public boolean equals(@Nullable Object obj) {
                if (this == obj) {
                    return true;
                }
                if (!(obj instanceof Many)) {
                    return false;
                }
                Many many = (Many) obj;
                return Intrinsics.areEqual(this.first, many.first) && Intrinsics.areEqual(this.last, many.last);
            }

            public int hashCode() {
                ExpandableView expandableView = this.first;
                int iHashCode = (expandableView != null ? expandableView.hashCode() : 0) * 31;
                ExpandableView expandableView2 = this.last;
                return iHashCode + (expandableView2 != null ? expandableView2.hashCode() : 0);
            }

            @NotNull
            public String toString() {
                return "Many(first=" + this.first + ", last=" + this.last + ")";
            }

            @NotNull
            public final ExpandableView getFirst() {
                return this.first;
            }

            @NotNull
            public final ExpandableView getLast() {
                return this.last;
            }

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            public Many(@NotNull ExpandableView first, @NotNull ExpandableView last) {
                super(null);
                Intrinsics.checkParameterIsNotNull(first, "first");
                Intrinsics.checkParameterIsNotNull(last, "last");
                this.first = first;
                this.last = last;
            }
        }

        /* compiled from: NotificationSectionsManager.kt */
        public static final class One extends SectionBounds {

            @NotNull
            private final ExpandableView lone;

            public boolean equals(@Nullable Object obj) {
                if (this != obj) {
                    return (obj instanceof One) && Intrinsics.areEqual(this.lone, ((One) obj).lone);
                }
                return true;
            }

            public int hashCode() {
                ExpandableView expandableView = this.lone;
                if (expandableView != null) {
                    return expandableView.hashCode();
                }
                return 0;
            }

            @NotNull
            public String toString() {
                return "One(lone=" + this.lone + ")";
            }

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            public One(@NotNull ExpandableView lone) {
                super(null);
                Intrinsics.checkParameterIsNotNull(lone, "lone");
                this.lone = lone;
            }

            @NotNull
            public final ExpandableView getLone() {
                return this.lone;
            }
        }

        /* compiled from: NotificationSectionsManager.kt */
        public static final class None extends SectionBounds {
            public static final None INSTANCE = new None();

            private None() {
                super(null);
            }
        }

        @NotNull
        public final SectionBounds addNotif(@NotNull ExpandableView notif) {
            Intrinsics.checkParameterIsNotNull(notif, "notif");
            if (this instanceof None) {
                return new One(notif);
            }
            if (this instanceof One) {
                return new Many(((One) this).getLone(), notif);
            }
            if (this instanceof Many) {
                return Many.copy$default((Many) this, null, notif, 1, null);
            }
            throw new NoWhenBranchMatchedException();
        }

        public final boolean updateSection(@NotNull NotificationSection section) {
            Intrinsics.checkParameterIsNotNull(section, "section");
            if (this instanceof None) {
                return setFirstAndLastVisibleChildren(section, null, null);
            }
            if (this instanceof One) {
                One one = (One) this;
                return setFirstAndLastVisibleChildren(section, one.getLone(), one.getLone());
            }
            if (!(this instanceof Many)) {
                throw new NoWhenBranchMatchedException();
            }
            Many many = (Many) this;
            return setFirstAndLastVisibleChildren(section, many.getFirst(), many.getLast());
        }

        private final boolean setFirstAndLastVisibleChildren(@NotNull NotificationSection notificationSection, ExpandableView expandableView, ExpandableView expandableView2) {
            return notificationSection.setFirstVisibleChild(expandableView) || notificationSection.setLastVisibleChild(expandableView2);
        }
    }

    public final boolean updateFirstAndLastViewsForAllSections(@NotNull NotificationSection[] sections, @NotNull List<? extends ExpandableView> children) {
        SparseArray sparseArray;
        Intrinsics.checkParameterIsNotNull(sections, "sections");
        Intrinsics.checkParameterIsNotNull(children, "children");
        final Sequence sequenceAsSequence = CollectionsKt___CollectionsKt.asSequence(children);
        Grouping<ExpandableView, Integer> grouping = new Grouping<ExpandableView, Integer>() { // from class: com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$updateFirstAndLastViewsForAllSections$$inlined$groupingBy$1
            @Override // kotlin.collections.Grouping
            @NotNull
            public Iterator<ExpandableView> sourceIterator() {
                return sequenceAsSequence.iterator();
            }

            @Override // kotlin.collections.Grouping
            public Integer keyOf(ExpandableView expandableView) {
                Integer bucket = this.getBucket(expandableView);
                if (bucket != null) {
                    return Integer.valueOf(bucket.intValue());
                }
                throw new IllegalArgumentException("Cannot find section bucket for view");
            }
        };
        SectionBounds.None none = SectionBounds.None.INSTANCE;
        int length = sections.length;
        if (length < 0) {
            sparseArray = new SparseArray();
        } else {
            sparseArray = new SparseArray(length);
        }
        Iterator<ExpandableView> itSourceIterator = grouping.sourceIterator();
        while (itSourceIterator.hasNext()) {
            ExpandableView next = itSourceIterator.next();
            int iIntValue = grouping.keyOf(next).intValue();
            Object obj = sparseArray.get(iIntValue);
            if (obj == null) {
                obj = none;
            }
            sparseArray.put(iIntValue, ((SectionBounds) obj).addNotif(next));
        }
        boolean z = false;
        for (NotificationSection notificationSection : sections) {
            SectionBounds sectionBounds = (SectionBounds) sparseArray.get(notificationSection.getBucket());
            if (sectionBounds == null) {
                sectionBounds = SectionBounds.None.INSTANCE;
            }
            z = sectionBounds.updateSection(notificationSection) || z;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void onGentleHeaderClick() {
        this.activityStarter.startActivity(new Intent("android.settings.NOTIFICATION_SETTINGS"), true, true, 536870912);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void onPeopleHeaderClick() {
        this.activityStarter.startActivity(new Intent("android.settings.CONVERSATION_SETTINGS"), true, true, 536870912);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void onClearGentleNotifsClick(View view) {
        View.OnClickListener onClickListener = this.onClearSilentNotifsClickListener;
        if (onClickListener != null) {
            onClickListener.onClick(view);
        }
    }

    public final void setOnClearSilentNotifsClickListener(@NotNull View.OnClickListener listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        this.onClearSilentNotifsClickListener = listener;
    }

    public final void hidePeopleRow() {
        this.peopleHubVisible = false;
        updateSectionBoundaries("PeopleHub dismissed");
    }

    public final void setHeaderForegroundColor(int i) {
        PeopleHubView peopleHubView = this.peopleHeaderView;
        if (peopleHubView != null) {
            peopleHubView.setTextColor(i);
        }
        SectionHeaderView sectionHeaderView = this.silentHeaderView;
        if (sectionHeaderView != null) {
            sectionHeaderView.setForegroundColor(i);
        }
        SectionHeaderView sectionHeaderView2 = this.alertingHeaderView;
        if (sectionHeaderView2 != null) {
            sectionHeaderView2.setForegroundColor(i);
        }
    }

    /* compiled from: NotificationSectionsManager.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }
}
