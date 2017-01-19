package com.example.phillip.fractalexplorer;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


/**
 * Created by Phillip on 29/12/2016.
 * <p>
 * A colour gradient is a function that transforms one colour to another colour over some interval.
 * Ignoring the interval, and just looking at the colour, it can be separated into 3 components,
 * red, green, and blue. We can give each component a continuous value in the range of 0 and 1,
 * meaning none of that component is in the colour and the maximum of that component is in the
 * colour respectively. These components together give us a 3 dimensional space representing all
 * possible colours. If we pick 2 points within this space, and connect them with a line, this is
 * the shortest distance between the 2 colours. If we take this line and look at the colour points
 * it passes between it will show a smooth transition between the 2 colours. Let a function
 * represent the rate at which we move along this line. This function determines what kind of colour
 * gradient is being represented.
 * <p>
 * This is an implementation of a linear gradient which supports
 * the transition between multiple colours. Each colour is defined with
 * a normalised (with respect to distance between colours) proportion of the gradient and an RGB
 * colour value, in component form.
 */

public class Gradient {
    private static final String TAG = FractalExplorerActivity.TAG;

    private ArrayList<ColorProportion> mGradientValues;

    public Gradient(ColorProportion c0, ColorProportion c1) { // TODO: 6/01/2017 make sure not equal
        mGradientValues = new ArrayList<ColorProportion>();
        mGradientValues.add(c0); //proportion must be 0
        mGradientValues.add(c1); //proportion must be 1
        Collections.sort(mGradientValues); //sort so these can be input in any order;


    }

    private int[] computeLocalGradient
            (ColorProportion colorMin, ColorProportion colorMax, float localSamplePoint) {



        int[] sampleColor = new int[4];

        float deltaR = colorMax.mVec[0] - colorMin.mVec[0];
        float deltaG = colorMax.mVec[1] - colorMin.mVec[1];
        float deltaB = colorMax.mVec[2] - colorMin.mVec[2];
        float deltaA = colorMax.mVec[3] - colorMin.mVec[3];

        sampleColor[0] = Math.round(deltaR * localSamplePoint + colorMin.mVec[0]);
        sampleColor[1] = Math.round(deltaG * localSamplePoint + colorMin.mVec[1]);
        sampleColor[2] = Math.round(deltaB * localSamplePoint + colorMin.mVec[2]);
        sampleColor[3] = Math.round(deltaA * localSamplePoint + colorMin.mVec[3]);
        return sampleColor;
    }

    //Returns an array of colours, sampled evenly from the gradient values, including each end.
    public int[] makeGradient(int intervals) {

//        for(int i = 0; i < mGradientValues.size(); i++){
//            Log.d(TAG, Integer.toString(i) + ": " + Arrays.toString(mGradientValues.get(i).mVec));
//        }

        int[] colorArray = new int[intervals * 4]; //only holds colour values
        ColorProportion c, previousC;
        float samplePoint;

        //evenly distribute sampling points
        //scale proportion to local range and calculate colour
        //colour is a function of the overall change * local offset + initial colour

        Collections.sort(mGradientValues);


        //this is an aesthetic choice and may not be the most sensible
        if (intervals == 1) {
            c = mGradientValues.get(0);
            colorArray[0] = (int) c.mVec[0];
            colorArray[1] = (int) c.mVec[1];
            colorArray[2] = (int) c.mVec[2];
            colorArray[3] = (int) c.mVec[3];

        } else {

            float deltaSamplePoint = 1f / (intervals - 1);
            int gradientValuesIndex = 0;

            for (int i = 0; i < intervals; i++) {

                samplePoint = i * deltaSamplePoint;

                c = mGradientValues.get(gradientValuesIndex);
                if (samplePoint == c.mVec[4]) { //equal case, should cover first and last samples
                    colorArray[i* 4] = (int) c.mVec[0];
                    colorArray[i * 4 + 1] = (int) c.mVec[1];
                    colorArray[i * 4 + 2] = (int) c.mVec[2];
                    colorArray[i * 4 + 3] = (int) c.mVec[3];
                    gradientValuesIndex++;
                }

                if (samplePoint < c.mVec[4]) {
                    //calculate values at locally adjusted proportion
                    //take this point and point index - 1
                    int[] sampleColor = new int[4];
                    previousC = mGradientValues.get(gradientValuesIndex - 1);

                    sampleColor = computeLocalGradient(
                            previousC,
                            c,
                            (samplePoint - previousC.mVec[4]) / (c.mVec[4] - previousC.mVec[4])
                    );

                    colorArray[i * 4] = sampleColor[0];
                    colorArray[i * 4 + 1] = sampleColor[1];
                    colorArray[i * 4 + 2] = sampleColor[2];
                    colorArray[i * 4 + 3] = sampleColor[3];

                }


            }
        }

        return colorArray;
    }

    public void add(ColorProportion c){

        boolean contains = false;

        for(int i = 0; i < mGradientValues.size(); i++) {
            if(mGradientValues.get(i).mVec[4] == c.mVec[4]){
                contains = true;
                break;
            }
        }

        if(!contains) {
            mGradientValues.add(c);
            Collections.sort(mGradientValues);
        } else {
            // TODO: 8/01/2017 throw error/handle
        }
    }
    
    public void remove(ColorProportion c) {

        boolean contains = false;
        int index = 0;

        for (int i = 0; i < mGradientValues.size(); i++) {
            if (mGradientValues.get(i).mVec[4] == c.mVec[4]) { //compare proportion value
                contains = true;
                index = i;
                break;
            }
        }

        if (contains) {
            mGradientValues.remove(index);
        } else {
            // TODO: 8/01/2017 throw error 
        }
    }


    public void replace(ColorProportion cOld, ColorProportion cNew) {
        // TODO: 8/01/2017 try catch     
        mGradientValues.remove(cOld);
        mGradientValues.add(cNew);
    }
    
}
