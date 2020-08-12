package com.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.gson.impl.Apple;
import com.gson.impl.Banana;

/**
 * 使用 RuntimeTypeAdapterFactory 类解决多态问题 ，需要使用gson-extras
 * 
 * @author hui.zhao
 *
 */
public class Test {

    public static void main(String[] args) {
        Apple apple = new Apple();
        apple.setName("apple");

        Buy buy = new Buy("online", apple);

        RuntimeTypeAdapterFactory<Fruit> typeFactory = RuntimeTypeAdapterFactory.of(Fruit.class, "id")
                .registerSubtype(Apple.class, "apple").registerSubtype(Banana.class, "banana");

        Gson gson = new GsonBuilder().registerTypeAdapterFactory(typeFactory).create();

        String jsonStr = gson.toJson(buy);
        System.out.println("jsonstr:" + jsonStr);

        Buy newBuy = gson.fromJson(jsonStr, Buy.class);
        Apple newApple = (Apple) newBuy.getFruit();
        System.out.println(newApple);
    }
}
