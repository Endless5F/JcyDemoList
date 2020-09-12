package com.android.leetcode;

/**
 * 最长回文子串
 */
public class LongestPalindrome {

    /**
     * 动态规划法：一旦在回文串两端，对称的加上相同元素，那么新形成的字符串也一定是回文串
     *
     * 在动态规划的思想中，总是希望把问题划分成相关联的子问题；然后从最基本的子问题出发来推导较大的子问题，直到所有的子问题都解决。
     * 假设字符串s的长度为length，建立一个length*length的矩阵dp。令 dp[i][j] 表示 S[i] 至 S[j] 所表示的子串是否是回文子串。
     *
     * 直观的动态规划解法，我们首先初始化一字母和二字母的回文，然后找到所有三字母回文，并依此类推…
     *
     * 当 i == j，dp[i][j] 是回文子串（单字符都是回文子串）；
     * 当 j - i < 3，只要 S[i] == S[j]，则 dp[i][j] 是回文子串（如"aa"，“aba”），否则不是；
     * 当 j - i >= 3，如果 S[i] == S[j] && dp[i+1][j-1] ，则 dp[i][j] 是回文子串，否则不是 。
     * @param s
     * @return
     *
     * abaaba
     *
     * 作者：仿写，https://blog.csdn.net/daidaineteasy/article/details/86238047
     */
    public String longestPalindromeStudy(String s) {
        if (s == null || s.length() == 0) return "";
        int max = 0;
        String maxSubString = "";
        boolean[][] dp = new boolean[s.length()][s.length()];
        // i随着j改变，j永远也不会大于i，判断i和j之间字串是否为回文串
        for (int i = 0; i < s.length(); i++) {
            // j <= i，原因：在i到了更大的位置，需要保证之前位置的所有情况的字串都已被判断过是否为回文串，已被同之后的位置进行判断
            for (int j = 0; j <= i; j++) {
                // 若i - j < 3，只需要判断i和j是否相等，而大于3，则需要判断i和j中间的串是否为回文串，例如：abccba。
                // 若此时i = 5为最后一个a，j为第一个a(i永远比j大或者等于j)，那么此时就需要判断bccb是否为回文字串，
                // 而判断bccb是否为回文字串，则需要判断第二位b和第五位b是否相等，然后第三位和第四位是否为回文字串，即第三位是否等于第四位。
                // 而第三位和第四位是否相等是在i = 3时判断的，而bccb是否为回文字串是在i = 4时判断的，并且用到了i = 3时的结果，最后i = 5的abccba用到了i = 4的结果。
                dp[j][i] = (s.charAt(i) == s.charAt(j)) && ((i - j < 3) || dp[j + 1][i - 1]);
                if (dp[j][i] && (i - j + 1) > max) {
                    max = i - j + 1;
                    maxSubString = s.substring(j, i + 1);
                }
            }
        }
        return maxSubString;
    }

    /**
     * 中心扩展法
     *
     * 事实上，只需使用恒定的空间，我们就可以在 O(n^2)的时间内解决这个问题。
     *
     * 我们观察到回文中心的两侧互为镜像。因此，回文可以从它的中心展开，并且只有 2n - 12n−1 个这样的中心。
     *
     * 你可能会问，为什么会是 2n - 12n−1 个，而不是 nn 个中心？
     * 原因在于所含字母数为偶数的回文的中心可以处于两字母之间（例如 \textrm{“abba”}“abba”的中心在两个 \textrm{‘b’}‘b’ 之间）。
     *
     * @param str
     * @return
     * 作者：仿写，https://leetcode-cn.com/problems/longest-palindromic-substring/solution/zui-chang-hui-wen-zi-chuan-by-leetcode/
     */
    private String longestPalindromeStudy2(String str) {
        if (str == null || str.length() == 0) return "";
        int start = 0, end = 0;
        for (int i = 0; i < str.length(); i++) {
            int len1 = expansionCenter(str, i, i);
            int len2 = expansionCenter(str, i, i + 1);
            int max = Math.max(len1, len2);
            if (max > end - start) {
                start = i - ((max - 1) >> 1);
                end = i + (max >> 1);
            }
        }

        return str.substring(start, end + 1);
    }

    private int expansionCenter(String s, int left, int right) {
        // 由于left和right都是字符串中间字符下标，中心扩散就要往两侧扩散，因此left--、right++，还要保证不越界
        while (left >= 0 && right < s.length() && s.charAt(left) == s.charAt(right)) {
            left--;
            right++;
        }
        // -1 的原因是：由于在循环结束前，无论left和right位置字符是否相等或者已超边界，此时left都已--而right都已++，又由于下标从0开始，因此只需要 -1即可
        return right - left - 1;
    }


    /**
     * Manacher 算法
     * @param s
     * @return
     */
    public String longestPalindromeStudy3(String s) {
        return "";
    }

    /**
     * 作者：自己
     * @param s
     * @return
     */
    public String longestPalindrome(String s) {
        char[] chars = s.toCharArray();
        int maxLength = 0;
        String maxSubString = "";
        // i代表开头，从左往右移动
        for (int i = 0; i < chars.length; i++) {
            // j代表结尾，从右往左移动，当j<i时停止重复遍历
            for (int left = i, j = chars.length - 1, right = j; j >= i; left = i, j--, right = j) {
                int currentLength = right - left + 1;
                // 指定位置，判断是否是回文子串
                if (isPalindromeString(chars, left, right)) {
                    if (currentLength > maxLength) {
                        maxSubString = s.substring(i, j + 1);
                        maxLength = Math.max(maxLength, currentLength);
                    }
                }
            }
        }
        System.out.println(maxLength);
        return maxSubString;
    }

    /**
     * 该字符数组的子数组是否是回文字符数组
     *
     * @param chars
     * @param left
     * @param right
     * @return
     */
    private boolean isPalindromeString(char[] chars, int left, int right) {
        boolean isPalindrome = true;
        while (left < right) {
            if (chars[left++] != chars[right--]) {
                isPalindrome = false;
                break;
            }
        }
        return isPalindrome;
    }

    private boolean isPalindrome(String str) {
        String s = new StringBuilder(str).reverse().toString();
        return s.equals(str);
    }
}
