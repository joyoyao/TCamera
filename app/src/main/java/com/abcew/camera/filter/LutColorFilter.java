package com.abcew.camera.filter;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;
import android.support.v4.util.LruCache;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsic3DLUT;
import android.support.v8.renderscript.Type;
import android.util.Log;

import com.abcew.camera.ImgSdk;
import com.abcew.camera.ScriptC_image_alpha;
import com.abcew.camera.ScriptC_image_translate_3d;
import com.abcew.camera.utils.BitmapFactoryUtils;
import com.abcew.camera.utils.ThreadUtils;

import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glTexParameterf;
import static android.opengl.GLES20.glTexParameteri;

/**
 * Created by laputan on 16/12/21.
 */

public class LutColorFilter extends ImageFilter implements ImageFilter.FilterConfigIntensity {

    private static final float MAX_MEMORY_PERCENTAGE = 0.15f;

    private static RenderScript rs = ImgSdk.getAppRsContext();

    private static class Scripts {
        @Nullable
        private ScriptIntrinsic3DLUT lutScript;



        @Nullable
        private ScriptC_image_alpha image_alphacript;

        private int getSize() {
            return lutScript == null ? 0 : 512 * 512 * 4;
        }
    }

    private static LruCache<LutColorFilter, Scripts> scriptsLruCache;
    private static final int cacheSize;
    static {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        cacheSize = (int) (maxMemory * MAX_MEMORY_PERCENTAGE);

        scriptsLruCache = new LruCache<LutColorFilter, Scripts>(cacheSize) {
            @Override
            protected int sizeOf(LutColorFilter key, @NonNull Scripts scripts) {
                return scripts.getSize();
            }
        };
    }

    private static final int RED_DIM   = 64;
    private static final int GREEN_DIM = 64;
    private static final int BLUE_DIM  = 64;

    private final int[] textures = new int[1];

    @Nullable
    private Bitmap lutBitmap;
    private final int lutResourceId;

    @NonNull
    private final Paint intensityPaint;

    private final static String FILTER_UNIFORM_SAMPLER = "lutTexture";

    @NonNull
    private final Resources resources;

    private boolean lutBitmapInOpenGlUse = false;

    private final static String FRAGMENT_SHADER_DUMMY =
            "precision highp float;\n" +
                    " varying highp vec2 vTextureCoord;\n" +
                    " uniform " + TARGET_PLACEHOLDER + " sTexture;\n" +
                    " uniform sampler2D lutTexture; // lookup texture\n" +
                    " \n" +
                    " void main()\n" +
                    " {\n" +
                    "     highp vec4 textureColor = texture2D(sTexture, vTextureCoord);\n" +
                    "     textureColor = clamp(textureColor, 0.0, 1.0);\n" +

                    "     highp float blueColor = textureColor.b * 63.0;\n" +

                    "     highp vec2 quad1;\n" +
                    "     quad1.y = floor(floor(blueColor) / 8.0);\n" +
                    "     quad1.x = floor(blueColor) - (quad1.y * 8.0);\n" +

                    "     highp vec2 quad2;\n" +
                    "     quad2.y = floor(ceil(blueColor) / 8.0);\n" +
                    "     quad2.x = ceil(blueColor) - (quad2.y * 8.0);\n" +

                    "     highp vec2 texPos1;\n" +
                    "     texPos1.x = clamp((quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r), 0.0, 1.0);\n" +
                    "     texPos1.y = clamp((quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g), 0.0, 1.0);\n" +

                    "     highp vec2 texPos2;\n" +
                    "     texPos2.x = clamp((quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r), 0.0, 1.0);\n" +
                    "     texPos2.y = clamp((quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g), 0.0, 1.0);\n" +

                    "     highp vec4 newColor1 = texture2D(lutTexture, texPos1);\n" +
                    "     highp vec4 newColor2 = texture2D(lutTexture, texPos2);\n" +

                    "     gl_FragColor = mix(newColor1, newColor2, fract(blueColor));\n" +
                    " }";


    public LutColorFilter(@StringRes int name, @DrawableRes int thumbnailRes, @DrawableRes @RawRes int lutResource) {
        super(name, thumbnailRes, DEFAULT_VERTEX_SHADER, FRAGMENT_SHADER_DUMMY);

        intensityPaint = new Paint();
        intensityPaint.setAntiAlias(false);
        intensityPaint.setFilterBitmap(false);

        resources = ImgSdk.getAppResource();
        lutResourceId = lutResource;
    }



    @Nullable
    @Override
    public Bitmap getThumbnailBitmap(int maxWidth) {
        return renderImage(super.getThumbnailBitmap(maxWidth));
    }

