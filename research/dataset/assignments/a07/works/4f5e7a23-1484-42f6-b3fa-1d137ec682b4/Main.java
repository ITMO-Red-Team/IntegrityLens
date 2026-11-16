/* package codechef; // don't place package name! */

import java.util.*;
import java.lang.*;
import java.io.*;

/* Name of the class has to be "Main" only if the class is public. */
public class Codechef {
	static long dp[][];
	public static void main(String[] args) throws java.lang.Exception {
		FastReader in = new FastReader(System.in);
		StringBuilder sb = new StringBuilder();
		int t = 1;
		//t = in.nextInt();
		while (t > 0) {
			--t;
			int n = in.nextInt();
			int arr[] = new int[n];
			List<Integer> ones = new ArrayList<Integer>();
			List<Integer> zero = new ArrayList<>();
			for(int i = 0;i<n;i++)
			{
				int a = in.nextInt();
				if(a == 1)
					ones.add(i);
				else 
					zero.add(i);
			}
			if(ones.size() == 0) {
				sb.append(0+"\n");
				continue;
			}
			dp = new long[ones.size()][zero.size()];
			for(int i = 0;i<ones.size();i++)
			    Arrays.fill(dp[i], -1);
			sb.append(findans(ones, zero, ones.size()-1, zero.size()-1));
           
		}
		System.out.print(sb);
	}
	
	static long findans(List<Integer> ones,List<Integer> zero,int x,int y)
	{
		if(x < 0)
			return 0;
		if(y<0)
			return Integer.MAX_VALUE;
        if(dp[x][y]!=-1)
        	return dp[x][y];
		return  dp[x][y] = Math.min(findans(ones, zero, x, y-1),findans(ones, zero, x-1, y-1) + (long)Math.abs(ones.get(x)-zero.get(y)));
	}

	static long gcd(long a, long b) {
		if (a == 0)
			return b;
		else
			return gcd(b % a, a);
	}

	static long lcm(long a, long b) {
		return (a / gcd(a, b)) * b;
	}

	static void sort(long[] a) {
		ArrayList<Long> l = new ArrayList<>();
		for (long i : a)
			l.add(i);
		Collections.sort(l);
		for (int i = 0; i < a.length; i++)
			a[i] = l.get(i);
	}
}

class FastReader {

	byte[] buf = new byte[2048];
	int index, total;
	InputStream in;

	FastReader(InputStream is) {
		in = is;
	}

	int scan() throws IOException {
		if (index >= total) {
			index = 0;
			total = in.read(buf);
			if (total <= 0) {
				return -1;
			}
		}
		return buf[index++];
	}

	String next() throws IOException {
		int c;
		for (c = scan(); c <= 32; c = scan())
			;
		StringBuilder sb = new StringBuilder();
		for (; c > 32; c = scan()) {
			sb.append((char) c);
		}
		return sb.toString();
	}

	String nextLine() throws IOException {
		int c;
		for (c = scan(); c <= 32; c = scan())
			;
		StringBuilder sb = new StringBuilder();
		for (; c != 10 && c != 13; c = scan()) {
			sb.append((char) c);
		}
		return sb.toString();
	}

	char nextChar() throws IOException {
		int c;
		for (c = scan(); c <= 32; c = scan())
			;
		return (char) c;
	}

	int nextInt() throws IOException {
		int c, val = 0;
		for (c = scan(); c <= 32; c = scan())
			;
		boolean neg = c == '-';
		if (c == '-' || c == '+') {
			c = scan();
		}
		for (; c >= '0' && c <= '9'; c = scan()) {
			val = (val << 3) + (val << 1) + (c & 15);
		}
		return neg ? -val : val;
	}

	long nextLong() throws IOException {
		int c;
		long val = 0;
		for (c = scan(); c <= 32; c = scan())
			;
		boolean neg = c == '-';
		if (c == '-' || c == '+') {
			c = scan();
		}
		for (; c >= '0' && c <= '9'; c = scan()) {
			val = (val << 3) + (val << 1) + (c & 15);
		}
		return neg ? -val : val;
	}
}