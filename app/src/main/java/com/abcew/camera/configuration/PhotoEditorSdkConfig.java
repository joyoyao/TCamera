package com.abcew.camera.configuration;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.abcew.camera.filter.ColorFilterAD1920;
import com.abcew.camera.filter.ColorFilterAncient;
import com.abcew.camera.filter.ColorFilterBW;
import com.abcew.camera.filter.ColorFilterBleached;
import com.abcew.camera.filter.ColorFilterBleachedBlue;
import com.abcew.camera.filter.ColorFilterBlueShadows;
import com.abcew.camera.filter.ColorFilterBlues;
import com.abcew.camera.filter.ColorFilterBreeze;
import com.abcew.camera.filter.ColorFilterCelsius;
import com.abcew.camera.filter.ColorFilterClassic;
import com.abcew.camera.filter.ColorFilterColorful;
import com.abcew.camera.filter.ColorFilterCool;
import com.abcew.camera.filter.ColorFilterCottonCandy;
import com.abcew.camera.filter.ColorFilterCreamy;
import com.abcew.camera.filter.ColorFilterEighties;
import com.abcew.camera.filter.ColorFilterElder;
import com.abcew.camera.filter.ColorFilterEvening;
import com.abcew.camera.filter.ColorFilterFall;
import com.abcew.camera.filter.ColorFilterFixie;
import com.abcew.camera.filter.ColorFilterFood;
import com.abcew.camera.filter.ColorFilterFridge;
import com.abcew.camera.filter.ColorFilterFront;
import com.abcew.camera.filter.ColorFilterGlam;
import com.abcew.camera.filter.ColorFilterHighCarb;
import com.abcew.camera.filter.ColorFilterHighContrast;
import com.abcew.camera.filter.ColorFilterK1;
import com.abcew.camera.filter.ColorFilterK2;
import com.abcew.camera.filter.ColorFilterK6;
import com.abcew.camera.filter.ColorFilterKDynamic;
import com.abcew.camera.filter.ColorFilterKeen;
import com.abcew.camera.filter.ColorFilterLenin;
import com.abcew.camera.filter.ColorFilterLitho;
import com.abcew.camera.filter.ColorFilterLomo;
import com.abcew.camera.filter.ColorFilterLomo100;
import com.abcew.camera.filter.ColorFilterLucid;
import com.abcew.camera.filter.ColorFilterMellow;
import com.abcew.camera.filter.ColorFilterNeat;
import com.abcew.camera.filter.ColorFilterNoGreen;
import com.abcew.camera.filter.ColorFilterOrchid;
import com.abcew.camera.filter.ColorFilterPale;
import com.abcew.camera.filter.ColorFilterPola669;
import com.abcew.camera.filter.ColorFilterPolaSx;
import com.abcew.camera.filter.ColorFilterPro400;
import com.abcew.camera.filter.ColorFilterQuozi;
import com.abcew.camera.filter.ColorFilterSepiahigh;
import com.abcew.camera.filter.ColorFilterSettled;
import com.abcew.camera.filter.ColorFilterSeventies;
import com.abcew.camera.filter.ColorFilterSin;
import com.abcew.camera.filter.ColorFilterSoft;
import com.abcew.camera.filter.ColorFilterSteel;
import com.abcew.camera.filter.ColorFilterSummer;
import com.abcew.camera.filter.ColorFilterSunset;
import com.abcew.camera.filter.ColorFilterTender;
import com.abcew.camera.filter.ColorFilterTexas;
import com.abcew.camera.filter.ColorFilterTwilight;
import com.abcew.camera.filter.ColorFilterWinter;
import com.abcew.camera.filter.ColorFilterX400;
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
        filter.add(new ColorFilterAD1920());
        filter.add(new ColorFilterAncient());
        filter.add(new ColorFilterBleached());
        filter.add(new ColorFilterBleachedBlue());
        filter.add(new ColorFilterBlues());
        filter.add(new ColorFilterBlueShadows());
        filter.add(new ColorFilterBreeze());
        filter.add(new ColorFilterBW());
        filter.add(new ColorFilterCelsius());
        filter.add(new ColorFilterClassic());
        filter.add(new ColorFilterColorful());
        filter.add(new ColorFilterCool());
        filter.add(new ColorFilterCottonCandy());
        filter.add(new ColorFilterCreamy());
        filter.add(new ColorFilterEighties());
        filter.add(new ColorFilterElder());
        filter.add(new ColorFilterEvening());
        filter.add(new ColorFilterFall());
        filter.add(new ColorFilterFixie());
        filter.add(new ColorFilterFood());
        filter.add(new ColorFilterFridge());
        filter.add(new ColorFilterFront());
        filter.add(new ColorFilterGlam());
        filter.add(new ColorFilterHighCarb());
        filter.add(new ColorFilterHighContrast());
        filter.add(new ColorFilterK1());
        filter.add(new ColorFilterK2());
        filter.add(new ColorFilterK6());
        filter.add(new ColorFilterKDynamic());
        filter.add(new ColorFilterKeen());
        filter.add(new ColorFilterLenin());
        filter.add(new ColorFilterLitho());
        filter.add(new ColorFilterLomo());
        filter.add(new ColorFilterLomo100());
        filter.add(new ColorFilterLucid());
        filter.add(new ColorFilterMellow());
        filter.add(new ColorFilterNeat());
        filter.add(new ColorFilterNoGreen());
        filter.add(new ColorFilterOrchid());
        filter.add(new ColorFilterPale());
        filter.add(new ColorFilterPola669());
        filter.add(new ColorFilterPolaSx());
        filter.add(new ColorFilterPro400());
        filter.add(new ColorFilterQuozi());
        filter.add(new ColorFilterSepiahigh());
        filter.add(new ColorFilterSettled());
        filter.add(new ColorFilterSeventies());
        filter.add(new ColorFilterSin());
        filter.add(new ColorFilterSoft());
        filter.add(new ColorFilterSteel());
        filter.add(new ColorFilterSummer());
        filter.add(new ColorFilterSunset());
        filter.add(new ColorFilterTender());
        filter.add(new ColorFilterTexas());
        filter.add(new ColorFilterTwilight());
        filter.add(new ColorFilterWinter());
        filter.add(new ColorFilterX400());

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
