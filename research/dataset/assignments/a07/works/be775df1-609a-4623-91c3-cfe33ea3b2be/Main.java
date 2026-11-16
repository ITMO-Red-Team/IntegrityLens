import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Try2{
	public static void main(String[] args) throws IOException {
		FastScanner fs = new FastScanner();
//		int t =fs.nextInt();
		int t=1;
		
		while (t-- > 0) {
		
			int n = fs.nextInt();
			
			
			int a[] = new int[n];
			
			int countOne = 0;
			
			
			ArrayList<Integer> aa = new ArrayList<Integer>();
			
			for(int i =0;i<n;i++) {
					a[i] =fs.nextInt();
					if(a[i]==1) {
						countOne++;
						aa.add(i);
					}
			}
			
			int dp[][] = new int[n+1][countOne+1];
			
			for(int i =0 ;i<=n;i++) {
				for(int j =0 ;j<=countOne;j++) {
					dp[i][j] = Integer.MAX_VALUE;
				}
			}
			
			
			
			dp[0][0] =0;
			for(int i =0 ;i<n;i++) {
				for(int j =0 ;j<countOne+1;j++) {
					
					if(dp[i][j]==Integer.MAX_VALUE) continue;
					
					dp[i+1][j] = Math.min(dp[i][j], dp[i+1][j]);
					
					if(j<countOne && a[i]==0) {
						dp[i+1][j+1]=Math.min(dp[i][j]+Math.abs(aa.get(j)-i),dp[i+1][j+1]);
					}
					
					
					
				}
			}
			
			System.out.println(dp[n][countOne]);
			
			
		
		}
		
	}
	
	
	
	
	static final Random random = new Random();

	static void ruffleSort(int[] a) {
		int n = a.length;// shuffle, then sort
		for (int i = 0; i < n; i++) {
			int oi = random.nextInt(n), temp = a[oi];
			a[oi] = a[i];
			a[i] = temp;
		}
		Arrays.sort(a);
	}

	static class FastScanner {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer("");

		String next() {
			while (!st.hasMoreTokens())
				try {
					st = new StringTokenizer(br.readLine());
				} catch (IOException e) {
					e.printStackTrace();
				}
			return st.nextToken();
		}

		int nextInt() {
			return Integer.parseInt(next());
		}

		int[] readArray(int n) {
			int[] a = new int[n];
			for (int i = 0; i < n; i++)
				a[i] = nextInt();
			return a;
		}

		long nextLong() {
			return Long.parseLong(next());
		}
	}

}
