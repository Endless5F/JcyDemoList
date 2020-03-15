package com.android.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * 寻找两个有序数组的中位数
 *
 * 给定两个大小为 m 和 n 的有序数组 nums1 和 nums2。
 * 请你找出这两个有序数组的中位数，并且要求算法的时间复杂度为 O(log(m + n))。
 * 你可以假设 nums1 和 nums2 不会同时为空。
 *
 */
public class FindMedianSortedArrays {

    /**
     * 根据官方示例，自己实现一遍
     * @param nums1
     * @param nums2
     * @return
     */
    public double findMedianSortedArraysStudy(int[] nums1, int[] nums2) {
        int length1 = nums1.length;
        int length2 = nums2.length;
        // 中位数有两种情况数组和为奇数或者偶数
        // 可以通过+1/2 +2/2的方式将奇数中位数和偶数中位数计算方式统一
        // 注：left 和 right 代表的是数组中的位置，而非下标
        int left = (length1 + length2 + 1) / 2;
        int right = (length1 + length2 + 2) / 2;
        return (getKth(nums1, 0, length1 -1, nums2, 0, length2 -1, left) + getKth(nums1, 0, length1 -1, nums2, 0, length2 -1, right)) * 0.5;
    }

    /**
     * 获取第k大小的值
     *
     * @param num1   第一个数组
     * @param start1 第一个数组开始位置
     * @param end1   第一个数组结束位置
     * @param num2   第二个数组
     * @param start2 第二个数组开始位置
     * @param end2   第二个数组结束位置
     * @param k      获取第k大小的值
     * @return
     */
    private int getKth(int[] num1, int start1, int end1, int[] num2, int start2, int end2, int k) {
        // +1 的原因：由于k，代表第k个值，这个在我们理解中一般都是从1开始的，而数组中一般从0开始，为了统一则+1处理
        int len1 = end1 - start1 + 1;
        int len2 = end2 - start2 + 1;
        // 保证len1 < len2，若len1 > len2，则将num1和num2传参颠倒一下
        if (len1 > len2) return getKth(num2, start2,end2, num1, start1, end1, k);
        if (len1 == 0) return num2[start2 + k - 1];

        if (k == 1) return Math.min(num1[start1], num2[start2]);

        // 若其中一个数组长度本身就比k值小，则直接取数组中最后一位
        int i = start1 + Math.min(len1, k / 2) - 1;
        int j = start2 + Math.min(len2, k / 2) - 1;

        if (num1[i] > num2[j]) {
            // 每次递归，通过i和j的比较，都会出现不合适的数据，而这些数据都需要舍弃，这些数据都是比第k个值还要小的值，因此现在再次递归则需要找 k-舍弃数据的个数
            // 例如：num1：[1,3,5,7] num2：[1,2,3,4,5,6,7,8,9,10] 数组和是14，而中位数则是 7和8平均值，以7为例
            // 1. 两个数组分别取出 k=7/2位的值(从1开始，即第7/2-1位)，进行比较，发现 num1的5比num2的3大，而num1和num2都是有序数组，因此num2的前三位肯定不是第k位小的值，因此舍弃num2的前三位，
            // 2. 再次求num1：[1,3,5,7] num2：[4,5,6,7,8,9,10]两个数组第 7-3位最小的值(由于已经舍弃比第7位还小的3个值，因此在此时第4小值就是原两数组第7小值)，继续分别取出两新数组(7-3)/2位值的大小
            // 3. 两新数组(7-3)/2位值的大小，进行比较，小则继续舍弃，重复第2步
            // 4. 迭代停止条件：k==1。若其中一个数组长度本身就比k值小，则直接取数组中最后一位
            return getKth(num1, start1, end1, num2, j + 1, end2,k - (j - start2 + 1));
        } else {
            return getKth(num1, i + 1, end1, num2, start2, end2,k - (i - start1 + 1));
        }
    }

    /**
     * 执行用时 :3 ms, 在所有 Java 提交中击败了90.35%的用户
     * 内存消耗 :42.2 MB, 在所有 Java 提交中击败了96.33%的用户
     *
     * @param nums1
     * @param nums2
     * @return 作者：自己
     */
    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        if (nums1 == null) {
            return nums2.length % 2 != 0 ? nums2[nums2.length / 2] :
                    nums2[nums2.length / 2 - 1] + nums1[nums1.length / 2];
        } else if (nums2 == null) {
            return nums1.length % 2 != 0 ? nums1[nums1.length / 2] :
                    nums1[nums1.length / 2 - 1] + nums1[nums1.length / 2];
        }
        int x = 0, y = 0;
        int lenght = nums1.length + nums2.length;
        List<Integer> allNum = new ArrayList<>();
        // 遍历只需要获取到中位数的位置即可停止，x代表num1的指针，y代表num2的指针
        while (x + y <= lenght / 2) {
            // 注意长度判断，防止数组越界
            if (nums1.length > x && (nums2.length <= y || nums1[x] < nums2[y])) {
                allNum.add(nums1[x++]);
            } else {
                allNum.add(nums2[y++]);
            }
        }
        int size = allNum.size();
        if (lenght % 2 == 0) {
            // 注意需要除以 2d，举例：[1,2]和[3,4]若除以2，则实际是(2+3)/2=2.0，而(2+3)/2d=2.5
            return (allNum.get(size - 2) + allNum.get(size - 1)) / 2d;
        } else {
            return allNum.get(size - 1);
        }
    }
}
