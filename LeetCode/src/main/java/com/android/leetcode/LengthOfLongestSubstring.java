package com.android.leetcode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 无重复字符的最长子串
 */
public class LengthOfLongestSubstring {

    /**
     * 事实上，它可以被进一步优化为仅需要 n 个步骤。我们可以定义字符到索引的映射，
     * 而不是使用集合来判断一个字符是否存在。当我们找到重复的字符时(不需要删除)，我们可以立即跳过该窗口。
     *
     * 即：如果 s[j]s[j] 在 [i, j)[i,j) 范围内有与 j'j′重复的字符，我们不需要逐渐增加 ii 。
     *  我们可以直接跳过 [i，j'][i，j′] 范围内的所有元素，并将 ii 变为 j' + 1j′+1。
     *
     * @param s
     * @return
     *
     * 作者：LeetCode
     * 链接：https://leetcode-cn.com/problems/longest-substring-without-repeating-characters/solution/wu-zhong-fu-zi-fu-de-zui-chang-zi-chuan-by-leetcod/
     * 来源：力扣（LeetCode）
     * 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
     */
    public int lengthOfLongestSubstringStudy(String s) {
        int n = s.length(), ans = 0;
        Map<Character, Integer> map = new HashMap<>(); // current index of character
        // try to extend the range [i, j]
        for (int j = 0, i = 0; j < n; j++) {
            if (map.containsKey(s.charAt(j))) {
                // 若包含当前字符，则 i，直接跳到map集合中重复字符的 '位置+1' 的位置('位置+1'，是由于下标从0开始，而put存储时即存的 下标+1)
                i = Math.max(map.get(s.charAt(j)), i);
            }
            ans = Math.max(ans, j - i + 1);
            map.put(s.charAt(j), j + 1); // 存储 下标+1
        }
        return ans;
    }

    /**
     * 滑动窗口算法：可以用以解决数组/字符串的子元素问题，它可以将嵌套的循环问题，转换为单循环问题，降低时间复杂度。
     * 本方法讲解：
     *  1. 创建一个Set集合，一个left左指针(窗口左位置)，一个right右指针(窗口右位置)。
     *  2. 循环遍历(当前位置为right右指针)：两种情况
     *      若set集合中不包含当前位置(right)的字符，则添加进set集合中并right右移(前进)一位，
     *      若包含当前字符，则set集合删除最左侧的元素(left)并left右移(前进)一位，
     *      注意：此时right并没有+1(即前进)，因此下次遍历还会判断当前set集合中是否存在当前字符(即同上一轮)，若还有(意味着set中重复的字符不在最左边)则继续移除，直到移除干净。
     *  3. 重复2中的动作直到结束
     *
     * 示例1：abcabcbb，当右指针移动到第二个abc时，左指针分别移动删除第一个abc
     * 示例2：xyabcadeg，当右指针移动到第二个a时，左指针移动并删除最左边x元素，由于right没有增加，则继续判断set中是否包含a，此时依旧包括则继续移除y，继续判断继续移除a，最后right+1
     * @param s
     * @return
     */
    public int lengthOfLongestSubstring3(String s) {
        if ("".equals(s)) return 0;
        int maxLength = 0;
        // 左指针，右指针，两个指针之间即是窗口
        int left = 0, right = 0;
        // 用于存储字符和对应的位置
        Set<Character> subMap = new HashSet<>();
        while (left< s.length() && right < s.length()) {
            if (!subMap.contains(s.charAt(right))) {
                // 若不包含当前字符，移动右指针
                subMap.add(s.charAt(right++));
                maxLength = Math.max(maxLength, right - left);
            } else {
                // 若包含当前字符，只需要移除最左边的元素，然后移动左指针，每重复一个移除一个，此时right并没有+1，即下一次遍历还判断刚刚的字符
                subMap.remove(s.charAt(left++));
            }
        }

        return maxLength;
    }

    /**
     * 执行用时 :10 ms, 在所有 Java 提交中击败了66.07%的用户
     * 内存消耗 :41.4 MB, 在所有 Java 提交中击败了5.01%的用户
     * @param s
     * @return
     */
    public int lengthOfLongestSubstring2(String s) {
        if ("".equals(s)) return 0;
        int maxLength = 1;
        int currentLength = 0;
        char[] chars = s.toCharArray();
        // 用于存储字符和对应的位置
        HashMap<Character, Integer> subMap = new HashMap<>();
        int deleteStartIndex = 0;
        for (int i = 0; i < chars.length; i++) {
            // 若subMap中包含当前字符，说明此字符前为一个字串
            if (subMap.containsKey(chars[i])) {
                Integer integer = subMap.get(chars[i]);
                // 将重复字符以及之前的所有字符移除，例如：adcdbef，当前字符为第二个b，则移除ab，因为从第三个开始又会是下一个无重复的字串。
                for (int j = deleteStartIndex; j <= integer; j++) {
                    subMap.remove(chars[j]);
                }
                // 记录下次重复时，移除的开始位置，现在已经进入下一个字串长度计算中(对于本次循环来说，则是当前字串)
                deleteStartIndex = integer + 1;
                // 计算上一个字串和当前字串的最大值
                maxLength = Math.max(maxLength, currentLength);
                // 当前长度需要加上当前字符(的本次循环)
                currentLength = subMap.size() + 1;
            } else {
                currentLength ++;
            }
            subMap.put(chars[i], i);
        }
        return Math.max(maxLength, currentLength);
    }

    /**
     * 第一版(错误的)，忽略了
     * @param s
     * @return
     */
    private static int lengthOfLongestSubstring1(String s) {
        if ("".equals(s)) return 0;
        int maxLength = 1;
        int currentLength = 0;
        char[] chars = s.toCharArray();
        HashMap<Character, Integer> subMap = new HashMap<>();
        for (int i = 0; i < chars.length; i++) {
            if (subMap.containsKey(chars[i])) {
                if (currentLength > maxLength) {
                    maxLength = currentLength;
                }
                subMap.clear();
                currentLength = 1;
            } else {
                currentLength ++;
            }
            subMap.put(chars[i], i);
        }
        if (currentLength > maxLength) {
            maxLength = currentLength;
        }
        return maxLength;
    }
}
