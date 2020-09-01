package com.zh.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zh.service.HiService;

@RestController
public class HiController {
    @Autowired
    HiService hiService;
    @GetMapping("/hi")
    public String sayHi(@RequestParam( defaultValue = "zh",required = false)String name){
        return hiService.sayHi(name);
    }
}
