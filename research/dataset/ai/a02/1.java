import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer("");
        StringBuilder out = new StringBuilder();
        int t = Integer.parseInt(next(br, st = refill(br, st)));
        for (int tc = 0; tc < t; tc++) {
            st = refill(br, st);
            int n = Integer.parseInt(st.nextToken());
            st = refill(br, st);
            String a = st.nextToken();
            st = refill(br, st);
            String b = st.nextToken();

            int diff = 0, onesA = 0, onesB = 0;
            for (int i = 0; i < n; i++) {
                char ca = a.charAt(i), cb = b.charAt(i);
                if (ca != cb) diff++;
                if (ca == '1') onesA++;
                if (cb == '1') onesB++;
            }

            int best = Integer.MAX_VALUE;
            // even number of operations: each mismatched position needs flipping
            if (onesA == onesB) best = Math.min(best, diff);
            // odd number of operations: work on the inverted target
            if (onesA == n - onesB + 1) best = Math.min(best, n - diff);

            out.append(best == Integer.MAX_VALUE ? -1 : best).append('\n');
        }
        System.out.print(out);
    }

    private static StringTokenizer refill(BufferedReader br, StringTokenizer st) throws IOException {
        while (!st.hasMoreTokens()) {
            String line = br.readLine();
            if (line == null) return st;
            st = new StringTokenizer(line);
        }
        return st;
    }

    private static String next(BufferedReader br, StringTokenizer st) {
        return st.nextToken();
    }
}
