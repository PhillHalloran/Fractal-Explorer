package com.example.phillip.fractalexplorer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.ConditionVariable;
import android.util.Log;
import android.view.MotionEvent;
import android.support.v4.view.MotionEventCompat; //not sure why this is here either

import java.util.Arrays; // not sure why this is here

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

    private float mViewHeight;
    private float mViewWidth;

    DrawingSurfaceRenderer(DrawingState state,
                                  DrawingSurfaceView surfaceView) {
        mSurfaceView = surfaceView;
        mDrawingState = state;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        Log.d(TAG, "onSurfaceCreated");
        TexturedMandelbrot.createProgram();
        Log.d(TAG, "program created successfully");

        DrawingState drawingState = mDrawingState;

        GLES20.glClearColor(0f ,0f ,0f ,1f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        /*
        * This function will pass through the width and height on the initial instantiation of the
        * view and also every time the values change. It initialises the calculations of
        * the drawing. For this calculation to happen somewhere else, another method of
        * getting the width and height of the view would have to be sought out.
        *
        */

        //area of world gets selected, shrunk to 1,1 then blown up to viewport size
        Log.d(TAG, "onSurfaceChanged");

        mViewHeight = height;
        mViewWidth = width;
        float viewRatio = (float) height / (float) width;

        GLES20.glViewport(0, 0, width, height);
        mDrawingState.mTexturedMandelbrot.setRatio(viewRatio);
        mDrawingState.mTexturedMandelbrot.allocTexturedMandelbrot();

        //sets area of world to capture and place within view
        Matrix.orthoM(mProjectionMatrix, 0,  -0.5f, 0.5f,
                -0.5f, 0.5f,  -1, 1);
}

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        mDrawingState.mTexturedMandelbrot.prepareToDraw();
        mDrawingState.drawTexturedMandelbrot();
        mDrawingState.mTexturedMandelbrot.finishedDrawing();

    }

    /**
     * Handles pausing of the drawing Activity.  This is called by the View (via queueEvent) at
     * pause time.  It tells DrawingState to save its state.
     *
     * @param syncObj Object to notify when we have finished saving state.
     */
    public void onViewPause(ConditionVariable syncObj) {

        syncObj.open();
    }

    private float previousX1;
    private float previousX2;
    private float previousY1;
    private float previousY2;
    private float currentX1;
    private float currentX2;
    private float currentY1;
    private float currentY2;
    private float xProp;
    private float yProp;
    private float [] previousMidpoint;
    private float [] currentMidpoint;
    private float previousAngle;
    private float currentAngle;

    private GestureDetector gestureDetector = new GestureDetector();


    public void touchEvent(MotionEvent e) {
        gestureDetector.push(e);

        //the pointer index is ordered at all times, check IDs for continuity
        switch(gestureDetector.gestureCheck()) {
            case GestureDetector.SINGLE_FINGER_GESTURE:
                currentX1 = gestureDetector.getCurrentX(0);
                currentY1 = gestureDetector.getCurrentY(0);
                previousX1 = gestureDetector.getPreviousX(0);
                previousY1 = gestureDetector.getPreviousY(0);
                xProp = (currentX1 - previousX1) / mViewWidth;
                yProp = (currentY1 - previousY1) / mViewHeight;

                mDrawingState.mTexturedMandelbrot.move(new float[] {xProp, yProp});
                mSurfaceView.requestRender();
                break;
            case GestureDetector.TWO_FINGER_GESTURE:
                currentX1 = gestureDetector.getCurrentX(0);
                currentY1 = gestureDetector.getCurrentY(0);
                currentX2 = gestureDetector.getCurrentX(1);
                currentY2 = gestureDetector.getCurrentY(1);
                previousX1 = gestureDetector.getPreviousX(0);
                previousY1 = gestureDetector.getPreviousY(0);
                previousX2 = gestureDetector.getPreviousX(1);
                previousY2 = gestureDetector.getPreviousY(1);
                currentMidpoint = calcMidpoint(currentX1, currentY1, currentX2, currentY2);
                previousMidpoint = calcMidpoint(previousX1, previousY1, previousX2, previousY2);
                xProp = (currentMidpoint[0] - previousMidpoint[0]) / mViewWidth;
                yProp = (currentMidpoint[1] - previousMidpoint[1]) / mViewHeight;

                float a = calcMagnitude(new float[] {previousX2 - previousX1, previousY2 - previousY1}) /
                        calcMagnitude(new float[] {currentX2 - currentX1, currentY2 - currentY1});
                float [] c = new float[] {
                        previousMidpoint[0] - (mViewWidth / 2f),
                        previousMidpoint[1] - (mViewHeight / 2f)};
                c = Util.scaleVector(c, a);
                mDrawingState.mTexturedMandelbrot
                        .move(new float[] {xProp, yProp})
                        .scaleWidth(a)
                        .scaleHeight(a);
                mSurfaceView.requestRender();
                break;
            case GestureDetector.NO_GESTURE:
            default:
                break;
        }
        gestureDetector.updateIdsOnUp(e);
    }

    private float[] calcMidpoint(float x1, float y1, float x2, float y2){
        float [] result = new float[] {x2 - x1, y2 - y1};

        result[0] *= 0.5f;
        result[1] *= 0.5f;

        result[0] += x1;
        result[1] += y1;

        return result;
    }

    private float calcAngle(float x0, float y0, float x1, float y1) { //todo implement this
        final float NOT_READY = 0f;


        return NOT_READY;
    }

    private float calcMagnitude(float [] vector) {
        return (float) Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1]);
    }
}
