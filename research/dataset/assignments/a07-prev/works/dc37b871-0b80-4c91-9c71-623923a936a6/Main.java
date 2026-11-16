/*
    JAI MATA DI
 */

import java.util.*;

import javax.print.attribute.HashAttributeSet;


import java.io.*;
import java.math.BigInteger;
import java.sql.Array;
public class CP {
	  static class FR{
			BufferedReader br;
			StringTokenizer st;
			public FR() {
				br = new BufferedReader(new InputStreamReader(System.in));
			}
			String next() 
		    { 
		        while (st == null || !st.hasMoreElements()) 
		        { 
		            try
		            { 
		                st = new StringTokenizer(br.readLine()); 
		            } 
		            catch (IOException  e) 
		            { 
		                e.printStackTrace(); 
		            } 
		        } 
		        return st.nextToken(); 
		    } 

		    int nextInt() 
		    { 
		        return Integer.parseInt(next()); 
		    } 

		    long nextLong() 
		    { 
		        return Long.parseLong(next()); 
		    } 

		    double nextDouble() 
		    { 
		        return Double.parseDouble(next()); 
		    } 

		    String nextLine() 
		    { 
		        String str = ""; 
		        try
		        { 
		            str = br.readLine(); 
		        } 
		        catch (IOException e) 
		        { 
		            e.printStackTrace(); 
		        } 
		        return str; 
		    } 
		}
	 static int mod  = 1000000007;
	 static int gcd(int a, int b)
	  {      
	     if (b == 0)
	        return a;
	     return gcd(b, a % b); 
	  }
	 static long gcd(long a, long b)
	  {      
	     if (b == 0)
	        return a;
	     return gcd(b, a % b); 
	  }
	 static boolean[] prime(int num) {
			boolean[] bool = new boolean[num];
		     
		      for (int i = 0; i< bool.length; i++) {
		         bool[i] = true;
		      }
		      for (int i = 2; i< Math.sqrt(num); i++) {
		         if(bool[i] == true) {
		            for(int j = (i*i); j<num; j = j+i) {
		               bool[j] = false;
		            }
		         }
		      }
		      if(num >= 0) {
		    	  bool[0] = false;
		    	  bool[1] = false;
		      }
		      
		      return bool;
		}
	 static class Pair implements Comparable<Pair>{
	 		int ind;
	 		int val;
	 		Pair(int key , int value){
	 			this.ind = key;
	 			this.val = value;
	 		}
			@Override
			public int compareTo(Pair o) {
				return this.val - o.val;
			}
//			@Override
//			public int hashCode(){
//				return first + second;
//			}
	 	}
	 	
	 
	 

/* ***************************************************************************************************************************************************/	 
	 static FR sc = new FR();
	 static StringBuilder sb = new StringBuilder();
	 public static void main(String args[]) {
//		 int tc = sc.nextInt();
//		 while(tc-- > 0) {
			 TEST_CASE();
//		 }
			System.out.println(sb);
		 }		

		static void TEST_CASE() {
			int n = sc.nextInt();
			int[] arr = new int[n];
			for(int i = 0 ; i< n ;i++) {
				arr[i] = sc.nextInt();
			}
			
			ArrayList<Integer> ao = new ArrayList<Integer>();
			ArrayList<Integer> az = new ArrayList<Integer>();
			for(int i = 0 ; i< n ;i++) {
				if(arr[i] == 1) ao.add(i);
				else az.add(i);
			}
			long[][] dp = new long[n+1][n+1];
			for(int i = 0 ; i<n ; i++) Arrays.fill(dp[i], -1);
			
			sb.append(fnc(dp, ao, az, 0, 0));
			
		}
		static long fnc(long[][] dp ,ArrayList<Integer> ao , ArrayList<Integer> az ,int i , int j) {
			if(i == ao.size()) return 0;
			if(j == az.size()) return Long.parseLong("1000000000000");
			if(dp[i][j] != -1) return dp[i][j];
			long a = Math.abs(ao.get(i) - az.get(j)) + fnc(dp, ao, az, i+1, j+1);
			long b = fnc(dp, ao, az, i, j+1);
			dp[i][j] = Math.min(a, b);
			return dp[i][j];
			
		}
		
}