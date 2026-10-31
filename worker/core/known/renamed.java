import java.util.Arrays;
import java.util.Scanner;

// Та же программа, что base.java: другие имена, комментарии и оформление.
public class Main {
    static long[] accumulate(int[] items) {
        long[] totals = new long[items.length + 1];

        for (int idx = 0; idx < items.length; idx++) {
            // накапливаем сумму
            totals[idx + 1] = totals[idx] + items[idx];
        }
        return totals;
    }

    static int findSegment(int[] items, int size) {
        long[] totals = accumulate(items);
        int answer = 0;
        for (int from = 1; from + size <= items.length; from++) {
            long sum = totals[from + size] - totals[from];
            if (sum > totals[answer + size] - totals[answer]) { answer = from; }
        }
        return answer;
    }

    public static void main(String[] argv) {
        Scanner reader = new Scanner(System.in);
        int count = reader.nextInt();
        int length = reader.nextInt();
        int[] data = new int[count];
        for (int j = 0; j < count; j++) {
            data[j] = reader.nextInt();
        }
        int where = findSegment(data, length);
        System.out.println(where + 1);
        System.out.println(Arrays.toString(Arrays.copyOfRange(data, where, where + length)));
    }
}
