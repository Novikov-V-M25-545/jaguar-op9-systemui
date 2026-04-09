package com.android.systemui.statusbar.notification;

import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import androidx.collection.ArraySet;
import com.android.systemui.Dumpable;
import com.android.systemui.statusbar.NotificationPresenter;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class VisualStabilityManager implements OnHeadsUpChangedListener, Dumpable {
    private boolean mGroupChangedAllowed;
    private final Handler mHandler;
    private boolean mIsTemporaryReorderingAllowed;
    private boolean mPanelExpanded;
    private boolean mPulsing;
    private boolean mReorderingAllowed;
    private boolean mScreenOn;
    private long mTemporaryReorderingStart;
    private VisibilityLocationProvider mVisibilityLocationProvider;
    private final ArrayList<Callback> mReorderingAllowedCallbacks = new ArrayList<>();
    private final ArraySet<Callback> mPersistentReorderingCallbacks = new ArraySet<>();
    private final ArrayList<Callback> mGroupChangesAllowedCallbacks = new ArrayList<>();
    private final ArraySet<Callback> mPersistentGroupCallbacks = new ArraySet<>();
    private ArraySet<View> mAllowedReorderViews = new ArraySet<>();
    private ArraySet<NotificationEntry> mLowPriorityReorderingViews = new ArraySet<>();
    private ArraySet<View> mAddedChildren = new ArraySet<>();
    private final Runnable mOnTemporaryReorderingExpired = new Runnable() { // from class: com.android.systemui.statusbar.notification.VisualStabilityManager$$ExternalSyntheticLambda0
        @Override // java.lang.Runnable
        public final void run() {
            this.f$0.lambda$new$0();
        }
    };

    public interface Callback {
        void onChangeAllowed();
    }

    public void setUpWithPresenter(NotificationPresenter notificationPresenter) {
    }

    public VisualStabilityManager(NotificationEntryManager notificationEntryManager, Handler handler) {
        this.mHandler = handler;
        notificationEntryManager.addNotificationEntryListener(new NotificationEntryListener() { // from class: com.android.systemui.statusbar.notification.VisualStabilityManager.1
            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onPreEntryUpdated(NotificationEntry notificationEntry) {
                if (notificationEntry.isAmbient() != notificationEntry.getRow().isLowPriority()) {
                    VisualStabilityManager.this.mLowPriorityReorderingViews.add(notificationEntry);
                }
            }
        });
    }

    public void addReorderingAllowedCallback(Callback callback, boolean z) {
        if (z) {
            this.mPersistentReorderingCallbacks.add(callback);
        }
        if (this.mReorderingAllowedCallbacks.contains(callback)) {
            return;
        }
        this.mReorderingAllowedCallbacks.add(callback);
    }

    public void addGroupChangesAllowedCallback(Callback callback, boolean z) {
        if (z) {
            this.mPersistentGroupCallbacks.add(callback);
        }
        if (this.mGroupChangesAllowedCallbacks.contains(callback)) {
            return;
        }
        this.mGroupChangesAllowedCallbacks.add(callback);
    }

    public void setPanelExpanded(boolean z) {
        this.mPanelExpanded = z;
        updateAllowedStates();
    }

    public void setScreenOn(boolean z) {
        this.mScreenOn = z;
        updateAllowedStates();
    }

    public void setPulsing(boolean z) {
        if (this.mPulsing == z) {
            return;
        }
        this.mPulsing = z;
        updateAllowedStates();
    }

    private void updateAllowedStates() {
        boolean z = ((this.mScreenOn && this.mPanelExpanded && !this.mIsTemporaryReorderingAllowed) || this.mPulsing) ? false : true;
        boolean z2 = z && !this.mReorderingAllowed;
        this.mReorderingAllowed = z;
        if (z2) {
            notifyChangeAllowed(this.mReorderingAllowedCallbacks, this.mPersistentReorderingCallbacks);
        }
        boolean z3 = ((this.mScreenOn && this.mPanelExpanded) || this.mPulsing) ? false : true;
        boolean z4 = z3 && !this.mGroupChangedAllowed;
        this.mGroupChangedAllowed = z3;
        if (z4) {
            notifyChangeAllowed(this.mGroupChangesAllowedCallbacks, this.mPersistentGroupCallbacks);
        }
    }

    private void notifyChangeAllowed(ArrayList<Callback> arrayList, ArraySet<Callback> arraySet) {
        int i = 0;
        while (i < arrayList.size()) {
            Callback callback = arrayList.get(i);
            callback.onChangeAllowed();
            if (!arraySet.contains(callback)) {
                arrayList.remove(callback);
                i--;
            }
            i++;
        }
    }

    public boolean isReorderingAllowed() {
        return this.mReorderingAllowed;
    }

    public boolean areGroupChangesAllowed() {
        return this.mGroupChangedAllowed;
    }

    public boolean canReorderNotification(ExpandableNotificationRow expandableNotificationRow) {
        if (this.mReorderingAllowed || this.mAddedChildren.contains(expandableNotificationRow) || this.mLowPriorityReorderingViews.contains(expandableNotificationRow.getEntry())) {
            return true;
        }
        return this.mAllowedReorderViews.contains(expandableNotificationRow) && !this.mVisibilityLocationProvider.isInVisibleLocation(expandableNotificationRow.getEntry());
    }

    public void setVisibilityLocationProvider(VisibilityLocationProvider visibilityLocationProvider) {
        this.mVisibilityLocationProvider = visibilityLocationProvider;
    }

    public void onReorderingFinished() {
        this.mAllowedReorderViews.clear();
        this.mAddedChildren.clear();
        this.mLowPriorityReorderingViews.clear();
    }

    @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
    public void onHeadsUpStateChanged(NotificationEntry notificationEntry, boolean z) {
        if (z) {
            this.mAllowedReorderViews.add(notificationEntry.getRow());
        }
    }

    public void temporarilyAllowReordering() {
        this.mHandler.removeCallbacks(this.mOnTemporaryReorderingExpired);
        this.mHandler.postDelayed(this.mOnTemporaryReorderingExpired, 1000L);
        if (!this.mIsTemporaryReorderingAllowed) {
            this.mTemporaryReorderingStart = SystemClock.elapsedRealtime();
        }
        this.mIsTemporaryReorderingAllowed = true;
        updateAllowedStates();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0() {
        this.mIsTemporaryReorderingAllowed = false;
        updateAllowedStates();
    }

    public void notifyViewAddition(View view) {
        this.mAddedChildren.add(view);
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("VisualStabilityManager state:");
        printWriter.print("  mIsTemporaryReorderingAllowed=");
        printWriter.println(this.mIsTemporaryReorderingAllowed);
        printWriter.print("  mTemporaryReorderingStart=");
        printWriter.println(this.mTemporaryReorderingStart);
        long jElapsedRealtime = SystemClock.elapsedRealtime();
        printWriter.print("    Temporary reordering window has been open for ");
        printWriter.print(jElapsedRealtime - (this.mIsTemporaryReorderingAllowed ? this.mTemporaryReorderingStart : jElapsedRealtime));
        printWriter.println("ms");
        printWriter.println();
    }
}
