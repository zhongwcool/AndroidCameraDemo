package com.wangyeming.simplecamera.interfaces;

/**
 * 处理捕获图片的抽象类
 */
public abstract class CapturedImageHandle {

    public abstract void savePhoto();

    public abstract void retryTakingPhoto();

    public abstract void cancelCamera();
}
