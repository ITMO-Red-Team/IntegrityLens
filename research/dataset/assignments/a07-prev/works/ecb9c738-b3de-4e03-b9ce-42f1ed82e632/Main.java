/* package codechef; // don't place package name! */

import java.util.*;
import java.lang.*;
import java.io.*;

/* Name of the class has to be "Main" only if the class is public. */
public class Codechef
{static class FastReader 
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
	public static void main (String[] args) throws java.lang.Exception
	{
		FastReader scan = new FastReader();
		PrintWriter pw = new PrintWriter(System.out);
        int n = scan.nextInt();
        ArrayList<Integer> a = new ArrayList<>();
        ArrayList<Integer> b = new ArrayList<>();
        for(int i=0;i<n;i++){
            int x = scan.nextInt();
            if(x==1)
            a.add(i);
            else
            b.add(i);  
        }
        int x = a.size();
        if(x==0){
            pw.println(0);
            pw.flush();
            return;
        }
        int y = b.size();
        int dp[][] = new int[x][y];
        int min = Integer.MAX_VALUE;
        for(int i=0;i<y;i++){
            min = Math.min(Math.abs(a.get(0) - b.get(i)),min);
            dp[0][i] = min;
        }
        for(int i=1;i<x;i++){
            min = Integer.MAX_VALUE;
            for(int j=i;j<y;j++){
                min = Math.min(Math.abs(a.get(i)-b.get(j))+dp[i-1][j-1],min);
                dp[i][j] = min;
            }
        }
        pw.println(dp[x-1][y-1]);
        pw.flush();
	}
   
}
