package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.view.View;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.Clock;
import com.android.systemui.tuner.TunerService;

/* loaded from: classes.dex */
public class ClockController implements TunerService.Tunable {
    private Clock mActiveClock;
    private Clock mCenterClock;
    private int mClockPosition = 2;
    private Context mContext;
    private Clock mLeftClock;
    private Clock mRightClock;

    public ClockController(Context context, View view) {
        this.mContext = context;
        this.mCenterClock = (Clock) view.findViewById(R.id.clock_center);
        this.mLeftClock = (Clock) view.findViewById(R.id.clock);
        this.mRightClock = (Clock) view.findViewById(R.id.clock_right);
        this.mActiveClock = this.mLeftClock;
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "lineagesystem:status_bar_clock");
    }

    public Clock getClock() {
        return this.mActiveClock;
    }

    private void updateActiveClock() {
        int i = this.mClockPosition;
        if (i == 0) {
            this.mActiveClock = this.mRightClock;
            this.mLeftClock.setClockVisibleByUser(false);
            this.mCenterClock.setClockVisibleByUser(false);
            this.mRightClock.setClockVisibleByUser(true);
            return;
        }
        if (i == 1) {
            this.mActiveClock = this.mCenterClock;
            this.mLeftClock.setClockVisibleByUser(false);
            this.mRightClock.setClockVisibleByUser(false);
            this.mCenterClock.setClockVisibleByUser(true);
            return;
        }
        if (i != 3) {
            this.mActiveClock = this.mLeftClock;
            this.mCenterClock.setClockVisibleByUser(false);
            this.mRightClock.setClockVisibleByUser(false);
            this.mLeftClock.setClockVisibleByUser(true);
            return;
        }
        this.mActiveClock = null;
        this.mLeftClock.setClockVisibleByUser(false);
        this.mCenterClock.setClockVisibleByUser(false);
        this.mRightClock.setClockVisibleByUser(false);
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        str.hashCode();
        if (str.equals("lineagesystem:status_bar_clock")) {
            this.mClockPosition = TunerService.parseInteger(str2, 2);
            updateActiveClock();
        }
    }
}
