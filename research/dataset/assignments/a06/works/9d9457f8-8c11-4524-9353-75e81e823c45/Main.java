import java.io.*;
import java.util.*;

public class E {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
			FastScanner sc = new FastScanner();
			
			int t = sc.nextInt();
			
			while(t-- > 0) {
				
				int n = sc.nextInt();
				int[] a = new int[n];
				
				for(int i = 0 ; i< n ; i++) {
					a[i] = sc.nextInt();
				}
				long oddMin = a[1];
				long evenMin = a[0];
				long res = (n * oddMin) + (n * evenMin);
				long oddSum = a[1];
				long evenSum = a[0];
				
				for(int i = 2 ;i < n ; i++) {
					
					if(i % 2 == 1) {
						oddSum += a[i];
						oddMin = Math.min(oddMin, a[i]);
					}
					else {
						evenSum += a[i];
						evenMin = Math.min(evenMin, a[i]);
					}
					
					int odd = (i + 1) / 2;
					int even = (i / 2) + 1;
					
					long minCostOdd = oddSum + oddMin*(n - odd);
					long minCostEven = evenSum + evenMin*(n - even);
					
					res = Math.min(res, minCostOdd + minCostEven);
				}
				
				System.out.println(res);
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
	
	// Use this instead of Arrays.sort() on an array of ints. Arrays.sort() is n^2
		// worst case since it uses a version of quicksort. Although this would never
		// actually show up in the real world, in codeforces, people can hack, so
		// this is needed.
		static void ruffleSort(int[] a) {
			//ruffle
			int n=a.length;
			Random r=new Random();
			for (int i=0; i<a.length; i++) {
				int oi=r.nextInt(n), temp=a[i];
				a[i]=a[oi];
				a[oi]=temp;
			}
			
			//then sort
			Arrays.sort(a);
		}

}
