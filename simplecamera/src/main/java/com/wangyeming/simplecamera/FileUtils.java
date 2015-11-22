package com.wangyeming.simplecamera;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class FileUtils {

    /**
     * 获取保存目录
     * @return
     */
    public static File getOutputFile() {
        File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "CameraAPIDemo");
    }

    /**
     * 创建图片文件
     * @param pictureFileDir
     * @param captureTime
     * @return
     */
    public static File createPhotoFile(File pictureFileDir, long captureTime) {
        String photoFile = "Picture_" + captureTime + ".jpg";
        String filename = pictureFileDir.getPath() + File.separator + photoFile;
        return new File(filename);
    }

    /**
     * 保存字节数据
     * @param bytes
     * @param pictureFile
     * @throws IOException
     */
    public static void saveByteData(byte[] bytes, File pictureFile) throws IOException {
        OutputStream output = null;
        try {
            output = new FileOutputStream(pictureFile);
            output.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }
}
