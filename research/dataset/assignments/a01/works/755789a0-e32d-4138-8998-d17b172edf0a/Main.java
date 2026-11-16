
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.io.DataInputStream;
import java.io.FileInputStream;

import java.io.IOException;

public class ParsasHumongousTree {

    public static void main(String args[]) throws IOException {
        Reader scan = new Reader();
        StringBuilder sb = new StringBuilder();

        int t = scan.nextInt();
        while (t-- > 0) {
            int n = scan.nextInt();
            int[] l = new int[n + 1];
            int[] r = new int[n + 1];
            for (int i = 1; i <= n; i++) {
                l[i] = scan.nextInt();
                r[i] = scan.nextInt();
            }
            Graph g = new Graph(n);
            for (int i = 0; i < n - 1; i++) {
                g.addEdge(scan.nextInt(), scan.nextInt());
            }
            sb.append(g.dfs(l, r) + "\n");
        }
        System.out.println(sb);
    }
}

class Graph {

    ArrayList<Integer>[] node;
    int n;
    int c = 0;
    boolean[] vis;

    Graph(int s) {
        n = s + 1;
        vis = new boolean[n + 1];
        node = new ArrayList[n + 1];
        for (int i = 0; i < n + 1; i++) {
            node[i] = new ArrayList<>();
        }
    }

    void addEdge(int u, int v) {

        node[u].add(v);
        node[v].add(u);
        if (node[u].size() == 1) {
            c = u;
        }
        if (node[v].size() == 1) {
            c = v;
        }
    }

    void cleanVisArray() {
        for (int i = 0; i < n + 1; i++) {
            vis[i] = false;
        }
    }

    long dfs(int[] l, int[] r) {
        cleanVisArray();

        long[][] dp = new long[n][2];
        dfsMain(1, dp, l, r);
        return Math.max(dp[1][0], dp[1][1]);
    }

    void dfsMain(int v, long[][] dp, int[] l, int[] r) {
        vis[v] = true;
        for (int i : node[v]) {
            if (!vis[i]) {
                dfsMain(i, dp, l, r);
                dp[v][0] += Math.max(Math.abs(l[v] - l[i]) + dp[i][0], Math.abs(l[v] - r[i]) + dp[i][1]);
                dp[v][1] += Math.max(Math.abs(r[v] - l[i]) + dp[i][0], Math.abs(r[v] - r[i]) + dp[i][1]);
            }
        }
    }

}

class Reader {

    final private int BUFFER_SIZE = 1 << 16;
    private DataInputStream din;
    private byte[] buffer;
    private int bufferPointer, bytesRead;

    public Reader() {
        din = new DataInputStream(System.in);
        buffer = new byte[BUFFER_SIZE];
        bufferPointer = bytesRead = 0;
    }

    public Reader(String file_name) throws IOException {
        din = new DataInputStream(new FileInputStream(file_name));
        buffer = new byte[BUFFER_SIZE];
        bufferPointer = bytesRead = 0;
    }

    public String readLine() throws IOException {
        byte[] buf = new byte[64]; // line length 
        int cnt = 0, c;
        while ((c = read()) != -1) {
            if (c == '\n') {
                break;
            }
            buf[cnt++] = (byte) c;
        }
        return new String(buf, 0, cnt);
    }

    public int nextInt() throws IOException {
        int ret = 0;
        byte c = read();
        while (c <= ' ') {
            c = read();
        }
        boolean neg = (c == '-');
        if (neg) {
            c = read();
        }
        do {
            ret = ret * 10 + c - '0';
        } while ((c = read()) >= '0' && c <= '9');

        if (neg) {
            return -ret;
        }
        return ret;
    }

    public long nextLong() throws IOException {
        long ret = 0;
        byte c = read();
        while (c <= ' ') {
            c = read();
        }
        boolean neg = (c == '-');
        if (neg) {
            c = read();
        }
        do {
            ret = ret * 10 + c - '0';
        } while ((c = read()) >= '0' && c <= '9');
        if (neg) {
            return -ret;
        }
        return ret;
    }

    public double nextDouble() throws IOException {
        double ret = 0, div = 1;
        byte c = read();
        while (c <= ' ') {
            c = read();
        }
        boolean neg = (c == '-');
        if (neg) {
            c = read();
        }

        do {
            ret = ret * 10 + c - '0';
        } while ((c = read()) >= '0' && c <= '9');

        if (c == '.') {
            while ((c = read()) >= '0' && c <= '9') {
                ret += (c - '0') / (div *= 10);
            }
        }

        if (neg) {
            return -ret;
        }
        return ret;
    }

    private void fillBuffer() throws IOException {
        bytesRead = din.read(buffer, bufferPointer = 0, BUFFER_SIZE);
        if (bytesRead == -1) {
            buffer[0] = -1;
        }
    }

    private byte read() throws IOException {
        if (bufferPointer == bytesRead) {
            fillBuffer();
        }
        return buffer[bufferPointer++];
    }

    public void close() throws IOException {
        if (din == null) {
            return;
        }
        din.close();
    }
}
