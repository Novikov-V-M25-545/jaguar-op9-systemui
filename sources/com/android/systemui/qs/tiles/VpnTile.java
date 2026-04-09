package com.android.systemui.qs.tiles;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.android.internal.net.VpnConfig;
import com.android.internal.net.VpnProfile;
import com.android.systemui.R;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.SecurityController;
import java.util.List;

/* loaded from: classes.dex */
public class VpnTile extends QSTileImpl<QSTile.BooleanState> {
    private final ActivityStarter mActivityStarter;
    private final Callback mCallback;
    private final SecurityController mController;
    private final KeyguardStateController mKeyguard;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 100;
    }

    public VpnTile(QSHost qSHost, SecurityController securityController, KeyguardStateController keyguardStateController, ActivityStarter activityStarter) {
        super(qSHost);
        this.mCallback = new Callback();
        this.mController = securityController;
        this.mKeyguard = keyguardStateController;
        this.mActivityStarter = activityStarter;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        if (QSTileImpl.DEBUG) {
            Log.d(this.TAG, "handleSetListening " + z);
        }
        if (z) {
            this.mController.addCallback(this.mCallback);
            this.mKeyguard.addCallback(this.mCallback);
        } else {
            this.mController.removeCallback(this.mCallback);
            this.mKeyguard.removeCallback(this.mCallback);
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleUserSwitch(int i) {
        super.handleUserSwitch(i);
        this.mController.onUserSwitched(i);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.settings.VPN_SETTINGS");
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleSecondaryClick() {
        handleClick();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleClick() {
        if (this.mKeyguard.isMethodSecure() && !this.mKeyguard.canDismissLockScreen()) {
            this.mActivityStarter.postQSRunnableDismissingKeyguard(new Runnable() { // from class: com.android.systemui.qs.tiles.VpnTile$$ExternalSyntheticLambda4
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$handleClick$0();
                }
            });
        } else {
            lambda$handleClick$0();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: showConnectDialogOrDisconnect, reason: merged with bridge method [inline-methods] */
    public void lambda$handleClick$0() {
        if (this.mController.isVpnRestricted()) {
            return;
        }
        if (this.mController.isVpnEnabled()) {
            this.mController.disconnectPrimaryVpn();
            return;
        }
        final List<VpnProfile> configuredLegacyVpns = this.mController.getConfiguredLegacyVpns();
        final List<String> vpnAppPackageNames = this.mController.getVpnAppPackageNames();
        if (configuredLegacyVpns.isEmpty() && vpnAppPackageNames.isEmpty()) {
            return;
        }
        if (configuredLegacyVpns.isEmpty() && vpnAppPackageNames.size() == 1) {
            this.mController.launchVpnApp(vpnAppPackageNames.get(0));
        } else if (vpnAppPackageNames.isEmpty() && configuredLegacyVpns.size() == 1) {
            connectVpnOrAskForCredentials(configuredLegacyVpns.get(0));
        } else {
            this.mUiHandler.post(new Runnable() { // from class: com.android.systemui.qs.tiles.VpnTile$$ExternalSyntheticLambda5
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$showConnectDialogOrDisconnect$2(configuredLegacyVpns, vpnAppPackageNames);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showConnectDialogOrDisconnect$2(final List list, final List list2) {
        CharSequence[] charSequenceArr = new CharSequence[list.size() + list2.size()];
        final int size = list.size();
        for (int i = 0; i < size; i++) {
            charSequenceArr[i] = ((VpnProfile) list.get(i)).name;
        }
        for (int i2 = 0; i2 < list2.size(); i2++) {
            int i3 = size + i2;
            try {
                charSequenceArr[i3] = VpnConfig.getVpnLabel(this.mContext, (String) list2.get(i2));
            } catch (PackageManager.NameNotFoundException unused) {
                charSequenceArr[i3] = (CharSequence) list2.get(i2);
            }
        }
        prepareAndShowDialog(new AlertDialog.Builder(this.mContext).setTitle(R.string.quick_settings_vpn_connect_dialog_title).setItems(charSequenceArr, new DialogInterface.OnClickListener() { // from class: com.android.systemui.qs.tiles.VpnTile$$ExternalSyntheticLambda0
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i4) {
                this.f$0.lambda$showConnectDialogOrDisconnect$1(size, list, list2, dialogInterface, i4);
            }
        }).setNegativeButton(android.R.string.cancel, (DialogInterface.OnClickListener) null).create());
        this.mHost.collapsePanels();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showConnectDialogOrDisconnect$1(int i, List list, List list2, DialogInterface dialogInterface, int i2) {
        if (i2 < i) {
            connectVpnOrAskForCredentials((VpnProfile) list.get(i2));
        } else {
            this.mController.launchVpnApp((String) list2.get(i2 - i));
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_vpn_label);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.label = this.mContext.getString(R.string.quick_settings_vpn_label);
        booleanState.value = this.mController.isVpnEnabled();
        booleanState.secondaryLabel = this.mController.getPrimaryVpnName();
        booleanState.contentDescription = booleanState.label;
        booleanState.icon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_vpn);
        boolean z = this.mController.getConfiguredLegacyVpns().size() > 0 || this.mController.getVpnAppPackageNames().size() > 0;
        if (this.mController.isVpnRestricted() || !z) {
            booleanState.state = 0;
        } else if (booleanState.value) {
            booleanState.state = 2;
        } else {
            booleanState.state = 1;
        }
    }

    private void connectVpnOrAskForCredentials(final VpnProfile vpnProfile) {
        if (vpnProfile.saveLogin) {
            this.mController.connectLegacyVpn(vpnProfile);
            return;
        }
        View viewInflate = LayoutInflater.from(this.mContext).inflate(R.layout.vpn_credentials_dialog, (ViewGroup) null);
        final EditText editText = (EditText) viewInflate.findViewById(R.id.username);
        final EditText editText2 = (EditText) viewInflate.findViewById(R.id.password);
        ((TextView) viewInflate.findViewById(R.id.hint)).setText(this.mContext.getString(R.string.vpn_credentials_hint, vpnProfile.name));
        final AlertDialog alertDialogCreate = new AlertDialog.Builder(this.mContext).setView(viewInflate).setPositiveButton(R.string.vpn_credentials_dialog_connect, new DialogInterface.OnClickListener() { // from class: com.android.systemui.qs.tiles.VpnTile$$ExternalSyntheticLambda1
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                this.f$0.lambda$connectVpnOrAskForCredentials$3(vpnProfile, editText, editText2, dialogInterface, i);
            }
        }).setNegativeButton(android.R.string.cancel, (DialogInterface.OnClickListener) null).create();
        prepareAndShowDialog(alertDialogCreate);
        this.mHost.collapsePanels();
        this.mUiHandler.post(new Runnable() { // from class: com.android.systemui.qs.tiles.VpnTile$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                VpnTile.lambda$connectVpnOrAskForCredentials$4(editText, editText2, alertDialogCreate);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$connectVpnOrAskForCredentials$3(VpnProfile vpnProfile, EditText editText, EditText editText2, DialogInterface dialogInterface, int i) {
        vpnProfile.username = editText.getText().toString();
        vpnProfile.password = editText2.getText().toString();
        this.mController.connectLegacyVpn(vpnProfile);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$connectVpnOrAskForCredentials$4(EditText editText, EditText editText2, AlertDialog alertDialog) {
        UsernameAndPasswordWatcher usernameAndPasswordWatcher = new UsernameAndPasswordWatcher(editText, editText2, alertDialog.getButton(-1));
        editText.addTextChangedListener(usernameAndPasswordWatcher);
        editText2.addTextChangedListener(usernameAndPasswordWatcher);
    }

    private void prepareAndShowDialog(final Dialog dialog) {
        dialog.getWindow().setType(2009);
        SystemUIDialog.setShowForAllUsers(dialog, true);
        SystemUIDialog.registerDismissListener(dialog);
        SystemUIDialog.setWindowOnTop(dialog);
        this.mUiHandler.post(new Runnable() { // from class: com.android.systemui.qs.tiles.VpnTile$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                dialog.show();
            }
        });
    }

    private static final class UsernameAndPasswordWatcher implements TextWatcher {
        private final Button mOkButton;
        private final EditText mPasswordEditor;
        private final EditText mUserNameEditor;

        @Override // android.text.TextWatcher
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // android.text.TextWatcher
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        public UsernameAndPasswordWatcher(EditText editText, EditText editText2, Button button) {
            this.mUserNameEditor = editText;
            this.mPasswordEditor = editText2;
            this.mOkButton = button;
            updateOkButtonState();
        }

        @Override // android.text.TextWatcher
        public void afterTextChanged(Editable editable) {
            updateOkButtonState();
        }

        private void updateOkButtonState() {
            this.mOkButton.setEnabled(this.mUserNameEditor.getText().length() > 0 && this.mPasswordEditor.getText().length() > 0);
        }
    }

    private final class Callback implements SecurityController.SecurityControllerCallback, KeyguardStateController.Callback {
        private Callback() {
        }

        @Override // com.android.systemui.statusbar.policy.SecurityController.SecurityControllerCallback
        public void onStateChanged() {
            VpnTile.this.refreshState();
        }

        @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
        public void onKeyguardShowingChanged() {
            VpnTile.this.refreshState();
        }
    }
}
