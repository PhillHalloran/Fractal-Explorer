package com.example.phillip.fractalexplorer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.widget.AdapterView;

public class FractalExplorerActivity extends AppCompatActivity {
    public static final String TAG = "fractal";

    private GLSurfaceView mGLView;

    private DrawingState mDrawingState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDrawingState = new DrawingState();

        mGLView = new DrawingSurfaceView(this, mDrawingState);
        setContentView(mGLView);
    }


    @Override
    public void onPause() {
        super.onPause();
        mGLView.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
        mGLView.onResume();

    }

}
