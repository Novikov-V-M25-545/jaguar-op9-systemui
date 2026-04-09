package com.android.systemui.util.leak;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/* loaded from: classes.dex */
public class WeakIdentityHashMap<K, V> {
    private final HashMap<WeakReference<K>, V> mMap = new HashMap<>();
    private final ReferenceQueue<Object> mRefQueue = new ReferenceQueue<>();

    private void cleanUp() {
        while (true) {
            Reference<? extends Object> referencePoll = this.mRefQueue.poll();
            if (referencePoll == null) {
                return;
            } else {
                this.mMap.remove(referencePoll);
            }
        }
    }

    public void put(K k, V v) {
        cleanUp();
        this.mMap.put(new CmpWeakReference(k, this.mRefQueue), v);
    }

    public V get(K k) {
        cleanUp();
        return this.mMap.get(new CmpWeakReference(k));
    }

    public Set<Map.Entry<WeakReference<K>, V>> entrySet() {
        return this.mMap.entrySet();
    }

    public int size() {
        cleanUp();
        return this.mMap.size();
    }

    public boolean isEmpty() {
        cleanUp();
        return this.mMap.isEmpty();
    }

    private static class CmpWeakReference<K> extends WeakReference<K> {
        private final int mHashCode;

        public CmpWeakReference(K k) {
            super(k);
            this.mHashCode = System.identityHashCode(k);
        }

        public CmpWeakReference(K k, ReferenceQueue<Object> referenceQueue) {
            super(k, referenceQueue);
            this.mHashCode = System.identityHashCode(k);
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            K k = get();
            return k != null && (obj instanceof CmpWeakReference) && ((CmpWeakReference) obj).get() == k;
        }

        public int hashCode() {
            return this.mHashCode;
        }
    }
}
