package com.zyh.pddassistant;

import android.content.ContextWrapper;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Utils {
    private static final String TAG = "Utils";
    public interface Action{
        void success();
        void failure();
    }
    public static void writeToFile(File file, String content) {
        Log.d(TAG, "writeToFile: "+file.getAbsolutePath());
        try {
            InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            // 1.建立通道对象
            FileOutputStream fos = new FileOutputStream(file,true);
            // 2.定义存储空间
            byte[] buffer = new byte[inputStream.available()];
            // 3.开始读文件
            int lenght = 0;
            while ((lenght = inputStream.read(buffer)) != -1) {// 循环从输入流读取buffer字节
                // 将Buffer中的数据写到outputStream对象中
                fos.write(buffer, 0, lenght);
            }
            fos.flush();// 刷新缓冲区
            // 4.关闭流
            fos.close();
            inputStream.close();
        } catch (Exception e) {
            Log.d(TAG, "writeToFile: Error!");
            e.printStackTrace();
        }
    }
    public static void cleanFile(File file, Action action) {
        Log.d(TAG, "writeToFile: "+file.getAbsolutePath());
        try {
            InputStream inputStream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
            // 1.建立通道对象
            FileOutputStream fos = new FileOutputStream(file);
            // 2.定义存储空间
            byte[] buffer = new byte[inputStream.available()];
            // 3.开始读文件
            int lenght = 0;
            while ((lenght = inputStream.read(buffer)) != -1) {// 循环从输入流读取buffer字节
                // 将Buffer中的数据写到outputStream对象中
                fos.write(buffer, 0, lenght);
            }
            fos.flush();// 刷新缓冲区
            // 4.关闭流
            fos.close();
            inputStream.close();
            action.success();
        } catch (Exception e) {
            Log.d(TAG, "writeToFile: Error!");
            action.failure();
            e.printStackTrace();
        }
    }
}
