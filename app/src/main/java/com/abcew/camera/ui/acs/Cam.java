package com.abcew.camera.ui.acs;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Build;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.abcew.camera.ImgSdk;
import com.abcew.camera.configuration.AbstractConfig;
import com.abcew.camera.filter.NoneImageFilter;
import com.abcew.camera.ui.utilities.OrientationSensor;
import com.abcew.camera.utils.ExifUtils;
import com.abcew.camera.utils.ThreadUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;

/**
 * Created by laputan on 16/10/31.
 */
public class Cam {

    public enum CAMERA_FACING {
        FRONT (Camera.CameraInfo.CAMERA_FACING_FRONT),
        BACK  (Camera.CameraInfo.CAMERA_FACING_BACK),
        EXTERNAL(2);

        final int value; CAMERA_FACING(int value) {this.value = value;}
    }

    public enum SCENE_MODE {
        /**
         * Scene mode is off.
         */
        AUTO            (Camera.Parameters.SCENE_MODE_AUTO),
        /**
         * Take photos of fast moving objects. Same as {@link
         * #SPORTS}.
         */
        ACTION          (Camera.Parameters.SCENE_MODE_ACTION),
        /**
         * Take people pictures.
         */
        PORTRAIT        (Camera.Parameters.SCENE_MODE_PORTRAIT),
        /**
         * Take pictures on distant objects.
         */
        LANDSCAPE       (Camera.Parameters.SCENE_MODE_LANDSCAPE)
        /**
         * Take photos at night.
         */,
        NIGHT           (Camera.Parameters.SCENE_MODE_NIGHT),
        /**
         * Take people pictures at night.
         */
        NIGHT_PORTRAIT  (Camera.Parameters.SCENE_MODE_NIGHT_PORTRAIT),
        /**
         * Take photos in a theater. Flash light is off.
         */
        THEATRE         (Camera.Parameters.SCENE_MODE_THEATRE),
        /**
         * Take pictures on the beach.
         */
        BEACH           (Camera.Parameters.SCENE_MODE_BEACH),
        /**
         * Take pictures on the snow.
         */
        SNOW            (Camera.Parameters.SCENE_MODE_SNOW),
        /**
         * Take sunset photos.
         */
        SUNSET          (Camera.Parameters.SCENE_MODE_SUNSET),
        /**
         * Avoid blurry pictures (for example, due to hand shake).
         */
        STEADY_PHOTO    (Camera.Parameters.SCENE_MODE_STEADYPHOTO),
        /**
         * For shooting firework displays.
         */
        FIREWORKS       (Camera.Parameters.SCENE_MODE_FIREWORKS),
        /**
         * Take photos of fast moving objects. Same as {@link
         * #ACTION}.
         */
        SPORTS          (Camera.Parameters.SCENE_MODE_SPORTS),
        /**
         * Take indoor low-light shot.
         */
        PARTY           (Camera.Parameters.SCENE_MODE_PARTY),
        /**
         * Capture the naturally warm color of scenes lit by candles.
         */
        CANDLELIGHT     (Camera.Parameters.SCENE_MODE_CANDLELIGHT),
        /**
         * Applications are looking for a barcode. Camera driver will be
         * optimized for barcode reading.
         */
        BARCODE         (Camera.Parameters.SCENE_MODE_BARCODE),
        /**
         * Capture a scene using high dynamic range imaging techniques. The
         * camera will return an image that has an extended dynamic range
         * compared to a regular capture. Capturing such an image may take
         * longer than a regular capture.
         */
        HDR             (Build.VERSION.SDK_INT >= 17 ? Camera.Parameters.SCENE_MODE_HDR : "hdr");

        final String value; SCENE_MODE(String value) {this.value = value;}

        public static SCENE_MODE get(String string) {
            for (SCENE_MODE mode : values()) if (mode.value.equals(string)) return mode;
            return null;
        }
    }

    public enum FOCUS_MODE {
        /**
         * Auto-focus mode.
         */
        AUTO                (Camera.Parameters.FOCUS_MODE_AUTO),
        /**
         * Focus is set at infinity.
         */
        INFINITY            (Camera.Parameters.FOCUS_MODE_INFINITY),
        /**
         * Macro (close-up) focus mode.
         */
        MACRO               (Camera.Parameters.FOCUS_MODE_MACRO),
        /**
         * Focus is fixed. The camera is always in this mode if the focus is not
         * adjustable. If the camera has auto-focus, this mode can fix the
         * focus, which is usually at hyperfocal distance.
         */
        FIXED               (Camera.Parameters.FOCUS_MODE_FIXED),
        /**
         * Extended depth of field (EDOF). Focusing is done digitally and
         * continuously.
         */
        EDOF                (Camera.Parameters.FOCUS_MODE_EDOF),
        /**
         * Continuous auto focus mode intended for video recording. The camera
         * continuously tries to focus. This is the best choice for video
         * recording because the focus changes smoothly . Applications still can
         * take picture in this mode but the
         * subject may not be in focus. Auto focus starts when the parameter is
         * set.
         *
         * @see #CONTINUOUS_PICTURE
         */
        CONTINUOUS_VIDEO    (Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO),
        /**
         * Continuous auto focus mode intended for taking pictures. The camera
         * continuously tries to focus. The speed of focus change is more
         * aggressive than {@link #CONTINUOUS_VIDEO}. Auto focus
         * starts when the parameter is set.
         *
         * @see #CONTINUOUS_VIDEO
         */
        CONTINUOUS_PICTURE  (Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        final String value; FOCUS_MODE(String value) {this.value = value;}

        public static FOCUS_MODE get(String string) {
            for (FOCUS_MODE mode : values()) if (mode.value.equals(string)) return mode;
            return null;
        }
    }

