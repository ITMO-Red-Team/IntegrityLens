import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

//6 4 1 1 4 1

//-3 4 -2 0 -4 6 1
// 4    6 1
//-4 -3 -2 0 1 4 6
public class C1 {
    private static void solve(List<Integer>[] g, int[][] a) {
        long[] dfs = dfs(g, a, 0, -1);
        System.out.println(Math.max(dfs[0], dfs[1]));
    }

    private static long[] dfs(List<Integer>[] g, int[][] a, int v, int p) {
        long lans = 0;
        long rans = 0;
        for (Integer i : g[v]) {
            if (i != p) {
                long[] dfs = dfs(g, a, i, v);
                lans += Math.max(Math.abs(a[v][0] - a[i][1]) + dfs[1], Math.abs(a[v][0] - a[i][0]) + dfs[0]);
                rans += Math.max(Math.abs(a[v][1] - a[i][1]) + dfs[1], Math.abs(a[v][1] - a[i][0]) + dfs[0]);

            }
        }
        return new long[]{lans, rans};
    }

    public static void main(String[] args) {
        FastScanner sc = new FastScanner();
        int t = sc.nextInt();
        for (int i = 0; i < t; i++) {
            int n = sc.nextInt();
            int[][] a = new int[n][2];
            for (int j = 0; j < n; j++) {
                a[j] = new int[]{sc.nextInt(), sc.nextInt()};
            }
            List<Integer>[] g = new ArrayList[n];
            for (int j = 0; j < n; j++) {
                g[j] = new ArrayList<>();
            }
            for (int j = 0; j < n - 1; j++) {
                int u = sc.nextInt() - 1;
                int v = sc.nextInt() - 1;
                g[u].add(v);
                g[v].add(u);
            }
            solve(g, a);
        }
    }


    static class FastScanner {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer("");

        String next() {
            while (!st.hasMoreTokens())
                try {
                    st = new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            return st.nextToken();
        }

        int nextInt() {
            return Integer.parseInt(next());
        }

        long[] readArrayLong(int n) {
            long[] a = new long[n];
            for (int i = 0; i < n; i++)
                a[i] = nextLong();
            return a;
        }

        int[] readArrayInt(int n) {
            int[] a = new int[n];
            for (int i = 0; i < n; i++)
                a[i] = nextInt();
            return a;
        }

        long nextLong() {
            return Long.parseLong(next());
        }

        double nextDouble() {
            return Double.parseDouble(next());
        }
    }
}