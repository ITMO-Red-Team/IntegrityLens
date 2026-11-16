import java.util.*;
import java.io.*;
public class Solution
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
    static final long mod=(long)1e9+7;
    public static long pow(long a,int p)
    {
        long res=1;
        while(p>0)
        {
            if(p%2==1)
            {
                p--;
                res*=a;
                res%=mod;
            }
            else
            {
                a*=a;
                a%=mod;
                p/=2;
            }
        }
        return res;
    }
    static class Pair
    {
        int u,v,w;
        Pair(int u,int v,int w)
        {
            this.u=u;
            this.v=v;
            this.w=w;
        }
    }
    /*static class Pair implements Comparable<Pair>
    {
        int v,l;
        Pair(int v,int l)
        {
            this.v=v;
            this.l=l;
        }
        public int compareTo(Pair p)
        {
            return l-p.l; 
        }
    }*/
    static int gcd(int a,int b)
    {
        if(b%a==0)
        return a;
        return gcd(b%a,a);
    }
    public static void dfs(int u,int dist[],int sub[],int mxv[],int par[],ArrayList<Integer> edge[])
    {
        sub[u]=1;
        for(int v:edge[u])
        {
            if(dist[v]==-1)
            {
                par[v]=u;
                dist[v]=dist[u]+1;
                dfs(v,dist,sub,mxv,par,edge);
                if(sub[v]+1>sub[u])
                {
                    sub[u]=sub[v]+1;
                    mxv[u]=v;
                }
            }
        }
    }
    public static void main(String args[])throws Exception
    {
        FastReader fs=new FastReader();
        PrintWriter pw=new PrintWriter(System.out);
        //int tc=fs.nextInt();
        int n=fs.nextInt();
        int a[]=new int[n];
        for(int i=0;i<n;i++)
        a[i]=fs.nextInt();
        ArrayList<Integer> o=new ArrayList<>();
        ArrayList<Integer> z=new ArrayList<>();
        for(int i=0;i<n;i++)
        {
            if(a[i]==1)o.add(i);
            else z.add(i);
        }
        int ans[][]=new int[o.size()+1][z.size()+1];
        for(int i=1;i<=o.size();i++)
        {
            for(int j=i;j<=z.size();j++)
            {
                if(i==j)ans[i][j]=ans[i-1][j-1]+(int)Math.abs(o.get(i-1)-z.get(j-1));
                else 
                ans[i][j]=Math.min(ans[i][j-1],ans[i-1][j-1]+(int)Math.abs(o.get(i-1)-z.get(j-1)));
            }
        }
        pw.println(ans[o.size()][z.size()]);
        pw.flush();
        pw.close();
    }
}