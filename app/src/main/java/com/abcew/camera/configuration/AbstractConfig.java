package com.abcew.camera.configuration;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;
import android.support.annotation.WorkerThread;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.abcew.camera.ImgSdk;
import com.abcew.camera.R;
import com.abcew.camera.gles.Texture;
import com.abcew.camera.ui.adapter.DataSourceListAdapter;
import com.abcew.camera.utils.BitmapFactoryUtils;

/**
 * Created by laputan on 16/10/31.
 */
public class AbstractConfig implements DataSourceInterface<AbstractConfig.BindData> {

    protected static final @DrawableRes
    int NO_THUMBNAIL_ID = -2;
    protected static final int ORIGINAL_THUMBNAIL_SIZE = -1;

    private final String name;
    private final boolean drawableIsSvg;
    private final @DrawableRes int thumbnailResId;

    protected boolean isDirty;

    public static class BindData {
        final AbstractConfig data;
        final Bitmap drawable;

        public BindData(AbstractConfig data, Bitmap drawable) {
            this.data = data;
            this.drawable = drawable;
        }
    }

    @Override
    public boolean isDirty() {
        return isDirty;
    }

    @Override
    public void setDirtyFlag(boolean isDirty) {
        this.isDirty = isDirty;
    }

    protected AbstractConfig(@StringRes int name, @DrawableRes @RawRes int thumbnailRes) {
        this(ImgSdk.getAppResource().getString(name), thumbnailRes);
    }

    protected AbstractConfig(@StringRes int name) {
        this(ImgSdk.getAppResource().getString(name), NO_THUMBNAIL_ID);
    }

    protected AbstractConfig(String name) {
        this(name, NO_THUMBNAIL_ID);
    }

    protected AbstractConfig(String name, @DrawableRes @RawRes int thumbnailResId) {
        this.name = name;
        this.thumbnailResId = thumbnailResId;
        this.drawableIsSvg = BitmapFactoryUtils.checkIsSvgResource(thumbnailResId);
    }

    /**
     * Get the Name
     * @return localized name
     * {@inheritDoc}
     */
    public String getName() {
        return name;
    }

    @Override
    public int getLayout() {
        return 0;
    }

    public String getTitle() {
        return getName();
    }

    public boolean isClickable() {
        return true;
    }

    @Override
    public int getVerticalLayout() {
        return 0;
    }

    @Nullable
    @Override
    public BindData generateBindData() {
        return new BindData(this, null);
    }

