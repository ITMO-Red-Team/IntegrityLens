import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        DataInputStream in = new DataInputStream(new BufferedInputStream(System.in, 1 << 16));
        int t = nextInt(in);
        StringBuilder sb = new StringBuilder();
        while (t-- > 0) {
            int n = nextInt(in);
            byte[] a = nextToken(in, n);
            byte[] b = nextToken(in, n);
            int c00 = 0, c01 = 0, c10 = 0, c11 = 0;
            for (int i = 0; i < n; i++) {
                if (a[i] == '1') {
                    if (b[i] == '1') c11++; else c10++;
                } else {
                    if (b[i] == '1') c01++; else c00++;
                }
            }
            long best = Long.MAX_VALUE;
            if (c01 == c10) best = Math.min(best, c01 + c10);
            if (c11 == c00 + 1) best = Math.min(best, c11 + c00);
            sb.append(best == Long.MAX_VALUE ? -1 : best).append('\n');
        }
        System.out.print(sb);
    }

    private static int readByte(DataInputStream in) throws IOException {
        return in.read();
    }

    private static int nextInt(DataInputStream in) throws IOException {
        int c = readByte(in);
        while (c != '-' && (c < '0' || c > '9')) c = readByte(in);
        boolean neg = false;
        if (c == '-') { neg = true; c = readByte(in); }
        int r = 0;
        while (c >= '0' && c <= '9') {
            r = r * 10 + (c - '0');
            c = readByte(in);
        }
        return neg ? -r : r;
    }

    private static byte[] nextToken(DataInputStream in, int n) throws IOException {
        byte[] s = new byte[n];
        int c = readByte(in);
        while (c == ' ' || c == '\n' || c == '\r' || c == '\t') c = readByte(in);
        int i = 0;
        while (c != -1 && c != ' ' && c != '\n' && c != '\r' && c != '\t') {
            if (i < n) s[i++] = (byte) c;
            c = readByte(in);
        }
        return s;
    }
}
