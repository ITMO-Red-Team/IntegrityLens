/*
 * akshaygupta26
 */
import java.io.*;
import java.util.*;

public class E
{ 
	
	static long dp[][];
	static long val[][];
	static ArrayList<Integer> arr[];
    public static void main(String[] args) 
	{ 
		FastReader sc=new FastReader(); 
		StringBuffer ans=new StringBuffer();
		int test=sc.nextInt();
		while(test-->0)
		{
			int n=sc.nextInt();
			arr=new ArrayList[n+1];
			val=new long[n+1][2];
			dp=new long[n+1][2];
			for(int i=0;i<=n;i++) Arrays.fill(dp[i], -1);
			for(int i=1;i<=n;i++) {
				val[i][0]=sc.nextLong();val[i][1]=sc.nextLong();
			}
			for(int i=0;i<=n;i++) arr[i]=new ArrayList<>();
			for(int i=0;i<n-1;i++) {
				int a=sc.nextInt(),b=sc.nextInt();
				arr[a].add(b);arr[b].add(a);
			}
			long max1=0,max2=0;
			
			for(int ch:arr[1]) {
				max1+=solve(ch,0,1);
				max2+=solve(ch,1,1);
			}
			ans.append(Math.max(max1, max2)+"\n");
		}
		
	    System.out.print(ans);
	} 
    
    static long solve(int nd,int prv,int par) {
    	
    	
    	
    	if(dp[nd][prv] != -1) return dp[nd][prv];
    	
    	long l=Math.abs(val[par][prv]-val[nd][0]);long r=Math.abs(val[par][prv]-val[nd][1]);
    
    		for(int ch:arr[nd]) {
    			if(ch != par) {
    				l+=solve(ch,0,nd);//Take left
    				r+=solve(ch,1,nd);//Take right
    			}
    		}
    
    		return dp[nd][prv] =Math.max(l, r);
    }
 
    static long _gcd(long a,long b) {
    	if(b == 0) return a;
    	return _gcd(b,a%b);
    }
    
    static final Random random=new Random();
    
	static void ruffleSort(int[] a) {
		int n=a.length;//shuffle, then sort 
		for (int i=0; i<n; i++) {
			int oi=random.nextInt(n), temp=a[oi];
			a[oi]=a[i]; a[i]=temp;
		}
		Arrays.sort(a);
	}
	
	static void ruffleSort(long[] a) {
		int n=a.length;//shuffle, then sort 
		for (int i=0; i<n; i++) {
			int oi=random.nextInt(n);
			long temp=a[oi];
			a[oi]=a[i]; a[i]=temp;
		}
		Arrays.sort(a);
	}
	
	static class FastReader 
	{ 
		BufferedReader br; 
		StringTokenizer st; 

		public FastReader() 
		{ 
			br = new BufferedReader(new
					InputStreamReader(System.in)); 
		} 

		String next() 
		{ 
			while (st == null || !st.hasMoreElements()) 
			{ 
				try
				{ 
					st = new StringTokenizer(br.readLine()); 
				} 
				catch (IOException e) 
				{ 
					e.printStackTrace(); 
				} 
			} 
			return st.nextToken(); 
		} 

		int nextInt() 
		{ 
			return Integer.parseInt(next()); 
		} 

		long nextLong() 
		{ 
			return Long.parseLong(next()); 
		} 

		double nextDouble() 
		{ 
			return Double.parseDouble(next()); 
		} 

		String nextLine() 
		{ 
			String str = ""; 
			try
			{ 
				str = br.readLine(); 
			} 
			catch (IOException e) 
			{ 
				e.printStackTrace(); 
			} 
			return str; 
		} 
	} 


} 
