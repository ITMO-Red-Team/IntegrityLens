
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Main {

	public Main() throws FileNotFoundException {

//		File file = Paths.get("input.txt").toFile();
//		if (file.exists()) {
//			System.setIn(new FileInputStream(file));
//		}
		long t = System.currentTimeMillis();

		InputReader reader = new InputReader();

		int ttt = reader.nextInt();

		for (int tt = 0; tt < ttt; tt++) {
			int n=reader.nextInt();
			
			long[] s=new long[n];
			for(int i=0;i<n;i++) {
				s[i]=reader.nextLong();
			}
			
			long smallest1=s[0];
			long smallest2=s[1];
			long val=n*s[0]+n*s[1];
			
			int left1=n-1;
			int left2=n-1;
			long base=s[0]+s[1];
			for(int i=2;i<n;i++) {
				if(i%2==0) {
					//left1
					val=Math.min(val, base+left2*smallest2+left1*s[i]);
					base+=s[i];
					smallest1=Math.min(smallest1, s[i]);
					left1--;
				}else {
					val=Math.min(val, base+left1*smallest1+left2*s[i]);
					base+=s[i];
					smallest2=Math.min(smallest2, s[i]);
					left2--;
					//left2
				}
			}
			

			System.out.println(val);
		}

	}

	static class InputReader {
		private byte[] buf = new byte[16384];
		private int curChar;
		private int numChars;

		public int read() {
			if (numChars == -1)
				throw new InputMismatchException();
			if (curChar >= numChars) {
				curChar = 0;
				try {
					numChars = System.in.read(buf);
				} catch (IOException e) {
					throw new InputMismatchException();
				}
				if (numChars <= 0)
					return -1;
			}
			return buf[curChar++];
		}

		public String nextLine() {
			int c = read();
			while (isSpaceChar(c))
				c = read();
			StringBuilder res = new StringBuilder();
			do {
				res.appendCodePoint(c);
				c = read();
			} while (!isEndOfLine(c));
			return res.toString();
		}

		public String nextString() {
			int c = read();
			while (isSpaceChar(c))
				c = read();
			StringBuilder res = new StringBuilder();
			do {
				res.appendCodePoint(c);
				c = read();
			} while (!isSpaceChar(c));
			return res.toString();
		}

		public long nextLong() {
			int c = read();
			while (isSpaceChar(c))
				c = read();
			int sgn = 1;
			if (c == '-') {
				sgn = -1;
				c = read();
			}
			long res = 0;
			do {
				if (c < '0' || c > '9')
					throw new InputMismatchException();
				res *= 10;
				res += c - '0';
				c = read();
			} while (!isSpaceChar(c));
			return res * sgn;
		}

		public int nextInt() {
			int c = read();
			while (isSpaceChar(c))
				c = read();
			int sgn = 1;
			if (c == '-') {
				sgn = -1;
				c = read();
			}
			int res = 0;
			do {
				if (c < '0' || c > '9')
					throw new InputMismatchException();
				res *= 10;
				res += c - '0';
				c = read();
			} while (!isSpaceChar(c));
			return res * sgn;
		}

		public int[] nextIntArray(int n) {
			int[] arr = new int[n];
			for (int i = 0; i < n; i++) {
				arr[i] = nextInt();
			}
			return arr;
		}

		public long[] nextLongArray(int n) {
			long[] arr = new long[n];
			for (int i = 0; i < n; i++) {
				arr[i] = nextLong();
			}
			return arr;
		}

		private boolean isSpaceChar(int c) {
			return c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == -1;
		}

		private boolean isEndOfLine(int c) {
			return c == '\n' || c == '\r' || c == -1;
		}

	}

	public static void main(String[] args) throws FileNotFoundException {
		new Main();
	}

}
