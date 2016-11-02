package com.abcew.camera.ui.widgets;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.abcew.camera.R;

/**
 * Created by laputan on 16/11/1.
 */
public class ShutterButton extends Button implements View.OnClickListener{

    @NonNull
    final AnimationDrawable frameAnimation;
    View.OnClickListener listener;

    public ShutterButton(Context context) {
        this(context, null, 0);
    }

    public ShutterButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShutterButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setBackgroundResource(R.drawable.imgly_button_shutter_pressed_animation);
        frameAnimation = (AnimationDrawable) getBackground();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOnClickListener(View.OnClickListener listener) {
        super.setOnClickListener(this);
        this.listener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View v) {
        listener.onClick(v);
        playAnimation();
    }

    private void playAnimation(){
        post(new Runnable() {
            public void run() {
                if(frameAnimation.isRunning()){
                    frameAnimation.stop();
                }
                frameAnimation.start();
            }
        });
    }
}
