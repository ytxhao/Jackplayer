package com.android.media.jackplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.WindowManager;

import com.android.media.jackplayer.widget.display.JackGLSurfaceView;
import com.android.media.videocapturemodule.VideoCapture;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity {

    private JackGLSurfaceView jackGLSurfaceView;
    private VideoCapture videoCapture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        videoCapture = new VideoCapture(this);
        jackGLSurfaceView = findViewById(R.id.jackGLSurfaceView);
        jackGLSurfaceView.setDisplayStateListener(new JackGLSurfaceView.DisplayStateListener() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config, SurfaceTexture surfaceTexture) {
                videoCapture.setSurfaceTexture(surfaceTexture);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height, SurfaceTexture surfaceTexture) {

            }

            @Override
            public void onDrawFrame(GL10 gl, SurfaceTexture surfaceTexture) {

            }

            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {

            }
        });
    }
}