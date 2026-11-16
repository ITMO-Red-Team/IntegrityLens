import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

public class Armchairs {

	static List<Integer> zpos ;
	static List<Integer> opos ;
	static long[][] dp ;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Scanner sc = new Scanner(System.in);
		int n = sc.nextInt();
		zpos = new ArrayList<Integer>();
		opos = new ArrayList<Integer>();
		for (int i = 0; i < n; i++) {
			int x = sc.nextInt();
			if (x == 1) opos.add(i);
			else zpos.add(i);
		}
		dp = new long[5001][5001];
		for (int i = 0; i < 5001; i++) Arrays.fill(dp[i], -1);
		System.out.println(dp(0, 0, opos.size(), zpos.size(), opos.size()));
	}

	private static long dp(int i, int j, int m, int n, int cnt) {
		// TODO Auto-generated method stub
		if (cnt == 0) return 0;
		if (i >= m || j >= n) return Integer.MAX_VALUE;
		if (dp[i][j] != -1) return dp[i][j];
		long dns = dp(i, j + 1, m, n, cnt);
		long s = Math.abs(zpos.get(j) - opos.get(i)) + dp(i + 1, j + 1, m, n, cnt - 1);
		return dp[i][j] = Math.min(dns, s);
	}

}
