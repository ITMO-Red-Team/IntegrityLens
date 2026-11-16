import java.util.*;
import java.io.*;

public class C{

	private static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    private static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
    private static int vertices;
    private static LinkedList<Integer> adj[];
    private static long dp[][];
    private static int l[];
    private static int r[];
    private static boolean vis[];

    public static void dfs(int v) {
        if(vis[v])
            return;
        vis[v] = true;
        Iterator<Integer> iterator = adj[v].listIterator();
        while(iterator.hasNext()) {
            int child = iterator.next();
            if(!vis[child]) {
                dfs(child);
                long left = dp[child][0] + Math.abs(l[v] - l[child]);
                dp[v][0] += Math.max(left, dp[child][1] + Math.abs(l[v] - r[child]));
                long right = dp[child][0] + Math.abs(r[v] - l[child]);
                dp[v][1] += Math.max(right, dp[child][1] + Math.abs(r[v] - r[child]));
            }
        }
    }

	public static void main (String[] args) throws IOException {
        int t = Integer.parseInt(br.readLine());
        while(t-- > 0) {
            int n = Integer.parseInt(br.readLine());
            vertices = n;
            dp = new long[n+1][2];
            adj = new LinkedList[n+1];
            vis = new boolean[n+1];
            l = new int[n+1];
            r = new int[n+1];
            for(int i=1;i<=n;i++) {
                adj[i] = new LinkedList<>();
            }
            for(int i=1;i<=n;i++) {
                String lr[] = br.readLine().split(" ");
                l[i] = Integer.parseInt(lr[0]);
                r[i] = Integer.parseInt(lr[1]);
            }
            for(int i=0;i<n-1;i++) {
                String uv[] = br.readLine().split(" ");
                int u = Integer.parseInt(uv[0]);
                int v = Integer.parseInt(uv[1]);
                adj[u].add(v);
                adj[v].add(u);
            }
            dfs(1);
            long ans = Math.max(dp[1][0], dp[1][1]);
            bw.write(ans + "\n");
        }
        bw.flush();
    }
}