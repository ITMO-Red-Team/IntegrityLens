
import java.util.*;
import java.util.stream.Collectors;
import java.io.*;
import java.math.*;

public class GR18_C2 {
	public static FastScanner sc;
	public static StringBuilder sb ;public static int MOD= 1000000007;
	public static class FastScanner {
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
		String nextLine() throws IOException {
			return br.readLine();
		}
    }

    public static void solve(int t) throws IOException {
    	int n=sc.nextInt();
    	String a=sc.next();
    	String b=sc.next();
    	
    	int _00=0;
    	int _01=0;
    	int _10=0;
    	int _11=0;
    	
    	for(int i=0;i<n;i++) {
    		if(a.charAt(i)=='0' && b.charAt(i)=='0') _00++;
    		if(a.charAt(i)=='0' && b.charAt(i)=='1') _01++;
    		if(a.charAt(i)=='1' && b.charAt(i)=='0') _10++;
    		if(a.charAt(i)=='1' && b.charAt(i)=='1') _11++;
    	}
    	
    	int ans=Integer.MAX_VALUE;
    	
    	
    	// All _01 and _10 can be converted to _11 and _00 by swapping and this requires to operations
    	if(_01==_10) {
    		ans=Math.min(ans, _10*2);
    	}
    	// By performing one operation, we get _01==_10 and then even operations can be done.
    	// Possible when _11=_00 and one additional _11 for odd operation
    	if(_00+1==_11) {
    		ans=Math.min(ans, (_11-1)*2+1);
    	}
    	if(ans==Integer.MAX_VALUE) sb.append(-1).append('\n');
    	else sb.append(ans).append('\n');
    }
    
	public static void main(String[] args) throws IOException {
		sb = new StringBuilder("");
		sc = new FastScanner();
		int t=sc.nextInt();
		for(int i=1;i<=t;i++){
			solve(i);
		}
		System.out.println(sb);
	}
	
	
}
