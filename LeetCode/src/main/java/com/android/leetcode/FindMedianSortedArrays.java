package com.android.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * 寻找两个有序数组的中位数
 */
public class FindMedianSortedArrays {
    /**
     * 执行用时 :3 ms, 在所有 Java 提交中击败了90.35%的用户
     * 内存消耗 :42.2 MB, 在所有 Java 提交中击败了96.33%的用户
     *
     * @param nums1
     * @param nums2
     * @return
     *
     * 作者：自己
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
