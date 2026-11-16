import java.util.*;
import java.io.*;

public class E_1547 {
	public static void main(String[] args) throws Exception {
		Scanner sc = new Scanner(System.in);
		PrintWriter pw = new PrintWriter(System.out);
		
		int T = sc.nextInt();
		while(T-->0) {
			int n = sc.nextInt(), k = sc.nextInt();
			int[] a = sc.nextIntArray(k);
			int[] t = sc.nextIntArray(k);
			
			int[] array = new int[n];
			Arrays.fill(array, Integer.MAX_VALUE);
			for(int i = 0; i < k; i++)
				array[a[i] - 1] = t[i];
			
			int[] pre = new int[n];
			int[] post = new int[n];
			
			int prev = (int)2e9;
			for(int i = 0; i < n; i++)
				prev = pre[i] = Math.min(prev + 1, array[i]);
			
			prev = (int)2e9;
			for(int i = n - 1; i >= 0; i--)
				prev = post[i] = Math.min(prev + 1, array[i]);
			
			for(int i = 0; i < n; i++)
				array[i] = Math.min(pre[i], post[i]);
			
			for(int i = 0; i < n; i++)
				pw.print(array[i] + (i == n - 1 ? "\n" : " "));
		}
		
		pw.flush();
	}

	public static class Scanner {
		StringTokenizer st;
		BufferedReader br;

		public Scanner(InputStream system) {
			br = new BufferedReader(new InputStreamReader(system));
		}

		public Scanner(String file) throws Exception {
			br = new BufferedReader(new FileReader(file));
		}

		public String next() throws IOException {
			while (st == null || !st.hasMoreTokens())
				st = new StringTokenizer(br.readLine());
			return st.nextToken();
		}

		public String nextLine() throws IOException {
			return br.readLine();
		}

		public int nextInt() throws IOException {
			return Integer.parseInt(next());
		}

		public double nextDouble() throws IOException {
			return Double.parseDouble(next());
		}

		public char nextChar() throws IOException {
			return next().charAt(0);
		}

		public long nextLong() throws IOException {
			return Long.parseLong(next());
		}

		public int[] nextIntArray(int n) throws IOException {
			int[] array = new int[n];
			for (int i = 0; i < n; i++)
				array[i] = nextInt();
			return array;
		}

		public Integer[] nextIntegerArray(int n) throws IOException {
			Integer[] array = new Integer[n];
			for (int i = 0; i < n; i++)
				array[i] = new Integer(nextInt());
			return array;
		}

		public long[] nextLongArray(int n) throws IOException {
			long[] array = new long[n];
			for (int i = 0; i < n; i++)
				array[i] = nextLong();
			return array;
		}

		public double[] nextDoubleArray(int n) throws IOException {
			double[] array = new double[n];
			for (int i = 0; i < n; i++)
				array[i] = nextDouble();
			return array;
		}

		public static int[] shuffle(int[] a) {
			int n = a.length;
			Random rand = new Random();
			for (int i = 0; i < n; i++) {
				int tmpIdx = rand.nextInt(n);
				int tmp = a[i];
				a[i] = a[tmpIdx];
				a[tmpIdx] = tmp;
			}
			return a;
		}

		public boolean ready() throws IOException {
			return br.ready();
		}

		public void waitForInput() throws InterruptedException {
			Thread.sleep(3000);
		}
	}
}
