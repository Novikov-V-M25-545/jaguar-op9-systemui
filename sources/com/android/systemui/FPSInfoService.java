package com.android.systemui;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
public class FPSInfoService extends Service {
    private Thread mCurFPSThread;
    private View mView;
    private final String TAG = "FPSInfoService";
    private String mFps = null;
    private String MEASURED_FPS = "";
    private BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() { // from class: com.android.systemui.FPSInfoService.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) throws InterruptedException {
            if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
                Log.d("FPSInfoService", "ACTION_SCREEN_ON");
                FPSInfoService.this.startThread();
                FPSInfoService.this.mView.setVisibility(0);
            } else if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                Log.d("FPSInfoService", "ACTION_SCREEN_OFF");
                FPSInfoService.this.mView.setVisibility(8);
                FPSInfoService.this.stopThread();
            }
        }
    };

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class FPSView extends View {
        private float mAscent;
        private Handler mCurFPSHandler;
        private boolean mDataAvail;
        private int mFH;
        private int mMaxWidth;
        private int mNeededHeight;
        private int mNeededWidth;
        private Paint mOnlinePaint;

        FPSView(Context context) {
            super(context);
            this.mCurFPSHandler = new Handler() { // from class: com.android.systemui.FPSInfoService.FPSView.1
                @Override // android.os.Handler
                public void handleMessage(Message message) {
                    Object obj = message.obj;
                    if (obj != null && message.what == 1) {
                        String str = (String) obj;
                        FPSInfoService.this.mFps = str.substring(0, Math.min(str.length(), 9));
                        FPSView.this.mDataAvail = true;
                        FPSView.this.updateDisplay();
                    }
                }
            };
            float f = context.getResources().getDisplayMetrics().density;
            int iRound = Math.round(f * 5.0f);
            setPadding(iRound, iRound, iRound, iRound);
            setBackgroundColor(Color.argb(96, 0, 0, 0));
            int iRound2 = Math.round(f * 12.0f);
            Paint paint = new Paint();
            this.mOnlinePaint = paint;
            paint.setAntiAlias(true);
            this.mOnlinePaint.setTextSize(iRound2);
            this.mOnlinePaint.setColor(-1);
            this.mOnlinePaint.setShadowLayer(5.0f, 0.0f, 0.0f, -16777216);
            this.mAscent = this.mOnlinePaint.ascent();
            this.mFH = (int) ((this.mOnlinePaint.descent() - this.mAscent) + 0.5f);
            this.mMaxWidth = (int) this.mOnlinePaint.measureText("fps: 60.1");
            updateDisplay();
        }

        @Override // android.view.View
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
        }

        @Override // android.view.View
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            this.mCurFPSHandler.removeMessages(1);
        }

        @Override // android.view.View
        protected void onMeasure(int i, int i2) {
            setMeasuredDimension(View.resolveSize(this.mNeededWidth, i), View.resolveSize(this.mNeededHeight, i2));
        }

        private String getFPSInfoString() {
            return FPSInfoService.this.mFps;
        }

        @Override // android.view.View
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (this.mDataAvail) {
                canvas.drawText(getFPSInfoString(), ((getWidth() - 1) - ((View) this).mPaddingLeft) - this.mMaxWidth, (((View) this).mPaddingTop - ((int) this.mAscent)) - 1, this.mOnlinePaint);
            }
        }

        void updateDisplay() {
            if (this.mDataAvail) {
                int i = ((View) this).mPaddingLeft + ((View) this).mPaddingRight + this.mMaxWidth;
                int i2 = ((View) this).mPaddingTop + ((View) this).mPaddingBottom + 40;
                if (i != this.mNeededWidth || i2 != this.mNeededHeight) {
                    this.mNeededWidth = i;
                    this.mNeededHeight = i2;
                    requestLayout();
                    return;
                }
                invalidate();
            }
        }

        @Override // android.view.View
        public Handler getHandler() {
            return this.mCurFPSHandler;
        }
    }

    protected class CurFPSThread extends Thread {
        private Handler mHandler;
        private boolean mInterrupt = false;

        public CurFPSThread(Handler handler) {
            this.mHandler = handler;
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
                    String oneLine = FPSInfoService.readOneLine(FPSInfoService.this.MEASURED_FPS);
                    Handler handler = this.mHandler;
                    handler.sendMessage(handler.obtainMessage(1, oneLine));
                } catch (InterruptedException unused) {
                    return;
                }
            }
        }
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        this.MEASURED_FPS = getResources().getString(R.string.config_fpsInfoSysNode);
        this.mView = new FPSView(this);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-2, -2, 2015, 24, -3);
        layoutParams.gravity = 51;
        layoutParams.setTitle("FPS Info");
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

    /* JADX INFO: Access modifiers changed from: private */
    public void startThread() {
        Log.d("FPSInfoService", "started CurFPSThread");
        CurFPSThread curFPSThread = new CurFPSThread(this.mView.getHandler());
        this.mCurFPSThread = curFPSThread;
        curFPSThread.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopThread() throws InterruptedException {
        Thread thread = this.mCurFPSThread;
        if (thread != null && thread.isAlive()) {
            Log.d("FPSInfoService", "stopping CurFPSThread");
            this.mCurFPSThread.interrupt();
            try {
                this.mCurFPSThread.join();
            } catch (InterruptedException unused) {
            }
        }
        this.mCurFPSThread = null;
    }
}
