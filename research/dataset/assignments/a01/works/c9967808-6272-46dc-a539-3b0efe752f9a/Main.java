import java.io.*;
import java.util.*;

public class Main {
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;

        public FastReader()
        {
            br = new BufferedReader(
                    new InputStreamReader(System.in));
        }

        String next()
        {
            while (st == null || !st.hasMoreElements()) {
                try {
                    st = new StringTokenizer(br.readLine());
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }

        int nextInt() { return Integer.parseInt(next()); }

        long nextLong() { return Long.parseLong(next()); }

        double nextDouble()
        {
            return Double.parseDouble(next());
        }

        String nextLine()
        {
            String str = "";
            try {
                str = br.readLine();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return str;
        }
    }
    static int N = (int)2e5 +10;
    static ArrayList<ArrayList<Integer>> adj= new ArrayList<>(N);
    static
    {
        for(int i=0; i<N; i++)
        {
            adj.add(new ArrayList<>());
        }
    }
    static int[][] a = new int[2][N];

    static long[][] dp = new long[2][N];

    static void dfs(int v, int p){
        dp[0][v] = 0;
        dp[1][v]=0;

        for(int u: adj.get(v))
        {
            if(u!=p) {


                dfs(u, v);
                dp[0][v] += Math.max(Math.abs(a[0][v] - a[1][u]) + dp[1][u], Math.abs(a[0][v] - a[0][u]) + dp[0][u]);
                dp[1][v] += Math.max(Math.abs(a[1][v] - a[1][u]) + dp[1][u], Math.abs(a[1][v] - a[0][u]) + dp[0][u]);
            }


        }
    }





    public static void main(String[] args) throws IOException{
	// write your code here
        FastReader sc = new FastReader();

        int t = sc.nextInt();
        while(t-->0)
        {
            int n = sc.nextInt();
            for(int i=1; i<=n; i++)
            {
                a[0][i] = sc.nextInt();
                a[1][i] = sc.nextInt();
                adj.set(i, new ArrayList<>());
            }


            for(int i=1; i<n; i++)
            {
                int u = sc.nextInt();
                int v = sc.nextInt();
                adj.get(u).add(v);
                adj.get(v).add(u);
            }
            dfs(1, 0);
            System.out.println(Math.max(dp[0][1], dp[1][1]));
        }

    }
}

