package lineageos.preference;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Log;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/* loaded from: classes2.dex */
public class RemotePreferenceManager {
    private static final boolean DEBUG = Log.isLoggable(RemotePreference.class.getSimpleName(), 2);
    private static final String TAG = "RemotePreferenceManager";
    private static RemotePreferenceManager sInstance;
    private final Context mContext;
    private Handler mHandler;
    private HandlerThread mThread;
    private final Map<String, Intent> mCache = new ArrayMap();
    private final Map<String, Set<OnRemoteUpdateListener>> mCallbacks = new ArrayMap();
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private final BroadcastReceiver mListener = new BroadcastReceiver() { // from class: lineageos.preference.RemotePreferenceManager.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (RemotePreferenceManager.DEBUG) {
                Log.d(RemotePreferenceManager.TAG, "onReceive: intent=" + Objects.toString(intent));
            }
            if ("lineageos.intent.action.REFRESH_PREFERENCE".equals(intent.getAction())) {
                String stringExtra = intent.getStringExtra(":lineage:pref_key");
                synchronized (RemotePreferenceManager.this.mCallbacks) {
                    if (stringExtra != null) {
                        if (RemotePreferenceManager.this.mCallbacks.containsKey(stringExtra)) {
                            RemotePreferenceManager.this.requestUpdate(stringExtra);
                        }
                    }
                }
                return;
            }
            if ("lineageos.intent.action.UPDATE_PREFERENCE".equals(intent.getAction())) {
                if (getAbortBroadcast()) {
                    Log.e(RemotePreferenceManager.TAG, "Broadcast aborted, code=" + getResultCode());
                    return;
                }
                final Bundle resultExtras = getResultExtras(true);
                final String string = resultExtras.getString(":lineage:pref_key");
                synchronized (RemotePreferenceManager.this.mCallbacks) {
                    if (string != null) {
                        if (RemotePreferenceManager.this.mCallbacks.containsKey(string)) {
                            RemotePreferenceManager.this.mMainHandler.post(new Runnable() { // from class: lineageos.preference.RemotePreferenceManager.1.1
                                @Override // java.lang.Runnable
                                public void run() {
                                    Set set;
                                    synchronized (RemotePreferenceManager.this.mCallbacks) {
                                        if (RemotePreferenceManager.this.mCallbacks.containsKey(string) && (set = (Set) RemotePreferenceManager.this.mCallbacks.get(string)) != null) {
                                            Iterator it = set.iterator();
                                            while (it.hasNext()) {
                                                ((OnRemoteUpdateListener) it.next()).onRemoteUpdated(resultExtras);
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            }
        }
    };

    public interface OnRemoteUpdateListener {
        Intent getReceiverIntent();

        void onRemoteUpdated(Bundle bundle);
    }

    private RemotePreferenceManager(Context context) {
        this.mContext = context;
    }

    public static synchronized RemotePreferenceManager get(Context context) {
        if (sInstance == null) {
            sInstance = new RemotePreferenceManager(context.getApplicationContext());
        }
        return sInstance;
    }

    public void attach(String str, OnRemoteUpdateListener onRemoteUpdateListener) {
        Intent receiverIntent;
        synchronized (this.mCache) {
            receiverIntent = this.mCache.get(str);
            if (receiverIntent == null && !this.mCache.containsKey(str)) {
                receiverIntent = onRemoteUpdateListener.getReceiverIntent();
                this.mCache.put(str, receiverIntent);
            }
        }
        synchronized (this.mCallbacks) {
            if (receiverIntent != null) {
                Set<OnRemoteUpdateListener> hashSet = this.mCallbacks.get(str);
                if (hashSet == null) {
                    hashSet = new HashSet<>();
                    this.mCallbacks.put(str, hashSet);
                    if (this.mCallbacks.size() == 1) {
                        HandlerThread handlerThread = new HandlerThread("RemotePreference");
                        this.mThread = handlerThread;
                        handlerThread.start();
                        this.mHandler = new Handler(this.mThread.getLooper());
                        this.mContext.registerReceiver(this.mListener, new IntentFilter("lineageos.intent.action.REFRESH_PREFERENCE"), "lineageos.permission.MANAGE_REMOTE_PREFERENCES", this.mHandler);
                    }
                }
                hashSet.add(onRemoteUpdateListener);
                requestUpdate(str);
            }
        }
    }

    public void detach(String str, OnRemoteUpdateListener onRemoteUpdateListener) {
        synchronized (this.mCallbacks) {
            Set<OnRemoteUpdateListener> set = this.mCallbacks.get(str);
            if (set != null && set.remove(onRemoteUpdateListener) && set.isEmpty() && this.mCallbacks.remove(str) != null && this.mCallbacks.isEmpty()) {
                this.mContext.unregisterReceiver(this.mListener);
                HandlerThread handlerThread = this.mThread;
                if (handlerThread != null) {
                    handlerThread.quit();
                    this.mThread = null;
                }
                this.mHandler = null;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void requestUpdate(String str) {
        synchronized (this.mCache) {
            Intent intent = this.mCache.get(str);
            if (intent == null) {
                return;
            }
            this.mContext.sendOrderedBroadcastAsUser(intent, UserHandle.CURRENT, "lineageos.permission.MANAGE_REMOTE_PREFERENCES", this.mListener, this.mHandler, -1, null, null);
        }
    }
}
