/**
 * Created by Himanshu
 **/

import java.util.*;
import java.io.*;
import java.math.*;

public class C1499 {
    public static void main(String[] args) throws IOException {
        PrintWriter out = new PrintWriter(System.out);
        Reader s = new Reader();
        int t = s.i();
        while (t-- > 0) {
            int n = s.i();
            long [] arr = s.arrLong(n);
            long odd = arr[1] , even = arr[0];
            long [] a = new long[n];
            a[0] = arr[0]*n;
            a[1] = arr[1]*n;
            int in1 = 0 , in2 = 1;
            long ans = a[0]+a[1];
            for (int i=2;i<n;i++) {
                if (i%2 == 0) {
                    long val = even + (n-i/2)*arr[i];
                    long val2 = a[i-2] - arr[in1] + arr[i];
                    if (val < val2) in1 = i;
                    a[i] = Math.min(val2,val);
                    even += arr[i];
                } else {
                    long val = odd + (n-i/2)*arr[i];
                    long val2 = a[i-2] - arr[in2] + arr[i];
                    if (val < val2) in2 = i;
                    a[i] = Math.min(val2,val);
                    odd += arr[i];
                }
                ans = Math.min(ans,a[i]+a[i-1]);
            }
            out.println(ans);
        }
        out.flush();
    }

    public static void shuffle(long[] arr) {
        int n = arr.length;
        Random rand = new Random();
        for (int i = 0; i < n; i++) {
            long temp = arr[i];
            int randomPos = i + rand.nextInt(n - i);
            arr[i] = arr[randomPos];
            arr[randomPos] = temp;
        }
    }

    private static int gcd(int a, int b) {
        if(b == 0) return a;
        return gcd(b,a%b);
    }

    public static long nCr(long[] fact, long[] inv, int n, int r, long mod) {
        if (n < r)
            return 0;
        return ((fact[n] * inv[n - r]) % mod * inv[r]) % mod;
    }

    private static void factorials(long[] fact, long[] inv, long mod, int n) {
        fact[0] = 1;
        inv[0] = 1;
        for (int i = 1; i <= n; ++i) {
            fact[i] = (fact[i - 1] * i) % mod;
            inv[i] = power(fact[i], mod - 2, mod);
        }
    }

    private static long power(long a, long n, long p) {
        long result = 1;
        while (n > 0) {
            if (n % 2 == 0) {
                a = (a * a) % p;
                n /= 2;
            } else {
                result = (result * a) % p;
                n--;
            }
        }
        return result;
    }

    private static long power(long a, long n) {
        long result = 1;
        while (n > 0) {
            if (n % 2 == 0) {
                a = (a * a);
                n /= 2;
            } else {
                result = (result * a);
                n--;
            }
        }
        return result;
    }

    private static long query(long[] tree, int in, int start, int end, int l, int r) {
        if (start >= l && r >= end) return tree[in];
        if (end < l || start > r) return 0;
        int mid = (start + end) / 2;
        long x = query(tree, 2 * in, start, mid, l, r);
        long y = query(tree, 2 * in + 1, mid + 1, end, l, r);
        return x + y;
    }

    private static void update(int[] arr, long[] tree, int in, int start, int end, int idx, int val) {
        if (start == end) {
            tree[in] = val;
            arr[idx] = val;
            return;
        }
        int mid = (start + end) / 2;
        if (idx > mid) update(arr, tree, 2 * in + 1, mid + 1, end, idx, val);
        else update(arr, tree, 2 * in, start, mid, idx, val);
        tree[in] = tree[2 * in] + tree[2 * in + 1];
    }

    private static void build(int[] arr, long[] tree, int in, int start, int end) {
        if (start == end) {
            tree[in] = arr[start];
            return;
        }
        int mid = (start + end) / 2;
        build(arr, tree, 2 * in, start, mid);
        build(arr, tree, 2 * in + 1, mid + 1, end);
        tree[in] = (tree[2 * in + 1] + tree[2 * in]);
    }

    static class Reader {
        private InputStream mIs;
        private byte[] buf = new byte[1024];
        private int curChar, numChars;

        public Reader() {
            this(System.in);
        }

        public Reader(InputStream is) {
            mIs = is;
        }

        public int read() {
            if (numChars == -1) throw new InputMismatchException();
            if (curChar >= numChars) {
                curChar = 0;
                try {
                    numChars = mIs.read(buf);
                } catch (IOException e) {
                    throw new InputMismatchException();
                }
                if (numChars <= 0) return -1;
            }
            return buf[curChar++];
        }

        public String nextLine() {
            int c = read();
            while (isSpaceChar(c)) c = read();
            StringBuilder res = new StringBuilder();
            do {
                res.appendCodePoint(c);
                c = read();
            } while (!isEndOfLine(c));
            return res.toString();
        }

        public String s() {
            int c = read();
            while (isSpaceChar(c)) c = read();
            StringBuilder res = new StringBuilder();
            do {
                res.appendCodePoint(c);
                c = read();
            } while (!isSpaceChar(c));
            return res.toString();
        }

        public long l() {
            int c = read();
            while (isSpaceChar(c)) c = read();
            int sgn = 1;
            if (c == '-') {
                sgn = -1;
                c = read();
            }
            long res = 0;
            do {
                if (c < '0' || c > '9') throw new InputMismatchException();
                res *= 10;
                res += c - '0';
                c = read();
            } while (!isSpaceChar(c));
            return res * sgn;
        }

        public int i() {
            int c = read();
            while (isSpaceChar(c)) c = read();
            int sgn = 1;
            if (c == '-') {
                sgn = -1;
                c = read();
            }
            int res = 0;
            do {
                if (c < '0' || c > '9') throw new InputMismatchException();
                res *= 10;
                res += c - '0';
                c = read();
            } while (!isSpaceChar(c));
            return res * sgn;
        }

        public double d() throws IOException {
            return Double.parseDouble(s());
        }

        public boolean isSpaceChar(int c) {
            return c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == -1;
        }

        public boolean isEndOfLine(int c) {
            return c == '\n' || c == '\r' || c == -1;
        }

        public int[] arr(int n) {
            int[] ret = new int[n];
            for (int i = 0; i < n; i++) {
                ret[i] = i();
            }
            return ret;
        }

        public long[] arrLong(int n) {
            long[] ret = new long[n];
            for (int i = 0; i < n; i++) {
                ret[i] = l();
            }
            return ret;
        }
    }
//    static class pairLong implements Comparator<pairLong> {
//        long first, second;
//
//        pairLong() {
//        }
//
//        pairLong(long first, long second) {
//            this.first = first;
//            this.second = second;
//        }
//
//        @Override
//        public int compare(pairLong p1, pairLong p2) {
//            if (p1.first == p2.first) {
//                if(p1.second > p2.second) return 1;
//                else return -1;
//            }
//            if(p1.first > p2.first) return 1;
//            else return -1;
//        }
//    }
//    static class pair implements Comparator<pair> {
//        int first, second;
//
//        pair() {
//        }
//
//        pair(int first, int second) {
//            this.first = first;
//            this.second = second;
//        }
//
//        @Override
//        public int compare(pair p1, pair p2) {
//            if (p1.first == p2.first) return p1.second - p2.second;
//            return p1.first - p2.first;
//        }
//    }
}

