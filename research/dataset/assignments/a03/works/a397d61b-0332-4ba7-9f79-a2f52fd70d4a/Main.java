import java.io.*;
import java.util.*;

public class AirConditioners {
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	static StringTokenizer st;
	static PrintWriter out = new PrintWriter(System.out);

	public static void main(String[] args) throws IOException {
		int q = readInt();
		
		while (q-- > 0) {
			int n = readInt(), k = readInt();
			int[] a = new int[k], t = new int[k], c = new int[n];
			for (int i = 0; i < k; i ++) a[i] = readInt();
			for (int i = 0; i < k; i ++) t[i] = readInt();
			Arrays.fill(c, Integer.MAX_VALUE);
			for (int i = 0; i < k; i ++) c[a[i] - 1] = t[i];
			
			int[] l = new int[n], r = new int[n];
			int prev = (int) 2e9;
			for (int i = 0; i < n; i ++) {
				l[i] = Math.min(prev + 1, c[i]);
				prev = l[i];
			}
			prev = (int) 2e9;
			for (int i = n - 1; i >= 0; i --) {
				r[i] = Math.min(prev + 1, c[i]);
				prev = r[i];
			}
			for (int i = 0; i < n; i ++) System.out.print(Math.min(l[i], r[i]) + " ");
			System.out.println();
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