    public enum WHITE_BALANCE {
        AUTO(Camera.Parameters.WHITE_BALANCE_AUTO),
        INCANDESCENT(Camera.Parameters.WHITE_BALANCE_INCANDESCENT),
        FLUORESCENT(Camera.Parameters.WHITE_BALANCE_FLUORESCENT),
        WARM_FLUORESCENT(Camera.Parameters.WHITE_BALANCE_WARM_FLUORESCENT),
        DAYLIGHT(Camera.Parameters.WHITE_BALANCE_DAYLIGHT),
        CLOUDY_DAYLIGHT(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT),
        TWILIGHT(Camera.Parameters.WHITE_BALANCE_TWILIGHT),
        SHADE(Camera.Parameters.WHITE_BALANCE_SHADE);

        final String value; WHITE_BALANCE(String value) {this.value = value;}

        public static WHITE_BALANCE get(String string){
            for (WHITE_BALANCE mode : values()) if (mode.value.equals(string)) return mode;
            return null;
        }
    }

    public enum ANTI_BANDING {
        AUTO(Camera.Parameters.ANTIBANDING_AUTO),
        RATE_50HZ(Camera.Parameters.ANTIBANDING_50HZ),
        RATE_60HZ(Camera.Parameters.ANTIBANDING_60HZ),
        OFF(Camera.Parameters.ANTIBANDING_OFF);

        final String value; ANTI_BANDING(String value) {this.value = value;}

        public static ANTI_BANDING get(String string){
            for (ANTI_BANDING mode : values()) if (mode.value.equals(string)) return mode;
            return null;
        }
    }

    public enum FLASH_MODE {
        /**
         * Flash will always be fired during snapshot. The flash may also be
         * fired during preview or auto-focus depending on the driver.
         */
        ON      (Camera.Parameters.FLASH_MODE_ON),
        /**
         * Flash will not be fired.
         */
        OFF     (Camera.Parameters.FLASH_MODE_OFF),
        /**
         * Flash will be fired automatically when required. The flash may be fired
         * during preview, auto-focus, or snapshot depending on the driver.
         */
        AUTO    (Camera.Parameters.FLASH_MODE_AUTO),
        /**
         * Constant emission of light during preview, auto-focus and snapshot.
         * This can also be used for video recording.
         */
        TORCH   (Camera.Parameters.FLASH_MODE_TORCH),
        /**
         * Flash will be fired in red-eye reduction mode.
         */
        RED_EYE (Camera.Parameters.FLASH_MODE_RED_EYE);

        final String value; FLASH_MODE(String value) {this.value = value;}

        public static FLASH_MODE get(String string){
            for (FLASH_MODE mode : values()) if (mode.value.equals(string)) return mode;
            return null;
        }
    }

    private CamView.CaptureCallback captureCallback;

    @Nullable
    private SurfaceTexture surfaceTexture;
    @Nullable
    private SurfaceHolder surfaceHolder;

    @Nullable
    private static Camera camInstance = null;
    private static final boolean hasCam = isAvailable();

    private int numberOfCameras = 0;

    private int cameraOrientation = 0;
    private int displayOrientation = 0;

    //private State state = new State();
    private final State stateManager = new State();

    private OnStateChangeListener onStateChangeListener;

    private static Cam instance;


    /**
     * Check if min. 1 Camera is available.
     * @return true if a Camera exists
     */
    public static synchronized boolean isAvailable() {
        return ImgSdk.getAppContext().getPackageManager().hasSystemFeature(Build.VERSION.SDK_INT > 17 ? PackageManager.FEATURE_CAMERA_ANY : PackageManager.FEATURE_CAMERA);
    }

    /**
     * Get a singleton Camera Instance.
     * @return Cam Class instance.
     */
    public static Cam getInstance() {
        if (instance == null) {
            instance = new Cam();
        }
        return instance;
    }

    private Cam() {
        numberOfCameras = Camera.getNumberOfCameras();
    }

