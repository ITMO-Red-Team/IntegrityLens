import java.io.*;
import java.util.*;

public class Main {
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	static StringTokenizer st;
	static String next() throws IOException {
		while (st == null || !st.hasMoreTokens())
			st = new StringTokenizer(br.readLine());
		return st.nextToken();
	}
	static int readInt() throws IOException {
		return Integer.parseInt(next());
	}
	static long readLong() throws IOException {
		return Long.parseLong(next());
	}
	static double readDouble() throws IOException {
		return Double.parseDouble(next());
	}
	static char readChar() throws IOException {
		return next().charAt(0);
	}
	static String readLine() throws IOException {
		return br.readLine();
	}
	static class Pair implements Comparable<Pair> {
		int f, s;
		Pair(int f, int s) {
			this.f = f; this.s = s;
		}
		public int compareTo(Pair other) {
			if (this.f != other.f) return this.f - other.f;
			return this.s - other.s;
		}
	}
	static List<Integer> adj[];
	static int n, a[][];
	static long dp[][];
	static void DFS(int v, int p) {
		for (int u : adj[v]) {
			if (u != p) {
				DFS(u, v);
				for (int i = 0; i <= 1; ++i) {
					long add = 0;
					for (int j = 0; j <= 1; ++j)
						add = Math.max(add, dp[u][j] + Math.abs(a[v][i] - a[u][j]));
					dp[v][i] += add;
				}
			}
		}
	}
	public static void main(String[] args) throws IOException {
		for (int t = readInt(); t > 0; --t) {
			n = readInt();
			adj = new List[n + 1];
			for (int i = 1; i <= n; ++i)
				adj[i] = new ArrayList();
			a = new int[n + 1][2];
			for (int i = 1; i <= n; ++i) {
				a[i][0] = readInt();
				a[i][1] = readInt();
			}
			for (int i = 1; i < n; ++i) {
				int u = readInt(), v = readInt();
				adj[u].add(v);
				adj[v].add(u);
			}
			dp = new long[n + 1][2];
			DFS(1, 0);
			System.out.println(Math.max(dp[1][0], dp[1][1]));
		}
	}
}

