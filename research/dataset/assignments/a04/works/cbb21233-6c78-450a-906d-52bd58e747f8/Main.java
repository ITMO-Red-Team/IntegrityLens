import java.util.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.StringTokenizer;



public class Test{

    static class Pair implements Comparable<Pair>{
    int wt;
    int idx;
    Pair(int x,int y){
        this.wt=x;
        this.idx=y;
    }
    @Override
    public int compareTo(Pair x){
        return this.wt-x.wt;
    }

    public String toString(){
        return "("+wt+" "+idx+")";
    }
}   



    public static void main (String[] args) throws java.lang.Exception{
        FastReader scan=new FastReader();
        int t=scan.nextInt();
        while(t-->0){
            int n=scan.nextInt();
            int m=scan.nextInt();
            int x=scan.nextInt();
            int[]arr=new int[n];
            for(int i=0;i<n;i++){
                arr[i]=scan.nextInt();
            }
            Queue<Pair>q=new PriorityQueue<>();
            for(int i=1;i<=m;i++){
                q.add(new Pair(0,i));
            }
            System.out.println("YES");
            for(int i=0;i<n;i++){
                Pair temp=q.poll();
                int wt=temp.wt;
                int idx=temp.idx;
                System.out.print(idx+" ");
                //System.out.println(temp);
                q.add(new Pair(wt+arr[i],idx));
            }
            System.out.println();


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
 
        double nextDouble() { return Double.parseDouble(next()); }
 
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
}

