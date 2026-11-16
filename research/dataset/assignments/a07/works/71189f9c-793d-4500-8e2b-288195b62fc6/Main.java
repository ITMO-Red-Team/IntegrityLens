import java.io.*;

import java.util.*;
 public class Solution {
	 
	static long res;
	public static void main(String[] args) throws Exception
    {
		FastReader fr=new FastReader();
        int n=fr.nextInt();
        ArrayList<Integer> oc=new ArrayList<>();
        ArrayList<Integer> em=new ArrayList<>();
        res=Long.MAX_VALUE;
        for(int i=0;i<n;i++) {
        	int v=fr.nextInt();
        	if(v==1)
        		oc.add(i);
        	else
        		em.add(i);
        }
        Collections.sort(oc);
        Collections.sort(em);
        long dp[][]=new long[5001][5001];
        for(int i=0;i<dp.length;i++) {
        	for(int j=0;j<dp[i].length;j++) {
        		dp[i][j]=-1;
        	}
        }
        System.out.println(getMin(oc,em,0,0,dp));
        
        
		
    }
	public static long getMin(ArrayList<Integer> oc,ArrayList<Integer> em,int idx,int j,long dp[][]) {
	   if(idx==oc.size())
		   return 0;
		long available=em.size()-j;
		long req=oc.size()-idx;
		if(available<req)
			return Integer.MAX_VALUE;
		if(dp[idx][j]!=-1)
			return dp[idx][j];
		
		long ch1=getMin(oc,em,idx,j+1,dp);
		long ch2=getMin(oc,em,idx+1,j+1,dp)+Math.abs(em.get(j)-oc.get(idx));
		
		return dp[idx][j]=Math.min(ch1,ch2);
		
	}
	
		
	
	 
public static String lcs(String a,String b) {
		int dp[][]=new int[a.length()+1][b.length()+1];
		for(int i=0;i<=a.length();i++)
		{
			for(int j=0;j<=b.length();j++)
			{
				if(i==0||j==0)
					dp[i][j]=0;
			}
		}
		
		for(int i=1;i<=a.length();i++)
		{
			for(int j=1;j<=b.length();j++)
			{
				if(a.charAt(i-1)==b.charAt(j-1))
					dp[i][j]=1+dp[i-1][j-1];
				else
					dp[i][j]=Math.max(dp[i-1][j],dp[i][j-1]);
			}
		}
		
		int i=a.length();
		int j=b.length();
		String lcs="";
		while(i>0&&j>0)
		{
			if(a.charAt(i-1)==b.charAt(j-1)) {
				lcs=a.charAt(i-1)+lcs;
				i--;
				j--;
			}
			else 
			{
				if(dp[i-1][j]>dp[i][j-1])
					i--;
				else
					j--;
			}
		}
		
		return lcs;
            
	}
	
	 
	 
	 
	 public static long facto(long n) {
		 if(n==1||n==0)
			 return 1;
		 return n*facto(n-1);
	 }
	 public static long gcd(long n1,long n2) {
		 if (n2 == 0) {
		        return n1;
		    }
		    return gcd(n2, n1 % n2);
	}
	 public static boolean isPali(String s) {
		 int i=0;
		 int j=s.length()-1;
		 while(i<=j) {
			 if(s.charAt(i)!=s.charAt(j))
				 return false;
			 i++;
			 j--;
		 }
		 return true;
	 }
	 public static String reverse(String s) {
		 String res="";
		 for(int i=0;i<s.length();i++) {
			 res+=s.charAt(i);
		 }
		 return res;
	 }
	
		
	 
	 public static int bsearch(long suf[],long val) {
		 int i=0;
		 int j=suf.length-1;
		 while(i<=j) {
			 int mid=(i+j)/2;
			 if(suf[mid]==val)
				 return mid;
			 else if(suf[mid]<val)
				 j=mid-1;
			 else
				 i=mid+1;
		 }
		 return -1;
	 }
	 
	 
	 
	
	 
	 
	public static int[] getFreq(String s) {
		int a[]=new int[26];
		for(int i=0;i<s.length();i++) {
			a[s.charAt(i)-'a']++;
		}
		return a;
		
	}
	 
	 public static boolean isPrime(int n) {
		 
		 for(int i=2;(i*i)<=n;i++) {
			 if(n%i==0)
				 return false;
		 }
		 return true;
	 }
            	  
             
             
    }
class Pair{
	long i;
	long j;
	
	Pair(long num,long freq){
		this.i=num;
		
		this.j=freq;
	}
	
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
            catch (IOException e)
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

