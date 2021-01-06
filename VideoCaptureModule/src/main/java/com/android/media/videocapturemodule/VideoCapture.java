package com.android.media.videocapturemodule;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.SessionConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.android.media.videocapturemodule.params.CameraParam;

import java.util.ArrayList;

public class VideoCapture {
    private static final String TAG = "VideoCapture";

    private static final int DEFAULT_VIDEO_WIDTH = 640;
    private static final int DEFAULT_VIDEO_HEIGHT = 480;
    private static final int DEFAULT_FPS = 30;

    private static final int PREVIEW_WIDTH = 720;                                         //预览的宽度
    private static final int PREVIEW_HEIGHT = 1280;                                       //预览的高度
    private static final int SAVE_WIDTH = 720;                                            //保存图片的宽度
    private static final int SAVE_HEIGHT = 1280;                                          //保存图片的高度

    private CameraParam cameraParams;
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraCharacteristics cameraCharacteristics;
    private CaptureRequest captureRequest;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureResult captureResult;
    private ImageReader imageReader;
    private String cameraId = "0";

    private Handler cameraHandler;
    private HandlerThread cameraHandlerThread;
    private SurfaceTexture surfaceTexture;
    private Context context;

    private int cameraSensorOrientation = 0;       //摄像头方向
    private int cameraFacing = CameraCharacteristics.LENS_FACING_BACK; //默认使用后置摄像头
    private int displayRotation = android.view.Surface.ROTATION_0;//手机方向


    private Size previewSize = new  Size(PREVIEW_WIDTH, PREVIEW_HEIGHT);                     //预览大小
    private Size savePictureSize = new Size(SAVE_WIDTH, SAVE_HEIGHT);                         //保存图片大小

    private Surface surface;
    public VideoCapture(Context context) {
        this.context = context;

        cameraHandlerThread = new HandlerThread("cameraThread");
        cameraHandlerThread.start();
        cameraHandler = new Handler(cameraHandlerThread.getLooper());

    }

    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        this.surfaceTexture = surfaceTexture;
    }
    /**
     * 初始化
     */
    public void initCameraInfo() throws CameraAccessException {
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        String[] cameraIdList = cameraManager.getCameraIdList();
        if (cameraIdList.length == 0) {
            Toast.makeText(context, "没有可用相机", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i=0 ; i<cameraIdList.length ; i++) {
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraIdList[i]);
            int facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
            if (facing == cameraFacing) {
                cameraId = cameraIdList[i];
                this.cameraCharacteristics = cameraCharacteristics;
            }
            Log.d(TAG,"设备中的摄像头 id=" + cameraIdList[i]);
        }

        int supportLevel = this.cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        if (supportLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
            Toast.makeText(context, "相机硬件不支持新特性", Toast.LENGTH_SHORT).show();
        }

        //获取摄像头方向
        cameraSensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        //获取StreamConfigurationMap，它是管理摄像头支持的所有输出格式和尺寸
        StreamConfigurationMap configurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

//        Size[] savePictureSize = configurationMap.getOutputSizes(ImageFormat.JPEG);
//        Size[] previewSize = configurationMap.getOutputSizes(SurfaceTexture.class);

//        this.savePictureSize = savePictureSize;
//        this.previewSize = previewSize;

        surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
        imageReader = ImageReader.newInstance(savePictureSize.getWidth(), savePictureSize.getHeight(),ImageFormat.JPEG, 1);


        imageReader.setOnImageAvailableListener((ImageReader.OnImageAvailableListener) reader -> {
            Log.d(TAG,"OnImageAvailableListener");
        },cameraHandler);

        openCamera();
    }

    /**
     * 打开相机
     */
    private void openCamera() throws CameraAccessException {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "没有相机权限!", Toast.LENGTH_SHORT).show();
            return;
        }

        cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                Log.d(TAG,"onOpened");
                cameraDevice = camera;
                try {
                    createCaptureSession(camera);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                Log.d(TAG,"onDisconnected");
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                Log.d(TAG,"onError code:"+error);
            }
        }, cameraHandler);
    }

    private void createCaptureSession(CameraDevice cameraDevice) throws CameraAccessException {

        CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        surface = new Surface(surfaceTexture);
        captureRequestBuilder.addTarget(surface);// 将CaptureRequest的构建器与Surface对象绑定在一起
        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);      // 闪光灯
        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE); // 自动对焦


        // 为相机预览，创建一个CameraCaptureSession对象
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            SessionConfiguration config = null;
            cameraDevice.createCaptureSession(config);
        } else {
            ArrayList<Surface> arrayList = new ArrayList<>();
            arrayList.add(surface);
            arrayList.add(imageReader.getSurface());
            cameraDevice.createCaptureSession(arrayList, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    cameraCaptureSession = session;
                    try {
                        session.setRepeatingRequest(captureRequestBuilder.build(), new CameraCaptureSession.CaptureCallback(){
                            @Override
                            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                                super.onCaptureCompleted(session, request, result);
                                Log.d(TAG,"onCaptureCompleted");
                            }

                            @Override
                            public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                                super.onCaptureFailed(session, request, failure);
                                Log.d(TAG,"onCaptureFailed");
                                Toast.makeText(context, "开启预览失败!", Toast.LENGTH_SHORT).show();
                            }
                        }, cameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(context, "开启预览会话失败!", Toast.LENGTH_SHORT).show();
                }
            },cameraHandler);
        }


//        // surface 在什么时机执行 release ？
//        if (surface != null) {
//            surface.release();
//            surface = null;
//        }
    }

    public void releaseCamera() {
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }

        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }

        if (surface != null) {
            surface.release();
            surface = null;
        }
    }

    public void releaseThread() {
        cameraHandlerThread.quitSafely();
    }
}
