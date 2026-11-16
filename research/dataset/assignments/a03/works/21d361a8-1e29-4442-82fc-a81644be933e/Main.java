import java.io.*;
import java.util.*;

public class Main {
    private static final long INF = Long.MAX_VALUE / 4;

    public static void main(String[] args) throws IOException {
        FastReader in = new FastReader(System.in);
        int q = in.nextInt();
        StringBuilder sb = new StringBuilder();
        while (q-- > 0) {
            int n = in.nextInt();
            int k = in.nextInt();
            int[] a = new int[k];
            for (int j = 0; j < k; j++) {
                a[j] = in.nextInt();
            }
            long[] best = new long[n + 2];
            Arrays.fill(best, INF);
            for (int j = 0; j < k; j++) {
                long t = in.nextInt();
                if (t < best[a[j]]) {
                    best[a[j]] = t;
                }
            }
            long[] left = new long[n + 2];
            left[0] = INF;
            for (int i = 1; i <= n; i++) {
                left[i] = Math.min(left[i - 1] + 1, best[i]);
            }
            long[] right = new long[n + 2];
            right[n + 1] = INF;
            for (int i = n; i >= 1; i--) {
                right[i] = Math.min(right[i + 1] + 1, best[i]);
            }
            for (int i = 1; i <= n; i++) {
                if (i > 1) {
                    sb.append(' ');
                }
                sb.append(Math.min(left[i], right[i]));
            }
            sb.append('\n');
        }
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
        out.print(sb);
        out.flush();
    }

    private static class FastReader {
        private final DataInputStream stream;
        private final byte[] buffer = new byte[1 << 16];
        private int pointer = 0;
        private int length = 0;

        FastReader(InputStream is) {
            stream = new DataInputStream(is);
        }

        private int read() throws IOException {
            if (pointer == length) {
                length = stream.read(buffer, 0, buffer.length);
                pointer = 0;
                if (length <= 0) {
                    return -1;
                }
            }
            return buffer[pointer++];
        }

        int nextInt() throws IOException {
            int c = read();
            while (c != '-' && (c < '0' || c > '9')) {
                c = read();
            }
            boolean negative = false;
            if (c == '-') {
                negative = true;
                c = read();
            }
            int result = 0;
            while (c >= '0' && c <= '9') {
                result = result * 10 + (c - '0');
                c = read();
            }
            return negative ? -result : result;
        }
    }
}
