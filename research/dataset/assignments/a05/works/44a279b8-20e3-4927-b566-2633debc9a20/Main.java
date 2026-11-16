import java.util.*;
import java.io.*;

public class C1551 {

	public static void main(String[] args) throws IOException {
		Scanner sc = new Scanner(System.in);
		PrintWriter pw = new PrintWriter(System.out);
		int t = sc.nextInt();
		while (t-- > 0) {
			int n = sc.nextInt();
			char[][] arr = new char[n][];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = sc.next().toCharArray();
			}
			int[][] cnt = new int[n][5];
			for (int i = 0; i < cnt.length; i++) {
				for (char c : arr[i]) {
					cnt[i][c - 'a']++;
				}
			}
			int fans = 0;
			for (int letter = 0; letter < 5; letter++) {
				ArrayList<Integer> al = new ArrayList<Integer>();
				for (int i = 0; i < n; i++) {
					al.add(2 * cnt[i][letter] - arr[i].length);
				}
				Collections.sort(al, Collections.reverseOrder());
				int sum = 0;
				int ans = 0;
				for (int x : al) {
					sum += x;
					if (sum > 0) {
						ans++;
					} else {
						break;
					}
				}
				fans = Math.max(ans, fans);
			}
			pw.println(fans);
		}
		pw.close();
	}

	static class Scanner {
		BufferedReader br;
		StringTokenizer st;

		public Scanner(InputStream s) {
			br = new BufferedReader(new InputStreamReader(s));
		}

		public Scanner(FileReader f) {
			br = new BufferedReader(f);
		}

		public String next() throws IOException {
			while (st == null || !st.hasMoreTokens())
				st = new StringTokenizer(br.readLine());
			return st.nextToken();
		}

		public int nextInt() throws IOException {
			return Integer.parseInt(next());
		}

		public long nextLong() throws IOException {
			return Long.parseLong(next());
		}

		public double nextDouble() throws IOException {
			return Double.parseDouble(next());
		}

		public int[] nextIntArr(int n) throws IOException {
			int[] arr = new int[n];
			for (int i = 0; i < n; i++) {
				arr[i] = Integer.parseInt(next());
			}
			return arr;
		}

	}

}
