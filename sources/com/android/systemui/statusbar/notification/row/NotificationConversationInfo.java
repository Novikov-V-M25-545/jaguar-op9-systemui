package com.android.systemui.statusbar.notification.row;

import android.animation.TimeInterpolator;
import android.app.ActivityManager;
import android.app.INotificationManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.pm.Signature;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.notification.ConversationIconFactory;
import com.android.systemui.Interpolators;
import com.android.systemui.Prefs;
import com.android.systemui.R;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.statusbar.notification.NotificationChannelHelper;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.NotificationGuts;
import com.android.systemui.statusbar.notification.row.PriorityOnboardingDialogController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import java.util.Objects;
import javax.inject.Provider;

/* loaded from: classes.dex */
public class NotificationConversationInfo extends LinearLayout implements NotificationGuts.GutsContent {
    private int mActualHeight;
    private int mAppBubble;
    private String mAppName;
    private int mAppUid;
    private Handler mBgHandler;
    private BubbleController mBubbleController;
    private Notification.BubbleMetadata mBubbleMetadata;
    private Provider<PriorityOnboardingDialogController.Builder> mBuilderProvider;
    private TextView mDefaultDescriptionView;
    private String mDelegatePkg;
    private NotificationEntry mEntry;
    private NotificationGuts mGutsContainer;
    private INotificationManager mINotificationManager;
    private ConversationIconFactory mIconFactory;
    private boolean mIsDeviceProvisioned;
    private Handler mMainHandler;
    private NotificationChannel mNotificationChannel;
    private OnConversationSettingsClickListener mOnConversationSettingsClickListener;
    private View.OnClickListener mOnDefaultClick;
    private View.OnClickListener mOnDone;
    private View.OnClickListener mOnFavoriteClick;
    private View.OnClickListener mOnMuteClick;
    private OnSettingsClickListener mOnSettingsClickListener;
    private OnSnoozeClickListener mOnSnoozeClickListener;
    private String mPackageName;
    private PackageManager mPm;
    private boolean mPressedApply;
    private TextView mPriorityDescriptionView;
    private StatusBarNotification mSbn;
    private int mSelectedAction;
    private ShortcutInfo mShortcutInfo;
    private ShortcutManager mShortcutManager;
    private TextView mSilentDescriptionView;

    @VisibleForTesting
    boolean mSkipPost;
    private Context mUserContext;
    private VisualStabilityManager mVisualStabilityManager;

    public interface OnConversationSettingsClickListener {
        void onClick();
    }

    public interface OnSettingsClickListener {
        void onClick(View view, NotificationChannel notificationChannel, int i);
    }

    public interface OnSnoozeClickListener {
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
    public void onFinishedClosing() {
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public boolean willBeRemoved() {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0(View view) {
        setSelectedAction(2);
        updateToggleActions(this.mSelectedAction, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$1(View view) {
        setSelectedAction(0);
        updateToggleActions(this.mSelectedAction, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$2(View view) {
        setSelectedAction(4);
        updateToggleActions(this.mSelectedAction, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$3(View view) {
        this.mPressedApply = true;
        if (this.mSelectedAction == 2 && shouldShowPriorityOnboarding()) {
            showPriorityOnboarding();
        }
        this.mGutsContainer.closeControls(view, true);
    }

    public NotificationConversationInfo(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mSelectedAction = -1;
        this.mSkipPost = false;
        this.mOnFavoriteClick = new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationConversationInfo$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$new$0(view);
            }
        };
        this.mOnDefaultClick = new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationConversationInfo$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$new$1(view);
            }
        };
        this.mOnMuteClick = new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationConversationInfo$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$new$2(view);
            }
        };
        this.mOnDone = new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationConversationInfo$$ExternalSyntheticLambda3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$new$3(view);
            }
        };
    }

    @VisibleForTesting
    void setSelectedAction(int i) {
        if (this.mSelectedAction == i) {
            return;
        }
        this.mSelectedAction = i;
    }

