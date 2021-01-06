package com.android.media.jackplayer.widget.display;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class JackRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    private JackGLSurfaceView jackGLSurfaceView;
    private SurfaceTexture surfaceTexture;
    private int[] textures;

    public JackRenderer(JackGLSurfaceView jackGLSurfaceView) {
        this.jackGLSurfaceView = jackGLSurfaceView;
    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //通过opengl创建一个纹理id
        textures = new int[1];
        //偷懒 这里可以不配置 （当然 配置了也可以）
        GLES20.glGenTextures(textures.length, textures, 0);
        surfaceTexture = new SurfaceTexture(textures[0]);
        surfaceTexture.setOnFrameAvailableListener(this);
        jackGLSurfaceView.getDisplayStateListener().onSurfaceCreated(gl,config,surfaceTexture);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        jackGLSurfaceView.getDisplayStateListener().onSurfaceChanged(gl,width,height,surfaceTexture);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        jackGLSurfaceView.getDisplayStateListener().onDrawFrame(gl,surfaceTexture);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        jackGLSurfaceView.getDisplayStateListener().onFrameAvailable(surfaceTexture);
    }
}
