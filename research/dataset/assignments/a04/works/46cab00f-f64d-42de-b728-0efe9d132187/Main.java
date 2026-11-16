import java.util.*;
import java.lang.*;
import java.io.*;

public class codeforces
{
    public static class FastReader {  
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
	public static void main (String[] args) throws java.lang.Exception
	{
		// your code goes here
		FastReader sc=new FastReader();
		int t=sc.nextInt();
		while(t-->0)
		{
		    int n=sc.nextInt();
		    int m=sc.nextInt();
		    int x=sc.nextInt();
		    PriorityQueue<pair> pq=new PriorityQueue<>(new myComp());
		    myarr a[]=new myarr[n];
		    int b[]=new int[n];
		  //  int c[]=new int[m];
		    for(int i=0;i<n;i++)
		    {
		        a[i]=new myarr(sc.nextInt(),i);
		    }
		    Arrays.sort(a,new mycom());
		    for(int i=0;i<m;i++)
		    {
		        pq.add(new pair(a[i].val,i+1));
		      //  c[i]=a[i].val;
		        b[a[i].ii]=i+1;
		    }
    		StringBuffer sb=new StringBuffer();
		    boolean flag=false;
		    if(m!=1)
		    {
    		    for(int i=m;i<n;i++)
    		    {
    		        pair p=pq.poll();
    		        int g=p.b;
    		        int h=pq.peek().b;
    		        if(Math.abs((g+a[i].val)-h)<=x)
    		        {
    		            b[a[i].ii]=p.t;
    		            pq.add(new pair(g+a[i].val,p.t));
    		        }
    		        else
    		        {
    		            flag=true;
    		            break;
    		        }
    		    }
    		    if(flag)
    		    System.out.println("NO");
    		    else
    		    {
    		        System.out.println("YES");
    		        for(int i=0;i<n;i++)
    		        {
    		            sb.append(b[i]+" ");
    		        }
    		        System.out.println(sb.toString());
    		    }
		    }
		    else
		    {
    		    System.out.println("YES");
    		    for(int i=0;i<n;i++)
    		    {
    		        sb.append("1 ");
    		    }
    		    System.out.println(sb.toString());
		    }
		}
	}
	static class pair
	{
	    int b;
	    int t;
	    pair(int b,int t)
	    {
	        this.b=b;
	        this.t=t;
	    }
	}
	static class myComp implements Comparator<pair>
	{
	    public int compare(pair p1,pair p2)
	    {
	        if(p1.b==p2.b)
	        return 0;
	        else if(p1.b<p2.b)
	        return -1;
	        else
	        return 1;
	    }
	}
	static class myarr
	{
	    int val,ii;
	    myarr(int val,int ii)
	    {
	        this.val=val;
	        this.ii=ii;
	    }
	}
	static class mycom implements Comparator<myarr>
	{
	    public int compare(myarr a,myarr b)
	    {
	        if(a.val==b.val)
	        return 0;
	        else if(a.val<b.val)
	        return -1;
	        else
	        return 1;
	    }
	}
}






