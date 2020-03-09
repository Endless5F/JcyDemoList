package com.android.leetcode;

import java.util.HashMap;

public class MainClass {


    public static void main(String[] args) {
        TwoSum twoSum = new TwoSum();
        int[] ints = twoSum.twoSumTime(new int[]{0, 4, 3, 0}, 0);
        for (int value :ints){
            System.out.println(value);
        }
    }
}
