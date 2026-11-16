import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;

public class TaskA {
    public static void main(String[] args) {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;
        FastReader in = new FastReader(inputStream);
        PrintWriter out = new PrintWriter(outputStream);
        Solver solver = new Solver();
        int testCount = 1;
        testCount = Integer.parseInt(in.next());
        for (int i = 1; i <= testCount; i++)
            solver.solve(i, in, out);
        out.close();
    }

    static class Solver {
        public void fastSort(int[] a){
            Random random = new Random();
            for(int i=0;i<a.length;i++){
                int nextInd = random.nextInt(a.length);
                int temp = a[nextInd];
                a[nextInd] = a[i];
                a[i] = temp;
            }
            Arrays.sort(a);
            return;
        }
        public Pair[] pairs;
        ArrayList<ArrayList<Integer>> adj;
        long[][] dp;
        public void dfs(int vertex,int parent){
//            dp[0][vertex] = dp[1][vertex] = 0;
            for(int i=0;i<adj.get(vertex).size();i++){
                if((int)adj.get(vertex).get(i)!=parent){
                    int curVer = adj.get(vertex).get(i);
                    dfs(curVer,vertex);
                    dp[0][vertex] += Math.max( (dp[0][curVer] + Math.abs(pairs[curVer].f - pairs[vertex].f) )
                            , (dp[1][curVer] + Math.abs(pairs[curVer].s - pairs[vertex].f)) );
                    dp[1][vertex] += Math.max( (dp[0][curVer] + Math.abs(pairs[curVer].f - pairs[vertex].s) )
                            , (dp[1][curVer] + Math.abs(pairs[curVer].s - pairs[vertex].s)) );
                }
            }
        }
        public void solve(int testNumber, FastReader in, PrintWriter out) {
            int n = in.nextInt();
            pairs = new Pair[n];
            adj = new ArrayList<>();
            for(int i=0;i<n;i++){
                pairs[i] = new Pair(in.nextInt(),in.nextInt());
                adj.add(new ArrayList<>());
            }
            for(int i=0;i<n-1;i++){
                int u = in.nextInt(),v = in.nextInt();
                adj.get(u-1).add(v-1);
                adj.get(v-1).add(u-1);
            }
            dp = new long[2][n];
            dfs(0,-1);
//            out.println(Arrays.toString(dp[0]));
//            out.println(Arrays.toString(dp[1]));
            out.println(Math.max(dp[0][0],dp[1][0]));
        }
    }
    static class Pair implements Comparable<Pair>{
        int f;
        int s;
        public Pair(int a,int b){
            this.f = a;
            this.s = b;
        }
        public  int compareTo(Pair other){
            if(other.f==this.f){
                return (int)(this.s-other.s);
            }
            else return this.f-other.f;
        }
    }
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;

        public FastReader(InputStream stream){
            br = new BufferedReader(new
                InputStreamReader(stream));
        }

        public String next(){
            while (st == null || !st.hasMoreElements()){
                try{
                    st = new StringTokenizer(br.readLine());
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }

        public int nextInt(){
            return Integer.parseInt(next());
        }

        public long nextLong(){
            return Long.parseLong(next());
        }

        public double nextDouble(){
            return Double.parseDouble(next());
        }

        public String nextLine(){
            String str = "";
            try{
                str = br.readLine();
            }
            catch (IOException e){
                e.printStackTrace();
            }
            return str;
        }

        public int[] nextArray(int n) {
            int[] a=new int[n];
            for (int i=0; i<n; i++) a[i]=nextInt();
                return a;
        }
    }
}

