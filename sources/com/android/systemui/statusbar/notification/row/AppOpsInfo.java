package com.android.systemui.statusbar.notification.row;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.service.notification.StatusBarNotification;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.R;
import com.android.systemui.statusbar.notification.row.NotificationGuts;

/* loaded from: classes.dex */
public class AppOpsInfo extends LinearLayout implements NotificationGuts.GutsContent {
    private String mAppName;
    private ArraySet<Integer> mAppOps;
    private int mAppUid;
    private NotificationGuts mGutsContainer;
    private MetricsLogger mMetricsLogger;
    private View.OnClickListener mOnOk;
    private OnSettingsClickListener mOnSettingsClickListener;
    private String mPkg;
    private PackageManager mPm;
    private StatusBarNotification mSbn;
    private UiEventLogger mUiEventLogger;

    public interface OnSettingsClickListener {
        void onClick(View view, String str, int i, ArraySet<Integer> arraySet);
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public View getContentView() {
        return this;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public boolean needsFalsingProtection() {
        return false;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public boolean shouldBeSaved() {
        return false;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public boolean willBeRemoved() {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0(View view) {
        this.mGutsContainer.closeControls(view, false);
    }

    public AppOpsInfo(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mOnOk = new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.AppOpsInfo$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$new$0(view);
            }
        };
    }

    public void bindGuts(PackageManager packageManager, OnSettingsClickListener onSettingsClickListener, StatusBarNotification statusBarNotification, UiEventLogger uiEventLogger, ArraySet<Integer> arraySet) {
        String packageName = statusBarNotification.getPackageName();
        this.mPkg = packageName;
        this.mSbn = statusBarNotification;
        this.mPm = packageManager;
        this.mAppName = packageName;
        this.mOnSettingsClickListener = onSettingsClickListener;
        this.mAppOps = arraySet;
        this.mUiEventLogger = uiEventLogger;
        bindHeader();
        bindPrompt();
        bindButtons();
        logUiEvent(NotificationAppOpsEvent.NOTIFICATION_APP_OPS_OPEN);
        MetricsLogger metricsLogger = new MetricsLogger();
        this.mMetricsLogger = metricsLogger;
        metricsLogger.visibility(1345, true);
    }

    private void bindHeader() throws PackageManager.NameNotFoundException {
        Drawable defaultActivityIcon;
        try {
            ApplicationInfo applicationInfo = this.mPm.getApplicationInfo(this.mPkg, 795136);
            if (applicationInfo != null) {
                this.mAppUid = this.mSbn.getUid();
                this.mAppName = String.valueOf(this.mPm.getApplicationLabel(applicationInfo));
                defaultActivityIcon = this.mPm.getApplicationIcon(applicationInfo);
            } else {
                defaultActivityIcon = null;
            }
        } catch (PackageManager.NameNotFoundException unused) {
            defaultActivityIcon = this.mPm.getDefaultActivityIcon();
        }
        ((ImageView) findViewById(R.id.pkgicon)).setImageDrawable(defaultActivityIcon);
        ((TextView) findViewById(R.id.pkgname)).setText(this.mAppName);
    }

    private void bindPrompt() {
        ((TextView) findViewById(R.id.prompt)).setText(getPrompt());
    }

    private void bindButtons() {
        findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.AppOpsInfo$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$bindButtons$1(view);
            }
        });
        TextView textView = (TextView) findViewById(R.id.ok);
        textView.setOnClickListener(this.mOnOk);
        textView.setAccessibilityDelegate(this.mGutsContainer.getAccessibilityDelegate());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$bindButtons$1(View view) {
        this.mOnSettingsClickListener.onClick(view, this.mPkg, this.mAppUid, this.mAppOps);
    }

    private String getPrompt() {
        ArraySet<Integer> arraySet = this.mAppOps;
        if (arraySet == null || arraySet.size() == 0) {
            return "";
        }
        if (this.mAppOps.size() == 1) {
            if (this.mAppOps.contains(26)) {
                return ((LinearLayout) this).mContext.getString(R.string.appops_camera);
            }
            if (this.mAppOps.contains(27)) {
                return ((LinearLayout) this).mContext.getString(R.string.appops_microphone);
            }
            return ((LinearLayout) this).mContext.getString(R.string.appops_overlay);
        }
        if (this.mAppOps.size() == 2) {
            if (this.mAppOps.contains(26)) {
                if (this.mAppOps.contains(27)) {
                    return ((LinearLayout) this).mContext.getString(R.string.appops_camera_mic);
                }
                return ((LinearLayout) this).mContext.getString(R.string.appops_camera_overlay);
            }
            return ((LinearLayout) this).mContext.getString(R.string.appops_mic_overlay);
        }
        return ((LinearLayout) this).mContext.getString(R.string.appops_camera_mic_overlay);
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
    public boolean handleCloseControls(boolean z, boolean z2) {
        logUiEvent(NotificationAppOpsEvent.NOTIFICATION_APP_OPS_CLOSE);
        MetricsLogger metricsLogger = this.mMetricsLogger;
        if (metricsLogger != null) {
            metricsLogger.visibility(1345, false);
        }
        return false;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public int getActualHeight() {
        return getHeight();
    }

    private void logUiEvent(NotificationAppOpsEvent notificationAppOpsEvent) {
        StatusBarNotification statusBarNotification = this.mSbn;
        if (statusBarNotification != null) {
            this.mUiEventLogger.logWithInstanceId(notificationAppOpsEvent, statusBarNotification.getUid(), this.mSbn.getPackageName(), this.mSbn.getInstanceId());
        }
    }
}
