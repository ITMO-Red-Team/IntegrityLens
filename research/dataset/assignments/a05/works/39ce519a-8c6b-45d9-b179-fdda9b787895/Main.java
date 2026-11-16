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
    //static final long mod=(long)1e9+7;
    static final long mod=998244353L;
    public static long pow(long a,long p)
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
        int u,v;
        Pair(int u,int v)
        {
            this.u=u;
            this.v=v;
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
    static long gcd(long a,long b)
    {
        if(b%a==0)
        return a;
        return gcd(b%a,a);
    }
    public static class comp implements Comparator<Pair>
    {
        public int compare(Pair p1,Pair p2)
        {
            if(p1.v>p2.v)
            return 1;
            return -1;
        }
    }
    public static void upd1(int a[],int i)
    {
        for(;i<a.length;i+=(i&(-i)))
        a[i]+=1;
    }
    public static void upd2(long a[],int up)
    {
        for(int i=up;i<a.length;i+=(i&(-i)))
        a[i]=a[i]+1L*up;
    }
    public static int q1(int a[],int i)
    {
        int s=0;
        for(;i>0;i-=(i&(-i)))
        s+=a[i];
        return s;
    }
    public static long q2(long a[],int i)
    {
        long s=0;
        for(;i>0;i-=(i&(-i)))
        s+=a[i];
        return s;
    }
    public static void main(String args[])throws Exception
    {
        FastReader fs=new FastReader();
        PrintWriter pw=new PrintWriter(System.out);
        int tc=fs.nextInt();
        while(tc-->0)
        {
            int n=fs.nextInt();
            char[][] c=new char[n][];
            for(int i=0;i<n;i++)
            c[i]=fs.nextLine().toCharArray();
            ArrayList<Integer> list[]=new ArrayList[5];
            for(int i=0;i<5;i++)
            list[i]=new ArrayList<>();
            for(int i=0;i<n;i++)
            {
                int cnt[]=new int[5];
                for(int j=0;j<c[i].length;j++)
                cnt[c[i][j]-'a']++;
                for(int j=0;j<5;j++)
                list[j].add(2*cnt[j]-c[i].length);
            }
            for(int i=0;i<5;i++)
            Collections.sort(list[i]);
            int ans=0;
            for(int i=0;i<5;i++)
            {
                int s=0;
                for(int j=n-1;j>=0;j--)
                {
                    ans=Math.max(ans,n-1-j);
                    s+=list[i].get(j);
                    if(s<=0)
                    break;
                }
                if(s>0)
                ans=n;
            }
            pw.println(ans);
        }
        pw.flush();
        pw.close();
    }
}