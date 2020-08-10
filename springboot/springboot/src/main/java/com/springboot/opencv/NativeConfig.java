package com.springboot.opencv;

import org.springframework.context.annotation.Configuration;

@Configuration
public class NativeConfig {

    static {
        try {
            NativeLoader.loader();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
