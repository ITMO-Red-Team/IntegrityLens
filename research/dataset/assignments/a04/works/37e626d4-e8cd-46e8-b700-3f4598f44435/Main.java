import java.util.*;
import java.io.*;

public class Solution {

	static Scanner scn = new Scanner(System.in);
	static PrintWriter out = new PrintWriter(System.out);
	static StringBuilder sb = new StringBuilder();

	public static void main(String[] HastaLaVistaLa) {
		int t = scn.nextInt();
		while(t-- > 0) solve();
		out.println(sb);
		out.close();
	}

	public static void solve() {
		// Road To Specialist Day 3
		int n = scn.nextInt(), m = scn.nextInt(), x = scn.nextInt();
		int[] a = new int[n], ans = new int[n];
		for(int i = 0; i < n; i++) a[i] = scn.nextInt();
		PriorityQueue<Pair> pq = new PriorityQueue<>();
		for(int i = 0; i < m; i++) pq.add(new Pair(0L, i));
		for(int i = 0; i < n; i++) {
			int e = a[i];
			Pair p = pq.poll();
			p.value += e;
			pq.add(p);
			ans[i] = p.id + 1;
		}
		boolean check = false;
		long prev = pq.poll().value;
		while(!pq.isEmpty()) {
			long cur = pq.poll().value;
			if(Math.abs(cur - prev) > x) check = true;
			prev = cur;
		}
		if(check) sb.append("NO");
		else {
			sb.append("YES\n");
			for(int i : ans) sb.append(i + " ");
		}
		sb.append("\n");
	}
	static class Pair implements Comparable<Pair> {
		int id;
		long value;
		public Pair(long value, int id) {
			this.id = id;
			this.value = value;
		}
		public int compareTo(Pair o) {
			return Long.compare(value, o.value);
		}
	}
}