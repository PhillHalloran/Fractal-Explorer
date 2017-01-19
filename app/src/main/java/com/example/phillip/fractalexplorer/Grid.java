package com.example.phillip.fractalexplorer;

import android.util.Log;

import java.util.Arrays;

/**
 * Created by Phillip on 1/01/2017.
 *
 * This can be calculated from calls from two places, the renderer class (onSurfaceChanged) and
 * when on config change (centrePoint/zoom/escapeLimit)
 */

public class Grid {
    private static final String TAG = FractalExplorerActivity.TAG;

    static final double DIVERGE_RADIUS = 2d; //Values exceeding radius are considered divergent
    private int mGridWidth, mGridHeight;
    private double mMinI, mMaxI, mMinJ, mMaxJ ;
    private int mEscapeLimit;
    private double mAngle; // TODO: 5/01/2017 implement rotation
    private double mCoordinateValuesI[], mCoordinateValuesJ[];
    private int mEscapeValues[];

    public Grid(double minI, double maxI,
                double minJ, double maxJ,
                int escapeLimit) {

        mMinI = minI;
        mMaxI = maxI;
        mMinJ = minJ;
        mMaxJ = maxJ;
        mEscapeLimit = escapeLimit;
    }

    // TODO: 5/01/2017 Handle execution when w|h is void

    public Grid fillCoordinateGrid() {
        double deltaI = (mMaxI - mMinI)/(mGridWidth - 1);
        double deltaJ = (mMaxJ - mMinJ)/(mGridHeight - 1);

        for (int i = 0; i < mGridWidth; i++) {
            mCoordinateValuesI[i] = mMinI + i * deltaI;
        }
        for (int j = 0; j < mGridHeight; j++) {
            mCoordinateValuesJ[j] = mMinJ + j * deltaJ;
        }

        return this;
    }

    public Grid computeEscapeValues() {

        for (int j = 0; j < mGridHeight; j++) {
            for (int i = 0; i < mGridWidth; i++) {
                mEscapeValues[j * mGridWidth + i] =
                        divergeTime(mCoordinateValuesI[i], mCoordinateValuesJ[j]);
            }
        }

        return this;
    }

    /*Calculates the diverge time of a specific imaginary set of coordinates*/
    private int divergeTime(double i, double j) {


        double iConst = i;
        double jConst = j;

        double iValue = 0;
        double jValue = 0;

        double a, b;

        int time = 0;

        while(!hasDiverged(iValue, jValue) && time < mEscapeLimit - 1) { //ensure equal to sampling point count

            a = iterateI(iValue, jValue, iConst);
            b = iterateJ(iValue, jValue, jConst);

            iValue = a;
            jValue = b;

            time++;


        }

        return time;

    }

    /*Iterates i component of coordinate*/
    private double iterateI(double i, double j, double iConst) {
        return (Math.pow(i, 2) - Math.pow(j, 2) + iConst);
    }
    /*Iterates j component of coordinate*/
    private double iterateJ(double i, double j, double jConst) {
        return ((2d * i * j) + jConst);
    }

    /*Finds if point is outside of safe region*/
    private boolean hasDiverged(double i, double j) {
        return ((Math.pow(i,2) + Math.pow(j,2)) > DIVERGE_RADIUS * DIVERGE_RADIUS);
    }

    //needed for saving of escape values
    public int[] getEscapeValues() {
        return this.mEscapeValues;
    }

    public void setEscapeValues(int[] values) { this.mEscapeValues = values; } // TODO: 9/01/2017 throw errors from here to ensure grid is of valid size

    public Grid setWidth(int viewWidth) {
        mGridWidth = viewWidth;
        return this;
    }

    public Grid setHeight(int viewHeight) {
        mGridHeight = viewHeight;
        return this;
    }

    public Grid setMinI(double minI){
        mMinI = minI;
        return this;
    }

    public Grid setMaxI(double maxI) {
        mMaxI = maxI;
        return this;
    }

    public Grid setMinJ(double minJ) {
        mMinJ = minJ;
        return this;
    }

    public Grid setMaxJ(double maxJ) {
        mMaxJ = maxJ;
        return this;
    }

    public int getEscapeLimit() { return mEscapeLimit; }

    //called once on every surfaceHasChangedCall
    void allocGrid() {
        mCoordinateValuesI = new double[mGridWidth];
        mCoordinateValuesJ = new double[mGridHeight];
        mEscapeValues = new int[mGridWidth * mGridHeight];
        Log.d(TAG, "before grid filled");
        fillCoordinateGrid();
        Log.d(TAG, "after grid filled");

        long lStart = System.currentTimeMillis();

        computeEscapeValues();

        long lTimeElapsed = System.currentTimeMillis() - lStart;

        Log.d(TAG, "Time taken: " + Long.toString(lTimeElapsed) + "ms");
        Log.d(TAG, "escapeValuesComputed");
    }


}
