import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        DataInputStream in = new DataInputStream(new BufferedInputStream(System.in, 1 << 16));
        int t = nextInt(in);
        StringBuilder sb = new StringBuilder();
        while (t-- > 0) {
            int n = nextInt(in);
            long[] c = new long[n];
            for (int i = 0; i < n; i++) c[i] = nextInt(in);

            long[] sum = new long[2];
            long[] mn = {Long.MAX_VALUE, Long.MAX_VALUE};
            long[] cnt = new long[2];
            long best = Long.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                int p = i & 1;
                sum[p] += c[i];
                cnt[p]++;
                mn[p] = Math.min(mn[p], c[i]);
                if (i >= 1) {
                    long cost = sum[0] + mn[0] * (n - cnt[0]) + sum[1] + mn[1] * (n - cnt[1]);
                    best = Math.min(best, cost);
                }
            }
            sb.append(best).append('\n');
        }
        System.out.print(sb);
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
