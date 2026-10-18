import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        long[] values = new long[n];
        for (int i = 0; i < n; i++) {
            values[i] = in.nextLong();
        }
        long best = Long.MIN_VALUE;
        long current = 0;
        for (int i = 0; i < n; i++) {
            current = Math.max(values[i], current + values[i]);
            if (current > best) {
                best = current;
            }
        }
        System.out.println(best);
    }
}
