// "static void main" must be defined in a public class.
import java.util.*;
import java.io.*;
public class Main {
    static long[][]dp;
    static ArrayList<Integer>[]adj;
    static int[][]arr;
    static class FastScanner {
		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st=new StringTokenizer("");
		String next() {
			while (!st.hasMoreTokens())
				try { 
                                        st=new StringTokenizer(br.readLine());				               
                                } catch (IOException e) {}
			return st.nextToken();
		}
		
		int nextInt() {
			return Integer.parseInt(next());
		}
		long nextLong() {
			return Long.parseLong(next());
		}
	}
    public static void main(String[] args) {
        FastScanner sc=new FastScanner();
        int t=sc.nextInt();
        while(t-->0){
            int n=sc.nextInt();
            arr=new int[n][2];
            adj=new ArrayList[n];
            for(int i=0;i<n;i++){
                arr[i][0]=sc.nextInt();
                arr[i][1]=sc.nextInt();
                adj[i]=new ArrayList<>();
            }
            for(int i=0;i<n-1;i++){
                int a=sc.nextInt()-1;
                int b=sc.nextInt()-1;   
                adj[a].add(b);
                adj[b].add(a);
            }
            dp=new long[n][2];
            dfs(0,-1);
            System.out.println(Math.max(dp[0][0],dp[0][1]));
        }
    }
    public static void dfs(int v,int parent){
        for(int u:adj[v]){
            if(u!=parent){
                dfs(u,v);
            }
        }
        long ans1=0;
        long ans2=0;
        for(int u:adj[v]){
            if(u!=parent){
                long a=Math.abs(arr[v][0]-arr[u][0])+dp[u][0];
                long b=Math.abs(arr[v][0]-arr[u][1])+dp[u][1];
                long a1=Math.abs(arr[v][1]-arr[u][0])+dp[u][0];
                long b1=Math.abs(arr[v][1]-arr[u][1])+dp[u][1];
                ans1+=Math.max(a,b);
                ans2+=Math.max(a1,b1);
            }
        }
        dp[v][0]=ans1;
        dp[v][1]=ans2;
    }
}