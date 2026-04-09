package com.android.systemui.util.leak;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.util.LongSparseArray;
import com.android.systemui.Dumpable;
import com.android.systemui.R;
import com.android.systemui.SystemUI;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class GarbageMonitor implements Dumpable {
    private static final boolean DEBUG;
    private static final boolean ENABLE_AM_HEAP_LIMIT;
    public static final boolean HEAP_TRACKING_ENABLED;
    public static final boolean LEAK_REPORTING_ENABLED;
    private final ActivityManager mAm;
    private final Context mContext;
    private DumpTruck mDumpTruck;
    private final Handler mHandler;
    private long mHeapLimit;
    private final LeakReporter mLeakReporter;
    private MemoryTile mQSTile;
    private final TrackedGarbage mTrackedGarbage;
    private final LongSparseArray<ProcessMemInfo> mData = new LongSparseArray<>();
    private final ArrayList<Long> mPids = new ArrayList<>();

    static {
        LEAK_REPORTING_ENABLED = Build.IS_DEBUGGABLE && SystemProperties.getBoolean("debug.enable_leak_reporting", false);
        boolean z = Build.IS_DEBUGGABLE;
        HEAP_TRACKING_ENABLED = z;
        ENABLE_AM_HEAP_LIMIT = z && SystemProperties.getBoolean("debug.enable_sysui_heap_limit", false);
        DEBUG = Log.isLoggable("GarbageMonitor", 3);
    }

    public GarbageMonitor(Context context, Looper looper, LeakDetector leakDetector, LeakReporter leakReporter) {
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        this.mAm = (ActivityManager) context.getSystemService("activity");
        this.mHandler = new BackgroundHeapCheckHandler(looper);
        this.mTrackedGarbage = leakDetector.getTrackedGarbage();
        this.mLeakReporter = leakReporter;
        this.mDumpTruck = new DumpTruck(applicationContext);
        if (ENABLE_AM_HEAP_LIMIT) {
            this.mHeapLimit = Settings.Global.getInt(context.getContentResolver(), "systemui_am_heap_limit", applicationContext.getResources().getInteger(R.integer.watch_heap_limit));
        }
    }

    public void startLeakMonitor() {
        if (this.mTrackedGarbage == null) {
            return;
        }
        this.mHandler.sendEmptyMessage(1000);
    }

    public void startHeapTracking() {
        startTrackingProcess(Process.myPid(), this.mContext.getPackageName(), System.currentTimeMillis());
        this.mHandler.sendEmptyMessage(3000);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean gcAndCheckGarbage() {
        if (this.mTrackedGarbage.countOldGarbage() <= 5) {
            return false;
        }
        Runtime.getRuntime().gc();
        return true;
    }

    void reinspectGarbageAfterGc() throws IOException {
        int iCountOldGarbage = this.mTrackedGarbage.countOldGarbage();
        if (iCountOldGarbage > 5) {
            this.mLeakReporter.dumpLeak(iCountOldGarbage);
        }
    }

    public ProcessMemInfo getMemInfo(int i) {
        return this.mData.get(i);
    }

    public List<Long> getTrackedProcesses() {
        return this.mPids;
    }

    public void startTrackingProcess(long j, String str, long j2) {
        synchronized (this.mPids) {
            if (this.mPids.contains(Long.valueOf(j))) {
                return;
            }
            this.mPids.add(Long.valueOf(j));
            logPids();
            this.mData.put(j, new ProcessMemInfo(j, str, j2));
        }
    }

    private void logPids() {
        if (DEBUG) {
            StringBuffer stringBuffer = new StringBuffer("Now tracking processes: ");
            for (int i = 0; i < this.mPids.size(); i++) {
                this.mPids.get(i).intValue();
                stringBuffer.append(" ");
            }
            Log.v("GarbageMonitor", stringBuffer.toString());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void update() {
        synchronized (this.mPids) {
            int i = 0;
            while (true) {
                if (i >= this.mPids.size()) {
                    break;
                }
                int iIntValue = this.mPids.get(i).intValue();
                long[] rss = Process.getRss(iIntValue);
                if (rss == null && rss.length == 0) {
                    if (DEBUG) {
                        Log.e("GarbageMonitor", "update: Process.getRss() didn't provide any values.");
                    }
                } else {
                    long j = rss[0];
                    long j2 = iIntValue;
                    ProcessMemInfo processMemInfo = this.mData.get(j2);
                    long[] jArr = processMemInfo.rss;
                    int i2 = processMemInfo.head;
                    processMemInfo.currentRss = j;
                    jArr[i2] = j;
                    processMemInfo.head = (i2 + 1) % jArr.length;
                    if (j > processMemInfo.max) {
                        processMemInfo.max = j;
                    }
                    if (j == 0) {
                        if (DEBUG) {
                            Log.v("GarbageMonitor", "update: pid " + iIntValue + " has rss=0, it probably died");
                        }
                        this.mData.remove(j2);
                    }
                    i++;
                }
            }
            for (int size = this.mPids.size() - 1; size >= 0; size--) {
                if (this.mData.get(this.mPids.get(size).intValue()) == null) {
                    this.mPids.remove(size);
                    logPids();
                }
            }
        }
        MemoryTile memoryTile = this.mQSTile;
        if (memoryTile != null) {
            memoryTile.update();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setTile(MemoryTile memoryTile) {
        this.mQSTile = memoryTile;
        if (memoryTile != null) {
            memoryTile.update();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String formatBytes(long j) {
        String[] strArr = {"B", "K", "M", "G", "T"};
        int i = 0;
        while (i < 5 && j >= 1024) {
            j /= 1024;
            i++;
        }
        return j + strArr[i];
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Intent dumpHprofAndGetShareIntent() {
        return this.mDumpTruck.captureHeaps(getTrackedProcesses()).createShareIntent();
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("GarbageMonitor params:");
        printWriter.println(String.format("   mHeapLimit=%d KB", Long.valueOf(this.mHeapLimit)));
        printWriter.println(String.format("   GARBAGE_INSPECTION_INTERVAL=%d (%.1f mins)", 900000L, Float.valueOf(15.0f)));
        printWriter.println(String.format("   HEAP_TRACK_INTERVAL=%d (%.1f mins)", 60000L, Float.valueOf(1.0f)));
        printWriter.println(String.format("   HEAP_TRACK_HISTORY_LEN=%d (%.1f hr total)", 720, Float.valueOf(12.0f)));
        printWriter.println("GarbageMonitor tracked processes:");
        Iterator<Long> it = this.mPids.iterator();
        while (it.hasNext()) {
            ProcessMemInfo processMemInfo = this.mData.get(it.next().longValue());
            if (processMemInfo != null) {
                processMemInfo.dump(fileDescriptor, printWriter, strArr);
            }
        }
    }

    private static class MemoryIconDrawable extends Drawable {
        final Drawable baseIcon;
        final float dp;
        long limit;
        final Paint paint;
        long rss;

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -3;
        }

        MemoryIconDrawable(Context context) {
            Paint paint = new Paint();
            this.paint = paint;
            this.baseIcon = context.getDrawable(R.drawable.ic_memory).mutate();
            this.dp = context.getResources().getDisplayMetrics().density;
            paint.setColor(QSTileImpl.getColorForState(context, 2));
        }

        public void setRss(long j) {
            if (j != this.rss) {
                this.rss = j;
                invalidateSelf();
            }
        }

        public void setLimit(long j) {
            if (j != this.limit) {
                this.limit = j;
                invalidateSelf();
            }
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            this.baseIcon.draw(canvas);
            long j = this.limit;
            if (j > 0) {
                long j2 = this.rss;
                if (j2 > 0) {
                    float fMin = Math.min(1.0f, j2 / j);
                    float f = getBounds().left;
                    float f2 = this.dp;
                    canvas.translate(f + (f2 * 8.0f), r2.top + (f2 * 5.0f));
                    float f3 = this.dp;
                    canvas.drawRect(0.0f, f3 * 14.0f * (1.0f - fMin), (8.0f * f3) + 1.0f, (f3 * 14.0f) + 1.0f, this.paint);
                }
            }
        }

        @Override // android.graphics.drawable.Drawable
        public void setBounds(int i, int i2, int i3, int i4) {
            super.setBounds(i, i2, i3, i4);
            this.baseIcon.setBounds(i, i2, i3, i4);
        }

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicHeight() {
            return this.baseIcon.getIntrinsicHeight();
        }

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicWidth() {
            return this.baseIcon.getIntrinsicWidth();
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int i) {
            this.baseIcon.setAlpha(i);
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
            this.baseIcon.setColorFilter(colorFilter);
            this.paint.setColorFilter(colorFilter);
        }

        @Override // android.graphics.drawable.Drawable
        public void setTint(int i) {
            super.setTint(i);
            this.baseIcon.setTint(i);
        }

        @Override // android.graphics.drawable.Drawable
        public void setTintList(ColorStateList colorStateList) {
            super.setTintList(colorStateList);
            this.baseIcon.setTintList(colorStateList);
        }

        @Override // android.graphics.drawable.Drawable
        public void setTintMode(PorterDuff.Mode mode) {
            super.setTintMode(mode);
            this.baseIcon.setTintMode(mode);
        }
    }

    private static class MemoryGraphIcon extends QSTile.Icon {
        long limit;
        long rss;

        private MemoryGraphIcon() {
        }

        public void setRss(long j) {
            this.rss = j;
        }

        public void setHeapLimit(long j) {
            this.limit = j;
        }

        @Override // com.android.systemui.plugins.qs.QSTile.Icon
        public Drawable getDrawable(Context context) {
            MemoryIconDrawable memoryIconDrawable = new MemoryIconDrawable(context);
            memoryIconDrawable.setRss(this.rss);
            memoryIconDrawable.setLimit(this.limit);
            return memoryIconDrawable;
        }
    }

    public static class MemoryTile extends QSTileImpl<QSTile.State> {
        private boolean dumpInProgress;
        private final GarbageMonitor gm;
        private final ActivityStarter mActivityStarter;
        private ProcessMemInfo pmi;

        @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
        public int getMetricsCategory() {
            return 0;
        }

        public MemoryTile(QSHost qSHost, GarbageMonitor garbageMonitor, ActivityStarter activityStarter) {
            super(qSHost);
            this.gm = garbageMonitor;
            this.mActivityStarter = activityStarter;
        }

        @Override // com.android.systemui.qs.tileimpl.QSTileImpl
        public QSTile.State newTileState() {
            return new QSTile.State();
        }

        @Override // com.android.systemui.qs.tileimpl.QSTileImpl
        public Intent getLongClickIntent() {
            return new Intent();
        }

        @Override // com.android.systemui.qs.tileimpl.QSTileImpl
        protected void handleClick() {
            if (this.dumpInProgress) {
                return;
            }
            this.dumpInProgress = true;
            refreshState();
            new AnonymousClass1("HeapDumpThread").start();
        }

        /* renamed from: com.android.systemui.util.leak.GarbageMonitor$MemoryTile$1, reason: invalid class name */
        class AnonymousClass1 extends Thread {
            AnonymousClass1(String str) {
                super(str);
            }

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() throws InterruptedException {
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException unused) {
                }
                final Intent intentDumpHprofAndGetShareIntent = MemoryTile.this.gm.dumpHprofAndGetShareIntent();
                ((QSTileImpl) MemoryTile.this).mHandler.post(new Runnable() { // from class: com.android.systemui.util.leak.GarbageMonitor$MemoryTile$1$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$run$0(intentDumpHprofAndGetShareIntent);
                    }
                });
            }

            /* JADX INFO: Access modifiers changed from: private */
            public /* synthetic */ void lambda$run$0(Intent intent) {
                MemoryTile.this.dumpInProgress = false;
                MemoryTile.this.refreshState();
                MemoryTile.this.getHost().collapsePanels();
                MemoryTile.this.mActivityStarter.postStartActivityDismissingKeyguard(intent, 0);
            }
        }

        @Override // com.android.systemui.qs.tileimpl.QSTileImpl
        public void handleSetListening(boolean z) {
            super.handleSetListening(z);
            GarbageMonitor garbageMonitor = this.gm;
            if (garbageMonitor != null) {
                garbageMonitor.setTile(z ? this : null);
            }
            ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService(ActivityManager.class);
            if (z && this.gm.mHeapLimit > 0) {
                activityManager.setWatchHeapLimit(this.gm.mHeapLimit * 1024);
            } else {
                activityManager.clearWatchHeapLimit();
            }
        }

        @Override // com.android.systemui.plugins.qs.QSTile
        public CharSequence getTileLabel() {
            return getState().label;
        }

        @Override // com.android.systemui.qs.tileimpl.QSTileImpl
        protected void handleUpdateState(QSTile.State state, Object obj) {
            this.pmi = this.gm.getMemInfo(Process.myPid());
            MemoryGraphIcon memoryGraphIcon = new MemoryGraphIcon();
            memoryGraphIcon.setHeapLimit(this.gm.mHeapLimit);
            boolean z = this.dumpInProgress;
            state.state = z ? 0 : 2;
            state.label = z ? "Dumping..." : this.mContext.getString(R.string.heap_dump_tile_name);
            ProcessMemInfo processMemInfo = this.pmi;
            if (processMemInfo != null) {
                memoryGraphIcon.setRss(processMemInfo.currentRss);
                state.secondaryLabel = String.format("rss: %s / %s", GarbageMonitor.formatBytes(this.pmi.currentRss * 1024), GarbageMonitor.formatBytes(this.gm.mHeapLimit * 1024));
            } else {
                memoryGraphIcon.setRss(0L);
                state.secondaryLabel = null;
            }
            state.icon = memoryGraphIcon;
        }

        public void update() {
            refreshState();
        }
    }

    public static class ProcessMemInfo implements Dumpable {
        public long currentRss;
        public String name;
        public long pid;
        public long startTime;
        public long[] rss = new long[720];
        public long max = 1;
        public int head = 0;

        public ProcessMemInfo(long j, String str, long j2) {
            this.pid = j;
            this.name = str;
            this.startTime = j2;
        }

        public long getUptime() {
            return System.currentTimeMillis() - this.startTime;
        }

        @Override // com.android.systemui.Dumpable
        public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
            printWriter.print("{ \"pid\": ");
            printWriter.print(this.pid);
            printWriter.print(", \"name\": \"");
            printWriter.print(this.name.replace('\"', '-'));
            printWriter.print("\", \"start\": ");
            printWriter.print(this.startTime);
            printWriter.print(", \"rss\": [");
            for (int i = 0; i < this.rss.length; i++) {
                if (i > 0) {
                    printWriter.print(",");
                }
                long[] jArr = this.rss;
                printWriter.print(jArr[(this.head + i) % jArr.length]);
            }
            printWriter.println("] }");
        }
    }

    public static class Service extends SystemUI {
        private final GarbageMonitor mGarbageMonitor;

        public Service(Context context, GarbageMonitor garbageMonitor) {
            super(context);
            this.mGarbageMonitor = garbageMonitor;
        }

        @Override // com.android.systemui.SystemUI
        public void start() {
            boolean z = Settings.Secure.getInt(this.mContext.getContentResolver(), "sysui_force_enable_leak_reporting", 0) != 0;
            if (GarbageMonitor.LEAK_REPORTING_ENABLED || z) {
                this.mGarbageMonitor.startLeakMonitor();
            }
            if (GarbageMonitor.HEAP_TRACKING_ENABLED || z) {
                this.mGarbageMonitor.startHeapTracking();
            }
        }

        @Override // com.android.systemui.SystemUI, com.android.systemui.Dumpable
        public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
            GarbageMonitor garbageMonitor = this.mGarbageMonitor;
            if (garbageMonitor != null) {
                garbageMonitor.dump(fileDescriptor, printWriter, strArr);
            }
        }
    }

    private class BackgroundHeapCheckHandler extends Handler {
        BackgroundHeapCheckHandler(Looper looper) {
            super(looper);
            if (Looper.getMainLooper().equals(looper)) {
                throw new RuntimeException("BackgroundHeapCheckHandler may not run on the ui thread");
            }
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i != 1000) {
                if (i != 3000) {
                    return;
                }
                GarbageMonitor.this.update();
                removeMessages(3000);
                sendEmptyMessageDelayed(3000, 60000L);
                return;
            }
            if (GarbageMonitor.this.gcAndCheckGarbage()) {
                final GarbageMonitor garbageMonitor = GarbageMonitor.this;
                postDelayed(new Runnable() { // from class: com.android.systemui.util.leak.GarbageMonitor$BackgroundHeapCheckHandler$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() throws IOException {
                        garbageMonitor.reinspectGarbageAfterGc();
                    }
                }, 100L);
            }
            removeMessages(1000);
            sendEmptyMessageDelayed(1000, 900000L);
        }
    }
}
