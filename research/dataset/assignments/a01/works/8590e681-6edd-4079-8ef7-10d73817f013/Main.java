import java.util.*;
import java.io.*;

public class Solution{
	
	static long abs(long a){return Math.abs(a);}
	static long max(long a, long b){return Math.max(a, b);}
	
	static class Graph{
		
		int l[], r[], v;
		ArrayList<Integer> adj[];		
		Graph(int v){
			this.v = v;
			this.l = new int[v];
			this.r = new int[v];
			adj = new ArrayList[v];
			for(int i = 0 ; i < v ; i++){
				adj[i] = new ArrayList<>();
			}
		}
		
		void addEdge(int u, int v){
			this.adj[u].add(v);
			this.adj[v].add(u);
		}
		
		long[] dfs(int u, int parent){
			long left = 0, right = 0;
			int n = adj[u].size();
			for (int i = 0 ; i < n; i ++ ) {
				int child = adj[u].get(i);
				if ( child == parent ) continue;
				long val[] = dfs(child, u);
				left += max(abs(l[child]- l[u])+val[0], abs(r[child] - l[u])+val[1]);
				right += max(abs(l[child]- r[u])+val[0], abs(r[child] - r[u])+val[1]);
			}
			
			return new long[]{left, right};
		}
	}

	public static void main(String args[])throws IOException{
		BufferedReader br = new 
		BufferedReader(new InputStreamReader(System.in));
		
		int tests = Integer.parseInt(br.readLine());
		while( tests --> 0 ){
			int n = Integer.parseInt(br.readLine());
			Graph g = new Graph(n);
			for(int i = 0 ; i < n ; i++ ){
				String line[] = br.readLine().split(" ");
				g.l[i] = Integer.parseInt(line[0]);
				g.r[i] = Integer.parseInt(line[1]);
			}
			
			for(int i = 0 ; i < n - 1 ; i++ ){
				String line[] = br.readLine().split(" ");
				int u = Integer.parseInt(line[0]) - 1;
				int v = Integer.parseInt(line[1]) - 1;			
				g.addEdge(u, v);
			}
			
			long res[] = g.dfs(0, -1);
			System.out.println(max(res[0], res[1]));
		}
	}
}