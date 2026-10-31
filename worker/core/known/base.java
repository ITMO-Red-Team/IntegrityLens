import java.util.Arrays;
import java.util.Scanner;

public class Main {
    static long[] weightedSums(int[] values) {
        long[] sums = new long[values.length + 1];
        long weight = 1;
        for (int i = 0; i < values.length; i++) {
            if (values[i] % 3 == 0) {
                weight += values[i] / 3;
            } else if (weight > 100) {
                weight = weight / 2 + 1;
            }
            long bonus = i % 2 == 0 ? weight * 2 : -weight;
            sums[i + 1] = sums[i] + values[i] * weight + bonus;
        }
        return sums;
    }

    static int bestWindow(int[] values, int width) {
        long[] sums = weightedSums(values);
        int best = 0;
        for (int start = 1; start + width <= values.length; start++) {
            long current = sums[start + width] - sums[start];
            if (current > sums[best + width] - sums[best]) {
                best = start;
            }
        }
        return best;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int k = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = in.nextInt();
        }
        int position = bestWindow(a, k);
        System.out.println(position + 1);
        System.out.println(Arrays.toString(Arrays.copyOfRange(a, position, position + k)));
    }
}
