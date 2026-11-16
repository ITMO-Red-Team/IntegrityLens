import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.InputMismatchException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Collections;
import java.io.InputStream;

public class Main {
    public static void main(String[] args) {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;
        FastReader in = new FastReader(inputStream);
        PrintWriter out = new PrintWriter(outputStream);
        CPhoenixAndTowers solver = new CPhoenixAndTowers();
        int testCount = Integer.parseInt(in.next());
        for (int i = 1; i <= testCount; i++)
            solver.solve(i, in, out);
        out.close();
    }

    static class CPhoenixAndTowers {
        public void solve(int testNumber, FastReader in, PrintWriter out) {
            int n = in.nextInt(), m = in.nextInt(), k = in.nextInt();
            ArrayList<Pair<Integer, Integer>> a = new ArrayList<>();
            for (int i = 0; i < n; ++i) {
                a.add(new Pair<>(in.nextInt(), i));
            }
            Collections.sort(a);

            int[] ans = new int[n];
            int[] sum = new int[m];
            int j = 1;

            for (int i = 0; i < n; ++i) {
                ans[a.get(i).y] = j;
                sum[j - 1] += a.get(i).x;
                j++;
                if (j == m + 1) j = 1;
            }

            for (int i = 1; i < m; ++i) {
                if (Math.abs(sum[i - 1] - sum[i]) > k) {
                    out.println("NO");
                }
            }

            out.println("YES");
            for (int e : ans) {
                out.print(e + " ");
            }
            out.println();

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

    static class Pair<U, V> implements Comparable<Pair<U, V>> {
        public U x;
        public V y;

        public Pair(U x, V y) {
            this.x = x;
            this.y = y;
        }

        public int compareTo(Pair<U, V> o) {
            int value = ((Comparable<U>) x).compareTo(o.x);
            if (value != 0) return value;
            return ((Comparable<V>) y).compareTo(o.y);
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return x.equals(pair.x) && y.equals(pair.y);
        }

        public int hashCode() {
            return Objects.hash(x, y);
        }

    }
}


