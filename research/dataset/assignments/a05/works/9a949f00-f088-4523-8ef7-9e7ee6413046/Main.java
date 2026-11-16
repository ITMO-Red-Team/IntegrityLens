import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.Arrays;
import java.util.Comparator;

import java.util.PriorityQueue;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Stack;
import java.util.Queue;



import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static java.lang.Math.abs;
import static java.lang.Math.random;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import static java.util.Collections.reverseOrder;

public final class Test1 {
	static int mod = 1000000007;
	static int mod1 = 998244353;
	public static void main(String[] args) {
		solve();
	}
	static long gcd(int a, int b)              {if (b == 0) return a;	return gcd(b, a % b); }
	static long modAdd(long a, long b, long m) {a = a % m; b = b % m; return (((a + b) % m) + m) % m;}
	static long modMul(long a, long b, long m) {a = a % m; b = b % m; return (((a * b) % m) + m) % m;}
	static long modSub(long a, long b, long m) {a = a % m; b = b % m; return (((a - b) % m) + m) % m;}
	static long modDiv(long a, long b, long m) {a = a % m; b = b % m; return (modMul(a, mminvprime(b, m), m) + m) % m;}  //only for prime m
	static long mminvprime(long a, long b)     {return expo(a, b - 2, b);}
	static long expo(long a, long b, long mod) {long res = 1; while (b > 0) {if ((b & 1) != 0)res = (res * a) % mod; a = (a * a) % mod; b = b >> 1;} return res;}
	static long setBits(int n)                 {int cnt = 0;  while (n != 0)   { cnt++; n = n & (n - 1);  }   return cnt;}
	static int[] sieve(int n)                  {int[] arr = new int[n + 1];  for (int i = 2; i <= n; i++)if (arr[i] == 0) {for (int j = 2 * i; j <= n; j += i)arr[j] = 1;} return arr;}
	static void debug(int[] nums)              { for (int i = 0; i < nums.length; i++)	System.out.println(nums[i] + " "); }
	static void debug(long[] nums)             { for (int i = 0; i < nums.length; i++)	System.out.println(nums[i] + " "); }
	static void debug(String[] nums)           { for (int i = 0; i < nums.length; i++)	System.out.println(nums[i] + " "); }
	static void reverse(int[] nums)            { int start = 0, end = nums.length - 1;      while (start < end) {int temp = nums[start]; nums[start] = nums[end]; nums[end] = temp; start++; end--; }  }
	static void reverse(char[] nums)            { int start = 0, end = nums.length - 1;      while (start < end) {char temp = nums[start]; nums[start] = nums[end]; nums[end] = temp; start++; end--; }  }

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
			for (int i = 0; i < n; i++) a[i] = nextInt();
			return a;
		}
		long nextLong() {
			return Long.parseLong(next());
		}
	}

	static void sort(int[] a) {
		ArrayList<Integer> l = new ArrayList<>();
		for (int i : a) l.add(i);
		Collections.sort(l);
		for (int i = 0; i < a.length; i++) a[i] = l.get(i);
	}





//	<------------------------- CODE START FROM HERE -------------------------------->


	public static void solve() {
		FastScanner sc = new FastScanner();
		int t = sc.nextInt();
		while (t-- != 0) {
			int n = sc.nextInt();
			String[] arr = new String[n];
			for (int i = 0; i < n; i++)
				arr[i] = sc.next();

			int cnt[] = new int[5];
			for (int i = 0; i < n; i++) {

				for (char ch : arr[i].toCharArray()) {
					cnt[ch - 'a']++;
				}
			}

			int res = 0;
			for (int i = 0; i < 5; i++) {

				res = Math.max(res, fun(arr, cnt, (char)( 'a' + i)));
			}
			System.out.println(res);

		}
	}
	static int fun(String[] arr, int[] cnt, char c) {
		int n = arr.length;

		int total = cnt[0] + cnt[1] + cnt[2] + cnt[3] + cnt[4] - cnt[c - 'a'];
		int letter_total = cnt[c - 'a'];


		PriorityQueue<Pair>pq = new PriorityQueue<>();

		for (int i = 0; i < n; i++) {

			int letter = 0;
			int ext = 0;
			for (char ch : arr[i].toCharArray()) {

				if (ch == c)
					letter++;
				else
					ext++;
			}
			pq.offer(new Pair(letter, ext));
		}



		while (pq.size() > 0) {

			if (total < letter_total)
				return pq.size();


			Pair temp = pq.poll();

			total -= temp.ext;
			letter_total -= temp.letter;
		}


		return 0;
	}

	static class Pair implements Comparable<Pair> {
		int letter, ext;
		Pair(int letter, int ext) {
			this.letter = letter;
			this.ext = ext;
		}
		public int compareTo(Pair p) {
			return (p.ext - p.letter) - (this.ext - this.letter);
		}
	}











// <----------------------------------CODE END HERE------------------------>

// System.out.println();
// int b[] = a.clone();


}