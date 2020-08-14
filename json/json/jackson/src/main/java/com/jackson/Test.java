package com.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jackson.impl.Apple;
import com.jackson.impl.Banana;

public class Test {

    public static void main(String[] args) throws JsonProcessingException {
        Apple apple = new Apple();
        apple.setName("apple");

        Banana banana = new Banana();
        banana.setName("banana");

        Buy buy = new Buy("online", banana);

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(buy);
        System.out.println("toJSONString : " + jsonString);

        Buy newBuy = mapper.readValue(jsonString, Buy.class);
        banana = (Banana) newBuy.getFruit();
        System.out.println(banana);
    }
}
