package com.wangyeming.simplecamera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.wangyeming.simplecamera.interfaces.CapturedImageHandle;

/**
 * 拍照后的底部操作栏
 */
public class HandleCaptureView extends LinearLayout implements View.OnClickListener {

    private Button vRetry;
    private Button vSave;
    private Button vCancel;
    private CapturedImageHandle mCapturedImageHandle;

    public HandleCaptureView(Context context) {
        super(context);
        init(context);
    }

    public HandleCaptureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.camera_comp_handle_capture_view, this);
        vRetry = (Button) view.findViewById(R.id.camera_retry_photo);
        vSave = (Button) view.findViewById(R.id.camera_save_photo);
        vCancel = (Button) view.findViewById(R.id.camera_cancel);
        vRetry.setOnClickListener(this);
        vSave.setOnClickListener(this);
        vCancel.setOnClickListener(this);
    }

    public void setCapturedImageCallback(CapturedImageHandle capturedImageHandle) {
        mCapturedImageHandle = capturedImageHandle;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(mCapturedImageHandle != null) {
            if(id == R.id.camera_retry_photo) {
                mCapturedImageHandle.retryTakingPhoto();
            } else if(id == R.id.camera_save_photo) {
                mCapturedImageHandle.savePhoto();
            } else if(id == R.id.camera_cancel) {
                mCapturedImageHandle.cancelCamera();
            }
        }
    }
}
