package com.abcew.camera.renderer;

import android.content.Context;
import android.opengl.Matrix;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.abcew.camera.configuration.AbstractConfig;
import com.abcew.camera.configuration.PhotoEditorSdkConfig;
import com.abcew.camera.filter.NoneImageFilter;
import com.abcew.camera.gles.GlSurfaceTexture;
import com.abcew.camera.gles.PreviewTextureInterface;
import com.abcew.camera.ui.acs.Cam;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;

/**
 * Created by laputan on 16/11/1.
 */
public class PreviewRenderer extends GlFrameBufferObjectRenderer implements PreviewTextureInterface.OnFrameAvailableListener{
    private static final String TAG = "PreviewRenderer";

    @NonNull
    private final Handler mainHandler;

    private PreviewTextureInterface previewTexture;
    private boolean updateSurface = false;

    @NonNull
    private final ArrayList<Runnable> nextTextureImageReadyRunnables;
    @NonNull
    private final ArrayList<Runnable> onTextureImageReadyRunnables;

    private final float[] mvpMatrix = new float[16];
    private final float[] projMatrix = new float[16];
    private final float[] matrix = new float[16];
    private final float[] vMatrix = new float[16];
    private final float[] stMatrix = new float[16];
    private float cameraRatio = 1.0f;

    private final RendererCallback rendererCallback;

    @Nullable
    private AbstractConfig.ImageFilterInterface filter = PhotoEditorSdkConfig.getFilterConfig().get(0);

    public float surfaceWidth  = 1;
    public float surfaceHeight = 1;

    public PreviewRenderer(@NonNull Context context, final RendererCallback callback) {
        super();
        resetMatrix();
        rendererCallback = callback;
        mainHandler = new Handler(context.getMainLooper());
        onTextureImageReadyRunnables = new ArrayList<>();
        nextTextureImageReadyRunnables = new ArrayList<>();
    }

    public void resetMatrix() {
        Matrix.setIdentityM(stMatrix, 0);
        Matrix.setIdentityM(matrix, 0);
        Matrix.rotateM(matrix, 0, 180, 0.0f, 0.0f, 1.0f);
        Matrix.scaleM(matrix, 0, -1f, 1f, 1f);
        Matrix.setLookAtM(vMatrix, 0,
                0.0f, 0.0f, 5.0f,
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f
        );
    }

    @Nullable
    public AbstractConfig.ImageFilterInterface getFilter() {
        return filter;
    }

    public void setFilter(@Nullable final AbstractConfig.ImageFilterInterface shader) {
        if (shader != null && shader.equals(this.filter)) {
            return;
        }

        if (this.filter != null) {
            this.filter.release();
        }

        this.filter = shader;
        rendererCallback.requestRender();
    }

    public void onStartPreview(@NonNull final Cam camera, final boolean faceMirror) {

        final int orientation = (camera.getCameraOrientation() + 270) % 360;

        resetMatrix();

        Matrix.setIdentityM(matrix, 0);
        Matrix.rotateM(matrix, 0, -orientation, 0.0f, 0.0f, 1f);

        if (camera.isFront() && !faceMirror) {
            Matrix.scaleM(matrix, 0, 1.0f, -1.0f, 1.0f);
        }

        final Cam.Size previewSize = camera.getPreviewSize();
        cameraRatio = 1;
        if (previewSize != null) {
            cameraRatio = previewSize.width / (float) previewSize.height;
        }



        if (previewTexture == null) {
            previewTexture = new GlSurfaceTexture();
            previewTexture.setOnFrameAvailableListener(this);
        }
        previewTexture.setup(camera);

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                rendererCallback.onStartPreviewFinished();
            }
        });
    }

    @Override
    public void onSurfaceCreated(final EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        filter = new NoneImageFilter();

        resetMatrix();

        synchronized (this) {
            updateSurface = false;
        }

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                rendererCallback.onRendererInitialized();
            }
        });
    }

    private float stageRatio = Float.MIN_VALUE;

    @Override
    public void onSurfaceChanged(final int width, final int height) {

        this.surfaceWidth  = width;
        this.surfaceHeight = height;

        //resetMatrix();

        stageRatio = (stageRatio == Float.MIN_VALUE) ? width / (float) height : stageRatio;
        try {
            Matrix.frustumM(projMatrix, 0, -stageRatio, stageRatio, -1, 1, 5, 7);
        } catch (Exception ignored) {
            Log.e("glbla", "onSurfaceChanged exeption", ignored);
        }



        if (rendererCallback != null) {
            rendererCallback.onSurfaceChanged(width, height);
        }

    }

    @Override
    public synchronized void onDrawFrame() {
        if (onTextureImageReadyRunnables.size() > 0) {
            for (Runnable runnable : onTextureImageReadyRunnables) {
                mainHandler.post(runnable);
            }
            onTextureImageReadyRunnables.clear();
        }

        if (updateSurface && previewTexture != null) {
            previewTexture.updateTexImage();
            previewTexture.getTransformMatrix(stMatrix);
            updateSurface = false;
        }

        glClear(GL_COLOR_BUFFER_BIT);
        Matrix.multiplyMM(mvpMatrix, 0, vMatrix,    0, matrix,    0);
        Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, mvpMatrix, 0);

        if (previewTexture != null) {
            filter.draw(previewTexture, mvpMatrix, stMatrix, cameraRatio);
            //filter.draw(previewTexture, mvpMatrix, stMatrix, cameraRatio);
        }

        if (nextTextureImageReadyRunnables.size() > 0) {
            for (Runnable runnable : nextTextureImageReadyRunnables) {
                onTextureImageReadyRunnables.add(runnable);
            }
            nextTextureImageReadyRunnables.clear();
            rendererCallback.requestRender();
        }
    }

    @Override
    public synchronized void onFrameAvailable(final PreviewTextureInterface previewTexture) {
        updateSurface = true;
        rendererCallback.requestRender();
    }

    public interface RendererCallback {
        void onRendererInitialized();

        void onStartPreviewFinished();

        void requestRender();

        void onSurfaceChanged(final int width, final int height);
    }

}
