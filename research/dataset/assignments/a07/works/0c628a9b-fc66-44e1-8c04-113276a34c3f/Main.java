import java.util.*;
// import java.lang.*;
import java.io.*;

//           THIS TEMPLATE MADE BY AKSH BANSAL.

public class Solution {
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;

        public FastReader() {
            br = new BufferedReader(new InputStreamReader(System.in));
        }

        String next() {
            while (st == null || !st.hasMoreElements()) {
                try {
                    st = new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }

        int nextInt() {
            return Integer.parseInt(next());
        }

        long nextLong() {
            return Long.parseLong(next());
        }

        double nextDouble() {
            return Double.parseDouble(next());
        }

        String nextLine() {
            String str = "";
            try {
                str = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return str;
        }
    }
    private static boolean[] isPrime;
    private static void primes(){
        int num = (int)1e6; // PRIMES FROM 1 TO NUM
        isPrime = new boolean[num];
     
        for (int i = 2; i< isPrime.length; i++) {
           isPrime[i] = true;
        }
        for (int i = 2; i< Math.sqrt(num); i++) {
           if(isPrime[i] == true) {
              for(int j = (i*i); j<num; j = j+i) {
                 isPrime[j] = false;
              }
           }
        }
    }
    private static long gcd(long a, long b){
        if(b==0)return a;
        return gcd(b,a%b);
    }
    private static long pow(long x,long y){
        if(y==0)return 1;
        long temp = pow(x, y/2);
        if(y%2==1){
            return x*temp*temp;
        }
        else{
            return temp*temp;
        }
    }
    // static ArrayList<Integer>[] adj;
    // static void getAdj(int n,int q, FastReader sc){
    //     adj = new ArrayList[n+1];
    //     for(int i=1;i<=n;i++){
    //         adj[i] = new ArrayList<>();
    //     }
    //     for(int i=0;i<q;i++){
    //         int a = sc.nextInt();
    //         int b = sc.nextInt();

    //         adj[a].add(b);
    //         adj[b].add(a);
    //     }
    // }
    public static void main(String[] args) throws IOException {
        FastReader sc = new FastReader();
        PrintWriter out = new PrintWriter(System.out);
        // primes();
        // ________________________________

        // int t = sc.nextInt();
        // StringBuilder output = new StringBuilder();

        // while (t-- > 0) {

        //     output.append(solver()).append("\n");
        // }

        // out.println(output);
        // _______________________________

        int n = sc.nextInt();
        int arr[] = new int[n];
        for(int i=0;i<n;i++){
            arr[i] = sc.nextInt();
        }
        out.println(solver(n, arr));
        // ________________________________
        out.flush();
    }

    public static long solver(int n, int[] arr) {
        ArrayList<Integer> a = new ArrayList<>();
        ArrayList<Integer> b = new ArrayList<>();
        for(int i=0;i<n;i++){
            if(arr[i] ==1){
                a.add(i);
            }
            else{
                b.add(i);
            }
        }
        // System.out.println("__"+ a);
        // System.out.println("__"+ b);
        long inf = (long)1e10;
        int aLen = a.size(), bLen = b.size();
        long[][] dp = new long[bLen+1][aLen+1];
        for(int i=0;i<bLen+1;i++)Arrays.fill(dp[i],inf);
        // dp[0][0] = 0;
        for(int i=0;i<=bLen;i++){
            dp[i][0] = 0;
        }
        for(int i=1;i<=bLen;i++){
            for(int j=1;j<=i && j<=aLen;j++){
                int aa = a.get(j-1);
                int bb = b.get(i-1);
                // System.out.println((i-1)+" "+(j-1)+"__"+ aa+" "+bb);
                dp[i][j] = Math.min(
                    Math.abs(aa-bb)+dp[i-1][j-1],
                    dp[i-1][j]
                );
                // System.out.println((i-1)+" "+(j-1)+"__"+ dp[i][j]);
            }
        }
        // for(int i=0;i<=bLen;i++){
        //     for(int j=0;j<=aLen;j++){
        //         System.out.print(dp[i][j]+" ");
        //     }
        //     System.out.println("__" );
        // }
        return dp[bLen][aLen]==inf?0:dp[bLen][aLen];
    }
}