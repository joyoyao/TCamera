package com.abcew.camera.gles;

import com.abcew.camera.ui.acs.Cam;

/**
 * Created by laputan on 16/11/1.
 */
public interface PreviewTextureInterface extends Texture{
    interface OnFrameAvailableListener {
        void onFrameAvailable(PreviewTextureInterface previewTexture);
    }

    void setOnFrameAvailableListener(final OnFrameAvailableListener l);

    void setup(Cam camera);

    void updateTexImage();

    void getTransformMatrix(float[] mtx);
}
