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
            String a=input.next();
            String b=input.next();
            int same1=0,same0=0,opp1=0,opp0=0;
            for(int i=0;i<n;i++)
            {
                if(a.charAt(i)==b.charAt(i))
                {
                    if(a.charAt(i)=='1') same1++;
                    else same0++;
                }
                else
                {
                    if(a.charAt(i)=='1') opp1++;
                    else opp0++;
                }
            }
            if(same0+same1==n)
            {
                out.println(0);
            }
            else
            {
                int x=same1+opp1;
                int y=same1+opp0;
                int z=same0+opp0;
                if(x==y || (z+1)==y)
                {
                    int min=Integer.MAX_VALUE;
                    if((same0+same1)%2!=0 && same0==(same0+same1)/2)
                    {
                        min=Math.min(min,same0+same1);
                    }
                    if((opp0+opp1)%2==0 && opp0==(opp0+opp1)/2)
                    {
                        min=Math.min(min,opp0+opp1);
                    }
                    out.println(min);
                }
                else
                {
                    out.println(-1);
                }
            }
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