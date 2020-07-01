package com.lombok;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import lombok.Cleanup;

/**
 * 自动管理资源，用在局部变量之前，在当前变量范围内即将执行完毕退出之前会自动清理资源，自动生成try-finally这样的代码来关闭流
 * 
 * @author hui.zhao
 *
 */
public class T_Cleanup {

    public static void main(String[] args) {
        try {
            @Cleanup
            InputStream inputStream1 = new FileInputStream(args[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // =>相当于
        InputStream inputStream2 = null;
        try {
            inputStream2 = new FileInputStream(args[0]);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inputStream2 != null) {
                try {
                    inputStream2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
