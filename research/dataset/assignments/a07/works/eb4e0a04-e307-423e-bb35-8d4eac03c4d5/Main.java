import java.util.*;
import java.lang.*;
import java.io.*;

/* Name of the class has to be "Main" only if the class is public. */
public class Armchairs
{
	public static void main (String[] args) throws java.lang.Exception
	{
		Scanner sc = new Scanner(System.in);
		PrintWriter out = new PrintWriter(System.out);
		
		int n = sc.nextInt();
		Vector<Integer> a = new Vector();
		Vector<Integer> b = new Vector();
		    
		for (int i=0; i<n; ++i){
		    int input = sc.nextInt();
		    if(input==1) a.add(i);
		    else b.add(i);
		}
		
		n = a.size();
		int m = b.size();
		
		if(n==0) out.print(0);
		else{
		    int [][]dp = new int [n][m];
    		for (int i=0; i<n; ++i) for (int j=0; j<m; ++j) dp[i][j] = 25000000;
    		dp[0][0] = Math.abs(a.get(0) - b.get(0));
    		for (int i=0; i<n; ++i) for (int j=1; j<m; ++j){
    		    if(i==0) dp[i][j] = Math.min(Math.abs(a.get(i) - b.get(j)),dp[i][j-1]);
    		    else dp[i][j] = Math.min(Math.abs(a.get(i) - b.get(j)) + dp[i-1][j-1],dp[i][j-1]);
    		}
    		out.print(dp[n-1][m-1]);
		}
		
		out.close();
	}
	
	/*Methods*/
	static int min(int... ar){int min=Integer.MAX_VALUE;for(int i:ar)min=Math.min(min, i);return min;}
    static long min(long... ar){long min=Long.MAX_VALUE;for(long i:ar)min=Math.min(min, i);return min;}
    static int max(int... ar) {int max=Integer.MIN_VALUE;for(int i:ar)max=Math.max(max, i);return max;}
    static long max(long... ar) {long max=Long.MIN_VALUE;for(long i:ar)max=Math.max(max, i);return max;}
    static void reverse(int a[]){for(int i=0;i<a.length>>1;i++){int tem=a[i];a[i]=a[a.length-1-i];a[a.length-1-i]=tem;}}
    static void reverse(long a[]){for(int i=0;i<a.length>>1;i++){long tem=a[i];a[i]=a[a.length-1-i];a[a.length-1-i]=tem;}}
    static String reverse(String s){StringBuilder sb=new StringBuilder(s);sb.reverse();return sb.toString();}
}
