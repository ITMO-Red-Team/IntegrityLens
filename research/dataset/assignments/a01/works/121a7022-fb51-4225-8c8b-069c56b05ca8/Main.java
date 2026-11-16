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
            g=new ArrayList[n];
            int i;
            for(i=0;i<n;i++)
            {
                g[i]=new ArrayList<>();
                String st[]=bu.readLine().split(" ");
                a[i][0]=Integer.parseInt(st[0]); a[i][1]=Integer.parseInt(st[1]);
                s[i][0]=s[i][1]=0;
            }

            for(i=0;i<n-1;i++)
            {
                String st[]=bu.readLine().split(" ");
                int u=Integer.parseInt(st[0])-1,v=Integer.parseInt(st[1])-1;
                g[u].add(v); g[v].add(u);
            }

            dfs(0,-1);
            sb.append(Math.max(s[0][0],s[0][1])+"\n");
        }
        System.out.print(sb);
    }

    static ArrayList<Integer> g[];
    static int N=100000,a[][]=new int[N][2];
    static long s[][]=new long[N][2];
    static void dfs(int n,int p)
    {
        for(int x:g[n])
        if(x!=p)
        {
            dfs(x,n);
            s[n][0]+=Math.max(s[x][0]+Math.abs(a[x][0]-a[n][0]),s[x][1]+Math.abs(a[x][1]-a[n][0]));
            s[n][1]+=Math.max(s[x][0]+Math.abs(a[x][0]-a[n][1]),s[x][1]+Math.abs(a[x][1]-a[n][1]));
        }
    }
}
