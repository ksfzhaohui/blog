package com.lombok;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import lombok.SneakyThrows;

/**
 * 自动抛受检异常，而无需显式在方法上使用throws语句
 * @author hui.zhao
 *
 */
public class T_SneakyThrows {

    @SneakyThrows()
    public void read1() {
        InputStream inputStream = new FileInputStream("");
    }

    @SneakyThrows
    public void write1() {
        throw new UnsupportedEncodingException();
    }

    // 相当于
    public void read2() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream("");
    }

    public void write2() throws UnsupportedEncodingException {
        throw new UnsupportedEncodingException();
    }
}
