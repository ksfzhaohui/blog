package com.lombok;

import lombok.Getter;

/**
 * 可以替代经典的Double Check Lock样板代码
 * @author hui.zhao
 *
 */
public abstract class T_Getter_Lazy {

    @Getter(lazy = true)
    private final double[] cached = expensive();

    private double[] expensive() {
        double[] result = new double[1000000];
        for (int i = 0; i < result.length; i++) {
            result[i] = Math.asin(i);
        }
        return result;
    }
}
