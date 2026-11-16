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
            long[] st = new long[n];
            long[] en = new long[n];
            int top = 0;
            for (int i = 0; i < n; i++) {
                long a = k[i] - h[i] + 1;
                long b = k[i];
                while (top > 0 && en[top - 1] >= a) {
                    top--;
                    a = Math.min(a, st[top]);
                }
                st[top] = a;
                en[top] = b;
                top++;
            }
            long ans = 0;
            for (int i = 0; i < top; i++) {
                long len = en[i] - st[i] + 1;
                ans += len * (len + 1) / 2;
            }
            sb.append(ans).append('\n');
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
