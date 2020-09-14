package com.data.algorithm.leetcode.动态规划;

import java.util.ArrayList;
import java.util.List;

/**
 * 假设你正在爬楼梯。需要 n 阶你才能到达楼顶。 每次你可以爬 1 或 2 个台阶。你有多少种不同的方法可以爬到楼顶呢
 * 
 * @author hui.zhao
 *
 */
public class 爬楼梯 {

    private static int count(List<Integer> types, int number) {
        int count = 0;
        for (int type : types) {
            if (number == type) {
                count++;
            }
            if (number < type) {
                continue;
            }
            count = count + count(types, number - type);
        }
        return count;
    }

    public static void main(String[] args) {
        List<Integer> types = new ArrayList<Integer>();
        types.add(1);
        types.add(2);

        int step = 10;
        System.out.println(count(types, step));
    }

}
