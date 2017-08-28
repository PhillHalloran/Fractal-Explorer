package com.example.phillip.fractalexplorer;

/**
 * Created by Phillip on 21/01/2017.
 */

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import javax.microedition.khronos.opengles.GL;

public class TexturedMandelbrot {
    private static final String TAG = FractalExplorerActivity.TAG;

    static final String VERTEX_SHADER_CODE =
            "uniform mat4 u_mvpMatrix;" +       // model/view/projection matrix
                    "attribute vec4 a_position;" +      // vertex data for us to transform
                    "attribute vec2 a_texCoord;" +      // texture coordinate for vertex...
                    "varying vec2 v_texCoord;" +        // ...which we forward to the fragment shader

                    "void main() {" +
                    "  gl_Position = u_mvpMatrix * a_position;" +
                    "  v_texCoord = a_texCoord;" +
                    "}";

    static final String FRAGMENT_SHADER_CODE =
            "precision mediump float;" +
                    "uniform sampler2D u_texture;" + // interpolated gradient in 1 high 2D texture
                    "varying vec2 v_texCoord;" +     // tex coord passed from vertex shader
                    "uniform vec2 u_cp;" +
                    "uniform vec2 u_vecA;"+
                    "uniform vec2 u_vecB;"+
                    "uniform int u_iter;" +            // escape limit

                    "void main() {" +
                    "  vec2 z, c, texCoord;" +

                    "c = u_cp - u_vecA - u_vecB + vec2(2.0) * (v_texCoord.x * u_vecA + v_texCoord.y * u_vecB);"+

                    "  int i;" +
                    "  z = c;" +
                    "  for(i = 0; i < u_iter; i++) {" +
                    "    if((z.x * z.x + z.y * z.y) > 4.0) break;" +
                    "    float x = (z.x * z.x - z.y * z.y) + c.x;" +
                    "    float y = (z.y * z.x + z.x * z.y) + c.y;" +
                    "    z.x = x;" +
                    "    z.y = y;" +
                    "  }" +

                    "  texCoord.x = i == u_iter ? 1.0 - 1.0 / float(u_iter) : float(i) / float(u_iter);"+
                    "  texCoord.y = 0.0;"+
                    "  gl_FragColor = texture2D(u_texture, texCoord);" +
                    "}";


    /**
     * Model/view matrix for this object.  Updated by setPosition() and setScale().  This
     * should be merged with the projection matrix when it's time to draw the object.
     */
    protected float[] mModelView;

    /**
     * Simple square, specified as a triangle strip.  The square is centered on (0,0) and has
     * a size of 1x1.
     * <p>
     * Triangles are 0-1-2 and 2-1-3 (counter-clockwise winding).
     */
    private static final float COORDS[] = {
            -0.5f, -0.5f,   // 0 bottom left
            0.5f, -0.5f,   // 1 bottom right
            -0.5f,  0.5f,   // 2 top left
            0.5f,  0.5f,   // 3 top right
    };

    /**
     * Texture coordinates.  These are flipped upside-down to match pixel data that starts
     * at the top left (typical of many image formats).
     */
    private static final float TEX_COORDS[] = {
            0.0f,   1.0f,   // bottom left
            1.0f,   1.0f,   // bottom right
            0.0f,   0.0f,   // top left
            1.0f,   0.0f,   // top right
    };

    // Common arrays of vertices.
    private static FloatBuffer sVertexArray = TexturedMandelbrot.createVertexArray(COORDS);
    private static FloatBuffer sTexArray = TexturedMandelbrot.createVertexArray(TEX_COORDS);

    // References to vertex data.
    private static FloatBuffer sVertexBuffer = sVertexArray;

    // Handles to uniforms and attributes in the shader.
    private static int sProgramHandle = -1;
    private static int sTexCoordHandle = -1;
    private static int sPositionHandle = -1;
    private static int sMVPMatrixHandle = -1;
    private static int sIterUniformHandle = -1;
    private static int sVecAUniformHandle = -1;
    private static int sVecBUniformHandle = -1;
    private static int sCentrePointHandle = -1;

