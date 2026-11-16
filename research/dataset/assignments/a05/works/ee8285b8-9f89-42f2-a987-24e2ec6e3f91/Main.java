import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;


public class C_CF {

    public static void main(String[] args) {
        FastScanner57 fs = new FastScanner57();
        PrintWriter pw = new PrintWriter(System.out);
        int t = fs.ni();
        //int t = 1;
        for (int tc = 0; tc < t; tc++) {
            int n = fs.ni();
            String[] s = new String[n];
            for (int i = 0; i < n; i++) {
                s[i] = fs.next();
            }
            int res = c(0,s);
            for (int i = 1; i < 5; i++) {
                res = Math.max(res,c(i,s));
            }
            pw.println(res);
        }
        
        pw.close();

    }
    public static int c(int l, String[] s) {
        List<Integer> list = new ArrayList();
        for (int i = 0; i < s.length; i++) {
            String t = s[i];
            int ct = 0;
            for (int j = 0; j < t.length(); j++) {
                if (t.charAt(j)-'a'==l) {
                    ct++;
                } else {
                    ct--;
                }
            }
            list.add(ct);
        }
        Collections.sort(list);
        Collections.reverse(list);
        int sum = 0;
        for (int i = 0; i < s.length; i++) {
            sum += list.get(i);
            if (sum<=0) return i;
        }
        return list.size();
    }
    public static int gcd(int n1, int n2) {
        if (n2 == 0) {
            return n1;
        }
        return gcd(n2, n1 % n2);
    }

    static class BIT18 {

        int[] bit;

        public BIT18(int size) {
            bit = new int[size];
        }

        public void update(int ind, int delta) {
            while (ind < bit.length) {
                bit[ind] += delta;
                ind = ind + (ind & (-1 * ind));
            }
        }

        public int sum(int ind) {
            int s = 0;
            while (ind > 0) {
                s += bit[ind];
                ind = ind - (ind & (-1 * ind));
            }
            return s;
        }

        public int query(int l, int r) {
            return sum(r) - sum(l);
        }
    }

    // 0 -> left was chosen
    // 1 -> right was chosen

    public static void sort(long[] a) {
        List<Long> list = new ArrayList();
        for (int i = 0; i < a.length; i++) {
            list.add(a[i]);
        }
        Collections.sort(list);
        for (int i = 0; i < a.length; i++) {
            a[i] = list.get(i);
        }
    }

    public static long gcd(long n1, long n2) {
        if (n2 == 0) {
            return n1;
        }
        return gcd(n2, n1 % n2);
    }

}

class UnionFind16 {

    int[] id;

    public UnionFind16(int size) {
        id = new int[size];
        for (int i = 0; i < size; i++) {
            id[i] = i;
        }
    }

    public int find(int p) {
        int root = p;
        while (root != id[root]) {
            root = id[root];
        }
        while (p != root) {
            int next = id[p];
            id[p] = root;
            p = next;
        }
        return root;
    }

    public void union(int p, int q) {
        int a = find(p), b = find(q);
        if (a == b) {
            return;
        }
        id[b] = a;
    }
}

class FastScanner57 {

    BufferedReader br;
    StringTokenizer st;

    public FastScanner57() {
        br = new BufferedReader(new InputStreamReader(System.in), 32768);
        st = null;
    }

    String next() {
        while (st == null || !st.hasMoreElements()) {
            try {
                st = new StringTokenizer(br.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return st.nextToken();
    }

    int ni() {
        return Integer.parseInt(next());
    }

    int[] intArray(int N) {
        int[] ret = new int[N];
        for (int i = 0; i < N; i++) {
            ret[i] = ni();
        }
        return ret;
    }

    long nl() {
        return Long.parseLong(next());
    }

    long[] longArray(int N) {
        long[] ret = new long[N];
        for (int i = 0; i < N; i++) {
            ret[i] = nl();
        }
        return ret;
    }

    double nd() {
        return Double.parseDouble(next());
    }

    String nextLine() {
        String str = "";
        try {
            str = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }
}
