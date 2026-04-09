package com.android.systemui.screenrecord;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaMuxer;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes.dex */
public class ScreenRecordingMuxer {
    private static String TAG = "ScreenRecordingMuxer";
    private ArrayMap<Pair<MediaExtractor, Integer>, Integer> mExtractorIndexToMuxerIndex = new ArrayMap<>();
    private ArrayList<MediaExtractor> mExtractors = new ArrayList<>();
    private String[] mFiles;
    private int mFormat;
    private String mOutFile;

    public ScreenRecordingMuxer(int i, String str, String... strArr) {
        this.mFiles = strArr;
        this.mOutFile = str;
        this.mFormat = i;
        Log.d(TAG, "out: " + this.mOutFile + " , in: " + this.mFiles[0]);
    }

    public void mux() throws IOException {
        MediaMuxer mediaMuxer = new MediaMuxer(this.mOutFile, this.mFormat);
        for (String str : this.mFiles) {
            MediaExtractor mediaExtractor = new MediaExtractor();
            try {
                mediaExtractor.setDataSource(str);
                Log.d(TAG, str + " track count: " + mediaExtractor.getTrackCount());
                this.mExtractors.add(mediaExtractor);
                for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
                    int iAddTrack = mediaMuxer.addTrack(mediaExtractor.getTrackFormat(i));
                    Log.d(TAG, "created extractor format" + mediaExtractor.getTrackFormat(i).toString());
                    this.mExtractorIndexToMuxerIndex.put(Pair.create(mediaExtractor, Integer.valueOf(i)), Integer.valueOf(iAddTrack));
                }
            } catch (IOException e) {
                Log.e(TAG, "error creating extractor: " + str);
                e.printStackTrace();
            }
        }
        mediaMuxer.start();
        for (Pair<MediaExtractor, Integer> pair : this.mExtractorIndexToMuxerIndex.keySet()) {
            MediaExtractor mediaExtractor2 = (MediaExtractor) pair.first;
            mediaExtractor2.selectTrack(((Integer) pair.second).intValue());
            int iIntValue = this.mExtractorIndexToMuxerIndex.get(pair).intValue();
            Log.d(TAG, "track format: " + mediaExtractor2.getTrackFormat(((Integer) pair.second).intValue()));
            mediaExtractor2.seekTo(0L, 2);
            ByteBuffer byteBufferAllocate = ByteBuffer.allocate(4194304);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            while (true) {
                int sampleData = mediaExtractor2.readSampleData(byteBufferAllocate, byteBufferAllocate.arrayOffset());
                bufferInfo.size = sampleData;
                if (sampleData < 0) {
                    break;
                }
                bufferInfo.presentationTimeUs = mediaExtractor2.getSampleTime();
                bufferInfo.flags = mediaExtractor2.getSampleFlags();
                mediaMuxer.writeSampleData(iIntValue, byteBufferAllocate, bufferInfo);
                mediaExtractor2.advance();
            }
        }
        Iterator<MediaExtractor> it = this.mExtractors.iterator();
        while (it.hasNext()) {
            it.next().release();
        }
        mediaMuxer.stop();
        mediaMuxer.release();
    }
}
