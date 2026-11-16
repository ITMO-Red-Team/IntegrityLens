import java.util.*;
import java.io.*;
import java.lang.*;

public class CodeChef {
	static long dp[][];
	public static void main (String[] args) throws java.lang.Exception
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
		int t=Integer.parseInt(br.readLine());
		while(t-->0)
		{
			int n=Integer.parseInt(br.readLine());
			int arr[][]=new int[n+1][2];
			for(int i=1;i<=n;i++)
			{
				String str[]=br.readLine().split(" ");
				int l=Integer.parseInt(str[0]);int r=Integer.parseInt(str[1]);
				arr[i][0]=l;arr[i][1]=r;
			}
			dp=new long[n+1][2];
			Set<Integer>adj[]=new HashSet[n+1];
			for(int i=0;i<=n;i++)adj[i]=new HashSet();
			for(int i=0;i<n-1;i++)
			{
				String str[]=br.readLine().split(" ");
				int src=Integer.parseInt(str[0]);  int dest=Integer.parseInt(str[1]);
				adj[src].add(dest);
				adj[dest].add(src);
			}
			fun(1,adj,arr);System.out.println(Math.max(dp[1][0], dp[1][1]));
		}
	}
	static void fun(int root,Set<Integer>[] adj,int arr[][])
	{
		dp[root][0]=dp[root][1]=0;
		Iterator<Integer>iter=adj[root].iterator();
		while(iter.hasNext())
		{
			int i=iter.next();
			adj[i].remove(root);
			fun(i,adj,arr);
			dp[root][0]+=Math.max(Math.abs(arr[root][0]-arr[i][0])+dp[i][0], Math.abs(arr[root][0]-arr[i][1])+dp[i][1]);
			dp[root][1]+=Math.max(Math.abs(arr[root][1]-arr[i][0])+dp[i][0], Math.abs(arr[root][1]-arr[i][1])+dp[i][1]);
		}
		
	}
}
