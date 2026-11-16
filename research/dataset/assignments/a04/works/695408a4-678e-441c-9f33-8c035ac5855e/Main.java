import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

public class C {
	
	public static void main(String[] args) {
		FastScanner fs=new FastScanner();
		int T=fs.nextInt();
		for (int tt=1; tt<=T; tt++) {
			int n=fs.nextInt();
			int m=fs.nextInt();
			int x=fs.nextInt();
			int arr[]=fs.readArray(n);
			if(m>n) {
				System.out.println("NO");
				continue;
			}
			int copy[]=new int [n];
			for(int i=0;i<n;i++) {
				copy[i]=arr[i];
			}
			Arrays.sort(copy);
			ArrayList<Integer> container[]= new ArrayList[x+1];
			for(int i=1;i<=x;i++) {
				container[i]=new ArrayList<Integer>();
			}
			for(int i=0;i<m;i++) {
				for(int j=i;j<n;j+=m) {
					container[copy[j]].add(i+1);
				}
			}
			int counter[]=new int [n];
			for(int i=0;i<n;i++) {
				counter[i]=container[arr[i]].get(0);
				container[arr[i]].remove(0);
			}
			System.out.println("YES");
			for(int i=0;i<n;i++) {
				System.out.print(counter[i]+" ");
			}
			System.out.println();
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
		String nextLine() {
			String str = "";
			try {
				str = br.readLine();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			return str;
		}
		
		int nextInt() {
			return Integer.parseInt(next());
		}
		
		int[] readArray(int n) {
			int[] a=new int[n];
			for (int i=0; i<n; i++) a[i]=nextInt();
			return a;
		}
		
		int[][] readArray(int n,int m) {
			int[][] a=new int[n][m];
			for (int i=0; i<n; i++) for(int j=0; j<m; j++) a[i][j]=nextInt();
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