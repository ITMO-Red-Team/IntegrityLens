import java.util.*;
import java.io.*;
import java.math.*;
 

public final class Main{
	public static void main(String[] args) throws IOException{
//		Scanner sc=new Scanner(System.in);
		FastReader sc=new FastReader();
		PrintWriter writer=new PrintWriter(System.out);
		
		int tc=sc.nextInt();
		while(tc-->0) {
			  int n=sc.nextInt();
	          long[] c=new long[n];
	          for(int i=0;i<n;i++) c[i]=sc.nextInt();   
	          long ans=c[0]*n+c[1]*n;
	          long x=Long.MAX_VALUE-1,y=Long.MAX_VALUE-1,sumx=0,sumy=0,cx=0,cy=0,min1=Long.MAX_VALUE-1,min2=Long.MAX_VALUE-1;
	          for(int i=0;i<n;i++) {
	          if(i%2==0){
	              x=Math.min(x,c[i]);
	              sumx+=c[i];
	              cx=i/2+1;
	              min1=sumx+x*(n-cx);
	          }
	          else {
	        	  y=Math.min(y,c[i]);
	        	  sumy+=c[i];cy=(i+1)/2;
	              min2=sumy+y*(n-cy);
	          }
	          if(i>=1) ans=Math.min(ans,min1+min2);
	         }
	          
	         System.out.println(ans);
		}
		writer.flush();
		writer.close();
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