import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        DataInputStream in = new DataInputStream(new BufferedInputStream(System.in, 1 << 16));
        int t = nextInt(in);
        StringBuilder sb = new StringBuilder();
        while (t-- > 0) {
            int n = nextInt(in);
            int m = nextInt(in);
            int x = nextInt(in);
            int[] h = new int[n];
            for (int i = 0; i < n; i++) h[i] = nextInt(in);

            // min-heap of towers by current height: {height, index}
            PriorityQueue<long[]> pq = new PriorityQueue<>((a, b) -> Long.compare(a[0], b[0]));
            for (int j = 1; j <= m; j++) pq.add(new long[]{0, j});

            int[] ans = new int[n];
            for (int i = 0; i < n; i++) {
                long[] top = pq.poll();
                ans[i] = (int) top[1];
                top[0] += h[i];
                pq.add(top);
            }

            sb.append("YES\n");
            for (int i = 0; i < n; i++) {
                sb.append(ans[i]);
                sb.append(i + 1 < n ? ' ' : '\n');
            }
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
