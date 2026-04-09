package com.android.settingslib.deviceinfo;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import androidx.preference.Preference;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import java.lang.ref.WeakReference;

/* loaded from: classes.dex */
public abstract class AbstractSleeptimePreferenceController extends AbstractPreferenceController implements LifecycleObserver, OnStart, OnStop {
    static final String KEY_SLEEPTIME = "sleep_time";
    private Handler mHandler;
    private Preference mSleeptime;

    @Override // com.android.settingslib.core.lifecycle.events.OnStart
    public void onStart() {
        getHandler().sendEmptyMessage(500);
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnStop
    public void onStop() {
        getHandler().removeMessages(500);
    }

    private Handler getHandler() {
        if (this.mHandler == null) {
            this.mHandler = new MyHandler(this);
        }
        return this.mHandler;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateTimes() {
        this.mSleeptime.setSummary(Math.round((Math.max(SystemClock.elapsedRealtime() - SystemClock.uptimeMillis(), 0.0f) / SystemClock.elapsedRealtime()) * 100.0f) + "%");
    }

    private static class MyHandler extends Handler {
        private WeakReference<AbstractSleeptimePreferenceController> mStatus;

        public MyHandler(AbstractSleeptimePreferenceController abstractSleeptimePreferenceController) {
            this.mStatus = new WeakReference<>(abstractSleeptimePreferenceController);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            AbstractSleeptimePreferenceController abstractSleeptimePreferenceController = this.mStatus.get();
            if (abstractSleeptimePreferenceController == null) {
                return;
            }
            if (message.what == 500) {
                abstractSleeptimePreferenceController.updateTimes();
                sendEmptyMessageDelayed(500, 1000L);
            } else {
                throw new IllegalStateException("Unknown message " + message.what);
            }
        }
    }
}
