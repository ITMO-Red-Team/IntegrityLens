import java.util.Arrays;
import java.util.Scanner;

// другая задача: медиана массива
public class Main {
    static double median(int[] values) {
        int[] sorted = values.clone();
        Arrays.sort(sorted);
        int mid = sorted.length / 2;
        if (sorted.length % 2 == 1) {
            return sorted[mid];
        }
        return (sorted[mid - 1] + sorted[mid]) / 2.0;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] values = new int[n];
        int i = 0;
        while (i < n) {
            values[i] = in.nextInt();
            i = i + 1;
        }
        System.out.println(median(values));
    }
}
