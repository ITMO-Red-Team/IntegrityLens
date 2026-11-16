import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Main
{
	static int N=(int)2e5+10;
	static long[][] dp=new long[2][N];
	static int[][] A=new int[2][N];
	static ArrayList<ArrayList<Integer>> adj=new ArrayList<>(N);
	static
	{
		for(int i=0;i<N;i++)
			adj.add(new ArrayList<>());
	}
	
	public static void dfs(int v,int p)
	{
		dp[0][v]=dp[1][v]=0;
		for(Integer u:adj.get(v))
		{
			if(u==p)
				continue;
			dfs(u, v);
			dp[0][v]+=Math.max(Math.abs(A[0][v]-A[1][u])+dp[1][u], dp[0][u]+Math.abs(A[0][v]-A[0][u]));
			dp[1][v]+=Math.max(Math.abs(A[1][v]-A[1][u])+dp[1][u], dp[0][u]+Math.abs(A[1][v]-A[0][u]));
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
		int t=Integer.parseInt(br.readLine());
		while(t-->0)
		{
			int n=Integer.parseInt(br.readLine());
			for(int i=1;i<=n;i++)
			{
				String[] ss1=br.readLine().split(" ");
				A[0][i]=Integer.parseInt(ss1[0]);
				A[1][i]=Integer.parseInt(ss1[1]);
				adj.set(i, new ArrayList<>());
			}
			for(int i=1;i<n;i++)
			{
				String[] ss2=br.readLine().split(" ");
				int u=Integer.parseInt(ss2[0]);
				int v=Integer.parseInt(ss2[1]);
				adj.get(u).add(v);
				adj.get(v).add(u);
			}
			dfs(1, -1);
			System.out.println(Math.max(dp[0][1], dp[1][1]));
		}
    }
}