//package Practice;
import java.io.*;
import java.util.*;


public class codefor
{
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
	public static void main(String[] args) 
	{
		FastReader sc=new FastReader();
		int T=sc.nextInt();
		while(T-->0)
		{
			int n=sc.nextInt(),k=sc.nextInt(),i=0;
			long t[]=new long[n];
			int a[]=new int[k];
			for(i=0;i<k;i++)
				a[i]=sc.nextInt()-1;
			for(i=0;i<k;i++)
				t[a[i]]=sc.nextLong();
			long ans[]=new long[n];
			PriorityQueue<Long> pq=new PriorityQueue<>();
			for(i=0;i<n;i++)
			{
				if(t[i]!=0)
					pq.add(t[i]-i);
				if(pq.size()!=0)
				{
					ans[i]=pq.peek()+i;
				}
			}
			pq.clear();
			for(i=n-1;i>=0;i--)
			{
				if(t[i]!=0)
					pq.add(t[i]+i);
				if(pq.size()!=0)
				{
					long val=pq.peek()-i;
					if(ans[i]==0)
						ans[i]=val;
					else 
						ans[i]=Math.min(val, ans[i]);
				}
				}
			pq.clear();
			for(i=0;i<n;i++)
				System.out.print(ans[i]+" ");
			System.out.println();
		}
	}

}











