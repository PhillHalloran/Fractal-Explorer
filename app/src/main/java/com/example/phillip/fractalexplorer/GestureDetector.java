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

    MotionEvent mPreviousEvent;
    MotionEvent mCurrentEvent;
    ArrayList mIds;

    GestureDetector(){
        mPreviousEvent = null;
        mCurrentEvent = null;
        mIds = new ArrayList();
    }

    //add event time checking to avoid unordered events and to throw away duplicates
    public void push(MotionEvent e){
        if(mPreviousEvent == null) {
            mPreviousEvent = MotionEvent.obtain(e);
        } else if (mCurrentEvent == null && !mPreviousEvent.equals(mCurrentEvent)) {
            mCurrentEvent = MotionEvent.obtain(e);
        } else if (!mPreviousEvent.equals(mCurrentEvent)){
            mPreviousEvent = mCurrentEvent;
            mCurrentEvent = MotionEvent.obtain(e);
        }
        updateIdsOnDown(e);
    }

    public String toString(){
        String builder = "";
        if(mPreviousEvent != null){
            builder += "P: " + mPreviousEvent.toString();
        }
        if(mCurrentEvent != null){
            builder += ",\n C: " + mCurrentEvent.toString();
        }
        return builder;
    }

    public void dump(){
        mPreviousEvent = null;
        mCurrentEvent = null;
    }

    // This function returns the gesture type or no gesture if none has occurred or the events
    // break congruence with possible state transitions

    public int gestureCheck(){
        if (mPreviousEvent == null || mCurrentEvent == null){
            return NO_GESTURE;
        }

        int actionPrevious = mPreviousEvent.getActionMasked();
        int actionCurrent = mCurrentEvent.getActionMasked();
        int countPrevious = mPreviousEvent.getPointerCount();
        int countCurrent = mCurrentEvent.getPointerCount();

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

     public int getCurrentId(int pointerIndex){
         return mCurrentEvent.getPointerId(pointerIndex);
     }

     public int getPreviousIndex(int Id){
         return mPreviousEvent.findPointerIndex(Id);
     }


     public float getPreviousX(int idIndex) {
        return mPreviousEvent.getX(mPreviousEvent.findPointerIndex((int) mIds.get(idIndex)));
     }

     public float getPreviousY(int idIndex) {
         return mPreviousEvent.getY(mPreviousEvent.findPointerIndex((int) mIds.get(idIndex)));
     }

     public float getCurrentX(int idIndex) {
         return mCurrentEvent.getX(mCurrentEvent.findPointerIndex((int) mIds.get(idIndex)));
     }

     public float getCurrentY(int idIndex) {
         return mCurrentEvent.getY(mCurrentEvent.findPointerIndex((int) mIds.get(idIndex)));
     }

    private boolean isEmpty(){
        if(mPreviousEvent == null && mCurrentEvent == null) {
            return true;
        } else {
            return false;
        }
    }

    private int activePointer(MotionEvent e) {
        return e.getPointerId(e.getActionIndex());
    }

    private void updateIdsOnDown(MotionEvent e){
        int action = e.getActionMasked();
        int currentId = e.getPointerId(e.getActionIndex());
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                mIds.add(currentId);
                break;
        }
    }

    public void updateIdsOnUp(MotionEvent e) {
        int action = e.getActionMasked();
        int currentId = e.getPointerId(e.getActionIndex());
        switch(action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (mIds.contains(currentId)) {
                    mIds.remove(mIds.indexOf(currentId));
                }
                break;
        }

    }
}
