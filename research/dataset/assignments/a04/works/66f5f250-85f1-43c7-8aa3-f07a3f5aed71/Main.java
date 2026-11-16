import java.util.*;

import java.lang.*;
import java.io.*;

public class Template {
	
	static class FastReader {
  BufferedReader br;
  StringTokenizer st;

  public FastReader()
  {
      br = new BufferedReader(
          new InputStreamReader(System.in));
  }

  String next()
  {
      while (st == null ||!st.hasMoreElements()) {
          try {
              st = new StringTokenizer(br.readLine());
          }
          catch (IOException e) {
              e.printStackTrace();
          }
      }
      return st.nextToken();
  }

  int nextInt() { return Integer.parseInt(next()); }

  long nextLong() { return Long.parseLong(next()); }

  double nextDouble()
  {
      return Double.parseDouble(next());
  }

  String nextLine()
  {
      String str = "";
      try {
          str = br.readLine();
      }
      catch (IOException e) {
          e.printStackTrace();
      }
      return str;
  }
  
}
	
//	static void solve(String s) 
//	{
////		Scanner sc = new Scanner(System.in);
////		String s = sc.next();
//		
//		int x[] = new int[2];
//		x[0] = x[1] = -1;
//		
//		int ans = 0;
//		int n = s.length();
//		for(int i=0;i<n;i++)
//		{
//			int c = s.charAt(i) - '0';
//			if(c == 1 || c == 0)
//			{
//				x[(i%2) ^ c] = i;
//			}
//			int min = Math.min(x[0], x[1]);
//			ans += i - min;
//			//System.out.println(ans);
//		}
//		System.out.println(ans);
//	}
//	

//	    public static void main(String args[])
//	    {
//	    	FastReader sc = new FastReader();
//	    	//solve();
//	    	//Scanner sc = new Scanner(System.in)
//	       int testcases = sc.nextInt();  // nTest is the number of treasure hunts.
//	      
////	    	int testcases = 3;
//	        while(testcases-- > 0)
//	        {
//	        	String s = sc.next();
//	        	solve(s);
//	        	
//	        }
//	    
//	    }
	
	static class Pair implements Comparable<Pair>
	{
		int h;
		int ind;
		
		Pair(int h, int ind)
		{
			this.h = h;
			this.ind = ind;
		}
		@Override
		public int compareTo(Pair o) {
			
			return this.h - o.h;
		}
		
		
	}
	public static void main(String[] args) {

		FastReader fs=new FastReader();

		int T=fs.nextInt();

		for (int tt=0; tt<T; tt++) {
		int n = fs.nextInt();
		int m = fs.nextInt();
		int x = fs.nextInt();
		
		if(n < m)
		{
			System.out.println("NO");
			continue;
		}
		Pair a[] = new Pair[n];
		PriorityQueue<Pair> heap = new PriorityQueue<>();
		for(int i=0;i<n;i++)
		{
			a[i] = new Pair(fs.nextInt(), i);
			
		}
		
		Arrays.sort(a);
		for(int i=1;i<=m;i++)
		{
			heap.add(new Pair(0, i));
		}
		int max = Integer.MIN_VALUE;
		int min = Integer.MAX_VALUE;
		int ans[] = new int[n];
		int idx = 0;
		while(!heap.isEmpty() && idx < n)
		{
			Pair curr = heap.poll();
			curr.h += a[idx].h;
			ans[a[idx].ind] = curr.ind;
			heap.add(new Pair(curr.h,  curr.ind));
			idx++;
		}
			
		
		
//		int towers[] = new int[m+1];
//		int tower = 1;
//		boolean flag = false;
//		boolean inc = true;
//		for(int i=0;i<n;i++)
//		{
//			if(tower == m+1)
//			{
//				tower = m;
//				inc = false;
//			}
//			if(tower == 0)
//			{
//				tower = 1;
//				inc = true;
//			}
//			towers[tower] += a[i].h;
//			System.out.println(a[i].h +" THis" + tower);
////			min = Math.min(min, towers[tower]);
////			max = Math.max(max, towers[tower]);
//			ans[a[i].ind] = tower;
////			if(Math.abs(max - min) > x)
////			{
////				System.out.println("NO" + a[i].ind+" "+a[i].h +" "+min +" "+max);
////				flag = true;
////				break;
////			}
//			if(inc)
//				tower++;
//			else
//				tower--;
//		}
//		for(int i=1;i<=m;i++)
//		{
//			min = Math.min(min, towers[i]);
//			max = Math.max(max, towers[i]);
//		}
//		if(Math.abs(max - min) > x)
//			{
//				System.out.println("NO" + max+" "+min);// + a[i].ind+" "+a[i].h +" "+min +" "+max);
//				//flag = true;
//				continue;
//			}
//		if(flag)
//			continue;
		System.out.println("YES");
		for(int i:ans)
			System.out.print(i+" ");
		
		System.out.println();

		}
	
}
}
