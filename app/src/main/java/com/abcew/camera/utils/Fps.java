package com.abcew.camera.utils;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by laputan on 16/11/1.
 */
public class Fps implements Runnable {
    public interface Callback {
        void onFps(final int fps);
    }

    @Nullable
    Callback mCallback;

    private final Handler mHandler = new Handler();
    private final Runnable mCallbackRunner = new Runnable() {
        @Override
        public void run() {
            mCallback.onFps(mFrameCount);
            mFrameCount = 0;
        }
    };

    volatile int mFrameCount;

    @Nullable
    private Thread mThread;

    public Fps(@Nullable final Callback callback) {
        if (callback == null) {
            throw new NullPointerException("Callback must not be null");
        }
        mCallback = callback;
    }

    public void start() {
        synchronized (this) {
            stop();
            mFrameCount = 0;
            mThread = new Thread(this);
            mThread.start();
        }
    }

    public void stop() {
        synchronized (this) {
            mThread = null;
        }
    }

    public void countup() {
        mFrameCount++;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000L);

                synchronized (this) {
                    if (mThread == null || mThread != Thread.currentThread()) {
                        break;
                    }
                }

                mHandler.post(mCallbackRunner);
            } catch (@NonNull final InterruptedException e) {
                break;
            }
        }
    }

}
