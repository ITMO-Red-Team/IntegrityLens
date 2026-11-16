import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;

/**
 * Built using CHelper plug-in
 * Actual solution is at the top
 */
public class Main {
    public static void main(String[] args) {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;
        FastScanner in = new FastScanner(inputStream);
        PrintWriter out = new PrintWriter(outputStream);
        TaskC solver = new TaskC();
        solver.solve(1, in, out);
        out.close();
    }

    static class TaskC {
        public void solve(int testNumber, FastScanner in, PrintWriter out) {
            int numTests = in.nextInt();
            for (int test = 0; test < numTests; test++) {
                int n = in.nextInt();
                char[] a = in.next().toCharArray();
                char[] b = in.next().toCharArray();
                int ka = 0;
                int kb = 0;
                int k11 = 0;
                int k01 = 0;
                for (int i = 0; i < n; i++) {
                    if (a[i] == '1') {
                        ++ka;
                    }
                    if (b[i] == '1') {
                        ++kb;
                    }
                    if (a[i] == '1' && b[i] == '1') {
                        ++k11;
                    }
                    if (a[i] == '0' && b[i] == '1') {
                        ++k01;
                    }
                }
                if (ka == 0) {
                    out.println(kb == 0 ? 0 : -1);
                    continue;
                }
                int ans = Integer.MAX_VALUE;
                if (ka == kb) {
                    ans = Math.min(ans, 2 * (ka - k11));
                }
                if (ka == n - kb + 1) {
                    int cur = 0;
                    if (k11 == 0) {
                        cur = 1 + 2 * (kb - k01);
                    } else {
                        cur = 1 + 2 * (kb - k01 - 1);
                    }
                    ans = Math.min(ans, cur);
                }

                if (ans == Integer.MAX_VALUE) {
                    ans = -1;
                }
                out.println(ans);
            }
        }

    }

    static class FastScanner {
        private BufferedReader in;
        private StringTokenizer st;

        public FastScanner(InputStream stream) {
            try {
                in = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            } catch (Exception e) {
                throw new AssertionError();
            }
        }

        public String next() {
            while (st == null || !st.hasMoreTokens()) {
                try {
                    String rl = in.readLine();
                    if (rl == null) {
                        return null;
                    }
                    st = new StringTokenizer(rl);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return st.nextToken();
        }

        public int nextInt() {
            return Integer.parseInt(next());
        }

    }
}

