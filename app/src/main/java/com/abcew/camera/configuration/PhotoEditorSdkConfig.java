package com.abcew.camera.configuration;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.abcew.camera.filter.NoneImageFilter;
import com.abcew.camera.ui.utilities.OrientationSensor.SCREEN_ROTATION_MODE;

import java.util.ArrayList;

/**
 * Created by laputan on 16/10/31.
 */
public class PhotoEditorSdkConfig {
    private static final String fontAssetsFolder = "fonts/";

//    private static final ArrayList<AbstractConfig.ToolConfigInterface> tools = new ArrayList<>();
    private static final ArrayList<AbstractConfig.FontConfigInterface> fonts = new ArrayList<>();
    private static final ArrayList<AbstractConfig.ImageFilterInterface> filter = new ArrayList<>();
    private static final ArrayList<AbstractConfig.AspectConfigInterface> aspects = new ArrayList<>();
    private static final ArrayList<AbstractConfig.StickerConfigInterface> stickers = new ArrayList<>();
    private static final ArrayList<AbstractConfig.ColorConfigInterface> textColors = new ArrayList<>();
    private static final ArrayList<AbstractConfig.ColorConfigInterface> brushColors = new ArrayList<>();

    private static boolean isForceCropCaptureEnabled;

    private static SCREEN_ROTATION_MODE cameraScreenRotationMode = SCREEN_ROTATION_MODE.SENSOR_ALWAYS;
    private static SCREEN_ROTATION_MODE editorScreenRotationMode = SCREEN_ROTATION_MODE.SENSOR_WHEN_ROTATION_ENABLED;

    @Nullable
    private static CropAspectConfig forcePortraitCrop = null;
    @Nullable
    private static CropAspectConfig forceLandscapeCrop = null;
    static {
        filter.add(new NoneImageFilter());

    }

    /**
     * Change Fontset
     * <p>
     * <pre>
     *  // Step1 get current configuration.
     * ArrayList&lt;AbstractConfig.FontConfigInterface&gt; fonts = PhotoEditorSdkConfig.getCropConfig();
     *
     * // Step2 optional clear it.
     * fonts.clear();
     *
     * // Step3 add the needed fonts
     * fonts.add(new FontConfig("Geared Slab", fontAssetsFolder + "GearedSlab.ttf"));
     * </pre>
     */
    @NonNull
    public static ArrayList<AbstractConfig.FontConfigInterface> getFontConfig() {
        return fonts;
    }


    public static boolean isForceCropCaptureEnabled() {
        return isForceCropCaptureEnabled;
    }


    /**
     * @deprecated Please use {@link #getTextColorConfig()} instead. Will be removed in the next big version update.
     */
    @NonNull
    @Deprecated
    public static ArrayList<AbstractConfig.ColorConfigInterface> getColorConfig() { // TODO: Remove in next version.
        Log.e("deprecated", "Deprecation warning getColorConfig() will be removed in the next version, please use getTextColorConfig()");
        return textColors;
    }

    /**
     * Add a or remove text colors
     * <p>
     * <pre>
     * // Step1 get current configuration.
     * ArrayList&lt;AbstractConfig.ColorConfigInterface&gt; stickers = PhotoEditorSdkConfig.getTextColorConfig();
     *
     * // Step2 optional clear it.
     * stickers.clear();
     *
     * // Step3 add the needed textColors
     * textColors.add(new ColorConfig(R.string.imgly_color_accessibility_name, 0xF4744D));
     * </pre>
     */
    @NonNull
    public static ArrayList<AbstractConfig.ColorConfigInterface> getTextColorConfig() {
        return textColors;
    }

    /**
     * Add a or remove  brush colors @see #getTextColorConfig()
     * <p>
     * <pre>
     * // Step1 get current configuration.
     * ArrayList&lt;AbstractConfig.ColorConfigInterface&gt; stickers = PhotoEditorSdkConfig.getTextColorConfig();
     *
     * // Step2 optional clear it.
     * stickers.clear();
     *
     * // Step3 add the needed textColors
     * textColors.add(new ColorConfig(R.string.imgly_color_accessibility_name, 0xF4744D));
     * </pre>
     */
    public static ArrayList<AbstractConfig.ColorConfigInterface> getBrushColors() {
        return brushColors;
    }

    /**
     * Set screen rotation mode in editor mode.
     * Default: #SCREEN_ROTATION_MODE.SENSOR_WHEN_ROTATION_ENABLED
     * @param mode desired screen mode
     */
    public static void setEditorScreenRotationMode(SCREEN_ROTATION_MODE mode) {
        PhotoEditorSdkConfig.editorScreenRotationMode = mode;
    }

    /**
     * Set screen rotation mode in camera mode.
     * Default: #SCREEN_ROTATION_MODE.SENSOR_ALWAYS
     * @param mode desired screen mode
     */
    public static void setCameraScreenRotationMode(SCREEN_ROTATION_MODE mode) {
        PhotoEditorSdkConfig.cameraScreenRotationMode = mode;
    }

    public static SCREEN_ROTATION_MODE getCameraScreenRotationMode() {
        return cameraScreenRotationMode;
    }

    public static SCREEN_ROTATION_MODE getEditorScreenRotationMode() {
        return editorScreenRotationMode;
    }


    @Nullable
    public static CropAspectConfig getForcePortraitCrop() {
        return forcePortraitCrop;
    }


    @Nullable
    public static CropAspectConfig getForceLandscapeCrop() {
        return forceLandscapeCrop;
    }


    @NonNull
    public static ArrayList<AbstractConfig.ImageFilterInterface> getFilterConfig() {
        if (filter.size() == 0) {
            filter.add(new NoneImageFilter());
        }

        return filter;
    }

}
