package com.abcew.camera.filter;

import com.abcew.camera.R;

/**
 * Created by laputan on 16/12/21.
 */

public class ColorFilterFront extends LutColorFilter {

    public ColorFilterFront() {
        super(R.string.color_filter_name_front, R.drawable.filter_preview_photo, R.drawable.lut_front);
    }
}
