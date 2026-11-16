import java.util.*;
import java.io.*;

public class C_1551 {
	public static void main(String[] args) throws Exception {
		Scanner sc = new Scanner(System.in);
		PrintWriter pw = new PrintWriter(System.out);
		
		int t = sc.nextInt();
		while(t-->0) {
			int n = sc.nextInt();
			int[] a = new int[n];
			int[] b = new int[n];
			int[] c = new int[n];
			int[] d = new int[n];
			int[] e = new int[n];
			for(int i = 0; i < n; i++) {
				String s = sc.next();
				int[] cnt = new int[5];
				for(int j = 0; j < s.length(); j++)
					cnt[s.charAt(j) - 'a']++;
				a[i] = 2 * cnt[0] - s.length();
				b[i] = 2 * cnt[1] - s.length();
				c[i] = 2 * cnt[2] - s.length();
				d[i] = 2 * cnt[3] - s.length();
				e[i] = 2 * cnt[4] - s.length();
			}
			
			Arrays.sort(a);
			Arrays.sort(b);
			Arrays.sort(c);
			Arrays.sort(d);
			Arrays.sort(e);
			
			int max = 0;
			int sum = 0;
			for(int i = n - 1; i >= 0; i--) {
				sum += a[i];
				if(sum <= 0)
					break;
				max = n - i;
			}
			
			sum = 0;
			for(int i = n - 1; i >= 0; i--) {
				sum += b[i];
				if(sum <= 0)
					break;
				max = Math.max(max, n - i);	
			}
			
			sum = 0;
			for(int i = n - 1; i >= 0; i--) {
				sum += c[i];
				if(sum <= 0)
					break;
				max = Math.max(max, n - i);	
			}
			
			sum = 0;
			for(int i = n - 1; i >= 0; i--) {
				sum += d[i];
				if(sum <= 0)
					break;
				max = Math.max(max, n - i);	
			}
			
			sum = 0;
			for(int i = n - 1; i >= 0; i--) {
				sum += e[i];
				if(sum <= 0)
					break;
				max = Math.max(max, n - i);	
			}
			
			pw.println(max);
			
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
