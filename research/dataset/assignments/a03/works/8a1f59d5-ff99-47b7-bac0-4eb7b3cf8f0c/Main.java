//https://codeforces.com/contest/1547/problem/E
//E. Air Conditioners
import java.util.*;
import java.io.*;
public class CF_1547_E{
	public static void main(String[] args) throws Exception{
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out));
		StringBuilder sb = new StringBuilder();
		StringTokenizer st, st1;

		int q = Integer.parseInt(br.readLine());
		while(q-->0){
			br.readLine();
			st = new StringTokenizer(br.readLine());
			int n = Integer.parseInt(st.nextToken());
			int k = Integer.parseInt(st.nextToken());

			PriorityQueue<Pair> right_side = new PriorityQueue<Pair>();

			int at[] = new int[n+1];
			st = new StringTokenizer(br.readLine());
			st1 = new StringTokenizer(br.readLine());

			for(int i=0;i<k;i++){
				int a = Integer.parseInt(st.nextToken());
				int t = Integer.parseInt(st1.nextToken());
				at[a] = t;
				right_side.add(new Pair(a, t));
			}

			long left= Integer.MAX_VALUE;

			for(int i=1;i<=n;i++){

				while(right_side.isEmpty()==false && right_side.peek().a<=i){
					Pair temp = right_side.poll();
					if(temp.t - temp.a <= left)
						left = temp.t - temp.a;
				}

				if(at[i]!=0){
					if(at[i]-i <=left)
						left = at[i] - i;
				}

				long ans = left+i;

				if(!right_side.isEmpty()){
					Pair right = right_side.peek();
					ans = Math.min(ans, right.t+right.a-i);
				}

				sb.append(ans+" ");
			}
			sb.append("\n");
		}

		pw.print(sb);
		pw.flush();
		pw.close();
	}
}

class Pair implements Comparable<Pair>{
	int a, t;
	Pair(int a, int t){
		this.a = a;
		this.t = t;
	}
	public int compareTo(Pair A){
		return (this.a+this.t-A.a-A.t);
	}
}