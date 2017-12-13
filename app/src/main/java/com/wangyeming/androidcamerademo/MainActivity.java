package com.wangyeming.androidcamerademo;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.wangyeming.simplecamera.TakePhotoUtils;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * 主页面
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_CODE_FOR_CAMERA = 1;
    private static final int RC_CAMERA = 123;
    private String[] mPerms = {
            Manifest.permission.CAMERA,
    };

    private TextView vPath;
    private ImageView vPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button vCamera = (Button) findViewById(R.id.camera_route_camera);
        Button vCamera2 = (Button) findViewById(R.id.camera_route_camera2);
        vPath = (TextView) findViewById(R.id.camera_image_path);
        vPreview = (ImageView) findViewById(R.id.camera_image_preview);

        vCamera.setOnClickListener(this);
        vCamera2.setOnClickListener(this);

        vCamera2.setEnabled(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    @Override
    public void onClick(View v) {
        if (hasRequiredPermission()) {
            int id = v.getId();
            if (id == R.id.camera_route_camera) {
                TakePhotoUtils.takePhotoByCamera(this, REQUEST_CODE_FOR_CAMERA);
            } else if (id == R.id.camera_route_camera2) {
                TakePhotoUtils.takePhotoByCamera2(this, REQUEST_CODE_FOR_CAMERA);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_FOR_CAMERA) {
            if(resultCode == RESULT_OK) {
                String path = TakePhotoUtils.parsePhotoPath(data);
                long createTime = TakePhotoUtils.parseCreateTime(data);
                vPath.setText(path + " createTime " + createTime);
                Glide.with(this)
                        .load(path)
                        .fitCenter()
                        .thumbnail(0.1f)
                        .into(vPreview);
            } else if(resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "cancel", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show();

            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @AfterPermissionGranted(RC_CAMERA)
    private boolean hasRequiredPermission() {

        if (EasyPermissions.hasPermissions(this, mPerms)) {
            // Already have permission, do the thing
            return true;
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.camera_rationale),
                    RC_CAMERA,
                    mPerms
            );
            return false;
        }
    }
}
