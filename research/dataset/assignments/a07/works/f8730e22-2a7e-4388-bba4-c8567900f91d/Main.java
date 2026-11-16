import java.util.*;
import java.io.*;

public class Main2 {
	static class FastReader{ 
        BufferedReader br; 
        StringTokenizer st; 
        public FastReader(){br = new BufferedReader(new InputStreamReader(System.in));}
        String next(){while (st == null || !st.hasMoreElements()){try{st = new StringTokenizer(br.readLine());} 
        catch (IOException  e){e.printStackTrace();}}return st.nextToken();}
        int nextInt(){ return Integer.parseInt(next());}long nextLong(){return Long.parseLong(next());}double nextDouble(){return Double.parseDouble(next());}
        String nextLine(){String str = ""; try{str = br.readLine(); } catch (IOException e) {e.printStackTrace();} return str; } 
    }
	static long mod = 998244353;
//	static Scanner sc = new Scanner(System.in);
	static FastReader sc = new FastReader();
	static PrintWriter out = new PrintWriter(System.out);
	public static void main (String[] args) {
		int t = 1;
//    	t = sc.nextInt();
	    z : while(t-->0) {
	    	int n = sc.nextInt();
	    	int a[] = new int[n];
	    	for(int i=0;i<n;i++) a[i] = sc.nextInt();
	    	List<Integer> a1 = new ArrayList<>();
	    	ArrayList<Integer> a2 = new ArrayList<>();
	    	for (int i = 0; i < n; i++) {
	    		if(a[i] == 0) a1.add(i);
	    		else a2.add(i);
			}
	    	long dp[][] = new long[n+1][n+1];
	    	for (int i = 0; i <= n; i++) {
	    		Arrays.fill(dp[i],-1);
			}
	    	out.write(find(0,0,a1,a2,dp)+"\n");
	    }
		out.close();
	}
	private static long find(int i, int j, List<Integer> a1, ArrayList<Integer> a2, long[][] dp) {
		if(j == a2.size()) return 0;
		
		int req = a2.size()-j;
		int ava = a1.size()-i;
		
		if(ava<req) return Integer.MAX_VALUE/2;
		
		if(dp[i][j] != -1) return dp[i][j];
		
		long ans1 = find(i+1,j,a1,a2,dp);
		long ans2 = Math.abs(a1.get(i)-a2.get(j)) + find(i+1,j+1,a1,a2,dp);
		
		return dp[i][j] = Math.min(ans1, ans2);
	}
}
