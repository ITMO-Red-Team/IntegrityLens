import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        DataInputStream in = new DataInputStream(new BufferedInputStream(System.in, 1 << 16));
        int n = nextInt(in);
        int[] occ = new int[n];
        int[] free = new int[n];
        int k = 0, m = 0;
        for (int i = 1; i <= n; i++) {
            int a = nextInt(in);
            if (a == 1) occ[k++] = i;
            else free[m++] = i;
        }
        final long INF = Long.MAX_VALUE / 4;
        // dp[j] = min cost to match first i occupied using first j free chairs
        long[] dp = new long[m + 1];
        Arrays.fill(dp, 0);
        for (int i = 1; i <= k; i++) {
            long[] nd = new long[m + 1];
            nd[0] = INF;
            for (int j = 1; j <= m; j++) {
                long best = nd[j - 1];
                if (dp[j - 1] < INF) {
                    long c = dp[j - 1] + Math.abs(occ[i - 1] - free[j - 1]);
                    if (c < best) best = c;
                }
                nd[j] = best;
            }
            dp = nd;
        }
        System.out.println(dp[m]);
    }

    private static int nextInt(DataInputStream in) throws IOException {
        int ret = 0;
        int b = in.read();
        while (b < '0' || b > '9') {
            if (b == '-') {
                b = in.read();
                int r = 0;
                while (b >= '0' && b <= '9') {
                    r = r * 10 + (b - '0');
                    b = in.read();
                }
                return -r;
            }
            b = in.read();
        }
        while (b >= '0' && b <= '9') {
            ret = ret * 10 + (b - '0');
            b = in.read();
        }
        return ret;
    }
}
