import java.io.*;
import java.util.*;
public class MonstersAndSpells {
	public static PrintWriter out;
	public static void main(String[] args)throws IOException {
        BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st=new StringTokenizer(br.readLine());
        out=new PrintWriter(System.out);
        int t=Integer.parseInt(st.nextToken());
        while(t-->0) {
        	st=new StringTokenizer(br.readLine());
        	int n=Integer.parseInt(st.nextToken());//monsters
        	int time[]=new int[n];//time
        	int health[]=new int[n];//health
        	st=new StringTokenizer(br.readLine());
        	for(int i=0;i<n;i++) {
        		time[i]=Integer.parseInt(st.nextToken());
        	}
        	st=new StringTokenizer(br.readLine());
        	for(int i=0;i<n;i++) {
        		health[i]=Integer.parseInt(st.nextToken());
        	}
        	State a[]=new State[n];
        	for(int i=0;i<n;i++) {
        		a[i]=new State(time[i]-health[i], time[i]);
        	}
        	Arrays.sort(a);
        	long ans=0;
        	for(int i=0;i<n;i++) {
        		int j=i+1;
        		int max=a[i].time;
        		while(j<n&&a[j].x<max) {
        			max=Math.max(max, time[j]);
        			j++;
        		}
        		ans+=((long)(max-a[i].x)*(long)(max-a[i].x+1))/2;
        		i=j-1;
        	}
        	out.println(ans);
        }
        out.close();
	}
	static class State implements Comparable<State>{
		int x, time;
		public State(int x, int time) {
			this.x=x;this.time=time;
		}
		public int compareTo(State o) {
			return x-o.x;
		}
	}
}
