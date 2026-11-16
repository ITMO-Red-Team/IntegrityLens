import java.io.*;
import java.util.*;

public class Main {
    static DataInputStream in;
    static byte[] buf = new byte[1 << 16];
    static int bufLen = 0, bufPtr = 0;

    static int readByte() throws IOException {
        if (bufPtr == bufLen) {
            bufLen = in.read(buf, 0, buf.length);
            bufPtr = 0;
            if (bufLen <= 0) return -1;
        }
        return buf[bufPtr++];
    }

    static int readInt() throws IOException {
        int c = readByte();
        while (c != '-' && (c < '0' || c > '9')) c = readByte();
        boolean neg = false;
        if (c == '-') { neg = true; c = readByte(); }
        int r = 0;
        while (c >= '0' && c <= '9') {
            r = r * 10 + (c - '0');
            c = readByte();
        }
        return neg ? -r : r;
    }

    public static void main(String[] args) throws IOException {
        in = new DataInputStream(new BufferedInputStream(System.in, 1 << 16));
        int t = readInt();
        StringBuilder sb = new StringBuilder();
        while (t-- > 0) {
            int n = readInt();
            int[][] bal = new int[5][n];
            for (int i = 0; i < n; i++) {
                int c = readByte();
                while (c < 'a' || c > 'e') c = readByte();
                int[] cnt = new int[5];
                int len = 0;
                while (c >= 'a' && c <= 'e') {
                    cnt[c - 'a']++;
                    len++;
                    c = readByte();
                }
                for (int k = 0; k < 5; k++) bal[k][i] = 2 * cnt[k] - len;
            }
            int best = 0;
            for (int k = 0; k < 5; k++) {
                int[] a = bal[k];
                Arrays.sort(a);
                long sum = 0;
                int taken = 0;
                for (int i = n - 1; i >= 0; i--) {
                    sum += a[i];
                    if (sum > 0) taken++;
                    else break;
                }
                if (taken > best) best = taken;
            }
            sb.append(best).append('\n');
        }
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
        out.print(sb);
        out.flush();
    }
}
