import java.util.*;
import java.io.*;

////***************************************************************************
 /* public class E_Gardener_and_Tree implements Runnable{

       public static void main(String[] args) throws Exception {
        new Thread(null, new E_Gardener_and_Tree(), "E_Gardener_and_Tree", 1<<28).start();
       }
    public void run(){
         WRITE YOUR CODE HERE!!!!
         JUST WRITE EVERYTHING HERE WHICH YOU WRITE IN MAIN!!!
       }

  }
*/
/////**************************************************************************


public class C_Menorah{
    public static void main(String[] args) {
        FastScanner s= new FastScanner();
        //PrintWriter out=new PrintWriter(System.out);
        //end of program
        //out.println(answer);
        //out.close();
        StringBuilder res = new StringBuilder();
        int t=s.nextInt();
        int p=0;
        
        while(p<t){
        int n=s.nextInt();
        String str1=s.nextToken();
        String str2=s.nextToken();
       
        if(str1.equals(str2)){
            res.append("0 \n");
        }
      
        else{
            long count1=0;
            long count0=0;
            for(int i=0;i<n;i++){
                char ch=str1.charAt(i);
                if(ch=='1'){
                    count1++;
                }
            }
            count0=n-count1;
            if(count1==0){
                res.append("-1 \n");
            }
            else{
                long nice1=0;
                long nice0=0;
                for(int i=0;i<n;i++){
                    char ch=str2.charAt(i);
                    if(ch=='1'){
                        nice1++;
                    }
                }
                nice0=(n-nice1);
                int check1=0;
                int check2=0;
                if((count1==nice1)&&(count0==nice0)){
                    check1=1;
                }
                long yo1=(1+count0);
                long yo0=(count1-1);
                if((yo1==nice1)&&(yo0==nice0)){
                    check2=1;
                }
                if(check1==0 && check2==0){
                    res.append("-1 \n");
                }
                else{
                    //System.out.println("here");
                    long correct=0;
                    long wrong=0;
                    long correct1=0;
                    long correct0=0;
                    long wrong1=0;
                    long wrong0=0;
                   
                    for(int i=0;i<n;i++){
                        char ch1=str1.charAt(i);
                        char ch2=str2.charAt(i);
                        if(ch1==ch2){
                            correct++;
                            if(ch1=='1'){
                                correct1++;
                            }
                            else{
                                correct0++;
                            }
                        }
                        else{
                            wrong++;
                            if(ch1=='1'){
                                wrong1++;
                            }
                            else{
                                wrong0++;
                            }
                        }
                    }
              long ans1=      solve(correct1,correct0,wrong1,wrong0,1);
              long ans2=      solve(correct1,correct0,wrong1,wrong0,0);
              long ans=Math.min(ans1,ans2);
              if(ans>=Integer.MAX_VALUE){
                  ans=-1;
              }
              res.append(ans+" \n");
                  
                    
                   
                }



            }

        }
        p++;
    }
    
    System.out.println(res);
    
    }
    private static long solve( long correct1, long correct0, long wrong1, long wrong0,long a) {
               long op1=Integer.MAX_VALUE;
               long op2=Integer.MAX_VALUE;
               if(wrong1==0 && wrong0==0){
                   return 0;
               }
               if(a==1){
               {
                   // using correct1
                   if(correct1>0){
                       long newcorrect1=1+wrong0;
                       long newcorrect0=wrong1;
                       long newwrong1=correct0;
                       long newwrong0=correct1-1;
                       op1=(1+solve(newcorrect1,newcorrect0,newwrong1,newwrong0,0));

                   }
               }
            }
            else{

               {
                   //using wrong1
                   {
                       if(wrong1>0){
                        long newcorrect1=wrong0;
                        long newcorrect0=wrong1-1;
                        long newwrong1=1+correct0;
                        long newwrong0=correct1;
                        op2=(1+solve(newcorrect1,newcorrect0,newwrong1,newwrong0,1));
                       }
                   }
               }
            }  
               long ans=Math.min(op1,op2);
               return ans;
    
    }
    static class FastScanner {
		BufferedReader br;
		StringTokenizer st;
 
		public FastScanner(String s) {
			try {
				br = new BufferedReader(new FileReader(s));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
 
		public FastScanner() {
			br = new BufferedReader(new InputStreamReader(System.in));
		}
 
		String nextToken() {
			while (st == null || !st.hasMoreElements()) {
				try {
					st = new StringTokenizer(br.readLine());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return st.nextToken();
		}
 
		int nextInt() {
			return Integer.parseInt(nextToken());
		}
 
		long nextLong() {
			return Long.parseLong(nextToken());
		}
 
		double nextDouble() {
			return Double.parseDouble(nextToken());
		}
	}


    static long modpower(long x, long y, long p)
     {
      long res = 1; // Initialize result
   
      x = x % p; // Update x if it is more than or
      // equal to p
   
      if (x == 0)
        return 0; // In case x is divisible by p;
   
      while (y > 0)
      {
   
        // If y is odd, multiply x with result
        if ((y & 1) != 0)
          res = (res * x) % p;
   
        // y must be even now
        y = y >> 1; // y = y/2
        x = (x * x) % p;
      }
      return res;
    }

  
   // SIMPLE POWER FUNCTION=>


      static long power(long x, long y)
        {
            long res = 1;     // Initialize result
        
            while (y > 0)
            {
            
                // If y is odd, multiply x with result
                if ((y & 1) != 0)
                    res = res * x;
        
                // y must be even now
                y = y >> 1; // y = y/2
                x = x * x;  // Change x to x^2
            }
            return res;
        }





}