
import java.util.*;


import java.io.*;
 public class Solution {
		static FastScanner scr=new FastScanner();
//		static Scanner scr=new Scanner(System.in);
		static PrintStream out=new PrintStream(System.out);
		static StringBuilder sb=new StringBuilder();
		
		
		
		
		static class pair{
			int x;int y;
			pair(int x,int y){ 
				this.x=x;this.y=y;
				
			}
		}
		static class triplet{
			int x;
			long y;
			int z;
			triplet(int x,long y,int z){
				this.x=x;
				this.y=y;
				this.z=z;
			}
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
			} long gcd(long a,long b){
				if(b==0) {
					return a;
				}
					
				return gcd(b,a%b);
			}
		 	int[]  sort(int a[]) {
				 
		        int multiplier = 1, len = a.length, max = Integer.MIN_VALUE;
		        int b[] = new int[len];
		        int bucket[];
		        for (int i = 0; i < len; i++) if (max < a[i]) max = a[i];
		        while (max / multiplier > 0) {
		            bucket = new int[10];
		            for (int i = 0; i < len; i++) bucket[(a[i] / multiplier) % 10]++;
		            for (int i = 1; i < 10; i++) bucket[i] += (bucket[i - 1]);
		            for (int i = len - 1; i >= 0; i--) b[--bucket[(a[i] / multiplier) % 10]] = a[i];
		            for (int i = 0; i < len; i++) a[i] = b[i];
		            multiplier *= 10;
		        }
		        return a;
		    }
		 	 long modPow(long base,long exp) {
				if(exp==0) {
					return 1;
				}
				if(exp%2==0) {
					long res=(modPow(base,exp/2));
					return (res*res);
				}
				return (base*modPow(base,exp-1));
			}
			int []reverse(int a[]){
				int b[]=new int[a.length];
				int index=0;
				for(int i=a.length-1;i>=0;i--) {
					b[index]=a[i];
				}
				return b;
			}
			int nextInt() {
				return Integer.parseInt(next());
			}
			int[] readArray(int n) {
				int[] a=new int[n];
				for (int i=0; i<n; i++) a[i]=nextInt();
				return a;
			}
			long[] readLongArray(int n) {
				long  [] a=new long  [n];
				for (int i=0; i<n; i++) a[i]=nextLong();
				return a;
			}
			long nextLong() {
				return Long.parseLong(next());
			}
		}
		
		static HashMap<Character,ArrayList<Integer>>hm=new HashMap<>();
		static void solve() {
			int n=scr.nextInt();
			
			int count[]=new int[5];
			int store[][]=new int[n][5];
			int max=MIN;
			int length[]=new int[n];
			for(int i=0;i<n;i++) {
				String s=scr.next();
				length[i]=s.length();
				for(int j=0;j<s.length();j++) {
					store[i][s.charAt(j)-'a']++;
					count[s.charAt(j)-'a']++;
				}
			}
			
			for(int j=0;j<5;j++) {
				
				ArrayList<Integer>a=new ArrayList<>();
				for(int i=0;i<n;i++) {
					a.add((store[i][j]*2)-length[i]);
				}
				
				Collections.sort(a,Collections.reverseOrder());
				
				long sum=0;
				int ans=0;
				for(int i=0;i<a.size();i++) {
					sum+=a.get(i);
					if(sum>0) {
						ans++;
					}else {
						break;
					}
				}
				max=Math.max(max, ans);
			}
			out.println(max);
			
		}
		static int  MAX = Integer.MAX_VALUE;
		static int  MIN = Integer.MIN_VALUE;
		public static void main(String []args) {
			int t=scr.nextInt();
			while(t-->0) {
				solve();
			}
//			solve();
//			out.println(sb);
		}
	}
 	