import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.InputMismatchException;
import java.io.IOException;
import java.io.InputStream;

public class Main {
    public static void main(String[] args) {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;
        FastReader in = new FastReader(inputStream);
        PrintWriter out = new PrintWriter(outputStream);
        CMenorah solver = new CMenorah();
        int testCount = Integer.parseInt(in.next());
        for (int i = 1; i <= testCount; i++)
            solver.solve(i, in, out);
        out.close();
    }

    static class CMenorah {
        public void solve(int testNumber, FastReader in, PrintWriter out) {
            int n = in.nextInt();
            char[] a = in.next().toCharArray();
            char[] b = in.next().toCharArray();

            int ans = checkPairs(a, b, -1);
            ans = Math.min(ans, changePair(a, b, '1'));
            ans = Math.min(ans, changePair(a, b, '0'));

            if (ans == Integer.MAX_VALUE) ans = -1;
            out.println(ans);
        }

        int changePair(char[] a, char[] b, char t) {
            int index = -1;
            int n = a.length;

            for (int i = 0; i < n; ++i) {
                if (a[i] == '1' && b[i] == t) {
                    index = i;
                    break;
                }
            }

            return checkPairs(a, b, index);
        }

        int checkPairs(char[] a, char[] b, int changeStringIndex) {
            int n = a.length;
            int val = 0;

            char[] tmp = new char[n];
            System.arraycopy(a, 0, tmp, 0, n);

            if (changeStringIndex != -1) {
                val = 1;

                for (int i = 0; i < n; ++i) {
                    if (i == changeStringIndex) continue;
                    tmp[i] = a[i] == '0' ? '1' : '0';
                }
            }

            int _10 = 0, _01 = 0;
            for (int i = 0; i < n; ++i) {
                if (tmp[i] != b[i]) {
                    if (tmp[i] == '0') _01++;
                    else _10++;
                }
            }

            return _01 == _10 ? 2 * _01 + val : Integer.MAX_VALUE;
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

