package com.android.leetcode;

class ListNode {
    int val;
    ListNode next;
    ListNode(int x) { val = x; }
}

/**
 * 两数相加
 */
public class AddTwoNumbers {

    /**
     * 执行用时 :3 ms, 在所有 Java 提交中击败了39.78%的用户
     * 内存消耗 :41.2 MB, 在所有 Java 提交中击败了92.09%的用户
     * @param l1
     * @param l2
     * @return
     * 作者：自己
     * 自我评价：思路虽然感觉还可以，但是实现起来逻辑太多，思路需要更好，逻辑需要更简洁
     */
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        int quotient = 0;
        int x = 0;

        ListNode next1 = l1;
        ListNode next2 = l2;
        ListNode startNode = new ListNode(0);
        ListNode newNext = startNode;
        while (next1 != null && next2 != null) {
            // 两链表节点相加，再加上一个节点相加的进位(两数之和大于10，则需要往上进 1)
            int add = next1.val + next2.val + quotient;
            if (add >= 10) {
                x = add % 10; // 实际该位置结果
                quotient = 1; // 需要进位的值(取名商的原因：quotient = (两数之和+进位) / 10)
            } else {
                x = add;
                quotient = 0;
            }
            newNext.next = new ListNode(x);
            next1 = next1.next;
            next2 = next2.next;
            newNext = newNext.next;
        }

        while (next1 != null) {
            int xNext1 = next1.val + quotient;
            if (xNext1 >= 10) {
                x = xNext1 % 10;
                quotient = 1;
            } else {
                x = xNext1;
                quotient = 0;
            }
            newNext.next = new ListNode(x);
            next1 = next1.next;
            newNext = newNext.next;
        }

        while (next2 != null) {
            int xNext2 = next2.val + quotient;
            if (xNext2 >= 10) {
                x = xNext2 % 10;
                quotient = 1;
            } else {
                x = xNext2;
                quotient = 0;
            }
            newNext.next = new ListNode(x);
            next2 = next2.next;
            newNext = newNext.next;
        }

        // 若最后还存在进位，则直接添加到链表最后一位，例如：[5]和[9,9]
        if(quotient == 1) {
            newNext.next = new ListNode(1);
        }
        return startNode.next;
    }

    /**
     * 执行用时 :2 ms, 在所有 Java 提交中击败了99.97%的用户
     * 内存消耗 :41.7 MB, 在所有 Java 提交中击败了91.82%的用户
     * @param l1
     * @param l2
     * @return
     * 作者：LeetCode
     * 链接：https://leetcode-cn.com/problems/add-two-numbers/solution/liang-shu-xiang-jia-by-leetcode/
     * 来源：力扣（LeetCode）
     * 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
     */
    public ListNode addTwoNumbersStudy(ListNode l1, ListNode l2) {
        ListNode dummyHead = new ListNode(0);
        ListNode p = l1, q = l2, curr = dummyHead;
        int carry = 0;
        while (p != null || q != null) {
            int x = (p != null) ? p.val : 0;
            int y = (q != null) ? q.val : 0;
            int sum = carry + x + y;
            carry = sum / 10;
            curr.next = new ListNode(sum % 10);
            curr = curr.next;
            if (p != null) p = p.next;
            if (q != null) q = q.next;
        }
        if (carry > 0) {
            curr.next = new ListNode(carry);
        }
        return dummyHead.next;
    }
}
