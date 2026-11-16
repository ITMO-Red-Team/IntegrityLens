/* package codechef; // don't place package name! */

import java.util.*;
import java.lang.*;
import java.io.*;

/* Name of the class has to be "Main" only if the class is public. */
public  class cses
{   
    static int mod=1000000007;
    static FastReader sc=new FastReader();
    public static void main (String[] args) throws java.lang.Exception
    {
        long startTime=System.currentTimeMillis();
        int t=sc.nextInt();
        for(int y=0;y<t;++y)
        {
           int n=sc.nextInt();
           int m=sc.nextInt();
           int x=sc.nextInt();
           int arr[]=arrayintinput(n);
           //PriorityQueue<Node> pq=new PriorityQueue<>();
           int index[]=new int[n];
           
           
           PriorityQueue<Node> pq =new PriorityQueue<Node>(new comp());           
           for(int i=0;i<m;++i)
           {
               pq.add(new Node(0,i+1));
           }
           for(int i=0;i<n;++i)
           {
            Node temp=pq.remove();
            temp.sum+=arr[i];
            index[i]=temp.build;
            pq.add(new Node(temp.sum,temp.build));
           }
           System.out.println("YES");
            for(int i=0;i<n;++i)
            {
                System.out.print(index[i]+" ");    
            }  
            System.out.println();
        }
    //  getExecutionTime(startTime);
    }
    

    static int[] arrayintinput(int n)
    {
        int arr[]=new int[n];
        for(int i=0;i<n;++i)
        {
            arr[i]=sc.nextInt();
        }
        return arr;
    }
    static class comp implements Comparator<Node>{ 
        public int compare(Node a,Node b)
        {
            return a.sum-b.sum;
        }
    }
   static class Node{
    int sum;
    int build;
    Node(int sum ,int build)
    {
    this.sum=sum;
    this.build=build;
    }
    }

    static class FastReader {
        BufferedReader br;
        StringTokenizer st;
 
        public FastReader()
        {
            br = new BufferedReader(
                new InputStreamReader(System.in));
        }
 
        String next()
        {
            while (st == null || !st.hasMoreElements()) {
                try {
                    st = new StringTokenizer(br.readLine());
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }
 
        int nextInt() { return Integer.parseInt(next()); }
 
        long nextLong() { return Long.parseLong(next()); }
 
        double nextDouble()
        {
            return Double.parseDouble(next());
        }
 
        String nextLine()
        {
            String str = "";
            try {
                str = br.readLine();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return str;
        }
    }
      
    
   static void getExecutionTime(long startTime)
   {
        final long endTime = System.currentTimeMillis();
        System.out.println();
        System.out.println("Time take is "+(endTime-startTime)+" ms");
   }
  

}
//  System.out.println("Case #"+(y+1)+": "+ans);

