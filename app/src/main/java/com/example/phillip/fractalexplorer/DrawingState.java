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
 *
 * Originally all the grid data was generated in the grid class. By changing implementing the
 * escape value generation in shader code, the escape values can be calculated in parallel
 * on the gpu. The GL instance only needs to be passed the parameter values for the generation and
 * the colouring information (a 1 high 2D_TEXTURE).
 */

public class DrawingState {
    private static final String TAG = FractalExplorerActivity.TAG;

    private static final int ESCAPE_LIMIT = 20;

    private float MINIMUM_I = -2f;
    private float MAXIMUM_I = 2f;
    private float MINIMUM_J = -2f;
    private float MAXIMUM_J = 2f;


    private static final Gradient mGradient = new Gradient(
            new ColorProportion(100f,   255f,   0f, 255f, 0f),
            new ColorProportion(50f,   0f,   150f, 255f, 1f));

    public TexturedMandelbrot mTexturedMandelbrot;

    //styles

    DrawingState(){
        mTexturedMandelbrot =
                new TexturedMandelbrot(MINIMUM_I, MAXIMUM_I, MINIMUM_J, MAXIMUM_J, ESCAPE_LIMIT, mGradient);
    }


    void drawTexturedMandelbrot(){
        mTexturedMandelbrot.draw();
    }

}
