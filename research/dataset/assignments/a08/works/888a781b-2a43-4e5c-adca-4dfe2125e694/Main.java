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
            int n=Integer.parseInt(bu.readLine());
            int k[]=new int[n+1],h[]=new int[n+1],i;
            String s[]=bu.readLine().split(" ");
            for(i=1;i<=n;i++) k[i]=Integer.parseInt(s[i-1]);
            s=bu.readLine().split(" ");
            for(i=1;i<=n;i++) h[i]=Integer.parseInt(s[i-1]);

            long max[]=new long[n+1];
            for(i=1;i<=n;i++)
            {
                int j;
                long here=h[i];
                for(j=i-1;j>=0;j--)
                {
                    long diff=here-(k[i]-k[j]);
                    if(diff<=0) {max[i]=max[j]+here*(here+1)/2; break;}
                    if(diff<h[j]) here+=h[j]-diff;
                }
            }
            sb.append(max[n]+"\n");
        }
        System.out.print(sb);
    }
}