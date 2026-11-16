import java.io.*;

import java.util.*;
/*


 */



public class C {
    static FastReader sc=null;
 
    
    public static void main(String[] args) {
    	sc=new FastReader();
    	int t=sc.nextInt();
    	while(t-->0) {
    		int n=sc.nextInt();
    		int c[]=sc.readArray(n);
    		long dp[]=new long[n];
    		dp[0]=Long.MAX_VALUE;
    		dp[1]=(long)n*c[1]+ (long)n*c[0];
    		long preMin[]=new long[n],pre[]=new long[n];
    		preMin[0]=pre[0]=c[0];
    		preMin[1]=pre[1]=c[1];
    		for(int i=2;i<n;i++) {
    			preMin[i]=Math.min(preMin[i-2],c[i]);
    			pre[i]=pre[i-2]+c[i];
    		}
    		for(int i=2;i<n;i++) {
    			dp[i]=preMin[i]*(n-i/2)+pre[i]-preMin[i]+preMin[i-1]*(n-(i-1)/2)+pre[i-1]-preMin[i-1];
    		}
    		long ans=Long.MAX_VALUE;
    		//print(dp);
    		for(long e:dp)ans=Math.min(ans, e);
    		System.out.println(ans);
    	}
    	
    	
    }
  
  
   
    
    
   
 
   
    
    static void ruffleSort(int a[]) {
        ArrayList<Integer> al=new ArrayList<>();
        for(int i:a)al.add(i);
        Collections.sort(al);
        for(int i=0;i<a.length;i++)a[i]=al.get(i);
    }
    static void ruffleSort(long a[]) {
        ArrayList<Long> al=new ArrayList<>();
        for(long i:a)al.add(i);
        Collections.sort(al);
        for(int i=0;i<a.length;i++)a[i]=al.get(i);
    }
    
    static int[] reverse(int a[]) {
        ArrayList<Integer> al=new ArrayList<>();
        for(int i:a)al.add(i);
        Collections.sort(al,Collections.reverseOrder());
        for(int i=0;i<a.length;i++)a[i]=al.get(i);
        return a;
    }
 
    static int gcd(int a,int b) {
		if(b==0)return a;
		else return gcd(b,a%b);
	}   
        
 
    
    
    
    static void print(long a[]) {
        for(long e:a) {
            System.out.print(e+" ");
        }
        System.out.println();
    }
    static void print(char a[]) {
        for(char e:a) {
            System.out.print(e);
        }
        System.out.println();
    }
    
    
    static void print(int a[]) {
        for(int e:a) {
            System.out.print(e+" ");
        }
        System.out.println();
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
        int[] readArray(int n) {
            int a[]=new int [n];
            for(int i=0;i<n;i++) {
                a[i]=sc.nextInt();
            }
            return a;
        }
    } 
}
