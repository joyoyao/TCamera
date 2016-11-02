package com.abcew.camera.configuration;

import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.abcew.camera.R;

/**
 * Created by laputan on 16/10/31.
 */
public class CropAspectConfig extends AbstractConfig implements AbstractConfig.AspectConfigInterface {
    public static final float CUSTOM_ASPECT = -1;

    private final float aspect;

    private final int cropWidth;
    private final int cropHeight;

    public CropAspectConfig(@StringRes int name, @DrawableRes int drawableId, float aspect) {
        super(name, drawableId);
        this.aspect = aspect;
        cropWidth  = -1;
        cropHeight = -1;
    }

    public CropAspectConfig(@StringRes int name, @DrawableRes int drawableId, int cropWidth, int cropHeight) {
        super(name, drawableId);
        this.aspect = cropWidth / (float) cropHeight;
        this.cropWidth  = cropWidth;
        this.cropHeight = cropHeight;
    }

    /**
     * Get Aspect the ration or #CropAspectConfig.CUSTOM_ASPECT if it is custom aspect mode.
     * @return cropWidth / cropHeight
     */
    public float getAspect() {
        return aspect;
    }

    public boolean hasSpecificSize() {
        return cropWidth > -1 && cropHeight > -1;
    }

    public int getCropWidth() {
        return cropWidth;
    }

    public int getCropHeight() {
        return cropHeight;
    }

    @Override
    public int getLayout() {
        return R.layout.imgly_list_item_crop;
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CropAspectConfig that = (CropAspectConfig) o;

        return Float.compare(that.aspect, aspect) == 0 && cropWidth == that.cropWidth && cropHeight == that.cropHeight;

    }

    @Override
    public int hashCode() {
        int result = (aspect != +0.0f ? Float.floatToIntBits(aspect) : 0);
        result = 31 * result + cropWidth;
        result = 31 * result + cropHeight;
        return result;
    }
}
