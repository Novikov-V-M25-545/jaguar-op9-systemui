package com.android.systemui.statusbar.notification.row;

import android.animation.TimeInterpolator;
import android.app.ActivityManager;
import android.app.INotificationManager;
import android.app.KeyguardManager;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;
import android.metrics.LogMaker;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.NotificationGuts;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import java.util.List;
import java.util.Set;

/* loaded from: classes.dex */
public class NotificationInfo extends LinearLayout implements NotificationGuts.GutsContent {
    private int mActualHeight;
    private String mAppName;
    private OnAppSettingsClickListener mAppSettingsClickListener;
    private int mAppUid;
    private ChannelEditorDialogController mChannelEditorDialogController;
    private Integer mChosenImportance;
    private String mDelegatePkg;
    private NotificationGuts mGutsContainer;
    private INotificationManager mINotificationManager;
    private boolean mIsDeviceProvisioned;
    private boolean mIsNonblockable;
    private boolean mIsSingleDefaultChannel;
    private MetricsLogger mMetricsLogger;
    private int mNumUniqueChannelsInRow;
    private View.OnClickListener mOnAlert;
    private View.OnClickListener mOnDismissSettings;
    private OnSettingsClickListener mOnSettingsClickListener;
    private View.OnClickListener mOnSilent;
    private String mPackageName;
    private Drawable mPkgIcon;
    private PackageManager mPm;
    private boolean mPresentingChannelEditorDialog;
    private boolean mPressedApply;
    private TextView mPriorityDescriptionView;
    private StatusBarNotification mSbn;
    private TextView mSilentDescriptionView;
    private NotificationChannel mSingleNotificationChannel;

    @VisibleForTesting
    boolean mSkipPost;
    private int mStartingChannelImportance;
    private UiEventLogger mUiEventLogger;
    private Set<NotificationChannel> mUniqueChannelsInRow;
    private VisualStabilityManager mVisualStabilityManager;
    private boolean mWasShownHighPriority;

    public interface CheckSaveListener {
    }

    public interface OnAppSettingsClickListener {
        void onClick(View view, Intent intent);
    }

    public interface OnSettingsClickListener {
        void onClick(View view, NotificationChannel notificationChannel, int i);
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public View getContentView() {
        return this;
    }

