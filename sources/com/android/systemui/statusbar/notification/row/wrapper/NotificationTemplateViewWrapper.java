package com.android.systemui.statusbar.notification.row.wrapper;

import android.R;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.service.notification.StatusBarNotification;
import android.util.ArraySet;
import android.view.NotificationHeaderView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.internal.util.ContrastColorUtil;
import com.android.internal.widget.NotificationActionListLayout;
import com.android.systemui.Dependency;
import com.android.systemui.UiOffloadThread;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.TransformableView;
import com.android.systemui.statusbar.ViewTransformationHelper;
import com.android.systemui.statusbar.notification.ImageTransformState;
import com.android.systemui.statusbar.notification.TransformState;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.HybridNotificationView;

/* loaded from: classes.dex */
public class NotificationTemplateViewWrapper extends NotificationHeaderViewWrapper {
    private NotificationActionListLayout mActions;
    protected View mActionsContainer;
    private ArraySet<PendingIntent> mCancelledPendingIntents;
    private int mContentHeight;
    private final int mFullHeaderTranslation;
    private float mHeaderTranslation;
    private int mMinHeightHint;
    protected ImageView mPicture;
    private ProgressBar mProgressBar;
    private View mRemoteInputHistory;
    private ImageView mReplyAction;
    private TextView mText;
    private TextView mTitle;
    private UiOffloadThread mUiOffloadThread;

