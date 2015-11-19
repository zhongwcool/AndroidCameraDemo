package com.wangyeming.androidcamerademo.photo;

import android.hardware.Camera;
import android.util.Log;

/**
 * Created by Wang Yeming on 2015/11/19.
 */
public final class CameraUtil {

    public static final int INVALID_CAMERA_ID = -1;

    public static int findFrontFacingCamera() {
        int cameraId = INVALID_CAMERA_ID;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                Log.d("CameraUtil", "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    public static int findBackFacingCamera() {
        int cameraId = INVALID_CAMERA_ID;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                Log.d("CameraUtil", "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    public static boolean isCameraIdValid(int cameraId) {
        return cameraId != INVALID_CAMERA_ID;
    }

}
