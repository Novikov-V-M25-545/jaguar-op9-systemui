package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkStats;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.tuner.TunerService;
import java.text.DecimalFormat;

/* loaded from: classes.dex */
public class NetworkTraffic extends TextView implements TunerService.Tunable {
    protected boolean mAttached;
    private boolean mAutoHide;
    private long mAutoHideThreshold;
    protected boolean mConnectionAvailable;
    private ConnectivityManager mConnectivityManager;
    private Drawable mDrawable;
    protected boolean mEnabled;
    private boolean mHideArrows;
    protected int mIconTint;
    private final BroadcastReceiver mIntentReceiver;
    protected boolean mIsActive;
    private long mLastRxBytes;
    private long mLastTxBytes;
    private long mLastUpdateTime;
    protected int mLocation;
    private int mMode;
    private INetworkManagementService mNetworkManagementService;
    private int mRefreshInterval;
    private RelativeSizeSpan mSpeedRelativeSizeSpan;
    private int mSubMode;
    private boolean mTrafficActive;
    private Handler mTrafficHandler;
    private RelativeSizeSpan mUnitRelativeSizeSpan;
    private int mUnits;
    protected boolean mVisible;
    protected int newTint;

    public NetworkTraffic(Context context) {
        this(context, null);
    }

    public NetworkTraffic(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NetworkTraffic(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mLocation = 0;
        this.mMode = 0;
        this.mSubMode = 0;
        this.mIconTint = 0;
        this.newTint = -1;
        this.mRefreshInterval = 2;
        this.mVisible = true;
        this.mSpeedRelativeSizeSpan = new RelativeSizeSpan(0.7f);
        this.mUnitRelativeSizeSpan = new RelativeSizeSpan(0.65f);
        this.mEnabled = false;
        this.mConnectionAvailable = true;
        this.mTrafficHandler = new Handler() { // from class: com.android.systemui.statusbar.policy.NetworkTraffic.1
            /* JADX WARN: Removed duplicated region for block: B:66:0x0104  */
            /* JADX WARN: Removed duplicated region for block: B:7:0x0059  */
            @Override // android.os.Handler
            /*
                Code decompiled incorrectly, please refer to instructions dump.
                To view partially-correct code enable 'Show inconsistent code' option in preferences
            */
            public void handleMessage(android.os.Message r14) {
                /*
                    Method dump skipped, instructions count: 334
                    To view this dump change 'Code comments level' option to 'DEBUG'
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.NetworkTraffic.AnonymousClass1.handleMessage(android.os.Message):void");
            }

            private CharSequence formatOutput(long j) {
                String string;
                String string2;
                String string3;
                String str;
                if (NetworkTraffic.this.mUnits == 0) {
                    j *= 8;
                    string = ((TextView) NetworkTraffic.this).mContext.getString(R.string.gigabitspersecond_short);
                    string2 = ((TextView) NetworkTraffic.this).mContext.getString(R.string.megabitspersecond_short);
                    string3 = ((TextView) NetworkTraffic.this).mContext.getString(R.string.kilobitspersecond_short);
                } else {
                    string = ((TextView) NetworkTraffic.this).mContext.getString(R.string.gigabytespersecond_short);
                    string2 = ((TextView) NetworkTraffic.this).mContext.getString(R.string.megabytespersecond_short);
                    string3 = ((TextView) NetworkTraffic.this).mContext.getString(R.string.kilobytespersecond_short);
                }
                if (j >= 1000000000) {
                    str = new DecimalFormat("0.##").format(j / 1.0E9f);
                } else {
                    if (j >= 100000000) {
                        str = new DecimalFormat("##0").format(j / 1000000.0f);
                    } else if (j >= 10000000) {
                        str = new DecimalFormat("#0.#").format(j / 1000000.0f);
                    } else if (j >= 1000000) {
                        str = new DecimalFormat("0.##").format(j / 1000000.0f);
                    } else {
                        if (j >= 100000) {
                            str = new DecimalFormat("##0").format(j / 1000.0f);
                        } else if (j >= 10000) {
                            str = new DecimalFormat("#0.#").format(j / 1000.0f);
                        } else {
                            str = new DecimalFormat("0.##").format(j / 1000.0f);
                        }
                        string = string3;
                    }
                    string = string2;
                }
                SpannableString spannableString = new SpannableString(str);
                spannableString.setSpan(NetworkTraffic.this.mSpeedRelativeSizeSpan, 0, str.length(), 18);
                SpannableString spannableString2 = new SpannableString(string);
                spannableString2.setSpan(NetworkTraffic.this.mUnitRelativeSizeSpan, 0, string.length(), 18);
                return TextUtils.concat(spannableString, "\n", spannableString2);
            }

            private long[] getTotalRxTxBytes() {
                long[] jArr = {0, 0};
                jArr[0] = TrafficStats.getTotalRxBytes();
                jArr[1] = TrafficStats.getTotalTxBytes();
                TetheringStats offloadTetheringStats = NetworkTraffic.this.getOffloadTetheringStats();
                jArr[0] = jArr[0] + offloadTetheringStats.rxBytes;
                jArr[1] = jArr[1] + offloadTetheringStats.txBytes;
                return jArr;
            }
        };
        this.mIntentReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.policy.NetworkTraffic.2
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                String action = intent.getAction();
                if (action != null && action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                    NetworkTraffic networkTraffic = NetworkTraffic.this;
                    networkTraffic.mConnectionAvailable = networkTraffic.mConnectivityManager.getActiveNetworkInfo() != null;
                    NetworkTraffic.this.updateViews();
                }
            }
        };
        ((TextView) this).mContext = context;
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
    }

    @Override // android.widget.TextView, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!this.mAttached) {
            this.mAttached = true;
            TunerService tunerService = (TunerService) Dependency.get(TunerService.class);
            tunerService.addTunable(this, "lineagesecure:network_traffic_location");
            tunerService.addTunable(this, "lineagesecure:network_traffic_mode");
            tunerService.addTunable(this, "lineagesecure:network_traffic_autohide");
            tunerService.addTunable(this, "lineagesecure:network_traffic_autohide_threshold");
            tunerService.addTunable(this, "lineagesecure:network_traffic_units");
            tunerService.addTunable(this, "lineagesecure:network_traffic_refresh_interval");
            tunerService.addTunable(this, "lineagesecure:network_traffic_hidearrow");
            this.mConnectionAvailable = this.mConnectivityManager.getActiveNetworkInfo() != null;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            ((TextView) this).mContext.registerReceiver(this.mIntentReceiver, intentFilter, null, this.mTrafficHandler);
        }
        updateViews();
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clearHandlerCallbacks();
        if (this.mAttached) {
            ((TextView) this).mContext.unregisterReceiver(this.mIntentReceiver);
            ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
            this.mAttached = false;
        }
    }

    protected void setEnabled() {
        this.mEnabled = this.mLocation == 2;
    }

    protected void updateVisibility() {
        boolean z = this.mEnabled && this.mIsActive && getText() != "";
        if (z != this.mVisible) {
            this.mVisible = z;
            setVisibility(z ? 0 : 8);
        }
    }

    private class TetheringStats {
        long rxBytes;
        long txBytes;

        private TetheringStats() {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public TetheringStats getOffloadTetheringStats() {
        NetworkStats networkStatsTethering;
        NetworkStats.Entry values = null;
        TetheringStats tetheringStats = new TetheringStats();
        if (this.mNetworkManagementService == null) {
            this.mNetworkManagementService = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        }
        try {
            networkStatsTethering = this.mNetworkManagementService.getNetworkStatsTethering(0);
        } catch (RemoteException e) {
            Log.e("NetworkTraffic", "Unable to call getNetworkStatsTethering: " + e);
            networkStatsTethering = null;
        }
        if (networkStatsTethering == null) {
            return tetheringStats;
        }
        for (int i = 0; i < networkStatsTethering.size(); i++) {
            values = networkStatsTethering.getValues(i, values);
            if (values.uid == -1) {
                tetheringStats.txBytes += values.txBytes;
                tetheringStats.rxBytes += values.rxBytes;
            }
        }
        return tetheringStats;
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        str.hashCode();
        switch (str) {
            case "lineagesecure:network_traffic_hidearrow":
                boolean integerSwitch = TunerService.parseIntegerSwitch(str2, false);
                this.mHideArrows = integerSwitch;
                if (!integerSwitch) {
                    setGravity(8388629);
                } else {
                    setGravity(17);
                }
                setTrafficDrawable();
                break;
            case "lineagesecure:network_traffic_refresh_interval":
                this.mRefreshInterval = TunerService.parseInteger(str2, 2);
                updateViews();
                break;
            case "lineagesecure:network_traffic_units":
                this.mUnits = TunerService.parseInteger(str2, 1);
                updateViews();
                break;
            case "lineagesecure:network_traffic_mode":
                this.mMode = TunerService.parseInteger(str2, 0);
                updateViews();
                setTrafficDrawable();
                break;
            case "lineagesecure:network_traffic_autohide":
                this.mAutoHide = TunerService.parseIntegerSwitch(str2, false);
                updateViews();
                break;
            case "lineagesecure:network_traffic_location":
                this.mLocation = TunerService.parseInteger(str2, 0);
                setEnabled();
                if (this.mEnabled) {
                    setLines(2);
                    setTypeface(Typeface.create(getResources().getString(android.R.string.common_name), 1));
                    setLineSpacing(0.8f, 0.8f);
                }
                updateViews();
                break;
            case "lineagesecure:network_traffic_autohide_threshold":
                this.mAutoHideThreshold = TunerService.parseInteger(str2, 0) * 1000;
                updateViews();
                break;
        }
    }

    protected void updateViews() {
        if (this.mEnabled) {
            updateViewState();
        }
    }

    private void updateViewState() {
        this.mTrafficHandler.removeMessages(1);
        this.mTrafficHandler.sendEmptyMessageDelayed(1, 1000L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void clearHandlerCallbacks() {
        this.mTrafficHandler.removeMessages(0);
        this.mTrafficHandler.removeMessages(1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setTrafficDrawable() {
        int i;
        int i2 = 0;
        if (!this.mHideArrows) {
            if (!this.mTrafficActive) {
                i2 = R.drawable.stat_sys_network_traffic;
            } else {
                int i3 = this.mMode;
                if (i3 == 1 || (i = this.mSubMode) == 1) {
                    i2 = R.drawable.stat_sys_network_traffic_up;
                } else if (i3 == 2 || i == 2) {
                    i2 = R.drawable.stat_sys_network_traffic_down;
                } else if (i3 == 0) {
                    i2 = R.drawable.stat_sys_network_traffic_updown;
                }
            }
        }
        Drawable drawable = i2 != 0 ? getResources().getDrawable(i2) : null;
        if (this.mDrawable == drawable && this.mIconTint == this.newTint) {
            return;
        }
        this.mDrawable = drawable;
        this.mIconTint = this.newTint;
        setCompoundDrawablesWithIntrinsicBounds((Drawable) null, (Drawable) null, drawable, (Drawable) null);
        updateTrafficDrawable();
    }

    protected void updateTrafficDrawable() {
        Drawable drawable = this.mDrawable;
        if (drawable != null) {
            drawable.setColorFilter(this.mIconTint, PorterDuff.Mode.MULTIPLY);
        }
        setTextColor(this.mIconTint);
    }
}
