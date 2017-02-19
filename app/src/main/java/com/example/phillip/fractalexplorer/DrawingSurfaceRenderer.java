package com.example.phillip.fractalexplorer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.ConditionVariable;
import android.util.Log;
import android.view.MotionEvent;
import android.support.v4.view.MotionEventCompat;

import java.util.Arrays;

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
        * This function will pass through the width and height on the intial instantiation of the
        * view and also every time the values change. It initialises the calculations of
        * the drawing. For this caluclation to happen somewhere else, another method of
        * getting the width and height of the view would have to be sought out.
        *
        */


        //area of world gets selected, shrunk to 1,1 then blown up to viewport size
        Log.d(TAG, "onSurfaceChanged");

        mViewHeight = height;
        mViewWidth = width;

        GLES20.glViewport(0, 0, width, height);
        
        float viewRatio = (float) height / (float) width;
        mDrawingState.mTexturedMandelbrot.setRatio(viewRatio);
        
        mDrawingState.mTexturedMandelbrot.allocTexturedMandelbrot();

        //sets area of world to capture and place within view
        Matrix.orthoM(mProjectionMatrix, 0,  -0.5f, 0.5f,
                -0.5f, 0.5f,  -1, 1);
        //scale and centre match vertex coords
}

    @Override
    public void onDrawFrame(GL10 unused) {

        DrawingState drawingState = mDrawingState;

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


    float previousX1 = -1f;
    float previousY1 = -1f;
    float previousX2 = -1f;
    float previousY2 = -1f;

    float tempX1 = -1f;
    float tempY1 = -1f;
    float tempX2 = -1f;
    float tempY2 = -1f;

    float xProp;
    float yProp;

    float [] previousMidpoint;
    float [] currentMidpoint;

    float previousAngle;


    public void touchEvent(MotionEvent e) {

        int pointerCount = e.getPointerCount();
        int indexModified = e.getActionIndex();
        int Id = e.getPointerId(indexModified);

        switch (Id){
            case 0:
                tempX1 = e.getX(indexModified);
                tempY1 = e.getY(indexModified);
                break;

            case 1:
                tempX2 = e.getX(indexModified);
                tempY2 = e.getY(indexModified);
                break;

            default:
                break;
        }




        switch(e.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
                previousX1 = tempX1;
                previousY1 = tempY1;

                Log.d(TAG, "0 down");
                break;

            case MotionEvent.ACTION_UP:

                previousX1 = -1;
                previousY1 = -1;

                break;


            case MotionEvent.ACTION_POINTER_DOWN:
                if(Id == 1) {
                    previousX2 = tempX2;
                    previousY2 = tempY2;

                    Log.d(TAG, "1 down");
                }
                if(Id == 0) {
                    previousX1 = tempX1;
                    previousY1 = tempY1;

                    Log.d(TAG, "0 down");
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                if(Id == 1) {
                    previousX2 = -1;
                    previousY2 = -1;
                    Log.d(TAG, "1 up");
                }

                if(Id == 0) {
                    previousX1 = -1;
                    previousY1 = -1;
                    Log.d(TAG, "0 up");
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if(pointerCount == 1){

                    if(Id == 0) {

                        //following code must be reused with two and more action points

                        xProp = (tempX1 - previousX1) / mViewWidth;
                        yProp = (tempY1 - previousY1) / mViewHeight;

                        mDrawingState.mTexturedMandelbrot.move(new float[]{xProp, yProp});
                        previousX1 = tempX1;
                        previousY1 = tempY1;

                        Log.d(TAG, "0: " + xProp + ", " + yProp);
                    }

                    if(Id == 1) {


                        xProp = (tempX1 - previousX2) / mViewWidth;
                        yProp = (tempY1 - previousY2) / mViewHeight;

                        mDrawingState.mTexturedMandelbrot.move(new float[]{xProp, yProp});
                        previousX2 = tempX1;
                        previousY2 = tempY1;

                        Log.d(TAG, "1: " + xProp + ", " + yProp);
                    }
                }


                if(pointerCount > 1) {

                    if(previousX1 == -1 || previousY1 == -1) {
                        previousX1 =  tempX1;
                        previousY1 = tempY1;
                    }

                    if(previousX2 == -1 || previousY2 == -1) {
                        previousX2 = tempX2;
                        previousY2 = tempY2;

                    }

                    previousMidpoint = calcMidpoint(
                            previousX1, previousY1,
                            previousX2, previousY2);

                    currentMidpoint = calcMidpoint(
                            tempX1, tempY1,
                            tempX2, tempY2
                    );

                    xProp = (currentMidpoint[0] - previousMidpoint[0]) / mViewWidth;
                    yProp = (currentMidpoint[1] - previousMidpoint[1]) / mViewHeight;

                    float a = calcMagnitude(new float[] {previousX2 - previousX1, previousY2 - previousY1}) /
                            calcMagnitude(new float[] {tempX2 - tempX1, tempY2 - tempY1});

                    float [] c = new float[] {
                            previousMidpoint[0] - (mViewWidth / 2f),
                            previousMidpoint[1] - (mViewHeight / 2f)};

                    float [] cTemp = scaleVector(c, a - 1);

                    cTemp[0] = cTemp[0] / mViewHeight;
                    cTemp[1] = cTemp[1] / mViewWidth;

                    xProp = xProp + cTemp[0];
                    yProp = yProp + cTemp[1];

                    mDrawingState.mTexturedMandelbrot.move(new float[] {xProp, yProp});

                    mDrawingState.mTexturedMandelbrot.scaleWidth(a);
                    mDrawingState.mTexturedMandelbrot.scaleHeight(a);

                    previousX1 = tempX1;
                    previousY1 = tempY1;
                    previousX2 = tempX2;
                    previousY2 = tempY2;

                }




                break;

            default:
                break;
        }

        mSurfaceView.requestRender();
    }

    private float[] calcMidpoint(float x0, float y0, float x1, float y1){
        float [] result = new float[] {x1 - x0, y1 - y0};

        result[0] *= 0.5f;
        result[1] *= 0.5f;

        result[0] += x0;
        result[1] += y0;

        return result;
    }

    private float calcAngle(float x0, float y0, float x1, float y1) {
        final float NOT_READY = 0f;


        return NOT_READY;
    }

    private float calcMagnitude(float [] vector) {
        return (float) Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1]);
    }

    private float [] scaleVector(float [] v, float scale){
        float [] result = new float[2];

        if(scale == 0f){
            return result;
        }

        result[0] = v[0] * scale;
        result[1] = v[1] * scale;

        return result;
    }
}
