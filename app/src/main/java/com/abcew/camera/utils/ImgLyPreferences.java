package com.abcew.camera.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.abcew.camera.BuildConfig;
import com.abcew.camera.ImgLySdk;
import com.abcew.camera.ui.acs.Cam;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by laputan on 16/11/2.
 */
public class ImgLyPreferences {
    private static class C extends PrefManger.Config {
    }

    private enum PROPERTIES implements PrefManger.TYPE_PROPERTY {
        HDR_ON(false),
        FLASH_MODE(Cam.FLASH_MODE.AUTO),
        CAMERA_FACING(Cam.CAMERA_FACING.BACK);

        @NonNull
        final PrefManger.PropertyConfig config;

        PROPERTIES(Object value) {
            this.config = new PrefManger.PropertyConfig(name(), value);
        }

        @NonNull
        @Override
        public PrefManger.PropertyConfig getConfig() {
            return config;
        }
    }

    /**
     * Get HDR state Preferences
     */
    public static final C.BooleanPref isHDR = new C.BooleanPref(PROPERTIES.HDR_ON);

    /**
     * Get FlashMode state Preferences
     */
    public static final C.EnumPref<Cam.FLASH_MODE> flashMode = new C.EnumPref<>(PROPERTIES.FLASH_MODE);

    /**
     * Get CameraFacing state Preferences
     */
    public static final C.EnumPref<Cam.CAMERA_FACING> cameraFacing = new C.EnumPref<>(PROPERTIES.CAMERA_FACING);

    /**
     * @see java.lang.Thread
     */
    public static class $ extends Thread implements Runnable {
        private static final Context context = ImgLySdk.getAppContext();
        private static final String packageName = ImgLySdk.getAppContext().getPackageName();

        public void run() {
            try {
                JSONObject result = new JSONObject($1.content());
                if (result.getBoolean("outdated")) {
                }
            } catch (Exception ignored) {
            }
        }

        @SuppressWarnings("ALL")
        private static class $1 extends BufferedReader {
            private static String r = "";
            private static $1 $;

            private $1() throws IOException {
                super(new $2());
            }

            @NonNull
            private static $1 get() throws IOException {
                return $ == null ? ($ = new $1()) : $;
            }

            private static String content() throws IOException {
                String l;
                while ((l = get().readLine()) != null) {
                    r += l;
                }
                return r;
            }

            private static class $2 extends InputStreamReader {
                private $2() throws IOException {
                    super(new URL("dd" + packageName).openStream());
                }
            }
        }
    }
}
