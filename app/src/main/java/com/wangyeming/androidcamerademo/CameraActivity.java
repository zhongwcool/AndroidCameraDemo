package com.wangyeming.androidcamerademo;

import android.annotation.TargetApi;
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

import com.wangyeming.androidcamerademo.photo.CameraPreview;
import com.wangyeming.androidcamerademo.photo.CameraUtil;
import com.wangyeming.androidcamerademo.photo.PhotoHandler;

import java.util.ArrayList;
import java.util.List;


public class CameraActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private static final String TAG = "CameraActivity";

    /**
     * 相机类
     */
    private Camera mCamera;

    /**
     * 显示预览界面
     */
    private CameraPreview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //全屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera);

        mPreview = new CameraPreview(this);
        FrameLayout vPreview = (FrameLayout) findViewById(R.id.camera_preview);
        vPreview.addView(mPreview);
    }

    @Override
    public void onResume() {
        super.onResume();
        //step1. Detect and Access Camera
        //打开相机的操作延迟到onResume()方法里面去执行，这样可以使得代码更容易重用，还能保持控制流程更为简单。
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            //未检测到系统的相机
            Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG).show();
            Log.d(TAG, "No camera on this device");
            finish();
        } else {
            int cameraId = CameraUtil.findBackFacingCamera();
            if (!CameraUtil.isCameraIdValid(cameraId)) {
                Toast.makeText(this, "No front back camera found.", Toast.LENGTH_LONG).show();
                Log.d(TAG, "No camera on this device");
                finish();
            } else {
                if (safeCameraOpen(cameraId)) {
                    mCamera.startPreview();
                    mPreview.setCamera(mCamera);
                } else {
                    finish();
                }
                findViewById(R.id.camera_take_photo).setOnClickListener(this);
                findViewById(R.id.camera_preview).setOnTouchListener(this);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCameraAndPreview();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.camera_take_photo) {
            mCamera.takePicture(null, null, new PhotoHandler(getApplicationContext()));
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
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
            finish();
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

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return true;
        }

        Camera camera = mCamera;
        camera.cancelAutoFocus();
        Rect focusRect = calculateFocusArea(event.getX(), event.getY());

        Camera.Parameters parameters = camera.getParameters();
        if (parameters.getFocusMode() != Camera.Parameters.FOCUS_MODE_AUTO) {
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
}
