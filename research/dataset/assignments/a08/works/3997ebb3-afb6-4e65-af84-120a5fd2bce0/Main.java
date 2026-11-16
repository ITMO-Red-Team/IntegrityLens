import java.util.*;
import java.lang.*;
import java.io.*;

public class Codechef {
	static long fans[] = new long[200001];
	static long inv[] = new long[200001];
	static long mod = 1000000007;

	static void init() {
		fans[0] = 1;
		inv[0] = 1;
		fans[1] = 1;
		inv[1] = 1;
		for (int i = 2; i < 200001; i++) {
			fans[i] = ((long) i * fans[i - 1]) % mod;
			inv[i] = power(fans[i], mod - 2);
		}
	}

	static long ncr(int n, int r) {
		return (((fans[n] * inv[n - r]) % mod) * (inv[r])) % mod;
	}

	public static void main(String[] args) throws java.lang.Exception {
		FastReader in = new FastReader(System.in);
		StringBuilder sb = new StringBuilder();
		int t = 1;
		t = in.nextInt();
		while (t > 0) {
			--t;
			int n = in.nextInt();
			long time[] = generateArray(in, n);
			long hp[] = generateArray(in, n);
			int s = 0;
			long ans = 0;
			while(s<time.length)
			{
				long l = time[s] - hp[s];
				long r = time[s];
				for(int i = s+1;i<n;i++)
				{
					if(time[i]-hp[i]<=l)
					{
						l = time[i]-hp[i];
						r = time[i];
					}
					else if(time[i]-hp[i]<r)
					{
						r = time[i];
					}
				}
					
					while(s<n && time[s]>=l && time[s]<=r)
					{
						++s;
					}
					long temp = r - l;
					ans += (temp*(temp+1))/2;
			}
			sb.append(ans+"\n");

		}
		System.out.print(sb);
	}

	static long power(long x, long y) {
		long res = 1; // Initialize result

		while (y > 0) {

			// If y is odd, multiply x with result
			if ((y & 1) != 0)
				res = ((res % mod) * (x % mod)) % mod;

			// y must be even now
			y = y >> 1; // y = y/2
			x = ((x % mod) * (x % mod)) % mod; // Change x to x^2
		}
		return res;
	}

	static long[] generateArray(FastReader in, int n) throws IOException {
		long arr[] = new long[n];
		for (int i = 0; i < n; i++)
			arr[i] = in.nextLong();
		return arr;
	}

	static long[][] generatematrix(FastReader in, int n, int m) throws IOException {
		long arr[][] = new long[n][m];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				arr[i][j] = in.nextLong();
			}
		}
		return arr;
	}

	static long gcd(long a, long b) {
		if (a == 0)
			return b;
		else
			return gcd(b % a, a);
	}

	static long lcm(long a, long b) {
		return (a / gcd(a, b)) * b;
	}

	static void sort(long[] a) {
		ArrayList<Long> l = new ArrayList<>();
		for (long i : a)
			l.add(i);
		Collections.sort(l);
		for (int i = 0; i < a.length; i++)
			a[i] = l.get(i);
	}
}

class FastReader {

	byte[] buf = new byte[2048];
	int index, total;
	InputStream in;

	FastReader(InputStream is) {
		in = is;
	}

	int scan() throws IOException {
		if (index >= total) {
			index = 0;
			total = in.read(buf);
			if (total <= 0) {
				return -1;
			}
		}
		return buf[index++];
	}

	String next() throws IOException {
		int c;
		for (c = scan(); c <= 32; c = scan())
			;
		StringBuilder sb = new StringBuilder();
		for (; c > 32; c = scan()) {
			sb.append((char) c);
		}
		return sb.toString();
	}

	String nextLine() throws IOException {
		int c;
		for (c = scan(); c <= 32; c = scan())
			;
		StringBuilder sb = new StringBuilder();
		for (; c != 10 && c != 13; c = scan()) {
			sb.append((char) c);
		}
		return sb.toString();
	}

	char nextChar() throws IOException {
		int c;
		for (c = scan(); c <= 32; c = scan())
			;
		return (char) c;
	}

	int nextInt() throws IOException {
		int c, val = 0;
		for (c = scan(); c <= 32; c = scan())
			;
		boolean neg = c == '-';
		if (c == '-' || c == '+') {
			c = scan();
		}
		for (; c >= '0' && c <= '9'; c = scan()) {
			val = (val << 3) + (val << 1) + (c & 15);
		}
		return neg ? -val : val;
	}

	long nextLong() throws IOException {
		int c;
		long val = 0;
		for (c = scan(); c <= 32; c = scan())
			;
		boolean neg = c == '-';
		if (c == '-' || c == '+') {
			c = scan();
		}
		for (; c >= '0' && c <= '9'; c = scan()) {
			val = (val << 3) + (val << 1) + (c & 15);
		}
		return neg ? -val : val;
	}
}