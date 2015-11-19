package com.wangyeming.androidcamerademo.Camera.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import com.wangyeming.androidcamerademo.Camera.CameraActivity;
import com.wangyeming.androidcamerademo.Camera2.Camera2Activity;

public class TakePhotoUtil {


    /**
     * 开启拍照
     * @param activity
     */
    public static void startTakePhoto(Activity activity) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(activity, Camera2Activity.class);
            activity.startActivity(intent);
        } else {
            Intent intent = new Intent(activity, CameraActivity.class);
            activity.startActivity(intent);
        }
    }
}
