import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

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
        solver.solve(1, in, out);
        out.close();
    }

    static class TaskC {
        public void solve(int testNumber, Scanner in, PrintWriter out) {
            int T = in.nextInt();
            while (T-- > 0) {
                solveOne(in, out);
            }
        }

        private void solveOne(Scanner in, PrintWriter out) {
            int N = in.nextInt();
            int[] k = L.readIntArray(N, in), h = L.readIntArray(N, in);
            Set<Integer> toRemove = new HashSet<>();

            int prevCost = h[0], prevX = k[0];
            for (int i = 1; i < N; i++) {
                for (int j = i; j < N; j++) {
                    int currCost = h[j], currX = k[j];
                    if (currX - currCost + 1 <= prevX) {
                        if (prevCost + (currX - prevX) > h[j]) h[j] = prevCost + (currX - prevX);
                    }
                }
                prevX = k[i];
                prevCost = h[i];
            }

            int nextCost = h[N - 1], nextX = k[N - 1];
            for (int i = N - 2; i >= 0; i--) {
                int currCost = h[i], currX = k[i];
                if ((nextCost - currCost) >= (nextX - currX)) {
                    toRemove.add(i);
                    continue;
                }
                nextCost = currCost;
                nextX = currX;
            }
            long cost = 0;
            for (int i = N - 1; i >= 0; i--)
                if (!toRemove.contains(i)) cost += (h[i] * ((long) h[i] + 1)) / 2;
            out.println(cost);
        }

    }

    static class L {
        public static int[] readIntArray(int size, Scanner in) {
            int[] array = new int[size];
            for (int i = 0; i < size; i++) {
                array[i] = in.nextInt();
            }
            return array;
        }

    }
}

