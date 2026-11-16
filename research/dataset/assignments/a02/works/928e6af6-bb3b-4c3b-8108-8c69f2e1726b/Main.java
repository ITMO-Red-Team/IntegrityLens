import java.io.*;
import java.util.*;

public class B {
	
	public static void main(String[] args)throws IOException {
		
		FastScanner scan = new FastScanner();
		PrintWriter output = new PrintWriter(System.out);
		int t = scan.nextInt();
		for(int tt = 0;tt<t;tt++) {
			int n = scan.nextInt();
			char initial[] = scan.next().toCharArray();
			char desired[] = scan.next().toCharArray();
			int lit1 = 0, lit2 = 0;
			int ans = Integer.MAX_VALUE;
			for(int i = 0;i<n;i++) {
				if(initial[i]=='1') lit1++;
				if(desired[i]=='1') lit2++;
			}
			if(lit1==lit2) {
				int count = 0;
				for(int i = 0;i<n;i++) if(initial[i]!=desired[i]) count++;
				ans = Math.min(ans, count);
			}
			if(lit2==(n-lit1+1)) {
				int count = 0;
				for(int i = 0;i<n;i++) if(initial[i]==desired[i]) count++;
				ans = Math.min(ans, count);
			}
			if(ans == Integer.MAX_VALUE) ans = -1;
			output.println(ans);	
		}
		output.flush();
	}
	
	public static int[] sort(int arr[]) {
		List<Integer> list = new ArrayList<>();
		for(int i:arr) list.add(i);
		Collections.sort(list);
		for(int i = 0;i<list.size();i++) arr[i] = list.get(i);
		return arr;
	}
	
	public static int gcd(int a, int b) {
		if(a == 0) return b;
		return gcd(b%a, a);
	}
	
	public static void printArray(int arr[]) {
		for(int i:arr) System.out.print(i+" ");
		System.out.println();
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
		
		double nextDouble() {
			return Double.parseDouble(next());
		}
	}

}
