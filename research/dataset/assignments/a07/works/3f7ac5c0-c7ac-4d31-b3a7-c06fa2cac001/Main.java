import java.io.*;
import java.util.*;
 
public class Codeforces {
	public static void main(String[] args) throws IOException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		//int cases = Integer.parseInt(br.readLine());
		//while(cases-- > 0) {
			int n = Integer.parseInt(br.readLine());
			String[] str = br.readLine().split(" ");
			int[] a = new int[n];
			int k = 0;
			ArrayList<Integer> pos = new ArrayList<>();
			for(int i=0; i<n; i++) {
				a[i] = Integer.parseInt(str[i]);
				if(a[i] == 1) {
					k++;
					pos.add(i);
				}
			}
			int[][] dp = new int[n+1][k+1];
			for(int i=0; i<=n; i++) {
				Arrays.fill(dp[i], Integer.MAX_VALUE);
			}
			dp[0][0] = 0;
			for(int i=0; i<n; i++) {
				for(int j=0; j<=k; j++) {
					if(dp[i][j] == Integer.MAX_VALUE) {
						continue;
					}
					dp[i+1][j] = Math.min(dp[i+1][j], dp[i][j]);
					if(j < k && a[i] == 0) {
						dp[i+1][j+1] = Math.min(dp[i+1][j+1], dp[i][j]+Math.abs(pos.get(j)-i));
					}
				}
			}
			System.out.println(dp[n][k]);
		//}
	}
}
