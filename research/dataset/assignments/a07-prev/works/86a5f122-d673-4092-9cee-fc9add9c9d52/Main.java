import java.util.*;
import java.io.*;
import java.lang.*;

public class Solution {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    PrintWriter out = new PrintWriter(System.out);
    StringTokenizer st = new StringTokenizer("");

    String next() {
        if (!st.hasMoreTokens()) {
            try {
                st = new StringTokenizer(br.readLine());
            } catch (Exception e) {

            }
        }
        return st.nextToken();
    }

    int nextInt() {
        return Integer.parseInt(next());
    }

    long nextLong() {
        return Long.parseLong(next());
    }

    public static void main(String[] args) {
        new Solution().solve();
    }

    long res = Long.MAX_VALUE;
    List<Integer> l = new ArrayList<>(), r = new ArrayList<>();
    long dp[][];

    long ok(int i, int j) {
        if (dp[i][j] != -1)
            return dp[i][j];
        if (i >= l.size()) {
            return 0;
        }
        if (j >= r.size())
            return Integer.MAX_VALUE;
        long op1 = ok(i + 1, j + 1) + Math.abs(l.get(i) - r.get(j));
        long op2 = ok(i, j + 1);

        return dp[i][j] = Math.min(op1, op2);
    }

    void solve() {
        int t = 1;
        // t = nextInt();
        for (int tt = 0; tt < t; tt++) {
            int n = nextInt();
            int arr[] = new int[n];
            for (int i = 0; i < n; i++) {
                arr[i] = nextInt();
                if (arr[i] == 0)
                    r.add(i + 1);
                else
                    l.add(i + 1);
            }
            dp = new long[n][n];
            for (int i = 0; i < n; i++)
                Arrays.fill(dp[i], -1);
            out.println(ok(0, 0));
        }
        out.close();
    }
}