import java.io.*;
import java.util.*;

public class Codeforces
{
    public static void main(String args[])throws Exception
    {
        BufferedReader bu=new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb=new StringBuilder();
        int t=Integer.parseInt(bu.readLine());
        while(t-->0)
        {
            bu.readLine();
            String s[]=bu.readLine().split(" ");
            int n=Integer.parseInt(s[0]),k=Integer.parseInt(s[1]);
            int a[]=new int[n],i,x,ac[]=new int[n],b[]=new int[k];
            Arrays.fill(a,Integer.MAX_VALUE);

            s=bu.readLine().split(" ");
            for(i=0;i<k;i++) b[i]=Integer.parseInt(s[i])-1;
            s=bu.readLine().split(" ");
            for(i=0;i<k;i++)
            {
                x=Integer.parseInt(s[i]);
                ac[b[i]]=x;
            }

            PriorityQueue<Integer> pq=new PriorityQueue<>();
            for(i=0;i<n;i++)
            {
                if(ac[i]!=0) pq.add(ac[i]-i);
                if(!pq.isEmpty()) a[i]=Math.min(a[i],pq.peek()+i);
            }
            pq=new PriorityQueue<>();
            for(i=n-1;i>=0;i--)
            {
                if(ac[i]!=0) pq.add(ac[i]+i);
                if(!pq.isEmpty()) a[i]=Math.min(a[i],pq.peek()-i);
            }

            for(i=0;i<n;i++) sb.append(a[i]+" ");
            sb.append("\n");
        }
        System.out.print(sb);
    }
}