    @Nullable
    @Override
    public BindData generateBindDataAsync() {
        return new BindData(this, !hasStaticThumbnail() ? getThumbnailBitmap() : null);
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @NonNull
    @Override
    public DataSourceListAdapter.DataSourceViewHolder<BindData> createViewHolder(@NonNull View view, boolean useVerticalLayout) {
        return new ConfigViewHolder(view);
    }

    protected static class ConfigViewHolder extends DataSourceListAdapter.DataSourceViewHolder<BindData> implements View.OnClickListener {

        public final View contentHolder;
        @NonNull
        public final TextView textView;
        @NonNull
        public final ImageView imageView;

        //private static final int ICON_SIZE = Math.round(48 * ImgSdk.getAppResource().getDisplayMetrics().density);

        public ConfigViewHolder(@NonNull View v) {
            super(v);
            contentHolder = v.findViewById(R.id.contentHolder);
            imageView = (ImageView) v.findViewById(R.id.image);
            textView = (TextView) v.findViewById(R.id.label);

            contentHolder.setOnClickListener(this);
        }

        @Override
        public void setSelectedState(boolean selected) {
            contentHolder.setSelected(selected);
        }

        public void onClick(View v) {
            dispatchSelection();
            dispatchOnItemClick();
        }

        private volatile boolean hasImage = false;

        @Override
        protected void bind(@NonNull BindData bindData) {

            textView.setText(bindData.data.getName());

            if (bindData.data.hasStaticThumbnail()) {
                hasImage = true;
                imageView.setImageResource(bindData.data.getThumbnailResId());
                imageView.setAlpha(1f);
            } else if (bindData.drawable != null) {
                hasImage = true;
                imageView.setImageBitmap(bindData.drawable);
                imageView.setAlpha(1f);
            } else {
                hasImage = false;
                imageView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!hasImage) {
                            imageView.setAlpha(0f);
                        }
                    }
                }, 100);
            }
        }

    }

    /**
     * Get the static thumbnail drawable resource id if exist.
     * @see #hasStaticThumbnail()
     * @return Drawable resource
     * {@inheritDoc}
     */
    public @DrawableRes int getThumbnailResId() {
        return thumbnailResId;
    }

    /**
     * Check if it has a static thumbnail drawable.
     * @see #getThumbnailBitmap()
     * @return true has static Thumbnail
     */
    public boolean hasStaticThumbnail(){
        return !this.drawableIsSvg;
    }

    /**
     * Return Thumbnail in full size.
     * @see #getThumbnailBitmap(int maxWidth)
     * @return thumbnail drawable.
     * {@inheritDoc}
     */
    @Nullable
    public Bitmap getThumbnailBitmap() {
        return getThumbnailBitmap(ORIGINAL_THUMBNAIL_SIZE);
    }

    /*  Check if it is a Svg Thumbnail
    public Boolean isDrawableSvg() {
        return drawableIsSvg;
    }*/

    /**
     * Return the Thumbnail, do not call in Main Thread and use #getThumbnailResId() if #hasStaticThumbnail
     * @param maxWidth Size the drawable should be limited to.
     * @return thumbnail drawable.
     */
    @Nullable
    @WorkerThread
    public Bitmap getThumbnailBitmap(int maxWidth) {
        return createThumbnailBitmap(maxWidth);
    }

    @Nullable
    @WorkerThread
    private Bitmap createThumbnailBitmap(int minSize) {

        if (thumbnailResId == NO_THUMBNAIL_ID){
            return null;
        }

        Resources resources = ImgSdk.getAppResource();
        return BitmapFactoryUtils.decodeResource(resources, thumbnailResId, minSize);

    }


    public interface AspectConfigInterface extends DataSourceInterface<AbstractConfig.BindData> {
        float getAspect();
        boolean hasSpecificSize();

        int getCropWidth();

        int getCropHeight();
    }

    public interface FontConfigInterface<T> extends DataSourceInterface<T> {
        @Nullable
        Typeface getTypeface();
    }

    public interface ImageFilterInterface extends DataSourceInterface<AbstractConfig.BindData> {

        /**
         * Apply the renderscript filter without intensity changes.
         * @param bitmap the source image.
         * @return the filter result.
         */
        @Nullable
        Bitmap renderImage(Bitmap bitmap);

        /**
         * Apply the renderscript filter with a specific intensity.
         * @param bitmap the source image.
         * @param intensity the filter intensity
         * @return the filter result.
         */
        @Nullable
        Bitmap renderImage(Bitmap bitmap, final float intensity);

        /**
         * Release the shader program and texture
         */
        void release();

        /**
         * Draw texture in OpenGl Context
         * @param texture the camera preview texture
         * @param mvpMatrix the camera source matrix
         * @param stMatrix the stage destination matrix
         * @param aspectRatio the camera image aspect
         */
        void draw(Texture texture, final float[] mvpMatrix, final float[] stMatrix, final float aspectRatio);

        /**
         * Check if the Filter has Intensity Control
         * @return true if it has intensity
         */
        boolean hasIntensityConfig();
    }




    public interface StickerConfigInterface extends DataSourceInterface<AbstractConfig.BindData> {
        /**
         * Type of sticker.
         */
        enum STICKER_TYPE {
            IMAGE,
            TEXT
        }

        /**
         * Return the type of the sticker
         * @return sticker typ
         * @see STICKER_TYPE
         */
        @Nullable
        STICKER_TYPE getType();

        /*
         * Check if the Sticker is a SVG sticker
         * @return true if it is a SVG sticker
         */
        //boolean isSvg();


        /**
         * Get sticker drawable resource id.
         * @return
         */
        @DrawableRes @RawRes int getStickerId();
    }

    public interface ColorConfigInterface extends DataSourceInterface<AbstractConfig.BindData> {

        int getColor();

    }
}
