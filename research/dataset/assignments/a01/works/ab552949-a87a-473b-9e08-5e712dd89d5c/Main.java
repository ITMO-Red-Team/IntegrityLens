import java.lang.*;
import java.util.*;
import java.io.*;
public class C {
	final static int mod = (int)(1e9 + 7);
	static LinkedList<Integer>[] adj;
	static int arr[][] ;
	static long dp[][] ;
	public static void main(String[] args) {
		FastReader fs = new FastReader();
		int testcase = 1;
		testcase = fs.nextInt();
		//int i = 1;
		while (testcase-- > 0) {
			//System.out.print("Case #" + Integer.toString(i) + ": ");
			solve(fs);
			//i++;
		}

	}
	public static void solve(FastReader fs) {
		int n = fs.nextInt();
		arr = new int[n + 1][2];
		adj = new LinkedList[n + 1];
		dp = new long[n + 1][2];
		for (int i = 0; i < adj.length; i++) {
			adj[i] = new LinkedList<Integer>();
		}
		for (int i = 1; i < arr.length; i++) {
			arr[i][0] = fs.nextInt();
			arr[i][1] = fs.nextInt();
		}
		for (int i = 0; i < n - 1; i++) {
			int u = fs.nextInt();
			int v = fs.nextInt();
			adj[u].add(v);
			adj[v].add(u);
		}
		dfs(1, -1);

		System.out.println(Math.max(dp[1][0], dp[1][1]));
	}

	static void dfs(int source , int parent) {
		for (int val : adj[source]) {
			if (val == parent)
				continue;
			dfs(val, source);
			dp[source][0] += Math.max(dp[val][0] + Math.abs(arr[source][0] - arr[val][0]),
			                          dp[val][1] + Math.abs(arr[source][0] - arr[val][1]));
			dp[source][1] += Math.max(dp[val][0] + Math.abs(arr[source][1] - arr[val][0]),
			                          dp[val][1] + Math.abs(arr[source][1] - arr[val][1]));
		}
	}


//IO operation
	static class FastReader {
		BufferedReader br;
		StringTokenizer st;
		public FastReader() {
			br = new BufferedReader(
			    new InputStreamReader(System.in));
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

		int nextInt() { return Integer.parseInt(next()); }

		long nextLong() { return Long.parseLong(next()); }

		double nextDouble() {
			return Double.parseDouble(next());
		}

		String nextLine() {
			String str = "";
			try {
				str = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return str;
		}
	}
}