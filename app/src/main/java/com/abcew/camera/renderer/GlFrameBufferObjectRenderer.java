package com.abcew.camera.renderer;

import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.abcew.camera.utils.Fps;

import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by laputan on 16/11/1.
 */
public abstract class GlFrameBufferObjectRenderer implements GLSurfaceView.Renderer {
    @NonNull
    private final Queue<Runnable> mRunOnDraw;
    @Nullable
    private Fps mFps;

    protected GlFrameBufferObjectRenderer() {
        mRunOnDraw = new LinkedList<>();
    }


    @Override
    public final void onSurfaceCreated(final GL10 gl, final EGLConfig config) {

        onSurfaceCreated(config);
        if (mFps != null) {
            mFps.start();
        }
    }

    @Override
    public final void onSurfaceChanged(final GL10 gl, final int width, final int height) {
        onSurfaceChanged(width, height);
    }

    @Override
    public final void onDrawFrame(final GL10 gl) {
        synchronized (mRunOnDraw) {
            while (!mRunOnDraw.isEmpty()) {
                mRunOnDraw.poll().run();
            }
        }

        onDrawFrame();

        if (mFps != null) {
            mFps.countup();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (mFps != null) {
                mFps.stop();
                mFps = null;
            }
        } finally {
            super.finalize();
        }
    }

    public abstract void onSurfaceCreated(EGLConfig config);

    public abstract void onSurfaceChanged(int width, int height);

    public abstract void onDrawFrame();
}