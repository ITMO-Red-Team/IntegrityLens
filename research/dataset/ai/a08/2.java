import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        DataInputStream in = new DataInputStream(new BufferedInputStream(System.in, 1 << 16));
        int t = nextInt(in);
        StringBuilder sb = new StringBuilder();
        while (t-- > 0) {
            int n = nextInt(in);
            long[] k = new long[n];
            long[] h = new long[n];
            for (int i = 0; i < n; i++) k[i] = nextInt(in);
            for (int i = 0; i < n; i++) h[i] = nextInt(in);
            long total = 0;
            long end = k[n - 1];
            long start = k[n - 1] - h[n - 1] + 1;
            for (int i = n - 2; i >= 0; i--) {
                if (k[i] >= start) {
                    start = Math.min(start, k[i] - h[i] + 1);
                } else {
                    long len = end - start + 1;
                    total += len * (len + 1) / 2;
                    end = k[i];
                    start = k[i] - h[i] + 1;
                }
            }
            long len = end - start + 1;
            total += len * (len + 1) / 2;
            sb.append(total).append('\n');
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
