package lineageos.preference;

import android.R;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.AttributeSet;
import android.util.Log;
import androidx.preference.R$attr;
import java.util.List;
import java.util.Objects;
import lineageos.preference.RemotePreferenceManager;

/* loaded from: classes2.dex */
public class RemotePreference extends SelfRemovingPreference implements RemotePreferenceManager.OnRemoteUpdateListener {
    private static final boolean DEBUG;
    private static final String TAG;
    protected final Context mContext;

    static {
        String simpleName = RemotePreference.class.getSimpleName();
        TAG = simpleName;
        DEBUG = Log.isLoggable(simpleName, 2);
    }

    public RemotePreference(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mContext = context;
    }

    public RemotePreference(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public RemotePreference(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, ConstraintsHelper.getAttr(context, R$attr.preferenceScreenStyle, R.attr.preferenceScreenStyle));
    }

    @Override // lineageos.preference.RemotePreferenceManager.OnRemoteUpdateListener
    public void onRemoteUpdated(Bundle bundle) {
        boolean z;
        if (DEBUG) {
            Log.d(TAG, "onRemoteUpdated: " + bundle.toString());
        }
        if (bundle.containsKey(":lineage:pref_enabled") && (z = bundle.getBoolean(":lineage:pref_enabled", true)) != isAvailable()) {
            setAvailable(z);
        }
        if (isAvailable()) {
            setSummary(bundle.getString(":lineage:pref_summary"));
        }
    }

    @Override // lineageos.preference.SelfRemovingPreference, androidx.preference.Preference
    public void onAttached() {
        super.onAttached();
        if (isAvailable()) {
            RemotePreferenceManager.get(this.mContext).attach(getKey(), this);
        }
    }

    @Override // androidx.preference.Preference
    public void onDetached() {
        super.onDetached();
        RemotePreferenceManager.get(this.mContext).detach(getKey(), this);
    }

    protected String getRemoteKey(Bundle bundle) {
        String string = bundle.getString("org.lineageos.settings.summary.key");
        if (string == null || !string.equals(getKey())) {
            return null;
        }
        return string;
    }

    @Override // lineageos.preference.RemotePreferenceManager.OnRemoteUpdateListener
    public Intent getReceiverIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            Log.w(TAG, "No target intent specified in preference!");
            return null;
        }
        List<ResolveInfo> listQueryIntentActivitiesAsUser = this.mContext.getPackageManager().queryIntentActivitiesAsUser(intent, 1048704, UserHandle.myUserId());
        if (listQueryIntentActivitiesAsUser.size() == 0) {
            Log.w(TAG, "No activity found for: " + Objects.toString(intent));
        }
        for (ResolveInfo resolveInfo : listQueryIntentActivitiesAsUser) {
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            String str = TAG;
            Log.d(str, "ResolveInfo " + Objects.toString(resolveInfo));
            Bundle bundle = activityInfo.metaData;
            if (bundle != null && bundle.containsKey("org.lineageos.settings.summary.receiver")) {
                String string = bundle.getString("org.lineageos.settings.summary.receiver");
                String str2 = activityInfo.packageName;
                String remoteKey = getRemoteKey(bundle);
                if (DEBUG) {
                    Log.d(str, "getReceiverIntent class=" + string + " package=" + str2 + " key=" + remoteKey);
                }
                if (remoteKey != null) {
                    Intent intent2 = new Intent("lineageos.intent.action.UPDATE_PREFERENCE");
                    intent2.setComponent(new ComponentName(str2, string));
                    intent2.putExtra(":lineage:pref_key", remoteKey);
                    return intent2;
                }
            }
        }
        return null;
    }
}
