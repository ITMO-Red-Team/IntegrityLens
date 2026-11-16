import java.io.*;
import java.util.*;

public class Main {
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
	public static void main(String[] args) throws IOException {
		PriorityQueue<Pair> pq = new PriorityQueue();
		for (int t = readInt(); t > 0; --t) {
			int n = readInt(), m = readInt(), x = readInt();
			System.out.println("YES");
			for (int i = 1; i <= m; ++i) {
				pq.add(new Pair(readInt(), i));
				System.out.print(i + " ");
			}
			for (int i = m + 1; i <= n; ++i) {
				Pair p = pq.poll();
				p.f += readInt();
				pq.add(p);
				System.out.print(p.s + " ");
			}
			System.out.println();
			pq.clear();
		}
	}
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
}
