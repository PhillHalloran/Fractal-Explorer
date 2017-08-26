package com.example.phillip.fractalexplorer;

import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by Phillip on 26/08/2017.
 * This class allows for the detection of gestures. It supports gestures that arise from both one
 * and two motion events. By storing two motion events, each with their own event time, an
 * understanding of an intended action can be derived.
 *
 * After core interactivity works, double-tap and slide zoom will be implements. This would require
 * more data than two motion events could provide alone.
 */

public class GestureDetector {

    private static final String TAG = FractalExplorerActivity.TAG;
    //The following does not allow for multiple gestures at once (which is possible)
    public static final int MOVE = 0;
    public static final int ZOOM = 1;
    public static final int ROTATE = 2;
    public static final int NONE = 3;

    MotionEvent mEvent1;
    MotionEvent mEvent2;

    public void GestureDetector(){

    }



}
