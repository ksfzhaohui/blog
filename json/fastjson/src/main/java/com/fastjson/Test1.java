package com.fastjson;

import com.alibaba.fastjson.JSON;
import com.fastjson.impl.Apple;

public class Test1 {

    public static void main(String[] args) {
        Apple apple = new Apple();
        apple.setName("apple");
        Buy buy = new Buy("online", apple);

        String jsonString = JSON.toJSONString(buy);
        System.out.println("toJSONString : " + jsonString);

        Buy newBuy = JSON.parseObject(jsonString, Buy.class);
        Apple newApple = (Apple) newBuy.getFruit();
        System.out.println(newApple);

    }

}
