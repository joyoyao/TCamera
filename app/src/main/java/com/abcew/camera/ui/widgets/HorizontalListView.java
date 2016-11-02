package com.abcew.camera.ui.widgets;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;

/**
 * Created by laputan on 16/11/1.
 */
public class HorizontalListView extends android.support.v7.widget.RecyclerView{
    public HorizontalListView(Context context) {
        this(context, null);
    }

    public HorizontalListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        setLayoutManager(layoutManager);

        setItemAnimator(null);

    }

}
