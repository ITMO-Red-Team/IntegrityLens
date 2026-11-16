//package Div_2B_Problems;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.ArrayList;
public class Armchairs {
	static ArrayList<Integer> one;
	static ArrayList<Integer> zero;
	public static void main(String args[]) {
		FastScanner fs=new FastScanner();
		int n=fs.nextInt();
		int []arr=new int[n];
		one=new ArrayList<>();
		zero=new ArrayList<>();
		for(int i=0;i<arr.length;i++) {
			arr[i]=fs.nextInt();
			if(arr[i]==1)
				one.add(i);
			else zero.add(i);
		}
		int [][]dp=new int[arr.length][arr.length];
		for(int i=0;i<arr.length;i++)
			Arrays.fill(dp[i], -1);
		System.out.println(dfs(dp,0,0));
	}
	public static int dfs(int [][]dp,int i,int j) {
		//System.out.println(i+"  "+j);
		if(i>=one.size())
			return 0;
		if(j>=zero.size())
			return (int)(1e9);
		if(dp[i][j]!=-1)
			return dp[i][j];
		dp[i][j]=Math.min(Math.abs(one.get(i)-zero.get(j))+dfs(dp,i+1,j+1), dfs(dp,i,j+1));
		return dp[i][j];
	}
	private static int[] readArray(int n) {
		// TODO Auto-generated method stub
		return null;
	}

static final Random random=new Random();
	
	static void ruffleSort(int[] a) {
		int n=a.length;//shuffle, then sort 
		for (int i=0; i<n; i++) {
			int oi=random.nextInt(n), temp=a[oi];
			a[oi]=a[i]; a[i]=temp;
		}
		Arrays.sort(a);
	}
	static class FastScanner {
		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st=new StringTokenizer("");
		String next() {
			while (!st.hasMoreTokens())
				try {
					st=new StringTokenizer(br.readLine());
				} catch (IOException e) {
					e.printStackTrace();
				}
			return st.nextToken();
		}
		
		int nextInt() {
			return Integer.parseInt(next());
		}
		int[] readArray(int n) {
			int[] a=new int[n];
			for (int i=0; i<n; i++) a[i]=nextInt();
			return a;
		}
		long nextLong() {
			return Long.parseLong(next());
		}
}
}
