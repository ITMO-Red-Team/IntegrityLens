import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

public class B{
	static FastScanner sc = new FastScanner();
	static int inf = Integer.MAX_VALUE;
	static long mod = 1000000007;
	static BufferedWriter output = new BufferedWriter(
			new OutputStreamWriter(System.out));
	
	
	public static long util(int arr[]) {
		Arrays.sort(arr);
		int i = 0;
		int j = arr.length - 1;
		while(i <= j) {
			int tmp = arr[i];
			arr[i] = arr[j];
			arr[j] = tmp;
			i++;
			j--;
		}
		int n = arr.length;
		//min priority queue
		PriorityQueue<Integer> pq = new PriorityQueue<>();
		long ans = 0;
		long sum = 0;
		long cnt = 0;
		for(int ii: arr) {
			if( ii == 0) cnt++;
		}
		for(i =0;i<n;i++) {
			
			sum += arr[i];
			ans++;
			if(arr[i] < 0) {
				pq.add(arr[i]);
			}
			if( sum <= 0 ) {
				while( sum <= 0 ) {
					if(pq.isEmpty()) break;
					sum -= pq.peek();
					ans--;
					pq.remove();
					
				}
			}
		}
		if(sum > 0) 
			return ans;
		return ans - cnt;
		
		
		
	}
	
	
	public static void solve() {
		int n = sc.nextInt();
		String a[] = new String[n];
		for(int i =0;i<n;i++) {
			a[i] = sc.next();
		}
		
		long ans =  -1;
		for(int i =0;i<5;i++) {
			char mx = (char)(i + 'a');
			int c1 = 0, c2 = 0;
			int arr[] = new int[n];
			for(int j =0;j<n;j++) {
				c1 = 0;
				c2 = 0;
				for(int k = 0;k<a[j].length();k++) {
					if(a[j].charAt(k) == mx) c1++;
					else c2++;
				}
				arr[j]= c1 - c2;
			}
			ans = max(ans, util(arr));
		}
		System.out.println(ans);
		
	}
	public static void main(String[] args) {
		int t = 1;
		t = sc.nextInt();
		outer: for (int tt = 0; tt < t; tt++) {
			solve();
		}
	}
	
////////////////////////////////////////////////////////////////////////////////////
////////////////////DO NOT BELOW THIS LINE //////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////
	
	static int max(int a, int b) { return Math.max(a, b);} 
	static int min(int a, int b) {return Math.min(a, b);}
	static long max(long a, long b){return Math.max(a, b);}
	static long min(long a, long b) {return Math.min(a, b);}
	static void sort(int arr[]) {
		ArrayList<Integer> a = new ArrayList<Integer>();
		for(int i: arr) a.add(i);
		Collections.sort(a);
		for(int i=0;i<arr.length;i++) arr[i] = a.get(i);
	}
	static void sort(long arr[]) {
		ArrayList<Long> a = new ArrayList<Long>();
		for(long i: arr) a.add(i);
		Collections.sort(a);
		for(int i=0;i<arr.length;i++) arr[i] = a.get(i);
	}
	static int abs(int a) {return Math.abs(a);}
	static long abs(long a) {return Math.abs(a);}
	static int gcd(int a, int b) {
		if (b == 0)
			return a;
		return gcd(b, a % b);
	}
	static long gcd(long a, long b) {
		if (b == 0)
			return a;
		return gcd(b, a % b);
	}
	
	static class FastScanner {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer("");
		
		String next() {
			while (!st.hasMoreTokens())
				try {
					st = new StringTokenizer(br.readLine());
				} catch (IOException e) {
					e.printStackTrace();
				}
			return st.nextToken();
		}
		int nextInt() {
			return Integer.parseInt(next());
		}
		int[] readArray(int n) {
			int[] a = new int[n];
			for (int i = 0; i < n; i++)
				a[i] = nextInt();
			return a;
		}
		long[] readLongArray(int n) {
			long[] a = new long[n];
			for (int i = 0; i < n; i++)
				a[i] = nextLong();
			return a;
		}
		
		int[][] read2dArray(int n, int m) {
			int arr[][] = new int[n][m];
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m; j++) {
					arr[i][j] = nextInt();
				}
			}
			return arr;
		}
		
		ArrayList<Integer> readArrayList(int n) {
			ArrayList<Integer> arr = new ArrayList<Integer>();
			for (int i = 0; i < n; i++) {
				int a = nextInt();
				arr.add(a);
			}
			return arr;
		}
		
		long nextLong() {
			return Long.parseLong(next());
		}
	}
	
	static class pair {
		int fr, sc;
		
		pair(int fr, int sc) {
			this.fr = fr;
			this.sc = sc;
		}
	}
	
////////////////////////////////////////////////////////////////////////////////////	
	
}
