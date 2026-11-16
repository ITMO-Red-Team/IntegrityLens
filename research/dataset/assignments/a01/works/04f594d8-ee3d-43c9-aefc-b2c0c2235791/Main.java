import java.io.*;
import java.util.*;
public class ParsasHumongousTree
{
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
    static class Output 
    {
        private final PrintWriter writer;
        
        public Output(OutputStream outputStream) 
        {
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream)));
        }
        
        public Output(Writer writer) 
        {
            this.writer = new PrintWriter(writer);
        }
        
        public void print(Object...objects) 
        {
            for (int i = 0; i < objects.length; i++) 
            {
                if (i != 0)
                    writer.print(' ');
                writer.print(objects[i]);
            }
        }
        
        public void printLine(Object...objects) 
        {
            print(objects);
            writer.println();
        }
        
        public void close() 
        {
            writer.close();
        }
        
        public void flush() {
            writer.flush();
        }
    }
    static class Pair
    {
    	int a;
    	int b;
    	public Pair(int a, int b)
    	{
    		this.a=a;
    		this.b=b;
    	}
    }
    static HashMap<Integer,List<Integer>> edge=new HashMap();
    static Pair a[];
    static long dp[][];
    static void dfs(int node, int parent)
    {
        for(int i:edge.get(node))
        {
            if(i!=parent)
            {
                dfs(i,node);
                dp[node][0]+=Math.max(Math.abs(a[node].a-a[i].a)+dp[i][0],Math.abs(a[node].a-a[i].b)+dp[i][1]);
                dp[node][1]+=Math.max(Math.abs(a[node].b-a[i].a)+dp[i][0],Math.abs(a[node].b-a[i].b)+dp[i][1]);
            }
        }
    }
    public static void main(String args[])
    {
        FastReader sc=new FastReader();
        Output out=new Output(System.out);
        int t=sc.nextInt();
        while(t-->0)
        {
            int n=sc.nextInt();
            a=new Pair[n+1];
            edge.clear();
            for(int i=1;i<n+1;i++)
            {
                int l=sc.nextInt();
                int r=sc.nextInt();
                Pair p=new Pair(l,r);
                a[i]=p;
            }
            for(int i=0;i<n-1;i++)
            {
                int u=sc.nextInt();
                int v=sc.nextInt();
                if(edge.containsKey(u))
                {
                    List<Integer> list=edge.get(u);
                    list.add(v);
                    edge.put(u,list);
                }
                else
                {
                    List<Integer> list=new ArrayList();
                    list.add(v);
                    edge.put(u,list);
                }
                if(edge.containsKey(v))
                {
                    List<Integer> list=edge.get(v);
                    list.add(u);
                    edge.put(v,list);
                }
                else
                {
                    List<Integer> list=new ArrayList();
                    list.add(u);
                    edge.put(v,list);
                }
            }
            dp=new long[n+1][2];
            dfs(1,0);
            out.printLine(((long)Math.max(dp[1][0],dp[1][1])));
            out.flush();
        }
    }
}