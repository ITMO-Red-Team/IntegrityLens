import java.io.*;
import java.util.*;
import java.math.BigInteger;
import java.util.InputMismatchException;

public class Main {
	static PrintWriter out;
	static Reader in;
	public static void main(String[] args) throws IOException {
		input_output();
		Main solver = new Main();
		solver.solve();
		out.close();

		out.flush();
	} 

	static long INF = (long)1e18;
	static int MAXN = (int)1e5 + 5;
	static int MOD = (int)1e9 + 7;
	static int q, t, n, m, k;
	static double pi = Math.PI;

	void solve() throws IOException {
		t = in.nextInt();
		

		while (t --> 0) {
			n = in.nextInt();

			String[] arr = new String[n];
			for (int i = 0; i < n; i++) {
				arr[i] = in.next();
			}

			int ans = 0;
			for (int i = 0; i < 26; i++) {
				List<Integer> list = new ArrayList<>();
				for (int j = 0; j < n; j++) {
					int tmp = 0;
					for (int x = 0; x < arr[j].length(); x++) {
						if (arr[j].charAt(x)-'a' == i) tmp++;
						else tmp--;
					}

					list.add(tmp);
				}

				Collections.sort(list);
				int sum = 0, tmpAns = 0;
				for (int j = n-1; j >= 0; j--) {
					if (sum + list.get(j) > 0) {
						tmpAns++;
						sum += list.get(j);
					} else break;
				}
				ans = Math.max(ans, tmpAns);
			}

			out.println(ans);
		}	
	}


	static class Reader {
		private InputStream mIs;
		private byte[] buf = new byte[1024];
		private int curChar;
		private int numChars;

		public Reader() {
			this(System.in);
		}

		public Reader(InputStream is) {
			mIs = is;
		}

		public int read() {
			if (numChars == -1) {
				throw new InputMismatchException();

			}
			if (curChar >= numChars) {
				curChar = 0;
				try {
					numChars = mIs.read(buf);
				} catch (IOException e) {
					throw new InputMismatchException();
				}
				if (numChars <= 0) {
					return -1;
				}
			}
			return buf[curChar++];
		}

		public String nextLine() {
			int c = read();
			while (isSpaceChar(c)) {
				c = read();
			}
			StringBuilder res = new StringBuilder();
			do {
				res.appendCodePoint(c);
				c = read();
			} while (!isEndOfLine(c));
			return res.toString();
		}

		public String next() {
			int c = read();
			while (isSpaceChar(c)) {
				c = read();
			}
			StringBuilder res = new StringBuilder();
			do {
				res.appendCodePoint(c);
				c = read();
			} while (!isSpaceChar(c));
			return res.toString();
		}

		double nextDouble() {
			return Double.parseDouble(next());
		}

		public long nextLong() {
			int c = read();
			while (isSpaceChar(c)) {
				c = read();
			}
			int sgn = 1;
			if (c == '-') {
				sgn = -1;
				c = read();
			}
			long res = 0;
			do {
				if (c < '0' || c > '9') {
					throw new InputMismatchException();
				}
				res *= 10;
				res += c - '0';
				c = read();
			} while (!isSpaceChar(c));
			return res * sgn;
		}

		public int nextInt() {
			int c = read();
			while (isSpaceChar(c)) {
				c = read();
			}
			int sgn = 1;
			if (c == '-') {
				sgn = -1;
				c = read();
			}
			int res = 0;
			do {
				if (c < '0' || c > '9') {
					throw new InputMismatchException();
				}
				res *= 10;
				res += c - '0';
				c = read();
			} while (!isSpaceChar(c));
			return res * sgn;
		}

		public boolean isSpaceChar(int c) {
			return c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == -1;
		}

		public boolean isEndOfLine(int c) {
			return c == '\n' || c == '\r' || c == -1;
		}

	}
	static void input_output() throws IOException {
		File f = new File("in.txt");
		if (f.exists() && !f.isDirectory()) {
			in = new Reader(new FileInputStream("in.txt"));
		} else in = new Reader();
		f = new File("out.txt");
		if (f.exists() && !f.isDirectory()) {
			out = new PrintWriter(new File("out.txt"));
		} else out = new PrintWriter(System.out);
	}
}