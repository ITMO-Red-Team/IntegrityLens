import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
public class Main 
{
    static int N=(int)2e5+10;
    static int[][] A=new int[2][N];
    static long[][] dp=new long[2][N];
    static ArrayList<ArrayList<Integer>> links=new ArrayList<>(N);
    static {
        for (int i=0;i<N;i++)
            links.add(new ArrayList<>());
    }
    static void dfs(int v,int p) 
    {
        dp[0][v]=dp[1][v]=0;
        for (Integer link :links.get(v))
        {
            if (link==p) continue;
            dfs(link,v);
            dp[0][v]+=Math.max(Math.abs(A[0][v]-A[0][link])+dp[0][link],Math.abs(A[0][v]-A[1][link])+dp[1][link]);
            dp[1][v]+=Math.max(Math.abs(A[1][v]-A[0][link])+dp[0][link],Math.abs(A[1][v]-A[1][link])+dp[1][link]);
        }
    }

	public static void main (String[] args) throws Exception
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
		int t=Integer.parseInt(br.readLine()),n,i;
		while (t-->0)
		{
		    n=Integer.parseInt(br.readLine());
		    for (i=1;i<=n;i++)
		    {
		        String[] in=br.readLine().split(" ");
		        A[0][i]=Integer.parseInt(in[0]);
		        A[1][i]=Integer.parseInt(in[1]);
		        links.set(i,new ArrayList<>());
		    }
		    for (i=1;i<n;i++)
		    {
		        String[] in=br.readLine().split(" ");
		        int a=Integer.parseInt(in[0]);
		        int b=Integer.parseInt(in[1]);
		        links.get(a).add(b);
		        links.get(b).add(a);
		    }
		    dfs(1,-1);
		    System.out.println(Math.max(dp[0][1],dp[1][1]));
		}
	}
}

