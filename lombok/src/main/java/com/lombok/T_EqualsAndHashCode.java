package com.lombok;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(exclude = { "id" }, callSuper = false)
public class T_EqualsAndHashCode {
    private int id;
    private String shape;

    public static void main(String[] args) {
        T_EqualsAndHashCode t1 = new T_EqualsAndHashCode();
        t1.id = 1;
        t1.shape = "t";

        T_EqualsAndHashCode t2 = new T_EqualsAndHashCode();
        t2.id = 2;
        t2.shape = "t";

        System.out.println(t1.equals(t2));// true
    }
}
