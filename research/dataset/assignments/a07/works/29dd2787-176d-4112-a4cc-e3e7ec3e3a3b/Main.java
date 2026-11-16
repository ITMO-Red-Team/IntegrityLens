import java.io.*;
import java.util.*;
public class Main {
    public static void main(String args[])
    {
        FastReader input=new FastReader();
        PrintWriter out=new PrintWriter(System.out);
        int T=1;
        while(T-->0)
        {
            int n=input.nextInt();
            int a[]=new int[n];
            ArrayList<Integer> list=new ArrayList<>();
            ArrayList<Integer> space=new ArrayList<>();
            for(int i=0;i<n;i++)
            {
                a[i]=input.nextInt();
                if(a[i]==1)
                {
                    list.add(i);
                }
                else
                {
                    space.add(i);
                }
            }
            int pre[]=new int[space.size()];
            for(int i=0;i<list.size();i++)
            {
                if(i==0)
                {
                    int min=Integer.MAX_VALUE;
                    for(int j=0;j<space.size();j++)
                    {
                        pre[j]=Math.abs(list.get(i)-space.get(j));
                        min=Math.min(min,pre[j]);
                        pre[j]=min;
                    }
                }
                else
                {
                    int arr[]=new int[space.size()];
                    for(int j=0;j<i;j++)
                    {
                        arr[j]=Integer.MAX_VALUE;
                    }

                    int min=Integer.MAX_VALUE;
                    for(int j=i;j<space.size();j++)
                    {
                        int v=Math.abs(list.get(i)-space.get(j));
                        v+=pre[j-1];
                        arr[j]=v;
                        min=Math.min(min,v);
                        arr[j]=min;
                    }
                    for(int j=0;j<space.size();j++)
                    {
                        pre[j]=arr[j];
                    }
                }
            }
            out.println(pre[space.size()-1]);
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