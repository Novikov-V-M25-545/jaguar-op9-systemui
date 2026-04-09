package com.android.systemui.bubbles;

import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.R;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.bubbles.BubbleLogger;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;

/* loaded from: classes.dex */
public class BubbleData {
    private static final Comparator<Bubble> BUBBLES_BY_SORT_KEY_DESCENDING = Comparator.comparing(new Function() { // from class: com.android.systemui.bubbles.BubbleData$$ExternalSyntheticLambda5
        @Override // java.util.function.Function
        public final Object apply(Object obj) {
            return Long.valueOf(BubbleData.sortKey((Bubble) obj));
        }
    }).reversed();
    private final List<Bubble> mBubbles;
    private BubbleController.PendingIntentCanceledListener mCancelledListener;
    private final Context mContext;
    private boolean mExpanded;
    private Listener mListener;
    private final int mMaxBubbles;
    private int mMaxOverflowBubbles;
    private final List<Bubble> mOverflowBubbles;
    private final HashMap<String, Bubble> mPendingBubbles;
    private Bubble mSelectedBubble;
    private boolean mShowingOverflow;
    private Update mStateChange;
    private BubbleController.NotificationSuppressionChangedListener mSuppressionListener;
    private BubbleLogger mLogger = new BubbleLoggerImpl();
    private TimeSource mTimeSource = new TimeSource() { // from class: com.android.systemui.bubbles.BubbleData$$ExternalSyntheticLambda0
        @Override // com.android.systemui.bubbles.BubbleData.TimeSource
        public final long currentTimeMillis() {
            return System.currentTimeMillis();
        }
    };
    private HashMap<String, String> mSuppressedGroupKeys = new HashMap<>();

    interface Listener {
        void applyUpdate(Update update);
    }

    interface TimeSource {
        long currentTimeMillis();
    }

    static final class Update {
        Bubble addedBubble;
        final List<Bubble> bubbles;
        boolean expanded;
        boolean expandedChanged;
        boolean orderChanged;
        final List<Bubble> overflowBubbles;
        final List<Pair<Bubble, Integer>> removedBubbles;
        Bubble selectedBubble;
        boolean selectionChanged;
        Bubble updatedBubble;

        private Update(List<Bubble> list, List<Bubble> list2) {
            this.removedBubbles = new ArrayList();
            this.bubbles = Collections.unmodifiableList(list);
            this.overflowBubbles = Collections.unmodifiableList(list2);
        }

        boolean anythingChanged() {
            return this.expandedChanged || this.selectionChanged || this.addedBubble != null || this.updatedBubble != null || !this.removedBubbles.isEmpty() || this.orderChanged;
        }

        void bubbleRemoved(Bubble bubble, int i) {
            this.removedBubbles.add(new Pair<>(bubble, Integer.valueOf(i)));
        }
    }

    public BubbleData(Context context) {
        this.mContext = context;
        ArrayList arrayList = new ArrayList();
        this.mBubbles = arrayList;
        ArrayList arrayList2 = new ArrayList();
        this.mOverflowBubbles = arrayList2;
        this.mPendingBubbles = new HashMap<>();
        this.mStateChange = new Update(arrayList, arrayList2);
        this.mMaxBubbles = context.getResources().getInteger(R.integer.bubbles_max_rendered);
        this.mMaxOverflowBubbles = context.getResources().getInteger(R.integer.bubbles_max_overflow);
    }

    public void setSuppressionChangedListener(BubbleController.NotificationSuppressionChangedListener notificationSuppressionChangedListener) {
        this.mSuppressionListener = notificationSuppressionChangedListener;
    }

    public void setPendingIntentCancelledListener(BubbleController.PendingIntentCanceledListener pendingIntentCanceledListener) {
        this.mCancelledListener = pendingIntentCanceledListener;
    }

    public boolean hasBubbles() {
        return !this.mBubbles.isEmpty();
    }

    public boolean isExpanded() {
        return this.mExpanded;
    }

    public boolean hasAnyBubbleWithKey(String str) {
        return hasBubbleInStackWithKey(str) || hasOverflowBubbleWithKey(str);
    }

    public boolean hasBubbleInStackWithKey(String str) {
        return getBubbleInStackWithKey(str) != null;
    }

    public boolean hasOverflowBubbleWithKey(String str) {
        return getOverflowBubbleWithKey(str) != null;
    }

    public Bubble getSelectedBubble() {
        return this.mSelectedBubble;
    }

