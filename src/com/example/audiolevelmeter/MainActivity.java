package com.example.audiolevelmeter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private AudioRecorder mRecorder;
    private LevelMeter mLevelMeter;
    private volatile boolean mRecording = false;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private Future<?> mFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecorder = AudioRecorder.createInstance();

        mLevelMeter = (LevelMeter) findViewById(R.id.level_meter);
        final Button button = (Button) findViewById(R.id.start_button);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mRecording) {
                    button.setText(R.string.stop);
                    mFuture = mExecutor.submit(new RecorderTask());
                } else {
                    button.setText(R.string.start);
                    mFuture.cancel(true);	// Interrupt to the thread
                }
            }
        });
    }

    private class RecorderTask implements Runnable {
        private final String TAG_TASK = RecorderTask.class.getSimpleName();

        private final ByteBuffer pcmBuffer;

        RecorderTask() {
            pcmBuffer = ByteBuffer.allocate(mRecorder.getBufferSize());
            pcmBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }

        @Override
        public void run() {
            Log.d(TAG_TASK, "started");

            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
            mRecording = true;

            byte[] buffer = pcmBuffer.array();

            mRecorder.startRecording();
            Thread myThread = Thread.currentThread();

            try {
                while (!myThread.isInterrupted()) {
                    // Read PCM data from recorder
                    int readSize = mRecorder.read(buffer, 0, buffer.length);
                    if (readSize < 0) {
                        Log.e(TAG_TASK, "read error : " + readSize);
                        break;
                    }

                    // Check max amplitude value (16-bit value).
                    pcmBuffer.clear();
                    int maxAmplitude = 0;
                    for (int i = 0; i < (readSize / 2); i++) {
                        maxAmplitude = Math.max(maxAmplitude, Math.abs(pcmBuffer.getShort()));
                    }

                    // Set level meter
                    mLevelMeter.setLevel(maxAmplitude);
                }
            } finally {
                Log.d(TAG_TASK, "stop recorder");
                mRecorder.stop();
                mLevelMeter.setLevel(0);
                mRecording = false;
            }
            Log.d(TAG_TASK, "stopped");
        }
    }
}
