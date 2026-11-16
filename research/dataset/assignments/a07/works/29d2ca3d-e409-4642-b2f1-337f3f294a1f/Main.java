//package codeforces;
import java.io.PrintWriter;
import java.util.*;
public class codeforces {
	static int dp[][]=new int[5001][5001];
	public static void main(String[] args) {
		Scanner s=new Scanner(System.in);  
		PrintWriter out=new PrintWriter(System.out);
        int t=1;
        for(int tt=0;tt<t;tt++) {
        	int n=s.nextInt();
        	int a[]=new int[n];
        	ArrayList<Integer> z=new ArrayList<>();
        	ArrayList<Integer> o=new ArrayList<>();
        	for(int i=0;i<n;i++) {
        		a[i]=s.nextInt();
        		if(a[i]==1) {
        			o.add(i);
        		}else {
        			z.add(i);
        		}
        	}
        	for(int i=0;i<5001;i++) {
        		Arrays.fill(dp[i], -1);
        	}
        	System.out.println(sol(0,0,z,o));
        }
        out.close();
	    s.close();
	}
	
	static void sort(long[] a) {
		ArrayList<Long> l=new ArrayList<>();
		for (long i:a) l.add(i);
		Collections.sort(l);
		for (int i=0; i<a.length; i++) a[i]=l.get(i);
	}
	static void sortcol(int a[][],int c) {
		Arrays.sort(a, (x, y) -> {
			if (x[c] != y[c]) return(int)( x[c] - y[c]);
			return (int)-(x[1]+x[2] - y[1]-y[2]);
		});
	}
	static int gcd(int a, int b)
    {
      if (b == 0)
        return a;
      return gcd(b, a % b);
    }
	public static int sol(int i,int j,ArrayList<Integer> z,ArrayList<Integer> o) {
		if(j==o.size()) {
			return 0;
		}
		int h=z.size()-i;
		int l=o.size()-j;
		if(i==z.size()) {
			return 10000000;
		}
		if(dp[i][j]!=-1) {
			//System.out.println(i+" "+j);
			return dp[i][j];
		}
		int ans1=sol(i+1,j,z,o);
		int ans2=sol(i+1,j+1,z,o)+Math.abs(z.get(i)-o.get(j));
		dp[i][j]=Math.min(ans1, ans2);
		return dp[i][j];
	}
}

