package com.example.demo.csbasics.datastructure;

import java.util.*;

/**
 * 数据结构核心知识点
 *
 * 【复杂数据结构】
 * 1. 链表（单向、双向、循环）
 * 2. 二叉树（遍历、BST、AVL）
 * 3. B+树（MySQL索引结构）
 * 4. 跳表（Redis有序集合）
 */
public class AdvancedDataStructures {

    // ==================== 链表 ====================

    /**
     * 单向链表节点
     */
    static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }
    }

    /**
     * 双向链表节点
     */
    static class DoublyListNode {
        int val;
        DoublyListNode prev;
        DoublyListNode next;

        DoublyListNode(int val) {
            this.val = val;
        }
    }

    /**
     * 常见链表操作
     */
    static class LinkedListOperations {

        /**
         * 反转链表
         */
        public static ListNode reverse(ListNode head) {
            ListNode prev = null;
            ListNode curr = head;

            while (curr != null) {
                ListNode next = curr.next;
                curr.next = prev;
                prev = curr;
                curr = next;
            }

            return prev;
        }

        /**
         * 检测环（快慢指针）
         */
        public static boolean hasCycle(ListNode head) {
            if (head == null || head.next == null) {
                return false;
            }

            ListNode slow = head;
            ListNode fast = head.next;

            while (slow != fast) {
                if (fast == null || fast.next == null) {
                    return false;
                }
                slow = slow.next;
                fast = fast.next.next;
            }

            return true;
        }

        /**
         * 找到链表中点
         */
        public static ListNode findMiddle(ListNode head) {
            ListNode slow = head;
            ListNode fast = head;

            while (fast != null && fast.next != null) {
                slow = slow.next;
                fast = fast.next.next;
            }

            return slow;
        }

        /**
         * 合并两个有序链表
         */
        public static ListNode mergeTwoLists(ListNode l1, ListNode l2) {
            ListNode dummy = new ListNode(0);
            ListNode curr = dummy;

            while (l1 != null && l2 != null) {
                if (l1.val <= l2.val) {
                    curr.next = l1;
                    l1 = l1.next;
                } else {
                    curr.next = l2;
                    l2 = l2.next;
                }
                curr = curr.next;
            }

            curr.next = l1 != null ? l1 : l2;

            return dummy.next;
        }

        /**
         * 删除倒数第N个节点
         */
        public static ListNode removeNthFromEnd(ListNode head, int n) {
            ListNode dummy = new ListNode(0);
            dummy.next = head;

            ListNode fast = dummy;
            ListNode slow = dummy;

            // fast先走n步
            for (int i = 0; i <= n; i++) {
                fast = fast.next;
            }

            // 同时移动
            while (fast != null) {
                fast = fast.next;
                slow = slow.next;
            }

            // 删除节点
            slow.next = slow.next.next;

            return dummy.next;
        }
    }

    // ==================== 二叉树 ====================

    /**
     * 二叉树节点
     */
    static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode(int val) {
            this.val = val;
        }
    }

    /**
     * 二叉树遍历
     */
    static class TreeTraversal {

        /**
         * 前序遍历（递归）：根 → 左 → 右
         */
        public static void preOrderRecursive(TreeNode root, List<Integer> result) {
            if (root == null) return;
            result.add(root.val);
            preOrderRecursive(root.left, result);
            preOrderRecursive(root.right, result);
        }

        /**
         * 前序遍历（迭代）
         */
        public static List<Integer> preOrderIterative(TreeNode root) {
            List<Integer> result = new ArrayList<>();
            if (root == null) return result;

            java.util.Stack<TreeNode> stack = new java.util.Stack<>();
            stack.push(root);

            while (!stack.isEmpty()) {
                TreeNode node = stack.pop();
                result.add(node.val);

                if (node.right != null) stack.push(node.right);
                if (node.left != null) stack.push(node.left);
            }

            return result;
        }

        /**
         * 中序遍历（递归）：左 → 根 → 右
         */
        public static void inOrderRecursive(TreeNode root, List<Integer> result) {
            if (root == null) return;
            inOrderRecursive(root.left, result);
            result.add(root.val);
            inOrderRecursive(root.right, result);
        }

        /**
         * 中序遍历（迭代）
         */
        public static List<Integer> inOrderIterative(TreeNode root) {
            List<Integer> result = new ArrayList<>();
            java.util.Stack<TreeNode> stack = new java.util.Stack<>();
            TreeNode curr = root;

            while (curr != null || !stack.isEmpty()) {
                while (curr != null) {
                    stack.push(curr);
                    curr = curr.left;
                }

                curr = stack.pop();
                result.add(curr.val);
                curr = curr.right;
            }

            return result;
        }

        /**
         * 后序遍历（递归）：左 → 右 → 根
         */
        public static void postOrderRecursive(TreeNode root, List<Integer> result) {
            if (root == null) return;
            postOrderRecursive(root.left, result);
            postOrderRecursive(root.right, result);
            result.add(root.val);
        }

        /**
         * 层序遍历（BFS）
         */
        public static List<List<Integer>> levelOrder(TreeNode root) {
            List<List<Integer>> result = new ArrayList<>();
            if (root == null) return result;

            java.util.Queue<TreeNode> queue = new java.util.LinkedList<>();
            queue.offer(root);

            while (!queue.isEmpty()) {
                int size = queue.size();
                List<Integer> level = new ArrayList<>();

                for (int i = 0; i < size; i++) {
                    TreeNode node = queue.poll();
                    level.add(node.val);

                    if (node.left != null) queue.offer(node.left);
                    if (node.right != null) queue.offer(node.right);
                }

                result.add(level);
            }

            return result;
        }
    }

    /**
     * 二叉搜索树（BST）
     *
     * 特点：左子树所有节点 < 根节点 < 右子树所有节点
     */
    static class BinarySearchTree {
        /**
         * 查找
         * 时间复杂度：平均O(logn)，最坏O(n)
         */
        public static TreeNode search(TreeNode root, int val) {
            if (root == null || root.val == val) {
                return root;
            }

            if (val < root.val) {
                return search(root.left, val);
            } else {
                return search(root.right, val);
            }
        }

        /**
         * 插入
         */
        public static TreeNode insert(TreeNode root, int val) {
            if (root == null) {
                return new TreeNode(val);
            }

            if (val < root.val) {
                root.left = insert(root.left, val);
            } else if (val > root.val) {
                root.right = insert(root.right, val);
            }

            return root;
        }

        /**
         * 删除
         */
        public static TreeNode delete(TreeNode root, int val) {
            if (root == null) return null;

            if (val < root.val) {
                root.left = delete(root.left, val);
            } else if (val > root.val) {
                root.right = delete(root.right, val);
            } else {
                // 找到要删除的节点
                if (root.left == null) return root.right;
                if (root.right == null) return root.left;

                // 两个子节点：用后继节点（右子树最小值）替换
                TreeNode successor = findMin(root.right);
                root.val = successor.val;
                root.right = delete(root.right, successor.val);
            }

            return root;
        }

        private static TreeNode findMin(TreeNode node) {
            while (node.left != null) {
                node = node.left;
            }
            return node;
        }
    }

    // ==================== B+树 ====================

    /**
     * B+树特点：
     *
     * 1. 非叶子节点只存储键值，不存储数据
     * 2. 所有数据存储在叶子节点
     * 3. 叶子节点通过指针连接成链表
     * 4. 树的高度低（通常3层），减少磁盘IO
     *
     * 示例（3阶B+树）：
     *
     *                    ┌───────────────────┐
     *                    │      30 | 60      │  ← 非叶子节点（索引）
     *                    └─────────┬─────────┘
     *              ┌───────────────┼───────────────┐
     *              ▼               ▼               ▼
     *     ┌───────────────┐ ┌───────────────┐ ┌───────────────┐
     *     │ 10 | 20 | 30  │ │ 30 | 40 | 50  │ │ 60 | 70 | 80  │ ← 叶子节点（数据）
     *     │ d1 | d2 | d3  │ │ d4 | d5 | d6  │ │ d7 | d8 | d9  │
     *     └───────┬───────┘ └───────┬───────┘ └───────┬───────┘
     *             │                 │                 │
     *             └─────────────────┴─────────────────┘
     *                           叶子节点链表
     *
     * B+树 vs B树：
     * ┌─────────────────┬─────────────────────┬─────────────────────┐
     * │ 特性             │ B树                 │ B+树                │
     * ├─────────────────┼─────────────────────┼─────────────────────┤
     * │ 数据存储         │ 所有节点            │ 只有叶子节点         │
     * │ 叶子节点连接     │ 无                  │ 链表连接            │
     * │ 范围查询         │ 需要中序遍历        │ 直接遍历叶子链表    │
     * │ 查询效率         │ 不稳定（可能在非叶子）│ 稳定（必须到叶子）   │
     * │ 空间利用率       │ 较低                │ 较高                │
     * └─────────────────┴─────────────────────┴─────────────────────┘
     *
     * 为什么MySQL使用B+树：
     * 1. 范围查询效率高（叶子节点链表）
     * 2. 单节点存储更多键值（树更矮）
     * 3. 减少磁盘IO次数
     */

    // ==================== 跳表 ====================

    /**
     * 跳表（Skip List）：基于链表的多层索引
     *
     * 结构示例：
     *
     * 第3层：    HEAD ─────────────────────────────────────→ 50
     * 第2层：    HEAD ────────────→ 20 ────────────────────→ 50
     * 第1层：    HEAD ────→ 10 ──→ 20 ────→ 30 ────────────→ 50
     * 第0层：    HEAD → 5 → 10 → 15 → 20 → 25 → 30 → 40 → 50 → NULL
     *           （原始链表）
     *
     * 查找过程（查找30）：
     * 1. 第3层：HEAD → 50（30 < 50，下降）
     * 2. 第2层：HEAD → 20（30 > 20，继续）→ 50（30 < 50，下降）
     * 3. 第1层：20 → 30（找到！）
     *
     * 时间复杂度：O(logn)
     * 空间复杂度：O(n)
     *
     * 跳表 vs 平衡树：
     * ┌─────────────────┬─────────────────────┬─────────────────────┐
     * │ 特性             │ 跳表                 │ 平衡树（AVL/红黑树） │
     * ├─────────────────┼─────────────────────┼─────────────────────┤
     * │ 实现难度         │ 简单                │ 复杂                │
     * │ 范围查询         │ 高效（底层链表）     │ 需要中序遍历        │
     * │ 并发友好         │ 是（局部锁）         │ 否（需要全局锁）    │
     * │ 内存占用         │ 较高（多层索引）     │ 较低                │
     * └─────────────────┴─────────────────────┴─────────────────────┘
     *
     * Redis ZSET使用跳表原因：
     * 1. 实现简单
     * 2. 范围查询高效
     * 3. 并发性能好
     */

    /**
     * 简化版跳表实现
     */
    static class SkipList {
        private static final int MAX_LEVEL = 16;
        private final Node head = new Node(-1, MAX_LEVEL);
        private int level = 0;
        private final java.util.Random random = new java.util.Random();

        static class Node {
            int val;
            Node[] next;

            Node(int val, int level) {
                this.val = val;
                this.next = new Node[level];
            }
        }

        /**
         * 查找
         */
        public boolean search(int target) {
            Node curr = head;

            for (int i = level - 1; i >= 0; i--) {
                while (curr.next[i] != null && curr.next[i].val < target) {
                    curr = curr.next[i];
                }
            }

            curr = curr.next[0];
            return curr != null && curr.val == target;
        }

        /**
         * 插入
         */
        public void add(int val) {
            Node[] update = new Node[MAX_LEVEL];
            Node curr = head;

            // 找到每一层的前驱节点
            for (int i = level - 1; i >= 0; i--) {
                while (curr.next[i] != null && curr.next[i].val < val) {
                    curr = curr.next[i];
                }
                update[i] = curr;
            }

            // 随机决定层数
            int lvl = randomLevel();
            level = Math.max(level, lvl);

            Node newNode = new Node(val, lvl);

            // 更新每一层的指针
            for (int i = 0; i < lvl; i++) {
                newNode.next[i] = update[i].next[i];
                update[i].next[i] = newNode;
            }
        }

        /**
         * 删除
         */
        public boolean erase(int val) {
            Node[] update = new Node[MAX_LEVEL];
            Node curr = head;

            for (int i = level - 1; i >= 0; i--) {
                while (curr.next[i] != null && curr.next[i].val < val) {
                    curr = curr.next[i];
                }
                update[i] = curr;
            }

            curr = curr.next[0];
            if (curr == null || curr.val != val) {
                return false;
            }

            for (int i = 0; i < level; i++) {
                if (update[i].next[i] != curr) break;
                update[i].next[i] = curr.next[i];
            }

            while (level > 1 && head.next[level - 1] == null) {
                level--;
            }

            return true;
        }

        private int randomLevel() {
            int lvl = 1;
            while (random.nextDouble() < 0.5 && lvl < MAX_LEVEL) {
                lvl++;
            }
            return lvl;
        }
    }

    // ==================== 测试代码 ====================
    public static void main(String[] args) {
        // 测试跳表
        SkipList skipList = new SkipList();
        skipList.add(1);
        skipList.add(3);
        skipList.add(2);

        System.out.println("查找1：" + skipList.search(1));  // true
        System.out.println("查找4：" + skipList.search(4));  // false

        // 测试链表反转
        ListNode head = new ListNode(1);
        head.next = new ListNode(2);
        head.next.next = new ListNode(3);

        ListNode reversed = LinkedListOperations.reverse(head);
        System.out.print("反转链表：");
        while (reversed != null) {
            System.out.print(reversed.val + " ");
            reversed = reversed.next;
        }
    }
}
