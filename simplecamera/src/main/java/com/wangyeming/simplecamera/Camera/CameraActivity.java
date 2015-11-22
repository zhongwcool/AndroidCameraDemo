package com.wangyeming.simplecamera.Camera;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.wangyeming.simplecamera.Camera.photo.CameraPreview;
import com.wangyeming.simplecamera.Camera.photo.CameraUtil;
import com.wangyeming.simplecamera.CameraConstant;
import com.wangyeming.simplecamera.ErrorConstant;
import com.wangyeming.simplecamera.R;
import com.wangyeming.simplecamera.View.HandleCaptureView;
import com.wangyeming.simplecamera.interfaces.CameraCallback;
import com.wangyeming.simplecamera.interfaces.CapturedImageHandle;
import com.wangyeming.simplecamera.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CameraActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener,
        Camera.PictureCallback, CameraCallback {

    private static final String TAG = "NewCamera2Activity";


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

    /**
     * 相机类
     */
    private Camera mCamera;
    /**
     * 显示预览界面
     */
    private CameraPreview mPreview;

    private ViewFlipper vButtonFlipper;
    private HandleCaptureView vHandleCaptureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //全屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera);

        vButtonFlipper = (ViewFlipper) findViewById(R.id.camera_flipper);
        vHandleCaptureView = (HandleCaptureView) findViewById(R.id.camera_handle);

        mPreview = new CameraPreview(this);
        FrameLayout vPreview = (FrameLayout) findViewById(R.id.camera_preview);
        vPreview.addView(mPreview);

        vHandleCaptureView.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        //打开相机的操作延迟到onResume()方法里面去执行，这样可以使得代码更容易重用，还能保持控制流程更为简单。
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            //未检测到系统的相机
            Toast.makeText(this, ErrorConstant.ERROR_NO_BACK_CAMERA, Toast.LENGTH_LONG).show();
            onFail(ErrorConstant.ERROR_NO_BACK_CAMERA);
        } else {
            int cameraId = CameraUtil.findBackFacingCamera();
            if (!CameraUtil.isCameraIdValid(cameraId)) {
                Toast.makeText(this, ErrorConstant.ERROR_NO_CAMERA, Toast.LENGTH_LONG).show();
                onFail(ErrorConstant.ERROR_NO_CAMERA);
            } else {
                if (safeCameraOpen(cameraId)) {
                    mCamera.startPreview();
                    mPreview.setCamera(mCamera);
                    mState = STATE_PREVIEW;
                    findViewById(R.id.camera_take_photo).setOnClickListener(this);
                    findViewById(R.id.camera_preview).setOnTouchListener(this);
                } else {
                    onFail(ErrorConstant.ERROR_OPEN_CAMERA_FAIL_BY_ID);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCameraAndPreview();
    }

    @Override
    public void onBackPressed() {
        onCancel();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.camera_take_photo) {
            if(mState != STATE_PREVIEW) {
                return;
            }
            //触发一个异步的图片捕获回调
            mCamera.takePicture(null, null, this);
            mState = STATE_WAITING_SAVE;
        }
    }


    /**
     * 获取Camera，并加入开启检测
     *
     * @param id
     * @return
     */
    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;

        try {
            releaseCameraAndPreview();
            mCamera = Camera.open(id);
            qOpened = (mCamera != null);
        } catch (Exception e) {
            e.printStackTrace();
            onFail(ErrorConstant.ERROR_OPEN_CAMERA_FAIL_BY_ID);
        }

        return qOpened;
    }

    /**
     * 释放相机和预览
     */
    private void releaseCameraAndPreview() {
        mPreview.setCamera(null);
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 重置相机
     */
    private void resetCamera() {
        mCamera.startPreview();
        mPreview.setCamera(mCamera);
        mState = STATE_PREVIEW;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return true;
        }

        if (mState != STATE_PREVIEW) {
            return true;
        }

        Camera camera = mCamera;
        camera.cancelAutoFocus();
        Rect focusRect = calculateFocusArea(event.getX(), event.getY());

        Camera.Parameters parameters = camera.getParameters();
        if (!Camera.Parameters.FOCUS_MODE_AUTO.equals(parameters.getFocusMode())) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        if (parameters.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> mylist = new ArrayList<>();
            mylist.add(new Camera.Area(focusRect, 1000));
            parameters.setFocusAreas(mylist);
        }

        try {
            camera.cancelAutoFocus();
            camera.setParameters(parameters);
            camera.startPreview();
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE.equals(camera.getParameters().getFocusMode())) {
                        Camera.Parameters parameters = camera.getParameters();
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                        if (parameters.getMaxNumFocusAreas() > 0) {
                            parameters.setFocusAreas(null);
                        }
                        camera.setParameters(parameters);
                        camera.startPreview();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 下面为获取对焦区域的代码
     */
    private static final int FOCUS_AREA_SIZE = 300;

    private Rect calculateFocusArea(float x, float y) {
        int left = clamp(Float.valueOf((x / mPreview.getWidth()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
        int top = clamp(Float.valueOf((y / mPreview.getHeight()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);

        return new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper) + focusAreaSize / 2 > 1000) {
            if (touchCoordinateInCameraReper > 0) {
                result = 1000 - focusAreaSize / 2;
            } else {
                result = -1000 + focusAreaSize / 2;
            }
        } else {
            result = touchCoordinateInCameraReper - focusAreaSize / 2;
        }
        return result;
    }

    @Override
    public void onPictureTaken(final byte[] data, Camera camera) {
        vButtonFlipper.setDisplayedChild(1);
        final long captureTime = System.currentTimeMillis();

        CapturedImageHandle capturedImageHandle = new CapturedImageHandle() {
            @Override
            public void savePhoto() {
                mState = STATE_SAVING;
                Log.d("onPictureTaken", "savePhoto");

                File pictureFileDir = FileUtils.getOutputFile();

                if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
                    Toast.makeText(CameraActivity.this, ErrorConstant.ERROR_CREATE_DIR, Toast.LENGTH_LONG).show();
                    return;
                }

                File pictureFile = FileUtils.createPhotoFile(pictureFileDir, captureTime);

                try {
                    FileUtils.saveByteData(data, pictureFile);
                    onSuccess(pictureFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(CameraActivity.this, ErrorConstant.ERROR_FAIL_SAVE_PHOTO, Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void retryTakingPhoto() {
                Log.d("onPictureTaken", "retryTakingPhoto");
                vButtonFlipper.setDisplayedChild(0);
                resetCamera();
            }

            @Override
            public void cancelCamera() {
                Log.d("onPictureTaken", "cancelCamera");
                onCancel();
            }
        };

        vHandleCaptureView.setCapturedImageCallback(capturedImageHandle);
    }

    @Override
    public void onSuccess(String photoFile) {
        mState = STATE_PREVIEW;
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        intent.putExtra(CameraConstant.INTENT_PATH, photoFile);
        CameraActivity.this.finish();
    }

    @Override
    public void onFail(String errMsg) {
        mState = STATE_PREVIEW;
        Intent intent = new Intent();
        intent.putExtra("errMsg", errMsg);
        setResult(2, intent);
        CameraActivity.this.finish();
    }

    @Override
    public void onCancel() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        CameraActivity.this.finish();
    }

}
