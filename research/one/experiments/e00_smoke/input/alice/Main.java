import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        long sum = 0;
        int count = 0;
        for (int i = 0; i < n; i++) {
            int x = in.nextInt();
            if (x > 0) {
                sum += x;
                count++;
            }
        }
        System.out.println(sum + " " + count);
    }
}
