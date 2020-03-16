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
     * 当 i == j，dp[i][j] 是回文子串（单字符都是回文子串）；
     * 当 j - i < 3，只要 S[i] == S[j]，则 dp[i][j] 是回文子串（如"aa"，“aba”），否则不是；
     * 当 j - i >= 3，如果 S[i] == S[j] && dp[i+1][j-1] ，则 dp[i][j] 是回文子串，否则不是 。
     * @param s
     * @return
     *
     * abaaba
     *
     * 作者：https://blog.csdn.net/daidaineteasy/article/details/86238047
     */
    public String longestPalindromeStudy(String s) {
        if (s == null || s.length() == 0) return "";
        int maxLength = 0;
        String maxSubString = "";
        boolean[][] dp = new boolean[s.length()][s.length()];
        for (int j = 0; j < s.length(); j++) {
            for (int i = 0; i <= j; i++) {
                dp[i][j] = s.charAt(i) == s.charAt(j) && ((j - i < 3) || (dp[i + 1][j - 1]));
                if (dp[i][j] && (j - i + 1 > maxLength)) {
                    maxLength = j - i + 1;
                    maxSubString = s.substring(i, j + 1);
                }
            }
        }
        System.out.println(maxLength);
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
     * @param s
     * @return
     * 作者：LeetCode
     * 链接：https://leetcode-cn.com/problems/longest-palindromic-substring/solution/zui-chang-hui-wen-zi-chuan-by-leetcode/
     * 来源：力扣（LeetCode）
     * 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
     */
    public String longestPalindromeStudy2(String s) {
        if (s == null || s.length() < 1) return "";
        int start = 0, end = 0;
        for (int i = 0; i < s.length(); i++) {
            int len1 = expandAroundCenter(s, i, i);
            int len2 = expandAroundCenter(s, i, i + 1);
            int len = Math.max(len1, len2);
            if (len > end - start) {
                start = i - (len - 1) / 2;
                end = i + len / 2;
            }
        }
        return s.substring(start, end + 1);
    }

    private int expandAroundCenter(String s, int left, int right) {
        int L = left, R = right;
        while (L >= 0 && R < s.length() && s.charAt(L) == s.charAt(R)) {
            L--;
            R++;
        }
        return R - L - 1;
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
