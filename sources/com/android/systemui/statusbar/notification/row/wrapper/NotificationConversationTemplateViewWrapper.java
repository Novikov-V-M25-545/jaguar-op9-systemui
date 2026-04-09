package com.android.systemui.statusbar.notification.row.wrapper;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.widget.CachingIconView;
import com.android.internal.widget.ConversationLayout;
import com.android.internal.widget.MessagingLinearLayout;
import com.android.systemui.R;
import com.android.systemui.statusbar.TransformableView;
import com.android.systemui.statusbar.ViewTransformationHelper;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.TransformState;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.HybridNotificationView;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: NotificationConversationTemplateViewWrapper.kt */
/* loaded from: classes.dex */
public final class NotificationConversationTemplateViewWrapper extends NotificationTemplateViewWrapper {
    private View appName;
    private View conversationBadgeBg;
    private CachingIconView conversationIconView;
    private final ConversationLayout conversationLayout;
    private View conversationTitleView;
    private View expandButton;
    private View expandButtonContainer;
    private View expandButtonInnerContainer;
    private View facePileBottom;
    private View facePileBottomBg;
    private View facePileTop;
    private ViewGroup imageMessageContainer;
    private View importanceRing;
    private MessagingLinearLayout messagingLinearLayout;
    private final int minHeightWithActions;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public NotificationConversationTemplateViewWrapper(@NotNull Context ctx, @NotNull View view, @NotNull ExpandableNotificationRow row) {
        super(ctx, view, row);
        Intrinsics.checkParameterIsNotNull(ctx, "ctx");
        Intrinsics.checkParameterIsNotNull(view, "view");
        Intrinsics.checkParameterIsNotNull(row, "row");
        this.minHeightWithActions = NotificationUtils.getFontScaledHeight(ctx, R.dimen.notification_messaging_actions_min_height);
        this.conversationLayout = (ConversationLayout) view;
    }

