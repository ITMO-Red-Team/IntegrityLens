import java.io.BufferedReader;
import java.util.StringTokenizer;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class C {


    static int[][] lr = new int[2][(int)2e5+10];
    static long[][] dp = new long[2][(int)2e5+10];
    static ArrayList<ArrayList<Integer>> g;
    public static void main(String[] args) {
        
        FastReader fr = new FastReader();
        PrintWriter out = new PrintWriter(System.out, true);
        int cases = fr.nextInt();
        for(int c = 0; c < cases; c++) {
            int nodes = fr.nextInt();
            g = new ArrayList<ArrayList<Integer>>();
            
            for(int i = 1; i <= nodes; i++) {
                lr[0][i] = fr.nextInt();
                lr[1][i] = fr.nextInt();
            }
            for(int i = 0; i <= nodes; i++) {
                g.add(new ArrayList<Integer>());
            }

            for(int i = 0; i < nodes-1; i++) {
                int f = fr.nextInt();
                int t = fr.nextInt();
                g.get(f).add(t);
                g.get(t).add(f);
            }
            DFS(1, -1);
            out.write(Math.max(dp[0][1], dp[1][1]) + "\n");

        }
        out.close();
    }

    static void DFS(int v, int p) {
        dp[0][v] = dp[1][v] = 0;
        for(Integer u : g.get(v)) {
            if (u == p) continue;
            DFS(u, v);
            dp[0][v] += Math.max(Math.abs(lr[0][v] - lr[1][u]) + dp[1][u], dp[0][u] + Math.abs(lr[0][v] - lr[0][u]));
            dp[1][v] += Math.max(Math.abs(lr[1][v] - lr[1][u]) + dp[1][u], dp[0][u] + Math.abs(lr[1][v] - lr[0][u]));
        }
    }

    static class FastReader {

        BufferedReader br;
        StringTokenizer st;

        public FastReader() {
            this.br = new BufferedReader(new InputStreamReader(System.in));
        }

        String next() { 
            while (st == null || !st.hasMoreElements()) {
                try{
                    st = new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }

        int nextInt() { return Integer.parseInt(next()); }

        long nextLong() { return Long.parseLong(next()); }

        double nextDouble() { return Double.parseDouble(next()); }

        String nextLine() {
            String str = "";
            try {
                str = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return str;
        }
    }
}
