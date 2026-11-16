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


public class Main{

	static InputStream inputStream = System.in;
	static OutputStream outputStream = System.out;
	static InputReader in = new InputReader(inputStream);
	static PrintWriter out = new PrintWriter(outputStream);

	public static void main(String[] args) {

		int t = in.nextInt();

		while (t-- > 0) {
			int n = in.nextInt();
			String[] v = new String[n];

			for (int i = 0; i < n; i++) {
				v[i] = in.next();
			}

			ArrayList<ArrayList<Integer>> v1 = new ArrayList<>();

			for (int i = 0; i < n; i++) {
				ArrayList<Integer> list = new ArrayList<>();

				for (int j = 0; j < 5; j++) {

					list.add(0);
				}
				v1.add(list);
			}

			for (int i = 0; i < n; i++) {
				String s = v[i];
				for (int j = 0; j < s.length(); j++) {
					int val = v1.get(i).get(s.charAt(j) - 'a');
					val++;
					v1.get(i).set(s.charAt(j) - 'a', val);

				}

			}
			int ret = 0;
			for (int i = 0; i < 5; i++) {
				ArrayList<Integer> v3 = new ArrayList<>();

				for (int j = 0; j < n; j++) {
					String s1 = v[j];
					int n2 = s1.length();
					int cnt = v1.get(j).get(i);
					int diff = cnt - (n2 - cnt);
					v3.add(diff);
				}
				Collections.sort(v3);
				Collections.reverse(v3);

				int till = 0;
				for (int k = 0; k < n; k++) {
					till += v3.get(k);
					if (till > 0) {
						ret = Math.max(ret, k + 1);
					}
				}

			}
			out.println(ret);

		}

		out.close();
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
