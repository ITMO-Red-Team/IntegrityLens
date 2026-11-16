//package currentContest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.StringTokenizer;

public class P4 {
	static int N = 1000001;

	// Array to store inverse of 1 to N
	static long[] factorialNumInverse = new long[N + 1];

	// Array to precompute inverse of 1! to N!
	static long[] naturalNumInverse = new long[N + 1];

	// Array to store factorial of first N numbers
	static long[] fact = new long[N + 1];

	// Function to precompute inverse of numbers
	public static void InverseofNumber(int p) {
		naturalNumInverse[0] = naturalNumInverse[1] = 1;

		for (int i = 2; i <= N; i++)
			naturalNumInverse[i] = naturalNumInverse[p % i] * (long) (p - p / i) % p;
	}

	// Function to precompute inverse of factorials
	public static void InverseofFactorial(int p) {
		factorialNumInverse[0] = factorialNumInverse[1] = 1;

		// Precompute inverse of natural numbers
		for (int i = 2; i <= N; i++)
			factorialNumInverse[i] = (naturalNumInverse[i] * factorialNumInverse[i - 1]) % p;
	}

	// Function to calculate factorial of 1 to N
	public static void factorial(int p) {
		fact[0] = 1;

		// Precompute factorials
		for (int i = 1; i <= N; i++) {
			fact[i] = (fact[i - 1] * (long) i) % p;
		}
	}

	// Function to return nCr % p in O(1) time
	public static long Binomial(int N, int R, int p) {

		// n C r = n!*inverse(r!)*inverse((n-r)!)
		long ans = ((fact[N] * factorialNumInverse[R]) % p * factorialNumInverse[N - R]) % p;

		return ans;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FastReader sc = new FastReader();
		int t;
		t = sc.nextInt();
		StringBuilder st = new StringBuilder();
//		int p = 998244353;
//	    InverseofNumber(p);
//	    InverseofFactorial(p);
//	    factorial(p);
		// System.out.println(3|1);
		while (t-- != 0) {
			int n=sc.nextInt();
			String s[]=new String[n];
			for(int i=0;i<n;i++) {
				s[i]=sc.nextLine();
			}
			int max=0;
			ArrayList<Integer> freq=new ArrayList<>();
			for(int i=0;i<5;i++) {
				int f1=0;
				freq=new ArrayList<>();
				for(int j=0;j<n;j++) {
					f1=0;
					for(int k=0;k<s[j].length();k++) {
						if(s[j].charAt(k)==i+'a') {
							f1++;
						}else {
							f1--;
						}
					}
					freq.add(f1);
				}
				Collections.sort(freq);
				int x=0;
				int total=0;
				for(int j=n-1;j>=0;j--) {
					total=total+freq.get(j);
					if(total>0) {
						x++;
					}else {
						break;
					}
				}
				max=Math.max(max, x);
			}
			st.append(max+"\n");
		}
		System.out.println(st);
	}

	static FastReader sc = new FastReader();

	public static void solvegraph() {
		int n = sc.nextInt();

		int edge[][] = new int[n - 1][2];
		for (int i = 0; i < n - 1; i++) {
			edge[i][0] = sc.nextInt() - 1;
			edge[i][1] = sc.nextInt() - 1;
		}
		ArrayList<ArrayList<Integer>> ad = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			ad.add(new ArrayList<Integer>());
		}
		for (int i = 0; i < n - 1; i++) {
			ad.get(edge[i][0]).add(edge[i][1]);
			ad.get(edge[i][1]).add(edge[i][0]);
		}
		int parent[] = new int[n];
		Arrays.fill(parent, -1);
		parent[0] = n;
		ArrayDeque<Integer> queue = new ArrayDeque<>();
		queue.add(0);
		int child[] = new int[n];
		Arrays.fill(child, 0);
		ArrayList<Integer> lv = new ArrayList<Integer>();
		while (!queue.isEmpty()) {
			int toget = queue.getFirst();
			queue.removeFirst();
			child[toget] = ad.get(toget).size() - 1;
			for (int i = 0; i < ad.get(toget).size(); i++) {
				if (parent[ad.get(toget).get(i)] == -1) {
					parent[ad.get(toget).get(i)] = toget;
					queue.addLast(ad.get(toget).get(i));
				}
			}
			lv.add(toget);
		}

		child[0]++;
	}

	static void sort(int[] A) {
		int n = A.length;
		Random rnd = new Random();
		for (int i = 0; i < n; ++i) {
			int tmp = A[i];
			int randomPos = i + rnd.nextInt(n - i);
			A[i] = A[randomPos];
			A[randomPos] = tmp;
		}
		Arrays.sort(A);
	}

	static void sort(long[] A) {
		int n = A.length;
		Random rnd = new Random();
		for (int i = 0; i < n; ++i) {
			long tmp = A[i];
			int randomPos = i + rnd.nextInt(n - i);
			A[i] = A[randomPos];
			A[randomPos] = tmp;
		}
		Arrays.sort(A);
	}

	static String sort(String s) {
		Character ch[] = new Character[s.length()];
		for (int i = 0; i < s.length(); i++) {
			ch[i] = s.charAt(i);
		}
		Arrays.sort(ch);
		StringBuffer st = new StringBuffer("");
		for (int i = 0; i < s.length(); i++) {
			st.append(ch[i]);
		}
		return st.toString();
	}

	public static long gcd(long a, long b) {
		if (a == 0) {
			return b;
		}
		return gcd(b % a, a);
	}

	static class FastReader {
		BufferedReader br;
		StringTokenizer st;

		public FastReader() {
			br = new BufferedReader(new InputStreamReader(System.in));
		}

		String next() {
			while (st == null || !st.hasMoreElements()) {
				try {
					st = new StringTokenizer(br.readLine());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return st.nextToken();
		}

		int nextInt() {
			return Integer.parseInt(next());
		}

		long nextLong() {
			return Long.parseLong(next());
		}

		double nextDouble() {
			return Double.parseDouble(next());
		}

		String nextLine() {
			String str = "";
			try {
				str = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return str;
		}
	}
}
