import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.InputMismatchException;
import java.io.IOException;
import java.util.ArrayList;
import java.io.InputStream;


public class Main {
    public static void main(String[] args) {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;
        FastReader in = new FastReader(inputStream);
        PrintWriter out = new PrintWriter(outputStream);
        CParsasHumongousTree solver = new CParsasHumongousTree();
        int testCount = Integer.parseInt(in.next());
        for (int i = 1; i <= testCount; i++)
            solver.solve(i, in, out);
        out.close();
    }

    static class CParsasHumongousTree {
        int n;
        long[][] dp;
        int[] l;
        int[] r;
        ArrayList<Integer>[] a;

        public void solve(int testNumber, FastReader in, PrintWriter out) {
            n = in.nextInt();
            l = new int[n];
            r = new int[n];
            dp = new long[n][2];
            a = new ArrayList[n];
            for (int i = 0; i < n; ++i) {
                a[i] = new ArrayList<>();
            }

            for (int i = 0; i < n; ++i) {
                l[i] = in.nextInt();
                r[i] = in.nextInt();
            }

            for (int i = 0; i < n - 1; ++i) {
                int u = in.nextInt() - 1, v = in.nextInt() - 1;
                a[u].add(v);
                a[v].add(u);
            }

            dfs(0, -1);

            out.println(Math.max(dp[0][0], dp[0][1]));
        }

        void dfs(int u, int p) {
            dp[u][0] = dp[u][1] = 0;
            for (int v : a[u]) {
                if (v != p) {
                    dfs(v, u);
                    dp[u][0] += Math.max(dp[v][0] + Math.abs(l[u] - l[v]), dp[v][1] + Math.abs(l[u] - r[v]));
                    dp[u][1] += Math.max(dp[v][0] + Math.abs(r[u] - l[v]), dp[v][1] + Math.abs(r[u] - r[v]));
                }
            }
        }

    }

    static class FastReader {
        private InputStream stream;
        private byte[] buf = new byte[1024];
        private int curChar;
        private int numChars;
        private FastReader.SpaceCharFilter filter;

        public FastReader(InputStream stream) {
            this.stream = stream;
        }

        public int read() {
            if (numChars == -1) throw new InputMismatchException();
            if (curChar >= numChars) {
                curChar = 0;
                try {
                    numChars = stream.read(buf);
                } catch (IOException e) {
                    throw new InputMismatchException();
                }
                if (numChars <= 0) return -1;
            }
            return buf[curChar++];
        }

        public int nextInt() {
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
            }
            while (!isSpaceChar(c));
            return res * sgn;
        }

        public String next() {
            int c = read();
            while (isSpaceChar(c)) c = read();
            StringBuilder res = new StringBuilder();
            do {
                res.appendCodePoint(c);
                c = read();
            } while (!isSpaceChar(c));
            return res.toString();
        }

        public boolean isSpaceChar(int c) {
            if (filter != null) return filter.isSpaceChar(c);
            return c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == -1;
        }

        public interface SpaceCharFilter {
            public boolean isSpaceChar(int ch);

        }

    }
}

