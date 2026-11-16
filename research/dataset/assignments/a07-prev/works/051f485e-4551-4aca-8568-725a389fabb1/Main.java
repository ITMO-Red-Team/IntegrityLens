import java.util.Scanner;
import java.util.ArrayList;
import java.lang.Math;

public class fell {
	private static final Scanner cin = new Scanner(System.in);
	private static final int maxVal = 1000000000;

	public static void main(String[] args) {
		int n = cin.nextInt();
		ArrayList<Integer> a = new ArrayList<>();
		ArrayList<Integer> b = new ArrayList<>();

		for (int i = 0; i < n; i++) {
			int t = cin.nextInt();
			if (t == 0)
				a.add(i);
			else
				b.add(i);
		}

		if (a.size() == n) {
			System.out.println(0);
			return;
		}

		int[][] dp = new int[n][n];

		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++) {
				if (i == 0)
					dp[i][j] = 0;
				else
					dp[i][j] = maxVal;
			}
		
		for (int i = 0; i < a.size(); i++) {
			for (int j = 0; j < Math.min(i + 1, b.size() + 1); j++) {
				dp[i + 1][j] = Math.min(dp[i + 1][j], dp[i][j]);
				if (j == b.size())
					continue;
				dp[i + 1][j + 1] = Math.min(dp[i + 1][j + 1], dp[i][j] + Math.abs(a.get(i) - b.get(j)));
			}
		}

		System.out.println(dp[a.size()][b.size()]);
	}
}