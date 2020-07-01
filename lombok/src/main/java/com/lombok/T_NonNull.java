package com.lombok;

import lombok.NonNull;

/**
 * 给方法参数增加这个注解会自动在方法内对该参数进行是否为空的校验，如果为空，则抛出NPE（NullPointerException）
 * 
 * @author hui.zhao
 *
 */
public class T_NonNull {
    
    public static void main(String[] args) {
        notNullExample1(null);
    }

    public static void notNullExample1(@NonNull String string) {
        System.out.println(string);
    }

    // =>相当于
    public static void notNullExample2(String string) {
        if (string != null) {
            string.length();
        } else {
            throw new NullPointerException("null");
        }
    }
}
