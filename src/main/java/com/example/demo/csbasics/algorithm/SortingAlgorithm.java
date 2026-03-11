package com.example.demo.csbasics.algorithm;

import java.util.*;

/**
 * 排序算法详解
 *
 * 【排序算法分类】
 * ┌────────────────┬─────────────────────┬─────────────────────┬─────────────┐
 * │ 算法            │ 时间复杂度(平均)     │ 空间复杂度           │ 稳定性       │
 * ├────────────────┼─────────────────────┼─────────────────────┼─────────────┤
 * │ 冒泡排序        │ O(n²)               │ O(1)               │ 稳定        │
 * │ 选择排序        │ O(n²)               │ O(1)               │ 不稳定      │
 * │ 插入排序        │ O(n²)               │ O(1)               │ 稳定        │
 * │ 希尔排序        │ O(n^1.3)            │ O(1)               │ 不稳定      │
 * │ 快速排序        │ O(nlogn)            │ O(logn)            │ 不稳定      │
 * │ 归并排序        │ O(nlogn)            │ O(n)               │ 稳定        │
 * │ 堆排序          │ O(nlogn)            │ O(1)               │ 不稳定      │
 * │ 计数排序        │ O(n+k)              │ O(k)               │ 稳定        │
 * │ 桶排序          │ O(n+k)              │ O(n+k)             │ 稳定        │
 * │ 基数排序        │ O(d(n+k))           │ O(n+k)             │ 稳定        │
 * └────────────────┴─────────────────────┴─────────────────────┴─────────────┘
 *
 * 【稳定性】
 * 稳定：相等元素的相对顺序不变
 * 意义：当需要按多个字段排序时，稳定性很重要
 */
public class SortingAlgorithm {

