import java.util.*;
import java.io.*;
import java.math.*;

public class A {
	static FastReader f = new FastReader();
	static PrintWriter out = new PrintWriter(System.out);

	public static void main(String[] args) {
		int t = f.nextInt();
		while(t-- > 0)
			solve();
		out.close();
	}

	static Node[] nodes;
	static long[][] dp;
	static void solve() {
		int n = f.nextInt();

		nodes = new Node[n];

		for(int i=0;i<n;i++) {
			nodes[i] = new Node(f.nextInt(), f.nextInt());
		}

		for(int i=1;i<n;i++) {
			int u = f.nextInt() - 1;
			int v = f.nextInt() - 1;
			nodes[u].adj.add(v);
			nodes[v].adj.add(u);
		}


		dp = new long[n][2];
		for(int i=0;i<n;i++) {
			for(int j=0;j<2;j++) {
				dp[i][j] = -1;
			}
		}


		System.out.println(Math.max(rek(-1, 0,false),rek(-1, 0,true)));


	}

	static long rek(int from, int now, boolean left) { //TODO: DP
		if(dp[now][left?0:1] != -1) {
			return dp[now][left?0:1];
		}

		long ret = 0;

		for(int i : nodes[now].adj) {
			if(i == from) {
				continue;
			}
			long l = rek(now,i,false) + Math.abs(nodes[i].right - (left ? nodes[now].left : nodes[now].right));
			long r = rek(now,i,true) + Math.abs(nodes[i].left - (left ? nodes[now].left : nodes[now].right));
			
			ret += Math.max(l, r);
		}

		dp[now][left?0:1] = ret;
		return ret;
	}

	static class Node {
		int left, right;
		ArrayList<Integer> adj = new ArrayList<>();

		Node(int left, int right) {
			this.left = left;
			this.right = right;
		}
	}


	

	static class FastReader {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st;

		String next() {
			while(st == null || !st.hasMoreElements()) {
				try {
					st = new StringTokenizer(br.readLine());
				} catch(IOException ioe) {
					ioe.printStackTrace();
				}
			}
			return st.nextToken();
		}

		String nextLine() {
			String s = "";
			try {
				s = br.readLine();
			} catch(IOException ioe) {
				ioe.printStackTrace();
			}

			return s;
		}

		int  nextInt() {
			return Integer.parseInt(next());
		}

		double  nextDouble() {
			return Double.parseDouble(next());
		}

		long  nextLong() {
			return Long.parseLong(next());
		}
	}
}
