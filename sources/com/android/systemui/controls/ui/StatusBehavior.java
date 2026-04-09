package com.android.systemui.controls.ui;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.service.controls.Control;
import android.view.View;
import com.android.systemui.R;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: StatusBehavior.kt */
/* loaded from: classes.dex */
public final class StatusBehavior implements Behavior {

    @NotNull
    public ControlViewHolder cvh;

    @NotNull
    public final ControlViewHolder getCvh() {
        ControlViewHolder controlViewHolder = this.cvh;
        if (controlViewHolder == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
        }
        return controlViewHolder;
    }

    @Override // com.android.systemui.controls.ui.Behavior
    public void initialize(@NotNull ControlViewHolder cvh) {
        Intrinsics.checkParameterIsNotNull(cvh, "cvh");
        this.cvh = cvh;
    }

    @Override // com.android.systemui.controls.ui.Behavior
    public void bind(@NotNull final ControlWithState cws, int i) throws Resources.NotFoundException {
        int i2;
        Intrinsics.checkParameterIsNotNull(cws, "cws");
        Control control = cws.getControl();
        int status = control != null ? control.getStatus() : 0;
        if (status == 2) {
            ControlViewHolder controlViewHolder = this.cvh;
            if (controlViewHolder == null) {
                Intrinsics.throwUninitializedPropertyAccessException("cvh");
            }
            controlViewHolder.getLayout().setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.controls.ui.StatusBehavior$bind$msg$1
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    StatusBehavior statusBehavior = this.this$0;
                    statusBehavior.showNotFoundDialog(statusBehavior.getCvh(), cws);
                }
            });
            ControlViewHolder controlViewHolder2 = this.cvh;
            if (controlViewHolder2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("cvh");
            }
            controlViewHolder2.getLayout().setOnLongClickListener(new View.OnLongClickListener() { // from class: com.android.systemui.controls.ui.StatusBehavior$bind$msg$2
                @Override // android.view.View.OnLongClickListener
                public final boolean onLongClick(View view) {
                    StatusBehavior statusBehavior = this.this$0;
                    statusBehavior.showNotFoundDialog(statusBehavior.getCvh(), cws);
                    return true;
                }
            });
            i2 = R.string.controls_error_removed;
        } else if (status == 3) {
            i2 = R.string.controls_error_generic;
        } else if (status == 4) {
            i2 = R.string.controls_error_timeout;
        } else {
            ControlViewHolder controlViewHolder3 = this.cvh;
            if (controlViewHolder3 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("cvh");
            }
            controlViewHolder3.setLoading(true);
            i2 = android.R.string.global_action_screenshot;
        }
        ControlViewHolder controlViewHolder4 = this.cvh;
        if (controlViewHolder4 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
        }
        ControlViewHolder controlViewHolder5 = this.cvh;
        if (controlViewHolder5 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
        }
        String string = controlViewHolder5.getContext().getString(i2);
        Intrinsics.checkExpressionValueIsNotNull(string, "cvh.context.getString(msg)");
        ControlViewHolder.setStatusText$default(controlViewHolder4, string, false, 2, null);
        ControlViewHolder controlViewHolder6 = this.cvh;
        if (controlViewHolder6 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
        }
        ControlViewHolder.applyRenderInfo$frameworks__base__packages__SystemUI__android_common__SystemUI_core$default(controlViewHolder6, false, i, false, 4, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void showNotFoundDialog(final ControlViewHolder controlViewHolder, final ControlWithState controlWithState) {
        PackageManager packageManager = controlViewHolder.getContext().getPackageManager();
        final CharSequence applicationLabel = packageManager.getApplicationLabel(packageManager.getApplicationInfo(controlWithState.getComponentName().getPackageName(), 128));
        final AlertDialog.Builder builder = new AlertDialog.Builder(controlViewHolder.getContext(), android.R.style.Theme.DeviceDefault.Dialog.Alert);
        Resources resources = controlViewHolder.getContext().getResources();
        builder.setTitle(resources.getString(R.string.controls_error_removed_title));
        builder.setMessage(resources.getString(R.string.controls_error_removed_message, controlViewHolder.getTitle().getText(), applicationLabel));
        builder.setPositiveButton(R.string.controls_open_app, new DialogInterface.OnClickListener() { // from class: com.android.systemui.controls.ui.StatusBehavior$showNotFoundDialog$$inlined$apply$lambda$1
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) throws Resources.NotFoundException, PendingIntent.CanceledException {
                PendingIntent appIntent;
                try {
                    Control control = controlWithState.getControl();
                    if (control != null && (appIntent = control.getAppIntent()) != null) {
                        appIntent.send();
                    }
                    builder.getContext().sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
                } catch (PendingIntent.CanceledException unused) {
                    controlViewHolder.setErrorStatus();
                }
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() { // from class: com.android.systemui.controls.ui.StatusBehavior$showNotFoundDialog$builder$1$2
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog alertDialogCreate = builder.create();
        alertDialogCreate.getWindow().setType(2020);
        alertDialogCreate.show();
        controlViewHolder.setVisibleDialog(alertDialogCreate);
    }
}
