package com.wangyeming.androidcamerademo.NewCamera2;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.wangyeming.androidcamerademo.CameraConstant;
import com.wangyeming.androidcamerademo.CapturedImageHandle;
import com.wangyeming.androidcamerademo.ErrorConstant;
import com.wangyeming.androidcamerademo.HandleCaptureView;
import com.wangyeming.androidcamerademo.R;
import com.wangyeming.androidcamerademo.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Camera2Activity extends AppCompatActivity implements View.OnClickListener,
        TextureView.SurfaceTextureListener {

    private final static String TAG = "Camera2Activity";

    /**
     * 预览中
     */
    private static final int STATE_PREVIEW = 0;
    /**
     * 等待保存
     */
    private static final int STATE_WAITING_SAVE = 1;
    /**
     * 保存中
     */
    private static final int STATE_SAVING = 2;

    private int mState = -1;

    //缺省拍照的宽高
    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_WEIGHT = 480;

    private Size mPreviewSize;

    /**
     * 相机预览TextureView
     */
    private AutoFitTextureView mTextureView;
    /**
     * 相机设备
     */
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;

    private ViewFlipper vButtonFlipper;
    private HandleCaptureView vHandleCaptureView;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_new_camera2);

        mTextureView = (AutoFitTextureView) findViewById(R.id.camera_texture);
        vButtonFlipper = (ViewFlipper) findViewById(R.id.camera_flipper);
        vHandleCaptureView = (HandleCaptureView) findViewById(R.id.camera_handle);

        //当SurfaceTexture可用时，开启相机
        mTextureView.setSurfaceTextureListener(this);

        findViewById(R.id.camera_picture).setOnClickListener(this);
        vHandleCaptureView.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }


    /**
     * 捕获图像
     */
    protected void takePicture() {
        //检测当前状态是否为预览
        if (mState != STATE_PREVIEW) {
            return;
        }

        //检测相机设备是否成功获开启
        if (mCameraDevice == null) {
            Toast.makeText(this, ErrorConstant.ERROR_TAKE_PHOTO_FAIL, Toast.LENGTH_SHORT).show();
            return;
        }

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            ImageReader reader = createImageReader(manager, mCameraDevice.getId());

            List<Surface> outputSurfaces = new ArrayList<>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(mTextureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {

                @Override
                public void onImageAvailable(final ImageReader reader) {
                    Log.d(TAG, "onImageAvailable");
                    mState = STATE_WAITING_SAVE;

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            vButtonFlipper.setDisplayedChild(1);
                        }
                    });
                    //记录捕获时间
                    final long captureTime = System.currentTimeMillis();

                    CapturedImageHandle capturedImageHandle = new CapturedImageHandle() {

                        @Override
                        public void savePhoto() {
                            Log.d(TAG, "savePhoto");

                            mState = STATE_SAVING;
                            Image image = null;

                            final File pictureFileDir = FileUtils.getOutputFile();

                            if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
                                Toast.makeText(Camera2Activity.this, ErrorConstant.ERROR_CREATE_DIR, Toast.LENGTH_LONG).show();
                                return;
                            }

                            File pictureFile = FileUtils.createPhotoFile(pictureFileDir, captureTime);

                            try {
                                image = reader.acquireLatestImage();
                                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                                byte[] bytes = new byte[buffer.capacity()];
                                buffer.get(bytes);
                                FileUtils.saveByteData(bytes, pictureFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                if (image != null) {
                                    image.close();
                                }
                            }
                        }

                        @Override
                        public void retryTakingPhoto() {
                            Log.d("onImageAvailable", "retryTakingPhoto");
                            vButtonFlipper.setDisplayedChild(0);
                            mState = STATE_PREVIEW;
//                            resetCamera();
                        }

                        @Override
                        public void cancelCamera() {
                            Log.d("onImageAvailable", "cancelCamera");
                            onCancel();
                        }

                    };
                    vHandleCaptureView.setCapturedImageCallback(capturedImageHandle);
                }
            };


            HandlerThread thread = new HandlerThread("CameraPicture");
            thread.start();
            final Handler backgroudHandler = new Handler(thread.getLooper());
            reader.setOnImageAvailableListener(readerListener, backgroudHandler);

            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {

                    super.onCaptureCompleted(session, request, result);
                    Log.d(TAG, "onCaptureCompleted");
                    startPreview();
                }

            };

            //创建CaptureSession耗时，异步回调
            mCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {

                    try {
                        session.capture(captureBuilder.build(), captureListener, backgroudHandler);
                    } catch (CameraAccessException e) {
                        Toast.makeText(Camera2Activity.this, ErrorConstant.ERROR_TAKE_PHOTO_FAIL, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, backgroudHandler);

        } catch (CameraAccessException e) {
            Toast.makeText(Camera2Activity.this, ErrorConstant.ERROR_TAKE_PHOTO_FAIL, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    /**
     * 打开相机
     */
    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                onFail(ErrorConstant.ERROR_OPEN_CAMERA_FAIL_BY_ID);
                return;
            }
            mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];
            manager.openCamera(cameraId, new CameraDevice.StateCallback() {

                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    mCameraDevice = camera;
                    startPreview();
                    mState = STATE_PREVIEW;
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    Toast.makeText(Camera2Activity.this, ErrorConstant.ERROR_CAMERA_DISCONNECTED, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Toast.makeText(Camera2Activity.this, ErrorConstant.ERROR_OPEN_CAMERA_FAIL_BY_ID, Toast.LENGTH_SHORT).show();
                }

            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开启预览
     */
    protected void startPreview() {
        Log.d(TAG, "startPreview");
        if (mCameraDevice == null) {
            onFail(ErrorConstant.ERROR_NO_CAMERA);
            return;
        }

        //检查相机是否可预览
        if (!mTextureView.isAvailable() || mPreviewSize == null) {
            onFail(ErrorConstant.ERROR_CREATE_PREVIEW_FAIL);
            return;
        }

        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        if (texture == null) {
            onFail(ErrorConstant.ERROR_CREATE_PREVIEW_FAIL);
            return;
        }

        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface surface = new Surface(texture);

        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewBuilder.addTarget(surface);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            onFail(ErrorConstant.ERROR_CREATE_PREVIEW_FAIL);
        }

        try {
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession session) {
                    mPreviewSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Toast.makeText(Camera2Activity.this, "onConfigureFailed", Toast.LENGTH_LONG).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            onFail(ErrorConstant.ERROR_TAKE_PHOTO_FAIL);
        }
    }

    /**
     * 更新预览
     */
    protected void updatePreview() {
        Log.d(TAG, "updatePreview");

        if (mCameraDevice == null) {
            onFail(ErrorConstant.ERROR_UPDATE_PREVIEW_FAIL);
        }

        mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        HandlerThread thread = new HandlerThread("CameraPreview");
        thread.start();
        Handler backgroundHandler = new Handler(thread.getLooper());

        try {
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Toast.makeText(Camera2Activity.this, ErrorConstant.ERROR_UPDATE_PREVIEW_FAIL, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.camera_picture) {
            takePicture();
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        //当SurfaceTexture可用时,开启相机
        openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private void onSuccess(String photoFile) {
        mState = STATE_PREVIEW;
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        intent.putExtra(CameraConstant.INTENT_PATH, photoFile);
        Camera2Activity.this.finish();
    }

    private void onFail(String errMsg) {
        Toast.makeText(Camera2Activity.this, errMsg, Toast.LENGTH_LONG).show();
        mState = STATE_PREVIEW;
        Intent intent = new Intent();
        intent.putExtra("errMsg", errMsg);
        setResult(2, intent);
        Camera2Activity.this.finish();
    }

    private void onCancel() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        Camera2Activity.this.finish();
    }

    private static ImageReader createImageReader(CameraManager manager, String cameraId) throws CameraAccessException {
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

        Size[] jpegSizes = characteristics
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                .getOutputSizes(ImageFormat.JPEG);
        int width = DEFAULT_WIDTH;
        int height = DEFAULT_WEIGHT;
        if (jpegSizes != null && jpegSizes.length > 0) {
            width = jpegSizes[0].getWidth();
            height = jpegSizes[0].getHeight();
        }

        return ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
    }
}
