import java.io.*;
import java.util.*;
public class Main {
    public static void main(String args[])
    {
        FastReader input=new FastReader();
        PrintWriter out=new PrintWriter(System.out);
        int T=input.nextInt();
        while(T-->0)
        {
            int n=input.nextInt();
            int k[]=new int[n];
            int h[]=new int[n];
            for(int i=0;i<n;i++)
            {
                k[i]=input.nextInt();
            }
            for(int i=0;i<n;i++)
            {
                h[i]=input.nextInt();
            }
            ArrayList<int []> list=new ArrayList<>();
            for(int i=0;i<n;i++)
            {
                int p=k[i]-h[i]+1;
                if(list.size()==0) list.add(new int[]{p,k[i]});
                else
                {
                    while(list.size()>0)
                    {
                        int j=list.size()-1;
                        int a[]=list.get(j);
                        int l=a[0],r=a[1];
                        int l1=p,r1=k[i];
                        if(l1>r)
                        {
                            list.add(new int[]{l1,r1});
                            break;
                        }
                        else if(l1>=l && l1<=r)
                        {
                            list.remove(j);
                            list.add(new int[]{l,r1});
                            break;
                        }
                        else
                        {
                            list.remove(j);
                        }
                    }
                    if(list.size()==0) list.add(new int[]{p,k[i]});
                }
            }
            long sum=0;
            for(int i=0;i<list.size();i++)
            {
                long d=list.get(i)[1]-list.get(i)[0]+1;
                long val=d*(d+1)/2;
                sum+=val;
            }
            out.println(sum);
        }
        out.close();
    }
    static class FastReader
    {
        BufferedReader br;
        StringTokenizer st;
        public FastReader()
        {
            br = new BufferedReader(new InputStreamReader(System.in));
        }
        String next()
        {
            while (st == null || !st.hasMoreElements())
            {
                try
                {
                    st = new StringTokenizer(br.readLine());
                }
                catch (IOException e)
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
            String str="";
            try
            {
                str=br.readLine();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return str;
        }
    }
}