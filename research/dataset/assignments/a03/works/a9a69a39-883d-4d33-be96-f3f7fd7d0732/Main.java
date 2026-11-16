import java.io.*;
import java.util.*;

/*

*/


public class E {
	static FastReader sc=null;
	static long nax=(long)1e15+7;
	
	public static void main(String[] args) {
		sc=new FastReader();
		PrintWriter out=new PrintWriter(System.out);
		int test=sc.nextInt();
		while(test-->0) {
			int n=sc.nextInt(),k=sc.nextInt();
			int a[]=sc.readArray(k),t[]=sc.readArray(k);
			int present[]=new int[n];
			Arrays.fill(present, -1);
			for(int i=0;i<k;i++) {
				a[i]--;
				present[a[i]]=t[i];
			}
			long min=nax;
			long temps[]=new long[n];
			Arrays.fill(temps, nax);
			for(int i=n-1;i>=0;i--) {
				if(present[i]!=-1) {
					min=Math.min(min, present[i]+i);
				}
				temps[i]=Math.min(temps[i], min-i);
			}
			min=nax;
			for(int i=0;i<n;i++) {
				if(present[i]!=-1) {
					min=Math.min(min, present[i]-i);
				}
				temps[i]=Math.min(temps[i], min+i);
			}
			for(long e:temps)out.print(e+" ");
			out.println();
			
		}
		out.close();
		
	
	}
	static class Pair implements Comparable<Pair>{
		int id,t;
		Pair(int id,int t){
			this.id=id;
			this.t=t;
		}
		@Override
		public int compareTo(Pair o) {
			return this.id-o.id;
		}
	}
	static int[] ruffleSort(int a[]) {
		ArrayList<Integer> al=new ArrayList<>();
		for(int i:a)al.add(i);
		Collections.sort(al);
		for(int i=0;i<a.length;i++)a[i]=al.get(i);
		return a;
	}
	static void print(int a[]) {
		for(int e:a) {
			System.out.print(e+" ");
		}
		System.out.println();
	}
	
	static class FastReader{
		
		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st=new StringTokenizer("");
		
		String next() {
			while(!st.hasMoreTokens())
				try {
					st=new StringTokenizer(br.readLine());
				}
				catch(IOException e){
					e.printStackTrace();
				}
			return st.nextToken();
		}
		
		int nextInt() {
			return Integer.parseInt(next());
		}
		
		long nextLong() {
			return Long.parseLong(next());
		}
		
		int[] readArray(int n) {
			int a[]=new int[n];
			for(int i=0;i<n;i++)a[i]=sc.nextInt();
			return a;
		}
	}
}






