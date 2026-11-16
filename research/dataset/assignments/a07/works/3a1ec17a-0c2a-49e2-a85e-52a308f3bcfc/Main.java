import java.util.Arrays;
import java.util.Scanner;

public class P1525D {

    public static int[] ones, zeros;

    public static int[][] memo;

    public static void main(String[] args) {

        Scanner s = new Scanner(System.in);
        int n = s.nextInt();
        int[] arr = new int[n];
        int oneCount = 0;
        for (int i = 0; i < n; i++) {
            arr[i] = s.nextInt();
            oneCount += arr[i] % 2;
        }
        int o = 0, z = 0;
        ones = new int[oneCount];
        zeros = new int[n - oneCount];
        for (int i = 0; i < n; i++) {
            if (arr[i] % 2 == 1) {
                ones[o++] = i;
            } else {
                zeros[z++] = i;
            }
        }
        memo = new int[oneCount][n - oneCount];
        for (int[] row : memo) Arrays.fill(row, -1);
        System.out.println(dp(0, 0));

    }

    public static int dp(int oi, int zi) {

        if (oi == ones.length) {

            return 0;

        } else if (zi == zeros.length) {

            return 100000000;

        } else {

            if (memo[oi][zi] == -1) {
                memo[oi][zi] = Math.min(Math.abs(ones[oi] - zeros[zi]) + dp(oi + 1, zi + 1), dp(oi, zi + 1));
            }
            return memo[oi][zi];

        }

    }

}
