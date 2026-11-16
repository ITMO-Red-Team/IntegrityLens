import java.io.*;
import java.util.*;


public class Agad {
    public static void main(String[] args) throws IOException {
        FastReader reader = new FastReader();
        PrintWriter writer = new PrintWriter(System.out);

        int t = reader.nextInt();
        while(t-- > 0) {
            n = reader.nextInt();
            lr = new int[n][2];
            for (int i = 0; i < n; i++) {
                lr[i][0] = reader.nextInt(); lr[i][1] = reader.nextInt();
            }
            g = new ArrayList[n];
            for (int i = 0; i < n; i++) g[i] = new ArrayList<>();
            for (int i = 1; i < n; i++) {
                int a = reader.nextInt() - 1, b = reader.nextInt() - 1;
                g[a].add(b); g[b].add(a);
            }
            long[] dp = dfs(0, -1);
            writer.write(Math.max(dp[0], dp[1]) + "\n");
        }        

        writer.flush();
        writer.close();
    }

    static int n;
    static int[][] lr;
    static ArrayList<Integer>[] g;

    static long[] dfs(int v, int p) {
        long[] dp = new long[2];
        for (int ch : g[v]) if (ch != p) {
            long[] tmp = dfs(ch, v);
            dp[0] += Math.max(tmp[0] + Math.abs(lr[v][0] - lr[ch][0]), tmp[1] + Math.abs(lr[v][0] - lr[ch][1]));
            dp[1] += Math.max(tmp[0] + Math.abs(lr[v][1] - lr[ch][0]), tmp[1] + Math.abs(lr[v][1] - lr[ch][1]));
        }
        return dp;
    }

    static class FastReader
    {
        BufferedReader br;
        StringTokenizer st;

        public FastReader()
        {
            br = new BufferedReader(new
                    InputStreamReader(System.in));
        }

        String next()
        {
            while (st == null || !st.hasMoreElements())
            {
                try
                {
                    st = new StringTokenizer(br.readLine());
                }
                catch (IOException  e)
                {
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }

        int nextInt()
        {
            return Integer.parseInt(next());
        }

        long nextLong()
        {
            return Long.parseLong(next());
        }

        double nextDouble()
        {
            return Double.parseDouble(next());
        }

        String nextLine()
        {
            String str = "";
            try
            {
                str = br.readLine();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return str;
        }
    }
}

