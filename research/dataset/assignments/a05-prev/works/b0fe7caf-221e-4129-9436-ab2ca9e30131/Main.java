
import java.util.*;
import java.util.Map.Entry;
import java.lang.*;
import java.io.*;
import java.math.BigInteger;


public class CF {
	private static FS sc = new FS();
	
	private static class FS {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer("");
		String next() {
			while (!st.hasMoreTokens())
				try {
					st=new StringTokenizer(br.readLine());				               
	            } catch (IOException e) {}
			return st.nextToken();
		}
		
		int nextInt() {
			return Integer.parseInt(next());
		}
		long nextLong() {
			return Long.parseLong(next());
		}
	}
	
	private static class extra {
		
		static int[] intArr(int size) {
			int[] a = new int[size];
			for(int i = 0; i < size; i++) a[i] = sc.nextInt();
			return a;
		}
		
		static long[] longArr(int size) {
			long[] a = new long[size];
			for(int i = 0; i < size; i++) a[i] = sc.nextLong();
			return a;
		}
		
		static long intSum(int[] a) {
			long sum = 0; 
			for(int i = 0; i < a.length; i++) {
				sum += a[i];
			}
			return sum;
		}
		
		static long longSum(long[] a) {
			long sum = 0; 
			for(int i = 0; i < a.length; i++) {
				sum += a[i];
			}
			return sum;
		}
		
		static LinkedList[] graphD(int vertices, int edges) {
			LinkedList<Integer>[] temp = new LinkedList[vertices+1];
			for(int i = 0; i <= vertices; i++) temp[i] = new LinkedList<>();
			for(int i = 0; i < edges; i++) {
				int x = sc.nextInt();
				int y = sc.nextInt();
				temp[x].add(y);
			}
			return temp;
		}
		
		static LinkedList[] graphUD(int vertices, int edges) {
			LinkedList<Integer>[] temp = new LinkedList[vertices+1];
			for(int i = 0; i <= vertices; i++) temp[i] = new LinkedList<>();
			for(int i = 0; i < edges; i++) {
				int x = sc.nextInt();
				int y = sc.nextInt();
				temp[x].add(y);
				temp[y].add(x);
			}
			return temp;
		}
		
		static void printG(LinkedList[] temp) {
			for(LinkedList<Integer> aa:temp) System.out.println(aa);
		}
		
		static long cal(long val, long pow) {
			if(pow == 0) return 1;
			long res = cal(val, pow/2);
			
			long ret = (res*res)%mod;
			if(pow%2 == 0) return ret;
			return (val*ret)%mod;
		}
		
		static long gcd(long a, long b) { return b == 0 ? a:gcd(b, a%b); }
		
	}
	
	static int mod = 998244353;//(int) 1e9 + 7;
//	static int mod = (int) 998244353;
	static int max = (int) 1e6, sq = 316;
	static LinkedList<Integer>[] temp;
//	static int[] par, rank;
	
	static class pair implements Comparator<pair>{
		int val1;
		int val2;
		int idx;
		pair(int x, int y, int z) {
			val1 = x; val2 = y; idx = z;
		}
		pair() {}
		
		@Override
		public int compare(pair o1, pair o2) {
			if(o1.val1-o1.val2 > o2.val1 - o2.val2) return -1;
			else return 1;
		}
	}
	
	public static void main(String[] args) {	
		int t = sc.nextInt();
//		int t = 1;
		StringBuilder ret = new StringBuilder();
		while(t-- > 0) {
			int n = sc.nextInt();
			String[] s = new String[n];
			int[][] pre = new int[n][5];
			for(int i = 0; i < n; i++) {
				s[i] = sc.next();
				for(int j = 0; j < s[i].length(); j++) {
					pre[i][s[i].charAt(j)-'a']++;
				}
			}
			int max = 0;
			for(int i = 0; i < 5; i++) {
				int[] vis = new int[n];
				int co = 0, toC = 0, oth = 0;
//				char tar = (char)(i+'a');
				for(int j = 0; j < n; j++) {
					int tot = 0;
					for(int aa:pre[j]) tot += aa;
					int rem = tot-pre[j][i];
					if(pre[j][i] > rem) {
						co++;
						toC += pre[j][i];
						vis[j] = 1;
						oth += rem;
					}
				}
				PriorityQueue<pair> q = new PriorityQueue<>(new pair());
				for(int j = 0; j < n; j++) {
					if(vis[j] == 1) continue;
					int tot = 0;
					for(int aa:pre[j]) tot += aa;
					int rem = tot - pre[j][i];
					q.add(new pair(pre[j][i], rem, j));
				}
//				for(pair aa:q) System.out.println(aa.val1 + " " + aa.idx);
//				System.out.println();
				while(!q.isEmpty()) {
					pair cur = q.poll();
					if(cur.val1 + toC > cur.val2+oth) {
						toC += cur.val1;
						oth += cur.val2;
						co++;
					}
				}
//				System.out.println(co);
				max = Math.max(max, co);
			}
			ret.append(max + "\n");
   		}
		System.out.println(ret);
	}
	
	
}