    protected NotificationTemplateViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
        this.mCancelledPendingIntents = new ArraySet<>();
        this.mTransformationHelper.setCustomTransformation(new ViewTransformationHelper.CustomTransformation() { // from class: com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper.1
            @Override // com.android.systemui.statusbar.ViewTransformationHelper.CustomTransformation
            public boolean transformTo(TransformState transformState, TransformableView transformableView, float f) {
                if (!(transformableView instanceof HybridNotificationView)) {
                    return false;
                }
                TransformState currentState = transformableView.getCurrentState(1);
                CrossFadeHelper.fadeOut(transformState.getTransformedView(), f);
                if (currentState != null) {
                    transformState.transformViewVerticalTo(currentState, this, f);
                    currentState.recycle();
                }
                return true;
            }

            @Override // com.android.systemui.statusbar.ViewTransformationHelper.CustomTransformation
            public boolean customTransformTarget(TransformState transformState, TransformState transformState2) {
                transformState.setTransformationEndY(getTransformationY(transformState, transformState2));
                return true;
            }

            @Override // com.android.systemui.statusbar.ViewTransformationHelper.CustomTransformation
            public boolean transformFrom(TransformState transformState, TransformableView transformableView, float f) {
                if (!(transformableView instanceof HybridNotificationView)) {
                    return false;
                }
                TransformState currentState = transformableView.getCurrentState(1);
                CrossFadeHelper.fadeIn(transformState.getTransformedView(), f, true);
                if (currentState != null) {
                    transformState.transformViewVerticalFrom(currentState, this, f);
                    currentState.recycle();
                }
                return true;
            }

            @Override // com.android.systemui.statusbar.ViewTransformationHelper.CustomTransformation
            public boolean initTransformation(TransformState transformState, TransformState transformState2) {
                transformState.setTransformationStartY(getTransformationY(transformState, transformState2));
                return true;
            }

            private float getTransformationY(TransformState transformState, TransformState transformState2) {
                return ((transformState2.getLaidOutLocationOnScreen()[1] + transformState2.getTransformedView().getHeight()) - transformState.getLaidOutLocationOnScreen()[1]) * 0.33f;
            }
        }, 2);
        this.mFullHeaderTranslation = context.getResources().getDimensionPixelSize(R.dimen.indeterminate_progress_alpha_58) - context.getResources().getDimensionPixelSize(R.dimen.input_extract_action_button_height);
    }

    private void resolveTemplateViews(StatusBarNotification statusBarNotification) {
        ImageView imageView = (ImageView) this.mView.findViewById(R.id.permissionDialog_description);
        this.mPicture = imageView;
        if (imageView != null) {
            imageView.setTag(ImageTransformState.ICON_TAG, statusBarNotification.getNotification().getLargeIcon());
        }
        this.mTitle = (TextView) this.mView.findViewById(R.id.title);
        this.mText = (TextView) this.mView.findViewById(R.id.sensitive);
        View viewFindViewById = this.mView.findViewById(R.id.progress);
        if (viewFindViewById instanceof ProgressBar) {
            this.mProgressBar = (ProgressBar) viewFindViewById;
        } else {
            this.mProgressBar = null;
        }
        this.mActionsContainer = this.mView.findViewById(R.id.account_row_text);
        this.mActions = this.mView.findViewById(R.id.account_row_icon);
        this.mReplyAction = (ImageView) this.mView.findViewById(R.id.overlay);
        this.mRemoteInputHistory = this.mView.findViewById(R.id.map);
        updatePendingIntentCancellations();
    }

    private void updatePendingIntentCancellations() {
        NotificationActionListLayout notificationActionListLayout = this.mActions;
        if (notificationActionListLayout != null) {
            int childCount = notificationActionListLayout.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final Button button = (Button) this.mActions.getChildAt(i);
                performOnPendingIntentCancellation(button, new Runnable() { // from class: com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper$$ExternalSyntheticLambda4
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$updatePendingIntentCancellations$0(button);
                    }
                });
            }
        }
        ImageView imageView = this.mReplyAction;
        if (imageView != null) {
            imageView.setEnabled(true);
            performOnPendingIntentCancellation(this.mReplyAction, new Runnable() { // from class: com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper$$ExternalSyntheticLambda2
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$updatePendingIntentCancellations$1();
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updatePendingIntentCancellations$0(Button button) {
        if (button.isEnabled()) {
            button.setEnabled(false);
            ColorStateList textColors = button.getTextColors();
            int[] colors = textColors.getColors();
            int[] iArr = new int[colors.length];
            float f = this.mView.getResources().getFloat(R.dimen.indeterminate_progress_alpha_47);
            for (int i = 0; i < colors.length; i++) {
                iArr[i] = blendColorWithBackground(colors[i], f);
            }
            button.setTextColor(new ColorStateList(textColors.getStates(), iArr));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updatePendingIntentCancellations$1() {
        ImageView imageView = this.mReplyAction;
        if (imageView == null || !imageView.isEnabled()) {
            return;
        }
        this.mReplyAction.setEnabled(false);
        Drawable drawableMutate = this.mReplyAction.getDrawable().mutate();
        PorterDuffColorFilter porterDuffColorFilter = (PorterDuffColorFilter) drawableMutate.getColorFilter();
        float f = this.mView.getResources().getFloat(R.dimen.indeterminate_progress_alpha_47);
        if (porterDuffColorFilter != null) {
            drawableMutate.mutate().setColorFilter(blendColorWithBackground(porterDuffColorFilter.getColor(), f), porterDuffColorFilter.getMode());
        } else {
            this.mReplyAction.setAlpha(f);
        }
    }

    private int blendColorWithBackground(int i, float f) {
        return ContrastColorUtil.compositeColors(Color.argb((int) (f * 255.0f), Color.red(i), Color.green(i), Color.blue(i)), resolveBackgroundColor());
    }

    private void performOnPendingIntentCancellation(View view, final Runnable runnable) {
        final PendingIntent pendingIntent = (PendingIntent) view.getTag(R.id.month_name);
        if (pendingIntent == null) {
            return;
        }
        if (this.mCancelledPendingIntents.contains(pendingIntent)) {
            runnable.run();
            return;
        }
        final PendingIntent.CancelListener cancelListener = new PendingIntent.CancelListener() { // from class: com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper$$ExternalSyntheticLambda0
            public final void onCancelled(PendingIntent pendingIntent2) {
                this.f$0.lambda$performOnPendingIntentCancellation$3(pendingIntent, runnable, pendingIntent2);
            }
        };
        if (this.mUiOffloadThread == null) {
            this.mUiOffloadThread = (UiOffloadThread) Dependency.get(UiOffloadThread.class);
        }
        if (view.isAttachedToWindow()) {
            this.mUiOffloadThread.execute(new Runnable() { // from class: com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    pendingIntent.registerCancelListener(cancelListener);
                }
            });
        }
        view.addOnAttachStateChangeListener(new AnonymousClass2(pendingIntent, cancelListener));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$performOnPendingIntentCancellation$3(final PendingIntent pendingIntent, final Runnable runnable, PendingIntent pendingIntent2) {
        this.mView.post(new Runnable() { // from class: com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$performOnPendingIntentCancellation$2(pendingIntent, runnable);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$performOnPendingIntentCancellation$2(PendingIntent pendingIntent, Runnable runnable) {
        this.mCancelledPendingIntents.add(pendingIntent);
        runnable.run();
    }

    /* renamed from: com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper$2, reason: invalid class name */
    class AnonymousClass2 implements View.OnAttachStateChangeListener {
        final /* synthetic */ PendingIntent.CancelListener val$listener;
        final /* synthetic */ PendingIntent val$pendingIntent;

        AnonymousClass2(PendingIntent pendingIntent, PendingIntent.CancelListener cancelListener) {
            this.val$pendingIntent = pendingIntent;
            this.val$listener = cancelListener;
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewAttachedToWindow(View view) {
            UiOffloadThread uiOffloadThread = NotificationTemplateViewWrapper.this.mUiOffloadThread;
            final PendingIntent pendingIntent = this.val$pendingIntent;
            final PendingIntent.CancelListener cancelListener = this.val$listener;
            uiOffloadThread.execute(new Runnable() { // from class: com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper$2$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    pendingIntent.registerCancelListener(cancelListener);
                }
            });
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewDetachedFromWindow(View view) {
            UiOffloadThread uiOffloadThread = NotificationTemplateViewWrapper.this.mUiOffloadThread;
            final PendingIntent pendingIntent = this.val$pendingIntent;
            final PendingIntent.CancelListener cancelListener = this.val$listener;
            uiOffloadThread.execute(new Runnable() { // from class: com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper$2$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    pendingIntent.unregisterCancelListener(cancelListener);
                }
            });
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public boolean disallowSingleClick(float f, float f2) {
        ImageView imageView = this.mReplyAction;
        if (imageView != null && imageView.getVisibility() == 0 && (isOnView(this.mReplyAction, f, f2) || isOnView(this.mPicture, f, f2))) {
            return true;
        }
        return super.disallowSingleClick(f, f2);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void onContentUpdated(ExpandableNotificationRow expandableNotificationRow) {
        resolveTemplateViews(expandableNotificationRow.getEntry().getSbn());
        super.onContentUpdated(expandableNotificationRow);
        if (expandableNotificationRow.getHeaderVisibleAmount() != 1.0f) {
            setHeaderVisibleAmount(expandableNotificationRow.getHeaderVisibleAmount());
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper
    protected void updateTransformedTypes() {
        super.updateTransformedTypes();
        TextView textView = this.mTitle;
        if (textView != null) {
            this.mTransformationHelper.addTransformedView(1, textView);
        }
        TextView textView2 = this.mText;
        if (textView2 != null) {
            this.mTransformationHelper.addTransformedView(2, textView2);
        }
        ImageView imageView = this.mPicture;
        if (imageView != null) {
            this.mTransformationHelper.addTransformedView(3, imageView);
        }
        ProgressBar progressBar = this.mProgressBar;
        if (progressBar != null) {
            this.mTransformationHelper.addTransformedView(4, progressBar);
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void setContentHeight(int i, int i2) {
        super.setContentHeight(i, i2);
        this.mContentHeight = i;
        this.mMinHeightHint = i2;
        updateActionOffset();
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public boolean shouldClipToRounding(boolean z, boolean z2) {
        View view;
        if (super.shouldClipToRounding(z, z2)) {
            return true;
        }
        return (!z2 || (view = this.mActionsContainer) == null || view.getVisibility() == 8) ? false : true;
    }

    private void updateActionOffset() {
        if (this.mActionsContainer != null) {
            this.mActionsContainer.setTranslationY((Math.max(this.mContentHeight, this.mMinHeightHint) - this.mView.getHeight()) - getHeaderTranslation(false));
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public int getHeaderTranslation(boolean z) {
        return z ? this.mFullHeaderTranslation : (int) this.mHeaderTranslation;
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void setHeaderVisibleAmount(float f) {
        float f2;
        super.setHeaderVisibleAmount(f);
        NotificationHeaderView notificationHeaderView = this.mNotificationHeader;
        if (notificationHeaderView != null) {
            notificationHeaderView.setAlpha(f);
            f2 = (1.0f - f) * this.mFullHeaderTranslation;
        } else {
            f2 = 0.0f;
        }
        this.mHeaderTranslation = f2;
        this.mView.setTranslationY(f2);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public int getExtraMeasureHeight() {
        NotificationActionListLayout notificationActionListLayout = this.mActions;
        int extraMeasureHeight = notificationActionListLayout != null ? notificationActionListLayout.getExtraMeasureHeight() : 0;
        View view = this.mRemoteInputHistory;
        if (view != null && view.getVisibility() != 8) {
            extraMeasureHeight += this.mRow.getContext().getResources().getDimensionPixelSize(com.android.systemui.R.dimen.remote_input_history_extra_height);
        }
        return extraMeasureHeight + super.getExtraMeasureHeight();
    }
}
