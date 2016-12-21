package com.abcew.camera.filter;

import android.content.res.Resources;
import android.graphics.Bitmap;
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
import android.support.v8.renderscript.Type;

import com.abcew.camera.ImgLySdk;
import com.abcew.camera.script.ScriptC_render_3d_lut;
import com.abcew.camera.utils.BitmapFactoryUtils;
import com.abcew.camera.utils.ThreadUtils;

/**
 * Created by laputan on 16/12/21.
 */

public class LutColorFilter extends ImageFilter implements ImageFilter.FilterConfigIntensity {
    private static final float MAX_MEMORY_PERCENTAGE = 0.15F;
    private RenderScript rs = ImgLySdk.getAppRsContext();
    private static LruCache<LutColorFilter, Scripts> scriptsLruCache;
    private static int cacheSize;
    private static final int RED_DIM = 64;
    private static final int GREEN_DIM = 64;
    private static final int BLUE_DIM = 64;
    private final int[] textures = new int[1];
    @Nullable
    private Bitmap lutBitmap;
    private final int lutResourceId;
    private static final String FILTER_UNIFORM_SAMPLER = "lutTexture";
    @NonNull
    private final Resources resources = ImgLySdk.getAppResource();
    private boolean lutBitmapInOpenGlUse = false;
    private static final String FRAGMENT_SHADER_DUMMY = "precision highp float;\n varying highp vec2 vTextureCoord;\n uniform #*SAMPLER_TYPE*# sTexture;\n uniform sampler2D lutTexture; // lookup texture\n \n void main()\n {\n     highp vec4 textureColor = texture2D(sTexture, vTextureCoord);\n     textureColor = clamp(textureColor, 0.0, 1.0);\n     highp float blueColor = textureColor.b * 63.0;\n     highp vec2 quad1;\n     quad1.y = floor(floor(blueColor) / 8.0);\n     quad1.x = floor(blueColor) - (quad1.y * 8.0);\n     highp vec2 quad2;\n     quad2.y = floor(ceil(blueColor) / 8.0);\n     quad2.x = ceil(blueColor) - (quad2.y * 8.0);\n     highp vec2 texPos1;\n     texPos1.x = clamp((quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r), 0.0, 1.0);\n     texPos1.y = clamp((quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g), 0.0, 1.0);\n     highp vec2 texPos2;\n     texPos2.x = clamp((quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r), 0.0, 1.0);\n     texPos2.y = clamp((quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g), 0.0, 1.0);\n     highp vec4 newColor1 = texture2D(lutTexture, texPos1);\n     highp vec4 newColor2 = texture2D(lutTexture, texPos2);\n     gl_FragColor = mix(newColor1, newColor2, fract(texPos2.x));\n }";
    private static Allocation allocationOut;

    public LutColorFilter(@StringRes int name, @DrawableRes int thumbnailRes, @DrawableRes @RawRes int lutResource) {
        super(name, thumbnailRes, "uniform mat4 uMVPMatrix;\nuniform mat4 uSTMatrix;\nuniform float uCRatio;\nattribute vec4 aPosition;\nattribute vec4 aTextureCoord;\nvarying lowp vec2 vTextureCoord;\nvoid main() {\nvec4 scaledPos = aPosition;\nscaledPos.x = scaledPos.x * uCRatio;\ngl_Position = uMVPMatrix * scaledPos;\nvTextureCoord = (uSTMatrix * aTextureCoord).xy;\n}\n", "precision highp float;\n varying highp vec2 vTextureCoord;\n uniform #*SAMPLER_TYPE*# sTexture;\n uniform sampler2D lutTexture; // lookup texture\n \n void main()\n {\n     highp vec4 textureColor = texture2D(sTexture, vTextureCoord);\n     textureColor = clamp(textureColor, 0.0, 1.0);\n     highp float blueColor = textureColor.b * 63.0;\n     highp vec2 quad1;\n     quad1.y = floor(floor(blueColor) / 8.0);\n     quad1.x = floor(blueColor) - (quad1.y * 8.0);\n     highp vec2 quad2;\n     quad2.y = floor(ceil(blueColor) / 8.0);\n     quad2.x = ceil(blueColor) - (quad2.y * 8.0);\n     highp vec2 texPos1;\n     texPos1.x = clamp((quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r), 0.0, 1.0);\n     texPos1.y = clamp((quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g), 0.0, 1.0);\n     highp vec2 texPos2;\n     texPos2.x = clamp((quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r), 0.0, 1.0);\n     texPos2.y = clamp((quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g), 0.0, 1.0);\n     highp vec4 newColor1 = texture2D(lutTexture, texPos1);\n     highp vec4 newColor2 = texture2D(lutTexture, texPos2);\n     gl_FragColor = mix(newColor1, newColor2, fract(texPos2.x));\n }");
        this.lutResourceId = lutResource;
    }

    @Nullable
    public Bitmap getThumbnailBitmap(int maxWidth) {
        return renderImage(super.getThumbnailBitmap(maxWidth), true);
    }

    public void onDraw() {
        GLES20.glActiveTexture('蓃');
        GLES20.glBindTexture(3553, this.textures[0]);
        GLES20.glUniform1i(this.getHandle("lutTexture"), 3);
    }

    private synchronized LutColorFilter.Scripts getScript() {
        LutColorFilter.Scripts scripts = (LutColorFilter.Scripts) scriptsLruCache.get(this);
        if (scripts == null) {
            scripts = new LutColorFilter.Scripts();
            scriptsLruCache.put(this, scripts);
        }

        return scripts;
    }

