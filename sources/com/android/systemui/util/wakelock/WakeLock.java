package com.android.systemui.util.wakelock;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;
import java.util.HashMap;

/* loaded from: classes.dex */
public interface WakeLock {
    void acquire(String str);

    void release(String str);

    Runnable wrap(Runnable runnable);

    static WakeLock createPartial(Context context, String str) {
        return createPartial(context, str, 20000L);
    }

    static WakeLock createPartial(Context context, String str, long j) {
        return wrap(createPartialInner(context, str), j);
    }

    static PowerManager.WakeLock createPartialInner(Context context, String str) {
        return ((PowerManager) context.getSystemService(PowerManager.class)).newWakeLock(1, str);
    }

    static Runnable wrapImpl(final WakeLock wakeLock, final Runnable runnable) {
        wakeLock.acquire("wrap");
        return new Runnable() { // from class: com.android.systemui.util.wakelock.WakeLock$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                WakeLock.lambda$wrapImpl$0(runnable, wakeLock);
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    static /* synthetic */ void lambda$wrapImpl$0(Runnable runnable, WakeLock wakeLock) {
        try {
            runnable.run();
        } finally {
            wakeLock.release("wrap");
        }
    }

    static WakeLock wrap(final PowerManager.WakeLock wakeLock, final long j) {
        return new WakeLock() { // from class: com.android.systemui.util.wakelock.WakeLock.1
            private final HashMap<String, Integer> mActiveClients = new HashMap<>();

            @Override // com.android.systemui.util.wakelock.WakeLock
            public void acquire(String str) {
                this.mActiveClients.putIfAbsent(str, 0);
                HashMap<String, Integer> map = this.mActiveClients;
                map.put(str, Integer.valueOf(map.get(str).intValue() + 1));
                wakeLock.acquire(j);
            }

            @Override // com.android.systemui.util.wakelock.WakeLock
            public void release(String str) {
                Integer num = this.mActiveClients.get(str);
                if (num == null) {
                    Log.wtf("WakeLock", "Releasing WakeLock with invalid reason: " + str, new Throwable());
                    return;
                }
                if (num.intValue() == 1) {
                    this.mActiveClients.remove(str);
                } else {
                    this.mActiveClients.put(str, Integer.valueOf(num.intValue() - 1));
                }
                if (wakeLock.isHeld()) {
                    wakeLock.release();
                }
            }

            @Override // com.android.systemui.util.wakelock.WakeLock
            public Runnable wrap(Runnable runnable) {
                return WakeLock.wrapImpl(this, runnable);
            }

            public String toString() {
                return "active clients= " + this.mActiveClients.toString();
            }
        };
    }

    public static class Builder {
        private final Context mContext;
        private long mMaxTimeout = 20000;
        private String mTag;

        public Builder(Context context) {
            this.mContext = context;
        }

        public Builder setTag(String str) {
            this.mTag = str;
            return this;
        }

        public WakeLock build() {
            return WakeLock.createPartial(this.mContext, this.mTag, this.mMaxTimeout);
        }
    }
}
