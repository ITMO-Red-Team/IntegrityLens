import java.io.*;
import java.util.*;

public class Main {

//	static boolean[] prime = new boolean[10000000];
	final static long mod = 1000000007;

	public static void main(String[] args) {
//		sieve();
		InputReader in = new InputReader(System.in);
		PrintWriter out = new PrintWriter(System.out);

		int t = in.nextInt();
		while (t-- > 0) {
			int n = in.nextInt();

			Integer[] k = intInput(n, in), h = intInput(n, in);
			long ans = 0;
			int a = k[n - 1];
			int current = k[n - 1];
			for (int i = n - 1; i >= 0; i--) {
				if (current > k[i]) {
					ans += sum(a - current + 1);
					a = k[i];
					current = k[i]-h[i]+1;
				}else {
					current = Math.min(current, k[i] - h[i]+1);
				}
			}
			ans += sum(a - current + 1);
			out.println(ans);
		}

		out.flush();
	}

	static long sum(long a) {
		return a * (a + 1) / 2;
	}

	static boolean prime(long k) {
		for (int i = 2; i * i <= k; i++) {
			if (k % i == 0) {
				return false;
			}
		}
		return true;
	}

	static long gcd(long a, long b) {
		if (a % b == 0) {
			return b;
		} else {
			return gcd(b, a % b);
		}
	}

	static void reverseArray(Integer[] a) {
		for (int i = 0; i < (a.length >> 1); i++) {
			Integer temp = a[i];
			a[i] = a[a.length - 1 - i];
			a[a.length - 1 - i] = temp;
		}
	}

	static Integer[] intInput(int n, InputReader in) {
		Integer[] a = new Integer[n];
		for (int i = 0; i < a.length; i++)
			a[i] = in.nextInt();
		return a;
	}

	static Long[] longInput(int n, InputReader in) {
		Long[] a = new Long[n];
		for (int i = 0; i < a.length; i++)
			a[i] = in.nextLong();
		return a;
	}

	static String[] strInput(int n, InputReader in) {
		String[] a = new String[n];
		for (int i = 0; i < a.length; i++)
			a[i] = in.next();
		return a;
	}

//	static void sieve() {
//		for (int i = 2; i * i < prime.length; i++) {
//			if (prime[i])
//				continue;	
//			for (int j = i * i; j < prime.length; j += i) {
//				prime[j] = true;
//			}
//		}
//	}

}

class Data {
	int val;
	int ind;
	boolean positive;

	Data(int val, int ind) {
		this.val = Math.abs(val);
		this.ind = ind;
		this.positive = val >= 0;
	}
}

class compareVal implements Comparator<Data> {

	@Override
	public int compare(Data o1, Data o2) {
//		return (o1.val - o2.val == 0 ? o1.ind - o2.ind : o1.val - o2.val);
		return (o1.val - o2.val);
	}

}

class compareInd implements Comparator<Data> {

	@Override
	public int compare(Data o1, Data o2) {
		return o1.ind - o2.ind == 0 ? o1.val - o2.val : o1.ind - o2.ind;
	}

}

class InputReader {
	public BufferedReader reader;
	public StringTokenizer tokenizer;

	public InputReader(InputStream stream) {
		reader = new BufferedReader(new InputStreamReader(stream), 32768);
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

	long nextLong() {
		return Long.parseLong(next());
	}

	double nextDouble() {
		return Double.parseDouble(next());
	}

	String nextLine() {
		String str = "";
		try {
			str = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return str;
	}

}