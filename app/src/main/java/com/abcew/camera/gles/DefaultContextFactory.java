package com.abcew.camera.gles;

import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import static javax.microedition.khronos.egl.EGL10.EGL_NONE;
import static javax.microedition.khronos.egl.EGL10.EGL_NO_CONTEXT;

/**
 * Created by laputan on 16/11/2.
 */
public class DefaultContextFactory implements GLSurfaceView.EGLContextFactory {
    private static final String TAG = "DefaultContextFactory";

    private final int mEGLContextClientVersion;

    public DefaultContextFactory(final int version) {
        mEGLContextClientVersion = version;
    }

    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

    @Override
    public EGLContext createContext(@NonNull final EGL10 egl, final EGLDisplay display, final EGLConfig config) {
        final int[] attrib_list;
        if (mEGLContextClientVersion != 0) {
            attrib_list = new int[]{ EGL_CONTEXT_CLIENT_VERSION, mEGLContextClientVersion, EGL_NONE };
        } else {
            attrib_list = null;
        }
        return egl.eglCreateContext(display, config, EGL_NO_CONTEXT, attrib_list);
    }

    @Override
    public void destroyContext(@NonNull final EGL10 egl, final EGLDisplay display, final EGLContext context) {
        if (!egl.eglDestroyContext(display, context)) {
            Log.e(TAG, "display:" + display + " context: " + context);
            throw new RuntimeException("eglDestroyContex" + egl.eglGetError());
        }
    }

}
