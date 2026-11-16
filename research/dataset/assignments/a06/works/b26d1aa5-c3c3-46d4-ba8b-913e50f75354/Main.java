import java.util.*;
import java.lang.*;
import java.io.*;
public class Main
{
    // static Scanner sc=new Scanner(System.in);
//    static ArrayList<Integer> al; 
	static long min;
// 	static long[] dp;
    static FastReader sc=new FastReader(); 
	public static void main (String[] args) throws java.lang.Exception
	{
        PrintWriter w=new PrintWriter(System.out);
        int t=sc.nextInt();
        while(t-->0){
            // dp=new long[n+1][n+1][n];
        	int n=sc.nextInt();
        	long[] c=new long[n];
        	for(int i=0;i<n;i++) {
        		c[i]=sc.nextLong();
        	}
        	min=c[0]*n+c[1]*n;
        // 	f(n,c,n,n,0,0);
        long x=Long.MAX_VALUE-1,y=Long.MAX_VALUE-1,sumx=0,sumy=0,cx=0,cy=0,min1=Long.MAX_VALUE-1,min2=Long.MAX_VALUE-1;
            for(int i=0;i<n;i++) {
        		if(i%2==0){
        		    x=Math.min(x,c[i]);sumx+=c[i];cx=i/2+1;
        		    min1=sumx+x*(n-cx);
        		}
        		else {y=Math.min(y,c[i]);sumy+=c[i];cy=(i+1)/2;
        		    min2=sumy+y*(n-cy);
        		}
        		if(i>=1)min=Math.min(min,min1+min2);
        	}
        	System.out.println(min);
        }
		w.flush();
		w.close();
	}
// 	public static long f(int n, long[] c, int x, int y, int i, long cost) {
// 	    long l=Long.MAX_VALUE;
// 		if(i>=n || x>n || y>n)return 0;
// 		if(dp[x][y][i]!=-1)return dp[x][y][i];
// 		if(x==0 && y==0) {min=Math.min(cost, min);return;}
// 		if(x==0) {
// 			if(i%2!=0)min=Math.min(cost+c[i]*y, min);
// 			return;
// 		}
// 		if(y==0) {
// 			if(i%2==0)min=Math.min(cost+c[i]*x, min);
// 			return;
// 		}
// 		if(i%2==0) {
// 			for(int j=1;j<=x;j++) {
// 				l=Math.min(l,f(n,c,x-j,y,i+1,cost+j*c[i]));
// 			}
// 		}
// 		else {
// 			for(int j=1;j<=y;j++) {
// 				l=Math.min(l,f(n,c,x,y-j,i+1,cost+j*c[i]));
// 			}
// 		}
// 	}
}

class FastReader 
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
