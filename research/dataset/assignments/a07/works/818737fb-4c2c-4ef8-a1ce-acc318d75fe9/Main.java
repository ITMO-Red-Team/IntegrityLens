// package codeforces;

import java.util.*;

public class ArmChairs {
	static int[]arr;
	static ArrayList<Integer>a;
	static ArrayList<Integer>b;
	static int dp[][];
	public static void main(String[] args) {
		Scanner scn = new Scanner(System.in);
		int n = scn.nextInt();
		arr=new int[n];
		for(int i=0;i<n;i++) {
			arr[i]=scn.nextInt();
		}
		dp=new int[n+1][n+1];
		a =new ArrayList<>();
		b =new ArrayList<>();
		for(int i=0;i<n;i++) {
			if(arr[i]==0) {
				a.add(i);
			}else{
				b.add(i);
			}
		}
		System.out.println(solve(0,0));	
	}
	
	public static int solve(int i,int j) {
		if(i==b.size()) {
			return 0;
		}
		if(j==a.size()) {
			return 100000000;
		}

		if(dp[i][j]!=0) {
			return dp[i][j];
		}
		int x=Math.abs(a.get(j)-b.get(i))+solve(i+1,j+1);
		int y=solve(i,j+1);
		return dp[i][j]=Math.min(x, y);
	}
	

}