    @VisibleForTesting
    public boolean isAnimating() {
        return false;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public boolean needsFalsingProtection() {
        return true;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public boolean willBeRemoved() {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0(View view) {
        this.mChosenImportance = 3;
        applyAlertingBehavior(0, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$1(View view) {
        this.mChosenImportance = 2;
        applyAlertingBehavior(1, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$2(View view) {
        this.mPressedApply = true;
        this.mGutsContainer.closeControls(view, true);
    }

    public NotificationInfo(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mPresentingChannelEditorDialog = false;
        this.mSkipPost = false;
        this.mOnAlert = new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationInfo$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$new$0(view);
            }
        };
        this.mOnSilent = new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationInfo$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$new$1(view);
            }
        };
        this.mOnDismissSettings = new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationInfo$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$new$2(view);
            }
        };
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mPriorityDescriptionView = (TextView) findViewById(R.id.alert_summary);
        this.mSilentDescriptionView = (TextView) findViewById(R.id.silence_summary);
    }

    public void bindNotification(PackageManager packageManager, INotificationManager iNotificationManager, VisualStabilityManager visualStabilityManager, ChannelEditorDialogController channelEditorDialogController, String str, NotificationChannel notificationChannel, Set<NotificationChannel> set, NotificationEntry notificationEntry, OnSettingsClickListener onSettingsClickListener, OnAppSettingsClickListener onAppSettingsClickListener, UiEventLogger uiEventLogger, boolean z, boolean z2, boolean z3) throws RemoteException {
        this.mINotificationManager = iNotificationManager;
        this.mMetricsLogger = (MetricsLogger) Dependency.get(MetricsLogger.class);
        this.mVisualStabilityManager = visualStabilityManager;
        this.mChannelEditorDialogController = channelEditorDialogController;
        this.mPackageName = str;
        this.mUniqueChannelsInRow = set;
        this.mNumUniqueChannelsInRow = set.size();
        this.mSbn = notificationEntry.getSbn();
        this.mPm = packageManager;
        this.mAppSettingsClickListener = onAppSettingsClickListener;
        this.mAppName = this.mPackageName;
        this.mOnSettingsClickListener = onSettingsClickListener;
        this.mSingleNotificationChannel = notificationChannel;
        this.mStartingChannelImportance = notificationChannel.getImportance();
        this.mWasShownHighPriority = z3;
        this.mIsNonblockable = z2;
        this.mAppUid = this.mSbn.getUid();
        this.mDelegatePkg = this.mSbn.getOpPkg();
        this.mIsDeviceProvisioned = z;
        this.mUiEventLogger = uiEventLogger;
        boolean z4 = false;
        int numNotificationChannelsForPackage = this.mINotificationManager.getNumNotificationChannelsForPackage(str, this.mAppUid, false);
        int i = this.mNumUniqueChannelsInRow;
        if (i == 0) {
            throw new IllegalArgumentException("bindNotification requires at least one channel");
        }
        if (i == 1 && this.mSingleNotificationChannel.getId().equals("miscellaneous") && numNotificationChannelsForPackage == 1) {
            z4 = true;
        }
        this.mIsSingleDefaultChannel = z4;
        bindHeader();
        bindChannelDetails();
        bindInlineControls();
        logUiEvent(NotificationControlsEvent.NOTIFICATION_CONTROLS_OPEN);
        this.mMetricsLogger.write(notificationControlsLogMaker());
    }

    private void bindInlineControls() {
        int i = 8;
        if (this.mIsNonblockable) {
            findViewById(R.id.non_configurable_text).setVisibility(0);
            findViewById(R.id.non_configurable_multichannel_text).setVisibility(8);
            findViewById(R.id.interruptiveness_settings).setVisibility(8);
            ((TextView) findViewById(R.id.done)).setText(R.string.inline_done_button);
            findViewById(R.id.turn_off_notifications).setVisibility(8);
        } else if (this.mNumUniqueChannelsInRow > 1) {
            findViewById(R.id.non_configurable_text).setVisibility(8);
            findViewById(R.id.interruptiveness_settings).setVisibility(8);
            findViewById(R.id.non_configurable_multichannel_text).setVisibility(0);
        } else {
            findViewById(R.id.non_configurable_text).setVisibility(8);
            findViewById(R.id.non_configurable_multichannel_text).setVisibility(8);
            findViewById(R.id.interruptiveness_settings).setVisibility(0);
        }
        View viewFindViewById = findViewById(R.id.turn_off_notifications);
        viewFindViewById.setOnClickListener(getTurnOffNotificationsClickListener());
        if (viewFindViewById.hasOnClickListeners() && !this.mIsNonblockable) {
            i = 0;
        }
        viewFindViewById.setVisibility(i);
        View viewFindViewById2 = findViewById(R.id.done);
        viewFindViewById2.setOnClickListener(this.mOnDismissSettings);
        viewFindViewById2.setAccessibilityDelegate(this.mGutsContainer.getAccessibilityDelegate());
        View viewFindViewById3 = findViewById(R.id.silence);
        View viewFindViewById4 = findViewById(R.id.alert);
        viewFindViewById3.setOnClickListener(this.mOnSilent);
        viewFindViewById4.setOnClickListener(this.mOnAlert);
        applyAlertingBehavior(!this.mWasShownHighPriority ? 1 : 0, false);
    }

    private void bindHeader() throws PackageManager.NameNotFoundException {
        this.mPkgIcon = null;
        try {
            ApplicationInfo applicationInfo = this.mPm.getApplicationInfo(this.mPackageName, 795136);
            if (applicationInfo != null) {
                this.mAppName = String.valueOf(this.mPm.getApplicationLabel(applicationInfo));
                this.mPkgIcon = this.mPm.getApplicationIcon(applicationInfo);
            }
        } catch (PackageManager.NameNotFoundException unused) {
            this.mPkgIcon = this.mPm.getDefaultActivityIcon();
        }
        ((ImageView) findViewById(R.id.pkg_icon)).setImageDrawable(this.mPkgIcon);
        ((TextView) findViewById(R.id.pkg_name)).setText(this.mAppName);
        bindDelegate();
        View viewFindViewById = findViewById(R.id.app_settings);
        final Intent appSettingsIntent = getAppSettingsIntent(this.mPm, this.mPackageName, this.mSingleNotificationChannel, this.mSbn.getId(), this.mSbn.getTag());
        if (appSettingsIntent != null && !TextUtils.isEmpty(this.mSbn.getNotification().getSettingsText())) {
            viewFindViewById.setVisibility(0);
            viewFindViewById.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationInfo$$ExternalSyntheticLambda5
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.f$0.lambda$bindHeader$3(appSettingsIntent, view);
                }
            });
        } else {
            viewFindViewById.setVisibility(8);
        }
        View viewFindViewById2 = findViewById(R.id.info);
        viewFindViewById2.setOnClickListener(getSettingsOnClickListener());
        viewFindViewById2.setVisibility(viewFindViewById2.hasOnClickListeners() ? 0 : 8);
        View viewFindViewById3 = findViewById(R.id.force_stop);
        if ((Settings.System.getIntForUser(((LinearLayout) this).mContext.getContentResolver(), "notification_guts_kill_app_button", 0, -2) != 0) && !isThisASystemPackage(this.mPackageName)) {
            viewFindViewById3.setVisibility(0);
            viewFindViewById3.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationInfo.1
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    if (((KeyguardManager) ((LinearLayout) NotificationInfo.this).mContext.getSystemService("keyguard")).inKeyguardRestrictedInputMode()) {
                        return;
                    }
                    SystemUIDialog systemUIDialog = new SystemUIDialog(((LinearLayout) NotificationInfo.this).mContext);
                    systemUIDialog.setTitle(((LinearLayout) NotificationInfo.this).mContext.getText(R.string.force_stop_dlg_title));
                    systemUIDialog.setMessage(((LinearLayout) NotificationInfo.this).mContext.getText(R.string.force_stop_dlg_text));
                    systemUIDialog.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationInfo.1.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ((ActivityManager) ((LinearLayout) NotificationInfo.this).mContext.getSystemService("activity")).forceStopPackage(NotificationInfo.this.mPackageName);
                        }
                    });
                    systemUIDialog.setNegativeButton(R.string.dlg_cancel, null);
                    systemUIDialog.show();
                }
            });
        } else {
            viewFindViewById3.setVisibility(8);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$bindHeader$3(Intent intent, View view) {
        this.mAppSettingsClickListener.onClick(view, intent);
    }

    private View.OnClickListener getSettingsOnClickListener() {
        final int i = this.mAppUid;
        if (i < 0 || this.mOnSettingsClickListener == null || !this.mIsDeviceProvisioned) {
            return null;
        }
        return new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationInfo$$ExternalSyntheticLambda4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$getSettingsOnClickListener$4(i, view);
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$getSettingsOnClickListener$4(int i, View view) {
        this.mOnSettingsClickListener.onClick(view, this.mNumUniqueChannelsInRow > 1 ? null : this.mSingleNotificationChannel, i);
    }

    private View.OnClickListener getTurnOffNotificationsClickListener() {
        return new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationInfo$$ExternalSyntheticLambda3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$getTurnOffNotificationsClickListener$6(view);
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$getTurnOffNotificationsClickListener$6(View view) {
        ChannelEditorDialogController channelEditorDialogController;
        if (this.mPresentingChannelEditorDialog || (channelEditorDialogController = this.mChannelEditorDialogController) == null) {
            return;
        }
        this.mPresentingChannelEditorDialog = true;
        channelEditorDialogController.prepareDialogForApp(this.mAppName, this.mPackageName, this.mAppUid, this.mUniqueChannelsInRow, this.mPkgIcon, this.mOnSettingsClickListener);
        this.mChannelEditorDialogController.setOnFinishListener(new OnChannelEditorDialogFinishedListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationInfo$$ExternalSyntheticLambda6
            @Override // com.android.systemui.statusbar.notification.row.OnChannelEditorDialogFinishedListener
            public final void onChannelEditorDialogFinished() {
                this.f$0.lambda$getTurnOffNotificationsClickListener$5();
            }
        });
        this.mChannelEditorDialogController.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$getTurnOffNotificationsClickListener$5() {
        this.mPresentingChannelEditorDialog = false;
        this.mGutsContainer.closeControls(this, false);
    }

    private void bindChannelDetails() throws RemoteException {
        bindName();
        bindGroup();
    }

    private void bindName() {
        TextView textView = (TextView) findViewById(R.id.channel_name);
        if (this.mIsSingleDefaultChannel || this.mNumUniqueChannelsInRow > 1) {
            textView.setVisibility(8);
        } else {
            textView.setText(this.mSingleNotificationChannel.getName());
        }
    }

    private void bindDelegate() {
        TextView textView = (TextView) findViewById(R.id.delegate_name);
        if (!TextUtils.equals(this.mPackageName, this.mDelegatePkg)) {
            textView.setVisibility(0);
        } else {
            textView.setVisibility(8);
        }
    }

    private void bindGroup() throws RemoteException {
        NotificationChannelGroup notificationChannelGroupForPackage;
        NotificationChannel notificationChannel = this.mSingleNotificationChannel;
        CharSequence name = (notificationChannel == null || notificationChannel.getGroup() == null || (notificationChannelGroupForPackage = this.mINotificationManager.getNotificationChannelGroupForPackage(this.mSingleNotificationChannel.getGroup(), this.mPackageName, this.mAppUid)) == null) ? null : notificationChannelGroupForPackage.getName();
        TextView textView = (TextView) findViewById(R.id.group_name);
        if (name != null) {
            textView.setText(name);
            textView.setVisibility(0);
        } else {
            textView.setVisibility(8);
        }
    }

    private boolean isThisASystemPackage(String str) throws PackageManager.NameNotFoundException {
        Signature[] signatureArr;
        try {
            PackageManager packageManagerForUser = StatusBar.getPackageManagerForUser(((LinearLayout) this).mContext, this.mSbn.getUser().getIdentifier());
            PackageInfo packageInfo = packageManagerForUser.getPackageInfo(str, 64);
            PackageInfo packageInfo2 = packageManagerForUser.getPackageInfo("android", 64);
            if (packageInfo == null || (signatureArr = packageInfo.signatures) == null) {
                return false;
            }
            return packageInfo2.signatures[0].equals(signatureArr[0]);
        } catch (PackageManager.NameNotFoundException unused) {
            return false;
        }
    }

    private void saveImportance() {
        if (this.mIsNonblockable) {
            return;
        }
        if (this.mChosenImportance == null) {
            this.mChosenImportance = Integer.valueOf(this.mStartingChannelImportance);
        }
        updateImportance();
    }

    private void updateImportance() {
        if (this.mChosenImportance != null) {
            logUiEvent(NotificationControlsEvent.NOTIFICATION_CONTROLS_SAVE_IMPORTANCE);
            this.mMetricsLogger.write(importanceChangeLogMaker());
            int iIntValue = this.mChosenImportance.intValue();
            if (this.mStartingChannelImportance != -1000 && ((this.mWasShownHighPriority && this.mChosenImportance.intValue() >= 3) || (!this.mWasShownHighPriority && this.mChosenImportance.intValue() < 3))) {
                iIntValue = this.mStartingChannelImportance;
            }
            new Handler((Looper) Dependency.get(Dependency.BG_LOOPER)).post(new UpdateImportanceRunnable(this.mINotificationManager, this.mPackageName, this.mAppUid, this.mNumUniqueChannelsInRow == 1 ? this.mSingleNotificationChannel : null, this.mStartingChannelImportance, iIntValue));
            this.mVisualStabilityManager.temporarilyAllowReordering();
        }
    }

    @Override // android.view.View
    public boolean post(Runnable runnable) {
        if (this.mSkipPost) {
            runnable.run();
            return true;
        }
        return super.post(runnable);
    }

    private void applyAlertingBehavior(int i, boolean z) {
        int i2;
        if (z) {
            TransitionSet transitionSet = new TransitionSet();
            transitionSet.setOrdering(0);
            TransitionSet transitionSetAddTransition = transitionSet.addTransition(new Fade(2)).addTransition(new ChangeBounds());
            Transition duration = new Fade(1).setStartDelay(150L).setDuration(200L);
            Interpolator interpolator = Interpolators.FAST_OUT_SLOW_IN;
            transitionSetAddTransition.addTransition(duration.setInterpolator(interpolator));
            transitionSet.setDuration(350L);
            transitionSet.setInterpolator((TimeInterpolator) interpolator);
            TransitionManager.beginDelayedTransition(this, transitionSet);
        }
        final View viewFindViewById = findViewById(R.id.alert);
        final View viewFindViewById2 = findViewById(R.id.silence);
        if (i == 0) {
            this.mPriorityDescriptionView.setVisibility(0);
            this.mSilentDescriptionView.setVisibility(8);
            post(new Runnable() { // from class: com.android.systemui.statusbar.notification.row.NotificationInfo$$ExternalSyntheticLambda7
                @Override // java.lang.Runnable
                public final void run() {
                    NotificationInfo.lambda$applyAlertingBehavior$7(viewFindViewById, viewFindViewById2);
                }
            });
        } else if (i == 1) {
            this.mSilentDescriptionView.setVisibility(0);
            this.mPriorityDescriptionView.setVisibility(8);
            post(new Runnable() { // from class: com.android.systemui.statusbar.notification.row.NotificationInfo$$ExternalSyntheticLambda8
                @Override // java.lang.Runnable
                public final void run() {
                    NotificationInfo.lambda$applyAlertingBehavior$8(viewFindViewById, viewFindViewById2);
                }
            });
        } else {
            throw new IllegalArgumentException("Unrecognized alerting behavior: " + i);
        }
        boolean z2 = this.mWasShownHighPriority != (i == 0);
        TextView textView = (TextView) findViewById(R.id.done);
        if (z2) {
            i2 = R.string.inline_ok_button;
        } else {
            i2 = R.string.inline_done_button;
        }
        textView.setText(i2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$applyAlertingBehavior$7(View view, View view2) {
        view.setSelected(true);
        view2.setSelected(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$applyAlertingBehavior$8(View view, View view2) {
        view.setSelected(false);
        view2.setSelected(true);
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public void onFinishedClosing() {
        Integer num = this.mChosenImportance;
        if (num != null) {
            this.mStartingChannelImportance = num.intValue();
        }
        bindInlineControls();
        logUiEvent(NotificationControlsEvent.NOTIFICATION_CONTROLS_CLOSE);
        this.mMetricsLogger.write(notificationControlsLogMaker().setType(2));
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        if (this.mGutsContainer == null || accessibilityEvent.getEventType() != 32) {
            return;
        }
        if (this.mGutsContainer.isExposed()) {
            accessibilityEvent.getText().add(((LinearLayout) this).mContext.getString(R.string.notification_channel_controls_opened_accessibility, this.mAppName));
        } else {
            accessibilityEvent.getText().add(((LinearLayout) this).mContext.getString(R.string.notification_channel_controls_closed_accessibility, this.mAppName));
        }
    }

    private Intent getAppSettingsIntent(PackageManager packageManager, String str, NotificationChannel notificationChannel, int i, String str2) {
        Intent intent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.NOTIFICATION_PREFERENCES").setPackage(str);
        List<ResolveInfo> listQueryIntentActivities = packageManager.queryIntentActivities(intent, 65536);
        if (listQueryIntentActivities == null || listQueryIntentActivities.size() == 0 || listQueryIntentActivities.get(0) == null) {
            return null;
        }
        ActivityInfo activityInfo = listQueryIntentActivities.get(0).activityInfo;
        intent.setClassName(activityInfo.packageName, activityInfo.name);
        if (notificationChannel != null) {
            intent.putExtra("android.intent.extra.CHANNEL_ID", notificationChannel.getId());
        }
        intent.putExtra("android.intent.extra.NOTIFICATION_ID", i);
        intent.putExtra("android.intent.extra.NOTIFICATION_TAG", str2);
        return intent;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public void setGutsParent(NotificationGuts notificationGuts) {
        this.mGutsContainer = notificationGuts;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public boolean shouldBeSaved() {
        return this.mPressedApply;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public boolean handleCloseControls(boolean z, boolean z2) {
        ChannelEditorDialogController channelEditorDialogController;
        if (this.mPresentingChannelEditorDialog && (channelEditorDialogController = this.mChannelEditorDialogController) != null) {
            this.mPresentingChannelEditorDialog = false;
            channelEditorDialogController.setOnFinishListener(null);
            this.mChannelEditorDialogController.close();
        }
        if (z) {
            saveImportance();
        }
        return false;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public int getActualHeight() {
        return this.mActualHeight;
    }

    @Override // android.widget.LinearLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        this.mActualHeight = getHeight();
    }

    private static class UpdateImportanceRunnable implements Runnable {
        private final int mAppUid;
        private final NotificationChannel mChannelToUpdate;
        private final int mCurrentImportance;
        private final INotificationManager mINotificationManager;
        private final int mNewImportance;
        private final String mPackageName;

        public UpdateImportanceRunnable(INotificationManager iNotificationManager, String str, int i, NotificationChannel notificationChannel, int i2, int i3) {
            this.mINotificationManager = iNotificationManager;
            this.mPackageName = str;
            this.mAppUid = i;
            this.mChannelToUpdate = notificationChannel;
            this.mCurrentImportance = i2;
            this.mNewImportance = i3;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                NotificationChannel notificationChannel = this.mChannelToUpdate;
                if (notificationChannel != null) {
                    notificationChannel.setImportance(this.mNewImportance);
                    this.mChannelToUpdate.lockFields(4);
                    this.mINotificationManager.updateNotificationChannelForPackage(this.mPackageName, this.mAppUid, this.mChannelToUpdate);
                } else {
                    this.mINotificationManager.setNotificationsEnabledWithImportanceLockForPackage(this.mPackageName, this.mAppUid, this.mNewImportance >= this.mCurrentImportance);
                }
            } catch (RemoteException e) {
                Log.e("InfoGuts", "Unable to update notification importance", e);
            }
        }
    }

    private void logUiEvent(NotificationControlsEvent notificationControlsEvent) {
        StatusBarNotification statusBarNotification = this.mSbn;
        if (statusBarNotification != null) {
            this.mUiEventLogger.logWithInstanceId(notificationControlsEvent, statusBarNotification.getUid(), this.mSbn.getPackageName(), this.mSbn.getInstanceId());
        }
    }

    private LogMaker getLogMaker() {
        StatusBarNotification statusBarNotification = this.mSbn;
        return statusBarNotification == null ? new LogMaker(1621) : statusBarNotification.getLogMaker().setCategory(1621);
    }

    private LogMaker importanceChangeLogMaker() {
        Integer num = this.mChosenImportance;
        return getLogMaker().setCategory(291).setType(4).setSubtype(Integer.valueOf(num != null ? num.intValue() : this.mStartingChannelImportance).intValue() - this.mStartingChannelImportance);
    }

    private LogMaker notificationControlsLogMaker() {
        return getLogMaker().setCategory(204).setType(1).setSubtype(0);
    }
}
