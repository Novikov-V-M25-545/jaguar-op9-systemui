package androidx.slice;

import android.content.ContentProviderClient;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.ArrayMap;
import android.util.Pair;
import androidx.slice.SliceViewManager;
import androidx.slice.widget.SliceLiveData;
import java.util.concurrent.Executor;

/* loaded from: classes.dex */
public abstract class SliceViewManagerBase extends SliceViewManager {
    protected final Context mContext;
    private final ArrayMap<Pair<Uri, SliceViewManager.SliceCallback>, SliceListenerImpl> mListenerLookup = new ArrayMap<>();

    SliceViewManagerBase(Context context) {
        this.mContext = context;
    }

    @Override // androidx.slice.SliceViewManager
    public void registerSliceCallback(Uri uri, SliceViewManager.SliceCallback sliceCallback) {
        final Handler handler = new Handler(Looper.getMainLooper());
        registerSliceCallback(uri, new Executor() { // from class: androidx.slice.SliceViewManagerBase.1
            @Override // java.util.concurrent.Executor
            public void execute(Runnable runnable) {
                handler.post(runnable);
            }
        }, sliceCallback);
    }

    public void registerSliceCallback(Uri uri, Executor executor, SliceViewManager.SliceCallback sliceCallback) {
        getListener(uri, sliceCallback, new SliceListenerImpl(uri, executor, sliceCallback)).startListening();
    }

    @Override // androidx.slice.SliceViewManager
    public void unregisterSliceCallback(Uri uri, SliceViewManager.SliceCallback sliceCallback) {
        synchronized (this.mListenerLookup) {
            SliceListenerImpl sliceListenerImplRemove = this.mListenerLookup.remove(new Pair(uri, sliceCallback));
            if (sliceListenerImplRemove != null) {
                sliceListenerImplRemove.stopListening();
            }
        }
    }

    private SliceListenerImpl getListener(Uri uri, SliceViewManager.SliceCallback sliceCallback, SliceListenerImpl sliceListenerImpl) {
        Pair<Uri, SliceViewManager.SliceCallback> pair = new Pair<>(uri, sliceCallback);
        synchronized (this.mListenerLookup) {
            SliceListenerImpl sliceListenerImplPut = this.mListenerLookup.put(pair, sliceListenerImpl);
            if (sliceListenerImplPut != null) {
                sliceListenerImplPut.stopListening();
            }
        }
        return sliceListenerImpl;
    }

    private class SliceListenerImpl {
        final SliceViewManager.SliceCallback mCallback;
        final Executor mExecutor;
        private boolean mPinned;
        Uri mUri;
        final Runnable mUpdateSlice = new Runnable() { // from class: androidx.slice.SliceViewManagerBase.SliceListenerImpl.1
            @Override // java.lang.Runnable
            public void run() {
                SliceListenerImpl.this.tryPin();
                SliceListenerImpl sliceListenerImpl = SliceListenerImpl.this;
                final Slice sliceBindSlice = Slice.bindSlice(SliceViewManagerBase.this.mContext, sliceListenerImpl.mUri, SliceLiveData.SUPPORTED_SPECS);
                SliceListenerImpl.this.mExecutor.execute(new Runnable() { // from class: androidx.slice.SliceViewManagerBase.SliceListenerImpl.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        SliceListenerImpl.this.mCallback.onSliceUpdated(sliceBindSlice);
                    }
                });
            }
        };
        private final ContentObserver mObserver = new ContentObserver(new Handler(Looper.getMainLooper())) { // from class: androidx.slice.SliceViewManagerBase.SliceListenerImpl.2
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                AsyncTask.execute(SliceListenerImpl.this.mUpdateSlice);
            }
        };

        SliceListenerImpl(Uri uri, Executor executor, SliceViewManager.SliceCallback sliceCallback) {
            this.mUri = uri;
            this.mExecutor = executor;
            this.mCallback = sliceCallback;
        }

        void startListening() {
            ContentProviderClient contentProviderClientAcquireContentProviderClient = SliceViewManagerBase.this.mContext.getContentResolver().acquireContentProviderClient(this.mUri);
            if (contentProviderClientAcquireContentProviderClient != null) {
                contentProviderClientAcquireContentProviderClient.release();
                SliceViewManagerBase.this.mContext.getContentResolver().registerContentObserver(this.mUri, true, this.mObserver);
                tryPin();
            }
        }

        void tryPin() {
            if (this.mPinned) {
                return;
            }
            try {
                SliceViewManagerBase.this.pinSlice(this.mUri);
                this.mPinned = true;
            } catch (SecurityException unused) {
            }
        }

        void stopListening() {
            SliceViewManagerBase.this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
            if (this.mPinned) {
                SliceViewManagerBase.this.unpinSlice(this.mUri);
                this.mPinned = false;
            }
        }
    }
}
