package com.fastjson.impl;

import com.fastjson.Fruit;

public class Apple implements Fruit {

    private String name;

    public Apple() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        System.err.println("fastjson set name=" + name);
        this.name = name;
    }

}
