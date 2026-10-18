import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int count = sc.nextInt();
        long[] a = new long[count];
        for (int k = 0; k < count; k++) {
            a[k] = sc.nextLong();
        }
        long answer = Long.MIN_VALUE;
        long run = 0;
        for (int k = 0; k < count; k++) {
            run = Math.max(a[k], run + a[k]);
            if (run > answer) {
                answer = run;
            }
        }
        System.out.println(answer);
    }
}
