package com.fastjson;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;

public class Test3 {

    public static void main(String[] args) {
//        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
//        ParserConfig.getGlobalInstance().setSafeMode(true);
        String jsonString2 = "{\"@type\":\"com.fastjson.DB\",\"dburl\":\"127.0.0.1\"}\r\n";
        System.out.println("toJSONString : " + jsonString2);

        JSONObject obj = JSON.parseObject(jsonString2);
        System.out.println(obj);
    }

}
