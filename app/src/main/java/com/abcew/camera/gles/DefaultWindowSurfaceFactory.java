package com.abcew.camera.gles;

import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * Created by laputan on 16/11/2.
 */
public class DefaultWindowSurfaceFactory implements GLSurfaceView.EGLWindowSurfaceFactory{

    private static final String TAG = "DefaultWindowSurface";

    @Nullable
    @Override
    public EGLSurface createWindowSurface(@NonNull final EGL10 egl, final EGLDisplay display, final EGLConfig config, final Object nativeWindow) {
        try {
            return egl.eglCreateWindowSurface(display, config, nativeWindow, null);
        } catch (@NonNull final IllegalArgumentException e) {
            Log.e(TAG, "eglCreateWindowSurface", e);
            return null;
        }
    }

    @Override
    public void destroySurface(@NonNull final EGL10 egl, final EGLDisplay display, final EGLSurface surface) {
        egl.eglDestroySurface(display, surface);
    }
}
