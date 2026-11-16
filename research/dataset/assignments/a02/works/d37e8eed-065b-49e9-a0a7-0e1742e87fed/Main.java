import java.io.*;
import java.util.*;

public class C {
    public static void main(String[] args) {
        while (N-- > 0) {
            solve();
        }
        out.close();
    }

    public static void solve() {
        int M = sc.nextInt();
        String a = sc.nextLine();
        String b = sc.nextLine();
        int d = 0;
        int al = 0;
        int bl = 0;
        for (int i = 0; i < M; i++) {
            if (a.charAt(i) != b.charAt(i)) {
                d++;
            }
            if (a.charAt(i) == '1') {
                al++;
            }
            if (b.charAt(i) == '1') {
                bl++;
            }
        }

        if ((al == bl && d % 2 == 0) || (M - al + 1 == bl && (M - d) % 2 == 1)) {
            if ((al == bl && d % 2 == 0) && (M - al + 1 == bl && (M - d) % 2 == 1)) {
                out.println(Math.min(d, M - d));
            } else if (al == bl && d % 2 == 0) {
                out.println(d);
            } else {
                out.println(M - d);
            }
        } else {
            out.println(-1);
            return;
        }
    }

    private static PrintWriter out = new PrintWriter(new BufferedOutputStream(System.out));

    private static MyScanner sc = new MyScanner();

    private static int N = sc.nextInt();

    @SuppressWarnings("unused")
    private static class MyScanner {
        BufferedReader br;
        StringTokenizer st;

        public MyScanner() {
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

        int[] readArray(int n) {
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = nextInt();
            }
            return a;
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
}