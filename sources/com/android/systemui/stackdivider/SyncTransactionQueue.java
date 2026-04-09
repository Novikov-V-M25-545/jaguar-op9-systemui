package com.android.systemui.stackdivider;

import android.os.Handler;
import android.util.Slog;
import android.view.SurfaceControl;
import android.window.WindowContainerTransaction;
import android.window.WindowContainerTransactionCallback;
import android.window.WindowOrganizer;
import com.android.systemui.TransactionPool;
import java.util.ArrayList;

/* loaded from: classes.dex */
class SyncTransactionQueue {
    private final Handler mHandler;
    private final TransactionPool mTransactionPool;
    private final ArrayList<SyncCallback> mQueue = new ArrayList<>();
    private SyncCallback mInFlight = null;
    private final ArrayList<TransactionRunnable> mRunnables = new ArrayList<>();
    private final Runnable mOnReplyTimeout = new Runnable() { // from class: com.android.systemui.stackdivider.SyncTransactionQueue$$ExternalSyntheticLambda0
        @Override // java.lang.Runnable
        public final void run() {
            this.f$0.lambda$new$0();
        }
    };

    interface TransactionRunnable {
        void runWithTransaction(SurfaceControl.Transaction transaction);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0() {
        synchronized (this.mQueue) {
            SyncCallback syncCallback = this.mInFlight;
            if (syncCallback != null && this.mQueue.contains(syncCallback)) {
                Slog.w("SyncTransactionQueue", "Sync Transaction timed-out: " + this.mInFlight.mWCT);
                SyncCallback syncCallback2 = this.mInFlight;
                syncCallback2.onTransactionReady(syncCallback2.mId, new SurfaceControl.Transaction());
            }
        }
    }

    SyncTransactionQueue(TransactionPool transactionPool, Handler handler) {
        this.mTransactionPool = transactionPool;
        this.mHandler = handler;
    }

    void queue(WindowContainerTransaction windowContainerTransaction) {
        SyncCallback syncCallback = new SyncCallback(windowContainerTransaction);
        synchronized (this.mQueue) {
            this.mQueue.add(syncCallback);
            if (this.mQueue.size() == 1) {
                syncCallback.send();
            }
        }
    }

    boolean queueIfWaiting(WindowContainerTransaction windowContainerTransaction) {
        synchronized (this.mQueue) {
            if (this.mQueue.isEmpty()) {
                return false;
            }
            SyncCallback syncCallback = new SyncCallback(windowContainerTransaction);
            this.mQueue.add(syncCallback);
            if (this.mQueue.size() == 1) {
                syncCallback.send();
            }
            return true;
        }
    }

    void runInSync(TransactionRunnable transactionRunnable) {
        synchronized (this.mQueue) {
            if (this.mInFlight != null) {
                this.mRunnables.add(transactionRunnable);
                return;
            }
            SurfaceControl.Transaction transactionAcquire = this.mTransactionPool.acquire();
            transactionRunnable.runWithTransaction(transactionAcquire);
            transactionAcquire.apply();
            this.mTransactionPool.release(transactionAcquire);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onTransactionReceived(SurfaceControl.Transaction transaction) {
        int size = this.mRunnables.size();
        for (int i = 0; i < size; i++) {
            this.mRunnables.get(i).runWithTransaction(transaction);
        }
        this.mRunnables.clear();
        transaction.apply();
        transaction.close();
    }

    /* JADX INFO: Access modifiers changed from: private */
    class SyncCallback extends WindowContainerTransactionCallback {
        int mId = -1;
        final WindowContainerTransaction mWCT;

        SyncCallback(WindowContainerTransaction windowContainerTransaction) {
            this.mWCT = windowContainerTransaction;
        }

        void send() {
            if (SyncTransactionQueue.this.mInFlight == null) {
                SyncTransactionQueue.this.mInFlight = this;
                this.mId = new WindowOrganizer().applySyncTransaction(this.mWCT, this);
                SyncTransactionQueue.this.mHandler.postDelayed(SyncTransactionQueue.this.mOnReplyTimeout, 5300L);
            } else {
                throw new IllegalStateException("Sync Transactions must be serialized. In Flight: " + SyncTransactionQueue.this.mInFlight.mId + " - " + SyncTransactionQueue.this.mInFlight.mWCT);
            }
        }

        public void onTransactionReady(final int i, final SurfaceControl.Transaction transaction) {
            SyncTransactionQueue.this.mHandler.post(new Runnable() { // from class: com.android.systemui.stackdivider.SyncTransactionQueue$SyncCallback$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onTransactionReady$0(i, transaction);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onTransactionReady$0(int i, SurfaceControl.Transaction transaction) {
            synchronized (SyncTransactionQueue.this.mQueue) {
                if (this.mId == i) {
                    SyncTransactionQueue.this.mInFlight = null;
                    SyncTransactionQueue.this.mHandler.removeCallbacks(SyncTransactionQueue.this.mOnReplyTimeout);
                    SyncTransactionQueue.this.mQueue.remove(this);
                    SyncTransactionQueue.this.onTransactionReceived(transaction);
                    if (!SyncTransactionQueue.this.mQueue.isEmpty()) {
                        ((SyncCallback) SyncTransactionQueue.this.mQueue.get(0)).send();
                    }
                    return;
                }
                Slog.e("SyncTransactionQueue", "Got an unexpected onTransactionReady. Expected " + this.mId + " but got " + i);
            }
        }
    }
}
