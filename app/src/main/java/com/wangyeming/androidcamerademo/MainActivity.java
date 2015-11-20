package com.wangyeming.androidcamerademo;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.wangyeming.androidcamerademo.Camera.CameraActivity;
import com.wangyeming.androidcamerademo.Camera2.Camera2Activity;

/**
 * 主页面
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button vCamera = (Button) findViewById(R.id.camera_route_camera);
        Button vCamera2 = (Button) findViewById(R.id.camera_route_camera2);

        vCamera.setOnClickListener(this);
        vCamera2.setOnClickListener(this);

        vCamera2.setEnabled(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.camera_route_camera) {
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
        } else if (id == R.id.camera_route_camera2) {
            Intent intent = new Intent(this, Camera2Activity.class);
            startActivity(intent);
        }
    }
}
