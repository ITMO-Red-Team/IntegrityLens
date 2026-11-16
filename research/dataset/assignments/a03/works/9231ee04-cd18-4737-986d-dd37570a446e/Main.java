import java.util.*;
import java.io.*;
public class E1547{
	static int inf = 2*(int)Math.pow(10,9);
	static FastScanner fs = null;
	public static void main(String[] args) {
		fs = new FastScanner();
		int t = fs.nextInt();
		while(t-->0){
			int n = fs.nextInt();
			int k = fs.nextInt();
			int pos[] = new int[k];
			for(int i=0;i<k;i++){
				pos[i] = fs.nextInt();
			}	
			int time[] = new int[n];
			Arrays.fill(time,inf);
			for(int i=0;i<k;i++){
				time[pos[i]-1] = fs.nextInt();
			}
			for(int i=0;i<n;i++){
				if(i==0){
					continue;
				}
				else{
					time[i] = Math.min(time[i],time[i-1]+1);
				}
			}
			for(int i=n-2;i>=0;i--){
				time[i] = Math.min(time[i],1+time[i+1]);
			}
			for(int i=0;i<n;i++){
				System.out.print(time[i]+" ");
			}
			System.out.println();			
		}
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
