import java.io.*;
import java.util.*;

public class Codeforces
{
    public static void main(String args[])throws Exception
    {
        BufferedReader bu=new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb=new StringBuilder();
        int n=Integer.parseInt(bu.readLine());
        String s[]=bu.readLine().split(" ");
        ArrayList<Integer> z=new ArrayList<>(),o=new ArrayList<>();
        long dp[][]=new long[n+1][n+1];
        int i,j,a;
        for(i=0;i<n;i++)
        {
            a=Integer.parseInt(s[i]);
            if(a==0) z.add(i);
            else o.add(i);
        }

        for(i=1;i<=o.size();i++)
        {
            long min=dp[i-1][i-1];
            for(j=i;j<=z.size();j++)
            {
                dp[i][j]=min+Math.abs(z.get(j-1)-o.get(i-1));
                min=Math.min(min,dp[i-1][j]);
            }
        }
        long ans=Long.MAX_VALUE;
        for(i=o.size();i<=z.size();i++)
        ans=Math.min(ans,dp[o.size()][i]);
        System.out.print(ans);
    }
}