package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import com.android.systemui.R;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSDetailItemsList;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lineageos.app.Profile;
import lineageos.app.ProfileManager;
import lineageos.providers.LineageSettings;

/* loaded from: classes.dex */
public class ProfilesTile extends QSTileImpl<QSTile.State> {
    private static final Intent PROFILES_SETTINGS = new Intent("org.lineageos.lineageparts.PROFILES_SETTINGS");
    private final ActivityStarter mActivityStarter;
    private ProfileAdapter mAdapter;
    private final Callback mCallback;
    private final ProfileDetailAdapter mDetailAdapter;
    private QSDetailItemsList mDetails;
    private final QSTile.Icon mIcon;
    private final KeyguardStateController mKeyguard;
    private boolean mListening;
    private final ProfilesObserver mObserver;
    private final ProfileManager mProfileManager;
    private final BroadcastReceiver mReceiver;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return -2147483615;
    }

    public ProfilesTile(QSHost qSHost, ActivityStarter activityStarter, KeyguardStateController keyguardStateController) {
        super(qSHost);
        this.mIcon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_profiles);
        this.mCallback = new Callback();
        this.mReceiver = new BroadcastReceiver() { // from class: com.android.systemui.qs.tiles.ProfilesTile.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if ("lineageos.platform.intent.action.PROFILE_SELECTED".equals(intent.getAction()) || "lineageos.platform.intent.action.PROFILE_UPDATED".equals(intent.getAction())) {
                    ProfilesTile.this.refreshState();
                }
            }
        };
        this.mActivityStarter = activityStarter;
        this.mKeyguard = keyguardStateController;
        this.mProfileManager = ProfileManager.getInstance(this.mContext);
        this.mObserver = new ProfilesObserver(this.mHandler);
        this.mDetailAdapter = (ProfileDetailAdapter) createDetailAdapter();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.State newTileState() {
        return new QSTile.State();
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_profiles_label);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return PROFILES_SETTINGS;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleClick() {
        if (this.mKeyguard.isMethodSecure() && this.mKeyguard.isShowing()) {
            this.mActivityStarter.postQSRunnableDismissingKeyguard(new Runnable() { // from class: com.android.systemui.qs.tiles.ProfilesTile$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$handleClick$0();
                }
            });
        } else {
            setProfilesEnabled(Boolean.valueOf(!profilesEnabled()));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleClick$0() {
        setProfilesEnabled(Boolean.valueOf(!profilesEnabled()));
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleSecondaryClick() {
        if (this.mKeyguard.isMethodSecure() && this.mKeyguard.isShowing()) {
            this.mActivityStarter.postQSRunnableDismissingKeyguard(new Runnable() { // from class: com.android.systemui.qs.tiles.ProfilesTile$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$handleSecondaryClick$1();
                }
            });
        } else {
            setProfilesEnabled(Boolean.TRUE);
            showDetail(true);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleSecondaryClick$1() {
        setProfilesEnabled(Boolean.TRUE);
        showDetail(true);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleLongClick() {
        this.mActivityStarter.postStartActivityDismissingKeyguard(PROFILES_SETTINGS, 0);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleUpdateState(QSTile.State state, Object obj) {
        state.icon = this.mIcon;
        state.label = this.mContext.getString(R.string.quick_settings_profiles_label);
        if (profilesEnabled()) {
            state.secondaryLabel = this.mProfileManager.getActiveProfile().getName();
            state.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_profiles, state.label);
            state.state = 2;
        } else {
            state.secondaryLabel = null;
            state.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_profiles_off);
            state.state = 1;
        }
        state.dualTarget = true;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected String composeChangeAnnouncement() {
        return profilesEnabled() ? this.mContext.getString(R.string.accessibility_quick_settings_profiles_changed, this.mState.label) : this.mContext.getString(R.string.accessibility_quick_settings_profiles_changed_off);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setProfilesEnabled(Boolean bool) {
        LineageSettings.System.putInt(this.mContext.getContentResolver(), "system_profiles_enabled", bool.booleanValue() ? 1 : 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean profilesEnabled() {
        return LineageSettings.System.getInt(this.mContext.getContentResolver(), "system_profiles_enabled", 1) == 1;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        if (this.mListening == z) {
            return;
        }
        this.mListening = z;
        if (z) {
            this.mObserver.startObserving();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("lineageos.platform.intent.action.PROFILE_SELECTED");
            intentFilter.addAction("lineageos.platform.intent.action.PROFILE_UPDATED");
            this.mContext.registerReceiver(this.mReceiver, intentFilter);
            this.mKeyguard.addCallback(this.mCallback);
            refreshState();
            return;
        }
        this.mObserver.endObserving();
        this.mContext.unregisterReceiver(this.mReceiver);
        this.mKeyguard.removeCallback(this.mCallback);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public DetailAdapter getDetailAdapter() {
        return this.mDetailAdapter;
    }

    protected DetailAdapter createDetailAdapter() {
        return new ProfileDetailAdapter();
    }

    private final class Callback implements KeyguardStateController.Callback {
        private Callback() {
        }

        @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
        public void onKeyguardShowingChanged() {
            ProfilesTile.this.refreshState();
        }
    }

    private class ProfileAdapter extends ArrayAdapter<Profile> {
        public ProfileAdapter(Context context, List<Profile> list) {
            super(context, android.R.layout.simple_list_item_single_choice, list);
        }

        @Override // android.widget.ArrayAdapter, android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            CheckedTextView checkedTextView;
            if (view == null) {
                checkedTextView = (CheckedTextView) LayoutInflater.from(((QSTileImpl) ProfilesTile.this).mContext).inflate(android.R.layout.simple_list_item_single_choice, viewGroup, false);
            } else {
                checkedTextView = (CheckedTextView) view;
            }
            checkedTextView.setText(getItem(i).getName());
            return checkedTextView;
        }
    }

    public class ProfileDetailAdapter implements DetailAdapter, AdapterView.OnItemClickListener {
        private List<Profile> mProfilesList;

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public int getMetricsCategory() {
            return -2147483614;
        }

        public ProfileDetailAdapter() {
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public CharSequence getTitle() {
            return ((QSTileImpl) ProfilesTile.this).mContext.getString(R.string.quick_settings_profiles_label);
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public Boolean getToggleState() {
            return Boolean.valueOf(ProfilesTile.this.profilesEnabled());
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            ProfilesTile.this.mDetails = QSDetailItemsList.convertOrInflate(context, view, viewGroup);
            this.mProfilesList = new ArrayList();
            ProfilesTile.this.mDetails.setAdapter(ProfilesTile.this.mAdapter = ProfilesTile.this.new ProfileAdapter(context, this.mProfilesList));
            ListView listView = ProfilesTile.this.mDetails.getListView();
            listView.setChoiceMode(1);
            listView.setOnItemClickListener(this);
            buildProfilesList();
            return ProfilesTile.this.mDetails;
        }

        private void buildProfilesList() {
            this.mProfilesList.clear();
            Profile[] profiles = ProfilesTile.this.mProfileManager.getProfiles();
            Profile activeProfile = ProfilesTile.this.mProfileManager.getActiveProfile();
            UUID uuid = activeProfile != null ? activeProfile.getUuid() : null;
            int i = -1;
            for (int i2 = 0; i2 < profiles.length; i2++) {
                this.mProfilesList.add(profiles[i2]);
                if (uuid != null && uuid.equals(profiles[i2].getUuid())) {
                    i = i2;
                }
            }
            ProfilesTile.this.mDetails.getListView().setItemChecked(i, true);
            ProfilesTile.this.mAdapter.notifyDataSetChanged();
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public Intent getSettingsIntent() {
            return ProfilesTile.PROFILES_SETTINGS;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public void setToggleState(boolean z) {
            ProfilesTile.this.setProfilesEnabled(Boolean.valueOf(z));
            ProfilesTile.this.showDetail(false);
        }

        @Override // android.widget.AdapterView.OnItemClickListener
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
            ProfilesTile.this.mProfileManager.setActiveProfile(((Profile) adapterView.getItemAtPosition(i)).getUuid());
        }
    }

    private class ProfilesObserver extends ContentObserver {
        public ProfilesObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            ProfilesTile.this.refreshState();
        }

        public void startObserving() {
            ((QSTileImpl) ProfilesTile.this).mContext.getContentResolver().registerContentObserver(LineageSettings.System.getUriFor("system_profiles_enabled"), false, this);
        }

        public void endObserving() {
            ((QSTileImpl) ProfilesTile.this).mContext.getContentResolver().unregisterContentObserver(this);
        }
    }
}
