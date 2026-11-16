
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

public class Main {

	static InputStream inputStream = System.in;
	static OutputStream outputStream = System.out;
	static InputReader in = new InputReader(inputStream);
	static PrintWriter out = new PrintWriter(outputStream);

	public static void main(String[] args) {

		int t = in.nextInt();

		while (t-- > 0) {

			int n = in.nextInt();

			int k = in.nextInt();

			int[] v1 = new int[k];
			int[] v2 = new int[k];

			for (int i = 0; i < k; i++) {
				v1[i] = in.nextInt();
			}

			for (int i = 0; i < k; i++) {
				v2[i] = in.nextInt();
			}
			
			
			
			//wejhfduiwehiofhw

			int[] res = new int[n + 2];

			Arrays.fill(res, 2000000000);

			for (int i = 0; i < k; i++) {
				res[v1[i]] = v2[i];

			}

			for (int i = 1; i <= n; i++) {

				int val = Math.min(res[i], res[i - 1] + 1);

				res[i] = val;

			}
			
			//ewhfowiejp
			
			
			
			
			
			
			
			
			
			//wedhciuwahidochqowi
			
			
			
			
			
			
			
			
			//wjdhoiqwnlidhqw
			for (int i = n; i >= 1; i--) {
				int val1 = Math.min(res[i], res[i + 1] + 1);
				res[i] = val1;
				// out.println(res[i]);
			}

			for (int i = 1; i <= n; i++) {
				int r = res[i];
				out.print(r + " ");

			}

			out.println();

		}

		out.close();
	}

	private static void takeinput(int[] arr, int k) {
		for (int i = 0; i < k; i++) {
			arr[i] = in.nextInt();
		}

	}

	static class InputReader {
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

		public long nextLong() {
			return Long.parseLong(next());
		}

		public int nextInt() {
			return Integer.parseInt(next());
		}

		public String nextLine() throws IOException {
			return reader.readLine().trim();
		}
	}

}
