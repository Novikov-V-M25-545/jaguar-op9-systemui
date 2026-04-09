package com.android.systemui.controls.ui;

import android.app.ActivityOptions;
import android.app.ActivityView;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.ImageView;
import com.android.internal.policy.ScreenDecorationsUtils;
import com.android.systemui.R;
import kotlin.TypeCastException;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import lineageos.hardware.LineageHardwareManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: DetailDialog.kt */
/* loaded from: classes.dex */
public final class DetailDialog extends Dialog {
    public static final Companion Companion = new Companion(null);

    @NotNull
    private ActivityView activityView;

    @NotNull
    private final ControlViewHolder cvh;
    private final Intent fillInIntent;

    @NotNull
    private final PendingIntent pendingIntent;

    @NotNull
    private final ActivityView.StateCallback stateCallback;

    @NotNull
    public final PendingIntent getPendingIntent() {
        return this.pendingIntent;
    }

    /* compiled from: DetailDialog.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public DetailDialog(@NotNull ControlViewHolder cvh, @NotNull PendingIntent pendingIntent) throws Resources.NotFoundException {
        super(cvh.getContext(), R.style.Theme_SystemUI_Dialog_Control_DetailPanel);
        Intrinsics.checkParameterIsNotNull(cvh, "cvh");
        Intrinsics.checkParameterIsNotNull(pendingIntent, "pendingIntent");
        this.cvh = cvh;
        this.pendingIntent = pendingIntent;
        Intent intent = new Intent();
        intent.putExtra("controls.DISPLAY_IN_PANEL", true);
        intent.addFlags(LineageHardwareManager.FEATURE_TOUCHSCREEN_GESTURES);
        intent.addFlags(134217728);
        this.fillInIntent = intent;
        this.activityView = new ActivityView(getContext(), (AttributeSet) null, 0, false);
        this.stateCallback = new ActivityView.StateCallback() { // from class: com.android.systemui.controls.ui.DetailDialog$stateCallback$1
            public void onActivityViewDestroyed(@NotNull ActivityView view) {
                Intrinsics.checkParameterIsNotNull(view, "view");
            }

            public void onActivityViewReady(@NotNull ActivityView view) {
                Intrinsics.checkParameterIsNotNull(view, "view");
                view.startActivity(this.this$0.getPendingIntent(), this.this$0.fillInIntent, ActivityOptions.makeBasic());
            }

            public void onTaskRemovalStarted(int i) {
                this.this$0.dismiss();
            }

            public void onTaskCreated(int i, @Nullable ComponentName componentName) {
                ((ViewGroup) this.this$0.requireViewById(R.id.controls_activity_view)).setAlpha(1.0f);
            }
        };
        getWindow().setType(2020);
        setContentView(R.layout.controls_detail_dialog);
        ViewGroup viewGroup = (ViewGroup) requireViewById(R.id.controls_activity_view);
        viewGroup.addView(this.activityView);
        viewGroup.setAlpha(0.0f);
        ((ImageView) requireViewById(R.id.control_detail_close)).setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.controls.ui.DetailDialog$$special$$inlined$apply$lambda$1
            @Override // android.view.View.OnClickListener
            public final void onClick(@NotNull View view) {
                Intrinsics.checkParameterIsNotNull(view, "<anonymous parameter 0>");
                this.this$0.dismiss();
            }
        });
        final ImageView imageView = (ImageView) requireViewById(R.id.control_detail_open_in_app);
        imageView.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.controls.ui.DetailDialog$$special$$inlined$apply$lambda$2
            @Override // android.view.View.OnClickListener
            public final void onClick(@NotNull View v) throws PendingIntent.CanceledException {
                Intrinsics.checkParameterIsNotNull(v, "v");
                this.dismiss();
                imageView.getContext().sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
                this.getPendingIntent().send();
            }
        });
        getWindow().getDecorView().setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() { // from class: com.android.systemui.controls.ui.DetailDialog.4
            @Override // android.view.View.OnApplyWindowInsetsListener
            public final WindowInsets onApplyWindowInsets(@NotNull View view, @NotNull WindowInsets insets) {
                Intrinsics.checkParameterIsNotNull(view, "<anonymous parameter 0>");
                Intrinsics.checkParameterIsNotNull(insets, "insets");
                ActivityView activityView = DetailDialog.this.getActivityView();
                activityView.setPadding(activityView.getPaddingLeft(), activityView.getPaddingTop(), activityView.getPaddingRight(), insets.getInsets(WindowInsets.Type.systemBars()).bottom);
                return WindowInsets.CONSUMED;
            }
        });
        ViewGroup viewGroup2 = (ViewGroup) requireViewById(R.id.control_detail_root);
        int i = Settings.Secure.getInt(cvh.getContext().getContentResolver(), "systemui.controls_panel_top_offset", cvh.getContext().getResources().getDimensionPixelSize(R.dimen.controls_activity_view_top_offset));
        ViewGroup.LayoutParams layoutParams = viewGroup2.getLayoutParams();
        if (layoutParams == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup.MarginLayoutParams");
        }
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
        marginLayoutParams.topMargin = i;
        viewGroup2.setLayoutParams(marginLayoutParams);
        viewGroup2.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.controls.ui.DetailDialog$$special$$inlined$apply$lambda$3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.this$0.dismiss();
            }
        });
        Object parent = viewGroup2.getParent();
        if (parent == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.view.View");
        }
        ((View) parent).setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.controls.ui.DetailDialog$$special$$inlined$apply$lambda$4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.this$0.dismiss();
            }
        });
        if (ScreenDecorationsUtils.supportsRoundedCornersOnWindows(getContext().getResources())) {
            Context context = getContext();
            Intrinsics.checkExpressionValueIsNotNull(context, "context");
            this.activityView.setCornerRadius(context.getResources().getDimensionPixelSize(R.dimen.controls_activity_view_corner_radius));
        }
    }

    @NotNull
    public final ActivityView getActivityView() {
        return this.activityView;
    }

    @Override // android.app.Dialog
    public void show() {
        this.activityView.setCallback(this.stateCallback);
        super.show();
    }

    @Override // android.app.Dialog, android.content.DialogInterface
    public void dismiss() {
        if (isShowing()) {
            this.activityView.release();
            super.dismiss();
        }
    }
}
