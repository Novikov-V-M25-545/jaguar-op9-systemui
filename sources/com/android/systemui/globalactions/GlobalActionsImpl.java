package com.android.systemui.globalactions;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.internal.colorextraction.drawable.ScrimDrawable;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.settingslib.Utils;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.plugins.GlobalActions;
import com.android.systemui.plugins.GlobalActionsPanelPlugin;
import com.android.systemui.statusbar.BlurUtils;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.ExtensionController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import dagger.Lazy;

/* loaded from: classes.dex */
public class GlobalActionsImpl implements GlobalActions, CommandQueue.Callbacks {
    private final BlurUtils mBlurUtils;
    private final CommandQueue mCommandQueue;
    private final Context mContext;
    private boolean mDisabled;
    private GlobalActionsDialog mGlobalActionsDialog;
    private final Lazy<GlobalActionsDialog> mGlobalActionsDialogLazy;
    private final ExtensionController.Extension<GlobalActionsPanelPlugin> mWalletPluginProvider;
    private final KeyguardStateController mKeyguardStateController = (KeyguardStateController) Dependency.get(KeyguardStateController.class);
    private final DeviceProvisionedController mDeviceProvisionedController = (DeviceProvisionedController) Dependency.get(DeviceProvisionedController.class);

    public GlobalActionsImpl(Context context, CommandQueue commandQueue, Lazy<GlobalActionsDialog> lazy, BlurUtils blurUtils) {
        this.mContext = context;
        this.mGlobalActionsDialogLazy = lazy;
        this.mCommandQueue = commandQueue;
        this.mBlurUtils = blurUtils;
        commandQueue.addCallback((CommandQueue.Callbacks) this);
        this.mWalletPluginProvider = ((ExtensionController) Dependency.get(ExtensionController.class)).newExtension(GlobalActionsPanelPlugin.class).withPlugin(GlobalActionsPanelPlugin.class).build();
    }

    @Override // com.android.systemui.plugins.GlobalActions
    public void destroy() {
        this.mCommandQueue.removeCallback((CommandQueue.Callbacks) this);
        GlobalActionsDialog globalActionsDialog = this.mGlobalActionsDialog;
        if (globalActionsDialog != null) {
            globalActionsDialog.destroy();
            this.mGlobalActionsDialog = null;
        }
    }

    @Override // com.android.systemui.plugins.GlobalActions
    public void showGlobalActions(GlobalActions.GlobalActionsManager globalActionsManager) throws Resources.NotFoundException {
        if (this.mDisabled) {
            return;
        }
        GlobalActionsDialog globalActionsDialog = this.mGlobalActionsDialogLazy.get();
        this.mGlobalActionsDialog = globalActionsDialog;
        globalActionsDialog.showOrHideDialog(this.mKeyguardStateController.isShowing(), this.mDeviceProvisionedController.isDeviceProvisioned(), this.mWalletPluginProvider.get());
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).requestFaceAuth();
    }

    @Override // com.android.systemui.plugins.GlobalActions
    public void showShutdownUi(boolean z, String str, boolean z2) {
        final Drawable scrimDrawable = new ScrimDrawable();
        final Dialog dialog = new Dialog(this.mContext, R.style.Theme_SystemUI_Dialog_GlobalActions);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() { // from class: com.android.systemui.globalactions.GlobalActionsImpl$$ExternalSyntheticLambda0
            @Override // android.content.DialogInterface.OnShowListener
            public final void onShow(DialogInterface dialogInterface) {
                this.f$0.lambda$showShutdownUi$0(scrimDrawable, dialog, dialogInterface);
            }
        });
        Window window = dialog.getWindow();
        window.requestFeature(1);
        window.getAttributes().systemUiVisibility |= 1792;
        window.getDecorView();
        window.getAttributes().width = -1;
        window.getAttributes().height = -1;
        window.getAttributes().layoutInDisplayCutoutMode = 3;
        window.setType(2020);
        window.getAttributes().setFitInsetsTypes(0);
        window.clearFlags(2);
        window.addFlags(17629472);
        window.setBackgroundDrawable(scrimDrawable);
        window.setWindowAnimations(R.style.Animation_ShutdownUi);
        dialog.setContentView(android.R.layout.resolver_list);
        dialog.setCancelable(false);
        int colorAttrDefaultColor = Utils.getColorAttrDefaultColor(this.mContext, R.attr.wallpaperTextColor);
        ((ProgressBar) dialog.findViewById(android.R.id.progress)).getIndeterminateDrawable().setTint(colorAttrDefaultColor);
        TextView textView = (TextView) dialog.findViewById(android.R.id.text1);
        TextView textView2 = (TextView) dialog.findViewById(android.R.id.text2);
        textView.setTextColor(colorAttrDefaultColor);
        textView2.setTextColor(colorAttrDefaultColor);
        textView2.setText(getRebootMessage(z, str, z2));
        String reasonMessage = getReasonMessage(str, z2);
        if (reasonMessage != null) {
            textView.setVisibility(0);
            textView.setText(reasonMessage);
        }
        dialog.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showShutdownUi$0(ScrimDrawable scrimDrawable, Dialog dialog, DialogInterface dialogInterface) {
        if (this.mBlurUtils.supportsBlursOnWindows()) {
            scrimDrawable.setAlpha(137);
            this.mBlurUtils.applyBlur(dialog.getWindow().getDecorView().getViewRootImpl(), this.mBlurUtils.blurRadiusOfRatio(1.0f));
        } else {
            scrimDrawable.setAlpha((int) (this.mContext.getResources().getFloat(R.dimen.shutdown_scrim_behind_alpha) * 255.0f));
        }
    }

    private int getRebootMessage(boolean z, String str, boolean z2) {
        if (str != null && str.startsWith("recovery-update")) {
            return android.R.string.permdesc_manageNetworkPolicy;
        }
        if (str != null && !z2 && str.equals("recovery")) {
            return android.R.string.permdesc_install_shortcut;
        }
        if (str != null && str.equals("recovery")) {
            return R.string.global_action_restart_recovery_progress;
        }
        if (str != null && str.equals("bootloader")) {
            return R.string.global_action_restart_bootloader_progress;
        }
        if (str != null && str.equals("download")) {
            return R.string.global_action_restart_download_progress;
        }
        if (str != null && str.equals("fastboot")) {
            return R.string.global_action_restart_fastboot_progress;
        }
        if (str == null || !str.equals("systemui")) {
            return z ? android.R.string.permdesc_install_shortcut : android.R.string.permlab_bindCarrierServices;
        }
        return R.string.global_action_restart_systemui_progress;
    }

    private String getReasonMessage(String str, boolean z) {
        if (str != null && str.startsWith("recovery-update")) {
            return this.mContext.getString(android.R.string.permdesc_manageOngoingCalls);
        }
        if (str == null || !str.equals("recovery") || z) {
            return null;
        }
        return this.mContext.getString(android.R.string.permdesc_invokeCarrierSetup);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void disable(int i, int i2, int i3, boolean z) {
        GlobalActionsDialog globalActionsDialog;
        boolean z2 = (i3 & 8) != 0;
        if (i != this.mContext.getDisplayId() || z2 == this.mDisabled) {
            return;
        }
        this.mDisabled = z2;
        if (!z2 || (globalActionsDialog = this.mGlobalActionsDialog) == null) {
            return;
        }
        globalActionsDialog.dismissDialog();
    }
}
