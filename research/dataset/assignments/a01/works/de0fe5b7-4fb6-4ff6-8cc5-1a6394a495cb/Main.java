

import java.io.*;
import java.math.*;
import java.util.*;



public class test {
	 
	   static class Pair{ 
		   long x;
		   long y;
		   
		   Pair(long x,long y){ 
			   this.x = x;
			   this.y = y;
			   
		   }
	   }
	  
	   static class Compare { 
		   
		     void compare(Pair arr[], int n) 
		    { 
		        // Comparator to sort the pair according to second element 
		        Arrays.sort(arr, new Comparator<Pair>() { 
		            @Override public int compare(Pair p1, Pair p2) 
		            { 
		            	if(p1.x!=p2.x) {
		                return (int)(p1.x - p2.x); 
		            	}
		            	else { 
		            		return (int)(p1.y - p2.y);
		            	}
		            } 
		        }); 
		  
//		        for (int i = 0; i < n; i++) { 
//		            System.out.print(arr[i].x + " " + arr[i].y + " "); 
//		        } 
//		        System.out.println(); 
		    } 
		} 
	 
	   static class Scanner {
	        BufferedReader br;
	        StringTokenizer st;
	 
	        public Scanner()
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
	
	   static void dfs(int cur , int prnt , ArrayList<Integer> graph[],long dp[][],Pair p[]) { 
		   
		   for(int i : graph[cur]) { 
			   
			   if(i!=prnt) { 
				   dfs(i,cur,graph,dp,p);
				   
				   dp[cur][0] += Math.max(Math.abs(p[cur].x - p[i].x)+ dp[i][0], Math.abs(p[cur].x - p[i].y)+dp[i][1]);
				   dp[cur][1] += Math.max(Math.abs(p[cur].y -p[i].x) + dp[i][0], Math.abs(p[cur].y - p[i].y)+dp[i][1]);
			   }
		   }
	   }
	   public static void main(String args[]) throws Exception { 
		
		   
		   Scanner sc = new Scanner();
		   StringBuffer res = new StringBuffer();
		   
		   int tc = sc.nextInt();
		   
		   while(tc-->0) { 
			
			int n = sc.nextInt();
			
			Pair p[] = new Pair[n+1];
			
			for(int i=1;i<=n;i++) { 
				p[i] = new Pair(0,0);
				p[i].x = sc.nextLong();
				p[i].y = sc.nextLong();
			}
			
			ArrayList<Integer> graph[] = new ArrayList[n+1];
			
			for(int i=0;i<n+1;i++) { 
				graph[i] = new ArrayList<>();
			}
			for(int i=0;i<n-1;i++) { 
				
				int u = sc.nextInt();
				int v = sc.nextInt();
				
				graph[u].add(v);
				graph[v].add(u);
				
			}
			
			long dp[][] = new long[n+1][2];
			
			dfs(1,0,graph,dp,p);
			
			System.out.println(Math.max(dp[1][0], dp[1][1]));
		   }
			System.out.println(res);
		   
	   }
}  




