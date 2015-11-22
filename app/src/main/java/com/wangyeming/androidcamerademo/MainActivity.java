package com.wangyeming.androidcamerademo;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wangyeming.androidcamerademo.Camera.CameraActivity;
import com.wangyeming.androidcamerademo.NewCamera2.Camera2Activity;

/**
 * 主页面
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_FOR_CAMERA = 1;

    private TextView vPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button vCamera = (Button) findViewById(R.id.camera_route_camera);
        Button vCamera2 = (Button) findViewById(R.id.camera_route_camera2);
        vPath = (TextView) findViewById(R.id.camera_image_path);

        vCamera.setOnClickListener(this);
        vCamera2.setOnClickListener(this);

        vCamera2.setEnabled(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.camera_route_camera) {
            Intent intent = new Intent(this, CameraActivity.class);
            startActivityForResult(intent, REQUEST_CODE_FOR_CAMERA);
        } else if (id == R.id.camera_route_camera2) {
            Intent intent = new Intent(this, Camera2Activity.class);
            startActivityForResult(intent, REQUEST_CODE_FOR_CAMERA);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_FOR_CAMERA) {
            if(resultCode == RESULT_OK) {
                String path = data.getStringExtra(CameraConstant.INTENT_PATH);
                vPath.setText(path);
            } else if(resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "cancel", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show();

            }
        }

    }
}