    // Texture data for this instance.
    private int mTextureDataHandle = -1;
    private int mTextureWidth = -1;
    private int mTextureHeight = 1;
    private FloatBuffer mTexBuffer;

    // Sanity check on draw prep.
    private static boolean sDrawPrepared;

    public static final int COORDS_PER_VERTEX = 2;         // x,y
    public static final int TEX_COORDS_PER_VERTEX = 2;     // s,t
    public static final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4; // 4 bytes per float
    public static final int TEX_VERTEX_STRIDE = TEX_COORDS_PER_VERTEX * 4;

    // vertex count should be the same for both COORDS and TEX_COORDS
    public static final int VERTEX_COUNT = COORDS.length / COORDS_PER_VERTEX;

    private Gradient mGradient;
    private int mEscapeLimit;

    private static float[] sVecA = new float[2];
    private static float[] sVecB = new float[2];
    private static float[] sCentrePoint = new float[2];


    private static float[] sTempMVP = new float[16];

    //format and internal format for sending data to opengl target
    private static final int DATA_FORMAT = GLES20.GL_RGBA;
    //R,G,B,A values each held in 1 byte values 00-ff
    private static final int BYTES_PER_PIXEL = 4;

    //bounds are calculated to match pixel ratio
    TexturedMandelbrot(float minI, float maxI,
                       float minJ, float maxJ,
                       int escapeLimit,
                       Gradient gradient){

        mGradient = gradient;

        mModelView = new float[16];
        Matrix.setIdentityM(mModelView, 0);

        ByteBuffer bb = ByteBuffer.allocateDirect(VERTEX_COUNT * TEX_VERTEX_STRIDE);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(sTexArray);
        sTexArray.position(0);
        fb.position(0);
        mTexBuffer = fb;

        sVecA[0] = 2f;
        sVecA[1] = 0f;
        sVecB[0] = 0f;
        sVecB[1] = 2f;
        sCentrePoint[0] = 0f;
        sCentrePoint[1] = 0f;

        mEscapeLimit = escapeLimit;
        mTextureWidth = escapeLimit;
    }

    // TODO: 5/02/2017 parameter setter methods -cp -veca -vecb

    // TODO: 5/02/2017 get bounding parameters

    public int getLimit() {
        return mEscapeLimit;
    }

