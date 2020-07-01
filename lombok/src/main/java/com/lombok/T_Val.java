package com.lombok;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.val;

/**
 * val：用在局部变量前面，相当于将变量声明为final
 * 
 * @author hui.zhao
 *
 */
public class T_Val {

    public static void main(String[] args) {
        val sets = new HashSet<String>();
        val lists = new ArrayList<String>();
        val maps = new HashMap<String, String>();
        // =>相当于如下
        final Set<String> sets2 = new HashSet<>();
        final List<String> lists2 = new ArrayList<>();
        final Map<String, String> maps2 = new HashMap<>();
    }
}
