import java.util.*;
 
import java.io.*;
 
public class AiseHi {
	static Scanner sc = new Scanner(System.in);
	static int mod = (int)(1e9+7);
	public static void main (String[] args) {
		PrintWriter out = new PrintWriter(System.out);
		int t = 1;
    	t = sc.nextInt();
    	z : while(t-->0) {
    		int n = sc.nextInt();
    		int m = sc.nextInt();
    		int x = sc.nextInt();
    		PriorityQueue<twoval> myQueue = new PriorityQueue<>();
    		for(int i=1;i<=m;i++) {
    			myQueue.add(new twoval(0,i));
    		}
    		List<twoval> arrayList = new ArrayList<>();
    		int myArray[] = new int[n];
    		for(int i=0;i<n;i++) {
    			int aasjd = sc.nextInt();
    			arrayList.add(new twoval(aasjd,i));
    			myArray[i] = aasjd;
    		}
    		Collections.sort(arrayList);
    		int ans[] = new int[n];
    		for(int i=n-1;i>=0;i--) {
    			twoval p = myQueue.poll();
    			long aasjd = p.myArray;
    			int idx = p.b;
    			aasjd += arrayList.get(i).myArray;
    			ans[arrayList.get(i).b] = idx;
    			myQueue.add(new twoval(aasjd,idx));
    		}
    		long dasdaknw[] = new long[m];
    		long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
    		for(int i=0;i<n;i++) {
    			dasdaknw[ans[i]-1] += myArray[i];
    		}
    		for(int i=0;i<m;i++) {
    			min = Math.min(min, dasdaknw[ans[i]-1]);
    			max = Math.max(max, dasdaknw[ans[i]-1]);
    		}
    		if(max-min>x) {
    			out.write("NO\n");
    			continue;
    		}
    		out.write("YES\n");
    		for(int aasjd : ans) out.write(aasjd+" ");
    		out.write("\n");
    	}
		out.close();
	}
	static int ceil(int myArray,int b) {
		return myArray/b + (myArray%b==0?0:1);
	}
	static boolean prime[] = new boolean[2000009];
	static int fac[] = new int[2000009];
	
	static void sieve() {
		prime[0] = true;
		prime[1] = true;
		int max = 1000000;
		for(int i=2;i*i<=max;i++) {
			if(!prime[i]) {
				for(int j=i*i;j<=max;j+=i) {
					prime[j] = true;
					fac[j] = i;
				}
			}
		}
	}
	static int gcd(int myArray,int b) { if(b==0) return myArray; return gcd(b,myArray%b); }
}
 
class twoval implements Comparable<twoval>{
	long myArray;
	int b;
	twoval(long aasjd,int r){
		this.myArray = aasjd;
		this.b = r;
	}
	public int compareTo(twoval o) {
		return (int) (this.myArray - o.myArray);
	}
}
