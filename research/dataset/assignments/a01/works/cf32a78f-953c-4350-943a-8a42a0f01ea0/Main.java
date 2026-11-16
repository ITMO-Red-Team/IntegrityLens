import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;

public class Main {
    public static void main(String[] args) {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;
        Scanner in = new Scanner(inputStream);
        PrintWriter out = new PrintWriter(outputStream);
        CParsasHumongousTree solver = new CParsasHumongousTree();
        solver.solve(1, in, out);
        out.close();
    }

    static class CParsasHumongousTree {
        int n;
        long[] l;
        long[] r;
        ArrayList<Integer>[] adj;
        long[][] dp;

        public void solve(int testNumber, Scanner sc, PrintWriter pw) {
            int t = 1;
            t = sc.nextInt();
            while (t-- > 0) {
                n = sc.nextInt();
                l = new long[n];
                r = new long[n];
                for (int i = 0; i < n; i++) {
                    l[i] = sc.nextInt();
                    r[i] = sc.nextInt();
                }
                adj = new ArrayList[n];
                for (int i = 0; i < n; i++) adj[i] = new ArrayList<>();
                for (int i = 0; i < n - 1; i++) {
                    int x = sc.nextInt() - 1;
                    int y = sc.nextInt() - 1;
                    adj[x].add(y);
                    adj[y].add(x);
                }
                dp = new long[n + 1][2];
                for (long[] x : dp) Arrays.fill(x, -1);
                long ans = solve(-1, 0, 0);
                for (long[] x : dp) Arrays.fill(x, -1);
                ans = Math.max(ans, solve(-1, 0, 1));
                pw.println(ans);
            }

        }

        long solve(int from, int i, int f) {
            if (dp[i][f] != -1) return dp[i][f];
            long ans = 0;
            long a = f == 0 ? l[i] : r[i];
            for (int x : adj[i]) {
                if (x != from)
                    ans += Math.max((Math.abs(l[x] - a) + solve(i, x, 0)), (Math.abs(r[x] - a) + solve(i, x, 1)));
            }
            return dp[i][f] = ans;
        }

    }

    static class Scanner {
        StringTokenizer st;
        BufferedReader br;

        public Scanner(FileReader r) {
            br = new BufferedReader(r);
        }

        public Scanner(InputStream s) {
            br = new BufferedReader(new InputStreamReader(s));
        }

        public String next() {
            while (st == null || !st.hasMoreTokens()) {
                try {
                    st = new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return st.nextToken();
        }

        public int nextInt() {
            return Integer.parseInt(next());
        }

    }
}

