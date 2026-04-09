package com.android.systemui.statusbar.notification.row;

import android.app.INotificationManager;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ParceledListSlice;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.R;
import com.android.systemui.statusbar.notification.row.ChannelEditorDialog;
import com.android.systemui.statusbar.notification.row.NotificationInfo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.collections.CollectionsKt__MutableCollectionsKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.comparisons.ComparisonsKt__ComparisonsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt___SequencesKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ChannelEditorDialogController.kt */
/* loaded from: classes.dex */
public final class ChannelEditorDialogController {
    private Drawable appIcon;
    private String appName;
    private Boolean appNotificationsCurrentlyEnabled;
    private boolean appNotificationsEnabled;
    private Integer appUid;
    private final List<NotificationChannelGroup> channelGroupList;

    @NotNull
    private final Context context;
    private ChannelEditorDialog dialog;
    private final ChannelEditorDialog.Builder dialogBuilder;
    private final Map<NotificationChannel, Integer> edits;

    @NotNull
    private final HashMap<String, CharSequence> groupNameLookup;
    private final INotificationManager noMan;

    @Nullable
    private OnChannelEditorDialogFinishedListener onFinishListener;
    private NotificationInfo.OnSettingsClickListener onSettingsClickListener;
    private String packageName;

    @NotNull
    private final List<NotificationChannel> paddedChannels;
    private boolean prepared;
    private final List<NotificationChannel> providedChannels;
    private final int wmFlags;

    @VisibleForTesting
    public static /* synthetic */ void groupNameLookup$annotations() {
    }

    @VisibleForTesting
    public static /* synthetic */ void paddedChannels$annotations() {
    }

    public ChannelEditorDialogController(@NotNull Context c, @NotNull INotificationManager noMan, @NotNull ChannelEditorDialog.Builder dialogBuilder) {
        Intrinsics.checkParameterIsNotNull(c, "c");
        Intrinsics.checkParameterIsNotNull(noMan, "noMan");
        Intrinsics.checkParameterIsNotNull(dialogBuilder, "dialogBuilder");
        this.noMan = noMan;
        this.dialogBuilder = dialogBuilder;
        Context applicationContext = c.getApplicationContext();
        Intrinsics.checkExpressionValueIsNotNull(applicationContext, "c.applicationContext");
        this.context = applicationContext;
        this.paddedChannels = new ArrayList();
        this.providedChannels = new ArrayList();
        this.edits = new LinkedHashMap();
        this.appNotificationsEnabled = true;
        this.groupNameLookup = new HashMap<>();
        this.channelGroupList = new ArrayList();
        this.wmFlags = -2130444288;
    }

    @Nullable
    public final OnChannelEditorDialogFinishedListener getOnFinishListener() {
        return this.onFinishListener;
    }

    public final void setOnFinishListener(@Nullable OnChannelEditorDialogFinishedListener onChannelEditorDialogFinishedListener) {
        this.onFinishListener = onChannelEditorDialogFinishedListener;
    }

    @NotNull
    public final List<NotificationChannel> getPaddedChannels$frameworks__base__packages__SystemUI__android_common__SystemUI_core() {
        return this.paddedChannels;
    }

    public final void prepareDialogForApp(@NotNull String appName, @NotNull String packageName, int i, @NotNull Set<NotificationChannel> channels, @NotNull Drawable appIcon, @Nullable NotificationInfo.OnSettingsClickListener onSettingsClickListener) {
        Intrinsics.checkParameterIsNotNull(appName, "appName");
        Intrinsics.checkParameterIsNotNull(packageName, "packageName");
        Intrinsics.checkParameterIsNotNull(channels, "channels");
        Intrinsics.checkParameterIsNotNull(appIcon, "appIcon");
        this.appName = appName;
        this.packageName = packageName;
        this.appUid = Integer.valueOf(i);
        this.appIcon = appIcon;
        boolean zCheckAreAppNotificationsOn = checkAreAppNotificationsOn();
        this.appNotificationsEnabled = zCheckAreAppNotificationsOn;
        this.onSettingsClickListener = onSettingsClickListener;
        this.appNotificationsCurrentlyEnabled = Boolean.valueOf(zCheckAreAppNotificationsOn);
        this.channelGroupList.clear();
        this.channelGroupList.addAll(fetchNotificationChannelGroups());
        buildGroupNameLookup();
        this.providedChannels.clear();
        this.providedChannels.addAll(channels);
        padToFourChannels(channels);
        initDialog();
        this.prepared = true;
    }

