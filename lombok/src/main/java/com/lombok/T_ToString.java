package com.lombok;

import lombok.ToString;

/**
 * @ToString：用在类上，可以自动覆写toString方法，当然还可以加其他参数，例如@ToString(exclude=”id”)排除id属性，或者@ToString(callSuper=true, includeFieldNames=true)调用父类的toString方法，包含所有属性
 * @author hui.zhao
 *
 */
@ToString(exclude = "id", callSuper = true, includeFieldNames = true)
public class T_ToString {
    private int id;
    private String name;
    private int age;

    public static void main(String[] args) {
        // 输出LombokDemo(super=LombokDemo@48524010, name=null, age=0)
        System.out.println(new T_ToString());
    }
}
