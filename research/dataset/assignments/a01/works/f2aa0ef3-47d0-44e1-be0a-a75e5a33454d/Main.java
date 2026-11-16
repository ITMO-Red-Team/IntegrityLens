import java.io.*;
import java.util.*;
 
public class MySolution {
    public static void main(String[] args) throws Exception {
        BufferedReader bu = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder out = new StringBuilder();
        int numOfTestCases = Integer.parseInt(bu.readLine());
        for (int tc = 1; tc <= numOfTestCases; tc++) {
            int vertices = Integer.parseInt(bu.readLine());
            connections = new ArrayList[vertices];
            for (int i = 0; i < vertices; i++) {
                connections[i] = new ArrayList<Integer>();
                String st[] = bu.readLine().split(" ");
                a[i][0] = Integer.parseInt(st[0]);
                a[i][1] = Integer.parseInt(st[1]);
                s[i][0] = s[i][1] = 0;
            }
            for (int j = 0; j < vertices-1; j++) {
                String st[] = bu.readLine().split(" ");
                int u = Integer.parseInt(st[0]) - 1, v = Integer.parseInt(st[1]) - 1;
                connections[u].add(v);
                connections[v].add(u);
            }
            dfs(0, -1);
            out.append(Math.max(s[0][0], s[0][1]) + "\n");
        }
        System.out.print(out);
    }
 
    static int N = 100000;
    static int[][] a = new int[N][2];
    static long[][] s = new long[N][2];
    static ArrayList<Integer>[] connections;
 
    public static void dfs(int n, int parent) {
        for (int child : connections[n]) {
            if (child != parent) {
                dfs(child, n);
                s[n][0] += Math.max(s[child][0] + Math.abs(a[n][0] - a[child][0]), s[child][1] + Math.abs(a[n][0] - a[child][1]));
                s[n][1] += Math.max(s[child][0] + Math.abs(a[n][1] - a[child][0]), s[child][1] + Math.abs(a[n][1] - a[child][1]));
            }
        }
    }
}
