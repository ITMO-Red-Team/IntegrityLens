import java.io.*;
import java.util.*;

public class C {

	public static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	public static PrintWriter out = new PrintWriter(new BufferedOutputStream(System.out));

	public static void main(String[] args) throws IOException {
		readInput();
		out.close();
	}
	
	static class Tower implements Comparable<Tower>{
		int i, w;
		public Tower(int a, int b) {
			i=a;
			w=b;
		}
		public int compareTo(Tower o) {return w - o.w;}
	}
	
	static class Block implements Comparable<Block>{
		int i, w;
		public Block(int a, int b) {
			i=a;
			w=b;
		}
		public int compareTo(Block o) {return o.w-w;}
	}

	static PriorityQueue<Tower> towers;
	static PriorityQueue<Block> blocks;
	static int[] par;
	
	static void solve() {
		while (!blocks.isEmpty()) {
			Block h = blocks.poll();
			Tower t = towers.poll();
			par[h.i] = t.i;
			t.w += h.w;
			towers.add(t);
		}
		List<Tower> res = new ArrayList<Tower>(towers);
		Collections.sort(res);
		boolean works = true;
		for (int i =1 ; i < m; i++) {
			if (Math.abs(res.get(i-1).w - res.get(i).w) > x) works = false;
		}
		if (works) {
			out.println("YES");
			for (int i = 0; i < n; i++) out.print(par[i] + 1 + " ");
			out.println();
		}
		else out.println("NO");
	}
	static int n,m,x;
	public static void readInput() throws IOException {
		// br = new BufferedReader(new FileReader(".in"));
		// out = new PrintWriter(new FileWriter(".out"));
		int t = Integer.parseInt(br.readLine());
		while (t-->0) {
			StringTokenizer st=  new StringTokenizer(br.readLine());
			n = Integer.parseInt(st.nextToken());
			m = Integer.parseInt(st.nextToken());
			x = Integer.parseInt(st.nextToken());
			towers = new PriorityQueue<Tower>();
			blocks = new PriorityQueue<Block>();
			par = new int[n];
			st = new StringTokenizer(br.readLine());
			for (int i= 0 ; i < n; i++) {
				blocks.add(new Block(i,Integer.parseInt(st.nextToken())));
			}
			for (int i =0 ; i < m; i++) {
				towers.add(new Tower(i, 0));
			}
			solve();
		}
	}
}
