package com.example.phillip.fractalexplorer;

/**
 * Created by Phillip on 13/08/2017.
 *
 * Class for simplifying the holding of pointer related data between motionEvents.
 */

public class Finger {

    private float mPreviousX;
    private float mPreviousY;
    private float mCurrentX;
    private float mCurrentY;

    private int mPointerID;

    private boolean mActive;

    Finger(float pX, float pY, float cX, float cY, int PID, boolean a){
        mPreviousX = pX;
        mPreviousY = pY;
        mCurrentX = cX;
        mCurrentY = cY;
        mPointerID = PID;
        mActive = a;
    }

    Finger(float pX, float pY, int PID, boolean a){
        mPreviousX = pX;
        mPreviousY = pY;
        mPointerID = PID;
        mActive = a;
    }

    void update(float cX, float cY, int PID){
        if(mPointerID == PID && mActive){
            mPreviousX = mCurrentX;
            mPreviousY = mCurrentY;
            mCurrentX = cX;
            mCurrentY = cY;
        }
    }

    //This allows for a simple way to set inactive
    void setInactive(){
        mActive = false;
    }

    boolean equ(Finger mFinger) {
        return false;
    }
}