    public void bindNotification(ShortcutManager shortcutManager, PackageManager packageManager, INotificationManager iNotificationManager, VisualStabilityManager visualStabilityManager, String str, NotificationChannel notificationChannel, NotificationEntry notificationEntry, Notification.BubbleMetadata bubbleMetadata, OnSettingsClickListener onSettingsClickListener, OnSnoozeClickListener onSnoozeClickListener, ConversationIconFactory conversationIconFactory, Context context, Provider<PriorityOnboardingDialogController.Builder> provider, boolean z, Handler handler, Handler handler2, OnConversationSettingsClickListener onConversationSettingsClickListener, BubbleController bubbleController) {
        this.mSelectedAction = -1;
        this.mINotificationManager = iNotificationManager;
        this.mVisualStabilityManager = visualStabilityManager;
        this.mPackageName = str;
        this.mEntry = notificationEntry;
        StatusBarNotification sbn = notificationEntry.getSbn();
        this.mSbn = sbn;
        this.mPm = packageManager;
        this.mAppName = this.mPackageName;
        this.mOnSettingsClickListener = onSettingsClickListener;
        this.mNotificationChannel = notificationChannel;
        this.mAppUid = sbn.getUid();
        this.mDelegatePkg = this.mSbn.getOpPkg();
        this.mIsDeviceProvisioned = z;
        this.mOnSnoozeClickListener = onSnoozeClickListener;
        this.mOnConversationSettingsClickListener = onConversationSettingsClickListener;
        this.mIconFactory = conversationIconFactory;
        this.mUserContext = context;
        this.mBubbleMetadata = bubbleMetadata;
        this.mBubbleController = bubbleController;
        this.mBuilderProvider = provider;
        this.mMainHandler = handler;
        this.mBgHandler = handler2;
        this.mShortcutManager = shortcutManager;
        ShortcutInfo shortcutInfo = notificationEntry.getRanking().getShortcutInfo();
        this.mShortcutInfo = shortcutInfo;
        if (shortcutInfo == null) {
            throw new IllegalArgumentException("Does not have required information");
        }
        this.mNotificationChannel = NotificationChannelHelper.createConversationChannelIfNeeded(getContext(), this.mINotificationManager, notificationEntry, this.mNotificationChannel);
        try {
            this.mAppBubble = this.mINotificationManager.getBubblePreferenceForPackage(this.mPackageName, this.mAppUid);
        } catch (RemoteException e) {
            Log.e("ConversationGuts", "can't reach OS", e);
            this.mAppBubble = 2;
        }
        bindHeader();
        bindActions();
        View viewFindViewById = findViewById(R.id.done);
        viewFindViewById.setOnClickListener(this.mOnDone);
        viewFindViewById.setAccessibilityDelegate(this.mGutsContainer.getAccessibilityDelegate());
    }

