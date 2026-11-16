import java.io.*;
import java.util.*;

public class C {

	static long mod = (long) (1e9 + 7);

	public static void main(String[] args) throws IOException {
		Scanner scn = new Scanner(System.in);
		PrintWriter out = new PrintWriter(System.out);
		StringBuilder sb = new StringBuilder();
		int T = scn.ni(), tcs = 0;
		C: while (tcs++ < T) {
			int n = scn.ni();
			tree = new ArrayList[n + 1];
			range = new long[n + 1][2];
			for (int i = 0; i <= n; i++)
				tree[i] = new ArrayList<>();
			for (int i = 1; i <= n; i++) {
				range[i][0] = scn.nl();
				range[i][1] = scn.nl();
			}
			for (int i = 0; i < n - 1; i++) {
				int x = scn.ni();
				int y = scn.ni();
				tree[x].add(y);
				tree[y].add(x);
			}
			strg = new long[n + 1][2];
			for (long a1[] : strg)
				Arrays.fill(a1, -1L);
			sb.append(Math.max(DFS(1, -1, 0), DFS(1, -1, 1)));
			sb.append("\n");
		}
		out.print(sb);
		out.close();
	}

	static ArrayList<Integer> tree[];
	static long range[][], strg[][];

	static long DFS(int u, int pa, int ok) {
		if (strg[u][ok] != -1)
			return strg[u][ok];
		long tg = 0;
		for (int ch : tree[u]) {
			if (ch == pa)
				continue;
			long sg = 0;
			if (ok == 0) {
				sg = Math.max(DFS(ch, u, 0) + Math.abs(range[u][0] - range[ch][0]),
						DFS(ch, u, 1) + Math.abs(range[u][0] - range[ch][1]));
			} else {
				sg = Math.max(DFS(ch, u, 0) + Math.abs(range[u][1] - range[ch][0]),
						DFS(ch, u, 1) + Math.abs(range[u][1] - range[ch][1]));
			}
			tg += sg;
		}
		return strg[u][ok] = tg;
	}

	static class Scanner {

		StringTokenizer st;
		BufferedReader br;

		public Scanner(InputStream s) {
			br = new BufferedReader(new InputStreamReader(s));
		}

		public String next() throws IOException {
			while (st == null || !st.hasMoreTokens())
				st = new StringTokenizer(br.readLine());
			return st.nextToken();
		}

		public int ni() throws IOException {
			return Integer.parseInt(next());
		}

		public long nl() throws IOException {
			return Long.parseLong(next());
		}

		public int[] nia(int n) throws IOException {
			int a[] = new int[n];
			String sa[] = br.readLine().split(" ");
			for (int i = 0; i < n; i++)
				a[i] = Integer.parseInt(sa[i]);
			return a;
		}

		public long[] nla(int n) throws IOException {
			long a[] = new long[n];
			String sa[] = br.readLine().split(" ");
			for (int i = 0; i < n; i++)
				a[i] = Long.parseLong(sa[i]);
			return a;
		}

		public void sort(int[] a) {
			ArrayList<Integer> l = new ArrayList<>();
			for (int v : a)
				l.add(v);
			Collections.sort(l);
			for (int i = 0; i < a.length; i++)
				a[i] = l.get(i);
		}

		public void sort(long[] a) {
			ArrayList<Long> l = new ArrayList<>();
			for (long v : a)
				l.add(v);
			Collections.sort(l);
			for (int i = 0; i < a.length; i++)
				a[i] = l.get(i);
		}
	}
}