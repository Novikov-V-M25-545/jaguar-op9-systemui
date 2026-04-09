package com.android.systemui.screenrecord;

import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.util.Log;
import android.view.Surface;
import java.io.IOException;
import java.nio.ByteBuffer;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class ScreenInternalAudioRecorder {
    private static String TAG = "ScreenAudioRecorder";
    private AudioRecord mAudioRecord;
    private AudioRecord mAudioRecordMic;
    private MediaCodec mCodec;
    private MediaProjection mMediaProjection;
    private boolean mMic;
    private MediaMuxer mMuxer;
    private long mPresentationTime;
    private Thread mThread;
    private long mTotalBytes;
    private Config mConfig = new Config();
    private int mTrackId = -1;

    public ScreenInternalAudioRecorder(String str, MediaProjection mediaProjection, boolean z) throws IOException {
        this.mMic = z;
        this.mMuxer = new MediaMuxer(str, 0);
        this.mMediaProjection = mediaProjection;
        Log.d(TAG, "creating audio file " + str);
        setupSimple();
    }

    public static class Config {
        public int channelOutMask = 4;
        public int channelInMask = 16;
        public int encoding = 2;
        public int sampleRate = 44100;
        public int bitRate = 196000;
        public int bufferSizeBytes = LineageHardwareManager.FEATURE_COLOR_BALANCE;
        public boolean privileged = true;
        public boolean legacy_app_looback = false;

        public String toString() {
            return "channelMask=" + this.channelOutMask + "\n   encoding=" + this.encoding + "\n sampleRate=" + this.sampleRate + "\n bufferSize=" + this.bufferSizeBytes + "\n privileged=" + this.privileged + "\n legacy app looback=" + this.legacy_app_looback;
        }
    }

    private void setupSimple() throws IOException {
        Config config = this.mConfig;
        final int minBufferSize = AudioRecord.getMinBufferSize(config.sampleRate, config.channelInMask, config.encoding) * 2;
        Log.d(TAG, "audio buffer size: " + minBufferSize);
        this.mAudioRecord = new AudioRecord.Builder().setAudioFormat(new AudioFormat.Builder().setEncoding(this.mConfig.encoding).setSampleRate(this.mConfig.sampleRate).setChannelMask(this.mConfig.channelOutMask).build()).setAudioPlaybackCaptureConfig(new AudioPlaybackCaptureConfiguration.Builder(this.mMediaProjection).addMatchingUsage(1).addMatchingUsage(0).addMatchingUsage(14).build()).build();
        if (this.mMic) {
            Config config2 = this.mConfig;
            this.mAudioRecordMic = new AudioRecord(7, config2.sampleRate, 16, config2.encoding, minBufferSize);
        }
        this.mCodec = MediaCodec.createEncoderByType("audio/mp4a-latm");
        MediaFormat mediaFormatCreateAudioFormat = MediaFormat.createAudioFormat("audio/mp4a-latm", this.mConfig.sampleRate, 1);
        mediaFormatCreateAudioFormat.setInteger("aac-profile", 2);
        mediaFormatCreateAudioFormat.setInteger("bitrate", this.mConfig.bitRate);
        mediaFormatCreateAudioFormat.setInteger("pcm-encoding", this.mConfig.encoding);
        this.mCodec.configure(mediaFormatCreateAudioFormat, (Surface) null, (MediaCrypto) null, 1);
        this.mThread = new Thread(new Runnable() { // from class: com.android.systemui.screenrecord.ScreenInternalAudioRecorder$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() throws MediaCodec.CryptoException {
                this.f$0.lambda$setupSimple$0(minBufferSize);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setupSimple$0(int i) throws MediaCodec.CryptoException {
        short[] sArr;
        short[] sArrScaleValues;
        int iMin;
        int i2;
        byte[] bArr = null;
        if (this.mMic) {
            int i3 = i / 2;
            sArr = new short[i3];
            sArrScaleValues = new short[i3];
        } else {
            sArr = null;
            bArr = new byte[i];
            sArrScaleValues = null;
        }
        while (true) {
            int i4 = 0;
            if (this.mMic) {
                int i5 = this.mAudioRecord.read(sArr, 0, sArr.length);
                int i6 = this.mAudioRecordMic.read(sArrScaleValues, 0, sArrScaleValues.length);
                sArrScaleValues = scaleValues(sArrScaleValues, i6, 1.4f);
                iMin = Math.min(i5, i6) * 2;
                byte[] bArrAddAndConvertBuffers = addAndConvertBuffers(sArr, i5, sArrScaleValues, i6);
                i4 = i5;
                bArr = bArrAddAndConvertBuffers;
                i2 = i6;
            } else {
                iMin = this.mAudioRecord.read(bArr, 0, bArr.length);
                i2 = 0;
            }
            if (iMin < 0) {
                Log.e(TAG, "read error " + iMin + ", shorts internal: " + i4 + ", shorts mic: " + i2);
                endStream();
                return;
            }
            encode(bArr, iMin);
        }
    }

    private short[] scaleValues(short[] sArr, int i, float f) {
        for (int i2 = 0; i2 < i; i2++) {
            short s = sArr[i2];
            int i3 = (int) (sArr[i2] * f);
            if (i3 > 32767) {
                i3 = 32767;
            } else if (i3 < -32768) {
                i3 = -32768;
            }
            sArr[i2] = (short) i3;
        }
        return sArr;
    }

    private byte[] addAndConvertBuffers(short[] sArr, int i, short[] sArr2, int i2) {
        int i3;
        int iMax = Math.max(i, i2);
        if (iMax < 0) {
            return new byte[0];
        }
        byte[] bArr = new byte[iMax * 2];
        for (int i4 = 0; i4 < iMax; i4++) {
            if (i4 > i) {
                i3 = sArr2[i4];
            } else if (i4 > i2) {
                i3 = sArr[i4];
            } else {
                i3 = sArr[i4] + sArr2[i4];
            }
            int i5 = i3;
            if (i3 > 32767) {
                i5 = 32767;
            }
            if (i5 < -32768) {
                i5 = -32768;
            }
            int i6 = i4 * 2;
            bArr[i6] = (byte) (i5 & 255);
            bArr[i6 + 1] = (byte) ((i5 >> 8) & 255);
        }
        return bArr;
    }

    private void encode(byte[] bArr, int i) throws MediaCodec.CryptoException {
        int i2 = 0;
        while (i > 0) {
            int iDequeueInputBuffer = this.mCodec.dequeueInputBuffer(500L);
            if (iDequeueInputBuffer < 0) {
                writeOutput();
                return;
            }
            ByteBuffer inputBuffer = this.mCodec.getInputBuffer(iDequeueInputBuffer);
            inputBuffer.clear();
            int iCapacity = inputBuffer.capacity();
            int i3 = i > iCapacity ? iCapacity : i;
            i -= i3;
            inputBuffer.put(bArr, i2, i3);
            i2 += i3;
            this.mCodec.queueInputBuffer(iDequeueInputBuffer, 0, i3, this.mPresentationTime, 0);
            long j = this.mTotalBytes + i3 + 0;
            this.mTotalBytes = j;
            this.mPresentationTime = ((j / 2) * 1000000) / this.mConfig.sampleRate;
            writeOutput();
        }
    }

    private void endStream() throws MediaCodec.CryptoException {
        this.mCodec.queueInputBuffer(this.mCodec.dequeueInputBuffer(500L), 0, 0, this.mPresentationTime, 4);
        writeOutput();
    }

    private void writeOutput() {
        while (true) {
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int iDequeueOutputBuffer = this.mCodec.dequeueOutputBuffer(bufferInfo, 500L);
            if (iDequeueOutputBuffer == -2) {
                this.mTrackId = this.mMuxer.addTrack(this.mCodec.getOutputFormat());
                this.mMuxer.start();
            } else {
                if (iDequeueOutputBuffer == -1 || this.mTrackId < 0) {
                    return;
                }
                ByteBuffer outputBuffer = this.mCodec.getOutputBuffer(iDequeueOutputBuffer);
                if ((bufferInfo.flags & 2) == 0 || bufferInfo.size == 0) {
                    this.mMuxer.writeSampleData(this.mTrackId, outputBuffer, bufferInfo);
                }
                this.mCodec.releaseOutputBuffer(iDequeueOutputBuffer, false);
            }
        }
    }

    public void start() throws IllegalStateException {
        if (this.mThread != null) {
            Log.e(TAG, "a recording is being done in parallel or stop is not called");
        }
        this.mAudioRecord.startRecording();
        if (this.mMic) {
            this.mAudioRecordMic.startRecording();
        }
        Log.d(TAG, "channel count " + this.mAudioRecord.getChannelCount());
        this.mCodec.start();
        if (this.mAudioRecord.getRecordingState() != 3) {
            throw new IllegalStateException("Audio recording failed to start");
        }
        this.mThread.start();
    }

    public void end() throws IllegalStateException, InterruptedException {
        this.mAudioRecord.stop();
        if (this.mMic) {
            this.mAudioRecordMic.stop();
        }
        this.mAudioRecord.release();
        if (this.mMic) {
            this.mAudioRecordMic.release();
        }
        try {
            this.mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.mCodec.stop();
        this.mCodec.release();
        this.mMuxer.stop();
        this.mMuxer.release();
        this.mThread = null;
    }
}
