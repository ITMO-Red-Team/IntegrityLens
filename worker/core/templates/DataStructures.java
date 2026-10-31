class Pair implements Comparable<Pair> {
    int first;
    int second;

    Pair(int first, int second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int compareTo(Pair other) {
        if (first != other.first) {
            return Integer.compare(first, other.first);
        }
        return Integer.compare(second, other.second);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Pair pair = (Pair) o;
        return first == pair.first && second == pair.second;
    }

    @Override
    public int hashCode() {
        return 31 * first + second;
    }
}

class Point implements Comparable<Point> {
    int x;
    int y;

    Point(int a, int b) {
        x = a;
        y = b;
    }

    @Override
    public int compareTo(Point other) {
        return x - other.x;
    }

    public int compareBoth(Point other) {
        if (this.y == other.y) {
            return this.x - other.x;
        }
        return this.y - other.y;
    }

    public int compareSigns(Point other) {
        if (this.x > other.x) {
            return 1;
        } else if (this.x < other.x) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Point) {
            Point p = (Point) o;
            return p.x == x && p.y == y;
        }
        return false;
    }

    @Override
    public String toString() {
        return x + " " + y;
    }
}

class SignedDsu {
    int[] parent;

    int find(int a) {
        if (parent[a] < 0) {
            return a;
        }
        return parent[a] = find(parent[a]);
    }

    void union(int a, int b) {
        a = find(a);
        b = find(b);
        if (a != b) {
            parent[a] += parent[b];
            parent[b] = a;
        }
    }
}

class Dsu {
    int[] parent;
    int[] size;

    Dsu(int n) {
        parent = new int[n];
        size = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }

    int find(int x) {
        if (parent[x] == x) {
            return x;
        }
        return parent[x] = find(parent[x]);
    }

    boolean union(int a, int b) {
        a = find(a);
        b = find(b);
        if (a == b) {
            return false;
        }
        if (size[a] < size[b]) {
            int t = a;
            a = b;
            b = t;
        }
        parent[b] = a;
        size[a] += size[b];
        return true;
    }
}

class Fenwick {
    long[] tree;

    Fenwick(int n) {
        tree = new long[n + 1];
    }

    void update(int i, long delta) {
        for (i++; i < tree.length; i += i & -i) {
            tree[i] += delta;
        }
    }

    long query(int i) {
        long sum = 0;
        for (i++; i > 0; i -= i & -i) {
            sum += tree[i];
        }
        return sum;
    }
}

class BinaryIndexedTree {
    static long getSum(int index, long[] tree) {
        long sum = 0;
        index = index + 1;
        while (index > 0) {
            sum += tree[index];
            index -= index & (-index);
        }
        return sum;
    }

    static void update(int n, int index, long value, long[] tree) {
        index = index + 1;
        while (index <= n) {
            tree[index] += value;
            index += index & (-index);
        }
    }
}

class SegmentTree {
    long[] tree;

    SegmentTree(int[] a) {
        tree = new long[4 * a.length];
        build(a, 1, 0, a.length - 1);
    }

    void build(int[] a, int node, int left, int right) {
        if (left == right) {
            tree[node] = a[left];
            return;
        }
        int mid = (left + right) / 2;
        build(a, 2 * node, left, mid);
        build(a, 2 * node + 1, mid + 1, right);
        tree[node] = tree[2 * node] + tree[2 * node + 1];
    }

    void update(int node, int left, int right, int index, int value) {
        if (left == right) {
            tree[node] = value;
            return;
        }
        int mid = (left + right) / 2;
        if (index <= mid) {
            update(2 * node, left, mid, index, value);
        } else {
            update(2 * node + 1, mid + 1, right, index, value);
        }
        tree[node] = tree[2 * node] + tree[2 * node + 1];
    }

    long query(int node, int left, int right, int from, int to) {
        if (from > right || to < left) {
            return 0;
        }
        if (from <= left && right <= to) {
            return tree[node];
        }
        int mid = (left + right) / 2;
        return query(2 * node, left, mid, from, to) + query(2 * node + 1, mid + 1, right, from, to);
    }
}

class IterativeSegmentTree {
    int n;
    long[] tree;

    IterativeSegmentTree(int n) {
        this.n = n;
        tree = new long[2 * n];
    }

    void update(int i, long value) {
        i += n;
        tree[i] = value;
        while (i > 1) {
            i /= 2;
            tree[i] = tree[2 * i] + tree[2 * i + 1];
        }
    }

    long query(int left, int right) {
        long sum = 0;
        for (left += n, right += n; left < right; left /= 2, right /= 2) {
            if ((left & 1) == 1) {
                sum += tree[left++];
            }
            if ((right & 1) == 1) {
                sum += tree[--right];
            }
        }
        return sum;
    }
}

class SparseTable {
    int[][] table;
    int[] log;

    SparseTable(int[] a) {
        int n = a.length;
        log = new int[n + 1];
        for (int i = 2; i <= n; i++) {
            log[i] = log[i / 2] + 1;
        }
        table = new int[log[n] + 1][n];
        table[0] = a.clone();
        for (int k = 1; k < table.length; k++) {
            for (int i = 0; i + (1 << k) <= n; i++) {
                table[k][i] = Math.min(table[k - 1][i], table[k - 1][i + (1 << (k - 1))]);
            }
        }
    }

    int query(int left, int right) {
        int k = log[right - left + 1];
        return Math.min(table[k][left], table[k][right - (1 << k) + 1]);
    }
}

class Trie {
    static class Node {
        Node[] children = new Node[26];
        boolean end;
    }

    Node root = new Node();

    void insert(String word) {
        Node node = root;
        for (char c : word.toCharArray()) {
            int i = c - 'a';
            if (node.children[i] == null) {
                node.children[i] = new Node();
            }
            node = node.children[i];
        }
        node.end = true;
    }

    boolean contains(String word) {
        Node node = root;
        for (char c : word.toCharArray()) {
            int i = c - 'a';
            if (node.children[i] == null) {
                return false;
            }
            node = node.children[i];
        }
        return node.end;
    }
}

class MinHeap {
    int[] heap;
    int size;

    MinHeap(int capacity) {
        heap = new int[capacity];
    }

    void push(int value) {
        int i = size++;
        heap[i] = value;
        while (i > 0 && heap[(i - 1) / 2] > heap[i]) {
            int parent = (i - 1) / 2;
            int t = heap[i];
            heap[i] = heap[parent];
            heap[parent] = t;
            i = parent;
        }
    }

    int pop() {
        int top = heap[0];
        heap[0] = heap[--size];
        int i = 0;
        while (2 * i + 1 < size) {
            int child = 2 * i + 1;
            if (child + 1 < size && heap[child + 1] < heap[child]) {
                child++;
            }
            if (heap[i] <= heap[child]) {
                break;
            }
            int t = heap[i];
            heap[i] = heap[child];
            heap[child] = t;
            i = child;
        }
        return top;
    }
}

class SinglyLinkedList {
    static class Node {
        int value;
        Node next;

        Node(int value) {
            this.value = value;
        }
    }

    Node head;

    void add(int value) {
        Node node = new Node(value);
        if (head == null) {
            head = node;
            return;
        }
        Node current = head;
        while (current.next != null) {
            current = current.next;
        }
        current.next = node;
    }

    void reverse() {
        Node previous = null;
        Node current = head;
        while (current != null) {
            Node next = current.next;
            current.next = previous;
            previous = current;
            current = next;
        }
        head = previous;
    }
}
