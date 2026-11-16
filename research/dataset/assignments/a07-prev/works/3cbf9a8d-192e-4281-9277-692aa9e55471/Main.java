import java.io.*;
import java.util.*;

public class D {

	public static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	public static PrintWriter out = new PrintWriter(new BufferedOutputStream(System.out));

	public static void main(String[] args) throws IOException {
		readInput();
		out.close();
	}
	
	static List<Integer> o1, o0;
	static int[][] dp;
	
	static int solve(int i, int j) {
		if (i >= dp.length) return 0;
		if (j >= dp[0].length) return Integer.MAX_VALUE/10;
		
		if (dp[i][j] == -1) {
			dp[i][j] = Integer.min(solve(i,j+1), solve(i+1,j+1) + Math.abs(o1.get(i)-o0.get(j)));
		}
		return dp[i][j];
	}
	
	public static void readInput() throws IOException {
		// br = new BufferedReader(new FileReader(".in"));
		// out = new PrintWriter(new FileWriter(".out"));
		int n;
		n = Integer.parseInt(br.readLine());
		int[] a=  new int[n];
		StringTokenizer st = new StringTokenizer(br.readLine());
		o1 = new ArrayList<Integer>();
		o0 = new ArrayList<Integer>();
		for (int i = 0; i < n; i++) {
			a[i] = Integer.parseInt(st.nextToken());
			if (a[i] == 1) o1.add(i);
			else o0.add(i);
		}
		if (o1.size() == 0) {
			out.println(0);
			return;
		}
		dp = new int[o1.size()][o0.size()];
		for (int[] x: dp) Arrays.fill(x, -1);
		out.println(solve(0,0));
		
	}
}
