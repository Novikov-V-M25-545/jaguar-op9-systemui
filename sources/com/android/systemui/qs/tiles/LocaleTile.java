package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.LocaleList;
import android.widget.Toast;
import com.android.internal.app.LocalePicker;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import java.util.Locale;

/* loaded from: classes.dex */
public class LocaleTile extends QSTileImpl<QSTile.BooleanState> {
    private Runnable applyLocale;
    private Locale currentLocaleBackup;
    private LocaleList mLocaleList;
    private final BroadcastReceiver mReceiver;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 4000;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
    }

    public LocaleTile(QSHost qSHost) {
        super(qSHost);
        this.applyLocale = new Runnable() { // from class: com.android.systemui.qs.tiles.LocaleTile.1
            @Override // java.lang.Runnable
            public void run() {
                if (!LocaleTile.this.mLocaleList.get(0).equals(LocaleTile.this.currentLocaleBackup)) {
                    ((QSTileImpl) LocaleTile.this).mHost.collapsePanels();
                    LocalePicker.updateLocales(LocaleTile.this.mLocaleList);
                }
                LocaleTile.this.currentLocaleBackup = null;
            }
        };
        this.mReceiver = new BroadcastReceiver() { // from class: com.android.systemui.qs.tiles.LocaleTile.2
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.LOCALE_CHANGED".equals(intent.getAction())) {
                    LocaleTile.this.updateLocaleList();
                    LocaleTile.this.refreshState();
                }
            }
        };
        updateLocaleList();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleClick() {
        if (checkToggleDisabled()) {
            return;
        }
        toggleLocale();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleSecondaryClick() {
        if (checkToggleDisabled()) {
            return;
        }
        toggleLocale();
    }

    private void toggleLocale() {
        int i = 0;
        if (this.currentLocaleBackup == null) {
            this.currentLocaleBackup = this.mLocaleList.get(0);
        }
        int size = this.mLocaleList.size();
        Locale[] localeArr = new Locale[size];
        while (i < size) {
            int i2 = i + 1;
            localeArr[i] = this.mLocaleList.get(i2 % size);
            i = i2;
        }
        this.mLocaleList = new LocaleList(localeArr);
        this.mHandler.removeCallbacks(this.applyLocale);
        this.mHandler.postDelayed(this.applyLocale, 800L);
        refreshState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent().setComponent(new ComponentName("com.android.settings", "com.android.settings.LanguageSettings"));
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_locale_label);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        int i;
        Locale locale = this.currentLocaleBackup;
        if (locale == null || locale.equals(this.mLocaleList.get(0))) {
            i = R.drawable.ic_qs_locale;
        } else {
            i = R.drawable.ic_qs_locale_pending;
        }
        booleanState.icon = QSTileImpl.ResourceIcon.get(i);
        booleanState.label = this.mLocaleList.get(0).getDisplayLanguage();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateLocaleList() {
        if (this.currentLocaleBackup != null) {
            return;
        }
        this.mLocaleList = LocaleList.getAdjustedDefault();
    }

    private boolean checkToggleDisabled() {
        updateLocaleList();
        if (this.mLocaleList.size() > 1) {
            return false;
        }
        handleLongClick();
        Context context = this.mContext;
        Toast.makeText(context, context.getString(R.string.quick_settings_locale_more_locales_toast), 1).show();
        return true;
    }
}
