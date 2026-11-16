
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;

public class C {
    private static FastReader fr = new FastReader();
    private static PrintWriter out=new PrintWriter(System.out);
    private static Random random = new Random();

    private static long[][] dp;

    private static long calculate(List<Integer>[] graph, int current, long[][] r, boolean[] stack, int use){
        if(dp[current][use] != -1) return dp[current][use];
        stack[current] = true;
        long max = 0;
        if(graph[current] != null){
            for(int next : graph[current]){
                if(!stack[next]){
                    stack[next] = true;
                    long r1 = Math.abs(r[current][use] - r[next][0]) + calculate(graph, next, r, stack, 0);
                    long r2 = Math.abs(r[current][use] - r[next][1]) + calculate(graph, next, r, stack, 1);
                    max += Math.max(r1, r2);
                }
            }
        }
        stack[current] = false;
        dp[current][use] = max;
        return max;
    }

    public static void main(String[] args) throws IOException {
        StringBuilder sb = new StringBuilder();
        // code goes here
        int t = fr.nextInt();
        while (t-- > 0){
            int n = fr.nextInt();
            long[][] r = new long[n][2];
            for(int i = 0; i < n; i++){
                r[i] = fr.nextLongArray(2);
            }
            List<Integer>[] graph = new ArrayList[n];
            for(int i = 0; i < n - 1; i++){
                int u = fr.nextInt();
                int v = fr.nextInt();
                if(graph[u - 1] == null) graph[u - 1] = new ArrayList<>();
                if(graph[v - 1] == null) graph[v - 1] = new ArrayList<>();
                graph[u - 1].add(v - 1);
                graph[v - 1].add(u - 1);
            }

            boolean[] stack = new boolean[n];
            dp = new long[n][2];
            for(int i = 0; i < dp.length; i++){
                Arrays.fill(dp[i], -1);
            }

            long r1 = calculate(graph, 0, r, stack, 0);
            long r2 = calculate(graph, 0, r, stack, 1);
            sb.append(Math.max(r1, r2)).append("\n");
        }
        System.out.print(sb.toString());
    }

    static void ruffleSort(int[] a) {
        int n=a.length;//shuffle, then sort
        for (int i=0; i<n; i++) {
            int oi=random.nextInt(n), temp=a[oi];
            a[oi]=a[i]; a[i]=temp;
        }
        Arrays.sort(a);
    }

    static class FastReader{
        BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st=new StringTokenizer("");

        public String next() {
            while (!st.hasMoreTokens())
                try {
                    st=new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            return st.nextToken();
        }

        public int nextInt() {
            return Integer.parseInt(next());
        }

        public int[] nextIntArray(int n) {
            int[] a=new int[n];
            for (int i=0; i<n; i++) a[i]=nextInt();
            return a;
        }

        public long nextLong() {
            return Long.parseLong(next());
        }

        public long[] nextLongArray(int n) {
            long[] a=new long[n];
            for (int i=0; i<n; i++) a[i]=nextLong();
            return a;
        }
    }

    static class Pair<A, B>{
        A first;
        B second;
        public Pair(A first, B second){
            this.first = first;
            this.second = second;
        }
    }

    static long mod(String num, long a)
    {
        // Initialize result
        long res = 0;

        // One by one process all digits of 'num'
        for (int i = 0; i < num.length(); i++)
            res = (res*10 +  num.charAt(i) - '0') %a;

        return res;
    }

    static long binomialCoeff(long n, long k, long MOD)
    {
        long res = 1;

        // Since C(n, k) = C(n, n-k)
        if (k > n - k)
            k = n - k;

        // Calculate value of
        // [n * (n-1) *---* (n-k+1)] / [k * (k-1) *----* 1]
        for (int i = 0; i < k; ++i) {
            res *= (n - i);
            res /= (i + 1);
            res %= MOD;
        }

        return res;
    }

    static long power(long x, long y, long p)
    {

        // Initialize result
        long res = 1;

        // Update x if it is more than or
        // equal to p
        x = x % p;

        while (y > 0) {

            // If y is odd, multiply x
            // with result
            if (y % 2 == 1)
                res = (res * x) % p;

            // y must be even now
            y = y >> 1; // y = y/2
            x = (x * x) % p;
        }

        return res;
    }

    // Returns n^(-1) mod p
    static long modInverse(long n, long p)
    {
        return power(n, p - 2, p);
    }

    // Returns nCr % p using Fermat's
    // little theorem.
    static long nCrModPFermat(int n, int r,
                              long p)
    {

        // Base case
        if (r == 0)
            return 1;

        // Fill factorial array so that we
        // can find all factorial of r, n
        // and n-r
        long[] fac = new long[n + 1];
        fac[0] = 1;

        for (int i = 1; i <= n; i++)
            fac[i] = fac[i - 1] * i % p;

        return (fac[n] * modInverse(fac[r], p)
                % p * modInverse(fac[n - r], p)
                % p)
                % p;
    }
}
