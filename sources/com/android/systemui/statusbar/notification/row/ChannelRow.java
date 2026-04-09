package com.android.systemui.statusbar.notification.row;

import android.R;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.NotificationChannel;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.android.settingslib.Utils;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ChannelEditorListView.kt */
/* loaded from: classes.dex */
public final class ChannelRow extends LinearLayout {

    @Nullable
    private NotificationChannel channel;
    private TextView channelDescription;
    private TextView channelName;

    @NotNull
    public ChannelEditorDialogController controller;
    private boolean gentle;
    private final int highlightColor;

    /* renamed from: switch, reason: not valid java name */
    private Switch f1switch;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public ChannelRow(@NotNull Context c, @NotNull AttributeSet attrs) {
        super(c, attrs);
        Intrinsics.checkParameterIsNotNull(c, "c");
        Intrinsics.checkParameterIsNotNull(attrs, "attrs");
        this.highlightColor = Utils.getColorAttrDefaultColor(getContext(), R.attr.colorControlHighlight);
    }

    public static final /* synthetic */ Switch access$getSwitch$p(ChannelRow channelRow) {
        Switch r1 = channelRow.f1switch;
        if (r1 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("switch");
        }
        return r1;
    }

    @NotNull
    public final ChannelEditorDialogController getController() {
        ChannelEditorDialogController channelEditorDialogController = this.controller;
        if (channelEditorDialogController == null) {
            Intrinsics.throwUninitializedPropertyAccessException("controller");
        }
        return channelEditorDialogController;
    }

    public final void setController(@NotNull ChannelEditorDialogController channelEditorDialogController) {
        Intrinsics.checkParameterIsNotNull(channelEditorDialogController, "<set-?>");
        this.controller = channelEditorDialogController;
    }

    @Nullable
    public final NotificationChannel getChannel() {
        return this.channel;
    }

    public final void setChannel(@Nullable NotificationChannel notificationChannel) {
        this.channel = notificationChannel;
        updateImportance();
        updateViews();
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        View viewFindViewById = findViewById(com.android.systemui.R.id.channel_name);
        Intrinsics.checkExpressionValueIsNotNull(viewFindViewById, "findViewById(R.id.channel_name)");
        this.channelName = (TextView) viewFindViewById;
        View viewFindViewById2 = findViewById(com.android.systemui.R.id.channel_description);
        Intrinsics.checkExpressionValueIsNotNull(viewFindViewById2, "findViewById(R.id.channel_description)");
        this.channelDescription = (TextView) viewFindViewById2;
        View viewFindViewById3 = findViewById(com.android.systemui.R.id.toggle);
        Intrinsics.checkExpressionValueIsNotNull(viewFindViewById3, "findViewById(R.id.toggle)");
        Switch r0 = (Switch) viewFindViewById3;
        this.f1switch = r0;
        if (r0 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("switch");
        }
        r0.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.android.systemui.statusbar.notification.row.ChannelRow.onFinishInflate.1
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                NotificationChannel channel = ChannelRow.this.getChannel();
                if (channel != null) {
                    ChannelRow.this.getController().proposeEditForChannel(channel, z ? channel.getImportance() : 0);
                }
            }
        });
        setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.ChannelRow.onFinishInflate.2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ChannelRow.access$getSwitch$p(ChannelRow.this).toggle();
            }
        });
    }

    public final void playHighlight() {
        ValueAnimator fadeInLoop = ValueAnimator.ofObject(new ArgbEvaluator(), 0, Integer.valueOf(this.highlightColor));
        Intrinsics.checkExpressionValueIsNotNull(fadeInLoop, "fadeInLoop");
        fadeInLoop.setDuration(200L);
        fadeInLoop.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.notification.row.ChannelRow.playHighlight.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator animator) {
                ChannelRow channelRow = ChannelRow.this;
                Intrinsics.checkExpressionValueIsNotNull(animator, "animator");
                Object animatedValue = animator.getAnimatedValue();
                if (animatedValue == null) {
                    throw new TypeCastException("null cannot be cast to non-null type kotlin.Int");
                }
                channelRow.setBackgroundColor(((Integer) animatedValue).intValue());
            }
        });
        fadeInLoop.setRepeatMode(2);
        fadeInLoop.setRepeatCount(5);
        fadeInLoop.start();
    }

    /* JADX WARN: Removed duplicated region for block: B:32:0x005c  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private final void updateViews() {
        /*
            r6 = this;
            android.app.NotificationChannel r0 = r6.channel
            if (r0 == 0) goto L7b
            android.widget.TextView r1 = r6.channelName
            if (r1 != 0) goto Ld
            java.lang.String r2 = "channelName"
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r2)
        Ld:
            java.lang.CharSequence r2 = r0.getName()
            if (r2 == 0) goto L14
            goto L16
        L14:
            java.lang.String r2 = ""
        L16:
            r1.setText(r2)
            java.lang.String r1 = r0.getGroup()
            java.lang.String r2 = "channelDescription"
            if (r1 == 0) goto L38
            android.widget.TextView r3 = r6.channelDescription
            if (r3 != 0) goto L28
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r2)
        L28:
            com.android.systemui.statusbar.notification.row.ChannelEditorDialogController r4 = r6.controller
            if (r4 != 0) goto L31
            java.lang.String r5 = "controller"
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r5)
        L31:
            java.lang.CharSequence r1 = r4.groupNameForId(r1)
            r3.setText(r1)
        L38:
            java.lang.String r1 = r0.getGroup()
            r3 = 0
            if (r1 == 0) goto L5c
            android.widget.TextView r1 = r6.channelDescription
            if (r1 != 0) goto L46
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r2)
        L46:
            java.lang.CharSequence r1 = r1.getText()
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 == 0) goto L51
            goto L5c
        L51:
            android.widget.TextView r1 = r6.channelDescription
            if (r1 != 0) goto L58
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r2)
        L58:
            r1.setVisibility(r3)
            goto L68
        L5c:
            android.widget.TextView r1 = r6.channelDescription
            if (r1 != 0) goto L63
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r2)
        L63:
            r2 = 8
            r1.setVisibility(r2)
        L68:
            android.widget.Switch r6 = r6.f1switch
            if (r6 != 0) goto L71
            java.lang.String r1 = "switch"
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r1)
        L71:
            int r0 = r0.getImportance()
            if (r0 == 0) goto L78
            r3 = 1
        L78:
            r6.setChecked(r3)
        L7b:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.row.ChannelRow.updateViews():void");
    }

    private final void updateImportance() {
        NotificationChannel notificationChannel = this.channel;
        boolean z = false;
        int importance = notificationChannel != null ? notificationChannel.getImportance() : 0;
        if (importance != -1000 && importance < 3) {
            z = true;
        }
        this.gentle = z;
    }
}
