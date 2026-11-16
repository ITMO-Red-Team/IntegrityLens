import java.io.*;
import java.util.*;

public class Main{

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
            while (st == null || !st.hasMoreElements()) {
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
    
    


    

    

public static void main(String[] args) throws Exception {


    FastReader sc = new FastReader();
    
   int t=sc.nextInt();

   
  while(t-->0)
  {
      int n=sc.nextInt();

      long k[]=new long[n];
      for(int i=0;i<n;i++)
      {
          k[i]=sc.nextLong();
      }

      long h[]=new long[n];
      for(int i=0;i<n;i++)
      {
          h[i]=sc.nextLong();
      }
      ArrayList<Long> al=new ArrayList<>();
      long csp=h[n-1],idx=k[n-1]-h[n-1];
      for(int i=n-2;i>=0;i--)
      {
          if(idx<k[i])
          {
              if(k[i]-idx<h[i])
              {
                  long diff=h[i]-(k[i]-idx);
                  csp+=diff;
                  idx-=diff;
              }
          }
          else
          {
              al.add(csp);
              csp=h[i];
              idx=k[i]-csp;
          }
         

      }

      long sum=0;
      al.add(csp);
      for(long i:al)
      {
         sum=sum+((i*(i+1))/2);
      }

      System.out.println(sum);
     
  }
}
}




