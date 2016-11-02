package com.abcew.camera.ui.utilities;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.support.annotation.NonNull;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by laputan on 16/10/31.
 */
public class SetHardwareAnimatedViews implements AnimatorSet.AnimatorListener {

    @NonNull
    private final HashMap<View, Integer> viewsHash;

    /**
     * Set A View to Hardware Layer, for Hardware Acceleration during the Animation and revert at the end of animation to the previously layer type.
     * @param mainView The first hardware animated View
     * @param views some other additional animated views
     */
    public SetHardwareAnimatedViews(@NonNull View mainView, @NonNull View... views) {
        viewsHash = new HashMap<>();

        viewsHash.put(mainView, mainView.getLayerType());
        for (View view : views) {
            viewsHash.put(view, view.getLayerType());
        }

    }

    @Override public final void onAnimationStart(Animator animator) {
        for (Map.Entry<View, Integer> entry : viewsHash.entrySet()) {
            View view = entry.getKey();
            entry.setValue(view.getLayerType());
            view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
    }

    @Override public final void onAnimationEnd(Animator animator) {
        for (Map.Entry<View, Integer> entry : viewsHash.entrySet()) {
            View view = entry.getKey();
            view.setLayerType(entry.getValue(), null);
        }
    }

    @Override public final void onAnimationCancel(Animator animator) {}
    @Override public final void onAnimationRepeat(Animator animator) {}
}
