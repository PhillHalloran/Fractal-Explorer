package com.example.phillip.fractalexplorer;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.ConditionVariable;
import android.util.Log;

/**
 * Created by Phillip on 28/12/2016.
 */

public class DrawingSurfaceView extends GLSurfaceView{
    private static final String TAG = FractalExplorerActivity.TAG;

    private DrawingSurfaceRenderer mRenderer;
    private final ConditionVariable syncObj = new ConditionVariable();



    public DrawingSurfaceView(Context context, DrawingState drawingState) {

        super(context);

        setEGLContextClientVersion(2); // Request OpenGL ES 2.0

        mRenderer = new DrawingSurfaceRenderer(drawingState, this);



        setRenderer(mRenderer);
    }

    @Override
    public void onPause() {
        /*
         * We call a "pause" function in our Renderer class, which tells it to save state and
         * go to sleep.  Because it's running in the Renderer thread, we call it through
         * queueEvent(), which doesn't wait for the code to actually execute.  In theory the
         * application could be killed shortly after we return from here, which would be bad if
         * it happened while the Renderer thread was still saving off important state.  We need
         * to wait for it to finish.
         */

        super.onPause();

        //Log.d(TAG, "asking renderer to pause");
        syncObj.close();
        queueEvent(new Runnable() {
            @Override public void run() {
                mRenderer.onViewPause(syncObj);
            }});
        syncObj.block();

        Log.d(TAG, "renderer pause complete");
    }
}
