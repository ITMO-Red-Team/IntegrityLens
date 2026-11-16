import java.io.*;
import java.util.*;


public class Solution{
    public static void main(String[] args) {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;
        InputReader in = new InputReader(inputStream);
        PrintWriter out = new PrintWriter(outputStream);
        Task solver = new Task();
        solver.solve(in, out);
        out.close();
    }
    // main solver
    static class Task{

        double eps= 0.00000001;
        static final int MAXN = 10000001;

        // stores smallest prime factor for every number
        static int spf[] = new int[MAXN];

        Map<Integer,Set<Integer>> dp= new HashMap<>();

        // Calculating SPF (Smallest Prime Factor) for every
        // number till MAXN.
        // Time Complexity : O(nloglogn)
        public void sieve()
        {
            spf[1] = 1;
            for (int i=2; i<MAXN; i++)

                // marking smallest prime factor for every
                // number to be itself.
                spf[i] = i;

            // separately marking spf for every even
            // number as 2
            for (int i=4; i<MAXN; i+=2)
                spf[i] = 2;

            for (int i=3; i*i<MAXN; i++)
            {
                // checking if i is prime
                if (spf[i] == i)
                {
                    // marking SPF for all numbers divisible by i
                    for (int j=i*i; j<MAXN; j+=i)

                        // marking spf[j] if it is not
                        // previously marked
                        if (spf[j]==j)
                            spf[j] = i;
                }
            }
        }

        // A O(log n) function returning primefactorization
        // by dividing by smallest prime factor at every step
        public Set<Integer> getFactorization(int x)
        {
            if(dp.containsKey(x)) return dp.get(x);
            Set<Integer> ret = new HashSet<>();
            while (x != 1)
            {
                if(spf[x]!=2) ret.add(spf[x]);
                x = x / spf[x];
            }
            dp.put(x,ret);
            return ret;
        }
        // function to find first index >= x
        public int lowerIndex(List<Integer> arr, int n, int x)
        {
            int l = 0, h = n - 1;
            while (l <= h)
            {
                int mid = (l + h) / 2;
                if (arr.get(mid) >= x)
                    h = mid - 1;
                else
                    l = mid + 1;
            }
            return l;
        }

        // function to find last index <= y
        public int upperIndex(List<Integer> arr, int n, int y)
        {
            int l = 0, h = n - 1;
            while (l <= h)
            {
                int mid = (l + h) / 2;
                if (arr.get(mid) <= y)
                    l = mid + 1;
                else
                    h = mid - 1;
            }
            return h;
        }

        // function to count elements within given range
        public int countInRange(List<Integer> arr, int n, int x, int y)
        {
            // initialize result
            int count = 0;
            count = upperIndex(arr, n, y) -
                    lowerIndex(arr, n, x) + 1;
            return count;
        }
        
        long[] dpl, dpr;
        List<Integer>[] adj;
        int[] l,r;
        public void solve(InputReader in, PrintWriter out) {
            int t= in.nextInt();
            while(t-->0){
                int n= in.nextInt();
                l= new int[n];
                r= new int[n];
                adj= new List[n];
                for(int i=0;i<n;i++){
                    l[i]= in.nextInt();
                    r[i]= in.nextInt();
                }
                for(int i=0;i<n;i++) adj[i]= new ArrayList<>();
                for(int i=0;i<n-1;i++){
                    int u= in.nextInt()-1, v= in.nextInt()-1;
                    adj[u].add(v); adj[v].add(u);
                }
                dpl= new long[n]; dpr= new long[n];
                dfs(0,-1);

                out.println(Math.max(dpl[0],dpr[0]));
                
            }
        }

        public void dfs(int u, int p){
            long suml=0, sumr=0;
            for(int v: adj[u] ){
                if(v==p) continue;
                dfs(v,u);
                suml+= Math.max((long)Math.abs((long)l[u]-(long)l[v])+dpl[v], (long)Math.abs((long)l[u]-(long)r[v])+dpr[v]);
                sumr+= Math.max((long)Math.abs((long)r[u]-(long)l[v])+dpl[v],(long) Math.abs((long)r[u]-(long)r[v])+dpr[v]);
            }
            dpl[u]= suml; dpr[u]= sumr;
        }

        // public static class compareL implements Comparator<Tuple>{
        //     @Override
        //     public int compare(Tuple t1, Tuple t2) {
        //         return t2.l - t1.l;
        //     }
        // }
        
        public int _gcd(int a,  int  b)
        {

            if(b == 0) {
                return a;
            }
            else {
                return _gcd(b, a % b);
            }
        }

    }

    // static class Data{
    //     List<Integer> arr;
    //     BigInteger prod=new BigInteger(-1);
    //     public Data(List<Integer> arr, BigInteger prod) {
    //         this.arr= arr;
    //         this.prod= prod;
    //     }
    // }

    static class Tuple implements Comparable<Tuple>{
        int x, y, z;
        public Tuple(int x, int y, int z){
            this.x= x;
            this.y= y;
            this.z=z;
        }
        @Override
        public int compareTo(Tuple o){
            return this.x-o.x;
        }
    }

    static class Pair implements Comparable<Pair>{
        public int x;
        public int y;
        public Pair(int x, int y){
            this.x= x;
            this.y= y;
        }

        @Override
        public int compareTo(Pair o) {
            return this.x-o.x;
        }
    }
    // fast input reader class;
    static class InputReader {
        BufferedReader br;
        StringTokenizer st;

        public InputReader(InputStream stream) {
            br = new BufferedReader(new InputStreamReader(stream));
        }

        public String nextToken() {
            while (st == null || !st.hasMoreTokens()) {
                String line = null;
                try {
                    line = br.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (line == null) {
                    return null;
                }
                st = new StringTokenizer(line);
            }
            return st.nextToken();
        }

        public int nextInt() {
            return Integer.parseInt(nextToken());
        }
        public double nextDouble(){
            return Double.parseDouble(nextToken());
        }
        public long nextLong(){
            return Long.parseLong(nextToken());
        }
    }
}