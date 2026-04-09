package com.android.systemui.controls.management;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.service.controls.Control;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controls.ControlsServiceInfo;
import com.android.systemui.controls.controller.ControlInfo;
import com.android.systemui.controls.controller.ControlsController;
import com.android.systemui.controls.controller.StructureInfo;
import com.android.systemui.controls.management.ControlsListingController;
import com.android.systemui.controls.ui.RenderInfo;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.util.LifecycleActivity;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlsRequestDialog.kt */
/* loaded from: classes.dex */
public class ControlsRequestDialog extends LifecycleActivity implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
    public static final Companion Companion = new Companion(null);
    private final BroadcastDispatcher broadcastDispatcher;
    private final ControlsRequestDialog$callback$1 callback;
    private Control control;
    private ComponentName controlComponent;
    private final ControlsController controller;
    private final ControlsListingController controlsListingController;
    private final ControlsRequestDialog$currentUserTracker$1 currentUserTracker;
    private Dialog dialog;

    /* JADX WARN: Type inference failed for: r2v1, types: [com.android.systemui.controls.management.ControlsRequestDialog$callback$1] */
    /* JADX WARN: Type inference failed for: r2v2, types: [com.android.systemui.controls.management.ControlsRequestDialog$currentUserTracker$1] */
    public ControlsRequestDialog(@NotNull ControlsController controller, @NotNull final BroadcastDispatcher broadcastDispatcher, @NotNull ControlsListingController controlsListingController) {
        Intrinsics.checkParameterIsNotNull(controller, "controller");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(controlsListingController, "controlsListingController");
        this.controller = controller;
        this.broadcastDispatcher = broadcastDispatcher;
        this.controlsListingController = controlsListingController;
        this.callback = new ControlsListingController.ControlsListingCallback() { // from class: com.android.systemui.controls.management.ControlsRequestDialog$callback$1
            @Override // com.android.systemui.controls.management.ControlsListingController.ControlsListingCallback
            public void onServicesUpdated(@NotNull List<ControlsServiceInfo> serviceInfos) {
                Intrinsics.checkParameterIsNotNull(serviceInfos, "serviceInfos");
            }
        };
        this.currentUserTracker = new CurrentUserTracker(broadcastDispatcher) { // from class: com.android.systemui.controls.management.ControlsRequestDialog$currentUserTracker$1
            private final int startingUser;

            {
                this.startingUser = this.this$0.controller.getCurrentUserId();
            }

            @Override // com.android.systemui.settings.CurrentUserTracker
            public void onUserSwitched(int i) {
                if (i != this.startingUser) {
                    stopTracking();
                    this.this$0.finish();
                }
            }
        };
    }

    /* compiled from: ControlsRequestDialog.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    protected void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        if (!this.controller.getAvailable()) {
            Log.w("ControlsRequestDialog", "Quick Controls not available for this user ");
            finish();
        }
        startTracking();
        this.controlsListingController.addCallback(this.callback);
        int intExtra = getIntent().getIntExtra("android.intent.extra.USER_ID", -10000);
        int currentUserId = this.controller.getCurrentUserId();
        if (intExtra != currentUserId) {
            Log.w("ControlsRequestDialog", "Current user (" + currentUserId + ") different from request user (" + intExtra + ')');
            finish();
        }
        ComponentName componentName = (ComponentName) getIntent().getParcelableExtra("android.intent.extra.COMPONENT_NAME");
        if (componentName == null) {
            Log.e("ControlsRequestDialog", "Request did not contain componentName");
            finish();
            return;
        }
        this.controlComponent = componentName;
        Control control = (Control) getIntent().getParcelableExtra("android.service.controls.extra.CONTROL");
        if (control != null) {
            this.control = control;
        } else {
            Log.e("ControlsRequestDialog", "Request did not contain control");
            finish();
        }
    }

    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        CharSequence charSequenceVerifyComponentAndGetLabel = verifyComponentAndGetLabel();
        if (charSequenceVerifyComponentAndGetLabel == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("The component specified (");
            ComponentName componentName = this.controlComponent;
            if (componentName == null) {
                Intrinsics.throwUninitializedPropertyAccessException("controlComponent");
            }
            sb.append(componentName.flattenToString());
            sb.append(' ');
            sb.append("is not a valid ControlsProviderService");
            Log.e("ControlsRequestDialog", sb.toString());
            finish();
            return;
        }
        if (isCurrentFavorite()) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("The control ");
            Control control = this.control;
            if (control == null) {
                Intrinsics.throwUninitializedPropertyAccessException("control");
            }
            sb2.append(control.getTitle());
            sb2.append(" is already a favorite");
            Log.w("ControlsRequestDialog", sb2.toString());
            finish();
        }
        Dialog dialogCreateDialog = createDialog(charSequenceVerifyComponentAndGetLabel);
        this.dialog = dialogCreateDialog;
        if (dialogCreateDialog != null) {
            dialogCreateDialog.show();
        }
    }

    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    protected void onDestroy() {
        Dialog dialog = this.dialog;
        if (dialog != null) {
            dialog.dismiss();
        }
        stopTracking();
        this.controlsListingController.removeCallback(this.callback);
        super.onDestroy();
    }

    private final CharSequence verifyComponentAndGetLabel() {
        ControlsListingController controlsListingController = this.controlsListingController;
        ComponentName componentName = this.controlComponent;
        if (componentName == null) {
            Intrinsics.throwUninitializedPropertyAccessException("controlComponent");
        }
        return controlsListingController.getAppLabel(componentName);
    }

    private final boolean isCurrentFavorite() {
        boolean z;
        ControlsController controlsController = this.controller;
        ComponentName componentName = this.controlComponent;
        if (componentName == null) {
            Intrinsics.throwUninitializedPropertyAccessException("controlComponent");
        }
        List<StructureInfo> favoritesForComponent = controlsController.getFavoritesForComponent(componentName);
        if (!(favoritesForComponent instanceof Collection) || !favoritesForComponent.isEmpty()) {
            Iterator<T> it = favoritesForComponent.iterator();
            while (it.hasNext()) {
                List<ControlInfo> controls = ((StructureInfo) it.next()).getControls();
                if ((controls instanceof Collection) && controls.isEmpty()) {
                    z = false;
                } else {
                    Iterator<T> it2 = controls.iterator();
                    while (it2.hasNext()) {
                        String controlId = ((ControlInfo) it2.next()).getControlId();
                        Control control = this.control;
                        if (control == null) {
                            Intrinsics.throwUninitializedPropertyAccessException("control");
                        }
                        if (Intrinsics.areEqual(controlId, control.getControlId())) {
                            z = true;
                            break;
                        }
                    }
                    z = false;
                }
                if (z) {
                    return true;
                }
            }
        }
        return false;
    }

    @NotNull
    public final Dialog createDialog(@NotNull CharSequence label) {
        Intrinsics.checkParameterIsNotNull(label, "label");
        RenderInfo.Companion companion = RenderInfo.Companion;
        ComponentName componentName = this.controlComponent;
        if (componentName == null) {
            Intrinsics.throwUninitializedPropertyAccessException("controlComponent");
        }
        Control control = this.control;
        if (control == null) {
            Intrinsics.throwUninitializedPropertyAccessException("control");
        }
        RenderInfo renderInfoLookup$default = RenderInfo.Companion.lookup$default(companion, this, componentName, control.getDeviceType(), 0, 8, null);
        View viewInflate = LayoutInflater.from(this).inflate(R.layout.controls_dialog, (ViewGroup) null);
        ImageView imageView = (ImageView) viewInflate.requireViewById(R.id.icon);
        imageView.setImageDrawable(renderInfoLookup$default.getIcon());
        Context context = imageView.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context, "context");
        Resources resources = context.getResources();
        int foreground = renderInfoLookup$default.getForeground();
        Context context2 = imageView.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context2, "context");
        imageView.setImageTintList(resources.getColorStateList(foreground, context2.getTheme()));
        View viewRequireViewById = viewInflate.requireViewById(R.id.title);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById, "requireViewById<TextView>(R.id.title)");
        TextView textView = (TextView) viewRequireViewById;
        Control control2 = this.control;
        if (control2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("control");
        }
        textView.setText(control2.getTitle());
        View viewRequireViewById2 = viewInflate.requireViewById(R.id.subtitle);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById2, "requireViewById<TextView>(R.id.subtitle)");
        TextView textView2 = (TextView) viewRequireViewById2;
        Control control3 = this.control;
        if (control3 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("control");
        }
        textView2.setText(control3.getSubtitle());
        View viewRequireViewById3 = viewInflate.requireViewById(R.id.control);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById3, "requireViewById<View>(R.id.control)");
        viewRequireViewById3.setElevation(viewInflate.getResources().getFloat(R.dimen.control_card_elevation));
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(getString(R.string.controls_dialog_title)).setMessage(getString(R.string.controls_dialog_message, new Object[]{label})).setPositiveButton(R.string.controls_dialog_ok, this).setNegativeButton(android.R.string.cancel, this).setOnCancelListener(this).setView(viewInflate).create();
        SystemUIDialog.registerDismissListener(dialog);
        dialog.setCanceledOnTouchOutside(true);
        Intrinsics.checkExpressionValueIsNotNull(dialog, "dialog");
        return dialog;
    }

    @Override // android.content.DialogInterface.OnCancelListener
    public void onCancel(@Nullable DialogInterface dialogInterface) {
        finish();
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(@Nullable DialogInterface dialogInterface, int i) {
        if (i == -1) {
            ControlsController controlsController = this.controller;
            ComponentName componentName = this.controlComponent;
            if (componentName == null) {
                Intrinsics.throwUninitializedPropertyAccessException("controlComponent");
            }
            Control control = this.control;
            if (control == null) {
                Intrinsics.throwUninitializedPropertyAccessException("control");
            }
            CharSequence structure = control.getStructure();
            if (structure == null) {
                structure = "";
            }
            Control control2 = this.control;
            if (control2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("control");
            }
            String controlId = control2.getControlId();
            Intrinsics.checkExpressionValueIsNotNull(controlId, "control.controlId");
            Control control3 = this.control;
            if (control3 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("control");
            }
            CharSequence title = control3.getTitle();
            Intrinsics.checkExpressionValueIsNotNull(title, "control.title");
            Control control4 = this.control;
            if (control4 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("control");
            }
            CharSequence subtitle = control4.getSubtitle();
            Intrinsics.checkExpressionValueIsNotNull(subtitle, "control.subtitle");
            Control control5 = this.control;
            if (control5 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("control");
            }
            controlsController.addFavorite(componentName, structure, new ControlInfo(controlId, title, subtitle, control5.getDeviceType()));
        }
        finish();
    }
}
