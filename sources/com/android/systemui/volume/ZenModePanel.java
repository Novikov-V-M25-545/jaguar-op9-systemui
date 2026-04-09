package com.android.systemui.volume;

import android.animation.LayoutTransition;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.service.notification.Condition;
import android.service.notification.ZenModeConfig;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.Prefs;
import com.android.systemui.R;
import com.android.systemui.qs.QSDndEvent;
import com.android.systemui.qs.QSEvents;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.volume.Interaction;
import com.android.systemui.volume.SegmentedButtons;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Objects;

/* loaded from: classes.dex */
public class ZenModePanel extends FrameLayout {
    private static final boolean DEBUG = Log.isLoggable("ZenModePanel", 3);
    private static final int DEFAULT_BUCKET_INDEX;
    private static final int MAX_BUCKET_MINUTES;
    private static final int[] MINUTE_BUCKETS;
    private static final int MIN_BUCKET_MINUTES;
    public static final Intent ZEN_PRIORITY_SETTINGS;
    public static final Intent ZEN_SETTINGS;
    private boolean mAttached;
    private int mAttachedZen;
    private View mAutoRule;
    private TextView mAutoTitle;
    private int mBucketIndex;
    private Callback mCallback;
    private final ConfigurableTexts mConfigurableTexts;
    private final Context mContext;
    private ZenModeController mController;
    private ViewGroup mEdit;
    private View mEmpty;
    private ImageView mEmptyIcon;
    private TextView mEmptyText;
    private Condition mExitCondition;
    private boolean mExpanded;
    private final Uri mForeverId;
    private final H mHandler;
    private boolean mHidden;
    protected final LayoutInflater mInflater;
    private final Interaction.Callback mInteractionCallback;
    private final ZenPrefs mPrefs;
    private Condition mSessionExitCondition;
    private int mSessionZen;
    private int mState;
    private String mTag;
    private final TransitionHelper mTransitionHelper;
    private final UiEventLogger mUiEventLogger;
    private boolean mVoiceCapable;
    private TextView mZenAlarmWarning;
    protected SegmentedButtons mZenButtons;
    protected final SegmentedButtons.Callback mZenButtonsCallback;
    private final ZenModeController.Callback mZenCallback;
    protected LinearLayout mZenConditions;
    private View mZenIntroduction;
    private View mZenIntroductionConfirm;
    private TextView mZenIntroductionCustomize;
    private TextView mZenIntroductionMessage;
    protected int mZenModeButtonLayoutId;
    protected int mZenModeConditionLayoutId;
    private RadioGroup mZenRadioGroup;
    private LinearLayout mZenRadioGroupContent;

    public interface Callback {
        void onExpanded(boolean z);

        void onInteraction();

        void onPrioritySettings();
    }

    private static String prefKeyForConfirmation(int i) {
        if (i == 1) {
            return "DndConfirmedPriorityIntroduction";
        }
        if (i == 2) {
            return "DndConfirmedSilenceIntroduction";
        }
        if (i != 3) {
            return null;
        }
        return "DndConfirmedAlarmIntroduction";
    }

    static {
        int[] iArr = ZenModeConfig.MINUTE_BUCKETS;
        MINUTE_BUCKETS = iArr;
        MIN_BUCKET_MINUTES = iArr[0];
        MAX_BUCKET_MINUTES = iArr[iArr.length - 1];
        DEFAULT_BUCKET_INDEX = Arrays.binarySearch(iArr, 60);
        ZEN_SETTINGS = new Intent("android.settings.ZEN_MODE_SETTINGS");
        ZEN_PRIORITY_SETTINGS = new Intent("android.settings.ZEN_MODE_PRIORITY_SETTINGS");
    }

