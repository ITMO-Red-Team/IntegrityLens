
import java.io.*;
import java.math.*;
import java.util.*;


public class test {
	 
	   static class Pair{ 
		   long x;
		   long y;
		   
		   Pair(long x,long y){ 
			   this.x = x;
			   this.y = y;
			   
		   }
	   }
	  
	   static class Compare { 
		   
		     void compare(Pair arr[], int n) 
		    { 
		        // Comparator to sort the pair according to second element 
		        Arrays.sort(arr, new Comparator<Pair>() { 
		            @Override public int compare(Pair p1, Pair p2) 
		            { 
		            	if(p1.x!=p2.x) {
		                return (int)(p1.x - p2.x); 
		            	}
		            	else { 
		            		return (int)(p1.y - p2.y);
		            	}
		            } 
		        }); 
		  
//		        for (int i = 0; i < n; i++) { 
//		            System.out.print(arr[i].x + " " + arr[i].y + " "); 
//		        } 
//		        System.out.println(); 
		    } 
		} 
	 
	   static class Scanner {
	        BufferedReader br;
	        StringTokenizer st;
	 
	        public Scanner()
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
	
	
	   public static void main(String args[]) throws Exception { 
		
		   
		   Scanner sc = new Scanner();
		   StringBuilder res = new StringBuilder();
		   
		   int tc = sc.nextInt();
		   
		   while(tc-->0) { 
			
			int n = sc.nextInt();
			
			long c[] = new long[n];
			
			for(int i=0;i<n;i++) { 
				c[i] = sc.nextLong();
			}
			
			
			long min_odd = Integer.MAX_VALUE;
			long min_even = Integer.MAX_VALUE;
		    long ans = Long.MAX_VALUE;
		    long sum = 0;
		    
			int cnt1 = n;
			
			int cnt2 = n;
			
			
		    for(int i = 0; i < n; i++){
		        sum += c[i];
		        if(i % 2 == 0){
		            cnt1--;
		            min_odd = Math.min(min_odd, c[i]);
		        }
		        else{
		            cnt2--;
		            min_even = Math.min(min_even, c[i]);
		        }
		        if(i > 0){
		            long temp = sum + (min_odd * cnt1) + (min_even * cnt2);
		            ans = Math.min(ans, temp);
		        }
		 
		    }
		    
		    System.out.println(ans);
		   }
		   
		   System.out.println(res);
	   }
}  




