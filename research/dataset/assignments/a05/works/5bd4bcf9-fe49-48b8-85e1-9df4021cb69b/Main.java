import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
public class solution { 
    public static void main(String args[]) throws java.lang.Exception{ 
    	FastScanner s=new FastScanner();
		PrintWriter out=new PrintWriter(System.out);
		int t=s.nextInt();
		for(int tt=0;tt<t;tt++) {
			int n=s.nextInt();
			int c[][]=new int[n][5];
			int sum[]=new int[5];
			for(int i=0;i<n;i++) {
				String a=s.next();
				for(int j=0;j<a.length();j++) {
					c[i][a.charAt(j)-'a']++;
				}
				for(int j=0;j<5;j++) {
					sum[j]+=c[i][j];
				}
			}
			int sum1=0;
			for(int i=0;i<5;i++) {
				sum1+=sum[i];
			}
			int ans=0;
			
			for(int i=0;i<5;i++) {
				int sum2=sum1-sum[i];
				//out.println(sum2);
				if(sum[i]>sum2) {
					ans=n;
					break;
				}else {
					
					ArrayList<Integer> f=new ArrayList<>();
					for(int j=0;j<n;j++) {
						int sum3=0;
						for(int k=0;k<5;k++) {
							if(k!=i) {
								sum3+=c[j][k];
							}else {
								sum3-=c[j][k];
							}
						}
						f.add(sum3);
					}
					Collections.sort(f);
					while(sum[i]<=sum2 && f.size()>0 && f.get(f.size()-1)>0) {
						sum2-=f.get(f.size()-1);
						f.remove(f.size()-1);
						//out.println(sum2);
					}
					if(sum[i]>sum2) {
						ans=Math.max(ans,f.size());
					}
					//out.println(ans+" Ayush");
				}
			}
			out.println(ans);
		}
		out.close();
    }
    public static int modulus(int a,int b) {
    	int ans=a%b;
    	if(ans<0) {
    		ans=b+ans;
    	}
    	return ans;
    }
    static void sort(long [] a) {
		ArrayList<Long> l=new ArrayList<>();
		for (long i:a) l.add(i);
		Collections.sort(l);
		for (int i=0; i<a.length; i++) a[i]=l.get(i);
	}
    static void sort(int [] a) {
		ArrayList<Integer> l=new ArrayList<>();
		for (int i:a) l.add(i);
		Collections.sort(l);
		for (int i=0; i<a.length; i++) a[i]=l.get(i);
	}
    static int gcd(int a, int b){
      if (b == 0)
        return a;
      return gcd(b, a % b);
    }
    static void sortcol(int a[][],int c) {
		Arrays.sort(a, (x, y) -> {
			if(x[c]>y[c]) {
				return 1;
			}else {
				return -1;
			}
		});
	}
    public static void printb(boolean ans) {
    	if(ans) {
    		System.out.println("Yes");
    	}else {
    		System.out.println("No");
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
		double nextDouble() {
			return Double.parseDouble(next());
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
    static class Pair implements Comparable<Pair>{
    	int a , b; 
    	Pair(int x , int y){
    		a=x;
    		b=y;
    	}
    	public int compareTo(Pair o) {
    		return a != o.a ? a - o.a : b - o.b;
    	}
    }
}
