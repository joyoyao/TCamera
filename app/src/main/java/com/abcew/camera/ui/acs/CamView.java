package com.abcew.camera.ui.acs;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.abcew.camera.ImgSdk;
import com.abcew.camera.configuration.AbstractConfig;
import com.abcew.camera.configuration.PhotoEditorSdkConfig;
import com.abcew.camera.ui.utilities.OrientationSensor;
import com.abcew.camera.utils.ImageViewUtil;

/**
 * Created by laputan on 16/10/31.
 */
public class CamView extends ViewGroup implements Cam.OnStateChangeListener, OrientationSensor.OrientationListener {
    private static final String TAG = "CamView";

    private final Cam camera;
    @Nullable
    private Preview preview;
    private View previewView;

    private OnSizeChangeListener sizeChangeListener;

    public CamView(final Context context) {
        this(context, null);
    }

    public CamView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CamView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        camera = Cam.getInstance();
        camera.setOnStateChangeListener(this);
        setWillNotDraw(false);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode()) {
            OrientationSensor.getInstance().removeListener(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!isInEditMode()) {
            OrientationSensor.getInstance().addListener(this);
        }
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {

        final int width  = r - l;
        final int height = b - t;

        final View child = (this.preview instanceof View ) ? (View) this.preview : null;
        if (child != null) {
            int childWidth  = width  - getPaddingLeft() - getPaddingRight();
            int childHeight = height - getPaddingTop() - getPaddingBottom();


            final Cam.Size previewSize = camera.getPreviewSize();
            if (previewSize != null) {
                final int previewWidth  = previewSize.width;
                final int previewHeight = previewSize.height;

                final double scale = Math.min(childWidth / (double) previewWidth, childHeight / (double) previewHeight);
                childWidth  = (int) Math.floor(previewWidth  * scale);
                childHeight = (int) Math.floor(previewHeight * scale);
                if (sizeChangeListener != null) {
                    sizeChangeListener.onCamViewResize(childWidth, childHeight);
                }
            }

            final int childLeft;
            final int childTop;
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                childLeft = (width - childWidth) / 2;
                childTop = 0;
            } else {
                childLeft = 0;
                childTop = (height - childHeight) / 2;
            }
            child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        }
    }

    @Override
    public void onCamViewStateChange(Cam.State state) {
        invalidate();
    }

    public void setOnStateChangeListener(Cam.OnStateChangeListener onStateChangeListener) {
        this.camera.setOnStateChangeListener(onStateChangeListener);
    }

    @Nullable
    public Preview getPreview() {
        return preview;
    }


    public void setPreview(final Preview preview) {
        if (preview instanceof View) {
            removePreview();

            previewView = (View) preview;

            if (preview instanceof TextureView) {
                final TextureView surface = (TextureView) preview;
                addView(surface, 0);
            }
            this.preview = preview;

        } else {
            throw new IllegalArgumentException("Preview must be a View");
        }
    }

    public void removePreview() {
        if (preview != null) {
            if (preview instanceof View) {
                final View view = (View) preview;
                removeView(view);
            }
            preview = null;
        }
    }

    public Cam.CAMERA_FACING setCameraFacing(final Cam.CAMERA_FACING cameraFacing) {
        stopPreview(true);
        Cam.CAMERA_FACING facing = camera.setCameraFacing(cameraFacing);
        startPreview();
        return facing;
    }

    public Cam.CAMERA_FACING getCameraFacing() {
        return camera.getCameraFacing();
    }

    @Nullable
    public Cam.FLASH_MODE setFlashMode(Cam.FLASH_MODE mode) {
        return camera.setFlashMode(mode);
    }

    public Cam.FLASH_MODE getFlashMode() {
        return camera.getFlashMode();
    }

    @Nullable
    public Cam.SCENE_MODE setSceneMode(Cam.SCENE_MODE mode) {
        return camera.setSceneMode(mode);
    }

    @Nullable
    public boolean hasSceneMode(String mode) {
        return camera.hasSceneMode(mode);
    }

    /**
     * Can be call on Activity Resume
     * and start the preview.
     */
    public void onResume() {
        post(new Runnable() {
            @Override
            public void run() {
                //preview.onResume();
                post(new Runnable() {
                    @Override
                    public void run() {
                        startPreview();
                    }
                });

            }
        });
    }

    /**
     * Must be call on Activity Resume!
     * It stop the preview and release the camera.
     */
    public void onPause() {
        stopPreview(true);
    }

    /**
     * Start Preview manually
     * @see #onResume()
     */
    public synchronized void startPreview() {
        camera.startPreview();
        preview.startPreview();
    }

    /**
     * Set a size change callback.
     * @param sizeChangeListener callback object.
     */
    public void setOnSizeChangeListener(OnSizeChangeListener sizeChangeListener) {
        this.sizeChangeListener = sizeChangeListener;
    }

    /**
     * Start Preview manually
     * @see #startPreview() and #onPause()
     * @param release true if camera should be release.
     */
    public synchronized void stopPreview(boolean release) {
        preview.onStopPreview();
        camera.stopPreview(release);
    }

    /**
     * Take a Picture.
     * @param outputPath output path the will save.
     * @param callback a callback when it's done.
     */
    public void capture(String outputPath, final CamView.CaptureCallback callback) {
        camera.takePicture(callback, outputPath);
    }

    /**
     * Preview Surface callback.
     */
    public interface Preview {
        void onPause();
        void onResume();
        void startPreview();
        void onStopPreview();
    }

    /**
     * Image capture callback.
     */
    public interface CaptureCallback {
        void onImageCaptured(String outputPath);
        void onImageCaptureError(Exception exception);
    }

    /**
     * Size change callback.
     */
    public interface OnSizeChangeListener {
        void onCamViewResize(int w, int h);
    }

    private static final Paint darkPaint = new Paint();
    private static final Paint linePaint = new Paint();
    private static final Rect drawRect = new Rect();

    static {
        darkPaint.setColor(0xCC000000);
        darkPaint.setStyle(Paint.Style.FILL);
        darkPaint.setAntiAlias(true);

        linePaint.setColor(0xFFFFFFFF);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2 * ImgSdk.getAppResource().getDisplayMetrics().density);
        linePaint.setAntiAlias(true);
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        super.dispatchDraw(canvas);

        boolean cropCapture = PhotoEditorSdkConfig.isForceCropCaptureEnabled();

        final AbstractConfig.AspectConfigInterface forcedCrop;
        final boolean portrait = OrientationSensor.isScreenPortrait();
        if (portrait) {
            forcedCrop = PhotoEditorSdkConfig.getForcePortraitCrop();
        } else {
            forcedCrop = PhotoEditorSdkConfig.getForceLandscapeCrop();
        }

        if (cropCapture && forcedCrop != null) {
            final int stageWidth  = previewView.getWidth();
            final int stageHeight = previewView.getHeight();

            final float aspect = portrait ? forcedCrop.getAspect() : 1 / forcedCrop.getAspect();
            final int cropWidth  = Math.round(stageHeight * aspect);
            final int cropHeight = stageHeight;

            Rect cropRect = ImageViewUtil.getBitmapRectCenterInside(cropWidth, cropHeight, stageWidth, stageHeight);

            int left   = cropRect.left;
            int top    = cropRect.top;
            int right  = cropRect.right;
            int bottom = cropRect.bottom;

            int canvasWidth  = getWidth();
            int canvasHeight = getHeight();

            canvas.drawRect(0,          0, canvasWidth,          top, darkPaint);
            canvas.drawRect(0,          0,        left, canvasHeight, darkPaint);
            canvas.drawRect(0,     bottom, canvasWidth, canvasHeight, darkPaint);
            canvas.drawRect(right,      0, canvasWidth, canvasHeight, darkPaint);

            drawRect.set(left, bottom, right, bottom);
            canvas.drawRect(drawRect, linePaint);
        }

    }

    /**
     * Would be set by the OrientationSensor Event.
     * @param screenOrientation Orientation set by Sensor.
     */
    @Override
    public void onOrientationChange(OrientationSensor.ScreenOrientation screenOrientation) {
        invalidate();
    }
}
