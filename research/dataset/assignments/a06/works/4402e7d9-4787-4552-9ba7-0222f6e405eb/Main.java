       import java.util.*;


import java.awt.Point;
import java.io.*;
import java.math.BigInteger;
public class Solutions {

		static int MAX=Integer.MAX_VALUE;
		static int MIN=Integer.MIN_VALUE;
//static ArrayList<ArrayList<Integer>>list=new ArrayList<ArrayList<Integer>>();
	static FastScanner scr=new FastScanner();
	static PrintStream out=new PrintStream(System.out);
	public static void main(String []args) {
		int T=scr.nextInt();
		t:for(int tt=0;tt<T;tt++) {
			int n=scr.nextInt();
			int []a=scr.readArray(n);
			long min[]=new long[2];  
  			long sum[]=new long[2]; 
			sum  [0]=a[0];
			sum  [1]=0;
			min[0]=a[0];
			min[1]=MAX;
			long ans=Long.MAX_VALUE;
			for(int i=1;i<n;i++) {
				min[i%2]=Math.min(min[i%2],a[i]);
				sum[i%2]+=a[i];
				
				int odd=(i+2)/2;
				int even=(i+1)/2;
				ans=Math.min(ans,sum[0]+((n-odd)*min[0])+sum[1]+((n-even)*min[1]));
			}
			out.println(ans);
		}
	}
	static long modPow(long base,long exp,long mod) {
		if(exp==0) {
			return 1;
		}
		if(exp%2==0) {
			long res=(modPow(base,exp/2,mod));
			return (res*res)%mod;
		}
		return (base*modPow(base,exp-1,mod))%mod;
	}
	static long gcd(long a,long b) {
		if(b==0) {
			return a;
			}
		return gcd(b,a%b);
	}
	static class FastScanner {
		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st=new StringTokenizer("");
	 	String next() {
	 		while (!st.hasMoreTokens())
	 			try {
	 				st=new StringTokenizer(br.readLine());
	 			} catch (IOException e) {
	 				e.printStackTrace();
	 			}
	 			return st.nextToken();
		}
		int[] sort(int a[]) {
			Arrays.sort(a);
			return a;
		}
		int []reverse(int a[]){
			int b[]=new int[a.length];
			int index=0;
			for(int i=a.length-1;i>=0;i--) {
				b[index]=a[i];
			}
			return b;
		}
		int nextInt() {
			return Integer.parseInt(next());
		}
		int[] readArray(int n) {
			int[] a=new int[n];
			for (int i=0; i<n; i++) a[i]=nextInt();
			return a;
		}
		long[] readLongArray(int n) {
			long  [] a=new long  [n];
			for (int i=0; i<n; i++) a[i]=nextLong();
			return a;
		}
		long nextLong() {
			return Long.parseLong(next());
		}
	}

}
class triplet{
	int x;
	int y;
	int z;
	triplet(int fisrt,int second,int third){
		this.x=fisrt;
		this.y=second;
		this.z=third;
	}
}
class pair{
	int x=0;
	int y=0;
	pair(int first,int second){
		this.x=first;
		this.y=second;
	}
}
