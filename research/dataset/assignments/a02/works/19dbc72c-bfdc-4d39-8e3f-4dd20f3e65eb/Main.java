import java.io.*;
import java.util.*;

public class Solution {

    public static void main(String[] args) throws Exception {
        int tc = io.nextInt();
        for (int i = 0; i < tc; i++) {
            solve();
        }
        io.close();
    }

    private static void solve() throws Exception {
        int n = io.nextInt();
        String a = io.next();
        String b = io.next();
        int zz = 0;
        int zo = 0;
        int oo = 0;
        int oz = 0;
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) == '1' && b.charAt(i) == '0') {
                oz++;
            }
            if (a.charAt(i) == '0' && b.charAt(i) == '0') {
                zz++;
            }
            if (a.charAt(i) == '1' && b.charAt(i) == '1') {
                oo++;
            }
            if (a.charAt(i) == '0' && b.charAt(i) == '1') {
                zo++;
            }
        }
        int ans = Integer.MAX_VALUE;
        if (oz == zo) {
            ans = Math.min(ans, oz + zo);
        }
        if (oo - 1 == zz) {
            ans = Math.min(ans, oo + zz);
        }
        if (ans == Integer.MAX_VALUE) {
            ans = -1;
        }
        io.println(ans);
    }

    static void sort(int[] a) {
        ArrayList<Integer> l = new ArrayList<>(a.length);
        for (int i : a) l.add(i);
        Collections.sort(l);
        for (int i = 0; i < a.length; i++) a[i] = l.get(i);
    }

    //-----------PrintWriter for faster output---------------------------------
    public static FastIO io = new FastIO();

    //-----------MyScanner class for faster input----------
    static class FastIO extends PrintWriter {
        private InputStream stream;
        private byte[] buf = new byte[1 << 16];
        private int curChar, numChars;

        // standard input
        public FastIO() {
            this(System.in, System.out);
        }

        public FastIO(InputStream i, OutputStream o) {
            super(o);
            stream = i;
        }

        // file input
        public FastIO(String i, String o) throws IOException {
            super(new FileWriter(o));
            stream = new FileInputStream(i);
        }

        // throws InputMismatchException() if previously detected end of file
        private int nextByte() {
            if (numChars == -1) throw new InputMismatchException();
            if (curChar >= numChars) {
                curChar = 0;
                try {
                    numChars = stream.read(buf);
                } catch (IOException e) {
                    throw new InputMismatchException();
                }
                if (numChars == -1) return -1; // end of file
            }
            return buf[curChar++];
        }

        // to read in entire lines, replace c <= ' '
        // with a function that checks whether c is a line break
        public String next() {
            int c;
            do {
                c = nextByte();
            } while (c <= ' ');
            StringBuilder res = new StringBuilder();
            do {
                res.appendCodePoint(c);
                c = nextByte();
            } while (c > ' ');
            return res.toString();
        }

        public String nextLine() {
            int c;
            do {
                c = nextByte();
            } while (c < '\n');
            StringBuilder res = new StringBuilder();
            do {
                res.appendCodePoint(c);
                c = nextByte();
            } while (c > '\n');
            return res.toString();
        }

        public int nextInt() {
            int c;
            do {
                c = nextByte();
            } while (c <= ' ');
            int sgn = 1;
            if (c == '-') {
                sgn = -1;
                c = nextByte();
            }
            int res = 0;
            do {
                if (c < '0' || c > '9')
                    throw new InputMismatchException();
                res = 10 * res + c - '0';
                c = nextByte();
            } while (c > ' ');
            return res * sgn;
        }

        public long nextLong() {
            int c;
            do {
                c = nextByte();
            } while (c <= ' ');
            int sgn = 1;
            if (c == '-') {
                sgn = -1;
                c = nextByte();
            }
            long res = 0;
            do {
                if (c < '0' || c > '9')
                    throw new InputMismatchException();
                res = 10 * res + c - '0';
                c = nextByte();
            } while (c > ' ');
            return res * sgn;
        }


        public double nextDouble() {
            return Double.parseDouble(next());
        }
    }
    //--------------------------------------------------------
}