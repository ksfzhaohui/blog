package com.lombok;

import lombok.Value;

/**
 * 用在类上，是@Data的不可变形式，相当于为属性添加final声明，只提供getter方法，而不提供setter方法
 * 
 * @author hui.zhao
 *
 */
@Value
public class T_Value {

    private int id;

    public static void main(String[] args) {
        T_Value t = new T_Value(1);
        System.out.println(t.getId());// 无set方法
    }
}
