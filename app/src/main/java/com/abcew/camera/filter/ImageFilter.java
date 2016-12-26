package com.abcew.camera.filter;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.abcew.camera.ImgSdk;
import com.abcew.camera.R;
import com.abcew.camera.configuration.AbstractConfig;
import com.abcew.camera.gles.OpenGLES;
import com.abcew.camera.gles.Texture;
import com.abcew.camera.operator.ImageLoadOperation;
import com.abcew.camera.utils.BitmapFactoryUtils;

import java.util.HashMap;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteBuffers;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by laputan on 16/11/1.
 */
public abstract class ImageFilter extends AbstractConfig implements AbstractConfig.ImageFilterInterface {
    public static final String DEFAULT_ATTRIB_POSITION = "aPosition";
    public static final String DEFAULT_ATTRIB_TEXTURE_COORDINATE = "aTextureCoord";
    public static final String DEFAULT_UNIFORM_SAMPLER = "sTexture";


    private final OpenGLES openGLES = OpenGLES.getInstance();

    private int textureTarget =  -1;

    protected static final String DEFAULT_VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "uniform float uCRatio;\n" +

                    "attribute vec4 "+DEFAULT_ATTRIB_POSITION+";\n" +
                    "attribute vec4 "+DEFAULT_ATTRIB_TEXTURE_COORDINATE+";\n" +
                    "varying lowp vec2 vTextureCoord;\n" +

                    "void main() {\n" +
                    "vec4 scaledPos = "+DEFAULT_ATTRIB_POSITION+";\n" +
                    "scaledPos.x = scaledPos.x * uCRatio;\n" +
                    "gl_Position = uMVPMatrix * scaledPos;\n" +
                    "vTextureCoord = (uSTMatrix * "+DEFAULT_ATTRIB_TEXTURE_COORDINATE+").xy;\n" +
                    "}\n";


    protected static final String TARGET_PLACEHOLDER = "#*SAMPLER_TYPE*#";

    private static final String DEFAULT_FRAGMENT_SHADER_DUMMY =
            "precision mediump float;\n" +
                    "varying lowp vec2 vTextureCoord;\n" +
                    "uniform lowp " + TARGET_PLACEHOLDER + " "+DEFAULT_UNIFORM_SAMPLER+";\n" +
                    "void main() {\n" +
                    "gl_FragColor = texture2D("+DEFAULT_UNIFORM_SAMPLER+", vTextureCoord);\n" +
                    "}\n";


    private static final float[] VERTICES_DATA = new float[] {
            // X, Y, Z, U, V
            -1.0f,  1.0f, 0.0f, 0.0f, 1.0f,
            1.0f,  1.0f, 0.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 0.0f, 1.0f, 0.0f
    };


    private static final int FLOAT_SIZE_BYTES = 4;
    protected static final int VERTICES_DATA_POS_SIZE = 3;
    protected static final int VERTICES_DATA_UV_SIZE = 2;
    protected static final int VERTICES_DATA_STRIDE_BYTES = (VERTICES_DATA_POS_SIZE + VERTICES_DATA_UV_SIZE) * FLOAT_SIZE_BYTES;
    protected static final int VERTICES_DATA_POS_OFFSET = 0; //0 * FLOAT_SIZE_BYTES;
    protected static final int VERTICES_DATA_UV_OFFSET = VERTICES_DATA_POS_OFFSET + VERTICES_DATA_POS_SIZE * FLOAT_SIZE_BYTES;

    private final String mVertexShaderSource;
    private final String mFragmentShaderSource;

    private int program;
    private int vertexShader;
    private int fragmentShader;
    private int vertexBufferName;
    private boolean setupCompleted = false;

    private final HashMap<String, Integer> mHandleMap = new HashMap<>();

    public ImageFilter() {
        super("");
        mVertexShaderSource = DEFAULT_VERTEX_SHADER;
        mFragmentShaderSource = DEFAULT_FRAGMENT_SHADER_DUMMY;
    }

    public Bitmap renderImage(Bitmap bitmap) {
        return bitmap;
    }

    public ImageFilter(@StringRes int name, @DrawableRes int drawableId) {
        this(name, drawableId, DEFAULT_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER_DUMMY);
    }

    public ImageFilter(@StringRes int name, @DrawableRes int drawableId, @StringRes final int vertexShaderSourceResId, @StringRes final int fragmentShaderSourceResId) {
        this(name, drawableId, ImgSdk.getAppResource().getString(vertexShaderSourceResId), ImgSdk.getAppResource().getString(fragmentShaderSourceResId));
    }