    private void invalidateParameterState() {
        if (onStateChangeListener != null) {
            ThreadUtils.runOnMainThread(new ThreadUtils.MainThreadRunnable() {
                @Override
                public void run() {
                    if (onStateChangeListener != null) {
                        onStateChangeListener.onCamViewStateChange(getState());
                    }
                }
            });
        }
    }

    /**
     * Get the State of the current Camera settings. This is a singleton
     * @return Cam State object
     */
    @NonNull
    public State getState() {
        return stateManager;
    }

    /**
     * Set a listener to catch any settings state changes.
     * @param onStateChangeListener state change callback.
     */
    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        this.onStateChangeListener = onStateChangeListener;
    }

    /**
     * Set Preview Texture
     * @param surfaceTexture SurfaceTexture that should receive the Camera image
     */
    public synchronized void setSurface(SurfaceTexture surfaceTexture) {
        this.surfaceTexture = surfaceTexture;

        stateManager.invalidate();
    }

    /**
     * Set Preview Texture or a Surface holder
     * @param holder Surface holder that should receive the Camera image
     */
    public synchronized void setSurface(SurfaceHolder holder) {
        this.surfaceHolder = holder;

        stateManager.invalidate();
    }

    /**
     * * Set Camera Focus.
     * @param focusAreas list of focus areas
     */

    public synchronized void setFocus(List<Camera.Area> focusAreas) {
        Camera camera = hasCam ? getCamInstance() : null;

        if (camera != null) {
            stateManager.setFocusArea(focusAreas);
        }
    }


    /**
     * Try to set a flash mode and take a fallback mode if not supported by current Camera.
     *
     * @param mode flash mode.
     * @return return the accepted mode, it can be the same as requested or a supported fallback mode.
     */
    @Nullable
    public synchronized FLASH_MODE setFlashMode(FLASH_MODE mode) {
        stateManager.flashMode = mode;

        if (stateManager.sceneMode != SCENE_MODE.AUTO && mode != FLASH_MODE.OFF) {
            stateManager.sceneMode = SCENE_MODE.AUTO;
        }

        stateManager.invalidate();

        return stateManager.getFlashMode();
    }

    /**
     * Get the current FlashMode
     */
    public synchronized FLASH_MODE getFlashMode() {
        return stateManager.flashMode;
    }

    /**
     * Try to set a Scene mode and take a fallback mode if not supported by current Camera.
     *
     * @param mode scene mode.
     * @return return the accepted mode, it can be the same as requested or a supported fallback mode.
     */
    @Nullable
    public synchronized SCENE_MODE setSceneMode(SCENE_MODE mode) {

        stateManager.sceneMode = mode;

        if (stateManager.flashMode != FLASH_MODE.OFF && mode != SCENE_MODE.AUTO) {
            stateManager.flashMode = FLASH_MODE.OFF;
        }

        stateManager.invalidate();

        return stateManager.getSceneMode();
    }
    /**
     * Check if the current state has scene mode is available.
     *
     * @param mode scene mode.
     * @return return the accepted mode, it can be the same as requested or a supported fallback mode.
     */
    @Nullable
    public synchronized boolean hasSceneMode(String mode) {
        return stateManager.hasModeSupport("getSupportedSceneModes", mode);
    }

    /**
     * Try to set a Camera Facing.
     *
     * @param cameraFacing camera facing.
     * @return return the accepted camera facing.
     */
    public synchronized CAMERA_FACING setCameraFacing(CAMERA_FACING cameraFacing) {

        if (!hasFrontCamera()) {
            return CAMERA_FACING.BACK;
        }

        surfaceHolder = null;
        surfaceTexture = null;
        stateManager.changeCameraFacing(cameraFacing);

        return this.stateManager.getCameraFacing();
    }

    /**
     * Get the current camera facing.
     * @return current camera facing.
     */
    public CAMERA_FACING getCameraFacing() {
        return stateManager.getCameraFacing();
    }