    @Override
    public void onDraw() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glUniform1i(getHandle(FILTER_UNIFORM_SAMPLER), 3);
    }


    private Scripts getScript() {
        Scripts scripts = scriptsLruCache.get(this);
        if (scripts == null) {
            scripts = new Scripts();
            scriptsLruCache.put(this, scripts);
        }

        return scripts;
    }

    @NonNull
    private ScriptIntrinsic3DLUT getLutRenderScript() {
        Scripts scripts = getScript();
        ScriptIntrinsic3DLUT script = scripts.lutScript == null ? null : scripts.lutScript;
        if (script == null ) {
            script = ScriptIntrinsic3DLUT.create(rs, Element.RGBA_8888(rs));
            script.setLUT(getLutCube(rs));
            scripts.lutScript = script;
            scriptsLruCache.trimToSize(cacheSize);
        }

        return script;
    }

//    @NonNull
//    private ScriptC_alpha2 getAlphaRenderScript(float intensity) {
//        Scripts scripts = getScript();
//        ScriptC_alpha2 alphaScript = scripts.alphaScript == null ? null : scripts.alphaScript;//.get();
//
//        if (alphaScript == null ) {
//            alphaScript = new ScriptC_alpha2(rs);
//            scripts.alphaScript = alphaScript;
//            scriptsLruCache.trimToSize(cacheSize);
//        }
//
//        alphaScript.set_alpha((short) ((255 * intensity)));
//
//
//
//        ScriptC_image_alpha image_alphaScript = scripts.alphaScript == null ? null : scripts.image_alphacript;//.get();
//        if (image_alphaScript == null ) {
//            image_alphaScript = new ScriptC_image_alpha(rs);
//            scripts.image_alphacript = image_alphaScript;
//            scriptsLruCache.trimToSize(cacheSize);
//        }
//        image_alphaScript.set_alpha((short) ((255 * intensity)));
//
//        return alphaScript;
//    }

    @NonNull
    private ScriptC_image_alpha getImageAlphaRenderScript(float intensity) {
        Scripts scripts = getScript();
//        ScriptC_alpha2 alphaScript = scripts.alphaScript == null ? null : scripts.alphaScript;//.get();
//
//        if (alphaScript == null ) {
//            alphaScript = new ScriptC_alpha2(rs);
//            scripts.alphaScript = alphaScript;
//            scriptsLruCache.trimToSize(cacheSize);
//        }
//
//        alphaScript.set_alpha((short) ((255 * intensity)));



        ScriptC_image_alpha image_alphaScript = scripts.image_alphacript == null ? null : scripts.image_alphacript;//.get();
        if (image_alphaScript == null ) {
            image_alphaScript = new ScriptC_image_alpha(rs);
            scripts.image_alphacript = image_alphaScript;
            scriptsLruCache.trimToSize(cacheSize);
        }
        image_alphaScript.set_alpha((short) ((255 * intensity)));

        return image_alphaScript;
    }

    /*private static LutColorFilter lastFilter;
    private static Bitmap lastRender;

    @Nullable
    private static Bitmap getLastRender(LutColorFilter filter){
        if (filter.equals(lastFilter)) {
            return null;//lastRender;
        } else {
            return null;
        }
    }

    private static void setLastRender(LutColorFilter filter, Bitmap bitmap) {
        lastFilter = filter;
        lastRender = bitmap;
    }

    private static Bitmap resultRender;
    private static Bitmap getOutputBitmap(Bitmap source) {
        if (resultRender == null || (resultRender.getWidth() != source.getWidth() || resultRender.getHeight() != source.getHeight())) {
            resultRender = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        }
        return resultRender;
    }*/

    @Nullable
    public Bitmap renderImage(@Nullable Bitmap bitmap, float intensity) {

        if (bitmap == null) {
            return null;
        }

        Bitmap outputBitmap = null;//getLastRender(this);
        if (outputBitmap == null) {

            rs = ImgSdk.getAppRsContext();

            ScriptIntrinsic3DLUT scriptLut = getLutRenderScript();

            outputBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());

            Allocation mAllocIn  = Allocation.createFromBitmap(rs, bitmap);
            Allocation mAllocOut = Allocation.createFromBitmap(rs, outputBitmap);

            scriptLut.forEach(mAllocIn, mAllocOut);
            mAllocOut.copyTo(outputBitmap);
            //setLastRender(this, outputBitmap);
        }

        if (intensity != 1) {
            outputBitmap = renderIntensity(bitmap, outputBitmap, intensity);
        }

        return outputBitmap;
    }

    @Nullable
    @Override
    public Bitmap renderImage(Bitmap bitmap) {
        return renderImage(bitmap, 1f);
    }

    @Override
    public void release() {
        super.release();

        if (!lutBitmapInOpenGlUse) {
            glBindTexture(GL_TEXTURE_2D, 0);
            glDeleteTextures(textures.length, textures, 0);
        }

        if (lutBitmap != null) {
            lutBitmapInOpenGlUse = false;
            lutBitmap.recycle();
            lutBitmap = null;
            /*if (this.equals(lastFilter)) {
                setLastRender(null, null);
            }*/
            scriptsLruCache.remove(this);
        }

    }

    /**
     * Return the Lut as Bitmap. Look at the imgly_lut_identity.png drawable to get a basic non changing LUT.
     * @return a lut to change Image Colors.
     */
    @Nullable
    public synchronized Bitmap getLutBitmap() {
        return BitmapFactoryUtils.decodeResource(resources, lutResourceId);
    }

    @Override
    public boolean hasStaticThumbnail() {
        return false;
    }

    /**
     * Return the Renderscript LUT Allocation
     * @param rs reference to the renderscript.
     * @return the Lut Allocation
     */
    @NonNull
    public Allocation getLutCube(@NonNull RenderScript rs){
        return getLutCube(rs, false);
    }

    @NonNull
    private Allocation getLutCube(@NonNull RenderScript rs, boolean forceRender) {

        Bitmap lutBitmap = getLutBitmap();

        if (!ThreadUtils.thisIsUiThread()) {
            //Memory Allocation UpKick.
            Thread thread = Thread.currentThread();
            while ((lutBitmap == null || lutBitmap.getWidth() < RED_DIM * 8)) {
                if(thread.isInterrupted()){
                    return null;
                }

                if (lutBitmap != null) {
                    lutBitmap.recycle();
                }
                lutBitmap = getLutBitmap();
                try {
                    Thread.sleep((int)(1000 * Math.random() + 100));
                } catch (InterruptedException ignored) {}
            }
        }

        Bitmap cache = Bitmap.createBitmap(512,512, Bitmap.Config.ARGB_8888);

        Allocation mAllocIn  = Allocation.createFromBitmap(rs, lutBitmap);
        Allocation mAllocOut = Allocation.createFromBitmap(rs, cache);

        final Type.Builder tb = new Type.Builder(rs, Element.U8_4(rs));
        tb.setX(RED_DIM);
        tb.setY(GREEN_DIM);
        tb.setZ(BLUE_DIM);
        Allocation mAllocCube = Allocation.createTyped(rs, tb.create());

        ScriptC_image_translate_3d script = new ScriptC_image_translate_3d(rs);
        script.set_gIn(mAllocIn);
        script.set_gOut(mAllocOut);
        script.forEach_root(mAllocIn, mAllocOut);
        byte[] lut    = new byte[512 * 512 * 4];
        mAllocOut.copyTo(lut);
        mAllocCube.copyFromUnchecked(lut);
        return mAllocCube;
    }

    @Override
    protected synchronized void setup(final int texTarget) {
        super.setup(texTarget);

        glGenTextures(1, textures, 0);

        glBindTexture(GL_TEXTURE_2D, textures[0]);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        lutBitmapInOpenGlUse = true;
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, getLutBitmap(), 0);
    }

    @NonNull
    protected Bitmap renderIntensity(@NonNull Bitmap source, @NonNull Bitmap blend, float intensity) {

        if (intensity != 1) {
            //ScriptIntrinsicBlend blendScript = getBlendRenderScript();

            //Bitmap output = getOutputBitmap(source);

            Allocation allocIn    = Allocation.createFromBitmap(rs, source);
            //Allocation allocOut   = Allocation.createFromBitmap(rs, output);
            Allocation allocBlend = Allocation.createFromBitmap(rs, blend);

//            ScriptC_alpha2 alphaBlendScript = getAlphaRenderScript(intensity);
//
//            alphaBlendScript.set_rsAllocationIn(allocIn);
//            alphaBlendScript.set_rsAllocationOut(allocBlend);
//
//            alphaBlendScript.forEach_setImageAlpha(allocBlend);
//
//            allocBlend.copyTo(blend);

            ScriptC_image_alpha scriptC_image_alpha = getImageAlphaRenderScript(intensity);

            scriptC_image_alpha.set_gIn(allocIn);
            scriptC_image_alpha.set_gOut(allocBlend);
            scriptC_image_alpha.forEach_setImageAlpha(allocIn,allocBlend);
            allocBlend.copyTo(blend);

            Log.i("tag","ScriptC_image_alpha");
//            scriptC_image_alpha.forEach_setImageAlpha();
//            scriptC_image_alpha.forEach_setImageAlpha(allocBlend);
            //allocOut.copyTo(output);
            return blend;
        } else {
            return source;
        }
    }
}