    @NonNull
    private ScriptC_render_3d_lut getLutRenderScript() {
        LutColorFilter.Scripts scripts = this.getScript();
        ScriptC_render_3d_lut script = scripts.lutScript == null ? null : scripts.lutScript;
        if (script == null) {
            Runtime runtime = Runtime.getRuntime();
            cacheSize = (int) (runtime.maxMemory() - (runtime.totalMemory() - (long) scriptsLruCache.size())) / 2;
            scriptsLruCache.trimToSize(cacheSize);
            script = new ScriptC_render_3d_lut(rs);
            script.set_rsAllocationLut(this.getLutCube(this.rs));
            scripts.lutScript = script;
        }

        return script;
    }

    @Nullable
    public Bitmap renderImage(@Nullable Bitmap bitmap, float intensity, boolean previewMode) {
        if (bitmap == null) {
            return null;
        } else {
            Bitmap outputBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
            Allocation allocationIn = Allocation.createFromBitmap(this.rs, bitmap);
            Allocation allocationOut = Allocation.createFromBitmap(this.rs, outputBitmap);
            this.renderImage(allocationIn, allocationOut, intensity, previewMode);
            allocationOut.copyTo(outputBitmap);
            return outputBitmap;
        }
    }

    @Nullable
    public Allocation renderImage(Allocation allocationIn, float intensity, boolean previewMode) {
        if (allocationIn == null) {
            return null;
        } else {
            if (allocationOut == null || !allocationOut.getType().equals(allocationIn.getType())) {
                allocationOut = Allocation.createTyped(this.rs, allocationIn.getType());
            }

            this.renderImage(allocationIn, allocationOut, intensity, previewMode);
            return allocationOut;
        }
    }

    public void renderImage(Allocation allocationIn, Allocation allocationOut, float intensity, boolean previewMode) {
        ScriptC_render_3d_lut scriptLut = this.getLutRenderScript();
        scriptLut.set_rsAllocationIn(allocationIn);
        scriptLut.set_alpha((short) ((int) (255.0F * intensity)));
        if (previewMode) {
            scriptLut.forEach_approximated(allocationOut);
        } else {
            scriptLut.forEach_root(allocationOut);
        }

    }

    public void release() {
        super.release();
        if (!this.lutBitmapInOpenGlUse) {
            GLES20.glBindTexture(3553, 0);
            GLES20.glDeleteTextures(this.textures.length, this.textures, 0);
        }

        if (this.lutBitmap != null) {
            this.lutBitmapInOpenGlUse = false;
            this.lutBitmap.recycle();
            this.lutBitmap = null;
            scriptsLruCache.remove(this);
        }

    }

    @Nullable
    public synchronized Bitmap getLutBitmap() {
        return BitmapFactoryUtils.decodeResource(this.resources, this.lutResourceId);
    }

    public boolean hasResourceLut() {
        return this.lutResourceId >= 0;
    }

    public boolean hasStaticThumbnail() {
        return false;
    }

    @NonNull
    public Allocation getLutCube(@NonNull RenderScript rs) {
        if (this.hasResourceLut()) {
            return Allocation.createFromBitmapResource(rs, this.resources, this.lutResourceId);
        } else {
            Bitmap lutBitmap = this.getLutBitmap();
            if (!ThreadUtils.thisIsUiThread()) {
                Thread allocation = Thread.currentThread();

                while (lutBitmap == null || lutBitmap.getWidth() < 512) {
                    if (allocation.isInterrupted()) {
                        return null;
                    }

                    if (lutBitmap != null) {
                        lutBitmap.recycle();
                    }

                    lutBitmap = this.getLutBitmap();

                    try {
                        Thread.sleep((long) ((int) (1000.0D * Math.random() + 100.0D)));
                    } catch (InterruptedException var5) {
                        ;
                    }
                }
            }

            Allocation allocation1 = Allocation.createTyped(rs, Type.createXY(rs, Element.RGBA_8888(rs), 512, 512), 2);
            allocation1.copyFrom(lutBitmap);
            lutBitmap.recycle();
            return allocation1;
        }
    }

    protected synchronized void setup(int texTarget) {
        super.setup(texTarget);
        GLES20.glGenTextures(1, this.textures, 0);
        GLES20.glBindTexture(3553, this.textures[0]);
        GLES20.glTexParameterf(3553, 10240, 9729.0F);
        GLES20.glTexParameterf(3553, 10241, 9729.0F);
        GLES20.glTexParameteri(3553, 10242, '脯');
        GLES20.glTexParameteri(3553, 10243, '脯');
        this.lutBitmapInOpenGlUse = true;
        GLUtils.texImage2D(3553, 0, this.getLutBitmap(), 0);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            LutColorFilter that = (LutColorFilter) o;
            return this.lutResourceId == that.lutResourceId;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.lutResourceId;
    }



    static {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 40L);
        cacheSize = (int) ((float) maxMemory * 0.15F);
        scriptsLruCache = new LruCache(cacheSize) {
            protected int sizeOf(LutColorFilter key, @NonNull LutColorFilter.Scripts scripts) {
                return scripts.getSize();
            }
        };

    }

    private static class Scripts {
        @Nullable
        private ScriptC_render_3d_lut lutScript;

        private Scripts() {
        }

        private int getSize() {
            return this.lutScript == null ? 0 : 1048576;
        }
    }
}
