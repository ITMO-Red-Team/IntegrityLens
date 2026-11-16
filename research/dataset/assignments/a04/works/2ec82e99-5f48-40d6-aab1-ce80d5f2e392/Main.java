import java.io.*;
import java.util.*;

public class PhoenixAndTowers {
	public static void main(String[] args) throws IOException {
		// BufferedReader in = new BufferedReader(new FileReader("PhoenixAndTowers.in"));
		// PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("PhoenixAndTowers.out")));
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		PrintWriter out = new PrintWriter(System.out);

		int T = Integer.parseInt(in.readLine());
		for (int i = 0; i < T; i++) {
			StringTokenizer st = new StringTokenizer(in.readLine());
			int N = Integer.parseInt(st.nextToken());
			int M = Integer.parseInt(st.nextToken());
			int X = Integer.parseInt(st.nextToken());
			PriorityQueue<Tower> towers = new PriorityQueue<Tower>();
			for (int j = 1; j <= M; j++) towers.add(new Tower(j, 0));
			out.println("YES");
			st = new StringTokenizer(in.readLine());
			for (int j = 0; j < N; j++) {
				Tower t = towers.remove();
				t.size += Integer.parseInt(st.nextToken());
				towers.add(t);
				out.print(t.idx + " ");
			}
			out.println();
		}

		out.close();
		in.close();
	}
	
	public static class Tower implements Comparable<Tower> {
		int idx, size;
		public Tower(int idx, int size) {
			this.idx = idx;
			this.size = size;
		}
		@Override
		public int compareTo(Tower o) {
			return size - o.size;
		}
	}
}