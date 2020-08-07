package com.springboot.opencv;

import org.opencv.core.Core;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NativeConfig {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
}
