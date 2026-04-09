package lineageos.preference;

import android.R;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceViewHolder;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import lineageos.hardware.LineageHardwareManager;
import lineageos.platform.R$styleable;

/* loaded from: classes2.dex */
public class ConstraintsHelper {
    private static final boolean DEBUG = Log.isLoggable("ConstraintsHelper", 2);
    private final AttributeSet mAttrs;
    private final Context mContext;
    private final Preference mPref;
    private String[] mReplacesKey;
    private int mSummaryMinLines;
    private boolean mAvailable = true;
    private boolean mVerifyIntent = true;

    public ConstraintsHelper(Context context, AttributeSet attributeSet, Preference preference) {
        this.mSummaryMinLines = -1;
        this.mReplacesKey = null;
        this.mContext = context;
        this.mAttrs = attributeSet;
        this.mPref = preference;
        TypedArray typedArrayObtainAttributes = context.getResources().obtainAttributes(attributeSet, R$styleable.lineage_SelfRemovingPreference);
        this.mSummaryMinLines = typedArrayObtainAttributes.getInteger(7, -1);
        String string = typedArrayObtainAttributes.getString(6);
        if (string != null) {
            this.mReplacesKey = string.split("\\|");
        }
        setAvailable(checkConstraints());
        Log.d("ConstraintsHelper", "construct key=" + preference.getKey() + " available=" + this.mAvailable);
    }

    public void setAvailable(boolean z) {
        this.mAvailable = z;
        if (z) {
            return;
        }
        Graveyard.get(this.mContext).addTombstone(this.mPref.getKey());
    }

    public boolean isAvailable() {
        return this.mAvailable;
    }

    private boolean isNegated(String str) {
        return str != null && str.startsWith("!");
    }

    private void checkIntent() {
        Intent intent = this.mPref.getIntent();
        if (intent == null || resolveIntent(this.mContext, intent)) {
            return;
        }
        Graveyard.get(this.mContext).addTombstone(this.mPref.getKey());
        this.mAvailable = false;
    }

    private boolean checkConstraints() {
        if (this.mAttrs == null) {
            return true;
        }
        TypedArray typedArrayObtainAttributes = this.mContext.getResources().obtainAttributes(this.mAttrs, R$styleable.lineage_SelfRemovingPreference);
        try {
            if (typedArrayObtainAttributes.getBoolean(4, false) && UserHandle.myUserId() != 0) {
                return false;
            }
            String string = typedArrayObtainAttributes.getString(0);
            if (string != null) {
                boolean zIsNegated = isNegated(string);
                if (zIsNegated) {
                    string = string.substring(1);
                }
                if (isPackageInstalled(this.mContext, string, false) == zIsNegated) {
                    return false;
                }
            }
            String string2 = typedArrayObtainAttributes.getString(5);
            if (string2 != null) {
                boolean zIsNegated2 = isNegated(string2);
                if (zIsNegated2) {
                    string2 = string2.substring(1);
                }
                if (resolveIntent(this.mContext, string2) == zIsNegated2) {
                    return false;
                }
            }
            String string3 = typedArrayObtainAttributes.getString(1);
            if (string3 != null) {
                boolean zIsNegated3 = isNegated(string3);
                if (zIsNegated3) {
                    string3 = string3.substring(1);
                }
                if ((string3.startsWith("lineagehardware:") ? LineageHardwareManager.getInstance(this.mContext).isSupported(string3.substring(16)) : hasSystemFeature(this.mContext, string3)) == zIsNegated3) {
                    return false;
                }
            }
            String string4 = typedArrayObtainAttributes.getString(3);
            if (string4 != null) {
                boolean zIsNegated4 = isNegated(string4);
                if (zIsNegated4) {
                    string4 = string4.substring(1);
                }
                String str = SystemProperties.get(string4);
                if ((str != null && Boolean.parseBoolean(str)) == zIsNegated4) {
                    return false;
                }
            }
            TypedValue typedValuePeekValue = typedArrayObtainAttributes.peekValue(2);
            if (typedValuePeekValue != null && typedValuePeekValue.resourceId != 0) {
                if (typedValuePeekValue.type == 3 && this.mContext.getResources().getString(typedValuePeekValue.resourceId) == null) {
                    return false;
                }
                int i = typedValuePeekValue.type;
                if (i == 18 && typedValuePeekValue.data == 0) {
                    return false;
                }
                if (i == 16) {
                    int i2 = typedArrayObtainAttributes.getInt(8, -1);
                    int i3 = typedValuePeekValue.data;
                    if (i3 == 0 || (i2 >= 0 && (i2 & i3) == 0)) {
                        return false;
                    }
                }
            }
            return true;
        } finally {
            typedArrayObtainAttributes.recycle();
        }
    }

