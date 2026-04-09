package com.android.systemui;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/* loaded from: classes.dex */
public class CPUInfoService extends Service {
    private boolean mCpuTempAvail;
    private Thread mCurCPUThread;
    private View mView;
    private final String TAG = "CPUInfoService";
    private int mNumCpus = 2;
    private String[] mCpu = null;
    private String[] mCurrFreq = null;
    private String[] mCurrGov = null;
    private int CPU_TEMP_DIVIDER = 1;
    private String CPU_TEMP_SENSOR = "";
    private String DISPLAY_CPUS = "";
    private BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() { // from class: com.android.systemui.CPUInfoService.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) throws InterruptedException {
            if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
                Log.d("CPUInfoService", "ACTION_SCREEN_ON ");
                CPUInfoService.this.startThread();
                CPUInfoService.this.mView.setVisibility(0);
            } else if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                Log.d("CPUInfoService", "ACTION_SCREEN_OFF");
                CPUInfoService.this.mView.setVisibility(8);
                CPUInfoService.this.stopThread();
            }
        }
    };

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class CPUView extends View {
        private float mAscent;
        private String mCpuTemp;
        private Handler mCurCPUHandler;
        private boolean mDataAvail;
        private int mFH;
        private int mMaxWidth;
        private int mNeededHeight;
        private int mNeededWidth;
        private Paint mOfflinePaint;
        private Paint mOnlinePaint;

        CPUView(Context context) {
            super(context);
            this.mCurCPUHandler = new Handler() { // from class: com.android.systemui.CPUInfoService.CPUView.1
                @Override // android.os.Handler
                public void handleMessage(Message message) {
                    Object obj = message.obj;
                    if (obj != null && message.what == 1) {
                        String str = (String) obj;
                        try {
                            String[] strArrSplit = str.split(";");
                            CPUView.this.mCpuTemp = strArrSplit[0];
                            String[] strArrSplit2 = strArrSplit[1].split("\\|");
                            for (int i = 0; i < strArrSplit2.length; i++) {
                                String[] strArrSplit3 = strArrSplit2[i].split(":");
                                if (strArrSplit3.length == 3) {
                                    CPUInfoService.this.mCurrFreq[i] = strArrSplit3[1];
                                    CPUInfoService.this.mCurrGov[i] = strArrSplit3[2];
                                } else {
                                    CPUInfoService.this.mCurrFreq[i] = "0";
                                    CPUInfoService.this.mCurrGov[i] = "";
                                }
                            }
                            CPUView.this.mDataAvail = true;
                            CPUView.this.updateDisplay();
                        } catch (ArrayIndexOutOfBoundsException unused) {
                            Log.e("CPUInfoService", "illegal data " + str);
                        }
                    }
                }
            };
            float f = context.getResources().getDisplayMetrics().density;
            int iRound = Math.round(f * 5.0f);
            setPadding(iRound, iRound, iRound, iRound);
            setBackgroundColor(Color.argb(96, 0, 0, 0));
            int iRound2 = Math.round(f * 12.0f);
            Typeface typefaceCreate = Typeface.create("monospace", 0);
            Paint paint = new Paint();
            this.mOnlinePaint = paint;
            paint.setTypeface(typefaceCreate);
            this.mOnlinePaint.setAntiAlias(true);
            float f2 = iRound2;
            this.mOnlinePaint.setTextSize(f2);
            this.mOnlinePaint.setColor(-1);
            this.mOnlinePaint.setShadowLayer(5.0f, 0.0f, 0.0f, -16777216);
            Paint paint2 = new Paint();
            this.mOfflinePaint = paint2;
            paint2.setTypeface(typefaceCreate);
            this.mOfflinePaint.setAntiAlias(true);
            this.mOfflinePaint.setTextSize(f2);
            this.mOfflinePaint.setColor(-65536);
            this.mAscent = this.mOnlinePaint.ascent();
            this.mFH = (int) ((this.mOnlinePaint.descent() - this.mAscent) + 0.5f);
            this.mMaxWidth = (int) this.mOnlinePaint.measureText("cpuX: interactive 00000000");
            updateDisplay();
        }

        @Override // android.view.View
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
        }

        @Override // android.view.View
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            this.mCurCPUHandler.removeMessages(1);
        }

        @Override // android.view.View
        protected void onMeasure(int i, int i2) {
            setMeasuredDimension(View.resolveSize(this.mNeededWidth, i), View.resolveSize(this.mNeededHeight, i2));
        }

        private String getCPUInfoString(int i) {
            String str = CPUInfoService.this.mCpu[i];
            String str2 = CPUInfoService.this.mCurrFreq[i];
            return "cpu" + str + ": " + CPUInfoService.this.mCurrGov[i] + " " + String.format("%8s", toMHz(str2));
        }

        private String getCpuTemp(String str) {
            return CPUInfoService.this.CPU_TEMP_DIVIDER > 1 ? String.format("%s", Integer.valueOf(Integer.parseInt(str) / CPUInfoService.this.CPU_TEMP_DIVIDER)) : str;
        }

        @Override // android.view.View
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (this.mDataAvail) {
                int width = getWidth() - 1;
                int i = ((View) this).mPaddingTop - ((int) this.mAscent);
                if (!this.mCpuTemp.equals("0")) {
                    canvas.drawText("Temp: " + getCpuTemp(this.mCpuTemp) + "°C", (width - ((View) this).mPaddingRight) - this.mMaxWidth, i - 1, this.mOnlinePaint);
                    i += this.mFH;
                }
                for (int i2 = 0; i2 < CPUInfoService.this.mCurrFreq.length; i2++) {
                    String cPUInfoString = getCPUInfoString(i2);
                    if (CPUInfoService.this.mCurrFreq[i2].equals("0")) {
                        canvas.drawText("cpu" + CPUInfoService.this.mCpu[i2] + ": offline", (width - ((View) this).mPaddingRight) - this.mMaxWidth, i - 1, this.mOfflinePaint);
                    } else {
                        canvas.drawText(cPUInfoString, (width - ((View) this).mPaddingRight) - this.mMaxWidth, i - 1, this.mOnlinePaint);
                    }
                    i += this.mFH;
                }
            }
        }

        void updateDisplay() {
            if (this.mDataAvail) {
                int i = CPUInfoService.this.mNumCpus;
                int i2 = ((View) this).mPaddingLeft + ((View) this).mPaddingRight + this.mMaxWidth;
                int i3 = ((View) this).mPaddingTop + ((View) this).mPaddingBottom + (this.mFH * ((CPUInfoService.this.mCpuTempAvail ? 1 : 0) + i));
                if (i2 != this.mNeededWidth || i3 != this.mNeededHeight) {
                    this.mNeededWidth = i2;
                    this.mNeededHeight = i3;
                    requestLayout();
                    return;
                }
                invalidate();
            }
        }

        private String toMHz(String str) {
            return (Integer.valueOf(str).intValue() / 1000) + " MHz";
        }

        @Override // android.view.View
        public Handler getHandler() {
            return this.mCurCPUHandler;
        }
    }

    protected class CurCPUThread extends Thread {
        private Handler mHandler;
        private boolean mInterrupt = false;

        public CurCPUThread(Handler handler, int i) {
            this.mHandler = handler;
            CPUInfoService.this.mNumCpus = i;
        }

        @Override // java.lang.Thread
        public void interrupt() {
            this.mInterrupt = true;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() throws InterruptedException, IOException {
            while (!this.mInterrupt) {
                try {
                    Thread.sleep(500L);
                    StringBuffer stringBuffer = new StringBuffer();
                    String oneLine = CPUInfoService.readOneLine(CPUInfoService.this.CPU_TEMP_SENSOR);
                    if (oneLine == null) {
                        oneLine = "0";
                    }
                    stringBuffer.append(oneLine);
                    stringBuffer.append(";");
                    for (int i = 0; i < CPUInfoService.this.mNumCpus; i++) {
                        String str = CPUInfoService.this.mCpu[i];
                        String oneLine2 = CPUInfoService.readOneLine("/sys/devices/system/cpu/cpu" + CPUInfoService.this.mCpu[i] + "/cpufreq/scaling_cur_freq");
                        String oneLine3 = CPUInfoService.readOneLine("/sys/devices/system/cpu/cpu" + CPUInfoService.this.mCpu[i] + "/cpufreq/scaling_governor");
                        if (oneLine2 == null) {
                            oneLine3 = "";
                            oneLine2 = "0";
                        }
                        stringBuffer.append(str + ":" + oneLine2 + ":" + oneLine3 + "|");
                    }
                    stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                    Handler handler = this.mHandler;
                    handler.sendMessage(handler.obtainMessage(1, stringBuffer.toString()));
                } catch (InterruptedException unused) {
                    return;
                }
            }
        }
    }

    @Override // android.app.Service
    public void onCreate() throws Resources.NotFoundException, NumberFormatException {
        super.onCreate();
        this.CPU_TEMP_DIVIDER = getResources().getInteger(R.integer.config_cpuTempDivider);
        this.CPU_TEMP_SENSOR = getResources().getString(R.string.config_cpuTempSensor);
        String string = getResources().getString(R.string.config_displayCpus);
        this.DISPLAY_CPUS = string;
        int cpus = getCpus(string);
        this.mNumCpus = cpus;
        this.mCurrFreq = new String[cpus];
        this.mCurrGov = new String[cpus];
        this.mCpuTempAvail = readOneLine(this.CPU_TEMP_SENSOR) != null;
        this.mView = new CPUView(this);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-2, -2, 2015, 24, -3);
        layoutParams.gravity = 53;
        layoutParams.setTitle("CPU Info");
        startThread();
        IntentFilter intentFilter = new IntentFilter("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        registerReceiver(this.mScreenStateReceiver, intentFilter);
        ((WindowManager) getSystemService("window")).addView(this.mView, layoutParams);
    }

    @Override // android.app.Service
    public void onDestroy() throws InterruptedException {
        super.onDestroy();
        stopThread();
        ((WindowManager) getSystemService("window")).removeView(this.mView);
        this.mView = null;
        unregisterReceiver(this.mScreenStateReceiver);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String readOneLine(String str) throws IOException {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(str), 512);
            try {
                return bufferedReader.readLine();
            } finally {
                bufferedReader.close();
            }
        } catch (Exception unused) {
            return null;
        }
    }

    private int getCpus(String str) throws NumberFormatException {
        int length;
        int i = 0;
        if (str != null) {
            String[] strArrSplit = str.split(",");
            if (strArrSplit.length > 0) {
                length = strArrSplit.length;
                this.mCpu = new String[length];
                while (i < length) {
                    try {
                        Integer.parseInt(strArrSplit[i]);
                        this.mCpu[i] = strArrSplit[i];
                        i++;
                    } catch (NumberFormatException unused) {
                        return getCpus(null);
                    }
                }
            } else {
                return getCpus(null);
            }
        } else {
            String[] strArrSplit2 = readOneLine("/sys/devices/system/cpu/present").split("-");
            int i2 = 1;
            if (strArrSplit2.length > 1) {
                try {
                    int i3 = (Integer.parseInt(strArrSplit2[1]) - Integer.parseInt(strArrSplit2[0])) + 1;
                    if (i3 >= 0) {
                        i2 = i3;
                    }
                } catch (NumberFormatException unused2) {
                }
            }
            length = i2;
            this.mCpu = new String[length];
            while (i < length) {
                this.mCpu[i] = String.valueOf(i);
                i++;
            }
        }
        return length;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startThread() {
        Log.d("CPUInfoService", "started CurCPUThread");
        CurCPUThread curCPUThread = new CurCPUThread(this.mView.getHandler(), this.mNumCpus);
        this.mCurCPUThread = curCPUThread;
        curCPUThread.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopThread() throws InterruptedException {
        Thread thread = this.mCurCPUThread;
        if (thread != null && thread.isAlive()) {
            Log.d("CPUInfoService", "stopping CurCPUThread");
            this.mCurCPUThread.interrupt();
            try {
                this.mCurCPUThread.join();
            } catch (InterruptedException unused) {
            }
        }
        this.mCurCPUThread = null;
    }
}
