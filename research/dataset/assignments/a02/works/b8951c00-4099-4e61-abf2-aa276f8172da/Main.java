import java.util.*;
import java.io.*;
public class C1615{
	static FastScanner fs = null;
	public static void main(String[] args) {
		fs = new FastScanner();
		PrintWriter out = new PrintWriter(System.out);
		int t = fs.nextInt();
		while (t-->0) {
			int n = fs.nextInt();
			String a = fs.next();
			String b = fs.next();
			char ch1[] = a.toCharArray();
			char ch2[] = b.toCharArray();
			int c00 = 0;
			int c01 = 0;
			int c10 = 0;
			int c11 = 0;
			for(int i=0;i<n;i++){
				if(ch1[i]=='0'){
					if(ch2[i]=='0'){
						c00+=1;
					}
					else{
						c01+=1;
					}
				}
				else{
					if(ch2[i]=='0'){
						c10+=1;
					}
					else{
						c11+=1;
					}
				}
			}
			int ans = -1;
			if((c11-c00)==1 || c10==c01){
				int s1 = (int)1e7; 
				int s2 = (int)1e7;
				if((c11-c00)==1){
					s1 = c11+c00;
				}
				if(c10==c01)
				s2 = c10+c01;
				ans = Math.min(s1,s2); 
			}
			out.println(ans);
		}
		out.close();
	}
	static void sort(int[] a) {
		ArrayList<Integer> l=new ArrayList<>();
		for (int i:a) l.add(i);
		Collections.sort(l);
		for (int i=0; i<a.length; i++) a[i]=l.get(i);
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