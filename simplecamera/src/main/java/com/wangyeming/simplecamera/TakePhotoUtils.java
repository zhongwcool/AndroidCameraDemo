package com.wangyeming.simplecamera;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.wangyeming.simplecamera.Camera.CameraActivity;
import com.wangyeming.simplecamera.Camera2.Camera2Activity;

/**
 *
 */
public class TakePhotoUtils {

    /**
     * 跳转拍照（camera）
     * @param activity
     * @param requestCode
     */
    public static void takePhotoByCamera(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, CameraActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 跳转拍照（camera）
     * @param fragment
     * @param requestCode
     */
    public static void takePhotoByCamera(Fragment fragment, int requestCode) {
        Intent intent = new Intent(fragment.getActivity(), CameraActivity.class);
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * 跳转拍照（camera2）
     * @param activity
     * @param requestCode
     */
    public static void takePhotoByCamera2(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, Camera2Activity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 跳转拍照（camera2）
     * @param fragment
     * @param requestCode
     */
    public static void takePhotoByCamera2(Fragment fragment, int requestCode) {
        Intent intent = new Intent(fragment.getActivity(), Camera2Activity.class);
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * 获取照片路径
     * @param intent
     * @return
     */
    public static String parsePhotoPath(Intent intent) {
        return intent.getStringExtra(CameraIntentConstant.INTENT_PATH);
    }

    /**
     * 获取照片路径
     * @param intent
     * @return
     */
    public static long parseCreateTime(Intent intent) {
        return intent.getLongExtra(CameraIntentConstant.INTENT_CREATE_TIME, 0);
    }
}
