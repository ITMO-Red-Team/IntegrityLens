import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class C {
    public static void main(String[] args) {
        new C().solve(System.in, System.out);
    }

    public void solve(InputStream in, OutputStream out) {
        InputReader inputReader = new InputReader(in);
        PrintWriter writer = new PrintWriter(new BufferedOutputStream(out));

        int t = inputReader.nextInt();
        for (int t1 = 0; t1 < t; t1++) {
            int n = inputReader.nextInt();
            List<Long> c = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                c.add(inputReader.nextLong());
            }
            writer.println(solve(n, c));
        }

        writer.close();
    }

    public long solve(int n, List<Long> c) {
        long[] minEven = new long[n];
        long[] minOdd = new long[n];
        long[] sumOdd = new long[n];
        long[] sumEven = new long[n];

        minEven[0] = Long.MAX_VALUE;
        minOdd[0] = Long.MAX_VALUE;

        for (int i = 0; i < n; i++) {
            if (i > 0) {
                minEven[i] = minEven[i - 1];
                minOdd[i] = minOdd[i - 1];
                sumOdd[i] = sumOdd[i - 1];
                sumEven[i] = sumEven[i - 1];
            }
            if (i % 2 == 0) {
                minEven[i] = Math.min(minEven[i], c.get(i));
                sumEven[i] += c.get(i);
            } else {
                minOdd[i] = Math.min(minOdd[i], c.get(i));
                sumOdd[i] += c.get(i);
            }
        }

        long best = Long.MAX_VALUE;
        for (int k = 1; k < n; k++) {
            int countOdd = (k + 1) / 2;
            int countEven = (k + 1) / 2;
            if (k % 2 == 0) {
                countEven++;
            }
            long oddResult = minOdd[k] * (n - countOdd) + sumOdd[k];
            long evenResult = minEven[k] * (n - countEven) + sumEven[k];
            long current = oddResult + evenResult;
            best = Math.min(best, current);
        }
        return best;
    }

    private static class InputReader {
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
    }
}
