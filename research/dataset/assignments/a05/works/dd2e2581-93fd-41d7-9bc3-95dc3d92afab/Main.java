

import java.io.*;
import java.util.*;
public class Main
{
	public static void main(String[] args)throws Exception
	{
	    Main ob=new Main();
	    ob.fun();
	}
	public void fun()throws Exception
	{
	    BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
	    PrintWriter pw=new PrintWriter(System.out);
	    int t=Integer.parseInt(br.readLine());
	    while(t-->0)
	    {
	        int n=Integer.parseInt(br.readLine());
	        int ar[][]=new int [n][5];
	        int len[]=new int[n];
	        for(int i=0;i<n;i++)
	        {
	            String s=(br.readLine());
	            for(int j=0;j<s.length();j++)
	            {
	                ar[i][s.charAt(j)-'a']++;
	                len[i]=s.length();
	            }
	        }
	        int max=0;
	        for(int i=0;i<5;i++)
	        {
	            int num=fun2(ar,len,i);
	            max=Math.max(num,max);
	        }
	        pw.println(max);
	        
	    }
	    pw.flush();
	}
	public int fun2(int ar[][],int len[],int col)
	{
	    int ct=0;
	    int n=ar.length;
	    PriorityQueue<Integer> pq=new PriorityQueue<Integer>(Collections.reverseOrder());
	    for(int i=0;i<n;i++)
	    {
	        int dif=2*ar[i][col]-len[i];
	        pq.add(dif);
	    }
	    int sum=0;
	    while(pq.size()>0)
	    {
	        int num=(int)(pq.poll());
	        if((sum+num)>0)
	        {
	            ct++;
	            sum+=num;
	        }
	    }
	    return ct;
	}
}
