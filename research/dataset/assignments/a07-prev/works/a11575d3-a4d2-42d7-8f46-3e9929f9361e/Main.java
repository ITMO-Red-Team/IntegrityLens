import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class E1525D {
    public static void main(String[] args) {
        Scanner scn = new Scanner(System.in);
        int n = scn.nextInt();
        int[] arr = new int[n];
        ArrayList<Integer> occupied = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            arr[i] = scn.nextInt();
            if (arr[i] == 1) occupied.add(i);
        }
        int[][] dp = new int[n + 1][occupied.size() + 1];
        for (int[] row : dp) Arrays.fill(row, (int) 1e9);
        dp[0][0] = 0;
        for (int i = 1; i <= n; i++) {
            dp[i][0] = 0;
            for (int j = 1; j <= occupied.size(); j++) {
                dp[i][j] = dp[i - 1][j];
                if (arr[i - 1] == 0)
                    dp[i][j] = Math.min(dp[i][j], dp[i - 1][j - 1] + Math.abs(i - 1 - occupied.get(j - 1)));
            }
        }
        System.out.println(dp[n][occupied.size()]);


    }
}
