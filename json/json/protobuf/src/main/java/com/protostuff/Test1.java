package com.protostuff;

import java.util.Arrays;

import com.protostuff.impl.Apple;
import com.protostuff.impl.Banana;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class Test1 {

    public static void main(String[] args) throws Exception {
        write();
        read();
    }
    
    private static void read() {
        byte data[]= {10, 6, 111, 110, 108, 105, 110, 101, 19, -6, 7, 23, 99, 111, 109, 46, 112, 114, 111, 116, 111, 98, 117, 102, 46, 105, 109, 112, 108, 46, 65, 112, 112, 108, 101, 10, 5, 97, 112, 112, 108, 101, 20};
        Schema<Buy> schema2 = RuntimeSchema.getSchema(Buy.class);
        Buy newBuy = schema2.newMessage();
        ProtostuffIOUtil.mergeFrom(data, newBuy, schema2);
        Apple newApple = (Apple) newBuy.getFruit();
        System.out.println(newApple);
    }
    
    private static void write() {
        Apple apple = new Apple();
        apple.setName("apple");

        Banana banana = new Banana();
        banana.setName("banana");

        Buy buy = new Buy("online", apple);

        Schema<Buy> schema = RuntimeSchema.getSchema(Buy.class);
        LinkedBuffer buffer = LinkedBuffer.allocate(1024);
        byte[] data = ProtostuffIOUtil.toByteArray(buy, schema, buffer);
        System.out.println(Arrays.toString(data));
        
        String clazz = "com.protobuf.impl.Apple";
        System.out.println(Arrays.toString(clazz.getBytes()));
        
        System.out.println(Arrays.toString("apple".getBytes()));
    }
}
