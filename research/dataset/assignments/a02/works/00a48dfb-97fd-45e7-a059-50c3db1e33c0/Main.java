import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class c {
	public static void main(String[] args) {
		FastScanner scan=new FastScanner();
		PrintWriter out=new PrintWriter(System.out);
		
		int t=scan.nextInt();
		for(int tt=0;tt<t;tt++) {
			int n=scan.nextInt();
			char[] a=scan.next().toCharArray(), b=scan.next().toCharArray();
			int o=0, e=0;
			int onea=0, oneb=0, zeroa=0, zerob=0;
			int evenon=0, oddon=0;
			for(int i=0;i<n;i++) {
				if(a[i]==b[i]) {
					e++;
					if(a[i]=='1') {
						evenon++;
					}
				}
				else {
					o++;
					if(a[i]=='1') oddon++;
				}
				if(a[i]=='1') onea++;
				else zeroa++;
				if(b[i]=='1') oneb++;
				else zerob++;
			}
			boolean good=false;
			int otheronea=onea==0?0:n-onea+1;
			if(oneb>=otheronea&&oneb<=onea) good=true;
			if(oneb>=onea&&oneb<=otheronea) good=true;
			
			if(!good) {
				out.println(-1);
				continue;
			}
			if(e==n) out.println(0);
			else if(e%2==1&&o%2==0&&evenon==(e+1)/2&&oddon==o/2) {
				out.println(Math.min(o,e));
			}
			else if(e%2==1&&evenon==(e+1)/2) {
				out.println(e);
			}
			else if(o%2==0&&oddon==o/2) {
				out.println(o);
			}
			else {
				out.println(-1);
			}
		}
		out.close();
	}
	static class FastScanner {
		BufferedReader br;
		StringTokenizer st;

		public FastScanner() {
			try	{
				br = new BufferedReader(new InputStreamReader(System.in));
				st = new StringTokenizer(br.readLine());
			} catch (Exception e){e.printStackTrace();}
		}

		public String next() {
			if (st.hasMoreTokens())	return st.nextToken();
			try {st = new StringTokenizer(br.readLine());}
			catch (Exception e) {e.printStackTrace();}
			return st.nextToken();
		}

		public int nextInt() {return Integer.parseInt(next());}

		public long nextLong() {return Long.parseLong(next());}

		public double nextDouble() {return Double.parseDouble(next());}

		public String nextLine() {
			String line = "";
			if(st.hasMoreTokens()) line = st.nextToken();
			else try {return br.readLine();}catch(IOException e){e.printStackTrace();}
			while(st.hasMoreTokens()) line += " "+st.nextToken();
			return line;
		}
	}
}
