package com.abcew.camera.ui.widgets;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.abcew.camera.R;

/**
 * Created by laputan on 16/11/1.
 */
public class ExpandToggleButton extends ToggleButton implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final int ROTATION_SPEED_IN_MS = 500;

    OnClickListener clickListener;
    OnCheckedChangeListener checkedChangeListener;

    public ExpandToggleButton(Context context) {
        this(context, null, 0);
    }

    public ExpandToggleButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandToggleButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setBackgroundResource(R.drawable.icon_show_filter);

        super.setOnClickListener(this);
        super.setOnCheckedChangeListener(this);

        setTextOn("");
        setTextOff("");

        if (!isChecked()) {
            setRotation(180);
        }
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        this.clickListener = listener;
    }

    @Override
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.checkedChangeListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (clickListener != null) {
            clickListener.onClick(v);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (checkedChangeListener != null) {
            checkedChangeListener.onCheckedChanged(buttonView, isChecked);
        }
        playAnimation();
    }

    @NonNull
    private AnimatorSet getToggleAnimation(boolean isChecked) {
        AnimatorSet animatorSet = new AnimatorSet();
        float currentValue = getRotation();
        float destinationValue = isChecked ? 0 : 180;

        animatorSet.play(ObjectAnimator.ofFloat(this, "rotation", currentValue, destinationValue));
        animatorSet.setDuration(ROTATION_SPEED_IN_MS);

        animatorSet.setInterpolator(new BounceInterpolator());
        return animatorSet;
    }

    private void playAnimation(){
        post(new Runnable() {
            public void run() {
                getToggleAnimation(isChecked()).start();
            }
        });
    }
}
