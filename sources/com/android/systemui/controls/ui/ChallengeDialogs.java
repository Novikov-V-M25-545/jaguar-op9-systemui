package com.android.systemui.controls.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.service.controls.actions.BooleanAction;
import android.service.controls.actions.CommandAction;
import android.service.controls.actions.ControlAction;
import android.service.controls.actions.FloatAction;
import android.service.controls.actions.ModeAction;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import com.android.systemui.R;
import kotlin.Pair;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ChallengeDialogs.kt */
/* loaded from: classes.dex */
public final class ChallengeDialogs {
    public static final ChallengeDialogs INSTANCE = new ChallengeDialogs();

    private ChallengeDialogs() {
    }

    @Nullable
    public final Dialog createPinDialog(@NotNull final ControlViewHolder cvh, final boolean z, boolean z2, @NotNull final Function0<Unit> onCancel) {
        Pair pair;
        Intrinsics.checkParameterIsNotNull(cvh, "cvh");
        Intrinsics.checkParameterIsNotNull(onCancel, "onCancel");
        final ControlAction lastAction = cvh.getLastAction();
        if (lastAction == null) {
            Log.e("ControlsUiController", "PIN Dialog attempted but no last action is set. Will not show");
            return null;
        }
        Resources resources = cvh.getContext().getResources();
        if (z2) {
            pair = new Pair(resources.getString(R.string.controls_pin_wrong), Integer.valueOf(R.string.controls_pin_instructions_retry));
        } else {
            pair = new Pair(resources.getString(R.string.controls_pin_verify, cvh.getTitle().getText()), Integer.valueOf(R.string.controls_pin_instructions));
        }
        final String str = (String) pair.component1();
        final int iIntValue = ((Number) pair.component2()).intValue();
        AlertDialog.Builder builder = new AlertDialog.Builder(cvh.getContext(), android.R.style.Theme.DeviceDefault.Dialog.Alert);
        builder.setTitle(str);
        builder.setView(R.layout.controls_dialog_pin);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() { // from class: com.android.systemui.controls.ui.ChallengeDialogs$createPinDialog$$inlined$apply$lambda$1
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface instanceof Dialog) {
                    Dialog dialog = (Dialog) dialogInterface;
                    int i2 = R.id.controls_pin_input;
                    dialog.requireViewById(i2);
                    cvh.action(ChallengeDialogs.INSTANCE.addChallengeValue(lastAction, ((EditText) dialog.requireViewById(i2)).getText().toString()));
                    dialogInterface.dismiss();
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() { // from class: com.android.systemui.controls.ui.ChallengeDialogs$createPinDialog$$inlined$apply$lambda$2
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                onCancel.invoke();
                dialogInterface.cancel();
            }
        });
        final AlertDialog alertDialogCreate = builder.create();
        Window window = alertDialogCreate.getWindow();
        window.setType(2020);
        window.setSoftInputMode(4);
        alertDialogCreate.setOnShowListener(new DialogInterface.OnShowListener() { // from class: com.android.systemui.controls.ui.ChallengeDialogs$createPinDialog$$inlined$apply$lambda$3
            @Override // android.content.DialogInterface.OnShowListener
            public final void onShow(DialogInterface dialogInterface) {
                final EditText editText = (EditText) alertDialogCreate.requireViewById(R.id.controls_pin_input);
                editText.setHint(iIntValue);
                AlertDialog alertDialog = alertDialogCreate;
                int i = R.id.controls_pin_use_alpha;
                final CheckBox checkBox = (CheckBox) alertDialog.requireViewById(i);
                checkBox.setChecked(z);
                ChallengeDialogs challengeDialogs = ChallengeDialogs.INSTANCE;
                Intrinsics.checkExpressionValueIsNotNull(editText, "editText");
                challengeDialogs.setInputType(editText, checkBox.isChecked());
                ((CheckBox) alertDialogCreate.requireViewById(i)).setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.controls.ui.ChallengeDialogs$createPinDialog$$inlined$apply$lambda$3.1
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        ChallengeDialogs challengeDialogs2 = ChallengeDialogs.INSTANCE;
                        EditText editText2 = editText;
                        Intrinsics.checkExpressionValueIsNotNull(editText2, "editText");
                        challengeDialogs2.setInputType(editText2, checkBox.isChecked());
                    }
                });
                editText.requestFocus();
            }
        });
        return alertDialogCreate;
    }

    @Nullable
    public final Dialog createConfirmationDialog(@NotNull final ControlViewHolder cvh, @NotNull final Function0<Unit> onCancel) {
        Intrinsics.checkParameterIsNotNull(cvh, "cvh");
        Intrinsics.checkParameterIsNotNull(onCancel, "onCancel");
        final ControlAction lastAction = cvh.getLastAction();
        if (lastAction == null) {
            Log.e("ControlsUiController", "Confirmation Dialog attempted but no last action is set. Will not show");
            return null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(cvh.getContext(), android.R.style.Theme.DeviceDefault.Dialog.Alert);
        builder.setTitle(cvh.getContext().getResources().getString(R.string.controls_confirmation_message, cvh.getTitle().getText()));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() { // from class: com.android.systemui.controls.ui.ChallengeDialogs$createConfirmationDialog$$inlined$apply$lambda$1
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                cvh.action(ChallengeDialogs.INSTANCE.addChallengeValue(lastAction, "true"));
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() { // from class: com.android.systemui.controls.ui.ChallengeDialogs$createConfirmationDialog$$inlined$apply$lambda$2
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                onCancel.invoke();
                dialogInterface.cancel();
            }
        });
        AlertDialog alertDialogCreate = builder.create();
        alertDialogCreate.getWindow().setType(2020);
        return alertDialogCreate;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void setInputType(EditText editText, boolean z) {
        if (z) {
            editText.setInputType(129);
        } else {
            editText.setInputType(18);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final ControlAction addChallengeValue(ControlAction controlAction, String str) {
        String templateId = controlAction.getTemplateId();
        if (controlAction instanceof BooleanAction) {
            return new BooleanAction(templateId, ((BooleanAction) controlAction).getNewState(), str);
        }
        if (controlAction instanceof FloatAction) {
            return new FloatAction(templateId, ((FloatAction) controlAction).getNewValue(), str);
        }
        if (controlAction instanceof CommandAction) {
            return new CommandAction(templateId, str);
        }
        if (controlAction instanceof ModeAction) {
            return new ModeAction(templateId, ((ModeAction) controlAction).getNewMode(), str);
        }
        throw new IllegalStateException("'action' is not a known type: " + controlAction);
    }
}
