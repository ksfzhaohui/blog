package com.lombok;

import lombok.Getter;
import lombok.Setter;

public class T_Getter {

    @Setter
    @Getter
    private int id;
    @Setter
    @Getter
    private String shap;

    public static void main(String[] args) {
        T_Getter getter = new T_Getter();
        getter.setId(1);
        System.out.println(getter.getId());
    }
}
