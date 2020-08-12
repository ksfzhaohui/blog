package com.fastjson;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fastjson.impl.Apple;

public class Test2 {

    public static void main(String[] args) throws Exception {
        ParserConfig.getGlobalInstance().setAutoTypeSupport(false);
        Apple apple = new Apple();
        apple.setName("apple");
        Buy buy = new Buy("online", apple);

        String jsonString2 = JSON.toJSONString(buy, SerializerFeature.WriteClassName);
        System.out.println("toJSONString : " + jsonString2);

        Buy newBuy2 = JSON.parseObject(jsonString2, Buy.class);
        Apple apple2 = (Apple) newBuy2.getFruit();
        System.out.println(apple2);
    }

}
