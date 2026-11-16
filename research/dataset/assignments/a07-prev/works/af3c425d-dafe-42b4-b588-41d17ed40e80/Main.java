import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
 
public class taskd {
    public static void main(String[] args) {
        FastScanner in = new FastScanner();
        PrintWriter out = new PrintWriter(System.out);
        taskd sol = new taskd();
        sol.solve(in, out);
        out.flush();
    }

    void solve(FastScanner in, PrintWriter out) {
        int n = in.nextInt();
        ArrayList<Integer> a = new ArrayList<>();
        ArrayList<Integer> b = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int x = in.nextInt();
            if (x == 1) {
                a.add(i);
            } else {
                b.add(i);
            }
        }

        long dp[][] = new long[a.size() + 5][b.size() + 5];
        for (int i = a.size()-1; i >= 0; i--) {
            dp[i][b.size()] = Integer.MAX_VALUE;
            for (int j = b.size()-1; j >= 0; j--) {
                dp[i][j] = dp[i][j + 1];
                dp[i][j] = Math.min(dp[i][j], Math.abs(a.get(i) - b.get(j)) + dp[i + 1][j + 1]);
            }
        }
        out.println(dp[0][0]);
    }

    static class FastScanner {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer("");
        String next() {
            while (!st.hasMoreTokens()) {
                try {
                    st = new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }
        int nextInt() { return Integer.parseInt(next()); }
        long nextLong() { return Long.parseLong(next()); }
        float nextFloat() { return Float.parseFloat(next()); }
        double nextDouble() { return Double.parseDouble(next()); }
    }
}