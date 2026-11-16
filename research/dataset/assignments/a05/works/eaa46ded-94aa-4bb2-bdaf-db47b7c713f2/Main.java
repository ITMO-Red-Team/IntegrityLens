import java.io.*;
import java.util.*;

public class C {
	static int n;
	public static void main (String[] args) throws IOException {
		Kattio io = new Kattio();
		int t = io.nextInt();
		for (int ii=0; ii<t; ii++) {
			n = io.nextInt();

			String[] arr = new String[n];

			for (int i=0; i<n; i++) {
				String str = io.next();
				arr[i] = str;
			}

			char[] chars = new char[]{'a','b','c','d','e'};

			int ans = -1;

			for (int i=0; i<5; i++) {
				ans = Math.max(ans, solve(arr, chars[i]));
			}
			System.out.println(ans);
		}
	}

	static int solve(String[] arr, char c) {
		//System.out.println("Comparing based on " + c);
		Arrays.sort(arr, new Comp(c));

		int good = 0;
		int total = 0;
		int ret = 0;

		for (int i=0; i<n; i++) {
			//System.out.println(good + " " + total);
			for (int j=0; j<arr[i].length(); j++) {
				if (arr[i].charAt(j) == c) good++;
			}
			total += arr[i].length();
			if (2 * good > total) {
				ret++;
			} else {
				return ret;
			}
		}

		return ret;
	}

	static class Comp implements Comparator<String> {
		char c;

		public Comp (char c) {
			this.c = c;
		}

		public int compare(String a, String b) {
			double cnt1 = 0;
			double cnt2 = 0;
			for (int i=0; i<a.length(); i++) {
				if (a.charAt(i) == c) {
					cnt1++;
				}
			}
			for (int i=0; i<b.length(); i++) {
				if (b.charAt(i) == c) {
					cnt2++;
				}
			}
			//higher ratio is better

			return -Double.compare(cnt1 - (a.length() - cnt1), cnt2 - (b.length() - cnt2));
		}
	}

	static class Kattio extends PrintWriter {
		private BufferedReader r;
		private StringTokenizer st;

		// standard input
		public Kattio() { this(System.in, System.out); }
		public Kattio(InputStream i, OutputStream o) {
			super(o);
			r = new BufferedReader(new InputStreamReader(i));
		}
		// USACO-style file input
		public Kattio(String problemName) throws IOException {
			super(new FileWriter(problemName + ".out"));
			r = new BufferedReader(new FileReader(problemName + ".in"));
		}

		// returns null if no more input
		public String next() {
			try {
				while (st == null || !st.hasMoreTokens())
					st = new StringTokenizer(r.readLine());
				return st.nextToken();
			} catch (Exception e) { }
			return null;
		}

		public int nextInt() { return Integer.parseInt(next()); }
		public double nextDouble() { return Double.parseDouble(next()); }
		public long nextLong() { return Long.parseLong(next()); }
	}
}