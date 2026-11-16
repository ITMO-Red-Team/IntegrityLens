import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

public class ProblemD {
	
	public static void main(String[] args) throws IOException {
		final int INF = 20000000;
		InputStream in = System.in;
		InputReader scan = new InputReader(in);
		int n = scan.nextInt();
		int occ = 0;
		List<Integer> occPos = new ArrayList<>();
		HashSet<Integer> occPosSet = new HashSet<>();
		for(int i=1;i<=n;i++) {
			int num = scan.nextInt();
			if(num==1) {
				occ++;
				occPos.add(i);
				occPosSet.add(i);
			}
		}
		int[][] dp = new int[n+1][occ+1];
		for(int i=0;i<=n;i++) {
			for(int j=0;j<=occ;j++) {
				dp[i][j] = 20000000;
			}
		}
		for(int i=1;i<=n;i++) {
			int k=1;
			for(int pos: occPos) {
				if(occPosSet.contains(i)) {
					dp[i][k] = dp[i-1][k];
				} else {
					dp[i][k] = Math.min(dp[i-1][k], dp[i-1][k-1]+Math.abs(pos-i));
					if(k==1) dp[i][k]=Math.min(dp[i][k],Math.abs(pos-i));
				}
				k++;
			}
		}
		if(dp[n][occ]==INF) {
			System.out.println(0);
		} else {
			System.out.println(dp[n][occ]);
		}
	}
	
	static class InputReader {
		BufferedReader br;
		StringTokenizer st;
		
		InputReader(InputStream in) {
			br = new BufferedReader(new InputStreamReader(in));
		}
		
		String next() {
			while (st == null || !st.hasMoreTokens()) {
				try {
					st = new StringTokenizer(br.readLine());
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			return st.nextToken();
		}
		
		int nextInt() {
			return Integer.parseInt(next());
		}
		
		public long nextLong() {
			return Long.parseLong(next());
		}
	}
}
