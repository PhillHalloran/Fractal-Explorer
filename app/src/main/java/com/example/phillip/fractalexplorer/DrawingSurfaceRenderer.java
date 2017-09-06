package com.example.phillip.fractalexplorer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.ConditionVariable;
import android.util.Log;
import android.view.MotionEvent;

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

    private float [][] finger1, finger2 = new float [2][2];
    public static final int 
            OLD = 0, X = 0,
            NEW = 1, Y = 1;
    private float [] offset;
    private float oldAngle;
    private float newAngle;
    private float angleDifference;
    private float scale;

    private GestureDetector gestureDetector = new GestureDetector();


    public void touchEvent(MotionEvent e) {
        gestureDetector.push(e);
        switch(gestureDetector.gestureCheck()) {

            case GestureDetector.SINGLE_FINGER_GESTURE:
                finger1 = gestureDetector.getFinger(0);
                finger1[NEW][X] = gestureDetector.getCurrentX(0);
                finger1[NEW][Y] = gestureDetector.getCurrentY(0);
                finger1[OLD][X] = gestureDetector.getPreviousX(0);
                finger1[OLD][Y] = gestureDetector.getPreviousY(0);

                offset = new float [2];

                offset[X] = (finger1[NEW][X] - finger1[OLD][X]) / mViewWidth;
                offset[Y] = (finger1[NEW][Y] - finger1[OLD][Y]) / mViewHeight;

                mDrawingState.mTexturedMandelbrot.move(offset);
                mSurfaceView.requestRender();
                break;


            case GestureDetector.TWO_FINGER_GESTURE:
                finger1 = gestureDetector.getFinger(0);
                finger2 = gestureDetector.getFinger(1);

                scale =
                        calcMagnitude(new float[] {
                                finger2[OLD][X] - finger1[OLD][X],
                                finger2[OLD][Y] - finger1[OLD][Y]})
                        /
                        calcMagnitude(new float[] {
                                finger2[NEW][X] - finger1[NEW][X],
                                finger2[NEW][Y] - finger1[NEW][Y]});

                oldAngle = calcAngle(
                        finger1[OLD][X], finger2[OLD][X],
                        finger1[OLD][Y], finger2[OLD][Y]);
                newAngle = calcAngle(
                        finger1[NEW][X], finger2[NEW][X],
                        finger1[NEW][Y], finger2[NEW][Y]);

                angleDifference = oldAngle - newAngle;

                offset = new float[2];
                offset[X] = -finger1[OLD][X] + mViewWidth / 2;
                offset[Y] = -finger1[OLD][Y] + mViewHeight / 2;
                offset = Util.rotateVector(offset, -angleDifference);
                offset = Util.scaleVector(offset, 1f /scale);
                offset[X] += finger1[NEW][X] - mViewWidth / 2;
                offset[Y] += finger1[NEW][Y] - mViewHeight / 2;
                offset[X] = offset[X] / (mViewWidth);
                offset[Y] = offset[Y] / (mViewHeight);

                Log.d(TAG,
                        "Offset: " + offset[X] + ", " + offset[Y] +
                                " Scale: " + scale +
                                " Angle: " + angleDifference +
                                "\n");

                mDrawingState.mTexturedMandelbrot
                        .rotate(angleDifference)
                        .move(offset)
                        .scaleWidth(scale)
                        .scaleHeight(scale);

                mSurfaceView.requestRender();
                break;


            case GestureDetector.NO_GESTURE:
            default:
                break;
        }
        gestureDetector.updateIdsOnUp(e);
    }


    private float calcAngle(float x1, float x2,
                            float y1, float y2) {
        double i = (double) (x2 - x1);
        double j = (double) (y2 - y1);
        if(j != 0) {
            return (float) Math.atan2(j, i);
        } else if(i >= 0){
            return 0f;
        } else {
            return (float) Math.PI;
        }

    }

    private float calcMagnitude(float [] vector) {
        return (float) Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1]);
    }
}
