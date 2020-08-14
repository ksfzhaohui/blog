package com.fastjson;

import com.alibaba.fastjson.JSON;
import com.fastjson.impl.Apple;

public class Test1 {

    public static void main(String[] args) {
        Apple apple = new Apple();
        apple.setName("apple");
        //多态
        Buy buy = new Buy("online", apple);
        
        //非多态
        BuyApple buyApple = new BuyApple();
        buyApple.setMode("online");
        buyApple.setApple(apple);

        String jsonString = JSON.toJSONString(buy);
        System.out.println("toJSONString : " + jsonString);
        
        String jsonString2 = JSON.toJSONString(buyApple);
        System.out.println("toJSONString : " + jsonString2);
        
        BuyApple newBuyApple = JSON.parseObject(jsonString2, BuyApple.class);
        Apple newApple2 = newBuyApple.getApple();
        System.out.println(newApple2);

        Buy newBuy = JSON.parseObject(jsonString, Buy.class);
        Apple newApple = (Apple) newBuy.getFruit();
        System.out.println(newApple);

    }

}
