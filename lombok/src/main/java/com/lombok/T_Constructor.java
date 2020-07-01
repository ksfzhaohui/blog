package com.lombok;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @NoArgsConstructor, @RequiredArgsConstructor
 * and @AllArgsConstructor：用在类上，自动生成无参构造和使用所有参数的构造函数以及把所有@NonNull属性作为参数的构造函数，如果指定staticName
 * = “of”参数，同时还会生成一个返回类对象的静态工厂方法，比使用构造函数方便很多
 * 
 * @author hui.zhao
 *
 */
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor
public class T_Constructor {

    @NonNull
    private int id;
    @NonNull
    private String shap;
    private int age;

    public static void main(String[] args) {
        new T_Constructor(1, "circle");
        // 使用静态工厂方法
        T_Constructor.of(2, "circle");
        // 无参构造
        new T_Constructor();
        // 包含所有参数
        new T_Constructor(1, "circle", 2);
    }
}
