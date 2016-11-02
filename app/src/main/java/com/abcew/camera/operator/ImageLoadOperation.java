package com.abcew.camera.operator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

import com.abcew.camera.utils.BitmapFactoryUtils;

/**
 * Created by laputan on 16/11/2.
 */
public class ImageLoadOperation {

    private static String currentSourceImagePath;
    private static Bitmap thumbnail;

    /**
     * Set Image source path
     * @param imagePath path of the image.
     */
    public void setSourceImagePath(String imagePath) {
        thumbnail = null;
        currentSourceImagePath = imagePath;
    }

    @Nullable
    public static Bitmap getThumbnailBitmap(int size) {
        if (currentSourceImagePath != null && (thumbnail == null)) {
            thumbnail = BitmapFactoryUtils.decodeFile(currentSourceImagePath, size, true, true);
            if (thumbnail != null) {
                int width  = thumbnail.getWidth() - thumbnail.getWidth() % 16;
                int height = Math.round(thumbnail.getHeight() * (width / (float) thumbnail.getWidth()));
                thumbnail  = Bitmap.createScaledBitmap(thumbnail, width, height, true);
            }

        }
        return thumbnail;
    }
}
