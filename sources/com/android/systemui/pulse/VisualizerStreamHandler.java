package com.android.systemui.pulse;

import android.content.Context;
import android.media.audiofx.Visualizer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.util.concurrent.Executor;

/* loaded from: classes.dex */
public class VisualizerStreamHandler {
    protected static final String TAG = "VisualizerStreamHandler";
    protected int mConsecutiveFrames;
    protected Context mContext;
    protected PulseControllerImpl mController;
    private final Handler mHandler = new Handler(Looper.getMainLooper()) { // from class: com.android.systemui.pulse.VisualizerStreamHandler.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 55) {
                VisualizerStreamHandler visualizerStreamHandler = VisualizerStreamHandler.this;
                visualizerStreamHandler.mIsAnalyzed = true;
                visualizerStreamHandler.mIsValidated = true;
                visualizerStreamHandler.mIsPrepared = false;
                visualizerStreamHandler.mListener.onStreamAnalyzed(true);
                return;
            }
            if (i != 56) {
                return;
            }
            VisualizerStreamHandler visualizerStreamHandler2 = VisualizerStreamHandler.this;
            visualizerStreamHandler2.mIsAnalyzed = true;
            visualizerStreamHandler2.mIsValidated = false;
            visualizerStreamHandler2.mIsPrepared = false;
            visualizerStreamHandler2.mListener.onStreamAnalyzed(false);
        }
    };
    protected boolean mIsAnalyzed;
    protected boolean mIsPaused;
    protected boolean mIsPrepared;
    protected boolean mIsValidated;
    protected Listener mListener;
    private final Executor mUiBgExecutor;
    protected Visualizer mVisualizer;

    public interface Listener {
        void onFFTUpdate(byte[] bArr);

        void onStreamAnalyzed(boolean z);
    }

    public VisualizerStreamHandler(Context context, PulseControllerImpl pulseControllerImpl, Listener listener, Executor executor) {
        this.mContext = context;
        this.mController = pulseControllerImpl;
        this.mListener = listener;
        this.mUiBgExecutor = executor;
    }

    public final void link() {
        this.mUiBgExecutor.execute(new Runnable() { // from class: com.android.systemui.pulse.VisualizerStreamHandler$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() throws IllegalStateException {
                this.f$0.lambda$link$0();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$link$0() throws IllegalStateException {
        pause();
        resetAnalyzer();
        if (this.mVisualizer == null) {
            try {
                Visualizer visualizer = new Visualizer(0);
                this.mVisualizer = visualizer;
                visualizer.setEnabled(false);
                this.mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
                this.mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() { // from class: com.android.systemui.pulse.VisualizerStreamHandler.2
                    @Override // android.media.audiofx.Visualizer.OnDataCaptureListener
                    public void onWaveFormDataCapture(Visualizer visualizer2, byte[] bArr, int i) {
                    }

                    @Override // android.media.audiofx.Visualizer.OnDataCaptureListener
                    public void onFftDataCapture(Visualizer visualizer2, byte[] bArr, int i) {
                        VisualizerStreamHandler.this.analyze(bArr);
                        if (VisualizerStreamHandler.this.isValidStream()) {
                            VisualizerStreamHandler visualizerStreamHandler = VisualizerStreamHandler.this;
                            if (visualizerStreamHandler.mIsPaused) {
                                return;
                            }
                            visualizerStreamHandler.mListener.onFFTUpdate(bArr);
                        }
                    }
                }, (int) (Visualizer.getMaxCaptureRate() * 0.75d), false, true);
            } catch (Exception e) {
                Log.e(TAG, "Error enabling visualizer!", e);
                return;
            }
        }
        this.mVisualizer.setEnabled(true);
    }

    public final void unlink() {
        if (this.mVisualizer != null) {
            pause();
            this.mVisualizer.setEnabled(false);
            this.mVisualizer.release();
            this.mVisualizer = null;
            resetAnalyzer();
        }
    }

    public boolean isValidStream() {
        return this.mIsAnalyzed && this.mIsValidated;
    }

    public void resetAnalyzer() {
        this.mIsAnalyzed = false;
        this.mIsValidated = false;
        this.mIsPrepared = false;
        this.mConsecutiveFrames = 0;
    }

    public void pause() {
        this.mIsPaused = true;
    }

    public void resume() {
        this.mIsPaused = false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void analyze(byte[] bArr) {
        if (this.mIsAnalyzed) {
            return;
        }
        if (!this.mIsPrepared) {
            Handler handler = this.mHandler;
            handler.sendMessageDelayed(handler.obtainMessage(56), 6000L);
            this.mIsPrepared = true;
        }
        if (isDataEmpty(bArr)) {
            this.mConsecutiveFrames = 0;
        } else {
            this.mConsecutiveFrames++;
        }
        if (this.mConsecutiveFrames == 3) {
            this.mIsPaused = true;
            this.mHandler.removeMessages(56);
            this.mHandler.sendEmptyMessage(55);
        }
    }

    private boolean isDataEmpty(byte[] bArr) {
        for (byte b : bArr) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }
}
