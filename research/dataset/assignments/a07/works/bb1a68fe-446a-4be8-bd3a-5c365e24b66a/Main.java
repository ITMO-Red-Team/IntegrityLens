/*
 * akshaygupta26
 */
import java.io.*;
import java.util.*;
public class D
{ 
	
	static ArrayList<Integer> taken,vacant;
	static long dp[][];
    public static void main(String[] args) 
	{ 
		FastReader sc=new FastReader(); 
		StringBuffer ans=new StringBuffer();
		int test=1;
		while(test-->0)
		{
			int n=sc.nextInt();
			taken=new ArrayList<>();;
			vacant=new ArrayList<>();
			
			dp=new long[n+1][n+1];
			for(int i=0;i<=n;i++) {
				Arrays.fill(dp[i], -1);
			}
			
			for(int i=0;i<n;i++) {
				if(sc.nextInt() == 1) taken.add(i);
				else vacant.add(i);
			}
			
			long res =solve(0,0,taken.size(),vacant.size());
			ans.append(res+"\n");
		}

	    System.out.print(ans);
	} 
 
    static long solve(int t1,int v1,int nt1,int nv1) {
    	
    	//All taken given a seat
    	if(t1 == nt1) return 0;
    	
    	//Vacant seats finishes
    	if(v1 == nv1) return Integer.MAX_VALUE;
    	
    	//Not enough seat
    	if(nt1-t1 > nv1-v1) return Integer.MAX_VALUE;
    	
    	/*
    	 * Two options: 
    	 * 1) Use the current vacant seat
    	 * 2) Skip the current vacant seat
    	 */
    	
    	if(dp[t1][v1] != -1) return dp[t1][v1];
    	
    	long op1 = solve(t1+1,v1+1,nt1,nv1) + Math.abs(vacant.get(v1)-taken.get(t1));
    	long op2 = solve(t1,v1+1,nt1,nv1);
    	
    	return dp[t1][v1]=Math.min(op1, op2);
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