    private final void buildGroupNameLookup() {
        for (NotificationChannelGroup notificationChannelGroup : this.channelGroupList) {
            if (notificationChannelGroup.getId() != null) {
                HashMap<String, CharSequence> map = this.groupNameLookup;
                String id = notificationChannelGroup.getId();
                Intrinsics.checkExpressionValueIsNotNull(id, "group.id");
                CharSequence name = notificationChannelGroup.getName();
                Intrinsics.checkExpressionValueIsNotNull(name, "group.name");
                map.put(id, name);
            }
        }
    }

    private final void padToFourChannels(Set<NotificationChannel> set) {
        this.paddedChannels.clear();
        CollectionsKt__MutableCollectionsKt.addAll(this.paddedChannels, SequencesKt___SequencesKt.take(CollectionsKt___CollectionsKt.asSequence(set), 4));
        CollectionsKt__MutableCollectionsKt.addAll(this.paddedChannels, SequencesKt___SequencesKt.take(SequencesKt___SequencesKt.distinct(SequencesKt___SequencesKt.filterNot(getDisplayableChannels(CollectionsKt___CollectionsKt.asSequence(this.channelGroupList)), new Function1<NotificationChannel, Boolean>() { // from class: com.android.systemui.statusbar.notification.row.ChannelEditorDialogController.padToFourChannels.1
            {
                super(1);
            }

            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Boolean invoke(NotificationChannel notificationChannel) {
                return Boolean.valueOf(invoke2(notificationChannel));
            }

            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final boolean invoke2(@NotNull NotificationChannel it) {
                Intrinsics.checkParameterIsNotNull(it, "it");
                return ChannelEditorDialogController.this.getPaddedChannels$frameworks__base__packages__SystemUI__android_common__SystemUI_core().contains(it);
            }
        })), 4 - this.paddedChannels.size()));
        if (this.paddedChannels.size() == 1 && Intrinsics.areEqual("miscellaneous", this.paddedChannels.get(0).getId())) {
            this.paddedChannels.clear();
        }
    }

    private final Sequence<NotificationChannel> getDisplayableChannels(Sequence<NotificationChannelGroup> sequence) {
        return SequencesKt___SequencesKt.sortedWith(SequencesKt___SequencesKt.flatMap(sequence, new Function1<NotificationChannelGroup, Sequence<? extends NotificationChannel>>() { // from class: com.android.systemui.statusbar.notification.row.ChannelEditorDialogController$getDisplayableChannels$channels$1
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final Sequence<NotificationChannel> invoke(@NotNull NotificationChannelGroup group) {
                Intrinsics.checkParameterIsNotNull(group, "group");
                List<NotificationChannel> channels = group.getChannels();
                Intrinsics.checkExpressionValueIsNotNull(channels, "group.channels");
                return SequencesKt___SequencesKt.filterNot(CollectionsKt___CollectionsKt.asSequence(channels), new Function1<NotificationChannel, Boolean>() { // from class: com.android.systemui.statusbar.notification.row.ChannelEditorDialogController$getDisplayableChannels$channels$1.1
                    @Override // kotlin.jvm.functions.Function1
                    public /* bridge */ /* synthetic */ Boolean invoke(NotificationChannel notificationChannel) {
                        return Boolean.valueOf(invoke2(notificationChannel));
                    }

                    /* renamed from: invoke, reason: avoid collision after fix types in other method */
                    public final boolean invoke2(NotificationChannel channel) {
                        Intrinsics.checkExpressionValueIsNotNull(channel, "channel");
                        return channel.isImportanceLockedByOEM() || channel.getImportance() == 0 || channel.isImportanceLockedByCriticalDeviceFunction();
                    }
                });
            }
        }), new Comparator<T>() { // from class: com.android.systemui.statusbar.notification.row.ChannelEditorDialogController$getDisplayableChannels$$inlined$compareBy$1
            /* JADX WARN: Multi-variable type inference failed */
            @Override // java.util.Comparator
            public final int compare(T t, T t2) {
                String id;
                String id2;
                NotificationChannel it = (NotificationChannel) t;
                Intrinsics.checkExpressionValueIsNotNull(it, "it");
                CharSequence name = it.getName();
                if (name == null || (id = name.toString()) == null) {
                    id = it.getId();
                }
                NotificationChannel it2 = (NotificationChannel) t2;
                Intrinsics.checkExpressionValueIsNotNull(it2, "it");
                CharSequence name2 = it2.getName();
                if (name2 == null || (id2 = name2.toString()) == null) {
                    id2 = it2.getId();
                }
                return ComparisonsKt__ComparisonsKt.compareValues(id, id2);
            }
        });
    }

