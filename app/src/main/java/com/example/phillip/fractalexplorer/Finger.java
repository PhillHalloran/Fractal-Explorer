package com.example.phillip.fractalexplorer;

import android.view.MotionEvent;

/**
 * Created by Phillip on 13/08/2017.
 *
 * Class for simplifying the holding of pointer related data between motionEvents.
 */

public class Finger {

    private float previousX;
    private float previousY;
    private float currentX;
    private float currentY;

    private int pointerID;

    private boolean active;

    Finger(float pX, float pY, float cX, float cY, int PID, boolean a){
        previousX = pX;
        previousY = pY;
        currentX = cX;
        currentY = cY;
        pointerID = PID;
        active = a;
    }

    Finger(float pX, float pY, int PID, boolean a){
        previousX = pX;
        previousY = pY;
        pointerID = PID;
        active = a;
    }

    void update(float cX, float cY, int PID){
        if(pointerID == PID && active){
            previousX = currentX;
            previousY = currentY;
            currentX = cX;
            currentY = cY;
        }
    }

    //This allows for a simple way to set inactive
    void setInactive(){
        active = false;
    }


}