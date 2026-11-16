import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;


public class Main {
    static int i, j, k, n, m, t, y, x, sum = 0;
    static long mod = 1000000007;
    static FastScanner fs = new FastScanner();
    static PrintWriter out = new PrintWriter(System.out);
    static String str;
    static long ans;

    static List<Integer> zeros = new ArrayList<>();
    static List<Integer> ones = new ArrayList<>();
    static int oneCount = 0;
    static int[][] dp = new int[5005][5005];

    public static void main(String[] args) {
        t = 1;

        while (t-- > 0) {

            n = fs.nextInt();

            for(int i = 0;i<n;i++){
                x = fs.nextInt();
                if(x==1){
                    ones.add(i);
                    oneCount++;
                }
                else
                    zeros.add(i);
            }

            for(int i=0;i<n;i++){
                for(int j = 0; j<n;j++){
                    dp[i][j]=-1;
                }
            }

            out.println(minCost(0,0));
        }

        out.close();
    }

    static int minCost(int zIndex, int oIndex){

        if(oIndex == ones.size())
            return 0;
        if(zIndex == zeros.size())
            return 1000000007;

        if(dp[zIndex][oIndex]==-1)
            dp[zIndex][oIndex]= Math.min(Math.abs(zeros.get(zIndex) - ones.get(oIndex))+minCost(zIndex+1, oIndex+1) , minCost(zIndex+1, oIndex));

        return dp[zIndex][oIndex];
    }


    static int gcd(int a, int b)
    {
        if (b == 0)
            return a;
        return gcd(b, a % b);
    }

    static void ruffleSort(int[] a) {
        //ruffle
        int n = a.length;
        Random r = new Random();
        for (int i = 0; i < a.length; i++) {
            int oi = r.nextInt(n), temp = a[i];
            a[i] = a[oi];
            a[oi] = temp;
        }

        //then sort
        Arrays.sort(a);
    }

    static void ruffleSort(long[] a) {
        //ruffle
        int n = a.length;
        Random r = new Random();
        for (int i = 0; i < a.length; i++) {
            int oi = r.nextInt(n);
            long temp = a[i];
            a[i] = a[oi];
            a[oi] = temp;
        }

        //then sort
        Arrays.sort(a);
    }

    static class FastScanner {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer("");

        String next() {
            while (!st.hasMoreTokens())
                try {
                    st = new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            return st.nextToken();
        }

        int nextInt() {
            return Integer.parseInt(next());
        }

        long nextLong() {
            return Long.parseLong(next());
        }
    }

    static class Pair implements Comparable<Pair> {
        long first, second;

        public Pair(long first, long second) {
            this.first = first;
            this.second = second;

        }

        @Override
        public int compareTo(Pair o) {
            return Long.compare(first, o.first);
        }
    }


}