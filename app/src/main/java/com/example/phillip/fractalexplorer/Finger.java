package com.example.phillip.fractalexplorer;

import android.view.MotionEvent;

/**
 * Created by Phillip on 13/08/2017.
 *
 * Class for simplifying the holding of pointer related data between motionEvents.
 */

public class Finger {

    float previousX;
    float previousY;
    float currentX;
    float currentY;

    int pointerID;

    boolean active;

    Finger(float pX, float pY, float cX, float cY, int PID, boolean a){
        this.previousX = pX;
        this.previousY = pY;
        this.currentX = cX;
        this.currentY = cY;
        this.pointerID = PID;
        this.active = a;
    }

    void setInactive(){
        this.active = false;
    }

    updateFinger(MotionEvent e){

    }

}
