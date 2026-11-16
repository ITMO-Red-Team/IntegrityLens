//package practice;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;


public class Main {

	
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
	
	 static int gcd(int a,int b) {
			if(b<=0) {
				return a;
			}
			return gcd(b,a%b);
		}
	 
	
	 static int digSum(int n)
	    {
	        if (n == 0)
	        return 0;
	        return (n % 9 == 0) ? 9 : (n % 9);
	    }
	 
	 static Set<Long> set=new HashSet<>();
	 
	static void sieveOfEratosthenes(int n)
	    {
	       
	        boolean prime[] = new boolean[n+1];
	        for(int i=0;i<n;i++)
	            prime[i] = true;
	          
	        for(int p = 2; p*p <=n; p++)
	        {
	            
	            if(prime[p] == true)
	            {
	                
	                for(int i = p*p; i <= n; i += p)
	                    prime[i] = false;
	            }
	        }
	          
	        // Print all prime numbers
	        for(int i = 2; i <= n; i++)
	        {
	            if(prime[i] == true) {
	            	set.add((long)i);
	            }
	               
	        }
	    }
	      
	static boolean isPrime(long x){
	        if(x <= 1)return false;
	        if(x == 2)return true;
	        if(x%2 == 0)return false;
	        for(long i = 3; i*i <= x; i+= 2)
	            if(x%i == 0)
	                return false;
	            return true;
	    }
	
	static int power(int x, int y, int p) {
        
        int res = 1; // Initialize result
         
        //Update x if it is more than or
        // equal to p
        x = x % p;
 
        while (y > 0) {
             
            // If y is odd, multiply x with result
            if ((y & 1) == 1)
                res = (res * x) % p;
         
            // y must be even now
            y = y >> 1; // y = y/2
            x = (x * x) % p;
        }
         
        return res;
    }
	
	static boolean palin(char ch[]) {
		int i=0;int j=ch.length-1;
		while(i<j) {
			if(ch[i]!=ch[j]) return false;
			
			i++;
			j--;
		}
		return true;
	}
	
	static int highestDivisor(int n) {
	    if ((n & 1) == 0)
	        return n / 2;
	    int i = 3;
	    while (i * i <= n) {
	        if (n % i == 0) {
	            return n / i;
	        }
	        i = i + 2;
	    }
	    return 1;
	}
	
	static boolean isPowerOfTwo(int n)
	{
	    if(n==0)
	    return false;
	 
	return (int)(Math.ceil((Math.log(n) / Math.log(2)))) ==
	       (int)(Math.floor(((Math.log(n) / Math.log(2)))));
	}
	
	 static boolean isPerfectSquare(int x)
	    {
	        if (x >= 0) {
	           
	          
	            int sr = (int)Math.sqrt(x);
	           
	           
	 
	            return ((sr * sr) == x);
	        }
	        return false;
	    }
	 
	 static boolean solve(int l,String s) {
		 int cnt=0;
		 int n=s.length();
		 for(int i=0;i<n;i++) {
			 if(s.charAt(i)=='1') cnt++;
			 
			 int per= (cnt *100)/(i+1);
			 if(per>=50) return true;
		 }
		 return false;
	 }
	 
	 
	 
	
	public static void main(String[] args) throws IOException {
      FastReader sc=new FastReader();
      int t=sc.nextInt();
      while(t-->0) {
    	  int n=sc.nextInt();
    	  int m=sc.nextInt();
    	  int x=sc.nextInt();
    	  int a[]=new int[n];
    	  int b[]=new int[n];
    	  int freq[]=new int[100002];
    	  Arrays.fill(freq, 0);
    	  for(int i=0;i<n;i++) {
    		  a[i]=b[i]=sc.nextInt();
    	  }
    	  Arrays.sort(b);
    	  int temp=1;
   
    	  Map<Integer,List<Integer>> map=new HashMap<>();
    	  for(int i=0;i<n;i++) {
    		  if(map.get(b[i])==null) {
    			  List<Integer> lis=new ArrayList<Integer>();
    			  lis.add(temp);
    			  map.put(b[i], lis);
    		  }
    		  else {
    			  List<Integer> lis=map.get(b[i]);
    			  lis.add(temp);
    			  map.put(b[i], lis);
    		  }
    		  temp++;
    		  if(temp==m+1) {
    			  temp=1;
    		  }
    	  }
//    	  for(Map.Entry<Integer,List<Integer>> set: map.entrySet()) {
//    		   System.out.println(set.getKey()+" "+set.getValue());
//    	  }
    	  StringBuilder sb=new StringBuilder();
     	  List<Integer> res=new ArrayList<Integer>();
    	  for(int i=0;i<n;i++) {
    		  List<Integer> lis=map.get(a[i]);
    		  res.add(lis.get(freq[a[i]]));
    		//  sb.append(lis.get(freq[a[i]])+" ");
    		//  System.out.println(lis);
    		  freq[a[i]]++;
    	  }
    	  boolean flag=true;
    	  int arr[]=new int[m+1];
    	  for(int i=0;i<n;i++) {
    		  arr[res.get(i)] += a[i];
    	  }
    	  for(int i=1;i<=m-1;i++) {
    		
    			 long cal= Math.abs(arr[i+1]-arr[i]);
    			 if(cal >x) {
    				 flag=false;
    				 break;
    			 
    		 }
    		 if(!flag) break;
    	  }
    	  if(!flag) {
    		  System.out.println("NO");
    	  }
    	  else {
    		  System.out.println("YES");
    		  for(int i=0;i<res.size();i++) {
    			  System.out.print(res.get(i)+" ");
    		  }
    		  System.out.println();
    	  }
    	  
      }
	}

}
