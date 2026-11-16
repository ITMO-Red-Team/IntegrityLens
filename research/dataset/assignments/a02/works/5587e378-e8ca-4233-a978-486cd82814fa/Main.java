import java.io.*;

import java.util.*;
/*




*/

 
 public class A{
	static FastReader sc=null;
	
	public static void main(String[] args) {
		sc=new FastReader();
		int t=sc.nextInt();
		
		for(int tt=0;tt<t;tt++) {
			int n=sc.nextInt();
			char a[]=sc.next().toCharArray(),b[]=sc.next().toCharArray();
			
			int fa=0,fb=0,da=0,db=0,sum=0;
			boolean dif=false;
			for(int i=0;i<n;i++) {
				sum+=a[i]-'0';
				if(a[i]!=b[i]) {
					dif=true;
					if(a[i]=='1')fa++;
					else fb++;
				}
				else {
					if(a[i]=='1')da++;
					else db++;
				}
			}
			
			if(sum==0) {
				System.out.println(dif?-1:0);
				continue;
			}
			
			int ans=n+1;
			
			if(fa==fb) ans=(fa+fb);
			if(da==db+1) ans=Math.min(da+db, ans);
			
			System.out.println(ans==(n+1)?-1:ans);
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
		
		StringTokenizer st=new StringTokenizer("");
		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
		
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
