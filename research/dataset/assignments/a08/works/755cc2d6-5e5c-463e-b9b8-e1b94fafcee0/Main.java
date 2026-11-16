/* package codechef; // don't place package name! */

import java.util.*;
import java.lang.*;
import java.io.*;




public class Codechef
{
    
      static class FastReader 
    { 
        BufferedReader br; 
        StringTokenizer st; 
  
        public FastReader() 
        { 
            try {
               br=new BufferedReader(new FileReader("input.txt"));
               PrintStream out= new PrintStream(new FileOutputStream("output.txt"));
               System.setOut(out);
            }
  
            // Catch block to handle the exceptions
            catch (Exception e) {
                br=new BufferedReader(new InputStreamReader(System.in));
            }
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
     static int computeXOR(int n)
    {
        // If n is a multiple of 4
        if (n % 4 == 0)
            return n;
      
        // If n%4 gives remainder 1
        if (n % 4 == 1)
            return 1;
      
        // If n%4 gives remainder 2
        if (n % 4 == 2)
            return n + 1;
      
        // If n%4 gives remainder 3
        return 0;
    }
    public static void main (String[] args) throws java.lang.Exception
    {
 
       try{
        FastReader sc=new FastReader();
        BufferedWriter out = new BufferedWriter(
            new OutputStreamWriter(System.out));
        int t=sc.nextInt();
        while(t-->0)
        {
            
            int n = sc.nextInt();
            
            int k[] = new int[n];
            for(int i=0;i<n;i++)
            {
                k[i] = sc.nextInt();
            }
            int h[] = new int[n];
            for(int i=0;i<n;i++)
            {
                h[i] = sc.nextInt();
            }
            
            
        long tail = k[n-1];
        long span = h[n-1];
            
            long ans = 0;
            
            for(int i=n-2;i>=0;i--)
            {
                if((tail-span)+1>k[i])
                {
                    ans+=(span*(span+1))/2;
                    tail=k[i];
                    span=h[i];
                }
                else if((tail-span)+1<=((k[i]-h[i])+1))
                {
                    continue;
                }
                else
                {
                    span+=(((tail-span)+1) - ((k[i]-h[i])+1));
                }
            }
            
            ans+=(span*(span+1))/2;
            
            System.out.println(ans);
            
        }
    }catch(Exception e){
            return;
        }
    }
}

