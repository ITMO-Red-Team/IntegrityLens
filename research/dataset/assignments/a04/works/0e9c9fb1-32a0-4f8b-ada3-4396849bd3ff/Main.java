import java.io.*;
import java.util.*;
 
public class Main {
	static class FastReader{ 
        BufferedReader br; 
        StringTokenizer st; 
        public FastReader(){br = new BufferedReader(new InputStreamReader(System.in));}
        String next(){while (st == null || !st.hasMoreElements()){try{st = new StringTokenizer(br.readLine());} 
        catch (IOException  e){e.printStackTrace();}}return st.nextToken();}
        int nextInt(){ return Integer.parseInt(next());}long nextLong(){return Long.parseLong(next());}double nextDouble(){return Double.parseDouble(next());}
        String nextLine(){String str = ""; try{str = br.readLine(); } catch (IOException e) {e.printStackTrace();} return str; } 
    }
	static FastReader sc = new FastReader();
	static long mod = (long) (1e9+7);
	static boolean spe[];
	static int spen[];
	static int dep[];
	static List<Integer> tre[];
	static int par[][];
	public static void main (String[] args){
        PrintWriter out = new PrintWriter(System.out);
        int t = 1;
        t = sc.nextInt();
        z :for(int tc=1;tc<=t;tc++){
    		int n = sc.nextInt();
    		int m = sc.nextInt();
    		int x = sc.nextInt();
    		PriorityQueue<pairClass> priority = new PriorityQueue<>();
    		for(int i=1;i<=m;i++) {
    			priority.add(new pairClass(0,i));
    		}
    		List<pairClass> list = new ArrayList<>();
    		int a[] = new int[n];
    		for(int i=0;i<n;i++) {
    			int value = sc.nextInt();
    			list.add(new pairClass(value,i));
    			a[i] = value;
    		}
    		Collections.sort(list);
    		int answer[] = new int[n];
    		for(int i=n-1;i>=0;i--) {
    			pairClass p = priority.poll();
    			long value = p.a;
    			int idx = p.b;
    			value += list.get(i).a;
    			answer[list.get(i).b] = idx;
    			priority.add(new pairClass(value,idx));
    		}
    		long totalSum[] = new long[m];
    		long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
    		for(int i=0;i<n;i++) {
    			totalSum[answer[i]-1] += a[i];
    		}
    		for(int i=0;i<m;i++) {
    			min = Math.min(min, totalSum[answer[i]-1]);
    			max = Math.max(max, totalSum[answer[i]-1]);
    		}
    		if(max-min>x) {
    			out.write("NO\n");
    			continue;
    		}
    		out.write("YES\n");
    		for(int value : answer) out.write(value+" ");
    		out.write("\n");
        }
		out.close();
	}
}
class pairClass implements Comparable<pairClass>{
	long a;
	int b;
	pairClass(long value,int r){
		this.a = value;
		this.b = r;
	}
	@Override
	public int compareTo(pairClass o) {
		return (int) (this.a - o.a);
	}
}
