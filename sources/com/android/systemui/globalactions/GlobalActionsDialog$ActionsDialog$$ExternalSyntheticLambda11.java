package com.android.systemui.globalactions;

import com.android.systemui.globalactions.GlobalActionsDialog;

/* loaded from: classes.dex */
public final /* synthetic */ class GlobalActionsDialog$ActionsDialog$$ExternalSyntheticLambda11 implements Runnable {
    public final /* synthetic */ GlobalActionsDialog.ActionsDialog f$0;

    public /* synthetic */ GlobalActionsDialog$ActionsDialog$$ExternalSyntheticLambda11(GlobalActionsDialog.ActionsDialog actionsDialog) {
        this.f$0 = actionsDialog;
    }

    @Override // java.lang.Runnable
    public final void run() {
        this.f$0.dismissForControlsActivity();
    }
}