    public ZenModePanel(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mHandler = new H();
        this.mTransitionHelper = new TransitionHelper();
        this.mUiEventLogger = QSEvents.INSTANCE.getQsUiEventsLogger();
        this.mTag = "ZenModePanel/" + Integer.toHexString(System.identityHashCode(this));
        this.mBucketIndex = -1;
        this.mState = 0;
        this.mZenCallback = new ZenModeController.Callback() { // from class: com.android.systemui.volume.ZenModePanel.6
            @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
            public void onManualRuleChanged(ZenModeConfig.ZenRule zenRule) {
                ZenModePanel.this.mHandler.obtainMessage(2, zenRule).sendToTarget();
            }
        };
        this.mZenButtonsCallback = new SegmentedButtons.Callback() { // from class: com.android.systemui.volume.ZenModePanel.7
            @Override // com.android.systemui.volume.SegmentedButtons.Callback
            public void onSelected(Object obj, boolean z) {
                if (obj != null && ZenModePanel.this.mZenButtons.isShown() && ZenModePanel.this.isAttachedToWindow()) {
                    final int iIntValue = ((Integer) obj).intValue();
                    if (z) {
                        MetricsLogger.action(ZenModePanel.this.mContext, 165, iIntValue);
                    }
                    if (ZenModePanel.DEBUG) {
                        Log.d(ZenModePanel.this.mTag, "mZenButtonsCallback selected=" + iIntValue);
                    }
                    ZenModePanel zenModePanel = ZenModePanel.this;
                    final Uri realConditionId = zenModePanel.getRealConditionId(zenModePanel.mSessionExitCondition);
                    AsyncTask.execute(new Runnable() { // from class: com.android.systemui.volume.ZenModePanel.7.1
                        @Override // java.lang.Runnable
                        public void run() {
                            ZenModePanel.this.mController.setZen(iIntValue, realConditionId, "ZenModePanel.selectZen");
                            if (iIntValue != 0) {
                                Prefs.putInt(ZenModePanel.this.mContext, "DndFavoriteZen", iIntValue);
                            }
                        }
                    });
                }
            }

            @Override // com.android.systemui.volume.Interaction.Callback
            public void onInteraction() {
                ZenModePanel.this.fireInteraction();
            }
        };
        this.mInteractionCallback = new Interaction.Callback() { // from class: com.android.systemui.volume.ZenModePanel.8
            @Override // com.android.systemui.volume.Interaction.Callback
            public void onInteraction() {
                ZenModePanel.this.fireInteraction();
            }
        };
        this.mContext = context;
        this.mPrefs = new ZenPrefs();
        this.mInflater = LayoutInflater.from(context);
        this.mForeverId = Condition.newId(context).appendPath("forever").build();
        this.mConfigurableTexts = new ConfigurableTexts(context);
        this.mVoiceCapable = com.android.settingslib.volume.Util.isVoiceCapable(context);
        this.mZenModeConditionLayoutId = R.layout.zen_mode_condition;
        this.mZenModeButtonLayoutId = R.layout.zen_mode_button;
        if (DEBUG) {
            Log.d(this.mTag, "new ZenModePanel");
        }
    }

