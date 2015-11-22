package com.wangyeming.simplecamera.interfaces;

/**
 * 拍照回调接口
 */
public interface CameraCallback {

    /**
     * 拍照成功
     * @param photoFile
     */
    void onSuccess(String photoFile, long createTime);

    /**
     * 拍照失败
     */
    void onFail(String errMsg);

    /**
     * 取消拍照
     */
    void onCancel();
}
