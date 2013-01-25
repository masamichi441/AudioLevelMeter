package com.example.audiolevelmeter;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

public class AudioRecorder extends AudioRecord {
    private static final String TAG = AudioRecorder.class.getSimpleName();

    private static final int READ_DATA_INTERVAL = 200;	// in mSec

    private final int mSamplingRate;

    AudioRecorder(int samplingRate) {
        super(AudioSource.MIC,
              samplingRate,
              AudioFormat.CHANNEL_IN_MONO,
              AudioFormat.ENCODING_PCM_16BIT,
              getBufferSize(samplingRate) * 2);

        mSamplingRate = samplingRate;
    }

    /**
     * Calculates buffer size (in bytes) based on sampling rate. Assume 16-bit mono sampling.
     * @param samplingRate
     * @return buffer size in bytes
     */
    static int getBufferSize(int samplingRate) {
        int singleFrameBytes = 2 * 1; 		// 16-bit * mono
        int readoutFrames = (samplingRate * READ_DATA_INTERVAL) / 1000; // frames/read
        return singleFrameBytes * readoutFrames;
    }

    /**
     * Returns buffer size for current instance.
     * @return buffer size in bytes
     */
    int getBufferSize() {
        return getBufferSize(mSamplingRate);
    }

    private static final int[] FREQUENCIES = {
          192000,
           96000,
           48000,
           44100,
           22050,
           16000,
           11025,
            8000
    };

    /**
     * Creates AudioRecorder instance with supported highest frequency.
     * @return AudioRecorder instance
     */
    public static AudioRecorder createInstance() {
        AudioRecorder recorder = null;

        for (int freq : FREQUENCIES) {
            try {
                recorder = new AudioRecorder(freq);
                Log.d(TAG, "Freq.=" + freq);
                break;
            } catch (IllegalArgumentException ex) {
                continue;
            }
        }
        return recorder;
    }
}
