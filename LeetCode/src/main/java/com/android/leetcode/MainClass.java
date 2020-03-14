package com.android.leetcode;

import java.util.HashMap;

public class MainClass {


    public static void main(String[] args) {
//        runTwoSum();
        runFindMedianSortedArrays();
    }

    private static void runTwoSum() {
        TwoSum twoSum = new TwoSum();
        int[] ints = twoSum.twoSumTime(new int[]{0, 4, 3, 0}, 0);
        for (int value : ints) {
            System.out.println(value);
        }
    }

    private static void runFindMedianSortedArrays() {
        int[] num1 = new int[]{1, 2};
        int[] num2 = new int[]{3, 4};
        FindMedianSortedArrays findMedianSortedArrays = new FindMedianSortedArrays();
        double medianSortedArrays = findMedianSortedArrays.findMedianSortedArrays(num1, num2);
        System.out.println(medianSortedArrays);
    }
}
