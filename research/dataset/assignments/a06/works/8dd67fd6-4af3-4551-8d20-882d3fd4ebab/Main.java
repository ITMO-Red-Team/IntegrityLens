import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Built using CHelper plug-in
 * Actual solution is at the top
 */
public class Main {
    public static void main(String[] args) {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;
        Scanner in = new Scanner(inputStream);
        PrintWriter out = new PrintWriter(outputStream);
        TaskC solver = new TaskC();
        int testCount = Integer.parseInt(in.next());
        for (int i = 1; i <= testCount; i++)
            solver.solve(i, in, out);
        out.close();
    }

    static class TaskC {
        public void solve(int testNumber, Scanner in, PrintWriter out) {
            int n = in.nextInt();
            int[] arr = new int[n];

            for (int i = 0; i < n; i++) {
                arr[i] = in.nextInt();
            }
            long ans = Long.MAX_VALUE;
            // sum so far + min*(n-k)
            long[] evenSum = new long[n];
            long[] oddSum = new long[n];
            int[] evenMin = new int[n];
            int[] oddMin = new int[n];
            evenSum[0] = arr[0];
            oddSum[1] = arr[1];
            evenMin[0] = arr[0];
            oddMin[1] = arr[1];
            for (int i = 2; i < n; i++) {
                if (i % 2 == 0) {
                    evenSum[i] = evenSum[i - 2] + arr[i];
                    evenMin[i] = Math.min(evenMin[i - 2], arr[i]);
                } else {
                    oddSum[i] = oddSum[i - 2] + arr[i];
                    oddMin[i] = Math.min(oddMin[i - 2], arr[i]);
                }
            }
            for (int i = 1; i < n; i++) {
                ans = Math.min(ans, compute(arr, i, evenSum, oddSum, evenMin, oddMin));
            }
            out.println(ans);
        }

        private long compute(int[] arr, int i, long[] evenSum, long[] oddSum, int[] evenMin, int[] oddMin) {
            if (i % 2 == 0) {
                return evenSum[i] + (arr.length - (i / 2) - 1) * (long) evenMin[i] + oddSum[i - 1] + (arr.length - (i / 2)) * (long) oddMin[i - 1];
            } else {
                return evenSum[i - 1] + (arr.length - (i / 2) - 1) * (long) evenMin[i - 1] + oddSum[i] + (arr.length - (i / 2) - 1) * (long) oddMin[i];
            }
        }

    }
}

