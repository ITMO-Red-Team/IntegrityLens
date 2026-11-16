import java.io.*;
import java.util.*;

public class Main {
    private static final int BUF = 1 << 16;
    private static final DataInputStream in = new DataInputStream(new BufferedInputStream(System.in, BUF));
    private static final byte[] buf = new byte[BUF];
    private static int bufLen = 0, bufPtr = 0;

    private static int readByte() throws IOException {
        if (bufPtr == bufLen) {
            bufLen = in.read(buf, 0, BUF);
            bufPtr = 0;
            if (bufLen <= 0) return -1;
        }
        return buf[bufPtr++];
    }

    private static long readLong() throws IOException {
        int c = readByte();
        while (c != '-' && (c < '0' || c > '9')) c = readByte();
        boolean neg = false;
        if (c == '-') {
            neg = true;
            c = readByte();
        }
        long r = 0;
        while (c >= '0' && c <= '9') {
            r = r * 10 + (c - '0');
            c = readByte();
        }
        return neg ? -r : r;
    }

    private static int readInt() throws IOException {
        return (int) readLong();
    }

    public static void main(String[] args) throws IOException {
        int q = readInt();
        StringBuilder sb = new StringBuilder(1 << 20);
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out), BUF));
        final long INF = Long.MAX_VALUE / 4;
        while (q-- > 0) {
            int n = readInt();
            int k = readInt();
            int[] a = new int[k];
            for (int j = 0; j < k; j++) a[j] = readInt();
            long[] t = new long[n + 2];
            Arrays.fill(t, INF);
            for (int j = 0; j < k; j++) {
                long v = readLong();
                t[a[j]] = Math.min(t[a[j]], v);
            }
            long[] left = new long[n + 2];
            long cur = INF;
            for (int i = 1; i <= n; i++) {
                cur = Math.min(cur + 1, t[i]);
                left[i] = cur;
            }
            cur = INF;
            for (int i = n; i >= 1; i--) {
                cur = Math.min(cur + 1, t[i]);
                left[i] = Math.min(left[i], cur);
            }
            for (int i = 1; i <= n; i++) {
                sb.append(left[i]);
                sb.append(i == n ? '\n' : ' ');
            }
            if (sb.length() > (1 << 16)) {
                out.print(sb);
                sb.setLength(0);
            }
        }
        out.print(sb);
        out.flush();
    }
}
