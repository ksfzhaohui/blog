package com.gson;

public class Buy {

    private String mode;

    private Fruit fruit;

    public Buy() {

    }

    public Buy(String mode, Fruit fruit) {
        super();
        this.mode = mode;
        this.fruit = fruit;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Fruit getFruit() {
        return fruit;
    }

    public void setFruit(Fruit fruit) {
        this.fruit = fruit;
    }

}
