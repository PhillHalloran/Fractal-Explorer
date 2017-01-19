package com.example.phillip.fractalexplorer;

import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

/**
 * Created by Phillip on 4/01/2017.
 *
 * Drawing call and shader code go here. Gets passed an escape value grid and a style (gradient).
 */



public class TexturedGrid extends Grid {
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
            "precision mediump float;" +        // medium is fine for texture maps
                    "uniform sampler2D u_texture;" +    // texture data
                    "varying vec2 v_texCoord;" +        // linearly interpolated texture coordinate

                    "void main() {" +
                    "  gl_FragColor = texture2D(u_texture, v_texCoord);" +
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

    /**
     * Square, suitable for GL_LINE_LOOP.  (The standard COORDS will create an hourglass.)
     * This is expected to have the same number of vertices and coords per vertex as COORDS.
     */
    private static final float OUTLINE_COORDS[] = {
            -0.5f, -0.5f,   // bottom left
            0.5f, -0.5f,   // bottom right
            0.5f,  0.5f,   // top right
            -0.5f,  0.5f,   // top left
    };

    // Common arrays of vertices.
    private static FloatBuffer sVertexArray = TexturedGrid.createVertexArray(COORDS);
    private static FloatBuffer sTexArray = TexturedGrid.createVertexArray(TEX_COORDS);
    private static FloatBuffer sOutlineVertexArray = TexturedGrid.createVertexArray(OUTLINE_COORDS);


    // References to vertex data.
    private static FloatBuffer sVertexBuffer = sVertexArray;

    // Handles to uniforms and attributes in the shader.
    private static int sProgramHandle = -1;
    private static int sTexCoordHandle = -1;
    private static int sPositionHandle = -1;
    private static int sMVPMatrixHandle = -1;

    // Texture data for this instance.
    private int mTextureDataHandle = -1;
    private int mTextureWidth = -1;
    private int mTextureHeight = -1;
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

    private static float[] sTempMVP = new float[16];

    //format and internal format for sending data to opengl target
    private static final int DATA_FORMAT = GLES20.GL_RGBA;
    //R,G,B,A values each held in 1 byte values 00-ff
    private static final int BYTES_PER_PIXEL = 4;

    //texture generaton and setting called elsewhere because no height/width available yet
    public TexturedGrid
    (double minI, double maxI,
     double minJ, double maxJ,
     int escapeLimit,
     Gradient gradient) {

        super(minI, maxI, minJ, maxJ, escapeLimit);
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
        Util.checkGlError("glGetAttribLocation");

        // Get handle to transformation matrix.
        sMVPMatrixHandle = GLES20.glGetUniformLocation(sProgramHandle, "u_mvpMatrix");
        Util.checkGlError("glGetUniformLocation");

        // Get handle to texture reference.
        int textureUniformHandle = GLES20.glGetUniformLocation(sProgramHandle, "u_texture");
        Util.checkGlError("glGetUniformLocation");

        // Set u_texture to reference texture unit 0.  (We don't change the value, so we can just
        // set it here.)
        GLES20.glUseProgram(sProgramHandle);
        GLES20.glUniform1i(textureUniformHandle, 0);
        Util.checkGlError("glUniform1i");
        GLES20.glUseProgram(0);

        Util.checkGlError("TexturedGrid setup complete");
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

    /**
     * NOT NEEDED
     *
     * Specifies the rectangle within the texture map where the texture data is.  By default,
     * the entire texture will be used.
     * <p>
     * Texture coordinates use the image coordinate system, i.e. (0,0) is in the top left.
     * Remember that the bottom-right coordinates are exclusive.
     *
     * @param coords Coordinates within the texture.
     */
    public void setTextureCoords(Rect coords) {
        // Convert integer rect coordinates to [0.0, 1.0].
        float left = (float) coords.left / mTextureWidth;
        float right = (float) coords.right / mTextureWidth;
        float top = (float) coords.top / mTextureHeight;
        float bottom = (float) coords.bottom / mTextureHeight;

        FloatBuffer fb = mTexBuffer;
        fb.put(left);           // bottom left
        fb.put(bottom);
        fb.put(right);          // bottom right
        fb.put(bottom);
        fb.put(left);           // top left
        fb.put(top);
        fb.put(right);          // top right
        fb.put(top);
        fb.position(0);
    }



    @Override
    public Grid setWidth(int viewWidth) {
        super.setWidth(viewWidth);
        mTextureWidth = viewWidth;
        return this;
    }

    @Override
    public Grid setHeight(int viewHeight) {
        super.setHeight(viewHeight);
        mTextureHeight = viewHeight;
        return this;
    }




    //can only be called after height and width have been set
    private ByteBuffer generateTexture() {

        byte[] buffer = new byte[mTextureWidth * mTextureHeight * BYTES_PER_PIXEL];

        //Log.d(TAG, Integer.toString(android.os.Process.myTid()));

        int[] escapeValues = getEscapeValues();
        int[] gradientArray = mGradient.makeGradient(getEscapeLimit());

//        Log.d(TAG, "EscapeValuesSize: " + Integer.toString(escapeValues.length));
//        Log.d(TAG, "GradientArraySize: " + Integer.toString(gradientArray.length));

        for(int i = 0; i < escapeValues.length; i++){ //iterates through every pixel

            buffer[i * BYTES_PER_PIXEL]     =
                    (byte) gradientArray[escapeValues[i] * BYTES_PER_PIXEL];

            buffer[i * BYTES_PER_PIXEL + 1] =
                    (byte) gradientArray[escapeValues[i] * BYTES_PER_PIXEL + 1];

            buffer[i * BYTES_PER_PIXEL + 2] =
                    (byte) gradientArray[escapeValues[i] * BYTES_PER_PIXEL + 2];

            buffer[i * BYTES_PER_PIXEL + 3] =
                    (byte) gradientArray[escapeValues[i] * BYTES_PER_PIXEL + 3];
        }

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(buffer.length);

        byteBuffer.put(buffer).position(0);

        return byteBuffer;
    }

    public void setTexture(ByteBuffer buf, int width, int height, int format) {
        mTextureDataHandle = Util.createImageTexture(buf, width, height, format);
        mTextureWidth = width;
        mTextureHeight = height;
    }

    /**
     * Returns the X position (orld coordinates).
     */
    public float getXPosition() {
        return mModelView[12];
    }

    /**
     * Returns the Y position (world coordinates).
     */
    public float getYPosition() {
        return mModelView[13];
    }

    /**
     * NOT USED: TEXTURE ALWAYS CENTRED
     *
     * Sets the position in the world.
     */
    public void setPosition(float x, float y) {
        // column-major 4x4 matrix
        mModelView[12] = x;
        mModelView[13] = y;
    }

    /**
     * Gets the scale value in the X dimension.
     */
    public float getXScale() {
        return mModelView[0];
    }

    /**
     * Gets the scale value in the Y dimension.
     */
    public float getYScale() {
        return mModelView[5];
    }

    /**
     * Sets the size of the rectangle.
     */
    public void setScale(float xs, float ys) {
        // column-major 4x4 matrix
        mModelView[0] = xs;
        mModelView[5] = ys;
    }
    public void allocTexturedGrid(){
//        Log.d(TAG, "alloc grid");
        allocGrid();
        //Log.d(TAG, "alloc after super");
        //width and height set in renderer before these calls are made
        setTexture(generateTexture(), mTextureWidth, mTextureHeight, DATA_FORMAT);
        setScale(mTextureWidth, mTextureHeight);
        setPosition(mTextureWidth/2.0f, mTextureHeight/2.0f);
//        Log.d(TAG, "alloc grid complete");
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

