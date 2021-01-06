package com.android.media.jackplayer.widget.display;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class JackGLSurfaceView extends GLSurfaceView {
    private JackRenderer jackRenderer;

    private DisplayStateListener displayStateListener;
    public JackGLSurfaceView(Context context) {
        this(context,null);
    }

    public JackGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        /**
         * 配置GLSurfaceView
         */
        //设置EGL版本
        setEGLContextClientVersion(2);
        jackRenderer = new JackRenderer(this);
        setRenderer(jackRenderer);
        //设置按需渲染 当我们调用 requestRender 请求GLThread 回调一次 onDrawFrame
        // 连续渲染 就是自动的回调onDrawFrame
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public void setDisplayStateListener (DisplayStateListener displayStateListener) {
        this.displayStateListener = displayStateListener;
    }

    public DisplayStateListener getDisplayStateListener() {
        return displayStateListener;
    }

    public interface DisplayStateListener {
        void onSurfaceCreated(GL10 gl, EGLConfig config,SurfaceTexture surfaceTexture);
        void onSurfaceChanged(GL10 gl, int width, int height, SurfaceTexture surfaceTexture);
        void onDrawFrame(GL10 gl, SurfaceTexture surfaceTexture);
        void onFrameAvailable(SurfaceTexture surfaceTexture);
    }
}
