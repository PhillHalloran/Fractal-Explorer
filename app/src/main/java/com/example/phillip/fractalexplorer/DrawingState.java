package com.example.phillip.fractalexplorer;

/**
 * Created by Phillip on 28/12/2016.
 *
 * This class represents the configuration of the drawing in it's current state.
 * It does not attempt to deal with the time between each call for a drawing-state
 * calculation. A frame drawing will be called in a dirty way. ;)
 *
 * Some considerations will have to be made when it comes to whether or not
 * the renderer thread should be stopped. If interaction occurs then frame
 * management will need to be taken into consideration.
 */

public class DrawingState {
    private static final String TAG = FractalExplorerActivity.TAG;

    private static final double MINIMUM_I = -2d;
    private static final double MAXIMUM_I = 2d;
    private static final double MINIMUM_J = -2d;
    private static final double MAXIMUM_J = 2d;
    private static final int ESCAPE_LIMIT = 20;

    private static final Gradient mGradient = new Gradient(
            new ColorProportion(100f,   255f,   0f, 255f, 0f),
            new ColorProportion(50f,   0f,   150f, 255f, 1f));

    public TexturedGrid mTexturedGrid;

    //styles

    DrawingState(){
        mTexturedGrid =
                new TexturedGrid(MINIMUM_I, MAXIMUM_I, MINIMUM_J, MAXIMUM_J, ESCAPE_LIMIT, mGradient);
    }


    void drawTexturedGrid(){
        mTexturedGrid.draw();
    }

}
