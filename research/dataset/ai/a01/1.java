import java.io.*;
import java.util.*;

public class Main {
    static DataInputStream in;
    static byte[] buf = new byte[1 << 16];
    static int bufLen = 0, bufPtr = 0;

    static int readByte() throws IOException {
        if (bufPtr == bufLen) {
            bufLen = in.read(buf, 0, buf.length);
            bufPtr = 0;
            if (bufLen <= 0) return -1;
        }
        return buf[bufPtr++];
    }

    static int nextInt() throws IOException {
        int c = readByte();
        while (c < '0' || c > '9') {
            if (c == '-') {
                c = readByte();
                int r = 0;
                while (c >= '0' && c <= '9') {
                    r = r * 10 + (c - '0');
                    c = readByte();
                }
                return -r;
            }
            c = readByte();
        }
        int r = 0;
        while (c >= '0' && c <= '9') {
            r = r * 10 + (c - '0');
            c = readByte();
        }
        return r;
    }

    public static void main(String[] args) throws IOException {
        in = new DataInputStream(new BufferedInputStream(System.in, 1 << 16));
        StringBuilder sb = new StringBuilder();
        int t = nextInt();
        while (t-- > 0) {
            int n = nextInt();
            long[] l = new long[n + 1];
            long[] r = new long[n + 1];
            for (int i = 1; i <= n; i++) {
                l[i] = nextInt();
                r[i] = nextInt();
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
                    int u = to[e];
                    if (u != parent[v]) {
                        parent[u] = v;
                        stack[sp++] = u;
                    }
                }
            }

            long[] dpL = new long[n + 1];
            long[] dpR = new long[n + 1];
            for (int i = n - 1; i >= 0; i--) {
                int v = order[i];
                int p = parent[v];
                if (p == 0) continue;
                dpL[p] += Math.max(dpL[v] + Math.abs(l[p] - l[v]), dpR[v] + Math.abs(l[p] - r[v]));
                dpR[p] += Math.max(dpL[v] + Math.abs(r[p] - l[v]), dpR[v] + Math.abs(r[p] - r[v]));
            }
            sb.append(Math.max(dpL[1], dpR[1])).append('\n');
        }
        System.out.print(sb);
    }
}