    private float getMagnitude(float [] vector) {
        return (float) Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1]);
    }

    public float getWidth() {
        return getMagnitude(sVecA);
    }

    public float getheight() {
        return getMagnitude(sVecB);
    }

    public void setLimit(int limit){
        mEscapeLimit = limit;
        mTextureWidth = limit;
    }

    //changes the vector lengths to a desired ratio, ensuring
    public void setRatio(float viewRatio) {
        float magA, magB, vectorRatio;

        magA = getMagnitude(sVecA);
        magB = getMagnitude(sVecB);

        vectorRatio = magB / magA;

        if(vectorRatio >= 1f) {
            if(viewRatio >= 1f) {
                setHeight(magB * viewRatio/vectorRatio);
            } else {
                setHeight(magA);
                setWidth(magA / viewRatio);
            }
        } else {
            if(viewRatio >= 1f) {
                setWidth(magB);
                setHeight(magB * viewRatio);
            } else {
                setWidth(magA * viewRatio/vectorRatio);

            }

        }

    }

    private float [] setVectorLength(float[] v, float l) {

        float [] result = new float[2];

        if(l == 0f) {
            return result;
        }

        float m = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1]);
        float scale = l / m;

        result[0] = v[0] * scale;
        result[1] = v[1] * scale;

        return result;
    }

    public void setWidth(float w){
        sVecA = setVectorLength(sVecA, w);
    }

    public void setHeight(float h){
        sVecB = setVectorLength(sVecB, h);
    }

    public TexturedMandelbrot scaleWidth(float proportion) {
        sVecA = Util.scaleVector(sVecA, proportion);
        return this;
    }

    public TexturedMandelbrot scaleHeight(float proportion) {
        sVecB = Util.scaleVector(sVecB, proportion);
        return this;
    }

    public TexturedMandelbrot move(float [] normalizedOffset) {

        float [] xVector = Util.scaleVector(sVecA,  2f * normalizedOffset[0]);
        float [] yVector = Util.scaleVector(sVecB,  2f * normalizedOffset[1]);

        sCentrePoint[0] -= xVector[0] + yVector[0];
        sCentrePoint[1] -= xVector[1] + yVector[1];
        return this;
    }

    public TexturedMandelbrot rotate(float r) {
        float x, y;

        x = sVecA[0];
        y = sVecA[1];

        sVecA[0] = x * (float)Math.cos(r) - y * (float)Math.sin(r);
        sVecA[1] = x * (float)Math.sin(r) + y * (float)Math.cos(r);

        x = sVecB[0];
        y = sVecB[1];

        sVecB[0] = x * (float)Math.cos(r) - y * (float)Math.sin(r);
        sVecB[1] = x * (float)Math.sin(r) + y * (float)Math.cos(r);

        return this;
    }

        public void setTexture(ByteBuffer buf, int width, int height, int format) {
        mTextureDataHandle = Util.createImageTexture(buf, width, height, format);
        mTextureWidth = width;
        mTextureHeight = height;
    }

    private ByteBuffer generateTexture() {

        byte[] buffer = new byte[mTextureWidth * mTextureHeight * BYTES_PER_PIXEL];

        int[] gradientArray = mGradient.makeGradient(mEscapeLimit);

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(buffer.length);

        for(int i = 0; i < gradientArray.length; i++) {

            buffer[i] = (byte) gradientArray[i];
        }

        byteBuffer.put(buffer).position(0);

        return byteBuffer;
    }


    /**
     * Creates the GL program and associated references.
     */
    public static void createProgram() {
        sProgramHandle = Util.createProgram(VERTEX_SHADER_CODE,
                FRAGMENT_SHADER_CODE);
        Log.d(TAG, "Created program " + sProgramHandle);

        // Get handle to vertex shader's a_position member.
        sPositionHandle = GLES20.glGetAttribLocation(sProgramHandle, "a_position");
        Util.checkGlError("glGetAttribLocation");

        // Get handle to vertex shader's a_texCoord member.
        sTexCoordHandle = GLES20.glGetAttribLocation(sProgramHandle, "a_texCoord");
        Log.d(TAG, "texCoordHandle" + sTexCoordHandle);
        Util.checkGlError("glGetAttribLocation");

        // Get handle to transformation matrix.
        sMVPMatrixHandle = GLES20.glGetUniformLocation(sProgramHandle, "u_mvpMatrix");
        Util.checkGlError("glGetUniformLocation");

        // Get handle to texture reference.
        int textureUniformHandle = GLES20.glGetUniformLocation(sProgramHandle, "u_texture");
        Util.checkGlError("glGetUniformLocation");

        sVecAUniformHandle = GLES20.glGetUniformLocation(sProgramHandle, "u_vecA");
        Util.checkGlError("glGetUniformLocation");

        sVecBUniformHandle = GLES20.glGetUniformLocation(sProgramHandle, "u_vecB");
        Util.checkGlError("glGetUniformLocation");

        sCentrePointHandle = GLES20.glGetUniformLocation(sProgramHandle, "u_cp");
        Util.checkGlError("glGetUniformLocation");

        sIterUniformHandle = GLES20.glGetUniformLocation(sProgramHandle, "u_iter");
        Util.checkGlError("glGetUniformLocation");

        // Set u_texture to reference texture unit 0.  (We don't change the value, so we can just
        // set it here.)
        GLES20.glUseProgram(sProgramHandle);
        GLES20.glUniform1i(textureUniformHandle, 0);
        Util.checkGlError("glUniform1i");
        GLES20.glUseProgram(0);

        Util.checkGlError("TexturedMandelbrot setup complete");
    }


    /**
     * Allocates a direct float buffer, and populates it with the vertex data.
     */
    private static FloatBuffer createVertexArray(float[] coords) {
        // Allocate a direct ByteBuffer, using 4 bytes per float, and copy coords into it.
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(coords);
        fb.position(0);
        return fb;
    }

    public void allocTexturedMandelbrot(){
        setTexture(generateTexture(), mTextureWidth, mTextureHeight, DATA_FORMAT);
    }


    public static void prepareToDraw(){
        // Select our program.
        GLES20.glUseProgram(sProgramHandle);
        Util.checkGlError("glUseProgram");

        // Enable the "a_position" vertex attribute.
        GLES20.glEnableVertexAttribArray(sPositionHandle);
        Util.checkGlError("glEnableVertexAttribArray");

        // Connect sVertexBuffer to "a_position".
        GLES20.glVertexAttribPointer(sPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, VERTEX_STRIDE, sVertexBuffer);
        Util.checkGlError("glEnableVertexAttribPointer");

        // Enable the "a_texCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(sTexCoordHandle);
        Util.checkGlError("glEnableVertexAttribArray");

        sDrawPrepared = true;

    }

    /**
     * Cleans up after drawing.
     */
    public static void finishedDrawing() {
        sDrawPrepared = false;

        // Disable vertex array and program.  Not strictly necessary.
        GLES20.glDisableVertexAttribArray(sPositionHandle);
        GLES20.glUseProgram(0);
    }

    /**
     * Draws the textured rect.
     */
    public void draw() {
//        if (GameSurfaceRenderer.EXTRA_CHECK) Util.checkGlError("draw start");
        if (!sDrawPrepared) {
            throw new RuntimeException("not prepared");
        }

        // Connect mTexBuffer to "a_texCoord".
        GLES20.glVertexAttribPointer(sTexCoordHandle, TEX_COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, TEX_VERTEX_STRIDE, mTexBuffer);
//        if (GameSurfaceRenderer.EXTRA_CHECK) Util.checkGlError("glVertexAttribPointer");



        // Compute model/view/projection matrix.
        float[] mvp = sTempMVP;     // scratch storage
        Matrix.multiplyMM(mvp, 0, DrawingSurfaceRenderer.mProjectionMatrix, 0, mModelView, 0);

        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(sMVPMatrixHandle, 1, false, mvp, 0);
//        if (GameSurfaceRenderer.EXTRA_CHECK) Util.checkGlError("glUniformMatrix4fv");

        GLES20.glUniform2fv(sVecAUniformHandle, 1, FloatBuffer.wrap(sVecA));

        GLES20.glUniform2fv(sVecBUniformHandle, 1, FloatBuffer.wrap(sVecB));

        GLES20.glUniform2fv(sCentrePointHandle, 1, FloatBuffer.wrap(sCentrePoint));

        GLES20.glUniform1i(sIterUniformHandle, mEscapeLimit);

        // Set the active texture unit to unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//        if (GameSurfaceRenderer.EXTRA_CHECK) Util.checkGlError("glActiveTexture");

        // In OpenGL ES 1.1 you needed to call glEnable(GLES20.GL_TEXTURE_2D).  This is not
        // required in 2.0, and will actually raise a GL_INVALID_ENUM error.

        // Bind the texture data to the 2D texture target.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
//        if (GameSurfaceRenderer.EXTRA_CHECK) Util.checkGlError("glBindTexture");

        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_COUNT);
//        if (GameSurfaceRenderer.EXTRA_CHECK) Util.checkGlError("glDrawArrays");
    }

}
