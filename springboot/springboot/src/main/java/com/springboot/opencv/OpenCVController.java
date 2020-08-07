package com.springboot.opencv;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpenCVController {
    
    @RequestMapping("/testOpenCV")
    public void testOpenCV() {
        Mat src = Imgcodecs.imread("");
        System.out.println(src);
    }

}