    public ImageFilter(@StringRes int name, @DrawableRes int drawableId, final String vertexShaderSource, final String fragmentShaderSource) {
        super(name, drawableId);
        mVertexShaderSource = vertexShaderSource;
        mFragmentShaderSource = fragmentShaderSource;
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    protected static String createTargetShader(@NonNull String shader, final int texTarget) {
        switch (texTarget){
            case GLES11Ext.GL_TEXTURE_EXTERNAL_OES:
                final String gl_oes_require = "#extension GL_OES_EGL_image_external : require";

                return (shader.contains(gl_oes_require) ? "" : gl_oes_require + "\n") + shader.replace(TARGET_PLACEHOLDER, "samplerExternalOES");

            case GL_TEXTURE_2D:
            default:
                return shader.replace(TARGET_PLACEHOLDER, "sampler2D");
        }
    }

    protected synchronized void setup(final int textureTarget) {
        this.textureTarget = textureTarget;
        if (setupCompleted) {
            releaseProgram();
        }

        String targetFragmentShader = createTargetShader(mFragmentShaderSource, textureTarget);

        vertexShader     = openGLES.generateShader(mVertexShaderSource, GL_VERTEX_SHADER);
        fragmentShader   = openGLES.generateShader(targetFragmentShader, GL_FRAGMENT_SHADER);
        program          = openGLES.loadProgram2(vertexShader, fragmentShader);
        vertexBufferName = openGLES.initBuffer(VERTICES_DATA);
        setupCompleted = true;
    }

    protected synchronized void releaseProgram() {
        if (!setupCompleted) {
            return;
        }

        glDeleteProgram(program);
        program = 0;
        glDeleteShader(vertexShader);
        vertexShader = 0;
        glDeleteShader(fragmentShader);
        fragmentShader = 0;
        glDeleteBuffers(1, new int[]{vertexBufferName}, 0);
        vertexBufferName = 0;

        mHandleMap.clear();
        setupCompleted = false;

    }

    /**
     * Release the shader program and texture
     */
    public synchronized void release() {
        if (!setupCompleted) {
            return;
        }

        textureTarget = -1;
        releaseProgram();
    }

    @Override
    public synchronized void draw(@NonNull Texture texture, final float[] mvpMatrix, final float[] stMatrix, final float aspectRatio) {
        if (textureTarget != texture.getTextureTarget()) {
            setup(texture.getTextureTarget());
        }

        useProgram();

        glUniformMatrix4fv(getHandle("uMVPMatrix"), 1, false, mvpMatrix, 0);
        glUniformMatrix4fv(getHandle("uSTMatrix"),  1, false, stMatrix,  0);
        glUniform1f(getHandle("uCRatio"), aspectRatio);

        internalDraw(texture);
    }


    private void internalDraw(@NonNull Texture texture) {

        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferName);
        glEnableVertexAttribArray(getHandle(DEFAULT_ATTRIB_POSITION));
        glVertexAttribPointer(getHandle(DEFAULT_ATTRIB_POSITION), VERTICES_DATA_POS_SIZE, GL_FLOAT, false, VERTICES_DATA_STRIDE_BYTES, VERTICES_DATA_POS_OFFSET);
        glEnableVertexAttribArray(getHandle(DEFAULT_ATTRIB_TEXTURE_COORDINATE));
        glVertexAttribPointer(getHandle(DEFAULT_ATTRIB_TEXTURE_COORDINATE), VERTICES_DATA_UV_SIZE, GL_FLOAT, false, VERTICES_DATA_STRIDE_BYTES, VERTICES_DATA_UV_OFFSET);

        glActiveTexture(GL_TEXTURE0);

        glBindTexture(texture.getTextureTarget(), texture.getTextureId());
        glUniform1i(getHandle(DEFAULT_UNIFORM_SAMPLER), 0);

        onDraw();

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        glDisableVertexAttribArray(getHandle(DEFAULT_ATTRIB_POSITION));
        glDisableVertexAttribArray(getHandle(DEFAULT_ATTRIB_TEXTURE_COORDINATE));
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    /**
     * Check if the Filter has Intensity Control
     * @return true if it has intensity
     */
    public boolean hasIntensityConfig() {
        return (this instanceof FilterConfigIntensity);
    }

    /**
     * Implement this interface if a Filter has Intensity Control
     */
    public interface FilterConfigIntensity {
        Bitmap renderImage(Bitmap bitmap, final float intensity);
    }

    @Nullable
    public Bitmap renderImage(Bitmap bitmap, final float intensity) {
        return null;
    }

    protected void onDraw() {}

    protected final void useProgram() {
        glUseProgram(program);
    }

    protected final int getHandle(final String name) {
        final Integer value = mHandleMap.get(name);
        if (value != null) {
            return value;
        }

        int location = glGetAttribLocation(program, name);
        if (location == -1) {
            location = glGetUniformLocation(program, name);
        }
        if (location == -1) {
            throw new IllegalStateException("Could not get attrib or uniform location for " + name);
        }
        mHandleMap.put(name, location);
        return location;
    }
    public Bitmap renderImage(Bitmap bitmap, boolean previewRendering) {
        return this.renderImage(bitmap, 1.0F, previewRendering);
    }

    @Nullable
    public Bitmap renderImage(Bitmap bitmap, float intensity, boolean previewRendering) {
        return bitmap;
    }

    @Override
    public int getLayout() {
        return R.layout.list_item_filter;
    }

    @Override
    public boolean hasStaticThumbnail() {
        return false;
    }

    @Override
    public Bitmap getThumbnailBitmap(int maxWidth) {
        Resources res = ImgSdk.getAppResource();
        Bitmap bitmap = ImageLoadOperation.getThumbnailBitmap((int) (56 * res.getDisplayMetrics().density));
        if(bitmap == null){
            bitmap = BitmapFactoryUtils.decodeResource(res, R.drawable.filter_preview_photo);
        }
        return bitmap;
    }



}