    public final void show() {
        if (!this.prepared) {
            throw new IllegalStateException("Must call prepareDialogForApp() before calling show()");
        }
        ChannelEditorDialog channelEditorDialog = this.dialog;
        if (channelEditorDialog == null) {
            Intrinsics.throwUninitializedPropertyAccessException("dialog");
        }
        channelEditorDialog.show();
    }

    public final void close() {
        done();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void done() {
        resetState();
        ChannelEditorDialog channelEditorDialog = this.dialog;
        if (channelEditorDialog == null) {
            Intrinsics.throwUninitializedPropertyAccessException("dialog");
        }
        channelEditorDialog.dismiss();
    }

    private final void resetState() {
        this.appIcon = null;
        this.appUid = null;
        this.packageName = null;
        this.appName = null;
        this.appNotificationsCurrentlyEnabled = null;
        this.edits.clear();
        this.paddedChannels.clear();
        this.providedChannels.clear();
        this.groupNameLookup.clear();
    }

    @NotNull
    public final CharSequence groupNameForId(@Nullable String str) {
        CharSequence charSequence = this.groupNameLookup.get(str);
        return charSequence != null ? charSequence : "";
    }

    public final void proposeEditForChannel(@NotNull NotificationChannel channel, int i) {
        Intrinsics.checkParameterIsNotNull(channel, "channel");
        if (channel.getImportance() == i) {
            this.edits.remove(channel);
        } else {
            this.edits.put(channel, Integer.valueOf(i));
        }
        ChannelEditorDialog channelEditorDialog = this.dialog;
        if (channelEditorDialog == null) {
            Intrinsics.throwUninitializedPropertyAccessException("dialog");
        }
        channelEditorDialog.updateDoneButtonText(hasChanges());
    }

    public final void proposeSetAppNotificationsEnabled(boolean z) {
        this.appNotificationsEnabled = z;
        ChannelEditorDialog channelEditorDialog = this.dialog;
        if (channelEditorDialog == null) {
            Intrinsics.throwUninitializedPropertyAccessException("dialog");
        }
        channelEditorDialog.updateDoneButtonText(hasChanges());
    }

    public final boolean areAppNotificationsEnabled() {
        return this.appNotificationsEnabled;
    }

    private final boolean hasChanges() {
        return (this.edits.isEmpty() ^ true) || (Intrinsics.areEqual(Boolean.valueOf(this.appNotificationsEnabled), this.appNotificationsCurrentlyEnabled) ^ true);
    }

    private final List<NotificationChannelGroup> fetchNotificationChannelGroups() {
        try {
            INotificationManager iNotificationManager = this.noMan;
            String str = this.packageName;
            if (str == null) {
                Intrinsics.throwNpe();
            }
            Integer num = this.appUid;
            if (num == null) {
                Intrinsics.throwNpe();
            }
            ParceledListSlice notificationChannelGroupsForPackage = iNotificationManager.getNotificationChannelGroupsForPackage(str, num.intValue(), false);
            Intrinsics.checkExpressionValueIsNotNull(notificationChannelGroupsForPackage, "noMan.getNotificationCha…eName!!, appUid!!, false)");
            List<NotificationChannelGroup> list = notificationChannelGroupsForPackage.getList();
            if (!(list instanceof List)) {
                list = null;
            }
            return list != null ? list : CollectionsKt__CollectionsKt.emptyList();
        } catch (Exception e) {
            Log.e("ChannelDialogController", "Error fetching channel groups", e);
            return CollectionsKt__CollectionsKt.emptyList();
        }
    }

    private final boolean checkAreAppNotificationsOn() {
        try {
            INotificationManager iNotificationManager = this.noMan;
            String str = this.packageName;
            if (str == null) {
                Intrinsics.throwNpe();
            }
            Integer num = this.appUid;
            if (num == null) {
                Intrinsics.throwNpe();
            }
            return iNotificationManager.areNotificationsEnabledForPackage(str, num.intValue());
        } catch (Exception e) {
            Log.e("ChannelDialogController", "Error calling NoMan", e);
            return false;
        }
    }

    private final void applyAppNotificationsOn(boolean z) {
        try {
            INotificationManager iNotificationManager = this.noMan;
            String str = this.packageName;
            if (str == null) {
                Intrinsics.throwNpe();
            }
            Integer num = this.appUid;
            if (num == null) {
                Intrinsics.throwNpe();
            }
            iNotificationManager.setNotificationsEnabledForPackage(str, num.intValue(), z);
        } catch (Exception e) {
            Log.e("ChannelDialogController", "Error calling NoMan", e);
        }
    }

    private final void setChannelImportance(NotificationChannel notificationChannel, int i) {
        try {
            notificationChannel.setImportance(i);
            INotificationManager iNotificationManager = this.noMan;
            String str = this.packageName;
            if (str == null) {
                Intrinsics.throwNpe();
            }
            Integer num = this.appUid;
            if (num == null) {
                Intrinsics.throwNpe();
            }
            iNotificationManager.updateNotificationChannelForPackage(str, num.intValue(), notificationChannel);
        } catch (Exception e) {
            Log.e("ChannelDialogController", "Unable to update notification importance", e);
        }
    }

    @VisibleForTesting
    public final void apply() {
        for (Map.Entry<NotificationChannel, Integer> entry : this.edits.entrySet()) {
            NotificationChannel key = entry.getKey();
            int iIntValue = entry.getValue().intValue();
            if (key.getImportance() != iIntValue) {
                setChannelImportance(key, iIntValue);
            }
        }
        if (!Intrinsics.areEqual(Boolean.valueOf(this.appNotificationsEnabled), this.appNotificationsCurrentlyEnabled)) {
            applyAppNotificationsOn(this.appNotificationsEnabled);
        }
    }

    @VisibleForTesting
    public final void launchSettings(@NotNull View sender) {
        Intrinsics.checkParameterIsNotNull(sender, "sender");
        NotificationInfo.OnSettingsClickListener onSettingsClickListener = this.onSettingsClickListener;
        if (onSettingsClickListener != null) {
            Integer num = this.appUid;
            if (num == null) {
                Intrinsics.throwNpe();
            }
            onSettingsClickListener.onClick(sender, null, num.intValue());
        }
    }

    private final void initDialog() {
        this.dialogBuilder.setContext(this.context);
        ChannelEditorDialog channelEditorDialogBuild = this.dialogBuilder.build();
        this.dialog = channelEditorDialogBuild;
        if (channelEditorDialogBuild == null) {
            Intrinsics.throwUninitializedPropertyAccessException("dialog");
        }
        Window window = channelEditorDialogBuild.getWindow();
        if (window != null) {
            window.requestFeature(1);
        }
        ChannelEditorDialog channelEditorDialog = this.dialog;
        if (channelEditorDialog == null) {
            Intrinsics.throwUninitializedPropertyAccessException("dialog");
        }
        channelEditorDialog.setTitle(" ");
        ChannelEditorDialog channelEditorDialog2 = this.dialog;
        if (channelEditorDialog2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("dialog");
        }
        channelEditorDialog2.setContentView(R.layout.notif_half_shelf);
        channelEditorDialog2.setCanceledOnTouchOutside(true);
        channelEditorDialog2.setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: com.android.systemui.statusbar.notification.row.ChannelEditorDialogController$initDialog$$inlined$apply$lambda$1
            @Override // android.content.DialogInterface.OnDismissListener
            public final void onDismiss(DialogInterface dialogInterface) {
                OnChannelEditorDialogFinishedListener onFinishListener = this.this$0.getOnFinishListener();
                if (onFinishListener != null) {
                    onFinishListener.onChannelEditorDialogFinished();
                }
            }
        });
        final ChannelEditorListView channelEditorListView = (ChannelEditorListView) channelEditorDialog2.findViewById(R.id.half_shelf_container);
        if (channelEditorListView != null) {
            channelEditorListView.setController(this);
            channelEditorListView.setAppIcon(this.appIcon);
            channelEditorListView.setAppName(this.appName);
            channelEditorListView.setChannels(this.paddedChannels);
        }
        channelEditorDialog2.setOnShowListener(new DialogInterface.OnShowListener() { // from class: com.android.systemui.statusbar.notification.row.ChannelEditorDialogController$initDialog$$inlined$apply$lambda$2
            @Override // android.content.DialogInterface.OnShowListener
            public final void onShow(DialogInterface dialogInterface) {
                for (NotificationChannel notificationChannel : this.providedChannels) {
                    ChannelEditorListView channelEditorListView2 = channelEditorListView;
                    if (channelEditorListView2 != null) {
                        channelEditorListView2.highlightChannel(notificationChannel);
                    }
                }
            }
        });
        TextView textView = (TextView) channelEditorDialog2.findViewById(R.id.done_button);
        if (textView != null) {
            textView.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.ChannelEditorDialogController$initDialog$$inlined$apply$lambda$3
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.this$0.apply();
                    this.this$0.done();
                }
            });
        }
        TextView textView2 = (TextView) channelEditorDialog2.findViewById(R.id.see_more_button);
        if (textView2 != null) {
            textView2.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.ChannelEditorDialogController$initDialog$$inlined$apply$lambda$4
                @Override // android.view.View.OnClickListener
                public final void onClick(View it) {
                    ChannelEditorDialogController channelEditorDialogController = this.this$0;
                    Intrinsics.checkExpressionValueIsNotNull(it, "it");
                    channelEditorDialogController.launchSettings(it);
                    this.this$0.done();
                }
            });
        }
        Window window2 = channelEditorDialog2.getWindow();
        if (window2 != null) {
            window2.setBackgroundDrawable(new ColorDrawable(0));
            window2.addFlags(this.wmFlags);
            window2.setType(2017);
            window2.setWindowAnimations(android.R.style.Animation.InputMethod);
            WindowManager.LayoutParams attributes = window2.getAttributes();
            attributes.format = -3;
            attributes.setTitle(ChannelEditorDialogController.class.getSimpleName());
            attributes.gravity = 81;
            WindowManager.LayoutParams attributes2 = window2.getAttributes();
            Intrinsics.checkExpressionValueIsNotNull(attributes2, "attributes");
            attributes.setFitInsetsTypes(attributes2.getFitInsetsTypes() & (~WindowInsets.Type.statusBars()));
            attributes.width = -1;
            attributes.height = -2;
            window2.setAttributes(attributes);
        }
    }
}