    private boolean isSystemPackage(String str) throws PackageManager.NameNotFoundException {
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

    private void bindActions() {
        if (this.mAppBubble == 1) {
            ((TextView) findViewById(R.id.default_summary)).setText(getResources().getString(R.string.notification_channel_summary_default_with_bubbles, this.mAppName));
        }
        findViewById(R.id.priority).setOnClickListener(this.mOnFavoriteClick);
        findViewById(R.id.default_behavior).setOnClickListener(this.mOnDefaultClick);
        findViewById(R.id.silence).setOnClickListener(this.mOnMuteClick);
        View viewFindViewById = findViewById(R.id.info);
        viewFindViewById.setOnClickListener(getSettingsOnClickListener());
        viewFindViewById.setVisibility(viewFindViewById.hasOnClickListeners() ? 0 : 8);
        View viewFindViewById2 = findViewById(R.id.force_stop);
        if ((Settings.System.getIntForUser(((LinearLayout) this).mContext.getContentResolver(), "notification_guts_kill_app_button", 0, -2) != 0) && !isSystemPackage(this.mPackageName)) {
            viewFindViewById2.setVisibility(0);
            viewFindViewById2.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationConversationInfo.1
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    if (((KeyguardManager) ((LinearLayout) NotificationConversationInfo.this).mContext.getSystemService("keyguard")).inKeyguardRestrictedInputMode()) {
                        return;
                    }
                    SystemUIDialog systemUIDialog = new SystemUIDialog(((LinearLayout) NotificationConversationInfo.this).mContext);
                    systemUIDialog.setTitle(((LinearLayout) NotificationConversationInfo.this).mContext.getText(R.string.force_stop_dlg_title));
                    systemUIDialog.setMessage(((LinearLayout) NotificationConversationInfo.this).mContext.getText(R.string.force_stop_dlg_text));
                    systemUIDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationConversationInfo.1.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ((ActivityManager) ((LinearLayout) NotificationConversationInfo.this).mContext.getSystemService("activity")).forceStopPackage(NotificationConversationInfo.this.mPackageName);
                        }
                    });
                    systemUIDialog.setNegativeButton(android.R.string.cancel, null);
                    systemUIDialog.show();
                }
            });
        } else {
            viewFindViewById2.setVisibility(8);
        }
        updateToggleActions(getSelectedAction(), false);
    }

    private void bindHeader() throws PackageManager.NameNotFoundException {
        bindConversationDetails();
        bindDelegate();
    }

    private View.OnClickListener getSettingsOnClickListener() {
        final int i = this.mAppUid;
        if (i < 0 || this.mOnSettingsClickListener == null || !this.mIsDeviceProvisioned) {
            return null;
        }
        return new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationConversationInfo$$ExternalSyntheticLambda4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$getSettingsOnClickListener$4(i, view);
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$getSettingsOnClickListener$4(int i, View view) {
        this.mOnSettingsClickListener.onClick(view, this.mNotificationChannel, i);
    }

    private void bindConversationDetails() throws PackageManager.NameNotFoundException {
        ((TextView) findViewById(R.id.parent_channel_name)).setText(this.mNotificationChannel.getName());
        bindGroup();
        bindPackage();
        bindIcon(this.mNotificationChannel.isImportantConversation());
    }

    private void bindIcon(boolean z) {
        ((ImageView) findViewById(R.id.conversation_icon)).setImageDrawable(this.mIconFactory.getConversationDrawable(this.mShortcutInfo, this.mPackageName, this.mAppUid, z));
    }

    private void bindPackage() throws PackageManager.NameNotFoundException {
        try {
            ApplicationInfo applicationInfo = this.mPm.getApplicationInfo(this.mPackageName, 795136);
            if (applicationInfo != null) {
                this.mAppName = String.valueOf(this.mPm.getApplicationLabel(applicationInfo));
            }
        } catch (PackageManager.NameNotFoundException unused) {
        }
        ((TextView) findViewById(R.id.pkg_name)).setText(this.mAppName);
    }

    private void bindDelegate() {
        TextView textView = (TextView) findViewById(R.id.delegate_name);
        if (!TextUtils.equals(this.mPackageName, this.mDelegatePkg)) {
            textView.setVisibility(0);
        } else {
            textView.setVisibility(8);
        }
    }

    private void bindGroup() {
        NotificationChannel notificationChannel = this.mNotificationChannel;
        CharSequence name = null;
        if (notificationChannel != null && notificationChannel.getGroup() != null) {
            try {
                NotificationChannelGroup notificationChannelGroupForPackage = this.mINotificationManager.getNotificationChannelGroupForPackage(this.mNotificationChannel.getGroup(), this.mPackageName, this.mAppUid);
                if (notificationChannelGroupForPackage != null) {
                    name = notificationChannelGroupForPackage.getName();
                }
            } catch (RemoteException unused) {
            }
        }
        TextView textView = (TextView) findViewById(R.id.group_name);
        if (name != null) {
            textView.setText(name);
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
        this.mPriorityDescriptionView = (TextView) findViewById(R.id.priority_summary);
        this.mDefaultDescriptionView = (TextView) findViewById(R.id.default_summary);
        this.mSilentDescriptionView = (TextView) findViewById(R.id.silence_summary);
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

    private void updateToggleActions(int i, boolean z) {
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
        final View viewFindViewById = findViewById(R.id.priority);
        final View viewFindViewById2 = findViewById(R.id.default_behavior);
        final View viewFindViewById3 = findViewById(R.id.silence);
        if (i == 0) {
            this.mDefaultDescriptionView.setVisibility(0);
            this.mSilentDescriptionView.setVisibility(8);
            this.mPriorityDescriptionView.setVisibility(8);
            post(new Runnable() { // from class: com.android.systemui.statusbar.notification.row.NotificationConversationInfo$$ExternalSyntheticLambda7
                @Override // java.lang.Runnable
                public final void run() {
                    NotificationConversationInfo.lambda$updateToggleActions$7(viewFindViewById, viewFindViewById2, viewFindViewById3);
                }
            });
        } else if (i == 2) {
            this.mPriorityDescriptionView.setVisibility(0);
            this.mDefaultDescriptionView.setVisibility(8);
            this.mSilentDescriptionView.setVisibility(8);
            post(new Runnable() { // from class: com.android.systemui.statusbar.notification.row.NotificationConversationInfo$$ExternalSyntheticLambda5
                @Override // java.lang.Runnable
                public final void run() {
                    NotificationConversationInfo.lambda$updateToggleActions$5(viewFindViewById, viewFindViewById2, viewFindViewById3);
                }
            });
        } else if (i == 4) {
            this.mSilentDescriptionView.setVisibility(0);
            this.mDefaultDescriptionView.setVisibility(8);
            this.mPriorityDescriptionView.setVisibility(8);
            post(new Runnable() { // from class: com.android.systemui.statusbar.notification.row.NotificationConversationInfo$$ExternalSyntheticLambda6
                @Override // java.lang.Runnable
                public final void run() {
                    NotificationConversationInfo.lambda$updateToggleActions$6(viewFindViewById, viewFindViewById2, viewFindViewById3);
                }
            });
        } else {
            throw new IllegalArgumentException("Unrecognized behavior: " + this.mSelectedAction);
        }
        boolean z2 = getSelectedAction() != i;
        TextView textView = (TextView) findViewById(R.id.done);
        if (z2) {
            i2 = R.string.inline_ok_button;
        } else {
            i2 = R.string.inline_done_button;
        }
        textView.setText(i2);
        bindIcon(i == 2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$updateToggleActions$5(View view, View view2, View view3) {
        view.setSelected(true);
        view2.setSelected(false);
        view3.setSelected(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$updateToggleActions$6(View view, View view2, View view3) {
        view.setSelected(false);
        view2.setSelected(false);
        view3.setSelected(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$updateToggleActions$7(View view, View view2, View view3) {
        view.setSelected(false);
        view2.setSelected(true);
        view3.setSelected(false);
    }

    int getSelectedAction() {
        if (this.mNotificationChannel.getImportance() > 2 || this.mNotificationChannel.getImportance() <= -1000) {
            return this.mNotificationChannel.isImportantConversation() ? 2 : 0;
        }
        return 4;
    }

    private void updateChannel() {
        this.mBgHandler.post(new UpdateChannelRunnable(this.mINotificationManager, this.mPackageName, this.mAppUid, this.mSelectedAction, this.mNotificationChannel));
        this.mEntry.markForUserTriggeredMovement(true);
        Handler handler = this.mMainHandler;
        final VisualStabilityManager visualStabilityManager = this.mVisualStabilityManager;
        Objects.requireNonNull(visualStabilityManager);
        handler.postDelayed(new Runnable() { // from class: com.android.systemui.statusbar.notification.row.NotificationConversationInfo$$ExternalSyntheticLambda8
            @Override // java.lang.Runnable
            public final void run() {
                visualStabilityManager.temporarilyAllowReordering();
            }
        }, 360L);
    }

    private boolean shouldShowPriorityOnboarding() {
        return !Prefs.getBoolean(this.mUserContext, "HasUserSeenPriorityOnboarding", false);
    }

    private void showPriorityOnboarding() {
        View viewInflate = LayoutInflater.from(((LinearLayout) this).mContext).inflate(R.layout.priority_onboarding_half_shell, (ViewGroup) null);
        try {
        } catch (RemoteException e) {
            Log.e("ConversationGuts", "Could not check conversation senders", e);
        }
        boolean z = this.mINotificationManager.getConsolidatedNotificationPolicy().priorityConversationSenders == 2;
        Notification.BubbleMetadata bubbleMetadata = this.mBubbleMetadata;
        boolean z2 = bubbleMetadata != null && bubbleMetadata.getAutoExpandBubble() && Settings.Global.getInt(((LinearLayout) this).mContext.getContentResolver(), "notification_bubbles", 0) == 1;
        Drawable baseIconDrawable = this.mIconFactory.getBaseIconDrawable(this.mShortcutInfo);
        if (baseIconDrawable == null) {
            baseIconDrawable = ((LinearLayout) this).mContext.getDrawable(R.drawable.ic_person).mutate();
            TypedArray typedArrayObtainStyledAttributes = ((LinearLayout) this).mContext.obtainStyledAttributes(new int[]{android.R.attr.colorAccent});
            int color = typedArrayObtainStyledAttributes.getColor(0, 0);
            typedArrayObtainStyledAttributes.recycle();
            baseIconDrawable.setTint(color);
        }
        PriorityOnboardingDialogController priorityOnboardingDialogControllerBuild = this.mBuilderProvider.get().setContext(this.mUserContext).setView(viewInflate).setIgnoresDnd(z).setShowsAsBubble(z2).setIcon(baseIconDrawable).setBadge(this.mIconFactory.getAppBadge(this.mPackageName, UserHandle.getUserId(this.mSbn.getUid()))).setOnSettingsClick(this.mOnConversationSettingsClickListener).build();
        priorityOnboardingDialogControllerBuild.init();
        priorityOnboardingDialogControllerBuild.show();
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
        if (!z || this.mSelectedAction <= -1) {
            return false;
        }
        updateChannel();
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

    class UpdateChannelRunnable implements Runnable {
        private final int mAction;
        private final String mAppPkg;
        private final int mAppUid;
        private NotificationChannel mChannelToUpdate;
        private final INotificationManager mINotificationManager;

        public UpdateChannelRunnable(INotificationManager iNotificationManager, String str, int i, int i2, NotificationChannel notificationChannel) {
            this.mINotificationManager = iNotificationManager;
            this.mAppPkg = str;
            this.mAppUid = i;
            this.mChannelToUpdate = notificationChannel;
            this.mAction = i2;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                int i = this.mAction;
                if (i == 0) {
                    NotificationChannel notificationChannel = this.mChannelToUpdate;
                    notificationChannel.setImportance(Math.max(notificationChannel.getOriginalImportance(), 3));
                    if (this.mChannelToUpdate.isImportantConversation()) {
                        this.mChannelToUpdate.setImportantConversation(false);
                        this.mChannelToUpdate.setAllowBubbles(false);
                    }
                } else if (i == 2) {
                    this.mChannelToUpdate.setImportantConversation(true);
                    if (this.mChannelToUpdate.isImportantConversation()) {
                        this.mChannelToUpdate.setAllowBubbles(true);
                        if (NotificationConversationInfo.this.mAppBubble == 0) {
                            this.mINotificationManager.setBubblesAllowed(this.mAppPkg, this.mAppUid, 2);
                        }
                        NotificationConversationInfo.this.post(new Runnable() { // from class: com.android.systemui.statusbar.notification.row.NotificationConversationInfo$UpdateChannelRunnable$$ExternalSyntheticLambda0
                            @Override // java.lang.Runnable
                            public final void run() {
                                this.f$0.lambda$run$0();
                            }
                        });
                    }
                    NotificationChannel notificationChannel2 = this.mChannelToUpdate;
                    notificationChannel2.setImportance(Math.max(notificationChannel2.getOriginalImportance(), 3));
                } else if (i == 4) {
                    if (this.mChannelToUpdate.getImportance() == -1000 || this.mChannelToUpdate.getImportance() >= 3) {
                        this.mChannelToUpdate.setImportance(2);
                    }
                    if (this.mChannelToUpdate.isImportantConversation()) {
                        this.mChannelToUpdate.setImportantConversation(false);
                        this.mChannelToUpdate.setAllowBubbles(false);
                    }
                }
                this.mINotificationManager.updateNotificationChannelForPackage(this.mAppPkg, this.mAppUid, this.mChannelToUpdate);
            } catch (RemoteException e) {
                Log.e("ConversationGuts", "Unable to update notification channel", e);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$run$0() {
            NotificationConversationInfo.this.mBubbleController.onUserChangedImportance(NotificationConversationInfo.this.mEntry);
        }
    }
}
