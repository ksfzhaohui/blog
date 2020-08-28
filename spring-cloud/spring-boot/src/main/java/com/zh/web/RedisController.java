package com.zh.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zh.dao.RedisDao;

import io.swagger.annotations.ApiOperation;

@RestController
public class RedisController {
    
    @Autowired
    private RedisDao redisDao;

    @GetMapping("/redis-set")
    @ApiOperation("往redis写数据")
    public String set(String key,String value) {
        redisDao.setKey(key, value);
        return "OK";
    }
    
    @GetMapping("/redis-get")
    @ApiOperation("往redis读数据")
    public String get(String key) {
        return redisDao.getValue(key);
    }
}
