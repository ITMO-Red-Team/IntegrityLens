import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class Exam1 {
    /*
    1. Time complexity: O(t*n*logn).
    2. Space complexity: O(n).
    2. n*t < 10^8 , it should be finished in the time constraint.
     */

    static int[] l, r;
    static List<Integer>[] adj;
    static Long[][] a;
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String s = br.readLine();
        int t = Integer.parseInt(s);
        while (t-- > 0) {
            s = br.readLine();
            int n = Integer.parseInt(s);
            l = new int[n];
            r = new int[n];
            for (int i = 0; i < n; i++) {
                s = br.readLine();
                String[] num = s.split(" ");
                l[i] = Integer.parseInt(num[0]);
                r[i] = Integer.parseInt(num[1]);
            }
            adj = new ArrayList[n];
            for (int i = 0; i < n; i++) adj[i] = new ArrayList<>();
            for (int i = 1; i < n; i++) {
                s = br.readLine();
                String[] num = s.split(" ");
                int u = Integer.parseInt(num[0]) - 1, v = Integer.parseInt(num[1]) - 1;
                adj[u].add(v);
                adj[v].add(u);
            }
            a = new Long[n][2];
            dfs(0, 0);
            System.out.println(Math.max(a[0][0], a[0][1]));
        }
    }
    private static void dfs(int cur, int fa) {
        a[cur][0] = a[cur][1] = 0L;
        for (int i = 0; i < adj[cur].size(); i++) {
            int p = adj[cur].get(i);
            if (p == fa) continue;
            dfs(p, cur);
            a[cur][0] += Math.max(a[p][0] + Math.abs(l[cur] - l[p]), a[p][1] + Math.abs(l[cur] - r[p]));
            a[cur][1] += Math.max(a[p][0] + Math.abs(r[cur] - l[p]), a[p][1] + Math.abs(r[cur] - r[p]));
        }
    }
}
   			 	     	  			 		 	 	 	 		