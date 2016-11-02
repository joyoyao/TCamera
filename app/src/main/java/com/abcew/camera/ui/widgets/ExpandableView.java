package com.abcew.camera.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;

/**
 * Created by laputan on 16/11/1.
 */
public class ExpandableView extends RelativeLayout{
    private static final int ANIMATION_DURATION = 500;

    public ExpandableView(Context context) {
        this(context, null, 0);
    }

    public ExpandableView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setVisibility(View.GONE);

        post(new Runnable() {
            @Override
            public void run() {
                setHeight(1);
            }
        });
    }

    /**
     * Expend the view to Wrap Content.
     */
    public void expand() {
        measure(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        setHeight(1);
        setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                setHeight(interpolatedTime == 1 ? RelativeLayout.LayoutParams.WRAP_CONTENT : (int)(targetHeight * interpolatedTime));
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setInterpolator(new BounceInterpolator());
        a.setDuration(ANIMATION_DURATION);
        startAnimation(a);
    }

    /**
     * Collapse the view to zero height.
     */
    public void collapse() {
        final int initialHeight = getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    setVisibility(View.GONE);
                } else {
                    setHeight(initialHeight - (int)(initialHeight * interpolatedTime));
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setInterpolator(new BounceInterpolator());
        a.setDuration(ANIMATION_DURATION);
        startAnimation(a);
    }

    public void setHeight(int height) {

        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = height == 0 ? 1 : height;
        setLayoutParams(layoutParams);

        requestLayout(this);
    }

    private void requestLayout(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                requestLayout(group.getChildAt(i));
            }
        }
        view.requestLayout();
    }
}