    public void setExpanded(boolean z) {
        setExpandedInternal(z);
        dispatchPendingChanges();
    }

    public void setSelectedBubble(Bubble bubble) {
        setSelectedBubbleInternal(bubble);
        dispatchPendingChanges();
    }

    void setShowingOverflow(boolean z) {
        this.mShowingOverflow = z;
    }

    Bubble getOrCreateBubble(NotificationEntry notificationEntry, Bubble bubble) {
        String key = notificationEntry != null ? notificationEntry.getKey() : bubble.getKey();
        Bubble bubbleInStackWithKey = getBubbleInStackWithKey(key);
        if (bubbleInStackWithKey != null) {
            bubble = bubbleInStackWithKey;
        } else {
            bubbleInStackWithKey = getOverflowBubbleWithKey(key);
            if (bubbleInStackWithKey != null) {
                this.mOverflowBubbles.remove(bubbleInStackWithKey);
                bubble = bubbleInStackWithKey;
            } else if (this.mPendingBubbles.containsKey(key)) {
                bubble = this.mPendingBubbles.get(key);
            } else if (notificationEntry != null) {
                bubble = new Bubble(notificationEntry, this.mSuppressionListener, this.mCancelledListener);
            }
        }
        if (notificationEntry != null) {
            bubble.setEntry(notificationEntry);
        }
        this.mPendingBubbles.put(key, bubble);
        return bubble;
    }

    void notificationEntryUpdated(Bubble bubble, boolean z, boolean z2) {
        this.mPendingBubbles.remove(bubble.getKey());
        Bubble bubbleInStackWithKey = getBubbleInStackWithKey(bubble.getKey());
        boolean z3 = z | (!bubble.isVisuallyInterruptive());
        if (bubbleInStackWithKey == null) {
            bubble.setSuppressFlyout(z3);
            doAdd(bubble);
            trim();
        } else {
            bubble.setSuppressFlyout(z3);
            doUpdate(bubble, !z3);
        }
        if (bubble.shouldAutoExpand()) {
            bubble.setShouldAutoExpand(false);
            setSelectedBubbleInternal(bubble);
            if (!this.mExpanded) {
                setExpandedInternal(true);
            }
        }
        boolean z4 = this.mExpanded && this.mSelectedBubble == bubble;
        bubble.setSuppressNotification((!z4 && z2 && bubble.showInShade()) ? false : true);
        bubble.setShowDot(!z4);
        dispatchPendingChanges();
    }

    public void dismissBubbleWithKey(String str, int i) {
        doRemove(str, i);
        dispatchPendingChanges();
    }

    void addSummaryToSuppress(String str, String str2) {
        this.mSuppressedGroupKeys.put(str, str2);
    }

    String getSummaryKey(String str) {
        return this.mSuppressedGroupKeys.get(str);
    }

    void removeSuppressedSummary(String str) {
        this.mSuppressedGroupKeys.remove(str);
    }

    boolean isSummarySuppressed(String str) {
        return this.mSuppressedGroupKeys.containsKey(str);
    }

    ArrayList<Bubble> getBubblesInGroup(String str, NotificationEntryManager notificationEntryManager) {
        ArrayList<Bubble> arrayList = new ArrayList<>();
        if (str == null) {
            return arrayList;
        }
        for (Bubble bubble : this.mBubbles) {
            NotificationEntry pendingOrActiveNotif = notificationEntryManager.getPendingOrActiveNotif(bubble.getKey());
            if (pendingOrActiveNotif != null && str.equals(pendingOrActiveNotif.getSbn().getGroupKey())) {
                arrayList.add(bubble);
            }
        }
        return arrayList;
    }

