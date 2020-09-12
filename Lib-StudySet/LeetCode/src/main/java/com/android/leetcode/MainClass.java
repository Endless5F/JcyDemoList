package com.android.leetcode;

import java.util.Arrays;
import java.util.HashMap;

public class MainClass {


    public static void main(String[] args) {
//        runTwoSum();
//        runQuickSort();
        runHeapSort();
//        runFindMedianSortedArrays();
    }

    private static void runHeapSort() {
        HeapSort heapSort = new HeapSort();
        int[] arr = new int[]{1, 3, 5,2, 0,10,6};
        System.out.println(Arrays.toString(arr));
        arr = heapSort.heapSort(arr, arr.length);
        System.out.println(Arrays.toString(arr));
    }

    private static void runQuickSort() {
        QuickSort quickSort = new QuickSort();
        int[] ints = {7, 1, 3, 5, 13, 9, 3, 6, 11};
//        quickSort.quickSort2(ints, 0, ints.length-1);
        quicksort(ints, 0, ints.length-1);
        for (int i = 0; i < ints.length; i++) {
            System.out.println(ints[i]);
        }
    }

    private static void quicksort(int[] array, int startIndex, int endIndex) {
        if (startIndex > endIndex) {
            return;
        }
        // 坑位置
        int index = startIndex;
        // 基准元素
        int base = array[startIndex];
        int left = startIndex, right = endIndex;
        while (left <= right) {
            while (right >= left) {
                if (array[right] < base) {
                    array[left] = array[right];
                    index = right;
                    left++;
                    break;
                }
                right--;
            }

            while (right >= left) {
                if (array[left] > base) {
                    array[right] = array[left];
                    index = left;
                    right--;
                    break;
                }
                left++;
            }
        }
        array[index] = base;
        quicksort(array, startIndex, index - 1);
        quicksort(array, index + 1, endIndex);
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
