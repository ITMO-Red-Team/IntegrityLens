
import java.io.*;
import java.util.*;



public class Test2  {
	static class node{
		int l,r;
		ArrayList<Integer> child;
		node(int l,int r){
			child = new  ArrayList<Integer>();
			this.l = l;
			this.r = r;
		}
	}
	  static FastReader f = new FastReader();
	public static void main(String[] args) throws IOException {	
		int t =f.nextInt();
		while(t-->0) {
			int n = f.nextInt();
			ArrayList<node> arr = new  ArrayList<node>();
			for(int i = 0;i<n;i++) {
				int l = f.nextInt(),r = f.nextInt();
				arr.add(new node(l,r));
			}
			for(int i = 0;i<n-1;i++) {
				int u = f.nextInt()-1,v = f.nextInt()-1;
				arr.get(u).child.add(v);
				arr.get(v).child.add(u);
			}
			long dp[][] = new long[n][2];
			for(int i = 0;i<n;i++)Arrays.fill(dp[i],-1);
			long min = 0;
			long max = 0;
			for(int e:arr.get(0).child) {
				 min+=fun(arr, e, 0, arr.get(0).l,dp);
			}
			for(int e:arr.get(0).child) {
				 max+=fun(arr, e, 0, arr.get(0).r,dp);
			}
			System.out.println(Math.max(min, max));
	     }
     }
   static long fun(ArrayList<node> arr,int i,int p,int pre,long dp[][]) {
	   long min = 0,max = 0;
	   if(dp[i][0]!=-1) {
		   min = dp[i][0];
	   }else {
	   for(int e:arr.get(i).child) {
		   if(e!=p) {
			   min+=fun(arr, e, i, arr.get(i).l,dp);
		   }
	   }
	   dp[i][0] =min;
	   }
	   if(dp[i][1]!=-1) {
		   max = dp[i][1];
	   }else {
	   for(int e:arr.get(i).child) {
		   if(e!=p) {
			   max+=fun(arr, e, i, arr.get(i).r,dp);
		   }
	   }dp[i][1] = max;
	   }
	   min+=(Math.abs(arr.get(i).l-pre));
	   max +=Math.abs(arr.get(i).r-pre);
	   //System.out.println(min+" max = "+max+" i = "+i+" per ="+p+" pre = "+pre);
	   return Math.max(min, max);
   }
	static ArrayList<Integer> primeFectors(int n){
		ArrayList<Integer> ret = new ArrayList<Integer>();
		while (n%2==0)
        {
           ret.add(2);
            n /= 2;
        }
		for (int i = 3; i <= Math.ceil(Math.sqrt(n)); i+= 2)
        {
			
            // While i divides n, print i and divide n
            while (n%i==0)
            {
                ret.add(i);
                n /= i;
            }
        }
		 if (n > 2)
	            ret.add(n);
		return ret;
	}
	static class FastReader 
    { 
         BufferedReader br; 
        StringTokenizer st; 
        public FastReader() 
        { 
            br = new BufferedReader(new
                     InputStreamReader(System.in)); 
        } 
        String next() 
        { 
            while (st == null || !st.hasMoreElements()) 
            { 
                try
                { 
                    st = new StringTokenizer(br.readLine()); 
                } 
                catch (IOException  e) 
                { 
                    e.printStackTrace(); 
                } 
            } 
            return st.nextToken(); 
        } 
        int nextInt() 
        { 
            return Integer.parseInt(next()); 
        } 	  
        long nextLong() 
        { 
            return Long.parseLong(next()); 
        } 
        double nextDouble() 
        { 
            return Double.parseDouble(next()); 
        } 
        String nextLine() 
        { 
            String str = ""; 
            try
            { 
                str = br.readLine(); 
            } 
            catch (IOException e) 
            { 
                e.printStackTrace(); 
            } 
            return str; 
        } 
    }
}



