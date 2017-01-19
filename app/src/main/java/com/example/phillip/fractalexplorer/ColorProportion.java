package com.example.phillip.fractalexplorer;

import java.util.Objects;

/**
 * Created by Phillip on 30/12/2016.
 *
 *Represents a proportionally-located colour value.
 */

public class ColorProportion implements Comparable<ColorProportion> {
    private static final String TAG = FractalExplorerActivity.TAG;

    public float [] mVec = new float[5];

    public ColorProportion(float r, float g, float b, float a, float p){
        this.mVec[0] = r;
        this.mVec[1] = g;
        this.mVec[2] = b;
        this.mVec[3] = a;
        this.mVec[4] = p;
    }

    @Override
    public int compareTo(ColorProportion c){
        float sig = (this.mVec[4] - c.mVec[4]);
        if(sig > 0) {
            return 1;
        } else if(sig < 0) {
            return -1;
        } else {
            return 0;
        }
    }
    @Override
    public boolean equals(Object o){

        return true;
    }
}
