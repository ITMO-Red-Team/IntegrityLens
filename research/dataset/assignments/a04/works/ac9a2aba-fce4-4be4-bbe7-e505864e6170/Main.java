import java.io.*;
import java.util.*;

public class PhoenixAndTowers {
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	static StringTokenizer st;
	
	static class Pair implements Comparable<Pair> {
		int val, idx;
		Pair (int val, int idx) { this.val = val; this.idx = idx; }
		public int compareTo(Pair p) { 
			if (val == p.val) return Integer.compare(idx, p.idx);
			return Integer.compare(val, p.val); 
		}
	}
	public static void main(String[] args) throws IOException {
		int T = readInt();
		for (int t = 0; t < T; t ++) {
			int n = readInt(), m = readInt(), x = readInt();
			Pair[] h = new Pair[n + 1];
			h[0] = new Pair(100000, 0);
			for (int i = 1; i <= n; i ++) h[i] = new Pair(readInt(), i);
			Arrays.sort(h, Collections.reverseOrder());
			
			int[] ans = new int[n + 1], sum = new int[m + 1];

			PriorityQueue<Pair> q = new PriorityQueue<Pair>();
			for (int i = 1; i <= m; i ++) q.add(new Pair(0, i));
			for (int i = 1; i <= n; i ++) {
				Pair p = q.poll();
				sum[p.idx] += h[i].val;
				ans[h[i].idx] = p.idx;
				q.add(new Pair(p.val + h[i].val, p.idx));
			}
			
			
			int max = 0, min = Integer.MAX_VALUE;
			for (int i = 1; i <= m; i ++) {
				max = Math.max(max, sum[i]);
				min = Math.min(min, sum[i]);
			}
			
			if (max - min > x) System.out.println("NO");
			else {
				System.out.println("YES");
				for (int i = 1; i < n; i ++) System.out.print(ans[i] + " ");
				System.out.println(ans[n]);
			}
		}
	}

	static String next() throws IOException {
		while (st == null || !st.hasMoreTokens()) 
			st = new StringTokenizer(br.readLine().trim());
		return st.nextToken();
	}
	static long readLong() throws IOException {
		return Long.parseLong(next());
	}
	static int readInt() throws IOException {
		return Integer.parseInt(next());
	}
	static double readDouble() throws IOException {
		return Double.parseDouble(next());
	}
	static char readCharacter() throws IOException {
		return next().charAt(0);
	}
	static String readLine() throws IOException {
		return br.readLine().trim();
	}
}