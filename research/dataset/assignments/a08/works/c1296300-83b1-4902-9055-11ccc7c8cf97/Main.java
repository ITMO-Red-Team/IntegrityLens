import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.io.*;
import java.util.*;
 
public class A {
	static class FastReader {
		BufferedReader br;
		StringTokenizer st;
 
		public FastReader() {
			br = new BufferedReader(new InputStreamReader(System.in));
		}
 
		String next() {
			while (st == null || !st.hasMoreElements()) {
				try {
					st = new StringTokenizer(br.readLine());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return st.nextToken();
		}
 
		int nextInt() {
			return Integer.parseInt(next());
		}
 
		long nextLong() {
			return Long.parseLong(next());
		}
 
		double nextDouble() {
			return Double.parseDouble(next());
		}
 
		String nextLine() {
			String str = "";
			try {
				str = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return str;
		}
	}
 
 
	public static void main(String args[]) {
		if (System.getProperty("ONLINE_JUDGE") == null) {
  			// Input is a file
  			try {
				System.setIn(new FileInputStream("input.txt"));
				System.setOut(new PrintStream(new FileOutputStream("output.txt")));
			} catch (Exception e) {
				System.err.println("Error");
			}
		} else {
  			// Input is System.in
		}

		FastReader sc = new FastReader();
		// Scanner sc = new Scanner(System.in);
		//System.out.println(java.time.LocalTime.now());

		StringBuilder sb = new StringBuilder();
		int t = sc.nextInt();
		while(t>0) {
			
			int n = sc.nextInt(); 
			long[] arr = new long[n];
			long[] pow = new long[n];
			for(int i = 0; i<n; i++)arr[i] = sc.nextLong();
			for(int i = 0; i<n ;i++)pow[i] = sc.nextLong();

			Pair[] p= new Pair[n];

			for(int i = 0; i<n; i++){
				Pair pp = new Pair(arr[i]-pow[i], arr[i]);
				p[i] = pp;
			}
			long ans = 0;
			Arrays.sort(p);
			//System.out.println(Arrays.toString(p));
			long prevEnd = -1;
			long prevN = 0;
			for(int i = 0; i<n; i++){

				if(p[i].x >= prevEnd){
					long nn = p[i].y-p[i].x;
			
					ans+= ((nn)*(nn+1))/2;
					prevEnd = p[i].y;
					prevN = nn;
				}else{

					if(prevEnd < p[i].y){
						long a = prevN+1;
						long nn = p[i].y-prevEnd;
						long sum = ((nn)*(2*a+(nn-1)))/2;
						ans +=sum;
						prevN = a+(nn-1);
						prevEnd = p[i].y;
					}

				}
				
			}




			sb.append(ans+"\n");

			
			
			t--;
		}
		
		System.out.println(sb);
		
 
		
    }

}

class Pair implements Comparable<Pair>{
  	long x;
  	long y;

  	Pair(long x, long y){
  		this.x = x;
  		this.y = y;
  	}

  	 // @Override
    //  public boolean equals(Object o) {
    //  	if(this == o)return true;
    //  	if(o == null || this.getClass() != o.getClass())return false;
    //  	Pair p = (Pair)o;
        
    //     return x == p.x && y == p.y;
    // }

    // @Override
    // public int hashCode(){
    // 	return Objects.hash(x , y);
    // }
  	@Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
    @Override
    public int compareTo(Pair o) {
    	return (int)this.x-(int)o.x;
    }
}