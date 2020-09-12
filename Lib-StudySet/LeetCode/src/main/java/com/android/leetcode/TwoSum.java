package com.android.leetcode;

import java.util.HashMap;
import java.util.Map;

/**
 * 两数之和
 */
public class TwoSum {
    /**
     * 执行用时 :2 ms, 在所有 Java 提交中击败了99.54%的用户
     * 内存消耗 :42.1 MB, 在所有 Java 提交中击败了5.06%的用户
     *
     * @param nums
     * @param target
     * @return 标签：哈希映射
     * 这道题本身如果通过暴力遍历的话也是很容易解决的，时间复杂度在 O(n2)
     * 由于哈希查找的时间复杂度为 O(1)，所以可以利用哈希容器 map 降低时间复杂度
     * 遍历数组 nums，i 为当前下标，每个值都判断map中是否存在 target-nums[i] 的 key 值
     * 如果存在则找到了两个值，如果不存在则将当前的 (nums[i],i) 存入 map 中，继续遍历直到找到为止
     * 如果最终都没有结果则抛出异常
     * 时间复杂度：O(n)
     * <p>
     * 作者：guanpengchn
     * 链接：https://leetcode-cn.com/problems/two-sum/solution/jie-suan-fa-1-liang-shu-zhi-he-by
     * -guanpengchn/
     * 来源：力扣（LeetCode）
     * 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
     */
    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            if (map.containsKey(target - nums[i])) {
                return new int[]{map.get(target - nums[i]), i};
            }
            map.put(nums[i], i);
        }
        throw new IllegalArgumentException("No two sum solution");
    }

    /**
     * 执行用时 :3 ms, 在所有 Java 提交中击败了94.59%的用户
     * 内存消耗 :42.3 MB, 在所有 Java 提交中击败了5.06%的用户
     *
     * @param nums
     * @param target
     * @return 作者：自己
     */
    public int[] twoSumTime(int[] nums, int target) {
        Map<Integer, Integer> map = new HashMap(nums.length);
        for (int i = 0; i < nums.length; i++) {
            map.put(nums[i], i);
        }

        for (int i = 0; i < nums.length; i++) {
            // 如果目标值 - 遍历当前值，存在于map中，则证明数组中有一个值和当前值之和为target
            Integer index = map.get(target - nums[i]);
            // 若index不为null，并且index不能和当前位置重合，则证明数组中有一个值(非当前值)和当前值之和为target
            // 举例：[3,2,4] target = 6    6 = 3 + 3;  因此不允许重合
            if (index != null && index != i) {
                return new int[]{i, index};
            }
        }
        return new int[]{};
    }

    /**
     * 二分查找
     */
    private int binarySearch(int[] array, int size, int value) {
        int lo = 0;
        int hi = size - 1;

        while (lo <= hi) {
            // 计算中间值，使用无符号右移，而非 /
            int mid = lo + hi >>> 1;
            int midVal = array[mid];
            if (midVal < value) {
                lo = mid + 1;
            } else {
                if (midVal <= value) {
                    return mid;
                }

                hi = mid - 1;
            }
        }

        return ~lo;
    }
}
