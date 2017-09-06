package com.example.phillip.fractalexplorer;

import android.opengl.*;
import android.util.Log;
import java.nio.ByteBuffer;


/**
 * Created by Phillip on 29/12/2016.
 */

public final class Util {
    private static final String TAG = FractalExplorerActivity.TAG;

    public static int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public static int createProgram(String vertexShaderCode, String fragmentShaderCode){
        int vertexShader =
                Util.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader =
                Util.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        //create program object, attach shaders, link & check for compatibility
        int programHandle = GLES20.glCreateProgram();
        GLES20.glAttachShader(programHandle, vertexShader);
        GLES20.glAttachShader(programHandle, fragmentShader);
        GLES20.glLinkProgram(programHandle);

        //check link
        int[] linkStatus = new int[1];

        /** get GL_LINK_STATUS parameter value from program object, could add more parameter values
         *  to array, or query at another point, incrementing offset (currently set to 0) to check
         *  or compare values of same parameter at different times
         */
        GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

        //choice to handle possible linking error immediately rather than just throwing error
        if(linkStatus[0] == GLES20.GL_FALSE) {
            String msg = GLES20.glGetProgramInfoLog(programHandle);
            GLES20.glDeleteProgram(programHandle);

            Log.e(TAG, "glLinkProgram: " + msg);
            throw new RuntimeException("glLinkProgram failed");
        }

        GLES20.glDetachShader(programHandle, vertexShader);
        GLES20.glDetachShader(programHandle, fragmentShader);
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);

        return programHandle;
    }


    /**
     * Creates a texture from raw data.
     *
     * @param data Image data.
     * @param width Texture width, in pixels (not bytes).
     * @param height Texture height, in pixels.
     * @param format Image data format (use constant appropriate for glTexImage2D(), e.g. GL_RGBA).
     * @return Handle to texture.
     */
    public static int createImageTexture(ByteBuffer data, int width, int height, int format) {
        int[] textureHandles = new int[1];
        int textureHandle;

        GLES20.glGenTextures(1, textureHandles, 0);
        textureHandle = textureHandles[0];
        Util.checkGlError("glGenTextures");

        // Bind the texture handle to the 2D texture target.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);

        // Configure min/mag filtering, i.e. what scaling method do we use if what we're rendering
        // is smaller or larger than the source image.
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_NEAREST);
        Util.checkGlError("loadImageTexture");

        // Load the data from the buffer into the texture handle.
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, /*level*/ 0, format,
                width, height, /*border*/ 0, format, GLES20.GL_UNSIGNED_BYTE, data);
        Util.checkGlError("loadImageTexture");

        return textureHandle;
    }

    /**
     * Utility method for checking for OpenGL errors.  Use like this:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If an error was detected, this will throw an exception.
     *
     * @param msg string to display in the error message (usually the name of the last
     *      GL operation)
     */
    public static void checkGlError(String msg) {
        int error, lastError = GLES20.GL_NO_ERROR;

        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, msg + ": glError " + error);
            lastError = error;
        }
        if (lastError != GLES20.GL_NO_ERROR) {
            throw new RuntimeException(msg + ": glError " + lastError);
        }
    }


    /**
     * Functional method for calculation of scaled vectors.
     * @param v input vector
     * @param scale float of result vector
     * @return
     */
    public static float [] scaleVector(float [] v, float scale){
        float [] result = new float[2];

        if(scale == 0f){
            return result;
        }

        result[0] = v[0] * scale;
        result[1] = v[1] * scale;

        return result;
    }

    public static float [] rotateVector(float [] v, float angle){
        float [] result = new float[2];
        float x = v[0], y = v[1];

        result[0] = x * (float)Math.cos(angle) - y * (float)Math.sin(angle);
        result[1] = x * (float)Math.sin(angle) + y * (float)Math.cos(angle);

        return result;
    }
}
