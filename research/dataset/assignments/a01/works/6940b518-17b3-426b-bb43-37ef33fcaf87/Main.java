import java.io.*;
import java.util.*;

public class Main {

    static public void dfs(ArrayList<ArrayList<Integer>> g, int u, long[][] dp, long[] l, long[] r, int p) {
        dp[u][0] = 1;
        for (int i = 0; i < g.get(u).size(); i++) {
            int v = g.get(u).get(i);
            if (v == p) continue;
            if (dp[v][0] == 0) {
                dfs(g, v, dp, l, r, u);
            }
            dp[u][1] += Math.max(Math.abs(l[u] - l[v]) + dp[v][1], Math.abs(l[u] - r[v]) + dp[v][2]);
            dp[u][2] += Math.max(Math.abs(r[u] - l[v]) + dp[v][1], Math.abs(r[u] - r[v]) + dp[v][2]);
        }
    }

    static public void main(String args[]) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        StringTokenizer st = new StringTokenizer(br.readLine());
        int T = Integer.parseInt(st.nextToken());
        for (int t = 1; t <= T; t++) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            long[] l = new long[n + 1], r = new long[n + 1];
            ArrayList<ArrayList<Integer>> g = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                st = new StringTokenizer(br.readLine());
                l[i + 1] = Integer.parseInt(st.nextToken());
                r[i + 1] = Integer.parseInt(st.nextToken());
                g.add(new ArrayList<>());
            }
            g.add(new ArrayList<>());

            for (int i = 0; i < n - 1; i++) {
                st = new StringTokenizer(br.readLine());
                int u = Integer.parseInt(st.nextToken()), v = Integer.parseInt(st.nextToken());
                g.get(v).add(u);
                g.get(u).add(v);
            }
            long[][] dp = new long[n + 1][3];
            dfs(g, 1 , dp, l, r, -1);
            bw.write(Math.max(dp[1][1], dp[1][2]) + "\n");
        }
        bw.flush();
    }
}
