import java.io.*;
import java.util.*;

public class Test {
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static StringTokenizer st;

    static boolean[] vis;
    static ArrayList<Integer>[] adj;
    static int[] l, r;
    static long[][] dp;

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException {
        int T = readInt();
        for (int t = 0; t < T; t++) {
            int n = readInt();
            l = new int[n + 1];
            r = new int[n + 1];
            for (int i = 1; i <= n; i++) {
                l[i] = readInt();
                r[i] = readInt();
            }

            adj = new ArrayList[n + 1];
            for (int i = 1; i <= n; i++) adj[i] = new ArrayList<>();
            for (int i = 0; i < n - 1; i++) {
                int u = readInt(), v = readInt();
                adj[u].add(v);
                adj[v].add(u);
            }

            dp = new long[n + 1][2];
            vis = new boolean[n + 1];
            vis[1] = true;
            dfs(1);
            System.out.println(Math.max(dp[1][0], dp[1][1]));
        }
    }

    static void dfs(int u) {
        for (int x : adj[u]) {
            if (!vis[x]) {
                vis[x] = true;
                dfs(x);
                dp[u][0] += Math.max(dp[x][0] + Math.abs(l[u] - l[x]), dp[x][1] + Math.abs(l[u] - r[x]));
                dp[u][1] += Math.max(dp[x][0] + Math.abs(r[u] - l[x]), dp[x][1] + Math.abs(r[u] - r[x]));
            }
        }
    }

    static String next() throws IOException {
        while (st == null || !st.hasMoreTokens())
            st = new StringTokenizer(br.readLine().trim());
        return st.nextToken();
    }

    static int readInt() throws IOException {
        return Integer.parseInt(next());
    }
}
