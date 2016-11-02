package com.abcew.camera.gles;

import android.opengl.GLES20;
import android.opengl.GLException;
import android.support.annotation.NonNull;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glGenBuffers;

/**
 * Created by laputan on 16/11/1.
 */
public class OpenGLES {
    private final static String TAG = "OpenGLES";

    private static final int FLOAT_SIZE_BYTES = 4;

    private static final class InstanceHolder {
        static final OpenGLES INSTANCE = new OpenGLES();
    }

    @NonNull
    public static OpenGLES getInstance () {
        return InstanceHolder.INSTANCE;
    }

    private OpenGLES() {
    }

    public synchronized int generateShader(final String shader, final int type) {
        int[] compiled = new int[1];

        int shaderID = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shaderID, shader);
        GLES20.glCompileShader(shaderID);
        GLES20.glGetShaderiv(shaderID, GLES20.GL_COMPILE_STATUS, compiled, 0);

        if (compiled[0] == 0) {
            Log.d(TAG, "Shader Compilation\n" + GLES20.glGetShaderInfoLog(shaderID));
            return 0;
        }
        return shaderID;
    }


    public synchronized int loadProgram2(final int vertexShader, final int pixelShader) throws GLException {
        final int program = GLES20.glCreateProgram();
        if (program == 0) {
            throw new RuntimeException("Could not create program");
        }

        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, pixelShader);

        GLES20.glLinkProgram(program);
        final int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] !=  GLES20.GL_TRUE) {
            GLES20.glDeleteProgram(program);
            throw new RuntimeException("Could not link program");
        }
        return program;
    }

    @NonNull
    private FloatBuffer toFloatBuffer(@NonNull final float[] data) {
        final FloatBuffer buffer = ByteBuffer
                .allocateDirect(data.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        buffer.put(data).position(0);
        return buffer;
    }


    private void loadBuffer(final int bufferName, @NonNull final FloatBuffer data) {
        glBindBuffer(GL_ARRAY_BUFFER, bufferName);
        glBufferData(GL_ARRAY_BUFFER, data.capacity() * FLOAT_SIZE_BYTES, data, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public synchronized int initBuffer(@NonNull final float[] data) {
        return initBuffer(toFloatBuffer(data));
    }

    public synchronized int initBuffer(@NonNull final FloatBuffer data) {
        final int[] buffers = new int[1];
        glGenBuffers(buffers.length, buffers, 0);
        loadBuffer(buffers[0], data);
        return buffers[0];
    }

}