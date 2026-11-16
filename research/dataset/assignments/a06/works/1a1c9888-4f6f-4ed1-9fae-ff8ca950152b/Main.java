import java.lang.*;
import java.util.*;
import java.io.*;

public class Main {
    static FastScanner in = new FastScanner();

    static void solve() {
        int n = in.nextInt();
        long[] a = new long[n], odd = new long[n], even = new long[n];
        long[] sum = new long[n];
        long m1 = Long.MAX_VALUE, m2 = Long.MAX_VALUE;
        long st = 0;

        for (int i = 0; i < n; ++i) {
            a[i] = in.nextLong();
            if (i % 2 == 0 && a[i] < m1)
                m1 = a[i];
            if (i % 2 == 1 && a[i] < m2)
                m2 = a[i];
            st += a[i];
            odd[i] = m1; even[i] = m2;
            sum[i] = st;
        }

        long ans = Long.MAX_VALUE;
        for (int i = 1; i < n; ++i) {
            long aux = sum[i] + odd[i] * (n - i - 1 + (i + 1) / 2) + even[i] * (n - (i + 1) / 2);
            if (aux < ans && aux > 0)
                ans = aux;
        }

        System.out.println(ans);
    }

    public static void main(String[] args) {
        int T = in.nextInt();
        while (T-- > 0)
            solve();
    }

    static class Pair<X, Y> {
        X x;
        Y y;

        public Pair(X x, Y y) {
            this.x = x;
            this.y = y;
        }
    }
}

class FastScanner {
    BufferedReader br;
    StringTokenizer st;

    public FastScanner() {
        br = new BufferedReader(new InputStreamReader(System.in));
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

    int nextInt() {
        return Integer.parseInt(next());
    }

    long nextLong() {
        return Long.parseLong(next());
    }

    double nextDouble() {
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

