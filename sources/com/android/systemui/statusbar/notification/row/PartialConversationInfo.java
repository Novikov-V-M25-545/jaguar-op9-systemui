package com.android.systemui.statusbar.notification.row;

import android.app.INotificationManager;
import android.app.NotificationChannel;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.R;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.NotificationGuts;
import com.android.systemui.statusbar.notification.row.NotificationInfo;
import java.util.Set;

/* loaded from: classes.dex */
public class PartialConversationInfo extends LinearLayout implements NotificationGuts.GutsContent {
    private String mAppName;
    private int mAppUid;
    private ChannelEditorDialogController mChannelEditorDialogController;
    private String mDelegatePkg;
    private NotificationGuts mGutsContainer;
    private INotificationManager mINotificationManager;
    private boolean mIsDeviceProvisioned;
    private boolean mIsNonBlockable;
    private NotificationChannel mNotificationChannel;
    private View.OnClickListener mOnDone;
    private NotificationInfo.OnSettingsClickListener mOnSettingsClickListener;
    private String mPackageName;
    private Drawable mPkgIcon;
    private PackageManager mPm;
    private boolean mPresentingChannelEditorDialog;
    private boolean mPressedApply;
    private StatusBarNotification mSbn;
    private int mSelectedAction;

    @VisibleForTesting
    boolean mSkipPost;
    private Set<NotificationChannel> mUniqueChannelsInRow;

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public View getContentView() {
        return this;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public boolean handleCloseControls(boolean z, boolean z2) {
        return false;
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
    public void onFinishedClosing() {
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public boolean willBeRemoved() {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0(View view) {
        this.mPressedApply = true;
        this.mGutsContainer.closeControls(view, true);
    }

    public PartialConversationInfo(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mSelectedAction = -1;
        this.mPresentingChannelEditorDialog = false;
        this.mSkipPost = false;
        this.mOnDone = new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.PartialConversationInfo$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$new$0(view);
            }
        };
    }

    public void bindNotification(PackageManager packageManager, INotificationManager iNotificationManager, ChannelEditorDialogController channelEditorDialogController, String str, NotificationChannel notificationChannel, Set<NotificationChannel> set, NotificationEntry notificationEntry, NotificationInfo.OnSettingsClickListener onSettingsClickListener, boolean z, boolean z2) {
        this.mSelectedAction = -1;
        this.mINotificationManager = iNotificationManager;
        this.mPackageName = str;
        StatusBarNotification sbn = notificationEntry.getSbn();
        this.mSbn = sbn;
        this.mPm = packageManager;
        this.mAppName = this.mPackageName;
        this.mOnSettingsClickListener = onSettingsClickListener;
        this.mNotificationChannel = notificationChannel;
        this.mAppUid = sbn.getUid();
        this.mDelegatePkg = this.mSbn.getOpPkg();
        this.mIsDeviceProvisioned = z;
        this.mIsNonBlockable = z2;
        this.mChannelEditorDialogController = channelEditorDialogController;
        this.mUniqueChannelsInRow = set;
        bindHeader();
        bindActions();
        View viewFindViewById = findViewById(R.id.turn_off_notifications);
        viewFindViewById.setOnClickListener(getTurnOffNotificationsClickListener());
        viewFindViewById.setVisibility((!viewFindViewById.hasOnClickListeners() || this.mIsNonBlockable) ? 8 : 0);
        View viewFindViewById2 = findViewById(R.id.done);
        viewFindViewById2.setOnClickListener(this.mOnDone);
        viewFindViewById2.setAccessibilityDelegate(this.mGutsContainer.getAccessibilityDelegate());
    }

    private void bindActions() {
        View.OnClickListener settingsOnClickListener = getSettingsOnClickListener();
        View viewFindViewById = findViewById(R.id.info);
        viewFindViewById.setOnClickListener(settingsOnClickListener);
        viewFindViewById.setVisibility(viewFindViewById.hasOnClickListeners() ? 0 : 8);
        findViewById(R.id.settings_link).setOnClickListener(settingsOnClickListener);
        ((TextView) findViewById(R.id.non_configurable_text)).setText(getResources().getString(R.string.no_shortcut, this.mAppName));
    }

    private void bindHeader() throws PackageManager.NameNotFoundException {
        bindPackage();
        bindDelegate();
    }

    private View.OnClickListener getSettingsOnClickListener() {
        final int i = this.mAppUid;
        if (i < 0 || this.mOnSettingsClickListener == null || !this.mIsDeviceProvisioned) {
            return null;
        }
        return new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.PartialConversationInfo$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$getSettingsOnClickListener$1(i, view);
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$getSettingsOnClickListener$1(int i, View view) {
        this.mOnSettingsClickListener.onClick(view, this.mNotificationChannel, i);
    }

    private View.OnClickListener getTurnOffNotificationsClickListener() {
        return new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.PartialConversationInfo$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$getTurnOffNotificationsClickListener$3(view);
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$getTurnOffNotificationsClickListener$3(View view) {
        ChannelEditorDialogController channelEditorDialogController;
        if (this.mPresentingChannelEditorDialog || (channelEditorDialogController = this.mChannelEditorDialogController) == null) {
            return;
        }
        this.mPresentingChannelEditorDialog = true;
        channelEditorDialogController.prepareDialogForApp(this.mAppName, this.mPackageName, this.mAppUid, this.mUniqueChannelsInRow, this.mPkgIcon, this.mOnSettingsClickListener);
        this.mChannelEditorDialogController.setOnFinishListener(new OnChannelEditorDialogFinishedListener() { // from class: com.android.systemui.statusbar.notification.row.PartialConversationInfo$$ExternalSyntheticLambda3
            @Override // com.android.systemui.statusbar.notification.row.OnChannelEditorDialogFinishedListener
            public final void onChannelEditorDialogFinished() {
                this.f$0.lambda$getTurnOffNotificationsClickListener$2();
            }
        });
        this.mChannelEditorDialogController.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$getTurnOffNotificationsClickListener$2() {
        this.mPresentingChannelEditorDialog = false;
        this.mGutsContainer.closeControls(this, false);
    }

    private void bindPackage() throws PackageManager.NameNotFoundException {
        try {
            ApplicationInfo applicationInfo = this.mPm.getApplicationInfo(this.mPackageName, 795136);
            if (applicationInfo != null) {
                this.mAppName = String.valueOf(this.mPm.getApplicationLabel(applicationInfo));
                this.mPkgIcon = this.mPm.getApplicationIcon(applicationInfo);
            }
        } catch (PackageManager.NameNotFoundException unused) {
            this.mPkgIcon = this.mPm.getDefaultActivityIcon();
        }
        ((TextView) findViewById(R.id.name)).setText(this.mAppName);
        ((ImageView) findViewById(R.id.icon)).setImageDrawable(this.mPkgIcon);
    }

    private void bindDelegate() {
        TextView textView = (TextView) findViewById(R.id.delegate_name);
        if (!TextUtils.equals(this.mPackageName, this.mDelegatePkg)) {
            textView.setVisibility(0);
        } else {
            textView.setVisibility(8);
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

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
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

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public void setGutsParent(NotificationGuts notificationGuts) {
        this.mGutsContainer = notificationGuts;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public boolean shouldBeSaved() {
        return this.mPressedApply;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public int getActualHeight() {
        return getHeight();
    }
}