    public static boolean hasSystemFeature(Context context, String str) {
        return context.getPackageManager().hasSystemFeature(str);
    }

    public static boolean isPackageInstalled(Context context, String str, boolean z) {
        if (str != null) {
            return context.getPackageManager().getPackageInfo(str, 0).applicationInfo.enabled || z;
        }
        return true;
    }

    public static boolean resolveIntent(Context context, Intent intent) {
        if (DEBUG) {
            Log.d("ConstraintsHelper", "resolveIntent " + Objects.toString(intent));
        }
        for (ResolveInfo resolveInfo : context.getPackageManager().queryIntentActivitiesAsUser(intent, 1048576, UserHandle.myUserId())) {
            if (DEBUG) {
                Log.d("ConstraintsHelper", "resolveInfo: " + Objects.toString(resolveInfo));
            }
            if ((resolveInfo.activityInfo.applicationInfo.flags & 1) != 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean resolveIntent(Context context, String str) {
        return resolveIntent(context, new Intent(str));
    }

    public static int getAttr(Context context, int i, int i2) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(i, typedValue, true);
        return typedValue.resourceId != 0 ? i : i2;
    }

    public void onAttached() {
        checkIntent();
        if (isAvailable() && this.mReplacesKey != null) {
            Graveyard.get(this.mContext).addTombstones(this.mReplacesKey);
        }
        Graveyard.get(this.mContext).summonReaper(this.mPref.getPreferenceManager());
    }

    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        TextView textView;
        if (isAvailable() && this.mSummaryMinLines > 0 && (textView = (TextView) preferenceViewHolder.itemView.findViewById(R.id.summary)) != null) {
            textView.setMinLines(this.mSummaryMinLines);
        }
    }

    private static class Graveyard {
        private static Graveyard sInstance;
        private final Context mContext;
        private Set<String> mDeathRow = new ArraySet();

        private Graveyard(Context context) {
            this.mContext = context;
        }

        public static synchronized Graveyard get(Context context) {
            if (sInstance == null) {
                sInstance = new Graveyard(context);
            }
            return sInstance;
        }

        public void addTombstone(String str) {
            synchronized (this.mDeathRow) {
                this.mDeathRow.add(str);
            }
        }

        public void addTombstones(String[] strArr) {
            synchronized (this.mDeathRow) {
                this.mDeathRow.addAll(Arrays.asList(strArr));
            }
        }

        private PreferenceGroup getParent(Preference preference, Preference preference2) {
            return getParent((PreferenceGroup) preference.getPreferenceManager().getPreferenceScreen(), preference2);
        }

        private PreferenceGroup getParent(PreferenceGroup preferenceGroup, Preference preference) {
            PreferenceGroup parent;
            for (int i = 0; i < preferenceGroup.getPreferenceCount(); i++) {
                Preference preference2 = preferenceGroup.getPreference(i);
                if (preference2 == preference) {
                    return preferenceGroup;
                }
                if (PreferenceGroup.class.isInstance(preference2) && (parent = getParent((PreferenceGroup) preference2, preference)) != null) {
                    return parent;
                }
            }
            return null;
        }

        private void hidePreference(PreferenceManager preferenceManager, Preference preference) {
            boolean z;
            preference.setVisible(false);
            PreferenceGroup parent = getParent(preference, preference);
            int i = 0;
            while (true) {
                if (i >= parent.getPreferenceCount()) {
                    z = true;
                    break;
                } else {
                    if (parent.getPreference(i).isVisible()) {
                        z = false;
                        break;
                    }
                    i++;
                }
            }
            if (z) {
                parent.setVisible(false);
            }
        }

        public void summonReaper(PreferenceManager preferenceManager) {
            synchronized (this.mDeathRow) {
                ArraySet arraySet = new ArraySet();
                for (String str : this.mDeathRow) {
                    Preference preferenceFindPreference = preferenceManager.findPreference(str);
                    if (preferenceFindPreference != null) {
                        hidePreference(preferenceManager, preferenceFindPreference);
                    } else {
                        arraySet.add(str);
                    }
                }
                this.mDeathRow = arraySet;
            }
        }
    }
}