    /**
     * * Take a Picture
     * @param callback Callback that fired after image will saved as jpg.
     * @param outputPath the output path for saving the image
     */
    public void takePicture(final CamView.CaptureCallback callback, String outputPath, final AbstractConfig.ImageFilterInterface filterInterface) {
        if (captureCallback != null) {
            return;
        }

        captureCallback = callback;
        shoot(new Cam.PictureCallback(outputPath) {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Cam.this.onPictureTaken(data, this.getOutputPath(),filterInterface);
            }
        });
    }

    private void onPictureTaken(@Nullable final byte[] data, @NonNull final String outputPath, final AbstractConfig.ImageFilterInterface filterInterface) {
        if (data == null) {

            Log.e("camera", "Camera picture is null");
            return;
        }

        final Date captureDate = new Date();
        final int exifOrientation = getCurrentExifOrientation();

        final Runnable saveOrgImage = new Runnable() {
            @Override


            public void run() {
                OutputStream orgStreamOutput;

                if (captureCallback == null) {
                    return;
                }

                try {
                    if(filterInterface==null||filterInterface instanceof NoneImageFilter){
                        orgStreamOutput = new FileOutputStream(outputPath);
                        orgStreamOutput.write(data);
                        orgStreamOutput.close();
                    }else {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Bitmap outBitmap =  filterInterface.renderImage(bitmap);
                        orgStreamOutput = new FileOutputStream(outputPath);
                        outBitmap.compress(Bitmap.CompressFormat.JPEG, 100, orgStreamOutput);
                        orgStreamOutput.close();
                        if(bitmap!=null&&!bitmap.isRecycled()){
                            bitmap.recycle();
                        }
                        if(outBitmap!=null&&!outBitmap.isRecycled()){
                            outBitmap.recycle();
                        }
                    }


                    ExifUtils.save(outputPath, captureDate, exifOrientation, false, null);

                    captureCallback.onImageCaptured(outputPath);
                } catch (IOException error) {
                    captureCallback.onImageCaptureError(error);
                } finally {
                    captureCallback = null;
                }
            }
        };

        Thread thread = new Thread(saveOrgImage);
        thread.start();
    }

    public int getCurrentExifOrientation() {
        int exif = 0;

        final OrientationSensor.ScreenOrientation screenOrientation = OrientationSensor.getScreenOrientation();
        final int screenRotation = screenOrientation.getRotation() + cameraOrientation;
        final boolean front = getCameraFacing() == CAMERA_FACING.FRONT;

        if (front) switch ((screenRotation / 90) % 4) {
            case 0:          exif = ExifInterface.ORIENTATION_ROTATE_180; break;
            case 1:          exif = ExifInterface.ORIENTATION_ROTATE_270; break;
            case 2: default: exif = ExifInterface.ORIENTATION_NORMAL;     break;
            case 3:          exif = ExifInterface.ORIENTATION_ROTATE_90;  break;

        } else switch ((screenRotation / 90) % 4) {
            case 0: default: exif = ExifInterface.ORIENTATION_NORMAL;     break;
            case 1:          exif = ExifInterface.ORIENTATION_ROTATE_270; break;
            case 2:          exif = ExifInterface.ORIENTATION_ROTATE_180; break;
            case 3:          exif = ExifInterface.ORIENTATION_ROTATE_90;  break;
        }



        return exif;
    }

    /**
     * Start the Camera Preview, this is async and will begin if the surface are set.
     */
    public synchronized void startPreview() {
        stateManager.startPreview();
    }

    /**
     * Stop the Camera Preview and release the surface.
     */
    public synchronized void stopPreview (boolean release) {
        if (release) {
            surfaceHolder = null;
            surfaceTexture = null;
        }
        stateManager.stopPreview(release);
    }

    private synchronized void shoot(PictureCallback callback) {
        Camera camera = hasCam ? getCamInstance() : null;
        if (camera != null) {
            try {
//                camera.setPreviewCallback(null);
//                camera.setOneShotPreviewCallback(null);
                camera.takePicture(null, callback, callback);
            } catch (Exception ignored) {}
        }
    }

    /**
     * Set the maximum TextureSize the device will support.
     * The preview only start, if it would be set.
     * @param maxTextureSize max OpenGl texture size
     * @param maxRenderBufferSize max OpenGl buffer size
     */
    public synchronized void setPreviewSize(final int maxTextureSize, final int maxRenderBufferSize) {

        stateManager.maxTextureSize = maxTextureSize;
        stateManager.maxRenderBufferSize = maxRenderBufferSize;
        stateManager.invalidate();
    }

    @Nullable
    private Camera getCamInstance() {
        return camInstance;
    }

    /**
     * Get Preview Size
     * @return
     */
    @Nullable
    public synchronized Cam.Size getPreviewSize() {
        return stateManager.getPreviewSize();
    }

    @Nullable
    @SuppressWarnings("unused")
    private synchronized Cam.Size getPictureSize() {
        return stateManager.pictureSize;
    }

    /*public synchronized void setRotation(int v1, int v2) {
        stateManager.setRotation(v1, v2);
    }*/

    private boolean hasFrontCamera() {
        return (numberOfCameras > 1);
    }

    /**
     * Abstract picture callback
     */
    public static abstract class PictureCallback implements Camera.PictureCallback {
        private final String outputPath;
        public PictureCallback(String outputPath) {
            this.outputPath = outputPath;
        }

        protected String getOutputPath(){
            return outputPath;
        }
    }

    /**
     * Size model
     */
    public class Size {
        /** width of the picture */
        public final int orgWidth;
        /** height of the picture */
        public final int orgHeight;

        /** width of the picture */
        public int width;
        /** height of the picture */
        public int height;

        private int orientation = 0;
        /**
         * Sets the dimensions for pictures.
         *
         * @param width the photo width (pixels)
         * @param height the photo height (pixels)
         */
        public Size(int width, int height, int orientation) {
            this.orgWidth = width;
            this.orgHeight = height;
            setOrientation(orientation);
        }

        public Size(@NonNull Camera.Size size, int orientation) {
            this(size.width, size.height, orientation);
        }

        private void setOrientation(int orientation) {
            this.orientation = orientation;
            width  = orientation % 180 == 90 ? orgHeight : orgWidth;
            height = orientation % 180 == 90 ? orgWidth : orgHeight;
        }

        private int getOrientation() {
            return orientation;
        }

        /**
         * Compares {@code obj} to this size.
         *
         * @param obj the object to compare this size with.
         * @return {@code true} if the width and height of {@code obj} is the
         *         same as those of this size. {@code false} otherwise.
         */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Size)) {
                return false;
            }
            Size s = (Size) obj;
            return width == s.width && height == s.height;
        }


        @Override
        public int hashCode() {
            return width * 32713 + height;
        }

    }

    /**
     * Camera state change Interface
     */
    public interface OnStateChangeListener {
        /**
         * Will fire if Camera parameter state would change
         * @param state Camera state
         * {@inheritDoc}
         */
        void onCamViewStateChange(State state);
    }


    /**
     * Get camera cameraOrientation
     * @return cameraOrientation of the camera.
     */
    public synchronized int getCameraOrientation() {
        return cameraOrientation;
    }

    /**
     * Check if Camera is Front Camera
     * @return true if current camera is front camera
     */
    public boolean isFront(){
        return stateManager.getCameraFacing().value == Camera.CameraInfo.CAMERA_FACING_FRONT;
    }

    /**
     * Current Camera state object
     */
    public class State {
        private static final int MAX_FPS = 30000;

        @Nullable
        private Camera.CameraInfo cameraInfo = null;

        //Camera Info
        @Nullable
        private Camera.Parameters currentParameters;
        private boolean hasFaceDetectionSupport;

        //Settings whit first time Defaults
        protected CAMERA_FACING cameraFacing = CAMERA_FACING.BACK;
        @Nullable
        protected Cam.Size previewSize = null;
        @Nullable
        protected Cam.Size pictureSize = null;
        @NonNull
        protected FOCUS_MODE focusMode = FOCUS_MODE.CONTINUOUS_PICTURE;
        protected SCENE_MODE sceneMode = SCENE_MODE.AUTO;
        protected FLASH_MODE flashMode = FLASH_MODE.OFF;
        protected final WHITE_BALANCE whiteBalanceMode = WHITE_BALANCE.AUTO;
        protected final ANTI_BANDING antiBandingMode = ANTI_BANDING.AUTO;
        protected final boolean faceDetection = false;
        @Nullable
        protected int[] fpsRange = null;

        protected int maxTextureSize = -1;
        protected int maxRenderBufferSize = -1;

        //Current State
        private boolean isFaceDetectionStarted = false;
        private boolean isRunning = false;

        private boolean isPreviewWaiting = false;

        private final Camera.AutoFocusCallback autoFocus = new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, @NonNull Camera camera) {
                if (!success) {
                    camera.autoFocus(this);
                }
            }
        };

        /**
         * Get current scene mode
         * @return current scene mode
         */
        @Nullable
        public SCENE_MODE getSceneMode() {

            if (sceneMode != SCENE_MODE.AUTO && flashMode != FLASH_MODE.OFF) {
                sceneMode = SCENE_MODE.AUTO;
            }

            return SCENE_MODE.get(getSupportedMode("getSupportedSceneModes", sceneMode.value, new String[] {
                    (Build.VERSION.SDK_INT >= 17 ? Camera.Parameters.SCENE_MODE_HDR : Camera.Parameters.SCENE_MODE_AUTO),
                    Camera.Parameters.SCENE_MODE_AUTO,

                    Camera.Parameters.SCENE_MODE_SPORTS,
                    Camera.Parameters.SCENE_MODE_ACTION,
                    Camera.Parameters.SCENE_MODE_AUTO,

                    Camera.Parameters.SCENE_MODE_STEADYPHOTO,
                    Camera.Parameters.SCENE_MODE_PORTRAIT,
                    Camera.Parameters.SCENE_MODE_AUTO,

                    Camera.Parameters.SCENE_MODE_SUNSET,
                    Camera.Parameters.SCENE_MODE_BEACH,
                    Camera.Parameters.SCENE_MODE_LANDSCAPE,
                    Camera.Parameters.SCENE_MODE_AUTO,

                    Camera.Parameters.SCENE_MODE_SNOW,
                    Camera.Parameters.SCENE_MODE_LANDSCAPE,
                    Camera.Parameters.SCENE_MODE_AUTO,

                    Camera.Parameters.SCENE_MODE_THEATRE,
                    Camera.Parameters.SCENE_MODE_CANDLELIGHT,
                    Camera.Parameters.SCENE_MODE_PARTY,
                    Camera.Parameters.SCENE_MODE_FIREWORKS,
                    Camera.Parameters.SCENE_MODE_NIGHT,

                    Camera.Parameters.SCENE_MODE_NIGHT_PORTRAIT,
                    Camera.Parameters.SCENE_MODE_NIGHT,
                    Camera.Parameters.SCENE_MODE_AUTO,

                    Camera.Parameters.SCENE_MODE_BARCODE,
                    Camera.Parameters.SCENE_MODE_AUTO
            }));
        }

        /**
         * Get current focus mode
         * @return current focus mode
         */
        @Nullable
        public FOCUS_MODE getFocusMode() {

            return FOCUS_MODE.get(getSupportedMode("getSupportedFocusModes", focusMode.value, new String[] {
                    Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE,
                    Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO,
                    Camera.Parameters.FOCUS_MODE_AUTO,
                    Camera.Parameters.FOCUS_MODE_INFINITY,
                    Camera.Parameters.FOCUS_MODE_FIXED
            }));
        }

        /**
         * Get current flash mode
         * @return current flash mode
         */
        @Nullable
        public FLASH_MODE getFlashMode() {

            if (sceneMode != SCENE_MODE.AUTO && flashMode != FLASH_MODE.OFF) {
                flashMode = FLASH_MODE.OFF;
            }

            return FLASH_MODE.get(getSupportedMode("getSupportedFlashModes", flashMode.value, new String[] {
                    Camera.Parameters.FLASH_MODE_TORCH,
                    Camera.Parameters.FLASH_MODE_OFF,
                    Camera.Parameters.FLASH_MODE_RED_EYE,
                    Camera.Parameters.FLASH_MODE_AUTO,
                    Camera.Parameters.FLASH_MODE_ON,
                    Camera.Parameters.FLASH_MODE_OFF
            }));
        }

        /**
         * Get current white balance mode
         * @return current white balance mode
         */
        @Nullable
        public WHITE_BALANCE getWhiteBalanceMode() {
            return WHITE_BALANCE.get(getSupportedMode("getSupportedWhiteBalance", whiteBalanceMode.value, new String[] {
                    Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT,
                    Camera.Parameters.WHITE_BALANCE_DAYLIGHT,
                    Camera.Parameters.WHITE_BALANCE_AUTO,
                    Camera.Parameters.WHITE_BALANCE_INCANDESCENT,
                    Camera.Parameters.WHITE_BALANCE_AUTO,
                    Camera.Parameters.WHITE_BALANCE_AUTO,
                    Camera.Parameters.WHITE_BALANCE_WARM_FLUORESCENT,
                    Camera.Parameters.WHITE_BALANCE_FLUORESCENT,
                    Camera.Parameters.WHITE_BALANCE_AUTO,
                    Camera.Parameters.WHITE_BALANCE_SHADE,
                    Camera.Parameters.WHITE_BALANCE_TWILIGHT,
                    Camera.Parameters.WHITE_BALANCE_AUTO,
            }));
        }

        /**
         * Get current anti banding mode
         * @return current anti banding mode
         */
        @Nullable
        public ANTI_BANDING getAntiBandingMode() {
            return ANTI_BANDING.get(getSupportedMode("getSupportedAntibanding", antiBandingMode.value, new String[] {
                    Camera.Parameters.ANTIBANDING_60HZ,
                    Camera.Parameters.ANTIBANDING_AUTO,
                    Camera.Parameters.ANTIBANDING_50HZ,
                    Camera.Parameters.ANTIBANDING_AUTO,
                    Camera.Parameters.ANTIBANDING_OFF
            }));
        }

        @Nullable
        private Integer getPictureFormat() {
            return getSupportedMode("getPictureFormat", ImageFormat.JPEG, new Integer[] {
                    ImageFormat.JPEG
            });
        }

        private void setFocusArea(List<Camera.Area> focusAreas) {

            Camera.Parameters params = getParameters();

            if (params != null) {
                Camera camera = getCamInstance();

                camera.cancelAutoFocus();

                focusMode = FOCUS_MODE.AUTO;

                FOCUS_MODE mode = getFocusMode();
                if (mode != null) params.setFocusMode(mode.value);

                if (params.getMaxNumFocusAreas() > 0) {
                    params.setFocusAreas(focusAreas);
                }

                if (params.getMaxNumMeteringAreas() > 0) {
                    params.setMeteringAreas(focusAreas);
                }

                try {
                    camera.setParameters(params);
                    camera.autoFocus(autoFocus);
                } catch (RuntimeException ignored) {}
            }
        }

        /**
         * Get preview frame rate range
         * @return the current range of the camera preview
         */
        @Nullable
        public int[] getFrameRateRange() {
            Camera.Parameters params = getParameters();
            if (fpsRange == null && params != null) {
                if (params.getSupportedPreviewFpsRange().size() <= 1) {
                    fpsRange = params.getSupportedPreviewFpsRange().get(0);
                } else {
                    for (int[] range : params.getSupportedPreviewFpsRange()) {
                        if (range[0] > MAX_FPS || range[1] > MAX_FPS) {
                            continue;
                        }

                        if (fpsRange == null) {
                            fpsRange = range;
                        } else if (range[0] > fpsRange[0] || range[1] > fpsRange[1]) {
                            fpsRange = range;
                        }
                    }
                }
            }
            return fpsRange;
        }


        /**
         * Get face detection state
         * @return true if face detection ist supported and enabled
         */
        public boolean isFaceDetection() {
            return hasFaceDetectionSupport && faceDetection;
        }

        /**
         * Get current camera facing
         * @return current camera facing
         */
        public CAMERA_FACING getCameraFacing() {
            return cameraFacing;
        }

        /*public Size getPictureSize() {
            return pictureSize;
        }*/

        private <T> boolean hasModeSupport (String methodName, @NonNull T preferred) {
            Camera.Parameters params = getParameters();
            if (params != null) {
                List modes = null;
                try {
                    java.lang.reflect.Method method;
                    method = params.getClass().getMethod(methodName);

                    Object object = method.invoke(params);
                    if (object instanceof List) {
                        List list = (List) object;
                        if (list.size() > 0) {
                            modes = (List) method.invoke(params);
                        }
                    }
                } catch ( @NonNull SecurityException
                        | NoSuchMethodException
                        | InvocationTargetException
                        | IllegalAccessException ignored
                        ) {}

                if (modes != null) {
                    return modes.contains(preferred);
                }
            }
            return false;
        }

        private <T> T getSupportedMode(String methodName, @Nullable T preferred, @NonNull Object[] fallbacks) {
            Camera.Parameters params = getParameters();
            if (params != null) {
                List modes = null;
                try {
                    java.lang.reflect.Method method;
                    method = params.getClass().getMethod(methodName);

                    Object object = method.invoke(params);
                    if (object instanceof List) {
                        List list = (List) object;
                        if (list.size() > 0) {
                            modes = (List) method.invoke(params);
                        }
                    }
                } catch ( @NonNull SecurityException
                        | NoSuchMethodException
                        | InvocationTargetException
                        | IllegalAccessException ignored
                        ) {}

                if(modes != null) {
                    boolean hasStartPosition = preferred == null;
                    for (Object mode : fallbacks) {
                        if (!hasStartPosition && !mode.equals(preferred)){
                            continue;
                        } else {
                            hasStartPosition = true;
                        }
                        if (modes.contains(mode)) {
                            try {
                                return (T) mode;
                            } catch (ClassCastException ignored) {}
                        }
                    }
                }
            }
            return null;
        }

        @Nullable
        private synchronized Camera.Parameters getParameters() {
            if (currentParameters == null && hasCam) {
                Camera camera = getCamInstance();
                currentParameters = camera != null ? camera.getParameters() : null;
            }
            return currentParameters;
        }

        private  synchronized void changeCameraFacing(CAMERA_FACING facing) {
            if (cameraFacing != facing) {
                cameraFacing = facing;
                init();
            }
        }


        private synchronized void checkPreviewRun() {
            if (isPreviewWaiting) {
                isPreviewWaiting = false;
                startPreview();
            }
        }

        private synchronized void startPreview() {

            if (hasCam && getCamInstance() == null) {
                init();
            }

            Camera camera = hasCam ? getCamInstance() : null;
            isPreviewWaiting = true;

            if (camera != null && !isRunning && (surfaceTexture != null || surfaceHolder != null)) {
                try{
                    camera.startPreview();
                } catch (Exception ignored) {
                    ThreadUtils.runOnMainThread(new ThreadUtils.MainThreadRunnable() {
                        @Override
                        @MainThread
                        public void run() {
                            Toast.makeText(ImgSdk.getAppContext(), "Camera Error",  Toast.LENGTH_LONG).show();
                        }
                    });
                }
                isPreviewWaiting = false;
                isRunning = true;
            }
        }

        private synchronized void stopPreview(boolean release) {
            Camera camera = hasCam ? getCamInstance() : null;

            if (camera != null && isRunning) {
                camera.cancelAutoFocus();
                camera.setPreviewCallback(null);
                camera.stopPreview();

                isRunning = false;

                if (isFaceDetectionStarted) {
                    camera.stopFaceDetection();
                    isFaceDetectionStarted = false;
                }
            }

            if (release) {
                releaseCamera();
            }
        }

        private synchronized void releaseCamera() {
            Camera camera = hasCam ? getCamInstance() : null;
            if (camera != null) {
                isRunning = false;
                fpsRange = null;
                cameraInfo = null;
                pictureSize = null;
                previewSize = null;
                camInstance = null;
                currentParameters = null;
                camera.release();
            }
        }

        private synchronized boolean init() {

            if (!hasCam) return false;

            int face = -1;
            try {
                face = stateManager.getCameraFacing() != null ? stateManager.getCameraFacing().value : 0;
                if (camInstance != null) {
                    stopPreview(true);
                }

                camInstance = Camera.open(face);
                currentParameters = getParameters();
                invalidate();
                return true;
            } catch (Exception e) {
                Log.e("glbla", "Camera init Exception in face: " + face, e);
                releaseCamera();
            }
            return false;
        }

        @Nullable
        private synchronized Cam.Size getPreviewSize() {
            Camera camera = hasCam ? getCamInstance() : null;

            final int maxPreviewSizeLength = Math.min(maxTextureSize, maxRenderBufferSize);
            final int screenSize = ImgSdk.getAppResource().getDisplayMetrics().widthPixels * ImgSdk.getAppResource().getDisplayMetrics().heightPixels;

            Camera.Parameters params = getParameters();
            if (previewSize == null && camera != null  && params != null && maxPreviewSizeLength > 0) {
                for (Camera.Size size : params.getSupportedPreviewSizes()) {
                    if (maxPreviewSizeLength < Math.max(size.height, size.width) || screenSize < size.height * size.width) {
                        continue;
                    }

                    if (previewSize == null || size.height * size.width > previewSize.height * previewSize.width) {
                        previewSize = new Size(size, displayOrientation);
                    }
                }
            }
            return previewSize;
        }

        @Nullable
        private synchronized Cam.Size getPictureSize() {

            Camera camera = hasCam ? getCamInstance() : null;

            Camera.Parameters params = getParameters();
            if (pictureSize == null && camera != null  && params != null) {
                for (Camera.Size size : params.getSupportedPictureSizes()) {
                    if (pictureSize == null || size.height * size.width > pictureSize.height * pictureSize.width) {
                        pictureSize = new Size(size, 0);
                    }
                }
            }

            return pictureSize;
        }

        @NonNull
        private Camera.CameraInfo getCamInfo() {
            if (cameraInfo == null) {
                cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(cameraFacing.value, cameraInfo);
            }
            return cameraInfo;
        }

        int orientationOffset = 180;
        int displayOffset = 180;

        public void setRotation(int v1, int v2) {
            orientationOffset = v1;
            displayOffset = v2;
            invalidate();
        }

        private synchronized void invalidate() {
            Camera camera = getCamInstance();

            if (camera != null && currentParameters != null) try {

                hasFaceDetectionSupport = currentParameters.getMaxNumDetectedFaces() > 0;

                //Set Parameters
                FOCUS_MODE focusMode = getFocusMode();
                if (focusMode != null) currentParameters.setFocusMode(focusMode.value);

                FLASH_MODE flashMode = getFlashMode();
                if (flashMode != null) currentParameters.setFlashMode(flashMode.value);

                //Call Scene Mode after FlashMode
                SCENE_MODE sceneMode = getSceneMode();
                if (sceneMode != null) currentParameters.setSceneMode(sceneMode.value);

                WHITE_BALANCE whiteBalanceMode = getWhiteBalanceMode();
                if (whiteBalanceMode != null) currentParameters.setWhiteBalance(whiteBalanceMode.value);

                ANTI_BANDING bandingMode = getAntiBandingMode();
                if (bandingMode != null) currentParameters.setAntibanding(bandingMode.value);

                Integer pictureMode = getPictureFormat();
                if (pictureMode != null) currentParameters.setPictureFormat(pictureMode);

                cameraOrientation = (getCamInfo().orientation + 360 - 180 + OrientationSensor.getDeviceDefaultOrientation()) % 360;
                displayOrientation = (cameraOrientation + 360) % 360;

                //270 - 90

                camera.setDisplayOrientation(displayOrientation);

                if (!isFaceDetectionStarted && isFaceDetection()) {
                    camera.startFaceDetection();
                    isFaceDetectionStarted = true;
                } else if (isFaceDetectionStarted && !isFaceDetection()) {
                    camera.stopFaceDetection();
                    isFaceDetectionStarted = false;
                }

                int[] range = getFrameRateRange();
                if (range != null) {
                    currentParameters.setPreviewFpsRange(range[0], range[1]);
                }

                Cam.Size previewSize = getPreviewSize();
                if (previewSize != null) {
                    currentParameters.setPreviewSize(previewSize.orgWidth, previewSize.orgHeight);
                }


                Cam.Size pictureSize = getPictureSize();
                if (pictureSize != null) {
                    currentParameters.setPictureSize(pictureSize.orgWidth, pictureSize.orgHeight);
                }

                camera.setParameters(currentParameters);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && camInstance != null) {
                    camInstance.enableShutterSound(true);
                }

                if (surfaceTexture != null) {
                    try {
                        camera.setPreviewTexture(surfaceTexture);
                    } catch (Exception ignored) {}
                } else if (surfaceHolder != null) {
                    try {
                        camera.setPreviewDisplay(surfaceHolder);
                    } catch (Exception ignored) {}
                }
                checkPreviewRun();
            } catch (RuntimeException ignored) { /* it is imposable to prevent all runtime Exceptions on all devices */ }

            invalidateParameterState();
        }
    }
}
