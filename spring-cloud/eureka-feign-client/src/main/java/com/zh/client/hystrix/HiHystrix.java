package com.zh.client.hystrix;

import org.springframework.stereotype.Component;

import com.zh.client.EurekaClientFeign;

@Component
public class HiHystrix implements EurekaClientFeign {
    @Override
    public String sayHiFromClientEureka(String name) {
           return "hi,"+name+",sorry,error!";
    }
}
