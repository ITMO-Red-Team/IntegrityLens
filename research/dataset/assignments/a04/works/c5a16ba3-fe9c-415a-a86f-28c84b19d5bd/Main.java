import java.util.*;
import java.io.*;
public class C {

	public static void main(String[] args) {
		FastScanner sc = new FastScanner();
		int T = sc.nextInt();
		StringBuilder sb = new StringBuilder();
		while(T-->0) {
			int n = sc.nextInt();
			int m = sc.nextInt();
			long x = sc.nextLong();
			long[] arr = new long[n];
			for(int i = 0; i < n; i++) {
				arr[i] = sc.nextLong();
			}
			int[] res = new int[n];
			PriorityQueue<Pair> q = new PriorityQueue<>();
			for(int i = 0; i < m; i++) {
				q.add(new Pair(i+1, 0));
			}
			for(int i = 0; i < n; i++) {
				Pair p = q.poll();
				res[i] = p.i;
				q.add(new Pair(p.i, p.w + arr[i]));
			}
			sb.append("YES\n");
			for(int i = 0; i < n; i++) {
				sb.append(res[i]+" ");
			}
			sb.replace(sb.length()-1, sb.length(), "\n");
		}
		PrintWriter pw = new PrintWriter(System.out);
		pw.println(sb.toString().trim());
		pw.flush();

	}
	static class Pair implements Comparable<Pair>{
		int i; long w;
		public Pair(int i, long w) {
			this.i = i; this.w = w;
		}
		public String toString() {
			return i+" "+w;
		}
		@Override
		public int compareTo(Pair p) {
			return Long.compare(w, p.w);
		}
	}
	
	static class FastScanner {
		public BufferedReader reader;
		public StringTokenizer tokenizer;
		public FastScanner() {
			reader = new BufferedReader(new InputStreamReader(System.in), 32768);
			tokenizer = null;
		}
		public String next() {
			while (tokenizer == null || !tokenizer.hasMoreTokens()) {
				try {
					tokenizer = new StringTokenizer(reader.readLine());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			return tokenizer.nextToken();
		}
		public int nextInt() {
			return Integer.parseInt(next());
		}
		public long nextLong() {
			return Long.parseLong(next());
		}
		public double nextDouble() {
			return Double.parseDouble(next());
		}
		public String nextLine() {
			try {
				return reader.readLine();
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
