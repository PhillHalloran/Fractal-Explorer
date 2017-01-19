package com.example.phillip.fractalexplorer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.ConditionVariable;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Phillip on 28/12/2016.
 * This class initialises the drawing of a frame to view
 *
 * <p>
 * The methods here expect to run on the Renderer thread.  Calling them from other threads
 * must be done through GLSurfaceView.queueEvent().
 * This needs a link to the surfaceView so it can catch events (touch, pause, ...). It also needs
 * a link to the drawing state to update it and call draw methods
 */

public class DrawingSurfaceRenderer implements GLSurfaceView.Renderer{
    private static final String TAG = FractalExplorerActivity.TAG;

    private DrawingSurfaceView mSurfaceView;
    private DrawingState mDrawingState;

    static final float mProjectionMatrix[] = new float[16];

    DrawingSurfaceRenderer(DrawingState state,
                                  DrawingSurfaceView surfaceView) {
        mSurfaceView = surfaceView;
        mDrawingState = state;


    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        Log.d(TAG, "onSurfaceCreated");
        TexturedGrid.createProgram();
        Log.d(TAG, "program created successfully");

        DrawingState drawingState = mDrawingState;

        GLES20.glClearColor(0f ,0f ,0f ,1f);



    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        /*
        * This function will pass through the width and height on the intial instantiation of the
        * view and also every time the values change. It initialises the calculations of
        * the drawing. For this caluclation to happen somewhere else, another method of
        * getting the width and height of the view would have to be sought out.
        *
        */

        Log.d(TAG, "onSurfaceChanged");

        mDrawingState.mTexturedGrid.setWidth(width);
        mDrawingState.mTexturedGrid.setHeight(height);

        Log.d(TAG, Integer.toString(android.os.Process.myTid()));

        mDrawingState.mTexturedGrid.allocTexturedGrid();

        GLES20.glViewport(0, 0, width, height);

        Matrix.orthoM(mProjectionMatrix, 0,  0, width,
                0, height,  -1, 1);

}

    @Override
    public void onDrawFrame(GL10 unused) {

        DrawingState drawingState = mDrawingState;

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        TexturedGrid.prepareToDraw();
        drawingState.drawTexturedGrid();
        TexturedGrid.finishedDrawing();
    }

    /**
     * Handles pausing of the game Activity.  This is called by the View (via queueEvent) at
     * pause time.  It tells GameState to save its state.
     *
     * @param syncObj Object to notify when we have finished saving state.
     */
    public void onViewPause(ConditionVariable syncObj) {
        /*
         * We don't explicitly pause the game action, because the main game loop is being driven
         * by the framework's calls to our onDrawFrame() callback.  If we were driving the updates
         * ourselves we'd need to do something more.
         */


        syncObj.open();
    }
}
