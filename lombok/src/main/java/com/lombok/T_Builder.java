package com.lombok;

import lombok.Builder;
import lombok.Data;

/**
 * 用在类、构造器、方法上，为你提供复杂的builder
 * APIs，让你可以像如下方式一样调用Person.builder().name("a").city("nj").build();
 * 
 * @author hui.zhao
 *
 */
@Builder
@Data
public class T_Builder {
    private String name;
    private int age;

    public static void main(String[] args) {
        T_Builder t = T_Builder.builder().age(11).name("test").build();
        System.out.println(t.toString());
    }
}
