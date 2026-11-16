import java.math.BigInteger;
//import static java.lang.Math.max;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import java.util.Scanner;

public class ahh {
//trihund
	static Scanner scn = new Scanner(System.in);
	static boolean vis[][];

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

	static FastReader s = new FastReader();
	static int MOD = 1000000007;

	public static void main(String[] args) {
		
		int n=scn.nextInt(),count=0;
		int arr[]=new int[n];
		for(int i=0;i<n;i++)
		{
			arr[i]=scn.nextInt();
		}
		ArrayList<Integer>zer=new ArrayList<Integer>(),one=new ArrayList<Integer>();
		for(int i=0;i<n;i++)
		{
			if(arr[i]==0)
				zer.add(i);
			else
				one.add(i);
		}
		count=one.size();
		long memo[][]=new long[one.size()+1][zer.size()+1];
		for(int i=0;i<=one.size();i++)
		{
			for(int j=0;j<=zer.size();j++)
				memo[i][j]=-1;
		}
		System.out.println(arm(one, zer, 0, 0, count,memo));
				
		
	}

	public static long arm(ArrayList<Integer>one,ArrayList<Integer>zer,int i,int j,int count,long memo[][])
	{	if(count==0)
		return 0;
		if(i==one.size()||j==zer.size())
			return Integer.MAX_VALUE;
		
		if(memo[i][j]!=-1)
			return memo[i][j];
		
		long a=Integer.MAX_VALUE,b=Integer.MAX_VALUE;
		a=arm(one, zer, i+1, j+1,count-1,memo)+Math.abs(one.get(i)-zer.get(j));
		b=arm(one, zer, i, j+1,count,memo);
		memo[i][j]=Math.min(a, b);
		return Math.min(a, b);
	}
	public static void fac(int n) {
		BigInteger b = new BigInteger("1");
		for (int i = 1; i <= n; i++) {
			b = b.multiply(BigInteger.valueOf(i));
		}
		System.out.println(b);

	}

	static void ruffleSort(long[] a) {
		int n = a.length;
		Random r = new Random();
		for (int i = 0; i < a.length; i++) {
			int oi = r.nextInt(n);
			long temp = a[i];
			a[i] = a[oi];
			a[oi] = temp;
		}
		Arrays.sort(a);
	}

	public static int gcd(int a, int b) {
		if (a == 0)
			return b;
		return gcd(b % a, a);
	}

}

class pm {
	int ini, fin;

	pm(int a, int b) {
		this.ini = a;
		this.fin = b;
	}

}

class surt implements Comparator<pm> {

	@Override
	public int compare(pm o1, pm o2) {
		// TODO Auto-generated method stub
		int a = o1.ini - o2.ini, b = o1.fin - o2.fin;
		if (a < 0)
			return -1;
		if (a == 0) {
			if (b < 0)
				return -1;
			else
				return 1;
		} else
			return 1;
	}

}

class pair {
	int x, y;

	pair(int a, int b) {
		this.x = a;
		this.y = b;
	}

	public int hashCode() {
		return x * 31 + y * 31;
	}

	public boolean equals(Object other) {
		if (this == other)
			return true;

		if (other instanceof pair) {
			pair pt = (pair) other;

			return pt.x == this.x && pt.y == this.y;
		} else
			return false;
	}

}

class sort implements Comparator<pair> {

	@Override
	public int compare(pair o1, pair o2) {
		// TODO Auto-generated method stub
		long a = o1.x - o2.x, b = o1.y - o2.y;
		if (b < 0)
			return -1;
		else if (a == 0) {
			if (a < 0)
				return -1;
			else
				return 1;
		} else
			return 1;

	}

}