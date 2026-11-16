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
        int t = nextInt();
        StringBuilder sb = new StringBuilder();
        while (t-- > 0) {
            int n = nextInt();
            long[] val0 = new long[n + 1];
            long[] val1 = new long[n + 1];
            for (int i = 1; i <= n; i++) {
                val0[i] = nextInt();
                val1[i] = nextInt();
            }
            int[] head = new int[n + 1];
            Arrays.fill(head, -1);
            int[] nxt = new int[2 * (n - 1)];
            int[] to = new int[2 * (n - 1)];
            int ec = 0;
            for (int i = 0; i < n - 1; i++) {
                int u = nextInt(), v = nextInt();
                to[ec] = v; nxt[ec] = head[u]; head[u] = ec++;
                to[ec] = u; nxt[ec] = head[v]; head[v] = ec++;
            }
            int[] parent = new int[n + 1];
            int[] order = new int[n];
            int[] stack = new int[n];
            int sp = 0, cnt = 0;
            stack[sp++] = 1;
            parent[1] = 0;
            while (sp > 0) {
                int v = stack[--sp];
                order[cnt++] = v;
                for (int e = head[v]; e != -1; e = nxt[e]) {
                    int w = to[e];
                    if (w != parent[v]) {
                        parent[w] = v;
                        stack[sp++] = w;
                    }
                }
            }
            long[] dp0 = new long[n + 1];
            long[] dp1 = new long[n + 1];
            for (int i = n - 1; i >= 0; i--) {
                int v = order[i];
                int p = parent[v];
                if (p != 0) {
                    long a0 = Math.max(dp0[v] + Math.abs(val0[p] - val0[v]),
                                       dp1[v] + Math.abs(val0[p] - val1[v]));
                    long a1 = Math.max(dp0[v] + Math.abs(val1[p] - val0[v]),
                                       dp1[v] + Math.abs(val1[p] - val1[v]));
                    dp0[p] += a0;
                    dp1[p] += a1;
                }
            }
            sb.append(Math.max(dp0[1], dp1[1])).append('\n');
        }
        System.out.print(sb);
    }
}
