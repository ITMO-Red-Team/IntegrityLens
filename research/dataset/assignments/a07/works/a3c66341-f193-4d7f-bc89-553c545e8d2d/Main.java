
import java.io.*;
import java.util.*;

public class D {
	
	static int[][] dp;
	
	static int solve(int i,int j,ArrayList<Integer> A,ArrayList<Integer> B) {
		if(i==A.size()) {
			return 0;
		}
		if(j==B.size()) {
			return 1000000009;
		}
		if(dp[i][j]!=-1) {
			return dp[i][j];
		}
		int ans=1000000009;
		int a=A.get(i);
		int b=B.get(j);
		ans=Math.min(ans, Math.abs(a-b)+solve(i+1,j+1,A,B));
		ans=Math.min(ans, solve(i,j+1,A,B));
		return dp[i][j]=ans;
	}

	public static void main(String[] args){
		FastReader sc=new FastReader();
		int t=1;
		while(t-->0) {
			int n=sc.nextInt();
			int[] a=new int[n];
			ArrayList<Integer> A=new ArrayList<>();
			ArrayList<Integer> B=new ArrayList<>();
			for(int i=0;i<n;i++) {
				a[i]=sc.nextInt();
				if(a[i]==1) {
					A.add(i);
				} else {
					B.add(i);
				}
			}
			dp=new int[5010][5010];
			for(int i=0;i<5010;i++) {
				Arrays.fill(dp[i], -1);
			}
			System.out.println(solve(0,0,A,B));
			
		}
	}
	static class FastReader {
		BufferedReader br;
		StringTokenizer st;

		public FastReader()
		{
			br = new BufferedReader(
				new InputStreamReader(System.in));
		}

		String next()
		{
			while (st == null || !st.hasMoreElements()) {
				try {
					st = new StringTokenizer(br.readLine());
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			return st.nextToken();
		}

		int nextInt() { return Integer.parseInt(next()); }

		long nextLong() { return Long.parseLong(next()); }

		double nextDouble()
		{
			return Double.parseDouble(next());
		}

		String nextLine()
		{
			String str = "";
			try {
				str = br.readLine();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			return str;
		}
	}

}
