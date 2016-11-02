package com.abcew.camera.gles;

/**
 * Created by laputan on 16/11/1.
 */
public interface Texture {
    int getTextureId();
    void release();

    int getTextureTarget();
}
