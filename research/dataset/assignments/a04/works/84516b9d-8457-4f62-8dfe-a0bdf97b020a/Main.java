import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.StringTokenizer;
import java.io.*;
 
public class PhoenixTow {
	
	private static class MyScanner {
	    BufferedReader br;
	    StringTokenizer st;

	    public MyScanner() {
	       br = new BufferedReader(new InputStreamReader(System.in));
	    }

	    String next() {
	        while (st == null || !st.hasMoreElements()) {
	            try {
	                st = new StringTokenizer(br.readLine());
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	        return st.nextToken();
	    }

	    int nextInt() {
	        return Integer.parseInt(next());
	    }

	    long nextLong() {
	        return Long.parseLong(next());
	    }

	    double nextDouble() {
	        return Double.parseDouble(next());
	    }

	    String nextLine(){
	        String str = "";
		  try {
		     str = br.readLine();
		  } catch (IOException e) {
		     e.printStackTrace();
		  }
		  return str;
	    }

	 }
	

	public static class  Pair implements Comparable<Pair>
	{
		int x; 
		int y;
		
		Pair(int x, int y)
		{
			this.x = x;
			this.y = y;
		}
		
		public int compareTo(Pair p)
		{
			return ((Integer)this.x).compareTo(p.x);
		}
	}
	
	public static void solution(int[] arr, int n, int m, int x)
			
	{
		ArrayList<Pair> list = new ArrayList<>();
		
		for(int i = 0; i<n; i++)
		{
			list.add(new Pair(arr[i], i));
		}
		
		Collections.sort(list);
		
		long[] sum = new long[m];
		
		int[] ans = new int[n];
		
		int k = 1;
		
		for(int i = 0; i<list.size(); i++)
		{
			if(k<m)
		    { 
				if(sum[k-1]+list.get(i).x - sum[k]>x)
			      {out.println("NO"); return; }
		      
		    }
			
			  
	    	sum[k-1]+=list.get(i).x;
	    	ans[list.get(i).y] = k;
	    	k++;
	    	
	    	if(k==(m+1))
	    		k=1;
	    	
			
		}
		
		out.println("YES"); 
		
		for(int i = 0; i<n; i++)
			out.print(ans[i]+" ");
		
		out.println();
		
		
	}
        
private static PrintWriter out = new PrintWriter(System.out);

public static void main (String[] args)
{
	MyScanner s =  new MyScanner();
     
    int t = s.nextInt();
    
    for(int j = 0; j<t ; j++)
    {
    	int n = s.nextInt();
    	int m = s.nextInt();
    	int x = s.nextInt();
    	
    	int[] arr = new int[n];
    	for(int i =0; i<n; i++)
    		arr[i] = s.nextInt();
    	
    	solution(arr,n,m,x);
    }
    
    out.flush();
    out.close();
    
}
}