    protected void createZenButtons() {
        SegmentedButtons segmentedButtons = (SegmentedButtons) findViewById(R.id.zen_buttons);
        this.mZenButtons = segmentedButtons;
        segmentedButtons.addButton(R.string.interruption_level_none_twoline, R.string.interruption_level_none_with_warning, 2);
        this.mZenButtons.addButton(R.string.interruption_level_alarms_twoline, R.string.interruption_level_alarms, 3);
        this.mZenButtons.addButton(R.string.interruption_level_priority_twoline, R.string.interruption_level_priority, 1);
        this.mZenButtons.setCallback(this.mZenButtonsCallback);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        createZenButtons();
        this.mZenIntroduction = findViewById(R.id.zen_introduction);
        this.mZenIntroductionMessage = (TextView) findViewById(R.id.zen_introduction_message);
        View viewFindViewById = findViewById(R.id.zen_introduction_confirm);
        this.mZenIntroductionConfirm = viewFindViewById;
        viewFindViewById.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.volume.ZenModePanel$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onFinishInflate$0(view);
            }
        });
        TextView textView = (TextView) findViewById(R.id.zen_introduction_customize);
        this.mZenIntroductionCustomize = textView;
        textView.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.volume.ZenModePanel$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onFinishInflate$1(view);
            }
        });
        this.mConfigurableTexts.add(this.mZenIntroductionCustomize, R.string.zen_priority_customize_button);
        this.mZenConditions = (LinearLayout) findViewById(R.id.zen_conditions);
        this.mZenAlarmWarning = (TextView) findViewById(R.id.zen_alarm_warning);
        this.mZenRadioGroup = (RadioGroup) findViewById(R.id.zen_radio_buttons);
        this.mZenRadioGroupContent = (LinearLayout) findViewById(R.id.zen_radio_buttons_content);
        this.mEdit = (ViewGroup) findViewById(R.id.edit_container);
        View viewFindViewById2 = findViewById(android.R.id.empty);
        this.mEmpty = viewFindViewById2;
        viewFindViewById2.setVisibility(4);
        this.mEmptyText = (TextView) this.mEmpty.findViewById(android.R.id.title);
        this.mEmptyIcon = (ImageView) this.mEmpty.findViewById(android.R.id.icon);
        View viewFindViewById3 = findViewById(R.id.auto_rule);
        this.mAutoRule = viewFindViewById3;
        this.mAutoTitle = (TextView) viewFindViewById3.findViewById(android.R.id.title);
        this.mAutoRule.setVisibility(4);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onFinishInflate$0(View view) {
        confirmZenIntroduction();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onFinishInflate$1(View view) {
        confirmZenIntroduction();
        Callback callback = this.mCallback;
        if (callback != null) {
            callback.onPrioritySettings();
        }
    }

    public void setEmptyState(final int i, final int i2) {
        this.mEmptyIcon.post(new Runnable() { // from class: com.android.systemui.volume.ZenModePanel$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$setEmptyState$2(i, i2);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setEmptyState$2(int i, int i2) {
        this.mEmptyIcon.setImageResource(i);
        this.mEmptyText.setText(i2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setAutoText$3(CharSequence charSequence) {
        this.mAutoTitle.setText(charSequence);
    }

    public void setAutoText(final CharSequence charSequence) {
        this.mAutoTitle.post(new Runnable() { // from class: com.android.systemui.volume.ZenModePanel$$ExternalSyntheticLambda5
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$setAutoText$3(charSequence);
            }
        });
    }

    public void setState(int i) {
        int i2 = this.mState;
        if (i2 == i) {
            return;
        }
        transitionFrom(getView(i2), getView(i));
        this.mState = i;
    }

    private void transitionFrom(final View view, final View view2) {
        view.post(new Runnable() { // from class: com.android.systemui.volume.ZenModePanel$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                ZenModePanel.lambda$transitionFrom$5(view2, view);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$transitionFrom$5(View view, final View view2) {
        view.setAlpha(0.0f);
        view.setVisibility(0);
        view.bringToFront();
        view.animate().cancel();
        view.animate().alpha(1.0f).setDuration(300L).withEndAction(new Runnable() { // from class: com.android.systemui.volume.ZenModePanel$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                view2.setVisibility(4);
            }
        }).start();
    }

    private View getView(int i) {
        if (i == 1) {
            return this.mAutoRule;
        }
        if (i == 2) {
            return this.mEmpty;
        }
        return this.mEdit;
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mConfigurableTexts.update();
        SegmentedButtons segmentedButtons = this.mZenButtons;
        if (segmentedButtons != null) {
            segmentedButtons.update();
        }
    }

    private void confirmZenIntroduction() {
        String strPrefKeyForConfirmation = prefKeyForConfirmation(getSelectedZen(0));
        if (strPrefKeyForConfirmation == null) {
            return;
        }
        if (DEBUG) {
            Log.d("ZenModePanel", "confirmZenIntroduction " + strPrefKeyForConfirmation);
        }
        Prefs.putBoolean(this.mContext, strPrefKeyForConfirmation, true);
        this.mHandler.sendEmptyMessage(3);
    }

    private void onAttach() {
        setExpanded(true);
        this.mAttachedZen = this.mController.getZen();
        ZenModeConfig.ZenRule manualRule = this.mController.getManualRule();
        this.mExitCondition = manualRule != null ? manualRule.condition : null;
        if (DEBUG) {
            Log.d(this.mTag, "onAttach " + this.mAttachedZen + " " + manualRule);
        }
        handleUpdateManualRule(manualRule);
        this.mZenButtons.setSelectedValue(Integer.valueOf(this.mAttachedZen), false);
        this.mSessionZen = this.mAttachedZen;
        this.mTransitionHelper.clear();
        this.mController.addCallback(this.mZenCallback);
        setSessionExitCondition(copy(this.mExitCondition));
        updateWidgets();
        setAttached(true);
    }

    private void onDetach() {
        if (DEBUG) {
            Log.d(this.mTag, "onDetach");
        }
        setExpanded(false);
        checkForAttachedZenChange();
        setAttached(false);
        this.mAttachedZen = -1;
        this.mSessionZen = -1;
        this.mController.removeCallback(this.mZenCallback);
        setSessionExitCondition(null);
        this.mTransitionHelper.clear();
    }

    @VisibleForTesting
    void setAttached(boolean z) {
        this.mAttached = z;
    }

    @Override // android.view.View
    public void onVisibilityAggregated(boolean z) {
        super.onVisibilityAggregated(z);
        if (z == this.mAttached) {
            return;
        }
        if (z) {
            onAttach();
        } else {
            onDetach();
        }
    }

    private void setSessionExitCondition(Condition condition) {
        if (Objects.equals(condition, this.mSessionExitCondition)) {
            return;
        }
        if (DEBUG) {
            Log.d(this.mTag, "mSessionExitCondition=" + getConditionId(condition));
        }
        this.mSessionExitCondition = condition;
    }

    private void checkForAttachedZenChange() {
        int selectedZen = getSelectedZen(-1);
        boolean z = DEBUG;
        if (z) {
            Log.d(this.mTag, "selectedZen=" + selectedZen);
        }
        if (selectedZen != this.mAttachedZen) {
            if (z) {
                Log.d(this.mTag, "attachedZen: " + this.mAttachedZen + " -> " + selectedZen);
            }
            if (selectedZen == 2) {
                this.mPrefs.trackNoneSelected();
            }
        }
    }

    private void setExpanded(boolean z) {
        if (z == this.mExpanded) {
            return;
        }
        if (DEBUG) {
            Log.d(this.mTag, "setExpanded " + z);
        }
        this.mExpanded = z;
        updateWidgets();
        fireExpanded();
    }

    protected void addZenConditions(int i) {
        for (int i2 = 0; i2 < i; i2++) {
            View viewInflate = this.mInflater.inflate(this.mZenModeButtonLayoutId, this.mEdit, false);
            viewInflate.setId(i2);
            this.mZenRadioGroup.addView(viewInflate);
            View viewInflate2 = this.mInflater.inflate(this.mZenModeConditionLayoutId, this.mEdit, false);
            viewInflate2.setId(i2 + i);
            this.mZenRadioGroupContent.addView(viewInflate2);
        }
    }

    public void init(ZenModeController zenModeController) {
        this.mController = zenModeController;
        addZenConditions(3);
        this.mSessionZen = getSelectedZen(-1);
        handleUpdateManualRule(this.mController.getManualRule());
        if (DEBUG) {
            Log.d(this.mTag, "init mExitCondition=" + this.mExitCondition);
        }
        hideAllConditions();
    }

    private void setExitCondition(Condition condition) {
        if (Objects.equals(this.mExitCondition, condition)) {
            return;
        }
        this.mExitCondition = condition;
        if (DEBUG) {
            Log.d(this.mTag, "mExitCondition=" + getConditionId(this.mExitCondition));
        }
        updateWidgets();
    }

    private static Uri getConditionId(Condition condition) {
        if (condition != null) {
            return condition.id;
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Uri getRealConditionId(Condition condition) {
        if (isForever(condition)) {
            return null;
        }
        return getConditionId(condition);
    }

    private static Condition copy(Condition condition) {
        if (condition == null) {
            return null;
        }
        return condition.copy();
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    @VisibleForTesting
    void handleUpdateManualRule(ZenModeConfig.ZenRule zenRule) {
        Condition conditionCreateCondition;
        handleUpdateZen(zenRule != null ? zenRule.zenMode : 0);
        if (zenRule == null) {
            conditionCreateCondition = null;
        } else {
            Condition condition = zenRule.condition;
            conditionCreateCondition = condition != null ? condition : createCondition(zenRule.conditionId);
        }
        handleUpdateConditions(conditionCreateCondition);
        setExitCondition(conditionCreateCondition);
    }

    private Condition createCondition(Uri uri) {
        if (ZenModeConfig.isValidCountdownToAlarmConditionId(uri)) {
            return ZenModeConfig.toNextAlarmCondition(this.mContext, ZenModeConfig.tryParseCountdownConditionId(uri), ActivityManager.getCurrentUser());
        }
        if (ZenModeConfig.isValidCountdownConditionId(uri)) {
            long jTryParseCountdownConditionId = ZenModeConfig.tryParseCountdownConditionId(uri);
            return ZenModeConfig.toTimeCondition(this.mContext, jTryParseCountdownConditionId, (int) (((jTryParseCountdownConditionId - System.currentTimeMillis()) + 30000) / 60000), ActivityManager.getCurrentUser(), false);
        }
        return forever();
    }

    private void handleUpdateZen(int i) {
        int i2 = this.mSessionZen;
        if (i2 != -1 && i2 != i) {
            this.mSessionZen = i;
        }
        this.mZenButtons.setSelectedValue(Integer.valueOf(i), false);
        updateWidgets();
    }

    @VisibleForTesting
    int getSelectedZen(int i) {
        Object selectedValue = this.mZenButtons.getSelectedValue();
        return selectedValue != null ? ((Integer) selectedValue).intValue() : i;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateWidgets() {
        int i;
        if (this.mTransitionHelper.isTransitioning()) {
            this.mTransitionHelper.pendingUpdateWidgets();
            return;
        }
        int selectedZen = getSelectedZen(0);
        boolean z = true;
        boolean z2 = selectedZen == 1;
        boolean z3 = selectedZen == 2;
        boolean z4 = selectedZen == 3;
        boolean z5 = (z2 && !this.mPrefs.mConfirmedPriorityIntroduction) || (z3 && !this.mPrefs.mConfirmedSilenceIntroduction) || (z4 && !this.mPrefs.mConfirmedAlarmIntroduction);
        this.mZenButtons.setVisibility(this.mHidden ? 8 : 0);
        this.mZenIntroduction.setVisibility(z5 ? 0 : 8);
        if (z5) {
            if (z2) {
                i = R.string.zen_priority_introduction;
            } else if (z4) {
                i = R.string.zen_alarms_introduction;
            } else if (this.mVoiceCapable) {
                i = R.string.zen_silence_introduction_voice;
            } else {
                i = R.string.zen_silence_introduction;
            }
            this.mConfigurableTexts.add(this.mZenIntroductionMessage, i);
            this.mConfigurableTexts.update();
            this.mZenIntroductionCustomize.setVisibility(z2 ? 0 : 8);
        }
        if (z3 || (z2 && !this.mController.areAlarmsAllowedInPriority())) {
            z = false;
        }
        String strComputeAlarmWarningText = computeAlarmWarningText(z);
        this.mZenAlarmWarning.setVisibility(strComputeAlarmWarningText == null ? 8 : 0);
        this.mZenAlarmWarning.setText(strComputeAlarmWarningText);
    }

    private String computeAlarmWarningText(boolean z) {
        int i;
        if (z) {
            return null;
        }
        long jCurrentTimeMillis = System.currentTimeMillis();
        long nextAlarm = this.mController.getNextAlarm();
        if (nextAlarm < jCurrentTimeMillis) {
            return null;
        }
        Condition condition = this.mSessionExitCondition;
        if (condition == null || isForever(condition)) {
            i = R.string.zen_alarm_warning_indef;
        } else {
            long jTryParseCountdownConditionId = ZenModeConfig.tryParseCountdownConditionId(this.mSessionExitCondition.id);
            i = (jTryParseCountdownConditionId <= jCurrentTimeMillis || nextAlarm >= jTryParseCountdownConditionId) ? 0 : R.string.zen_alarm_warning;
        }
        if (i == 0) {
            return null;
        }
        boolean z2 = nextAlarm - jCurrentTimeMillis < 86400000;
        boolean zIs24HourFormat = DateFormat.is24HourFormat(this.mContext, ActivityManager.getCurrentUser());
        return getResources().getString(i, getResources().getString(z2 ? R.string.alarm_template : R.string.alarm_template_far, DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), z2 ? zIs24HourFormat ? "Hm" : "hma" : zIs24HourFormat ? "EEEHm" : "EEEhma"), nextAlarm)));
    }

    @VisibleForTesting
    void handleUpdateConditions(Condition condition) {
        if (this.mTransitionHelper.isTransitioning()) {
            return;
        }
        bind(forever(), this.mZenRadioGroupContent.getChildAt(0), 0);
        if (condition == null) {
            bindGenericCountdown();
            bindNextAlarm(getTimeUntilNextAlarmCondition());
        } else if (isForever(condition)) {
            getConditionTagAt(0).rb.setChecked(true);
            bindGenericCountdown();
            bindNextAlarm(getTimeUntilNextAlarmCondition());
        } else if (isAlarm(condition)) {
            bindGenericCountdown();
            bindNextAlarm(condition);
            getConditionTagAt(2).rb.setChecked(true);
        } else if (isCountdown(condition)) {
            bindNextAlarm(getTimeUntilNextAlarmCondition());
            bind(condition, this.mZenRadioGroupContent.getChildAt(1), 1);
            getConditionTagAt(1).rb.setChecked(true);
        } else {
            Slog.wtf("ZenModePanel", "Invalid manual condition: " + condition);
        }
        this.mZenConditions.setVisibility(this.mSessionZen == 0 ? 8 : 0);
    }

    private void bindGenericCountdown() {
        int i = DEFAULT_BUCKET_INDEX;
        this.mBucketIndex = i;
        Condition timeCondition = ZenModeConfig.toTimeCondition(this.mContext, MINUTE_BUCKETS[i], ActivityManager.getCurrentUser());
        if (!this.mAttached || getConditionTagAt(1).condition == null) {
            bind(timeCondition, this.mZenRadioGroupContent.getChildAt(1), 1);
        }
    }

    private void bindNextAlarm(Condition condition) {
        View childAt = this.mZenRadioGroupContent.getChildAt(2);
        ConditionTag conditionTag = (ConditionTag) childAt.getTag();
        if (condition != null && (!this.mAttached || conditionTag == null || conditionTag.condition == null)) {
            bind(condition, childAt, 2);
        }
        ConditionTag conditionTag2 = (ConditionTag) childAt.getTag();
        boolean z = (conditionTag2 == null || conditionTag2.condition == null) ? false : true;
        this.mZenRadioGroup.getChildAt(2).setVisibility(z ? 0 : 4);
        childAt.setVisibility(z ? 0 : 4);
    }

    private Condition forever() {
        return new Condition(this.mForeverId, foreverSummary(this.mContext), "", "", 0, 1, 0);
    }

    private static String foreverSummary(Context context) {
        return context.getString(android.R.string.resolver_personal_tab);
    }

    private Condition getTimeUntilNextAlarmCondition() {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        setToMidnight(gregorianCalendar);
        gregorianCalendar.add(5, 6);
        long nextAlarm = this.mController.getNextAlarm();
        if (nextAlarm <= 0) {
            return null;
        }
        GregorianCalendar gregorianCalendar2 = new GregorianCalendar();
        gregorianCalendar2.setTimeInMillis(nextAlarm);
        setToMidnight(gregorianCalendar2);
        if (gregorianCalendar.compareTo((Calendar) gregorianCalendar2) >= 0) {
            return ZenModeConfig.toNextAlarmCondition(this.mContext, nextAlarm, ActivityManager.getCurrentUser());
        }
        return null;
    }

    private void setToMidnight(Calendar calendar) {
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
    }

    @VisibleForTesting
    ConditionTag getConditionTagAt(int i) {
        return (ConditionTag) this.mZenRadioGroupContent.getChildAt(i).getTag();
    }

    @VisibleForTesting
    int getVisibleConditions() {
        int childCount = this.mZenRadioGroupContent.getChildCount();
        int i = 0;
        for (int i2 = 0; i2 < childCount; i2++) {
            i += this.mZenRadioGroupContent.getChildAt(i2).getVisibility() == 0 ? 1 : 0;
        }
        return i;
    }

    private void hideAllConditions() {
        int childCount = this.mZenRadioGroupContent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            this.mZenRadioGroupContent.getChildAt(i).setVisibility(8);
        }
    }

    private static boolean isAlarm(Condition condition) {
        return condition != null && ZenModeConfig.isValidCountdownToAlarmConditionId(condition.id);
    }

    private static boolean isCountdown(Condition condition) {
        return condition != null && ZenModeConfig.isValidCountdownConditionId(condition.id);
    }

    private boolean isForever(Condition condition) {
        return condition != null && this.mForeverId.equals(condition.id);
    }

    private void bind(Condition condition, final View view, final int i) {
        if (condition == null) {
            throw new IllegalArgumentException("condition must not be null");
        }
        boolean z = condition.state == 1;
        final ConditionTag conditionTag = view.getTag() != null ? (ConditionTag) view.getTag() : new ConditionTag();
        view.setTag(conditionTag);
        RadioButton radioButton = conditionTag.rb;
        boolean z2 = radioButton == null;
        if (radioButton == null) {
            conditionTag.rb = (RadioButton) this.mZenRadioGroup.getChildAt(i);
        }
        conditionTag.condition = condition;
        final Uri conditionId = getConditionId(condition);
        if (DEBUG) {
            Log.d(this.mTag, "bind i=" + this.mZenRadioGroupContent.indexOfChild(view) + " first=" + z2 + " condition=" + conditionId);
        }
        conditionTag.rb.setEnabled(z);
        conditionTag.rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.android.systemui.volume.ZenModePanel.1
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public void onCheckedChanged(CompoundButton compoundButton, boolean z3) {
                if (ZenModePanel.this.mExpanded && z3) {
                    conditionTag.rb.setChecked(true);
                    if (ZenModePanel.DEBUG) {
                        Log.d(ZenModePanel.this.mTag, "onCheckedChanged " + conditionId);
                    }
                    MetricsLogger.action(ZenModePanel.this.mContext, 164);
                    ZenModePanel.this.mUiEventLogger.log(QSDndEvent.QS_DND_CONDITION_SELECT);
                    ZenModePanel.this.select(conditionTag.condition);
                    ZenModePanel.this.announceConditionSelection(conditionTag);
                }
            }
        });
        if (conditionTag.lines == null) {
            conditionTag.lines = view.findViewById(android.R.id.content);
        }
        if (conditionTag.line1 == null) {
            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            conditionTag.line1 = textView;
            this.mConfigurableTexts.add(textView);
        }
        if (conditionTag.line2 == null) {
            TextView textView2 = (TextView) view.findViewById(android.R.id.text2);
            conditionTag.line2 = textView2;
            this.mConfigurableTexts.add(textView2);
        }
        String str = !TextUtils.isEmpty(condition.line1) ? condition.line1 : condition.summary;
        String str2 = condition.line2;
        conditionTag.line1.setText(str);
        if (TextUtils.isEmpty(str2)) {
            conditionTag.line2.setVisibility(8);
        } else {
            conditionTag.line2.setVisibility(0);
            conditionTag.line2.setText(str2);
        }
        conditionTag.lines.setEnabled(z);
        conditionTag.lines.setAlpha(z ? 1.0f : 0.4f);
        ImageView imageView = (ImageView) view.findViewById(android.R.id.button1);
        imageView.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.volume.ZenModePanel.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                ZenModePanel.this.onClickTimeButton(view, conditionTag, false, i);
            }
        });
        ImageView imageView2 = (ImageView) view.findViewById(android.R.id.button2);
        imageView2.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.volume.ZenModePanel.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                ZenModePanel.this.onClickTimeButton(view, conditionTag, true, i);
            }
        });
        conditionTag.lines.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.volume.ZenModePanel.4
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                conditionTag.rb.setChecked(true);
            }
        });
        long jTryParseCountdownConditionId = ZenModeConfig.tryParseCountdownConditionId(conditionId);
        if (i != 2 && jTryParseCountdownConditionId > 0) {
            imageView.setVisibility(0);
            imageView2.setVisibility(0);
            int i2 = this.mBucketIndex;
            if (i2 > -1) {
                imageView.setEnabled(i2 > 0);
                imageView2.setEnabled(this.mBucketIndex < MINUTE_BUCKETS.length - 1);
            } else {
                imageView.setEnabled(jTryParseCountdownConditionId - System.currentTimeMillis() > ((long) (MIN_BUCKET_MINUTES * 60000)));
                imageView2.setEnabled(!Objects.equals(condition.summary, ZenModeConfig.toTimeCondition(this.mContext, MAX_BUCKET_MINUTES, ActivityManager.getCurrentUser()).summary));
            }
            imageView.setAlpha(imageView.isEnabled() ? 1.0f : 0.5f);
            imageView2.setAlpha(imageView2.isEnabled() ? 1.0f : 0.5f);
        } else {
            imageView.setVisibility(8);
            imageView2.setVisibility(8);
        }
        if (z2) {
            Interaction.register(conditionTag.rb, this.mInteractionCallback);
            Interaction.register(conditionTag.lines, this.mInteractionCallback);
            Interaction.register(imageView, this.mInteractionCallback);
            Interaction.register(imageView2, this.mInteractionCallback);
        }
        view.setVisibility(0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void announceConditionSelection(ConditionTag conditionTag) {
        String string;
        int selectedZen = getSelectedZen(0);
        if (selectedZen == 1) {
            string = this.mContext.getString(R.string.interruption_level_priority);
        } else if (selectedZen == 2) {
            string = this.mContext.getString(R.string.interruption_level_none);
        } else if (selectedZen != 3) {
            return;
        } else {
            string = this.mContext.getString(R.string.interruption_level_alarms);
        }
        announceForAccessibility(this.mContext.getString(R.string.zen_mode_and_condition, string, conditionTag.line1.getText()));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onClickTimeButton(View view, ConditionTag conditionTag, boolean z, int i) {
        MetricsLogger.action(this.mContext, 163, z);
        this.mUiEventLogger.log(z ? QSDndEvent.QS_DND_TIME_UP : QSDndEvent.QS_DND_TIME_DOWN);
        Condition timeCondition = null;
        int[] iArr = MINUTE_BUCKETS;
        int length = iArr.length;
        int i2 = this.mBucketIndex;
        if (i2 == -1) {
            long jTryParseCountdownConditionId = ZenModeConfig.tryParseCountdownConditionId(getConditionId(conditionTag.condition));
            long jCurrentTimeMillis = System.currentTimeMillis();
            for (int i3 = 0; i3 < length; i3++) {
                int i4 = z ? i3 : (length - 1) - i3;
                int i5 = MINUTE_BUCKETS[i4];
                long j = jCurrentTimeMillis + (60000 * i5);
                if ((z && j > jTryParseCountdownConditionId) || (!z && j < jTryParseCountdownConditionId)) {
                    this.mBucketIndex = i4;
                    timeCondition = ZenModeConfig.toTimeCondition(this.mContext, j, i5, ActivityManager.getCurrentUser(), false);
                    break;
                }
            }
            if (timeCondition == null) {
                int i6 = DEFAULT_BUCKET_INDEX;
                this.mBucketIndex = i6;
                timeCondition = ZenModeConfig.toTimeCondition(this.mContext, MINUTE_BUCKETS[i6], ActivityManager.getCurrentUser());
            }
        } else {
            int iMax = Math.max(0, Math.min(length - 1, i2 + (z ? 1 : -1)));
            this.mBucketIndex = iMax;
            timeCondition = ZenModeConfig.toTimeCondition(this.mContext, iArr[iMax], ActivityManager.getCurrentUser());
        }
        bind(timeCondition, view, i);
        conditionTag.rb.setChecked(true);
        select(timeCondition);
        announceConditionSelection(conditionTag);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void select(Condition condition) {
        int i;
        boolean z = DEBUG;
        if (z) {
            Log.d(this.mTag, "select " + condition);
        }
        int i2 = this.mSessionZen;
        if (i2 == -1 || i2 == 0) {
            if (z) {
                Log.d(this.mTag, "Ignoring condition selection outside of manual zen");
                return;
            }
            return;
        }
        final Uri realConditionId = getRealConditionId(condition);
        if (this.mController != null) {
            AsyncTask.execute(new Runnable() { // from class: com.android.systemui.volume.ZenModePanel.5
                @Override // java.lang.Runnable
                public void run() {
                    ZenModePanel.this.mController.setZen(ZenModePanel.this.mSessionZen, realConditionId, "ZenModePanel.selectCondition");
                }
            });
        }
        setExitCondition(condition);
        if (realConditionId == null) {
            this.mPrefs.setMinuteIndex(-1);
        } else if ((isAlarm(condition) || isCountdown(condition)) && (i = this.mBucketIndex) != -1) {
            this.mPrefs.setMinuteIndex(i);
        }
        setSessionExitCondition(copy(condition));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fireInteraction() {
        Callback callback = this.mCallback;
        if (callback != null) {
            callback.onInteraction();
        }
    }

    private void fireExpanded() {
        Callback callback = this.mCallback;
        if (callback != null) {
            callback.onExpanded(this.mExpanded);
        }
    }

    private final class H extends Handler {
        private H() {
            super(Looper.getMainLooper());
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 2) {
                ZenModePanel.this.handleUpdateManualRule((ZenModeConfig.ZenRule) message.obj);
            } else {
                if (i != 3) {
                    return;
                }
                ZenModePanel.this.updateWidgets();
            }
        }
    }

    @VisibleForTesting
    static class ConditionTag {
        Condition condition;
        TextView line1;
        TextView line2;
        View lines;
        RadioButton rb;

        ConditionTag() {
        }
    }

    private final class ZenPrefs implements SharedPreferences.OnSharedPreferenceChangeListener {
        private boolean mConfirmedAlarmIntroduction;
        private boolean mConfirmedPriorityIntroduction;
        private boolean mConfirmedSilenceIntroduction;
        private int mMinuteIndex;
        private final int mNoneDangerousThreshold;
        private int mNoneSelected;

        private ZenPrefs() {
            this.mNoneDangerousThreshold = ZenModePanel.this.mContext.getResources().getInteger(R.integer.zen_mode_alarm_warning_threshold);
            Prefs.registerListener(ZenModePanel.this.mContext, this);
            updateMinuteIndex();
            updateNoneSelected();
            updateConfirmedPriorityIntroduction();
            updateConfirmedSilenceIntroduction();
            updateConfirmedAlarmIntroduction();
        }

        public void trackNoneSelected() {
            this.mNoneSelected = clampNoneSelected(this.mNoneSelected + 1);
            if (ZenModePanel.DEBUG) {
                Log.d(ZenModePanel.this.mTag, "Setting none selected: " + this.mNoneSelected + " threshold=" + this.mNoneDangerousThreshold);
            }
            Prefs.putInt(ZenModePanel.this.mContext, "DndNoneSelected", this.mNoneSelected);
        }

        public void setMinuteIndex(int i) {
            int iClampIndex = clampIndex(i);
            if (iClampIndex == this.mMinuteIndex) {
                return;
            }
            this.mMinuteIndex = clampIndex(iClampIndex);
            if (ZenModePanel.DEBUG) {
                Log.d(ZenModePanel.this.mTag, "Setting favorite minute index: " + this.mMinuteIndex);
            }
            Prefs.putInt(ZenModePanel.this.mContext, "DndCountdownMinuteIndex", this.mMinuteIndex);
        }

        @Override // android.content.SharedPreferences.OnSharedPreferenceChangeListener
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
            updateMinuteIndex();
            updateNoneSelected();
            updateConfirmedPriorityIntroduction();
            updateConfirmedSilenceIntroduction();
            updateConfirmedAlarmIntroduction();
        }

        private void updateMinuteIndex() {
            this.mMinuteIndex = clampIndex(Prefs.getInt(ZenModePanel.this.mContext, "DndCountdownMinuteIndex", ZenModePanel.DEFAULT_BUCKET_INDEX));
            if (ZenModePanel.DEBUG) {
                Log.d(ZenModePanel.this.mTag, "Favorite minute index: " + this.mMinuteIndex);
            }
        }

        private int clampIndex(int i) {
            return MathUtils.constrain(i, -1, ZenModePanel.MINUTE_BUCKETS.length - 1);
        }

        private void updateNoneSelected() {
            this.mNoneSelected = clampNoneSelected(Prefs.getInt(ZenModePanel.this.mContext, "DndNoneSelected", 0));
            if (ZenModePanel.DEBUG) {
                Log.d(ZenModePanel.this.mTag, "None selected: " + this.mNoneSelected);
            }
        }

        private int clampNoneSelected(int i) {
            return MathUtils.constrain(i, 0, Integer.MAX_VALUE);
        }

        private void updateConfirmedPriorityIntroduction() {
            boolean z = Prefs.getBoolean(ZenModePanel.this.mContext, "DndConfirmedPriorityIntroduction", false);
            if (z == this.mConfirmedPriorityIntroduction) {
                return;
            }
            this.mConfirmedPriorityIntroduction = z;
            if (ZenModePanel.DEBUG) {
                Log.d(ZenModePanel.this.mTag, "Confirmed priority introduction: " + this.mConfirmedPriorityIntroduction);
            }
        }

        private void updateConfirmedSilenceIntroduction() {
            boolean z = Prefs.getBoolean(ZenModePanel.this.mContext, "DndConfirmedSilenceIntroduction", false);
            if (z == this.mConfirmedSilenceIntroduction) {
                return;
            }
            this.mConfirmedSilenceIntroduction = z;
            if (ZenModePanel.DEBUG) {
                Log.d(ZenModePanel.this.mTag, "Confirmed silence introduction: " + this.mConfirmedSilenceIntroduction);
            }
        }

        private void updateConfirmedAlarmIntroduction() {
            boolean z = Prefs.getBoolean(ZenModePanel.this.mContext, "DndConfirmedAlarmIntroduction", false);
            if (z == this.mConfirmedAlarmIntroduction) {
                return;
            }
            this.mConfirmedAlarmIntroduction = z;
            if (ZenModePanel.DEBUG) {
                Log.d(ZenModePanel.this.mTag, "Confirmed alarm introduction: " + this.mConfirmedAlarmIntroduction);
            }
        }
    }

    private final class TransitionHelper implements LayoutTransition.TransitionListener, Runnable {
        private boolean mPendingUpdateWidgets;
        private boolean mTransitioning;
        private final ArraySet<View> mTransitioningViews;

        private TransitionHelper() {
            this.mTransitioningViews = new ArraySet<>();
        }

        public void clear() {
            this.mTransitioningViews.clear();
            this.mPendingUpdateWidgets = false;
        }

        public void pendingUpdateWidgets() {
            this.mPendingUpdateWidgets = true;
        }

        public boolean isTransitioning() {
            return !this.mTransitioningViews.isEmpty();
        }

        @Override // android.animation.LayoutTransition.TransitionListener
        public void startTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {
            this.mTransitioningViews.add(view);
            updateTransitioning();
        }

        @Override // android.animation.LayoutTransition.TransitionListener
        public void endTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {
            this.mTransitioningViews.remove(view);
            updateTransitioning();
        }

        @Override // java.lang.Runnable
        public void run() {
            if (ZenModePanel.DEBUG) {
                Log.d(ZenModePanel.this.mTag, "TransitionHelper run mPendingUpdateWidgets=" + this.mPendingUpdateWidgets);
            }
            if (this.mPendingUpdateWidgets) {
                ZenModePanel.this.updateWidgets();
            }
            this.mPendingUpdateWidgets = false;
        }

        private void updateTransitioning() {
            boolean zIsTransitioning = isTransitioning();
            if (this.mTransitioning == zIsTransitioning) {
                return;
            }
            this.mTransitioning = zIsTransitioning;
            if (ZenModePanel.DEBUG) {
                Log.d(ZenModePanel.this.mTag, "TransitionHelper mTransitioning=" + this.mTransitioning);
            }
            if (this.mTransitioning) {
                return;
            }
            if (this.mPendingUpdateWidgets) {
                ZenModePanel.this.mHandler.post(this);
            } else {
                this.mPendingUpdateWidgets = false;
            }
        }
    }
}
