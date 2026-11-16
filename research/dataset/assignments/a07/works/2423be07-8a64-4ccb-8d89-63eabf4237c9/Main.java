//https://codeforces.com/contest/1525/problem/D
//D. Armchairs
import java.util.*;
import java.io.*;
public class CF_1525_D{
	public static void main(String[] args) throws Exception{
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out));
		StringBuilder sb = new StringBuilder();
		StringTokenizer st;

		int n = Integer.parseInt(br.readLine());
		int a[] = new int[n];
		ArrayList<Integer> pos = new ArrayList<Integer>();

		st = new StringTokenizer(br.readLine());
		for(int i=0;i<n;i++){
			a[i] = Integer.parseInt(st.nextToken());
			if(a[i]==1)
				pos.add(i);
		}

		int z = pos.size();
		int dp[][] = new int[n+1][z+1];
		for(int i=0;i<=n;i++)
			Arrays.fill(dp[i], Integer.MAX_VALUE);

		dp[0][0] = 0;
		for(int i=0;i<n;i++){
			for(int j=0;j<=z;j++){
				if(dp[i][j] == Integer.MAX_VALUE)
					continue;
				dp[i+1][j] = Math.min(dp[i+1][j], dp[i][j]);

				if(j<z && a[i]==0)
					dp[i+1][j+1] = Math.min(dp[i+1][j+1], dp[i][j]+Math.abs(pos.get(j)-i));
			}
		}


		pw.print(dp[n][z]);
		pw.flush();
		pw.close();
	}
}