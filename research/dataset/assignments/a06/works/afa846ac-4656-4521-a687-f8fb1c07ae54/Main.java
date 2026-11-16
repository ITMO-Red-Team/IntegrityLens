/* package codechef; // don't place package name! */

import java.util.*;
import java.lang.*;
import java.io.*;

/* Name of the class has to be "Main" only if the class is public. */
public class Main
{
	public static void main (String[] args) throws java.lang.Exception
	{
	//  StringBuilder sb=new StringBuilder();
	
//	BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
	Scanner sc=new Scanner(System.in);
//	int t=Integer.parseInt(bf.readLine());
    int t=sc.nextInt();
	while(t-->0)
	{
	 //  ArrayList<Integer> abcd=new ArrayList<>();
	 //  HashMap<Integer,Integer> amd=new HashMap<>();
	 
	 
	 
	   //   int n=Integer.parseInt(bf.readLine());
	   //   int a[]=new int[n];
	   //   String s[]=bf.readLine().split(" ");
	   //   for(int i=0;i<n;i++)
	   //   {
	   //       a[i]=Integer.parseInt(s[i]);
	   //   }
	   
	      int n=sc.nextInt();
	      long a[]=new long[n];
	      for(int i=0;i<n;i++)
	      {
	          a[i]=sc.nextLong();
	      }
	      
	      long ans=Long.MAX_VALUE;
	      int count1=1;
	      int count2=1;
	      boolean san=true;
	      long sum1=a[0];
	      long sum2=a[1];
	      long min1=a[0];
	      long min2=a[1];
	      ans=((n*1L*sum1)+(n*1L*sum2));
	     // System.out.println("san "+ans);
	      for(int i=2;i<n;i++)
	      {
	          if(san)
	          {
	              count1++;
	              san=false;
	              sum1+=a[i];
	              min1=Math.min(min1,a[i]);
	              long z1=sum1+(n-count1)*min1;
	              long z2= sum2+(n-count2)*min2;
	              ans=Math.min(ans,z1+z2);
	              
	          }
	          else
	          {
	              count2++;
	              san=true;
	              sum2+=a[i];
	              min2=Math.min(min2,a[i]);
	              long z1=sum1+(n-count1)*min1;
	              long z2= sum2+(n-count2)*min2;
	              ans=Math.min(ans,z1+z2);
	              
	              
	          }
	       //   System.out.println(i+" "+ans);
	      }
	      System.out.println(ans);
	      
	      
	      
	      
	    
	}
	
	}
	
	static class Data
	{
	    int first;
	    int sec;
	    Data(int z1,int z2)
	    {
	        first=z1;
	        sec=z2;
	    }
	}
	static class MINasc implements Comparator<Data>
	{
	    public int compare (Data z1,Data z2)
	    {
	        if(z1.first>z2.first)
	        return 1;
	        else if(z1.first<z2.first)
	        return -1;
	        else 
	        return 0;
	    }
	}
	static class MINdsc implements Comparator<Data>
	{
	    public int compare (Data z1,Data z2)
	    {
	        if(z1.first>z2.first)
	        return -1;
	        else if(z1.first<z2.first)
	        return 1;
	        else 
	        return 0;
	    }
	}
	
	
}