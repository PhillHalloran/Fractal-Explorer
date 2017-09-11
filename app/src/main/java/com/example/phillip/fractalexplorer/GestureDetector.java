package com.example.phillip.fractalexplorer;

import android.view.MotionEvent;

import java.util.ArrayList;

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

    public static final int TWO_FINGER_GESTURE = 2;
    public static final int SINGLE_FINGER_GESTURE = 1;
    public static final int NO_GESTURE = 0;

    MotionEvent mOldEvent;
    MotionEvent mNewEvent;
    ArrayList fingerOrder;

    GestureDetector(){
        mOldEvent = null;
        mNewEvent = null;
        fingerOrder = new ArrayList();
    }

    //add event time checking to avoid unordered events and to throw away duplicates
    public void push(MotionEvent e){
        if(mOldEvent == null) {
            mOldEvent = MotionEvent.obtain(e);
        } else if (mNewEvent == null && !mOldEvent.equals(mNewEvent)) {
            mNewEvent = MotionEvent.obtain(e);
        } else if (!mOldEvent.equals(mNewEvent)){
            mOldEvent = mNewEvent;
            mNewEvent = MotionEvent.obtain(e);
        }
        updateIdsOnDown(e);
    }

    public String toString(){
        String builder = "";
        if(mOldEvent != null){
            builder += "P: " + mOldEvent.toString();
        }
        if(mNewEvent != null){
            builder += ",\n C: " + mNewEvent.toString();
        }
        return builder;
    }

    // This function returns the gesture type or no gesture if none has occurred or the events
    // break congruence with possible state transitions

    public int gestureCheck(){
        if (mOldEvent == null || mNewEvent == null){
            return NO_GESTURE;
        }

        int actionPrevious = mOldEvent.getActionMasked();
        int actionCurrent = mNewEvent.getActionMasked();
        int countPrevious = mOldEvent.getPointerCount();
        int countCurrent = mNewEvent.getPointerCount();

        switch(actionCurrent) {

            case MotionEvent.ACTION_MOVE:
                if(countCurrent == 1){
                    switch(actionPrevious) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_MOVE:
                        case MotionEvent.ACTION_POINTER_UP:
                            return SINGLE_FINGER_GESTURE;
                        default:
                            return NO_GESTURE;
                    }
                } else switch (actionPrevious) {
                    case MotionEvent.ACTION_POINTER_DOWN:
                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_POINTER_UP:
                        if(countPrevious > 1) {
                            return TWO_FINGER_GESTURE;
                        } else {
                            return SINGLE_FINGER_GESTURE;
                        }
                    default:
                        return NO_GESTURE;
                }

            case MotionEvent.ACTION_UP:
                switch(actionPrevious) {
                    case MotionEvent.ACTION_MOVE:
                        return SINGLE_FINGER_GESTURE;
                    default:
                        return NO_GESTURE;
                }
            case MotionEvent.ACTION_POINTER_DOWN:
                switch(actionPrevious) {
                    case MotionEvent.ACTION_POINTER_UP:
                        if(countPrevious == 2) {
                            return SINGLE_FINGER_GESTURE;
                        }
                    case MotionEvent.ACTION_POINTER_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        if(countPrevious > 1) {
                            return TWO_FINGER_GESTURE;
                        } else {
                            return SINGLE_FINGER_GESTURE;
                        }
                    default:
                        return NO_GESTURE;
                }
            case MotionEvent.ACTION_POINTER_UP:
                switch(actionPrevious) {
                    case MotionEvent.ACTION_POINTER_DOWN:
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_MOVE:
                        return TWO_FINGER_GESTURE;
                    default:
                        return NO_GESTURE;
                }

            default:
                return NO_GESTURE;
        }

     }

     public float [][] getFinger(int fingerNumber) {
         float [][] finger;
         int fingerId = (int) fingerOrder.get(fingerNumber);
         int oldIndex = mOldEvent.findPointerIndex(fingerId);
         int newIndex = mNewEvent.findPointerIndex(fingerId);
         finger = new float [][]
                 {
                         {mOldEvent.getX(oldIndex), mOldEvent.getY(oldIndex)},
                         {mNewEvent.getX(newIndex), mNewEvent.getY(newIndex)}
                 };
         return finger;
     }

     public float getPreviousX(int fingerNumber) {
        return mOldEvent.getX(mOldEvent.findPointerIndex((int) fingerOrder.get(fingerNumber)));
     }

     public float getPreviousY(int fingerNumber) {
         return mOldEvent.getY(mOldEvent.findPointerIndex((int) fingerOrder.get(fingerNumber)));
     }

     public float getCurrentX(int fingerNumber) {
         return mNewEvent.getX(mNewEvent.findPointerIndex((int) fingerOrder.get(fingerNumber)));
     }

     public float getCurrentY(int fingernumber) {
         return mNewEvent.getY(mNewEvent.findPointerIndex((int) fingerOrder.get(fingernumber)));
     }

    private void updateIdsOnDown(MotionEvent e){
        int action = e.getActionMasked();
        int currentId = e.getPointerId(e.getActionIndex());
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                fingerOrder.add(currentId);
                break;
        }
    }

    public void updateIdsOnUp(MotionEvent e) {
        int action = e.getActionMasked();
        int currentId = e.getPointerId(e.getActionIndex());
        switch(action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (fingerOrder.contains(currentId)) {
                    fingerOrder.remove(fingerOrder.indexOf(currentId));
                }
                break;
        }

    }
}
