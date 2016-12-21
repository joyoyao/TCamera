package com.abcew.camera.filter;

import com.abcew.camera.R;

/**
 * Created by laputan on 16/12/21.
 */

public class ColorFilterNoGreen extends LutColorFilter {


    public ColorFilterNoGreen() {
        super(R.string.imgly_color_filter_name_nogreen, R.drawable.imgly_filter_preview_photo, R.drawable.imgly_lut_nogreen);
    }
}
