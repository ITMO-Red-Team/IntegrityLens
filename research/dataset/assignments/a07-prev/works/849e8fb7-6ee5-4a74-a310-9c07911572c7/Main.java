import java.util.*;
import java.io.*;

public class _1525_D {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        int n = Integer.parseInt(in.readLine());
        int[] a = new int[n];
        StringTokenizer line = new StringTokenizer(in.readLine());
        int c = 0;
        for (int i = 0; i < n; i++) {
            a[i] = Integer.parseInt(line.nextToken());
            if (a[i] == 1)
                c++;
        }
        int[] pos = new int[c];
        int count = 0;
        for (int i = 0; i < n; i++) {
            if (a[i] == 1) {
                pos[count] = i;
                count++;
            }
        }
        int[][] dp = new int[c + 1][n];
        for (int i = 1; i <= c; i++) {
            Arrays.fill(dp[i], Integer.MAX_VALUE);
        }
        for (int i = 1; i <= c; i++) {
            for (int j = 0; j < n; j++) {
                if (j > 0) {
                    if (a[j] == 0) {
                        if (dp[i - 1][j - 1] != Integer.MAX_VALUE) {
                            dp[i][j] = dp[i - 1][j - 1] + Math.abs(j - pos[i - 1]);
                        }
                    }
                    dp[i][j] = Math.min(dp[i][j], dp[i][j - 1]);
                } else {
                    if (a[j] == 0 && i == 1) {
                        dp[i][j] = Math.abs(j - pos[i - 1]);
                    }
                }
            }
        }
        out.println(dp[c][n - 1]);
        in.close();
        out.close();
    }
}