    // ==================== 冒泡排序 ====================
    /**
     * 原理：相邻元素两两比较，较大者后移
     *
     * 优化：
     * 1. 添加标志位，如果某轮没有交换，说明已有序
     * 2. 记录最后交换位置，下次只需比较到该位置
     */
    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - 1 - i; j++) {
                if (arr[j] > arr[j + 1]) {
                    // 交换
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    swapped = true;
                }
            }
            // 如果没有交换，说明已有序
            if (!swapped) break;
        }
    }

    // ==================== 选择排序 ====================
    /**
     * 原理：每轮选择最小元素放到已排序序列末尾
     *
     * 不稳定原因：交换可能改变相等元素的相对位置
     * 例：[5, 5*, 3] → 第一轮交换后 → [3, 5*, 5]，5和5*顺序变了
     */
    public static void selectionSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (arr[j] < arr[minIdx]) {
                    minIdx = j;
                }
            }
            // 交换
            int temp = arr[i];
            arr[i] = arr[minIdx];
            arr[minIdx] = temp;
        }
    }

    // ==================== 插入排序 ====================
    /**
     * 原理：将元素插入已排序序列的正确位置
     *
     * 特点：
     * 1. 对近乎有序的数组效率高
     * 2. 适合小规模数据
     * 3. Java的Arrays.sort()对小数组使用插入排序
     */
    public static void insertionSort(int[] arr) {
        int n = arr.length;
        for (int i = 1; i < n; i++) {
            int key = arr[i];
            int j = i - 1;
            // 向后移动大于key的元素
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }

    // ==================== 快速排序 ====================
    /**
     * 原理：分治思想
     * 1. 选择基准元素(pivot)
     * 2. 分区：小于pivot放左边，大于pivot放右边
     * 3. 递归处理左右子数组
     *
     * 时间复杂度分析：
     * - 最好/平均：O(nlogn)，每次均匀划分
     * - 最坏：O(n²)，已排序数组且每次选第一个作为pivot
     *
     * 优化：
     * 1. 随机选择pivot或三数取中
     * 2. 小数组使用插入排序
     * 3. 双轴快排（Arrays.sort()）
     */
    public static void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            int pivotIdx = partition(arr, low, high);
            quickSort(arr, low, pivotIdx - 1);
            quickSort(arr, pivotIdx + 1, high);
        }
    }

    // 分区（Lomuto分区方案）
    private static int partition(int[] arr, int low, int high) {
        // 优化：三数取中
        int mid = low + (high - low) / 2;
        if (arr[low] > arr[mid]) swap(arr, low, mid);
        if (arr[low] > arr[high]) swap(arr, low, high);
        if (arr[mid] > arr[high]) swap(arr, mid, high);
        swap(arr, mid, high - 1); // 将中位数放到high-1位置
        int pivot = arr[high - 1];

        int i = low;
        for (int j = low; j < high - 1; j++) {
            if (arr[j] < pivot) {
                swap(arr, i, j);
                i++;
            }
        }
        swap(arr, i, high - 1);
        return i;
    }

    private static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    // ==================== 归并排序 ====================
    /**
     * 原理：分治思想
     * 1. 分：将数组分成两半
     * 2. 治：递归排序两半
     * 3. 合：合并两个有序数组
     *
     * 特点：
     * 1. 稳定排序
     * 2. 时间复杂度稳定O(nlogn)
     * 3. 需要额外空间O(n)
     *
     * 应用：
     * - 外部排序（大文件排序）
     * - 链表排序
     * - 求逆序对数量
     */
    public static void mergeSort(int[] arr, int left, int right, int[] temp) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            // 递归排序左半部分
            mergeSort(arr, left, mid, temp);
            // 递归排序右半部分
            mergeSort(arr, mid + 1, right, temp);
            // 合并
            merge(arr, left, mid, right, temp);
        }
    }

    private static void merge(int[] arr, int left, int mid, int right, int[] temp) {
        int i = left, j = mid + 1, k = 0;

        // 比较并合并
        while (i <= mid && j <= right) {
            if (arr[i] <= arr[j]) {
                temp[k++] = arr[i++];
            } else {
                temp[k++] = arr[j++];
            }
        }

        // 复制剩余元素
        while (i <= mid) {
            temp[k++] = arr[i++];
        }
        while (j <= right) {
            temp[k++] = arr[j++];
        }

        // 复制回原数组
        System.arraycopy(temp, 0, arr, left, k);
    }

    // ==================== 堆排序 ====================
    /**
     * 原理：利用堆的特性进行排序
     * 1. 建堆：将数组建成大顶堆
     * 2. 排序：每次取出堆顶（最大值），放到数组末尾，重新调整堆
     *
     * 堆的性质：
     * - 大顶堆：arr[i] >= arr[2i+1] && arr[i] >= arr[2i+2]
     * - 小顶堆：arr[i] <= arr[2i+1] && arr[i] <= arr[2i+2]
     *
     * 建堆时间复杂度：O(n)
     * 排序时间复杂度：O(nlogn)
     */
    public static void heapSort(int[] arr) {
        int n = arr.length;

        // 1. 建堆（从最后一个非叶子节点开始调整）
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(arr, n, i);
        }

        // 2. 排序
        for (int i = n - 1; i > 0; i--) {
            // 将堆顶（最大值）交换到末尾
            swap(arr, 0, i);
            // 重新调整堆
            heapify(arr, i, 0);
        }
    }

    // 堆化（调整以i为根的子树）
    private static void heapify(int[] arr, int n, int i) {
        int largest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        if (left < n && arr[left] > arr[largest]) {
            largest = left;
        }
        if (right < n && arr[right] > arr[largest]) {
            largest = right;
        }

        if (largest != i) {
            swap(arr, i, largest);
            // 递归调整受影响的子树
            heapify(arr, n, largest);
        }
    }

    // ==================== TopK问题 ====================
    /**
     * 找出数组中第K大的元素
     *
     * 方法1：快速选择（基于快排）
     * 时间复杂度：平均O(n)，最坏O(n²)
     *
     * 方法2：堆（维护大小为K的小顶堆）
     * 时间复杂度：O(nlogk)
     * 适合大数据量的TopK问题
     */
    public static int findKthLargest(int[] arr, int k) {
        // 使用小顶堆
        PriorityQueue<Integer> heap = new PriorityQueue<>();
        for (int num : arr) {
            heap.offer(num);
            if (heap.size() > k) {
                heap.poll(); // 移除最小的
            }
        }
        return heap.peek();
    }

    // ==================== 测试代码 ====================
    public static void main(String[] args) {
        int[] arr = {64, 34, 25, 12, 22, 11, 90};
        System.out.println("排序前：" + Arrays.toString(arr));

        // quickSort(arr, 0, arr.length - 1);
        heapSort(arr);

        System.out.println("排序后：" + Arrays.toString(arr));

        // 测试TopK
        int[] arr2 = {3, 2, 1, 5, 6, 4};
        System.out.println("第2大的元素：" + findKthLargest(arr2, 2));
    }
}
