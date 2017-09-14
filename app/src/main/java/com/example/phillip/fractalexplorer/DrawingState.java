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

    private static final int ESCAPE_LIMIT = 150;

    private double [] vecA = new double [] {2d, 0d};
    private double [] vecB = new double [] {0d, 2d};
    private double [] centrePoint = new double[] {0d, 0d};

    private static final Gradient mGradient = new Gradient(
            new ColorProportion(0f,   0f, 80f, 255f, 0f),
            new ColorProportion(0f,   250f, 50f, 255f, 1f));


    public TexturedMandelbrot mTexturedMandelbrot;


    DrawingState(){
        mGradient.add(new ColorProportion(15f, 200f, 120f, 255f, 0.15f));
        mGradient.add(new ColorProportion(90f, 0f, 0f, 255f, 0.05f));

        mTexturedMandelbrot = new TexturedMandelbrot(
                vecA,
                vecB,
                centrePoint,
                ESCAPE_LIMIT,
                TexturedMandelbrot.EMULATED_DOUBLE,
                mGradient);
    }


    void drawTexturedMandelbrot(){
        mTexturedMandelbrot.draw();
    }

}
