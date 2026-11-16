import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        DataInputStream in = new DataInputStream(new BufferedInputStream(System.in, 1 << 16));
        int n = nextInt(in);
        int[] ones = new int[n];
        int[] zeros = new int[n];
        int k = 0, m = 0;
        for (int i = 1; i <= n; i++) {
            int a = nextInt(in);
            if (a == 1) ones[k++] = i;
            else zeros[m++] = i;
        }
        final long INF = Long.MAX_VALUE / 4;
        // dp[i] = min cost to match first i ones using processed zeros
        long[] dp = new long[k + 1];
        Arrays.fill(dp, INF);
        dp[0] = 0;
        for (int j = 0; j < m; j++) {
            int z = zeros[j];
            int upper = Math.min(k, j + 1);
            for (int i = upper; i >= 1; i--) {
                if (dp[i - 1] < INF) {
                    long c = dp[i - 1] + Math.abs(ones[i - 1] - z);
                    if (c < dp[i]) dp[i] = c;
                }
            }
        }
        System.out.println(dp[k]);
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
