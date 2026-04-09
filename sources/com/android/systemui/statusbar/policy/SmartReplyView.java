package com.android.systemui.statusbar.policy;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ContrastColorUtil;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.SmartReplyController;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.phone.KeyguardDismissUtil;
import com.android.systemui.tuner.TunerService;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

/* loaded from: classes.dex */
public class SmartReplyView extends ViewGroup implements TunerService.Tunable {
    private ActivityStarter mActivityStarter;
    private int mAlpha;
    private final BreakIterator mBreakIterator;
    private PriorityQueue<Button> mCandidateButtonQueueForSqueezing;
    private final SmartReplyConstants mConstants;
    private int mCurrentAlpha;
    private int mCurrentBackgroundColor;
    private final int mDefaultBackgroundColor;
    private final int mDefaultStrokeColor;
    private final int mDefaultTextColor;
    private final int mDefaultTextColorDarkBg;
    private final int mDoubleLineButtonPaddingHorizontal;
    private final int mHeightUpperLimit;
    private final KeyguardDismissUtil mKeyguardDismissUtil;
    private final double mMinStrokeContrast;
    private final NotificationRemoteInputManager mRemoteInputManager;
    private final int mRippleColor;
    private final int mRippleColorDarkBg;
    private final int mSingleLineButtonPaddingHorizontal;
    private final int mSingleToDoubleLineButtonWidthIncrease;
    private boolean mSmartRepliesGeneratedByAssistant;
    private View mSmartReplyContainer;
    private final int mSpacing;
    private final int mStrokeWidth;
    private static final int MEASURE_SPEC_ANY_LENGTH = View.MeasureSpec.makeMeasureSpec(0, 0);
    private static final Comparator<View> DECREASING_MEASURED_WIDTH_WITHOUT_PADDING_COMPARATOR = new Comparator() { // from class: com.android.systemui.statusbar.policy.SmartReplyView$$ExternalSyntheticLambda4
        @Override // java.util.Comparator
        public final int compare(Object obj, Object obj2) {
            return SmartReplyView.lambda$static$0((View) obj, (View) obj2);
        }
    };

    private enum SmartButtonType {
        REPLY,
        ACTION
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ int lambda$static$0(View view, View view2) {
        return ((view2.getMeasuredWidth() - view2.getPaddingLeft()) - view2.getPaddingRight()) - ((view.getMeasuredWidth() - view.getPaddingLeft()) - view.getPaddingRight());
    }

