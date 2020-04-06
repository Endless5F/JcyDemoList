package com.android.leetcode;

public class HeapSort {
    public int[] heapSort(int[] array, int length) {
        for (int i = (length - 2) / 2; i >= 0; i--) {
            array = downAdjust(array, i, length);
        }

        for (int i = length - 1; i >= 1; i--) {
            int temp = array[i];
            array[i] = array[0];
            array[0] = temp;
            array = downAdjust(array, 0, i);
        }
        return array;
    }

    /**
     * 下沉操作，构建二叉堆
     * <p>
     * 小堆
     */
    public int[] downAdjust(int[] array, int parent, int length) {
        int temp = array[parent];
        int child = 2 * parent + 1;
        while (child < length) {
            if (child + 1 < length && array[child] > array[child + 1]) {
                child++;
            }

            if (array[child] >= temp) {
                break;
            }

            array[parent] = array[child];
            parent = child;
            child = 2 * parent + 1;
        }
        array[parent] = temp;
        return array;
    }

    /**上浮操作，对插入的节点进行上浮
     *
     * @param arr
     * @param length ：表示二叉堆的长度
     */
    public static int[] upAdjust(int arr[], int length){
        //标记插入的节点
        int child = length - 1;
        //其父亲节点
        int parent = (child - 1)/2;
        //把插入的节点临时保存起来
        int temp = arr[child];

        //进行上浮
        while (child > 0 && temp < arr[parent]) {
            //其实不用进行每次都进行交换，单向赋值就可以了
            //当temp找到正确的位置之后，我们再把temp的值赋给这个节点
            arr[child] = arr[parent];
            child = parent;
            parent = (child - 1) / 2;
        }
        //退出循环代表找到正确的位置
        arr[child] = temp;
        return arr;
    }
}
