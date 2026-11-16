import java.io.*;
import java.util.*;

public class C {

	public static void main(String[] args) {
        FastScanner in = new FastScanner();
        PrintWriter out = new PrintWriter(System.out);
        int t = in.nextInt();
        while(t-->0) { 
        	int n = in.nextInt();
        	int a[] = in.readArray(n);
        	int a1[] = new int[(n+1)/2];
        	int a2[] = new int[n/2];
        	int ind1 = 0, ind2 = 0;
        	for(int i=0;i<n;i++){
        		if(i%2==0) a1[ind1++] = a[i];
        		else a2[ind2++] = a[i];
        	}
        	int n1 = a1.length, n2 = a2.length;                  
        	long p1[] = new long[n1], p2[] = new long[n2];
        	p1[0] = a1[0]; p2[0] = a2[0];
        	for(int i=1;i<n1;i++) p1[i] = p1[i-1] + a1[i];
        	for(int i=1;i<n2;i++) p2[i] = p2[i-1] + a2[i];
        	long ans1[] = new long[n1];
        	long ans2[] = new long[n2];
        	ans1[0] = p1[0]*n;
        	ans2[0] = p2[0]*n;
        	
        	long mn = a1[0];
        	for(int i=1;i<n1;i++){
        		ans1[i] = p1[i];
        		mn = Math.min(mn,a1[i]);
        		ans1[i] -= mn;
        		ans1[i] += mn*(n-i);	
        	}
        	mn = a2[0];
        	for(int i=1;i<n2;i++){
        		ans2[i] = p2[i];
        		mn = Math.min(mn,a2[i]);
        		ans2[i] -= mn;
        		ans2[i] += mn*(n-i);	
        	}                    
        	long cost = Long.MAX_VALUE;
        	for(int i=0;i<n1;i++){
        		if(i==n2){
        			cost = Math.min(cost,ans1[i]+ans2[i-1]);
        		}
        		else{
        			cost = Math.min(cost,ans1[i]+ans2[i]);
        			if(i-1>-1) cost = Math.min(cost,ans1[i]+ans2[i-1]);
        		}
        	}
        	out.println(cost);
       	}
        out.flush();
	}
	
	static class FastScanner {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer("");
		
		String next() {
			while(!st.hasMoreTokens())
				try { st = new StringTokenizer(br.readLine()); }
				catch(IOException e) {}
			return st.nextToken();
		}
		
		String nextLine(){
			try{ return br.readLine(); } 
			catch(IOException e) { } return "";
		}
		
		int nextInt() {
			return Integer.parseInt(next());
		}
		
		long nextLong() {
			return Long.parseLong(next());
		}
		
		int[] readArray(int n) {
			int a[] = new int[n];
			for(int i=0;i<n;i++) a[i] = nextInt();
			return a;
		}
	}

	static final Random random = new Random();

	static void ruffleSort(int[] a){
		int n = a.length;
		for(int i=0;i<n;i++){
			int j = random.nextInt(n), temp = a[j];
			a[j] = a[i]; a[i] = temp;
		}
		Arrays.sort(a); 	
	}
}

