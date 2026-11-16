import java.io.*;
import java.util.*;
import static java.lang.Math.*;

public class C{
	
	static class Event implements Comparable<Event>{
		long i;
		int index;
		boolean start; 
		public Event(long a,int c, boolean b) {
			i=a;
			index = c;
			start=b;
		}
		public int compareTo(Event o){
			if (o.i != i) return (int)(i-o.i);
			if (start == o.start) return 0;
			return start ? -1:1;
		}
	}
	
	public static void main(String[] args) throws IOException{
		// br = new BufferedReader(new FileReader(".in"));
		// out = new PrintWriter(new FileWriter(".out"));
		// new Thread(null, new (), "fisa balls", 1<<28).start();
		
		int t= readInt();
		while(t-->0) {
			int n =readInt();
			long[] k = new long[n], h = new long[n];
			for (int i = 0; i < n; i++) k[i]=readInt();
			for (int i = 0; i < n; i++) h[i]=readInt();
			List<Event> e = new ArrayList<Event>();
			for (int i = 0 ; i <n; i++) {
				Event e1 = new Event(k[i]-h[i]+1, i, true);
				Event e2 = new Event(k[i], i, false);
				e.add(e1);
				e.add(e2);
			}
			Collections.sort(e);
			long ans = 0;
			long start = Long.MAX_VALUE;
			long end = 0;
			int stackSize = 0;
			for (Event ev: e) {
				if (ev.start) {
					start = min(start, ev.i);
					end = max(end, k[ev.index]);
					stackSize++;
				}
				else {
					end = max(end, ev.i);
					stackSize--;
					if (stackSize == 0) {
						long diff = end-start+1;
					//	System.out.println("YOYO " + diff);
						ans += diff*(diff+1)/2;
						
						
						start = Long.MAX_VALUE;
						end = 0;
					}
				}
			}
			out.println(ans);
		}
		
		out.close();
	}
	
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	static PrintWriter out = new PrintWriter(new BufferedOutputStream(System.out));
	static StringTokenizer st = new StringTokenizer("");
	static String read() throws IOException{
		while (!st.hasMoreElements()) st = new StringTokenizer(br.readLine());
		return st.nextToken();
	}
	public static int readInt() throws IOException{return Integer.parseInt(read());}
	public static long readLong() throws IOException{return Long.parseLong(read());}
}