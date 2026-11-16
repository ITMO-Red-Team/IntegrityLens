import java.util.*;
import java.io.*;
public class C {

	public static void main(String[] args) {
		FastScanner sc = new FastScanner();
		int T = sc.nextInt();
		while(T-->0) {
			int n = sc.nextInt();
			char[] s = new char[n];
			char[] t = new char[n];
			s = sc.next().toCharArray();
			t = sc.next().toCharArray();
			int a = 0, b = 0, c = 0, d = 0;
			for(int i = 0; i < n; i++) {
				if(s[i] == '0' && t[i] == '0') a++;
				if(s[i] == '1' && t[i] == '0') b++;
				if(s[i] == '0' && t[i] == '1') c++;
				if(s[i] == '1' && t[i] == '1') d++;
			}
			int res = Integer.MAX_VALUE;
			if(b == c || b+1 == c) {
				if((b + c) % 2 == 0) {
					res = Math.min(res, b + c);
				}
			}
			if(a == d || a+1 == d) {
				if((a + d) % 2 == 1) {
					res = Math.min(res, a + d);
				}
			}
			if(res == Integer.MAX_VALUE) System.out.println(-1);
			else System.out.println(res);
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
