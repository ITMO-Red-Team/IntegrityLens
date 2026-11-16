import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.io.IOException;
import java.io.InputStream;
 
public class Main {
    public static void main(String[] args) {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;
        FastReader in = new FastReader(inputStream);
        PrintWriter out = new PrintWriter(outputStream);
        EAirConditioners solver = new EAirConditioners();
        int testCount = Integer.parseInt(in.next());
        for (int i = 1; i <= testCount; i++)
            solver.solve(i, in, out);
        out.close();
    }
 
    static class EAirConditioners {
        public void solve(int testNumber, FastReader in, PrintWriter out) {
            int n = in.nextInt(), q = in.nextInt();
            int[] a = in.readArray(q);
            long[] t = in.readLongArray(q);
            long[] pref = new long[n + 2];
            long[] suff = new long[n + 2];
 
            Arrays.fill(pref, (long) 1e14);
            Arrays.fill(suff, (long) 1e14);
 
            for (int i = 0; i < q; ++i) {
                pref[a[i]] = t[i] - a[i];
                suff[a[i]] = t[i] + a[i];
            }
 
            for (int i = 1; i <= n; ++i) {
                pref[i] = Math.min(pref[i], pref[i - 1]);
            }
 
            for (int i = n; i >= 1; --i) {
                suff[i] = Math.min(suff[i], suff[i + 1]);
            }
 
            for (int i = 1; i <= n; ++i) {
                out.print(Math.min(pref[i] + i, suff[i] - i) + " ");
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
 
        public long nextLong() {
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
 
        public int[] readArray(int size) {
            int[] array = new int[size];
            for (int i = 0; i < size; i++) array[i] = nextInt();
            return array;
        }
 
        public long[] readLongArray(int size) {
            long[] array = new long[size];
            for (int i = 0; i < size; i++) array[i] = nextLong();
            return array;
        }
 
        public interface SpaceCharFilter {
            public boolean isSpaceChar(int ch);
 
        }
 
    }
}