    public void removeBubblesWithInvalidShortcuts(final String str, List<ShortcutInfo> list, final int i) {
        final HashSet hashSet = new HashSet();
        Iterator<ShortcutInfo> it = list.iterator();
        while (it.hasNext()) {
            hashSet.add(it.next().getId());
        }
        Predicate<Bubble> predicate = new Predicate() { // from class: com.android.systemui.bubbles.BubbleData$$ExternalSyntheticLambda8
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return BubbleData.lambda$removeBubblesWithInvalidShortcuts$0(str, hashSet, (Bubble) obj);
            }
        };
        Consumer<Bubble> consumer = new Consumer() { // from class: com.android.systemui.bubbles.BubbleData$$ExternalSyntheticLambda2
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.lambda$removeBubblesWithInvalidShortcuts$1(i, (Bubble) obj);
            }
        };
        performActionOnBubblesMatching(getBubbles(), predicate, consumer);
        performActionOnBubblesMatching(getOverflowBubbles(), predicate, consumer);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$removeBubblesWithInvalidShortcuts$0(String str, Set set, Bubble bubble) {
        boolean zEquals = str.equals(bubble.getPackageName());
        boolean zHasMetadataShortcutId = bubble.hasMetadataShortcutId();
        if (zEquals && zHasMetadataShortcutId) {
            return zEquals && !(bubble.hasMetadataShortcutId() && bubble.getShortcutInfo() != null && bubble.getShortcutInfo().isEnabled() && set.contains(bubble.getShortcutInfo().getId()));
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$removeBubblesWithInvalidShortcuts$1(int i, Bubble bubble) {
        dismissBubbleWithKey(bubble.getKey(), i);
    }

    public void removeBubblesWithPackageName(final String str, final int i) {
        Predicate<Bubble> predicate = new Predicate() { // from class: com.android.systemui.bubbles.BubbleData$$ExternalSyntheticLambda7
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return BubbleData.lambda$removeBubblesWithPackageName$2(str, (Bubble) obj);
            }
        };
        Consumer<Bubble> consumer = new Consumer() { // from class: com.android.systemui.bubbles.BubbleData$$ExternalSyntheticLambda3
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.lambda$removeBubblesWithPackageName$3(i, (Bubble) obj);
            }
        };
        performActionOnBubblesMatching(getBubbles(), predicate, consumer);
        performActionOnBubblesMatching(getOverflowBubbles(), predicate, consumer);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$removeBubblesWithPackageName$2(String str, Bubble bubble) {
        return bubble.getPackageName().equals(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$removeBubblesWithPackageName$3(int i, Bubble bubble) {
        dismissBubbleWithKey(bubble.getKey(), i);
    }

    private void doAdd(Bubble bubble) {
        this.mBubbles.add(0, bubble);
        Update update = this.mStateChange;
        update.addedBubble = bubble;
        update.orderChanged = this.mBubbles.size() > 1;
        if (isExpanded()) {
            return;
        }
        setSelectedBubbleInternal(this.mBubbles.get(0));
    }

    private void trim() {
        if (this.mBubbles.size() > this.mMaxBubbles) {
            this.mBubbles.stream().sorted(Comparator.comparingLong(new ToLongFunction() { // from class: com.android.systemui.bubbles.BubbleData$$ExternalSyntheticLambda9
                @Override // java.util.function.ToLongFunction
                public final long applyAsLong(Object obj) {
                    return ((Bubble) obj).getLastActivity();
                }
            })).filter(new Predicate() { // from class: com.android.systemui.bubbles.BubbleData$$ExternalSyntheticLambda6
                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return this.f$0.lambda$trim$4((Bubble) obj);
                }
            }).findFirst().ifPresent(new Consumer() { // from class: com.android.systemui.bubbles.BubbleData$$ExternalSyntheticLambda1
                @Override // java.util.function.Consumer
                public final void accept(Object obj) throws PendingIntent.CanceledException {
                    this.f$0.lambda$trim$5((Bubble) obj);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$trim$4(Bubble bubble) {
        return !bubble.equals(this.mSelectedBubble);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$trim$5(Bubble bubble) throws PendingIntent.CanceledException {
        doRemove(bubble.getKey(), 2);
    }

    private void doUpdate(Bubble bubble, boolean z) {
        this.mStateChange.updatedBubble = bubble;
        if (isExpanded() || !z) {
            return;
        }
        int iIndexOf = this.mBubbles.indexOf(bubble);
        this.mBubbles.remove(bubble);
        this.mBubbles.add(0, bubble);
        this.mStateChange.orderChanged = iIndexOf != 0;
        setSelectedBubbleInternal(this.mBubbles.get(0));
    }

    private void performActionOnBubblesMatching(List<Bubble> list, Predicate<Bubble> predicate, Consumer<Bubble> consumer) {
        ArrayList arrayList = new ArrayList();
        for (Bubble bubble : list) {
            if (predicate.test(bubble)) {
                arrayList.add(bubble);
            }
        }
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            consumer.accept((Bubble) it.next());
        }
    }

    private void doRemove(String str, int i) throws PendingIntent.CanceledException {
        if (this.mPendingBubbles.containsKey(str)) {
            this.mPendingBubbles.remove(str);
        }
        int iIndexForKey = indexForKey(str);
        if (iIndexForKey == -1) {
            if (hasOverflowBubbleWithKey(str)) {
                if (i == 5 || i == 9 || i == 7 || i == 4 || i == 12 || i == 13) {
                    Bubble overflowBubbleWithKey = getOverflowBubbleWithKey(str);
                    if (overflowBubbleWithKey != null) {
                        overflowBubbleWithKey.stopInflation();
                    }
                    this.mLogger.logOverflowRemove(overflowBubbleWithKey, i);
                    this.mStateChange.bubbleRemoved(overflowBubbleWithKey, i);
                    this.mOverflowBubbles.remove(overflowBubbleWithKey);
                    return;
                }
                return;
            }
            return;
        }
        Bubble bubble = this.mBubbles.get(iIndexForKey);
        bubble.stopInflation();
        if (this.mBubbles.size() == 1) {
            setExpandedInternal(false);
            this.mSelectedBubble = null;
        }
        if (iIndexForKey < this.mBubbles.size() - 1) {
            this.mStateChange.orderChanged = true;
        }
        this.mBubbles.remove(iIndexForKey);
        this.mStateChange.bubbleRemoved(bubble, i);
        if (!isExpanded()) {
            this.mStateChange.orderChanged |= repackAll();
        }
        overflowBubble(i, bubble);
        if (Objects.equals(this.mSelectedBubble, bubble)) {
            setSelectedBubbleInternal(this.mBubbles.get(Math.min(iIndexForKey, this.mBubbles.size() - 1)));
        }
        maybeSendDeleteIntent(i, bubble);
    }

    void overflowBubble(int i, Bubble bubble) {
        if (bubble.getPendingIntentCanceled()) {
            return;
        }
        if (i == 2 || i == 1) {
            this.mLogger.logOverflowAdd(bubble, i);
            this.mOverflowBubbles.add(0, bubble);
            bubble.stopInflation();
            if (this.mOverflowBubbles.size() == this.mMaxOverflowBubbles + 1) {
                List<Bubble> list = this.mOverflowBubbles;
                Bubble bubble2 = list.get(list.size() - 1);
                this.mStateChange.bubbleRemoved(bubble2, 11);
                this.mLogger.log(bubble, BubbleLogger.Event.BUBBLE_OVERFLOW_REMOVE_MAX_REACHED);
                this.mOverflowBubbles.remove(bubble2);
            }
        }
    }

    public void dismissAll(int i) {
        if (this.mBubbles.isEmpty()) {
            return;
        }
        setExpandedInternal(false);
        setSelectedBubbleInternal(null);
        while (!this.mBubbles.isEmpty()) {
            doRemove(this.mBubbles.get(0).getKey(), i);
        }
        dispatchPendingChanges();
    }

    void notifyDisplayEmpty(int i) {
        for (Bubble bubble : this.mBubbles) {
            if (bubble.getDisplayId() == i) {
                if (bubble.getExpandedView() != null) {
                    bubble.getExpandedView().notifyDisplayEmpty();
                    return;
                }
                return;
            }
        }
    }

    private void dispatchPendingChanges() {
        if (this.mListener != null && this.mStateChange.anythingChanged()) {
            this.mListener.applyUpdate(this.mStateChange);
        }
        this.mStateChange = new Update(this.mBubbles, this.mOverflowBubbles);
    }

    private void setSelectedBubbleInternal(Bubble bubble) {
        if (this.mShowingOverflow || !Objects.equals(bubble, this.mSelectedBubble)) {
            if (bubble != null && !this.mBubbles.contains(bubble) && !this.mOverflowBubbles.contains(bubble)) {
                Log.e("Bubbles", "Cannot select bubble which doesn't exist! (" + bubble + ") bubbles=" + this.mBubbles);
                return;
            }
            if (this.mExpanded && bubble != null) {
                bubble.markAsAccessedAt(this.mTimeSource.currentTimeMillis());
            }
            this.mSelectedBubble = bubble;
            Update update = this.mStateChange;
            update.selectedBubble = bubble;
            update.selectionChanged = true;
        }
    }

    private void setExpandedInternal(boolean z) {
        if (this.mExpanded == z) {
            return;
        }
        if (z) {
            if (this.mBubbles.isEmpty()) {
                Log.e("Bubbles", "Attempt to expand stack when empty!");
                return;
            }
            Bubble bubble = this.mSelectedBubble;
            if (bubble == null) {
                Log.e("Bubbles", "Attempt to expand stack without selected bubble!");
                return;
            } else {
                bubble.markAsAccessedAt(this.mTimeSource.currentTimeMillis());
                this.mStateChange.orderChanged |= repackAll();
            }
        } else if (!this.mBubbles.isEmpty()) {
            this.mStateChange.orderChanged |= repackAll();
            if (this.mShowingOverflow) {
                setSelectedBubbleInternal(this.mSelectedBubble);
            }
            if (this.mBubbles.indexOf(this.mSelectedBubble) > 0 && this.mBubbles.indexOf(this.mSelectedBubble) != 0) {
                this.mBubbles.remove(this.mSelectedBubble);
                this.mBubbles.add(0, this.mSelectedBubble);
                this.mStateChange.orderChanged = true;
            }
        }
        this.mExpanded = z;
        Update update = this.mStateChange;
        update.expanded = z;
        update.expandedChanged = true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static long sortKey(Bubble bubble) {
        return bubble.getLastActivity();
    }

    private boolean repackAll() {
        if (this.mBubbles.isEmpty()) {
            return false;
        }
        final ArrayList arrayList = new ArrayList(this.mBubbles.size());
        this.mBubbles.stream().sorted(BUBBLES_BY_SORT_KEY_DESCENDING).forEachOrdered(new Consumer() { // from class: com.android.systemui.bubbles.BubbleData$$ExternalSyntheticLambda4
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                arrayList.add((Bubble) obj);
            }
        });
        if (arrayList.equals(this.mBubbles)) {
            return false;
        }
        this.mBubbles.clear();
        this.mBubbles.addAll(arrayList);
        return true;
    }

    private void maybeSendDeleteIntent(int i, Bubble bubble) throws PendingIntent.CanceledException {
        PendingIntent deleteIntent;
        if (i == 1 && (deleteIntent = bubble.getDeleteIntent()) != null) {
            try {
                deleteIntent.send();
            } catch (PendingIntent.CanceledException unused) {
                Log.w("Bubbles", "Failed to send delete intent for bubble with key: " + bubble.getKey());
            }
        }
    }

    private int indexForKey(String str) {
        for (int i = 0; i < this.mBubbles.size(); i++) {
            if (this.mBubbles.get(i).getKey().equals(str)) {
                return i;
            }
        }
        return -1;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public List<Bubble> getBubbles() {
        return Collections.unmodifiableList(this.mBubbles);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    List<Bubble> getOverflowBubbles() {
        return Collections.unmodifiableList(this.mOverflowBubbles);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    Bubble getAnyBubbleWithkey(String str) {
        Bubble bubbleInStackWithKey = getBubbleInStackWithKey(str);
        return bubbleInStackWithKey == null ? getOverflowBubbleWithKey(str) : bubbleInStackWithKey;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    Bubble getBubbleInStackWithKey(String str) {
        for (int i = 0; i < this.mBubbles.size(); i++) {
            Bubble bubble = this.mBubbles.get(i);
            if (bubble.getKey().equals(str)) {
                return bubble;
            }
        }
        return null;
    }

    Bubble getBubbleWithView(View view) {
        for (int i = 0; i < this.mBubbles.size(); i++) {
            Bubble bubble = this.mBubbles.get(i);
            if (bubble.getIconView() != null && bubble.getIconView().equals(view)) {
                return bubble;
            }
        }
        return null;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    Bubble getOverflowBubbleWithKey(String str) {
        for (int i = 0; i < this.mOverflowBubbles.size(); i++) {
            Bubble bubble = this.mOverflowBubbles.get(i);
            if (bubble.getKey().equals(str)) {
                return bubble;
            }
        }
        return null;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    void setTimeSource(TimeSource timeSource) {
        this.mTimeSource = timeSource;
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    @VisibleForTesting
    void setMaxOverflowBubbles(int i) {
        this.mMaxOverflowBubbles = i;
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print("selected: ");
        Bubble bubble = this.mSelectedBubble;
        printWriter.println(bubble != null ? bubble.getKey() : "null");
        printWriter.print("expanded: ");
        printWriter.println(this.mExpanded);
        printWriter.print("count:    ");
        printWriter.println(this.mBubbles.size());
        Iterator<Bubble> it = this.mBubbles.iterator();
        while (it.hasNext()) {
            it.next().dump(fileDescriptor, printWriter, strArr);
        }
        printWriter.print("summaryKeys: ");
        printWriter.println(this.mSuppressedGroupKeys.size());
        Iterator<String> it2 = this.mSuppressedGroupKeys.keySet().iterator();
        while (it2.hasNext()) {
            printWriter.println("   suppressing: " + it2.next());
        }
    }
}
