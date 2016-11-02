package com.abcew.camera.ui.acs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by laputan on 16/10/31.
 */
public class FocusRectView extends View {
    private static final int AREA_SIZE = 2000;
    private static final int RECT_SIZE_IN_DPI = 50;
    private static final int DEFAULT_AREA_WEIGHT = 1000;

    private final Cam camera;

    private boolean haveTouch = false;
    private Rect touchArea;
    @NonNull
    private final Paint paint;

    public FocusRectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE);

        camera = Cam.getInstance();
    }

    /**
     * Show touch
     * @param rect area of touch
     */
    private void setHaveTouch(Rect rect) {
        haveTouch = true;
        touchArea = rect;
        invalidate();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                haveTouch = false;
                invalidate();
            }
        }, 1000);

    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        boolean success = super.onTouchEvent(event);
        focusOnEvent(event, getMeasuredWidth(), getMeasuredHeight());

        return success;
    }

    private synchronized void focusOnEvent(@NonNull MotionEvent event, int width, int height) {
        int rectSize = Math.round(RECT_SIZE_IN_DPI * getResources().getDisplayMetrics().density);

        List<Camera.Area> focusAreas = new ArrayList<>();
        Rect touchRect = new Rect(
                (int) (event.getX() - rectSize / 2),
                (int) (event.getY() - rectSize / 2),
                (int) (event.getX() + rectSize / 2),
                (int) (event.getY() + rectSize / 2));

        final Rect targetFocusRect = new Rect(
                touchRect.left   * AREA_SIZE / width  - DEFAULT_AREA_WEIGHT,
                touchRect.top    * AREA_SIZE / height - DEFAULT_AREA_WEIGHT,
                touchRect.right  * AREA_SIZE / width  - DEFAULT_AREA_WEIGHT,
                touchRect.bottom * AREA_SIZE / height - DEFAULT_AREA_WEIGHT);

        focusAreas.add(new Camera.Area(targetFocusRect, DEFAULT_AREA_WEIGHT));

        setHaveTouch(touchRect);

        if (camera != null) {
            camera.setFocus(focusAreas);
        }
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        if (haveTouch) {
            canvas.drawRect(
                    touchArea.left, touchArea.top, touchArea.right, touchArea.bottom,
                    paint);
        }
    }
}
