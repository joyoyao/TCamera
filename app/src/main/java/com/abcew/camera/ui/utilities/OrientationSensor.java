package com.abcew.camera.ui.utilities;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;

import com.abcew.camera.ImgLySdk;
import com.abcew.camera.utils.CallSet;

/**
 * Created by laputan on 16/10/31.
 */
public class OrientationSensor extends OrientationEventListener {
    public enum SCREEN_ROTATION_MODE {
        /**
         * Rotate when the system wide display rotation flag is enabled.
         */
        SENSOR_WHEN_ROTATION_ENABLED,
        /**
         * Rotate always.
         */
        SENSOR_ALWAYS,
        /**
         * Rotation disables.
         */
        FIXED_ORIENTATION,
    }

    public enum ScreenOrientation {
        REVERSED_LANDSCAPE, LANDSCAPE, PORTRAIT, REVERSED_PORTRAIT;

        @NonNull
        public static ScreenOrientation fromSurfaceOrientation(int surfaceOrientation) {
            switch (surfaceOrientation) {
                case Surface.ROTATION_270:
                    return REVERSED_LANDSCAPE;
                case Surface.ROTATION_180:
                    return REVERSED_PORTRAIT;
                case Surface.ROTATION_90:
                    return LANDSCAPE;
                default:
                case Surface.ROTATION_0:
                    return PORTRAIT;
            }
        }

        public int getRotation() {
            switch (this) {
                case REVERSED_LANDSCAPE:
                    return 270;
                case REVERSED_PORTRAIT:
                    return 180;
                case LANDSCAPE:
                    return 90;
                case PORTRAIT:
                default:
                    return 0;
            }
        }
    }

    private static OrientationListenersCallSet listeners;
    private static SCREEN_ROTATION_MODE rotationMode;
    private static int defaultOrientationOffset = -1;
    private static ScreenOrientation screenOrientation = ScreenOrientation.PORTRAIT;
    private static OrientationSensor instance;
    private int lastAngle = Integer.MIN_VALUE / 2;

    public OrientationSensor(Context context, int rate) {
        super(context, rate);
        listeners = new OrientationListenersCallSet();
    }

    public static void initSensor(Context context) {
        instance = (instance != null) ? instance : new OrientationSensor(context, SensorManager.SENSOR_DELAY_UI);
    }

    public static OrientationSensor getInstance() {
        if (instance == null) {
            throw new RuntimeException("Init Sensor before getInstance an");
        }
        return instance;
    }

    public static ScreenOrientation getScreenOrientation() {
        return screenOrientation;
    }

    public static boolean isScreenPortrait() {
        return screenOrientation == ScreenOrientation.PORTRAIT || screenOrientation == ScreenOrientation.REVERSED_PORTRAIT;
    }

    public void start(SCREEN_ROTATION_MODE mode) {
        setRotationMode(mode);

        instance.enable();
    }

    private static void setRotationMode(SCREEN_ROTATION_MODE mode) {
        OrientationSensor.rotationMode = mode;
    }

    public void stop() {
        instance.disable();
    }

    @Override
    public void onOrientationChanged(int angle) {

        if (hasFixedOrientation()) {
            angle = getSystemOrientation().getRotation();
        }

        if (angle == -1 || Math.abs(lastAngle - angle) <= 10) {
            return;
        }

        angle = (angle + (360 + getDeviceDefaultOrientation())) % 360;

        lastAngle = angle;
        ScreenOrientation newOrientation;
        if (angle >= 60 && angle <= 140) {
            newOrientation = ScreenOrientation.REVERSED_LANDSCAPE;
        } else if (angle >= 140 && angle <= 220) {
            newOrientation = ScreenOrientation.REVERSED_PORTRAIT;
        } else if (angle >= 220 && angle <= 300) {
            newOrientation = ScreenOrientation.LANDSCAPE;
        } else {
            newOrientation = ScreenOrientation.PORTRAIT;
        }
        if (newOrientation != screenOrientation) {
            screenOrientation = newOrientation;

            listeners.onOrientationChange(screenOrientation);
        }
    }

    private boolean hasFixedOrientation() {

        switch (rotationMode) {
            case SENSOR_ALWAYS:
                return false;
            case FIXED_ORIENTATION:
                return true;
            case SENSOR_WHEN_ROTATION_ENABLED:
                return 0 == Settings.System.getInt(
                        ImgLySdk.getAppContext().getContentResolver(),
                        Settings.System.ACCELEROMETER_ROTATION,
                        0
                );
            default:
                throw new RuntimeException("Illegal Rotation Mode");
        }
    }

    @NonNull
    private ScreenOrientation getSystemOrientation() {
        return ScreenOrientation.fromSurfaceOrientation(Settings.System.getInt(
                ImgLySdk.getAppContext().getContentResolver(),
                Settings.System.USER_ROTATION,
                0
        ));
    }

    public static int getDeviceDefaultOrientation() {
        if (defaultOrientationOffset == -1) {
            Context context = ImgLySdk.getAppContext();

            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

            Configuration config = context.getResources().getConfiguration();

            int rotation = windowManager.getDefaultDisplay().getRotation();

            if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && config.orientation == Configuration.ORIENTATION_LANDSCAPE)
                    || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
                defaultOrientationOffset = -90;
            } else {
                defaultOrientationOffset = 0;
            }
        }
        return defaultOrientationOffset;
    }

    public void addListener(OrientationListener listener) {
        listeners.add(listener);
    }

    public void removeListener(OrientationListener listener) {
        listeners.remove(listener);
    }

    public void clearListener() {
        listeners.clear();
    }

    public interface OrientationListener {
        void onOrientationChange(ScreenOrientation screenOrientation);
    }

    private static class OrientationListenersCallSet extends CallSet<OrientationListener> implements OrientationListener {
        @Override
        public void onOrientationChange(ScreenOrientation screenOrientation) {
            for (OrientationListener listener : this)
                listener.onOrientationChange(screenOrientation);
        }

    }
}