    private final void resolveViews() {
        MessagingLinearLayout messagingLinearLayout = this.conversationLayout.getMessagingLinearLayout();
        Intrinsics.checkExpressionValueIsNotNull(messagingLinearLayout, "conversationLayout.messagingLinearLayout");
        this.messagingLinearLayout = messagingLinearLayout;
        ViewGroup imageMessageContainer = this.conversationLayout.getImageMessageContainer();
        Intrinsics.checkExpressionValueIsNotNull(imageMessageContainer, "conversationLayout.imageMessageContainer");
        this.imageMessageContainer = imageMessageContainer;
        ConversationLayout conversationLayout = this.conversationLayout;
        CachingIconView cachingIconViewRequireViewById = conversationLayout.requireViewById(android.R.id.clamp);
        Intrinsics.checkExpressionValueIsNotNull(cachingIconViewRequireViewById, "requireViewById(com.andr…l.R.id.conversation_icon)");
        this.conversationIconView = cachingIconViewRequireViewById;
        View viewRequireViewById = conversationLayout.requireViewById(android.R.id.clipBounds);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById, "requireViewById(com.andr…nversation_icon_badge_bg)");
        this.conversationBadgeBg = viewRequireViewById;
        View viewRequireViewById2 = conversationLayout.requireViewById(android.R.id.current_scene);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById2, "requireViewById(com.andr…ernal.R.id.expand_button)");
        this.expandButton = viewRequireViewById2;
        View viewRequireViewById3 = conversationLayout.requireViewById(android.R.id.cycle);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById3, "requireViewById(com.andr….expand_button_container)");
        this.expandButtonContainer = viewRequireViewById3;
        View viewRequireViewById4 = conversationLayout.requireViewById(android.R.id.dangerous);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById4, "requireViewById(com.andr…d_button_inner_container)");
        this.expandButtonInnerContainer = viewRequireViewById4;
        View viewRequireViewById5 = conversationLayout.requireViewById(android.R.id.clip_children_set_tag);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById5, "requireViewById(com.andr…ersation_icon_badge_ring)");
        this.importanceRing = viewRequireViewById5;
        View viewRequireViewById6 = conversationLayout.requireViewById(android.R.id.aerr_mute);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById6, "requireViewById(com.andr…ernal.R.id.app_name_text)");
        this.appName = viewRequireViewById6;
        View viewRequireViewById7 = conversationLayout.requireViewById(android.R.id.clip_to_padding_tag);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById7, "requireViewById(com.andr…l.R.id.conversation_text)");
        this.conversationTitleView = viewRequireViewById7;
        this.facePileTop = conversationLayout.findViewById(android.R.id.chooser_row_text_option);
        this.facePileBottom = conversationLayout.findViewById(android.R.id.chooser_header);
        this.facePileBottomBg = conversationLayout.findViewById(android.R.id.chooser_nearby_button);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void onContentUpdated(@NotNull ExpandableNotificationRow row) {
        Intrinsics.checkParameterIsNotNull(row, "row");
        resolveViews();
        super.onContentUpdated(row);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper
    protected void updateTransformedTypes() {
        super.updateTransformedTypes();
        View[] viewArr = new View[3];
        MessagingLinearLayout messagingLinearLayout = this.messagingLinearLayout;
        if (messagingLinearLayout == null) {
            Intrinsics.throwUninitializedPropertyAccessException("messagingLinearLayout");
        }
        viewArr[0] = messagingLinearLayout;
        View view = this.appName;
        if (view == null) {
            Intrinsics.throwUninitializedPropertyAccessException("appName");
        }
        viewArr[1] = view;
        View view2 = this.conversationTitleView;
        if (view2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("conversationTitleView");
        }
        viewArr[2] = view2;
        addTransformedViews(viewArr);
        ViewTransformationHelper viewTransformationHelper = this.mTransformationHelper;
        ViewTransformationHelper.CustomTransformation customTransformation = new ViewTransformationHelper.CustomTransformation() { // from class: com.android.systemui.statusbar.notification.row.wrapper.NotificationConversationTemplateViewWrapper.updateTransformedTypes.1
            @Override // com.android.systemui.statusbar.ViewTransformationHelper.CustomTransformation
            public boolean transformTo(@NotNull TransformState ownState, @NotNull TransformableView otherView, float f) {
                Intrinsics.checkParameterIsNotNull(ownState, "ownState");
                Intrinsics.checkParameterIsNotNull(otherView, "otherView");
                if (otherView instanceof HybridNotificationView) {
                    return false;
                }
                ownState.ensureVisible();
                return true;
            }

            @Override // com.android.systemui.statusbar.ViewTransformationHelper.CustomTransformation
            public boolean transformFrom(@NotNull TransformState ownState, @NotNull TransformableView otherView, float f) {
                Intrinsics.checkParameterIsNotNull(ownState, "ownState");
                Intrinsics.checkParameterIsNotNull(otherView, "otherView");
                return transformTo(ownState, otherView, f);
            }
        };
        ViewGroup viewGroup = this.imageMessageContainer;
        if (viewGroup == null) {
            Intrinsics.throwUninitializedPropertyAccessException("imageMessageContainer");
        }
        viewTransformationHelper.setCustomTransformation(customTransformation, viewGroup.getId());
        View[] viewArr2 = new View[7];
        CachingIconView cachingIconView = this.conversationIconView;
        if (cachingIconView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("conversationIconView");
        }
        viewArr2[0] = cachingIconView;
        View view3 = this.conversationBadgeBg;
        if (view3 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("conversationBadgeBg");
        }
        viewArr2[1] = view3;
        View view4 = this.expandButton;
        if (view4 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("expandButton");
        }
        viewArr2[2] = view4;
        View view5 = this.importanceRing;
        if (view5 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("importanceRing");
        }
        viewArr2[3] = view5;
        viewArr2[4] = this.facePileTop;
        viewArr2[5] = this.facePileBottom;
        viewArr2[6] = this.facePileBottomBg;
        addViewsTransformingToSimilar(viewArr2);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    @NotNull
    public View getExpandButton() {
        View view = this.expandButtonInnerContainer;
        if (view == null) {
            Intrinsics.throwUninitializedPropertyAccessException("expandButtonInnerContainer");
        }
        return view;
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void setShelfIconVisible(boolean z) {
        if (this.conversationLayout.isImportantConversation()) {
            CachingIconView cachingIconView = this.conversationIconView;
            if (cachingIconView == null) {
                Intrinsics.throwUninitializedPropertyAccessException("conversationIconView");
            }
            if (cachingIconView.getVisibility() != 8) {
                CachingIconView cachingIconView2 = this.conversationIconView;
                if (cachingIconView2 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("conversationIconView");
                }
                cachingIconView2.setForceHidden(z);
                return;
            }
        }
        super.setShelfIconVisible(z);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    @Nullable
    public View getShelfTransformationTarget() {
        if (this.conversationLayout.isImportantConversation()) {
            CachingIconView cachingIconView = this.conversationIconView;
            if (cachingIconView == null) {
                Intrinsics.throwUninitializedPropertyAccessException("conversationIconView");
            }
            if (cachingIconView.getVisibility() != 8) {
                CachingIconView cachingIconView2 = this.conversationIconView;
                if (cachingIconView2 != null) {
                    return cachingIconView2;
                }
                Intrinsics.throwUninitializedPropertyAccessException("conversationIconView");
                return cachingIconView2;
            }
            return super.getShelfTransformationTarget();
        }
        return super.getShelfTransformationTarget();
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void setRemoteInputVisible(boolean z) {
        this.conversationLayout.showHistoricMessages(z);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void updateExpandability(boolean z, @Nullable View.OnClickListener onClickListener) {
        this.conversationLayout.updateExpandability(z, onClickListener);
    }

    /* JADX WARN: Removed duplicated region for block: B:13:0x0020  */
    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean disallowSingleClick(float r5, float r6) {
        /*
            r4 = this;
            android.view.View r0 = r4.expandButtonContainer
            java.lang.String r1 = "expandButtonContainer"
            if (r0 != 0) goto L9
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r1)
        L9:
            int r0 = r0.getVisibility()
            r2 = 1
            r3 = 0
            if (r0 != 0) goto L20
            android.view.View r0 = r4.expandButtonContainer
            if (r0 != 0) goto L18
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r1)
        L18:
            boolean r0 = r4.isOnView(r0, r5, r6)
            if (r0 == 0) goto L20
            r0 = r2
            goto L21
        L20:
            r0 = r3
        L21:
            if (r0 != 0) goto L2b
            boolean r4 = super.disallowSingleClick(r5, r6)
            if (r4 == 0) goto L2a
            goto L2b
        L2a:
            r2 = r3
        L2b:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.row.wrapper.NotificationConversationTemplateViewWrapper.disallowSingleClick(float, float):boolean");
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public int getMinLayoutHeight() {
        View mActionsContainer = this.mActionsContainer;
        if (mActionsContainer != null) {
            Intrinsics.checkExpressionValueIsNotNull(mActionsContainer, "mActionsContainer");
            if (mActionsContainer.getVisibility() != 8) {
                return this.minHeightWithActions;
            }
        }
        return super.getMinLayoutHeight();
    }

    private final void addTransformedViews(View... viewArr) {
        for (View view : viewArr) {
            if (view != null) {
                this.mTransformationHelper.addTransformedView(view);
            }
        }
    }

    private final void addViewsTransformingToSimilar(View... viewArr) {
        for (View view : viewArr) {
            if (view != null) {
                this.mTransformationHelper.addViewTransformingToSimilar(view);
            }
        }
    }
}
