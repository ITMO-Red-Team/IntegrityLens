import java.io.*;
import java.util.*;

public class Armchairs {
    static final int INF = 1000000000;
    public static void main(String[] args) {
        InputReader reader = new InputReader(System.in);
        PrintWriter writer = new PrintWriter(System.out, false);
        int N = reader.nextInt();
        int[] A = new int[N];
        for (int i = 0; i < N; i++) {
            A[i] = reader.nextInt();
        }
        List<Integer> occupied = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            if (A[i] == 1) occupied.add(i);
        }
        int K = occupied.size();
        int[][] dp = new int[N + 1][K + 1];
        for (int[] row : dp) Arrays.fill(row, INF);
        dp[0][0] = 0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j <= K; j++) {
                int x = j < K ? occupied.get(j) : 0;
                dp[i + 1][j] = Math.min(dp[i + 1][j], dp[i][j]);
                if (j + 1 <= K && A[i] == 0) {
                    dp[i + 1][j + 1] = Math.min(dp[i + 1][j + 1], dp[i][j] + Math.abs(i - x));
                }
            }
        }
        writer.println(dp[N][K]);
        writer.close();
        System.exit(0);
    }
    static class InputReader {
        public BufferedReader reader;
        public StringTokenizer tokenizer;
        public InputReader(InputStream stream) {
            reader = new BufferedReader(new InputStreamReader(stream), 32768);
            tokenizer = null;
        }
        public String next() {
            while (tokenizer == null || !tokenizer.hasMoreTokens()) {
                try {
                    tokenizer = new StringTokenizer(reader.readLine());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return tokenizer.nextToken();
        }
        public int nextInt() {
            return Integer.parseInt(next());
        }
        public long nextLong() {
            return Long.parseLong(next());
        }
        public double nextDouble() {
            return Double.parseDouble(next());
        }
        public String nextLine() {
            String str = "";
            try {
                str = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return str;
        }
    }
}