    public SmartReplyView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mSmartRepliesGeneratedByAssistant = false;
        this.mAlpha = 255;
        this.mConstants = (SmartReplyConstants) Dependency.get(SmartReplyConstants.class);
        this.mKeyguardDismissUtil = (KeyguardDismissUtil) Dependency.get(KeyguardDismissUtil.class);
        this.mRemoteInputManager = (NotificationRemoteInputManager) Dependency.get(NotificationRemoteInputManager.class);
        this.mHeightUpperLimit = NotificationUtils.getFontScaledHeight(((ViewGroup) this).mContext, R.dimen.smart_reply_button_max_height);
        int color = context.getColor(R.color.smart_reply_button_background);
        this.mCurrentBackgroundColor = color;
        this.mDefaultBackgroundColor = color;
        this.mDefaultTextColor = ((ViewGroup) this).mContext.getColor(R.color.smart_reply_button_text);
        this.mDefaultTextColorDarkBg = ((ViewGroup) this).mContext.getColor(R.color.smart_reply_button_text_dark_bg);
        int color2 = ((ViewGroup) this).mContext.getColor(R.color.smart_reply_button_stroke);
        this.mDefaultStrokeColor = color2;
        int color3 = ((ViewGroup) this).mContext.getColor(R.color.notification_ripple_untinted_color);
        this.mRippleColor = color3;
        this.mRippleColorDarkBg = Color.argb(Color.alpha(color3), 255, 255, 255);
        this.mMinStrokeContrast = ContrastColorUtil.calculateContrast(color2, color);
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.SmartReplyView, 0, 0);
        int indexCount = typedArrayObtainStyledAttributes.getIndexCount();
        int dimensionPixelSize = 0;
        int dimensionPixelSize2 = 0;
        int dimensionPixelSize3 = 0;
        int dimensionPixelSize4 = 0;
        for (int i = 0; i < indexCount; i++) {
            int index = typedArrayObtainStyledAttributes.getIndex(i);
            if (index == R.styleable.SmartReplyView_spacing) {
                dimensionPixelSize2 = typedArrayObtainStyledAttributes.getDimensionPixelSize(i, 0);
            } else if (index == R.styleable.SmartReplyView_singleLineButtonPaddingHorizontal) {
                dimensionPixelSize3 = typedArrayObtainStyledAttributes.getDimensionPixelSize(i, 0);
            } else if (index == R.styleable.SmartReplyView_doubleLineButtonPaddingHorizontal) {
                dimensionPixelSize4 = typedArrayObtainStyledAttributes.getDimensionPixelSize(i, 0);
            } else if (index == R.styleable.SmartReplyView_buttonStrokeWidth) {
                dimensionPixelSize = typedArrayObtainStyledAttributes.getDimensionPixelSize(i, 0);
            }
        }
        typedArrayObtainStyledAttributes.recycle();
        this.mStrokeWidth = dimensionPixelSize;
        this.mSpacing = dimensionPixelSize2;
        this.mSingleLineButtonPaddingHorizontal = dimensionPixelSize3;
        this.mDoubleLineButtonPaddingHorizontal = dimensionPixelSize4;
        this.mSingleToDoubleLineButtonWidthIncrease = (dimensionPixelSize4 - dimensionPixelSize3) * 2;
        this.mBreakIterator = BreakIterator.getLineInstance();
        reallocateCandidateButtonQueueForSqueezing();
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "system:notification_bg_alpha");
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        str.hashCode();
        if (str.equals("system:notification_bg_alpha")) {
            this.mAlpha = TunerService.parseInteger(str2, 255);
            setBackgroundTintColor(this.mCurrentBackgroundColor);
        }
    }

    public int getHeightUpperLimit() {
        return this.mHeightUpperLimit;
    }

    private void reallocateCandidateButtonQueueForSqueezing() {
        this.mCandidateButtonQueueForSqueezing = new PriorityQueue<>(Math.max(getChildCount(), 1), DECREASING_MEASURED_WIDTH_WITHOUT_PADDING_COMPARATOR);
    }

    public void resetSmartSuggestions(View view) {
        this.mSmartReplyContainer = view;
        removeAllViews();
        this.mCurrentBackgroundColor = this.mDefaultBackgroundColor;
    }

    public void addPreInflatedButtons(List<Button> list) {
        Iterator<Button> it = list.iterator();
        while (it.hasNext()) {
            addView(it.next());
        }
        reallocateCandidateButtonQueueForSqueezing();
    }

    public List<Button> inflateRepliesFromRemoteInput(SmartReplies smartReplies, SmartReplyController smartReplyController, NotificationEntry notificationEntry, boolean z) {
        ArrayList arrayList = new ArrayList();
        if (smartReplies.remoteInput != null && smartReplies.pendingIntent != null && smartReplies.choices != null) {
            for (int i = 0; i < smartReplies.choices.size(); i++) {
                arrayList.add(inflateReplyButton(this, getContext(), i, smartReplies, smartReplyController, notificationEntry, z));
            }
            this.mSmartRepliesGeneratedByAssistant = smartReplies.fromAssistant;
        }
        return arrayList;
    }

    public List<Button> inflateSmartActions(Context context, SmartActions smartActions, SmartReplyController smartReplyController, NotificationEntry notificationEntry, HeadsUpManager headsUpManager, boolean z) {
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, ((ViewGroup) this).mContext.getTheme());
        ArrayList arrayList = new ArrayList();
        int size = smartActions.actions.size();
        for (int i = 0; i < size; i++) {
            if (smartActions.actions.get(i).actionIntent != null) {
                arrayList.add(inflateActionButton(this, getContext(), contextThemeWrapper, i, smartActions, smartReplyController, notificationEntry, headsUpManager, z));
            }
        }
        return arrayList;
    }

    public static SmartReplyView inflate(Context context) {
        return (SmartReplyView) LayoutInflater.from(context).inflate(R.layout.smart_reply_view, (ViewGroup) null);
    }

    @VisibleForTesting
    static Button inflateReplyButton(final SmartReplyView smartReplyView, final Context context, final int i, final SmartReplies smartReplies, final SmartReplyController smartReplyController, final NotificationEntry notificationEntry, boolean z) {
        final Button button = (Button) LayoutInflater.from(context).inflate(R.layout.smart_reply_button, (ViewGroup) smartReplyView, false);
        final CharSequence charSequence = smartReplies.choices.get(i);
        button.setText(charSequence);
        final ActivityStarter.OnDismissAction onDismissAction = new ActivityStarter.OnDismissAction() { // from class: com.android.systemui.statusbar.policy.SmartReplyView$$ExternalSyntheticLambda2
            @Override // com.android.systemui.plugins.ActivityStarter.OnDismissAction
            public final boolean onDismiss() {
                return SmartReplyView.lambda$inflateReplyButton$1(this.f$0, smartReplies, charSequence, i, button, smartReplyController, notificationEntry, context);
            }
        };
        View.OnClickListener delayedOnClickListener = new View.OnClickListener() { // from class: com.android.systemui.statusbar.policy.SmartReplyView$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SmartReplyView.lambda$inflateReplyButton$2(this.f$0, onDismissAction, notificationEntry, view);
            }
        };
        if (z) {
            delayedOnClickListener = new DelayedOnClickListener(delayedOnClickListener, smartReplyView.mConstants.getOnClickInitDelay());
        }
        button.setOnClickListener(delayedOnClickListener);
        button.setAccessibilityDelegate(new View.AccessibilityDelegate() { // from class: com.android.systemui.statusbar.policy.SmartReplyView.1
            @Override // android.view.View.AccessibilityDelegate
            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, SmartReplyView.this.getResources().getString(R.string.accessibility_send_smart_reply)));
            }
        });
        setButtonColors(button, smartReplyView.mCurrentBackgroundColor, smartReplyView.mDefaultStrokeColor, smartReplyView.mDefaultTextColor, smartReplyView.mRippleColor, smartReplyView.mStrokeWidth, smartReplyView.mAlpha);
        return button;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$inflateReplyButton$1(SmartReplyView smartReplyView, SmartReplies smartReplies, CharSequence charSequence, int i, Button button, SmartReplyController smartReplyController, NotificationEntry notificationEntry, Context context) throws PendingIntent.CanceledException {
        if (smartReplyView.mConstants.getEffectiveEditChoicesBeforeSending(smartReplies.remoteInput.getEditChoicesBeforeSending())) {
            NotificationEntry.EditedSuggestionInfo editedSuggestionInfo = new NotificationEntry.EditedSuggestionInfo(charSequence, i);
            NotificationRemoteInputManager notificationRemoteInputManager = smartReplyView.mRemoteInputManager;
            RemoteInput remoteInput = smartReplies.remoteInput;
            notificationRemoteInputManager.activateRemoteInput(button, new RemoteInput[]{remoteInput}, remoteInput, smartReplies.pendingIntent, editedSuggestionInfo);
            return false;
        }
        smartReplyController.smartReplySent(notificationEntry, i, button.getText(), NotificationLogger.getNotificationLocation(notificationEntry).toMetricsEventEnum(), false);
        Bundle bundle = new Bundle();
        bundle.putString(smartReplies.remoteInput.getResultKey(), charSequence.toString());
        Intent intentAddFlags = new Intent().addFlags(268435456);
        RemoteInput.addResultsToIntent(new RemoteInput[]{smartReplies.remoteInput}, intentAddFlags, bundle);
        RemoteInput.setResultsSource(intentAddFlags, 1);
        notificationEntry.setHasSentReply();
        try {
            smartReplies.pendingIntent.send(context, 0, intentAddFlags);
        } catch (PendingIntent.CanceledException e) {
            Log.w("SmartReplyView", "Unable to send smart reply", e);
        }
        smartReplyView.mSmartReplyContainer.setVisibility(8);
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$inflateReplyButton$2(SmartReplyView smartReplyView, ActivityStarter.OnDismissAction onDismissAction, NotificationEntry notificationEntry, View view) {
        smartReplyView.mKeyguardDismissUtil.executeWhenUnlocked(onDismissAction, !notificationEntry.isRowPinned());
    }

    @VisibleForTesting
    static Button inflateActionButton(final SmartReplyView smartReplyView, Context context, Context context2, final int i, final SmartActions smartActions, final SmartReplyController smartReplyController, final NotificationEntry notificationEntry, final HeadsUpManager headsUpManager, boolean z) throws Resources.NotFoundException {
        final Notification.Action action = smartActions.actions.get(i);
        Button button = (Button) LayoutInflater.from(context).inflate(R.layout.smart_action_button, (ViewGroup) smartReplyView, false);
        button.setText(action.title);
        Drawable drawableLoadDrawable = action.getIcon().loadDrawable(context2);
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(R.dimen.smart_action_button_icon_size);
        drawableLoadDrawable.setBounds(0, 0, dimensionPixelSize, dimensionPixelSize);
        button.setCompoundDrawables(drawableLoadDrawable, null, null, null);
        View.OnClickListener delayedOnClickListener = new View.OnClickListener() { // from class: com.android.systemui.statusbar.policy.SmartReplyView$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SmartReplyView.lambda$inflateActionButton$4(this.f$0, action, smartReplyController, notificationEntry, i, smartActions, headsUpManager, view);
            }
        };
        if (z) {
            delayedOnClickListener = new DelayedOnClickListener(delayedOnClickListener, smartReplyView.mConstants.getOnClickInitDelay());
        }
        button.setOnClickListener(delayedOnClickListener);
        ((LayoutParams) button.getLayoutParams()).buttonType = SmartButtonType.ACTION;
        return button;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$inflateActionButton$4(SmartReplyView smartReplyView, final Notification.Action action, final SmartReplyController smartReplyController, final NotificationEntry notificationEntry, final int i, final SmartActions smartActions, final HeadsUpManager headsUpManager, View view) {
        smartReplyView.getActivityStarter().startPendingIntentDismissingKeyguard(action.actionIntent, new Runnable() { // from class: com.android.systemui.statusbar.policy.SmartReplyView$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                SmartReplyView.lambda$inflateActionButton$3(smartReplyController, notificationEntry, i, action, smartActions, headsUpManager);
            }
        }, notificationEntry.getRow());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$inflateActionButton$3(SmartReplyController smartReplyController, NotificationEntry notificationEntry, int i, Notification.Action action, SmartActions smartActions, HeadsUpManager headsUpManager) {
        smartReplyController.smartActionClicked(notificationEntry, i, action, smartActions.fromAssistant);
        headsUpManager.removeNotification(notificationEntry.getKey(), true);
    }

    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(((ViewGroup) this).mContext, attributeSet);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public LayoutParams generateDefaultLayoutParams() {
        int i = -2;
        return new LayoutParams(i, i);
    }

    @Override // android.view.ViewGroup
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return new LayoutParams(layoutParams.width, layoutParams.height);
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int i3;
        Iterator it;
        int size = View.MeasureSpec.getMode(i) == 0 ? Integer.MAX_VALUE : View.MeasureSpec.getSize(i);
        resetButtonsLayoutParams();
        if (!this.mCandidateButtonQueueForSqueezing.isEmpty()) {
            Log.wtf("SmartReplyView", "Single line button queue leaked between onMeasure calls");
            this.mCandidateButtonQueueForSqueezing.clear();
        }
        SmartSuggestionMeasures smartSuggestionMeasures = new SmartSuggestionMeasures(((ViewGroup) this).mPaddingLeft + ((ViewGroup) this).mPaddingRight, 0, this.mSingleLineButtonPaddingHorizontal);
        List<View> listFilterActionsOrReplies = filterActionsOrReplies(SmartButtonType.ACTION);
        List<View> listFilterActionsOrReplies2 = filterActionsOrReplies(SmartButtonType.REPLY);
        ArrayList arrayList = new ArrayList(listFilterActionsOrReplies);
        arrayList.addAll(listFilterActionsOrReplies2);
        ArrayList arrayList2 = new ArrayList();
        SmartSuggestionMeasures smartSuggestionMeasuresM372clone = null;
        int maxNumActions = this.mConstants.getMaxNumActions();
        Iterator it2 = arrayList.iterator();
        int i4 = 0;
        int i5 = 0;
        while (it2.hasNext()) {
            View view = (View) it2.next();
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            if (maxNumActions == -1 || layoutParams.buttonType != SmartButtonType.ACTION || i4 < maxNumActions) {
                i3 = maxNumActions;
                it = it2;
                view.setPadding(smartSuggestionMeasures.mButtonPaddingHorizontal, view.getPaddingTop(), smartSuggestionMeasures.mButtonPaddingHorizontal, view.getPaddingBottom());
                view.measure(MEASURE_SPEC_ANY_LENGTH, i2);
                arrayList2.add(view);
                Button button = (Button) view;
                int lineCount = button.getLineCount();
                if (lineCount >= 1 && lineCount <= 2) {
                    if (lineCount == 1) {
                        this.mCandidateButtonQueueForSqueezing.add(button);
                    }
                    SmartSuggestionMeasures smartSuggestionMeasuresM372clone2 = smartSuggestionMeasures.m372clone();
                    if (smartSuggestionMeasuresM372clone == null && layoutParams.buttonType == SmartButtonType.REPLY) {
                        smartSuggestionMeasuresM372clone = smartSuggestionMeasures.m372clone();
                    }
                    int i6 = i5 == 0 ? 0 : this.mSpacing;
                    int measuredWidth = view.getMeasuredWidth();
                    int measuredHeight = view.getMeasuredHeight();
                    smartSuggestionMeasures.mMeasuredWidth += i6 + measuredWidth;
                    smartSuggestionMeasures.mMaxChildHeight = Math.max(smartSuggestionMeasures.mMaxChildHeight, measuredHeight);
                    if (smartSuggestionMeasures.mButtonPaddingHorizontal == this.mSingleLineButtonPaddingHorizontal && (lineCount == 2 || smartSuggestionMeasures.mMeasuredWidth > size)) {
                        smartSuggestionMeasures.mMeasuredWidth += (i5 + 1) * this.mSingleToDoubleLineButtonWidthIncrease;
                        smartSuggestionMeasures.mButtonPaddingHorizontal = this.mDoubleLineButtonPaddingHorizontal;
                    }
                    if (smartSuggestionMeasures.mMeasuredWidth > size) {
                        while (smartSuggestionMeasures.mMeasuredWidth > size && !this.mCandidateButtonQueueForSqueezing.isEmpty()) {
                            Button buttonPoll = this.mCandidateButtonQueueForSqueezing.poll();
                            int iSqueezeButton = squeezeButton(buttonPoll, i2);
                            if (iSqueezeButton != -1) {
                                smartSuggestionMeasures.mMaxChildHeight = Math.max(smartSuggestionMeasures.mMaxChildHeight, buttonPoll.getMeasuredHeight());
                                smartSuggestionMeasures.mMeasuredWidth -= iSqueezeButton;
                            }
                        }
                        if (smartSuggestionMeasures.mMeasuredWidth > size) {
                            markButtonsWithPendingSqueezeStatusAs(3, arrayList2);
                            maxNumActions = i3;
                            it2 = it;
                            smartSuggestionMeasures = smartSuggestionMeasuresM372clone2;
                        } else {
                            markButtonsWithPendingSqueezeStatusAs(2, arrayList2);
                        }
                    }
                    layoutParams.show = true;
                    i5++;
                    if (layoutParams.buttonType == SmartButtonType.ACTION) {
                        i4++;
                    }
                }
            } else {
                i3 = maxNumActions;
                it = it2;
            }
            maxNumActions = i3;
            it2 = it;
        }
        if (this.mSmartRepliesGeneratedByAssistant && !gotEnoughSmartReplies(listFilterActionsOrReplies2)) {
            Iterator<View> it3 = listFilterActionsOrReplies2.iterator();
            while (it3.hasNext()) {
                ((LayoutParams) it3.next().getLayoutParams()).show = false;
            }
            smartSuggestionMeasures = smartSuggestionMeasuresM372clone;
        }
        this.mCandidateButtonQueueForSqueezing.clear();
        remeasureButtonsIfNecessary(smartSuggestionMeasures.mButtonPaddingHorizontal, smartSuggestionMeasures.mMaxChildHeight);
        int iMax = Math.max(getSuggestedMinimumHeight(), ((ViewGroup) this).mPaddingTop + smartSuggestionMeasures.mMaxChildHeight + ((ViewGroup) this).mPaddingBottom);
        Iterator it4 = arrayList.iterator();
        while (it4.hasNext()) {
            setCornerRadius((Button) ((View) it4.next()), iMax / 2.0f);
        }
        setMeasuredDimension(ViewGroup.resolveSize(Math.max(getSuggestedMinimumWidth(), smartSuggestionMeasures.mMeasuredWidth), i), ViewGroup.resolveSize(iMax, i2));
    }

    private static class SmartSuggestionMeasures {
        int mButtonPaddingHorizontal;
        int mMaxChildHeight;
        int mMeasuredWidth;

        SmartSuggestionMeasures(int i, int i2, int i3) {
            this.mMeasuredWidth = -1;
            this.mMaxChildHeight = -1;
            this.mButtonPaddingHorizontal = -1;
            this.mMeasuredWidth = i;
            this.mMaxChildHeight = i2;
            this.mButtonPaddingHorizontal = i3;
        }

        /* renamed from: clone, reason: merged with bridge method [inline-methods] */
        public SmartSuggestionMeasures m372clone() {
            return new SmartSuggestionMeasures(this.mMeasuredWidth, this.mMaxChildHeight, this.mButtonPaddingHorizontal);
        }
    }

    private boolean gotEnoughSmartReplies(List<View> list) {
        Iterator<View> it = list.iterator();
        int i = 0;
        while (it.hasNext()) {
            if (((LayoutParams) it.next().getLayoutParams()).show) {
                i++;
            }
        }
        return i == 0 || i >= this.mConstants.getMinNumSystemGeneratedReplies();
    }

    private List<View> filterActionsOrReplies(SmartButtonType smartButtonType) {
        ArrayList arrayList = new ArrayList();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            if (childAt.getVisibility() == 0 && (childAt instanceof Button) && layoutParams.buttonType == smartButtonType) {
                arrayList.add(childAt);
            }
        }
        return arrayList;
    }

    private void resetButtonsLayoutParams() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            LayoutParams layoutParams = (LayoutParams) getChildAt(i).getLayoutParams();
            layoutParams.show = false;
            layoutParams.squeezeStatus = 0;
        }
    }

    private int squeezeButton(Button button, int i) {
        int iEstimateOptimalSqueezedButtonTextWidth = estimateOptimalSqueezedButtonTextWidth(button);
        if (iEstimateOptimalSqueezedButtonTextWidth == -1) {
            return -1;
        }
        return squeezeButtonToTextWidth(button, i, iEstimateOptimalSqueezedButtonTextWidth);
    }

    /* JADX WARN: Code restructure failed: missing block: B:33:0x0089, code lost:
    
        r6 = true;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private int estimateOptimalSqueezedButtonTextWidth(android.widget.Button r14) {
        /*
            r13 = this;
            java.lang.CharSequence r0 = r14.getText()
            java.lang.String r0 = r0.toString()
            android.text.method.TransformationMethod r1 = r14.getTransformationMethod()
            if (r1 != 0) goto Lf
            goto L17
        Lf:
            java.lang.CharSequence r0 = r1.getTransformation(r0, r14)
            java.lang.String r0 = r0.toString()
        L17:
            int r1 = r0.length()
            java.text.BreakIterator r2 = r13.mBreakIterator
            r2.setText(r0)
            java.text.BreakIterator r2 = r13.mBreakIterator
            int r3 = r1 / 2
            int r2 = r2.preceding(r3)
            r3 = -1
            if (r2 != r3) goto L34
            java.text.BreakIterator r2 = r13.mBreakIterator
            int r2 = r2.next()
            if (r2 != r3) goto L34
            return r3
        L34:
            android.text.TextPaint r14 = r14.getPaint()
            java.text.BreakIterator r2 = r13.mBreakIterator
            int r2 = r2.current()
            r4 = 0
            float r5 = android.text.Layout.getDesiredWidth(r0, r4, r2, r14)
            float r2 = android.text.Layout.getDesiredWidth(r0, r2, r1, r14)
            float r6 = java.lang.Math.max(r5, r2)
            int r2 = (r5 > r2 ? 1 : (r5 == r2 ? 0 : -1))
            if (r2 == 0) goto L94
            r5 = 1
            if (r2 <= 0) goto L54
            r2 = r5
            goto L55
        L54:
            r2 = r4
        L55:
            com.android.systemui.statusbar.policy.SmartReplyConstants r7 = r13.mConstants
            int r7 = r7.getMaxSqueezeRemeasureAttempts()
            r8 = r4
        L5c:
            if (r8 >= r7) goto L94
            java.text.BreakIterator r9 = r13.mBreakIterator
            if (r2 == 0) goto L67
            int r9 = r9.previous()
            goto L6b
        L67:
            int r9 = r9.next()
        L6b:
            if (r9 != r3) goto L6e
            goto L94
        L6e:
            float r10 = android.text.Layout.getDesiredWidth(r0, r4, r9, r14)
            float r9 = android.text.Layout.getDesiredWidth(r0, r9, r1, r14)
            float r11 = java.lang.Math.max(r10, r9)
            int r12 = (r11 > r6 ? 1 : (r11 == r6 ? 0 : -1))
            if (r12 >= 0) goto L94
            if (r2 == 0) goto L85
            int r6 = (r10 > r9 ? 1 : (r10 == r9 ? 0 : -1))
            if (r6 > 0) goto L8b
            goto L89
        L85:
            int r6 = (r10 > r9 ? 1 : (r10 == r9 ? 0 : -1))
            if (r6 < 0) goto L8b
        L89:
            r6 = r5
            goto L8c
        L8b:
            r6 = r4
        L8c:
            if (r6 == 0) goto L90
            r6 = r11
            goto L94
        L90:
            int r8 = r8 + 1
            r6 = r11
            goto L5c
        L94:
            double r13 = (double) r6
            double r13 = java.lang.Math.ceil(r13)
            int r13 = (int) r13
            return r13
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.SmartReplyView.estimateOptimalSqueezedButtonTextWidth(android.widget.Button):int");
    }

    private int getLeftCompoundDrawableWidthWithPadding(Button button) {
        Drawable drawable = button.getCompoundDrawables()[0];
        if (drawable == null) {
            return 0;
        }
        return drawable.getBounds().width() + button.getCompoundDrawablePadding();
    }

    private int squeezeButtonToTextWidth(Button button, int i, int i2) {
        int measuredWidth = button.getMeasuredWidth();
        int paddingLeft = button.getPaddingLeft();
        int i3 = this.mDoubleLineButtonPaddingHorizontal;
        if (paddingLeft != i3) {
            measuredWidth += this.mSingleToDoubleLineButtonWidthIncrease;
        }
        button.setPadding(i3, button.getPaddingTop(), this.mDoubleLineButtonPaddingHorizontal, button.getPaddingBottom());
        button.measure(View.MeasureSpec.makeMeasureSpec((this.mDoubleLineButtonPaddingHorizontal * 2) + i2 + getLeftCompoundDrawableWidthWithPadding(button), Integer.MIN_VALUE), i);
        int measuredWidth2 = button.getMeasuredWidth();
        LayoutParams layoutParams = (LayoutParams) button.getLayoutParams();
        if (button.getLineCount() > 2 || measuredWidth2 >= measuredWidth) {
            layoutParams.squeezeStatus = 3;
            return -1;
        }
        layoutParams.squeezeStatus = 1;
        return measuredWidth - measuredWidth2;
    }

    private void remeasureButtonsIfNecessary(int i, int i2) {
        boolean z;
        int iMakeMeasureSpec = View.MeasureSpec.makeMeasureSpec(i2, 1073741824);
        int childCount = getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = getChildAt(i3);
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            if (layoutParams.show) {
                int measuredWidth = childAt.getMeasuredWidth();
                if (layoutParams.squeezeStatus == 3) {
                    measuredWidth = Integer.MAX_VALUE;
                    z = true;
                } else {
                    z = false;
                }
                if (childAt.getPaddingLeft() != i) {
                    if (measuredWidth != Integer.MAX_VALUE) {
                        if (i == this.mSingleLineButtonPaddingHorizontal) {
                            measuredWidth -= this.mSingleToDoubleLineButtonWidthIncrease;
                        } else {
                            measuredWidth += this.mSingleToDoubleLineButtonWidthIncrease;
                        }
                    }
                    childAt.setPadding(i, childAt.getPaddingTop(), i, childAt.getPaddingBottom());
                    z = true;
                }
                if (childAt.getMeasuredHeight() == i2 ? z : true) {
                    childAt.measure(View.MeasureSpec.makeMeasureSpec(measuredWidth, Integer.MIN_VALUE), iMakeMeasureSpec);
                }
            }
        }
    }

    private void markButtonsWithPendingSqueezeStatusAs(int i, List<View> list) {
        Iterator<View> it = list.iterator();
        while (it.hasNext()) {
            LayoutParams layoutParams = (LayoutParams) it.next().getLayoutParams();
            if (layoutParams.squeezeStatus == 1) {
                layoutParams.squeezeStatus = i;
            }
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        boolean z2 = getLayoutDirection() == 1;
        int i5 = z2 ? (i3 - i) - ((ViewGroup) this).mPaddingRight : ((ViewGroup) this).mPaddingLeft;
        int childCount = getChildCount();
        for (int i6 = 0; i6 < childCount; i6++) {
            View childAt = getChildAt(i6);
            if (((LayoutParams) childAt.getLayoutParams()).show) {
                int measuredWidth = childAt.getMeasuredWidth();
                int measuredHeight = childAt.getMeasuredHeight();
                int i7 = z2 ? i5 - measuredWidth : i5;
                childAt.layout(i7, 0, i7 + measuredWidth, measuredHeight);
                int i8 = measuredWidth + this.mSpacing;
                i5 = z2 ? i5 - i8 : i5 + i8;
            }
        }
    }

    @Override // android.view.ViewGroup
    protected boolean drawChild(Canvas canvas, View view, long j) {
        return ((LayoutParams) view.getLayoutParams()).show && super.drawChild(canvas, view, j);
    }

    public void setBackgroundTintColor(int i) {
        if (i == this.mCurrentBackgroundColor && this.mAlpha == this.mCurrentAlpha) {
            return;
        }
        this.mCurrentBackgroundColor = i;
        this.mCurrentAlpha = this.mAlpha;
        boolean z = !ContrastColorUtil.isColorLight(i);
        int i2 = (-16777216) | i;
        int iEnsureTextContrast = ContrastColorUtil.ensureTextContrast(z ? this.mDefaultTextColorDarkBg : this.mDefaultTextColor, i2, z);
        int iEnsureContrast = ContrastColorUtil.ensureContrast(this.mDefaultStrokeColor, i2, z, this.mMinStrokeContrast);
        int i3 = z ? this.mRippleColorDarkBg : this.mRippleColor;
        int childCount = getChildCount();
        for (int i4 = 0; i4 < childCount; i4++) {
            setButtonColors((Button) getChildAt(i4), i, iEnsureContrast, iEnsureTextContrast, i3, this.mStrokeWidth, this.mAlpha);
        }
    }

    private static void setButtonColors(Button button, int i, int i2, int i3, int i4, int i5, int i6) {
        Drawable background = button.getBackground();
        if (background instanceof RippleDrawable) {
            Drawable drawableMutate = background.mutate();
            RippleDrawable rippleDrawable = (RippleDrawable) drawableMutate;
            rippleDrawable.setColor(ColorStateList.valueOf(i4));
            Drawable drawable = rippleDrawable.getDrawable(0);
            if (drawable instanceof InsetDrawable) {
                Drawable drawable2 = ((InsetDrawable) drawable).getDrawable();
                if (drawable2 instanceof GradientDrawable) {
                    GradientDrawable gradientDrawable = (GradientDrawable) drawable2;
                    gradientDrawable.setColor(i);
                    gradientDrawable.setStroke(i5, i2);
                }
            }
            drawableMutate.setAlpha(i6);
            button.setBackground(drawableMutate);
        }
        button.setTextColor(i3);
    }

    private void setCornerRadius(Button button, float f) {
        Drawable background = button.getBackground();
        if (background instanceof RippleDrawable) {
            Drawable drawable = ((RippleDrawable) background.mutate()).getDrawable(0);
            if (drawable instanceof InsetDrawable) {
                Drawable drawable2 = ((InsetDrawable) drawable).getDrawable();
                if (drawable2 instanceof GradientDrawable) {
                    ((GradientDrawable) drawable2).setCornerRadius(f);
                }
            }
        }
    }

    private ActivityStarter getActivityStarter() {
        if (this.mActivityStarter == null) {
            this.mActivityStarter = (ActivityStarter) Dependency.get(ActivityStarter.class);
        }
        return this.mActivityStarter;
    }

    @VisibleForTesting
    static class LayoutParams extends ViewGroup.LayoutParams {
        private SmartButtonType buttonType;
        private boolean show;
        private int squeezeStatus;

        private LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.show = false;
            this.squeezeStatus = 0;
            this.buttonType = SmartButtonType.REPLY;
        }

        private LayoutParams(int i, int i2) {
            super(i, i2);
            this.show = false;
            this.squeezeStatus = 0;
            this.buttonType = SmartButtonType.REPLY;
        }

        @VisibleForTesting
        boolean isShown() {
            return this.show;
        }
    }

    public static class SmartReplies {
        public final List<CharSequence> choices;
        public final boolean fromAssistant;
        public final PendingIntent pendingIntent;
        public final RemoteInput remoteInput;

        public SmartReplies(List<CharSequence> list, RemoteInput remoteInput, PendingIntent pendingIntent, boolean z) {
            this.choices = list;
            this.remoteInput = remoteInput;
            this.pendingIntent = pendingIntent;
            this.fromAssistant = z;
        }
    }

    public static class SmartActions {
        public final List<Notification.Action> actions;
        public final boolean fromAssistant;

        public SmartActions(List<Notification.Action> list, boolean z) {
            this.actions = list;
            this.fromAssistant = z;
        }
    }

    private static class DelayedOnClickListener implements View.OnClickListener {
        private final View.OnClickListener mActualListener;
        private final long mInitDelayMs;
        private final long mInitTimeMs = SystemClock.elapsedRealtime();

        DelayedOnClickListener(View.OnClickListener onClickListener, long j) {
            this.mActualListener = onClickListener;
            this.mInitDelayMs = j;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (hasFinishedInitialization()) {
                this.mActualListener.onClick(view);
                return;
            }
            Log.i("SmartReplyView", "Accidental Smart Suggestion click registered, delay: " + this.mInitDelayMs);
        }

        private boolean hasFinishedInitialization() {
            return SystemClock.elapsedRealtime() >= this.mInitTimeMs + this.mInitDelayMs;
        }
    }
}
