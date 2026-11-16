import java.io.*;
import java.util.*;

class Main {
    public static void main(String[] args) throws IOException {
        DataInputStream in = new DataInputStream(new BufferedInputStream(System.in, 1 << 16));
        int t = nextInt(in);
        StringBuilder out = new StringBuilder();
        while (t-- > 0) {
            int n = nextInt(in);
            int[][] val = new int[5][n];
            for (int i = 0; i < n; i++) {
                int[] cnt = new int[5];
                int len = 0;
                int b = in.read();
                while (b < 'a' || b > 'e') b = in.read();
                while (b >= 'a' && b <= 'e') {
                    cnt[b - 'a']++;
                    len++;
                    b = in.read();
                }
                for (int c = 0; c < 5; c++) val[c][i] = 2 * cnt[c] - len;
            }
            int best = 0;
            for (int c = 0; c < 5; c++) {
                int[] v = val[c];
                Arrays.sort(v);
                long sum = 0;
                int taken = 0;
                for (int i = n - 1; i >= 0; i--) {
                    if (sum + v[i] > 0) {
                        sum += v[i];
                        taken++;
                    } else {
                        break;
                    }
                }
                best = Math.max(best, taken);
            }
            out.append(best).append('\n');
        }
        System.out.print(out);
    }

    private static int nextInt(DataInputStream in) throws IOException {
        int b = in.read();
        while (b < '0' || b > '9') b = in.read();
        int r = 0;
        while (b >= '0' && b <= '9') {
            r = r * 10 + (b - '0');
            b = in.read();
        }
        return r;
    }
}
