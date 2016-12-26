package com.abcew.camera.filter;

import com.abcew.camera.R;

/**
 * Created by laputan on 16/12/21.
 */

public class ColorFilterClassic extends LutColorFilter  {

    public ColorFilterClassic() {
        super(R.string.color_filter_name_classic, R.drawable.filter_preview_photo, R.drawable.lut_classic);
    }
}
