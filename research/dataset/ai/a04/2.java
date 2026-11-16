import java.io.*;
import java.util.*;

public class Main {
    private static final int BUF = 1 << 16;
    private static DataInputStream in;
    private static byte[] buf = new byte[BUF];
    private static int bufLen = 0, bufPtr = 0;

    private static int readByte() throws IOException {
        if (bufPtr == bufLen) {
            bufLen = in.read(buf, 0, BUF);
            bufPtr = 0;
            if (bufLen <= 0) return -1;
        }
        return buf[bufPtr++];
    }

    private static int nextInt() throws IOException {
        int c = readByte();
        while (c != '-' && (c < '0' || c > '9')) c = readByte();
        boolean neg = false;
        if (c == '-') { neg = true; c = readByte(); }
        int r = 0;
        while (c >= '0' && c <= '9') {
            r = r * 10 + (c - '0');
            c = readByte();
        }
        return neg ? -r : r;
    }

    public static void main(String[] args) throws IOException {
        in = new DataInputStream(new BufferedInputStream(System.in, BUF));
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
        StringBuilder sb = new StringBuilder();
        int t = nextInt();
        final long SHIFT = 1L << 20;
        while (t-- > 0) {
            int n = nextInt();
            int m = nextInt();
            int x = nextInt();
            PriorityQueue<Long> pq = new PriorityQueue<>();
            for (int j = 1; j <= m; j++) pq.add((long) j);
            sb.append("YES\n");
            for (int i = 0; i < n; i++) {
                int h = nextInt();
                long top = pq.poll();
                long height = top / SHIFT;
                int idx = (int) (top % SHIFT);
                sb.append(idx);
                sb.append(i + 1 < n ? ' ' : '\n');
                pq.add((height + h) * SHIFT + idx);
            }
        }
        out.print(sb);
        out.flush();
    }
}
